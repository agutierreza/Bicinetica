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

import bicinetica.com.bicinetica.model.bluetooth.characteristics.CpMeasurement;

/***
 * Cycling power bluetooth service profile
 */
public class BluetoothCpService {

    public static final UUID GATT_SERVICE_UUID = UUID.fromString("00001818-0000-1000-8000-00805F9B34FB");

    private ArrayList<BluetoothCpListener> listeners = new ArrayList<>();
    private BluetoothGatt gatt;

    public BluetoothCpService(Context context, BluetoothDevice device) {
        gatt = device.connectGatt(context, true, new CscCallback());
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
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            if (characteristic.getUuid().equals(CpMeasurement.CHARACTERISTIC_UUID)) {
                publishResult(CpMeasurement.decode(characteristic));
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

            BluetoothGattCharacteristic csc = service.getCharacteristic(CpMeasurement.CHARACTERISTIC_UUID);

            if (gatt.setCharacteristicNotification(csc, true)) {
                BluetoothGattDescriptor descriptor = csc.getDescriptor(GattCommonDescriptors.CLIENT_CHARACTERISTIC_CONFIG);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }
    }
}

