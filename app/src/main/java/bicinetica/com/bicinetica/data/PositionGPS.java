package bicinetica;



public class PositionGPS {

    private static final float geodesica = 40030000;
    private static final float geodesica_u = geodesica / 360;

    private float latitude, longitude, altitude;
    private float speed, power;
    private float seconds;

    public PositionGPS(float latitude, float longitude) {
        this(latitude, longitude, 0);
    }

    public PositionGPS(float latitude, float longitude, float altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public float getSeconds() {
        return seconds;
    }
    public void setSeconds(float seconds) {
        this.seconds = seconds;
    }

    /***
     * Get the latitude, in degrees.
     * @return
     */
    public float getLatitude() {
        return latitude;
    }
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    /***
     * Get the longitude, in degrees.
     * @return
     */
    public float getLongitude() {
        return longitude;
    }
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    /***
     * Get the altitude if available, in meters above the WGS 84 reference ellipsoid.
     * @return
     */
    public float getAltitude() {
        return altitude;
    }
    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    /***
     * Get the speed if it is available, in meters/second over ground.
     * @return
     */
    public float getSpeed() {
        return speed;
    }
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /***
     * Get the power if it is available, watts.
     * @return
     */
    public float getPower() {
        return power;
    }

    public void setPower(float power) {
        this.power = power;
    }

    public float getDistance(PositionGPS position) {
        float dLat = (position.getLatitude() - this.getLatitude()) * geodesica_u;
        float meanLat = Utilities.average(this.getLatitude(), position.getLatitude());
        float dLon = (float) ((position.getLongitude() - this.getLongitude()) * Math.sin(Math.toRadians(90 - meanLat)) * geodesica_u);
        return (float) Math.sqrt(Math.pow(dLat, 2) + Math.pow(dLon, 2));
    }
}
