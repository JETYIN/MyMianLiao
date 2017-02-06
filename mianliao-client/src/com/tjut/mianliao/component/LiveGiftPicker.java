package com.tjut.mianliao.component;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tjut.mianliao.R;
import com.tjut.mianliao.live.LiveGift;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by j_hao on 2016/6/24.
 */
public class LiveGiftPicker extends LinearLayout implements VisibleDelay, View.OnClickListener {
    /**
     * 大表情
     **/
    private static final int NUM_COLUM = 5;
    private Context mContext;
    /**
     * 滑动预览
     **/
    private ViewPager mVpEmotions;
    /**
     * view视图
     **/
    private PageIndicator mPiEmotions;
    private List<List<LiveGift>> mPageGifts;
    private PagerAdapter mPagerAdapter;
    private LayoutInflater mLayoutInflater;
    private List<LiveGift> mListGift;

    private TextView mTvTimerCount;

    private int mPageSizeBig;
    private int mNumColums;

    private int mEmotionBig;
    private boolean mVisible;

    private OnLiveGiftClickListener mClickListener;
    private OnSendMenuClickLisener mMenuClickLisener;

    public LiveGiftPicker(Context context) {
        this(context, null);
    }

    public LiveGiftPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setOrientation(VERTICAL);
        /**点击图片按钮弹出礼物列表**/
        inflate(context, R.layout.comp_gift_picker, this);
        mTvTimerCount = (TextView) findViewById(R.id.tv_timer_count);
        findViewById(R.id.tv_send_gift).setOnClickListener(this);
        mEmotionBig = context.getResources().getDimensionPixelSize(R.dimen.emo_picker_item_size_big);
        /**大表情尺寸4*2**/
        mPageSizeBig = 5 * 2;
        mLayoutInflater = LayoutInflater.from(context);

        mPiEmotions = (PageIndicator) findViewById(R.id.pi_emotions);
        mVpEmotions = (ViewPager) findViewById(R.id.vp_emotions);

        mListGift = new ArrayList<>();
        mPageGifts = new ArrayList<List<LiveGift>>();
        mPagerAdapter = new PagerAdapter();
        mVpEmotions.setAdapter(mPagerAdapter);
        mVpEmotions.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mPiEmotions.setCurrentPage(position);
            }
        });
        /**网络连接成功显示礼物**/
        fetchLiveGift();
    }

    public TextView getTimerCountView() {
        return mTvTimerCount;
    }

    public void hideCountView(){
        mTvTimerCount.setVisibility(GONE);
    }

    @Override
    public void setVisibleDelayed(boolean visible) {
        mVisible = visible;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                setVisibility(mVisible ? VISIBLE : GONE);
            }
        }, DELAY_MILLS);
    }

    @Override
    public void setVisible(boolean visible) {
        mVisible = visible;
        setVisibility(visible ? VISIBLE : GONE);
    }

    public LiveGift getLiveGiftInfo(int giftId) {
        for (LiveGift gift : mListGift) {
            if (gift.giftId ==  giftId) {
                return gift;
            }
        }
        return null;
    }

    @Override
    public boolean isVisible() {
        return mVisible;
    }

    public void registerGiftClickListener(OnLiveGiftClickListener listener) {
        mClickListener = listener;
    }

    public void registerMenuClickListner(OnSendMenuClickLisener listener) {
        mMenuClickLisener = listener;
    }

    private void showEmotions(List<LiveGift> emotions) {
        int count = emotions.size();
        int size = mPageSizeBig; // Reserve for backspace
        int pages = (int) Math.ceil(count / (double) size);
        for (int i = 0; i < pages; i++) {
            int start = i * size;
            int end = start + size;
            if (end > count) {
                end = count;
            }
            mPageGifts.add(emotions.subList(start, end));
        }
        mNumColums = NUM_COLUM;
        mPiEmotions.setNumPages(mPageGifts.size());
        mPagerAdapter.notifyDataSetChanged();
    }

    /**
     * 异步任务获取表情
     **/

    public void fetchLiveGift() {
        new FetchPostsTask().executeLong();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_send_gift:
                mTvTimerCount.setVisibility(View.VISIBLE);
                if (mMenuClickLisener != null) {
                    mMenuClickLisener.onMenuClick();
                }
                break;
        }
    }

    private class FetchPostsTask extends MsTask {

        public FetchPostsTask() {
            super(mContext, MsRequest.LIVE_LIST_GIFTS);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                /**保存数据库**/
                ArrayList<LiveGift> gifs = JsonUtil.getArray(response.getJsonArray(), LiveGift.TRANSFORMER);
                if (gifs != null && gifs.size() > 0) {
                    mPageGifts.clear();
                    mListGift.clear();
                    mListGift = gifs;
                    showEmotions(mListGift);
                }
            }
        }
    }

    /**
     * 表情adapter
     **/
    private class PagerAdapter extends ViewSwitcherAdapter {

        @Override
        public int getCount() {
            return mPageGifts.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mLayoutInflater.inflate(R.layout.emotion_picker_page, parent, false);
            } else {
                view = convertView;
            }

            GridAdapter adapter = new GridAdapter(mPageGifts.get(position));
            GridView gridView = (GridView) view;
            gridView.setNumColumns(mNumColums);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(adapter);

            return view;
        }
    }

    private class GridAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
        private List<LiveGift> mEmotions;

        public GridAdapter(List<LiveGift> emotions) {
            mEmotions = emotions;
        }

        @Override
        public int getCount() {
            return mEmotions.size();
        }

        @Override
        public LiveGift getItem(int position) {
            return mEmotions.get(position);

        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * 设置数据显示
         **/
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mLayoutInflater.inflate(R.layout.item_live_gift, parent, false);
            } else {
                view = convertView;
            }
            ImageView ivIcon = (ImageView) view.findViewById(R.id.live_gift_image);
            TextView tvPrice = (TextView) view.findViewById(R.id.live_git_price);
            TextView tvExp = (TextView) view.findViewById(R.id.live_git_exprience);
            LiveGift gift = getItem(position);
            if (!TextUtils.isEmpty(gift.icon)) {
                Picasso.with(mContext)
                        .load(gift.icon)
                        .into(ivIcon);
            }
            tvPrice.setText(String.valueOf(gift.price));
            tvExp.setText(String.valueOf(gift.price * 10)+"经验值");
            return view;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mClickListener != null) {
                LiveGift gift = (LiveGift) parent.getItemAtPosition(position);
                if (mClickListener != null) {
                    mClickListener.onGiftClick(gift);
                }
            }
        }
    }

       /* private boolean isLastPosition(int position) {
            return false;
        }
    }*/

    public interface OnLiveGiftClickListener {
        public void onGiftClick(LiveGift gift);
    }

    public interface OnSendMenuClickLisener{
        void onMenuClick();
    }
}
