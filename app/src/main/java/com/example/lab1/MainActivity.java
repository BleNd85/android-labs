package com.example.lab1;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private Calendar selectedDate;
    private BarChart barChart;
    private ImageButton prevDateButton, nextDateButton;
    private TextView stepsView, distanceView, caloriesView, selectedDateView;
    private final float distancePerStep = 0.0008f;
    private float caloriesPerStep = 0.04f;

    private static final int REQUEST_ACTIVITY_RECOGNITION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        checkAndRequestPermissions();

        Intent serviceIntent = new Intent(this, StepService.class);
        ContextCompat.startForegroundService(this, serviceIntent);

        barChart = findViewById(R.id.barChart);
        stepsView = findViewById(R.id.stepsText);
        distanceView = findViewById(R.id.distanceText);
        caloriesView = findViewById(R.id.caloriesText);
        selectedDateView = findViewById(R.id.selectedDateText);
        prevDateButton = findViewById(R.id.btnPrevDate);
        nextDateButton = findViewById(R.id.btnNextDate);
        selectedDate = Calendar.getInstance();

        StepMarkerView marker = new StepMarkerView(this, R.layout.marker_view);
        marker.setChartView(barChart);
        barChart.setMarker(marker);

        databaseHelper = new DatabaseHelper(this);
        updateUIForDate();

        prevDateButton.setOnClickListener(v -> {
            selectedDate.add(Calendar.DAY_OF_MONTH, -1);
            updateUIForDate();
        });

        nextDateButton.setOnClickListener(v -> {
            Calendar todayCalendar = Calendar.getInstance();
            if (selectedDate.before(todayCalendar)) {
                selectedDate.add(Calendar.DAY_OF_MONTH, 1);
                updateUIForDate();
            }
        });

        selectedDateView.setOnClickListener(v -> {
            Calendar todayCalendar = Calendar.getInstance();
            todayCalendar.set(Calendar.HOUR_OF_DAY, 23);
            todayCalendar.set(Calendar.MINUTE, 59);
            todayCalendar.set(Calendar.SECOND, 59);
            todayCalendar.set(Calendar.MILLISECOND, 999);

            DatePickerDialog dialog = new DatePickerDialog(MainActivity.this, (view, year, month, dayOfMonth) -> {
                selectedDate.set(year, month, dayOfMonth);
                updateUIForDate();
            },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH));

            dialog.getDatePicker().setMaxDate(todayCalendar.getTimeInMillis());
            dialog.show();
        });

    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION},
                    REQUEST_ACTIVITY_RECOGNITION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission required for step tracking", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupBarChart(int yMax) {
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.getDescription().setEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);
        barChart.setScaleEnabled(false);
        barChart.setTouchEnabled(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(6, false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int hour = (int) value;
                if (hour % 4 == 0 && hour >= 0 && hour <= 24) {
                    return String.format(Locale.getDefault(), "%02d", hour);
                } else {
                    return "";
                }
            }
        });

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(yMax);
        leftAxis.setGranularity(1000f);
        leftAxis.setLabelCount((yMax / 1000) + 1, true);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return ((int) value % 1000 == 0) ? String.valueOf((int) value) : "";
            }
        });

        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.animateY(1000);
    }

    private void showStepsChart(List<StepData> daySteps) {
        List<BarEntry> entries = new ArrayList<>();
        float[] hourSteps = new float[24];

        for (StepData data : daySteps) {
            int hour = data.getHours();
            if (hour >= 0 && hour < 24) {
                hourSteps[hour] += data.getSteps();
            }
        }

        for (int i = 0; i < 24; i++) {
            entries.add(new BarEntry(i, hourSteps[i]));
        }

        float maxSteps = 0;
        for (float steps : hourSteps) {
            if (steps > maxSteps) maxSteps = steps;
        }
        int yMax = ((int) (maxSteps / 1000) + 1) * 1000;

        setupBarChart(yMax);

        BarDataSet dataSet = new BarDataSet(entries, "Steps per Hour");
        dataSet.setColor(ContextCompat.getColor(this, R.color.light_brown));
        barChart.setRenderer(new RoundedBarChartRenderer(barChart, barChart.getAnimator(), barChart.getViewPortHandler()));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);
        barChart.setData(barData);
        barChart.getXAxis().setAxisMinimum(-0.5f);
        barChart.getXAxis().setAxisMaximum(23.5f);
        barData.setBarWidth(0.8f);
        barChart.invalidate();

        StepMarkerView marker = new StepMarkerView(this, R.layout.marker_view);
        marker.setChartView(barChart);
        barChart.setMarker(marker);
    }


    @SuppressLint("SetTextI18n")
    private void updateUIForDate() {
        String dateStr = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(selectedDate.getTime());
        selectedDateView.setText(dateStr);

        List<StepData> updatedSteps = databaseHelper.getStepsByDate(dateStr);
        for (StepData data : updatedSteps) {
            Log.d("MainActivity", "Hour: " + data.getHours() + ", Steps: " + data.getSteps());
        }
        showStepsChart(updatedSteps);

        int totalSteps = 0;
        for (StepData s : updatedSteps) totalSteps += s.getSteps();

        if (totalSteps == 0) {
            stepsView.setText("-");
            distanceView.setText("-");
            caloriesView.setText("-");
        } else {
            distanceView.setText(String.format(Locale.getDefault(), "%.2f km", totalSteps * distancePerStep));
            caloriesView.setText(String.format(Locale.getDefault(), "%.2f", totalSteps * caloriesPerStep));
            stepsView.setText(String.valueOf(totalSteps));
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateUIForDate();
    }
}