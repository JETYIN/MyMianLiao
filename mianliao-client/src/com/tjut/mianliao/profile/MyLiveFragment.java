package com.tjut.mianliao.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ExpandableGridView;
import com.tjut.mianliao.component.MyStickyPtrGridView;
import com.tjut.mianliao.component.StickyScrollView;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.LiveInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.live.LivingHaveActivity;
import com.tjut.mianliao.profile.ProfileFragment.StickyScrollCallBack;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

import java.util.ArrayList;

public class MyLiveFragment extends Fragment implements OnClickListener, PullToRefreshBase.OnRefreshListener2 {
    public static final int BOOKING = 1;
    public static final int BUY = 2;
    public static final int PUBLISH = 3;

    public static final String LIVE_OWN_TYPE = "live_owen_type";

    public static final String EXTRA_USER_GUID = "extra_user_guid";
    public static final String EXTRA_USER_FACE_ID = "extra_user_face_id";
    public static final String EXTRA_SHOW_CHAT_BUTTON = "extra_show_chat_button";
    public static final String EXTRA_NICK_NAME = "extra_nick_name";
    public static final String EXTRA_USER_DESC = "extra_user_desc";

    public static final int RESULT_UPDATED = 1;
    private static final int REQUEST_SET_BADGE = 200;
    protected static final int SHOW_IMAGE_REQUEST = 101;
    protected static final int REQUEST_UPDATE_NICK = 102;
    protected static final int REQUEST_UPDATE_DESC = 103;
    private static final int REQUEST_GOLD_DEPOS_CODE = 105;
    private static final int REQUEST_MY_MEDALS_CODE = 600;

    @ViewInject(R.id.gv_live_my_pay)
    private ExpandableGridView mGvMyPayLive;
    @ViewInject(R.id.gv_live_publish)
    private ExpandableGridView mGvPublishLive;
    @ViewInject(R.id.gv_live_booking)
    private ExpandableGridView mGvBookingLive;
    @ViewInject(R.id.gv_live)
    private MyStickyPtrGridView mGvOtherLive;
    @ViewInject(R.id.msc_view)
    private StickyScrollView mScLive;
    @ViewInject(R.id.ll_my_live)
    private LinearLayout mLlMyLive;
    @ViewInject(R.id.rl_pay_live_title)
    private RelativeLayout mRlPayLive;
    @ViewInject(R.id.rl_booking_live_title)
    private RelativeLayout mRBookingLive;
    @ViewInject(R.id.rl_publish_live_title)
    private RelativeLayout mRPublishLive;
    /**
     * 预定，购买，发布
     **/
    private ArrayList<LiveInfo> mPayLives;
    private ArrayList<LiveInfo> mPublishLives;
    private ArrayList<LiveInfo> mbookingLives;
    private ArrayList<LiveInfo> otherLives;

    private LayoutInflater mInflater;
    private boolean mIsMe;
    private UserInfo mUserInfo;
    private StickyScrollCallBack mScrollListener;

    private UserInfoManager mUserInfoManager;

    private SearchLiveAdapter mPayAdapter, mPublishAdapter, mBookingAdapter, otherAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserInfoManager = UserInfoManager.getInstance(getActivity());
        mUserInfo = AccountInfo.getInstance(getActivity()).getUserInfo();
        mIsMe = mUserInfo.isMine(getActivity());
        mInflater = LayoutInflater.from(getActivity());
        mPayLives = new ArrayList<>();
        mPublishLives = new ArrayList<>();
        mbookingLives = new ArrayList<>();
        otherLives = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_live_list_homepage, null);
        ViewUtils.inject(this, view);
        initView(view);
        updateUserInfo(mUserInfo);
        setOnClickListener();
        return view;
    }

    private void setOnClickListener() {
        mRlPayLive.setOnClickListener(this);
        mRBookingLive.setOnClickListener(this);
        mRPublishLive.setOnClickListener(this);
    }

    private void initView(View view) {
        View nullView = view.findViewById(R.id.null_view);
        LayoutParams layoutParams = nullView.getLayoutParams();
        layoutParams.height = ProfileFragment.sStickyTopToViewPager;
        mGvOtherLive.setMode(PullToRefreshBase.Mode.BOTH);
        mGvOtherLive.setOnRefreshListener(this);
        mScLive.setScrollCallBack(mScrollListener);
        mPayAdapter = new SearchLiveAdapter(mPublishLives);
        mBookingAdapter = new SearchLiveAdapter(mbookingLives);
        mPublishAdapter = new SearchLiveAdapter(mPublishLives);
        otherAdapter = new SearchLiveAdapter(otherLives);
        mGvMyPayLive.setAdapter(mPayAdapter);
        mGvPublishLive.setAdapter(mPublishAdapter);
        mGvBookingLive.setAdapter(mBookingAdapter);
        mGvOtherLive.setAdapter(otherAdapter);
        mLlMyLive.setVisibility(View.GONE);
        mGvOtherLive.setVisibility(View.GONE);
    }

    public void setScrollCallBack(StickyScrollCallBack scrollListener) {
        mScrollListener = scrollListener;
        if (mScLive != null)
            mScLive.setScrollCallBack(mScrollListener);
    }


    public void updateUserInfo(UserInfo userInfo) {
        if (userInfo.isMine(getActivity())) {
            mGvOtherLive.setVisibility(View.GONE);
            mLlMyLive.setVisibility(View.VISIBLE);
        } else {
            mGvOtherLive.setVisibility(View.VISIBLE);
            mLlMyLive.setVisibility(View.GONE);
        }

        new GetMyLiveTask().executeLong();
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {

    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {

    }

    private class SearchLiveAdapter extends BaseAdapter {

        private ArrayList<LiveInfo> mLiveInfos;

        public SearchLiveAdapter(ArrayList<LiveInfo> liveInfos) {
            mLiveInfos = liveInfos;
        }

        @Override
        public int getCount() {
            if (mUserInfo.isMine(getActivity())) {
                return mLiveInfos.size() < 3 ? mLiveInfos.size() : 3;
            } else {
                return mLiveInfos.size();
            }
        }

        @Override
        public LiveInfo getItem(int position) {
            return mLiveInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LiveInfo liveInfo = getItem(position);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.grid_item_live, parent, false);
            }
            ImageView mIvCove = (ImageView) convertView.findViewById(R.id.iv_live_cove);
            ImageView mIvHot = (ImageView) convertView.findViewById(R.id.iv_hot_live);
            TextView mTvSchool = (TextView) convertView.findViewById(R.id.tv_school_name);
            TextView mTvUserName = (TextView) convertView.findViewById(R.id.tv_name);
            TextView mTvSpectatorNum = (TextView) convertView.findViewById(R.id.tv_spectator_num);
            TextView mTvContent = (TextView) convertView.findViewById(R.id.tv_live_content);
            TextView tvFlag = (TextView) convertView.findViewById(R.id.tv_live);

            if (!TextUtils.isEmpty(liveInfo.prevUrl)) {
                Picasso.with(getActivity()).load(liveInfo.prevUrl).into(mIvCove);
            }
            mTvSchool.setText(liveInfo.school);
            mTvContent.setText(liveInfo.title);
            convertView.setTag(liveInfo);
            if (liveInfo.status == LiveInfo.STATU_LIVING) {
                tvFlag.setText("直播");
            } else if (liveInfo.status == LiveInfo.STATU_REPLAY) {
                tvFlag.setText("录播");
            } else {
                tvFlag.setText("结束");
            }
            mTvUserName.setText(liveInfo.nick);
            convertView.setOnClickListener(MyLiveFragment.this);
            return convertView;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_live_info:
                break;
            /**更多发布的**/
            case R.id.rl_publish_live_title:
                Intent intentPub = new Intent(getActivity(), LivingHaveActivity.class);
                intentPub.putExtra(LIVE_OWN_TYPE, PUBLISH);
                startActivity(intentPub);
                break;
            /**更多购买的**/
            case R.id.rl_pay_live_title:
                Intent intentPay = new Intent(getActivity(), LivingHaveActivity.class);
                intentPay.putExtra(LIVE_OWN_TYPE, BUY);
                startActivity(intentPay);
                break;
            /**更多预定的**/
            case R.id.rl_booking_live_title:
                Intent intentBook = new Intent(getActivity(), LivingHaveActivity.class);
                intentBook.putExtra(LIVE_OWN_TYPE, BOOKING);
                startActivity(intentBook);
                break;
            default:
                break;
        }
    }


    public int getStickyHeight() {
        int scrollHeight = mScLive.getScrollY();
        if (scrollHeight > ProfileFragment.sStickyTopToTab) {
            return ProfileFragment.sStickyTopToTab;
        }
        return scrollHeight;
    }

    public void setStickyH(int stickyH) {
        if (Math.abs(stickyH - getStickyHeight()) < 10) {
            return;
        }
        // 判断高度，根据高度来决定是ScrollView向上滚动还是TopView向下滚动，并算出向下滚动的距离
        int scrollContentHeight = mScLive.getHeight() - ProfileFragment.sStickyBarHeight;
        int contentHeight = getContentHeight();
        if (scrollContentHeight <= contentHeight) {
            mScLive.scrollTo(0, stickyH);
        } else {
            int distance = scrollContentHeight - contentHeight - ProfileFragment.sStickyTopToTab + 10;
            if (distance < -ProfileFragment.sStickyTopToTab) {
                distance = -ProfileFragment.sStickyTopToTab;
            }
            if (distance > 0) {
                distance = 0;
            }
            mScrollListener.onScrollChanged(distance);
        }
        mScLive.scrollTo(0, stickyH);
    }

    public int getContentHeight() {
        if (mUserInfo.isMine(getActivity())) {
            return mLlMyLive.getHeight();
        } else {
            return mGvOtherLive.getHeight();
        }
    }

    private class GetMyLiveTask extends MsTask {

        public GetMyLiveTask() {
            super(getActivity(), MsRequest.LIST_MY_OWN_LIVE);
        }


        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mPayLives = JsonUtil.getArray(response.getJsonObject().optJSONArray("buy"), LiveInfo.TRANSFORMER);
                mPublishLives = JsonUtil.getArray(response.getJsonObject().optJSONArray("publish"), LiveInfo.TRANSFORMER);
                mbookingLives = JsonUtil.getArray(response.getJsonObject().optJSONArray("order"), LiveInfo.TRANSFORMER);
                if (mUserInfo.isMine(getActivity())) {

                    if (mPayLives.size() > 0) {
                        mPayAdapter.notifyDataSetChanged();
                    }
                    if (mPublishLives.size() > 0) {
                        mPublishAdapter.notifyDataSetChanged();
                    }
                    if (mbookingLives.size() > 0) {
                        mBookingAdapter.notifyDataSetChanged();
                    }
                }

            }

        }
    }
}
