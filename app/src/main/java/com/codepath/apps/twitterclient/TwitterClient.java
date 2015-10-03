package com.codepath.apps.twitterclient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

import android.content.Context;

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

    /**
     *
     * @param maxId If greater than 0, indicates that we are querying for results with an ID
     *              less than (that is, older than) to the specified ID.
     * @param handler handles the http response
     */
	public void getHomeTweets(long sinceId, long maxId, AsyncHttpResponseHandler handler) {
		if (sinceId > 0 && maxId > 0) {
            throw new RuntimeException("Can't define since_id and max_id simultaneously");
        }

        String apiUrl = getApiUrl("/statuses/home_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", Integer.toString(RESULTS_PER_PAGE));

        if (sinceId > 0) {
            params.put("since_id", Long.toString(sinceId));
        }

        if (maxId > 0) {
            params.put("max_id", Long.toString(maxId + 1));
        }
		getClient().get(apiUrl, params, handler);
    }
}