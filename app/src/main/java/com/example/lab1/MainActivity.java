package com.example.lab1;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
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
    private TextView stepsView, distanceView, caloriesView, selectedDateView;
    private Button buttonPickDate, buttonDay, buttonWeek, buttonMonth;

    private enum Mode {DAY, WEEK, MONTH}

    private Mode currentMode = Mode.DAY;
    private float distancePerStep = 0.0008f;
    private float caloriesPerStep = 0.04f;

    private static final int REQUEST_ACTIVITY_RECOGNITION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        checkAndRequestPermissions();

        Intent serviceIntent = new Intent(this, StepCounterService.class);
        ContextCompat.startForegroundService(this, serviceIntent);

        barChart = findViewById(R.id.barChart);
        stepsView = findViewById(R.id.stepsText);
        distanceView = findViewById(R.id.distanceText);
        caloriesView = findViewById(R.id.caloriesText);
        buttonPickDate = findViewById(R.id.btnPickDate);

        selectedDateView = findViewById(R.id.selectedDateText);
        buttonDay = findViewById(R.id.btnDay);
        buttonWeek = findViewById(R.id.btnWeek);
        buttonMonth = findViewById(R.id.btnMonth);

        selectedDate = Calendar.getInstance();
        updateDateDisplay();

        buttonPickDate.setOnClickListener(v -> showDatePicker());

        updateModeUI();
        updateChartAndData();

        buttonDay.setOnClickListener(v -> {
            currentMode = Mode.DAY;
            updateModeUI();
            updateChartAndData();
        });

        buttonWeek.setOnClickListener(v -> {
            currentMode = Mode.WEEK;
            updateModeUI();
            updateChartAndData();
        });

        buttonMonth.setOnClickListener(v -> {
            currentMode = Mode.MONTH;
            updateModeUI();
            updateChartAndData();
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

    private void showDatePicker() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate.set(Calendar.YEAR, selectedYear);
                    selectedDate.set(Calendar.MONTH, selectedMonth);
                    selectedDate.set(Calendar.DAY_OF_MONTH, selectedDay);

                    updateDateDisplay();
                    updateChartAndData();
                }, year, month, day);

        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(selectedDate.getTime());
        selectedDateView.setText(formattedDate);
    }

    @SuppressLint({"StringFormatInvalid", "DefaultLocale"})
    private void updateChartAndData() {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(this);
        }
        updateDateDisplay();

        int totalSteps = 0;
        float totalDistance = 0f;
        float totalCalories = 0f;

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Calendar calendar = (Calendar) selectedDate.clone();

        int count;
        String fromDate = null, toDate = null;

        switch (currentMode) {
            case DAY:
                count = 1;
                fromDate = getDateKey(calendar);
                toDate = fromDate;
                break;
            case WEEK:
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                count = 7;
                fromDate = getDateKey(calendar);
                calendar.add(Calendar.DAY_OF_MONTH, 6);
                toDate = getDateKey(calendar);
                break;
            case MONTH:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                count = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                fromDate = getDateKey(calendar);
                calendar.add(Calendar.MONTH, 1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                toDate = getDateKey(calendar);
                break;
            default:
                count = 1;
        }

        List<StepData> periodData = databaseHelper.getStepsForPeriod(fromDate, toDate);

        if (currentMode == Mode.DAY) {
            String dateKey = getDateKey(calendar);
            for (int hour = 0; hour < 24; hour++) {
                int hourlySteps = 0;
                for (StepData data : periodData) {
                    if (data.getDate().equals(dateKey) && data.getHours() == hour) {
                        hourlySteps += data.getSteps();
                    }
                }

                float distance = hourlySteps * distancePerStep;
                float calories = hourlySteps * caloriesPerStep;

                entries.add(new BarEntry(hour, hourlySteps));
                labels.add(String.format("%02d", hour));

                totalSteps += hourlySteps;
                totalDistance += distance;
                totalCalories += calories;
            }
        } else {
            for (int i = 0; i < count; i++) {
                String dateKey = getDateKey(calendar);

                int dailySteps = 0;
                for (StepData data : periodData) {
                    if (data.getDate().equals(dateKey)) {
                        dailySteps += data.getSteps();
                    }
                }

                float distance = dailySteps * distancePerStep;
                float calories = dailySteps * caloriesPerStep;

                entries.add(new BarEntry(i, dailySteps));
                labels.add(formatDateLabel(calendar));

                totalSteps += dailySteps;
                totalDistance += distance;
                totalCalories += calories;

                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }


        BarDataSet dataSet = new BarDataSet(entries, "Steps");

        dataSet.setColor(Color.rgb(255, 99, 99));
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        barChart.setBackgroundColor(Color.TRANSPARENT);
        barChart.getDescription().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.LTGRAY);
        xAxis.setAxisLineColor(Color.LTGRAY);

        if (currentMode == Mode.DAY) {
            xAxis.setValueFormatter(new IndexAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int hour = (int) value;
                    if (hour % 4 == 0) {
                        return String.valueOf(hour);
                    }
                    return "";
                }
            });
            xAxis.setLabelCount(7, true);
            xAxis.setGranularity(4f);
        } else {
            xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
            xAxis.setLabelCount(Math.min(count, 10), true);
        }

        // Y-axis configuration
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setTextColor(Color.LTGRAY);
        leftAxis.setAxisLineColor(Color.TRANSPARENT);
        leftAxis.setDrawLabels(true);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#444444"));

        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 1000) {
                    return String.format("%.0f", value / 1000) + "K";
                }
                return String.format("%.0f", value);
            }
        });

        leftAxis.setAxisMinimum(0f);

        barChart.getAxisRight().setEnabled(false);

        barChart.getLegend().setEnabled(false);

        barChart.setExtraOffsets(10, 10, 10, 10);

        barChart.animateY(500);

        barChart.invalidate();

        stepsView.setText(String.valueOf(totalSteps));
        distanceView.setText(String.format("%.1f km", totalDistance));
        caloriesView.setText(String.format("%.0f kcal", totalCalories));
    }

    private StepData findStepDataForDate(List<StepData> periodData, String dateKey) {
        for (StepData data : periodData) {
            if (data.getDate().equals(dateKey)) {
                return data;
            }
        }
        return null;
    }

    @SuppressLint("DefaultLocale")
    private List<String> getHourLabels() {
        List<String> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d:00", i));
        }
        return hours;
    }

    @SuppressLint("DefaultLocale")
    private String getDateKey(Calendar cal) {
        return String.format("%04d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH));
    }

    @SuppressLint("DefaultLocale")
    private String formatDateLabel(Calendar cal) {
        return String.format("%02d.%02d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1);
    }

    private void updateModeUI() {
        buttonDay.setBackgroundTintList(ContextCompat.getColorStateList(this,
                currentMode == Mode.DAY ? android.R.color.holo_red_light : android.R.color.darker_gray));
        buttonWeek.setBackgroundTintList(ContextCompat.getColorStateList(this,
                currentMode == Mode.WEEK ? android.R.color.holo_red_light : android.R.color.darker_gray));
        buttonMonth.setBackgroundTintList(ContextCompat.getColorStateList(this,
                currentMode == Mode.MONTH ? android.R.color.holo_red_light : android.R.color.darker_gray));
    }
}