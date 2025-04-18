package com.example.lab1.Media;

import android.graphics.Bitmap;
import android.util.LruCache;

public class MediaCache {
    private static LruCache<String, Bitmap> thumbnailCache;

    public static LruCache<String, Bitmap> getCache() {
        if (thumbnailCache == null) {
            int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            int cacheSize = maxMemory / 16;
            thumbnailCache = new LruCache<>(cacheSize);
        }
        return thumbnailCache;
    }
}
