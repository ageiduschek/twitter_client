package com.codepath.apps.twitterclient.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

import com.codepath.apps.twitterclient.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Util functions
 */
public final class Util {
    public static long twitterDateToMillseconds(String dateStr){

        String dateFormatStr="EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(dateFormatStr, Locale.getDefault());
        sf.setLenient(true);
        try {
            return sf.parse(dateStr).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse date string");
        }
    }

    public static String getRelativeTimestamp(long timeMs) {
        if (System.currentTimeMillis() - timeMs <= DateUtils.MINUTE_IN_MILLIS) {
            return "Just now";
        }

        return DateUtils.getRelativeTimeSpanString(timeMs).toString();
    }

    public static void assertNotUIThread() {
        if ((Looper.getMainLooper().getThread() == Thread.currentThread())) {
            throw new RuntimeException("ASSERT NOT MAIN THREAD: FAILED");
        }
    }

    public static void assertUIThread() {
        if ((Looper.getMainLooper().getThread() != Thread.currentThread())) {
            throw new RuntimeException("ASSERT MAIN THREAD: FAILED");
        }
    }

    public static Boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private static String USER_ID_KEY = "account_user_id";
    public static long getUserId(Context context) {
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(context);
        long id = pref.getLong(USER_ID_KEY, -1);
        if (id == -1) {
            throw new RuntimeException("User id does not exist in SharedPreferences");
        }
        return id;
    }

    public static void setLoggedInUserInfo(Context context, JSONObject userJSON) {
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = pref.edit();
        User user = User.createOrUpdateFromJSON(userJSON);
        edit.putLong(USER_ID_KEY, user.getRemoteId());
        edit.commit();
    }
}
