package com.example.workerscontrol;
import java.util.Calendar;
import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.workerscontrol.data.EventRepository;
import com.example.workerscontrol.data.WorkerRepository;
import com.example.workerscontrol.data.WokerDbContract;
import com.example.workerscontrol.fragments.NowStatistic;
import com.example.workerscontrol.fragments.PeriodStatistic;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.app.AlertDialog;
import android.content.Intent;

public class ViewEmployeeActivity extends AppCompatActivity {

    long id;
    ImageView avatar_imageView;
    TextView name_textView;
    TextView post_textView;
    TextView work_days_textView;
    TextView work_time_textView;
    TextView workStatus_textView;
    EditText check_editText;
    Button addEvent_button;
    EventRepository eventsData;
    boolean isWorking = false;
    boolean dayCompleted = false;
    Button now_button;
    Button week_button;
    Button mouth_button;
    Button year_button;
    ListView dataLog_listView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_employee);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            id = extras.getLong("id");
        } else{
            Toast.makeText(this, "Работник не найден", Toast.LENGTH_LONG).show();
        }

        avatar_imageView = findViewById(R.id.worker_avatar_imageView);
        name_textView = findViewById(R.id.name_title_textView);
        post_textView = findViewById(R.id.post_textView);
        work_days_textView = findViewById(R.id.work_days_textView);
        work_time_textView = findViewById(R.id.work_time_textView);
        check_editText = findViewById(R.id.timeCheck_editText);
        addEvent_button = findViewById(R.id.addEvent_button);
        workStatus_textView = findViewById(R.id.textView_workStatus);
        now_button = findViewById(R.id.now_button);
        week_button = findViewById(R.id.week_button);
        mouth_button = findViewById(R.id.mouth_button);
        year_button = findViewById(R.id.year_button);
        dataLog_listView = findViewById(R.id.dataLog_listView);

        showWorkerData();

        Calendar calendar = Calendar.getInstance();
        check_editText.setText(calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));

        check_editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(ViewEmployeeActivity.this, (timePicker, hourOfDay, minutes) -> {
                    check_editText.setText(String.format("%02d:%02d", hourOfDay, minutes));
                }, 0, 0, true);
                timePickerDialog.show();
            }
        });

        eventsData = new EventRepository(this);
        updateWorkerStatus();
        
        addEvent_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dayCompleted) {
                    Toast.makeText(ViewEmployeeActivity.this, "Рабочий день уже завершен", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String currentTime = check_editText.getText().toString();
                
                if (!isWorking) {
                    eventsData.addEvent(id, currentDate, currentTime, WokerDbContract.Events.EVENT_TO_WORK);
                } else {
                    eventsData.addEvent(id, currentDate, currentTime, WokerDbContract.Events.EVENT_FROM_WORK);
                }
                
                updateWorkerStatus();
            }
        });

        showNowStatistic();
        updateLogOfEvents();

        dataLog_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long eventId) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    String date = cursor.getString(cursor.getColumnIndex(WokerDbContract.Events.COLUMN_DATE));
                    String currentTime = cursor.getString(cursor.getColumnIndex(WokerDbContract.Events.COLUMN_TIME));
                    int type = cursor.getInt(cursor.getColumnIndex(WokerDbContract.Events.COLUMN_TYPE));
                    long workerId = cursor.getLong(cursor.getColumnIndex(WokerDbContract.Events.COLUMN_WORKER_ID));

                    showTimeEditDialog(eventId, workerId, date, currentTime, type);
                }
            }
        });

        now_button.setOnClickListener(v -> showNowStatistic());
        week_button.setOnClickListener(v -> showWeekStatistic());
        mouth_button.setOnClickListener(v -> showMonthStatistic());
        year_button.setOnClickListener(v -> showYearStatistic());
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
            workStatus_textView.setText("Рабочий день завершен");
            addEvent_button.setEnabled(false);
            addEvent_button.setText("День завершен");
        } else if (isWorking) {
            workStatus_textView.setText("Работает");
            addEvent_button.setText("Закончить рабочий день");
            addEvent_button.setEnabled(true);
        } else {
            workStatus_textView.setText("Не на работе");
            addEvent_button.setText("Начать рабочий день");
            addEvent_button.setEnabled(true);
        }
        
        if (events != null) {
            events.close();
        }
    }

    public void showWorkerData(){
        WorkerRepository repository = new WorkerRepository(this);
        Cursor data = repository.getWorkerById(id);
        if(data.moveToFirst()) {
            String name = data.getString(data.getColumnIndex(WokerDbContract.Worker.COLUMN_FIO));
            String post = data.getString(data.getColumnIndex(WokerDbContract.Worker.COLUMN_POST));

            String[] dayLabels = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
            String workDays = "";
            for (int i = 0; i < dayLabels.length; i++) {
                int works = data.getInt(i + 3);
                if (works == 1) {
                    workDays = workDays + dayLabels[i] + " ";
                }
            }

            String work_time = "Рабочее время с " + data.getString(data.getColumnIndex(WokerDbContract.Worker.COLUMN_TIME_FROM)) + " до "
                    + data.getString(data.getColumnIndex(WokerDbContract.Worker.COLUMN_TIME_TO));

            name_textView.setText(name);
            post_textView.setText(post);
            work_days_textView.setText(workDays.trim());
            work_time_textView.setText(work_time);
        }
    }

    private void showNowStatistic() {
        NowStatistic fragment = NowStatistic.newInstance(id);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.statistic_frameLayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        updateButtonStyles(now_button);
    }

    private void showWeekStatistic() {
        PeriodStatistic fragment = PeriodStatistic.newInstance(id, PeriodStatistic.PERIOD_WEEK);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.statistic_frameLayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        updateButtonStyles(week_button);
    }

    private void showMonthStatistic() {
        PeriodStatistic fragment = PeriodStatistic.newInstance(id, PeriodStatistic.PERIOD_MONTH);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.statistic_frameLayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        updateButtonStyles(mouth_button);
    }

    private void showYearStatistic() {
        PeriodStatistic fragment = PeriodStatistic.newInstance(id, PeriodStatistic.PERIOD_YEAR);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.statistic_frameLayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        updateButtonStyles(year_button);
    }

    private void updateButtonStyles(Button selectedButton) {
        now_button.setAlpha(0.5f);
        week_button.setAlpha(0.5f);
        mouth_button.setAlpha(0.5f);
        year_button.setAlpha(0.5f);

        selectedButton.setAlpha(1.0f);
    }

    public void updateLogOfEvents(){
        EventRepository eventRepository = new EventRepository(this);
        Cursor log = eventRepository.getEventsByWorker(id);
        EventCursorAdapter cursorAdapter = new EventCursorAdapter(this, log, false);
        dataLog_listView.setAdapter(cursorAdapter);
    }

    private void showTimeEditDialog(final long eventId, final long workerId, final String date, String currentTime, final int type) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_time_edit, null);
        final EditText timeEditText = dialogView.findViewById(R.id.dialog_time_editText);
        timeEditText.setText(currentTime);

        timeEditText.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                ViewEmployeeActivity.this,
                (timePicker, hourOfDay, minutes) -> {
                    timeEditText.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minutes));
                },
                Integer.parseInt(currentTime.split(":")[0]),
                Integer.parseInt(currentTime.split(":")[1]),
                true
            );
            timePickerDialog.show();
        });

        String eventTypeText = type == WokerDbContract.Events.EVENT_TO_WORK ? "прихода" : "ухода";

        new AlertDialog.Builder(this)
            .setTitle("Изменить время " + eventTypeText)
            .setView(dialogView)
            .setPositiveButton("Сохранить", (dialog, which) -> {
                String newTime = timeEditText.getText().toString();
                if (!newTime.equals(currentTime)) {
                    EventRepository eventRepository = new EventRepository(this);
                    eventRepository.updateEvent(eventId, workerId, date, newTime, type);
                    updateLogOfEvents();
                    updateWorkerStatus();
                }
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_employee_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.edit_worker){
            Intent intent = new Intent(this, AddForm.class);
            intent.putExtra("id", id);
            startActivity(intent);
        }else if(item.getItemId() == R.id.delete_worker){
            new AlertDialog.Builder(this)
                .setTitle("Удаление работника")
                .setMessage("Вы действительно хотите удалить этого работника? Все его события также будут удалены.")
                .setPositiveButton("Да", (dialog, which) -> {
                    WorkerRepository workerRepository = new WorkerRepository(this);
                    EventRepository eventRepository = new EventRepository(this);

                    Cursor events = eventRepository.getEventsByWorker(id);
                    if (events != null && events.getCount() > 0) {
                        while (events.moveToNext()) {
                            long eventId = events.getLong(events.getColumnIndexOrThrow("_id"));
                            eventRepository.deleteEvent(eventId);
                        }
                        events.close();
                    }

                    int result = workerRepository.deleteWorker(id);
                    if (result > 0) {
                        Toast.makeText(this, "Работник успешно удален", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Ошибка при удалении работника", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Нет", null)
                .show();
        }

        return super.onOptionsItemSelected(item);
    }
}