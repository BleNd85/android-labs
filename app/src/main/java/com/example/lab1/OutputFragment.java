package com.example.lab1;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class OutputFragment extends Fragment {

    private TextView resultText;

    private Button cancelButton;

    private OnClearDataListener onClearDataListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnClearDataListener) {
            onClearDataListener = (OnClearDataListener) context;
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

        cancelButton.setVisibility(View.GONE);

        cancelButton.setOnClickListener(v -> {
            resultText.setText("");
            cancelButton.setVisibility(View.GONE);

            if (onClearDataListener != null) {
                onClearDataListener.onClearData();
            }
        });
    }

    public void updateData(String author, String year) {
        if (resultText != null && cancelButton != null) {
            resultText.setText("Автор: " + author + "\nРік: " + year);
            cancelButton.setVisibility(View.VISIBLE);
        }
    }


}
