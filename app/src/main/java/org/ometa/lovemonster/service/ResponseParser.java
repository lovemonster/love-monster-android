package org.ometa.lovemonster.service;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ometa.lovemonster.models.Love;
import org.ometa.lovemonster.models.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import cz.msebera.android.httpclient.annotation.NotThreadSafe;
import cz.msebera.android.httpclient.client.utils.DateUtils;

/**
 * Parses responses from the server into model objects.
 */
class ResponseParser {

    /**
     * The date format for iso8601.
     *
     * **N.B.**
     *      Due to a bug in java, the date format doesn't handle "Z" as a timezone offset.
     *      Instead of implementing complex logic around this, simply ignore the timezone and force
     *      it to UTC.
     */
    private static final String[] ISO8601 = new String[]{"yyyy-MM-dd'T'HH:mm:ss"};

    /**
     * UTC timezone.
     */
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    /**
     * Internal cache used to reuse {@link User} objects to reduce memory overhead and reduce parse time.
     * **N.B.** This is not made synchronized to help with performance, since it is unlikely to be
     * accessed by multiple threads concurrently.
     */
    private static final Map<String, User> userCache = new EntityCache<>(25);


    private final boolean useUserCache;

    /**
     * The format to use when generating user profile image urls.
     */
    private final String userProfileImageUrlFormat;

    ResponseParser(final String userProfileImageUrlFormat) {
        this(true, userProfileImageUrlFormat);
    }

    ResponseParser(final boolean useUserCache, final String userProfileImageUrlFormat) {
        this.useUserCache = useUserCache;
        this.userProfileImageUrlFormat = userProfileImageUrlFormat;
    }

    /**
     * Parses a list of {@link Love} objects from a json response payload. May return an empty list,
     * but will not return null. Handles null inputs as well as malformed json.
     * If an element cannot be parsed, it will be excluded from the returned list.
     *
     * @param responseJsonObject
     *      the json response object from the server
     * @return
     *      a list of parsed {@link Love} objects
     *
     */
    List<Love> parseLoveList(@Nullable final JSONObject responseJsonObject) {
        final ArrayList<Love> loves = new ArrayList<>();

        if (responseJsonObject != null) {
            final JSONArray lovesJsonArray = responseJsonObject.optJSONArray("data");
            if (lovesJsonArray != null) {
                for (int loveIndex = 0; loveIndex < lovesJsonArray.length(); loveIndex++) {
                    final Love love = parseLove(lovesJsonArray.optJSONObject(loveIndex));
                    if (love != null) {
                        loves.add(love);
                    }
                }
            }
        }

        return loves;
    }

    /**
     * Parses a {@link Love} object from the passed json. If any of the required fields cannot be parsed,
     * this method will return null. If any non-required fields cannot be parsed, then that field will
     * be set to null. Null-safe (will return null).
     *
     * @param loveJson
     *      the json object to parse
     * @return
     *      the parsed {@link Love}, or null if it cannot be parsed
     */
    private Love parseLove(@Nullable final JSONObject loveJson) {
        if (loveJson == null) {
            return null;
        }

        final String reason = parseString(loveJson, "reason");
        if (reason == null) {
            return null;
        }

        final User lover = parseUser(loveJson.optJSONObject("user_from"));
        if (lover == null) {
            return null;
        }

        final User lovee = parseUser(loveJson.optJSONObject("user_to"));
        if (lovee == null) {
            return null;
        }

        final Calendar createdAt = parseDatetime(loveJson.optString("created_at", null));
        if (createdAt == null) {
            return null;
        }

        final Love love = new Love(reason, lover, lovee, createdAt);

        love.message = parseString(loveJson, "message");
        love.isPrivate = loveJson.optBoolean("private_message", false);

        return love;
    }

    /**
     * Parses a {@link User} object from the passed json. If any of the required fields cannot be parsed,
     * this method will return null. If any non-required fields cannot be parsed, then that field will
     * be set to null. Null-safe (will return null).
     *
     * @param userJson
     *      the json object to parse
     * @return
     *      the parsed {@link User}, or null if it cannot be parsed
     */
    User parseUser(@Nullable final JSONObject userJson) {
        if (userJson == null) {
            return null;
        }

        final String email = parseString(userJson, "email");
        if (email == null) {
            return null;
        }

        User user;

        if (useUserCache) {
            user = userCache.get(email);
            if (user != null) {
                return user;
            }
        }

        final String username = parseString(userJson, "username");
        if (username == null) {
            return null;
        }

        user = new User(email, username);

        user.name = parseString(userJson, "name");
        user.profileImageUrl = String.format(userProfileImageUrlFormat, username);

        userCache.put(email, user);

        return user;
    }

    /**
     * Parses a datetime string into a {@link Calendar} object. Will return null if the date cannot
     * be parsed. Null-safe (will return null).
     *
     * @param datetime
     *      the datetime to parse
     * @return
     *      the parsed {@link Calendar}, or null if it cannot be parsed.
     */
    private Calendar parseDatetime(@Nullable final String datetime) {
        if (datetime == null) {
            return null;
        }

        final Date parsedDate = DateUtils.parseDate(datetime, ISO8601);
        if (parsedDate == null) {
            return null;
        }

        final Calendar calendar = Calendar.getInstance();

        calendar.clear();
        calendar.setTimeZone(UTC);
        calendar.setTime(parsedDate);

        return calendar;
    }

    /**
     * Parses the string value from the json object. If the string is a blank value (either no
     * characters or just whitespace), null will be returned. Null safe (will return null).
     *
     * @param jsonObject
     *      the json object to get the string value from
     * @param attributeName
     *      the name of the attribute on the json object
     * @return
     *      the non-blank value, or null otherwise
     */
    private String parseString(final JSONObject jsonObject, final String attributeName) {
        // JSONObject will helpfully coerce null to "null" for us.  We don't want that.
        // http://code.google.com/p/android/issues/detail?id=13830
        if (jsonObject.isNull(attributeName)) {
            return null;
        }

        final String value = jsonObject.optString(attributeName, null);

        if (value == null || value.replaceAll("\\s", "").equals("")) {
            return null;
        }

        return value;
    }

    /**
     * Provides lightweight caching of entities and automatically evicts the least recently used
     * entity once the cache size is exceeded.
     *
     * This class itself is not threadsafe, and is intended to be wrapped in
     * {@link java.util.Collections#synchronizedMap(Map)} if it will be modified by multiple threads.
     *
     * @param <T>
     *     the entity type
     */
    @NotThreadSafe
    private static class EntityCache<T> extends LinkedHashMap<String, T> {
        private final int maxCacheSize;

        public EntityCache(final int maxCacheSize) {
            super(16, 0.75f, true);
            this.maxCacheSize = maxCacheSize;
        }

        protected boolean removeEldestEntry(final Map.Entry<String, T> eldest) {
            return size() >= maxCacheSize;
        }
    }
}
