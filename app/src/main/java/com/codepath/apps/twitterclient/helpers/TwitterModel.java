package com.codepath.apps.twitterclient.helpers;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.codepath.apps.twitterclient.R;
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

    public interface OnPostFinishDelegate {
        /**
         * Callback when results come back in query.
         */
        public void onQueryComplete(int errorMessage);
    }

    /**
     *
     * @param <T> The query result type
     */
    public interface OnGetFinishDelegate<T> {
        /**
         * Callback when results come back in query.
         * @param result Query result
         */
        public void onQueryComplete(T result);

        /**
         *
         * @param partialResult Local-only result
         * @param errorMessage String describing error
         */
        public void onIncompleteQuery(T partialResult, int errorMessage);
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
                                    final OnGetFinishDelegate<List<Tweet>> delegate) {
        if (newestId <= 0) {
            throw new RuntimeException("Invalid newestId");
        }

        mIOHandler.post(new GetTweetsBeforeTask(newestId, delegate));
    }

    public void getNewHomeTweets(final OnGetFinishDelegate<List<Tweet>> delegate) {
        mIOHandler.post(new GetNewTweetsTask(delegate));
    }


    public void postTweet(final String tweet, final OnPostFinishDelegate delegate) {
        if (!Util.isNetworkAvailable(mContext)) {
            Log.d("ASDF", "HERE 1");
            delegate.onQueryComplete(R.string.network_connection_error);
            return;
        }

        TwitterApplication.getRestClient().postTweet(tweet, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("ASDF", "HERE 2");
                delegate.onQueryComplete(0);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                int errorMessage = generalFailureResponse(errorResponse);
                Log.d("ASDF", "HERE 3");
                delegate.onQueryComplete(errorMessage);
            }
        });
    }


    private class GetNewTweetsTask extends GetQueryTask<List<Tweet>> {

        public GetNewTweetsTask(OnGetFinishDelegate<List<Tweet>> delegate) {
            super(delegate);
        }

        @Override
        protected List<Tweet> getLocalResult() {
            return getLocalTweetsPage(0 /* newestId */, PAGE_SIZE);
        }

        @Override
        protected boolean shouldSkipRemoteQuery(List<Tweet> localTweetsResult) {
            return false;
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
        protected void fetchRemoteResult(JsonHttpResponseHandler httpResponseHandler) {
            TwitterApplication.getRestClient().getHomeTweets(getMostRecentTweetId(),
                                                             0,
                                                             httpResponseHandler);
        }

        @Override
        protected List<Tweet> parseAndSaveRemoteResult(JSONArray resultJSON) {
            return Tweet.fromJsonArray(resultJSON);
        }
    }

    private class GetTweetsBeforeTask extends GetQueryTask<List<Tweet>> {
        // Return tweets posted before this tweet
        private long mNewestId;

        public GetTweetsBeforeTask(long newestId, OnGetFinishDelegate<List<Tweet>> delegate) {
            super(delegate);
            mNewestId = newestId;
        }
        @Override
        protected List<Tweet> getLocalResult() {
            return getLocalTweetsPage(mNewestId, PAGE_SIZE);
        }

        @Override
        protected boolean shouldSkipRemoteQuery(List<Tweet> localTweetsResult) {
            return localTweetsResult.size() == PAGE_SIZE;
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
        protected void fetchRemoteResult(JsonHttpResponseHandler httpResponseHandler) {
            TwitterApplication.getRestClient().getHomeTweets(0 /* sinceId */,
                                                             mNewestId,
                                                             httpResponseHandler);
        }

        @Override
        protected List<Tweet> parseAndSaveRemoteResult(JSONArray resultJSON) {
            return Tweet.fromJsonArray(resultJSON);
        }
    }


    private abstract class GetQueryTask<T> implements Runnable {
        protected abstract T getLocalResult();
        protected abstract boolean shouldSkipRemoteQuery(T localResult);
        protected abstract void fetchRemoteResult(JsonHttpResponseHandler httpResponseHandler);
        protected abstract T parseAndSaveRemoteResult(JSONArray resultJSON);
        protected abstract T mergeLocalAndRemoteResults(T localResult,
                                                        T remoteResult);

        private OnGetFinishDelegate<T> mDelegate;

        // Defines a Handler object that's attached to the creating thread. The response
        // task is posted to this handler
        private Handler mResponseHandler;

        public GetQueryTask(OnGetFinishDelegate<T> delegate) {
            mDelegate = delegate;
            mResponseHandler = new Handler();
        }

        public void run() {
            final T localTweets = getLocalResult();

            boolean isNetworkAvailable = Util.isNetworkAvailable(mContext);
            if (!isNetworkAvailable || shouldSkipRemoteQuery(localTweets)) {
                if (!isNetworkAvailable && !shouldSkipRemoteQuery(localTweets)){
                    postFailure(localTweets, R.string.network_connection_error);
                } else {
                    postSuccess(localTweets);

                }
                return;
            }

            fetchRemoteResult(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    T mergedResult = mergeLocalAndRemoteResults(localTweets,
                                                                parseAndSaveRemoteResult(response));
                    postSuccess(mergedResult);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    int errorMessage = generalFailureResponse(errorResponse);
                    postFailure(localTweets, errorMessage);
                }
            });
        }

        private void postSuccess(final T result) {
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDelegate.onQueryComplete(result);
                }
            });
        }

        private void postFailure(final T result, final int errorMessage) {
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDelegate.onIncompleteQuery(result, errorMessage);
                }
            });
        }
    }

    private long getMostRecentTweetId() {
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

    private List<Tweet> getLocalTweetsPage(long newestId, int limit) {
        Util.assertNotUIThread();
        return new Select()
                .from(Tweet.class)
                .where(newestId > 0 ? "remote_id < " + newestId : "")
                .orderBy("remote_id DESC")
                .limit(limit)
                .execute();
    }

    private int generalFailureResponse(JSONObject errorResponse) {
        int errorMessage = R.string.general_error;
        try {
            JSONArray errors = errorResponse.optJSONArray("errors");
            if (errors != null) {
                int code = errors.getJSONObject(0).getInt("code");

                if (code == 88) {
                    errorMessage = R.string.rate_limit_error;
                } else if (code == 215) {
                    errorMessage = R.string.bad_auth_error;
                } else if (code == 187) {
                    errorMessage = R.string.duplicate_status_error;
                } else {
                    errorMessage = R.string.network_connection_error;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (errorResponse != null) {
            Log.e("DEBUG", errorResponse.toString());
        }

        return errorMessage;
    }
}
