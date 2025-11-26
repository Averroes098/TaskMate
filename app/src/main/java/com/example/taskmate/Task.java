package com.example.taskmate;

public class Task {
    private int id;
    private String title;
    private String date;
    private String description;
    private boolean isCompleted; // Tambahkan field ini

    public Task(int id, String title, String date, String description) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.description = description;
        this.isCompleted = false; // Default false
    }

    // Constructor dengan status completed
    public Task(int id, String title, String date, String description, boolean isCompleted) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.description = description;
        this.isCompleted = isCompleted;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return isCompleted; } // Getter baru

    public void setTitle(String title) { this.title = title; }
    public void setDate(String date) { this.date = date; }
    public void setDescription(String description) { this.description = description; }
    public void setCompleted(boolean completed) { isCompleted = completed; } // Setter baru
}