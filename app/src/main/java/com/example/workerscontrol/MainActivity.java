package com.example.workerscontrol;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
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
}