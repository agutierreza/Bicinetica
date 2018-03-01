package bicinetica.com.bicinetica.model.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.UUID;

public final class BluetoothScanHelper {

    private static BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    private boolean founded = false;
    private Callback callback;

    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            adapter.stopLeScan(this);
            if (!founded) {
                founded = true;
                callback.onDeviceDetected(bluetoothDevice);
            }
        }
    };

    public BluetoothScanHelper() { }

    public void searchDevice(UUID serviceId, Callback callback) {
        this.callback = callback;
        adapter.startLeScan(new UUID[] { serviceId }, scanCallback);
    }

    public interface Callback {
        void onDeviceDetected(BluetoothDevice device);
    }
}
