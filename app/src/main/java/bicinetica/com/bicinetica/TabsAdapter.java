package bicinetica.com.bicinetica;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import bicinetica.com.bicinetica.fragments.RealtimeFragment;
import bicinetica.com.bicinetica.fragments.RecordFragment;

public class TabsAdapter extends FragmentPagerAdapter {
    private String[] names;

    private Context context;
    private FragmentManager mFragmentManager;

    public TabsAdapter(FragmentManager fm , Context nContext) {
        super(fm);
        this.mFragmentManager = fm;
        context = nContext;
        names = nContext.getResources().getStringArray(R.array.tab_names);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new RealtimeFragment();
        }
        else if (position == 1) {
            return new RecordFragment();
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
}
