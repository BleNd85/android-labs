package com.example.lab1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DataBaseHelper extends SQLiteOpenHelper {
    private final static String DATABASE_NAME = "books.db";
    private final static Integer DATABASE_VERSION = 1;
    private final static String TABLE_NAME = "books";
    private final static String COLUMN_ID = "_id";
    private final static String COLUMN_YEAR = "year";
    private final static String COLUMN_AUTHOR = "author";

    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_AUTHOR + " TEXT, " +
                COLUMN_YEAR + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insertData(String year, String author) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AUTHOR, author);
        values.put(COLUMN_YEAR, year);

        long rowId = db.insert(TABLE_NAME, null, values);
        db.close();
        return rowId;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public int updateById(int id, String year, String author) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_YEAR, year);
        values.put(COLUMN_AUTHOR, author);

        int rowId = db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rowId;
    }

    public int deleteById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowId = db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rowId;
    }

}
