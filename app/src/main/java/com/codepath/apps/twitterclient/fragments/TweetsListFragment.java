package com.codepath.apps.twitterclient.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.adapters.TweetsListAdapter;
import com.codepath.apps.twitterclient.helpers.EndlessScrollListener;
import com.codepath.apps.twitterclient.helpers.TwitterModel;
import com.codepath.apps.twitterclient.models.Tweet;

import java.util.ArrayList;
import java.util.List;

public abstract class TweetsListFragment extends Fragment {
    private TweetsListAdapter mTweetsListAdapter;
    private SwipeRefreshLayout mSwipeContainer;
    protected TwitterModel mTwitterModel;
    private boolean mReachedEndOfTimeline = false;

    // inflation logic
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_tweets_list, container, false);
        ListView lvTweets = (ListView) v.findViewById(R.id.lvTweets);

        lvTweets.setAdapter(mTweetsListAdapter);

        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                if (!mReachedEndOfTimeline) {
                    extendTimeline();
                    return true; // ONLY if more data is actually being loaded; false otherwise.
                }
                return false;
            }
        });

        mSwipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);

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

        // Refresh timeline once on create
        refreshTimeline();
        return v;
    }

    // Creation lifecycle event
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTwitterModel = ((TwitterApplication) getActivity().getApplication()).getTwitterModel();
        if (mTwitterModel == null) {
            throw new RuntimeException("TWITTER MODEL IS NULL");
        }
        mTweetsListAdapter = new TweetsListAdapter(getActivity(), new ArrayList<Tweet>());
    }

    public void refreshTimeline() {
        fetchNewestTweets(getLoadTweetsDelegate(true /*isRefresh*/));
    }

    private void extendTimeline() {
        fetchNextTweets(mTweetsListAdapter.getLastKnownId(),
                        getLoadTweetsDelegate(false /*isRefresh*/));
    }

    protected abstract void fetchNewestTweets(TwitterModel.OnGetFinishDelegate<List<Tweet>> delegate);
    protected abstract void fetchNextTweets(long lastId,
                                            TwitterModel.OnGetFinishDelegate<List<Tweet>> delegate);

    private TwitterModel.OnGetFinishDelegate<List<Tweet>> getLoadTweetsDelegate(final boolean isRefresh) {
        return new TwitterModel.OnGetFinishDelegate<List<Tweet>>() {
            @Override
            public void onQueryComplete(List<Tweet> result) {
                if (!isRefresh && result.size() < 25) {
                    mReachedEndOfTimeline = true;
                }
                onFinishHelper(result, 0);
            }

            @Override
            public void onNetworkFailure(List<Tweet> localOnlyResult, int errorMessage) {
                onFinishHelper(localOnlyResult, errorMessage);
            }

            public void onFinishHelper(List<Tweet> result, int errorMessage) {
                if (result != null) {
                    if (isRefresh) {
                        mTweetsListAdapter.clear();
                    }

                    mTweetsListAdapter.addAll(result);
                }
                mSwipeContainer.setRefreshing(false);

                if (errorMessage != 0) {
                    Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }
}
