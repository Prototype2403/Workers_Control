package com.example.workerscontrol;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.workerscontrol.data.EventRepository;
import com.example.workerscontrol.data.WokerDbContract;
import com.example.workerscontrol.data.WorkerRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AuditLogActivity extends AppCompatActivity {

    private final ArrayList<Long> workerIds = new ArrayList<>();
    private EditText dateEditText;
    private Spinner workerSpinner;
    private ListView auditListView;
    private AuditLogAdapter adapter;
    private String selectedDateIso = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyTheme(this);
        AppSettings.applyLanguage(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_audit_log);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        dateEditText = findViewById(R.id.auditDate_editText);
        workerSpinner = findViewById(R.id.auditWorker_spinner);
        auditListView = findViewById(R.id.auditLog_listView);
        Button applyButton = findViewById(R.id.applyAuditFilter_button);
        Button clearButton = findViewById(R.id.clearAuditFilter_button);

        loadWorkers();
        dateEditText.setOnClickListener(v -> showDatePicker());
        applyButton.setOnClickListener(v -> loadAuditLog());
        clearButton.setOnClickListener(v -> {
            dateEditText.setText("");
            selectedDateIso = "";
            workerSpinner.setSelection(0);
            loadAuditLog();
        });
        loadAuditLog();
    }

    private void loadWorkers() {
        ArrayList<String> workerNames = new ArrayList<>();
        workerNames.add("Все сотрудники");
        workerIds.clear();
        workerIds.add(-1L);

        WorkerRepository repository = new WorkerRepository(this);
        Cursor cursor = repository.getAllWorkers();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                workerIds.add(cursor.getLong(cursor.getColumnIndexOrThrow("_id")));
                workerNames.add(cursor.getString(cursor.getColumnIndexOrThrow(WokerDbContract.Worker.COLUMN_FIO)));
            } while (cursor.moveToNext());
            cursor.close();
        }
        repository.close();

        workerSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, workerNames));
    }

    private void loadAuditLog() {
        long workerId = workerIds.get(workerSpinner.getSelectedItemPosition());
        Cursor cursor = new EventRepository(this).getAuditLog(selectedDateIso, workerId);
        if (adapter == null) {
            adapter = new AuditLogAdapter(this, cursor);
            auditListView.setAdapter(adapter);
        } else {
            adapter.changeCursor(cursor);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            selectedDateIso = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            String displayValue = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.getTime());
            dateEditText.setText(displayValue);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
}
