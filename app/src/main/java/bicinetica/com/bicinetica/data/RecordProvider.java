package bicinetica.com.bicinetica.data;

import android.os.Environment;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class RecordProvider extends ProviderBase<Record> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private static final RecordProvider instance = new RecordProvider();

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

        List<Record> records = new ArrayList<>();

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
            records.add(record);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
            records.sort(new Comparator<Record>() {
                @Override
                public int compare(Record o1, Record o2) {
                    return o2.getDate().compareTo(o1.getDate());
                }
            });
        }

        for (Record record : records) {
            super.add(record);
        }
    }

    public Record load(int i) throws IOException {
        return RecordMapper.load(getFile(get(i)));
    }

    @Override
    public void add(Record record)  {
        this.insert(0, record);
    }

    @Override
    public void insert(int index, Record record) {
        try {
            saveRecord(record);
            super.insert(index, record);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void remove(Record record) {
        File f = getFile(record);
        f.delete();

        super.remove(record);
    }

    @Override
    public int findIndex(Record item) {
        long itemTime = item.getDate().getTime();
        for (int i = 0; i < getItemCount(); i++) {
            if (equals(itemTime, get(i).getDate().getTime(), 1000)) {
                return i;
            }
        }
        return -1;
    }

    public String getPath(Record item) {
        return getFile(item).getAbsolutePath();
    }

    public File getFile(Record item) {
        File file = Environment.getExternalStorageDirectory();
        file = new File(file, String.format("%s_%s.json", item.getName(), DATE_FORMAT.format(item.getDate())));
        return file;
    }

    private void saveRecord(Record record) throws IOException {
        RecordMapper.save(record, getFile(record));
    }

    private static boolean equals(long x, long y, long tolerancy) {
        return Math.abs(x - y) < tolerancy;
    }
}
