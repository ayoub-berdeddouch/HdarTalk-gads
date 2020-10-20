package com.example.hdartalk.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "resources")
public class Resource {

    @PrimaryKey(autoGenerate = true)
    public int rid;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "hyperlink")
    public String hyperlink;

    @ColumnInfo(name = "mood")
    public int resmood;


    public Resource(){

    }
    public Resource(String title, String content, String hyperlink, int resmood) {
        this.title = title;
        this.content = content;
        this.hyperlink = hyperlink;
        this.resmood = resmood;
    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getHyperlink() {
        return hyperlink;
    }

    public void setHyperlink(String hyperlink) {
        this.hyperlink = hyperlink;
    }

    public int getResMood() {
        return resmood;
    }

    public void setResMood(int mood) {
        this.resmood = resmood;
    }


    @Override
    public String toString() {
        return "Resources{" +
                "rid=" + rid +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", hyperlink='" + hyperlink + '\'' +
                ", resmood=" + resmood +
                '}';
    }
}
