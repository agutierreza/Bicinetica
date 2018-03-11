package bicinetica.com.bicinetica.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import bicinetica.com.bicinetica.R;
import bicinetica.com.bicinetica.data.Record;
import bicinetica.com.bicinetica.fragments.RecordFragment.OnListFragmentInteractionListener;

import java.text.SimpleDateFormat;
import java.util.List;

public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.RecordViewHolder> {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");
    private final List<Record> mValues;
    private final OnListFragmentInteractionListener mListener;

    public RecordListAdapter(List<Record> items) {
        mValues = items;
        mListener = null;
    }

    public RecordListAdapter(List<Record> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public RecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_record, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecordViewHolder holder, int position) {

        holder.mContentView.setText(mValues.get(position).getName() + " - " + dateFormat.format(mValues.get(position).getDate()));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class RecordViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public Record mItem;

        public RecordViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = view.findViewById(R.id.content);
        }
    }
}
