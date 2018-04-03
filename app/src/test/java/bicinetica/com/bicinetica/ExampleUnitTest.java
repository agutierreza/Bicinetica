package bicinetica.com.bicinetica;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import bicinetica.com.bicinetica.data.Position;
import bicinetica.com.bicinetica.data.Record;
import bicinetica.com.bicinetica.model.CyclingIndoorPower;
import bicinetica.com.bicinetica.model.CyclingOutdoorPower;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void parameterTest() throws Exception {
        //Save the track in a Record
        Record track = readTrack();
        List<Float> distances = getDistances(track);
        float trackDistance = distances.get(distances.size() - 1);

        System.out.println("Comienza nuestro ciclista a pedalear:");

        float speedRodillo = 0;
        float newSpeedRodillo;
        float distanceCyclist = 0;
        float power, speedOutdoor = 0;
        int index = 0;
        int seconds = 0;
        while (distanceCyclist < trackDistance){
            seconds++;

            newSpeedRodillo = getSpeed();

            power = CyclingIndoorPower.calculatePower(speedRodillo, newSpeedRodillo);
            index = findIndex(distanceCyclist, distances);

            Position p1 = track.getPositions().get(index);
            Position p2 = track.getPositions().get(index + 1);

            speedOutdoor = CyclingOutdoorPower.calculateSpeed(power, speedOutdoor, getGrade(p1, p2));
            distanceCyclist += speedOutdoor;

            System.out.printf("Time: %s Distance: %s Position track: %s Speed: %s\n", seconds, distanceCyclist, index, speedOutdoor);
            //System.out.println("Time: "+ seconds + " Distance:" + distanceCyclist + " Position track: " + getIndex(distanceCyclist,track) + " Speed: " + speedOutdoor);
            speedRodillo = newSpeedRodillo;
        }

        //int [] powerArray= new int[power.size()];
        //for (int i=0; i<power.size();i++){
        //powerArray[i]=power.get(i);
        //}
        //float np = Utilities.normPower(powerArray);
        //System.out.printf("NP:%.2f",np);
    }

    @Test
    public void newMethod() throws Exception {
        //Save the track in a Record
        Record track = readTrack();

        List<Float> distances = getDistances(track);
        float totalDistance = distances.get(distances.size() - 1);

        float distance = 0;
        float speedOutdoor = 0;
        float lastSpeed = 0;

        System.out.println("Comienza nuestro ciclista a pedalear:");

        int seconds = 0;
        while (distance < totalDistance) {
            seconds++;
            int index = findIndex(distance, distances);

            Position p1 = track.getPositions().get(index);
            Position p2 = track.getPositions().get(index + 1);

            float speed = getSpeed();
            float power = CyclingIndoorPower.calculatePower(lastSpeed, speed);

            speedOutdoor = CyclingOutdoorPower.calculateSpeed(power, speedOutdoor, getGrade(p1, p2));

            distance += speedOutdoor;
            lastSpeed = speed;

            long time = interpolate(
                    distances.get(index),     p1.getTimestamp(),
                    distances.get(index + 1), p2.getTimestamp(), distance);

            updateMap(interpolate(p1, p2, time));

            System.out.printf("Time: %s Distance: %s Position track: %s Speed: %s\n", time / 1000, distance, index, speedOutdoor);

            //Thread.sleep(1000);
        }

        System.out.println("Total time: " + seconds);
    }

    private void updateMap(Position position) {
        // TODO: Mock of a Google Map update.
    }

    private float getSpeed() {
        // desde el segundo 1 hasta el infinito, y para simplificar las cosas en esta prueba, la velocidad en el rodillo va a ser la misma: 9.3 m/s
        return 9.3f;
    }

    private static List<Float> getDistances(Record track) {
        return getDistances(track.getPositions());
    }

    private static List<Float> getDistances(List<Position> positions) {
        List<Float> res = new ArrayList<>();

        res.add(0f);

        float distance = 0;

        for (int i = 1; i < positions.size(); i++) {
            Position p1 = positions.get(i - 1);
            Position p2 = positions.get(i);
            distance += p1.getDistance(p2);
            res.add(distance);
        }

        return res;
    }

    private Record readTrack() {
        Record track = new Record();
        try{
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("gibralfaro.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strLine;
            long time = 0;
            while ((strLine = br.readLine()) != null)   {
                String [] values = strLine.split(";");
                track.addPosition(Float.parseFloat(values[1]), Float.parseFloat(values[2]), Float.parseFloat(values[0]), time);
                time += 1000;
            }
            br.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return track;
    }

    /***
     * On a sorted list, returns the index of the value
     * which is immediately below to <code>value</code>.
     * @param value
     * @param values
     * @return
     */
    private static int findIndex(float value, List<Float> values) {
        for (int i = 0; i < values.size() - 1; i++) {
            //if (values.get(i) >= value && values.get(i) < value) {
            //if (values.get(i) >= value) {
            if (values.get(i) <= value && values.get(i + 1) > value) {
                return i;
            }
        }
        if (value > values.get(values.size() - 1)) {
            return values.size();
        }
        else {
            return -1;
        }
    }

    public static float getGrade(Position p1, Position p2){
        float hDiff = p2.getAltitude() - p1.getAltitude();
        return hDiff / p2.getDistance(p1);
    }

    public static <T extends Number, V extends Number> V interpolate(T x1, V y1, T x2, V y2, T v) {
        double _x1 = x1.doubleValue();
        double _y1 = y1.doubleValue();
        double _x2 = x2.doubleValue();
        double _y2 = y2.doubleValue();
        if (_x1 >= _x2) throw new IllegalArgumentException();

        final double m = (_y2 - _y1) / (_x2 - _x1);
        final double n = _y1 - m * _x1;

        final boolean isInt = y1 instanceof Integer;
        final boolean isLong = y1 instanceof Long;
        final boolean isFloat = y1 instanceof Float;
        final boolean isDouble = y1 instanceof Double;

        Number res = m * v.doubleValue() + n;

        if (isDouble) {
            return (V)(Number)res.doubleValue();
        }
        else if (isFloat) {
            return (V)(Number)res.floatValue();
        }
        else if (isLong) {
            return (V)(Number)res.longValue();
        }
        else if (isInt) {
            return (V)(Number)res.intValue();
        }
        else {
            throw  new RuntimeException();
        }
    }

    public static Position interpolate(Position p1, Position p2, long milliseconds) {
        float latitude = interpolate(
                p1.getTimestamp(), p1.getLatitude(),
                p2.getTimestamp(), p2.getLatitude(), milliseconds);
        float longitude = interpolate(
                p1.getTimestamp(), p1.getLongitude(),
                p2.getTimestamp(), p2.getLongitude(), milliseconds);
        float altitude = interpolate(
                p1.getTimestamp(), p1.getAltitude(),
                p2.getTimestamp(), p2.getAltitude(), milliseconds);
        float speed = interpolate(
                p1.getTimestamp(), p1.getSpeed(),
                p2.getTimestamp(), p2.getSpeed(), milliseconds);

        return new Position(latitude,
                longitude,
                altitude,
                speed,
                milliseconds);
    }
}


