package com.example.taskmate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DetailTaskActivity extends AppCompatActivity {

    private TextView detailTitle, detailDate, detailDesc;
    private TaskDatabase db;
    private int taskId = -1;
    private Task selectedTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_task);

        detailTitle = findViewById(R.id.detailTitle);
        detailDate = findViewById(R.id.detailDate);
        detailDesc = findViewById(R.id.detailDesc);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnDelete = findViewById(R.id.btnDelete);
        ImageView btnBack = findViewById(R.id.btnBack);

        db = new TaskDatabase(this);
        taskId = getIntent().getIntExtra("id", -1);

        if (taskId == -1) {
            Toast.makeText(this, "Error: Task ID tidak valid.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnBack.setOnClickListener(v -> finish());
        btnEdit.setOnClickListener(v -> editTask());
        btnDelete.setOnClickListener(v -> deleteTask());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTaskData();
    }

    private void loadTaskData() {
        selectedTask = db.getTaskById(taskId);
        if (selectedTask != null) {
            detailTitle.setText(selectedTask.getTitle());
            detailDate.setText(selectedTask.getDate());
            detailDesc.setText(selectedTask.getDescription());
        } else {
            Toast.makeText(this, "Tugas tidak ditemukan.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void editTask() {
        if (selectedTask != null) {
            Intent i = new Intent(DetailTaskActivity.this, EditTaskActivity.class);
            i.putExtra("id", selectedTask.getId());
            startActivity(i);
        }
    }

    private void deleteTask() {
        if (selectedTask != null) {
            db.deleteTask(taskId);
            
            // Batalkan semua alarm terjadwal untuk tugas ini
            DeadlineReceiver.cancelTaskNotifications(this, taskId);

            Toast.makeText(this, "Tugas berhasil dihapus.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}