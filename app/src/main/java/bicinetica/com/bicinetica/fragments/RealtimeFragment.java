package bicinetica.com.bicinetica.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import bicinetica.com.bicinetica.R;
import bicinetica.com.bicinetica.data.Buffer;
import bicinetica.com.bicinetica.data.Position;
import bicinetica.com.bicinetica.data.Record;
import bicinetica.com.bicinetica.data.RecordProvider;
import bicinetica.com.bicinetica.data.SensorData;
import bicinetica.com.bicinetica.data.SensorProvider;
import bicinetica.com.bicinetica.data.User;
import bicinetica.com.bicinetica.data.UserMapper;
import bicinetica.com.bicinetica.diagnostics.Trace;
import bicinetica.com.bicinetica.model.CyclingOutdoorPower;
import bicinetica.com.bicinetica.model.Function;
import bicinetica.com.bicinetica.model.LocationProvider;
import bicinetica.com.bicinetica.model.LocationProviderMock;
import bicinetica.com.bicinetica.model.Utilities;
import bicinetica.com.bicinetica.model.bluetooth.BluetoothCpService;
import bicinetica.com.bicinetica.model.bluetooth.BluetoothCscService;
import bicinetica.com.bicinetica.model.bluetooth.GattCommonDescriptors;
import bicinetica.com.bicinetica.model.bluetooth.characteristics.CpMeasurement;
import bicinetica.com.bicinetica.model.bluetooth.characteristics.CscMeasurement;
import bicinetica.com.bicinetica.widgets.ChronometerView;
import bicinetica.com.bicinetica.widgets.NumberView;

public class RealtimeFragment extends Fragment  implements SensorEventListener {

    private static final String TAG = RealtimeFragment.class.getSimpleName();

    private static final int INTERPOLATION_STEP = 1000; // 1s

    private User user;

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

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothGatt> connections = new ArrayList<>();

    private SensorManager sensorManager;
    private Sensor barometer;

    private LocationProvider locationProvider;
    private LocationProvider.LocationListener recordListener = new LocationProvider.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Trace.info("Location received: %s", locationToString(location, record));

            if (barometer != null) {
                location.setAltitude(_altitude);
            }

            Trace.debug("Location after altitude update: %s", locationToString(location, record));

            Position position = record.addPosition(location);

            Trace.debug("Location after include record: %s", position);

            if (!running) return;

            // Create a working copy
            position = position.clone();

            if (position.getAltitude() == 0 && newPosition != null && oldPosition != null) {
                softenAltitude(position);
            }

            if (newPosition != null) {
                interpolatePositions(position);
            }
            else {
                buffer.add(position);
            }

            if (visible) {
                updateView(position);
            }

            oldPosition = newPosition;
            newPosition = position;
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

        //locationProvider = LocationProvider.createProvider(getActivity(), LocationProvider.MOCK_PROVIDER);
        locationProvider = LocationProvider.createProvider(getActivity(), LocationProvider.FUSED_PROVIDER);

        bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        try {
            user = UserMapper.load(new File(Environment.getExternalStorageDirectory(), "kinwatt_user.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    /**
     * Calculates expected altitude and speed for positions with altitude missing.
     * @param position
     */
    private void softenAltitude(Position position) {
        Function<Long, Position> interpolation = Utilities.createInterpolation(oldPosition, newPosition);

        Position expected = interpolation.apply(position.getTimestamp());
        position.setAltitude(expected.getAltitude());
        if (position.getSpeed() == 0) {
            position.setSpeed(expected.getSpeed());
        }
    }

    private void interpolatePositions(Position position) {
        Trace.info("Creating interpolation.");
        Trace.info("Position 1: %s", newPosition);
        Trace.info("Position 2: %s", position);

        Function<Long, Position> interpolation = Utilities.createInterpolation(newPosition, position);

        long start = buffer.last().getTimestamp() + INTERPOLATION_STEP;
        long end = position.getTimestamp() + (INTERPOLATION_STEP - position.getTimestamp() % INTERPOLATION_STEP);

        for (long i = start; i <= end; i += INTERPOLATION_STEP) {
            //TODO: Calculate degrees from 5s ago;
            Position lastPosition = buffer.last();
            Position interpolatedPosition = interpolation.apply(i);

            if (buffer.size() >= 5) {
                Position target = buffer.peek(4);
                float hDiff = interpolatedPosition.getAltitude() - target.getAltitude();
                float grade = hDiff / target.getDistance(interpolatedPosition);

                interpolatedPosition.setPower(CyclingOutdoorPower.calculatePower(lastPosition, interpolatedPosition, grade));
            }
            else {
                interpolatedPosition.setPower(CyclingOutdoorPower.calculatePower(lastPosition, interpolatedPosition));
            }

            buffer.add(interpolatedPosition);
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
            Log.i(TAG, "Notification received");
            if (characteristic.getUuid().equals(CscMeasurement.CHARACTERISTIC_UUID)) {
                CscMeasurement csc = CscMeasurement.decode(characteristic);
                if (lastCsc != null) {
                    rpmValue = csc.getRpm(lastCsc);
                    rpm.post(new Runnable() {
                        @Override
                        public void run() {
                            rpm.setValue(rpmValue);
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
                        powerInst.setValue(power);
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

    private void commandStart() {
        Trace.start("Recording");

        newPosition = null;
        oldPosition = null;
        buffer.clear();

        record = new Record();
        record.setDate(Calendar.getInstance().getTime());
        record.setName("Cycling outdoor");

        running = true;
        duration.restart();

        connectSensors();

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void commandStop() {
        running = false;
        duration.stop();

        disconnectSensors();

        try {
            RecordProvider.getInstance().add(record);
        } catch (RuntimeException ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("MAPPER", ex.getMessage());
        }

        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Trace.stop("Recording");
    }

    private void connectSensors() {
        locationProvider.registerListener(recordListener);

        if (barometer != null) {
            sensorManager.registerListener(this, barometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        for (SensorData data : SensorProvider.getInstance().getAll()) {
            BluetoothDevice device = data.getDevice();
            if (device == null) {
                device = bluetoothAdapter.getRemoteDevice(data.getAddress());
                data.setDevice(device);
            }
            BluetoothGatt gatt = device.connectGatt(getContext(), false, callback);
            if (gatt == null) {
                Log.i(TAG, "Unable to connect GATT server");
            }
            else {
                Log.i(TAG, "Trying to connect GATT server");
            }
        }

        if (locationProvider instanceof LocationProviderMock) {
            LocationProviderMock mock = ((LocationProviderMock)locationProvider);
            try {
                mock.setRecord(RecordProvider.getInstance().load(0));
                mock.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void disconnectSensors() {
        locationProvider.unregisterListener(recordListener);

        if (barometer != null) {
            sensorManager.unregisterListener(this);
        }

        for (int i = connections.size() - 1; i >= 0; i--) {
            connections.get(i).close();
            connections.remove(i);
        }
    }

    private float _altitude = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {
        _altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, event.values[0]);
        altitude.setValue(_altitude);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private static String locationToString(Location location, Record record) {
        return String.format("[%s, %s] Timestamp: %s, Altitude: %s , Speed: %s",
                location.getLatitude(),
                location.getLongitude(),
                location.getTime() - record.getDate().getTime(),
                location.getAltitude(),
                location.getSpeed());
    }
}
