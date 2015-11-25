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
import org.mockito.MockitoAnnotations;
import org.ometa.lovemonster.models.Love;

import java.util.ArrayList;
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
    LoveMonsterClient.LoveListResponseHandler mockResponseHandler;

    LoveMonsterClient client;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        client = new LoveMonsterClient(mockResponseParser, mockAsyncHttpClient);
    }

    @Test
    public void testRetrieveRecentLoves_UserId_CreatesCorrectRequest() {
        final ArgumentCaptor<RequestParams> requestParamsCaptor = ArgumentCaptor.forClass(RequestParams.class);

        client.retrieveRecentLoves(mockResponseHandler, 77, 55);

        verify(mockAsyncHttpClient).get(eq("http://love.snc1/api/v1/loves"), requestParamsCaptor.capture(), any(JsonHttpResponseHandler.class));

        final Map<String, String> requestParams = parseParams(requestParamsCaptor.getValue());
        assertEquals("should set client id param", requestParams.get("clientId"), "androidapp");
        assertEquals("should set page param", requestParams.get("page"), "77");
        assertEquals("should set user_id param", requestParams.get("user_id"), "55");
    }

    @Test
    public void testRetrieveRecentLoves_NoUserId_CreatesCorrectRequest() {
        final ArgumentCaptor<RequestParams> requestParamsCaptor = ArgumentCaptor.forClass(RequestParams.class);

        client.retrieveRecentLoves(mockResponseHandler, 77);

        verify(mockAsyncHttpClient).get(eq("http://love.snc1/api/v1/loves"), requestParamsCaptor.capture(), any(JsonHttpResponseHandler.class));

        final Map<String, String> requestParams = parseParams(requestParamsCaptor.getValue());
        assertEquals("should set client id param", requestParams.get("clientId"), "androidapp");
        assertEquals("should set page param", requestParams.get("page"), "77");
        assertFalse("should not set user_id param", requestParams.containsKey("user_id"));
    }

    @Test
    public void testRetrieveRecentLoves_AsyncHttpThrowsException_InvokesOnFail() {
        doThrow(new RuntimeException()).when(mockAsyncHttpClient).get(anyString(), any(RequestParams.class), any(JsonHttpResponseHandler.class));

        client.retrieveRecentLoves(mockResponseHandler, 1);

        verify(mockResponseHandler).onFail();
    }

    @Test
    public void testRetrieveRecentLoves_RequestFails_InvokesOnFail() {
        final ArgumentCaptor<JsonHttpResponseHandler> jsonHttpResponseHandlerArgumentCaptor = ArgumentCaptor.forClass(JsonHttpResponseHandler.class);

        client.retrieveRecentLoves(mockResponseHandler, 1);
        verify(mockAsyncHttpClient).get(anyString(), any(RequestParams.class), jsonHttpResponseHandlerArgumentCaptor.capture());
        jsonHttpResponseHandlerArgumentCaptor.getValue().onFailure(500, null, null, new JSONObject());

        verify(mockResponseHandler).onFail();
    }

    @Test
    public void testRetrieveRecentLoves_RequestSucceeds_InvokesOnSuccess() throws JSONException {
        final ArgumentCaptor<JsonHttpResponseHandler> jsonHttpResponseHandlerArgumentCaptor = ArgumentCaptor.forClass(JsonHttpResponseHandler.class);
        final List<Love> expectedLoves = new ArrayList<>();
        final JSONObject expectedJsonObject = new JSONObject("{\"pages\":7}");
        when(mockResponseParser.parseLoveList(expectedJsonObject)).thenReturn(expectedLoves);

        client.retrieveRecentLoves(mockResponseHandler, 1);
        verify(mockAsyncHttpClient).get(anyString(), any(RequestParams.class), jsonHttpResponseHandlerArgumentCaptor.capture());
        jsonHttpResponseHandlerArgumentCaptor.getValue().onSuccess(200, null, expectedJsonObject);

        verify(mockResponseHandler).onSuccess(expectedLoves, 7);
    }

    @Test
    public void testRetrieveRecentLoves_RequestSucceeds_MissingPages_InvokesOnSuccessWithDefaultPages() {
        final ArgumentCaptor<JsonHttpResponseHandler> jsonHttpResponseHandlerArgumentCaptor = ArgumentCaptor.forClass(JsonHttpResponseHandler.class);
        final List<Love> expectedLoves = new ArrayList<>();
        final JSONObject expectedJsonObject = new JSONObject();
        when(mockResponseParser.parseLoveList(expectedJsonObject)).thenReturn(expectedLoves);

        client.retrieveRecentLoves(mockResponseHandler, 1);
        verify(mockAsyncHttpClient).get(anyString(), any(RequestParams.class), jsonHttpResponseHandlerArgumentCaptor.capture());
        jsonHttpResponseHandlerArgumentCaptor.getValue().onSuccess(200, null, expectedJsonObject);

        verify(mockResponseHandler).onSuccess(expectedLoves, 0);
    }

    @Test
    public void testRetrieveRecentLoves_RequestSucceeds_NullResponse_InvokesOnSuccessWithDefaultPages() {
        final ArgumentCaptor<JsonHttpResponseHandler> jsonHttpResponseHandlerArgumentCaptor = ArgumentCaptor.forClass(JsonHttpResponseHandler.class);
        final List<Love> expectedLoves = new ArrayList<>();

        client.retrieveRecentLoves(mockResponseHandler, 1);
        verify(mockAsyncHttpClient).get(anyString(), any(RequestParams.class), jsonHttpResponseHandlerArgumentCaptor.capture());
        jsonHttpResponseHandlerArgumentCaptor.getValue().onSuccess(200, null, (JSONObject) null);

        verify(mockResponseHandler).onSuccess(expectedLoves, 0);
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
