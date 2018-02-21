package bicinetica.com.bicinetica.fragments;


import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import bicinetica.com.bicinetica.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private ArrayList<LatLng> pointsProvider;
    private Polyline lineProvider;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private ArrayList<LatLng> pointsApi;
    private Polyline lineApi;

    public MapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pointsApi = new ArrayList<>();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    pointsApi.add(new LatLng(location.getLatitude(), location.getLongitude()));
                }
                if (lineApi != null) {
                    lineApi.remove();
                }
                lineApi = redrawLine(pointsApi, Color.RED);
            }
        };

        pointsProvider = new ArrayList<>();

        locationManager = getLocationManager();
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                pointsProvider.add(new LatLng(location.getLatitude(), location.getLongitude()));
                if (lineProvider != null) {
                    lineProvider.remove();
                }
                lineProvider = redrawLine(pointsProvider, Color.BLUE);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mMapFragment = new SupportMapFragment();
        getChildFragmentManager().beginTransaction().add(R.id.map_container, mMapFragment).commit();
        mMapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);

        // Add a marker in Sydney and move the camera
        LatLng malaga = new LatLng(36.7161622, -4.4233658);
        mMap.addMarker(new MarkerOptions().position(malaga).title("Marker in malaga"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(malaga));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

        mFusedLocationClient.requestLocationUpdates(createLocationRequest(), mLocationCallback,null);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    private LocationManager getLocationManager() {
        return (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
    }

    private Polyline redrawLine(List<LatLng> points, int color){

        PolylineOptions options = new PolylineOptions().width(5).color(color).geodesic(true);
        for (LatLng item : points) {
            options.add(item);
        }

        return mMap.addPolyline(options); //add Polyline
    }

    private LocationRequest createLocationRequest() {
        return new LocationRequest().setInterval(5000).setFastestInterval(5000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
}
