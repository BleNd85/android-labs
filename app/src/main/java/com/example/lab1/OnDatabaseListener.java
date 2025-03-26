package com.example.lab1;

import android.database.Cursor;

public interface OnDatabaseListener {
    long insert(String year, String author);

    int deleteById(int id);

    int updateById(int id, String year, String author);

    Cursor getAll();

    void viewDatabase();
}
