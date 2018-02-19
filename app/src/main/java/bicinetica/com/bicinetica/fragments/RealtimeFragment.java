package bicinetica.com.bicinetica.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import bicinetica.com.bicinetica.R;

public class RealtimeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public RealtimeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_realtime, container, false);

        Button beginButton = rootView.findViewById(R.id.button_begin);
        beginButton.setOnClickListener(mBeginButtonListener);

        Button endButton = rootView.findViewById(R.id.button_end);
        endButton.setOnClickListener(mEndButtonListener);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private View.OnClickListener mBeginButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //TODO
        }
    };

    private View.OnClickListener mEndButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mListener != null) {
                mListener.onActivityReported();
            }
            //TODO
        }
    };

    public interface OnFragmentInteractionListener {
        void onActivityReported();
    }
}
