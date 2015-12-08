package org.ometa.lovemonster.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.ometa.lovemonster.Logger;
import org.ometa.lovemonster.R;
import org.ometa.lovemonster.models.User;
import org.ometa.lovemonster.service.LoveMonsterClient;
import org.ometa.lovemonster.ui.fragments.HomeLoveFragment;
import org.ometa.lovemonster.ui.fragments.MakeLoveDialogFragment;
import org.ometa.lovemonster.ui.widget.RoundedRectangleTransformation;

import java.util.List;

public class LoveListActivity extends AppCompatActivity {

    private static final Logger logger = new Logger(LoveListActivity.class);
    private boolean searchInProgress = false;
    private Target userAvatarTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_love_list);
        LoveMonsterClient client = LoveMonsterClient.getInstance();

        final HomeLoveFragment fragment = (HomeLoveFragment) getSupportFragmentManager().findFragmentById(R.id.lovesList);
        fragment.setCurrentUser(client.getAuthenticatedUser());

        final FloatingActionButton makeLoveButton = (FloatingActionButton) findViewById(R.id.make_love);
        makeLoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MakeLoveDialogFragment makeLoveDialogFragment = MakeLoveDialogFragment.newInstance(new User("foo@example.com", ""));
                makeLoveDialogFragment.setOnSuccessCallback(new MakeLoveDialogFragment.SuccessCallback() {
                    @Override
                    public void onSuccess() {
                        fragment.reloadLoves();
                    }
                });
                makeLoveDialogFragment.show(getFragmentManager(), "makeLoveDialog");

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_love_list, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Username...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                logger.debug("Search for " + query);
                if (searchInProgress) {
                    logger.debug("Search already in progress");
                    // I can't find documentation on what the return value of this method does.
                    return false;
                }
                searchInProgress = true;
                LoveMonsterClient client = LoveMonsterClient.getInstance();
                client.getUserFromUsername(query, new LoveMonsterClient.UserLookupResponseHandler() {
                    @Override
                    public void onUserExists(final User user) {
                        searchInProgress = false;
                        final Intent intent = new Intent(LoveListActivity.this, UserLoveActivity.class);
                        intent.putExtra(User.PARCELABLE_KEY, user);
                        startActivity(intent);
                    }

                    @Override
                    public void onUserNotFound() {
                        searchInProgress = false;
                        Toast.makeText(getApplicationContext(), "Cannot find user " + query, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFail(final List<String> errorMessages) {
                        searchInProgress = false;
                        Toast.makeText(getApplicationContext(), "Network failure", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationFailure() {
                        searchInProgress = false;
                    }
                });

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        final MenuItem menuItem = menu.findItem(R.id.menu_love_list_user_avatar);

        userAvatarTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                logger.debug("method=onCreateOptionsMenu.loadAvatar handler=onBitmapLoaded");
                menuItem.setIcon(new BitmapDrawable(bitmap));
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                logger.debug("method=onCreateOptionsMenu.loadAvatar handler=onBitmapFailed ");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                logger.debug("method=onCreateOptionsMenu.loadAvatar handler=onPrepareLoad");
            }

            @Override
            public boolean equals(Object o) {
                return menuItem.equals(o);
            }

            @Override
            public int hashCode() {
                return menuItem.hashCode();
            }
        };

        setUserProfileIcon(menuItem);
        return true;
    }

    /**
     * Sets the user profile icon based on the user's avatar.
     *
     * @param menuItem
     *      the menu item to write the user avatar into
     */
    private void setUserProfileIcon(final MenuItem menuItem) {
        if (LoveMonsterClient.getInstance().getAuthenticatedUser() == null)
            return;

        Picasso.with(this)
                .load(Uri.parse(LoveMonsterClient.getInstance().getAuthenticatedUser().profileImageUrl))
                .transform(new RoundedRectangleTransformation(10, 1))
                .resize((int) (menuItem.getIcon().getIntrinsicWidth() * 1.75), (int) (menuItem.getIcon().getIntrinsicHeight() * 1.75))
                .into(userAvatarTarget);
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
