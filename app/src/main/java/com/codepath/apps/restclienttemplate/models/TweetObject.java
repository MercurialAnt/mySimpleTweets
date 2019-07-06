package com.codepath.apps.restclienttemplate.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.parceler.Parcel;

@Parcel
@Entity
public class TweetObject {

    @ColumnInfo
    public String jsonTweet;

    @NonNull
    @PrimaryKey
    public String id;

    public TweetObject() {
        // for parcel
    }

    // This model will be used to store tweets offline.
    public TweetObject(String id, String jsonTweet) {
        this.id = id;
        this.jsonTweet = jsonTweet;
    }

    public String getJsonTweet() {
        return jsonTweet;
    }

    public String getId() {
        return id;
    }
}
