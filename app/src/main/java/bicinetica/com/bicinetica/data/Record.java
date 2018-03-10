package bicinetica.com.bicinetica.data;

import android.location.Location;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bicinetica.com.bicinetica.model.Function;
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
        return positions.size() == 0 ? null : positions.get(positions.size() - 1);
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

    public void addPosition(Position position) {
        Position lastPosition = getLastPosition();
        if (lastPosition == null) {
            positions.add(position);
        }
        else if (position.getSeconds() - lastPosition.getSeconds() > 1) {
            Function<Integer, Position> interpolation = Utilities.createInterpolation(lastPosition, position);

            for (int i = lastPosition.getSeconds() + 1; i < position.getSeconds(); i++) {
                positions.add(interpolation.apply(i));
            }
            positions.add(position);
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
}
