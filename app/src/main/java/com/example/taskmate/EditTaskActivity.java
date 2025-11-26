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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class EditTaskActivity extends AppCompatActivity {

    EditText inputTitle, inputDate, inputDesc;
    Button btnUpdate;
    ImageView btnBack;

    TaskDatabase db;
    Calendar calendar;
    int taskId;
    Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        inputTitle = findViewById(R.id.inputNama);
        inputDate = findViewById(R.id.inputTanggal);
        inputDesc = findViewById(R.id.inputDeskripsi);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnBack = findViewById(R.id.btnBack);

        db = new TaskDatabase(this);
        calendar = Calendar.getInstance();

        // Get task ID from intent
        taskId = getIntent().getIntExtra("id", -1);
        if (taskId == -1) {
            Toast.makeText(this, "Error: Task ID tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadTaskData();
        setupDateTimePicker();

        btnBack.setOnClickListener(v -> {
            finish();
        });

        btnUpdate.setOnClickListener(v -> updateTask());
    }

    private void loadTaskData() {
        ArrayList<Task> list = db.getTasks();
        for (Task t : list) {
            if (t.getId() == taskId) {
                currentTask = t;
                inputTitle.setText(t.getTitle());
                inputDate.setText(t.getDate());
                inputDesc.setText(t.getDescription());
                break;
            }
        }

        if (currentTask == null) {
            Toast.makeText(this, "Task tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupDateTimePicker() {
        inputDate.setOnClickListener(v -> showDateTimePicker());
        inputDate.setFocusable(false);
        inputDate.setClickable(true);
    }

    private void showDateTimePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    showTimePicker();
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void showTimePicker() {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                    calendar.set(Calendar.MINUTE, selectedMinute);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                    String formattedDate = dateFormat.format(calendar.getTime());

                    inputDate.setText(formattedDate);
                },
                hour, minute, true
        );
        timePickerDialog.show();
    }

    private void updateTask() {
        String title = inputTitle.getText().toString();
        String date = inputDate.getText().toString();
        String desc = inputDesc.getText().toString();

        // Validasi input
        if (title.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Judul dan tanggal harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        Task updatedTask = new Task(taskId, title, date, desc);
        db.updateTask(updatedTask);

        Toast.makeText(this, "Tugas berhasil diupdate", Toast.LENGTH_SHORT).show();

        // Kembali ke DetailTaskActivity dengan data terupdate
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updated", true);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}