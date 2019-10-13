package com.days.kitchenstock;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;

import com.days.kitchenstock.data.StockContentHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UpdateItemDialog extends AlertDialog {

    private EditText mName, mQuantity, mExpiry;
    private RadioGroup mItemTypeGroup;
    private Calendar myCalendar = Calendar.getInstance();
    private CheckBox mAutoOutOfStock, mMoreOptionsCheckBox;
    private LinearLayout mMoreOptionsLayout;
    StockContentHelper.Item mItem;
    private Button mAddToShop, mAddToStock, mMoveToOutOfStock, mSave, mDelete;
    private RadioButton mFresh, mShortTerm, mLongTerm;

    public UpdateItemDialog(@NonNull final Context context, StockContentHelper.Item item) {
        super(context);
        if (item == null) {
            throw new NullPointerException();
        }
        mItem = item;
        View view = getLayoutInflater().inflate(R.layout.update_item_dialog, null);
        setView(view);
        mName = view.findViewById(R.id.item_name);
        mQuantity = view.findViewById(R.id.quantity);
        mExpiry = view.findViewById(R.id.expiry);
        mMoreOptionsCheckBox = view.findViewById(R.id.more_options_check);
        mMoreOptionsLayout = view.findViewById(R.id.more_options);
        setupDialogButtons(context, view);
        mMoreOptionsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mMoreOptionsLayout.setVisibility(View.VISIBLE);
                } else {
                    mMoreOptionsLayout.setVisibility(View.GONE);
                }
            }
        });
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
        mFresh = view.findViewById(R.id.fresh);
        mShortTerm = view.findViewById(R.id.short_term);
        mLongTerm = view.findViewById(R.id.long_term);
        ImageButton close = view.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        setCancelable(false);
        initViews();
    }

    private void initViews() {
        mName.setText(mItem.name);
        mQuantity.setText(mItem.quantity);
        if (mItem.expiry != null) {
            mExpiry.setText(StockContentHelper.DATE_FORMATTER.format(mItem.expiry));
        }
        switch (mItem.type) {
            case FRESH:
                mFresh.setChecked(true);
                break;
            case LONG_TERM:
                mLongTerm.setChecked(true);
                break;
            case SHORT_TERM:
                mShortTerm.setChecked(true);
                break;
        }
        mAutoOutOfStock.setChecked(mItem.autoOutOfStock);
    }

    private void setupDialogButtons(@NonNull Context context, View view) {
        mAddToShop = view.findViewById(R.id.button_add_to_shop);
        mAddToStock = view.findViewById(R.id.button_add_to_stock);
        mSave = view.findViewById(R.id.button_save);
        mMoveToOutOfStock = view.findViewById(R.id.button_move_out);
        mDelete = view.findViewById(R.id.delete);
        switch (mItem.status) {
            case IN_STOCK:
                mAddToStock.setVisibility(View.GONE);
                break;
            case OUT_OF_STOCK:
                mMoveToOutOfStock.setVisibility(View.GONE);
                break;
            case TO_BUY:
                mAddToShop.setVisibility(View.GONE);
                break;
        }
        mAddToShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItem.status = StockContentHelper.ItemStatus.TO_BUY;
                mItem.expiry = null;
                mItem.purchaseDate = null;
                updateItem();
            }
        });
        mAddToStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItem.status = StockContentHelper.ItemStatus.IN_STOCK;
                mItem.purchaseDate = myCalendar.getTime();
                try {
                    mItem.expiry = StockContentHelper.DATE_FORMATTER.parse(mExpiry.getText().toString());
                } catch (ParseException e) {
                    mItem.expiry = null;
                }
                updateItem();
            }
        });
        mMoveToOutOfStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItem.status = StockContentHelper.ItemStatus.OUT_OF_STOCK;
                mItem.expiry = null;
                mItem.purchaseDate = null;
                updateItem();
            }
        });
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mItem.expiry = StockContentHelper.DATE_FORMATTER.parse(mExpiry.getText().toString());
                } catch (ParseException e) {
                    mItem.expiry = null;
                }
                updateItem();
            }
        });
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext()).setMessage(getContext().getString(R.string.confirm_delete, mItem.name))
                        .setPositiveButton(android.R.string.yes, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteItem();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null).create().show();
            }
        });
    }

    private void updateItem() {
        String oldName = mItem.name;
        mItem.name = mName.getText().toString();
        mItem.quantity = mQuantity.getText().toString();
        mItem.type = getItemType();
        mItem.autoOutOfStock = mAutoOutOfStock.isChecked();

        StockContentHelper.updateItem(getContext(), mItem, oldName);
        dismiss();
    }

    private void deleteItem() {
        StockContentHelper.deleteItem(getContext(), mItem.name);
        dismiss();
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
