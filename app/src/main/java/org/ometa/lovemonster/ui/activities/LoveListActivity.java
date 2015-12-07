package org.ometa.lovemonster.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.ometa.lovemonster.Logger;
import org.ometa.lovemonster.R;
import org.ometa.lovemonster.models.User;
import org.ometa.lovemonster.service.LoveMonsterClient;
import org.ometa.lovemonster.ui.fragments.HomeLoveFragment;
import org.ometa.lovemonster.ui.fragments.MakeLoveDialogFragment;
import org.ometa.lovemonster.ui.widget.RoundedRectangleTransformation;

public class LoveListActivity extends AppCompatActivity {

    private static final Logger logger = new Logger(LoveListActivity.class);

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
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_love_list, menu);
        setUserProfileIcon(menu.getItem(0), 2);
        return true;
    }

    /**
     * Sets the user profile icon based on the user's avatar.
     *
     * @param menuItem
     *      the menu item to update
     * @param remainingRetries
     *      the number of remaining retries if the avatar fails to load
     */
    private void setUserProfileIcon(final MenuItem menuItem, final int remainingRetries) {
        Picasso.with(this)
                .load(Uri.parse(LoveMonsterClient.getInstance().getAuthenticatedUser().profileImageUrl))
                .transform(new RoundedRectangleTransformation(10, 1))
                .resize((int) (menuItem.getIcon().getIntrinsicWidth() * 1.75), (int) (menuItem.getIcon().getIntrinsicHeight() * 1.75))
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        logger.debug("method=onCreateOptionsMenu handler=onBitmapLoaded");
                        menuItem.setIcon(new BitmapDrawable(bitmap));
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        logger.debug("method=onCreateOptionsMenu handler=onBitmapFailed remainingRetries=" + remainingRetries);
                        if (remainingRetries >= 0) {
                            setUserProfileIcon(menuItem, remainingRetries - 1);
                        }
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
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
