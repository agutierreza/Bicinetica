package bicinetica.com.bicinetica.fragments;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import bicinetica.com.bicinetica.R;
import bicinetica.com.bicinetica.data.Buffer;
import bicinetica.com.bicinetica.data.Position;
import bicinetica.com.bicinetica.data.Record;
import bicinetica.com.bicinetica.data.RecordMapper;
import bicinetica.com.bicinetica.model.CyclingOutdoorPower;
import bicinetica.com.bicinetica.model.Function;
import bicinetica.com.bicinetica.model.LocationProvider;
import bicinetica.com.bicinetica.model.Utilities;
import bicinetica.com.bicinetica.model.bluetooth.BluetoothCpService;
import bicinetica.com.bicinetica.model.bluetooth.BluetoothCscService;
import bicinetica.com.bicinetica.model.bluetooth.BluetoothDevicesManager;
import bicinetica.com.bicinetica.model.bluetooth.GattCommonDescriptors;
import bicinetica.com.bicinetica.model.bluetooth.characteristics.CpMeasurement;
import bicinetica.com.bicinetica.model.bluetooth.characteristics.CscMeasurement;
import bicinetica.com.bicinetica.widgets.ChronometerView;
import bicinetica.com.bicinetica.widgets.NumberView;

public class RealtimeFragment extends Fragment {

    private static final String TAG = RealtimeFragment.class.getSimpleName();

    private static final int INTERPOLATION_STEP = 1000; // 1s

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

    private Record record;

    private Button buttonStart, buttonStop;

    private NumberView power3, power5, power10, powerInst;
    private NumberView speed, altitude, rpm;
    private ChronometerView duration;

    private boolean visible = false;
    private boolean running = false;

    private float rpmValue, power;

    private Buffer<Position> buffer = new Buffer<>(10);
    private Position newPosition, oldPosition;

    private LocationProvider locationProvider;
    private LocationProvider.LocationListener recordListener = new LocationProvider.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            Position position = record.addPosition(location);
            calculatePower(position);

            if (visible) {
                updateView(position);
            }
        }
    };

    private View.OnClickListener startButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            buttonStart.setEnabled(false);

            commandStart();

            buttonStop.setEnabled(true);
        }
    };

    private View.OnClickListener stopButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            buttonStop.setEnabled(false);

            commandStop();

            buttonStart.setEnabled(true);
        }
    };

    public RealtimeFragment() {  }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationProvider = LocationProvider.createProvider(getActivity(), LocationProvider.FUSED_PROVIDER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_realtime, container, false);

        buttonStart = view.findViewById(R.id.button_start);
        buttonStart.setOnClickListener(startButtonListener);
        buttonStop = view.findViewById(R.id.button_stop);
        buttonStop.setOnClickListener(stopButtonListener);

        buttonStop.setEnabled(false);

        duration = view.findViewById(R.id.duration);
        speed = view.findViewById(R.id.speed);
        altitude = view.findViewById(R.id.altitude);
        rpm = view.findViewById(R.id.speed_rpm);
        power3 = view.findViewById(R.id.power_3s);
        power5 = view.findViewById(R.id.power_5s);
        power10 = view.findViewById(R.id.power_10s);
        powerInst = view.findViewById(R.id.instant_power);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        visible = true;

        if (running) {
            updateView(newPosition);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        visible = false;
    }

    @Override
    public void onDestroy() {

        if (running) {
            this.commandStop();
        }

        super.onDestroy();
    }

    private void updateView(Position location) {
        speed.setValue(location.getSpeed() * 3.6f);
        altitude.setValue(Math.round(location.getAltitude()));

        powerInst.setValue(power);
        rpm.setValue(rpmValue);

        updatePowerMetrics();
    }

    private void updatePowerMetrics() {
        int currentSize = buffer.size();

        if (currentSize >= 3) {
            power3.setValue(Utilities.powerAverage(buffer.last(3)));

            if (currentSize >= 5) {
                power5.setValue(Utilities.powerAverage(buffer.last(5)));

                if (currentSize >= 10) {
                    power10.setValue(Utilities.powerAverage(buffer.last(10)));
                }
            }
        }
    }

    private void calculatePower(Position position) {
        position = position.clone(); // Create a working copy

        cleanAltitude(position);

        if (newPosition != null) {
            Function<Long, Position> interpolation = Utilities.createInterpolation(newPosition, position);

            long start = buffer.last().getTimestamp() + INTERPOLATION_STEP;
            long end = position.getTimestamp() + (INTERPOLATION_STEP - position.getTimestamp() % INTERPOLATION_STEP);

            for (long i = start; i <= end; i += 1000) {
                Position lastPosition = buffer.last();
                Position interpolatedPosition = interpolation.apply(i);
                interpolatedPosition.setPower(CyclingOutdoorPower.calculatePower(lastPosition, interpolatedPosition));
                buffer.add(interpolatedPosition);
            }
        }
        else {
            buffer.add(position);
        }

        oldPosition = newPosition;
        newPosition = position;
    }

    /**
     * Calculates expected altitude and speed for positions altitude missing
     * @param position
     */
    private void cleanAltitude(Position position) {
        if (position.getAltitude() == 0 && newPosition != null && oldPosition != null) {
            Function<Long, Position> interpolation = Utilities.createInterpolation(oldPosition, newPosition);

            Position expected = interpolation.apply(position.getTimestamp());
            position.setAltitude(expected.getAltitude());
            if (position.getSpeed() == 0) {
                position.setSpeed(expected.getSpeed());
            }
        }
    }

    private BluetoothGattCallback callback = new BluetoothGattCallback() {

        private CscMeasurement lastCsc;

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "BluetoothDevice CONNECTED: " + gatt.getDevice().getAddress() + " " + gatt.getDevice().getName());
                gatt.discoverServices();
                connections.add(gatt);
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "BluetoothDevice DISCONNECTED");
                if (status == 133) {
                    Log.i(TAG, "Many connections");
                }
                connections.remove(gatt);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, "Services discovered");

            enableNotifications(gatt, BluetoothCscService.SERVICE_UUID, CscMeasurement.CHARACTERISTIC_UUID);
            enableNotifications(gatt, BluetoothCpService.SERVICE_UUID, CpMeasurement.CHARACTERISTIC_UUID);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "notificationreceived!");
            if (characteristic.getUuid().equals(CscMeasurement.CHARACTERISTIC_UUID)) {
                CscMeasurement csc = CscMeasurement.decode(characteristic);
                if (lastCsc != null) {
                    rpmValue = csc.getRpm(lastCsc);
                    rpm.post(new Runnable() {
                        @Override
                        public void run() {
                            rpm.setText(rpmValue > 0 ? String.format("%.2f", rpmValue) : "--");
                        }
                    });
                }
                lastCsc = csc;
            }
            else if (characteristic.getUuid().equals(CpMeasurement.CHARACTERISTIC_UUID)) {
                CpMeasurement cp = CpMeasurement.decode(characteristic);
                power = cp.getInstantaneousPower();
                powerInst.post(new Runnable() {
                    @Override
                    public void run() {
                        powerInst.setText(power > 0 ? String.format("%.2f", power) : "--");
                    }
                });
            }
        }

        private void enableNotifications(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid) {
            BluetoothGattService service =  gatt.getService(serviceUuid);
            if (service != null) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
                if (gatt.setCharacteristicNotification(characteristic, true)) {
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(GattCommonDescriptors.CLIENT_CHARACTERISTIC_CONFIG);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                }
            }
        }
    };

    private List<BluetoothGatt> connections = new ArrayList<>();

    private void commandStart() {
        record = new Record();
        record.setDate(Calendar.getInstance().getTime());
        record.setName("Cycling outdoor");

        locationProvider.registerListener(recordListener);

        running = true;
        duration.restart();

        for (BluetoothDevice device: BluetoothDevicesManager.getInstance().getDevices()) {
            BluetoothGatt gatt = device.connectGatt(getContext(), false, callback);
            if (gatt == null) {
                Log.i(TAG, "Unable to connect GATT server");
            }
            else {
                Log.i(TAG, "Trying to connect GATT server");
            }
        }

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void commandStop() {
        running = false;
        duration.stop();

        locationProvider.unregisterListener(recordListener);

        for (int i = connections.size() - 1; i >= 0; i--) {
            connections.get(i).close();
        }

        try {
            performSave();
        } catch (IOException ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("MAPPER", ex.getMessage());
        }

        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void performSave() throws IOException {
        saveRecord(record);
    }

    private static void saveRecord(Record record) throws IOException {
        File file = Environment.getExternalStorageDirectory();
        file.mkdirs();
        file = new File(file, String.format("%s_%s.json", record.getName(), dateFormat.format(record.getDate())));

        RecordMapper.save(record, file);
    }
}
