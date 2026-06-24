package com.example.workerscontrol;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.workerscontrol.data.EventRepository;
import com.example.workerscontrol.data.WokerDbContract;
import com.example.workerscontrol.data.WorkerRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class BluetoothSearchActivity extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 7001;
    private static final String BLUELOCK_PREFIX = "BluLock";
    public static final String EXTRA_MODE = "mode";
    public static final String MODE_ATTENDANCE = "attendance";
    public static final String MODE_PASS = "pass";

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> devicesAdapter;
    private final Set<String> devicesSet = new LinkedHashSet<>();
    private boolean receiverRegistered = false;
    private ProgressBar loadingProgressBar;
    private Button scanButton;
    private ListView devicesListView;
    private long workerId = -1L;
    private String mode = MODE_ATTENDANCE;
    private EventRepository eventRepository;

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && hasConnectPermission()) {
                    String name = device.getName();
                    if (!isBlueLockDeviceName(name)) {
                        return;
                    }
                    String item = name + "\n" + device.getAddress();
                    if (devicesSet.add(item)) {
                        devicesAdapter.clear();
                        devicesAdapter.addAll(devicesSet);
                        devicesAdapter.notifyDataSetChanged();
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(BluetoothSearchActivity.this, "Поиск устройств завершен", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyTheme(this);
        AppSettings.applyLanguage(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bluetooth_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        workerId = getIntent().getLongExtra("worker_id", -1L);
        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (!MODE_PASS.equals(mode)) {
            mode = MODE_ATTENDANCE;
        }
        if (workerId <= 0) {
            Toast.makeText(this, "Сотрудник не выбран", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        eventRepository = new EventRepository(this);
        devicesListView = findViewById(R.id.bluetoothDevices_listView);
        scanButton = findViewById(R.id.scanBluetooth_button);
        loadingProgressBar = findViewById(R.id.loading_progressBar);

        devicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        devicesListView.setAdapter(devicesAdapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
            scanButton.setEnabled(false);
            return;
        }

        registerBluetoothReceiver();

        scanButton.setOnClickListener(v -> {
            if (!ensurePermissions()) {
                return;
            }
            startDeviceDiscovery();
        });

        devicesListView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= devicesAdapter.getCount()) {
                return;
            }
            String selectedDevice = devicesAdapter.getItem(position);
            if (selectedDevice == null) {
                return;
            }
            applySelectedDeviceWithDelay(selectedDevice);
        });

        if (ensurePermissions()) {
            showBondedDevices();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        if (receiverRegistered) {
            unregisterReceiver(bluetoothReceiver);
            receiverRegistered = false;
        }
    }

    private void registerBluetoothReceiver() {
        if (receiverRegistered) {
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, filter);
        receiverRegistered = true;
    }

    private boolean ensurePermissions() {
        ArrayList<String> requiredPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
        if (!requiredPermissions.isEmpty()) {
            requestPermissions(requiredPermissions.toArray(new String[0]), REQUEST_BLUETOOTH_PERMISSIONS);
            return false;
        }
        return true;
    }

    private boolean hasConnectPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    }

    private void showBondedDevices() {
        if (!hasConnectPermission()) {
            return;
        }
        if (bluetoothAdapter.getBondedDevices() != null) {
            for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
                String name = device.getName();
                if (!isBlueLockDeviceName(name)) {
                    continue;
                }
                devicesSet.add(name + "\n" + device.getAddress());
            }
            devicesAdapter.clear();
            devicesAdapter.addAll(devicesSet);
            devicesAdapter.notifyDataSetChanged();
        }
    }

    private void startDeviceDiscovery() {
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Enable Bluetooth and try again", Toast.LENGTH_SHORT).show();
            return;
        }
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        showBondedDevices();
        boolean started = bluetoothAdapter.startDiscovery();
        if (!started) {
            Toast.makeText(this, "Не удалось запустить поиск устройств", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Searching Bluetooth devices...", Toast.LENGTH_SHORT).show();
        }
    }

    private void applySelectedDeviceWithDelay(String selectedDevice) {
        setLoadingVisible(true);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            setLoadingVisible(false);
            if (MODE_PASS.equals(mode) ? applyAccessLog(selectedDevice) : applyAttendanceEvent()) {
                Toast.makeText(this, "Saved via Bluetooth:\n" + selectedDevice, Toast.LENGTH_SHORT).show();
                finish();
            }
        }, 2000);
    }

    private boolean applyAccessLog(String selectedDevice) {
        if (!hasObjectAccess()) {
            return false;
        }
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String place = extractDevicePlace(selectedDevice);
        long result = eventRepository.addAccessLog(workerId, currentDate, currentTime, place);
        if (result <= 0) {
            Toast.makeText(this, "Не удалось сохранить проход", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean applyAttendanceEvent() {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        Cursor events = eventRepository.getEventsByWorkerAndDate(workerId, currentDate);

        boolean isWorking = false;
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
            events.close();
        }
        if (hasArrival && hasDeparture) {
            Toast.makeText(this, "Рабочий день уже завершен", Toast.LENGTH_SHORT).show();
            return false;
        }

        int type = isWorking ? WokerDbContract.Events.EVENT_FROM_WORK : WokerDbContract.Events.EVENT_TO_WORK;
        if (type == WokerDbContract.Events.EVENT_TO_WORK && !hasObjectAccess()) {
            return false;
        }
        long result = eventRepository.addEvent(workerId, currentDate, currentTime, type);
        if (result <= 0) {
            Toast.makeText(this, "Не удалось сохранить отметку", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean hasObjectAccess() {
        WorkerRepository workerRepository = new WorkerRepository(this);
        int tokenCount = workerRepository.getAccessTokenCount(workerId);
        workerRepository.close();
        if (tokenCount == 0) {
            Toast.makeText(this, "Отсутствует доступ на объект", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void setLoadingVisible(boolean visible) {
        loadingProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        devicesListView.setEnabled(!visible);
        scanButton.setEnabled(!visible);
    }

    private boolean isBlueLockDeviceName(String name) {
        return name != null && name.startsWith(BLUELOCK_PREFIX);
    }

    private String extractDevicePlace(String selectedDevice) {
        if (selectedDevice == null || selectedDevice.trim().isEmpty()) {
            return "Bluetooth";
        }
        String[] lines = selectedDevice.split("\\n");
        return lines.length > 0 ? lines[0] : selectedDevice;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            boolean allGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                showBondedDevices();
                startDeviceDiscovery();
            } else {
                Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
