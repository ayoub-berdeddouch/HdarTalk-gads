package com.example.hdartalk.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "moods")
public class Mood {
    @PrimaryKey(autoGenerate = true)
    public int mid;

    @ColumnInfo(name = "date")
    public long  mooddate;

    @ColumnInfo(name = "value")
    public int value;

    @ColumnInfo (name="severity_level")
    public int severityLevel;

    public Mood() {
    }

    public Mood(long mooddate, int value, int severityLevel) {
        this.mooddate = mooddate;
        this.value = value;
        this.severityLevel= severityLevel;
    }

    public int getMid() { return mid; }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public long getMooddate() {
        return mooddate;
    }

    public void setMooddate(long mooddate) {
        this.mooddate = mooddate;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(int severityLevel) {
        this.severityLevel = severityLevel;
    }

    @Override
    public String toString() {
        return "Mood{" +
                "mid=" + mid +
                ", mooddate=" + mooddate +
                ", value=" + value +
                ", severityLevel=" + severityLevel +
                '}';
    }
}
