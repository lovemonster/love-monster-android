package org.ometa.lovemonster.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ometa.lovemonster.R;

/**
 * Created by bschmeckpeper on 11/18/15.
 */
public class LovesListFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        View view = inflater.inflate(R.layout.loves_list_fragment, container, false);
        // Setup handles to view objects here
        // etFoo = (EditText) view.findViewById(R.id.etFoo);
        return view;
    }

}
