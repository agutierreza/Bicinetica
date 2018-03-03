package bicinetica.com.bicinetica.model.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import java.util.UUID;

import bicinetica.com.bicinetica.model.bluetooth.characteristics.Characteristic;
import bicinetica.com.bicinetica.model.bluetooth.characteristics.CpMeasurement;

/***
 * Cycling power bluetooth service profile
 */
public class BluetoothCpService extends BluetoothGattServiceBase {
    public static final UUID SERVICE_UUID = UUID.fromString("00001818-0000-1000-8000-00805F9B34FB");

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
}

