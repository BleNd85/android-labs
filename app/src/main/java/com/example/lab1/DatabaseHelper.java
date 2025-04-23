package com.example.lab1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {
    private final static String DATABASE_NAME = "steps.db";
    private final static Integer DATABASE_VERSION = 1;
    private static final String TABLE_STEPS = "steps";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_HOUR = "hour";
    private static final String COLUMN_STEPS = "steps";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create = "CREATE TABLE " + TABLE_STEPS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_HOUR + " INTEGER, " +
                COLUMN_STEPS + " INTEGER)";
        db.execSQL(create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STEPS);
        onCreate(db);
    }

    public long insertOrUpdateStep(String date, int hour, int steps) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + COLUMN_STEPS + " FROM " + TABLE_STEPS +
                        " WHERE " + COLUMN_DATE + "=? AND " + COLUMN_HOUR + "=?",
                new String[]{date, String.valueOf(hour)});

        if (cursor.moveToFirst()) {
            int currentSteps = cursor.getInt(0);
            ContentValues values = new ContentValues();
            values.put(COLUMN_STEPS, currentSteps + steps);
            long id = db.update(TABLE_STEPS, values,
                    COLUMN_DATE + "=? AND " + COLUMN_HOUR + "=?",
                    new String[]{date, String.valueOf(hour)});
            cursor.close();
            db.close();
            return id;
        } else {
            ContentValues values = new ContentValues();
            values.put(COLUMN_DATE, date);
            values.put(COLUMN_HOUR, hour);
            values.put(COLUMN_STEPS, steps);
            long id = db.insert(TABLE_STEPS, null, values);
            cursor.close();
            db.close();
            return id;
        }
    }

    public List<StepData> getStepsForPeriod(String fromDate, String toDate) {
        List<StepData> steps = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_DATE + ", " + COLUMN_HOUR + ", " + COLUMN_STEPS +
                        " FROM " + TABLE_STEPS +
                        " WHERE " + COLUMN_DATE + " BETWEEN ? AND ? ORDER BY " + COLUMN_DATE + ", " + COLUMN_HOUR,
                new String[]{fromDate, toDate});

        while (cursor.moveToNext()) {
            String date = cursor.getString(0);
            int hour = cursor.getInt(1);
            int step = cursor.getInt(2);
            steps.add(new StepData(date, hour, step));
        }

        cursor.close();
        db.close();
        return steps;
    }
}
