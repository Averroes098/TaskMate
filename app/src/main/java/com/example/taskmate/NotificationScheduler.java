package com.example.taskmate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

public class NotificationScheduler {

    private static final String TAG = "NotificationScheduler";

    public static void scheduleDailyDeadlineCheck(Context context) {
        try {
            Log.d(TAG, "Menjadwalkan pengecekan deadline harian");

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e(TAG, "AlarmManager is null");
                return;
            }

            Intent intent = new Intent(context, DeadlineReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Set waktu untuk pengecekan (jam 9 pagi setiap hari)
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            // Jika waktu sudah lewat hari ini, set untuk besok
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            // Gunakan metode yang sesuai dengan versi Android
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Untuk Android 6.0+, cek permission dulu
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Android 12+ butuh SCHEDULE_EXACT_ALARM permission
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(),
                                pendingIntent
                        );
                        Log.d(TAG, "Exact alarm dijadwalkan (Android 12+)");
                    } else {
                        // Fallback ke inexact alarm jika permission tidak diberikan
                        alarmManager.setInexactRepeating(
                                AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(),
                                AlarmManager.INTERVAL_DAY,
                                pendingIntent
                        );
                        Log.d(TAG, "Inexact alarm dijadwalkan (fallback)");
                    }
                } else {
                    // Android 6.0 - 11
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                    Log.d(TAG, "Exact alarm dijadwalkan (Android 6.0-11)");
                }
            } else {
                // Untuk Android < 6.0
                alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                );
                Log.d(TAG, "Repeating alarm dijadwalkan (Android < 6.0)");
            }

            Log.d(TAG, "Pengecekan deadline dijadwalkan jam 9 pagi setiap hari");

        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: Tidak ada permission untuk exact alarm", e);
            // Fallback ke WorkManager atau metode lain
            scheduleWithWorkManager(context);
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling notification: " + e.getMessage(), e);
        }
    }

    // Fallback menggunakan WorkManager (lebih reliable untuk background tasks)
    private static void scheduleWithWorkManager(Context context) {
        try {
            Log.d(TAG, "Menggunakan WorkManager sebagai fallback");

            // WorkManager lebih reliable dan tidak memerlukan permission khusus
            // Implementasi WorkManager akan ditambahkan di langkah berikutnya
            scheduleWithAlarmManagerInexact(context);

        } catch (Exception e) {
            Log.e(TAG, "Error with WorkManager fallback: " + e.getMessage(), e);
        }
    }

    // Fallback ke inexact alarm (tidak memerlukan permission khusus)
    private static void scheduleWithAlarmManagerInexact(Context context) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, DeadlineReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            // Gunakan setInexactRepeating yang tidak memerlukan permission khusus
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );

            Log.d(TAG, "Inexact repeating alarm dijadwalkan (fallback)");

        } catch (Exception e) {
            Log.e(TAG, "Error with inexact alarm fallback: " + e.getMessage(), e);
        }
    }

    // Method untuk memeriksa apakah exact alarm diizinkan (Android 12+)
    public static boolean canScheduleExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true; // Untuk Android < 12, selalu return true
    }
}