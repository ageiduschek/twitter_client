package com.codepath.apps.twitterclient.models;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Model of a Twitter user
 */
@Table(name = "Users")
public class User extends Model {
    @Column(name = "remote_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long remoteId;
    @Column(name = "name")
    private String name;
    @Column(name = "screen_name")
    private String screenName;
    @Column(name = "profile_image_url")
    private String profileImageUrl;

    public String getName() {
        return name;
    }

    public long getRemoteId() {
        return remoteId;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public User() {
        super();
    }

    public static User createOrUpdateFromJSON(JSONObject json) {
        long remoteId;
        try {
            remoteId = json.getLong("id"); // get just the remote id
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        User user = new Select().from(User.class).where("remote_id = ?", remoteId).executeSingle();
        if (user == null) {
            user = new User();
        }

        try {
            user.name = json.getString("name");
            user.remoteId = json.getLong("id");
            user.screenName = json.getString("screen_name");
            user.profileImageUrl = json.getString("profile_image_url");
            user.save();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return user;
    }
}
