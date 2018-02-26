package bicinetica.com.bicinetica.fragments;


import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import bicinetica.com.bicinetica.R;
import bicinetica.com.bicinetica.data.Record;
import bicinetica.com.bicinetica.model.LocationListener;
import bicinetica.com.bicinetica.model.LocationProvider;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LocationProvider gpsLocationProvider;
    private LocationListener gpsLocationListener;

    private LocationProvider fusedLocationProvider;
    private LocationListener fusedLocationListener;

    private View.OnClickListener beginButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mMap.clear();

            fusedLocationProvider.registerListener(fusedLocationListener);
            gpsLocationProvider.registerListener(gpsLocationListener);
        }
    };
    private View.OnClickListener endButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            fusedLocationProvider.unregisterListener(fusedLocationListener);
            gpsLocationProvider.unregisterListener(gpsLocationListener);
        }
    };

    public MapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationProvider = LocationProvider.createProvider(getActivity(), LocationProvider.FUSED_PROVIDER);
        gpsLocationProvider = LocationProvider.createProvider(getActivity(), LocationProvider.GPS_PROVIDER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mMapFragment = new SupportMapFragment();
        getChildFragmentManager().beginTransaction().add(R.id.map_container, mMapFragment).commit();
        mMapFragment.getMapAsync(this);

        Button buttonBegin = view.findViewById(R.id.begin_map);
        buttonBegin.setOnClickListener(beginButtonListener);
        Button buttonEnd = view.findViewById(R.id.end_map);
        buttonEnd.setOnClickListener(endButtonListener);

        return view;
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

        fusedLocationListener = new LocationPrinterListener(mMap, Color.RED);
        gpsLocationListener = new LocationPrinterListener(mMap, Color.BLUE);
    }
}

class LocationPrinterListener implements LocationListener {
    private GoogleMap mMap;
    private int mColor;
    private ArrayList<LatLng> points;
    private Polyline line;

    public LocationPrinterListener(GoogleMap map, int color) {
        points = new ArrayList<>();
        mColor = color;
        mMap = map;
    }

    @Override
    public void onLocationChanged(Location location) {

        points.add(new LatLng(location.getLatitude(), location.getLongitude()));
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
