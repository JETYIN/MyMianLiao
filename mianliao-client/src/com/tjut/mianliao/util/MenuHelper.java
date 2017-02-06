package com.tjut.mianliao.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.SparseArray;

import com.tjut.mianliao.data.MenuItem;

public class MenuHelper {

    private ArrayList<MenuItem> mItems = new ArrayList<MenuItem>();
    private ArrayList<MenuItem> mDisabledItems = new ArrayList<MenuItem>();
    private SparseArray<MenuItem> mItemMap = new SparseArray<MenuItem>();

    private Comparator<MenuItem> mComparator = new Comparator<MenuItem>() {
        @Override
        public int compare(MenuItem lhs, MenuItem rhs) {
            return lhs.order - rhs.order;
        }
    };

    public MenuHelper(Context ctx, int array) {
        TypedArray ta = ctx.getResources().obtainTypedArray(array);
        int length = ta.length() / 2;
        for (int i = 0; i < length; i++) {
            add(new MenuItem(ta.getResourceId(2 * i, 0), ta.getString(2 * i + 1), i));
        }
        ta.recycle();
    }

    public static ArrayList<MenuItem> makeMenu(Context ctx, int array) {
        ArrayList<MenuItem> items = new ArrayList<MenuItem>();
        TypedArray ta = ctx.getResources().obtainTypedArray(array);
        int length = ta.length() / 2;
        for (int i = 0; i < length; i++) {
            items.add(new MenuItem(ta.getResourceId(2 * i, 0), ta.getString(2 * i + 1), i));
        }
        ta.recycle();
        return items;
    }

    public ArrayList<MenuItem> getMenu() {
        return mItems;
    }

    public MenuItem get(int index) {
        return mItems.get(index);
    }

    public void add(MenuItem item) {
        mItems.add(item);
        mItemMap.put(item.id, item);
    }

    public void remove(int id) {
        MenuItem item = mItemMap.get(id);
        if (item != null) {
            mItemMap.remove(id);
            mItems.remove(item);
            mDisabledItems.remove(item);
        }
    }

    public void disable(int id) {
        MenuItem item = mItemMap.get(id);
        if (item != null && mItems.remove(item)) {
            mDisabledItems.add(item);
        }
    }

    public void update(int id, String title) {
        MenuItem item = mItemMap.get(id);
        if (item != null) {
            item.title = title;
        }
    }

    public void enable(int id) {
        MenuItem item = mItemMap.get(id);
        if (item != null && mDisabledItems.remove(item)) {
            mItems.add(item);
            Collections.sort(mItems, mComparator);
        }
    }

    public void setEnabled(int id, boolean enable) {
        if (enable) {
            enable(id);
        } else {
            disable(id);
        }
    }
}
