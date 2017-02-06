package com.tjut.mianliao.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class CnArrayAdapter<T> extends BaseAdapter implements Filterable {

    private Context mContext;
    private int mResId;
    private List<T> mOrigObjects;
    private List<T> mObjects;
    private Filter mFilter;

    private int mShowExtraBound;
    private T mExtraObject;

    public CnArrayAdapter(Context ctx, int resId, List<T> objects) {
        mContext = ctx;
        mResId = resId;
        mOrigObjects = objects;
        mObjects = objects;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public T getItem(int position) {
        return mObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view;
        if (convertView != null && convertView instanceof TextView) {
            view = (TextView) convertView;
        } else {
            view = (TextView) LayoutInflater.from(mContext).inflate(mResId, parent, false);
        }
        view.setText(getItem(position).toString());
        return view;
    }

    public void setExtraItem(T item, int showExtraBound) {
        mShowExtraBound = showExtraBound;
        mExtraObject = item;

        if (mExtraObject != null && mShowExtraBound > 0 && mObjects.size() < mShowExtraBound) {
            mObjects = new ArrayList<T>(mObjects);
            mObjects.add(mExtraObject);
        }
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new CnFilter();
        }
        return mFilter;
    }

    private class CnFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<T> items;
            if (TextUtils.isEmpty(constraint)) {
                items = mOrigObjects;
            } else {
                items = new ArrayList<T>();
                for (T item : mOrigObjects) {
                    if (item.toString().toLowerCase().contains(constraint.toString()
                            .toLowerCase())) {
                        items.add(item);
                    }
                }
            }
            if (mShowExtraBound > 0 && items.size() < mShowExtraBound) {
                items.add(mExtraObject);
            }
            results.values = items;
            results.count = items.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mObjects = (List<T>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
