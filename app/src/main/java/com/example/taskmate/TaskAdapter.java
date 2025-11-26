package com.example.taskmate;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private static final String TAG = "TaskAdapter";
    private List<Task> taskList;
    private OnItemClickListener listener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(Task task);
    }

    public TaskAdapter(Context context, List<Task> taskList, OnItemClickListener listener) {
        this.context = context;
        this.taskList = taskList != null ? taskList : new ArrayList<>();
        this.listener = listener;
        Log.d(TAG, "Adapter dibuat dengan " + this.taskList.size() + " items");
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        if (taskList == null || taskList.isEmpty()) {
            Log.w(TAG, "onBindViewHolder: taskList kosong");
            return;
        }

        Task task = taskList.get(position);
        Log.d(TAG, "onBindViewHolder: " + position + " - " + task.getTitle());

        holder.txtTitle.setText(task.getTitle());
        holder.txtDate.setText(task.getDate());

        // Klik item → detail task
        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "Item diklik: " + task.getTitle());
            listener.onItemClick(task);
        });

        // Klik tombol edit
        holder.btnEdit.setOnClickListener(v -> {
            Log.d(TAG, "Edit diklik: " + task.getTitle());
            Intent intent = new Intent(context, EditTaskActivity.class);
            intent.putExtra("id", task.getId());
            context.startActivity(intent);
        });

        // Klik tombol delete
        holder.btnDelete.setOnClickListener(v -> {
            Log.d(TAG, "Delete diklik: " + task.getTitle());
            TaskDatabase db = new TaskDatabase(context);
            db.deleteTask(task.getId());

            // Remove from list and notify adapter
            taskList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, taskList.size());

            Toast.makeText(context, "Tugas dihapus: " + task.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        int count = taskList != null ? taskList.size() : 0;
        Log.d(TAG, "getItemCount: " + count);
        return count;
    }

    public void updateData(List<Task> newTasks) {
        Log.d(TAG, "updateData: " + (newTasks != null ? newTasks.size() : 0) + " items");

        if (newTasks == null) {
            this.taskList = new ArrayList<>();
        } else {
            this.taskList = new ArrayList<>(newTasks);
        }

        notifyDataSetChanged();
        Log.d(TAG, "Data updated, notifying adapter");
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDate;
        ImageView btnEdit, btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDate = itemView.findViewById(R.id.txtDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            // Log untuk memastikan view ditemukan
            if (txtTitle == null) Log.e("TaskViewHolder", "txtTitle not found");
            if (txtDate == null) Log.e("TaskViewHolder", "txtDate not found");
            if (btnEdit == null) Log.e("TaskViewHolder", "btnEdit not found");
            if (btnDelete == null) Log.e("TaskViewHolder", "btnDelete not found");
        }
    }
}