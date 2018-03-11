package bicinetica.com.bicinetica;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import bicinetica.com.bicinetica.data.Position;
import bicinetica.com.bicinetica.data.Record;
import bicinetica.com.bicinetica.data.RecordMapper;
import bicinetica.com.bicinetica.fragments.RecordFragment;

public class RecordSummary extends AppCompatActivity implements OnMapReadyCallback {

    private static final SimpleDateFormat durationFormat = new SimpleDateFormat("HH:mm:ss");

    private GoogleMap mMap;
    private Record record;

    private TextView duration, distance;
    private LineChart mChart;

    private String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_summary);

        durationFormat.setTimeZone(TimeZone.getTimeZone("GTM"));

        duration = findViewById(R.id.duration);
        distance = findViewById(R.id.distance);

        Intent intent = getIntent();
        filename = intent.getStringExtra("file_name");

        try {
            record = RecordMapper.load(filename);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        float distanceValue = record.getDistance() / 1000;
        distance.setText(distanceValue > 0 ? String.format("%.2f km", distanceValue) : "--");

        duration.setText(durationFormat.format(new Date(record.getLastPosition().getTimestamp())));


        mChart = findViewById(R.id.chart1);

        mChart.setDrawGridBackground(false);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        //mChart.setPinchZoom(true);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return durationFormat.format(new Date((long) value));
            }
        });
        xAxis.enableGridDashedLine(10f, 10f, 0f);

        updateData();

        SupportMapFragment mMapFragment = new SupportMapFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.map_container, mMapFragment).commit();
        mMapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delete, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete) {
            File f = new File(filename);
            f.delete();
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void updateData() {
        for (Position position : record.getPositions()) {
            updateData(position);
        }
    }

    private Position lastPosition;
    private float offset;

    private void updateData(Position position)
    {
        LineDataSet speed, altitude, distance;

        if (mChart.getData() == null)
        {
            speed = new LineDataSet(new ArrayList<Entry>(), "Speed (km/h)");
            speed.setDrawCircles(false);
            speed.setColor(Color.BLUE);
            speed.setAxisDependency(YAxis.AxisDependency.LEFT);
            speed.setDrawValues(false);
            speed.setLineWidth(1f);
            speed.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));

            altitude = new LineDataSet(new ArrayList<Entry>(), "Altitude (m)");
            altitude.setDrawCircles(false);
            altitude.setColor(Color.GREEN);
            altitude.setAxisDependency(YAxis.AxisDependency.RIGHT);
            altitude.setDrawValues(false);
            altitude.setLineWidth(1f);
            altitude.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            altitude.setDrawFilled(true);

            /*
            distance = new LineDataSet(new ArrayList<Entry>(), "Distance (m)");
            distance.setDrawCircles(false);
            distance.setColor(Color.RED);
            distance.setAxisDependency(YAxis.AxisDependency.RIGHT);
            distance.setDrawValues(false);
            distance.setLineWidth(1f);
            distance.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            */

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(speed);
            dataSets.add(altitude);
            //dataSets.add(distance);

            LineData line = new LineData(dataSets);

            mChart.setData(line);
        }
        else
        {
            speed = (LineDataSet)mChart.getData().getDataSetByIndex(0);
            altitude = (LineDataSet)mChart.getData().getDataSetByIndex(1);
            //distance = (LineDataSet)mChart.getData().getDataSetByIndex(2);
        }

        if (lastPosition != null) {
            offset += lastPosition.getDistance(position);
        }

        speed.addEntry(new Entry(position.getTimestamp(), position.getSpeed() * 3.6f));
        altitude.addEntry(new Entry(position.getTimestamp(), Math.max(position.getAltitude(), 0)));
        //distance.addEntry(new Entry(data.getTimestamp(), offset));

        lastPosition = position;

        mChart.getData().notifyDataChanged();
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }
}
