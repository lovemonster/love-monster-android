package org.ometa.lovemonster.ui.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;

import org.ometa.lovemonster.Logger;
import org.ometa.lovemonster.R;
import org.ometa.lovemonster.models.User;
import org.ometa.lovemonster.service.LoveMonsterClient;
import org.ometa.lovemonster.ui.adapters.SmartFragmentStatePagerAdapter;
import org.ometa.lovemonster.ui.fragments.LovesListFragment;
import org.ometa.lovemonster.ui.fragments.MakeLoveDialogFragment;
import org.ometa.lovemonster.ui.fragments.UserLoveFragment;

public class UserLoveActivity extends AppCompatActivity {
    private Logger logger;
    private LoveMonsterClient client;
    private LovesPagerAdapter fragmentAdapter;
    private User currentUser, user;
    private ViewPager viewPager;

    private PagerSlidingTabStrip tabsStrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger = new Logger(UserLoveActivity.class);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_love);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_left_white_24dp);

        // get and assign the user we're concerned about
        user = getIntent().getParcelableExtra(User.PARCELABLE_KEY);
        getSupportActionBar().setTitle(titleFor(user));

        setupMakeLoveButton();
        setupViewPager();

        // set the current user (must happen after the fragments are setup)
        client = LoveMonsterClient.getInstance();
        setCurrentUser(client.getAuthenticatedUser());
    }

    /**
     * Configures the fragments and the view pager
     */
    private void setupViewPager() {
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        fragmentAdapter = new LovesPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(fragmentAdapter);

        // Give the PagerSlidingTabStrip the ViewPager
        tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);
    }

    /**
     * Configures the floating action button to send love to the user being viewed.
     */
    private void setupMakeLoveButton() {
        final FloatingActionButton makeLoveButton = (FloatingActionButton) findViewById(R.id.make_love);
        makeLoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MakeLoveDialogFragment makeLoveDialogFragment = MakeLoveDialogFragment.newInstance(user);
                makeLoveDialogFragment.setOnSuccessCallback(new MakeLoveDialogFragment.SuccessCallback() {
                    @Override
                    public void onSuccess() {
                        /*
                         * If we're on the user's own view, reload their sent loves.  Otherwise, reload
                         * the received loves.
                         */
                        int tabIndex = (user.equals(currentUser)) ? LovesPagerAdapter.SENT_TAB_INDEX : LovesPagerAdapter.RECEIVED_TAB_INDEX;
                        UserLoveFragment fragment = (UserLoveFragment) fragmentAdapter.getRegisteredFragment(tabIndex);
                        fragment.reloadLoves();
                    }
                });
                makeLoveDialogFragment.show(getFragmentManager(), "makeLoveDialog");
            }
        });
    }

    private void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        if (!currentUser.equals(user)) {
            fragmentAdapter.setCurrentUser(currentUser);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_love, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private String titleFor(User user) {
        if (user.name != null)
            return user.name;
        return user.username;
    }

    public class LovesPagerAdapter extends SmartFragmentStatePagerAdapter {
        static final int SENT_TAB_INDEX = 0;
        static final int RECEIVED_TAB_INDEX = 1;
        private User currentUser;

        private String tabTitles[] = { getString(R.string.sent_tab_title), getString(R.string.received_tab_title) };

        public LovesPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public void setCurrentUser(User user) {
            currentUser = user;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == SENT_TAB_INDEX) {
                return makeSentFragment();
            } else {
                return makeReceivedFragment();
            }
        }

        private LovesListFragment makeSentFragment() {
            UserLoveFragment fragment = new UserLoveFragment();
            fragment.setCurrentUser(currentUser);
            fragment.setSubjectUser(user);
            fragment.setDirection(User.UserLoveAssociation.lover);
            return fragment;
        }

        private LovesListFragment makeReceivedFragment() {
            UserLoveFragment fragment = new UserLoveFragment();
            fragment.setCurrentUser(currentUser);
            fragment.setSubjectUser(user);
            fragment.setDirection(User.UserLoveAssociation.lovee);
            return fragment;
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