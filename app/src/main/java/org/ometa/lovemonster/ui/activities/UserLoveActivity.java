package org.ometa.lovemonster.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.ometa.lovemonster.Logger;
import org.ometa.lovemonster.R;
import org.ometa.lovemonster.models.Love;
import org.ometa.lovemonster.models.User;
import org.ometa.lovemonster.service.LoveMonsterClient;
import org.ometa.lovemonster.ui.adapters.SmartFragmentStatePagerAdapter;
import org.ometa.lovemonster.ui.fragments.LovesListFragment;

import java.util.List;

public class UserLoveActivity extends AppCompatActivity {
    private Logger logger;
    private LoveMonsterClient client;
    private LovesListFragment lovesSent;
    private LovesListFragment lovesReceived;
    private TweetsPagerAdapter fragmentAdapter;
    private User user;
    private ViewPager viewPager;
    private int nextPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_love);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        fragmentAdapter = new TweetsPagerAdapter(getSupportFragmentManager(), UserLoveActivity.this);
        viewPager.setAdapter(fragmentAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        lovesSent = (LovesListFragment) fragmentAdapter.getRegisteredFragment(0);
        lovesReceived = (LovesListFragment) fragmentAdapter.getRegisteredFragment(1);

        client = LoveMonsterClient.getInstance();
        user = (User) getIntent().getParcelableExtra(User.PARCELABLE_KEY);

        logger = new Logger(UserLoveActivity.class);

        getLoves(User.UserLoveAssociation.lovee);
        getLoves(User.UserLoveAssociation.lover);
    }

    private void getLoves(final User.UserLoveAssociation direction) {
        client.retrieveRecentLoves(new LoveMonsterClient.LoveListResponseHandler() {
            @Override
            public void onSuccess(@NonNull List<Love> loves, int totalPages) {
                for (Love love : loves) {
                    if (direction == User.UserLoveAssociation.lovee || isReceivedLove(love)) {
                        lovesReceived.addLove(love);
                    } else if (direction == User.UserLoveAssociation.lover || isSentLove(love)) {
                        lovesSent.addLove(love);
                    } else {
                        logger.debug("Received unexpected love from " + love.lover.username + " to " + love.lovee.username);
                    }
                }
            }

            @Override
            public void onFail() {
                Toast.makeText(getApplicationContext(), "Unable to retrieve loves", Toast.LENGTH_SHORT).show();
            }

            private boolean isReceivedLove(Love love) {
                return love.lovee.username == UserLoveActivity.this.user.username;
            }

            private boolean isSentLove(Love love) {
                return love.lover.username == UserLoveActivity.this.user.username;
            }
        }, nextPage, user, direction);
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

    public class TweetsPagerAdapter extends SmartFragmentStatePagerAdapter {
        private String tabTitles[] = { "Sent", "Received" };
        private Context context;

        public TweetsPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new LovesListFragment();
            } else {
                return new LovesListFragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }
    }
}
