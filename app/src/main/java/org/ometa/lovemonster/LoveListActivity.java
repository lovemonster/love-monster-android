package org.ometa.lovemonster;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.ometa.lovemonster.models.User;
import org.ometa.lovemonster.ui.fragments.MakeLoveDialogFragment;

public class LoveListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_love_list);

        final FloatingActionButton makeLoveButton = (FloatingActionButton) findViewById(R.id.make_love);
        makeLoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MakeLoveDialogFragment makeLoveDialogFragment = MakeLoveDialogFragment.newInstance(new User("foo@example.com", "foo"));
                makeLoveDialogFragment.show(getFragmentManager(), "makeLoveDialog");
            }
        });
    }
}
