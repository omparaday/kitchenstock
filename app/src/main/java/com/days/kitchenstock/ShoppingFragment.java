package com.days.kitchenstock;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.days.kitchenstock.data.StockContentHelper;
import com.days.kitchenstock.data.StockContentProvider;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShoppingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ShoppingFragment extends Fragment {

    private ItemStockAdapter mToBuyAdapter;
    private ItemStockAdapter mPurchasedTodayAdapter;
    private ListView mTooBuyListView;
    private ListView mPurchasedTodayListView;

    private OnFragmentInteractionListener mListener;
    private ArrayList<StockContentHelper.Item> mToBuyList;
    private ArrayList<StockContentHelper.Item> mPurchasedTodayList;
    private ContentObserver mObserver;

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
                Log.println(Log.INFO, "omprak Shopping CO", "self " + selfChange);
                updateLists();
            }
        };
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.shopping_fragment, container, false);
    }
    @Override
    public void onViewCreated( View view,  Bundle savedInstanceState) {
        Bundle args = getArguments();
        mTooBuyListView = view.findViewById(R.id.to_buy);
        mPurchasedTodayListView = view.findViewById(R.id.purchased_today);
        updateLists();
    }

    private void updateLists() {
        mToBuyList = fetchList(false);
        mToBuyAdapter = new ItemStockAdapter(getActivity(), mToBuyList);
        mTooBuyListView.setAdapter(mToBuyAdapter);
        mPurchasedTodayList = fetchList(true);
        mPurchasedTodayAdapter = new ItemStockAdapter(getActivity(), mPurchasedTodayList);
        mPurchasedTodayListView.setAdapter(mPurchasedTodayAdapter);
    }

    private ArrayList<StockContentHelper.Item> fetchList(boolean purchasedToday) {
        return StockContentHelper.queryShoppingItems(getContext(), purchasedToday);
    }

    private class ItemStockAdapter extends ArrayAdapter<StockContentHelper.Item> {
        private StockContentHelper.ItemType type;
        private boolean isInStock;
        private List<StockContentHelper.Item> itemList;

        public ItemStockAdapter(Context context, ArrayList<StockContentHelper.Item> list) {
            super(context, 0, list);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            StockContentHelper.Item item = (StockContentHelper.Item) getItem(i);
            view = getLayoutInflater().inflate(R.layout.list_item, null);
            TextView name = view.findViewById(R.id.item_name);
            name.setText(item.name);
            TextView quantity = view.findViewById(R.id.quantity);
            quantity.setText(item.quantity);
            TextView status = view.findViewById(R.id.status);
            status.setText(item.getStatusString(getContext()));
            TextView expiry = view.findViewById(R.id.expiry);
            if (item.expiry != null) {
                expiry.setText(StockContentHelper.DATE_FORMATTER.format(item.expiry));
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
