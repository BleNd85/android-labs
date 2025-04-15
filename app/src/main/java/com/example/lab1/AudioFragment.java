package com.example.lab1;

import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class AudioFragment extends Fragment {

    private RecyclerView recyclerView;
    private MediaAdapter adapter;
    private final List<MediaItem> audioList = new ArrayList<>();

    public AudioFragment() {

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

        loadAudioFiles();

        adapter = new MediaAdapter(audioList);
        recyclerView.setAdapter(adapter);
    }

    private void loadAudioFiles() {
        Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };

        try (Cursor cursor = requireContext().getContentResolver().query(
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
                    audioList.add(new MediaItem(name, contentUri.toString(), duration, thumbnail));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to load audio files", Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap createAudioThumbnail(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(requireContext(), uri);
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                return BitmapFactory.decodeByteArray(art, 0, art.length);
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