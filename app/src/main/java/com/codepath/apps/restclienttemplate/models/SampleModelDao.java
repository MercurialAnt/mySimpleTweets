package com.codepath.apps.restclienttemplate.models;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SampleModelDao {

    // @Query annotation requires knowing SQL syntax
    // See http://www.sqltutorial.org/


    @Query("SELECT * FROM TweetObject")
    List<TweetObject> recentItems();


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTweet(TweetObject... recentTweets);

    @Query("SELECT * FROM TweetObject")
    TweetObject dropTable();


}
