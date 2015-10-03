package com.codepath.apps.twitterclient.models;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.codepath.apps.twitterclient.helpers.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Model of a Tweet
 */
@Table(name = "Tweets")
public class Tweet extends Model {
    @Column(name = "remote_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long remoteId;
    @Column(name = "body")
    private String body;

    @Column(name = "user")
    private User user;

    @Column(name = "created_at")
    private long createdAt;


    public static Comparator<Tweet> sComparator = new Comparator<Tweet>() {
        @Override
        public int compare(Tweet lhs, Tweet rhs) {
            if (lhs.getRemoteId() > rhs.getRemoteId()) {
                return -1;
            }

            if (lhs.getRemoteId() == rhs.getRemoteId()) {
                return 0;
            }

            return 1;
        }
    };

    public String getBody() {
        return body;
    }

    public long getRemoteId() {
        return remoteId;
    }

    public User getUser() {
        return user;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Tweet(){
        super();
    }

    public static Tweet fromJSON(JSONObject json) {
        Tweet tweet = new Tweet();
        try {
            tweet.body = json.getString("text");
            tweet.remoteId = json.getLong("id");
            tweet.user = User.findOrCreateFromJson(json.getJSONObject("user"));
            tweet.createdAt = Util.twitterDateToMillseconds(json.getString("created_at"));
            tweet.save();
        } catch (JSONException e) {
            e.printStackTrace();
            // Don't crash on parse failure
            return null;
        }

        return tweet;
    }

    public static ArrayList<Tweet> fromJsonArray(JSONArray json) {
        ArrayList<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            try {
                Tweet tweet = Tweet.fromJSON(json.getJSONObject(i));
                if (tweet != null) {
                    tweets.add(tweet);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                // Don't crash on parse failure
            }
        }
        return tweets;
    }

    public static Comparator<Tweet> comparator() {
        return sComparator;
    }
}
