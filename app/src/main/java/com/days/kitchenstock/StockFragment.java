package com.days.kitchenstock;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Color;
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
import android.widget.ListView;
import android.widget.TextView;

import com.days.kitchenstock.data.StockContentHelper;
import com.days.kitchenstock.data.StockContentProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


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
                Log.println(Log.INFO, "omprak CO", "self " + selfChange);
                updateLists();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.stock_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Bundle args = getArguments();
        mInStockListView = view.findViewById(R.id.in_stock);
        mOutOfStockListView = view.findViewById(R.id.out_of_stock);
        TextView inStockListTitle = view.findViewById(R.id.in_stock_list_title);
        TextView outOfStockListTitle = view.findViewById(R.id.out_of_stock_list_title);
        View.OnClickListener titleClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInStockListView.getVisibility() == View.VISIBLE) {
                    mOutOfStockListView.setVisibility(View.VISIBLE);
                    mInStockListView.setVisibility(View.GONE);
                } else {
                    mInStockListView.setVisibility(View.VISIBLE);
                    mOutOfStockListView.setVisibility(View.GONE);
                }
            }
        };
        inStockListTitle.setOnClickListener(titleClickListener);
        outOfStockListTitle.setOnClickListener(titleClickListener);
        updateLists();

    }

    @Override
    public void onStart() {
        super.onStart();
        getContext().getContentResolver().registerContentObserver(StockContentProvider.CONTENT_URI, true, mObserver);

    }

    @Override
    public void onStop() {
        super.onStop();
        getContext().getContentResolver().unregisterContentObserver(mObserver);

    }

    private void updateLists() {
        mInStockList = fetchList(true);
        Collections.sort(mInStockList);
        mInStockAdapter = new ItemStockAdapter(getContext(), mInStockList);
        mInStockListView.setAdapter(mInStockAdapter);
        mOutOfStockList = fetchList(false);
        mOutOfStockAdapter = new ItemStockAdapter(getContext(), mOutOfStockList);
        mOutOfStockListView.setAdapter(mOutOfStockAdapter);
    }

    private ArrayList<StockContentHelper.Item> fetchList(boolean isInStock) {
        return StockContentHelper.queryItems(getContext(), itemType, isInStock);
    }

    private class ItemStockAdapter extends ArrayAdapter<StockContentHelper.Item> {
        private StockContentHelper.ItemType type;
        private boolean isInStock;
        private List<StockContentHelper.Item> itemList;

        public ItemStockAdapter(Context context, ArrayList<StockContentHelper.Item> list) {
            super(context, R.layout.list_item, list);
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            StockContentHelper.Item item = (StockContentHelper.Item) getItem(i);
            view = getLayoutInflater().inflate(R.layout.list_item, null);
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
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new UpdateItemDialog(getContext(), getItem(i)).show();
                }
            });
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
