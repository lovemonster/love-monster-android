package org.ometa.lovemonster.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;
import org.ometa.lovemonster.Logger;
import org.ometa.lovemonster.models.Love;

import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.annotation.NotThreadSafe;

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
    }

    /**
     * The singleton instance for this client. Because of {@link AsyncHttpClient}, this instance
     * is *NOT* threadsafe.
     */
    private static final LoveMonsterClient singletonInstance = new LoveMonsterClient();

    /**
     * Logger used by this class.
     */
    private static final Logger logger = new Logger(LoveMonsterClient.class);

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
     * The parser used to convert responses from the server into model objects.
     */
    @NonNull
    private final ResponseParser responseParser;

    /**
     * The client used to make asynchronous http requests.
     */
    @NonNull
    private final AsyncHttpClient asyncHttpClient;

    /**
     * Private constructor used to implement the singleton. Purposely not made protected to avoid
     * breaking the singleton.
     * Defaults to use newly instantiated {@link ResponseParser} and {@link AsyncHttpClient} objects.
     */
    private LoveMonsterClient() {
            this(new ResponseParser(), new AsyncHttpClient());
    }

    /**
     * Protected constructor used to create an instance. Is protected scope to allow overriding and
     * easier unit testing.
     *
     * @param responseParser
     *      the response parser to use to parse requests
     * @param asyncHttpClient
     *      the http client used to make requests
     * @throws IllegalArgumentException
     *      if {@code responseParser} or {@code asyncHttpClient} are {@code null}
     */
    protected LoveMonsterClient(@NonNull final ResponseParser responseParser, @NonNull final AsyncHttpClient asyncHttpClient) throws IllegalArgumentException {
        if (responseParser == null) {
            throw new IllegalArgumentException("argument `responseParser` cannot be null");
        }
        if (asyncHttpClient == null) {
            throw new IllegalArgumentException("argument `asyncHttpClient` cannot be null");
        }

        this.responseParser = responseParser;
        this.asyncHttpClient = asyncHttpClient;
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
        retrieveRecentLoves(loveListResponseHandler, page, -1);
    }

    /**
     * Retrieves recent loves asynchronously. Takes a {@link LoveListResponseHandler} which will be
     * invoked on response completion.
     *
     * @param loveListResponseHandler
     *      the response handler to use on response completion
     * @param page
     *      the page of results to send
     * @param userId
     *      the id of the user to filter results on. passing any non-positive number will cause this value to be ignored
     */
    public void retrieveRecentLoves(@NonNull final LoveListResponseHandler loveListResponseHandler, final int page, final int userId) {
        final String url = buildUrl("api/v1/loves");

        final RequestParams params = new RequestParams();
        params.put("clientId", "androidapp");
        params.put("page", page);
        if (userId >= 0) {
            params.put("user_id", userId);
        }

        logger.debug("method=retrieveRecentLoves url=" + url + " params=" + params.toString());

        try {
            asyncHttpClient.get(url, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(final int statusCode, final Header[] headers, final JSONObject response) {
                    final String responseBody;
                    final int totalPages;

                    if (response == null) {
                        responseBody = "null";
                        totalPages = 0;
                    } else {
                        responseBody = response.toString();
                        totalPages = response.optInt("pages", 0);
                    }

                    logger.debug("method=retrieveRecentLoves url=" + url + " handler=onSuccess statusCode=" + statusCode + " response=" + responseBody);
                    loveListResponseHandler.onSuccess(responseParser.parseLoveList(response), totalPages);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    final String responseBody;
                    if (errorResponse == null) {
                        responseBody = "null";
                    } else {
                        responseBody = errorResponse.toString();
                    }

                    logger.debug("method=retrieveRecentLoves url=" + url + "  handler=onFailure statusCode=" + statusCode + " response=" + responseBody, throwable);
                    loveListResponseHandler.onFail();
                }
            });
        } catch (final Exception e){
            logger.debug("method=retrieveRecentLoves url=" + url, e);
            loveListResponseHandler.onFail();
        }
    }

    /**
     * Builds the full url from the specified path.
     *
     * @param path
     *      the path for the url
     * @return
     *      the full url
     */
    private String buildUrl(final String path) {
        return "http://love.snc1/" + path;
    }
}
