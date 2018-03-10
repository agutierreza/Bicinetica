package bicinetica.com.bicinetica;

import org.junit.Test;

import java.util.ArrayList;

import bicinetica.com.bicinetica.data.Position;
import bicinetica.com.bicinetica.data.PositionGPS;
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
    public void interpolation_basePoints_isCorrect() throws Exception {

        PositionGPS position1 = new PositionGPS(36f, -4f, 215f, 5f, 919.01f);
        PositionGPS position2 = new PositionGPS(37f, -5f, 230f, 4f, 920.99f);

        Function<Float, PositionGPS> inte = Utilities.createInterpolation(position1, position2);

        assertEquals(position1, inte.apply(919.01f));
        assertEquals(position2, inte.apply(920.99f));
    }


}