package com.example.lab1.Fragment;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.example.lab1.DownloadService.DownloadService;
import com.example.lab1.R;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

public class DownloadFragment extends Fragment implements DownloadService.DownloadCallback {

    private EditText editUrl;
    private Button btnDownload, btnCancel;
    private ProgressBar progressBar;
    private TextView progressText, downloadingText;
    private LinearLayout downloadingLayout, downloadDataLayout;
    private DownloadService downloadService;
    private boolean isServiceBound = false;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadService.LocalBinder binder = (DownloadService.LocalBinder) service;
            downloadService = binder.getService();
            downloadService.setCallback(DownloadFragment.this);
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            downloadService = null;
            isServiceBound = false;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_download, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        editUrl = view.findViewById(R.id.edit_url);
        btnDownload = view.findViewById(R.id.btn_download);
        btnCancel = view.findViewById(R.id.btn_cancel);
        progressBar = view.findViewById(R.id.progress_bar);
        progressText = view.findViewById(R.id.progress_text);
        downloadingLayout = view.findViewById(R.id.downloading_layout);
        downloadDataLayout = view.findViewById(R.id.download_file_data);
        downloadingText = view.findViewById(R.id.is_downloading);

        downloadingLayout.setVisibility(View.GONE);

        btnDownload.setOnClickListener(v -> onDownloadClicked());

        btnCancel.setOnClickListener(v -> onCancelClicked());

        Intent serviceIntent = new Intent(getContext(), DownloadService.class);
        requireActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @SuppressLint("SetTextI18n")
    private void onDownloadClicked() {
        String url = editUrl.getText().toString().trim();
        if (!url.isEmpty()) {
            downloadDataLayout.setVisibility(View.GONE);
            downloadingText.setText("Downloading: " + url);
            downloadingLayout.setVisibility(View.VISIBLE);
            Executors.newSingleThreadExecutor().execute(() -> {
                boolean reachable = isURLReachable(url);

                requireActivity().runOnUiThread(() -> {
                    if (reachable) {
                        startDownload(url);
                    } else {
                        Toast.makeText(getContext(), "URL is not reachable", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        } else {
            Toast.makeText(getContext(), "Enter file URL", Toast.LENGTH_SHORT).show();
        }
    }

    private void onCancelClicked() {
        editUrl.setText("");
        downloadingLayout.setVisibility(View.GONE);
        downloadDataLayout.setVisibility(View.VISIBLE);
        cancelDownload();
    }

    private void startDownload(String url) {
        Intent serviceIntent = new Intent(getContext(), DownloadService.class);
        serviceIntent.putExtra("download_url", url);

        requireActivity().startService(serviceIntent);
    }

    private void cancelDownload() {
        if (isServiceBound && downloadService != null) {
            downloadService.cancelDownload();
        }
    }

    private boolean isURLReachable(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();

            return (responseCode >= 200 && responseCode < 400);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isServiceBound) {
            if (downloadService != null) {
                downloadService.removeCallback();
            }
            requireActivity().unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onProgressUpdate(int progress, String status) {
        requireActivity().runOnUiThread(() -> {
            progressBar.setProgress(progress);
            progressText.setText(status);
        });
    }

    @Override
    public void onComplete(boolean success, String message) {
        requireActivity().runOnUiThread(() -> {
            if (success) {
                downloadingLayout.setVisibility(View.GONE);
                downloadDataLayout.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                progressBar.setProgress(0);
                progressText.setText("");
                downloadingText.setText("");
                Toast.makeText(getContext(), "Download completed successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }
}


