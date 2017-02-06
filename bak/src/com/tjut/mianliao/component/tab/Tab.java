package com.tjut.mianliao.component.tab;

import android.view.View;

public abstract class Tab {

    private View mButton;
    private View mPage;
    private Object mDataRef;

    public Tab setButton(View button) {
        mButton = button;
        return this;
    }

    public Tab setPage(View page) {
        mPage = page;
        return this;
    }

    public Tab setDataRef(Object dataRef) {
        mDataRef = dataRef;
        return this;
    }

    public View getButton() {
        return mButton;
    }

    public View getPage() {
        return mPage;
    }

    public Object getDataRef() {
        return mDataRef;
    }

    public abstract void setChosen(boolean chosen);
}
