package com.days.kitchenstock;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;

import com.days.kitchenstock.data.StockContentHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AddItemDialog extends AlertDialog implements DialogInterface.OnShowListener {

    private OnClickListener mAddButtonListener;
    private EditText mQuantity, mExpiry;
    private AutoCompleteTextView mName;
    private RadioGroup mItemTypeGroup, mItemStatusGroup;
    private Calendar mExpiryCalendar = Calendar.getInstance();
    private CheckBox mAutoOutOfStock;

    public AddItemDialog(@NonNull final Context context) {
        super(context, R.style.MyDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.add_item_dialog, null);
        setView(view);
        mName = view.findViewById(R.id.item_name);

        ArrayList<String> allEasyAddItemNames = StockContentHelper.getAllEasyAddItemNames(context);
        ArrayList<String> allExistingItemNames = StockContentHelper.getExistingItemNames(context);
        ArrayList<String> addItemSuggestions = new ArrayList<String>();
        addItemSuggestions.addAll(allExistingItemNames);
        addItemSuggestions.addAll(allEasyAddItemNames);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, addItemSuggestions);
        adapter.setNotifyOnChange(true);
        mName.setAdapter(adapter);
        mName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    validateItemName();
                } else {
                    AddItemDialog.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        mQuantity = view.findViewById(R.id.quantity);
        mExpiry = view.findViewById(R.id.expiry);
        mAutoOutOfStock = view.findViewById(R.id.auto_move_to_out_of_stock);
        final DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                mExpiryCalendar.set(Calendar.YEAR, year);
                mExpiryCalendar.set(Calendar.MONTH, monthOfYear);
                mExpiryCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = StockContentHelper.DATE_FORMATTER;
                mExpiry.setText(sdf.format(mExpiryCalendar.getTime()));
            }

        };
        mExpiry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    new DatePickerDialog(context, datePickerListener, mExpiryCalendar
                            .get(Calendar.YEAR), mExpiryCalendar.get(Calendar.MONTH),
                            mExpiryCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            }
        });
        mExpiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(context, datePickerListener, mExpiryCalendar
                        .get(Calendar.YEAR), mExpiryCalendar.get(Calendar.MONTH),
                        mExpiryCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        mItemTypeGroup = view.findViewById(R.id.type_group);
        mItemStatusGroup = view.findViewById(R.id.status_group);
        mItemStatusGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.in_stock:
                        mExpiry.setVisibility(View.VISIBLE);
                        break;
                    default:
                        mExpiry.setVisibility(View.GONE);
                }
            }

            ;
        });
        Button close = view.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        setCancelable(false);
        setupDialogButtons(context);
        setOnShowListener(this);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    private boolean validateItemName() {
        String name = mName.getText().toString();
        if (TextUtils.isEmpty(name.trim())) {
            new Builder(getContext()).setMessage(R.string.name_empty_error)
                    .setPositiveButton(android.R.string.ok, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mName.requestFocus();
                        }
                    }).create().show();
            return false;
        } else {
            final StockContentHelper.Item oldItem = StockContentHelper.queryItem(getContext(), name.trim());
            if (oldItem != null) {
                new Builder(getContext()).setMessage(R.string.item_exist_error)
                        .setPositiveButton(android.R.string.yes, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new UpdateItemDialog(getContext(), oldItem).show();
                                AddItemDialog.this.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.modify_name, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mName.requestFocus();
                            }
                        }).create().show();
                return false;
            }
        }
        return true;
    }

    private void setupDialogButtons(@NonNull Context context) {
        setButton(AlertDialog.BUTTON_POSITIVE, context.getResources().getString(R.string.add_button), (OnClickListener) null);
    }

    private void addItem() {
        if (validateItemName()) {
            StockContentHelper.Item item = new StockContentHelper.Item();
            item.name = mName.getText().toString().trim();
            item.quantity = mQuantity.getText().toString();
            item.type = getItemType();
            item.status = getItemStatus();
            if (item.status == StockContentHelper.ItemStatus.IN_STOCK) {
                try {
                    item.purchaseDate = Calendar.getInstance().getTime();
                    item.expiry = StockContentHelper.DATE_FORMATTER.parse(mExpiry.getText().toString());
                } catch (ParseException e) {
                    item.expiry = null;
                }
            }
            item.autoOutOfStock = mAutoOutOfStock.isChecked();

            StockContentHelper.addItem(getContext(), item);
            dismiss();
        }
    }

    private StockContentHelper.ItemStatus getItemStatus() {
        switch (mItemStatusGroup.getCheckedRadioButtonId()) {
            case R.id.shopping:
                return StockContentHelper.ItemStatus.TO_BUY;
            case R.id.in_stock:
                return StockContentHelper.ItemStatus.IN_STOCK;
            case R.id.out_of_stock:
                return StockContentHelper.ItemStatus.OUT_OF_STOCK;
        }
        return StockContentHelper.ItemStatus.TO_BUY;
    }

    private StockContentHelper.ItemType getItemType() {
        switch (mItemTypeGroup.getCheckedRadioButtonId()) {
            case R.id.fresh:
                return StockContentHelper.ItemType.FRESH;
            case R.id.long_term:
                return StockContentHelper.ItemType.LONG_TERM;
        }
        return StockContentHelper.ItemType.FRESH;
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        getButton(BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem();
            }
        });
    }
}
