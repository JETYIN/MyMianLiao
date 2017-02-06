package com.tjut.mianliao.component.tab;

import java.util.ArrayList;

import android.view.View;

public class TabController implements View.OnClickListener {
    private ArrayList<Tab> mTabs = new ArrayList<Tab>();
    private int mCurrent;
    private Tab mActiveTab;
    private TabListener mListener;

    public void setListener(TabListener listener) {
        mListener = listener;
    }

    public void add(Tab tab) {
        tab.getButton().setOnClickListener(this);
        mTabs.add(tab);
    }

    public void select(Tab tab) {
        if (mActiveTab == tab || tab == null || !mTabs.contains(tab)) {
            return;
        }

        if (mActiveTab != null) {
            mActiveTab.setChosen(false);
            if (mListener != null) {
                mListener.onTabSelectionChanged(mCurrent, false, mActiveTab);
            } else if (mActiveTab.getPage() != null) {
                mActiveTab.getPage().setVisibility(View.GONE);
            }
        }

        mCurrent = mTabs.indexOf(tab);
        tab.setChosen(true);
        if (mListener != null) {
             mListener.onTabSelectionChanged(mCurrent, true, tab);
        } else if (tab.getPage() != null) {
            tab.getPage().setVisibility(View.VISIBLE);
        }

        mActiveTab = tab;
    }

    public void select(int index) {
        if (index < 0 || index > mTabs.size()) {
            return;
        }
        select(mTabs.get(index));
    }

    public int getCurrent() {
        return mCurrent;
    }

    public void clear() {
        mTabs.clear();
        mActiveTab = null;
    }

    @Override
    public void onClick(View v) {
        for (Tab tab : mTabs) {
            if (v == tab.getButton()) {
                select(tab);
            }
        }
    }

    public interface TabListener {
        public void onTabSelectionChanged(int index, boolean selected, Tab tab);
    }
}
