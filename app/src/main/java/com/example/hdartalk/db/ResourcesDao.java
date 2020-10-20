package com.example.hdartalk.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.hdartalk.model.Resource;

import java.util.List;

@Dao
public interface ResourcesDao {

    /**
     * A Data Access Object (Dao) is the bridge between the
     * user attempting to interact with the lower-level
     * database and the raw database. The access object
     * allows you to perform operations, retrieve data etc.
     */
    @Query("SELECT * FROM resources")
    List<Resource> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Resource resources);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Resource... resources);

    @Delete
    void delete(Resource resource);
}
