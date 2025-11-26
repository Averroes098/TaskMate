package com.example.taskmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class TaskDatabase {

    private static final String TAG = "TaskDatabase";
    private TaskDbHelper dbHelper;

    public TaskDatabase(Context context) {
        dbHelper = new TaskDbHelper(context);
    }

    public ArrayList<Task> getTasks() {
        return getTasksWithSelection(null, null);
    }

    public ArrayList<Task> getUncompletedTasks() {
        return getTasksWithSelection(TaskDbHelper.COLUMN_COMPLETED + " = ?", new String[]{"0"});
    }

    public ArrayList<Task> getCompletedTasks() {
        return getTasksWithSelection(TaskDbHelper.COLUMN_COMPLETED + " = ?", new String[]{"1"});
    }

    private ArrayList<Task> getTasksWithSelection(String selection, String[] selectionArgs) {
        ArrayList<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TaskDbHelper.TABLE_TASKS, null, selection, selectionArgs, null, null, TaskDbHelper.COLUMN_DATE + " ASC");
            if (cursor.moveToFirst()) {
                do {
                    tasks.add(cursorToTask(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting tasks from database", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return tasks;
    }

    public void addTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = taskToContentValues(task);
            db.insert(TaskDbHelper.TABLE_TASKS, null, values);
        } finally {
            db.close();
        }
    }

    public void updateTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = taskToContentValues(task);
            db.update(TaskDbHelper.TABLE_TASKS, values, TaskDbHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(task.getId())});
        } finally {
            db.close();
        }
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.delete(TaskDbHelper.TABLE_TASKS, TaskDbHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        } finally {
            db.close();
        }
    }

    public void toggleTaskStatus(int taskId, boolean isCompleted) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(TaskDbHelper.COLUMN_COMPLETED, isCompleted ? 1 : 0);
            db.update(TaskDbHelper.TABLE_TASKS, values, TaskDbHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(taskId)});
        } finally {
            db.close();
        }
    }

    public Task getTaskById(int taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        Task task = null;
        try {
            cursor = db.query(TaskDbHelper.TABLE_TASKS, null, TaskDbHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(taskId)}, null, null, null);
            if (cursor.moveToFirst()) {
                task = cursorToTask(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return task;
    }

    private Task cursorToTask(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(TaskDbHelper.COLUMN_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(TaskDbHelper.COLUMN_TITLE));
        String date = cursor.getString(cursor.getColumnIndexOrThrow(TaskDbHelper.COLUMN_DATE));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(TaskDbHelper.COLUMN_DESCRIPTION));
        boolean isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(TaskDbHelper.COLUMN_COMPLETED)) == 1;
        return new Task(id, title, date, description, isCompleted);
    }
    
    private ContentValues taskToContentValues(Task task) {
        ContentValues values = new ContentValues();
        values.put(TaskDbHelper.COLUMN_ID, task.getId());
        values.put(TaskDbHelper.COLUMN_TITLE, task.getTitle());
        values.put(TaskDbHelper.COLUMN_DATE, task.getDate());
        values.put(TaskDbHelper.COLUMN_DESCRIPTION, task.getDescription());
        values.put(TaskDbHelper.COLUMN_COMPLETED, task.isCompleted() ? 1 : 0);
        return values;
    }
}