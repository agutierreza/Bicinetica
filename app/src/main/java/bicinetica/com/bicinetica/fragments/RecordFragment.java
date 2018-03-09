package bicinetica.com.bicinetica.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

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

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();

            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            ArrayList<Record> items = new ArrayList<>();

            Record r = new Record();
            r.setId(1);
            r.setName("Running indoor");
            items.add(r);

            r = new Record();
            r.setId(2);
            r.setName("Running outdoor");
            items.add(r);

            r = new Record();
            r.setId(3);
            r.setName("Cycling indoor");
            items.add(r);

            r = new Record();
            r.setId(4);
            r.setName("Cycling outdoor");
            items.add(r);

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
