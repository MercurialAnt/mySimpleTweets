package com.codepath.apps.restclienttemplate.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class Tweet {

    public String body;
    public long uid;
    public User user;
    public String createdAt;
    public String embeddedMedia;
    public int like;
    public int retweets;
    public boolean isRetweet;
    public String old_name;


    public Tweet() {
        // empty constructor for parcel
    }

    // deserialize the JSON
    public static Tweet fromJSON(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();

        // extract the values from JSON
        tweet.isRetweet = jsonObject.getString("text").startsWith("RT");
        JSONObject twit_body;
        if (tweet.isRetweet) {
            twit_body = jsonObject.getJSONObject("retweeted_status");
            tweet.old_name = jsonObject.getJSONObject("user").getString("name");
        } else {
            twit_body = jsonObject;
            tweet.old_name = "";
        }
        tweet.body = twit_body.getString("text");
        tweet.uid = twit_body.getLong("id");
        tweet.createdAt = twit_body.getString("created_at");
        tweet.user = User.fromJSON(twit_body.getJSONObject("user"));
        tweet.retweets = twit_body.getInt("retweet_count");
        tweet.like = twit_body.getInt("favorite_count");
        JSONArray entityMedia;
        try {
            entityMedia = twit_body.getJSONObject("entities").getJSONArray("media");

        } catch (Exception e) {
            entityMedia = null;
        }
        JSONObject media = entityMedia == null ? null : entityMedia.getJSONObject(0);
        tweet.embeddedMedia = media == null ? "" : media.getString("media_url_https");
        return tweet;
    }


}


