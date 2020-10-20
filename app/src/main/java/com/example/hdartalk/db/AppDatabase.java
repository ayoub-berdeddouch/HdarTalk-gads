package com.example.hdartalk.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.hdartalk.model.Mood;
import com.example.hdartalk.model.Note;
import com.example.hdartalk.model.Resource;

@Database(entities = {Note.class, Mood.class, Resource.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {


    private static AppDatabase instance;
    public static final String DATABASE_NAME = "talkdatabase";

    public abstract NotesDao notesDao();
    public abstract MoodsDao moodsDao();
    public abstract ResourcesDao resourcesDao();

    public static AppDatabase getDatabase(final Context context) {
        if (instance == null)
            instance = Room.databaseBuilder(
                    context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build();
        return instance;
    }
}