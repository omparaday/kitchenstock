package com.days.kitchenstock;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.days.kitchenstock.data.StockContentHelper;
import com.google.android.material.tabs.TabLayout;

public class HomeFragment extends Fragment {

    DemoCollectionPagerAdapter demoCollectionPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    @Override
    public void onViewCreated( View view,  Bundle savedInstanceState) {
        demoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getChildFragmentManager());
        ViewPager pager = view.findViewById(R.id.pager);
        pager.setAdapter(demoCollectionPagerAdapter);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(pager);
    }

    public class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {
        public DemoCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 3) {
                return new ShoppingFragment();
            }
            Fragment fragment = new StockFragment();
            Bundle args = new Bundle();
            // Our object is just an integer :-P
            args.putInt(StockFragment.LIST_TYPE_PARAM, i);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = getString(R.string.shopping_list);
            StockContentHelper.ItemType type = StockContentHelper.ItemType.values()[position];
            switch (type) {
                case FRESH:
                    title = getString(R.string.fresh);
                    break;
                case SHORT_TERM:
                    title = getString(R.string.short_term);
                    break;
                case LONG_TERM:
                    title = getString(R.string.long_term);
                    break;
            }
            return title;
        }
    }
}

