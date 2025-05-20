package com.example.lab1;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final OnTransactionDeleteListener deleteListener;
    private final OnTransactionEditListener editListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private List<Transaction> transactions;

    public TransactionAdapter(List<Transaction> transactions, OnTransactionDeleteListener deleteListener, OnTransactionEditListener editListener) {
        this.transactions = transactions;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Transaction> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionAdapter.ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        String formattedAmount;
        if (transaction.getType() == Transaction.Type.EXPENSE) {
            formattedAmount = String.format(Locale.getDefault(), "-%.2f", transaction.getAmount());
            holder.tvAmount.setTextColor(Color.parseColor("#F44336"));
            holder.tvType.setText(R.string.expenses);
            holder.tvType.setBackgroundResource(R.drawable.bg_transaction_type);
        } else {
            formattedAmount = String.format(Locale.getDefault(), "+%.2f", transaction.getAmount());
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50"));
            holder.tvType.setText(R.string.incomes);
            holder.tvType.setBackgroundResource(R.drawable.bg_transaction_type);
        }
        holder.tvAmount.setText(formattedAmount);
        holder.tvCategory.setText(transaction.getCategory());
        holder.tvDescription.setText(transaction.getDescription());
        holder.tvDate.setText(dateFormat.format(transaction.getDate()));

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEdit(transaction);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvAmount;
        TextView tvType;
        TextView tvCategory;
        TextView tvDescription;
        TextView tvDate;
        Button btnEdit;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvType = itemView.findViewById(R.id.tv_type);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

}
