package com.days.kitchenstock;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShoppingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ShoppingFragment extends Fragment {

    private ItemStockAdapter mToBuyAdapter;
    private ItemStockAdapter mPurchasedTodayAdapter;
    private ListView mToBuyListView;
    private View mToBuyEmptyMessage, mPurchasedTodayEmptyMessge;
    private ListView mPurchasedTodayListView;

    private OnFragmentInteractionListener mListener;
    private ArrayList<StockContentHelper.Item> mToBuyList;
    private ArrayList<StockContentHelper.Item> mPurchasedTodayList;
    private View mToBuyLayout, mPurhcasedTodayLayout;
    private ContentObserver mObserver;
    private boolean mIsToBuyEditing;
    private boolean mIsPurchasedTodayEditing;
    private View.OnClickListener mTitleClickListener;
    private Button mToBuyListTitle;
    private Button mPurchasedTodayListTitle;
    private Button mToBuyEditButton;
    private Button mPurchasedTodayEditButton;

    public ShoppingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                updateLists();
            }
        };
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.shopping_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mToBuyEmptyMessage = view.findViewById(R.id.empty_to_buy_message);
        mPurchasedTodayEmptyMessge = view.findViewById(R.id.empty_purchased_today_message);
        mToBuyLayout = view.findViewById(R.id.to_buy_layout);
        mPurhcasedTodayLayout = view.findViewById(R.id.purchased_today_layout);
        mToBuyListView = view.findViewById(R.id.to_buy);
        mToBuyListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mToBuyAdapter.dismissSwipe();
                return false;
            }
        });
        mPurchasedTodayListView = view.findViewById(R.id.purchased_today);
        mPurchasedTodayListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mPurchasedTodayAdapter.dismissSwipe();
                return false;
            }
        });
        mTitleClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mToBuyLayout.getVisibility() == View.VISIBLE) {
                    mToBuyListTitle.setText(R.string.to_buy_collapse);
                    mPurchasedTodayListTitle.setText(R.string.purchased_today_expand);
                    mPurhcasedTodayLayout.setVisibility(View.VISIBLE);
                    mToBuyLayout.setVisibility(View.GONE);
                    mToBuyEditButton.setVisibility(View.GONE);
                    mPurchasedTodayEditButton.setVisibility(View.VISIBLE);
                    if (mToBuyAdapter != null) {
                        mToBuyAdapter.dismissSwipe();
                    }
                } else {
                    mToBuyListTitle.setText(R.string.to_buy_expand);
                    mPurchasedTodayListTitle.setText(R.string.purchased_today_collapse);
                    mToBuyLayout.setVisibility(View.VISIBLE);
                    mPurhcasedTodayLayout.setVisibility(View.GONE);
                    mToBuyEditButton.setVisibility(View.VISIBLE);
                    mPurchasedTodayEditButton.setVisibility(View.GONE);
                    if (mPurchasedTodayAdapter != null) {
                        mPurchasedTodayAdapter.dismissSwipe();
                    }
                }
            }
        };
        setupToBuyLayoutButtons(view);
        setupPurchasedTodayLayoutButtons(view);
        updateLists();
    }

    private void setupToBuyLayoutButtons(View view) {
        View toBuyTitleButtons = view.findViewById(R.id.to_buy_title_buttons);
        mToBuyEditButton = toBuyTitleButtons.findViewById(R.id.edit);
        final Button cancelEditButton = toBuyTitleButtons.findViewById(R.id.cancel);
        mToBuyListTitle = toBuyTitleButtons.findViewById(R.id.list_title);
        final View actionButtons = view.findViewById(R.id.to_buy_action_buttons);
        mToBuyListTitle.setText(R.string.to_buy_expand);
        mToBuyListTitle.setOnClickListener(mTitleClickListener);
        mToBuyEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mToBuyListTitle.setOnClickListener(null);
                mToBuyEditButton.setVisibility(View.GONE);
                mPurchasedTodayListTitle.setVisibility(View.GONE);
                cancelEditButton.setVisibility(View.VISIBLE);
                actionButtons.setVisibility(View.VISIBLE);
                updateToBuyList(true);
                mIsToBuyEditing = true;
            }
        });
        cancelEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitEditingToBuyList(cancelEditButton, mToBuyEditButton, mToBuyListTitle, actionButtons);
            }
        });


        final Button deleteSelectedItems = actionButtons.findViewById(R.id.delete);
        final Button moveToInStockSelectedItems = actionButtons.findViewById(R.id.button3);
        moveToInStockSelectedItems.setText(R.string.add_to_stock);
        final Button moveToOutOfStockSelectedItems = actionButtons.findViewById(R.id.button2);
        moveToOutOfStockSelectedItems.setText(R.string.move_to_out_of_stock);
        deleteSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mToBuyAdapter.getSelectedItems();
                StockContentHelper.deleteItemList(getContext(), selectedItems);
                exitEditingToBuyList(cancelEditButton, mToBuyEditButton, mToBuyListTitle, actionButtons);
            }
        });
        moveToInStockSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mToBuyAdapter.getSelectedItems();
                StockContentHelper.moveToInStockList(getContext(), selectedItems);
                exitEditingToBuyList(cancelEditButton, mToBuyEditButton, mToBuyListTitle, actionButtons);
            }
        });
        moveToOutOfStockSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mToBuyAdapter.getSelectedItems();
                StockContentHelper.moveToOutOfStockList(getContext(), selectedItems);
                exitEditingToBuyList(cancelEditButton, mToBuyEditButton, mToBuyListTitle, actionButtons);
            }
        });
    }


    private void setupPurchasedTodayLayoutButtons(View view) {
        View listTitleButtons = view.findViewById(R.id.purchased_today_title_buttons);
        final View actionButtons = view.findViewById(R.id.purchased_today_action_buttons);
        mPurchasedTodayEditButton = listTitleButtons.findViewById(R.id.edit);
        mPurchasedTodayEditButton.setVisibility(View.GONE);
        final Button cancelEditButton = listTitleButtons.findViewById(R.id.cancel);
        mPurchasedTodayListTitle = listTitleButtons.findViewById(R.id.list_title);
        mPurchasedTodayListTitle.setText(R.string.purchased_today_collapse);
        mPurchasedTodayListTitle.setOnClickListener(mTitleClickListener);
        mPurchasedTodayEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPurchasedTodayListTitle.setOnClickListener(null);
                mPurchasedTodayEditButton.setVisibility(View.GONE);
                mToBuyListTitle.setVisibility(View.GONE);
                cancelEditButton.setVisibility(View.VISIBLE);
                actionButtons.setVisibility(View.VISIBLE);
                updatePurchasedTodayList(true);
                mIsPurchasedTodayEditing = true;
            }
        });
        cancelEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitEditingPurchasedTodayList(cancelEditButton, mPurchasedTodayEditButton, mPurchasedTodayListTitle, actionButtons);
            }
        });


        final Button deleteToBuySelectedItems = actionButtons.findViewById(R.id.delete);
        final Button moveToShopSelectedItems = actionButtons.findViewById(R.id.button3);
        moveToShopSelectedItems.setText(R.string.add_to_shop);
        final Button moveToOutOfStockSelectedItems = actionButtons.findViewById(R.id.button2);
        moveToOutOfStockSelectedItems.setText(R.string.move_to_out_of_stock);
        deleteToBuySelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mPurchasedTodayAdapter.getSelectedItems();
                StockContentHelper.deleteItemList(getContext(), selectedItems);
                exitEditingPurchasedTodayList(cancelEditButton, mPurchasedTodayEditButton, mPurchasedTodayListTitle, actionButtons);
            }
        });
        moveToShopSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mPurchasedTodayAdapter.getSelectedItems();
                StockContentHelper.moveToShopList(getContext(), selectedItems);
                exitEditingPurchasedTodayList(cancelEditButton, mPurchasedTodayEditButton, mPurchasedTodayListTitle, actionButtons);
            }
        });
        moveToOutOfStockSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mPurchasedTodayAdapter.getSelectedItems();
                StockContentHelper.moveToOutOfStockList(getContext(), selectedItems);
                exitEditingPurchasedTodayList(cancelEditButton, mPurchasedTodayEditButton, mPurchasedTodayListTitle, actionButtons);
            }
        });
    }

    private void exitEditingToBuyList(Button cancelEdit, Button editButton, Button listTitle, View actionButtons) {
        cancelEdit.setVisibility(View.GONE);
        actionButtons.setVisibility(View.GONE);
        editButton.setVisibility(View.VISIBLE);
        mPurchasedTodayListTitle.setVisibility(View.VISIBLE);
        mIsToBuyEditing = false;
        listTitle.setOnClickListener(mTitleClickListener);
        updateToBuyList(false);
    }

    private void exitEditingPurchasedTodayList(Button cancelEdit, Button editButton, Button listTitle, View actionButtons) {
        cancelEdit.setVisibility(View.GONE);
        actionButtons.setVisibility(View.GONE);
        editButton.setVisibility(View.VISIBLE);
        mToBuyListTitle.setVisibility(View.VISIBLE);
        mIsPurchasedTodayEditing = false;
        listTitle.setOnClickListener(mTitleClickListener);
        updatePurchasedTodayList(false);
    }

    private void updateToBuyList(boolean editMode) {
        if (!mIsToBuyEditing) {
            mToBuyList = fetchList(false);
            if (mToBuyList.size() != 0) {
                mToBuyAdapter = new ItemStockAdapter(getActivity(), mToBuyList, editMode);
                mToBuyListView.setVisibility(View.VISIBLE);
                mToBuyEmptyMessage.setVisibility(View.GONE);
                mToBuyListView.setAdapter(mToBuyAdapter);
            } else {
                mToBuyListView.setVisibility(View.GONE);
                mToBuyEmptyMessage.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updatePurchasedTodayList(boolean editMode) {
        if (!mIsPurchasedTodayEditing) {
            mPurchasedTodayList = fetchList(true);
            if (mPurchasedTodayList.size() != 0) {
                mPurchasedTodayListView.setVisibility(View.VISIBLE);
                mPurchasedTodayEmptyMessge.setVisibility(View.GONE);
                mPurchasedTodayAdapter = new ItemStockAdapter(getActivity(), mPurchasedTodayList, editMode);
                mPurchasedTodayListView.setAdapter(mPurchasedTodayAdapter);
            } else {
                mPurchasedTodayListView.setVisibility(View.GONE);
                mPurchasedTodayEmptyMessge.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateLists() {
        updateToBuyList(false);
        updatePurchasedTodayList(false);
    }

    private ArrayList<StockContentHelper.Item> fetchList(boolean purchasedToday) {
        return StockContentHelper.queryShoppingItems(getContext(), purchasedToday);
    }

    private class ItemStockAdapter extends ArrayAdapter<StockContentHelper.Item> {
        private boolean isEditing;
        private ArrayList<StockContentHelper.Item> itemList;
        private boolean[] checkedValues;
        private View currentVisibleSwipe;

        private void dismissSwipe() {
            if (currentVisibleSwipe != null) {
                currentVisibleSwipe.setVisibility(View.GONE);
                currentVisibleSwipe = null;
            }
        }

        public ItemStockAdapter(Context context, ArrayList<StockContentHelper.Item> list, boolean isEditing) {
            super(context, 0, list);
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
        public View getView(final int i, View view, final ViewGroup viewGroup) {
            final StockContentHelper.Item item = (StockContentHelper.Item) getItem(i);
            if (!isEditing) {
                view = getLayoutInflater().inflate(R.layout.list_item, null);
            } else {
                view = getLayoutInflater().inflate(R.layout.editing_list_item, null);
            }
            final TextView name = view.findViewById(R.id.item_name);
            name.setText(item.name);
            TextView quantity = view.findViewById(R.id.quantity);
            quantity.setText(item.quantity);
            TextView expiry = view.findViewById(R.id.expiry);
            if (item.expiry != null) {
                expiry.setText(DateFormat.getMediumDateFormat(getContext()).format(item.expiry));
            }
            if (!isEditing) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismissSwipe();
                        new UpdateItemDialog(getContext(), getItem(i)).show();
                    }
                });
                setupSwipeButtons(view, item);
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

        private void setupSwipeButtons(View view, final StockContentHelper.Item item) {
            final View swipeButtons = view.findViewById(R.id.swipe_buttons);
            Button swipe1 = swipeButtons.findViewById(R.id.swipe1);
            swipe1.setText(R.string.out_of_stock);
            swipe1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    item.status = StockContentHelper.ItemStatus.OUT_OF_STOCK;
                    item.expiry = null;
                    item.purchaseDate = null;
                    StockContentHelper.updateItem(getContext(), item, item.name);
                }
            });
            Button swipe2 = swipeButtons.findViewById(R.id.swipe2);
            if (item.status == StockContentHelper.ItemStatus.IN_STOCK) {
                swipe2.setText(R.string.add_to_shop);
                swipe2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        item.status = StockContentHelper.ItemStatus.TO_BUY;
                        item.purchaseDate = null;
                        item.expiry = null;
                        StockContentHelper.updateItem(getContext(), item, item.name);
                    }
                });
            } else {
                swipe2.setText(R.string.add_to_stock);
                swipe2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        item.status = StockContentHelper.ItemStatus.IN_STOCK;
                        item.purchaseDate = Calendar.getInstance().getTime();
                        StockContentHelper.updateItem(getContext(), item, item.name);
                    }
                });
            }
            view.setOnTouchListener(new OnSwipeListener() {
                @Override
                public boolean onSwipeLeft() {
                    dismissSwipe();
                    swipeButtons.setVisibility(View.VISIBLE);
                    currentVisibleSwipe = swipeButtons;
                    return true;
                }

                @Override
                public boolean onSwipeRight() {
                    dismissSwipe();
                    return true;
                }
            });
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
