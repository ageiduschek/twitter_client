package com.codepath.apps.twitterclient.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.helpers.Util;
import com.codepath.apps.twitterclient.models.Tweet;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adapter for tweet list
 */
public class TweetsListAdapter extends ArrayAdapter<Tweet> {
    private class TweetSubViews {
        ImageView ivProfileImage;
        TextView tvScreenName;
        TextView tvTweetBody;
        TextView tvCreatedAt;
    }

    public TweetsListAdapter(Context context, List<Tweet> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TweetSubViews subViews;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tweet, parent, false);
            subViews = new TweetSubViews();
            subViews.ivProfileImage = (ImageView) convertView.findViewById(R.id.ivProfileImage);
            subViews.tvTweetBody = (TextView) convertView.findViewById(R.id.tvTweetBody);
            subViews.tvScreenName = (TextView) convertView.findViewById(R.id.tvScreenName);
            subViews.tvCreatedAt = (TextView) convertView.findViewById(R.id.tvCreatedAt);
            convertView.setTag(subViews);
        } else {
            subViews = (TweetSubViews) convertView.getTag();
        }

        Tweet tweet = getItem(position);
        subViews.tvTweetBody.setText(tweet.getBody());
        subViews.tvScreenName.setText(tweet.getUser().getScreenName());
        subViews.tvCreatedAt.setText(Util.getRelativeTimestamp(tweet.getCreatedAt()));

        Picasso.with(getContext()).load(tweet.getUser().getProfileImageUrl()).into(subViews.ivProfileImage);



        return convertView;
    }

    public long getLastKnownId() {
        if (getCount() == 0) {
            return 0;
        }

        return getItem(getCount()-1).getRemoteId();
    }
}
