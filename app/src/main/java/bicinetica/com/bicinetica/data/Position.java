package bicinetica.com.bicinetica.data;

import bicinetica.com.bicinetica.model.Utilities;

public class Position {

    private static final float geodesica = 40030000;
    private static final float geodesica_u = geodesica / 360;

    private float latitude, longitude, altitude;
    private float speed;
    private int seconds;

    public Position(float latitude, float longitude) {
        this(latitude, longitude, 0);
    }

    public Position(float latitude, float longitude, float altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public int getSeconds() {
        return seconds;
    }
    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public float getLatitude() {
        return latitude;
    }
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getAltitude() {
        return altitude;
    }
    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    public float getSpeed() {
        return speed;
    }
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getDistance(Position position) {
        float dLat = (position.getLatitude() - this.getLatitude()) * geodesica_u;
        float meanLat = Utilities.average(this.getLatitude(), position.getLatitude());
        float dLon = (float) ((position.getLongitude() - this.getLongitude()) * Math.sin(Math.toRadians(90 - meanLat)) * geodesica_u);
        return (float) Math.sqrt(Math.pow(dLat, 2) + Math.pow(dLon, 2));
    }
}
