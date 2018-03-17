package bicinetica.com.bicinetica.data;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class SensorProvider extends ProviderBase<SensorData> {
    private static final SensorProvider instance = new SensorProvider();
    private File file;

    public static SensorProvider getInstance() {
        return instance;
    }

    private SensorProvider() {
        file = Environment.getExternalStorageDirectory();
        file = new File(file, "devices.json");

        if (file.exists()) {
            try {
                for (SensorData sensor : SensorMapper.load(file)) {
                    super.add(sensor);
                }
            } catch (IOException e) {

            }
        }
    }

    public void save() throws IOException {
        SensorMapper.save(getAll(), file);
    }
}
