package com.codepath.apps.twitterclient.fragments;

import android.os.Bundle;

import com.codepath.apps.twitterclient.helpers.TwitterModel;
import com.codepath.apps.twitterclient.models.Tweet;

import java.util.List;

public class MentionsTimelineFragment extends TweetsListFragment {

    public static MentionsTimelineFragment newInstance() {

        Bundle args = new Bundle();

        MentionsTimelineFragment fragment = new MentionsTimelineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void fetchNewestTweets(TwitterModel.OnGetFinishDelegate<List<Tweet>> delegate) {
        mTwitterModel.getNewestMentions(delegate);
    }

    @Override
    protected void fetchNextTweets(long lastId, TwitterModel.OnGetFinishDelegate<List<Tweet>> delegate) {
        mTwitterModel.getMentionsBefore(lastId, delegate);
    }
}
