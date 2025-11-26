package com.example.taskmate;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button btnKelola, btnDaftar;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Izin notifikasi diberikan.", Toast.LENGTH_SHORT).show();
                    checkAndRequestExactAlarmPermission();
                } else {
                    Toast.makeText(this, "Izin notifikasi ditolak.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "MainActivity onCreate");

        btnKelola = findViewById(R.id.btnKelola);
        btnDaftar = findViewById(R.id.btnDaftar);

        askForNotificationPermission();

        btnKelola.setOnClickListener(v -> {
            Log.d(TAG, "Kelola Tugas diklik");
            startActivity(new Intent(MainActivity.this, TaskListActivity.class));
        });

        btnDaftar.setOnClickListener(v -> {
            Log.d(TAG, "Daftar Tugas diklik");
            startActivity(new Intent(MainActivity.this, DeadlineActivity.class));
        });
    }

    private void askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                checkAndRequestExactAlarmPermission();
            }
        } else {
            checkAndRequestExactAlarmPermission();
        }
    }

    private void checkAndRequestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Izin Diperlukan")
                        .setMessage("Untuk memastikan notifikasi muncul tepat waktu, aplikasi ini memerlukan izin untuk menjadwalkan alarm. Anda akan diarahkan ke pengaturan.")
                        .setPositiveButton("Buka Pengaturan", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intent);
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            } else {
                scheduleAllNotifications();
            }
        } else {
            scheduleAllNotifications();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cek kembali izin setiap kali pengguna kembali ke MainActivity, kalau-kalau mereka baru memberikannya
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager.canScheduleExactAlarms()) {
                scheduleAllNotifications();
            }
        } else {
             scheduleAllNotifications();
        }
    }

    private void scheduleAllNotifications() {
        DeadlineReceiver.scheduleAllNotifications(this);
        Log.d(TAG, "Rescheduling all notifications on resume.");
    }
}