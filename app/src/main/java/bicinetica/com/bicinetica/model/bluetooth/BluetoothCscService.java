package bicinetica.com.bicinetica.model.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.util.ArrayList;
import java.util.UUID;

/***
 * Cycling speed and cadence bluetooth service profile
 */
public class BluetoothCscService {

    public static final UUID GATT_SERVICE_UUID = UUID.fromString("00001816-0000-1000-8000-00805F9B34FB");

    //  Other services:
    //public static final UUID CSC_FEATURE = UUID.fromString("00002A5C-0000-1000-8000-00805F9B34FB");
    //public static final UUID SENSOR_LOCATION = UUID.fromString("00002A5D-0000-1000-8000-00805F9B34FB");
    //public static final UUID SC_CONTROL_POINT = UUID.fromString("00002A55-0000-1000-8000-00805F9B34FB");

    private ArrayList<BluetoothCscListener> listeners = new ArrayList<>();
    private BluetoothGatt gatt;

    public BluetoothCscService(Context context, BluetoothDevice device) {
        gatt = device.connectGatt(context, true, new CscCallback());
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
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            if (characteristic.getUuid().equals(CscMeasurement.CHARACTERISTIC_UUID)) {
                publishResult(CscMeasurement.decode(characteristic));
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            BluetoothGattService service =  gatt.getService(GATT_SERVICE_UUID);

            BluetoothGattCharacteristic csc = service.getCharacteristic(CscMeasurement.CHARACTERISTIC_UUID);

            if (gatt.setCharacteristicNotification(csc, true)) {
                BluetoothGattDescriptor descriptor = csc.getDescriptor(GattCommonDescriptors.CLIENT_CHARACTERISTIC_CONFIG);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }
    }
}
