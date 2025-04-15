package com.example.lab1;

import android.graphics.Bitmap;

public class MediaItem {
    public String name;
    public String uri;
    public Long duration;
    public Bitmap thumbnail;

    public MediaItem(String name, String uri, Long duration, Bitmap thumbnail) {
        this.name = name;
        this.uri = uri;
        this.duration = duration;
        this.thumbnail = thumbnail;
    }
}
