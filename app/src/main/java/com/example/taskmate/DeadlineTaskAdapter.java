package com.example.taskmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DeadlineTaskAdapter extends RecyclerView.Adapter<DeadlineTaskAdapter.TaskViewHolder> {

    private ArrayList<Task> tasks;
    private final OnTaskClickListener listener;
    private final boolean showCompleteButton;

    // Interface tunggal untuk semua interaksi klik
    public interface OnTaskClickListener {
        void onTaskClick(Task task, int position);
        void onCompleteClick(Task task, int position);
    }

    public DeadlineTaskAdapter(ArrayList<Task> tasks, OnTaskClickListener listener, boolean showCompleteButton) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.listener = listener;
        this.showCompleteButton = showCompleteButton;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_deadline, parent, false);
        return new TaskViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task, showCompleteButton);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateData(ArrayList<Task> newTasks) {
        this.tasks.clear();
        if (newTasks != null) {
            this.tasks.addAll(newTasks);
        }
        notifyDataSetChanged();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskTitle, taskDeadline, taskDescription;
        private final ImageView btnComplete;

        public TaskViewHolder(@NonNull View itemView, OnTaskClickListener listener) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDeadline = itemView.findViewById(R.id.taskDeadline);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            btnComplete = itemView.findViewById(R.id.btnComplete);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onTaskClick(tasks.get(position), position);
                }
            });

            btnComplete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onCompleteClick(tasks.get(position), position);
                }
            });
        }

        public void bind(Task task, boolean showButton) {
            taskTitle.setText(task.getTitle());
            taskDeadline.setText("Tenggat: " + task.getDate());
            taskDescription.setText(task.getDescription());
            taskDescription.setVisibility(task.getDescription().isEmpty() ? View.GONE : View.VISIBLE);

            setDeadlineColor(task);
            btnComplete.setVisibility(showButton ? View.VISIBLE : View.GONE);
        }
        
        private void setDeadlineColor(Task task) {
            long millisLeft = getTimeUntilDeadline(task.getDate());
            int colorRes;

            if (task.isCompleted()) {
                colorRes = R.color.gray_500;
            } else if (millisLeft < 0) {
                colorRes = R.color.red_dark;
            } else if (millisLeft < TimeUnit.DAYS.toMillis(1)) {
                colorRes = R.color.red_500;
            } else if (millisLeft < TimeUnit.DAYS.toMillis(3)) {
                colorRes = R.color.yellow_500;
            } else {
                colorRes = R.color.black;
            }
            taskTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), colorRes));
            taskDeadline.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.gray_500));
        }

        private long getTimeUntilDeadline(String deadlineStr) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                Date deadlineDate = sdf.parse(deadlineStr);
                return deadlineDate != null ? deadlineDate.getTime() - System.currentTimeMillis() : -1;
            } catch (ParseException e) {
                return -1; // Jika format tidak cocok, anggap sudah lewat
            }
        }
    }
}