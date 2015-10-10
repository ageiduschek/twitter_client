package com.codepath.apps.twitterclient.models;

import android.content.Context;

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

    @Column(name = "mentions_me")
    private boolean mentionsMe;

    @Column(name = "author_id")
    private long authorId;


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

    public static Tweet fromJSON(Context context, JSONObject json) {
        Tweet tweet = new Tweet();
        try {
            tweet.body = json.getString("text");
            tweet.remoteId = json.getLong("id");
            tweet.user = User.createOrUpdateFromJSON(json.getJSONObject("user"));
            tweet.createdAt = Util.twitterDateToMillseconds(json.getString("created_at"));
            tweet.mentionsMe = tweetMentionsMe(context, json);
            JSONObject tweetAuthor = json.getJSONObject("user");
            tweet.authorId = Long.parseLong(tweetAuthor.getString("id_str"));
            tweet.save();
        } catch (JSONException e) {
            e.printStackTrace();
            // Don't crash on parse failure
            return null;
        }

        return tweet;
    }

    public static ArrayList<Tweet> fromJsonArray(Context context, JSONArray json) {
        ArrayList<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            try {
                Tweet tweet = Tweet.fromJSON(context, json.getJSONObject(i));
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

    public static boolean tweetMentionsMe(Context context, JSONObject json) {
        try {
            long myUserId = Util.getUserId(context);
            JSONObject entities = json.getJSONObject("entities");
            JSONArray mentions = entities.getJSONArray("user_mentions");
            for (int i = 0; i < mentions.length(); i++) {
                if (myUserId == mentions.getJSONObject(i).getLong("id")) {
                    return true;
                }
            }
            return false;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
