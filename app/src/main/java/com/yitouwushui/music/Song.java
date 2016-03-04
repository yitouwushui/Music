package com.yitouwushui.music;

import java.io.Serializable;

/**
 * Created by yitouwushui on 2015/12/23.
 */
public class Song implements Serializable {

    long id;
    String data;
    String title;
    String artist;
    long duration;
    String album;

    public Song() {

    }

    public Song(long id, String data, String title,
                String artist, long duration, String album) {
        this.id = id;
        this.data = data;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.album = album;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getId() {
        return id;
    }

    public String getData() {
        return data;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public long getDuration() {
        return duration;
    }

    public String getAlbum() {
        return album;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", data='" + data + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", duration=" + duration +
                ", album='" + album + '\'' +
                '}';
    }
}
