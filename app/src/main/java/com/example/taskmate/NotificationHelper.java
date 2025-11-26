package com.example.taskmate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    public static void showDeadlineNotifications(Context context, ArrayList<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            Log.d(TAG, "Tidak ada tugas untuk diperiksa");
            return;
        }

        ArrayList<Task> urgentTasks = new ArrayList<>();

        // Cari tugas yang deadline-nya tinggal 2 hari lagi atau kurang
        for (Task task : tasks) {
            if (task.isCompleted()) {
                continue; // Skip tugas yang sudah selesai
            }

            long millisLeft = getTimeUntilDeadline(task.getDate());
            if (millisLeft > 0 && millisLeft <= TimeUnit.DAYS.toMillis(2)) {
                urgentTasks.add(task);
            }
        }

        if (!urgentTasks.isEmpty()) {
            showNotificationDialog(context, urgentTasks);
        }
    }

    private static void showNotificationDialog(Context context, ArrayList<Task> urgentTasks) {
        StringBuilder message = new StringBuilder();
        message.append("Tugas berikut mendekati deadline:\n\n");

        for (Task task : urgentTasks) {
            long millisLeft = getTimeUntilDeadline(task.getDate());
            String timeLeftText = getTimeLeftText(millisLeft);
            message.append("• ").append(task.getTitle())
                    .append(" - ").append(timeLeftText)
                    .append(" (").append(task.getDate()).append(")\n");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("⏰ Deadline Mendekati!")
                .setMessage(message.toString())
                .setPositiveButton("Mengerti", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("Lihat Detail", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Navigate to DeadlineActivity
                        if (context instanceof DeadlineActivity) {
                            // Already in DeadlineActivity
                            dialog.dismiss();
                        } else {
                            // TODO: Add intent to navigate to DeadlineActivity if needed
                            dialog.dismiss();
                        }
                    }
                })
                .setCancelable(false)
                .create()
                .show();

        Log.d(TAG, "Menampilkan notifikasi untuk " + urgentTasks.size() + " tugas urgent");
    }

    private static String getTimeLeftText(long millisLeft) {
        if (millisLeft < 0) {
            return "Telah lewat";
        }

        long days = TimeUnit.MILLISECONDS.toDays(millisLeft);
        millisLeft -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millisLeft);
        millisLeft -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisLeft);

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" hari ");
        }
        if (hours > 0) {
            sb.append(hours).append(" jam ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(" menit");
        }

        if (sb.length() == 0) {
            return "Kurang dari semenit";
        }

        return sb.toString().trim() + " lagi";
    }

    public static long getTimeUntilDeadline(String deadlineStr) {
        try {
            // Format tanggal: "dd-MM-yyyy HH:mm"
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            Date deadlineDate = sdf.parse(deadlineStr);

            if (deadlineDate == null) {
                return -1;
            }

            return deadlineDate.getTime() - System.currentTimeMillis();

        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + deadlineStr, e);
            // Coba format lama jika gagal
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Date deadlineDate = sdf.parse(deadlineStr);

                if (deadlineDate == null) {
                    return -1;
                }
                // Set to end of day
                Calendar cal = Calendar.getInstance();
                cal.setTime(deadlineDate);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);

                return cal.getTimeInMillis() - System.currentTimeMillis();
            } catch (ParseException ex) {
                Log.e(TAG, "Error parsing old date format: " + deadlineStr, ex);
                return -1;
            }
        }
    }
}