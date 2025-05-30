package com.example.workerscontrol;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.workerscontrol.data.WokerDbContract;

public class EventCursorAdapter extends CursorAdapter {

    public EventCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.event_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView dataEvent_textView = view.findViewById(R.id.dateEvent_textView);
        TextView timeEvent_textView = view.findViewById(R.id.timeEvent_textView);
        TextView nameEvent_textView = view.findViewById(R.id.nameEvent_textView);

        String date = cursor.getString(cursor.getColumnIndex(WokerDbContract.Events.COLUMN_DATE));
        String time = cursor.getString(cursor.getColumnIndex(WokerDbContract.Events.COLUMN_TIME));
        String type;
        if( cursor.getInt(cursor.getColumnIndex(WokerDbContract.Events.COLUMN_TYPE)) == WokerDbContract.Events.EVENT_TO_WORK){
            type = "Пришел";
        }else {
            type = "Ушел";
        }
        dataEvent_textView.setText(date);
        timeEvent_textView.setText(time);
        nameEvent_textView.setText(type);
    }
}
