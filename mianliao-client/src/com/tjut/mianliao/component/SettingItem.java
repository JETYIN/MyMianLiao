package com.tjut.mianliao.component;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.settings.Settings;

public abstract class SettingItem extends LinearLayout {

    protected TextView mTitle;

    protected TextView mSummary;

    protected View mWidget;

    protected String mKey;

    protected Settings mSettings;

    protected Interceptor mInterceptor;

    private Context mContext;

    public SettingItem(Context context) {
        this(context, null);
    }

    public SettingItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setOrientation(VERTICAL);
        LayoutInflater inflater = LayoutInflater.from(context);
//        inflater.inflate(R.layout.hr_divider_with_margin, this);

        View item = inflater.inflate(R.layout.setting_item, this, false);
        mTitle = (TextView) item.findViewById(R.id.tv_setting_title);
        mSummary = (TextView) item.findViewById(R.id.tv_setting_summary);
        mWidget = onCreateWidgetView((ViewGroup) item.findViewById(R.id.ll_setting_widget_frame));
        mSettings = Settings.getInstance(context);
        item.findViewById(R.id.ll_item).setBackgroundColor(Color.WHITE);
        item.setOnClickListener(mOnClickListener);
        addView(item);
    }

    public void setTitle(int resid) {
        mTitle.setText(resid);
    }

    public void setSummary(int resid) {
        mSummary.setText(resid);
    }

    public void setSummary(CharSequence text) {
        mSummary.setText(text);
    }

    public void setKey(String key) {
        mKey = key;
        boolean intercepted = mInterceptor != null
                && mInterceptor.onSettingKey(this);
        if (!intercepted) {
            onSettingKey();
        }
    }

    public void setInterceptor(Interceptor interceptor) {
        mInterceptor = interceptor;
    }

    protected View onCreateWidgetView(ViewGroup root) {
        View widget = null;
        int resource = getWidgetLayoutResID();
        if (resource == 0) {
            root.setVisibility(GONE);
        } else {
            widget = LayoutInflater.from(getContext()).inflate(resource, root, false);
            root.addView(widget);
        }
        return widget;
    }

    protected boolean getPersistedBoolean() {
        return mSettings.getBoolean(mKey);
    }

    protected void persistBoolean(boolean value) {
        mSettings.setBoolean(mKey, value);
    }

    protected int getPersistedInt() {
        return mSettings.getInt(mKey);
    }

    protected void persistInt(int value) {
        mSettings.setInt(mKey, value);
    }

    protected String getPersistedString() {
        return mSettings.getString(mKey);
    }

    protected void persistString(String value) {
        mSettings.setString(mKey, value);
    }

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean intercepted = mInterceptor != null
                    && mInterceptor.onSettingItemClick(SettingItem.this);
            if (!intercepted) {
                onSettingItemClick();
            }
        }
    };

    protected abstract int getWidgetLayoutResID();

    protected abstract void onSettingKey();

    protected abstract void onSettingItemClick();

    public interface Interceptor {
        public boolean onSettingKey(SettingItem si);
        public boolean onSettingItemClick(SettingItem si);
    }
}
