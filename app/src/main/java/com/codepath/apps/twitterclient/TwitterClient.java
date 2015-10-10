package com.codepath.apps.twitterclient;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

import android.content.Context;
import android.util.Log;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
    private static final String TAG = TwitterClient.class.getSimpleName();

	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class;
	public static final String REST_URL = "https://api.twitter.com/1.1/";
	public static final String REST_CONSUMER_KEY = "nDjrOBKfwhF2fFeZLqMeNKeHH";
	public static final String REST_CONSUMER_SECRET = "yuYSzro48s6D9ciQZMmsq21V6UGedEzw9cfjCmfREQyVaQB7KU";
	public static final String REST_CALLBACK_URL = "oauth://cpsimpletweets";

	public TwitterClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

	/* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
	 * 	  i.e getApiUrl("statuses/home_timeline.json");
	 * 2. Define the parameters to pass to the request (query or body)
	 *    i.e RequestParams params = new RequestParams("foo", "bar");
	 * 3. Define the request method and make a call to the client
	 *    i.e client.get(apiUrl, params, handler);
	 *    i.e client.post(apiUrl, params, handler);
	 */


	private static final int RESULTS_PER_PAGE = 100;
    private static final String HOME_TIMELINE_NAME = "home_timeline";
    private static final String MENTIONS_TIMELINE_NAME = "mentions_timeline";
    private static final String USER_TIMELINE_NAME = "user_timeline";


	public void getHomeTweets(long sinceId, long maxId, AsyncHttpResponseHandler handler) {
        getTimeLine(HOME_TIMELINE_NAME, sinceId, maxId, null /*user_id*/, handler);
    }

    public void getMentionsTimeline(long sinceId, long maxId, AsyncHttpResponseHandler handler) {
        getTimeLine(MENTIONS_TIMELINE_NAME, sinceId, maxId, null /*user_id*/, handler);
    }

    public void getUserTimeline(long userId, long sinceId, long maxId, AsyncHttpResponseHandler handler) {
        getTimeLine(USER_TIMELINE_NAME, sinceId, maxId,  userId, handler);
    }

    /**
     *
     * @param maxId If greater than 0, indicates that we are querying for results with an ID
     *              less than (that is, older than) to the specified ID.
     * @param handler handles the http response
     */
    private void getTimeLine(String timelineName, long sinceId, long maxId, Long userId, AsyncHttpResponseHandler handler) {
        if (sinceId > 0 && maxId > 0) {
            throw new RuntimeException("Can't define since_id and max_id simultaneously");
        }

        String apiUrl = getApiUrl("/statuses/" + timelineName + ".json");
        RequestParams params = new RequestParams();
        params.put("count", Integer.toString(RESULTS_PER_PAGE));

        if (sinceId > 0) {
            params.put("since_id", Long.toString(sinceId));
        }

        if (maxId > 0) {
            params.put("max_id", Long.toString(maxId + 1));
        }

        if (userId != null) {
            params.put("user_id", Long.toString(userId));
        }

        // Include mentions
        if (userId == null) {
            params.put("include_entities", "true");
        }

        Log.w(TAG, "ASDF MAKING A NETWORK CALL TO GET TWEETS");
        Log.w(TAG, "ASDF: " + apiUrl);
        Log.w(TAG, "ASDF: params: " + params.toString());
        getClient().get(apiUrl, params, handler);
    }

    public void postTweet(String tweet, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("/statuses/update.json");
        RequestParams params = new RequestParams();
        params.put("status", tweet);
        getClient().post(apiUrl, params, handler);
    }

    public void verifyCredentials(AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("/account/verify_credentials.json");
        RequestParams params = new RequestParams();
        getClient().get(apiUrl, params, handler);
    }
}