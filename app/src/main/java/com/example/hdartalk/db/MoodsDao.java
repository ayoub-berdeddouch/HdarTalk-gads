package com.example.hdartalk.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.hdartalk.model.Mood;

import java.util.List;

@Dao
public interface MoodsDao {

    /**
     * A Data Access Object (Dao) is the bridge between the
     * user attempting to interact with the lower-level
     * database and the raw database. The access object
     * allows you to perform operations, retrieve data etc.
     */
    @Query("SELECT * FROM moods")
    List<Mood> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Mood mood);

    @Insert
    void insertAll(Mood... moods);

    @Delete
    void delete(Mood mood);
}
