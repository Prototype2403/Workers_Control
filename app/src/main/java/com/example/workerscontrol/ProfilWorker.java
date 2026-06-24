package com.example.workerscontrol;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.workerscontrol.data.EventRepository;
import com.example.workerscontrol.data.WokerDbContract;
import com.example.workerscontrol.data.WorkerRepository;
import com.example.workerscontrol.fragments.NowStatistic;
import com.example.workerscontrol.fragments.PeriodStatistic;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfilWorker extends AppCompatActivity {

    private long id;
    private ImageView avatarImageView;
    private TextView nameTextView;
    private TextView postTextView;
    private TextView workDaysTextView;
    private TextView workTimeTextView;
    private TextView workStatusTextView;
    private EventRepository eventsData;
    private boolean isWorking = false;
    private boolean dayCompleted = false;
    private Button nowButton;
    private Button weekButton;
    private Button monthButton;
    private Button yearButton;
    private Button bluetoothButton;
    private ListView dataLogListView;
    private String currentAvatarPath;

    private final ActivityResultLauncher<String> pickAvatarLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onAvatarPicked);

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyTheme(this);
        AppSettings.applyLanguage(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profil_worker);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id = extras.getLong("id");
        } else {
            Toast.makeText(this, "Сотрудник не найден", Toast.LENGTH_LONG).show();
        }

        avatarImageView = findViewById(R.id.worker_avatar_imageView);
        nameTextView = findViewById(R.id.name_title_textView);
        postTextView = findViewById(R.id.post_textView);
        workDaysTextView = findViewById(R.id.work_days_textView);
        workTimeTextView = findViewById(R.id.work_time_textView);
        workStatusTextView = findViewById(R.id.textView_workStatus);
        nowButton = findViewById(R.id.now_button);
        weekButton = findViewById(R.id.week_button);
        monthButton = findViewById(R.id.mouth_button);
        yearButton = findViewById(R.id.year_button);
        bluetoothButton = findViewById(R.id.bluetooth_button);
        dataLogListView = findViewById(R.id.dataLog_listView);

        showWorkerData();

        eventsData = new EventRepository(this);
        updateWorkerStatus();

        showNowStatistic();
        updateLogOfEvents();

        nowButton.setOnClickListener(v -> showNowStatistic());
        weekButton.setOnClickListener(v -> showWeekStatistic());
        monthButton.setOnClickListener(v -> showMonthStatistic());
        yearButton.setOnClickListener(v -> showYearStatistic());
        bluetoothButton.setOnClickListener(v -> showBluetoothActionDialog());
        avatarImageView.setOnClickListener(v -> showAvatarOptionsDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (eventsData == null) {
            eventsData = new EventRepository(this);
        }
        updateWorkerStatus();
        updateLogOfEvents();
        loadAvatar();
    }

    private void updateWorkerStatus() {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Cursor events = eventsData.getEventsByWorkerAndDate(id, currentDate);

        isWorking = false;
        dayCompleted = false;
        boolean hasArrival = false;
        boolean hasDeparture = false;

        if (events != null && events.moveToFirst()) {
            do {
                int eventType = events.getInt(events.getColumnIndex(WokerDbContract.Events.COLUMN_TYPE));
                if (eventType == WokerDbContract.Events.EVENT_TO_WORK) {
                    hasArrival = true;
                    isWorking = true;
                } else if (eventType == WokerDbContract.Events.EVENT_FROM_WORK) {
                    hasDeparture = true;
                    isWorking = false;
                }
            } while (events.moveToNext());
        }

        dayCompleted = hasArrival && hasDeparture;
        if (dayCompleted) {
            workStatusTextView.setText("Рабочий день завершен");
        } else if (isWorking) {
            workStatusTextView.setText("На работе");
        } else {
            workStatusTextView.setText("Не на работе");
        }

        bluetoothButton.setEnabled(true);
        bluetoothButton.setAlpha(1f);

        if (events != null) {
            events.close();
        }
    }

    private void showWorkerData() {
        WorkerRepository repository = new WorkerRepository(this);
        Cursor data = repository.getWorkerById(id);
        if (data != null && data.moveToFirst()) {
            String name = data.getString(data.getColumnIndex(WokerDbContract.Worker.COLUMN_FIO));
            String post = data.getString(data.getColumnIndex(WokerDbContract.Worker.COLUMN_POST));
            currentAvatarPath = data.getString(data.getColumnIndex(WokerDbContract.Worker.COLUMN_AVATAR_PATH));

            String[] dayLabels = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
            StringBuilder workDays = new StringBuilder();
            for (int i = 0; i < dayLabels.length; i++) {
                int works = data.getInt(i + 3);
                if (works == 1) {
                    workDays.append(dayLabels[i]).append(" ");
                }
            }

            String workTime = "Рабочее время с "
                    + data.getString(data.getColumnIndex(WokerDbContract.Worker.COLUMN_TIME_FROM))
                    + " до "
                    + data.getString(data.getColumnIndex(WokerDbContract.Worker.COLUMN_TIME_TO));

            nameTextView.setText(name);
            postTextView.setText(post);
            workDaysTextView.setText(workDays.toString().trim());
            workTimeTextView.setText(workTime);
            loadAvatar();
        }
        if (data != null) {
            data.close();
        }
        repository.close();
    }

    private void showNowStatistic() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.statistic_frameLayout, NowStatistic.newInstance(id));
        transaction.addToBackStack(null);
        transaction.commit();
        updateButtonStyles(nowButton);
    }

    private void showWeekStatistic() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.statistic_frameLayout, PeriodStatistic.newInstance(id, PeriodStatistic.PERIOD_WEEK));
        transaction.addToBackStack(null);
        transaction.commit();
        updateButtonStyles(weekButton);
    }

    private void showMonthStatistic() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.statistic_frameLayout, PeriodStatistic.newInstance(id, PeriodStatistic.PERIOD_MONTH));
        transaction.addToBackStack(null);
        transaction.commit();
        updateButtonStyles(monthButton);
    }

    private void showYearStatistic() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.statistic_frameLayout, PeriodStatistic.newInstance(id, PeriodStatistic.PERIOD_YEAR));
        transaction.addToBackStack(null);
        transaction.commit();
        updateButtonStyles(yearButton);
    }

    private void updateButtonStyles(Button selectedButton) {
        nowButton.setAlpha(0.5f);
        weekButton.setAlpha(0.5f);
        monthButton.setAlpha(0.5f);
        yearButton.setAlpha(0.5f);
        selectedButton.setAlpha(1.0f);
    }

    private void updateLogOfEvents() {
        EventRepository eventRepository = new EventRepository(this);
        Cursor log = eventRepository.getAttendanceTableByWorker(id);
        EventCursorAdapter cursorAdapter = new EventCursorAdapter(this, log, false, false, null);
        dataLogListView.setAdapter(cursorAdapter);
    }

    private void showBluetoothActionDialog() {
        CharSequence[] actions = {"Проход", "Отметка"};
        new AlertDialog.Builder(this)
                .setTitle("Выберите действие")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        openBluetoothSearch(BluetoothSearchActivity.MODE_PASS);
                    } else {
                        confirmAttendanceMark();
                    }
                })
                .show();
    }

    private void confirmAttendanceMark() {
        new AlertDialog.Builder(this)
                .setTitle("Отметка рабочего времени")
                .setMessage("Вы уверены?")
                .setPositiveButton("Да", (dialog, which) -> openBluetoothSearch(BluetoothSearchActivity.MODE_ATTENDANCE))
                .setNegativeButton("Нет", null)
                .show();
    }

    private void openBluetoothSearch(String mode) {
        Intent intent = new Intent(ProfilWorker.this, BluetoothSearchActivity.class);
        intent.putExtra("worker_id", id);
        intent.putExtra(BluetoothSearchActivity.EXTRA_MODE, mode);
        startActivity(intent);
    }

    private void showAvatarOptionsDialog() {
        boolean hasAvatar = currentAvatarPath != null && !currentAvatarPath.trim().isEmpty()
                && new File(currentAvatarPath).exists();
        if (!hasAvatar) {
            pickAvatarLauncher.launch("image/*");
            return;
        }

        CharSequence[] options = {"Открыть аватарку", "Загрузить новую"};
        new AlertDialog.Builder(this)
                .setTitle("Аватарка")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openAvatarPreview();
                    } else {
                        pickAvatarLauncher.launch("image/*");
                    }
                })
                .show();
    }

    private void openAvatarPreview() {
        if (currentAvatarPath == null || currentAvatarPath.trim().isEmpty()) {
            Toast.makeText(this, "Аватарка не установлена", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(currentAvatarPath);
        if (!file.exists()) {
            Toast.makeText(this, "Файл аватарки не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        ImageView preview = new ImageView(this);
        int padding = Math.round(getResources().getDisplayMetrics().density * 16);
        preview.setPadding(padding, padding, padding, padding);
        AvatarUtils.loadAvatar(preview, currentAvatarPath);

        new AlertDialog.Builder(this)
                .setTitle("Аватарка")
                .setView(preview)
                .setPositiveButton("Закрыть", null)
                .show();
    }

    private void onAvatarPicked(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            String avatarPath = AvatarUtils.saveRoundedAvatar(this, uri, id);
            WorkerRepository repository = new WorkerRepository(this);
            ContentValues values = new ContentValues();
            values.put(WokerDbContract.Worker.COLUMN_AVATAR_PATH, avatarPath);
            repository.updateWorker(id, values);
            repository.close();

            currentAvatarPath = avatarPath;
            loadAvatar();
            Toast.makeText(this, "Аватарка обновлена", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Не удалось загрузить аватарку", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAvatar() {
        AvatarUtils.loadAvatar(avatarImageView, currentAvatarPath);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profil_worker_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_worker_settings) {
            Intent intent = new Intent(this, WorkerSettingsActivity.class);
            intent.putExtra("worker_id", id);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.menu_logout) {
            confirmLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Выход")
                .setMessage("Выйти на экран авторизации?")
                .setPositiveButton("Да", (dialog, which) -> {
                    Intent intent = new Intent(this, autorization.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
