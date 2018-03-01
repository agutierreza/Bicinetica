package bicinetica.com.bicinetica.fragments;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import bicinetica.com.bicinetica.R;
import bicinetica.com.bicinetica.data.Position;
import bicinetica.com.bicinetica.data.Record;
import bicinetica.com.bicinetica.data.RecordMapper;
import bicinetica.com.bicinetica.model.CyclingOutdoorPower;
import bicinetica.com.bicinetica.model.LocationProvider;
import bicinetica.com.bicinetica.model.Utilities;

public class RealtimeFragment extends Fragment {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

    private float max = 0;

    private Record record;

    private LocationProvider gpsLocationProvider;
    private LocationProvider.LocationListener recordListener = new LocationProvider.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Position position = record.addPosition(location);

            float power = calculatePower(position);
            if (power > max) {
                max = power;
                powerMax.setText(power + " W");
            }

            updatePowerMetrics();
        }
    };

    private float calculatePower(Position position) {
        float power = 0;
        if (record.getPositions().size() > 1) {
            Position previousPosition = record.getPreviousPosition(position);
            power = CyclingOutdoorPower.calculatePower(previousPosition, position);
            position.setPower(power);
        }
        return power;
    }

    private void updatePowerMetrics() {
        int currentSize = record.getPositions().size();

        if (currentSize >= 10) {
            List<Position> positions = record.getLastPositions(10);
            float average = Utilities.powerAverage(positions);
            power10.setText(average + " W");
        }
        if (currentSize >= 5) {
            List<Position> positions = record.getLastPositions(5);
            float average = Utilities.powerAverage(positions);
            power5.setText(average + " W");
        }
        if (currentSize >= 3) {
            List<Position> positions = record.getLastPositions(3);
            float average = Utilities.powerAverage(positions);
            power3.setText(average + " W");
        }
    }

    private Button buttonBegin, buttonEnd;

    private TextView powerMax, power3, power5, power10;

    private View.OnClickListener beginButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            buttonBegin.setEnabled(false);

            record = new Record();
            record.setDate(Calendar.getInstance().getTime());
            record.setName("Cycling outdoor");

            gpsLocationProvider.registerListener(recordListener);

            buttonEnd.setEnabled(true);
        }
    };

    private View.OnClickListener endButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            buttonEnd.setEnabled(false);

            gpsLocationProvider.unregisterListener(recordListener);

            try {
                saveRecord(record);
            } catch (IOException ex) {
                Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("MAPPER", ex.getMessage());
            }

            max = 0;

            buttonBegin.setEnabled(true);
        }
    };

    public RealtimeFragment() {  }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gpsLocationProvider = LocationProvider.createProvider(getActivity(), LocationProvider.GPS_PROVIDER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_realtime, container, false);

        buttonBegin = view.findViewById(R.id.button_begin);
        buttonBegin.setOnClickListener(beginButtonListener);
        buttonEnd = view.findViewById(R.id.button_end);
        buttonEnd.setOnClickListener(endButtonListener);

        buttonEnd.setEnabled(false);

        powerMax = view.findViewById(R.id.power_max);
        power3 = view.findViewById(R.id.power_3_s);
        power5 = view.findViewById(R.id.power_5_s);
        power10 = view.findViewById(R.id.power_10_s);

        return view;
    }

    private static void saveRecord(Record record) throws IOException {
        File file = Environment.getExternalStorageDirectory();
        file.mkdirs();
        file = new File(file, String.format("%s_%s.json", record.getName(), dateFormat.format(record.getDate())));

        RecordMapper.save(record, file);
    }
}
