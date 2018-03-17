package bicinetica.com.bicinetica;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import bicinetica.com.bicinetica.model.bluetooth.BluetoothCpService;
import bicinetica.com.bicinetica.model.bluetooth.BluetoothCscService;
import bicinetica.com.bicinetica.model.bluetooth.BluetoothDevicesManager;

public class SensorsActivity extends AppCompatActivity {

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;

    private List<BluetoothDevice> scanned = new ArrayList<>();
    private List<BluetoothDevice> connectedDevices;

    private BluetoothDeviceAdapter scannerAdapter;
    private BluetoothDeviceAdapter connectedAdapter;

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();

            if (!scanned.contains(device)) {
                Log.i("BluetoothSearch", "BluetoothDevice founded: " + device.getAddress() + " " + device.getName());

                scanned.add(device);
                scannerAdapter.notifyItemInserted(scanned.size() - 1);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private boolean searching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.devices);

        connectedDevices = BluetoothDevicesManager.getInstance().getDevices();

        RecyclerView connectedList = this.findViewById(R.id.connected_devices_list);
        connectedList.setLayoutManager(new LinearLayoutManager(this));
        connectedAdapter = new BluetoothDeviceAdapter(connectedDevices);
        connectedList.setAdapter(connectedAdapter);
        connectedList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        RecyclerView resultList = this.findViewById(R.id.result_list);
        resultList.setLayoutManager(new LinearLayoutManager(this));
        scannerAdapter = new BluetoothDeviceAdapter(scanned, listListener);
        resultList.setAdapter(scannerAdapter);
        resultList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
    }

    @Override
    protected void onDestroy() {
        if (searching) stopScan();
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (searching) {
            getMenuInflater().inflate(R.menu.bluetooth_stop, menu);
        }
        else {
            getMenuInflater().inflate(R.menu.bluetooth, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            startScan();
            invalidateOptionsMenu();
        }
        if (id == R.id.action_stop) {
            stopScan();
            invalidateOptionsMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    public void startScan() {
        startScan(BluetoothCscService.SERVICE_UUID,
                BluetoothCpService.SERVICE_UUID);
    }

    public void startScan(UUID serviceId) {
        startScan(new UUID[] { serviceId });
    }

    public void startScan(UUID... serviceIds) {
        List<UUID> ids = new ArrayList<>();
        for (UUID id : serviceIds) {
            ids.add(id);
        }
        startScan(ids);
    }

    public void startScan(Iterable<UUID> serviceIds) {
        List<ScanFilter> filters = new ArrayList<>();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        for (UUID uuid: serviceIds) {
            builder.setServiceUuid(new ParcelUuid(uuid));
            filters.add(builder.build());
        }

        ScanSettings settings = new ScanSettings.Builder().build();

        bluetoothLeScanner.startScan(filters, settings, scanCallback);

        Log.i("BluetoothSearch", "LE Search Started.");

        searching = true;
    }

    private void stopScan() {
        bluetoothLeScanner.stopScan(scanCallback);
        searching = false;
    }

    private ListListener listListener = new ListListener() {
        @Override
        public void onDeviceClicked(final BluetoothDevice device) {
            new AlertDialog.Builder(SensorsActivity.this)
                    .setMessage(device.getName())
                    .setTitle(R.string.ask_for_connection)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (!connectedDevices.contains(device)) {
                                connectedDevices.add(device);
                                connectedAdapter.notifyItemInserted(connectedDevices.size() - 1);
                            }
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { }
                    })
                    .create()
                    .show();
        }
    };

    public interface ListListener {
        void onDeviceClicked(BluetoothDevice item);
    }
}
