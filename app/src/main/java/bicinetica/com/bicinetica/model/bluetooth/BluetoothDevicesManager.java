package bicinetica.com.bicinetica.model.bluetooth;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paco on 11/03/2018.
 */

public class BluetoothDevicesManager {

    private List<BluetoothDevice> devices = new ArrayList<>();

    public static BluetoothDevicesManager instance;

    public static BluetoothDevicesManager getInstance() {
        if (instance == null) {
            instance = new BluetoothDevicesManager();
        }
        return instance;
    }

    private BluetoothDevicesManager() {

    }

    public List<BluetoothDevice> getDevices() {
        return devices;
    }
}
