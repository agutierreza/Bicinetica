package bicinetica.com.bicinetica.data;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class RecordProvider {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private static final RecordProvider instance = new RecordProvider();

    private List<Record> items = new ArrayList<>();

    private List<OnListChanged<Record>> listeners = new ArrayList<>();

    public static RecordProvider getInstance() {
        return instance;
    }

    private RecordProvider() {
        final String name = "Cycling outdoor_";
        final String extension = ".json";

        File[] files = Environment.getExternalStorageDirectory().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(name) && pathname.getName().endsWith(extension);
            }
        });

        for (File file: files) {
            String datetime = file.getName().substring(name.length(), file.getName().indexOf('.'));

            Date date = null;

            try {
                date = DATE_FORMAT.parse(datetime);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Record record = new Record();
            record.setName("Cycling outdoor");
            record.setDate(date);
            items.add(record);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
                items.sort(new Comparator<Record>() {
                    @Override
                    public int compare(Record o1, Record o2) {
                        return o2.getDate().compareTo(o1.getDate());
                    }
                });
            }
        }
    }

    public int getItemCount() {
        return items.size();
    }

    public Record get(int i) {
        return items.get(i);
    }

    public List<Record> getAll() {
        return items.subList(0, items.size() - 1);
    }

    public void add(Record record) throws IOException {
        saveRecord(record);
        items.add(0, record);

        for (OnListChanged<Record> listener : listeners) {
            listener.onItemAdded(0, record);
        }
    }

    public void remove(Record record) {
        int i = findIndex(record);
        items.remove(i);

        File f = getFile(record);
        f.delete();

        for (OnListChanged<Record> listener : listeners) {
            listener.onItemRemoved(i);
        }
    }

    public String getPath(Record item) {
        return getFile(item).getAbsolutePath();
    }

    public File getFile(Record item) {
        File file = Environment.getExternalStorageDirectory();
        file = new File(file, String.format("%s_%s.json", item.getName(), DATE_FORMAT.format(item.getDate())));
        return file;
    }

    public void suscribe(OnListChanged<Record> listener) {
        listeners.add(listener);
    }
    public void unsuscribe(OnListChanged<Record> listener) {
        listeners.remove(listener);
    }

    private int findIndex(Record r) {
        for (int i = 0; i < items.size(); i++) {
            if (get(i).hashCode() == r.hashCode()) {
                return i;
            }
        }
        return -1;
    }

    private void saveRecord(Record record) throws IOException {
        RecordMapper.save(record, getFile(record));
    }

    public interface OnListChanged<T> {
        void onItemAdded(int index, T item);
        void onItemRemoved(int index);
        //void onItemChanged(int index, T item);
    }
}
