package bicinetica.com.bicinetica.model.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;


public class CpFeature {

    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("00002A65-0000-1000-8000-00805F9B34FB");

    private boolean pedalPowerBalanceSupported, accumulatedPowerBalanceSupported,
            wheelRevolutionDataSupported, crankRevolutionDataSupported,
            extremeMagnitudesSupported, extremeAnglesSupported,
            topAndBottomDeadSpotAnglesSupported,
            accumulatedEnergySupported,
            offsetCompensationIndicatorSupported, offsetCompensationSupported,
            cyclingPowerMeasurementCharacteristicContentMaskingSupported, multipleSensorLocationsSupported,
            crankLenghtAdjustmentSupported,
            chainLengthAdjustmentSupported, chainWeightAdjustmentSupported,
            spanLengthAdjustmentSupported;

    private SensorContext sensorMeasurementContext;

    private boolean instantaneousMeasurementDirectionSupported, factoryCalibrationDateSupported, enhancedOffsetCompensationSupported;

    private DistributedSystemSupport distributedSystemSupport;

    public CpFeature() { }

    public static CpFeature decode(BluetoothGattCharacteristic characteristic) {
        CpFeature res = new CpFeature();

        int flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);

        res.pedalPowerBalanceSupported = flagCheck(flags, 1);
        res.accumulatedPowerBalanceSupported = flagCheck(flags, 2);
        res.wheelRevolutionDataSupported = flagCheck(flags, 4);
        res.crankRevolutionDataSupported = flagCheck(flags, 8);
        res.extremeMagnitudesSupported =flagCheck(flags, 16);
        res.extremeAnglesSupported = flagCheck(flags, 32);
        res.topAndBottomDeadSpotAnglesSupported = flagCheck(flags, 64);
        res.accumulatedEnergySupported = flagCheck(flags, 128);
        res.offsetCompensationIndicatorSupported = flagCheck(flags, 256);
        res.offsetCompensationSupported = flagCheck(flags, 512);
        res.cyclingPowerMeasurementCharacteristicContentMaskingSupported = flagCheck(flags, 1024);
        res.multipleSensorLocationsSupported = flagCheck(flags, 2048);
        res.crankLenghtAdjustmentSupported = flagCheck(flags, 4096);
        res.chainLengthAdjustmentSupported = flagCheck(flags, 8192);
        res.chainWeightAdjustmentSupported = flagCheck(flags, 16384);
        res.spanLengthAdjustmentSupported = flagCheck(flags, 32768);

        res.sensorMeasurementContext = flagCheck(flags, 65536) ? SensorContext.TorqueBased : SensorContext.ForceBased;

        res.instantaneousMeasurementDirectionSupported = flagCheck(flags, 131072);
        res.factoryCalibrationDateSupported = flagCheck(flags, 262144);
        res.enhancedOffsetCompensationSupported = flagCheck(flags, 524288);

        return res;
    }

    protected static boolean flagCheck(int flag, int value) {
        return (flag & value) == value;
    }

    public enum SensorContext {
        ForceBased,
        TorqueBased
    }

    public enum DistributedSystemSupport {
        Unspecified, //legacy sensor
        Enabled,
        Disabled
    }
}
