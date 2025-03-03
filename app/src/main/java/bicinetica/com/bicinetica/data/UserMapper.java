package bicinetica.com.bicinetica.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Scanner;

public class UserMapper {
    private static final String NAME_TAG = "name";
    private static final String AGE_TAG = "age";
    private static final String WEIGHT_TAG = "weight";
    private static final String HEIGHT_TAG = "height";

    private static final String BIKES_TAG = "bikes";
    private static final String BIKE_TYPE_TAG = "bikeType";

    public static void save(User user, String filePath) throws IOException {
        save(user, new File(filePath));
    }

    public static void save(User user, File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter writer = new FileWriter(file);
        save(user, writer);
        writer.close();
    }

    public static void save(User user, Writer streamWriter) throws IOException {
        try {
            JSONObject json = createJson(user);
            streamWriter.write(json.toString());
        } catch (JSONException e) {
            throw new IOException("Unable to create JSON format.", e);
        }
    }

    private static JSONObject createJson(User user) throws JSONException {
        JSONObject base = new JSONObject();

        base.put(NAME_TAG, user.getName())
                .put(AGE_TAG, user.getAge())
                .put(WEIGHT_TAG, user.getWeight())
                .put(HEIGHT_TAG, user.getHeight());

        JSONArray bikes = new JSONArray();
        for (Bike bike : user.getBikes()) {
            bikes.put(new JSONObject()
                    .put(BIKE_TYPE_TAG, bike.getType().toString())
                    .put(WEIGHT_TAG, bike.getWeight()));
        }
        base.put(BIKES_TAG, bikes);

        return base;
    }

    public static User load(File file) throws IOException {
        FileReader reader = new FileReader(file);
        User res = load(reader);
        reader.close();
        return res;
    }

    public static User load(InputStream stream) throws IOException {
        InputStreamReader reader = new InputStreamReader(stream);
        User res = load(reader);
        reader.close();
        return res;
    }

    public static User load(Reader reader) throws IOException {
        User user = new User();

        try {
            JSONObject object = new JSONObject(convertStreamToString(reader));

            user.setName(object.getString(NAME_TAG));
            user.setAge(object.getInt(AGE_TAG));
            user.setWeight((float) object.getDouble(WEIGHT_TAG));
            user.setHeight(object.getInt(HEIGHT_TAG));

            JSONArray array = object.getJSONArray(BIKES_TAG);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                Bike bike = new Bike();
                bike.setWeight((float)item.getDouble(WEIGHT_TAG));
                bike.setType(Bike.BikeType.valueOf(item.getString(BIKE_TYPE_TAG)));
                user.getBikes().add(bike);
            }

            return user;
        } catch (JSONException e) {
            throw new IOException("Unable to load JSON.", e);
        }
    }

    private static String convertStreamToString(Reader reader) {
        Scanner s = new Scanner(reader).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
