package com.example.lab1;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final List<Transaction> transactions = new ArrayList<>();
    private TextView tvDateStart;
    private TextView tvDateEnd;
    private Button btnApplyFilters;
    private Button btnClearFilters;
    private TextView tvExpensesSum;
    private TextView tvIncomesSum;
    private TextView tvBalance;
    private RecyclerView rvTransactions;
    private TextView tvEmptyState;
    private SwitchMaterial switchExpenses;
    private SwitchMaterial switchIncomes;
    private FloatingActionButton fabAddTransaction;
    private TransactionAdapter adapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupClickListeners();
        setupRecyclerView();
        initApiService();
        initCalendar();

        loadData();
    }

    private void initViews() {
        tvDateStart = findViewById(R.id.tv_date_start);
        tvDateEnd = findViewById(R.id.tv_date_end);
        btnApplyFilters = findViewById(R.id.btn_apply_filters);
        btnClearFilters = findViewById(R.id.btn_clear_filters);
        tvExpensesSum = findViewById(R.id.tv_expenses_sum);
        tvIncomesSum = findViewById(R.id.tv_incomes_sum);
        tvBalance = findViewById(R.id.tv_balance);
        rvTransactions = findViewById(R.id.rv_transactions);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        switchExpenses = findViewById(R.id.switch_expenses);
        switchIncomes = findViewById(R.id.switch_incomes);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);
    }

    private void setupClickListeners() {
        tvDateStart.setOnClickListener(v -> showDatePicker(tvDateStart));
        tvDateEnd.setOnClickListener(v -> showDatePicker(tvDateEnd));

        btnApplyFilters.setOnClickListener(v -> {
            loadData();
        });

        btnClearFilters.setOnClickListener(v -> {
            resetFilters();
        });

        switchExpenses.setOnCheckedChangeListener((buttonView, isChecked) -> {
            filterTransactions();
        });

        switchIncomes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            filterTransactions();
        });

        fabAddTransaction.setOnClickListener(v -> {
            showAddTransactionDialog();
        });
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(transactions, this::onDeleteTransaction, this::onEditTransaction);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);
    }

    private void initApiService() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    private void initCalendar() {
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.set(Calendar.DAY_OF_MONTH, 1);
        tvDateStart.setText(displayDateFormat.format(calendarStart.getTime()));
        Calendar calendarEnd = Calendar.getInstance();
        tvDateEnd.setText(displayDateFormat.format(calendarEnd.getTime()));
    }

    private void showDatePicker(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        try {
            Date date = displayDateFormat.parse(textView.getText().toString());
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
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    textView.setText(displayDateFormat.format(calendar.getTime()));
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void loadData() {
        loadExpenses();
        loadIncomes();
        loadSummary();
    }

    private void loadExpenses() {
        String startDate = tvDateStart.getText().toString();
        String endDate = tvDateEnd.getText().toString();

        Call<List<Expense>> call = apiService.getExpensesByDateRange(startDate, endDate, 0, 500);
        call.enqueue(new Callback<List<Expense>>() {
            @Override
            public void onResponse(@NonNull Call<List<Expense>> call, @NonNull Response<List<Expense>> response) {
                List<Expense> expenses = response.body();
                addExpensesToTransactions(expenses);
            }

            @Override
            public void onFailure(@NonNull Call<List<Expense>> call, @NonNull Throwable t) {
                Log.e("MainActivity", "Error loading expenses: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Server error occurred while loading expenses: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadIncomes() {
        String startDate = tvDateStart.getText().toString();
        String endDate = tvDateEnd.getText().toString();

        Call<List<Income>> call = apiService.getIncomesByDateRange(startDate, endDate, 0, 500);
        call.enqueue(new Callback<List<Income>>() {
            @Override
            public void onResponse(@NonNull Call<List<Income>> call, @NonNull Response<List<Income>> response) {
                List<Income> incomes = response.body();
                addIncomesToTransactions(incomes);
            }

            @Override
            public void onFailure(@NonNull Call<List<Income>> call, @NonNull Throwable t) {
                Log.e("MainActivity", "Error loading incomes: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Server error occurred while loading incomes: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadSummary() {
        String startDate = tvDateStart.getText().toString();
        String endDate = tvDateEnd.getText().toString();

        Call<Double> expenseSumCall = apiService.getExpenseSumForRange(startDate, endDate);
        expenseSumCall.enqueue(new Callback<Double>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<Double> call, @NonNull Response<Double> response) {
                if (response.isSuccessful() && response.body() != null) {
                    double expenseSum = response.body();
                    tvExpensesSum.setText(String.format(Locale.getDefault(), "%.2f", expenseSum));
                    updateBalance();
                } else {
                    Log.e("MainActivity", "Error loading expense sum: " + response.code());
                    Toast.makeText(MainActivity.this, "Error loading expense sum: " + response.code(), Toast.LENGTH_SHORT).show();
                    tvExpensesSum.setText("0.00");
                    updateBalance();
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<Double> call, @NonNull Throwable t) {
                Log.e("MainActivity", "Error loading expense sum: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Server error occurred while loading expense sum: " + t.getMessage(), Toast.LENGTH_LONG).show();
                tvExpensesSum.setText("0.00");
                updateBalance();
            }
        });

        Call<Double> incomeSumCall = apiService.getIncomeSumForRange(startDate, endDate);
        incomeSumCall.enqueue(new Callback<Double>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<Double> call, @NonNull Response<Double> response) {
                if (response.isSuccessful() && response.body() != null) {
                    double incomeSum = response.body();
                    tvIncomesSum.setText(String.format(Locale.getDefault(), "%.2f", incomeSum));
                    updateBalance();
                } else {
                    Log.e("MainActivity", "Error loading income sum: " + response.code());
                    Toast.makeText(MainActivity.this, "Error loading income sum: " + response.code(), Toast.LENGTH_SHORT).show();
                    tvIncomesSum.setText("0.00");
                    updateBalance();
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<Double> call, @NonNull Throwable t) {
                Log.e("MainActivity", "Error loading income sum: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Server error occurred while loading income sum: " + t.getMessage(), Toast.LENGTH_LONG).show();
                tvIncomesSum.setText("0.00");
                updateBalance();
            }
        });
    }

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    private void updateBalance() {
        try {
            double expenses = Double.parseDouble(tvExpensesSum.getText().toString());
            double incomes = Double.parseDouble(tvIncomesSum.getText().toString());
            double balance = incomes - expenses;
            tvBalance.setText(String.format(Locale.getDefault(), "%.2f", balance));

        } catch (NumberFormatException e) {
            tvBalance.setText("0.00");
            tvBalance.setTextColor(android.R.color.black);
        }
    }

    private void addExpensesToTransactions(List<Expense> expenses) {
        transactions.removeIf(transaction -> transaction.getType() == Transaction.Type.EXPENSE);

        for (Expense expense : expenses) {
            Transaction transaction = new Transaction(
                    expense.getId(),
                    expense.getAmount(),
                    expense.getDescription(),
                    expense.getCategory(),
                    expense.getDate(),
                    Transaction.Type.EXPENSE
            );
            transactions.add(transaction);
        }

        filterTransactions();
    }

    private void addIncomesToTransactions(List<Income> incomes) {
        transactions.removeIf(transaction -> transaction.getType() == Transaction.Type.INCOME);

        for (Income income : incomes) {
            Transaction transaction = new Transaction(
                    income.getId(),
                    income.getAmount(),
                    income.getDescription(),
                    income.getCategory(),
                    income.getDate(),
                    Transaction.Type.INCOME
            );
            transactions.add(transaction);
        }

        filterTransactions();
    }

    private void filterTransactions() {
        List<Transaction> filteredTransactions = new ArrayList<>();

        for (Transaction transaction : transactions) {
            boolean shouldInclude = false;

            if (transaction.getType() == Transaction.Type.EXPENSE && switchExpenses.isChecked()) {
                shouldInclude = true;
            } else if (transaction.getType() == Transaction.Type.INCOME && switchIncomes.isChecked()) {
                shouldInclude = true;
            }

            if (shouldInclude) {
                filteredTransactions.add(transaction);
            }
        }

        adapter.updateData(filteredTransactions);

        if (filteredTransactions.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
        }

    }

    private void resetFilters() {
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.set(Calendar.DAY_OF_MONTH, 1);
        tvDateStart.setText(displayDateFormat.format(calendarStart.getTime()));

        Calendar calendarEnd = Calendar.getInstance();
        tvDateEnd.setText(displayDateFormat.format(calendarEnd.getTime()));

        switchExpenses.setChecked(true);
        switchIncomes.setChecked(true);

        loadData();
    }

    private void showAddTransactionDialog() {
        TransactionDialog dialog = new TransactionDialog(this, null, transaction -> {
            if (transaction.getType() == Transaction.Type.EXPENSE) {
                saveExpense(transaction);
            } else {
                saveIncome(transaction);
            }
        });
        dialog.show();
    }

    private void onEditTransaction(Transaction transaction) {
        TransactionDialog dialog = new TransactionDialog(this, transaction, editedTransaction -> {
            if (editedTransaction.getType() == Transaction.Type.EXPENSE) {
                updateExpense(editedTransaction);
            } else {
                updateIncome(editedTransaction);
            }
        });
        dialog.show();
    }

    private void onDeleteTransaction(Transaction transaction) {
        if (transaction.getType() == Transaction.Type.EXPENSE) {
            deleteExpense(transaction.getId());
        } else {
            deleteIncome(transaction.getId());
        }
    }

    private void saveExpense(Transaction transaction) {
        Expense expense = new Expense();
        expense.setAmount(transaction.getAmount());
        expense.setDescription(transaction.getDescription());
        expense.setCategory(transaction.getCategory());
        expense.setDate(transaction.getDate());

        Call<Expense> call = apiService.createExpense(expense);
        call.enqueue(new Callback<Expense>() {
            @Override
            public void onResponse(@NonNull Call<Expense> call, @NonNull Response<Expense> response) {
                Toast.makeText(MainActivity.this, "Expense saved successfully", Toast.LENGTH_SHORT).show();
                loadData();
            }

            @Override
            public void onFailure(@NonNull Call<Expense> call, @NonNull Throwable t) {
                Log.e("MainActivity", "Error saving expense: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Error saving expense: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveIncome(Transaction transaction) {
        Income income = new Income();
        income.setAmount(transaction.getAmount());
        income.setDescription(transaction.getDescription());
        income.setCategory(transaction.getCategory());
        income.setDate(transaction.getDate());

        Call<Income> call = apiService.createIncome(income);
        call.enqueue(new Callback<Income>() {
            @Override
            public void onResponse(@NonNull Call<Income> call, @NonNull Response<Income> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MainActivity.this, "Income saved successfully", Toast.LENGTH_SHORT).show();
                    loadData();
                } else {
                    Log.e("MainActivity", "Error saving income: " + response.code());
                    Toast.makeText(MainActivity.this, "Error saving income: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Income> call, @NonNull Throwable t) {
                Log.e("MainActivity", "Error saving income: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Error saving income: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateExpense(Transaction transaction) {
        Expense expense = new Expense();
        expense.setId(transaction.getId());
        expense.setAmount(transaction.getAmount());
        expense.setDescription(transaction.getDescription());
        expense.setCategory(transaction.getCategory());
        expense.setDate(transaction.getDate());

        Call<Expense> call = apiService.updateExpense(transaction.getId(), expense);
        call.enqueue(new Callback<Expense>() {
            @Override
            public void onResponse(@NonNull Call<Expense> call, @NonNull Response<Expense> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MainActivity.this, "Expense updated successfully", Toast.LENGTH_SHORT).show();
                    loadData();
                } else {
                    Log.e("MainActivity", "Error updating expense: " + response.code());
                    Toast.makeText(MainActivity.this, "Error updating expense: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Expense> call, @NonNull Throwable t) {
                Log.e("MainActivity", "Error updating expense: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Error updating expense: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateIncome(Transaction transaction) {
        Income income = new Income();
        income.setId(transaction.getId());
        income.setAmount(transaction.getAmount());
        income.setDescription(transaction.getDescription());
        income.setCategory(transaction.getCategory());
        income.setDate(transaction.getDate());

        Call<Income> call = apiService.updateIncome(transaction.getId(), income);
        call.enqueue(new Callback<Income>() {
            @Override
            public void onResponse(@NonNull Call<Income> call, @NonNull Response<Income> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MainActivity.this, "Income updated successfully", Toast.LENGTH_SHORT).show();
                    loadData();
                } else {
                    Log.e("MainActivity", "Error updating income: " + response.code());
                    Toast.makeText(MainActivity.this, "Error updating income: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Income> call, @NonNull Throwable t) {
                Log.e("MainActivity", "Error updating income: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Error updating income: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteExpense(UUID id) {
        Call<Void> call = apiService.deleteExpense(id);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Expense deleted successfully", Toast.LENGTH_SHORT).show();
                    loadData();
                } else {
                    Log.e("MainActivity", "Error deleting expense: " + response.code());
                    Toast.makeText(MainActivity.this, "Error deleting expense: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("MainActivity", "Error deleting expense: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Error deleting expense: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteIncome(UUID id) {
        Call<Void> call = apiService.deleteIncome(id);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Income deleted successfully", Toast.LENGTH_SHORT).show();
                    loadData();
                } else {
                    Log.e("MainActivity", "Error deleting income: " + response.code());
                    Toast.makeText(MainActivity.this, "Error deleting income: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("MainActivity", "Error deleting income: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Error deleting income: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}