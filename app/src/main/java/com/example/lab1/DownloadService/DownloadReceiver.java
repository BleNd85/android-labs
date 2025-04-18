package com.example.lab1.DownloadService;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

public class DownloadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        long savedDownloadId = DownloadPreferences.getDownloadId(context);

        if (savedDownloadId == -1) {
            return;
        }

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (downloadId == savedDownloadId) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);

                try (Cursor cursor = downloadManager.query(query)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int status = cursor.getInt(statusIndex);

                        Intent serviceIntent = new Intent(context, DownloadService.class);
                        serviceIntent.putExtra("download_id", downloadId);

                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            serviceIntent.setAction("DOWNLOAD_COMPLETE");
                            context.startService(serviceIntent);
                        } else if (status == DownloadManager.STATUS_FAILED) {
                            int reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                            int reason = cursor.getInt(reasonIndex);

                            serviceIntent.setAction("DOWNLOAD_FAILED");
                            serviceIntent.putExtra("failure_reason", reason);
                            context.startService(serviceIntent);
                        }
                    } else {
                        notifyDownloadCanceled(context, savedDownloadId);
                    }
                } catch (Exception e) {
                    notifyDownloadCanceled(context, savedDownloadId);
                }
            }
        } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(savedDownloadId);

            try (Cursor cursor = downloadManager.query(query)) {
                if (cursor == null || !cursor.moveToFirst()) {
                    notifyDownloadCanceled(context, savedDownloadId);
                }
            } catch (Exception e) {
                notifyDownloadCanceled(context, savedDownloadId);
            }
        }
    }

    private void notifyDownloadCanceled(Context context, long downloadId) {
        // Notify service that download was canceled externally
        Intent serviceIntent = new Intent(context, DownloadService.class);
        serviceIntent.setAction("DOWNLOAD_CANCELED");
        serviceIntent.putExtra("download_id", downloadId);
        context.startService(serviceIntent);

        DownloadPreferences.setDownloadId(context, -1);
    }
}