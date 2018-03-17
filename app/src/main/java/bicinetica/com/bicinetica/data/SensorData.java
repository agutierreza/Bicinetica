package bicinetica.com.bicinetica.data;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.List;

public class SensorData {
    private String name;
    private String address;
    private List<ServiceData> services;
    private BluetoothDevice device;

    public SensorData() {
        services = new ArrayList<>();
        device = null;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public List<ServiceData> getServices() {
        return services;
    }

    public BluetoothDevice getDevice() {
        return device;
    }
    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof  SensorData) {
            SensorData other = (SensorData)obj;
            return this.address.equals(other.address);
        }
        else  {
            return false;
        }
    }
}
