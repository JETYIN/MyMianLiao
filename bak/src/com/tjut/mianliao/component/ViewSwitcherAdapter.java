package com.tjut.mianliao.component;

import java.util.ArrayList;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public abstract class ViewSwitcherAdapter extends PagerAdapter {

    private ArrayList<View> mRecycledViews;

    public ViewSwitcherAdapter() {
        mRecycledViews = new ArrayList<View>();
    }

    public abstract View getView(int position, View convertView, ViewGroup parent);

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int size = mRecycledViews.size();
        View convertView = size == 0 ? null : mRecycledViews.remove(size - 1);
        View view = getView(position, convertView, container);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;
        container.removeView(view);
        mRecycledViews.add(view);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
