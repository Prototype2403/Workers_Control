package com.example.workerscontrol;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.workerscontrol.data.WorkerRepository;

public class AddForm extends AppCompatActivity {

    EditText editText_name;
    EditText editText_family;
    EditText editText_fatherName;
    EditText editText_post;
    EditText editText_timeFrom;
    EditText editText_timeTo;

    CheckBox monday_check, tuesday_check, wednesday_check,
            thursday_check, friday_check, saturday_check, sunday_check;

    Button save_button;

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

        WorkerRepository workerRepository= new WorkerRepository(this);
        long result = workerRepository.insertWorker(
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
        boolean success = result != -1;
        if (success) {
            Toast.makeText(this, "Работник добавлен!", Toast.LENGTH_SHORT).show();
            finish(); // Закрыть форму
        } else {
            Toast.makeText(this, "Ошибка при добавлении!", Toast.LENGTH_SHORT).show();
        }
    }
}
