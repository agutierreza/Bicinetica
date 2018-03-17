package bicinetica.com.bicinetica;

import org.junit.Test;

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
    public void interpolation_basePoints_isCorrect() throws Exception {

        Position position1 = new Position(36f, -4f, 215f, 5f, 919010);
        Position position2 = new Position(37f, -5f, 230f, 4f, 920990);

        Function<Long, Position> inte = Utilities.createInterpolation(position1, position2);

        assertEquals(position1, inte.apply(919010L));
        assertEquals(position2, inte.apply(920990L));

    }


}