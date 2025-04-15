package com.example.lab1;

import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class VideoFragment extends Fragment {

    private RecyclerView recyclerView;
    private MediaAdapter adapter;
    private final List<MediaItem> videoList = new ArrayList<>();

    public VideoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadVideoFiles();

        adapter = new MediaAdapter(videoList);
        recyclerView.setAdapter(adapter);
    }

    private void loadVideoFiles() {
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION
        };

        try (Cursor cursor = requireContext().getContentResolver().query(
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
                    videoList.add(new MediaItem(name, contentUri.toString(), duration, thumbnail));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to load videos", Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap createVideoThumbnail(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(requireContext(), uri);
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                return android.graphics.BitmapFactory.decodeByteArray(art, 0, art.length);
            } else {
                return retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}