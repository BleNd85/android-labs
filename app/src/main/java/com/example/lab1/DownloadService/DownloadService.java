package com.example.lab1.DownloadService;

import static com.example.lab1.DownloadService.DownloadPreferences.setDownloadId;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DownloadService extends Service {
    private DownloadManager downloadManager;
    private long downloadId = -1;
    private ScheduledExecutorService scheduledExecutorService;
    private final IBinder binder = new LocalBinder();
    private DownloadCallback callback;

    public interface DownloadCallback {
        void onProgressUpdate(int progress, String status);

        void onComplete(boolean success, String message);
    }

    public class LocalBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }

        String action = intent.getAction();

        if (action != null) {
            switch (action) {
                case "DOWNLOAD_COMPLETE":
                    handleDownloadComplete();
                    break;

                case "DOWNLOAD_FAILED":
                    int failureReason = intent.getIntExtra("failure_reason", 0);
                    handleDownloadFailed(failureReason);
                    break;

                case "DOWNLOAD_CANCELED":
                    handleExternalCancellation();
                    break;

                default:
                    String url = intent.getStringExtra("download_url");
                    if (url != null && !url.isEmpty()) {
                        startDownload(url);
                    }
                    break;
            }
        } else {
            String url = intent.getStringExtra("download_url");

            if (url != null && !url.isEmpty()) {
                startDownload(url);
            }
        }

        return START_STICKY;
    }

    private void handleDownloadComplete() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }

        if (callback != null) {
            callback.onComplete(true, "Download completed!");
        }

        downloadId = -1;
        stopSelf();
    }

    private void handleDownloadFailed(int reason) {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }

        String errorMessage = "Download failed. Error code: " + reason;

        if (callback != null) {
            callback.onComplete(false, errorMessage);
        }

        downloadId = -1;
        stopSelf();
    }

    private void handleExternalCancellation() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }

        if (callback != null) {
            callback.onComplete(false, "Download canceled!");
        }

        downloadId = -1;
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setCallback(DownloadCallback callback) {
        this.callback = callback;
    }

    public void removeCallback() {
        this.callback = null;
    }

    public boolean isDownloading() {
        return downloadId != -1;
    }

    public void startDownload(String url) {
        if (downloadId != -1) {
            return;
        }

        Uri uri = Uri.parse(url);
        String fileName = uri.getLastPathSegment();

        if (fileName == null) {
            fileName = "downloaded_media_" + System.currentTimeMillis();
        }

        try {
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE).setTitle("Downloading Media").setDescription("Downloading media file").setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED).setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            downloadId = downloadManager.enqueue(request);
            setDownloadId(this, downloadId);

            if (callback != null) {
                callback.onProgressUpdate(0, "Starting download...");
            }

            trackDownloadProgress();
        } catch (Exception e) {
            Toast.makeText(this, "Invalid URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();

            if (callback != null) {
                callback.onComplete(false, "Invalid URL: " + e.getMessage());
            }
            stopSelf();
        }
    }

    private void trackDownloadProgress() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        final long[] lastProgressTime = {System.currentTimeMillis()};
        final long[] lastBytesDownloaded = {0};

        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (downloadId == -1) {
                return;
            }

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);

            try (Cursor cursor = downloadManager.query(query)) {
                if (cursor == null || !cursor.moveToFirst()) {
                    if (scheduledExecutorService != null) {
                        scheduledExecutorService.shutdown();
                    }

                    if (callback != null) {
                        callback.onComplete(false, "Download canceled externally");
                    }

                    downloadId = -1;
                    setDownloadId(this, -1);
                    stopSelf();
                    return;
                }

                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(statusIndex);

                if (status == DownloadManager.STATUS_RUNNING) {
                    int bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                    int bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);

                    long bytesDownloaded = cursor.getLong(bytesDownloadedIndex);
                    long bytesTotal = cursor.getLong(bytesTotalIndex);

                    if (bytesDownloaded > lastBytesDownloaded[0]) {
                        lastBytesDownloaded[0] = bytesDownloaded;
                        lastProgressTime[0] = System.currentTimeMillis();
                    }

                    if (bytesTotal > 0) {
                        int progress = (int) (100 * bytesDownloaded / bytesTotal);
                        String statusMessage = "Downloading: " + progress + "%";

                        if (callback != null) {
                            callback.onProgressUpdate(progress, statusMessage);
                        }
                    }
                } else if (status == DownloadManager.STATUS_PAUSED) {
                    int reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                    int reason = cursor.getInt(reasonIndex);
                    final String pauseReason = getPauseReason(reason);

                    if (callback != null) {
                        callback.onProgressUpdate(-1, "Download paused: " + pauseReason);
                    }
                } else if (status == DownloadManager.STATUS_PENDING) {
                    if (callback != null) {
                        callback.onProgressUpdate(0, "Download pending...");
                    }
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onProgressUpdate(-1, "Error tracking progress: " + e.getMessage());
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void cancelDownload() {
        if (downloadId != -1) {
            int removedDownloads = downloadManager.remove(downloadId);

            if (removedDownloads > 0) {
                Toast.makeText(this, "Download cancelled", Toast.LENGTH_SHORT).show();
            }

            if (scheduledExecutorService != null) {
                scheduledExecutorService.shutdown();
            }

            if (callback != null) {
                callback.onComplete(false, "Download cancelled");
            }

            setDownloadId(this, -1);

            downloadId = -1;
            stopSelf();
        }
    }

    private static String getPauseReason(int reason) {
        String pauseReason;

        switch (reason) {
            case DownloadManager.PAUSED_WAITING_TO_RETRY:
                pauseReason = "Waiting to retry";
                break;
            case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                pauseReason = "Waiting for network";
                break;
            case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                pauseReason = "Waiting for WiFi";
                break;
            case DownloadManager.PAUSED_UNKNOWN:
                pauseReason = "Paused for unknown reason";
                break;
            default:
                pauseReason = "Paused: " + reason;
                break;
        }

        return pauseReason;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
    }
}