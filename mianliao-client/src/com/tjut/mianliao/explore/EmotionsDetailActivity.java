package com.tjut.mianliao.explore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.PageIndicator;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.ViewSwitcherAdapter;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.IMResource;
import com.tjut.mianliao.im.IMResourceManager;
import com.tjut.mianliao.im.IMResourceManager.IMResourceListener;
import com.tjut.mianliao.util.Utils;

public class EmotionsDetailActivity extends BaseActivity implements IMResourceListener,
        OnClickListener, DialogInterface.OnClickListener {

    public static final String EXT_RES_ID = "ext_res_id";
    public static final String EXT_HAS_DETAIL = "ext_has_detail";
    
    private static final int ROWS = 5;
    private static final int COLS = 6;

    private ProImageView mPivEmoBg;
    private TextView mTvName;
    private TextView mTvIntro;
    private TextView mTvPrice, mTvVipPrice;
    private TextView mTvOper;
    private LinearLayout mLlPrice;
    private TextView mTvFree;
    private PageIndicator mPiEmotions;
    private int mPageSize;
    private PagerAdapter mPagerAdapter;
    private List<List<String>> mPageEmotions;
    private IMResourceManager mResourceManager;
    private ViewPager mVpEmotions;
    private IMResource mResource;
    private int mResId;
    private ArrayList<String> emotions;
    private LightDialog mShowInfoDialog, mPayDialog;
    private boolean mClickble;
    private boolean  mHasContent;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_emotions_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(R.string.explore_chat_emotion);
        emotions = new ArrayList<String>();
        mResourceManager = IMResourceManager.getInstance(this);
        mResourceManager.registerIMResourceListener(this);
        mResource = getIntent().getParcelableExtra(DressUpMallActivty.EXT_DATE);
        mResId = getIntent().getIntExtra(EXT_RES_ID, 0);
        if (mResId == 0 && mResource != null) {
            mResId = mResource.id;
        }
        if (mResId > 0) {
            mResourceManager.getImResourceById(IMResource.TYPE_EMOTION_PACKAGE, mResId);
        }
        mPivEmoBg = (ProImageView) findViewById(R.id.piv_emotion_bg);
        mTvName = (TextView) findViewById(R.id.tv_emotion_name);
        mTvIntro = (TextView) findViewById(R.id.tv_emotion_intro);
        mTvPrice = (TextView) findViewById(R.id.tv_price);
        mTvVipPrice = (TextView) findViewById(R.id.tv_vip_price);
        mLlPrice = (LinearLayout) findViewById(R.id.ll_price);
        mPiEmotions = (PageIndicator) findViewById(R.id.pi_emotions);
        mVpEmotions = (ViewPager) findViewById(R.id.vp_emotions);
        mTvOper = (TextView) findViewById(R.id.tv_oper);
        mTvOper.setEnabled(mResource != null);
        mTvFree = (TextView) findViewById(R.id.tv_free);

        // Simulated data
        mPageSize = ROWS * COLS;
        mPageEmotions = new ArrayList<List<String>>();
        mPagerAdapter = new PagerAdapter();
        mVpEmotions.setAdapter(mPagerAdapter);
        mVpEmotions.setOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mPiEmotions.setCurrentPage(position);
            }
        });

        if (mResource != null) {
            fillData();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_oper:
                if (!mResource.add) {
                    mResourceManager.AddImResource(mResource.id);
                    getTitleBar().showProgress();
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (mResourceManager != null) {
            mResourceManager.unregisterIMResourceListener(this);
        }
        super.onDestroy();
    }

    @Override
    public void onAddResSuccess() {
        getTitleBar().hideProgress();
        mResource.add = true;
        mResourceManager.UnzipFile(mResource);
        fillData();
    }

    @Override
    public void onUsingResUpdated(int userId, int type, IMResource res) {
    }

    @Override
    public void onGetImResourceSuccess(int type, int requestCode, ArrayList<IMResource> mResources) {
        if (type == IMResource.TYPE_EMOTION_PACKAGE) {
            if (mResources != null && mResources.size() > 0) {
                mResource = mResources.get(0);
                String url = mResource.urls[0][0];
                if(url == null || "".equals(url)){
                    findViewById(R.id.ll_detail).setVisibility(View.GONE);
                    findViewById(R.id.iv_no_detail).setVisibility(View.VISIBLE);
                    return;
                }
                mResourceManager.UnzipFile(mResource);
                findViewById(R.id.ll_detail).setVisibility(View.VISIBLE);
                mTvOper.setEnabled(true);
                fillData();
            }
        }
    }

    @Override
    public void onUnzipSuccess() {
        getEmotions(mResource.urls[0][0]);
        showEmotions();
        mPagerAdapter.notifyDataSetChanged();
        getTitleBar().hideProgress();
    }

    @Override
    public void onUseResSuccess(IMResource res) {
    }

    @Override
    public void onUnuseResSuccess() {
    }

    private void fillData() {
        mPivEmoBg.setImage(mResource.urls[0][1], R.drawable.bg_img_loading);
        mTvName.setText(mResource.name);
        mTvIntro.setText(mResource.intro);
        mTvOper.setText(mResource.add ? getString(R.string.pay_have) : mResource.isFree() ? getString(R.string.pay_get)
                : getString(R.string.pay_buy));
        mTvOper.setBackgroundResource(mResource.add ? R.drawable.btn_gray_unclickble : R.drawable.btn_dark_green);
        if (mResource.isFree()) {
            mLlPrice.setVisibility(View.GONE);
            mTvFree.setVisibility(View.VISIBLE);
            mTvFree.setText(mResource.isAllFree() ? getString(R.string.pay_free) : getString(R.string.pay_vip_free));
        } else {
            mLlPrice.setVisibility(View.VISIBLE);
            mTvFree.setVisibility(View.GONE);
            mTvPrice.setText(mResource.getImResNormalPrice());
            mTvVipPrice.setText(mResource.getImResVipPrice());
        }
    }

    private void showEmotions() {
        mPageEmotions.clear();
        int count = emotions.size();
        int pages = (int) Math.ceil(count / (double) mPageSize);
        for (int i = 0; i < pages; i++) {
            int start = i * mPageSize;
            int end = start + mPageSize;
            if (end > count) {
                end = count;
            }
            mPageEmotions.add(emotions.subList(start, end));
        }

        mPiEmotions.setNumPages(mPageEmotions.size());
        mPagerAdapter.notifyDataSetChanged();
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
                view = mInflater.inflate(R.layout.emotion_picker_page, parent, false);
            } else {
                view = convertView;
            }
            GridAdapter adapter = new GridAdapter(mPageEmotions.get(position));
            GridView gridView = (GridView) view;
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(adapter);

            return view;
        }
    }

    private class GridAdapter extends BaseAdapter implements OnItemClickListener {
        private List<String> mEmotions;

        public GridAdapter(List<String> emotions) {
            mEmotions = emotions;
        }

        @Override
        public int getCount() {
            if (mEmotions.size() < mPageSize) {
                return mEmotions.size();
            }
            return mPageSize;
        }

        @Override
        public String getItem(int position) {
            return mEmotions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.emotion_picker_item, parent, false);
            } else {
                view = convertView;
            }
            view.setVisibility(View.VISIBLE);
            ImageView imageView = (ImageView) view;

            String emotion = getItem(position);
            imageView.setImageBitmap(Utils.fileToBitmap(emotion));
            return view;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    }

    private void getEmotions(String url) {
        emotions.clear();
        File file = new File(Utils.getElementUnzipfilePath(url));
        File subFile[] = file.listFiles();
        for (int i = 0; i < subFile.length; i++) {
            String filePath = subFile[i].getAbsolutePath();
            int index = filePath.lastIndexOf("/") + 1;
            String fileName = filePath.substring(index);
            if (!fileName.startsWith("__") && !fileName.equals("icon.png") && !fileName.endsWith(".plist")) {
                emotions.add(Utils.getFileByPath(filePath).getAbsolutePath());
            }
        }
    }

    @Override
    public void onAddResFail(int code) {
        switch (code) {
            case 1501://FAIL_IM_USER_RESOURCE_NOT_EXIST
                showInfoDialog(getString(R.string.dressup_mall_the_res_not_exiest), false);
                break;
            case 2402://FAIL_TRADE_PRICE_NOT_ENOUGH
                showInfoDialog(getString(R.string.dressup_mall_the_money_low), false);
                break;
            case 2403://FAIL_TRADE_CREDIT_NOT_ENOUGH
                showInfoDialog(getString(R.string.dressup_mall_the_integral_low), false);
                break;
            case 2409: //FAIL_TRADE_RESOURCE_NEED_VIP
                showInfoDialog(getString(R.string.dressup_mall_the_vip_can_use), true);
                break;
            default:
                break;
        }
    }


    private void showInfoDialog(String msg, boolean clickble) {
        if (mShowInfoDialog == null) {
            mShowInfoDialog = new LightDialog(this).setTitleLd(R.string.dressup_mall_notice_info)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, this);
        }
        mClickble = clickble;
        mShowInfoDialog.setMessage(msg);
        mShowInfoDialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mShowInfoDialog) {
            if (mClickble) {
                openVip();
            }
        }
    }

    private void openVip() {
        Intent intent = new Intent(this, BuyVipHomePageActivity.class);
        intent.putExtra(BuyVipHomePageActivity.EXT_USER_INFO,
                AccountInfo.getInstance(this).getUserInfo());
        startActivity(intent);
    }
}
