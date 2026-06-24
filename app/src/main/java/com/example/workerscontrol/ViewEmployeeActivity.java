package com.example.workerscontrol;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ViewEmployeeActivity extends AppCompatActivity {
    private static final int PERIOD_TODAY = 3;
    private static final String[] ACCESS_PLACES = {
            "Главный вход 1",
            "Главный вход 2",
            "Склад 1",
            "Офис 2 этаж"
    };

    private long id;
    private TextView nameTextView, postTextView, workDaysTextView, workTimeTextView, workStatusTextView;
    private EditText checkEditText;
    private Button addEventButton, passWithoutMarkButton, exportExcelButton, nowButton, weekButton, monthButton, yearButton;
    private ListView dataLogListView;
    private EventRepository eventsData;
    private boolean isWorking, dayCompleted;
    private int selectedPeriod = PERIOD_TODAY;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyTheme(this);
        AppSettings.applyLanguage(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_employee);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        id = getIntent().getLongExtra("id", -1L);
        if (id <= 0) {
            Toast.makeText(this, "Сотрудник не найден", Toast.LENGTH_LONG).show();
        }

        ImageView avatar = findViewById(R.id.worker_avatar_imageView);
        nameTextView = findViewById(R.id.name_title_textView);
        postTextView = findViewById(R.id.post_textView);
        workDaysTextView = findViewById(R.id.work_days_textView);
        workTimeTextView = findViewById(R.id.work_time_textView);
        checkEditText = findViewById(R.id.timeCheck_editText);
        addEventButton = findViewById(R.id.addEvent_button);
        passWithoutMarkButton = findViewById(R.id.passWithoutMark_button);
        exportExcelButton = findViewById(R.id.exportExcel_button);
        workStatusTextView = findViewById(R.id.textView_workStatus);
        nowButton = findViewById(R.id.now_button);
        weekButton = findViewById(R.id.week_button);
        monthButton = findViewById(R.id.mouth_button);
        yearButton = findViewById(R.id.year_button);
        dataLogListView = findViewById(R.id.dataLog_listView);

        showWorkerData(avatar);
        Calendar c = Calendar.getInstance();
        checkEditText.setText(String.format(Locale.getDefault(), "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));
        checkEditText.setOnClickListener(v -> new TimePickerDialog(this, (tp, h, m) ->
                checkEditText.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m)), 0, 0, true).show());

        eventsData = new EventRepository(this);
        updateWorkerStatus();
        addEventButton.setOnClickListener(v -> addEvent());
        passWithoutMarkButton.setOnClickListener(v -> showPassWithoutMarkDialog());
        exportExcelButton.setOnClickListener(v -> exportCurrentPeriodToExcel());

        showNowStatistic();
        updateLogOfEvents();
        nowButton.setOnClickListener(v -> showNowStatistic());
        weekButton.setOnClickListener(v -> showPeriod(PeriodStatistic.PERIOD_WEEK, weekButton));
        monthButton.setOnClickListener(v -> showPeriod(PeriodStatistic.PERIOD_MONTH, monthButton));
        yearButton.setOnClickListener(v -> showPeriod(PeriodStatistic.PERIOD_YEAR, yearButton));
    }

    private void exportCurrentPeriodToExcel() {
        DateRange r = getSelectedDateRange();
        Cursor c = new EventRepository(this).getAttendanceTableByWorkerAndDateRange(id, r.startDate, r.endDate);
        File file;
        try {
            String workerName = nameTextView.getText() == null ? ("worker_" + id) : nameTextView.getText().toString();
            file = ExcelExportHelper.exportLegacyXls(this, id, workerName, r.periodTitle, r.fileNamePart, r.startDate, r.endDate, c, getPlannedTimes());
        } catch (Exception e) {
            Log.e("ViewEmployeeActivity", "Excel export failed", e);
            Toast.makeText(this, "Не удалось создать Excel-файл: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (c != null) c.close();
            return;
        }
        if (c != null) c.close();

        try {
            shareFile(file);
        } catch (Exception e) {
            Log.e("ViewEmployeeActivity", "Excel share failed", e);
            Toast.makeText(this, "Файл создан: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
    }

    private void shareFile(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("application/vnd.ms-excel");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.putExtra(Intent.EXTRA_SUBJECT, "Статистика сотрудника");
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(share, "Поделиться статистикой"));
    }

    private void addEvent() {
        if (dayCompleted) {
            Toast.makeText(this, "Рабочий день уже завершен", Toast.LENGTH_SHORT).show();
            return;
        }
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int type = isWorking ? WokerDbContract.Events.EVENT_FROM_WORK : WokerDbContract.Events.EVENT_TO_WORK;
        eventsData.addEvent(id, date, checkEditText.getText().toString(), type);
        updateWorkerStatus();
        updateLogOfEvents();
    }

    private void showPassWithoutMarkDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Куда пропустить")
                .setItems(ACCESS_PLACES, (dialog, which) -> createPassWithoutMark(ACCESS_PLACES[which]))
                .show();
    }

    private void createPassWithoutMark(String place) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Отправка данных...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            progressDialog.dismiss();
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            long result = new EventRepository(this).addAccessLog(id, date, time, place);
            Toast.makeText(this, result > 0 ? "Проход без отметки сохранен" : "Не удалось сохранить проход", Toast.LENGTH_SHORT).show();
        }, 1500);
    }

    private void showWorkerData(ImageView avatar) {
        WorkerRepository repo = new WorkerRepository(this);
        Cursor data = repo.getWorkerById(id);
        if (data != null && data.moveToFirst()) {
            nameTextView.setText(data.getString(data.getColumnIndexOrThrow(WokerDbContract.Worker.COLUMN_FIO)));
            postTextView.setText(data.getString(data.getColumnIndexOrThrow(WokerDbContract.Worker.COLUMN_POST)));
            String[] d = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
            StringBuilder days = new StringBuilder();
            for (int i = 0; i < d.length; i++) {
                if (data.getInt(i + 3) == 1) days.append(d[i]).append(" ");
            }
            workDaysTextView.setText(days.toString().trim());
            String from = data.getString(data.getColumnIndexOrThrow(WokerDbContract.Worker.COLUMN_TIME_FROM));
            String to = data.getString(data.getColumnIndexOrThrow(WokerDbContract.Worker.COLUMN_TIME_TO));
            workTimeTextView.setText("Рабочее время с " + from + " до " + to);
            AvatarUtils.loadAvatar(avatar, data.getString(data.getColumnIndexOrThrow(WokerDbContract.Worker.COLUMN_AVATAR_PATH)));
            data.close();
        }
        repo.close();
    }

    private String[] getPlannedTimes() {
        String start = "09:00", end = "18:00";
        WorkerRepository repo = new WorkerRepository(this);
        Cursor worker = repo.getWorkerById(id);
        if (worker != null && worker.moveToFirst()) {
            start = worker.getString(worker.getColumnIndexOrThrow(WokerDbContract.Worker.COLUMN_TIME_FROM));
            end = worker.getString(worker.getColumnIndexOrThrow(WokerDbContract.Worker.COLUMN_TIME_TO));
            worker.close();
        }
        repo.close();
        return new String[]{start, end};
    }

    private void updateWorkerStatus() {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Cursor events = eventsData.getEventsByWorkerAndDate(id, currentDate);
        isWorking = false;
        dayCompleted = false;
        boolean hasArrival = false, hasDeparture = false;
        if (events != null && events.moveToFirst()) {
            do {
                int t = events.getInt(events.getColumnIndexOrThrow(WokerDbContract.Events.COLUMN_TYPE));
                if (t == WokerDbContract.Events.EVENT_TO_WORK) { hasArrival = true; isWorking = true; }
                if (t == WokerDbContract.Events.EVENT_FROM_WORK) { hasDeparture = true; isWorking = false; }
            } while (events.moveToNext());
            events.close();
        }
        dayCompleted = hasArrival && hasDeparture;
        if (dayCompleted) {
            workStatusTextView.setText("Рабочий день завершен");
            addEventButton.setText("День завершен");
            addEventButton.setEnabled(false);
        } else if (isWorking) {
            workStatusTextView.setText("Работает");
            addEventButton.setText("Завершить рабочий день");
            addEventButton.setEnabled(true);
        } else {
            workStatusTextView.setText("Не на работе");
            addEventButton.setText("Начать рабочий день");
            addEventButton.setEnabled(true);
        }
    }

    private void showNowStatistic() {
        selectedPeriod = PERIOD_TODAY;
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.statistic_frameLayout, NowStatistic.newInstance(id));
        t.commit();
        updateButtons(nowButton);
    }

    private void showPeriod(int p, Button b) {
        selectedPeriod = p;
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.statistic_frameLayout, PeriodStatistic.newInstance(id, p));
        t.commit();
        updateButtons(b);
    }

    private void updateButtons(Button b) {
        nowButton.setAlpha(0.5f);
        weekButton.setAlpha(0.5f);
        monthButton.setAlpha(0.5f);
        yearButton.setAlpha(0.5f);
        b.setAlpha(1f);
    }

    private void updateLogOfEvents() {
        Cursor log = new EventRepository(this).getAttendanceTableByWorker(id);
        dataLogListView.setAdapter(new EventCursorAdapter(this, log, false, true, (eid, d, time, type) -> showTimeEditDialog(eid, id, d, time, type)));
    }

    private void showTimeEditDialog(long eventId, long workerId, String date, String currentTime, int type) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_time_edit, null);
        EditText timeEdit = dialogView.findViewById(R.id.dialog_time_editText);
        timeEdit.setText(currentTime);
        timeEdit.setOnClickListener(v -> new TimePickerDialog(this, (tp, h, m) ->
                timeEdit.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m)),
                Integer.parseInt(currentTime.split(":")[0]), Integer.parseInt(currentTime.split(":")[1]), true).show());
        String label = type == WokerDbContract.Events.EVENT_TO_WORK ? "прихода" : "ухода";
        new AlertDialog.Builder(this).setTitle("Изменить время " + label).setView(dialogView)
                .setPositiveButton("Сохранить", (d, w) -> {
                    String nt = timeEdit.getText().toString();
                    if (!nt.equals(currentTime)) {
                        new EventRepository(this).updateEvent(eventId, workerId, date, nt, type);
                        updateLogOfEvents();
                        updateWorkerStatus();
                    }
                }).setNegativeButton("Отмена", null).show();
    }

    private DateRange getSelectedDateRange() {
        Calendar c = Calendar.getInstance();
        String s;
        String e;
        String t;
        String f;
        if (selectedPeriod == PeriodStatistic.PERIOD_WEEK) {
            t = "Неделя";
            f = "week";
            c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
            s = fmt(c);
            c.add(Calendar.DAY_OF_WEEK, 6);
            e = fmt(c);
        } else if (selectedPeriod == PeriodStatistic.PERIOD_MONTH) {
            t = "Месяц";
            f = "month";
            c.set(Calendar.DAY_OF_MONTH, 1);
            s = fmt(c);
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            e = fmt(c);
        } else if (selectedPeriod == PeriodStatistic.PERIOD_YEAR) {
            t = "Год";
            f = "year";
            c.set(Calendar.DAY_OF_YEAR, 1);
            s = fmt(c);
            c.set(Calendar.DAY_OF_YEAR, c.getActualMaximum(Calendar.DAY_OF_YEAR));
            e = fmt(c);
        } else {
            t = "Сегодня";
            f = "today";
            s = fmt(c);
            e = s;
        }
        return new DateRange(s, e, t, f);
    }

    private String fmt(Calendar c) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.getTime());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_employee_menu, menu);
        syncNotify(menu.findItem(R.id.toggle_worker_notifications));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.toggle_worker_notifications) {
            boolean cur = AppSettings.isWorkerNotifyIconEnabled(this, id);
            AppSettings.setWorkerNotifyIconEnabled(this, id, !cur);
            syncNotify(item);
            return true;
        }
        if (itemId == R.id.edit_worker) {
            Intent i = new Intent(this, AddForm.class);
            i.putExtra("id", id);
            startActivity(i);
            return true;
        }
        if (itemId == R.id.worker_security) {
            Intent i = new Intent(this, WorkerSecurityActivity.class);
            i.putExtra("worker_id", id);
            startActivity(i);
            return true;
        }
        if (itemId == R.id.delete_worker) {
            showDeleteDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void syncNotify(MenuItem item) {
        item.setIcon(AppSettings.isWorkerNotifyIconEnabled(this, id)
                ? R.drawable.ic_notify_on_custom
                : R.drawable.ic_notify_off_custom);
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this).setTitle("Удаление сотрудника")
                .setMessage("Вы действительно хотите удалить этого сотрудника? Все его события также будут удалены.")
                .setPositiveButton("Да", (d, w) -> {
                    WorkerRepository wr = new WorkerRepository(this);
                    EventRepository er = new EventRepository(this);
                    Cursor ev = er.getEventsByWorker(id);
                    if (ev != null && ev.getCount() > 0) {
                        while (ev.moveToNext()) er.deleteEvent(ev.getLong(ev.getColumnIndexOrThrow("_id")));
                        ev.close();
                    }
                    int res = wr.deleteWorker(id);
                    Toast.makeText(this, res > 0 ? "Сотрудник удален" : "Ошибка при удалении сотрудника", Toast.LENGTH_SHORT).show();
                    if (res > 0) finish();
                }).setNegativeButton("Нет", null).show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private static class DateRange {
        final String startDate, endDate, periodTitle, fileNamePart;
        DateRange(String s, String e, String p, String f) {
            startDate = s;
            endDate = e;
            periodTitle = p;
            fileNamePart = f;
        }
    }
}
