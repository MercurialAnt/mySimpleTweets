package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codepath.apps.restclienttemplate.models.SampleModelDao;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetObject;
import com.codepath.apps.restclienttemplate.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {

    private  TwitterClient client;
    TweetAdapter tweetAdapter;
    ArrayList<Tweet> tweets;
    RecyclerView rvTweets;
    MenuItem miActionProgressItem;
    SampleModelDao sampleModelDao;
    User myProfile;

    private final int REQUEST_CODE = 20;
    private final int REPLY_CODE = 30;
    SwipeRefreshLayout swipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        client = TwitterApp.getRestClient(getApplicationContext());
        // find the RecyclerView
        rvTweets = findViewById(R.id.rvTweet);
        // init the arraylist
        tweets = new ArrayList<>();
        // construct the adapter from this datasource
        tweetAdapter = new TweetAdapter(tweets);
        // RecyclerView setup (layoutmanager, use adapter)
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        rvTweets.setAdapter(tweetAdapter);

        sampleModelDao = ((TwitterApp) getApplicationContext()).getMyDatabase().sampleModelDao();

        client.getUserInfo(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    myProfile = User.fromJSON(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });


        populateTimeline();
        swipe = findViewById(R.id.swipeToRefresh);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("Refresh", "why");
                clear();
                populateTimeline();
                swipe.setRefreshing(false);
            }
        });

    }

    public void clear() {
        tweets.clear();
        tweetAdapter.notifyDataSetChanged();
    }

    public void onClickReply() {
        intentToCompose(REPLY_CODE, "reply");
    }

    public void onClickBtn(MenuItem mi) {
        intentToCompose(REQUEST_CODE, "new_tweet");
    }

    public void intentToCompose(int request, String type) {
        Intent intent = new Intent(TimelineActivity.this, ComposeActivity.class);
        intent.putExtra(User.class.getSimpleName(), Parcels.wrap(myProfile));
        intent.putExtra("method", type);
        startActivityForResult(intent, request);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.miCompose:
                onClickBtn(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // REQUEST_CODE is defined above
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            // Extract name value from result extras
            Tweet newTwit = Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));
            final TweetObject newTw = Parcels.unwrap(getIntent().getParcelableExtra(TweetObject.class.getSimpleName()));

            tweets.add(0, newTwit);
            tweetAdapter.notifyItemInserted(0);
            rvTweets.scrollToPosition(0);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    sampleModelDao.insertTweet(newTw);
                }
            });


        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        // Extract the action-view from the menu item
        Toast.makeText(this, "aad", Toast.LENGTH_LONG);
        ProgressBar v = (ProgressBar) MenuItemCompat.getActionView(miActionProgressItem);

        // Return to finish
        return super.onPrepareOptionsMenu(menu);
    }

    public void showProgressBar() {
        // Show progress item
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        // Hide progress item
        miActionProgressItem.setVisible(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }


    private void populateTimeline() {
//        showProgressBar();
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("TwitterClient", response.toString());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONArray response) {

                final ArrayList<TweetObject> twOjs = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    // convert each obj to a tweet model
                    // add that tweet model to our data source
                    // notify the adapter model to our data source
                    Tweet tweet = null;
                    try {
                        JSONObject obj = response.getJSONObject(i);
                        TweetObject tw = new TweetObject(obj.getString("id_str"), obj.toString());
                        twOjs.add(tw);
                        tweet = Tweet.fromJSON(obj);
                        tweets.add(tweet);
                        tweetAdapter.notifyItemInserted(tweets.size() - 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // database stuff (removing old tweets and adding the new ones)
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        ((TwitterApp) getApplicationContext()).getMyDatabase().runInTransaction(new Runnable() {
                            @Override
                            public void run() {
                                sampleModelDao.dropTable();
                                for (TweetObject tw : twOjs) {
                                    sampleModelDao.insertTweet(tw);
                                }

                            }
                        });
                    }
                });


//                hideProgressBar();
//                Log.d("TwitterClient", response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TwitterClient", responseString);
                throwable.printStackTrace();
//                hideProgressBar();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
//                hideProgressBar();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(), "Couldn't connect to the network", Toast.LENGTH_LONG).show();

                Log.d("TwitterClient", "ahhahas");
                new asyncData(sampleModelDao, tweetAdapter, tweets).execute();

                throwable.printStackTrace();
                // retrieve tweet from data base
//                hideProgressBar();
            }
        });
    }


}
class asyncData extends AsyncTask {
    SampleModelDao sampleModelDao;
    ArrayList<TweetObject> twObj;
    TweetAdapter tweetAdapter;
    ArrayList<Tweet> tweets;

    public asyncData(SampleModelDao sampleModelDao, TweetAdapter tweetAdapter, ArrayList<Tweet> tweets) {
        this.sampleModelDao = sampleModelDao;
        this.tweetAdapter = tweetAdapter;
        this.tweets = tweets;
    }
    @Override
    protected Object doInBackground(Object[] objects) {
        twObj = (ArrayList) sampleModelDao.recentItems();
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        if (!twObj.isEmpty()) {
            populateFromDatabase(twObj);
        }
    }

    public void populateFromDatabase(ArrayList<TweetObject> list) {
        for (TweetObject twObj : list) {
            try {
                JSONObject obj = new JSONObject(twObj.getJsonTweet());
                tweets.add(Tweet.fromJSON(obj));
                tweetAdapter.notifyItemInserted(tweets.size() - 1);
            } catch (JSONException e) {
                Log.e("database", "error in trying to create tweet from the database");
            }
        }
    }
}
