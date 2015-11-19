package org.ometa.lovemonster.service;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.ometa.lovemonster.Fixtures;
import org.ometa.lovemonster.models.Love;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ResponseParserTest {

    ResponseParser responseParser;

    @Before
    public void setUp() throws Exception {
        responseParser = new ResponseParser();
    }

    @Test
    public void testParsesLoveList_Null_ReturnsEmptyList() {
        final List<Love> loves = responseParser.parseLoveList(null);

        assertNotNull("loves should not be null", loves);
        assertTrue("loves should be empty", loves.isEmpty());
    }

    @Test
    public void testParsesLoveList_NoDataSection_ReturnsEmptyList() throws JSONException {
        final List<Love> loves = responseParser.parseLoveList(new JSONObject("{}"));

        assertNotNull("loves should not be null", loves);
        assertTrue("loves should be empty", loves.isEmpty());
    }

    @Test
    public void testParsesLoveList_MissingFields_SkipsLove() {
        final List<Love> loves = responseParser.parseLoveList(Fixtures.getJsonObject("v1_loves-missing_fields.json"));

        assertNotNull("loves should not be null", loves);
        assertTrue("loves should be empty", loves.isEmpty());
    }

    @Test
    public void testParsesLoveList_FullResponse_ReturnsAllLoves() {
        final List<Love> loves = responseParser.parseLoveList(Fixtures.getJsonObject("v1_loves.json"));

        assertNotNull("loves should not be null", loves);
        assertEquals("should have 25 loves", 25, loves.size());

        final Love fullyFilledOutLove = loves.get(0);
        assertEquals("should set the reason", "organizing all the move-related stuff", fullyFilledOutLove.reason);
        assertEquals("should set the message", "this is a fake message", fullyFilledOutLove.message);
        assertTrue("should be a private message", fullyFilledOutLove.isPrivate);
        assertEquals("should set created at", Fixtures.getDatetime(2015, 11, 18, 15, 55, 46), fullyFilledOutLove.createdAt);
        assertEquals("should set lovee email", "jjenkins@groupon.com", fullyFilledOutLove.lovee.email);
        assertEquals("should set lovee name", "Jonathan Jenkins", fullyFilledOutLove.lovee.name);
        assertEquals("should set lovee username", "jjenkins", fullyFilledOutLove.lovee.username);
        assertEquals("should set lover email", "jepinho@groupon.com", fullyFilledOutLove.lover.email);
        assertEquals("should set lover name", "Jesse Pinho", fullyFilledOutLove.lover.name);
        assertEquals("should set lover username", "jepinho", fullyFilledOutLove.lover.username);

        final Love minimallyFilledOutLove = loves.get(24);
        assertEquals("should set the reason", "New Yooooooooooooork", minimallyFilledOutLove.reason);
        assertNull("should set the message to null when missing", minimallyFilledOutLove.message);
        assertFalse("should not be a private message", minimallyFilledOutLove.isPrivate);
        assertEquals("should set created at", Fixtures.getDatetime(2015, 11, 13, 15, 17, 2), minimallyFilledOutLove.createdAt);
        assertEquals("should set lovee email", "aderly@groupon.com", minimallyFilledOutLove.lovee.email);
        assertNull("should set lovee nam to null when missinge", minimallyFilledOutLove.lovee.name);
        assertEquals("should set lovee username", "aderly", minimallyFilledOutLove.lovee.username);
        assertEquals("should set lover email", "qnouffert@groupon.com", minimallyFilledOutLove.lover.email);
        assertNull("should set lover name to null when missing", minimallyFilledOutLove.lover.name);
        assertEquals("should set lover username", "qnouffert", minimallyFilledOutLove.lover.username);
    }
}