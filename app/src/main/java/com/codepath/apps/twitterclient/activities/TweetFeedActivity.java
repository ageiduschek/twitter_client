package com.codepath.apps.twitterclient.activities;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.adapters.TweetsListAdapter;
import com.codepath.apps.twitterclient.helpers.EndlessScrollListener;
import com.codepath.apps.twitterclient.helpers.TwitterModel;
import com.codepath.apps.twitterclient.models.Tweet;
import java.util.ArrayList;
import java.util.List;

public class TweetFeedActivity extends AppCompatActivity {
    private TwitterModel mTwitterModel;
    private TweetsListAdapter mTweetsListAdapter;
    private ListView lvTweets;
    private SwipeRefreshLayout mSwipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_feed);

        lvTweets = (ListView) findViewById(R.id.lvTweets);
        mTweetsListAdapter = new TweetsListAdapter(this, new ArrayList<Tweet>());
        lvTweets.setAdapter(mTweetsListAdapter);

        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                extendTimeline();
                return true; // ONLY if more data is actually being loaded; false otherwise.
            }
        });

        mTwitterModel = new TwitterModel(this);

        mSwipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        // Setup refresh listener which triggers new data loading
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshTimeline();
            }
        });

        // Configure the refreshing colors
        mSwipeContainer.setColorSchemeResources(R.color.twitter_dark_blue,
                R.color.twitter_logo_blue,
                R.color.twitter_verified_blue,
                R.color.twitter_background_blue);

        // Load initial batch
        refreshTimeline();
    }

    private void refreshTimeline() {
        mTwitterModel.getNewHomeTweets(getLoadTweetsDelegate(true /*isRefresh*/));
    }

    private void extendTimeline() {
        mTwitterModel.getHomeTweetsBefore(mTweetsListAdapter.getLastKnownId(),
                                          getLoadTweetsDelegate(false /*isRefresh*/));
    }

    private TwitterModel.TweetListQueryDelegate getLoadTweetsDelegate(final boolean isRefresh) {
        return new TwitterModel.TweetListQueryDelegate() {
            @Override
            public void onQueryComplete(List<Tweet> result) {
                if (result != null) {
                    if (isRefresh) {
                        mTweetsListAdapter.clear();
                    }

                    mTweetsListAdapter.addAll(result);
                    Toast.makeText(getApplicationContext(), "ADDED " + result.size() + " TWEETS", Toast.LENGTH_SHORT).show();
                }
                mSwipeContainer.setRefreshing(false);
            }
        };
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
