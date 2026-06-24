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

    public Cursor getAttendanceTableByWorker(long workerId) {
        String[] selectionArgs = { String.valueOf(workerId) };
        String sql = "SELECT " +
                "MIN(" + Events.COLUM_EVENT_ID + ") AS _id, " +
                Events.COLUMN_DATE + ", " +
                "MIN(CASE WHEN " + Events.COLUMN_TYPE + " = " + Events.EVENT_TO_WORK + " THEN " + Events.COLUM_EVENT_ID + " END) AS arrival_event_id, " +
                "MIN(CASE WHEN " + Events.COLUMN_TYPE + " = " + Events.EVENT_TO_WORK + " THEN " + Events.COLUMN_TIME + " END) AS arrival_time, " +
                "MAX(CASE WHEN " + Events.COLUMN_TYPE + " = " + Events.EVENT_FROM_WORK + " THEN " + Events.COLUM_EVENT_ID + " END) AS departure_event_id, " +
                "MAX(CASE WHEN " + Events.COLUMN_TYPE + " = " + Events.EVENT_FROM_WORK + " THEN " + Events.COLUMN_TIME + " END) AS departure_time " +
                "FROM " + Events.TABLE_NAME + " " +
                "WHERE " + Events.COLUMN_WORKER_ID + " = ? " +
                "GROUP BY " + Events.COLUMN_DATE + " " +
                "ORDER BY ABS(julianday(" + Events.COLUMN_DATE + ") - julianday('now')) ASC, " + Events.COLUMN_DATE + " DESC";
        return db.rawQuery(sql, selectionArgs);
    }

    public Cursor getAttendanceTableByWorkerAndDateRange(long workerId, String startDate, String endDate) {
        String[] selectionArgs = { String.valueOf(workerId), startDate, endDate };
        String sql = "SELECT " +
                "MIN(" + Events.COLUM_EVENT_ID + ") AS _id, " +
                Events.COLUMN_DATE + ", " +
                "MIN(CASE WHEN " + Events.COLUMN_TYPE + " = " + Events.EVENT_TO_WORK + " THEN " + Events.COLUM_EVENT_ID + " END) AS arrival_event_id, " +
                "MIN(CASE WHEN " + Events.COLUMN_TYPE + " = " + Events.EVENT_TO_WORK + " THEN " + Events.COLUMN_TIME + " END) AS arrival_time, " +
                "MAX(CASE WHEN " + Events.COLUMN_TYPE + " = " + Events.EVENT_FROM_WORK + " THEN " + Events.COLUM_EVENT_ID + " END) AS departure_event_id, " +
                "MAX(CASE WHEN " + Events.COLUMN_TYPE + " = " + Events.EVENT_FROM_WORK + " THEN " + Events.COLUMN_TIME + " END) AS departure_time " +
                "FROM " + Events.TABLE_NAME + " " +
                "WHERE " + Events.COLUMN_WORKER_ID + " = ? " +
                "AND " + Events.COLUMN_DATE + " BETWEEN ? AND ? " +
                "GROUP BY " + Events.COLUMN_DATE + " " +
                "ORDER BY " + Events.COLUMN_DATE + " ASC";
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

    public long addAccessLog(long workerId, String date, String time, String place) {
        ContentValues values = new ContentValues();
        values.put(WokerDbContract.AccessLog.COLUMN_WORKER_ID, workerId);
        values.put(WokerDbContract.AccessLog.COLUMN_DATE, date);
        values.put(WokerDbContract.AccessLog.COLUMN_TIME, time);
        values.put(WokerDbContract.AccessLog.COLUMN_PLACE, place);
        return db.insert(WokerDbContract.AccessLog.TABLE_NAME, null, values);
    }

    public Cursor getAuditLog(String date, long workerId) {
        String dateFilter = date == null || date.trim().isEmpty() ? null : date.trim();
        boolean filterWorker = workerId > 0;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT rowid AS _id, worker_id, worker_name, date, time, action, place FROM (");
        sql.append("SELECT e.").append(Events.COLUM_EVENT_ID).append(" AS rowid, ")
                .append("e.").append(Events.COLUMN_WORKER_ID).append(" AS worker_id, ")
                .append("w.").append(WokerDbContract.Worker.COLUMN_FIO).append(" AS worker_name, ")
                .append("e.").append(Events.COLUMN_DATE).append(" AS date, ")
                .append("e.").append(Events.COLUMN_TIME).append(" AS time, ")
                .append("CASE WHEN e.").append(Events.COLUMN_TYPE).append(" = ").append(Events.EVENT_TO_WORK)
                .append(" THEN 'Начало рабочего дня' ELSE 'Окончание рабочего дня' END AS action, ")
                .append("'Отметка времени' AS place ")
                .append("FROM ").append(Events.TABLE_NAME).append(" e ")
                .append("LEFT JOIN ").append(WokerDbContract.Worker.TABLE_NAME).append(" w ON w.")
                .append(WokerDbContract.Worker.COLUMN_WORKER_ID).append(" = e.").append(Events.COLUMN_WORKER_ID)
                .append(" UNION ALL ");
        sql.append("SELECT (a.").append(WokerDbContract.AccessLog.COLUMN_ACCESS_ID).append(" + 1000000) AS rowid, ")
                .append("a.").append(WokerDbContract.AccessLog.COLUMN_WORKER_ID).append(" AS worker_id, ")
                .append("w.").append(WokerDbContract.Worker.COLUMN_FIO).append(" AS worker_name, ")
                .append("a.").append(WokerDbContract.AccessLog.COLUMN_DATE).append(" AS date, ")
                .append("a.").append(WokerDbContract.AccessLog.COLUMN_TIME).append(" AS time, ")
                .append("'Проход без отметки' AS action, ")
                .append("a.").append(WokerDbContract.AccessLog.COLUMN_PLACE).append(" AS place ")
                .append("FROM ").append(WokerDbContract.AccessLog.TABLE_NAME).append(" a ")
                .append("LEFT JOIN ").append(WokerDbContract.Worker.TABLE_NAME).append(" w ON w.")
                .append(WokerDbContract.Worker.COLUMN_WORKER_ID).append(" = a.")
                .append(WokerDbContract.AccessLog.COLUMN_WORKER_ID)
                .append(") audit WHERE 1=1 ");

        java.util.ArrayList<String> args = new java.util.ArrayList<>();
        if (dateFilter != null) {
            sql.append("AND date = ? ");
            args.add(dateFilter);
        }
        if (filterWorker) {
            sql.append("AND worker_id = ? ");
            args.add(String.valueOf(workerId));
        }
        sql.append("ORDER BY date DESC, time DESC");

        return db.rawQuery(sql.toString(), args.toArray(new String[0]));
    }

}
