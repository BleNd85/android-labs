package com.example.lab1;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

public class MediaViewHolder extends RecyclerView.ViewHolder {
    ShapeableImageView imageThumbnail;
    TextView textName, textDuration;

    public MediaViewHolder(@NonNull View itemView){
        super(itemView);
        imageThumbnail = itemView.findViewById(R.id.image_thumbnail);
        textName = itemView.findViewById(R.id.text_name);
        textDuration = itemView.findViewById(R.id.text_duration);
    }
}
