package com.tjut.mianliao.component;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tjut.mianliao.R;

public class ViewSwitcher extends FrameLayout implements ViewPagerTouchListener {

    private static final long DEFAULT_SWITCH_INTERVAL = 3000L;

    private long mSwitchInterval = DEFAULT_SWITCH_INTERVAL;

    private DataSetObserver mDataSetObserver;
    private PagerAdapter mAdapter;

    private ImageView mIvLeft;
    private ImageView mIvRight;
    private MLViewPager mVpContainer;
    private PageIndicator mPiSwitcher;

    private Handler mHandler;
    private Runnable mRunnable;

    public void setViewPageParent(ViewGroup parent) {
        mVpContainer.setNestedpParent(parent);
    }

    public ViewSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.comp_view_switcher, this, true);

        mIvLeft = (ImageView) findViewById(R.id.iv_left);
        mIvLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPrevItem();
            }
        });

        mIvRight = (ImageView) findViewById(R.id.iv_right);
        mIvRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextItem();
            }
        });

        mPiSwitcher = (PageIndicator) findViewById(R.id.pi_switcher);
        mVpContainer = (MLViewPager) findViewById(R.id.vp_container);
        mVpContainer.setTouchListener(this);

        mVpContainer.setOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mPiSwitcher.setCurrentPage(position);
            }
        });

        mHandler = new Handler();

        mDataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                dataSetChanged();
            }
        };

        mRunnable = new Runnable() {
            @Override
            public void run() {
                showNextItem();
            }
        };
    }

    public void stop() {
        mHandler.removeCallbacks(mRunnable);
    }

    public void start() {
        if (mAdapter != null && mAdapter.getCount() > 1) {
            mHandler.postDelayed(mRunnable, mSwitchInterval);
        }
    }

    public void setSwitchInterval(long interval) {
        mSwitchInterval = interval;
    }

    public void setAdapter(PagerAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }

        mAdapter = adapter;
        mVpContainer.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerDataSetObserver(mDataSetObserver);
        }

        dataSetChanged();
    }

    private void dataSetChanged() {
        stop();
        int count = mAdapter == null ? 0 : mAdapter.getCount();
        if (count > 0) {
            setVisibility(VISIBLE);
            mPiSwitcher.setNumPages(count);
            if (count > 1) {
                mIvLeft.setVisibility(GONE);
                mIvRight.setVisibility(GONE);
                start();
            } else {
                mIvLeft.setVisibility(GONE);
                mIvRight.setVisibility(GONE);
            }
        } else {
            setVisibility(GONE);
        }
    }

    private void showPrevItem() {
        showItem(-1);
    }

    private void showNextItem() {
        showItem(1);
    }

    private void showItem(int shift) {
        stop();
        int count = mAdapter == null ? 0 : mAdapter.getCount();
        if (count == 0) {
            return;
        }

        int target = mVpContainer.getCurrentItem() + shift;
        if (target < 0) {
            target = count - 1;
        } else if (target >= count) {
            target = 0;
        }
        mVpContainer.setCurrentItem(target, true);
        start();
    }

    @Override
    public void onTouchMove() {
        stop();
    }

    @Override
    public void onTouchEnd() {
        start();
    }
}
