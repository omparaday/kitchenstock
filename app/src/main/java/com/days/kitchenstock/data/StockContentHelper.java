package com.days.kitchenstock.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.days.kitchenstock.R;

public class StockContentHelper {

    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");

    public enum ItemType {
        FRESH(), SHORT_TERM(), LONG_TERM();
        private final int value;
        private ItemType() {
            value = ordinal();
        }
        public int getValue() {
            return value;
        }
    };
    public enum ItemStatus {
        IN_STOCK(), OUT_OF_STOCK(), TO_BUY();
        private final int value;
        private ItemStatus() {
            value = ordinal();
        }
        public int getValue() {
            return value;
        }
    };

    public static class Item {
        public String name;
        public ItemType type;
        public ItemStatus status;
        public Date expiry;
        public Date purchaseDate;
        public String quantity;
        public boolean autoOutOfStock;

        @NonNull
        @Override
        public String toString() {
            return name + "," + type + "," + status + "," + (expiry == null ? "null" : DATE_FORMATTER.format(expiry)) + "," + (purchaseDate == null ? "null" : DATE_FORMATTER.format(purchaseDate)) + "," + quantity + "," + autoOutOfStock;
        }

        public Item setName(String nameArg) {
            name = nameArg;
            return this;
        }

        public Item setItemType(ItemType itemType) {
            type = itemType;
            return this;
        }

        public String getStatusString(Context context) {
            switch (status) {
                case TO_BUY:
                    return context.getString(R.string.to_buy);
                case IN_STOCK:
                    return context.getString(R.string.in_stock);
                case OUT_OF_STOCK:
                    return context.getString(R.string.out_of_stock);
            }
            return null;
        }
    }

    public static void addItem (Context context, Item item) {
        ContentValues values = new ContentValues();
        values.put(StockContentProvider.NAME, item.name);
        values.put(StockContentProvider.TYPE, item.type.getValue());
        values.put(StockContentProvider.STATUS, item.status.getValue());
        values.put(StockContentProvider.EXPIRY, item.expiry == null ? null : DATE_FORMATTER.format(item.expiry));
        values.put(StockContentProvider.PURCHASE_DATE, item.purchaseDate == null ? null : DATE_FORMATTER.format(item.purchaseDate));
        values.put(StockContentProvider.QUANTITY, item.quantity);
        values.put(StockContentProvider.AUTO_OUT_OF_STOCK, item.autoOutOfStock);

        Uri uri = context.getContentResolver().insert(
                StockContentProvider.CONTENT_URI, values);
    }

    public static void updateItem (Context context, Item item, String oldName) {
        ContentValues values = new ContentValues();
        values.put(StockContentProvider.NAME, item.name);
        values.put(StockContentProvider.TYPE, item.type.getValue());
        values.put(StockContentProvider.STATUS, item.status.getValue());
        values.put(StockContentProvider.EXPIRY, item.expiry == null ? null : DATE_FORMATTER.format(item.expiry));
        values.put(StockContentProvider.PURCHASE_DATE, item.purchaseDate == null ? null : DATE_FORMATTER.format(item.purchaseDate));
        values.put(StockContentProvider.QUANTITY, item.quantity);
        values.put(StockContentProvider.AUTO_OUT_OF_STOCK, item.autoOutOfStock);

        String[] args = {item.name};

        context.getContentResolver().update(
                StockContentProvider.CONTENT_URI, values,  StockContentProvider.NAME + "='" + oldName +"'", null);
    }

    public static void deleteItem (Context context, String name) {
        String[] args = {name};
        context.getContentResolver().delete(StockContentProvider.CONTENT_URI, StockContentProvider.NAME + "=?", args);
    }

    public static ArrayList<Item> queryAllItems (Context context) {
        Cursor cursor = context.getContentResolver().query(StockContentProvider.CONTENT_URI, null, null,null,null);
        cursor.moveToFirst();
        SimpleDateFormat formatter = DATE_FORMATTER;
        ArrayList<Item> itemArrayList = new ArrayList<>();
        do {
            Item item = new Item();
            item.name = cursor.getString(cursor.getColumnIndex(StockContentProvider.NAME));
            item.type = ItemType.values()[cursor.getInt(cursor.getColumnIndex(StockContentProvider.TYPE))];
            item.status = ItemStatus.values()[cursor.getInt(cursor.getColumnIndex(StockContentProvider.STATUS))];
            try {
                item.expiry = formatter.parse(cursor.getString(cursor.getColumnIndex(StockContentProvider.EXPIRY)));
                item.purchaseDate = formatter.parse(cursor.getString(cursor.getColumnIndex(StockContentProvider.PURCHASE_DATE)));
            } catch (Exception e) {
                item.expiry = null;
                item.purchaseDate = null;
            }
            item.quantity = cursor.getString(cursor.getColumnIndex(StockContentProvider.QUANTITY));
            item.autoOutOfStock = cursor.getInt(cursor.getColumnIndex(StockContentProvider.AUTO_OUT_OF_STOCK)) == 1;
            itemArrayList.add(item);
            if (cursor.isLast()) {
                break;
            }
            cursor.moveToNext();
        } while (true);
        cursor.close();
        return itemArrayList;
    }

    public static ArrayList<Item> queryItems (Context context, ItemType type, boolean isInStock) {
        String selection,sortOrder = null;
        if (isInStock) {
            selection = StockContentProvider.TYPE + "="+ type.value +" AND " + StockContentProvider.STATUS + "=" + ItemStatus.IN_STOCK.getValue();
        } else {
            selection = StockContentProvider.TYPE + "=" + type.value + " AND NOT " + StockContentProvider.STATUS + "=" + ItemStatus.IN_STOCK.getValue();
        }
        sortOrder = StockContentProvider.NAME + " ASC";
        ArrayList<Item> itemArrayList = new ArrayList<>();
        try {
            Cursor cursor = context.getContentResolver().query(StockContentProvider.CONTENT_URI, null, selection, null, sortOrder);
            if (cursor.moveToFirst()) {
                do {
                    Item item = new Item();
                    item.name = cursor.getString(cursor.getColumnIndex(StockContentProvider.NAME));
                    item.type = ItemType.values()[cursor.getInt(cursor.getColumnIndex(StockContentProvider.TYPE))];
                    item.status = ItemStatus.values()[cursor.getInt(cursor.getColumnIndex(StockContentProvider.STATUS))];
                    try {
                        item.expiry = DATE_FORMATTER.parse(cursor.getString(cursor.getColumnIndex(StockContentProvider.EXPIRY)));
                    } catch (ParseException e) {
                        item.expiry = null;
                    } catch (NullPointerException e) {
                        item.expiry = null;
                    }
                    try {
                        item.purchaseDate = DATE_FORMATTER.parse(cursor.getString(cursor.getColumnIndex(StockContentProvider.PURCHASE_DATE)));
                    } catch (ParseException e) {
                        item.purchaseDate = null;
                    } catch (NullPointerException e) {
                        item.purchaseDate = null;
                    }
                    item.quantity = cursor.getString(cursor.getColumnIndex(StockContentProvider.QUANTITY));
                    item.autoOutOfStock = cursor.getInt(cursor.getColumnIndex(StockContentProvider.AUTO_OUT_OF_STOCK)) == 1;
                    itemArrayList.add(item);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {

        }
        return itemArrayList;
    }

    public static ArrayList<Item> queryShoppingItems (Context context, boolean purchasedToday) {
        String selection, sortOrder = null;
        if (purchasedToday) {
            selection = StockContentProvider.STATUS + "="+ ItemStatus.IN_STOCK.getValue() +
                    " AND " + StockContentProvider.PURCHASE_DATE + "='" + DATE_FORMATTER.format(Calendar.getInstance().getTime()) + "'";

        } else {
            selection = StockContentProvider.STATUS + "=" + ItemStatus.TO_BUY.getValue();
        }
        sortOrder = StockContentProvider.NAME + " ASC";
        ArrayList<Item> itemArrayList = new ArrayList<>();
        try {
            Cursor cursor = context.getContentResolver().query(StockContentProvider.CONTENT_URI, null, selection, null, sortOrder);
            SimpleDateFormat formatter = DATE_FORMATTER;
            if (cursor.moveToFirst()) {
                do {
                    Item item = new Item();
                    item.name = cursor.getString(cursor.getColumnIndex(StockContentProvider.NAME));
                    item.type = ItemType.values()[cursor.getInt(cursor.getColumnIndex(StockContentProvider.TYPE))];
                    item.status = ItemStatus.values()[cursor.getInt(cursor.getColumnIndex(StockContentProvider.STATUS))];
                    try {
                        item.expiry = formatter.parse(cursor.getString(cursor.getColumnIndex(StockContentProvider.EXPIRY)));
                    } catch (ParseException e) {
                        item.expiry = null;
                    } catch (NullPointerException e) {
                        item.expiry = null;
                    }
                    try {
                        item.purchaseDate = formatter.parse(cursor.getString(cursor.getColumnIndex(StockContentProvider.PURCHASE_DATE)));
                    } catch (ParseException e) {
                        item.purchaseDate = null;
                    } catch (NullPointerException e) {
                        item.purchaseDate = null;
                    }
                    item.quantity = cursor.getString(cursor.getColumnIndex(StockContentProvider.QUANTITY));
                    item.autoOutOfStock = cursor.getInt(cursor.getColumnIndex(StockContentProvider.AUTO_OUT_OF_STOCK)) == 1;
                    itemArrayList.add(item);
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {

        }
        return itemArrayList;
    }
}