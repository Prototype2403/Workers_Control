package com.example.workerscontrol.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.workerscontrol.data.WokerDbContract.Events;
import com.example.workerscontrol.data.DbOpenHelper;
public class EventRepository {
    private final SQLiteDatabase db;
    private DbOpenHelper dbHelper;
    public EventRepository(Context context) {
        dbHelper = new DbOpenHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public long addEvent(long workerId, String date, String time, int type) {
        ContentValues values = new ContentValues();
        values.put(Events.COLUMN_WORKER_ID, workerId);
        values.put(Events.COLUMN_DATE, date);
        values.put(Events.COLUMN_TIME, time);
        values.put(Events.COLUMN_TYPE, type);
        return db.insert(Events.TABLE_NAME, null, values);
    }

    public int updateEvent(long eventId, long workerId, String date, String time, int type) {
        ContentValues values = new ContentValues();
        values.put(Events.COLUMN_WORKER_ID, workerId);
        values.put(Events.COLUMN_DATE, date);
        values.put(Events.COLUMN_TIME, time);
        values.put(Events.COLUMN_TYPE, type);

        String whereClause = Events.COLUM_EVENT_ID + " = ?";
        String[] whereArgs = { String.valueOf(eventId) };

        return db.update(Events.TABLE_NAME, values, whereClause, whereArgs);
    }

    public int deleteEvent(long eventId) {
        String whereClause = Events.COLUM_EVENT_ID + " = ?";
        String[] whereArgs = { String.valueOf(eventId) };
        return db.delete(Events.TABLE_NAME, whereClause, whereArgs);
    }

    public Cursor getEventsByDate(String date) {
        String selection = Events.COLUMN_DATE + " = ?";
        String[] selectionArgs = { date };
        return db.query(Events.TABLE_NAME, null, selection, selectionArgs, null, null, Events.COLUMN_TIME + " ASC");
    }

    public Cursor getEventsBetweenDates(String startDate, String endDate) {
        String selection = Events.COLUMN_DATE + " BETWEEN ? AND ?";
        String[] selectionArgs = { startDate, endDate };
        return db.query(Events.TABLE_NAME, null, selection, selectionArgs, null, null, Events.COLUMN_DATE + " ASC, " + Events.COLUMN_TIME + " ASC");
    }

    public Cursor getEventsByWorker(long workerId) {
        String selection = Events.COLUMN_WORKER_ID + " = ?";
        String[] selectionArgs = { String.valueOf(workerId) };

        String sql = "SELECT " +
                Events.COLUM_EVENT_ID + " AS _id, " +
                Events.COLUMN_WORKER_ID + ", " +
                Events.COLUMN_DATE + ", " +
                Events.COLUMN_TIME + ", " +
                Events.COLUMN_TYPE +
                " FROM " + Events.TABLE_NAME +
                " WHERE " + Events.COLUMN_WORKER_ID + " = ?" +
                " ORDER BY ABS(julianday(" + Events.COLUMN_DATE + ") - julianday('now')) ASC, " +
                Events.COLUMN_TIME + " ASC";


        return db.rawQuery(sql, selectionArgs);
    }


    public Cursor getEventsByWorkerAndDate(long workerId, String date) {
        String selection = Events.COLUMN_WORKER_ID + " = ? AND " + Events.COLUMN_DATE + " = ?";
        String[] selectionArgs = { String.valueOf(workerId), date };

        String sql = "SELECT " +
                Events.COLUM_EVENT_ID + " AS _id, " +
                Events.COLUMN_WORKER_ID + ", " +
                Events.COLUMN_DATE + ", " +
                Events.COLUMN_TIME + ", " +
                Events.COLUMN_TYPE +
                " FROM " + Events.TABLE_NAME +
                " WHERE " + selection +
                " ORDER BY " + Events.COLUMN_TIME + " ASC";

        return db.rawQuery(sql, selectionArgs);
    }


    public Cursor getEventsByWorkerAndDateRange(long workerId, String startDate, String endDate) {
        String selection = Events.COLUMN_WORKER_ID + " = ? AND " + Events.COLUMN_DATE + " BETWEEN ? AND ?";
        String[] selectionArgs = { String.valueOf(workerId), startDate, endDate };

        String sql = "SELECT " +
                Events.COLUM_EVENT_ID + " AS _id, " +
                Events.COLUMN_WORKER_ID + ", " +
                Events.COLUMN_DATE + ", " +
                Events.COLUMN_TIME + ", " +
                Events.COLUMN_TYPE +
                " FROM " + Events.TABLE_NAME +
                " WHERE " + selection +
                " ORDER BY " + Events.COLUMN_DATE + " ASC, " + Events.COLUMN_TIME + " ASC";

        return db.rawQuery(sql, selectionArgs);
    }

}
