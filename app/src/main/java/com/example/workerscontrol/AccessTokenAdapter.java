package com.example.workerscontrol;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.workerscontrol.data.WokerDbContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AccessTokenAdapter extends CursorAdapter {

    public AccessTokenAdapter(Context context, Cursor cursor) {
        super(context, cursor, false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.access_token_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.tokenName_textView);
        TextView issueTextView = view.findViewById(R.id.tokenIssue_textView);
        TextView expiryTextView = view.findViewById(R.id.tokenExpiry_textView);

        nameTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow(WokerDbContract.AccessToken.COLUMN_NAME)));
        issueTextView.setText("Дата выдачи: "
                + formatDate(cursor.getString(cursor.getColumnIndexOrThrow(WokerDbContract.AccessToken.COLUMN_ISSUE_DATE))));
        expiryTextView.setText("Срок действия: "
                + formatDate(cursor.getString(cursor.getColumnIndexOrThrow(WokerDbContract.AccessToken.COLUMN_EXPIRY_DATE))));
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
