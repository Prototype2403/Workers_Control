package com.example.workerscontrol;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.workerscontrol.data.WokerDbContract;
import com.example.workerscontrol.data.WorkerRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WorkerSecurityActivity extends AppCompatActivity {

    private static final String[] ACCESS_PLACES = {
            "Главный вход 1",
            "Главный вход 2",
            "Склад 1",
            "Офис 2 этаж"
    };

    private long workerId;
    private EditText loginEditText;
    private EditText passwordEditText;
    private ListView tokensListView;
    private AccessTokenAdapter tokenAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyTheme(this);
        AppSettings.applyLanguage(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_worker_security);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        workerId = getIntent().getLongExtra("worker_id", -1L);
        loginEditText = findViewById(R.id.securityLogin_editText);
        passwordEditText = findViewById(R.id.securityPassword_editText);
        tokensListView = findViewById(R.id.accessTokens_listView);
        Button saveButton = findViewById(R.id.saveCredentials_button);
        Button issueTokenButton = findViewById(R.id.issueToken_button);

        loadCredentials();
        loadTokens();

        saveButton.setOnClickListener(v -> saveCredentials());
        issueTokenButton.setOnClickListener(v -> showIssueTokenDialog());
        tokensListView.setOnItemLongClickListener((parent, view, position, tokenId) -> {
            showRevokeTokenDialog(tokenId);
            return true;
        });
    }

    private void loadCredentials() {
        WorkerRepository repository = new WorkerRepository(this);
        Cursor cursor = repository.getWorkerById(workerId);
        if (cursor != null && cursor.moveToFirst()) {
            String login = cursor.getString(cursor.getColumnIndexOrThrow(WokerDbContract.Worker.COLUMN_LOGIN));
            String password = cursor.getString(cursor.getColumnIndexOrThrow(WokerDbContract.Worker.COLUMN_PASSWORD));
            loginEditText.setText(isEmpty(login) ? "worker" + workerId : login);
            passwordEditText.setText(isEmpty(password) ? "pass" + workerId : password);
            cursor.close();
        }
        repository.close();
    }

    private void saveCredentials() {
        WorkerRepository repository = new WorkerRepository(this);
        int result = repository.updateCredentials(workerId,
                loginEditText.getText().toString().trim(),
                passwordEditText.getText().toString().trim());
        repository.close();
        Toast.makeText(this, result > 0 ? "Данные сохранены" : "Не удалось сохранить данные", Toast.LENGTH_SHORT).show();
    }

    private void loadTokens() {
        Cursor cursor = new WorkerRepository(this).getAccessTokens(workerId);
        if (tokenAdapter == null) {
            tokenAdapter = new AccessTokenAdapter(this, cursor);
            tokensListView.setAdapter(tokenAdapter);
        } else {
            tokenAdapter.changeCursor(cursor);
        }
    }

    private void showIssueTokenDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Выдать новый токен")
                .setItems(ACCESS_PLACES, (dialog, which) -> issueToken(ACCESS_PLACES[which]))
                .show();
    }

    private void issueToken(String place) {
        Calendar expiry = Calendar.getInstance();
        expiry.add(Calendar.YEAR, 1);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        WorkerRepository repository = new WorkerRepository(this);
        repository.addAccessToken(workerId, place, formatter.format(new Date()), formatter.format(expiry.getTime()));
        repository.close();
        loadTokens();
    }

    private void showRevokeTokenDialog(long tokenId) {
        new AlertDialog.Builder(this)
                .setTitle("Токен")
                .setPositiveButton("Отозвать", (dialog, which) -> revokeToken(tokenId))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void revokeToken(long tokenId) {
        WorkerRepository repository = new WorkerRepository(this);
        repository.deleteAccessToken(tokenId);
        repository.close();
        loadTokens();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
