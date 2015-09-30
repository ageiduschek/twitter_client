package com.codepath.apps.twitterclient.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.TwitterClient;
import com.codepath.apps.twitterclient.adapters.TweetsListAdapter;
import com.codepath.apps.twitterclient.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TweetFeedActivity extends AppCompatActivity {

    private TwitterClient mClient;
    private ArrayList<Tweet> mTweets;
    private TweetsListAdapter mTweetsListAdapter;
    private ListView lvTweets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_feed);

        lvTweets = (ListView) findViewById(R.id.lvTweets);
        mTweets = new ArrayList<>();
        mTweetsListAdapter = new TweetsListAdapter(this, mTweets);
        lvTweets.setAdapter(mTweetsListAdapter);

        mClient = TwitterApplication.getRestClient();
        populateTimeline();
    }

    private void populateTimeline() {
        mClient.getHomeTweets(new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Toast.makeText(getApplicationContext(),
                        "SUCCESS",
                        Toast.LENGTH_LONG).show();
                Log.e("ASDF", response.toString());
                mTweetsListAdapter.addAll(Tweet.fromJsonArray(response));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                String errorMessage = "General Error";
                try {
                    JSONArray errors = errorResponse.getJSONArray("errors");
                    int code = errors.getJSONObject(0).getInt("code");

                    if (code == 88) {
                        errorMessage = "You've reached your rate limit. Check back later.";
                    } else if (code == 215) {
                        errorMessage = "Bad Authentication data";
                    } else {
                        errorMessage = "Failed to load tweets. Check your network connection.";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Toast.makeText(getApplicationContext(),
                        errorMessage,
                        Toast.LENGTH_LONG).show();
                Log.e("DEBUG", errorResponse.toString());
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tweet_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
