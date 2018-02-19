package bicinetica.com.bicinetica;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import bicinetica.com.bicinetica.fragments.RealtimeFragment;

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
        return new RealtimeFragment();
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
