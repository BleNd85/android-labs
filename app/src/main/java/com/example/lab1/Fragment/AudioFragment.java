package com.example.lab1.Fragment;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.lab1.Media.MediaAdapter;
import com.example.lab1.Media.MediaCache;
import com.example.lab1.Media.MediaItem;
import com.example.lab1.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioFragment extends Fragment {

    private RecyclerView recyclerView;
    private MediaAdapter adapter;
    private final List<MediaItem> audioList = new ArrayList<>();
    private LruCache<String, Bitmap> thumbnailCache;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AudioFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media, container, false);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        thumbnailCache = MediaCache.getCache();

        loadAudioFiles();

        adapter = new MediaAdapter(audioList);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            audioList.clear();
            loadAudioFiles();
            adapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadAudioFiles() {
        executor.execute(() -> {
            List<MediaItem> tempList = new ArrayList<>();
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
                        tempList.add(new MediaItem(name, contentUri.toString(), duration, null));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to load audio files", Toast.LENGTH_LONG).show();
            }

            mainHandler.post(() -> {
                audioList.clear();
                audioList.addAll(tempList);
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);

                for (int i = 0; i < audioList.size(); i++) {
                    final int position = i;
                    final MediaItem item = audioList.get(i);
                    executor.execute(() -> {
                        Bitmap thumbnail = createAudioThumbnail(Uri.parse(item.uri));
                        if (thumbnail != null) {
                            item.setThumbnail(thumbnail);
                            mainHandler.post(() -> adapter.notifyItemChanged(position));
                        }
                    });
                }
            });
        });
    }

    private Bitmap createAudioThumbnail(Uri uri) {
        String key = uri.toString();
        Bitmap cachedThumbnail = thumbnailCache.get(key);
        if (cachedThumbnail != null) {
            return cachedThumbnail;
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(requireContext(), uri);
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                thumbnailCache.put(key, bitmap);
                return bitmap;
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