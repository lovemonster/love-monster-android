package org.ometa.lovemonster.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.ometa.lovemonster.Logger;
import org.ometa.lovemonster.models.Love;
import org.ometa.lovemonster.models.User;
import org.ometa.lovemonster.service.LoveMonsterClient;
import org.ometa.lovemonster.ui.activities.LoveListActivity;
import org.ometa.lovemonster.ui.adapters.LoveArrayAdapter;

import java.util.List;

/**
 * Created by devin on 12/5/15.
 */
public class UserLoveFragment extends LovesListFragment {

    private Logger logger = new Logger(UserLoveFragment.class);
    private LoveMonsterClient client;
    private User.UserLoveAssociation direction;
    private User subjectUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        client = LoveMonsterClient.getInstance();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSwipeUp(int page) {
        client.retrieveRecentLoves(getSwipeUpHandler(), page, subjectUser, direction);
    }

    @Override
    public void reloadLoves() {
        lovesList.clear();
        client.retrieveRecentLoves(getSwipeDownHandler(), 1, subjectUser, direction);
    }

    @Override
    protected void initialLoadNoInternet(LoveArrayAdapter lovesArrayAdapter) {
        // todo: load existing loves from sqlite db
        Toast.makeText(getContext(), "userlovefragment: initial load no inet", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void initialLoadWithInternet() {
        client.retrieveRecentLoves(getSwipeUpHandler(), 1, subjectUser, direction);
    }


    public void setSubjectUser(User user) {
        // We sometimes get called before the array adapter has been created.  In those cases,
        // cache the subjectUser in an instance var and set it on the adapter when we finally
        // get around to creating the adapter.
        if (lovesArrayAdapter == null) {
            subjectUser = user;
        } else {
            lovesArrayAdapter.subjectUser = user;
        }
    }

    public void setDirection(User.UserLoveAssociation direction) {
        this.direction = direction;
    }


    // custom swipe-up handler to use for this user fragment

    @Override
    protected LoveMonsterClient.LoveListResponseHandler getSwipeUpHandler() {

        return new LoveMonsterClient.LoveListResponseHandler() {
            @Override
            public void onSuccess(@NonNull List<Love> loves, int totalPages) {
                for (Love love : loves) {
                    if (isUnexpectedLove(love)) {
                        logger.debug("Received unexpected love from " + love.lover.username + " to " + love.lovee.username + ", direction: " + direction);
                        continue;
                    }
                    addLove(love);
                }
            }

            @Override
            public void onFail() {
                Toast.makeText(getParentFragment().getContext(), "Unable to retrieve loves", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailure() {
                final Intent intent = new Intent(getActivity(), LoveListActivity.class);
                getActivity().startActivity(intent);
                getActivity().finish();
            }

            private boolean isUnexpectedLove(Love love) {
                return (direction == User.UserLoveAssociation.lovee && !isReceivedLove(love)) ||
                        (direction == User.UserLoveAssociation.lover && !isSentLove(love));
            }

            private boolean isReceivedLove(Love love) {
                return love.lovee.username.equals(subjectUser.username);
            }

            private boolean isSentLove(Love love) {
                return love.lover.username.equals(subjectUser.username);
            }
        };
    }
}
