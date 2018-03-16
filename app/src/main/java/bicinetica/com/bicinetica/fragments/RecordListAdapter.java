package bicinetica.com.bicinetica.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import bicinetica.com.bicinetica.R;
import bicinetica.com.bicinetica.data.Record;
import bicinetica.com.bicinetica.data.RecordProvider;
import bicinetica.com.bicinetica.fragments.RecordFragment.OnRecordClicked;

import java.text.SimpleDateFormat;

public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.RecordViewHolder> {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");
    private final OnRecordClicked mListener;

    private RecordProvider provider = RecordProvider.getInstance();

    public RecordListAdapter(OnRecordClicked listener) {
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
        holder.setRecord(provider.get(position));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onRecordClicked(holder.getRecord());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return provider.getItemCount();
    }

    public class RecordViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        private Record record;

        public RecordViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = view.findViewById(R.id.content);
        }

        public Record getRecord() {
            return record;
        }

        public void setRecord(Record record) {
            this.record = record;
            mContentView.setText(record.getName() + " - " + dateFormat.format(record.getDate()));
        }
    }
}
