package com.example.lab1;

import android.database.Cursor;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements OnSelectedDataListener, OnClearDataListener, OnDatabaseListener {

    private OutputFragment outputFragment;
    private InputFragment inputFragment;
    private DataBaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DataBaseHelper(this);

        if (savedInstanceState == null) {
            inputFragment = new InputFragment();
            outputFragment = new OutputFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.input_fragment_container, inputFragment)
                    .add(R.id.output_fragment_container, outputFragment)
                    .commit();
        }
    }

    @Override
    public void onSelectedDataListener(String year, String author) {
        outputFragment.updateData(author, year);
    }

    @Override
    public void onClearData() {
        if (inputFragment != null) {
            inputFragment.clearForm();
        }
    }

    @Override
    public long insert(String year, String author) {
        return dbHelper.insertData(year, author);
    }

    @Override
    public int deleteById(int id) {
        return dbHelper.deleteById(id);
    }

    @Override
    public int updateById(int id, String year, String author) {
        return dbHelper.updateById(id, year, author);
    }

    @Override
    public Cursor getAll() {
        return dbHelper.getAllData();
    }

    @Override
    public void viewDatabase() {
        DataBaseFragment databaseFragment = new DataBaseFragment();
        getSupportFragmentManager().beginTransaction()
                .remove(outputFragment)
                .replace(R.id.input_fragment_container, databaseFragment)
                .addToBackStack(null)
                .commit();
    }
}