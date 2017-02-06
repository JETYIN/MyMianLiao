package com.tjut.mianliao.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.tab.TabController;
import com.tjut.mianliao.component.tab.TextTab;

public class TitleBar extends RelativeLayout {

	private static final int COLOR_DAY = 0xff515151;
	private static final int COLOR_NIGHT = 0xffb95167;
	private static final int BG_COLOR_DAY = 0xffffffff;
	private static final int BG_COLOR_NIGHT = 0xff211b2f;
	
    private ImageButton mLeftButton;

    private TextView mLeftText;

    private ImageButton mRightButton;

    private TextView mRightText;

    private TextView mTitleText;

    private View mProgressBar;

    private View mTabContainer;
    private TextView mTab1;
    private TextView mTab2;

    private TextView mSubTitleText;

    private TextTab textTab1;

    private TextTab textTab2;

    private Context mContext;
    
    private TextView mTvUnreadCount;
    
    private ImageView mIvRefreshCount;
    
    
    public TitleBar(Context context) {
        super(context);
        mContext = context;
    }

    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public TitleBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    private void init() {
	    mLeftButton = (ImageButton) findViewById(R.id.btn_left);
	    mLeftText = (TextView) findViewById(R.id.tv_left);
	    mRightButton = (ImageButton) findViewById(R.id.btn_right);
	    mRightText = (TextView) findViewById(R.id.tv_right);
	    mTitleText = (TextView) findViewById(R.id.tv_title);
	    mProgressBar = findViewById(R.id.pb_loading);
	    mSubTitleText = (TextView) findViewById(R.id.tv_little_title);
	    mTvUnreadCount = (TextView) findViewById(R.id.tv_remind_count);
	    mIvRefreshCount = (ImageView) findViewById(R.id.iv_refresh_red);
	    
	    mTabContainer = findViewById(R.id.ll_tab_container);
	    mTab1 = (TextView) findViewById(R.id.tv_tab_1);
	    mTab2 = (TextView) findViewById(R.id.tv_tab_2);
	
	    setTextColor();
	
	}
    
    @Override
    protected void onFinishInflate() {
        init();
        super.onFinishInflate();
    }
    
    public void showRefreshRed(boolean isShow) {
        if (isShow) {
            mIvRefreshCount.setVisibility(View.VISIBLE);
        } else {
            mIvRefreshCount.setVisibility(View.GONE);
        }
    } 

    public void showLeftButton(int imgRes, OnClickListener listener) {
        showButton(mLeftButton, imgRes, listener);
    }

    public void showRightButton(int imgRes, OnClickListener listener) {
        showButton(mRightButton, imgRes, listener);
    }

    public void setRightButtonImage(int imgRes) {
        mRightButton.setImageResource(imgRes);
    }

    public void showLeftText(int txtRes, OnClickListener listener) {
        showText(mLeftText, getResources().getText(txtRes), listener);
    }

    public TextView showRightText(int txtRes, OnClickListener listener) {
        return showText(mRightText, getResources().getText(txtRes), listener);
    }

    public TextView showRightText(CharSequence txt, OnClickListener listener) {
        return showText(mRightText, txt, listener);
    }

    public void showTitleText(int txtRes, OnClickListener listener) {
        showTitleText(getResources().getText(txtRes), listener);
    }

    public void showTitleText(CharSequence txt, OnClickListener listener) {
        showText(mTitleText, txt, listener);
        if (listener != null) {
            mTitleText.setBackgroundResource(R.drawable.selector_btn_title);
        }
    }
    
    public void showTitleTextIcon(){
    	Drawable mDrawable;
    	 mDrawable=getResources().getDrawable(R.drawable.icon_arrow_down);
    	 mDrawable.setBounds(0, 0, mDrawable.getMinimumWidth(), mDrawable.getMinimumHeight());
		 mTitleText.setCompoundDrawables(null,null,mDrawable,null);
    }
    
    public void showSubTitleText(CharSequence txt) {
        mSubTitleText.setText(txt);
        mSubTitleText.setVisibility(View.VISIBLE);
    }
    public void hideSubTitleText() {
    	mSubTitleText.setVisibility(View.GONE);
    }

    public void showTabs(TabController controller, String tabL, String tabR) {
        mTabContainer.setVisibility(VISIBLE);
        mTab1.setText(tabL);
        mTab2.setText(tabR);

        textTab1 = new TextTab(mTab1);
        controller.add(textTab1);

        textTab2 = new TextTab(mTab2);
        controller.add(textTab2);
        setupTabStyle();

//        mTab2.setBackgroundResource(R.drawable.button_nav_right);
//        mTab2.setTextColor(COLOR_DAY);

        setChosedTab(0);
        
        mTitleText.setVisibility(GONE);
    }

    public void setupTabStyle() {
        textTab1.setBackgroundResource(0, R.drawable.bg_light_blue_over);
        textTab1.setTextColorResource(R.color.txt_tab_item_normal, R.color.txt_tab_item_choosed);
        textTab2.setBackgroundResource(0, R.drawable.bg_light_blue_over);
        textTab2.setTextColorResource(R.color.txt_tab_item_normal, R.color.txt_tab_item_choosed);
    }

    public void setChosedTab(int current) {
        textTab1.setChosen(current == 0);
        textTab2.setChosen(current != 0);
    }

    public void showTitleArrow() {
        setTitleDrawable(0, 0, R.drawable.ic_title_arrow, 0);
    }

    public void setTitleDrawable(int left, int top, int right, int bottom) {
        mTitleText.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
    }

    public void setTitle(CharSequence text) {
        mTitleText.setText(text);
    }

    public void setTitle(int text) {
        mTitleText.setText(text);
    }

    public void showProgress() {
        mProgressBar.setVisibility(VISIBLE);
    }

    public void hideLeftButton() {
        mLeftButton.setVisibility(GONE);
    }

    public void hideRightButton() {
        mRightButton.setVisibility(GONE);
    }

    public void showRightButton() {
        mRightButton.setVisibility(VISIBLE);
    }

    public void hideLeftText() {
        mLeftText.setVisibility(GONE);
    }

    public void hideProgress() {
        mProgressBar.setVisibility(GONE);
    }

    public void setLeftButtonEnabled(boolean enabled) {
        mLeftButton.setEnabled(enabled);
    }

    public void setRightButtonEnabled(boolean enabled) {
        mRightButton.setEnabled(enabled);
    }

    public void setLeftTextEnabled(boolean enabled) {
        mLeftText.setEnabled(enabled);
    }

    public void setRightTextEnabled(boolean enabled) {
        mRightText.setEnabled(enabled);
    }

    public void setRemindMsgCount(int count) {
        String unCount = count < 100 ? String.valueOf(count) : "99+";
        mTvUnreadCount.setText(unCount);
        mTvUnreadCount.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    public void hideRemindMsgCount() {
        mTvUnreadCount.setVisibility(View.GONE);
    }
    
    private void setTextColor() {
        mLeftText.setTextColor(COLOR_DAY);
        mRightText.setTextColor(COLOR_DAY);
        mTitleText.setTextColor(COLOR_DAY);
        mSubTitleText.setTextColor(COLOR_DAY);
    }

    private void showButton(ImageButton btn, int imgResId, OnClickListener listener) {
        if (imgResId != -1) {
            btn.setImageResource(imgResId);
        }
        btn.setOnClickListener(listener);
        btn.setVisibility(VISIBLE);
    }

    public void setRightButtonPadding(int left, int top, int right, int bottom) {
        mRightButton.setPadding(left, top, right, bottom);
    }

    private TextView showText(TextView tv, CharSequence txt, OnClickListener listener) {
        tv.setText(txt);
        tv.setOnClickListener(listener);
        tv.setVisibility(VISIBLE);
        if (tv == mRightText) {
            tv.setTextColor(COLOR_DAY);
        } else {
            tv.setTextColor(COLOR_DAY);
        }
        return tv;
    }
    
    public void showRightBadge() {
        findViewById(R.id.iv_badge).setVisibility(VISIBLE);
    }

    public void hideRightBadge() {
        findViewById(R.id.iv_badge).setVisibility(GONE);
    }
    
    public void setRightTextColor(int color) {
        mRightText.setTextColor(color);
    }
}
