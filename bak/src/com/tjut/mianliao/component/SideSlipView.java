package com.tjut.mianliao.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.nineoldandroids.view.ViewHelper;
import com.tjut.mianliao.R;
import com.tjut.mianliao.util.Utils;

public class SideSlipView extends HorizontalScrollView {

    private static final String TAG = "SideSlipView";
    
    public static final int STATUS_MENU_CLOSE = 0;
    public static final int STATUS_MENU_OPEN = 1;

    private Context mContext;
    
    /**
     * The {@code mScreenWidth} is the screen width
     */
    private int mScreenWidth;

    /**
     * It decide the left view's padding-right, and the default value is 50f
     * @see {@link #setRightMenuPadding(int)}
     */
    private int mMenuRightPadding;

    /**
     * The menu(left view) width
     */
    private int mMenuWidth;

    /**
     * The half of menu(left view) width, if slide distance more than {@code mHalfMenuWidth},
     * it need to perform the current operation, otherwise cancel
     */
    private int mHalfMenuWidth;

    /**
     * It is the menu status, true if the menu is visibility.
     */
    private boolean mIsOpen;

    /**
     * {@link SideSlipView} will execute {@link #onMeasure(int, int)} while the {@code mOnce} is false.
     * In other words, {@link #onMeasure(int, int)} will execute in first time for once.
     */
    private boolean mOnce;

    /**
     * It determines whether the view on the right needs to be scaled.
     * It can call {@link #setRightViewScale(boolean)} to set and by attribute
     * @attr ref {@link com.tjut.mianliao.R.styleable#SideSlipView_scale_right_view}
     */
    private boolean mScaleRightView;
    
    /**
     * It determines the {@link SideSlipView} whether or not to slide around.
     * <p> It can set by call {@link #setScroollable(boolean)} and by attribute,
     * and the default value is false, so it cannot slide
     * @attr ref {@link com.tjut.mianliao.R.styleable#SideSlipView_scrollable}
     */
    private boolean mScrollable;
    
    private ViewGroup mMenu;
    
    private ViewGroup mContent;
    
    private View mMongoliaView;

    public SideSlipView(Context context) {
        this(context, null);
    }

    public SideSlipView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SideSlipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context.getApplicationContext();
        mScreenWidth = Utils.getDisplayWidth();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SideSlipView);
        mScaleRightView = ta.getBoolean(R.styleable.SideSlipView_scale_right_view, false);
        mScrollable = ta.getBoolean(R.styleable.SideSlipView_scrollable, false);
        int n = ta.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = ta.getIndex(i);
            switch (attr) {
                case R.styleable.SideSlipView_right_padding:
                    mMenuRightPadding = ta.getDimensionPixelSize(attr,
                            (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 100f,
                            getResources().getDisplayMetrics()));
                    break;
            }
        }
        ta.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!mOnce) {
            LinearLayout wrapper = (LinearLayout) getChildAt(0);
            mMenu = (ViewGroup) wrapper.getChildAt(0);
            mContent = (ViewGroup) wrapper.getChildAt(1);

            mMenuWidth = mScreenWidth - mMenuRightPadding;
            mHalfMenuWidth = mMenuWidth / 2;
            mMenu.getLayoutParams().width = mMenuWidth;
            mContent.getLayoutParams().width = mScreenWidth;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Utils.logD(TAG, "onMeasure");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            this.scrollTo(mMenuWidth, 0);
            mOnce = true;
            Utils.logD(TAG, "onlayout,changed");
        }
    }
    
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        float scale = l * 1.0f / mMenuWidth;
        float leftScale = 1 - 0.3f * scale;
        float rightScale = 0.8f + scale * 0.2f;
        if (mScaleRightView) {
            ViewHelper.setScaleX(mMenu, leftScale);
            ViewHelper.setScaleY(mMenu, leftScale);
            ViewHelper.setAlpha(mMenu, 0.6f + 0.4f * (1 - scale));
        }
        
        ViewHelper.setTranslationX(mMenu, mMenuWidth * scale);
        
        if (mScaleRightView) {
            ViewHelper.setPivotX(mContent, 0);
            ViewHelper.setPivotY(mContent, mContent.getHeight() / 2);
            ViewHelper.setScaleX(mContent, rightScale);
            ViewHelper.setScaleY(mContent, rightScale);
        }
        Utils.logD(TAG, "onScrollChanged:l=" + l +"--t=" + t + "--oldl=" + oldl + "--oldt=" + oldt);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                int scrollX = getScrollX();
                if (scrollX > mHalfMenuWidth) {
                    mIsOpen = false;
                    this.smoothScrollTo(mMenuWidth, 0);
                    removeMongoliaView();
                    Utils.logD(TAG, "close menu");
                } else {
                    mIsOpen = true;
                    this.smoothScrollTo(0, 0);
                    addMongoliaView();
                    Utils.logD(TAG, "open menu");
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if(!mScrollable){
                    return true;
                }
        }
        Utils.logD(TAG, "onTouchEvent");
        return super.onTouchEvent(ev);
    }
    
    /**
     * Call this method can reset the left value/menu padding-right value,
     * and it can setting by attribute also.
     * @attr ref {@link com.tjut.mianliao.R.styleable#SideSlipView_scale_right_view}
     * @param rightPadding
     */
    public void setRightMenuPadding(int rightPadding) {
        mMenuRightPadding = rightPadding;
    }
    
    /**
     * Call this method can set the {@link SideSlipView} whether or not can slide,
     * and it can setting by attribute also.
     * @attr ref {@link com.tjut.mianliao.R.styleable#SideSlipView_scrollable}
     * @param scrollable
     */
    public void setScroollable(boolean scrollable) {
        mScrollable = scrollable;
    }
    
    /**
     * Call this method can set the right view about {@link SideSlipView} whether or not
     * to scale, and it can setting by attribute also.
     * @attr ref {@link com.tjut.mianliao.R.styleable#SideSlipView_scale_right_view}
     */
    public void setRightViewScale(boolean scale) {
        mScaleRightView = scale;
    }
    
    /**
     * @return True if menu is open
     */
    public boolean isMenuOpen() {
        return mIsOpen;
    }
    
    /**
     * Open menu
     */
    public void openMenu() {
        if (mIsOpen)
            return;
        this.smoothScrollTo(0, 0);
        mIsOpen = true;
        addMongoliaView();
        Utils.logD(TAG, "open menu method");
    }

    /**
     * Close menu
     */
    public void closeMenu() {
        if (mIsOpen) {
            this.smoothScrollTo(mMenuWidth, 0);
            removeMongoliaView();
            mIsOpen = false;
            Utils.logD(TAG, "close menu method");
        }
    }

    /**
     * Toggle menu status
     */
    public void toggleMenu() {
        if (mIsOpen) {
            closeMenu();
            if (mListener != null) {
                mListener.onMenuToggle(STATUS_MENU_CLOSE);
            }
        } else {
            openMenu();
            if (mListener != null) {
                mListener.onMenuToggle(STATUS_MENU_OPEN);
            }
        }
        Utils.logD(TAG, "toggle menu");
    }
    
    private void addMongoliaView() {
        if (mMongoliaView == null) {
            mMongoliaView = new View(mContext);
            mMongoliaView.setBackgroundColor(Color.TRANSPARENT);
            LayoutParams params = new LayoutParams(mScreenWidth, mContent.getHeight());
            mMongoliaView.setLayoutParams(params);
            mMongoliaView.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    closeMenu();
                }
            });
        }
        mContent.removeView(mMongoliaView);
        mContent.addView(mMongoliaView);
    }
    
    private void removeMongoliaView() {
        mContent.removeView(mMongoliaView);
    }
    
    OnMenuToggleListener mListener;
    
    public void setOnMenuShowListener(OnMenuToggleListener listener) {
        mListener = listener;
    }
    
    public interface OnMenuToggleListener{
        void onMenuToggle(int status);
    }
}
