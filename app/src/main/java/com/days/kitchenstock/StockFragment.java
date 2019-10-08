package com.days.kitchenstock;

import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.days.kitchenstock.data.StockContentHelper;

import java.util.ArrayList;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StockFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StockFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StockFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public enum StockType  {
            FRESH,
        SHORT_TERM,
        LONG_TERM,
        SHOPPING_LIST
    }

    public static final String ARG_OBJECT = "obj";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public StockFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StockFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StockFragment newInstance(String param1, String param2) {
        StockFragment fragment = new StockFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
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
        TextView inStockItem = new TextView(getActivity());
        inStockItem.setText("Tomato");
        inStock.setAdapter(new InStockAdapter());
        //inStock.addView(inStockItem);
        ListView outOfStock = view.findViewById(R.id.out_of_stock);
        TextView ooItem = new TextView(getActivity());
        inStockItem.setText("Garlic");
        Button add = view.findViewById(R.id.add_item);
        final StockContentHelper.Item item = new StockContentHelper.Item();
        item.name = "beans";
        item.type = StockContentHelper.ItemType.LONG_TERM;
        item.status = StockContentHelper.ItemStatus.IN_STOCK;
        item.autoAddToCart = true;
        item.autoOutOfStock = true;
        Date date = new Date();
        date.setTime(System.currentTimeMillis());
        item.expiry = date;
        date.setTime(System.currentTimeMillis() + 84600);
        item.purchaseDate = date;
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StockContentHelper.deleteItem(getContext(), item.name);
            }
        });
        final TextView textView = view.findViewById(R.id.item_list);
        Button retrive = view.findViewById(R.id.retrieve_item);
        retrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<StockContentHelper.Item> itemArrayList = StockContentHelper.queryAllItems(getContext());
                String allItems = "";
                for (StockContentHelper.Item item : itemArrayList) {
                    allItems = allItems + "\n" + item.toString();
                }
                textView.setText(allItems);
            }
        });

        //inStock.addView(ooItem);
    }

    private class InStockAdapter implements ListAdapter {

        @Override
        public boolean areAllItemsEnabled() {
            return false;
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
            return false;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Object getItem(int i) {
            if (i == 0) {
                return new String("tomato");
            } else if (i==1) {
                return new String ("onion");
            }
            return null;
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
            CheckBox grocery = new CheckBox(getContext());
            grocery.setText((String) getItem(i));
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
