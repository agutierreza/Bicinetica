package bicinetica.com.bicinetica.data;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

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

    public static void save(List<Location> record, String filePath) throws IOException {
        save(record, new File(filePath));
    }

    public static void save(List<Location> record, File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter writer = new FileWriter(file);
        save(record, writer);
        writer.close();
    }

    public static void save(List<Location> record, OutputStreamWriter streamWriter) throws IOException {
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
                positions.put(new JSONObject()
                        .put("time", position.getTimestamp())
                        .put("latitude", position.getLatitude())
                        .put("longitude", position.getLongitude())
                        .put("altitude", position.getAltitude())
                        .put("speed", position.getSpeed())
                        .put("power", position.getPower()));
            }
            base.put("positions", positions);
        }
        catch (JSONException ex)
        {
            Log.e("MAPPER", ex.getMessage());
        }

        return base;
    }

    private static JSONObject createJson(List<Location> locations) {
        JSONObject base = new JSONObject();

        try
        {
            Location baseLocation = locations.get(0);

            base.put("name", "Cycling outdoor raw")
                    .put("date", baseLocation.getTime());

            JSONArray positions = new JSONArray();
            for (Location location : locations) {
                positions.put(new JSONObject()
                        .put("time", location.getTime() - baseLocation.getTime())
                        .put("speed", location.getSpeed())
                        .put("rpm", location.getExtras().getFloat("rpm"))
                        .put("power", location.getExtras().getFloat("power"))
                        .put("location", new JSONObject()
                                .put("longitude", location.getLongitude())
                                .put("latitude", location.getLatitude())
                                .put("horizontal error", location.getAccuracy())
                                .put("altitude", location.getAltitude())
                        )
                );
            }
            base.put("tracks", positions);
        }
        catch (JSONException ex)
        {
            Log.e("MAPPER", ex.getMessage());
        }

        return base;
    }
}
