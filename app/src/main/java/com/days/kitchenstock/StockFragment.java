package com.days.kitchenstock;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.days.kitchenstock.data.StockContentHelper;
import com.days.kitchenstock.data.StockContentProvider;

import java.util.ArrayList;
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StockFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class StockFragment extends Fragment {

    private ItemStockAdapter mInStockAdapter;
    private ItemStockAdapter mOutOfStockAdapter;
    private ListView mInStockListView;
    private ListView mOutOfStockListView;

    public static final String LIST_TYPE_PARAM = "listType";

    // TODO: Rename and change types of parameters
    private StockContentHelper.ItemType itemType;

    private OnFragmentInteractionListener mListener;
    private ArrayList<StockContentHelper.Item> mInStockList;
    private ArrayList<StockContentHelper.Item> mOutOfStockList;
    private ContentObserver mObserver;
    private boolean mIsInStockEditing;
    private boolean mIsOutOfStockEditing;
    private View.OnClickListener mTitleClickListener;
    private Button mInStockListTitle;
    private Button mOutOfStockListTitle;
    private Button mInStockEditButton;
    private Button mOutOfStockEditButton;

    public StockFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int listType = getArguments().getInt(LIST_TYPE_PARAM);
            itemType = StockContentHelper.ItemType.values()[listType];
        }
        mObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                updateLists();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.stock_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Bundle args = getArguments();
        mInStockListView = view.findViewById(R.id.in_stock);
        mOutOfStockListView = view.findViewById(R.id.out_of_stock);
        mTitleClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInStockListView.getVisibility() == View.VISIBLE) {
                    mOutOfStockListView.setVisibility(View.VISIBLE);
                    mInStockListView.setVisibility(View.GONE);
                    mInStockEditButton.setVisibility(View.GONE);
                    mOutOfStockEditButton.setVisibility(View.VISIBLE);
                } else {
                    mInStockListView.setVisibility(View.VISIBLE);
                    mOutOfStockListView.setVisibility(View.GONE);
                    mInStockEditButton.setVisibility(View.VISIBLE);
                    mOutOfStockEditButton.setVisibility(View.GONE);
                }
            }
        };
        setupInStockLayoutButtons(view);
        setupOutOfStockLayoutButtons(view);
        updateLists();

    }

    private void setupInStockLayoutButtons(View view) {
        View inStockTitleButtons = view.findViewById(R.id.in_stock_title_buttons);
        mInStockEditButton = inStockTitleButtons.findViewById(R.id.edit);
        final Button cancelEditButton = inStockTitleButtons.findViewById(R.id.cancel);
        mInStockListTitle = inStockTitleButtons.findViewById(R.id.list_title);
        final View actionButtons = view.findViewById(R.id.in_stock_action_buttons);
        mInStockListTitle.setText(R.string.in_stock);
        mInStockListTitle.setOnClickListener(mTitleClickListener);
        mInStockEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInStockListTitle.setOnClickListener(null);
                mInStockEditButton.setVisibility(View.GONE);
                mOutOfStockListTitle.setVisibility(View.GONE);
                cancelEditButton.setVisibility(View.VISIBLE);
                actionButtons.setVisibility(View.VISIBLE);
                updateInStockList(true);
                mIsInStockEditing = true;
            }
        });
        cancelEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitEditingInStockList(cancelEditButton, mInStockEditButton, mInStockListTitle, actionButtons);
            }
        });


        final Button deleteSelectedItems = actionButtons.findViewById(R.id.delete);
        final Button moveToShopSelectedItems = actionButtons.findViewById(R.id.button3);
        moveToShopSelectedItems.setText(R.string.add_to_shop);
        final Button moveToOutOfStockSelectedItems = actionButtons.findViewById(R.id.button2);
        moveToOutOfStockSelectedItems.setText(R.string.move_to_out_of_stock);
        deleteSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mInStockAdapter.getSelectedItems();
                StockContentHelper.deleteItemList(getContext(), selectedItems);
                exitEditingInStockList(cancelEditButton, mInStockEditButton, mInStockListTitle, actionButtons);
            }
        });
        moveToShopSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mInStockAdapter.getSelectedItems();
                StockContentHelper.moveToShopList(getContext(), selectedItems);
                exitEditingInStockList(cancelEditButton, mInStockEditButton, mInStockListTitle, actionButtons);
            }
        });
        moveToOutOfStockSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mInStockAdapter.getSelectedItems();
                StockContentHelper.moveToOutOfStockList(getContext(), selectedItems);
                exitEditingInStockList(cancelEditButton, mInStockEditButton, mInStockListTitle, actionButtons);
            }
        });
    }


    private void setupOutOfStockLayoutButtons(View view) {
        View listTitleButtons = view.findViewById(R.id.out_of_stock_title_buttons);
        final View actionButtons = view.findViewById(R.id.out_of_stock_action_buttons);
        mOutOfStockEditButton = listTitleButtons.findViewById(R.id.edit);
        mOutOfStockEditButton.setVisibility(View.GONE);
        final Button cancelEditButton = listTitleButtons.findViewById(R.id.cancel);
        mOutOfStockListTitle = listTitleButtons.findViewById(R.id.list_title);
        mOutOfStockListTitle.setText(R.string.out_of_stock);
        mOutOfStockListTitle.setOnClickListener(mTitleClickListener);
        mOutOfStockEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOutOfStockListTitle.setOnClickListener(null);
                mOutOfStockEditButton.setVisibility(View.GONE);
                mInStockListTitle.setVisibility(View.GONE);
                cancelEditButton.setVisibility(View.VISIBLE);
                actionButtons.setVisibility(View.VISIBLE);
                updateOutOfStockList(true);
                mIsOutOfStockEditing = true;
            }
        });
        cancelEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitEditingOutOfStockList(cancelEditButton, mOutOfStockEditButton, mOutOfStockListTitle, actionButtons);
            }
        });


        final Button deleteSelectedItems = actionButtons.findViewById(R.id.delete);
        final Button moveToShopSelectedItems = actionButtons.findViewById(R.id.button3);
        moveToShopSelectedItems.setText(R.string.add_to_shop);
        final Button moveToInStockSelectedItems = actionButtons.findViewById(R.id.button2);
        moveToInStockSelectedItems.setText(R.string.add_to_stock);
        deleteSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mOutOfStockAdapter.getSelectedItems();
                StockContentHelper.deleteItemList(getContext(), selectedItems);
                exitEditingOutOfStockList(cancelEditButton, mOutOfStockEditButton, mOutOfStockListTitle, actionButtons);
            }
        });
        moveToShopSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mOutOfStockAdapter.getSelectedItems();
                StockContentHelper.moveToShopList(getContext(), selectedItems);
                exitEditingOutOfStockList(cancelEditButton, mOutOfStockEditButton, mOutOfStockListTitle, actionButtons);
            }
        });
        moveToInStockSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mOutOfStockAdapter.getSelectedItems();
                StockContentHelper.moveToInStockList(getContext(), selectedItems);
                exitEditingOutOfStockList(cancelEditButton, mOutOfStockEditButton, mOutOfStockListTitle, actionButtons);
            }
        });
    }

    private void exitEditingInStockList(Button cancelEdit, Button editButton, Button listTitle, View actionButtons) {
        cancelEdit.setVisibility(View.GONE);
        actionButtons.setVisibility(View.GONE);
        editButton.setVisibility(View.VISIBLE);
        mOutOfStockListTitle.setVisibility(View.VISIBLE);
        mIsInStockEditing = false;
        listTitle.setOnClickListener(mTitleClickListener);
        updateInStockList(false);
    }

    private void exitEditingOutOfStockList(Button cancelEdit, Button editButton, Button listTitle, View actionButtons) {
        cancelEdit.setVisibility(View.GONE);
        actionButtons.setVisibility(View.GONE);
        editButton.setVisibility(View.VISIBLE);
        mInStockListTitle.setVisibility(View.VISIBLE);
        mIsOutOfStockEditing = false;
        listTitle.setOnClickListener(mTitleClickListener);
        updateOutOfStockList(false);
    }


    private void updateInStockList(boolean editMode) {
        if (!mIsInStockEditing) {
            mInStockList = fetchList(true);
            Collections.sort(mInStockList);
            mInStockAdapter = new ItemStockAdapter(getActivity(), mInStockList, editMode);
            mInStockListView.setAdapter(mInStockAdapter);
        }
    }

    private void updateOutOfStockList(boolean editMode) {
        if (!mIsOutOfStockEditing) {
            mOutOfStockList = fetchList(false);
            mOutOfStockAdapter = new ItemStockAdapter(getActivity(), mOutOfStockList, editMode);
            mOutOfStockListView.setAdapter(mOutOfStockAdapter);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getContext().getContentResolver().registerContentObserver(StockContentProvider.CONTENT_URI, true, mObserver);
        updateLists();
    }

    @Override
    public void onStop() {
        super.onStop();
        getContext().getContentResolver().unregisterContentObserver(mObserver);

    }

    private void updateLists() {
        updateInStockList(false);
        updateOutOfStockList(false);
    }

    private ArrayList<StockContentHelper.Item> fetchList(boolean isInStock) {
        return StockContentHelper.queryItems(getContext(), itemType, isInStock);
    }

    private class ItemStockAdapter extends ArrayAdapter<StockContentHelper.Item> {
        private boolean isEditing;
        private ArrayList<StockContentHelper.Item> itemList;
        private boolean[] checkedValues;

        public ItemStockAdapter(Context context, ArrayList<StockContentHelper.Item> list, boolean isEditing) {
            super(context, R.layout.list_item, list);
            this.isEditing = isEditing;
            checkedValues = new boolean[list.size()];
            itemList = list;
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
            if (!isEditing) {
                view = getLayoutInflater().inflate(R.layout.list_item, null);
            } else {
                view = getLayoutInflater().inflate(R.layout.editing_list_item, null);
            }
            TextView name = view.findViewById(R.id.item_name);
            name.setText(item.name);
            TextView quantity = view.findViewById(R.id.quantity);
            quantity.setText(item.quantity);
            TextView status = view.findViewById(R.id.status);
            TextView expiry = view.findViewById(R.id.expiry);
            if (item.status == StockContentHelper.ItemStatus.TO_BUY) {
                status.setText(item.getStatusString(getContext()));
            } else if (item.status == StockContentHelper.ItemStatus.IN_STOCK) {
                if (item.expiry != null) {
                    expiry.setText(DateFormat.getMediumDateFormat(getContext()).format(item.expiry));
                    if (item.isExpired()) {
                        status.setText(R.string.expired);
                        status.setTextColor(getResources().getColor(R.color.expired));
                    } else if (item.isExpiringSoon()) {
                        status.setTextColor(getResources().getColor(R.color.expiring_soon));
                        status.setText(R.string.expiring_soon);
                    }
                }
            }
            if (!isEditing) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new UpdateItemDialog(getContext(), getItem(i)).show();
                    }
                });
            } else {
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
            }
            return view;
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
