package bicinetica.com.bicinetica.model;

import android.app.Activity;
import android.location.Location;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import bicinetica.com.bicinetica.data.Position;
import bicinetica.com.bicinetica.data.Record;

public class LocationProviderMock extends LocationProvider {

    private Record record;
    private List<Position> positions;
    private int currentIndex = 0;

    private Activity activity;
    private LocationListener listener;

    private long delay;

    public LocationProviderMock(Activity activity) {
        this.activity = activity;
        this.delay = 1000;
    }

    public Record getRecord() {
        return record;
    }
    public void setRecord(Record record) {
        this.record = record;
        positions = record.getPositions();
    }

    public long getDelay() {
        return delay;
    }
    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void start() {
        currentIndex = 0;
        record.setDate(Calendar.getInstance().getTime());
        timer.schedule(timerTask, delay, delay);
    }

    @Override
    public void registerListener(LocationListener listener) {
        this.listener = listener;
    }

    private Timer timer = new Timer();
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            activity.runOnUiThread(task);

            currentIndex++;
            if (currentIndex == positions.size()) {
                timer.cancel();
            }
        }
    };

    public Runnable task = new Runnable() {
        @Override
        public void run() {
            Position p = positions.get(currentIndex);
            Location l = new Location("GpsMock");
            l.setTime(p.getTimestamp() + record.getDate().getTime());
            l.setLongitude(p.getLongitude());
            l.setLatitude(p.getLatitude());
            l.setAltitude(p.getAltitude());
            l.setSpeed(p.getSpeed());

            listener.onLocationChanged(l);
        }
    };
}
