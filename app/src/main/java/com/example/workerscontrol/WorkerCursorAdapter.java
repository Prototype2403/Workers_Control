package com.example.workerscontrol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.workerscontrol.data.WokerDbContract;

public class WorkerCursorAdapter extends CursorAdapter {
    public WorkerCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.worker_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.name_worker_layout);
        TextView postTextView = view.findViewById(R.id.post_layout);

        String name = cursor.getString(cursor.getColumnIndex(WokerDbContract.Worker.COLUMN_FIO));
        String post = cursor.getString(cursor.getColumnIndex(WokerDbContract.Worker.COLUMN_POST));

        nameTextView.setText(name);
        postTextView.setText(post);

    }
}
