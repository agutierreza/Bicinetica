package bicinetica.com.bicinetica.model.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

public class CpMeasurement {
    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("00002A63-0000-1000-8000-00805F9B34FB");

    public CpMeasurement() { }

    @Override
    public String toString() {

        return super.toString();
        /*
        StringBuilder sb = new StringBuilder();
        if (wheelRevolutions != 0) {
            sb.append(String.format("[%s] wheelRevolutions: %s", wheelRevolutionsEventTime, wheelRevolutions));
        }
        if (crankRevolutions != 0) {
            if (sb.length() != 0) {
                sb.append(" , ");
            }
            sb.append(String.format("[%s] crankRevolutions: %s", crankRevolutionsEventTime, crankRevolutions));
        }
        return sb.toString();*/
    }

    public static CpMeasurement create(BluetoothGattCharacteristic characteristic) {
        CpMeasurement res = new CpMeasurement();

        int offset = 0;

        int flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);

        boolean pedalPowerBalancePresent = (flags & 1) == 1;
        boolean pedalPowerReferencePresent = (flags & 2) == 2;
        boolean accumulatedTorquePresent = (flags & 4) == 4;
        boolean accumulatedTorqueSource = (flags & 8) == 8;
        boolean wheelRevolutionDataPresent = (flags & 16) == 16;
        boolean crankRevolutionDataPresent = (flags & 32) == 32;
        boolean extremeForceMagnitudesPresent = (flags & 64) == 64;
        boolean extremeTorqueMagnitudesPresent = (flags & 128) == 128;
        boolean extremeAnglesPresent = (flags & 256) == 256;
        boolean topDeadSpotAnglePresent = (flags & 512) == 512;
        boolean bottomDeadSpotAnglePresent = (flags & 1024) == 1024;
        boolean accumulatedEnergyPresent = (flags & 2048) == 2048;
        boolean offsetCompensationIndicator = (flags & 4096) == 4096;

        offset += 2;

        return res;
    }
}
