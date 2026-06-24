package com.example.workerscontrol;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AuditLogAdapter extends CursorAdapter {

    public AuditLogAdapter(Context context, Cursor cursor) {
        super(context, cursor, false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.audit_log_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView workerTextView = view.findViewById(R.id.auditWorker_textView);
        TextView dateTimeTextView = view.findViewById(R.id.auditDateTime_textView);
        TextView actionTextView = view.findViewById(R.id.auditAction_textView);
        TextView placeTextView = view.findViewById(R.id.auditPlace_textView);

        String worker = cursor.getString(cursor.getColumnIndexOrThrow("worker_name"));
        String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
        String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
        String action = cursor.getString(cursor.getColumnIndexOrThrow("action"));
        String place = cursor.getString(cursor.getColumnIndexOrThrow("place"));

        workerTextView.setText(worker == null || worker.trim().isEmpty() ? "Сотрудник удален" : worker);
        dateTimeTextView.setText(formatDate(date) + "  " + time);
        actionTextView.setText(action);
        placeTextView.setText(place == null || place.trim().isEmpty() ? "Не указано" : place);
    }

    private String formatDate(String source) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(source);
            if (date == null) {
                return source;
            }
            return new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date);
        } catch (Exception e) {
            return source;
        }
    }
}
