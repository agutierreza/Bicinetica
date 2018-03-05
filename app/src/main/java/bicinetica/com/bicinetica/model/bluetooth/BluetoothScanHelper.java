package bicinetica.com.bicinetica.model.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class BluetoothScanHelper {

    private static final String TAG = BluetoothScanHelper.class.getSimpleName();

    private boolean founded = false;
    private Callback callback;

    private BluetoothAdapter adapter;
    private BluetoothLeScanner bluetoothLeScanner;

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();

            bluetoothLeScanner.stopScan(this);
            if (!founded) {
                Log.i(TAG, "BluetoothDevice founded: " + device.getAddress() + " " + device.getName());
                founded = true;
                callback.onDeviceDetected(device);
                Log.i(TAG, "LE Search stopped.");
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

    public BluetoothScanHelper(Context context) {
        BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = mBluetoothManager.getAdapter();
        bluetoothLeScanner = adapter.getBluetoothLeScanner();
    }

    public void searchLeDevice(UUID serviceId, Callback callback) {
        this.callback = callback;

        ScanFilter filter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(serviceId)).build();
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(filter);

        ScanSettings settings = new ScanSettings.Builder().build();

        bluetoothLeScanner.startScan(filters, settings, scanCallback);

        Log.i(TAG, "LE Search Started.");
    }

    public void searchLeDevice(Callback callback) {
        this.callback = callback;

        bluetoothLeScanner.startScan(scanCallback);

        Log.i(TAG, "LE Search Started.");
    }

    public interface Callback {
        void onDeviceDetected(BluetoothDevice device);
    }
}
