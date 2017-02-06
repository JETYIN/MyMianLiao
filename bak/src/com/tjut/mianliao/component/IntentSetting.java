package com.tjut.mianliao.component;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;

import com.tjut.mianliao.R;

public class IntentSetting extends SettingItem {

    private Intent mIntent;
    private HighPriorityClickListener mListener;

    public void registerHighProiorityListener(HighPriorityClickListener listener) {
        mListener = listener;
    }

    public void unregisterHighProiorityListener(HighPriorityClickListener listener) {
        mListener = null;
    }

    public IntentSetting(Context context) {
        this(context, null);
    }

    public IntentSetting(Context context, AttributeSet attrs) {
        super(context, attrs);
        mIntent = new Intent();
    }

    public Intent getIntent() {
        return mIntent;
    }

    public void setClass(Class<?> cls) {
        mIntent.setClass(getContext(), cls);
    }

    @Override
    protected int getWidgetLayoutResID() {
        return R.layout.setting_widget_intent;
    }

    @Override
    protected void onSettingKey() {
    }

    @Override
    protected void onSettingItemClick() {
        if (mIntent.getComponent() != null) {
            getContext().startActivity(mIntent);
        } else if (mListener != null) {
            mListener.onClick();
        }
    }

    public interface HighPriorityClickListener{
        void onClick();
    }
}
