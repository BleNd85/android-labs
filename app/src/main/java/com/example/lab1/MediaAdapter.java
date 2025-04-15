package com.example.lab1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaViewHolder> {

    private final List<MediaItem> mediaList;

    public MediaAdapter(List<MediaItem> mediaList) {
        this.mediaList = mediaList;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.media_item, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaItem item = mediaList.get(position);
        holder.textName.setText(item.name);
        holder.textDuration.setText(formatDuration(item.duration));

        if (item.thumbnail != null) {
            holder.imageThumbnail.setImageBitmap(item.thumbnail);
        } else {
            holder.imageThumbnail.setImageResource(R.drawable.ic_launcher_foreground);
        }
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent;

            if (isAudioFile(item.name)) {
                intent = new Intent(context, AudioPlayerActivity.class);
            } else {
                intent = new Intent(context, VideoPlayerActivity.class);
            }

            intent.putExtra("media_name", item.name);
            intent.putExtra("media_uri", item.uri);
            intent.putExtra("media_index", position);

            ArrayList<String> uriList = new ArrayList<>();
            ArrayList<String> nameList = new ArrayList<>();
            for (MediaItem media : mediaList) {
                uriList.add(media.uri);
                nameList.add(media.name);
            }

            intent.putStringArrayListExtra("media_uri_list", uriList);
            intent.putStringArrayListExtra("media_name_list", nameList);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    @SuppressLint("DefaultLocale")
    private String formatDuration(Long durationMs) {
        if (durationMs == null) return "00:00";
        int totalSeconds = (int) (durationMs / 1000);
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    private boolean isAudioFile(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".mp3") || lower.endsWith(".m4a") || lower.endsWith(".wav") || lower.endsWith(".ogg");
    }
}
