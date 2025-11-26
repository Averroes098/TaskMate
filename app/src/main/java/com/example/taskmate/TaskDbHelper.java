package com.example.taskmate;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TaskDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "TaskDbHelper";
    private static final String DATABASE_NAME = "taskmate.db";
    private static final int DATABASE_VERSION = 3; // Tingkatkan version ke 3

    // Definisikan SEMUA constant TERLEBIH DAHULU
    public static final String TABLE_TASKS = "tasks";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_COMPLETED = "completed"; // Tambahkan ini

    // Baru kemudian CREATE_TABLE_TASKS yang menggunakan constant di atas
    public static final String CREATE_TABLE_TASKS =
            "CREATE TABLE " + TABLE_TASKS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_TITLE + " TEXT," +
                    COLUMN_DATE + " TEXT," +
                    COLUMN_DESCRIPTION + " TEXT," +
                    COLUMN_COMPLETED + " INTEGER DEFAULT 0" + // 0 = false, 1 = true
                    ")";

    public TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "TaskDbHelper created with version: " + DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating table: " + CREATE_TABLE_TASKS);
        db.execSQL(CREATE_TABLE_TASKS);
        Log.d(TAG, "Table 'tasks' created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
        Log.d(TAG, "Database upgraded successfully");
    }
}