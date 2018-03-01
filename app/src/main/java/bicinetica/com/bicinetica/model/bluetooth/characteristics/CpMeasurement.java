package bicinetica.com.bicinetica.model.bluetooth.characteristics;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

public class CpMeasurement {
    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("00002A63-0000-1000-8000-00805F9B34FB");

    private int instantaneousPower;
    private float pedalPowerBalance, accumulatedTorque;

    private int wheelRevolutions;
    private float wheelRevolutionsEventTime;

    private int crankRevolutions;
    private float crankRevolutionsEventTime;

    int minimumForce, maximumForce;
    int minimumTorque, maximumTorque;
    int minimumAngle, maximumAngle;

    int topDeadSpotAngle;
    int bottomDeadSpotAngle;

    int accumulatedEnergy;

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

    /**
     * Decoded as defined in bluetooth docs.
     * @see <a href="https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.cycling_power_measurement.xml">Bluetooth docs</a>
     * @param characteristic
     * @return
     */
    public static CpMeasurement decode(BluetoothGattCharacteristic characteristic) {
        CpMeasurement res = new CpMeasurement();

        int offset = 0;

        int flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);

        boolean pedalPowerBalancePresent = (flags & 1) == 1;
        boolean pedalPowerReferencePresent = (flags & 2) == 2; // true = left , false = unkown
        boolean accumulatedTorquePresent = (flags & 4) == 4;
        boolean accumulatedTorqueSource = (flags & 8) == 8; // true = crank based , false = wheel based
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

        res.instantaneousPower = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset);
        offset += 2;

        if (pedalPowerBalancePresent) {
            res.pedalPowerBalance = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset) / 2;
            offset ++;
        }

        if (accumulatedTorquePresent) {
            res.accumulatedTorque = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset) / 32;
            offset += 2;
        }

        if (wheelRevolutionDataPresent) {
            res.wheelRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, offset);
            offset += 4;
            res.wheelRevolutionsEventTime = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset) / 1024;
            offset += 2;
        }

        if (crankRevolutionDataPresent) {
            res.crankRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, offset);
            offset += 4;
            res.crankRevolutionsEventTime = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset) / 1024;
            offset += 2;
        }

        if (extremeForceMagnitudesPresent) {
            res.maximumForce = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset);
            offset += 2;
            res.minimumForce = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset);
            offset += 2;
        }

        if (extremeTorqueMagnitudesPresent) {
            res.maximumTorque = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset) / 32;
            offset += 2;
            res.minimumTorque = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset) / 32;
            offset += 2;
        }

        if (extremeAnglesPresent) {
            offset += 3;

            //TODO : decode UINT_12 values
            /*
            res.maximumTorque = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT12, offset);
            offset += 1.5;
            res.minimumTorque = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT12, offset);
            offset += 1.5;*/
        }

        if (topDeadSpotAnglePresent) {
            res.topDeadSpotAngle = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
            offset += 2;
        }

        if (bottomDeadSpotAnglePresent) {
            res.bottomDeadSpotAngle = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
            offset += 2;
        }

        if (accumulatedEnergyPresent) {
            res.accumulatedEnergy = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset) * 1000;
        }

        return res;
    }
}
