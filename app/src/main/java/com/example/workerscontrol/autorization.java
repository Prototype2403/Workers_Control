package com.example.workerscontrol;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class autorization extends AppCompatActivity {

    EditText editText_login;
    EditText editText_password;
    Button login_button;
    TextView error_text;
    long id;

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
        Intent next;
        if (login.equals("admin") && password.equals("1234")){
            next = new Intent(this, MainActivity.class);
            startActivity(next);
            finish();
        } else if (login.equals("profile1") && password.equals("1111")) {
            next = new Intent(this, ProfilWorker.class);
            id = 1;
            next.putExtra("id", id);
            startActivity(next);
            finish();
        } else if (login.equals("profile2") && password.equals("2222")) {
            next = new Intent(this, ProfilWorker.class);
            id = 2;
            next.putExtra("id", id);
            startActivity(next);
            finish();
        } else if (login.equals("profile3") && password.equals("3333")) {
            next = new Intent(this, ProfilWorker.class);
            id = 3;
            next.putExtra("id", id);
            startActivity(next);
            finish();
        } else if (login.equals("profile4") && password.equals("4444")) {
            next = new Intent(this, ProfilWorker.class);
            id = 4;
            next.putExtra("id", id);
            startActivity(next);
            finish();
        }
        else{
        error_text.setText("Неверный логин или пароль");
        }
    }

}