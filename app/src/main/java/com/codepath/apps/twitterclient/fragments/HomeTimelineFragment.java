package com.codepath.apps.twitterclient.fragments;


import android.os.Bundle;

import com.codepath.apps.twitterclient.helpers.TwitterModel;
import com.codepath.apps.twitterclient.models.Tweet;

import java.util.List;

public class HomeTimelineFragment extends TweetsListFragment {

    public static HomeTimelineFragment newInstance() {

        Bundle args = new Bundle();

        HomeTimelineFragment fragment = new HomeTimelineFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    protected void fetchNewestTweets(TwitterModel.OnGetFinishDelegate<List<Tweet>> delegate) {
        mTwitterModel.getNewestHomeTweets(delegate);
    }

    @Override
    protected void fetchNextTweets(long lastId, TwitterModel.OnGetFinishDelegate<List<Tweet>> delegate) {
        mTwitterModel.getHomeTweetsBefore(lastId,
                                          delegate);
    }
}
