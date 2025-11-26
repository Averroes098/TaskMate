package com.example.taskmate;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DeadlineReceiver extends BroadcastReceiver {

    private static final String TAG = "DeadlineReceiver";
    private static final String CHANNEL_ID = "DEADLINE_TASK_CHANNEL";
    public static final String ACTION_COMPLETE_TASK = "com.example.taskmate.ACTION_COMPLETE_TASK";

    // Request code unik untuk mencegah konflik PendingIntent
    private static final int RC_BASE_ALARM = 1000;
    private static final int RC_BASE_OPEN_APP = 2000;
    private static final int RC_BASE_COMPLETE = 3000;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive triggered with action: " + intent.getAction());
        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log.d(TAG, "Device rebooted, rescheduling all task alarms.");
            scheduleAllNotifications(context);
        } else if (ACTION_COMPLETE_TASK.equals(action)) {
            handleCompleteTaskAction(context, intent);
        } else {
            handleScheduledAlarm(context, intent);
        }
    }

    private void handleCompleteTaskAction(Context context, Intent intent) {
        int taskId = intent.getIntExtra("task_id", -1);
        if (taskId != -1) {
            TaskDatabase db = new TaskDatabase(context);
            db.toggleTaskStatus(taskId, true);
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(taskId); // Hapus notifikasi yang sedang tampil
            Log.d(TAG, "Task " + taskId + " marked complete via notification action.");
        }
    }

    private void handleScheduledAlarm(Context context, Intent intent) {
        int taskId = intent.getIntExtra("task_id", -1);
        if (taskId != -1) {
            TaskDatabase db = new TaskDatabase(context);
            Task task = db.getTaskById(taskId);
            // Hanya tampilkan notifikasi jika tugasnya masih ada dan belum selesai
            if (task != null && !task.isCompleted()) {
                showTaskNotification(context, task);
            }
        }
    }

    private void showTaskNotification(Context context, Task task) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel(context);

        long millisLeft = getTimeUntilDeadline(task.getDate());
        if (millisLeft <= 0) return; // Jangan tampilkan notif jika sudah lewat

        String timeLeftText = formatTimeLeft(millisLeft);
        String title = "Tenggat Mendekat: " + task.getTitle();
        String message = "Sisa waktu: " + timeLeftText;

        // Intent untuk membuka detail tugas
        Intent detailIntent = new Intent(context, DetailTaskActivity.class);
        detailIntent.putExtra("id", task.getId());
        PendingIntent detailPendingIntent = PendingIntent.getActivity(
                context, task.getId() + RC_BASE_OPEN_APP, detailIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent untuk tombol "Selesai"
        Intent completeIntent = new Intent(context, DeadlineReceiver.class);
        completeIntent.setAction(ACTION_COMPLETE_TASK);
        completeIntent.putExtra("task_id", task.getId());
        PendingIntent completePendingIntent = PendingIntent.getBroadcast(
                context, task.getId() + RC_BASE_COMPLETE, completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(detailPendingIntent)
                .addAction(android.R.drawable.checkbox_on_background, "Tandai Selesai", completePendingIntent)
                .setAutoCancel(true); // Hilang saat di-klik

        // Gunakan ID tugas sebagai ID notifikasi agar notifikasi lama otomatis tergantikan
        notificationManager.notify(task.getId(), builder.build());
        Log.d(TAG, "Notification shown for task: " + task.getTitle());
    }

    public static void scheduleTaskNotifications(Context context, Task task) {
        cancelTaskNotifications(context, task.getId()); // Batalkan alarm lama sebelum set yang baru

        Date deadlineDate = parseDate(task.getDate());
        if (deadlineDate == null || task.isCompleted()) return;

        long now = System.currentTimeMillis();
        long deadlineMillis = deadlineDate.getTime();

        // Tentukan interval notifikasi
        long[] intervals = {
                TimeUnit.DAYS.toMillis(7),
                TimeUnit.DAYS.toMillis(3),
                TimeUnit.DAYS.toMillis(1),
                TimeUnit.HOURS.toMillis(6),
                TimeUnit.HOURS.toMillis(1)
        };

        int alarmIndex = 0;
        for (long interval : intervals) {
            long triggerAtMillis = deadlineMillis - interval;
            if (triggerAtMillis > now) {
                scheduleExactAlarm(context, task.getId(), triggerAtMillis, alarmIndex++);
            }
        }
        Log.d(TAG, "Scheduled alarms for task: " + task.getTitle());
    }

    public static void scheduleAllNotifications(Context context) {
        TaskDatabase db = new TaskDatabase(context);
        ArrayList<Task> tasks = db.getUncompletedTasks();
        for (Task task : tasks) {
            scheduleTaskNotifications(context, task);
        }
        Log.d(TAG, "Rescheduled alarms for " + tasks.size() + " uncompleted tasks.");
    }

    public static void cancelTaskNotifications(Context context, int taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Kita harus membatalkan semua 5 kemungkinan alarm
        for (int i = 0; i < 5; i++) {
            int requestCode = taskId + RC_BASE_ALARM + i;
            Intent intent = new Intent(context, DeadlineReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, requestCode, intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            if (pendingIntent != null && alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
        // Hapus juga notifikasi yang mungkin sedang tampil
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(taskId);
        Log.d(TAG, "Cancelled alarms and notification for task ID: " + taskId);
    }

    private static void scheduleExactAlarm(Context context, int taskId, long triggerAtMillis, int index) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        
        Intent intent = new Intent(context, DeadlineReceiver.class);
        intent.putExtra("task_id", taskId);
        
        // Request code harus unik untuk setiap alarm
        int requestCode = taskId + RC_BASE_ALARM + index;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w(TAG, "Exact alarm permission not granted. Notifications may be delayed.");
            // Opsi: fallback ke alarm tidak tepat waktu
        }
        
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }

    // Helper Methods
    private static Date parseDate(String dateStr) {
        try {
            return new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    private long getTimeUntilDeadline(String dateStr) {
        Date deadline = parseDate(dateStr);
        return deadline != null ? deadline.getTime() - System.currentTimeMillis() : -1;
    }

    private String formatTimeLeft(long millisLeft) {
        long days = TimeUnit.MILLISECONDS.toDays(millisLeft);
        long hours = TimeUnit.MILLISECONDS.toHours(millisLeft) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisLeft) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(" hari ");
        if (hours > 0) sb.append(hours).append(" jam ");
        if (minutes > 0) sb.append(minutes).append(" menit");
        return sb.length() > 0 ? sb.toString().trim() : "Kurang dari semenit";
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                CharSequence name = "Pengingat Tenggat Tugas";
                String description = "Notifikasi untuk setiap tugas yang mendekati tenggat.";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                manager.createNotificationChannel(channel);
            }
        }
    }
}