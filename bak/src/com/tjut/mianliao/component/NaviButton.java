package com.tjut.mianliao.component;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.main.TabFragment;
import com.tjut.mianliao.settings.Settings;

public class NaviButton extends RelativeLayout {

    private CheckedTextView mTvTitle;
    private TabFragment mRefTab;
    private TextView mTvUnreadCount;

    private boolean mSelected, mIsNightMode;
    private Settings mSettings;

    private ColorStateList mColorNormal;
    private ColorStateList mColorNight;

    private Resources mRes;
    private int[] mNormalNaviRes, mNightNaviRes;

    private int mTitleId;

    public NaviButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRes = context.getResources();
        getResList();
        mSettings = Settings.getInstance(context);
        mIsNightMode = mSettings.isNightMode();
        mColorNormal = context.getResources().getColorStateList(R.color.selector_color_navi_btn);
        mColorNight = context.getResources().getColorStateList(R.color.selector_color_navi_btn_black);
        inflate(context, R.layout.btn_navi, this);
        mTvTitle = (CheckedTextView) findViewById(R.id.tv_title);
        mTvUnreadCount = (TextView) findViewById(R.id.tv_unread_count);
        mTitleId = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res-auto", "nb_title", -1);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.NaviButton, 0, 0);
        mTvTitle.setText(ta.getResourceId(R.styleable.NaviButton_nb_title, 0));
        mTvTitle.setCompoundDrawablesWithIntrinsicBounds(
                0, ta.getResourceId(R.styleable.NaviButton_src, 0), 0, 0);
        mTvTitle.setTextColor(mIsNightMode ? mColorNight : mColorNormal);
        mTvTitle.setCompoundDrawablesWithIntrinsicBounds(0, getResIdByTitle(), 0, 0);
        ta.recycle();
    }

    private int getResIdByTitle() {
        if (mTitleId == R.string.tab_chat) {
            if (mIsNightMode) {
                return mNightNaviRes[0];
            } else {
                return mNormalNaviRes[0];
            }
        } else if (mTitleId == R.string.tab_forum) {
            if (mIsNightMode) {
                return mNightNaviRes[1];
            } else {
                return mNormalNaviRes[1];
            }
        } else if (mTitleId == R.string.tab_tribe) {
            if (mIsNightMode) {
                return mNightNaviRes[2];
            } else {
                return mNormalNaviRes[2];
            }
        } else if (mTitleId == R.string.tab_explore) {
            if (mIsNightMode) {
                return mNightNaviRes[3];
            } else {
                return mNormalNaviRes[3];
            }
        }
        return 0;
    }

    private void getResList() {
        mNormalNaviRes = new int[] { R.drawable.selector_btn_navi_chat,
                R.drawable.selector_btn_navi_forum,
                R.drawable.selector_btn_navi_tribe,
                R.drawable.selector_btn_navi_more };
        mNightNaviRes = new int[] { R.drawable.selector_btn_navi_chat_night,
                R.drawable.selector_btn_navi_forum_night,
                R.drawable.selector_btn_navi_tribe_night,
                R.drawable.selector_btn_navi_more_night};
    }

    @Override
    public void setSelected(boolean selected) {
        if (mSelected == selected) {
            return;
        }
        mSelected = selected;
        if (mRefTab != null) {
            mRefTab.setFocus(mSelected);
        }
        mTvTitle.setChecked(selected);
    }

    @Override
    public boolean isSelected() {
        return mSelected;
    }

    public boolean isBadgeVisible() {
        return mTvUnreadCount.getVisibility() == VISIBLE;
    }

    public void updateBadge(int count) {
        if (count > 0) {
            String unCount = count < 100 ? String.valueOf(count) : "99+";
            mTvUnreadCount.setText(unCount);
            mTvUnreadCount.setVisibility(View.VISIBLE);
        } else {
            mTvUnreadCount.setVisibility(View.GONE);
        }
    }

    public void setRefTab(TabFragment tab) {
        mRefTab = tab;
    }

    public TabFragment getRefTab() {
        return mRefTab;
    }
}
