package bicinetica.com.bicinetica.fragments;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import bicinetica.com.bicinetica.R;
import bicinetica.com.bicinetica.data.Position;
import bicinetica.com.bicinetica.data.Record;
import bicinetica.com.bicinetica.data.RecordMapper;
import bicinetica.com.bicinetica.model.CyclingOutdoorPower;
import bicinetica.com.bicinetica.model.LocationProvider;
import bicinetica.com.bicinetica.model.Utilities;

public class RealtimeFragment extends Fragment {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private static final SimpleDateFormat durationFormat = new SimpleDateFormat("HH:mm:ss");

    private Record record;
    private List<Location> locations = new ArrayList<>();

    private Button buttonStart, buttonStop;

    private TextView power3, power5, power10;
    private TextView duration, speed, altitude;

    private boolean visible = false;
    private boolean running = false;
    private long baseTime;

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
        power3 = view.findViewById(R.id.power_3s);
        power5 = view.findViewById(R.id.power_5s);
        power10 = view.findViewById(R.id.power_10s);

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

        updatePowerMetrics();
    }

    private void updatePowerMetrics() {
        int currentSize = record.getPositions().size();

        if (currentSize >= 3) {
            float average = Utilities.powerAverage(record.getLastPositions(3));
            power3.setText(average > 0 ? String.format("%.2f", average) : "--");

            if (currentSize >= 5) {
                average = Utilities.powerAverage(record.getLastPositions(5));
                power5.setText(average > 0 ? String.format("%.2f", average) : "--");

                if (currentSize >= 10) {
                    average = Utilities.powerAverage(record.getLastPositions(10));
                    power10.setText(average > 0 ? String.format("%.2f", average) : "--");
                }
            }
        }
    }

    private void calculatePower(Position position) {

        if (record.getPositions().size() > 1) {
            Position previousPosition = record.getPreviousPosition(position);
            position.setPower(CyclingOutdoorPower.calculatePower(previousPosition, position));
        }
    }

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

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void commandStop() {
        running = false;
        duration.removeCallbacks(tickRunnable);

        locationProvider.unregisterListener(recordListener);

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
