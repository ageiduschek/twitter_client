package com.codepath.apps.twitterclient.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Model of a Tweet
 */
public class Tweet {
    private String body;
    private long uid;
    private User user;
    private String createdAt;

    public String getBody() {
        return body;
    }

    public long getUid() {
        return uid;
    }

    public User getUser() {
        return user;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public static Tweet fromJSON(JSONObject json) {
        Tweet tweet = new Tweet();
        try {
            tweet.body = json.getString("text");
            tweet.uid = json.getLong("id");
            tweet.user = User.fromJson(json.getJSONObject("user"));
            tweet.createdAt = json.getString("created_at");
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
                Log.d("ASDF", "trying tweet");
                Tweet tweet = Tweet.fromJSON(json.getJSONObject(i));
                if (tweet != null) {
                    Log.d("ASDF", "adding tweet");
                    tweets.add(tweet);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                // Don't crash on parse failure
            }
        }
        Log.d("ASDF", "SIZE FOUND: " + tweets.size());
        return tweets;
    }
}
