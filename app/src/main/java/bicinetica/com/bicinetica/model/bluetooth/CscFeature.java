package bicinetica.com.bicinetica.model.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

public class CscFeature {

    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("00002A5C-0000-1000-8000-00805F9B34FB");

    private boolean wheelRevolutionDataSupported, crankRevolutionDataSupported, multipleSensorLocationsSupported;

    public CscFeature() { }

    public static CscFeature decode(BluetoothGattCharacteristic characteristic) {
        CscFeature res = new CscFeature();

        int flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);

        res.wheelRevolutionDataSupported = (flags & 1) == 1;
        res.crankRevolutionDataSupported = (flags & 2) == 2;
        res.multipleSensorLocationsSupported = (flags & 2) == 4;

        return res;
    }
}
