package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {

    private List<Tweet> tweets;
    Context context;
    User myProfile;

    // pass in the tweets array in the constructor
    public TweetAdapter(List<Tweet> tweets) {
        this.tweets = tweets;
    }


    // for each row, inflate the layout and cache references into Viewholder

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View tweetView = inflater.inflate(R.layout.item_tweet, parent, false);
        ViewHolder viewHolder = new ViewHolder(tweetView);
        return viewHolder;
    }

    // bind the values based on the position of the element

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        // get according to position
        final Tweet tweet = tweets.get(position);

        // populate teh views according to data
        holder.tvUserName.setText(tweet.user.name);
        holder.tvScreenName.setText("@" + tweet.user.screenName);
        holder.tvBody.setText(tweet.body);
        holder.tvDate.setText(getRelativeTimeAgo(tweet.createdAt));
        if (tweet.like > 0)
            holder.tvLike.setVisibility(View.VISIBLE);
        else
            holder.tvLike.setVisibility(View.INVISIBLE);
        holder.tvLike.setText(String.format("%d",tweet.like));
        if (tweet.retweets > 0)
            holder.tvRetweet.setVisibility(View.VISIBLE);
        else
            holder.tvRetweet.setVisibility(View.INVISIBLE);

        holder.tvRetweet.setText(String.format("%d",tweet.retweets));

        if (!tweet.embeddedMedia.equals("")) {
            Glide.with(context)
                    .load(tweet.embeddedMedia)
                    .into(holder.ivMedia);
        } else {
            holder.ivMedia.setVisibility(View.GONE);
        }
        TwitterClient client = new TwitterClient(context);

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

        holder.ivReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ComposeActivity.class);
                intent.putExtra(User.class.getSimpleName(), Parcels.wrap(myProfile));
                intent.putExtra("method", "reply");
                intent.putExtra("replier", holder.tvScreenName.getText());
                intent.putExtra("old_tweet", tweet.body);
                intent.putExtra("uid", tweet.uid);
                context.startActivity(intent);
            }
        });






        Glide.with(context)
                .load(tweet.user.profileImageUrl)
                .asBitmap()
                .centerCrop()
                .into(new BitmapImageViewTarget(holder.ivProfileImage){
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                holder.ivProfileImage.setImageDrawable(circularBitmapDrawable);
            }
        });
    }

    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }


    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // create ViewHolder class


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivProfileImage;
        public ImageView ivMedia;
        public ImageView ivReply;
        public TextView tvScreenName;
        public TextView tvUserName;
        public TextView tvBody;
        public TextView tvDate;
        public TextView tvLike;
        public TextView tvRetweet;


        public ViewHolder(View itemView) {
            super(itemView);

            // do lookups

            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvUserName = itemView.findViewById(R.id.tvName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLike = itemView.findViewById(R.id.tvLike);
            tvRetweet = itemView.findViewById(R.id.tvRetweet);
            ivReply = itemView.findViewById(R.id.ivComment);
        }

    }
}
