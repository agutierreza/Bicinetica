package bicinetica.com.bicinetica.model.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import bicinetica.com.bicinetica.model.bluetooth.characteristics.CpFeature;
import bicinetica.com.bicinetica.model.bluetooth.characteristics.CpMeasurement;

/***
 * Cycling power bluetooth service profile
 */
public class BluetoothCpService {

    private static final String TAG = BluetoothCpService.class.getSimpleName();

    public static final UUID GATT_SERVICE_UUID = UUID.fromString("00001818-0000-1000-8000-00805F9B34FB");

    private ArrayList<BluetoothCpListener> listeners = new ArrayList<>();
    private BluetoothGatt gatt;

    public BluetoothCpService(Context context, BluetoothDevice device) {
        gatt = device.connectGatt(context, false, new CscCallback());

        if (gatt == null) {
            Log.i(TAG, "Unable to connect GATT server");
        }
        else {
            Log.i(TAG, "Trying to connect GATT server");
        }
    }

    public void registerLisneter(BluetoothCpListener listener) {
        listeners.add(listener);
    }

    public void unregisterLisneter(BluetoothCpListener listener) {
        listeners.remove(listener);
    }

    public void close() {
        gatt.close();
    }

    void publishResult(CpMeasurement csc) {
        for (BluetoothCpListener listener : listeners) {
            listener.onCpReceived(csc);
        }
    }

    public interface BluetoothCpListener {
        void onCpReceived(CpMeasurement cp);
    }

    private class CscCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "BluetoothDevice CONNECTED: " + gatt.getDevice().getAddress() + " " + gatt.getDevice().getName());
                gatt.discoverServices();
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "BluetoothDevice DISCONNECTED");
                if (status == 133) {
                    Log.i(TAG, "Many connections");
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, "Services discovered");

            BluetoothGattService service =  gatt.getService(GATT_SERVICE_UUID);
            BluetoothGattCharacteristic cp = service.getCharacteristic(CpMeasurement.CHARACTERISTIC_UUID);

            if (gatt.setCharacteristicNotification(cp, true)) {
                BluetoothGattDescriptor descriptor = cp.getDescriptor(GattCommonDescriptors.CLIENT_CHARACTERISTIC_CONFIG);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(CpMeasurement.CHARACTERISTIC_UUID)) {
                publishResult(CpMeasurement.decode(characteristic));
            }
        }
    }
}

