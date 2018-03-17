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

    @Override
    public int findIndex(SensorData item) {
        for (int i = 0; i < getItemCount(); i++) {
            if (item.getAddress().equals(get(i).getAddress())) {
                return i;
            }
        }
        return -1;
    }

    public void save() throws IOException {
        SensorMapper.save(getAll(), file);
    }
}
