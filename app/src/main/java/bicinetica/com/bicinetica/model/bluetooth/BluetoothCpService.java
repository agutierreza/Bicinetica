package bicinetica.com.bicinetica.model.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.util.UUID;

import bicinetica.com.bicinetica.model.bluetooth.characteristics.Characteristic;
import bicinetica.com.bicinetica.model.bluetooth.characteristics.CpFeature;
import bicinetica.com.bicinetica.model.bluetooth.characteristics.CpMeasurement;

/***
 * Cycling power bluetooth service profile
 */
public class BluetoothCpService extends BluetoothGattServiceBase {
    public static final UUID SERVICE_UUID = UUID.fromString("00001818-0000-1000-8000-00805F9B34FB");

    public static UUID SENSOR_LOCATION = UUID.fromString("00002A5D-0000-1000-8000-00805f9b34fb");

    public BluetoothCpService(Context context) {
        super(context, SERVICE_UUID);
        notificateCharacteristic(CpMeasurement.CHARACTERISTIC_UUID);
    }

    public BluetoothCpService(Context context, BluetoothDevice device) {
        this(context);
        connect(device);
    }

    @Override
    protected Characteristic decode(BluetoothGattCharacteristic characteristic) {
        return CpMeasurement.decode(characteristic);
    }

    public static BluetoothGattService createService() {

        BluetoothGattService service = new BluetoothGattService(SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic powerMeasurement = new BluetoothGattCharacteristic(CpMeasurement.CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        powerMeasurement.addDescriptor(new BluetoothGattDescriptor(GattCommonDescriptors.CLIENT_CHARACTERISTIC_CONFIG,
                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));

        // Only if broadcast supported
        /*powerMeasurement.addDescriptor(new BluetoothGattDescriptor(GattCommonDescriptors.SERVER_CHARACTERISTIC_CONFIG,
                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));*/


        BluetoothGattCharacteristic powerFeature = new BluetoothGattCharacteristic(CpFeature.CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);

        BluetoothGattCharacteristic sensorLocation = new BluetoothGattCharacteristic(SENSOR_LOCATION,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);

        service.addCharacteristic(powerMeasurement);
        service.addCharacteristic(powerFeature);
        service.addCharacteristic(sensorLocation);

        return service;
    }
}

