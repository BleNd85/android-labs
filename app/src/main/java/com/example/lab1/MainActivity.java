package com.example.lab1;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements OnSelectedDataListener, OnClearDataListener {

    private OutputFragment outputFragment;
    private InputFragment inputFragment;

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
}