package bicinetica.com.bicinetica.data;

import android.location.Location;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public List<Position> getLastPositions(int n) {
        if (positions.size() < n) {
            throw new RuntimeException();
        }

        return positions.subList(positions.size() - n, positions.size());
    }

    public Position getPreviousPosition(Position position) {
        int i = positions.indexOf(position);

        if (i == -1) {
            throw new RuntimeException();
        }

        return positions.get(i - 1);
    }

    public float getDistance() {
        float distance = 0;
        for (int i = 0; i < positions.size() - 2; i++) {
            Position p1 = positions.get(i);
            Position p2 = positions.get(i + 1);
            distance += p1.getDistance(p2);
        }
        return distance;
    }

    public void addPosition(Position position) {
        positions.add(position);
        /*
        Position lastPosition = getLastPosition();
        if (lastPosition == null) {
            positions.add(position);
        }
        else if (position.getTimestamp() - lastPosition.getTimestamp() > 1000) {
            // TODO: Interpolate
        }
        else {
            positions.add(position);
        }
        */
    }

    public Position addPosition(Location location) {
        if (this.positions.isEmpty()) {
            this.date = new Date(location.getTime());
        }
        Position position = new Position((float)location.getLatitude(), (float)location.getLongitude(), (float)location.getAltitude());
        position.setSpeed(location.getSpeed());
        position.setTimestamp(location.getTime() - date.getTime());
        addPosition(position);
        return position;
    }
}
