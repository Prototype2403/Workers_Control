package com.example.workerscontrol;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class Launch extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyTheme(this);
        AppSettings.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        boolean log = false;
        Intent next = log ? new Intent(this, MainActivity.class) : new Intent(this, autorization.class);
        startActivity(next);
        finish();

    }
}
