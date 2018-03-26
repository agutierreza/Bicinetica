package bicinetica.com.bicinetica.data;

import android.location.Location;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bicinetica.com.bicinetica.diagnostics.Trace;

public class Record {

    private String name;
    private Date date;
    private ArrayList<Position> positions;

    public Record() {
        positions = new ArrayList<>();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public Position getLastPosition() {
        return positions.isEmpty() ? null : positions.get(positions.size() - 1);
    }

    public float getDistance() {
        float distance = 0;
        for (int i = 0; i < positions.size() - 1; i++) {
            Position p1 = positions.get(i);
            Position p2 = positions.get(i + 1);
            distance += p1.getDistance(p2);
        }
        return distance;
    }

    public Position addPosition(Location location) {
        Trace.start("Record.addPosition");
        if (this.positions.isEmpty()) {
            Trace.verbose("Position list is empty, setting start time to %s", location.getTime());
            this.date = new Date(location.getTime());
        }
        Position position = new Position((float)location.getLatitude(), (float)location.getLongitude(), (float)location.getAltitude());
        position.setSpeed(location.getSpeed());
        position.setTimestamp(location.getTime() - date.getTime());
        positions.add(position);
        Trace.stop("Record.addPosition");
        return position;
    }

    @Override
    public String toString() {
        return String.format("Timestamp: %s, PositionsCount: %s", date.getTime(), positions.size());
    }
}
