package com.example.lab1;

import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 100;
    private RecyclerView recyclerView;
    private MediaAdapter adapter;
    private final ArrayList<MediaItem> mediaList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (checkPermission()) {
            try {
                loadMediaFiles();
                loadAudioFiles();
                if (mediaList.isEmpty()) {
                    Toast.makeText(this, "No videos found", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load videos", Toast.LENGTH_LONG).show();
            }
        } else {
            requestPermission();
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_VIDEO)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_MEDIA_VIDEO},
                    REQUEST_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    loadMediaFiles();
                    loadAudioFiles();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load videos", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadMediaFiles() throws IOException {
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION
        };
        try (Cursor cursor = getContentResolver().query(
                videoUri,
                projection,
                null,
                null,
                MediaStore.Video.Media.DATE_ADDED + " DESC")) {

            if (cursor != null) {
                int idIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                int durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idIndex);
                    String name = cursor.getString(nameIndex);
                    long duration = cursor.getLong(durationIndex);

                    Uri contentUri = ContentUris.withAppendedId(videoUri, id);
                    Bitmap thumbnail = createVideoThumbnail(contentUri);
                    mediaList.add(new MediaItem(name, contentUri.toString(), duration, thumbnail));
                }
            }
        }

        adapter = new MediaAdapter(mediaList);
        recyclerView.setAdapter(adapter);
    }

    private void loadAudioFiles() throws IOException {
        Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };

        try (Cursor cursor = getContentResolver().query(
                audioUri,
                projection,
                null,
                null,
                MediaStore.Audio.Media.DATE_ADDED + " DESC")) {

            if (cursor != null) {
                int idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                int durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idIndex);
                    String name = cursor.getString(nameIndex);
                    long duration = cursor.getLong(durationIndex);

                    Uri contentUri = ContentUris.withAppendedId(audioUri, id);
                    Bitmap thumbnail = createAudioThumbnail(contentUri);
                    mediaList.add(new MediaItem(name, contentUri.toString(), duration, thumbnail));
                }
            }
        }
    }

    private Bitmap createVideoThumbnail(Uri uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, uri);
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                return BitmapFactory.decodeByteArray(art, 0, art.length);
            } else {
                return retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return null;
    }

    private Bitmap createAudioThumbnail(Uri uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, uri);
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                return BitmapFactory.decodeByteArray(art, 0, art.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return null;
    }
}
