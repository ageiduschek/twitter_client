package com.codepath.apps.twitterclient.helpers;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Layer that retrieves data from database or server, as appropriate
 */
public class TwitterModel {
    public interface TweetListQueryDelegate {
        /**
         * Callback when results come back in query. May get called more than once
         * @param result Query result, may be null
         */
        public void onQueryComplete(List<Tweet> result);
    }


    private final Context mContext;
    private final Handler mIOHandler;


    public TwitterModel(Context context) {
        mContext = context;

        // Create a new background thread for processing I/O
        HandlerThread mIOHandlerThread = new HandlerThread("IOHandlerThread");

        // Starts the background thread
        mIOHandlerThread.start();
        // Create a handler attached to the HandlerThread's Looper
        mIOHandler = new Handler(mIOHandlerThread.getLooper());
    }

    public static int PAGE_SIZE = 25;

    /**
     *
     * @param newestId Queries for results with an ID less than (that is, older than)
     *                 to the specified ID.
     * @param delegate handles the http response
     */
    public void getHomeTweetsBefore(long newestId,
                                    final TweetListQueryDelegate delegate) {
        if (newestId <= 0) {
            throw new RuntimeException("Invalid newestId");
        }

        mIOHandler.post(new GetTweetsBeforeTask(newestId, delegate));
    }

    public void getNewHomeTweets(final TweetListQueryDelegate delegate) {
        mIOHandler.post(new GetNewTweetsTask(delegate));
    }

    private class GetNewTweetsTask extends GetTweetsTask {

        public GetNewTweetsTask(TweetListQueryDelegate delegate) {
            super(delegate);
        }

        @Override
        protected List<Tweet> getLocalTweets() {
            return getLocalTweetsPage(0 /* newestId */, PAGE_SIZE);
        }

        @Override
        protected boolean shouldSkipRemoteQuery(List<Tweet> localTweetsResult) {
            return !Util.isNetworkAvailable(mContext);
        }

        @Override
        protected List<Tweet> mergeLocalAndRemoteResults(List<Tweet> localTweets,
                                                         List<Tweet> remoteTweets) {
            final List<Tweet> mergedTweets = new ArrayList<>();
            for (int i = 0; i < Math.min(remoteTweets.size(), PAGE_SIZE); i++) {
                mergedTweets.add(remoteTweets.get(i));
            }

            if (mergedTweets.size() < PAGE_SIZE) {
                // fill remaining from disk
                int tweetsNeeded = PAGE_SIZE - mergedTweets.size();
                for (int i = 0; i < Math.min(localTweets.size(), tweetsNeeded); i++) {
                    mergedTweets.add(localTweets.get(i));
                }
            }
            return mergedTweets;
        }

        @Override
        protected void getRemoteTweets(JsonHttpResponseHandler httpResponseHandler) {
            TwitterApplication.getRestClient().getHomeTweets(getMostRecentTweetId(),
                                                             0,
                                                             httpResponseHandler);
        }
    }

    private class GetTweetsBeforeTask extends GetTweetsTask {
        // Return tweets posted before this tweet
        private long mNewestId;

        public GetTweetsBeforeTask(long newestId, TweetListQueryDelegate delegate) {
            super(delegate);
            mNewestId = newestId;
        }
        @Override
        protected List<Tweet> getLocalTweets() {
            return getLocalTweetsPage(mNewestId, PAGE_SIZE);
        }

        @Override
        protected boolean shouldSkipRemoteQuery(List<Tweet> localTweetsResult) {
            return localTweetsResult.size() == PAGE_SIZE || !Util.isNetworkAvailable(mContext);
        }

        @Override
        protected List<Tweet> mergeLocalAndRemoteResults(List<Tweet> localTweets,
                                                         List<Tweet> remoteTweets) {
            int tweetsNeeded = localTweets.size() - PAGE_SIZE;
            for (int i = 0; i < Math.min(tweetsNeeded, remoteTweets.size()); i++) {
                localTweets.add(remoteTweets.get(i));
            }
            return localTweets;
        }

        @Override
        protected void getRemoteTweets(JsonHttpResponseHandler httpResponseHandler) {
            TwitterApplication.getRestClient().getHomeTweets(0 /* sinceId */,
                                                             mNewestId,
                                                             httpResponseHandler);
        }
    }


    private abstract class GetTweetsTask implements Runnable {
        protected abstract List<Tweet> getLocalTweets();
        protected abstract boolean shouldSkipRemoteQuery(List<Tweet> localTweetsResult);
        protected abstract List<Tweet> mergeLocalAndRemoteResults(List<Tweet> localTweets,
                                                                  List<Tweet> remoteTweets);

        protected abstract void getRemoteTweets(JsonHttpResponseHandler httpResponseHandler);

        private TweetListQueryDelegate mDelegate;

        // Defines a Handler object that's attached to the creating thread. The response
        // task is posted to this handler
        private Handler mResponseHandler;

        public GetTweetsTask(TweetListQueryDelegate delegate) {
            mDelegate = delegate;
            mResponseHandler = new Handler();
        }

        public void run() {
            final List<Tweet> localTweets = getLocalTweets();

            if (shouldSkipRemoteQuery(localTweets)) {
                postResult(localTweets);
                return;
            }

            getRemoteTweets(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    List<Tweet> mergedResult = mergeLocalAndRemoteResults(localTweets,
                                                                          Tweet.fromJsonArray(response));
                    postResult(mergedResult);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    generalFailureResponse(errorResponse);
                    postResult(localTweets);
                }
            });
        }

        private void postResult(final List<Tweet> tweets) {
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDelegate.onQueryComplete(tweets);
                }
            });
        }
    }

    public long getMostRecentTweetId() {
        Util.assertNotUIThread();
        List<Tweet> result = new Select()
                                .from(Tweet.class)
                                .orderBy("remote_id DESC")
                                .limit(1)
                                .execute();
        if (result.size() > 0) {
            return result.get(0).getRemoteId();
        }

        return 0;
    }

    public List<Tweet> getLocalTweetsPage(long newestId, int limit) {
        Util.assertNotUIThread();
        return new Select()
                .from(Tweet.class)
                .where(newestId > 0 ? "remote_id < " + newestId : "")
                .orderBy("remote_id DESC")
                .limit(limit)
                .execute();
    }

    private void generalFailureResponse(JSONObject errorResponse) {
        String errorMessage = "General Error";
        try {
            JSONArray errors = errorResponse.optJSONArray("errors");
            if (errors != null) {
                int code = errors.getJSONObject(0).getInt("code");

                if (code == 88) {
                    errorMessage = "You've reached your rate limit. Check back later.";
                } else if (code == 215) {
                    errorMessage = "Bad Authentication data";
                } else {
                    errorMessage = "Failed to load tweets. Check your network connection.";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(mContext,
                errorMessage,
                Toast.LENGTH_LONG).show();
        if (errorResponse != null) {
            Log.e("DEBUG", errorResponse.toString());
        }
    }
}
