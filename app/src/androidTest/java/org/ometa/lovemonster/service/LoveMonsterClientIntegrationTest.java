package org.ometa.lovemonster.service;

import android.support.annotation.NonNull;
import android.test.AndroidTestCase;

import org.ometa.lovemonster.models.Love;

import java.util.ArrayList;
import java.util.List;

public class LoveMonsterClientIntegrationTest extends AndroidTestCase {

    public void testGetRecentLoves() {
        final List<Love> retrievedLoves = new ArrayList<>();

        LoveMonsterClient.getInstance().retrieveRecentLoves(new LoveMonsterClient.LoveListResponseHandler() {
            @Override
            public void onSuccess(@NonNull List<Love> loves, int totalPages) {
                retrievedLoves.addAll(loves);
            }

            @Override
            public void onFail() {
                fail("the get recent loves request failed");
            }
        }, 1);

        assertFalse("should have retrieved loves", retrievedLoves.isEmpty());

        final Love firstLove = retrievedLoves.get(0);
        assertNotNull("should set the reason", firstLove.reason);
        assertNotNull("should set the message", firstLove.message);
        assertNotNull("should set created at", firstLove.createdAt);
        assertNotNull("should set lovee email", firstLove.lovee.email);
        assertNotNull("should set lovee username", firstLove.lovee.username);
        assertNotNull("should set lover email", firstLove.lover.email);
        assertNotNull("should set lover username", firstLove.lover.username);
    }
}
