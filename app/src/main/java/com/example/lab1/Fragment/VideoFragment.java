package com.example.lab1.Fragment;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.LruCache;

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

public class VideoFragment extends Fragment {

    private RecyclerView recyclerView;
    private MediaAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final List<MediaItem> videoList = new ArrayList<>();
    private LruCache<String, Bitmap> thumbnailCache;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public VideoFragment() {
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

        loadVideoFiles();

        adapter = new MediaAdapter(videoList);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            videoList.clear();
            loadVideoFiles();
            adapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadVideoFiles() {
        executor.execute(() -> {
            List<MediaItem> tempList = new ArrayList<>();
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
                        MediaItem item = new MediaItem(name, contentUri.toString(), duration, null);
                        tempList.add(item);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            mainHandler.post(() -> {
                videoList.clear();
                videoList.addAll(tempList);
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);

                for (int i = 0; i < videoList.size(); i++) {
                    final int position = i;
                    final MediaItem item = videoList.get(i);
                    executor.execute(() -> {
                        Bitmap thumbnail = createVideoThumbnail(Uri.parse(item.uri));
                        if (thumbnail != null) {
                            item.setThumbnail(thumbnail);
                            mainHandler.post(() -> adapter.notifyItemChanged(position));
                        }
                    });
                }
            });
        });
    }


    private Bitmap createVideoThumbnail(Uri uri) {
        String key = uri.toString();
        Bitmap cachedThumbnail = thumbnailCache.get(key);
        if (cachedThumbnail != null) {
            return cachedThumbnail;
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(requireContext(), uri);
            Bitmap bitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST);

            if (bitmap != null) {
                thumbnailCache.put(key, bitmap);
            }

            return bitmap;
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