package com.days.kitchenstock;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.days.kitchenstock.data.StockContentHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EasyAddOptionsDialog extends AlertDialog {

    private EditText mName;
    private ListView mResultsList;
    private String mSearchString;
    private View mSearchResultsLayout, mOptionsList;
    private ArrayList<StockContentHelper.Item> mAllItemsList;
    private boolean[] mItemsMatch;
    private TextView mGroceries, mFruits, mNonVeg, mBabyCare, mStationery, mOthers;
    private ItemStockAdapter mAdapter;
    private Button mDone, mAddToShop, mAddToStock;
    enum Category {
        ALL, GROCERIES, FRUITS, NON_VEG, BABY_CARE, STATOINERY, OTHERS;
    }

    private Category mSelectedCategory;

    public EasyAddOptionsDialog(@NonNull final Context context) {
        super(context, R.style.MyDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.easy_add_options_dialog, null);
        setView(view);
        mSearchResultsLayout = view.findViewById(R.id.search_results);
        mOptionsList = view.findViewById(R.id.options_list);
        mResultsList = view.findViewById(R.id.results);
        mResultsList.setDividerHeight(0);
        mSelectedCategory = Category.ALL;
        mName = view.findViewById(R.id.search_item_name);
        setupSearchButtons();
        mName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    enterSearchMode();
                }
            }
        });
        initOptionsList();
        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int k, int i1, int i2) {
                mSearchString = charSequence.toString();
                if (mSearchString == null) {
                    for (int i = 0; i < mAllItemsList.size(); i++) {
                        mItemsMatch[i] =true;
                    }
                } else {
                    for (int i = 0; i < mAllItemsList.size(); i++) {
                        StockContentHelper.Item item = mAllItemsList.get(i);
                        mItemsMatch[i] = item.name.toLowerCase().contains(mSearchString.toLowerCase());
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        Button close = view.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        setCancelable(false);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    private void enterSearchMode() {
        if (mSearchResultsLayout.getVisibility() != View.VISIBLE) {
            EasyAddOptionsDialog.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            mOptionsList.setVisibility(View.GONE);
            mSearchResultsLayout.setVisibility(View.VISIBLE);
            mName.setHint(getSearchHint());
            updateList();
        }
    }

    private String getSearchHint() {
        switch (mSelectedCategory) {
            case GROCERIES:
                return getContext().getString(R.string.search_groceries);
            case FRUITS:
                return getContext().getString(R.string.search_fruits);
            case NON_VEG:
                return getContext().getString(R.string.search_non_veg);
            case BABY_CARE:
                return getContext().getString(R.string.search_baby_care);
            case OTHERS:
                return getContext().getString(R.string.search_other_items);
            case STATOINERY:
                return getContext().getString(R.string.search_stationery);
        }
        return getContext().getString(R.string.search_all_lists);
    }

    private void setupSearchButtons() {
        mDone = mSearchResultsLayout.findViewById(R.id.done);
        mAddToShop = mSearchResultsLayout.findViewById(R.id.button_add_to_shop);
        mAddToStock = mSearchResultsLayout.findViewById(R.id.button_add_to_stock);
        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mAddToStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mAdapter.getSelectedItems();
                StockContentHelper.addToInStockList(getContext(), selectedItems);
                updateList();
            }
        });
        mAddToShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mAdapter.getSelectedItems();
                StockContentHelper.addToInShopList(getContext(), selectedItems);
                updateList();
            }
        });
    }

    private void initOptionsList() {
        mGroceries = mOptionsList.findViewById(R.id.groceries);
        mFruits = mOptionsList.findViewById(R.id.fruits);
        mNonVeg = mOptionsList.findViewById(R.id.non_veg);
        mBabyCare = mOptionsList.findViewById(R.id.baby_care);
        mStationery = mOptionsList.findViewById(R.id.stationery);
        mOthers = mOptionsList.findViewById(R.id.other_items);
        mGroceries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectedCategory = Category.GROCERIES;
                enterSearchMode();
            }
        });
        mFruits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectedCategory = Category.FRUITS;
                enterSearchMode();
            }
        });
        mNonVeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectedCategory = Category.NON_VEG;
                enterSearchMode();
            }
        });
        mBabyCare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectedCategory = Category.BABY_CARE;
                enterSearchMode();
            }
        });
        mStationery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectedCategory = Category.STATOINERY;
                enterSearchMode();
            }
        });
        mOthers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectedCategory = Category.OTHERS;
                enterSearchMode();
            }
        });
    }

    private void updateList() {
        mAllItemsList = getItemsList();
        Collections.sort(mAllItemsList);
        mAdapter = new ItemStockAdapter(getContext(), mAllItemsList);
        mItemsMatch = new boolean[mAllItemsList.size()];
        for (int i = 0; i < mAllItemsList.size(); i++) {
            mItemsMatch[i] = true;
        }
        mResultsList.setAdapter(mAdapter);
    }

    private ArrayList<StockContentHelper.Item> getItemsList() {
        switch (mSelectedCategory){
            case GROCERIES:
                return StockContentHelper.getEasyAddItems(getContext(), R.array.veg, StockContentHelper.ItemType.FRESH);
            case FRUITS:
                return StockContentHelper.getEasyAddItems(getContext(), R.array.fruits, StockContentHelper.ItemType.FRESH);
            case NON_VEG:
                return StockContentHelper.getEasyAddItems(getContext(), R.array.non_veg, StockContentHelper.ItemType.FRESH);
            case BABY_CARE:
                return StockContentHelper.getEasyAddItems(getContext(), R.array.baby_care, StockContentHelper.ItemType.LONG_TERM);
            case OTHERS:
                return StockContentHelper.getEasyAddItems(getContext(), R.array.other_house_hold, StockContentHelper.ItemType.LONG_TERM);
            case STATOINERY:
                return StockContentHelper.getEasyAddItems(getContext(), R.array.stationery, StockContentHelper.ItemType.LONG_TERM);
        }
        return StockContentHelper.getAllEasyAddItems(getContext());
    }

    private class ItemStockAdapter extends ArrayAdapter<StockContentHelper.Item> {
        private StockContentHelper.ItemType type;
        private List<StockContentHelper.Item> itemList;
        private boolean[] checkedValues;

        public ItemStockAdapter(Context context, ArrayList<StockContentHelper.Item> list) {
            super(context, R.layout.list_item, list);
            itemList = list;
            checkedValues = new boolean[list.size()];
        }

        ArrayList<StockContentHelper.Item> getSelectedItems() {
            ArrayList<StockContentHelper.Item> selectedItems = new ArrayList<>();
            for (int i = 0; i < itemList.size(); i++) {
                if (checkedValues[i]) {
                    selectedItems.add(itemList.get(i));
                }
            }
            return selectedItems;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            StockContentHelper.Item item = (StockContentHelper.Item) getItem(i);
            if (!checkedValues[i] && !mItemsMatch[i]) {
                View view1 = new View(getContext());
                view1.setMinimumHeight(0);
                return view1;
            }
            view = getLayoutInflater().inflate(R.layout.list_item_with_divider, null);
            TextView name = view.findViewById(R.id.item_name);
            name.setText(item.name);
            TextView status = view.findViewById(R.id.quantity);
            status.setText(item.getTypeString(getContext()));
            final CheckBox checkBox = view.findViewById(R.id.checkbox);
            checkBox.setTag(i);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    checkedValues[(Integer) compoundButton.getTag()] = b;
                }
            });
            checkBox.setChecked(checkedValues[i]);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkBox.toggle();
                }
            });
            return view;
        }
    }
}
