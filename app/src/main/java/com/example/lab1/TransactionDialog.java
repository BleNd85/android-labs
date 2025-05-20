package com.example.lab1;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class TransactionDialog extends Dialog {


    private final OnTransactionSaveListener saveListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private EditText etAmount;
    private EditText etDescription;
    private EditText etCategory;
    private TextView tvDate;
    private RadioButton radioExpense;
    private RadioButton radioIncome;
    private Button btnSave;
    private Button btnCancel;
    private Transaction editTransaction;

    public TransactionDialog(@NonNull Context context, Transaction transaction, OnTransactionSaveListener saveListener) {
        super(context);
        this.editTransaction = transaction;
        this.saveListener = saveListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_transaction);

        initViews();
        setupClickListeners();
        if (editTransaction != null) {
            fillFormWithData();
        }

    }

    private void initViews() {
        etAmount = findViewById(R.id.et_amount);
        etDescription = findViewById(R.id.et_description);
        etCategory = findViewById(R.id.et_category);
        tvDate = findViewById(R.id.tv_date);
        radioExpense = findViewById(R.id.radio_expense);
        radioIncome = findViewById(R.id.radio_income);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        tvDate.setText(dateFormat.format(new Date()));
    }

    private void setupClickListeners() {
        tvDate.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> saveTransaction());

        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        try {
            Date date = dateFormat.parse(tvDate.getText().toString());
            if (date != null) {
                calendar.setTime(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    tvDate.setText(dateFormat.format(calendar.getTime()));
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void fillFormWithData() {
        if (editTransaction != null) {
            etAmount.setText(String.format(Locale.getDefault(), "%.2f", editTransaction.getAmount()));
            etDescription.setText(editTransaction.getDescription());
            tvDate.setText(dateFormat.format(editTransaction.getDate()));
            etCategory.setText(editTransaction.getCategory());

            if (editTransaction.getType() == Transaction.Type.EXPENSE) {
                radioExpense.setChecked(true);
            } else {
                radioIncome.setChecked(true);
            }
        }
    }

    private void saveTransaction() {
        try {
            if (etAmount.getText().toString().trim().isEmpty()) {
                Toast.makeText(getContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(etAmount.getText().toString().trim());
            if (amount <= 0) {
                Toast.makeText(getContext(), "Amount must be greater than zero", Toast.LENGTH_SHORT).show();
                return;
            }

            String description = etDescription.getText().toString().trim();
            if (description.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a description", Toast.LENGTH_SHORT).show();
                return;
            }

            String category = etCategory.getText().toString();
            Date date;
            try {
                date = dateFormat.parse(tvDate.getText().toString());
            } catch (ParseException e) {
                Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
                return;
            }

            Transaction.Type type = radioExpense.isChecked() ? Transaction.Type.EXPENSE : Transaction.Type.INCOME;

            UUID id = (editTransaction != null) ? editTransaction.getId() : null;
            Transaction transaction = new Transaction(id, amount, description, category, date, type);

            if (saveListener != null) {
                saveListener.onSave(transaction);
            }

            dismiss();

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();
        }
    }
}
