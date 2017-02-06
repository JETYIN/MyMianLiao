package com.tjut.mianliao.component;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.profile.ProfileFragment;
import com.tjut.mianliao.profile.ProfileFragment.StickyScrollCallBack;

public class StickyPtrListView extends PullToRefreshListView {
    private StickyScrollCallBack scrollCallBack;

    public StickyPtrListView(Context context) {
        this(context, null);
    }

    public StickyPtrListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOverScrollMode(OVER_SCROLL_ALWAYS);
    }

    public void setScrollCallBack(StickyScrollCallBack scrollCallBack) {
        this.scrollCallBack = scrollCallBack;
        getRefreshableView().setOnScrollListener(mOnScrollListener);
    }

    private final OnScrollListener mOnScrollListener = new OnScrollListener() {

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                int visibleItemCount, int totalItemCount) {
            if (null == scrollCallBack) {
                return;
            }
            
            if (firstVisibleItem == 0) {
                View firstView = getChildAt(0);
                if (null != firstView) {
                    int firstTop = firstView.getTop();
                    if (firstTop < -ProfileFragment.sStickyTopToTab) {
                        firstTop = -ProfileFragment.sStickyTopToTab;
                    }
                    scrollCallBack.onScrollChanged(firstTop);
                }
            }
            else if (firstVisibleItem < 6) {
                scrollCallBack.onScrollChanged(-ProfileFragment.sStickyTopToTab);
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            Log.i("LeiTest", "onScrollStateChanged=" + scrollState);

            if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                animScrollY();
            }
        }
    };

    private void animScrollY() {
        int offsetDistance = 0, firstTop = 0;
        if (getRefreshableView().getFirstVisiblePosition() == 0) {
            View firstView = getChildAt(0);
            if (firstView != null) {
                firstTop = firstView.getTop();
                if (firstTop < -ProfileFragment.sStickyTopToTab / 2) {
                    offsetDistance = -ProfileFragment.sStickyTopToTab;
                }
            }

            if (firstTop != offsetDistance) {
                new AnimUiThread(firstTop, offsetDistance).start();
            }
        }
    }

    public void invalidScroll() {
    }

    public int getFirstViewScrollTop() {
        if (getRefreshableView().getFirstVisiblePosition() == 0) {
            View firstView = getRefreshableView().getChildAt(1);
            if (null != firstView) {
                return -firstView.getTop();
            }
        } else if (getRefreshableView().getFirstVisiblePosition() == 1) {
            View firstView = getRefreshableView().getChildAt(0);
            if (null != firstView) {
                return -firstView.getTop();
            }
        }
        return Integer.MAX_VALUE;
    }

    class AnimUiThread extends Thread {
        private int fromPos, toPos;

        public AnimUiThread(int fromPos, int toPos) {
            this.fromPos = fromPos;
            this.toPos = toPos;
        }

        @Override
        public void run() {
            int num = 10;
            for (int i = 0; i < num; i++) {
                try {
                    sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int tempPos = fromPos + (toPos - fromPos) * (i + 1) / num;
                Message msg = uiHandler.obtainMessage();
                msg.what = tempPos;
                msg.sendToTarget();
            }
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        uiHandler = null;
        super.onDetachedFromWindow();
    }

    private Handler uiHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int pos = msg.what;
            getRefreshableView().setSelectionFromTop(0, pos);
        };
    };
    
    public int getContentHeight() {
        ListView listView = getRefreshableView();
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return 0;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        return totalHeight;
    }

}
