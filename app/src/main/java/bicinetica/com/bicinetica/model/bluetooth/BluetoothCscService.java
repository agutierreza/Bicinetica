package bicinetica.com.bicinetica.model.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

import bicinetica.com.bicinetica.model.bluetooth.characteristics.CscMeasurement;

/***
 * Cycling speed and cadence bluetooth service profile
 */
public class BluetoothCscService {

    private static final String TAG = BluetoothCpService.class.getSimpleName();

    public static final UUID GATT_SERVICE_UUID = UUID.fromString("00001816-0000-1000-8000-00805F9B34FB");

    //  Other services:
    //public static final UUID CSC_FEATURE = UUID.fromString("00002A5C-0000-1000-8000-00805F9B34FB");
    //public static final UUID SENSOR_LOCATION = UUID.fromString("00002A5D-0000-1000-8000-00805F9B34FB");
    //public static final UUID SC_CONTROL_POINT = UUID.fromString("00002A55-0000-1000-8000-00805F9B34FB");

    private ArrayList<BluetoothCscListener> listeners = new ArrayList<>();
    private BluetoothGatt gatt;

    public BluetoothCscService(Context context, BluetoothDevice device) {
        gatt = device.connectGatt(context, true, new CscCallback());

        if (gatt == null) {
            Log.i(TAG, "Unable to connect GATT server");
        }
        else {
            Log.i(TAG, "Trying to connect GATT server");
        }
    }

    public void registerLisneter(BluetoothCscListener listener) {
        listeners.add(listener);
    }

    public void unregisterLisneter(BluetoothCscListener listener) {
        listeners.remove(listener);
    }

    public void close() {
        gatt.close();
    }

    void publishResult(CscMeasurement csc) {
        for (BluetoothCscListener listener : listeners) {
            listener.onCscReceived(csc);
        }
    }

    public interface BluetoothCscListener {
        void onCscReceived(CscMeasurement csc);
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
            BluetoothGattCharacteristic csc = service.getCharacteristic(CscMeasurement.CHARACTERISTIC_UUID);

            if (gatt.setCharacteristicNotification(csc, true)) {
                BluetoothGattDescriptor descriptor = csc.getDescriptor(GattCommonDescriptors.CLIENT_CHARACTERISTIC_CONFIG);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(CscMeasurement.CHARACTERISTIC_UUID)) {
                publishResult(CscMeasurement.decode(characteristic));
            }
        }
    }
}
