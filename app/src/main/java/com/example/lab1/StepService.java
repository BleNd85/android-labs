package com.example.lab1;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StepService extends Service implements StepListener {

    private StepSensorManager stepSensorManager;
    private DatabaseHelper dbHelper;
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "step_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        stepSensorManager = new StepSensorManager(this, this);
        dbHelper = new DatabaseHelper(this);
        stepSensorManager.register();

        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stepSensorManager.unRegister();
        super.onDestroy();
    }

    @Override
    public void onStepChanged(int stepCount) {
        String date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        dbHelper.addSteps(date, hour, stepCount);

        Intent updateIntent = new Intent("com.example.lab1.UPDATE_UI");
        updateIntent.putExtra("steps", stepCount);
        sendBroadcast(updateIntent);
    }

    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("Step Counter is active").setSmallIcon(android.R.drawable.ic_menu_myplaces).setPriority(NotificationCompat.PRIORITY_MIN).setContentIntent(pendingIntent).setOngoing(true).build();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Step Tracker", NotificationManager.IMPORTANCE_MIN);
        channel.setSound(null, null);
        channel.enableLights(false);
        channel.enableVibration(false);

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

