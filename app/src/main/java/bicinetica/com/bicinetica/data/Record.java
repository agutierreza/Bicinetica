package bicinetica.com.bicinetica.data;

import android.location.Location;

import java.util.ArrayList;
import java.util.Date;

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

    public ArrayList<Position> getPositions() {
        return positions;
    }

    public void addPosition(Position position) {
        positions.add(position);
    }

    public void addPosition(Location location) {
        Position position = new Position((float)location.getLatitude(), (float)location.getLongitude(), (float)location.getAltitude());
        position.setSpeed(location.getSpeed());
        position.setSeconds((int)((location.getTime() - date.getTime()) / 1000));
        addPosition(position);
    }
}
