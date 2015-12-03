package org.ometa.lovemonster.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.ometa.lovemonster.R;
import org.ometa.lovemonster.models.Love;
import org.ometa.lovemonster.models.User;
import org.ometa.lovemonster.service.LoveMonsterClient;
import org.ometa.lovemonster.ui.fragments.LovesListFragment;
import org.ometa.lovemonster.ui.fragments.MakeLoveDialogFragment;

import java.util.List;

public class LoveListActivity extends AppCompatActivity {
    private LoveMonsterClient client;
    private LovesListFragment lovesList;
    private int nextPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_love_list);

        lovesList = (LovesListFragment) getSupportFragmentManager().findFragmentById(R.id.lovesList);
        client = LoveMonsterClient.getInstance();
        setCurrentUser(client.getAuthenticatedUser());

        final FloatingActionButton makeLoveButton = (FloatingActionButton) findViewById(R.id.make_love);
        makeLoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MakeLoveDialogFragment makeLoveDialogFragment = MakeLoveDialogFragment.newInstance(new User("foo@example.com", ""));
                makeLoveDialogFragment.show(getFragmentManager(), "makeLoveDialog");
            }
        });
        
        getLoves();
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

    private void setCurrentUser(User currentUser) {
        lovesList.setCurrentUser(currentUser);
    }

    private void getLoves() {
        client.retrieveRecentLoves(new LoveMonsterClient.LoveListResponseHandler() {
            @Override
            public void onSuccess(@NonNull List<Love> loves, int totalPages) {
                for (Love love : loves) {
                    lovesList.addLove(love);
                }
            }

            @Override
            public void onFail() {
                Toast.makeText(getApplicationContext(), "Unable to retrieve loves", Toast.LENGTH_SHORT).show();
            }
        }, nextPage);
        nextPage += 1;
    }
}
