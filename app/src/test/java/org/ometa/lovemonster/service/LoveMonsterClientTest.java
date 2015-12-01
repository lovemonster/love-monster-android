package org.ometa.lovemonster.service;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ometa.lovemonster.models.Love;
import org.ometa.lovemonster.models.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
        client = new LoveMonsterClient(mockResponseParser, mockAsyncHttpClient, "http://example.com/");
    }

    @Test
    public void testRetrieveRecentLoves_User_NullUserAssociation_CreatesCorrectRequest() {
        final ArgumentCaptor<RequestParams> requestParamsCaptor = ArgumentCaptor.forClass(RequestParams.class);

        client.retrieveRecentLoves(mockLoveListResponseHandler, 77, new User("foo@example.com", "example_username"), null);

        verify(mockAsyncHttpClient).get(eq("http://example.com/api/v1/loves"), requestParamsCaptor.capture(), any(JsonHttpResponseHandler.class));

        final Map<String, String> requestParams = parseParams(requestParamsCaptor.getValue());
        assertEquals("should set client id param", "androidapp", requestParams.get("clientId"));
        assertEquals("should set page param", "77", requestParams.get("page"));
        assertEquals("should set user_id param", "example_username", requestParams.get("user_id"));
        assertFalse("should not set filter param", requestParams.containsKey("filter"));
    }

    @Test
    public void testRetrieveRecentLoves_User_AllUserAssociation_CreatesCorrectRequest() {
        final ArgumentCaptor<RequestParams> requestParamsCaptor = ArgumentCaptor.forClass(RequestParams.class);

        client.retrieveRecentLoves(mockLoveListResponseHandler, 77, new User("foo@example.com", "example_username"), User.UserLoveAssociation.all);

        verify(mockAsyncHttpClient).get(eq("http://example.com/api/v1/loves"), requestParamsCaptor.capture(), any(JsonHttpResponseHandler.class));

        final Map<String, String> requestParams = parseParams(requestParamsCaptor.getValue());
        assertEquals("should set client id param", "androidapp", requestParams.get("clientId"));
        assertEquals("should set page param", "77", requestParams.get("page"));
        assertEquals("should set user_id param", "example_username", requestParams.get("user_id"));
        assertFalse("should not set filter param", requestParams.containsKey("filter"));
    }

    @Test
    public void testRetrieveRecentLoves_User_LoverUserAssociation_CreatesCorrectRequest() {
        final ArgumentCaptor<RequestParams> requestParamsCaptor = ArgumentCaptor.forClass(RequestParams.class);

        client.retrieveRecentLoves(mockLoveListResponseHandler, 77, new User("foo@example.com", "example_username"), User.UserLoveAssociation.lover);

        verify(mockAsyncHttpClient).get(eq("http://example.com/api/v1/loves"), requestParamsCaptor.capture(), any(JsonHttpResponseHandler.class));

        final Map<String, String> requestParams = parseParams(requestParamsCaptor.getValue());
        assertEquals("should set client id param", "androidapp", requestParams.get("clientId"));
        assertEquals("should set page param", "77", requestParams.get("page"));
        assertEquals("should set user_id param", "example_username", requestParams.get("user_id"));
        assertEquals("should set filter param", "from", requestParams.get("filter"));
    }

    @Test
    public void testRetrieveRecentLoves_User_LoveeUserAssociation_CreatesCorrectRequest() {
        final ArgumentCaptor<RequestParams> requestParamsCaptor = ArgumentCaptor.forClass(RequestParams.class);

        client.retrieveRecentLoves(mockLoveListResponseHandler, 77, new User("foo@example.com", "example_username"), User.UserLoveAssociation.lovee);

        verify(mockAsyncHttpClient).get(eq("http://example.com/api/v1/loves"), requestParamsCaptor.capture(), any(JsonHttpResponseHandler.class));

        final Map<String, String> requestParams = parseParams(requestParamsCaptor.getValue());
        assertEquals("should set client id param", "androidapp", requestParams.get("clientId"));
        assertEquals("should set page param", "77", requestParams.get("page"));
        assertEquals("should set user_id param", "example_username", requestParams.get("user_id"));
        assertEquals("should set filter param", "to", requestParams.get("filter"));
    }

    @Test
    public void testRetrieveRecentLoves_User_NoUserLoveAssociation_CreatesCorrectRequest() {
        final ArgumentCaptor<RequestParams> requestParamsCaptor = ArgumentCaptor.forClass(RequestParams.class);

        client.retrieveRecentLoves(mockLoveListResponseHandler, 77, new User("foo@example.com", "example_username"));

        verify(mockAsyncHttpClient).get(eq("http://example.com/api/v1/loves"), requestParamsCaptor.capture(), any(JsonHttpResponseHandler.class));

        final Map<String, String> requestParams = parseParams(requestParamsCaptor.getValue());
        assertEquals("should set client id param", "androidapp", requestParams.get("clientId"));
        assertEquals("should set page param", "77", requestParams.get("page"));
        assertEquals("should set user_id param", "example_username", requestParams.get("user_id"));
        assertFalse("should not set filter param", requestParams.containsKey("filter"));
    }

    @Test
    public void testRetrieveRecentLoves_NoUser_CreatesCorrectRequest() {
        final ArgumentCaptor<RequestParams> requestParamsCaptor = ArgumentCaptor.forClass(RequestParams.class);

        client.retrieveRecentLoves(mockLoveListResponseHandler, 77);

        verify(mockAsyncHttpClient).get(eq("http://example.com/api/v1/loves"), requestParamsCaptor.capture(), any(JsonHttpResponseHandler.class));

        final Map<String, String> requestParams = parseParams(requestParamsCaptor.getValue());
        assertEquals("should set client id param", "androidapp", requestParams.get("clientId"));
        assertEquals("should set page param", "77", requestParams.get("page"));
        assertFalse("should not set user_id param", requestParams.containsKey("user_id"));
    }

    @Test
    public void testRetrieveRecentLoves_AsyncHttpThrowsException_InvokesOnFail() {
        doThrow(new RuntimeException()).when(mockAsyncHttpClient).get(anyString(), any(RequestParams.class), any(JsonHttpResponseHandler.class));

        client.retrieveRecentLoves(mockLoveListResponseHandler, 1);

        verify(mockLoveListResponseHandler).onFail();
    }

    @Test
    public void testRetrieveRecentLoves_RequestFails_InvokesOnFail() {
        final ArgumentCaptor<JsonHttpResponseHandler> jsonHttpResponseHandlerArgumentCaptor = ArgumentCaptor.forClass(JsonHttpResponseHandler.class);

        client.retrieveRecentLoves(mockLoveListResponseHandler, 1);
        verify(mockAsyncHttpClient).get(anyString(), any(RequestParams.class), jsonHttpResponseHandlerArgumentCaptor.capture());
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
        verify(mockAsyncHttpClient).get(anyString(), any(RequestParams.class), jsonHttpResponseHandlerArgumentCaptor.capture());
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
        verify(mockAsyncHttpClient).get(anyString(), any(RequestParams.class), jsonHttpResponseHandlerArgumentCaptor.capture());
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
        verify(mockAsyncHttpClient).get(anyString(), any(RequestParams.class), jsonHttpResponseHandlerArgumentCaptor.capture());
        jsonHttpResponseHandlerArgumentCaptor.getValue().onSuccess(200, null, expectedJsonObject);

        verify(mockLoveListResponseHandler).onSuccess(expectedLoves, 0);
    }

    @Test
    public void testRetrieveRecentLoves_RequestSucceeds_NullResponse_InvokesOnSuccessWithDefaultPages() {
        final ArgumentCaptor<JsonHttpResponseHandler> jsonHttpResponseHandlerArgumentCaptor = ArgumentCaptor.forClass(JsonHttpResponseHandler.class);
        final List<Love> expectedLoves = new ArrayList<>();

        client.retrieveRecentLoves(mockLoveListResponseHandler, 1);
        verify(mockAsyncHttpClient).get(anyString(), any(RequestParams.class), jsonHttpResponseHandlerArgumentCaptor.capture());
        jsonHttpResponseHandlerArgumentCaptor.getValue().onSuccess(200, null, (JSONObject) null);

        verify(mockLoveListResponseHandler).onSuccess(expectedLoves, 0);
    }

    @Test
    public void testMakeLove_CreatesCorrectRequest() {
        final ArgumentCaptor<RequestParams> requestParamsCaptor = ArgumentCaptor.forClass(RequestParams.class);
        final Love love = new Love("the reason", new User("lover@example.com", "lover"), new User("lovee@example.com", "lovee"));
        love.isPrivate = true;
        love.message = "the message";

        client.makeLove(love, mockLoveResponseHandler);

        verify(mockAsyncHttpClient).post(eq("http://example.com/api/v1/loves"), requestParamsCaptor.capture(), any(JsonHttpResponseHandler.class));

        final Map<String, String> requestParams = parseParams(requestParamsCaptor.getValue());
        assertEquals("should set client id param", "androidapp", requestParams.get("clientId"));
        assertEquals("should set reason param", "the reason", requestParams.get("reason"));
        assertEquals("should set message param", "the message", requestParams.get("message"));
        assertEquals("should set private_message param", "true", requestParams.get("private_message"));
        assertEquals("should set to param", "lovee", requestParams.get("to"));
        assertEquals("should set from param", "lover", requestParams.get("from"));
    }

    @Test
    public void testMakeLove_AsyncHttpThrowsException_InvokesOnFail() {
        doThrow(new RuntimeException("some exception")).when(mockAsyncHttpClient).post(anyString(), any(RequestParams.class), any(JsonHttpResponseHandler.class));

        client.makeLove(Mockito.mock(Love.class), mockLoveResponseHandler);

        verify(mockLoveResponseHandler).onFail(Arrays.asList("some exception"));
    }

    @Test
    public void testMakeLove_RequestFails_InvokesOnFailWithPassedErrors() throws JSONException {
        final ArgumentCaptor<JsonHttpResponseHandler> jsonHttpResponseHandlerArgumentCaptor = ArgumentCaptor.forClass(JsonHttpResponseHandler.class);

        client.makeLove(Mockito.mock(Love.class), mockLoveResponseHandler);
        verify(mockAsyncHttpClient).post(anyString(), any(RequestParams.class), jsonHttpResponseHandlerArgumentCaptor.capture());
        jsonHttpResponseHandlerArgumentCaptor.getValue().onFailure(400, null, new Throwable("throwable message"), new JSONObject("{\"errors\": \"error message from server\"}"));

        verify(mockLoveResponseHandler).onFail(Arrays.asList("error message from server", "throwable message"));
    }

    @Test
    public void testMakeLove_RequestFails_NoResponseBody_InvokesOnFailWithNoErrors() {
        final ArgumentCaptor<JsonHttpResponseHandler> jsonHttpResponseHandlerArgumentCaptor = ArgumentCaptor.forClass(JsonHttpResponseHandler.class);

        client.makeLove(Mockito.mock(Love.class), mockLoveResponseHandler);
        verify(mockAsyncHttpClient).post(anyString(), any(RequestParams.class), jsonHttpResponseHandlerArgumentCaptor.capture());
        jsonHttpResponseHandlerArgumentCaptor.getValue().onFailure(400, null, null, new JSONObject());

        verify(mockLoveResponseHandler).onFail(new ArrayList<String>());
    }

    @Test
    public void testMakeLove_RequestSucceeds_InvokesOnSuccessWithPassedLove() throws JSONException {
        final ArgumentCaptor<JsonHttpResponseHandler> jsonHttpResponseHandlerArgumentCaptor = ArgumentCaptor.forClass(JsonHttpResponseHandler.class);
        final Love expectedLove = Mockito.mock(Love.class);

        client.makeLove(expectedLove, mockLoveResponseHandler);
        verify(mockAsyncHttpClient).post(anyString(), any(RequestParams.class), jsonHttpResponseHandlerArgumentCaptor.capture());
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
