package bicinetica.com.bicinetica.fragments;


import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import bicinetica.com.bicinetica.R;
import bicinetica.com.bicinetica.data.Record;
import bicinetica.com.bicinetica.data.RecordMapper;
import bicinetica.com.bicinetica.model.LocationListener;
import bicinetica.com.bicinetica.model.LocationProvider;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

    private Record record;

    private GoogleMap mMap;

    private LocationProvider gpsLocationProvider;
    private LocationPrinterListener gpsLocationListener;

    //private LocationProvider fusedLocationProvider;
    //private LocationPrinterListener fusedLocationListener;

    private LocationListener recordListener;

    private Button buttonBegin, buttonEnd;

    private View.OnClickListener beginButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mMap.clear();

            record = new Record();
            record.setDate(Calendar.getInstance().getTime());
            record.setName("Cycling outdoor");

            recordListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    record.addPosition(location);
                }
            };

            //fusedLocationProvider.registerListener(fusedLocationListener);
            gpsLocationProvider.registerListener(gpsLocationListener);
            gpsLocationProvider.registerListener(recordListener);

            buttonBegin.setEnabled(false);
            buttonEnd.setEnabled(true);
        }
    };
    private View.OnClickListener endButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            //fusedLocationProvider.unregisterListener(fusedLocationListener);
            gpsLocationProvider.unregisterListener(gpsLocationListener);
            gpsLocationProvider.unregisterListener(recordListener);

            try {
                saveRecord(record);
            } catch (IOException ex) {
                Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("MAPPER", ex.getMessage());
            }

            gpsLocationListener.clearPoints();

            buttonBegin.setEnabled(true);
            buttonEnd.setEnabled(false);
        }
    };

    public MapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //fusedLocationProvider = LocationProvider.createProvider(getActivity(), LocationProvider.FUSED_PROVIDER);
        gpsLocationProvider = LocationProvider.createProvider(getActivity(), LocationProvider.GPS_PROVIDER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mMapFragment = new SupportMapFragment();
        getChildFragmentManager().beginTransaction().add(R.id.map_container, mMapFragment).commit();
        mMapFragment.getMapAsync(this);

        buttonBegin = view.findViewById(R.id.begin_map);
        buttonBegin.setOnClickListener(beginButtonListener);
        buttonEnd = view.findViewById(R.id.end_map);
        buttonEnd.setOnClickListener(endButtonListener);

        buttonEnd.setEnabled(false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (gpsLocationListener != null) {
            gpsLocationListener.setAllowPrint(true);
            gpsLocationListener.printRoute();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (gpsLocationListener != null) {
            gpsLocationListener.setAllowPrint(false);
        }
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);

        LatLng malaga = new LatLng(36.7161622, -4.4233658);
        //mMap.addMarker(new MarkerOptions().position(malaga).title("Marker in malaga"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(malaga));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

        //fusedLocationListener = new LocationPrinterListener(mMap, Color.RED);
        gpsLocationListener = new LocationPrinterListener(mMap, Color.BLUE);
    }

    private static void saveRecord(Record record) throws IOException {
        File file = Environment.getExternalStorageDirectory();
        file.mkdirs();
        file = new File(file, String.format("%s_%s.json", record.getName(), dateFormat.format(record.getDate())));

        RecordMapper.save(record, file);
    }
}

class LocationPrinterListener implements LocationListener {
    private GoogleMap mMap;
    private int mColor;
    private ArrayList<LatLng> points;
    private Polyline line;

    private boolean allowPrint;

    public LocationPrinterListener(GoogleMap map, int color) {
        points = new ArrayList<>();
        mColor = color;
        mMap = map;

        allowPrint = true;
    }

    public void setAllowPrint(boolean allowPrint) {
        this.allowPrint = allowPrint;
    }

    public void clearPoints() {
        points.clear();
    }

    @Override
    public void onLocationChanged(Location location) {

        points.add(new LatLng(location.getLatitude(), location.getLongitude()));

        if (allowPrint) {
            printRoute();
        }
    }

    public void printRoute() {
        if (line != null) {
            line.remove();
        }

        PolylineOptions options = new PolylineOptions().width(5).color(mColor).geodesic(true);
        for (LatLng item : points) {
            options.add(item);
        }
        line = mMap.addPolyline(options);
    }
}
