package com.example.workerscontrol;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.workerscontrol.data.WokerDbContract.Events;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EventCursorAdapter extends CursorAdapter {

    public interface OnTimeCellClickListener {
        void onTimeCellClick(long eventId, String date, String currentTime, int type);
    }

    private final boolean canEdit;
    private final OnTimeCellClickListener cellClickListener;

    public EventCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.canEdit = false;
        this.cellClickListener = null;
    }

    public EventCursorAdapter(Context context, Cursor c, boolean autoRequery, boolean canEdit, OnTimeCellClickListener listener) {
        super(context, c, autoRequery);
        this.canEdit = canEdit;
        this.cellClickListener = listener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.event_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView dateEventTextView = view.findViewById(R.id.dateEvent_textView);
        TextView arrivalEventTextView = view.findViewById(R.id.arrivalEvent_textView);
        TextView departureEventTextView = view.findViewById(R.id.departureEvent_textView);
        TextView workedHoursTextView = view.findViewById(R.id.workedHours_textView);

        String date = cursor.getString(cursor.getColumnIndex(Events.COLUMN_DATE));
        String arrivalTime = cursor.getString(cursor.getColumnIndex("arrival_time"));
        String departureTime = cursor.getString(cursor.getColumnIndex("departure_time"));
        long arrivalEventId = cursor.getLong(cursor.getColumnIndex("arrival_event_id"));
        long departureEventId = cursor.getLong(cursor.getColumnIndex("departure_event_id"));

        if (arrivalTime == null || arrivalTime.trim().isEmpty()) {
            arrivalTime = "--:--";
        }
        if (departureTime == null || departureTime.trim().isEmpty()) {
            departureTime = "--:--";
        }

        String workedHours = calculateWorkedHours(arrivalTime, departureTime);

        dateEventTextView.setText(formatDate(date));
        arrivalEventTextView.setText(arrivalTime);
        departureEventTextView.setText(departureTime);
        workedHoursTextView.setText(workedHours);

        bindEditableCell(arrivalEventTextView, canEdit && arrivalEventId > 0 && !arrivalTime.equals("--:--"),
                arrivalEventId, date, arrivalTime, Events.EVENT_TO_WORK);
        bindEditableCell(departureEventTextView, canEdit && departureEventId > 0 && !departureTime.equals("--:--"),
                departureEventId, date, departureTime, Events.EVENT_FROM_WORK);
    }

    private void bindEditableCell(TextView textView, boolean editable, long eventId, String date, String time, int type) {
        if (!editable) {
            textView.setOnClickListener(null);
            return;
        }
        textView.setOnClickListener(v -> {
            if (cellClickListener != null) {
                cellClickListener.onTimeCellClick(eventId, date, time, type);
            }
        });
    }

    private String calculateWorkedHours(String arrivalTime, String departureTime) {
        if ("--:--".equals(arrivalTime) || "--:--".equals(departureTime)) {
            return "--";
        }
        int arrivalMinutes = parseMinutes(arrivalTime);
        int departureMinutes = parseMinutes(departureTime);
        if (arrivalMinutes < 0 || departureMinutes < 0 || departureMinutes < arrivalMinutes) {
            return "--";
        }
        int diff = departureMinutes - arrivalMinutes;
        int hours = diff / 60;
        int minutes = diff % 60;
        return String.format("%d:%02d", hours, minutes);
    }

    private int parseMinutes(String time) {
        try {
            String[] parts = time.split(":");
            if (parts.length != 2) {
                return -1;
            }
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return hours * 60 + minutes;
        } catch (Exception e) {
            return -1;
        }
    }

    private String formatDate(String sourceDate) {
        if (sourceDate == null || sourceDate.trim().isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat source = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat target = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            return target.format(source.parse(sourceDate));
        } catch (ParseException e) {
            return sourceDate;
        }
    }
}
