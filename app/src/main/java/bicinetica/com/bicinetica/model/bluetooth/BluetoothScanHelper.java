package bicinetica.com.bicinetica.model.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.UUID;

public final class BluetoothScanHelper {

    private static final String TAG = BluetoothScanHelper.class.getSimpleName();

    private static BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    private boolean founded = false;
    private Callback callback;

    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int i, byte[] bytes) {
            adapter.stopLeScan(this);
            if (!founded) {
                Log.i(TAG, "BluetoothDevice founded: " + device.getAddress() + " " + device.getName());
                founded = true;
                callback.onDeviceDetected(device);
                Log.i(TAG, "LE Search stopped.");
            }
        }
    };

    public BluetoothScanHelper() { }

    public void searchLeDevice(UUID serviceId, Callback callback) {
        this.callback = callback;

        adapter.startLeScan(new UUID[] { serviceId }, scanCallback);

        Log.i(TAG, "LE Search Started.");
    }

    public void searchLeDevice(Callback callback) {
        this.callback = callback;

        adapter.startLeScan(scanCallback);

        Log.i(TAG, "LE Search Started.");
    }

    public interface Callback {
        void onDeviceDetected(BluetoothDevice device);
    }
}
