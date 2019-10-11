package com.days.kitchenstock;

import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.days.kitchenstock.data.StockContentHelper;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StockFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class StockFragment extends Fragment {
    public enum StockType  {
            FRESH,
        SHORT_TERM,
        LONG_TERM,
        SHOPPING_LIST
    }

    public static final String ARG_OBJECT = "obj";

    // TODO: Rename and change types of parameters
    private StockContentHelper.ItemType itemType;

    private OnFragmentInteractionListener mListener;

    public StockFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            itemType = StockContentHelper.ItemType.values()[getArguments().getInt(ARG_OBJECT)];
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stock, container, false);
    }
    @Override
    public void onViewCreated( View view,  Bundle savedInstanceState) {
        Bundle args = getArguments();
        ListView inStock = view.findViewById(R.id.in_stock);
        inStock.setAdapter(new ItemStockAdapter(itemType, true));
        ListView outOfStock = view.findViewById(R.id.out_of_stock);
        outOfStock.setAdapter(new ItemStockAdapter(itemType, false));
    }

    private class ItemStockAdapter implements ListAdapter {
        private StockContentHelper.ItemType type;
        private boolean isInStock;
        private List<StockContentHelper.Item> itemList;

        ItemStockAdapter(StockContentHelper.ItemType type, boolean isInStock) {
            this.type = type;
            this.isInStock = isInStock;
            itemList = StockContentHelper.queryItems(getContext(), type, isInStock);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public int getItemViewType(int i) {
            return 1;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public boolean isEnabled(int i) {
            return true;
        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int i) {
            return itemList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            StockContentHelper.Item item = (StockContentHelper.Item) getItem(i);
            TextView grocery = new TextView(getContext());
            grocery.setText(item.name + item.quantity);
            return grocery;
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
