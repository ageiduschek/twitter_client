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
    @Column(name = "tag_line")
    private String tagLine;
    @Column(name = "num_followers")
    private long numFollowers;
    @Column(name = "num_following")
    private long numFollowing;

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

    public String getTagLine() {
        return tagLine;
    }

    public long getNumFollowers() {
        return numFollowers;
    }

    public long getNumFollowing() {
        return numFollowing;
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
            user.tagLine = json.getString("description");
            user.numFollowers = Long.parseLong(json.getString("followers_count"));
            user.numFollowing = Long.parseLong(json.getString("friends_count"));
            user.save();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    public static User lookupWithId(long id) {
        Log.d("ASDF", "lookupWithId: " + id);
        return new Select()
                .from(User.class)
                .where("remote_id = " + id)
                .executeSingle();
    }
}
