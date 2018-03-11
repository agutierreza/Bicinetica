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
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import bicinetica.com.bicinetica.R;
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

public class RealtimeFragment extends Fragment {

    private static final String TAG = RealtimeFragment.class.getSimpleName();

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private static final SimpleDateFormat durationFormat = new SimpleDateFormat("HH:mm:ss");

    private Record record;
    private List<Location> locations = new ArrayList<>();

    private Button buttonStart, buttonStop;

    private TextView power3, power5, power10, powerInst;
    private TextView duration, speed, altitude, rpm;

    private boolean visible = false;
    private boolean running = false;
    private long baseTime;

    private float rpmValue, power;

    private ArrayDeque<Position> buffer = new ArrayDeque<>();
    private Position previousPosition1, previousPosition2;

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (running) {
                duration.setText(durationFormat.format(new Date(SystemClock.elapsedRealtime() - baseTime)));
                duration.postDelayed(tickRunnable, 1000);
            }
        }
    };

    private LocationProvider locationProvider;
    private LocationProvider.LocationListener recordListener = new LocationProvider.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Bundle extras = new Bundle();
            extras.putFloat("power", power);
            extras.putFloat("rpm", rpmValue);
            location.setExtras(extras);
            locations.add(location);

            calculatePower(record.addPosition(location));

            if (visible) {
                updateView(location);
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

        durationFormat.setTimeZone(TimeZone.getTimeZone("GTM"));

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
            duration.post(tickRunnable);
            updateView(locations.get(locations.size() - 1));
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        visible = false;

        if (running) {
            duration.removeCallbacks(tickRunnable);
        }
    }

    @Override
    public void onDestroy() {

        if (running) {
            this.commandStop();
        }

        super.onDestroy();
    }

    private void updateView(Location location) {
        float speedValue = location.getSpeed() * 3.6f;
        speed.setText(speedValue > 0 ? String.format("%.2f", speedValue) : "--");

        long altitudeValue = Math.round(location.getAltitude());
        altitude.setText(altitudeValue > 0 ? String.valueOf(altitudeValue) : "--");

        powerInst.setText(power > 0 ? String.format("%.2f", power) : "--");
        rpm.setText(rpmValue > 0 ? String.format("%.2f", rpmValue) : "--");

        updatePowerMetrics();
    }

    private List<Position> getLastPositions(int n) {
        List<Position> res = new ArrayList<>();

        Object[] arr = buffer.toArray();
        for (int i = arr.length - 1; i >= arr.length - n; i--) {
            res.add((Position)arr[i]);
        }

        return res;
    }

    private void updatePowerMetrics() {
        int currentSize = buffer.size();

        if (currentSize >= 3) {
            float average = Utilities.powerAverage(getLastPositions(3));
            power3.setText(average > 0 ? String.format("%.2f", average) : "--");

            if (currentSize >= 5) {
                average = Utilities.powerAverage(getLastPositions(5));
                power5.setText(average > 0 ? String.format("%.2f", average) : "--");

                if (currentSize >= 10) {
                    average = Utilities.powerAverage(getLastPositions(10));
                    power10.setText(average > 0 ? String.format("%.2f", average) : "--");
                }
            }
        }
    }

    private void addToBuffer(Position position) {
        if (buffer.size() == 10) {
            buffer.remove();
        }
        if (buffer.size() > 0) {
            position.setPower(CyclingOutdoorPower.calculatePower(buffer.peekLast(), position));
        }
        buffer.add(position);
    }

    private void calculatePower(Position position) {
        position = position.clone(); // Create a working copy

        if (position.getAltitude() == 0 && previousPosition1 != null && previousPosition2 != null) {
            Function<Long, Position> interpolation = Utilities.createInterpolation(previousPosition2, previousPosition1);

            Position expected = interpolation.apply(position.getTimestamp());
            position.setAltitude(expected.getAltitude());
            if (position.getSpeed() == 0) {
                position.setSpeed(expected.getSpeed());
            }
        }

        if (previousPosition1 != null) {
            Function<Long, Position> interpolation = Utilities.createInterpolation(previousPosition1, position);

            //Position aux = buffer.peekLast();
            long start = buffer.peekLast().getTimestamp() + 1000;
            long end = position.getTimestamp() + (1000 - position.getTimestamp() % 1000);

            for (long i = start; i <= end; i += 1000) {
                addToBuffer(interpolation.apply(i));
            }
        }
        else {
            addToBuffer(position);
        }

        previousPosition2 = previousPosition1;
        previousPosition1 = position;
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
        locations.clear();

        record = new Record();
        record.setDate(Calendar.getInstance().getTime());
        record.setName("Cycling outdoor");

        locationProvider.registerListener(recordListener);

        running = true;
        baseTime = SystemClock.elapsedRealtime();
        duration.postDelayed(tickRunnable, 1000);
        duration.setText("00:00:00");

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
        duration.removeCallbacks(tickRunnable);

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
        saveRecord(locations, record.getName() + " raw data_" + dateFormat.format(record.getDate()));
    }

    private static void saveRecord(Record record) throws IOException {
        File file = Environment.getExternalStorageDirectory();
        file.mkdirs();
        file = new File(file, String.format("%s_%s.json", record.getName(), dateFormat.format(record.getDate())));

        RecordMapper.save(record, file);
    }

    private static void saveRecord(List<Location> record, String name) throws IOException {
        File file = Environment.getExternalStorageDirectory();
        file.mkdirs();
        file = new File(file, String.format("%s.json", name));

        RecordMapper.save(record, file);
    }
}
