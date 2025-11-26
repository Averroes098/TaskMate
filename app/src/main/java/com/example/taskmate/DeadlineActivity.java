package com.example.taskmate;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DeadlineActivity extends AppCompatActivity {

    private static final String TAG = "DeadlineActivity";

    private RecyclerView recyclerUncompleted, recyclerCompleted;
    private DeadlineTaskAdapter uncompletedAdapter, completedAdapter;
    private TaskDatabase db;
    private TextView sectionUncompleted, sectionCompleted;

    private LinearLayout menuKelolaTugas, menuDaftarTugas;
    private TextView textKelolaTugas, textDaftarTugas;
    private View indicatorKelolaTugas, indicatorDaftarTugas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deadline);
        initViews();
        initBottomNavigation();
        setupRecyclerViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        setActiveMenu(menuDaftarTugas);
    }

    private void initViews() {
        recyclerUncompleted = findViewById(R.id.recyclerUncompleted);
        recyclerCompleted = findViewById(R.id.recyclerCompleted);
        sectionUncompleted = findViewById(R.id.sectionUncompleted);
        sectionCompleted = findViewById(R.id.sectionCompleted);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        db = new TaskDatabase(this);

        menuKelolaTugas = findViewById(R.id.menuKelolaTugas);
        menuDaftarTugas = findViewById(R.id.menuDaftarTugas);
        textKelolaTugas = findViewById(R.id.textKelolaTugas);
        textDaftarTugas = findViewById(R.id.textDaftarTugas);
        indicatorKelolaTugas = findViewById(R.id.indicatorKelolaTugas);
        indicatorDaftarTugas = findViewById(R.id.indicatorDaftarTugas);
    }

    private void initBottomNavigation() {
        menuKelolaTugas.setOnClickListener(v -> {
            startActivity(new Intent(this, TaskListActivity.class));
            finish();
        });
        menuDaftarTugas.setOnClickListener(v -> setActiveMenu(menuDaftarTugas));
    }

    private void setActiveMenu(LinearLayout activeMenu) {
        resetAllMenus();
        if (activeMenu.getId() == R.id.menuKelolaTugas) {
            textKelolaTugas.setTextColor(ContextCompat.getColor(this, R.color.blue_500));
            textKelolaTugas.setTypeface(null, Typeface.BOLD);
            indicatorKelolaTugas.setVisibility(View.VISIBLE);
        } else if (activeMenu.getId() == R.id.menuDaftarTugas) {
            textDaftarTugas.setTextColor(ContextCompat.getColor(this, R.color.blue_500));
            textDaftarTugas.setTypeface(null, Typeface.BOLD);
            indicatorDaftarTugas.setVisibility(View.VISIBLE);
        }
    }

    private void resetAllMenus() {
        textKelolaTugas.setTextColor(ContextCompat.getColor(this, R.color.gray_500));
        textKelolaTugas.setTypeface(null, Typeface.NORMAL);
        indicatorKelolaTugas.setVisibility(View.INVISIBLE);
        textDaftarTugas.setTextColor(ContextCompat.getColor(this, R.color.gray_500));
        textDaftarTugas.setTypeface(null, Typeface.NORMAL);
        indicatorDaftarTugas.setVisibility(View.INVISIBLE);
    }

    private void setupRecyclerViews() {
        uncompletedAdapter = new DeadlineTaskAdapter(new ArrayList<>(), new DeadlineTaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task, int position) { openTaskDetail(task); }
            @Override
            public void onCompleteClick(Task task, int position) { markTaskAsCompleted(task); }
        }, true);

        completedAdapter = new DeadlineTaskAdapter(new ArrayList<>(), new DeadlineTaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task, int position) { openTaskDetail(task); }
            @Override
            public void onCompleteClick(Task task, int position) { /* No action */ }
        }, false);

        recyclerUncompleted.setLayoutManager(new LinearLayoutManager(this));
        recyclerUncompleted.setAdapter(uncompletedAdapter);
        recyclerCompleted.setLayoutManager(new LinearLayoutManager(this));
        recyclerCompleted.setAdapter(completedAdapter);
    }

    private void openTaskDetail(Task task) {
        Intent intent = new Intent(this, DetailTaskActivity.class);
        intent.putExtra("id", task.getId());
        startActivity(intent);
    }

    private void markTaskAsCompleted(Task task) {
        db.toggleTaskStatus(task.getId(), true);
        DeadlineReceiver.cancelTaskNotifications(this, task.getId());
        Toast.makeText(this, "Tugas '" + task.getTitle() + "' selesai", Toast.LENGTH_SHORT).show();
        loadData();
    }

    private void loadData() {
        ArrayList<Task> uncompletedTasks = db.getUncompletedTasks();
        ArrayList<Task> completedTasks = db.getCompletedTasks();

        uncompletedAdapter.updateData(uncompletedTasks);
        completedAdapter.updateData(completedTasks);
        updateSectionVisibility(uncompletedTasks, completedTasks);
    }

    private void updateSectionVisibility(ArrayList<Task> uncompleted, ArrayList<Task> completed) {
        sectionUncompleted.setVisibility(uncompleted.isEmpty() ? View.GONE : View.VISIBLE);
        recyclerUncompleted.setVisibility(uncompleted.isEmpty() ? View.GONE : View.VISIBLE);
        sectionCompleted.setVisibility(completed.isEmpty() ? View.GONE : View.VISIBLE);
        recyclerCompleted.setVisibility(completed.isEmpty() ? View.GONE : View.VISIBLE);
    }
}