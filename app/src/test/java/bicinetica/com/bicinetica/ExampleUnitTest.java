package bicinetica.com.bicinetica;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import bicinetica.com.bicinetica.data.Position;
import bicinetica.com.bicinetica.model.Function;
import bicinetica.com.bicinetica.model.Utilities;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test

    public void parameterTest() throws Exception {
        /*
        Position position1 = new Position(36f, -4f, 215f, 5f, 919010);
        Position position2 = new Position(37f, -5f, 230f, 4f, 920990);

        Function<Long, Position> inte = Utilities.createInterpolation(position1, position2);

        assertEquals(position1, inte.apply(919010L));
        assertEquals(position2, inte.apply(920990L));
        */

/*
        ArrayList<Integer> listpower = new ArrayList<>();
        ArrayList<Float> listvel=new ArrayList<>();
        if (power.length!=vel.length) {
            throw new Exception("power&vel different lenght");
        }
        else{
            for (int i=0; i<power.length;i++) {
                listpower.add(power[i]);
                listvel.add(vel[i]);
            }
        }
*/
        try{
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("pairs.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
                //probando...
                System.out.println (strLine);
            }
            br.close();
        }catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }


        //Utilities.estimateIndoorParam(listvel,listpower);

    }

}


