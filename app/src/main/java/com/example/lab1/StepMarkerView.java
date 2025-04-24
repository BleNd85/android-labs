package com.example.lab1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

@SuppressLint("ViewConstructor")
public class StepMarkerView extends MarkerView {
    private final TextView tvContent;

    public StepMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        tvContent = findViewById(R.id.tvContent);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        int steps = (int) e.getY();
        int hours = (int) e.getX();
        if (steps > 0) {
            tvContent.setText(steps + " steps" + " \n" + String.format("%02d:00", hours) + " - " + String.format("%02d:00", hours + 1));
        } else {
            tvContent.setText("");
        }
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2f), -getHeight());
    }
}
