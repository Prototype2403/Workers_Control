package com.example.workerscontrol;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.workerscontrol.data.WorkerRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton floatingActionButton;
    ListView dataListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyTheme(this);
        AppSettings.applyLanguage(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        floatingActionButton = findViewById(R.id.floatingActionButton_addNewPeople);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddForm.class);
                startActivity(intent);
            }
        });

        dataListView = findViewById(R.id.dataListView);
        dataListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent2 = new Intent(MainActivity.this, ViewEmployeeActivity.class);
                intent2.putExtra("id", id);
                startActivity(intent2);
            }
        });
    }

    @Override
    protected void onStart() {
        updateListOfWorkers();
        super.onStart();
    }

    public void updateListOfWorkers(){
        WorkerRepository workerRepository = new WorkerRepository(this);
        Cursor data = workerRepository.getAllWorkers();

        WorkerCursorAdapter cursorAdapter = new WorkerCursorAdapter(this, data, false);
        dataListView.setAdapter(cursorAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_admin_settings) {
            startActivity(new Intent(this, AdminSettingsActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.menu_audit_log) {
            startActivity(new Intent(this, AuditLogActivity.class));
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
}
