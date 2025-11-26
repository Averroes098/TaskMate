package com.example.taskmate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d(TAG, "Device rebooted, rescheduling notifications");
            // Jadwalkan ulang notifikasi setelah reboot
            NotificationScheduler.scheduleDailyDeadlineCheck(context);
        }
    }
}