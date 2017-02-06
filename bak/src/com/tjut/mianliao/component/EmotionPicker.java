package com.tjut.mianliao.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.tab.Tab;
import com.tjut.mianliao.component.tab.TabController;
import com.tjut.mianliao.component.tab.TabController.TabListener;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.Emotion;
import com.tjut.mianliao.data.explore.EmotionsInfo;
import com.tjut.mianliao.explore.DressUpMallActivty;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.EmotionManager;

public class EmotionPicker extends LinearLayout implements VisibleDelay {

    private static final int TAB_EMOJI = 0;
    
    private static final int NUM_COLUM_NORMAL = 6;
    private static final int NUM_COLUM_BIG = 4;

    private static final int DEFAUIL_EMOTION_SIZE = 2;

    private Context mContext;
    
    private ViewPager mVpEmotions;
    private PageIndicator mPiEmotions;
    private TabController mTabController;
    private PagerAdapter mPagerAdapter;

    private LayoutInflater mLayoutInflater;
    private EmotionManager mEmotionManager;
    private List<List<Emotion>> mPageEmotions;
    private int mPageSize, mPageSizeBig, mPageSizeNormal;
    private LinearLayout mLlEmoji;
    private int mNumColums;
    private int mEmotionSize, mEmotionBig, mEmotionNormal;

    List<EmotionsInfo> mCustomeEmotions;

    private boolean mVisible;
    private boolean mIsNightMode;
    private boolean mShowBigEmotion;
    private boolean mIsBigEmotionShow;
    
    private EmotionListener mEmotionListener;

    public EmotionPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mIsNightMode = Settings.getInstance(context).isNightMode();
        setOrientation(VERTICAL);
        inflate(context, R.layout.comp_emotion_picker, this);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EmotionPicker);
        mShowBigEmotion = ta.getBoolean(R.styleable.EmotionPicker_showBigEmotion, false);
        
        mEmotionNormal = context.getResources().getDimensionPixelSize(R.dimen.emo_picker_item_size);
        mEmotionBig = context.getResources().getDimensionPixelSize(R.dimen.emo_picker_item_size_big);
        
        int rows = getResources().getInteger(R.integer.emo_picker_rows);
        int cols = getResources().getInteger(R.integer.emo_picker_cols);
        mPageSizeNormal = rows * cols;
        
        mPageSizeBig = 4 * 2;

        mLayoutInflater = LayoutInflater.from(context);
        mEmotionManager = EmotionManager.getInstance(context);
//        mEmotionManager.showBigEmotion(mShowBigEmotion);
        mPageEmotions = new ArrayList<List<Emotion>>();

        mPiEmotions = (PageIndicator) findViewById(R.id.pi_emotions);
        mVpEmotions = (ViewPager) findViewById(R.id.vp_emotions);
        mLlEmoji = (LinearLayout) findViewById(R.id.ll_emoji);
        findViewById(R.id.iv_mall).setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                showDressMall();
            }
        });
        
        mPagerAdapter = new PagerAdapter();
        mVpEmotions.setAdapter(mPagerAdapter);
        mVpEmotions.setOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mPiEmotions.setCurrentPage(position);
            }
        });

        mTabController = new TabController();
        mTabController.add(makeTab(R.id.iv_default));
        mCustomeEmotions = DataHelper.queryEmotionInfo(context);
        this.setCustomEmoj();

        mTabController.setListener(new TabListener() {
            @Override
            public void onTabSelectionChanged(int index, boolean selected, Tab tab) {
                if (selected) {
                    switch (index) {
                        case TAB_EMOJI:
                            showEmotions(mEmotionManager.getEmojiList(), false);
                            break;
                        default:
                            showEmotions(mEmotionManager.getCustomEmojiList(index - 1), true);
                            break;
                    }
                } 
            }
        });
        mTabController.select(TAB_EMOJI);

    }

    protected void showDressMall() {
        mContext.startActivity(new Intent(mContext, DressUpMallActivty.class));
    }

    private void setCustomEmoj() {
        if (!mShowBigEmotion) {
            return;
        }
        if (mCustomeEmotions != null && mCustomeEmotions.size() > 0) {
            for(EmotionsInfo info : mCustomeEmotions){
                mTabController.add(makeTab(info));
            }
            addEmptyAreaBg();
        }
    }

    private void addEmptyAreaBg() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int mTotleWidth = wm.getDefaultDisplay().getWidth();
        int width = mContext.getResources().getDimensionPixelSize(R.dimen.emo_picker_item_size);
        int mCurrentWidth = width * (mCustomeEmotions.size() + DEFAUIL_EMOTION_SIZE);
        if (mCurrentWidth < mTotleWidth) {
            TextView tv = new TextView(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mTotleWidth - mCurrentWidth,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            tv.setBackgroundResource(mIsNightMode ? R.drawable.pic_bg_emo_black : R.drawable.pic_bg_emo);
            mLlEmoji.addView(tv, params);
        }
    }

    public void setEmotionListener(EmotionListener listener) {
        mEmotionListener = listener;
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

    @Override
    public boolean isVisible() {
        return mVisible;
    }

    private void showEmotions(List<Emotion> emotions, boolean isBig) {
        if (emotions == null) {
            return;
        }
        
        mIsBigEmotionShow = isBig;
        mPageSize = isBig ? mPageSizeBig : mPageSizeNormal;
        mPageEmotions.clear();
        int count = emotions.size();
        int size = isBig ? mPageSize : mPageSize - 1; // Reserve for backspace
        int pages = (int) Math.ceil(count / (double) size);
        for (int i = 0; i < pages; i++) {
            int start = i * size;
            int end = start + size;
            if (end > count) {
                end = count;
            }
            mPageEmotions.add(emotions.subList(start, end));
        }
        mEmotionSize = isBig ? mEmotionBig : mEmotionNormal;
        mNumColums = isBig ? NUM_COLUM_BIG : NUM_COLUM_NORMAL;
        mPiEmotions.setNumPages(mPageEmotions.size());
        mPagerAdapter.notifyDataSetChanged();
    }

    private ImageTab makeTab(int id) {
        ImageView iv = (ImageView) findViewById(id);
        ImageTab tab = new ImageTab(iv);
        tab.setNightMode(mIsNightMode);
        return tab;
    }

    private ImageTab makeTab(EmotionsInfo emotion) {
        ImageView iv = (ImageView) mLayoutInflater.inflate(R.layout.item_emotion_picker, null);
        String iconPath = emotion.path + "/icon.png";
        Drawable drawable = Drawable.createFromPath(iconPath);
        iv.setImageDrawable(drawable);
        int width = mContext.getResources().getDimensionPixelSize(R.dimen.emo_picker_item_size);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                width, LinearLayout.LayoutParams.MATCH_PARENT);
        //调用addView()方法增加一个TextView到线性布局中
        mLlEmoji.addView(iv, p);
        ImageTab tab = new ImageTab(iv);
        tab.setNightMode(mIsNightMode);
        tab.setChosen(false);
        return tab;
    }

    private class PagerAdapter extends ViewSwitcherAdapter {

        @Override
        public int getCount() {
            return mPageEmotions.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mLayoutInflater.inflate(R.layout.emotion_picker_page, parent, false);
            } else {
                view = convertView;
            }

            GridAdapter adapter = new GridAdapter(mPageEmotions.get(position));
            GridView gridView = (GridView) view;
            gridView.setNumColumns(mNumColums);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(adapter);

            return view;
        }
    }

    private class GridAdapter extends BaseAdapter implements OnItemClickListener {
        private List<Emotion> mEmotions;

        public GridAdapter(List<Emotion> emotions) {
            mEmotions = emotions;
        }

        @Override
        public int getCount() {
            return mIsBigEmotionShow ? mEmotions.size() : mPageSize;
        }

        @Override
        public Emotion getItem(int position) {
            return mIsBigEmotionShow ? mEmotions.get(position) : position < mEmotions.size() ?
                    mEmotions.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mLayoutInflater.inflate(R.layout.emotion_picker_item, parent, false);
            } else {
                view = convertView;
            }
            view.setVisibility(VISIBLE);
            ImageView imageView = (ImageView) view;
            android.view.ViewGroup.LayoutParams lp = imageView.getLayoutParams();
            lp.width = mEmotionSize;
            lp.height = mEmotionSize;
            
            if (isLastPosition(position)) {
                imageView.setImageResource(R.drawable.ic_backspace);
            } else {
                Emotion emotion = getItem(position);
                if (emotion == null) {
                    imageView.setVisibility(INVISIBLE);
                } else {

                    if (emotion.getImageName() != null) {
                        Drawable drawable = Drawable.createFromPath(emotion.getImageName());
                        if (drawable != null) {
                            imageView.setImageDrawable(drawable);
                        } else {
                            imageView.setImageResource(emotion.resource);
                        }
                    } else {
                        imageView.setImageResource(emotion.resource);
                    }

                }
            }

            return view;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mEmotionListener != null) {
                if (isLastPosition(position)) {
                    mEmotionListener.onBackspaceClicked();
                } else {
                    Emotion emotion = (Emotion) parent.getItemAtPosition(position);
                    if (emotion != null) {
                        mEmotionListener.onEmotionClicked(emotion);
                    }
                }
            }
        }

        private boolean isLastPosition(int position) {
            return mIsBigEmotionShow ? false : position == getCount() - 1;
        }
    }

    public interface EmotionListener {
        public void onEmotionClicked(Emotion emotion);

        public void onBackspaceClicked();
    }
}
