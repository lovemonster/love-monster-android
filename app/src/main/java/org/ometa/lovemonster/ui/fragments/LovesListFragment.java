package org.ometa.lovemonster.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.ometa.lovemonster.R;
import org.ometa.lovemonster.models.Love;
import org.ometa.lovemonster.models.User;
import org.ometa.lovemonster.ui.adapters.LoveArrayAdapter;

import java.util.ArrayList;

/**
 * Created by bschmeckpeper on 11/18/15.
 */
public class LovesListFragment extends Fragment {
    ArrayList<Love> lovesList;
    LoveArrayAdapter lovesArrayAdapter;
    ListView lvLoves;
    private View noLovesMessage;
    private User currentUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lovesList = new ArrayList<>();
        lovesArrayAdapter = new LoveArrayAdapter(getContext(), lovesList, currentUser);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        View view = inflater.inflate(R.layout.loves_list_fragment, container, false);

        lvLoves = (ListView) view.findViewById(R.id.lvLoves);
        lvLoves.setAdapter(lovesArrayAdapter);

        noLovesMessage = view.findViewById(R.id.lvLovesNoLoves);
        toggleNoLoveMessage();

        // Setup handles to view objects here
        // etFoo = (EditText) view.findViewById(R.id.etFoo);
        return view;
    }

    /**
     * Toggles whether the no love message is displayed or not.
     */
    private void toggleNoLoveMessage() {
        if (lovesArrayAdapter.isEmpty()) {
            noLovesMessage.setVisibility(View.VISIBLE);
        } else {
            noLovesMessage.setVisibility(View.GONE);
        }
    }

    public void setCurrentUser(User user) {
        // We sometimes get called before the array adapter has been created.  In those cases,
        // cache the currentUser in an instance var and set it on the adapter when we finally
        // get around to creating the adapter.
        if (lovesArrayAdapter == null) {
            currentUser = user;
        } else {
            lovesArrayAdapter.currentUser = user;
        }
    }

    public void addLove(Love love) {
        lovesArrayAdapter.add(love);
        toggleNoLoveMessage();
    }
}
