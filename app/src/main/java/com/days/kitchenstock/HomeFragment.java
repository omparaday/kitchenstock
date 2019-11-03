package com.days.kitchenstock;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.days.kitchenstock.data.StockContentHelper;
import com.google.android.material.tabs.TabLayout;

public class HomeFragment extends Fragment {

    public static final int TAB_COUNT = 3;
    DemoCollectionPagerAdapter demoCollectionPagerAdapter;
    private Button mAddItem, mSearch;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        demoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getChildFragmentManager());
        ViewPager pager = view.findViewById(R.id.pager);
        pager.setOffscreenPageLimit(TAB_COUNT - 1);
        pager.setAdapter(demoCollectionPagerAdapter);
        pager.setCurrentItem(TAB_COUNT - 1);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(pager);
        mAddItem = view.findViewById(R.id.add_item);
        mAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AddItemDialog(getContext()).show();
            }
        });
        mSearch = view.findViewById(R.id.search_button);
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SearchItemDialog(getContext()).show();
            }
        });
    }

    public class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {
        public DemoCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == TAB_COUNT - 1) {
                return new ShoppingFragment();
            }
            Fragment fragment = new StockFragment();
            Bundle args = new Bundle();
            args.putInt(StockFragment.LIST_TYPE_PARAM, i);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = getString(R.string.shopping_list_tab);
            if (position == TAB_COUNT - 1) {
                return title;
            }
            StockContentHelper.ItemType type = StockContentHelper.ItemType.values()[position];
            switch (type) {
                case FRESH:
                    title = getString(R.string.fresh);
                    break;
                case LONG_TERM:
                    title = getString(R.string.long_term);
                    break;
            }
            return title;
        }
    }
}

