package bicinetica.com.bicinetica;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        Record track = new Record();
        try{
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("gibralfaro.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
                String [] values = strLine.split(";");
                track.addPosition(Float.parseFloat(values[1]),Float.parseFloat(values[2]),Float.parseFloat(values[0]));
            }
            br.close();
        }catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        // I added a field in Record that stores the total distance for each node-position. Here they are calculated and stored
        float totalDist=0;
        track.addTotalDistance(0);
        for (int i=1; i<track.getPositions().size(); i++){
            totalDist += track.getPositions().get(i).getDistance(track.getPositions().get(i-1));
            track.addTotalDistance(totalDist);
            //System.out.println("Pos: " + i + " Dist: " + track.getTotalDistances().get(i));
        }
        float trackDistance = track.getTrackDistance();

        System.out.println("Comienza nuestro ciclista a pedalear:");
        float speedRodillo = 0;
        float newSpeedRodillo;
        float distanceCyclist = 0;
        float power, speedOutdoor = 0;
        int index = 0;
        int seconds = 0;
        while  (distanceCyclist < trackDistance){
            seconds += 1;
            newSpeedRodillo = 9.3f;//desde el segundo 1 hasta el infinito, y para simplificar las cosas en esta prueba, la velocidad en el rodillo va a ser la misma: 9.3 m/s
            power = CyclingIndoorPower.calculatePower(speedRodillo, newSpeedRodillo);
            index = getIndex(distanceCyclist,track);
            speedOutdoor = CyclingOutdoorPower.calculateSpeed(power,speedOutdoor,getGrade(index,track));
            distanceCyclist+=speedOutdoor;
            System.out.println("Time: "+ seconds + " Distance:" + distanceCyclist + " Position track: " + getIndex(distanceCyclist,track) + " Speed: " + speedOutdoor);
            speedRodillo = newSpeedRodillo;
        }

        //int [] powerArray= new int[power.size()];
        //for (int i=0; i<power.size();i++){
            //powerArray[i]=power.get(i);
        //}
        //float np = Utilities.normPower(powerArray);
        //System.out.printf("NP:%.2f",np);
    }

    public int getIndex(float distance, Record record){
        int pos = 0;
        while (pos < record.getTotalDistances().size()-1 && record.getTotalDistances().get(pos) <= distance){
            pos++;
        }
        return pos - 1;
    }

    public float getGrade(int index, Record record){
        float hDiff = record.getPositions().get(index + 1).getAltitude() - record.getPositions().get(index).getAltitude();
        float grade = hDiff / record.getPositions().get(index + 1).getDistance(record.getPositions().get(index));
        return grade;
    }

}


