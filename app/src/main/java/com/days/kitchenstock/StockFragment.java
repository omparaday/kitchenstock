package com.days.kitchenstock;

import android.content.Context;
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
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StockFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class StockFragment extends Fragment implements ITabFragment {

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
    private View.OnClickListener mInStockTitleClickListener, mOutOfStockTitleClickListener;
    private Button mInStockListTitle;
    private Button mOutOfStockListTitle;
    private Button mInStockEditButton;
    private Button mOutOfStockEditButton;
    private View mInStockEmptyMessage, mOutOfStockEmptyMessage;
    private View mInStockLayout, mOutOfStockLayout;
    private View mOutOfStockTitleButtons;
    private View mInStockTitleButtons;
    private View mInStockEditCanceLayout;
    private View mOutOfStockEditCancelLayout;
    private Button mCancelEditingInStockButton;
    private View mInStockActionButtons;
    private View mOutOfStockActionButtons;
    private Button mCancelEditingOutOfStockButton;

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
        mInStockLayout = view.findViewById(R.id.in_stock_layout);
        mOutOfStockLayout = view.findViewById(R.id.out_of_stock_layout);
        mInStockEmptyMessage = view.findViewById(R.id.empty_in_stock_message);
        mOutOfStockEmptyMessage = view.findViewById(R.id.empty_out_of_stock_message);
        mInStockListView = view.findViewById(R.id.in_stock);
        mInStockListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mInStockAdapter != null) {
                        mInStockAdapter.dismissSwipe();
                    }
                }
                return false;
            }
        });
        mOutOfStockListView = view.findViewById(R.id.out_of_stock);
        mOutOfStockListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mOutOfStockAdapter != null) {
                        mOutOfStockAdapter.dismissSwipe();
                    }
                }
                return false;
            }
        });
        mInStockTitleClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInStockLayout.getVisibility() != View.VISIBLE) {
                    mInStockListTitle.setText(R.string.in_stock_expand);
                    mOutOfStockListTitle.setText(R.string.out_of_stock_collapse);
                    mInStockLayout.setVisibility(View.VISIBLE);
                    mOutOfStockLayout.setVisibility(View.GONE);
                    mInStockEditCanceLayout.setVisibility(View.VISIBLE);
                    mOutOfStockEditCancelLayout.setVisibility(View.GONE);
                    if (mOutOfStockAdapter != null) {
                        mOutOfStockAdapter.dismissSwipe();
                    }
                }
            }
        };
        mOutOfStockTitleClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOutOfStockLayout.getVisibility() != View.VISIBLE) {
                    mInStockListTitle.setText(R.string.in_stock_collapse);
                    mOutOfStockListTitle.setText(R.string.out_of_stock_expand);
                    mOutOfStockLayout.setVisibility(View.VISIBLE);
                    mInStockLayout.setVisibility(View.GONE);
                    mInStockEditCanceLayout.setVisibility(View.GONE);
                    mOutOfStockEditCancelLayout.setVisibility(View.VISIBLE);
                    if (mInStockAdapter != null) {
                        mInStockAdapter.dismissSwipe();
                    }
                }
            }
        };
        setupInStockLayoutButtons(view);
        setupOutOfStockLayoutButtons(view);
        updateLists();

    }

    private void setupInStockLayoutButtons(View view) {
        mInStockTitleButtons = view.findViewById(R.id.in_stock_title_buttons);
        mInStockEditCanceLayout = mInStockTitleButtons.findViewById(R.id.other_buttons);
        mInStockEditButton = mInStockTitleButtons.findViewById(R.id.edit);
        mCancelEditingInStockButton = mInStockTitleButtons.findViewById(R.id.cancel);
        mInStockListTitle = mInStockTitleButtons.findViewById(R.id.list_title);
        mInStockActionButtons = view.findViewById(R.id.in_stock_action_buttons);
        mInStockListTitle.setText(R.string.in_stock_expand);
        mInStockListTitle.setOnClickListener(mInStockTitleClickListener);
        mInStockEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInStockListTitle.setOnClickListener(null);
                mInStockEditButton.setVisibility(View.GONE);
                mOutOfStockTitleButtons.setVisibility(View.GONE);
                mCancelEditingInStockButton.setVisibility(View.VISIBLE);
                mInStockActionButtons.setVisibility(View.VISIBLE);
                updateInStockList(true);
                mIsInStockEditing = true;
            }
        });
        mCancelEditingInStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitEditingInStockList();
            }
        });


        final Button deleteSelectedItems = mInStockActionButtons.findViewById(R.id.delete);
        final Button moveToShopSelectedItems = mInStockActionButtons.findViewById(R.id.button3);
        moveToShopSelectedItems.setText(R.string.add_to_shop);
        final Button moveToOutOfStockSelectedItems = mInStockActionButtons.findViewById(R.id.button2);
        moveToOutOfStockSelectedItems.setText(R.string.move_to_out_of_stock);
        deleteSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mInStockAdapter.getSelectedItems();
                StockContentHelper.deleteItemList(getContext(), selectedItems);
                exitEditingInStockList();
            }
        });
        moveToShopSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mInStockAdapter.getSelectedItems();
                StockContentHelper.moveToShopList(getContext(), selectedItems);
                exitEditingInStockList();
            }
        });
        moveToOutOfStockSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mInStockAdapter.getSelectedItems();
                StockContentHelper.moveToOutOfStockList(getContext(), selectedItems);
                exitEditingInStockList();
            }
        });
    }


    private void setupOutOfStockLayoutButtons(View view) {
        mOutOfStockTitleButtons = view.findViewById(R.id.out_of_stock_title_buttons);
        mOutOfStockEditCancelLayout = mOutOfStockTitleButtons.findViewById(R.id.other_buttons);
        mOutOfStockActionButtons = view.findViewById(R.id.out_of_stock_action_buttons);
        mOutOfStockEditButton = mOutOfStockTitleButtons.findViewById(R.id.edit);
        mOutOfStockEditCancelLayout.setVisibility(View.GONE);
        mCancelEditingOutOfStockButton = mOutOfStockTitleButtons.findViewById(R.id.cancel);
        mOutOfStockListTitle = mOutOfStockTitleButtons.findViewById(R.id.list_title);
        mOutOfStockListTitle.setText(R.string.out_of_stock_collapse);
        mOutOfStockListTitle.setOnClickListener(mOutOfStockTitleClickListener);
        mOutOfStockEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOutOfStockListTitle.setOnClickListener(null);
                mOutOfStockEditButton.setVisibility(View.GONE);
                mInStockTitleButtons.setVisibility(View.GONE);
                mCancelEditingOutOfStockButton.setVisibility(View.VISIBLE);
                mOutOfStockActionButtons.setVisibility(View.VISIBLE);
                updateOutOfStockList(true);
                mIsOutOfStockEditing = true;
            }
        });
        mCancelEditingOutOfStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitEditingOutOfStockList();
            }
        });


        final Button deleteSelectedItems = mOutOfStockActionButtons.findViewById(R.id.delete);
        final Button moveToShopSelectedItems = mOutOfStockActionButtons.findViewById(R.id.button3);
        moveToShopSelectedItems.setText(R.string.add_to_shop);
        final Button moveToInStockSelectedItems = mOutOfStockActionButtons.findViewById(R.id.button2);
        moveToInStockSelectedItems.setText(R.string.add_to_stock);
        deleteSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mOutOfStockAdapter.getSelectedItems();
                StockContentHelper.deleteItemList(getContext(), selectedItems);
                exitEditingOutOfStockList();
            }
        });
        moveToShopSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mOutOfStockAdapter.getSelectedItems();
                StockContentHelper.moveToShopList(getContext(), selectedItems);
                exitEditingOutOfStockList();
            }
        });
        moveToInStockSelectedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> selectedItems = mOutOfStockAdapter.getSelectedItems();
                StockContentHelper.moveToInStockList(getContext(), selectedItems);
                exitEditingOutOfStockList();
            }
        });
    }

    private void exitEditingInStockList() {
        mCancelEditingInStockButton.setVisibility(View.GONE);
        mInStockActionButtons.setVisibility(View.GONE);
        mOutOfStockTitleButtons.setVisibility(View.VISIBLE);
        mIsInStockEditing = false;
        mInStockListTitle.setOnClickListener(mInStockTitleClickListener);
        updateInStockList(false);
    }

    private void exitEditingOutOfStockList() {
        mCancelEditingOutOfStockButton.setVisibility(View.GONE);
        mOutOfStockActionButtons.setVisibility(View.GONE);
        mInStockTitleButtons.setVisibility(View.VISIBLE);
        mIsOutOfStockEditing = false;
        mOutOfStockListTitle.setOnClickListener(mOutOfStockTitleClickListener);
        updateOutOfStockList(false);
    }


    private void updateInStockList(boolean editMode) {
        if (!mIsInStockEditing) {
            mInStockList = fetchList(true);
            if (mInStockList.size() != 0) {
                mInStockListView.setVisibility(View.VISIBLE);
                mInStockEmptyMessage.setVisibility(View.GONE);
                Collections.sort(mInStockList);
                if (!editMode) {
                    mInStockEditButton.setVisibility(View.VISIBLE);
                }
                mInStockAdapter = new ItemStockAdapter(getActivity(), mInStockList, editMode);
                mInStockListView.setAdapter(mInStockAdapter);
            } else {
                mInStockListView.setVisibility(View.GONE);
                mInStockAdapter = null;
                mInStockEditButton.setVisibility(View.GONE);
                mInStockEmptyMessage.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateOutOfStockList(boolean editMode) {
        if (!mIsOutOfStockEditing) {
            mOutOfStockList = fetchList(false);
            if (mOutOfStockList.size() != 0) {
                mOutOfStockEmptyMessage.setVisibility(View.GONE);
                mOutOfStockListView.setVisibility(View.VISIBLE);
                mOutOfStockAdapter = new ItemStockAdapter(getActivity(), mOutOfStockList, editMode);
                if(!editMode) {
                    mOutOfStockEditButton.setVisibility(View.VISIBLE);
                }
                mOutOfStockListView.setAdapter(mOutOfStockAdapter);
            } else {
                mOutOfStockEmptyMessage.setVisibility(View.VISIBLE);
                mOutOfStockAdapter = null;
                mOutOfStockEditButton.setVisibility(View.GONE);
                mOutOfStockListView.setVisibility(View.GONE);
            }
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

    @Override
    public void onTabChanged() {
        if (mIsInStockEditing) {
            exitEditingInStockList();
        } else if (mIsOutOfStockEditing) {
            exitEditingOutOfStockList();
        } else {
            if (mInStockAdapter != null) {
                mInStockAdapter.dismissSwipe();
            }
            if (mOutOfStockAdapter != null) {
                mOutOfStockAdapter.dismissSwipe();
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
            String statusString = null;
            if (item.status == StockContentHelper.ItemStatus.TO_BUY) {
                statusString = item.getStatusString(getContext());
                status.setText(statusString);
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
            if (!TextUtils.isEmpty(item.quantity) || item.expiry != null || !TextUtils.isEmpty(statusString)) {
                view.findViewById(R.id.summary_layout).setVisibility(View.VISIBLE);
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
            Button swipe2 = swipeButtons.findViewById(R.id.swipe2);
            Button swipe1 = swipeButtons.findViewById(R.id.swipe1);
            if (item.status != StockContentHelper.ItemStatus.TO_BUY) {
                swipe2.setText(R.string.add_to_shop);
                swipe2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        item.status = StockContentHelper.ItemStatus.TO_BUY;
                        item.expiry = null;
                        item.purchaseDate = null;
                        StockContentHelper.updateItem(getContext(), item, item.name);
                    }
                });
                if (item.status == StockContentHelper.ItemStatus.IN_STOCK) {
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
                } else {
                    swipe1.setText(R.string.add_to_stock);
                    swipe1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            item.status = StockContentHelper.ItemStatus.IN_STOCK;
                            item.purchaseDate = Calendar.getInstance().getTime();
                            StockContentHelper.updateItem(getContext(), item, item.name);
                        }
                    });
                }
            } else {
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
                    params.width = (int)convertDpToPixel(BUTTONS_WIDTH, getContext());
                    swipeButtons.setLayoutParams(params);
                    swipeButtons.invalidate();
                    currentVisibleSwipe = swipeButtons;
                    return true;
                }
                public boolean onMoveLeft(float deltaX){
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(swipeButtons.getLayoutParams());
                    params.width = (int) Math.min(convertDpToPixel(BUTTONS_WIDTH, getContext()), deltaX);
                    swipeButtons.setLayoutParams(params);
                    swipeButtons.invalidate();
                    currentVisibleSwipe = swipeButtons;
                    return true;
                }

                public boolean onDown(){
                    return dismissSwipe();
                }

                public boolean onCancel(){
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
