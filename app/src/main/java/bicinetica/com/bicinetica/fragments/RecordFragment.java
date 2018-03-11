package bicinetica.com.bicinetica.fragments;

import android.content.Context;
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

import bicinetica.com.bicinetica.R;
import bicinetica.com.bicinetica.data.Record;

public class RecordFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;

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
            Context context = view.getContext();

            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            ArrayList<Record> items = new ArrayList<>();

            final String name = "Cycling outdoor_";
            final String extension = ".json";

            File[] files = Environment.getExternalStorageDirectory().listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().startsWith(name) && pathname.getName().endsWith(extension);
                }
            });

            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");

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

            recyclerView.setAdapter(new RecordListAdapter(items));
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        }

        return view;
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
