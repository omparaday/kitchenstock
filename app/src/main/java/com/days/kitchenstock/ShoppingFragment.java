package com.days.kitchenstock;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
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
public class ShoppingFragment extends Fragment implements ITabFragment {

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
    private View.OnClickListener mToBuyTitleClickListener, mPurchasedTodayTitleClickListener;
    private Button mToBuyListTitle;
    private Button mPurchasedTodayListTitle;
    private Button mToBuyEditButton;
    private Button mPurchasedTodayEditButton;
    private View mPurchasedTodayTitleButtons;
    private View mToBuyTitleButtons;
    private View mToBuyOtherButtonsLayout, mPurchasedTodayOtherButtonsLayout;
    private View mToBuyOptionButtonsLayout, mPurchasedTodayOptionButtonsLayout;
    private Button mToBuyShareButton, mPurchasedTodayShareButton;
    private Button mCancelEditingToBuyButton;
    private View mToBuyActionButtons;
    private Button mCancelEditingPurchasedTodayButton;
    private View mPurchasedTodayActionButtons;

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
        if (mToBuyAdapter != null) {
            mToBuyAdapter.dismissSwipe();
        }
        if (mPurchasedTodayAdapter != null) {
            mPurchasedTodayAdapter.dismissSwipe();
        }
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
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mToBuyAdapter != null) {
                        mToBuyAdapter.dismissSwipe();
                    }
                }
                return false;
            }
        });

        mPurchasedTodayListView = view.findViewById(R.id.purchased_today);
        mPurchasedTodayListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mPurchasedTodayAdapter != null) {
                        mPurchasedTodayAdapter.dismissSwipe();
                    }
                }
                return false;
            }
        });
        mToBuyTitleClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mToBuyLayout.getVisibility() != View.VISIBLE) {
                    mToBuyListTitle.setText(R.string.to_buy_expand);
                    mPurchasedTodayListTitle.setText(R.string.purchased_today_collapse);
                    mToBuyLayout.setVisibility(View.VISIBLE);
                    mPurhcasedTodayLayout.setVisibility(View.GONE);
                    mToBuyOtherButtonsLayout.setVisibility(View.VISIBLE);
                    mPurchasedTodayOtherButtonsLayout.setVisibility(View.GONE);
                    if (mPurchasedTodayAdapter != null) {
                        mPurchasedTodayAdapter.dismissSwipe();
                    }
                }
            }
        };
        mPurchasedTodayTitleClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPurhcasedTodayLayout.getVisibility() != View.VISIBLE) {
                    mToBuyListTitle.setText(R.string.to_buy_collapse);
                    mPurchasedTodayListTitle.setText(R.string.purchased_today_expand);
                    mPurhcasedTodayLayout.setVisibility(View.VISIBLE);
                    mToBuyLayout.setVisibility(View.GONE);
                    mToBuyOtherButtonsLayout.setVisibility(View.GONE);
                    mPurchasedTodayOtherButtonsLayout.setVisibility(View.VISIBLE);
                    if (mToBuyAdapter != null) {
                        mToBuyAdapter.dismissSwipe();
                    }
                }
            }
        };
        setupToBuyLayoutButtons(view);
        setupPurchasedTodayLayoutButtons(view);
        updateLists();
    }

    private void setupToBuyLayoutButtons(View view) {
        mToBuyTitleButtons = view.findViewById(R.id.to_buy_title_buttons);
        mToBuyOtherButtonsLayout = mToBuyTitleButtons.findViewById(R.id.other_buttons);
        mToBuyOptionButtonsLayout = mToBuyOtherButtonsLayout.findViewById(R.id.options_buttons);
        mToBuyShareButton = mToBuyOptionButtonsLayout.findViewById(R.id.share);
        mToBuyShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String shareBody = getString(R.string.share_to_buy);
                ArrayList<StockContentHelper.Item> itemArrayList = mToBuyAdapter.getSelectedItems();
                int sNo = 1;
                for (StockContentHelper.Item item : itemArrayList) {
                    shareBody = shareBody + "\n";
                    shareBody = shareBody + sNo + ". ";
                    sNo++;
                    shareBody = shareBody + item.name;
                    if (!TextUtils.isEmpty(item.quantity)) {
                        shareBody = shareBody + ", " + item.quantity;
                    }
                }
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_to_buy));
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_to_buy)));
            }
        });
        mToBuyEditButton = mToBuyTitleButtons.findViewById(R.id.edit);
        mCancelEditingToBuyButton = mToBuyTitleButtons.findViewById(R.id.cancel);
        mToBuyListTitle = mToBuyTitleButtons.findViewById(R.id.list_title);
        mToBuyActionButtons = view.findViewById(R.id.to_buy_action_buttons);
        mToBuyListTitle.setText(R.string.to_buy_expand);
        mToBuyListTitle.setOnClickListener(mToBuyTitleClickListener);
        mToBuyEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mToBuyListTitle.setOnClickListener(null);
                mToBuyEditButton.setVisibility(View.GONE);
                mPurchasedTodayTitleButtons.setVisibility(View.GONE);
                mCancelEditingToBuyButton.setVisibility(View.VISIBLE);
                mToBuyActionButtons.setVisibility(View.VISIBLE);
                updateToBuyList(true);
                mIsToBuyEditing = true;
            }
        });
        mCancelEditingToBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitEditingToBuyList();
            }
        });


        final Button deleteSelectedItems = mToBuyActionButtons.findViewById(R.id.delete);
        final Button moveToInStockSelectedItems = mToBuyActionButtons.findViewById(R.id.button3);
        moveToInStockSelectedItems.setText(R.string.add_to_stock);
        final Button moveToOutOfStockSelectedItems = mToBuyActionButtons.findViewById(R.id.button2);
        moveToOutOfStockSelectedItems.setText(R.string.move_to_out_of_stock);
        deleteSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mToBuyAdapter.getSelectedItems();
                StockContentHelper.deleteItemList(getContext(), selectedItems);
                exitEditingToBuyList();
            }
        });
        moveToInStockSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mToBuyAdapter.getSelectedItems();
                StockContentHelper.moveToInStockList(getContext(), selectedItems);
                exitEditingToBuyList();
            }
        });
        moveToOutOfStockSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mToBuyAdapter.getSelectedItems();
                StockContentHelper.moveToOutOfStockList(getContext(), selectedItems);
                exitEditingToBuyList();
            }
        });
    }


    private void setupPurchasedTodayLayoutButtons(View view) {
        mPurchasedTodayTitleButtons = view.findViewById(R.id.purchased_today_title_buttons);
        mPurchasedTodayOtherButtonsLayout = mPurchasedTodayTitleButtons.findViewById(R.id.other_buttons);
        mPurchasedTodayOptionButtonsLayout = mPurchasedTodayOtherButtonsLayout.findViewById(R.id.options_buttons);
        mPurchasedTodayShareButton = mPurchasedTodayOptionButtonsLayout.findViewById(R.id.share);
        mPurchasedTodayShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String shareBody = getString(R.string.share_purchased_today);
                ArrayList<StockContentHelper.Item> itemArrayList = mPurchasedTodayAdapter.getSelectedItems();
                int sNo = 1;
                for (StockContentHelper.Item item : itemArrayList) {
                    shareBody = shareBody + "\n";
                    shareBody = shareBody + sNo + ". ";
                    sNo++;
                    shareBody = shareBody + item.name;
                    if (!TextUtils.isEmpty(item.quantity)) {
                        shareBody = shareBody + ", " + item.quantity;
                    }
                }
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_purchased_today));
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_purchased_today)));
            }
        });
        mPurchasedTodayActionButtons = view.findViewById(R.id.purchased_today_action_buttons);
        mPurchasedTodayEditButton = mPurchasedTodayTitleButtons.findViewById(R.id.edit);
        mPurchasedTodayOtherButtonsLayout.setVisibility(View.GONE);
        mCancelEditingPurchasedTodayButton = mPurchasedTodayTitleButtons.findViewById(R.id.cancel);
        mPurchasedTodayListTitle = mPurchasedTodayTitleButtons.findViewById(R.id.list_title);
        mPurchasedTodayListTitle.setText(R.string.purchased_today_collapse);
        mPurchasedTodayListTitle.setOnClickListener(mPurchasedTodayTitleClickListener);
        mPurchasedTodayEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPurchasedTodayListTitle.setOnClickListener(null);
                mPurchasedTodayEditButton.setVisibility(View.GONE);
                mToBuyTitleButtons.setVisibility(View.GONE);
                mCancelEditingPurchasedTodayButton.setVisibility(View.VISIBLE);
                mPurchasedTodayActionButtons.setVisibility(View.VISIBLE);
                updatePurchasedTodayList(true);
                mIsPurchasedTodayEditing = true;
            }
        });
        mCancelEditingPurchasedTodayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitEditingPurchasedTodayList();
            }
        });


        final Button deleteToBuySelectedItems = mPurchasedTodayActionButtons.findViewById(R.id.delete);
        final Button moveToShopSelectedItems = mPurchasedTodayActionButtons.findViewById(R.id.button3);
        moveToShopSelectedItems.setText(R.string.add_to_shop);
        final Button moveToOutOfStockSelectedItems = mPurchasedTodayActionButtons.findViewById(R.id.button2);
        moveToOutOfStockSelectedItems.setText(R.string.move_to_out_of_stock);
        deleteToBuySelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mPurchasedTodayAdapter.getSelectedItems();
                StockContentHelper.deleteItemList(getContext(), selectedItems);
                exitEditingPurchasedTodayList();
            }
        });
        moveToShopSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mPurchasedTodayAdapter.getSelectedItems();
                StockContentHelper.moveToShopList(getContext(), selectedItems);
                exitEditingPurchasedTodayList();
            }
        });
        moveToOutOfStockSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mPurchasedTodayAdapter.getSelectedItems();
                StockContentHelper.moveToOutOfStockList(getContext(), selectedItems);
                exitEditingPurchasedTodayList();
            }
        });
    }

    private void exitEditingToBuyList() {
        mCancelEditingToBuyButton.setVisibility(View.GONE);
        mToBuyActionButtons.setVisibility(View.GONE);
        mToBuyEditButton.setVisibility(View.VISIBLE);
        mPurchasedTodayTitleButtons.setVisibility(View.VISIBLE);
        mIsToBuyEditing = false;
        mToBuyListTitle.setOnClickListener(mToBuyTitleClickListener);
        updateToBuyList(false);
    }

    private void exitEditingPurchasedTodayList() {
        mCancelEditingPurchasedTodayButton.setVisibility(View.GONE);
        mPurchasedTodayActionButtons.setVisibility(View.GONE);
        mPurchasedTodayEditButton.setVisibility(View.VISIBLE);
        mToBuyTitleButtons.setVisibility(View.VISIBLE);
        mIsPurchasedTodayEditing = false;
        mPurchasedTodayListTitle.setOnClickListener(mPurchasedTodayTitleClickListener);
        updatePurchasedTodayList(false);
    }

    private void updateToBuyList(boolean editMode) {
        if (!mIsToBuyEditing) {
            mToBuyList = fetchList(false);
            if (mToBuyList.size() != 0) {
                mToBuyAdapter = new ItemStockAdapter(getActivity(), mToBuyList, editMode);
                mToBuyListView.setVisibility(View.VISIBLE);
                mToBuyEmptyMessage.setVisibility(View.GONE);
                if (!editMode) {
                    mToBuyOptionButtonsLayout.setVisibility(View.VISIBLE);
                }
                mToBuyListView.setAdapter(mToBuyAdapter);
            } else {
                mToBuyListView.setVisibility(View.GONE);
                mToBuyOptionButtonsLayout.setVisibility(View.GONE);
                mToBuyAdapter = null;
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
                if (!editMode) {
                    mPurchasedTodayOptionButtonsLayout.setVisibility(View.VISIBLE);
                }
            } else {
                mPurchasedTodayListView.setVisibility(View.GONE);
                mPurchasedTodayAdapter = null;
                mPurchasedTodayOptionButtonsLayout.setVisibility(View.GONE);
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

    @Override
    public void onTabChanged() {
        if (mIsToBuyEditing) {
            exitEditingToBuyList();
        } else if (mIsPurchasedTodayEditing) {
            exitEditingPurchasedTodayList();
        } else {
            if (mPurchasedTodayAdapter != null) {
                mPurchasedTodayAdapter.dismissSwipe();
            }
            if (mToBuyAdapter != null) {
                mToBuyAdapter.dismissSwipe();
            }
        }
    }

    private class ItemStockAdapter extends ArrayAdapter<StockContentHelper.Item> {
        private boolean isEditing;
        private ArrayList<StockContentHelper.Item> itemList;
        private boolean[] checkedValues;
        private View currentVisibleSwipe;

        private boolean dismissSwipe() {
            if (currentVisibleSwipe != null) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(((LinearLayout) currentVisibleSwipe).getLayoutParams());
                params.width = 0;
                currentVisibleSwipe.setLayoutParams(params);
                currentVisibleSwipe.invalidate();
                currentVisibleSwipe = null;
                return true;
            }
            return false;
        }

        public ItemStockAdapter(Context context, ArrayList<StockContentHelper.Item> list, boolean isEditing) {
            super(context, 0, list);
            this.isEditing = isEditing;
            checkedValues = new boolean[list.size()];
            itemList = list;
        }

        ArrayList<StockContentHelper.Item> getSelectedItems() {
            if (!isEditing) {
                return itemList;
            }
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
            if (!TextUtils.isEmpty(item.quantity)) {
                view.findViewById(R.id.summary_layout).setVisibility(View.VISIBLE);
                quantity.setText(item.quantity);
            }
            TextView expiry = view.findViewById(R.id.expiry);
            if (item.expiry != null) {
                view.findViewById(R.id.summary_layout).setVisibility(View.VISIBLE);
                expiry.setText(DateFormat.getMediumDateFormat(getContext()).format(item.expiry));
            }
            if (!isEditing) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!dismissSwipe()) {
                            new UpdateItemDialog(getContext(), getItem(i)).show();
                        }
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

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return false;
                }
            });
            view.setOnTouchListener(new OnSwipeListener() {
                @Override
                public boolean onSwipeLeft() {
                    dismissSwipe();
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(swipeButtons.getLayoutParams());
                    params.width = (int) convertDpToPixel(BUTTONS_WIDTH, getContext());
                    swipeButtons.setLayoutParams(params);
                    swipeButtons.invalidate();
                    currentVisibleSwipe = swipeButtons;
                    return true;
                }

                public boolean onMoveLeft(float deltaX) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(swipeButtons.getLayoutParams());
                    params.width = (int) Math.min(convertDpToPixel(BUTTONS_WIDTH, getContext()), deltaX);
                    swipeButtons.setLayoutParams(params);
                    swipeButtons.invalidate();
                    currentVisibleSwipe = swipeButtons;
                    return true;
                }

                public boolean onDown() {
                    return dismissSwipe();
                }

                public boolean onCancel() {
                    dismissSwipe();
                    return false;
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
