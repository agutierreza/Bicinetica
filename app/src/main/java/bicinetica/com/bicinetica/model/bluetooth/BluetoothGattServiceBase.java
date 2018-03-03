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
import java.util.List;
import java.util.UUID;

import bicinetica.com.bicinetica.model.bluetooth.characteristics.Characteristic;
import bicinetica.com.bicinetica.model.bluetooth.characteristics.CscMeasurement;

public abstract class BluetoothGattServiceBase {

    private static final String TAG = BluetoothGattServiceBase.class.getSimpleName();

    private Context context;

    private UUID serviceUuid;

    private BluetoothGatt gatt;
    private BluetoothGattService service;
    private ServiceCallback callback = new ServiceCallback();

    private List<NotificationListener> listeners = new ArrayList<>();

    public BluetoothGattServiceBase(Context context, UUID serviceUuid) {
        this.context = context;
        this.serviceUuid = serviceUuid;
    }

    public void suscribe(NotificationListener listener) {
        listeners.add(listener);
    }

    public void remove(NotificationListener listener) {
        listeners.remove(listener);
    }

    public void notificateCharacteristic(UUID characteristic) {
        callback.enableNotifications(characteristic);
    }

    public void connect(BluetoothDevice device) {
        gatt = device.connectGatt(context, true, callback);

        if (gatt == null) {
            Log.i(TAG, "Unable to connect GATT server");
        }
        else {
            Log.i(TAG, "Trying to connect GATT server");
        }
    }

    public void close() {
        gatt.close();
    }

    protected abstract Characteristic decode(BluetoothGattCharacteristic characteristic);

    public interface NotificationListener {
        void onNotificationReceived(Characteristic csc);
    }

    protected class ServiceCallback extends BluetoothGattCallback {

        private List<UUID> notifications = new ArrayList<>();

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

            service =  gatt.getService(serviceUuid);

            for (UUID uuid : notifications) {
                enableNotificationsInternal(uuid);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(CscMeasurement.CHARACTERISTIC_UUID)) {
                notificate(decode(characteristic));
            }
        }

        public void enableNotifications(UUID uuid) {
            if (service == null) {
                notifications.add(uuid);
            }
            else {
                enableNotificationsInternal(uuid);
            }
        }

        private void enableNotificationsInternal(UUID uuid) {
            enableNotificationsInternal(service.getCharacteristic(uuid));
        }

        private void enableNotificationsInternal(BluetoothGattCharacteristic characteristic) {
            if (gatt.setCharacteristicNotification(characteristic, true)) {
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(GattCommonDescriptors.CLIENT_CHARACTERISTIC_CONFIG);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }

        private void notificate(Characteristic csc) {
            for (NotificationListener listener : listeners) {
                listener.onNotificationReceived(csc);
            }
        }
    }
}

