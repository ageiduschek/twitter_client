package com.codepath.apps.twitterclient.fragments;

import android.os.Bundle;

import com.codepath.apps.twitterclient.helpers.TwitterModel;
import com.codepath.apps.twitterclient.models.Tweet;

import java.util.List;

public class UserTimelineFragment extends TweetsListFragment {

    private static final String USER_ID_KEY = "user_id_key";
    public static UserTimelineFragment newInstance(long userId) {
        
        Bundle args = new Bundle();
        args.putLong(USER_ID_KEY, userId);
        
        UserTimelineFragment fragment = new UserTimelineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void fetchNewestTweets(TwitterModel.OnGetFinishDelegate<List<Tweet>> delegate) {
        mTwitterModel.getNewestUserTweets(getArguments().getLong(USER_ID_KEY), delegate);
    }

    @Override
    protected void fetchNextTweets(long lastId, TwitterModel.OnGetFinishDelegate<List<Tweet>> delegate) {
        mTwitterModel.getUserTweetsBefore(getArguments().getLong(USER_ID_KEY), lastId, delegate);
    }
}
