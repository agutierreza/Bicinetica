package bicinetica.com.bicinetica;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.BluetoothDeviceViewHolder> {

    private final SensorsActivity.ListListener mListener;
    private final List<BluetoothDevice> mValues;

    public BluetoothDeviceAdapter(List<BluetoothDevice> items) {
        this(items, null);
    }

    public BluetoothDeviceAdapter(List<BluetoothDevice> items, SensorsActivity.ListListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public void onBindViewHolder(final BluetoothDeviceViewHolder holder, int position) {

        holder.bind(mValues.get(position));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onDeviceClicked(holder.mItem);
                }
            }
        });
    }

    @Override
    public BluetoothDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_bluetooth_device, parent, false);
        return new BluetoothDeviceViewHolder(view);
    }

    public class BluetoothDeviceViewHolder extends RecyclerView.ViewHolder {

        public final TextView mContentView;
        public final View mView;
        public BluetoothDevice mItem;

        public BluetoothDeviceViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = view.findViewById(R.id.content);
        }

        public void bind(BluetoothDevice device) {
            mItem = device;
            mContentView.setText(device.getName() + " " + device.getAddress());
        }
    }
}
