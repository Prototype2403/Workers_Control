package com.example.workerscontrol;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.workerscontrol.data.WorkerRepository;

public class autorization extends AppCompatActivity {

    EditText editText_login;
    EditText editText_password;
    Button login_button;
    TextView error_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autorization);

        editText_login = findViewById(R.id.login);
        editText_password = findViewById(R.id.password);
        login_button = findViewById(R.id.login_button);
        error_text = findViewById(R.id.error_login);

        login_button.setOnClickListener(v -> login());
    }

    private void login(){
        String login = editText_login.getText().toString().trim();
        String password = editText_password.getText().toString().trim();

        if (login.equals("admin") && password.equals("1234")){
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        WorkerRepository repository = new WorkerRepository(this);
        Cursor worker = repository.getWorkerByCredentials(login, password);
        if (worker != null && worker.moveToFirst()) {
            long id = worker.getLong(worker.getColumnIndexOrThrow("_id"));
            worker.close();
            repository.close();

            Intent next = new Intent(this, ProfilWorker.class);
            next.putExtra("id", id);
            startActivity(next);
            finish();
            return;
        }
        if (worker != null) {
            worker.close();
        }
        repository.close();
        error_text.setText("Неверный логин или пароль");
    }
}
