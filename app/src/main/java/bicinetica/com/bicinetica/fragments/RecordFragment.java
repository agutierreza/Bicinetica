package bicinetica.com.bicinetica.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;

import bicinetica.com.bicinetica.R;
import bicinetica.com.bicinetica.RecordSummary;
import bicinetica.com.bicinetica.data.Record;
import bicinetica.com.bicinetica.data.RecordProvider;

public class RecordFragment extends Fragment implements RecordProvider.OnListChanged<Record> {

    private RecordListAdapter adapter;
    private RecyclerView recyclerView;

    private OnRecordClicked mListener;
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

            recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            adapter = new RecordListAdapter(new OnRecordClicked() {
                @Override
                public void onRecordClicked(Record item) {
                    Intent intent = new Intent(context, RecordSummary.class);
                    intent.putExtra("file_name", RecordProvider.getInstance().getPath(item));
                    context.startActivity(intent);
                }
            });
            recyclerView.setAdapter(adapter);
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

            RecordProvider.getInstance().suscribe(this);
        }

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RecordProvider.getInstance().unsuscribe(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRecordClicked) {
            mListener = (OnRecordClicked) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemAdded(int index, Record item) {
        adapter.notifyItemInserted(index);
        recyclerView.scrollToPosition(0);
    }

    @Override
    public void onItemRemoved(int index) {
        adapter.notifyItemRangeRemoved(index, 1);
    }

    public interface OnRecordClicked {
        void onRecordClicked(Record item);
    }
}
