package bicinetica.com.bicinetica.model;

import android.location.Location;

import bicinetica.com.bicinetica.data.Position;
import bicinetica.com.bicinetica.data.Record;

public class LocationProviderMock extends LocationProvider {

    private Record record;

    public LocationProviderMock(Record record) {
        this.record = record;
    }

    @Override
    public void registerListener(LocationListener listener) {
        for (Position p : record.getPositions()) {
            Location l = new Location("GpsMock");
            l.setTime(p.getTimestamp() + record.getDate().getTime());
            l.setLongitude(p.getLongitude());
            l.setLatitude(p.getLatitude());
            l.setAltitude(p.getAltitude());
            l.setSpeed(p.getSpeed());
            listener.onLocationChanged(l);
        }
    }
}
