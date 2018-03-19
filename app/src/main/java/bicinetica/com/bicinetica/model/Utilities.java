package bicinetica.com.bicinetica.model;


import java.util.Collection;
import java.util.List;

import bicinetica.com.bicinetica.data.Position;

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

    public static float angularVelocity(float rpm) {
        return (float)(2 * Math.PI * rpm / 60);
    }

    public static float linealVelocity(float rpm, float radious) {
        return radious * angularVelocity(rpm);
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