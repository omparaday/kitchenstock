package com.days.kitchenstock;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.days.kitchenstock.data.StockContentHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SearchItemDialog extends AlertDialog {

    private EditText mName;
    private ListView mResultsList;
    private ArrayList<StockContentHelper.Item> mAllItemsList;
    private Calendar myCalendar = Calendar.getInstance();

    public SearchItemDialog(@NonNull final Context context) {
        super(context);
        View view = getLayoutInflater().inflate(R.layout.search_item_dialog, null);
        setView(view);
        mName = view.findViewById(R.id.item_name);
        mResultsList = view.findViewById(R.id.results);
        mAllItemsList = StockContentHelper.getAllItems(context);
        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String searchString = charSequence.toString();
                if (!TextUtils.isEmpty(searchString)) {
                    ArrayList<StockContentHelper.Item> results = getResults(searchString);
                    mResultsList.setAdapter(new ItemStockAdapter(context, results));
                } else {
                    mResultsList.setAdapter(new ItemStockAdapter(context, new ArrayList<StockContentHelper.Item>()));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        ImageButton close = view.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        setCancelable(false);
    }

    private ArrayList<StockContentHelper.Item> getResults(String searchString) {
        ArrayList<StockContentHelper.Item> results = new ArrayList<>();
        for (StockContentHelper.Item item : mAllItemsList) {
            if (item.name.contains(searchString)) {
                results.add(item);
            }
        }
        return results;
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
                    Date today = Calendar.getInstance().getTime();
                    if (item.expiry.before(today)) {
                        status.setText(R.string.expired);
                        status.setTextColor(Color.parseColor("#ffcc0000"));
                    } else {
                        long totalDays = TimeUnit.DAYS.convert(item.expiry.getTime() - item.purchaseDate.getTime(), TimeUnit.MILLISECONDS);
                        long remainingDays = TimeUnit.DAYS.convert(item.expiry.getTime() - Calendar.getInstance().getTime().getTime(), TimeUnit.MILLISECONDS);
                        if (remainingDays < totalDays / StockContentHelper.EXPIRING_SOON_DIVISOR) {
                            status.setTextColor(Color.parseColor("#ff990000"));
                            status.setText(R.string.expiring_soon);
                        }
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
}
