package com.example.workerscontrol;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.workerscontrol.data.WorkerRepository;
import com.example.workerscontrol.data.WokerDbContract;

public class AddForm extends AppCompatActivity {
    long id = -1;
    EditText editText_name;
    EditText editText_family;
    EditText editText_fatherName;
    EditText editText_post;
    EditText editText_timeFrom;
    EditText editText_timeTo;
    TextView title_textView;

    CheckBox monday_check, tuesday_check, wednesday_check,
            thursday_check, friday_check, saturday_check, sunday_check;

    Button save_button;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_form);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editText_name = findViewById(R.id.editText_Name);
        editText_family = findViewById(R.id.editText_Family);
        editText_fatherName = findViewById(R.id.editText_FatherName);
        editText_post = findViewById(R.id.editText_Post);
        editText_timeFrom = findViewById(R.id.editText_TimeFrom);
        editText_timeTo = findViewById(R.id.editText_TimeTo);
        save_button = findViewById(R.id.button_save);

        monday_check = findViewById(R.id.checkBox_Monday);
        tuesday_check = findViewById(R.id.checkBox_Tuesday);
        wednesday_check = findViewById(R.id.checkBox_Wednesday);
        thursday_check = findViewById(R.id.checkBox_Thursday);
        friday_check = findViewById(R.id.checkBox_Friday);
        saturday_check = findViewById(R.id.checkBox_Saturday);
        sunday_check = findViewById(R.id.checkBox_Sunday);

        editText_timeFrom.setOnClickListener(v -> showTimePickerDialog(editText_timeFrom));
        editText_timeTo.setOnClickListener(v -> showTimePickerDialog(editText_timeTo));

        save_button.setOnClickListener(v -> saveWorkerToDatabase());

        title_textView = findViewById(R.id.title_textView);
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            id = extras.getLong("id");
            title_textView.setText("Редактировать работника");
            WorkerRepository repository = new WorkerRepository(this);
            Cursor data = repository.getWorkerById(id);
            if (data != null && data.moveToFirst()) {
                String fullName = data.getString(data.getColumnIndexOrThrow("fio"));
                String[] nameParts = fullName.split(" ");
                
                editText_family.setText(nameParts[0]);
                editText_name.setText(nameParts.length > 1 ? nameParts[1] : "");
                editText_fatherName.setText(nameParts.length > 2 ? nameParts[2] : "");
                
                editText_post.setText(data.getString(data.getColumnIndexOrThrow("post")));
                editText_timeFrom.setText(data.getString(data.getColumnIndexOrThrow("time_from")));
                editText_timeTo.setText(data.getString(data.getColumnIndexOrThrow("time_to")));
                
                monday_check.setChecked(data.getInt(data.getColumnIndexOrThrow("monday")) == 1);
                tuesday_check.setChecked(data.getInt(data.getColumnIndexOrThrow("tuesday")) == 1);
                wednesday_check.setChecked(data.getInt(data.getColumnIndexOrThrow("wednesday")) == 1);
                thursday_check.setChecked(data.getInt(data.getColumnIndexOrThrow("thursday")) == 1);
                friday_check.setChecked(data.getInt(data.getColumnIndexOrThrow("friday")) == 1);
                saturday_check.setChecked(data.getInt(data.getColumnIndexOrThrow("saturday")) == 1);
                sunday_check.setChecked(data.getInt(data.getColumnIndexOrThrow("sunday")) == 1);
                
                data.close();
            }
        }else {
            title_textView.setText("Добавить работника");
        }
    }

    private void showTimePickerDialog(EditText targetEditText) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(AddForm.this, (timePicker, hourOfDay, minutes) -> {
            targetEditText.setText(String.format("%02d:%02d", hourOfDay, minutes));
        }, 0, 0, true);
        timePickerDialog.show();
    }

    private void saveWorkerToDatabase() {
        String family = editText_family.getText().toString().trim();
        String name = editText_name.getText().toString().trim();
        String fatherName = editText_fatherName.getText().toString().trim();
        String post = editText_post.getText().toString().trim();
        String timeFrom = editText_timeFrom.getText().toString().trim();
        String timeTo = editText_timeTo.getText().toString().trim();

        boolean[] days = {
                monday_check.isChecked(),
                tuesday_check.isChecked(),
                wednesday_check.isChecked(),
                thursday_check.isChecked(),
                friday_check.isChecked(),
                saturday_check.isChecked(),
                sunday_check.isChecked()
        };

        if (family.isEmpty() || name.isEmpty() || post.isEmpty() || timeFrom.isEmpty() || timeTo.isEmpty()) {
            Toast.makeText(this, "Заполните все обязательные поля!", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = family + " " + name + " " + fatherName;
        WorkerRepository workerRepository = new WorkerRepository(this);
        long result;
        
        if (id != -1) {
            ContentValues values = new ContentValues();
            values.put(WokerDbContract.Worker.COLUMN_FIO, fullName);
            values.put(WokerDbContract.Worker.COLUMN_POST, post);
            values.put(WokerDbContract.Worker.COLUMN_TIME_FROM, timeFrom);
            values.put(WokerDbContract.Worker.COLUMN_TIME_TO, timeTo);
            values.put(WokerDbContract.Worker.COLUMN_MONDAY, days[0] ? 1 : 0);
            values.put(WokerDbContract.Worker.COLUMN_TUESDAY, days[1] ? 1 : 0);
            values.put(WokerDbContract.Worker.COLUMN_WEDNESDAY, days[2] ? 1 : 0);
            values.put(WokerDbContract.Worker.COLUMN_THURSDAY, days[3] ? 1 : 0);
            values.put(WokerDbContract.Worker.COLUMN_FRIDAY, days[4] ? 1 : 0);
            values.put(WokerDbContract.Worker.COLUMN_SATURDAY, days[5] ? 1 : 0);
            values.put(WokerDbContract.Worker.COLUMN_SUNDAY, days[6] ? 1 : 0);

            result = workerRepository.updateWorker(id, values);
            if (result > 0) {
                Toast.makeText(this, "Данные работника обновлены!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Ошибка при обновлении данных!", Toast.LENGTH_SHORT).show();
            }
        } else {
            result = workerRepository.insertWorker(
                fullName,
                post,
                timeFrom,
                timeTo,
                days[0] ? 1 : 0,
                days[1] ? 1 : 0,
                days[2] ? 1 : 0,
                days[3] ? 1 : 0,
                days[4] ? 1 : 0,
                days[5] ? 1 : 0,
                days[6] ? 1 : 0
            );
            if (result != -1) {
                Toast.makeText(this, "Работник добавлен!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Ошибка при добавлении!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
