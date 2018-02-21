package bicinetica.com.bicinetica.model;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class LocationProvider {
    public static final int GPS_PROVIDER = 0;
    public static final int NETWORK_PROVIDER = 1;
    public static final int FUSED_PROVIDER = 2;

    protected ArrayList<LocationListener> mListeners;

    protected LocationProvider() {
        mListeners = new ArrayList<>();
    }

    public void registerListener(LocationListener listener) {
        mListeners.add(listener);
    }

    public void unregisterListener(LocationListener listener) {
        mListeners.remove(listener);
    }

    public static LocationProvider createProvider(Context context, int providerType)
    {
        switch (providerType) {
            case GPS_PROVIDER:
                return new GpsLocationProvider(context);
            case NETWORK_PROVIDER:
                return new NetworkLocationProvider(context);
            case FUSED_PROVIDER:
                return new FusedLocationProvider(context);
        }
        throw new RuntimeException();
    }
}

class SystemLocationProvider extends LocationProvider {

    private android.location.LocationListener locationListener;
    private LocationManager locationManager;
    private boolean listening = false;
    private String mProvider;

    public SystemLocationProvider(Context context, String provider) {
        mProvider = provider;

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new android.location.LocationListener() {
            public void onLocationChanged(Location location) {
                for (LocationListener listener: mListeners) {
                    listener.onLocationChanged(location);
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void registerListener(LocationListener listener) {
        super.registerListener(listener);
        if (!listening) {
            locationManager.requestLocationUpdates(mProvider, 0, 0, locationListener);
            listening = true;
        }
    }

    @Override
    public void unregisterListener(LocationListener listener) {
        super.unregisterListener(listener);
        if (mListeners.size() == 0) {
            locationManager.removeUpdates(locationListener);
            listening = false;
        }
    }
}

class GpsLocationProvider  extends SystemLocationProvider {
    public GpsLocationProvider(Context context) {
        super(context, LocationManager.GPS_PROVIDER);
    }
}

class NetworkLocationProvider  extends SystemLocationProvider {
    public NetworkLocationProvider(Context context) {
        super(context, LocationManager.NETWORK_PROVIDER);
    }
}

class FusedLocationProvider extends LocationProvider {

    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean listening = false;

    public FusedLocationProvider(Context context) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    for (LocationListener listener: mListeners) {
                        listener.onLocationChanged(location);
                    }
                }
            }
        };
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void registerListener(LocationListener listener) {
        super.registerListener(listener);
        if (!listening) {
            mFusedLocationClient.requestLocationUpdates(createLocationRequest(), mLocationCallback,null);
            listening = true;
        }
    }

    @Override
    public void unregisterListener(LocationListener listener) {
        super.unregisterListener(listener);
        if (mListeners.size() == 0) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            listening = false;
        }
    }

    private LocationRequest createLocationRequest() {
        return new LocationRequest().setInterval(5000).setFastestInterval(5000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
}