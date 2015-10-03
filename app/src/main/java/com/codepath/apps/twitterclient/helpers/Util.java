package com.codepath.apps.twitterclient.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.text.format.DateUtils;
import android.util.Log;

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
}
