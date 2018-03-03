package bicinetica.com.bicinetica.model.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import java.util.UUID;

import bicinetica.com.bicinetica.model.bluetooth.characteristics.Characteristic;
import bicinetica.com.bicinetica.model.bluetooth.characteristics.CscMeasurement;

/***
 * Cycling speed and cadence bluetooth service profile
 */
public class BluetoothCscService extends BluetoothGattServiceBase {

    public static final UUID SERVICE_UUID = UUID.fromString("00001816-0000-1000-8000-00805F9B34FB");

    // Other characteistics:
    //public static final UUID SENSOR_LOCATION = UUID.fromString("00002A5D-0000-1000-8000-00805F9B34FB");
    //public static final UUID SC_CONTROL_POINT = UUID.fromString("00002A55-0000-1000-8000-00805F9B34FB");

    public BluetoothCscService(Context context) {
        super(context, SERVICE_UUID);
        notificateCharacteristic(CscMeasurement.CHARACTERISTIC_UUID);
    }

    public BluetoothCscService(Context context, BluetoothDevice device) {
        this(context);
        connect(device);
    }

    @Override
    protected Characteristic decode(BluetoothGattCharacteristic characteristic) {
        return CscMeasurement.decode(characteristic);
    }
}
