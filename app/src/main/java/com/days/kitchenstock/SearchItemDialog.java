package com.days.kitchenstock;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.graphics.Color;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.days.kitchenstock.data.StockContentHelper;
import com.days.kitchenstock.data.StockContentProvider;

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
    private String mSearchString;
    private ArrayList<StockContentHelper.Item> mAllItemsList;
    private ContentObserver mObserver;
    private Calendar myCalendar = Calendar.getInstance();

    public SearchItemDialog(@NonNull final Context context) {
        super(context);
        View view = getLayoutInflater().inflate(R.layout.search_item_dialog, null);
        setView(view);
        mName = view.findViewById(R.id.item_name);
        mName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    SearchItemDialog.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        mResultsList = view.findViewById(R.id.results);
        mAllItemsList = StockContentHelper.getAllItems(context);
        mObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                updateSearchResults(getContext());
            }
        };
        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mSearchString = charSequence.toString();
                updateSearchResults(context);
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

    private void updateSearchResults(@NonNull Context context) {
        if (!TextUtils.isEmpty(mSearchString)) {
            ArrayList<StockContentHelper.Item> results = getResults(mSearchString);
            mResultsList.setAdapter(new ItemStockAdapter(context, results));
        } else {
            mResultsList.setAdapter(new ItemStockAdapter(context, new ArrayList<StockContentHelper.Item>()));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getContext().getContentResolver().registerContentObserver(StockContentProvider.CONTENT_URI, true, mObserver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getContext().getContentResolver().unregisterContentObserver(mObserver);
    }

    private ArrayList<StockContentHelper.Item> getResults(String searchString) {
        ArrayList<StockContentHelper.Item> results = new ArrayList<>();
        ArrayList<StockContentHelper.Item> best = new ArrayList<>();
        ArrayList<StockContentHelper.Item> better = new ArrayList<>();
        ArrayList<StockContentHelper.Item> rest = new ArrayList<>();
        for (StockContentHelper.Item item : mAllItemsList) {
            if (item.name.equals(searchString)) {
                best.add(item);
            } else if (item.name.startsWith(searchString)) {
                better.add(item);
            } else if (item.name.contains(searchString)) {
                rest.add(item);
            }
        }
        results.addAll(best);
        results.addAll(better);
        results.addAll(rest);
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
            status.setText(item.getStatusString(getContext()));
            if (item.status == StockContentHelper.ItemStatus.IN_STOCK) {
                if (item.expiry != null) {
                    expiry.setText(DateFormat.getMediumDateFormat(getContext()).format(item.expiry));
                    if (item.isExpired()) {
                        expiry.setTextColor(getContext().getResources().getColor(R.color.expired));
                    } else if (item.isExpiringSoon()) {
                        expiry.setTextColor(getContext().getResources().getColor(R.color.expiring_soon));
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
