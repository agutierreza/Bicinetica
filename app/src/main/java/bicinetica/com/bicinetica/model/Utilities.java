package bicinetica.com.bicinetica.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import bicinetica.com.bicinetica.data.Position;
import bicinetica.com.bicinetica.data.PositionGPS;

public final class Utilities {

    private static final float ALTITUDE_ERROR = 10;

    public static float average(Collection<Float> items) {
        float res = 0;
        for (float item : items) res += item;
        return res / items.size();
    }

    public static float average(float... items) {
        float res = 0;
        for (float item : items) res += item;
        return res / items.length;
    }

    public static float powerAverage(Collection<Position> items) {
        float res = 0;
        for (Position item : items) res += item.getPower();
        return res / items.size();
    }

    /**
     * Gives the best average @seg seconds for powerList
     *
     * @param powerList List of activity power, second by second
     * @param seg The number of seconds to get the best average of
     */
    public static int cpseg(List<Integer> powerList, int seg) {
        int sum = 0, bestSum = 0;
        for (int i = 0; i < seg; i++) {
            sum += powerList.get(i);
        }
        bestSum = sum;
        for (int i = seg; i < powerList.size(); i++) {
            sum += powerList.get(i) - powerList.get(i-seg);
            if (sum > bestSum) {
                bestSum = sum;
            }
        }
        return Math.round((float)bestSum / seg);
    }

    public static ArrayList<Position> interpola(PositionGPS p1, PositionGPS p2) {
        ArrayList<Position> positions = new ArrayList<>();

        //The number of Positions that we are going to add
        int numPositions = (p2.getSeconds() - (int)p2.getSeconds() == 0) ?
                (int)p2.getSeconds() - (int)p1.getSeconds() + 1 : //This never happens anyway!
                (int)p2.getSeconds() - (int)p1.getSeconds() + 2;
        //numPositions = (int)p2.getSeconds() - (int)p1.getSeconds() + 2;

        //line parameters for Lat,Lon,Alt, speed
        float duration = p2.getSeconds()-p1.getSeconds();

        float modLat = (p2.getLatitude() - p1.getLatitude()) / duration;
        float modLon = (p2.getLongitude() - p1.getLongitude()) / duration;
        float modAlt = (p2.getAltitude() - p1.getAltitude()) / duration;
        float modSpd = (p2.getSpeed() - p1.getSpeed()) / duration;

        float bLat = p1.getLatitude() - modLat * p1.getSeconds();
        float bLon = p1.getLongitude() - modLon * p1.getSeconds();
        float bAlt = p1.getAltitude() - modAlt * p1.getSeconds();
        float bSpd = p1.getSpeed() - modSpd * p1.getSpeed();

        //First point using parameters
        int seconds = (int)p1.getSeconds();
        float lat = modLat * seconds + bLat;
        float lon = modLon * seconds + bLon;
        float alt = modAlt * seconds + bAlt;
        float spd = modSpd * seconds + bSpd;
        Position p = new Position(lat, lon, alt, spd, seconds);
        positions.add(p);

        //Adding the rest from the slope module :)
        for (int i = 1; i < numPositions; i++) {
            lat += modLat;
            lon += modLon;
            alt += modAlt;
            spd += modSpd;
            seconds++;
            positions.add(new Position(lat, lon, alt, spd, seconds));
        }

        return positions;
    }

    public static void suavice(List<Position> positions) {
        for (int i = 0; i < positions.size(); i++) {
            suavice(i, positions);
        }
    }

    private static void suavice(int i, List<Position> positions) {
        Position position = positions.get(i);
        Position previous = i > 0 ? positions.get(i - 1) : null;

        if (previous != null && position.getAltitude()  == 0) {
        //if (previous != null && previous.getAltitude() - position.getAltitude() > ALTITUDE_ERROR) {
            int j = getNextValidIndex(i, previous.getAltitude(), positions);

            Position next = j > 0 ? positions.get(j) : null;
            if (next != null) {

                Function<Long, Position> interpolation = Utilities.createInterpolation(previous, next);

                for (int k = i; k < j; k++) {
                    position = positions.get(k);

                    Position expected = interpolation.apply(position.getTimestamp());
                    if (position.getSpeed() == 0) {
                        position.setSpeed(expected.getSpeed());
                    }
                    position.setAltitude(expected.getAltitude());
                }
            }
        }
    }

    private static int getNextValidIndex(int i, float validAltitude, List<Position> positions) {
        for (int j = i; j < positions.size(); j++) {
            if (positions.get(j).getAltitude() != 0) {
            //if (validAltitude - positions.get(j).getAltitude() < ALTITUDE_ERROR) {
                return j;
            }
        }
        return -1;
    }

    public static Function<Long, Position> createInterpolation(final Position p1, final Position p2) {
        return new Function<Long, Position>() {

            InterpolatorLong latitude = new InterpolatorLong(
                    p1.getTimestamp(), p1.getLatitude(),
                    p2.getTimestamp(), p2.getLatitude());
            InterpolatorLong longitude = new InterpolatorLong(
                    p1.getTimestamp(), p1.getLongitude(),
                    p2.getTimestamp(), p2.getLongitude());
            InterpolatorLong altitude = new InterpolatorLong(
                    p1.getTimestamp(), p1.getAltitude(),
                    p2.getTimestamp(), p2.getAltitude());
            InterpolatorLong speed = new InterpolatorLong(
                    p1.getTimestamp(), p1.getSpeed(),
                    p2.getTimestamp(), p2.getSpeed());

            @Override
            public Position apply(Long milliseconds) {

                return new Position(latitude.apply(milliseconds),
                        longitude.apply(milliseconds),
                        altitude.apply(milliseconds),
                        speed.apply(milliseconds),
                        milliseconds);
            }
        };
    }

    public static Function<Float, PositionGPS> createInterpolation(final PositionGPS p1, final PositionGPS p2) {
        return new Function<Float, PositionGPS>() {

            InterpolatorFloat latitude = new InterpolatorFloat(
                    p1.getSeconds(), p1.getLatitude(),
                    p2.getSeconds(), p2.getLatitude());
            InterpolatorFloat longitude = new InterpolatorFloat(
                    p1.getSeconds(), p1.getLongitude(),
                    p2.getSeconds(), p2.getLongitude());
            InterpolatorFloat altitude = new InterpolatorFloat(
                    p1.getSeconds(), p1.getAltitude(),
                    p2.getSeconds(), p2.getAltitude());
            InterpolatorFloat speed = new InterpolatorFloat(
                    p1.getSeconds(), p1.getSpeed(),
                    p2.getSeconds(), p2.getSpeed());

            @Override
            public PositionGPS apply(Float seconds) {

                return new PositionGPS(latitude.apply(seconds),
                        longitude.apply(seconds),
                        altitude.apply(seconds),
                        speed.apply(seconds),
                        seconds);
            }
        };
    }

}

class InterpolatorLong implements Function<Long, Float> {

    private float m, n;

    public InterpolatorLong(long x1, float y1, long x2, float y2) {
        if (x1 >= x2) throw new IllegalArgumentException();

        m = (y2 - y1) / (x2 - x1);
        n = y1 - m * x1;
    }

    @Override
    public Float apply(Long value) {
        return m * value + n;
    }
}

class InterpolatorFloat implements Function<Float, Float> {

    private float m, n;

    public InterpolatorFloat(float x1, float y1, float x2, float y2) {
        if (x1 >= x2) throw new IllegalArgumentException();

        m = (y2 - y1) / (x2 - x1);
        n = y1 - m * x1;
    }

    @Override
    public Float apply(Float value) {
        return m * value + n;
    }
}