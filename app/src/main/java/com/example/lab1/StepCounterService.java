package com.example.lab1;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class StepCounterService extends Service {

    private static final String CHANNEL_ID = "step_channel";
    private static final int NOTIFICATION_ID = 1;

    private SensorManager sensorManager;
    private Sensor stepSensor;

    private int lastSteps = -1;
    private int totalSessionSteps = 0;

    private DatabaseHelper databaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        databaseHelper = new DatabaseHelper(this);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (stepSensor != null) {
                sensorManager.registerListener(stepListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        startForeground(NOTIFICATION_ID, createNotification("Steps: 0"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(stepListener);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification(String contentText) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Step Counter", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Counting steps");
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Step Counter is active")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_menu_myplaces)
                .setOngoing(true)
                .build();
    }

    private final SensorEventListener stepListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            int totalSteps = (int) event.values[0];
            if (lastSteps == -1) lastSteps = totalSteps;

            int stepCount = totalSteps - lastSteps;
            lastSteps = totalSteps;

            if (stepCount > 0) {
                totalSessionSteps += stepCount;

                Calendar calendar = Calendar.getInstance();
                @SuppressLint("DefaultLocale") String date = String.format("%04d-%02d-%02d",
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH));
                int hour = calendar.get(Calendar.HOUR_OF_DAY);

                databaseHelper.insertOrUpdateStep(date, hour, stepCount);

                Notification notification = createNotification("Steps: " + totalSessionSteps);
                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.notify(NOTIFICATION_ID, notification);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
}
