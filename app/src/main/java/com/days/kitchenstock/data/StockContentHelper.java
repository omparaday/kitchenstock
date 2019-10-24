package com.days.kitchenstock.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    public static final int EXPIRING_SOON_DIVISOR = 4;

    public enum ItemType {
        FRESH(), SHORT_TERM(), LONG_TERM();
        private final int value;

        private ItemType() {
            value = ordinal();
        }

        public int getValue() {
            return value;
        }
    }

    public enum ItemStatus {
        IN_STOCK(), OUT_OF_STOCK(), TO_BUY();
        private final int value;

        private ItemStatus() {
            value = ordinal();
        }

        public int getValue() {
            return value;
        }
    }

    public static class Item implements Comparable {
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

        public boolean isExpired() {
            if (expiry == null) {
                return false;
            }
            return expiry.before(Calendar.getInstance().getTime());
        }

        public boolean isExpiringSoon() {
            if (expiry == null) {
                return false;
            }
            long totalDays = TimeUnit.DAYS.convert(expiry.getTime() - purchaseDate.getTime(), TimeUnit.MILLISECONDS);
            long remainingDays = TimeUnit.DAYS.convert(expiry.getTime() - Calendar.getInstance().getTime().getTime(), TimeUnit.MILLISECONDS);
            if (remainingDays < totalDays / EXPIRING_SOON_DIVISOR) {
                return true;
            }
            return false;
        }

        @Override
        public int compareTo(Object o) {
            Item item2 = (Item) o;
            if (this.isExpired()) {
                if (item2.isExpired()) return 0;
                return -1;
            } else if (item2.isExpired()) {
                return 1;
            } else if (this.isExpiringSoon()) {
                if (item2.isExpiringSoon()) return 0;
                return -1;
            } else if (item2.isExpiringSoon()) {
                return 1;
            }
            return this.name.compareTo(item2.name);
        }
    }

    public static void addItem(Context context, Item item) {
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

    public static void updateItem(Context context, Item item, String oldName) {
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
                StockContentProvider.CONTENT_URI, values, StockContentProvider.NAME + "='" + oldName + "'", null);
    }

    public static void deleteItemList(Context context, ArrayList<Item> items) {
        for (Item item : items) {
            deleteItem(context, item.name);
        }
    }

    public static void moveToInStockList(Context context, ArrayList<Item> items) {
        for (Item item : items) {
            item.status = ItemStatus.IN_STOCK;
            item.purchaseDate = Calendar.getInstance().getTime();
            updateItem(context, item, item.name);
        }
    }

    public static void moveToOutOfStockList(Context context, ArrayList<Item> items) {
        for (Item item : items) {
            item.status = ItemStatus.OUT_OF_STOCK;
            item.purchaseDate = null;
            item.expiry = null;
            updateItem(context, item, item.name);
        }
    }

    public static void moveToShopList(Context context, ArrayList<Item> items) {
        for (Item item : items) {
            item.purchaseDate = null;
            item.expiry = null;
            item.status = ItemStatus.TO_BUY;
            updateItem(context, item, item.name);
        }
    }

    public static void deleteItem(Context context, String name) {
        String[] args = {name};
        context.getContentResolver().delete(StockContentProvider.CONTENT_URI, StockContentProvider.NAME + "=?", args);
    }

    public static ArrayList<Item> queryItems(Context context, ItemType type, boolean isInStock) {
        String selection, sortOrder = null;
        if (isInStock) {
            selection = StockContentProvider.TYPE + "=" + type.value + " AND " + StockContentProvider.STATUS + "=" + ItemStatus.IN_STOCK.getValue();
        } else {
            selection = StockContentProvider.TYPE + "=" + type.value + " AND NOT " + StockContentProvider.STATUS + "=" + ItemStatus.IN_STOCK.getValue();
            sortOrder = StockContentProvider.STATUS + " DESC" + ", " + StockContentProvider.NAME + " ASC";
        }
        ArrayList<Item> itemArrayList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(StockContentProvider.CONTENT_URI, null, selection, null, sortOrder);
        if (cursor.moveToFirst()) {
            do {
                Item item = getItemFromCursor(cursor);
                itemArrayList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return itemArrayList;
    }

    public static ArrayList<Item> getAllItems(Context context) {
        ArrayList<Item> itemArrayList = new ArrayList<>();
        String sortOrder = StockContentProvider.NAME + " ASC";
        Cursor cursor = context.getContentResolver().query(StockContentProvider.CONTENT_URI, null, null, null, sortOrder);
        if (cursor.moveToFirst()) {
            do {
                Item item = getItemFromCursor(cursor);
                itemArrayList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return itemArrayList;
    }

    public static int getExpiredSinceCount(Context context, Date since) {
        int count = 0;
        Date today = Calendar.getInstance().getTime();
        String selection = StockContentProvider.STATUS + "=" + ItemStatus.IN_STOCK.getValue();
        Cursor cursor = context.getContentResolver().query(StockContentProvider.CONTENT_URI, null, selection, null, null);
        if (cursor.moveToFirst()) {
            do {
                Item item = getItemFromCursor(cursor);
                if (item.expiry != null && item.expiry.before(today)) {
                    if (since == null) {
                        count = count + 1;
                    } else if (item.expiry.after(since)) {
                        count = count + 1;
                    }
                }
            } while (cursor.moveToNext());
        }
        return count;
    }

    public static int getExpiringSoonSinceCount(Context context, Date since) {
        int count = 0;
        Date today = Calendar.getInstance().getTime();
        String selection = StockContentProvider.STATUS + "=" + ItemStatus.IN_STOCK.getValue();
        Cursor cursor = context.getContentResolver().query(StockContentProvider.CONTENT_URI, null, selection, null, null);
        if (cursor.moveToFirst()) {
            do {
                Item item = getItemFromCursor(cursor);
                if (item.expiry != null && item.expiry.after(today)) {
                    long totalDays = TimeUnit.DAYS.convert(item.expiry.getTime() - item.purchaseDate.getTime(), TimeUnit.MILLISECONDS);
                    int lastFewDays = (int) (totalDays / EXPIRING_SOON_DIVISOR);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(item.expiry);
                    calendar.add(Calendar.DATE, -lastFewDays);
                    Date threshold = calendar.getTime();
                    if (threshold.before(today)) {
                        if (since == null) {
                            count = count + 1;
                        } else if (threshold.after(since)) {
                            count = count + 1;
                        }
                    }
                }
            } while (cursor.moveToNext());
        }
        return count;
    }

    public static boolean moveStockToShopAutoAddItems(Context context) {
        String selection = StockContentProvider.STATUS + "=" + ItemStatus.IN_STOCK.getValue() +
                " AND " + StockContentProvider.AUTO_OUT_OF_STOCK + "=1";
        Cursor cursor = context.getContentResolver().query(StockContentProvider.CONTENT_URI, null, selection, null, null);
        if (cursor.moveToFirst()) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Date today = calendar.getTime();
            do {
                Item item = getItemFromCursor(cursor);
                if (item.purchaseDate != today) {
                    item.purchaseDate = null;
                    item.expiry = null;
                    item.status = ItemStatus.TO_BUY;
                    updateItem(context, item, item.name);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return true;

    }

    private static Item getItemFromCursor(Cursor cursor) {
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
        return item;
    }

    public static ArrayList<Item> queryShoppingItems(Context context, boolean purchasedToday) {
        String selection, sortOrder = null;
        if (purchasedToday) {
            selection = StockContentProvider.STATUS + "=" + ItemStatus.IN_STOCK.getValue() +
                    " AND " + StockContentProvider.PURCHASE_DATE + "='" + DATE_FORMATTER.format(Calendar.getInstance().getTime()) + "'";

        } else {
            selection = StockContentProvider.STATUS + "=" + ItemStatus.TO_BUY.getValue();
        }
        sortOrder = StockContentProvider.NAME + " ASC";
        ArrayList<Item> itemArrayList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(StockContentProvider.CONTENT_URI, null, selection, null, sortOrder);
        if (cursor.moveToFirst()) {
            do {
                Item item = getItemFromCursor(cursor);
                itemArrayList.add(item);
            } while (cursor.moveToNext());
        }
        return itemArrayList;
    }


    public static Item queryItem(Context context, String name) {
        String selection = StockContentProvider.NAME + "='" + name + "'";
        Cursor cursor = context.getContentResolver().query(StockContentProvider.CONTENT_URI, null, selection, null, null);
        if (cursor.moveToFirst()) {
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
            cursor.close();
            return item;
        }
        cursor.close();
        return null;
    }
}