package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetObject;
import com.codepath.apps.restclienttemplate.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

public class ComposeActivity extends AppCompatActivity {

    private TwitterClient client;
    Context context;
    Button button;
    EditText etTweet;
    TextView tvChars;
    TextView tvScreenName;
    TextView tvName;
    TextView tvReply;
    TextView tvOldTweet;
    ImageView ivImage;
    ImageView ivExit;
    Intent intent;
    User myProfile;
    String method;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        button = findViewById(R.id.btSend);
        etTweet = findViewById(R.id.etTweet);
        tvChars = findViewById(R.id.tvChars);
        tvName = findViewById(R.id.tvName);
        tvOldTweet = findViewById(R.id.tvOldTweet);
        tvScreenName = findViewById(R.id.tvScreenName);
        tvReply = findViewById(R.id.tvReply);
        ivExit = findViewById(R.id.ivExit);
        ivImage = findViewById(R.id.ivProfile);

        myProfile = Parcels.unwrap(getIntent().getParcelableExtra(User.class.getSimpleName()));
        method = getIntent().getStringExtra("method");




        tvName.setText(myProfile.name);
        tvScreenName.setText("@" + myProfile.screenName);

        FloatingActionButton fab = findViewById(R.id.fab);
        context = ComposeActivity.this;
        Glide.with(context)
                .load(myProfile.profileImageUrl)
                .into(ivImage);
        client = new TwitterClient(context);
        final boolean new_tweet = newTweet(method);
        setHint(new_tweet);
        if (!new_tweet) {
            tvReply.setText(getIntent().getStringExtra("replier"));
            tvOldTweet.setText(getIntent().getStringExtra("old_tweet"));

        }

        etTweet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                tvChars.setText(String.format("%d", 280 - s.toString().length()));
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status = etTweet.getText().toString();

                if (new_tweet) {
                    client.sendTweet(status, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                Tweet newTwit = Tweet.fromJSON(response);
                                intent = new Intent(ComposeActivity.this, TimelineActivity.class);
                                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(newTwit));
                                TweetObject tw = new TweetObject(response.getString("id_str"), response.toString());
                                intent.putExtra(TweetObject.class.getSimpleName(), Parcels.wrap(tw));
                                context.startActivity(intent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            super.onFailure(statusCode, headers, responseString, throwable);
                        }
                    });
                } else {
                    client.sendReply(status, getIntent().getStringExtra("uid"), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                Tweet newTwit = Tweet.fromJSON(response);
                                intent = new Intent(ComposeActivity.this, TimelineActivity.class);
                                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(newTwit));
                                TweetObject tw = new TweetObject(response.getString("id_str"), response.toString());
                                intent.putExtra(TweetObject.class.getSimpleName(), Parcels.wrap(tw));
                                context.startActivity(intent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            super.onFailure(statusCode, headers, responseString, throwable);
                        }
                    });
                }

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        ivExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(ComposeActivity.this, TimelineActivity.class);
                context.startActivity(intent);
            }
        });
    }
    public boolean newTweet(String method) {
        return method.equals("new_tweet");
    }

    public void setHint(boolean new_tweet) {
        if (method.equals(new_tweet)) {
            etTweet.setHint("What's happening?");
            tvReply.setVisibility(View.GONE);
            tvOldTweet.setVisibility(View.GONE);
        }
        else {
            etTweet.setHint("Tweet your reply");
        }
    }

}
