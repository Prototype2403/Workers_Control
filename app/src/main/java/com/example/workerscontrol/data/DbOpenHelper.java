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
                + WokerDbContract.Worker.COLUMN_TIME_TO + " TEXT,"
                + WokerDbContract.Worker.COLUMN_AVATAR_PATH + " TEXT,"
                + WokerDbContract.Worker.COLUMN_LOGIN + " TEXT,"
                + WokerDbContract.Worker.COLUMN_PASSWORD + " TEXT)";
        db.execSQL(CREATE_WORKER_TABLE);
        String CREATE_EVENT_TABLE = "CREATE TABLE " + WokerDbContract.Events.TABLE_NAME +"("
                + WokerDbContract.Events.COLUM_EVENT_ID + " INTEGER PRIMARY KEY,"
                + WokerDbContract.Events.COLUMN_WORKER_ID + " INTEGER,"
                + WokerDbContract.Events.COLUMN_DATE + " TEXT,"
                + WokerDbContract.Events.COLUMN_TIME + " TEXT,"
                + WokerDbContract.Events.COLUMN_TYPE + " INTEGER)";
        db.execSQL(CREATE_EVENT_TABLE);
        createAccessLogTable(db);
        createAccessTokenTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + WokerDbContract.Worker.TABLE_NAME
                    + " ADD COLUMN " + WokerDbContract.Worker.COLUMN_AVATAR_PATH + " TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + WokerDbContract.Worker.TABLE_NAME
                    + " ADD COLUMN " + WokerDbContract.Worker.COLUMN_LOGIN + " TEXT");
            db.execSQL("ALTER TABLE " + WokerDbContract.Worker.TABLE_NAME
                    + " ADD COLUMN " + WokerDbContract.Worker.COLUMN_PASSWORD + " TEXT");
            createAccessLogTable(db);
            createAccessTokenTable(db);
        }
    }

    private void createAccessLogTable(SQLiteDatabase db) {
        String createAccessLogTable = "CREATE TABLE IF NOT EXISTS " + WokerDbContract.AccessLog.TABLE_NAME + "("
                + WokerDbContract.AccessLog.COLUMN_ACCESS_ID + " INTEGER PRIMARY KEY,"
                + WokerDbContract.AccessLog.COLUMN_WORKER_ID + " INTEGER,"
                + WokerDbContract.AccessLog.COLUMN_DATE + " TEXT,"
                + WokerDbContract.AccessLog.COLUMN_TIME + " TEXT,"
                + WokerDbContract.AccessLog.COLUMN_PLACE + " TEXT)";
        db.execSQL(createAccessLogTable);
    }

    private void createAccessTokenTable(SQLiteDatabase db) {
        String createAccessTokenTable = "CREATE TABLE IF NOT EXISTS " + WokerDbContract.AccessToken.TABLE_NAME + "("
                + WokerDbContract.AccessToken.COLUMN_TOKEN_ID + " INTEGER PRIMARY KEY,"
                + WokerDbContract.AccessToken.COLUMN_WORKER_ID + " INTEGER,"
                + WokerDbContract.AccessToken.COLUMN_NAME + " TEXT,"
                + WokerDbContract.AccessToken.COLUMN_ISSUE_DATE + " TEXT,"
                + WokerDbContract.AccessToken.COLUMN_EXPIRY_DATE + " TEXT)";
        db.execSQL(createAccessTokenTable);
    }
}
