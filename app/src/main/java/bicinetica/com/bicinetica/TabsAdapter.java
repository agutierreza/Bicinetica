package bicinetica.com.bicinetica;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import bicinetica.com.bicinetica.fragments.RealtimeFragment;
import bicinetica.com.bicinetica.fragments.RecordFragment;

public class TabsAdapter extends FragmentPagerAdapter {
    private String[] names;

    private Context mContext;
    private FragmentManager mFragmentManager;
    private RealtimeFragment mRealtimeFragment;
    private RecordFragment mRecordFragment;

    public TabsAdapter(FragmentManager fm , Context context) {
        super(fm);
        this.mFragmentManager = fm;
        mContext = context;
        names = context.getResources().getStringArray(R.array.tab_names);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return getRealtimeFragment();
        }
        else if (position == 1) {
            return getRecordFragment();
        }
        else {
            throw new RuntimeException();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return names[position];
    }

    private Fragment getRealtimeFragment()
    {
        if (mRealtimeFragment == null) {
            mRealtimeFragment = new RealtimeFragment();
        }
        return mRealtimeFragment;
    }

    private Fragment getRecordFragment()
    {
        if (mRecordFragment == null) {
            mRecordFragment = new RecordFragment();
        }
        return mRecordFragment;
    }
}
