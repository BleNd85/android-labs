package com.example.lab1.DownloadService;

import android.content.Context;
import android.content.SharedPreferences;

public class DownloadPreferences {
    private static final String PREF_NAME = "download_preferences";
    private static final String KEY_DOWNLOAD_ID = "download_id";

    public static void setDownloadId(Context context, long downloadId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_DOWNLOAD_ID, downloadId);
        editor.apply();
    }

    public static long getDownloadId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_DOWNLOAD_ID, -1);
    }
}