package org.ometa.lovemonster.ui.fragments;

import android.os.Bundle;

import org.ometa.lovemonster.service.LoveMonsterClient;
import org.ometa.lovemonster.ui.adapters.LoveArrayAdapter;

/**
 * Created by devin on 12/6/15.
 */
public class HomeLoveFragment extends LovesListFragment {

    private LoveMonsterClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        client = LoveMonsterClient.getInstance();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSwipeUp(int page) {
        client.retrieveRecentLoves(getSwipeUpHandler(), page);
    }

    @Override
    public void reloadLoves() {
        lovesList.clear();
        client.retrieveRecentLoves(getSwipeDownHandler(), 1);
    }

    @Override
    protected void initialLoadNoInternet(LoveArrayAdapter lovesArrayAdapter) {
        // todo: load existing loves from sqlite db
    }

    @Override
    protected void initialLoadWithInternet() {
        client.retrieveRecentLoves(getSwipeUpHandler(), 1);
    }
}