package com.example.workerscontrol;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WorkerSettingsActivity extends AppCompatActivity {

    private static final String[] REMINDER_LABELS = {"За 2 часа", "За час", "За 30 минут", "За 5 минут", "Никогда"};
    private static final String[] REMINDER_VALUES = {"120", "60", "30", "5", "never"};

    private Spinner beforeStartSpinner;
    private Spinner beforeEndSpinner;
    private Switch darkThemeSwitch;
    private Switch lateAlarmSwitch;
    private Button supportButton;
    private Button aboutButton;
    private long workerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyTheme(this);
        AppSettings.applyLanguage(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_worker_settings);
        setTitle("Настройки");
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        workerId = getIntent().getLongExtra("worker_id", -1L);
        if (workerId <= 0) {
            Toast.makeText(this, "Сотрудник не выбран", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        beforeStartSpinner = findViewById(R.id.before_start_spinner);
        beforeEndSpinner = findViewById(R.id.before_end_spinner);
        darkThemeSwitch = findViewById(R.id.dark_theme_switch);
        lateAlarmSwitch = findViewById(R.id.late_alarm_switch);
        supportButton = findViewById(R.id.support_button);
        aboutButton = findViewById(R.id.about_button);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, REMINDER_LABELS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        beforeStartSpinner.setAdapter(adapter);
        beforeEndSpinner.setAdapter(adapter);

        beforeStartSpinner.setSelection(findReminderIndex(AppSettings.getWorkerStartReminder(this, workerId)));
        beforeEndSpinner.setSelection(findReminderIndex(AppSettings.getWorkerEndReminder(this, workerId)));
        darkThemeSwitch.setChecked(AppSettings.isDarkThemeEnabled(this));
        lateAlarmSwitch.setChecked(AppSettings.isWorkerAlarmOnLateEnabled(this, workerId));

        beforeStartSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(position ->
                AppSettings.setWorkerStartReminder(this, workerId, REMINDER_VALUES[position])));
        beforeEndSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(position ->
                AppSettings.setWorkerEndReminder(this, workerId, REMINDER_VALUES[position])));
        darkThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppSettings.setDarkThemeEnabled(this, isChecked);
            AppSettings.applyTheme(this);
            recreate();
        });
        lateAlarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                AppSettings.setWorkerAlarmOnLateEnabled(this, workerId, isChecked));

        supportButton.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://ya.ru"))));
        aboutButton.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("О приложении")
                        .setMessage("Какоето описание, придумать потом")
                        .setPositiveButton("Закрыть", null)
                        .show());
    }

    private int findReminderIndex(String value) {
        for (int i = 0; i < REMINDER_VALUES.length; i++) {
            if (REMINDER_VALUES[i].equals(value)) {
                return i;
            }
        }
        return REMINDER_VALUES.length - 1;
    }
}
