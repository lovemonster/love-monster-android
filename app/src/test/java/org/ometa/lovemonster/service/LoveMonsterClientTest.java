package org.ometa.lovemonster.service;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ometa.lovemonster.models.Love;
import org.ometa.lovemonster.models.User;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoveMonsterClientTest {

    @Mock
    AsyncHttpClient mockAsyncHttpClient;
    @Mock
    ResponseParser mockResponseParser;
    @Mock
    LoveMonsterClient.LoveListResponseHandler mockLoveListResponseHandler;
    @Mock
    LoveMonsterClient.LoveResponseHandler mockLoveResponseHandler;

    LoveMonsterClient client;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        client = new LoveMonsterClient(mockResponseParser, mockAsyncHttpClient, "example.com");
    }

    private NameValuePair param(final String name, final String value) {
        return new BasicNameValuePair(name, value);
    }
    /**
     * Validates that the expected url and params match the actual url and parameters passed.
     *
     * @param url
     *      the url path
     * @param params
     *      the url parameters
     * @return
     *      an argument matcher which validates the url path and params
     */
    private String urlWithParams(final String url, final NameValuePair... params) {
        final URIBuilder expectedUri;

        try {
            expectedUri= new URIBuilder(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        expectedUri.addParameters(Arrays.asList(params));

        return argThat(new ArgumentMatcher<String>() {
            final Description description = new StringDescription();

            @Override
            public boolean matches(final Object argument) {
                if (!(argument instanceof String)) {
                    final String argumentClass;
                    if (argument == null) {
                        argumentClass = "<null>";
                    } else {
                        argumentClass = argument.getClass().getName();
                    }
                    description.appendText("expected class: String\nactual class: ").appendValue(argumentClass);
                    return false;
                }

                final URIBuilder actualUri;
                try {
                    actualUri= new URIBuilder((String) argument);
                } catch (final URISyntaxException e) {
                    throw new RuntimeException(e);
                }

                description.appendText("expected url: ").appendValue(expectedUri.toString());
                description.appendText("\nactual url: ").appendValue(actualUri.toString());
                description.appendText("\n");

               return expectedUri.toString().equals(actualUri.toString());
            }

            @Override
            public void describeTo(Description description) {
                super.describeTo(description);
                description.appendText(this.description.toString());
            }

            @Override
            public void describeMismatch(final Object item, final Description description) {
                super.describeMismatch(item, description);
                description.appendText(this.description.toString());
            }
        });
    }
    @Test
    public void testRetrieveRecentLoves_User_NullUserAssociation_CreatesCorrectRequest() {
        client.retrieveRecentLoves(mockLoveListResponseHandler, 77, new User("foo@example.com", "example_username"), null);

        verify(mockAsyncHttpClient).get(
                any(Context.class),
                urlWithParams(
                        "https://example.com/api/v1/loves",
                        param("clientId", "androidapp"),
                        param("page", "77"),
                        param("user_id", "example_username")
                ),
                any(Header[].class),
                isNull(RequestParams.class),
                any(JsonHttpResponseHandler.class)
        );
    }

    @Test
    public void testRetrieveRecentLoves_User_AllUserAssociation_CreatesCorrectRequest() {
        client.retrieveRecentLoves(mockLoveListResponseHandler, 77, new User("foo@example.com", "example_username"), User.UserLoveAssociation.all);

        verify(mockAsyncHttpClient).get(
                any(Context.class),
                urlWithParams(
                        "https://example.com/api/v1/loves",
                        param("clientId", "androidapp"),
                        param("page", "77"),
                        param("user_id", "example_username")
                ),
                any(Header[].class),
                isNull(RequestParams.class),
                any(JsonHttpResponseHandler.class)
        );
    }

    @Test
    public void testRetrieveRecentLoves_User_LoverUserAssociation_CreatesCorrectRequest() {
        client.retrieveRecentLoves(mockLoveListResponseHandler, 77, new User("foo@example.com", "example_username"), User.UserLoveAssociation.lover);

        verify(mockAsyncHttpClient).get(
                any(Context.class),
                urlWithParams(
                        "https://example.com/api/v1/loves",
                        param("clientId", "androidapp"),
                        param("page", "77"),
                        param("user_id", "example_username"),
                        param("filter", "from")
                ),
                any(Header[].class),
                isNull(RequestParams.class),
                any(JsonHttpResponseHandler.class)
        );
    }

    @Test
    public void testRetrieveRecentLoves_User_LoveeUserAssociation_CreatesCorrectRequest() {
        client.retrieveRecentLoves(mockLoveListResponseHandler, 77, new User("foo@example.com", "example_username"), User.UserLoveAssociation.lovee);

        verify(mockAsyncHttpClient).get(
                any(Context.class),
                urlWithParams(
                        "https://example.com/api/v1/loves",
                        param("clientId", "androidapp"),
                        param("page", "77"),
                        param("user_id", "example_username"),
                        param("filter", "to")
                ),
                any(Header[].class),
                isNull(RequestParams.class),
                any(JsonHttpResponseHandler.class)
        );
    }

    @Test
    public void testRetrieveRecentLoves_User_NoUserLoveAssociation_CreatesCorrectRequest() {
        client.retrieveRecentLoves(mockLoveListResponseHandler, 77, new User("foo@example.com", "example_username"));

        verify(mockAsyncHttpClient).get(
                any(Context.class),
                urlWithParams(
                        "https://example.com/api/v1/loves",
                        param("clientId", "androidapp"),
                        param("page", "77"),
                        param("user_id", "example_username")
                ),
                any(Header[].class),
                isNull(RequestParams.class),
                any(JsonHttpResponseHandler.class)
        );
    }

    @Test
    public void testRetrieveRecentLoves_NoUser_CreatesCorrectRequest() {
        client.retrieveRecentLoves(mockLoveListResponseHandler, 77);

        verify(mockAsyncHttpClient).get(
                any(Context.class),
                urlWithParams(
                        "https://example.com/api/v1/loves",
                        param("clientId", "androidapp"),
                        param("page", "77")
                ),
                any(Header[].class),
                isNull(RequestParams.class),
                any(JsonHttpResponseHandler.class)
        );
    }

    public void testRetrieveRecentLoves_AsyncHttpThrowsException_InvokesOnFail() {
        doThrow(new RuntimeException()).when(mockAsyncHttpClient).get(any(Context.class), anyString(), any(Header[].class), any(RequestParams.class), any(JsonHttpResponseHandler.class));

        client.retrieveRecentLoves(mockLoveListResponseHandler, 1);

        verify(mockLoveListResponseHandler).onFail();
    }

    @Test
    public void testRetrieveRecentLoves_RequestFails_InvokesOnFail() {
        final ArgumentCaptor<JsonHttpResponseHandler> jsonHttpResponseHandlerArgumentCaptor = ArgumentCaptor.forClass(JsonHttpResponseHandler.class);

        client.retrieveRecentLoves(mockLoveListResponseHandler, 1);
        verify(mockAsyncHttpClient).get(any(Context.class), anyString(), any(Header[].class), any(RequestParams.class), jsonHttpResponseHandlerArgumentCaptor.capture());
        jsonHttpResponseHandlerArgumentCaptor.getValue().onFailure(500, null, null, new JSONObject());

        verify(mockLoveListResponseHandler).onFail();
    }

    @Test
    public void testRetrieveRecentLoves_RequestSucceeds_InvokesOnSuccess() throws JSONException {
        final ArgumentCaptor<JsonHttpResponseHandler> jsonHttpResponseHandlerArgumentCaptor = ArgumentCaptor.forClass(JsonHttpResponseHandler.class);
        final List<Love> expectedLoves = new ArrayList<>();
        final JSONObject expectedJsonObject = new JSONObject("{\"meta\":{\"total_pages\":7}}");
        when(mockResponseParser.parseLoveList(expectedJsonObject)).thenReturn(expectedLoves);

        client.retrieveRecentLoves(mockLoveListResponseHandler, 1);
        verify(mockAsyncHttpClient).get(any(Context.class), anyString(), any(Header[].class), any(RequestParams.class), jsonHttpResponseHandlerArgumentCaptor.capture());
        jsonHttpResponseHandlerArgumentCaptor.getValue().onSuccess(200, null, expectedJsonObject);

        verify(mockLoveListResponseHandler).onSuccess(expectedLoves, 7);
    }

    @Test
    public void testRetrieveRecentLoves_RequestSucceeds_MissingMeta_InvokesOnSuccessWithDefaultPages() throws JSONException {
        final ArgumentCaptor<JsonHttpResponseHandler> jsonHttpResponseHandlerArgumentCaptor = ArgumentCaptor.forClass(JsonHttpResponseHandler.class);
        final List<Love> expectedLoves = new ArrayList<>();
        final JSONObject expectedJsonObject = new JSONObject("{}");
        when(mockResponseParser.parseLoveList(expectedJsonObject)).thenReturn(expectedLoves);

        client.retrieveRecentLoves(mockLoveListResponseHandler, 1);
        verify(mockAsyncHttpClient).get(any(Context.class), anyString(), any(Header[].class), any(RequestParams.class), jsonHttpResponseHandlerArgumentCaptor.capture());
        jsonHttpResponseHandlerArgumentCaptor.getValue().onSuccess(200, null, expectedJsonObject);

        verify(mockLoveListResponseHandler).onSuccess(expectedLoves, 0);
    }

    @Test
    public void testRetrieveRecentLoves_RequestSucceeds_MissingPages_InvokesOnSuccessWithDefaultPages() throws JSONException {
        final ArgumentCaptor<JsonHttpResponseHandler> jsonHttpResponseHandlerArgumentCaptor = ArgumentCaptor.forClass(JsonHttpResponseHandler.class);
        final List<Love> expectedLoves = new ArrayList<>();
        final JSONObject expectedJsonObject = new JSONObject("{\"meta\":{}}");
        when(mockResponseParser.parseLoveList(expectedJsonObject)).thenReturn(expectedLoves);

        client.retrieveRecentLoves(mockLoveListResponseHandler, 1);
        verify(mockAsyncHttpClient).get(any(Context.class), anyString(), any(Header[].class), any(RequestParams.class), jsonHttpResponseHandlerArgumentCaptor.capture());
        jsonHttpResponseHandlerArgumentCaptor.getValue().onSuccess(200, null, expectedJsonObject);

        verify(mockLoveListResponseHandler).onSuccess(expectedLoves, 0);
    }

    public void testRetrieveRecentLoves_RequestSucceeds_NullResponse_InvokesOnSuccessWithDefaultPages() {
        final ArgumentCaptor<JsonHttpResponseHandler> jsonHttpResponseHandlerArgumentCaptor = ArgumentCaptor.forClass(JsonHttpResponseHandler.class);
        final List<Love> expectedLoves = new ArrayList<>();

        client.retrieveRecentLoves(mockLoveListResponseHandler, 1);
        verify(mockAsyncHttpClient).get(any(Context.class), anyString(), any(Header[].class), isNull(RequestParams.class), jsonHttpResponseHandlerArgumentCaptor.capture());
        jsonHttpResponseHandlerArgumentCaptor.getValue().onSuccess(200, null, (JSONObject) null);

        verify(mockLoveListResponseHandler).onSuccess(expectedLoves, 0);
    }

    @Test
    public void testMakeLove_CreatesCorrectRequest() {
        final Love love = new Love("the reason", new User("lover@example.com", "lover"), new User("lovee@example.com", "lovee"));
        love.isPrivate = true;
        love.message = "the message";

        client.makeLove(love, mockLoveResponseHandler);

        verify(mockAsyncHttpClient).post(
                any(Context.class),
                urlWithParams(
                        "https://example.com/api/v1/loves",
                        param("clientId", "androidapp"),
                        param("reason", "the reason"),
                        param("message", "the message"),
                        param("to", "lovee"),
                        param("from", "lover"),
                        param("private_message", "true")
                ),
                any(Header[].class),
                isNull(RequestParams.class),
                anyString(),
                any(JsonHttpResponseHandler.class)
        );
    }
    
    public void testMakeLove_AsyncHttpThrowsException_InvokesOnFail() {
        doThrow(new RuntimeException("some exception")).when(mockAsyncHttpClient).post(any(Context.class), anyString(), any(Header[].class), any(RequestParams.class), eq("application/json"), any(JsonHttpResponseHandler.class));

        client.makeLove(Mockito.mock(Love.class), mockLoveResponseHandler);

        verify(mockLoveResponseHandler).onFail(Arrays.asList("some exception"));
    }

    @Test
    public void testMakeLove_RequestFails_InvokesOnFailWithPassedErrors() throws JSONException {
        final ArgumentCaptor<JsonHttpResponseHandler> jsonHttpResponseHandlerArgumentCaptor = ArgumentCaptor.forClass(JsonHttpResponseHandler.class);

        client.makeLove(Mockito.mock(Love.class), mockLoveResponseHandler);
        verify(mockAsyncHttpClient).post(any(Context.class), anyString(), any(Header[].class), any(RequestParams.class), eq("application/json"), jsonHttpResponseHandlerArgumentCaptor.capture());
        jsonHttpResponseHandlerArgumentCaptor.getValue().onFailure(400, null, new Throwable("throwable message"), new JSONObject("{\"errors\": \"error message from server\"}"));

        verify(mockLoveResponseHandler).onFail(Arrays.asList("error message from server", "throwable message"));
    }

    @Test
    public void testMakeLove_RequestFails_NoResponseBody_InvokesOnFailWithNoErrors() {
        final ArgumentCaptor<JsonHttpResponseHandler> jsonHttpResponseHandlerArgumentCaptor = ArgumentCaptor.forClass(JsonHttpResponseHandler.class);

        client.makeLove(Mockito.mock(Love.class), mockLoveResponseHandler);
        verify(mockAsyncHttpClient).post(any(Context.class), anyString(), any(Header[].class), any(RequestParams.class), eq("application/json"), jsonHttpResponseHandlerArgumentCaptor.capture());
        jsonHttpResponseHandlerArgumentCaptor.getValue().onFailure(400, null, null, new JSONObject());

        verify(mockLoveResponseHandler).onFail(new ArrayList<String>());
    }

    public void testMakeLove_RequestSucceeds_InvokesOnSuccessWithPassedLove() throws JSONException {
        final ArgumentCaptor<JsonHttpResponseHandler> jsonHttpResponseHandlerArgumentCaptor = ArgumentCaptor.forClass(JsonHttpResponseHandler.class);
        final Love expectedLove = Mockito.mock(Love.class);

        client.makeLove(expectedLove, mockLoveResponseHandler);
        verify(mockAsyncHttpClient).post(any(Context.class), anyString(), any(Header[].class), isNull(RequestParams.class), eq("application/json"), jsonHttpResponseHandlerArgumentCaptor.capture());
        jsonHttpResponseHandlerArgumentCaptor.getValue().onSuccess(200, null, new JSONObject("{\"result\":\"OK\"}"));

        verify(mockLoveResponseHandler).onSuccess(expectedLove);
    }

    /**
     * Parses the request params and returns a map of <param, value>.
     * This is to work around an issue with RequestParams not exposing the parameters in an
     * order-independent way.
     *
     * @param params
     *      the params to parse
     * @return
     *      the map of param name to value
     */
    private Map<String, String> parseParams(final RequestParams params) {
        final String paramString = params.toString();
        final Map<String, String> parsedParams = new HashMap<>();

        for (final String keyValuePair : paramString.split("&")) {
            final String[] splitKeyValuePair = keyValuePair.split("=");
            parsedParams.put(splitKeyValuePair[0], splitKeyValuePair[1]);
        }

        return parsedParams;
    }
}
