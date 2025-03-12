package com.example.lab1;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class InputFragment extends Fragment {

    private RadioGroup yearRadioGroup;

    private Spinner bookSpinner;

    private OnSelectedDataListener onSelectedDataListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof OnSelectedDataListener) {
            onSelectedDataListener = (OnSelectedDataListener) context;
        } else {
            throw new ClassCastException(context + "must implement OnSelectedDataListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_input, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);

        yearRadioGroup = view.findViewById(R.id.yearRadioGroup);
        bookSpinner = view.findViewById(R.id.books_spinner);
        Button submitButton = view.findViewById(R.id.submit_button);

        submitButton.setOnClickListener(c -> {
            int selectedId = yearRadioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(getActivity(), "Оберіть рік", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedButton = view.findViewById(selectedId);
            String selectedYear = selectedButton.getText().toString();
            String selectedAuthor = bookSpinner.getSelectedItem().toString();

            onSelectedDataListener.onSelectedDataListener(selectedYear, selectedAuthor);
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSelectedDataListener = null;
    }

    public void clearForm() {
        if (yearRadioGroup != null) {
            yearRadioGroup.clearCheck();
        }
        if (bookSpinner != null) {
            bookSpinner.setSelection(0);
        }
    }

}
