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


    public Tweet() {
        // empty constructor for parcel
    }

    // deserialize the JSON
    public static Tweet fromJSON(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();

        // extract the values from JSON
        tweet.body = jsonObject.getString("text");
        tweet.uid = jsonObject.getLong("id");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJSON(jsonObject.getJSONObject("user"));
        tweet.retweets = jsonObject.getInt("retweet_count");
        tweet.like = jsonObject.getInt("favorite_count");
        JSONArray entityMedia;
        try {
            entityMedia = jsonObject.getJSONObject("entities").getJSONArray("media");

        } catch (Exception e) {
            entityMedia = null;
        }
        JSONObject media = entityMedia == null ? null : entityMedia.getJSONObject(0);
        tweet.embeddedMedia = media == null ? "" : media.getString("media_url_https");
        return tweet;
    }


}


