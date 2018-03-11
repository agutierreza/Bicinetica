package bicinetica.com.bicinetica;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import bicinetica.com.bicinetica.data.Position;
import bicinetica.com.bicinetica.data.Record;
import bicinetica.com.bicinetica.data.RecordMapper;

public class RecordSummary extends AppCompatActivity implements OnMapReadyCallback {

    private static final SimpleDateFormat durationFormat = new SimpleDateFormat("HH:mm:ss");

    private GoogleMap mMap;
    private Record record;

    private TextView duration, distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_summary);

        durationFormat.setTimeZone(TimeZone.getTimeZone("GTM"));

        duration = findViewById(R.id.duration);
        distance = findViewById(R.id.distance);

        Intent intent = getIntent();
        String filename = intent.getStringExtra("file_name");

        try {
            record = RecordMapper.load(filename);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        float distanceValue = record.getDistance() / 1000;
        distance.setText(distanceValue > 0 ? String.format("%.2f km", distanceValue) : "--");

        duration.setText(durationFormat.format(new Date(record.getLastPosition().getTimestamp())));

        SupportMapFragment mMapFragment = new SupportMapFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.map_container, mMapFragment).commit();
        mMapFragment.getMapAsync(this);
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Position firstPosition = record.getPositions().get(0);
        LatLng initial = new LatLng(firstPosition.getLatitude(), firstPosition.getLongitude());

        Position lastPosition = record.getLastPosition();
        LatLng end = new LatLng(lastPosition.getLatitude(), lastPosition.getLongitude());

        mMap.addMarker(new MarkerOptions().position(initial).title("Start"));
        mMap.addMarker(new MarkerOptions().position(end).title("End"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(initial));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

        PolylineOptions options = new PolylineOptions().width(15).color(Color.BLUE).geodesic(true);
        for (Position position: record.getPositions()) {
            options.add(new LatLng(position.getLatitude(), position.getLongitude()));
        }

        Polyline line = mMap.addPolyline(options);
    }
}
