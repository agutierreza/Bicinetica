package bicinetica.com.bicinetica.data;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bicinetica.com.bicinetica.model.Utilities;

public class Record {

    private int id;
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

    public int getId() {
        return id;
    }
    public void setId(int id)  {
        this.id = id;
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
        if (positions.size() == 0) {
            return null;
        }
        else {
            return positions.get(positions.size() - 1);
        }
    }

    public List<Position> getLastPositions(int n) {
        if (positions.size() < n) {
            throw new RuntimeException();
        }
        else {
            return positions.subList(positions.size() - n, positions.size());
        }
    }

    public Position getPreviousPosition(Position position) {

        int i = positions.indexOf(position);

        if (i == -1) {
            throw new RuntimeException();
        }

        return positions.get(i - 1);
    }

    public void addPosition(Position position) {
        Position lastPosition = getLastPosition();
        if (lastPosition == null) {
            positions.add(position);
        }
        else if (position.getSeconds() - lastPosition.getSeconds() > 1) {
            positions.addAll(interpolate(lastPosition, position));
        }
        else {
            positions.add(position);
        }
    }

    public Position addPosition(Location location) {
        Position position = new Position((float)location.getLatitude(), (float)location.getLongitude(), (float)location.getAltitude());
        position.setSpeed(location.getSpeed());
        position.setSeconds((int)((location.getTime() - date.getTime()) / 1000));
        addPosition(position);
        return position;
    }

    private static ArrayList<Position> interpolate(Position p1, Position p2) {
        return interpolate(p1, p2,  p2.getSeconds() - p1.getSeconds());
    }

    private static ArrayList<Position> interpolate(Position p1, Position p2, int n) {

        ArrayList<Float> latitudeInterpolation = Utilities.linealInterpolation(p1.getLatitude(), p2.getLatitude(), n);
        ArrayList<Float> longitudeInterpolation = Utilities.linealInterpolation(p1.getLongitude(), p2.getLongitude(), n);
        ArrayList<Float> altitudeInterpolation = Utilities.linealInterpolation(p1.getAltitude(), p2.getAltitude(), n);
        ArrayList<Float> speedInterpolation = Utilities.linealInterpolation(p1.getSpeed(), p2.getSpeed(), n);

        int timeOffset = p1.getSeconds();

        ArrayList<Position> res = new ArrayList<>();

        for (int i = 1; i < n; i++) {
            Position position = new Position(latitudeInterpolation.get(i), longitudeInterpolation.get(i), altitudeInterpolation.get(i));
            position.setSpeed(speedInterpolation.get(i));
            position.setSeconds(timeOffset + i);
            res.add(position);
        }

        res.add(p2);

        return res;
    }
}
