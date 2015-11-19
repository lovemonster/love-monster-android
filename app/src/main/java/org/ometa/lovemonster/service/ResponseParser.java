package org.ometa.lovemonster.service;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ometa.lovemonster.models.Love;
import org.ometa.lovemonster.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses responses from the server into model objects.
 */
class ResponseParser {

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

        final String reason = loveJson.optString("reason", null);
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

        final Love love = new Love(reason, lover, lovee);

        love.message = loveJson.optString("message", null);
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
    private User parseUser(@Nullable final JSONObject userJson) {
        if (userJson == null) {
            return null;
        }

        final String email = userJson.optString("email", null);
        if (email == null) {
            return null;
        }

        final String username = userJson.optString("username", null);
        if (username == null) {
            return null;
        }

        final User user = new User(email, username);

        user.name = userJson.optString("name", null);

        return user;
    }

}
