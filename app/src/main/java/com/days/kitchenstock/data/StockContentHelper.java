package com.days.kitchenstock.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.database.Cursor;
import android.net.Uri;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

public class StockContentHelper {

    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");

    public enum ItemType {
        FRESH(), SHORT_TERM(), LONG_TERM(), MISC();
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
            return name + "," + type + "," + status + "," + expiry + "," + purchaseDate + "," + quantity + "," + autoOutOfStock;
        }

        public Item setName(String nameArg) {
            name = nameArg;
            return this;
        }

        public Item setItemType(ItemType itemType) {
            type = itemType;
            return this;
        }

    }

    public static void addItem (Context context, Item item) {
        // Add a new student record
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        ContentValues values = new ContentValues();
        values.put(StockContentProvider.NAME, item.name);
        values.put(StockContentProvider.TYPE, item.type.getValue());
        values.put(StockContentProvider.STATUS, item.status.getValue());
        values.put(StockContentProvider.EXPIRY, item.expiry == null ? null : formatter.format(item.expiry));
        values.put(StockContentProvider.PURCHASE_DATE, item.purchaseDate == null ? null : formatter.format(item.purchaseDate));
        values.put(StockContentProvider.QUANTITY, item.quantity);
        values.put(StockContentProvider.AUTO_OUT_OF_STOCK, item.autoOutOfStock);

        Uri uri = context.getContentResolver().insert(
                StockContentProvider.CONTENT_URI, values);
    }

    public static void updateItem (Context context, Item item) {
        SimpleDateFormat formatter = DATE_FORMATTER;
        ContentValues values = new ContentValues();
        values.put(StockContentProvider.NAME, item.name);
        values.put(StockContentProvider.TYPE, item.type.getValue());
        values.put(StockContentProvider.STATUS, item.status.getValue());
        values.put(StockContentProvider.EXPIRY, formatter.format(item.expiry));
        values.put(StockContentProvider.PURCHASE_DATE, formatter.format(item.purchaseDate));
        values.put(StockContentProvider.QUANTITY, item.quantity);
        values.put(StockContentProvider.AUTO_OUT_OF_STOCK, item.autoOutOfStock);
        String[] args = {item.name};

        context.getContentResolver().update(
                StockContentProvider.CONTENT_URI, values,  StockContentProvider.NAME + "=?" , args);
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
            Log.println(Log.INFO, "omprak", item.toString());
            if (cursor.isLast()) {
                break;
            }
            cursor.moveToNext();
        } while (true);
        return itemArrayList;
    }

    public static ArrayList<Item> queryItems (Context context, ItemType type, boolean isInStock) {
        String selection = null;
        if (isInStock) {
            selection = StockContentProvider.TYPE + "="+ type.value +" AND " + StockContentProvider.STATUS + "=" + ItemStatus.IN_STOCK.getValue();
        } else {
            selection = StockContentProvider.TYPE + "=" + type.value + " AND NOT " + StockContentProvider.STATUS + "=" + ItemStatus.IN_STOCK.getValue();
        }
        ArrayList<Item> itemArrayList = new ArrayList<>();
        try {
            Cursor cursor = context.getContentResolver().query(StockContentProvider.CONTENT_URI, null, selection, null, null);
            cursor.moveToFirst();
            SimpleDateFormat formatter = DATE_FORMATTER;
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
                Log.println(Log.INFO, "omprak", item.toString());
                cursor.moveToNext();
            } while (!cursor.isLast());
        } catch (Exception e) {

        }
        return itemArrayList;
    }
}