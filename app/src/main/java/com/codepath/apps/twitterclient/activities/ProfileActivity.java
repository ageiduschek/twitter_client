package com.codepath.apps.twitterclient.activities;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.fragments.ProfileHeaderFragment;
import com.codepath.apps.twitterclient.fragments.UserTimelineFragment;
import com.codepath.apps.twitterclient.models.User;

public class ProfileActivity extends AppCompatActivity {

    public static final String USER_ID_KEY = "user_name_key";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        long userId = getIntent().getLongExtra(USER_ID_KEY, -1);

        if (userId == -1) {
            throw new RuntimeException("ProfileActivity requires user id");
        }

        if (savedInstanceState == null) {
            UserTimelineFragment userTimelineFragment = UserTimelineFragment.newInstance(userId);
            ProfileHeaderFragment profileHeaderFragment = ProfileHeaderFragment.newInstance(userId);

            // Display user fragment within
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.flProfileHeaderContainer, profileHeaderFragment);
            ft.replace(R.id.flTimelineContainer, userTimelineFragment);
            ft.commit();
        }
        User user = User.lookupWithId(userId);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(user.getScreenName());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
