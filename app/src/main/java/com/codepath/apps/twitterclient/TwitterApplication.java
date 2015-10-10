package com.codepath.apps.twitterclient;

import android.content.Context;

import com.codepath.apps.twitterclient.helpers.TwitterModel;

/*
 * This is the Android application itself and is used to configure various settings
 * including the image cache in memory and on disk. This also adds a singleton
 * for accessing the relevant rest client.
 *
 *     RestClient client = RestApplication.getRestClient();
 *     // use client to send requests to API
 *
 */
public class TwitterApplication extends com.activeandroid.app.Application {
	private static Context context;
	private TwitterModel mTwitterModel;

	@Override
	public void onCreate() {
		super.onCreate();
		TwitterApplication.context = this;
	}

	public void onLogin() {
		mTwitterModel = new TwitterModel(context);
	}

	public void onLogout() {
		mTwitterModel = null;
	}

	public TwitterModel getTwitterModel() {
		return mTwitterModel;
	}
}