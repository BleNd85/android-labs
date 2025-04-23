package com.example.lab1;

public class StepData {
    public String date;
    public int hours;
    public int steps;

    public StepData(String date, int hours, int steps) {
        this.date = date;
        this.hours = hours;
        this.steps = steps;
    }

    public String getDate() {
        return date;
    }

    public int getHours() {
        return hours;
    }

    public int getSteps() {
        return steps;
    }
}
