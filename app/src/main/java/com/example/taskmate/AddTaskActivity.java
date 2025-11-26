package com.example.taskmate;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    EditText inputTitle, inputDate, inputDesc;
    Button btnSave;
    ImageView btnBack;
    TaskDatabase db;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        inputTitle = findViewById(R.id.inputNama);
        inputDate = findViewById(R.id.inputTanggal);
        inputDesc = findViewById(R.id.inputDeskripsi);
        btnSave = findViewById(R.id.btnSimpan);
        btnBack = findViewById(R.id.btnBack);

        db = new TaskDatabase(this);
        calendar = Calendar.getInstance();

        setupDateTimePicker();

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveTask());
    }

    private void setupDateTimePicker() {
        inputDate.setFocusable(false);
        inputDate.setClickable(true);
        inputDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, y, m, d) -> {
            calendar.set(Calendar.YEAR, y);
            calendar.set(Calendar.MONTH, m);
            calendar.set(Calendar.DAY_OF_MONTH, d);
            showTimePicker();
        }, year, month, day).show();
    }

    private void showTimePicker() {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(this, (view, h, m) -> {
            calendar.set(Calendar.HOUR_OF_DAY, h);
            calendar.set(Calendar.MINUTE, m);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            inputDate.setText(dateFormat.format(calendar.getTime()));
        }, hour, minute, true).show();
    }

    private void saveTask() {
        String title = inputTitle.getText().toString().trim();
        String date = inputDate.getText().toString().trim();
        String desc = inputDesc.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Judul dan tanggal harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        int id = (int) System.currentTimeMillis();
        Task newTask = new Task(id, title, date, desc, false);
        db.addTask(newTask);

        // Jadwalkan notifikasi individual untuk tugas baru ini
        DeadlineReceiver.scheduleTaskNotifications(this, newTask);

        Toast.makeText(this, "Tugas berhasil ditambahkan", Toast.LENGTH_SHORT).show();

        setResult(RESULT_OK);
        finish();
    }
}