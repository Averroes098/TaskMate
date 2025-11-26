package com.example.taskmate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class TaskListActivity extends AppCompatActivity {

    private static final String TAG = "TaskListActivity";
    private static final int REQUEST_ADD_TASK = 1;

    RecyclerView recyclerView;
    ImageView btnAdd, btnBack;
    TaskAdapter adapter;
    TaskDatabase db;

    // Bottom Navigation Variables
    private LinearLayout menuKelolaTugas, menuDaftarTugas;
    private TextView textKelolaTugas, textDaftarTugas;
    private View indicatorKelolaTugas, indicatorDaftarTugas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        Log.d(TAG, "onCreate: Activity dimulai");

        recyclerView = findViewById(R.id.recyclerTask);
        btnAdd = findViewById(R.id.btnAdd);
        btnBack = findViewById(R.id.btnBack);

        db = new TaskDatabase(this);

        initBottomNavigation();
        setupRecyclerView();
        loadData();

        btnAdd.setOnClickListener(v -> {
            Log.d(TAG, "Tombol tambah diklik");
            Intent i = new Intent(TaskListActivity.this, AddTaskActivity.class);
            startActivityForResult(i, REQUEST_ADD_TASK);
        });

        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Tombol back diklik - Kembali ke MainActivity");
            navigateToMainActivity();
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(TaskListActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void initBottomNavigation() {
        Log.d(TAG, "Initialize Bottom Navigation");

        menuKelolaTugas = findViewById(R.id.menuKelolaTugas);
        menuDaftarTugas = findViewById(R.id.menuDaftarTugas);
        textKelolaTugas = findViewById(R.id.textKelolaTugas);
        textDaftarTugas = findViewById(R.id.textDaftarTugas);
        indicatorKelolaTugas = findViewById(R.id.indicatorKelolaTugas);
        indicatorDaftarTugas = findViewById(R.id.indicatorDaftarTugas);

        setActiveMenu(menuKelolaTugas);

        menuKelolaTugas.setOnClickListener(v -> {
            Log.d(TAG, "Menu Kelola Tugas diklik");
            setActiveMenu(menuKelolaTugas);
            loadData();
        });

        menuDaftarTugas.setOnClickListener(v -> {
            Log.d(TAG, "Menu Daftar Tugas diklik");
            setActiveMenu(menuDaftarTugas);
            navigateToDaftarTugas();
        });
    }

    private void setActiveMenu(LinearLayout activeMenu) {
        resetAllMenus();

        if (activeMenu.getId() == R.id.menuKelolaTugas) {
            textKelolaTugas.setTextColor(getResources().getColor(R.color.blue_500));
            textKelolaTugas.setTypeface(textKelolaTugas.getTypeface(), Typeface.BOLD);
            indicatorKelolaTugas.setVisibility(View.VISIBLE);
        } else if (activeMenu.getId() == R.id.menuDaftarTugas) {
            textDaftarTugas.setTextColor(getResources().getColor(R.color.blue_500));
            textDaftarTugas.setTypeface(textDaftarTugas.getTypeface(), Typeface.BOLD);
            indicatorDaftarTugas.setVisibility(View.VISIBLE);
        }
    }

    private void resetAllMenus() {
        textKelolaTugas.setTextColor(getResources().getColor(R.color.gray_500));
        textKelolaTugas.setTypeface(null, Typeface.NORMAL);
        indicatorKelolaTugas.setVisibility(View.INVISIBLE);

        textDaftarTugas.setTextColor(getResources().getColor(R.color.gray_500));
        textDaftarTugas.setTypeface(null, Typeface.NORMAL);
        indicatorDaftarTugas.setVisibility(View.INVISIBLE);
    }

    private void navigateToDaftarTugas() {
        Log.d(TAG, "Navigasi ke Daftar Tugas (Deadline Activity)");
        Intent intent = new Intent(TaskListActivity.this, DeadlineActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_TASK && resultCode == RESULT_OK) {
            Log.d(TAG, "Data baru ditambahkan, refresh list");
            loadData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Refresh data");
        loadData();
        setActiveMenu(menuKelolaTugas);
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setup RecyclerView");
        adapter = new TaskAdapter(this, new ArrayList<>(), task -> {
            Log.d(TAG, "Item diklik: " + task.getTitle());
            Intent i = new Intent(TaskListActivity.this, DetailTaskActivity.class);
            i.putExtra("id", task.getId());
            startActivity(i);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadData() {
        try {
            ArrayList<Task> tasks = db.getTasks();
            Log.d(TAG, "Load data: " + tasks.size() + " tasks ditemukan");
            adapter.updateData(tasks);
        } catch (Exception e) {
            Log.e(TAG, "Error loadData: " + e.getMessage(), e);
            Toast.makeText(this, "Error memuat data.", Toast.LENGTH_LONG).show();
        }
    }
}