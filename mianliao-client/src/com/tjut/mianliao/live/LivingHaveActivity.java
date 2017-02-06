package com.tjut.mianliao.live;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.TitleBar;
import com.tjut.mianliao.contact.ContactUpdateCenter;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.LiveInfo;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

import java.util.ArrayList;

/**
 * Created by YoopWu on 2016/7/14 0014.
 * 当前activity用于展示登录用户预约的/购买的/自己的视频信息列表。用户需要传入需要显示的视频数据类型。
 * <p>
 * 视频数据显示类型分为三种:
 *
 * @code 1: TYPE_MY_OWN  用户自己的录制的直播视频
 * @code 2: TYPE_APPOINT 用户预约的视频
 * @code 3: TYPE_BUY //用户购买的视频
 * </p>
 */
public class LivingHaveActivity extends BaseActivity implements View.OnClickListener,
        PullToRefreshBase.OnRefreshListener2, ContactUpdateCenter.ContactObserver {

    public static final String LIVE_OWN_TYPE = "live_owen_type";

    public static final int TYPE_MY_BOOKING = 1;
    public static final int TYPE_MY_BUY = 2;
    public static final int TYPE_MY_PUBLISH = 3;

    public static final int IS_LIVING = 1;
    public static final int IS_LIVING_ENDDING = 0;
    public static final int IS_REPLAY = 2;

    @ViewInject(R.id.ptr_gv_lives)
    private PullToRefreshGridView mPtrGvLive;
    @ViewInject(R.id.rl_operate)
    private RelativeLayout mRlOperate;
    @ViewInject(R.id.tv_check)
    private TextView mTvCheck;
    @ViewInject(R.id.tv_notice)
    private TextView mTvNotice;

    private LayoutInflater mInflater;

    private LivesAdapter mAdapter;

    private ArrayList<LiveInfo> mBookingLives;
    private ArrayList<LiveInfo> mBuyLives;
    private ArrayList<LiveInfo> mPublishLives;

    private ArrayList<LiveInfo> mLiveInfos;
    private ArrayList<LiveInfo> mChoosedLiveInfos;

    private UserInfoManager mUserInfoManager;

    private int mOwnLiveType;
    private boolean mCanCheck;
    private boolean mCheckAll;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_living_have;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mUserInfoManager = UserInfoManager.getInstance(this);
        ContactUpdateCenter.registerObserver(this);
        mInflater = LayoutInflater.from(this);
        mOwnLiveType = getIntent().getIntExtra(LIVE_OWN_TYPE, TYPE_MY_PUBLISH);
        mLiveInfos = new ArrayList<>();
        mChoosedLiveInfos = new ArrayList<>();
        mBookingLives = new ArrayList<>();
        mBuyLives = new ArrayList<>();
        mPublishLives = new ArrayList<>();

        setTitleByType();
        setNoticeInfo();
        mPtrGvLive.setMode(PullToRefreshBase.Mode.BOTH);
        mPtrGvLive.setOnRefreshListener(this);
        mAdapter = new LivesAdapter();
        mPtrGvLive.setAdapter(mAdapter);
        new LoadLiveTask().executeLong();
    }

    private void setTitleByType() {
        TitleBar titleBar = getTitleBar();
        titleBar.showRightText(R.string.delete, this);
        switch (mOwnLiveType) {
            case TYPE_MY_PUBLISH:
                titleBar.setTitle(getString(R.string.live_my_complete));
                break;
            case TYPE_MY_BOOKING:
                titleBar.setTitle(getString(R.string.live_my_appoint));
                break;
            case TYPE_MY_BUY:
                titleBar.setTitle(getString(R.string.live_my_buy));
                break;
            default:
                break;
        }
    }

    private void choose(LiveInfo info) {
        if (info.choosed) {
            mChoosedLiveInfos.remove(info);
        } else {
            mChoosedLiveInfos.add(info);
        }
        info.choosed = !info.choosed;
        mAdapter.notifyDataSetChanged();
    }

    private void checkAll(boolean chooseAll) {
        if (chooseAll) {
            mChoosedLiveInfos.clear();
            for (LiveInfo info : mLiveInfos) {
                info.choosed = true;
                mChoosedLiveInfos.add(info);
            }
        } else {
            for (LiveInfo info : mLiveInfos) {
                info.choosed = false;
            }
            mChoosedLiveInfos.clear();
        }
        mAdapter.notifyDataSetChanged();
    }

    private void setNoticeInfo() {
        switch (mOwnLiveType) {
            case TYPE_MY_BOOKING:

                break;
            case TYPE_MY_BUY:

                break;
            case TYPE_MY_PUBLISH:

                break;
        }
    }

    private void showNoticeInfo(boolean show) {
        mTvNotice.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                mCanCheck = !mCanCheck;
                if (mCanCheck) {
                    mRlOperate.setVisibility(View.VISIBLE);
                } else {
                    mRlOperate.setVisibility(View.GONE);
                }
                break;
            case R.id.tv_check:
                mCheckAll = !mCheckAll;
                checkAll(mCheckAll);
                break;
            case R.id.tv_delete:
                // delete live info
                toast("即将删除" + mChoosedLiveInfos.size() + "条视频数据!");
                break;
            default:
                break;
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        new LoadLiveTask().executeLong();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {

    }

    @Override
    public void onContactsUpdated(ContactUpdateCenter.UpdateType type, Object data) {
        mAdapter.notifyDataSetChanged();
    }

    private class LivesAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mLiveInfos.size();
        }

        @Override
        public LiveInfo getItem(int position) {
            return mLiveInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_living_have, parent, false);
            }
            ImageView ivPreview = (ImageView) convertView.findViewById(R.id.iv_preview);
            TextView tvFlag = (TextView) convertView.findViewById(R.id.tv_flag);
            TextView tvSchooll = (TextView) convertView.findViewById(R.id.tv_school);
            FrameLayout flChoose = (FrameLayout) convertView.findViewById(R.id.fl_choosed);
            TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
            TextView tvCount = (TextView) convertView.findViewById(R.id.tv_count);
            TextView tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            final LiveInfo info = getItem(position);
            //UserInfo userInfo = mUserInfoManager.getUserInfo(info.uid);
           /* if (userInfo == null) {
//                 mUserInfoManager.a
            } else {*/
            if (info != null) {
                tvName.setText(info.nick);
                tvSchooll.setText(info.school);
                tvCount.setText(getString(R.string.living_see_people_num, info.member_numbers));
                tvTitle.setText(info.title);
                switch (info.status) {
                    case IS_LIVING:
                        tvFlag.setText("直播");
                        break;
                    case IS_LIVING_ENDDING:
                        tvFlag.setText("结束");
                        break;
                    case IS_REPLAY:
                        tvFlag.setText("录播");
                        break;
                }
            }
            if (!TextUtils.isEmpty(info.prevUrl)) {
                Picasso.with(LivingHaveActivity.this)
                        .load(info.prevUrl)
                        .into(ivPreview);
            } else {
                Picasso.with(LivingHaveActivity.this)
                        .load(info.avatar)
                        .into(ivPreview);
            }
            if (mCanCheck) {
                if (info.choosed) {
                    flChoose.setVisibility(View.VISIBLE);
                } else {
                    flChoose.setVisibility(View.GONE);
                }
            } else {
                flChoose.setVisibility(View.GONE);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    choose(info);
                }
            });
            return convertView;
        }
    }

    private class LoadLiveTask extends MsTask {
        public LoadLiveTask() {
            super(LivingHaveActivity.this, MsRequest.LIST_MY_OWN_LIVE);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mBookingLives = JsonUtil.getArray(response.getJsonObject().optJSONArray("order"), LiveInfo.TRANSFORMER);
                mBuyLives = JsonUtil.getArray(response.getJsonObject().optJSONArray("buy"), LiveInfo.TRANSFORMER);
                mPublishLives = JsonUtil.getArray(response.getJsonObject().optJSONArray("publish"), LiveInfo.TRANSFORMER);
                switch (mOwnLiveType) {
                    case TYPE_MY_BOOKING:
                        mLiveInfos.clear();
                        mLiveInfos.addAll(mBookingLives);
                        if (mLiveInfos.size() == 0) {
                            showNoticeInfo(true);
                        }
                        mAdapter.notifyDataSetChanged();
                        break;
                    case TYPE_MY_BUY:
                        mLiveInfos.clear();
                        mLiveInfos.addAll(mBuyLives);
                        if (mLiveInfos.size() == 0) {
                            showNoticeInfo(true);
                        }
                        mAdapter.notifyDataSetChanged();
                        break;
                    case TYPE_MY_PUBLISH:
                        mLiveInfos.clear();
                        mLiveInfos.addAll(mPublishLives);
                        if (mLiveInfos.size() == 0) {
                            showNoticeInfo(true);
                        }
                        mAdapter.notifyDataSetChanged();
                        break;

                }
            }
        }
    }
}
