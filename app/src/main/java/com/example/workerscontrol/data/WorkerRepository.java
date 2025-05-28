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

    public void close() {
        dbHelper.close();
    }
}
