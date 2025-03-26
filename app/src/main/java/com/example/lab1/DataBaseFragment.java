package com.example.lab1;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class DataBaseFragment extends Fragment {

    private EditText editId;
    private OnDatabaseListener dbListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnDatabaseListener) {
            dbListener = (OnDatabaseListener) context;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_database, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadList();

        editId = view.findViewById(R.id.edit_id);
        Button deleteButton = view.findViewById(R.id.delete_button);
        Button updateButton = view.findViewById(R.id.update_button);

        deleteButton.setOnClickListener(v -> deleteItem());

    }

    private void loadList() {
        if (dbListener != null) {
            Cursor cursor = dbListener.getAll();
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    getActivity(),
                    R.layout.list_item,
                    cursor,
                    new String[]{"_id", "author", "year"},
                    new int[]{R.id.item_id, R.id.item_author, R.id.item_year},
                    0
            );
            ListView listView = requireView().findViewById(R.id.list_view);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(((parent, view, position, id) -> editId.setText(String.valueOf(id))));
        }
    }

    private void deleteItem() {
        try {
            int id = Integer.parseInt(editId.getText().toString());
            new AlertDialog.Builder(getContext())
                    .setTitle("Confirm the deletion")
                    .setMessage("Are you sure you want to delete book with id " + id + "?")
                    .setPositiveButton("Yes", ((dialog, which) -> {
                        int deletedRows = dbListener.deleteById(id);
                        if (deletedRows > 0) {
                            Toast.makeText(getContext(), "Book with id: " + id + " was successfully deleted!", Toast.LENGTH_SHORT).show();
                            editId.setText("");
                            loadList();
                        } else {
                            Toast.makeText(getContext(), "Book with id: " + id + " was not found", Toast.LENGTH_SHORT).show();
                        }
                    })).setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss())).show();
        } catch (NumberFormatException ignored) {
            Toast.makeText(getContext(), "Enter id!", Toast.LENGTH_SHORT).show();
        }
    }

    //TODO update
    private void updateItem() {

    }
}