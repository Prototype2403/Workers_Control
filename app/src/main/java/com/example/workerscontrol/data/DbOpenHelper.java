package com.example.workerscontrol.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DbOpenHelper extends SQLiteOpenHelper {
    public DbOpenHelper(Context context) {
        super(context, WokerDbContract.DATABASE_NAME, null, WokerDbContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_WORKER_TABLE = "CREATE TABLE " + WokerDbContract.Worker.TABLE_NAME +"("
                + WokerDbContract.Worker.COLUMN_WORKER_ID + " INTEGER PRIMARY KEY,"
                + WokerDbContract.Worker.COLUMN_FIO + " TEXT,"
                + WokerDbContract.Worker.COLUMN_POST + " TEXT,"
                + WokerDbContract.Worker.COLUMN_MONDAY + " INTEGER,"
                + WokerDbContract.Worker.COLUMN_TUESDAY + " INTEGER,"
                + WokerDbContract.Worker.COLUMN_WEDNESDAY + " INTEGER,"
                + WokerDbContract.Worker.COLUMN_THURSDAY + " INTEGER,"
                + WokerDbContract.Worker.COLUMN_FRIDAY + " INTEGER,"
                + WokerDbContract.Worker.COLUMN_SATURDAY + " INTEGER,"
                + WokerDbContract.Worker.COLUMN_SUNDAY + " INTEGER,"
                + WokerDbContract.Worker.COLUMN_TIME_FROM + " TEXT,"
                + WokerDbContract.Worker.COLUMN_TIME_TO + " TEXT)";
        db.execSQL(CREATE_WORKER_TABLE);
        String CREATE_EVENT_TABLE = "CREATE TABLE " + WokerDbContract.Events.TABLE_NAME +"("
                + WokerDbContract.Events.COLUM_EVENT_ID + " INTEGER PRIMARY KEY,"
                + WokerDbContract.Events.COLUMN_WORKER_ID + " INTEGER,"
                + WokerDbContract.Events.COLUMN_DATE + " TEXT,"
                + WokerDbContract.Events.COLUMN_TIME + " TEXT,"
                + WokerDbContract.Events.COLUMN_TYPE + " INTEGER)";
        db.execSQL(CREATE_EVENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + WokerDbContract.Worker.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WokerDbContract.Events.TABLE_NAME);
        onCreate(db);
    }
}
