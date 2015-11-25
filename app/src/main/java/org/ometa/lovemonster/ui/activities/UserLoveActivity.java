package org.ometa.lovemonster.ui.activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.ometa.lovemonster.R;
import org.ometa.lovemonster.models.Love;
import org.ometa.lovemonster.models.User;
import org.ometa.lovemonster.service.LoveMonsterClient;
import org.ometa.lovemonster.ui.fragments.LovesListFragment;

import java.util.List;

public class UserLoveActivity extends AppCompatActivity {
    private LovesListFragment lovesSent;
    private LovesListFragment lovesReceived;
    private LoveMonsterClient client;
    private User user;
    private int nextPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_love);
        lovesSent = (LovesListFragment) getSupportFragmentManager().findFragmentById(R.id.lovesList);
        lovesReceived = (LovesListFragment) getSupportFragmentManager().findFragmentById(R.id.lovesList);
        client = LoveMonsterClient.getInstance();
        user = (User) getIntent().getParcelableExtra("user");

        getLoves();
    }

    private void getLoves() {
        client.retrieveRecentLoves(new LoveMonsterClient.LoveListResponseHandler() {
            @Override
            public void onSuccess(@NonNull List<Love> loves, int totalPages) {
                for (Love love : loves) {
                    if (love.lovee.username == user.username) {
                        lovesReceived.addLove(love);
                    } else if (love.lover.username == user.username) {
                        lovesSent.addLove(love);
                    }
                }
            }

            @Override
            public void onFail() {
                Toast.makeText(getApplicationContext(), "Unable to retrieve loves", Toast.LENGTH_SHORT).show();
            }
        }, nextPage, user);
        nextPage += 1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_love, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
