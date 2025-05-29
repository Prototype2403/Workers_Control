package com.example.workerscontrol;
import java.util.Calendar;
import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.workerscontrol.data.WorkerRepository;
import com.example.workerscontrol.data.WokerDbContract;

public class ViewEmployeeActivity extends AppCompatActivity {

    long id;
    ImageView avatar_imageView;
    TextView name_textView;
    TextView post_textView;
    TextView work_days_textView;
    TextView work_time_textView;
    EditText check_editText;


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
}