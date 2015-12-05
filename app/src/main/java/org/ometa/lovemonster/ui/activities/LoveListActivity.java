package org.ometa.lovemonster.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.ometa.lovemonster.R;
import org.ometa.lovemonster.models.User;
import org.ometa.lovemonster.service.LoveMonsterClient;
import org.ometa.lovemonster.ui.fragments.HomeLoveFragment;
import org.ometa.lovemonster.ui.fragments.MakeLoveDialogFragment;

public class LoveListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_love_list);
        LoveMonsterClient client = LoveMonsterClient.getInstance();

        HomeLoveFragment fragment = (HomeLoveFragment) getSupportFragmentManager().findFragmentById(R.id.lovesList);
        fragment.setCurrentUser(client.getAuthenticatedUser());

        final FloatingActionButton makeLoveButton = (FloatingActionButton) findViewById(R.id.make_love);
        makeLoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MakeLoveDialogFragment makeLoveDialogFragment = MakeLoveDialogFragment.newInstance(new User("foo@example.com", ""));
                makeLoveDialogFragment.show(getFragmentManager(), "makeLoveDialog");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_love_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_love_list_user_avatar) {
            final Intent intent = new Intent(this, UserLoveActivity.class);
            intent.putExtra(User.PARCELABLE_KEY, LoveMonsterClient.getInstance().getAuthenticatedUser());
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
