package bicinetica.com.bicinetica.model.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

public class CscMeasurement {
    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("00002A5B-0000-1000-8000-00805F9B34FB");

    private int wheelRevolutions;
    private float wheelRevolutionsEventTime;

    private int crankRevolutions;
    private float crankRevolutionsEventTime;

    public CscMeasurement() { }

    public int getWheelRevolutions() {
        return wheelRevolutions;
    }
    public void setWheelRevolutions(int wheelRevolutions) {
        this.wheelRevolutions = wheelRevolutions;
    }

    public float getWheelRevolutionsEventTime() {
        return wheelRevolutionsEventTime;
    }
    public void setWheelRevolutionsEventTime(float wheelRevolutionsEventTime) {
        this.wheelRevolutionsEventTime = wheelRevolutionsEventTime;
    }

    public int getCrankRevolutions() {
        return crankRevolutions;
    }
    public void setCrankRevolutions(int crankRevolutions) {
        this.crankRevolutions = crankRevolutions;
    }

    public float getCrankRevolutionsEventTime() {
        return crankRevolutionsEventTime;
    }
    public void setCrankRevolutionsEventTime(float crankRevolutionsEventTime) {
        this.crankRevolutionsEventTime = crankRevolutionsEventTime;
    }

    public float getRpm(CscMeasurement csc) {
        float dt = this.wheelRevolutionsEventTime - csc.wheelRevolutionsEventTime;
        float dr = this.wheelRevolutions - csc.wheelRevolutions;

        return Math.abs(dr / dt * 60);
    }

    @Override
    public String toString() {
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
        return sb.toString();
    }

    public static CscMeasurement create(BluetoothGattCharacteristic characteristic) {
        CscMeasurement res = new CscMeasurement();

        int offset = 0;

        int flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);

        boolean c1 = (flags & 1) == 1;
        boolean c2 = (flags & 2) == 2;

        offset++;

        if (c1) {
            res.wheelRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, offset);
            offset += 4;
            int eventTime = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
            res.wheelRevolutionsEventTime = (float)eventTime / 1024;
            offset += 2;
        }

        if (c2) {
            res.crankRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, offset);
            offset += 4;
            int eventTime = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
            res.crankRevolutionsEventTime = (float)eventTime / 1024;
        }

        return res;
    }
}
