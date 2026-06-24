package com.example.workerscontrol;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminSettingsActivity extends AppCompatActivity {

    private Switch notificationsSwitch;
    private Switch darkThemeSwitch;
    private Spinner languageSpinner;
    private Button supportButton;
    private Button aboutButton;

    private static final String[] LANGUAGE_LABELS = {"Русский", "Белорусский", "Казахский", "English", "中文"};
    private static final String[] LANGUAGE_CODES = {"ru", "be", "kk", "en", "zh"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyTheme(this);
        AppSettings.applyLanguage(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_settings);
        setTitle("Настройки");
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        notificationsSwitch = findViewById(R.id.notifications_switch);
        darkThemeSwitch = findViewById(R.id.dark_theme_switch);
        languageSpinner = findViewById(R.id.language_spinner);
        supportButton = findViewById(R.id.support_button);
        aboutButton = findViewById(R.id.about_button);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, LANGUAGE_LABELS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        notificationsSwitch.setChecked(AppSettings.isAdminNotificationsEnabled(this));
        darkThemeSwitch.setChecked(AppSettings.isDarkThemeEnabled(this));
        languageSpinner.setSelection(findLanguageIndex(AppSettings.getLanguageCode(this)));

        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                AppSettings.setAdminNotificationsEnabled(this, isChecked));

        darkThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppSettings.setDarkThemeEnabled(this, isChecked);
            AppSettings.applyTheme(this);
            recreate();
        });

        languageSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            String selectedCode = LANGUAGE_CODES[position];
            if (!selectedCode.equals(AppSettings.getLanguageCode(this))) {
                AppSettings.setLanguageCode(this, selectedCode);
                AppSettings.applyLanguage(this);
                recreate();
            }
        }));

        supportButton.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://ya.ru"))));

        aboutButton.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("О приложении")
                        .setMessage("Какоето описание, придумать потом")
                        .setPositiveButton("Закрыть", null)
                        .show());
    }

    private int findLanguageIndex(String code) {
        for (int i = 0; i < LANGUAGE_CODES.length; i++) {
            if (LANGUAGE_CODES[i].equals(code)) {
                return i;
            }
        }
        return 0;
    }
}
