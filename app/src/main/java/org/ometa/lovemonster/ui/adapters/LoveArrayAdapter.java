package org.ometa.lovemonster.ui.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import org.ometa.lovemonster.models.Love;

import java.util.List;

/**
 * Created by bschmeckpeper on 11/18/15.
 */
public class LoveArrayAdapter extends ArrayAdapter<Love> {
    public LoveArrayAdapter(Context context, List<Love> loves) {
        super(context, android.R.layout.simple_list_item_1, loves);
    }
}
