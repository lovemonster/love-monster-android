package org.ometa.lovemonster.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.MalformedJsonException;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ometa.lovemonster.Logger;
import org.ometa.lovemonster.models.Love;
import org.ometa.lovemonster.models.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.annotation.NotThreadSafe;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.message.BasicHeader;

/**
 * Client which makes requests to the Love Monster web service. For normal usage, this class is
 * intended to be used by using the singleton method {@link LoveMonsterClient#getInstance()} to
 * return a singleton instance. **N.B.** This singleton is not threadsafe due to dependencies on
 * {@link AsyncHttpClient}.
 *
 * {@code
 *      final LoveMonsterClient client = LoveMonsterClient.getInstance();
 *      client.retrieveRecentLoves(new LoveListResponseHandler() {... });
 * }
 *
 */
@NotThreadSafe
public class LoveMonsterClient {

    private static final String API_URL = "love-staging.groupondev.com";
    private static final String USER_AVATAR_TEMPLATE = "https://skynet.groupon.com/people/%s/avatar/40.png";

    /**
     * Handler for response callbacks from the {@link LoveMonsterClient} for calls which retrieve loves.
     */
    public interface LoveListResponseHandler {

        /**
         * Invoked when the request successfully completes.  The passed loves may be empty, but cannot
         * be null.
         *
         * @param loves
         *      the resulting loves
         */
        void onSuccess(@NonNull List<Love> loves, int totalPages);

        /**
         * Handler when a request fails.
         */
        void onFail();

        /**
         * Handler for when authentication has failed or the session has expired.
         */
        void onAuthenticationFailure();
    }

    /**
     * Handler for response callbacks from the {@link LoveMonsterClient} for calls which return a single love.
     */
    public interface LoveResponseHandler {

        /**
         * Invoked when the request successfully completes.  The passed love will not be null.
         *
         * @param love
         *      the resulting love
         */
        void onSuccess(@NonNull Love love);

        /**
         * Handler when a request fails.
         *
         * @param errorMessages
         *      the error messages from the server and/or exception messages
         */
        void onFail(@NonNull List<String> errorMessages);

        /**
         * Handler for when authentication has failed or the session has expired.
         */
        void onAuthenticationFailure();
    }

    public interface AuthenticationHandler {


        /**
         * Invoked when authentication succeeds.
         */
        void onSuccess();

        /**
         * Invoked when authentication fails.
         *
         * @param errorMessages
         *      the error messages from the server and/or exception messages
         */
        void onFail(List<String> errorMessages);

        /**
         * Handler for when authentication has failed or the session has expired.
         */
        void onAuthenticationFailure();
    }

    /**
     * The singleton instance for this client. Because of {@link AsyncHttpClient}, this instance
     * is *NOT* threadsafe.
     */
    private static final LoveMonsterClient singletonInstance = new LoveMonsterClient();


    /**
     * Returns the singleton {@code LoveMonsterClient} instance.  Because of {@link AsyncHttpClient}, this
     * instance is *NOT* threadsafe.
     *
     * @return
     *      the singleton {@code LoveMonsterClient}
     */
    public static LoveMonsterClient getInstance() {
        return singletonInstance;
    }

    /**
     * The host to use to make requests. This field *must* have a trailing slash.
     */
    private final String host;

    /**
     * The parser used to convert responses from the server into model objects.
     */
    @NonNull
    private final ResponseParser responseParser;

    /**
     * The remoter used to make http calls.
     */
    @NonNull
    private final HttpRemoter httpRemoter;

    /**
     * The authenticated authenticatedUser for the client. This may be null if no user has been
     * authenticated yet.
     */
    @Nullable
    private User authenticatedUser;

    /**
     * Private constructor used to implement the singleton. Purposely not made protected to avoid
     * breaking the singleton.
     * Defaults to use newly instantiated {@link ResponseParser} and {@link AsyncHttpClient} objects.
     */
    private LoveMonsterClient() {
            this(new ResponseParser(USER_AVATAR_TEMPLATE), new AsyncHttpClient(), API_URL);
    }

    /**
     * Protected constructor used to create an instance. Is protected scope to allow overriding and
     * easier unit testing.
     *
     * @param responseParser
     *      the response parser to use to parse requests
     * @param asyncHttpClient
     *      the http client used to make requests
     * @param host
     *      the host to make requests to.
     * @throws IllegalArgumentException
     *      if {@code responseParser}, {@code asyncHttpClient}, or @{code host} are {@code null}
     */
    protected LoveMonsterClient(@NonNull final ResponseParser responseParser, @NonNull final AsyncHttpClient asyncHttpClient, @NonNull final String host) throws IllegalArgumentException {
        if (responseParser == null) {
            throw new IllegalArgumentException("argument `responseParser` cannot be null");
        }
        if (asyncHttpClient == null) {
            throw new IllegalArgumentException("argument `asyncHttpClient` cannot be null");
        }
        if (host == null) {
            throw new IllegalArgumentException("argument `host` cannot be null");
        }

        this.responseParser = responseParser;
        this.httpRemoter = new HttpRemoter(asyncHttpClient);
        this.host = host;
    }

    /**
     * Retrieves recent loves asynchronously. Takes a {@link LoveListResponseHandler} which will be
     * invoked on response completion.
     *
     * @param loveListResponseHandler
     *      the response handler to use on response completion
     * @param page
     *      the page of results to send
     */
    public void retrieveRecentLoves(@NonNull final LoveListResponseHandler loveListResponseHandler, final int page) {
        retrieveRecentLoves(loveListResponseHandler, page, null);
    }

    /**
     * Retrieves recent loves asynchronously. Takes a {@link LoveListResponseHandler} which will be
     * invoked on response completion. Will filter results if {@code authenticatedUser} is specified, returning
     * *all* {@link Love}s sent to or sent by the specified authenticatedUser.
     *
     *  @param loveListResponseHandler
     *      the response handler to use on response completion
     * @param page
     *      the page of results to send
     * @param user
     *      the authenticatedUser to use for filtering results.  if null, no filtering will occur
     */
    public void retrieveRecentLoves(@NonNull final LoveListResponseHandler loveListResponseHandler, final int page, @Nullable final User user) {
        retrieveRecentLoves(loveListResponseHandler, page, user, null);
    }

    /**
     * Retrieves recent loves asynchronously. Takes a {@link LoveListResponseHandler} which will be
     * invoked on response completion. Will filter results if {@code authenticatedUser} is specified, returning
     * *all* {@link Love}s sent to or sent by the specified authenticatedUser, unless {@code userLoveAssociation}
     * is specified which will further constrain the results to loves *either* sent to or sent by the
     * specified authenticatedUser.
     *
     *  @param loveListResponseHandler
     *      the response handler to use on response completion
     * @param page
     *      the page of results to send
     * @param user
     *      the authenticatedUser to use for filtering results.  if null, no filtering will occur
     * @param userLoveAssociation
     *      used to indicate how results should be filtered for a authenticatedUser.  if null or
     *      {@link User.UserLoveAssociation#all}, then all loves sent or received to that
     *      authenticatedUser will be returned.  if {@link User.UserLoveAssociation#lover}, then only
     *      loves sent *by* the authenticatedUser (i.e. where the specified authenticatedUser is the lover) will be returned.
     *      likewise, if {@link User.UserLoveAssociation#lovee} is specified, then only
     *      loves *received* by the authenticatedUser (i.e. where the authenticatedUser is the lovee) will be returned.
     *      this value is only valid to be passed if {@code authenticatedUser} is also passed, but you may omit
     *      this value even if authenticatedUser is passed.
     * @throws IllegalArgumentException
     *      if {@code userLoveAssociation} is passed, but {@code authenticatedUser} is not passed
     */
    public void retrieveRecentLoves(@NonNull final LoveListResponseHandler loveListResponseHandler, final int page, @Nullable final User user, @Nullable final User.UserLoveAssociation userLoveAssociation) throws IllegalArgumentException {
        if (userLoveAssociation != null && user == null) {
            throw new IllegalArgumentException("cannot specify a `userLoveAssociation` without a `authenticatedUser`");
        }

        final URIBuilder url = buildUrl("/api/v1/loves");
        url.addParameter("page", Integer.toString(page));
        if (user != null) {
            url.addParameter("user_id", user.username);
            if (userLoveAssociation == User.UserLoveAssociation.lovee) {
                url.addParameter("filter", "to");
            } else if (userLoveAssociation == User.UserLoveAssociation.lover) {
                url.addParameter("filter", "from");
            }
        }

        httpRemoter.get(
                url,
                new JSONObjectHttpResponseHandler() {
                    @Override
                    void onSuccess(@Nullable final JSONObject response) {
                        int totalPages = 0;

                        if (response != null) {
                            final JSONObject metaJsonObject = response.optJSONObject("meta");
                            if (metaJsonObject != null) {
                                totalPages = metaJsonObject.optInt("total_pages", 0);
                            }
                        }

                        loveListResponseHandler.onSuccess(responseParser.parseLoveList(response), totalPages);
                    }

                    @Override
                    void onFailure(@NonNull final List<String> errorMessages) {
                        loveListResponseHandler.onFail();
                    }

                    @Override
                    public void onAuthenticationFailure() {
                        loveListResponseHandler.onAuthenticationFailure();
                    }
                }
        );
    }

    /**
     * Creates a new love on the server. If an error occurs, error messages will be passed to the
     * failure handler. Otherwise, the created love will be returned on the success handler.
     *
     * @param love
     *      the love to creeate
     * @param loveResponseHandler
     *      the handler for the response
     * @throws IllegalArgumentException
     *      if the specified love is null
     */
    public void makeLove(@NonNull final Love love, @NonNull final LoveResponseHandler loveResponseHandler) throws IllegalArgumentException {
        if (love == null) {
            throw new IllegalArgumentException("argument `love` cannot be null");
        }

        final URIBuilder url = buildUrl("/api/v1/loves");
        url.addParameter("reason", love.reason);
        url.addParameter("message", love.message);
        if (love.lovee != null) {
            url.addParameter("to", love.lovee.username);
        }
        if (love.lover != null) {
            url.addParameter("from", love.lover.username);
        }
        url.addParameter("private_message", Boolean.toString(love.isPrivate));

        httpRemoter.post(url, new JSONArrayHttpResponseHandler() {
            @Override
            void onSuccess(@Nullable final JSONArray response) {
                loveResponseHandler.onSuccess(love);
            }

            @Override
            void onFailure(@NonNull final List<String> errorMessages) {
                loveResponseHandler.onFail(errorMessages);
            }

            @Override
            void onAuthenticationFailure() {
                loveResponseHandler.onAuthenticationFailure();
            }
        });
    }

    /**
     * Returns the authenticated {@link User} for this client. This method will return {@code null}
     * if there is not yet an authenticated user.
     *
     * @return
     *      the authenticated user, or null if this client has not yet been authenticated
     */
    @Nullable
    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    /**
     * Returns the root url used to make requests. This includes the hostname with a trailing slash.
     *
     * @return
     *      the root url for requests
     */
    public String getRootUrl() {
        return buildUrl("/").toString();
    }

    /**
     * Authenticates against love monster, using the specified cookies for authentication.
     * @param cookies
     *      the authentication cookies
     * @throws IllegalArgumentException
     *      if cookies is {@code null}
     */
    public void authenticate(final String cookies, final AuthenticationHandler authenticationHandler) throws IllegalArgumentException {
        if (cookies == null) {
            throw new IllegalArgumentException("must specify non-null `cookies`");
        }

        httpRemoter.setCookies(cookies);
        httpRemoter.get(
                buildUrl("/api/v1/account"),
                new JSONObjectHttpResponseHandler() {
                    @Override
                    void onSuccess(@Nullable final JSONObject response) {
                        final User user = responseParser.parseUser(response);
                        authenticatedUser = user;
                        logger.debug("authenticatedAs=" + user.username);
                        authenticationHandler.onSuccess();
                    }

                    @Override
                    void onFailure(@NonNull final List<String> errorMessages) {
                        authenticationHandler.onFail(errorMessages);
                    }

                    @Override
                    void onAuthenticationFailure() {
                        authenticationHandler.onAuthenticationFailure();
                    }
                }
        );
    }

    /**
     * Creates a {@link URIBuilder} for the specified path.
     *
     * @param path
     *      the path for the url
     * @return
     *      a {@link URIBuilder} for the specified path
     */
    private URIBuilder buildUrl(final String path) {
        return new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(path)
                .addParameter("clientId", "androidapp");
    }

    private static class HttpRemoter {

        private static final Logger logger = new Logger(HttpRemoter.class);

        /**
         * The client used to make asynchronous http requests.
         */
        @NonNull
        private final AsyncHttpClient asyncHttpClient;

        private Header[] headers;

        HttpRemoter(@NonNull AsyncHttpClient asyncHttpClient) {
            this.asyncHttpClient = asyncHttpClient;
        }

        void setCookies(final String cookies) {
            headers = new Header[]{
                    new BasicHeader("Cookie", cookies),
                    new BasicHeader("Content-Type", "application/json"),
                    new BasicHeader("Accept", "*/*")
            };
        }

        void get(final URIBuilder url, final BaseHttpResponseHandler responseHandler) {
            logger.debug("httpMethod=get url=" + url);
            responseHandler.setUrl(url.toString());

            try {
                asyncHttpClient.get(null, url.toString(), headers, null, responseHandler);
            } catch (final Exception e){
                logger.debug("httpMethod=get url=" + url, e);
            }
        }

        void post(final URIBuilder url, final BaseHttpResponseHandler responseHandler) {
            logger.debug("httpMethod=post url=" + url);
            responseHandler.setUrl(url.toString());

            try {
                asyncHttpClient.post(null, url.toString(), headers, (RequestParams) null, "application/json", responseHandler);
            } catch (final Exception e){
                logger.debug("httpMethod=post url=" + url, e);
            }

        }
    }
    private static abstract class JSONObjectHttpResponseHandler extends BaseHttpResponseHandler<JSONObject> {
        public JSONObjectHttpResponseHandler() {
            super(JSONObject.class);
        }
    }

    private static abstract class JSONArrayHttpResponseHandler extends BaseHttpResponseHandler<JSONArray> {
        public JSONArrayHttpResponseHandler() {
            super(JSONArray.class);
        }
    }

    private static abstract class BaseHttpResponseHandler<T> extends JsonHttpResponseHandler {

        protected static final Logger logger = new Logger(BaseHttpResponseHandler.class);

        /**
         * The url the response is for. Generally used for logging purposes.
         */
        protected String url;

        /**
         * The expected response class. This is used to "swallow" responses which don't match the
         * expected type, and instead invoke the onfail handler.
         */
        private final Class<T> expectedResponseType;

        BaseHttpResponseHandler(final Class<T> expectedResponseType) {
            this.expectedResponseType = expectedResponseType;
        }

        void setUrl(String url) {
            this.url = url;
        }

        /**
         * Handler called on successful responses. The response may be a {@link String},
         * {@link JSONObject} or {@link JSONArray} object (or {@code null}); it is the responsibility
         * of the implementing class to handle the different object types accordingly.
         *
         * @param response
         *      the response from the server. may be null
         */
        abstract void onSuccess(@Nullable final T response);

        /**
         * Handler called on failed responses. The error messages may be empty, but will not be
         * {@code null}.
         *
         * @param errorMessages
         *      the error messages describing the failure. cannot be null
         */
        abstract void onFailure(@NonNull final List<String> errorMessages);

        /**
         * Handler called when authentication has failed. Typically, this should be used to fire the
         * login intent.
         */
        abstract void onAuthenticationFailure();

        @Override
        public void onSuccess(final int statusCode, final Header[] headers, final JSONArray response) {
            handleSuccess(statusCode, response);
        }

        @Override
        public void onSuccess(final int statusCode, final Header[] headers, final JSONObject response) {
            handleSuccess(statusCode, response);
        }

        @Override
        public void onSuccess(final int statusCode, final Header[] headers, final String responseString) {
            handleSuccess(statusCode, responseString);
        }

        /**
         * Centralized handler for successful responses.
         *
         * @param statusCode
         *      the status code for the response
         * @param responseObject
         *      the response from the server
         */
        private void handleSuccess(final int statusCode, final Object responseObject) {
            final String response;
            if (responseObject == null) {
                response = "<null>";
            } else {
                response = responseObject.toString();
            }

            logger.debug("url=" + url + " handler=onSuccess statusCode=" + statusCode + " response=" + response);

            if (response != null && expectedResponseType.isAssignableFrom(responseObject.getClass())) {
                onSuccess((T) responseObject);
            } else {
                final String typeError;

                if (response == null) {
                    typeError = "expectedType=" + expectedResponseType.getName() + " actualType=<null>";
                } else {
                    typeError = "expectedType=" + expectedResponseType.getName() + " actualType=" + responseObject.getClass().getName();
                }

                logger.debug("url=" + url + " " + typeError);

                onFailure(Arrays.asList(typeError));
            }
        }

        @Override
        public void onFailure(final int statusCode, final Header[] headers, final Throwable throwable, final JSONObject errorResponse) {
            handleFailure(statusCode, errorResponse, throwable);
        }

        @Override
        public void onFailure(final int statusCode, final Header[] headers, final String responseString, final Throwable throwable) {
            handleFailure(statusCode, responseString, throwable);
        }

        @Override
        public void onFailure(final int statusCode, final Header[] headers, final Throwable throwable, final JSONArray errorResponse) {
            handleFailure(statusCode, errorResponse, throwable);
        }

        /**
         * Centralized handler for failed responses.
         *
         * @param statusCode
         *      the status code for the response
         * @param responseObject
         *      the response from the server. may be null
         * @param throwable
         *      the thrown error. may be null
         */
        private void handleFailure(final int statusCode, @Nullable final Object responseObject, @Nullable final Throwable throwable) {
            if (isExpiredOktaCredentials(statusCode, throwable)) {
                onAuthenticationFailure();
                return;
            }
            final String response;
            final List<String> errorMessages = new ArrayList<>();

            if (responseObject == null) {
                response = "<null>";
            } else {
                response = responseObject.toString();
                if (responseObject instanceof JSONObject) {
                    final String errors = ((JSONObject)responseObject).optString("errors", null);
                    if (errors != null) {
                        errorMessages.add(errors);
                    }
                }
            }

            if (throwable != null) {
                errorMessages.add(throwable.getLocalizedMessage());
            }

            logger.debug(
                    "url=" + url + " handler=onFailure statusCode=" + statusCode + " response=" + response,
                    throwable
            );

            onFailure(errorMessages);
        }

        /**
         * Returns true if the response represents expired okta credentials.
         *
         * @param statusCode
         *      the status code from the response
         * @param throwable
         *      the exception thrown
         * @return
         *      true if the failure represents expired okta credentials, or false otherwise
         */
        private boolean isExpiredOktaCredentials(final int statusCode, final Throwable throwable) {
            return statusCode == 200 && throwable instanceof MalformedJsonException;
        }
    }
}
