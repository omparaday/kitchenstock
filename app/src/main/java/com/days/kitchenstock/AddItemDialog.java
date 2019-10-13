package com.days.kitchenstock;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;

import com.days.kitchenstock.data.StockContentHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddItemDialog extends AlertDialog {

    private OnClickListener mCancelListener;
    private OnClickListener mAddButtonListener;
    private EditText mName, mQuantity, mExpiry;
    private RadioGroup mItemTypeGroup, mItemStatusGroup;
    private Calendar myCalendar = Calendar.getInstance();
    private CheckBox mAutoOutOfStock;

    public AddItemDialog(@NonNull final Context context) {
        super(context);
        View view = getLayoutInflater().inflate(R.layout.add_item_dialog, null);
        setView(view);
        mName = view.findViewById(R.id.item_name);
        mQuantity = view.findViewById(R.id.quantity);
        mExpiry = view.findViewById(R.id.expiry);
        mAutoOutOfStock = view.findViewById(R.id.auto_move_to_out_of_stock);
        final DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = StockContentHelper.DATE_FORMATTER;
                mExpiry.setText(sdf.format(myCalendar.getTime()));
            }

        };
        mExpiry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    new DatePickerDialog(context, datePickerListener, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            }
        });
        mExpiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(context, datePickerListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
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
            };
        });
        ImageButton close = view.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        setCancelable(false);
        setupDialogButtons(context);
    }

    private void setupDialogButtons(@NonNull Context context) {
        mAddButtonListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                addItem();
            }
        };
        setButton(AlertDialog.BUTTON_POSITIVE, context.getResources().getString(R.string.add_item), mAddButtonListener);
    }

    private void addItem() {
        StockContentHelper.Item item = new StockContentHelper.Item();
        item.name = mName.getText().toString();
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
            case R.id.short_term:
                return StockContentHelper.ItemType.SHORT_TERM;
            case R.id.long_term:
                return StockContentHelper.ItemType.LONG_TERM;
        }
        return StockContentHelper.ItemType.FRESH;
    }
}
