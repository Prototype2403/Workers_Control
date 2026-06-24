package com.example.workerscontrol.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class WorkerRepository {
    private SQLiteDatabase db;
    private DbOpenHelper dbHelper;

    public WorkerRepository(Context context) {
        dbHelper = new DbOpenHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public long insertWorker(String fio, String post, String timeFrom, String timeTo,
                             int monday, int tuesday, int wednesday, int thursday,
                             int friday, int saturday, int sunday) {

        ContentValues values = new ContentValues();
        values.put(WokerDbContract.Worker.COLUMN_FIO, fio);
        values.put(WokerDbContract.Worker.COLUMN_POST, post);
        values.put(WokerDbContract.Worker.COLUMN_TIME_FROM, timeFrom);
        values.put(WokerDbContract.Worker.COLUMN_TIME_TO, timeTo);
        values.put(WokerDbContract.Worker.COLUMN_MONDAY, monday);
        values.put(WokerDbContract.Worker.COLUMN_TUESDAY, tuesday);
        values.put(WokerDbContract.Worker.COLUMN_WEDNESDAY, wednesday);
        values.put(WokerDbContract.Worker.COLUMN_THURSDAY, thursday);
        values.put(WokerDbContract.Worker.COLUMN_FRIDAY, friday);
        values.put(WokerDbContract.Worker.COLUMN_SATURDAY, saturday);
        values.put(WokerDbContract.Worker.COLUMN_SUNDAY, sunday);

        return db.insert(WokerDbContract.Worker.TABLE_NAME, null, values);
    }

    public int deleteWorker(long id) {
        return db.delete(WokerDbContract.Worker.TABLE_NAME,
                WokerDbContract.Worker.COLUMN_WORKER_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public int updateWorker(long id, ContentValues values) {
        return db.update(WokerDbContract.Worker.TABLE_NAME, values,
                WokerDbContract.Worker.COLUMN_WORKER_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public Cursor getAllWorkers() {
        return db.rawQuery("SELECT worker_id AS _id, * FROM " + WokerDbContract.Worker.TABLE_NAME, null);
    }

    public Cursor getWorkerById(long id) {
        return db.query(WokerDbContract.Worker.TABLE_NAME,
                null,
                WokerDbContract.Worker.COLUMN_WORKER_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);
    }

    public Cursor getWorkerByCredentials(String login, String password) {
        String sql = "SELECT " + WokerDbContract.Worker.COLUMN_WORKER_ID + " AS _id, * FROM "
                + WokerDbContract.Worker.TABLE_NAME
                + " WHERE " + WokerDbContract.Worker.COLUMN_LOGIN + " = ?"
                + " AND " + WokerDbContract.Worker.COLUMN_PASSWORD + " = ?"
                + " LIMIT 1";
        return db.rawQuery(sql, new String[]{login, password});
    }

    public int updateCredentials(long id, String login, String password) {
        ContentValues values = new ContentValues();
        values.put(WokerDbContract.Worker.COLUMN_LOGIN, login);
        values.put(WokerDbContract.Worker.COLUMN_PASSWORD, password);
        return updateWorker(id, values);
    }

    public long addAccessToken(long workerId, String name, String issueDate, String expiryDate) {
        ContentValues values = new ContentValues();
        values.put(WokerDbContract.AccessToken.COLUMN_WORKER_ID, workerId);
        values.put(WokerDbContract.AccessToken.COLUMN_NAME, name);
        values.put(WokerDbContract.AccessToken.COLUMN_ISSUE_DATE, issueDate);
        values.put(WokerDbContract.AccessToken.COLUMN_EXPIRY_DATE, expiryDate);
        return db.insert(WokerDbContract.AccessToken.TABLE_NAME, null, values);
    }

    public Cursor getAccessTokens(long workerId) {
        String sql = "SELECT " + WokerDbContract.AccessToken.COLUMN_TOKEN_ID + " AS _id, * FROM "
                + WokerDbContract.AccessToken.TABLE_NAME
                + " WHERE " + WokerDbContract.AccessToken.COLUMN_WORKER_ID + " = ?"
                + " ORDER BY " + WokerDbContract.AccessToken.COLUMN_ISSUE_DATE + " DESC";
        return db.rawQuery(sql, new String[]{String.valueOf(workerId)});
    }

    public int getAccessTokenCount(long workerId) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + WokerDbContract.AccessToken.TABLE_NAME
                + " WHERE " + WokerDbContract.AccessToken.COLUMN_WORKER_ID + " = ?",
                new String[]{String.valueOf(workerId)});
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int deleteAccessToken(long tokenId) {
        return db.delete(WokerDbContract.AccessToken.TABLE_NAME,
                WokerDbContract.AccessToken.COLUMN_TOKEN_ID + " = ?",
                new String[]{String.valueOf(tokenId)});
    }

    public void close() {
        dbHelper.close();
    }
}
