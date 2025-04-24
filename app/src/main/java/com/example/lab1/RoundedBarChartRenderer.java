package com.example.lab1;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class RoundedBarChartRenderer extends BarChartRenderer {

    public RoundedBarChartRenderer(BarDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }

    @Override
    protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
        Paint paint = mRenderPaint;
        float radius = 20f;

        for (int i = 0; i < dataSet.getEntryCount(); i++) {
            BarEntry entry = dataSet.getEntryForIndex(i);
            float left = entry.getX() - 0.3f;
            float right = entry.getX() + 0.3f;
            float top = entry.getY();
            float bottom = 0;

            paint.setColor(dataSet.getColor(i));

            mBarRect.set(left, top, right, bottom);
            mChart.getTransformer(dataSet.getAxisDependency()).rectValueToPixel(mBarRect);

            c.drawRoundRect(mBarRect, radius, radius, paint);
        }
    }

}


