package com.example.lab1;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class StepSensorManager implements SensorEventListener {

    private final SensorManager sensorManager;
    private final Sensor stepSensor;
    private final StepListener stepListener;
    private int lastSteps = -1;
    private final boolean isSensorAvailable;

    public StepSensorManager(Context context, StepListener stepListener) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        this.stepListener = stepListener;
        this.isSensorAvailable = (stepSensor != null);
    }

    public boolean isAvailable() {
        return isSensorAvailable;
    }

    public void register() {
        if (isSensorAvailable) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void unRegister() {
        if (isSensorAvailable) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int totalSteps = (int) event.values[0];
        if (lastSteps == -1) lastSteps = totalSteps;

        int stepCount = totalSteps - lastSteps;
        lastSteps = totalSteps;

        if (stepCount > 0) {
            stepListener.onStepChanged(stepCount);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
