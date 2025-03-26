package com.example.lab1;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class OutputFragment extends Fragment {

    private TextView resultText;
    private Button cancelButton;
    private Button saveButton;
    private Button databaseButton;
    private String year;
    private String author;

    private OnClearDataListener onClearDataListener;

    private OnDatabaseListener onDatabaseListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnClearDataListener && context instanceof OnDatabaseListener) {
            onClearDataListener = (OnClearDataListener) context;
            onDatabaseListener = (OnDatabaseListener) context;
        } else {
            throw new ClassCastException(context + " must implement OnClearDataListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_output, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);

        resultText = view.findViewById(R.id.result_text);
        cancelButton = view.findViewById(R.id.cancel_button);
        saveButton = view.findViewById(R.id.save_button);
        databaseButton = view.findViewById(R.id.view_db_button);

        cancelButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);

        cancelButton.setOnClickListener(v -> {
            resultText.setText("");
            cancelButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);

            if (onClearDataListener != null) {
                onClearDataListener.onClearData();
            }
        });

        databaseButton.setOnClickListener(v -> onDatabaseListener.viewDatabase());

        saveButton.setOnClickListener(v -> saveDataToDatabase());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onClearDataListener = null;
    }

    public void updateData(String author, String year) {
        if (resultText != null && cancelButton != null) {
            this.author = author;
            this.year = year;
            resultText.setText("Автор: " + this.author + "\nРік: " + this.year);
            cancelButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
        }
    }

    public void saveDataToDatabase() {
        long rowId = onDatabaseListener.insert(year, author);
        if (rowId == -1) {
            Toast.makeText(getContext(), "Error saving your book.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Success. Book is saved.", Toast.LENGTH_SHORT).show();
        }
    }
}
