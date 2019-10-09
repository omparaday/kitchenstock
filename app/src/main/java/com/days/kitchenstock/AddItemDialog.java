package com.days.kitchenstock;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.days.kitchenstock.data.StockContentHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddItemDialog extends AlertDialog {

    private final OnClickListener mCancelListener;
    private final OnClickListener mAddButtonListener;
    private EditText mName, mQuantity, mExpiry;
    private Calendar myCalendar = Calendar.getInstance();

    public AddItemDialog(@NonNull final Context context) {
        super(context);
        View view = getLayoutInflater().inflate(R.layout.add_item_dialog, null);
        setView(view);
        mName = view.findViewById(R.id.item_name);
        mQuantity = view.findViewById(R.id.quantity);
        mExpiry = view.findViewById(R.id.expiry);
        final DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = StockContentHelper.DATE_FORMATTER_INDIA;
                mExpiry.setText(sdf.format(myCalendar.getTime()));
            }

        };
        mExpiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(context, datePickerListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        setCancelable(false);
        mCancelListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                addItem();
            }
        };
        setButton(AlertDialog.BUTTON_POSITIVE, context.getResources().getString(R.string.add_item), mCancelListener);
        mAddButtonListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                cancel();
            }
        };
        setButton(AlertDialog.BUTTON_NEGATIVE, context.getResources().getString(android.R.string.cancel), mAddButtonListener);
    }

    private void addItem() {
        StockContentHelper.Item item = new StockContentHelper.Item();
        StockContentHelper.addItem(getContext(), item);
    }
}
