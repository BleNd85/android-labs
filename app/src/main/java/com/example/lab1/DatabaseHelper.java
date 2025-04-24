package com.example.lab1;

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
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_HOUR = "hour";
    private static final String COLUMN_STEPS = "steps";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_STEPS + " (" +
                COLUMN_DATE + " TEXT, " +
                COLUMN_HOUR + " INTEGER, " +
                COLUMN_STEPS + " INTEGER, " +
                "PRIMARY KEY(" + COLUMN_DATE + "," + COLUMN_HOUR + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STEPS);
        onCreate(db);
    }

    public void addSteps(String date, int hour, int steps) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT OR REPLACE INTO " + TABLE_STEPS + " (" +
                        COLUMN_DATE + ", " + COLUMN_HOUR + ", " + COLUMN_STEPS +
                        ") VALUES (?, ?, COALESCE((SELECT " + COLUMN_STEPS +
                        " FROM " + TABLE_STEPS + " WHERE " + COLUMN_DATE + "=? AND " +
                        COLUMN_HOUR + "=?), 0) + ?)",
                new Object[]{date, hour, date, hour, steps});
    }

    public List<StepData> getStepsByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_STEPS + " WHERE " + COLUMN_DATE + "=? ORDER BY " + COLUMN_HOUR + " ASC",
                new String[]{date});
        List<StepData> data = new ArrayList<>();
        while (cursor.moveToNext()) {
            data.add(new StepData(
                    cursor.getString(0),
                    cursor.getInt(1),
                    cursor.getInt(2)
            ));
        }
        cursor.close();
        return data;
    }
}
