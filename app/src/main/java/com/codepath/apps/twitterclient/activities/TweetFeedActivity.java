package com.codepath.apps.twitterclient.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.fragments.ComposeTweetDialog;
import com.codepath.apps.twitterclient.fragments.HomeTimelineFragment;
import com.codepath.apps.twitterclient.fragments.MentionsTimelineFragment;
import com.codepath.apps.twitterclient.fragments.TweetsListFragment;
import com.codepath.apps.twitterclient.helpers.Util;

// What to put in Activity:
// Removing or adding fragments
// navigating between fragments
// handling communication between fragments

public class TweetFeedActivity extends AppCompatActivity implements ComposeTweetDialog.OnComposeTweetActionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_feed);

        // Get the view pager
        ViewPager vpPager = (ViewPager) findViewById(R.id.vpTimelinePager);
        // set the viewpager adapter for the pager
        PagerAdapter pagerAdapter = new TweetsPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(pagerAdapter);

        PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip) findViewById(R.id.ptsTimelinePagerHeader);
        tabStrip.setViewPager(vpPager);

        // Attach the page change listener inside the activity
        vpPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
                refreshCurrentListFragment();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tweet_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_compose_tweet) {
            showComposeDialog();
            return true;
        } else if (id == R.id.action_view_profile) {
            showProfileView();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ComposeTweetDialog mComposeDialog;

    private void showProfileView() {
        long userId = Util.getUserId(this);

        Intent i = new Intent(this, ProfileActivity.class);
        i.putExtra(ProfileActivity.USER_ID_KEY, userId);
        startActivity(i);
    }

    private void showComposeDialog() {
        FragmentManager fm = getSupportFragmentManager();
        mComposeDialog = ComposeTweetDialog.newInstance();
        mComposeDialog.show(fm, "fragment_compose_tweet_dialog");
    }

    @Override
    public void onTweetSave(int errorMessage) {
        if (errorMessage == 0) {
            if (mComposeDialog != null) {
                mComposeDialog.dismiss();
            }
            refreshCurrentListFragment();
        } else {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }

    }


    public class TweetsPagerAdapter extends FragmentPagerAdapter {
        private String tabTitles[] = {"Home", "Mentions"};

        public TweetsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return HomeTimelineFragment.newInstance();
                case 1: return MentionsTimelineFragment.newInstance();
                default:
                    throw new RuntimeException("Unknown fragment index");
            }
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
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


    public void refreshCurrentListFragment() {
        ViewPager vpPager = (ViewPager) findViewById(R.id.vpTimelinePager);
        Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.vpTimelinePager + ":" + vpPager.getCurrentItem());
        // based on the current position you can then cast the page to the correct
        // class and call the method:

        ((TweetsListFragment)page).refreshTimeline();
    }
}
