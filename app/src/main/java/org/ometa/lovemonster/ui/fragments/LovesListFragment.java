package org.ometa.lovemonster.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.ometa.lovemonster.R;
import org.ometa.lovemonster.models.Love;
import org.ometa.lovemonster.ui.adapters.LoveArrayAdapter;

import java.util.ArrayList;

/**
 * Created by bschmeckpeper on 11/18/15.
 */
public class LovesListFragment extends Fragment {
    ArrayList<Love> lovesList;
    LoveArrayAdapter lovesArrayAdapter;
    ListView lvLoves;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lovesList = new ArrayList<>();
        lovesArrayAdapter = new LoveArrayAdapter(getContext(), lovesList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        View view = inflater.inflate(R.layout.loves_list_fragment, container, false);

        lvLoves = (ListView) view.findViewById(R.id.lvLoves);
        lvLoves.setAdapter(lovesArrayAdapter);

        // Setup handles to view objects here
        // etFoo = (EditText) view.findViewById(R.id.etFoo);
        return view;
    }
}
