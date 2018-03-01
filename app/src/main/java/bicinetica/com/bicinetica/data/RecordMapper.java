package bicinetica.com.bicinetica.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class RecordMapper {

    public static void save(Record record, String filePath) throws IOException {
        save(record, new File(filePath));
    }

    public static void save(Record record, File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter writer = new FileWriter(file);
        save(record, writer);
        writer.close();
    }

    public static void save(Record record, OutputStreamWriter streamWriter) throws IOException {
        streamWriter.write(createJson(record).toString());
    }

    private static JSONObject createJson(Record record) {
        JSONObject base = new JSONObject();

        try
        {
            base.put("name", record.getName())
                    .put("date", record.getDate().getTime());

            JSONArray positions = new JSONArray();
            for (Position position : record.getPositions()) {
                JSONObject positionObject = new JSONObject()
                        .put("time", position.getSeconds())
                        .put("latitude", position.getLatitude())
                        .put("longitude", position.getLongitude())
                        .put("altitude", position.getAltitude())
                        .put("speed", position.getSpeed());
                positions.put(positionObject);
            }
            base.put("positions", positions);
        }
        catch (JSONException ex)
        {
            Log.d("MAPPER", ex.getMessage());
        }

        return base;
    }
}
