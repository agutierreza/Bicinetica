package bicinetica.com.bicinetica.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bicinetica.com.bicinetica.R;
import bicinetica.com.bicinetica.RecordSummary;
import bicinetica.com.bicinetica.data.Record;

public class RecordFragment extends Fragment {

    private RecordListAdapter adapter;
    private List<Record> items;

    private OnListFragmentInteractionListener mListener;
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");

    public RecordFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_list, container, false);

        if (view instanceof RecyclerView) {
            final Context context = view.getContext();

            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            items = new ArrayList<>();

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
                    date = format.parse(datetime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Record record = new Record();
                record.setName("Cycling outdoor");
                record.setDate(date);
                items.add(record);
            }

            adapter = new RecordListAdapter(items, new OnListFragmentInteractionListener() {
                @Override
                public void onListFragmentInteraction(Record item) {

                    File file = Environment.getExternalStorageDirectory();
                    file = new File(file, String.format("%s_%s.json", item.getName(), format.format(item.getDate())));

                    Intent intent = new Intent(context, RecordSummary.class);
                    intent.putExtra("file_name", file.getAbsolutePath());
                    context.startActivity(intent);
                }
            });
            recyclerView.setAdapter(adapter);
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean notify = false;

        for (int i = items.size() -  1; i >= 0; i--) {
            Record item = items.get(i);
            File file = Environment.getExternalStorageDirectory();
            file = new File(file, String.format("%s_%s.json", item.getName(), format.format(item.getDate())));
            if (!file.exists()) {
                notify = true;
                items.remove(i);
            }
        }

        if (notify) adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Record item);
    }
}
