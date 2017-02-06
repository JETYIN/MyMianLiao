package com.tjut.mianliao.tribe;

import android.app.LocalActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.MainActivity;
import com.tjut.mianliao.MianLiaoApp;
import com.tjut.mianliao.R;
import com.tjut.mianliao.SearchActivity;
import com.tjut.mianliao.common.Constant;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.CommonBanner;
import com.tjut.mianliao.component.ExpandableGridView;
import com.tjut.mianliao.component.MyScrollView;
import com.tjut.mianliao.component.MyScrollView.OnScrollListener;
import com.tjut.mianliao.component.TopicTagView;
import com.tjut.mianliao.component.TopicTagView.TagClickListener;
import com.tjut.mianliao.component.tab.Tab;
import com.tjut.mianliao.component.tab.TabController;
import com.tjut.mianliao.component.tab.TabController.TabListener;
import com.tjut.mianliao.contact.FollowUserManager;
import com.tjut.mianliao.contact.FollowUserManager.OnUserFollowListener;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.HotPostInfo;
import com.tjut.mianliao.data.RadMenInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.tribe.TribeInfo;
import com.tjut.mianliao.data.tribe.TribeTypeInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.TopicInfo;
import com.tjut.mianliao.forum.nova.ForumPostDetailActivity;
import com.tjut.mianliao.forum.nova.TribePostAdapter;
import com.tjut.mianliao.main.TabFragment;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class
        TribeTabFragement extends TabFragment implements OnClickListener,
        OnRefreshListener2<ListView>, TagClickListener, OnScrollListener,
        OnUserFollowListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String NAME = "TribeFragement";
    private static final String SP_CACHE_HOT_TRIBE = "sp_cache_hot_tribe";
    private static final String SP_CACHE_LATEST_TRIBE = "sp_cache_latest_tribe";
    private static final String SP_CACHE_HOT_TOPIC = "sp_cache_hot_topic";
    private static final String SP_CACHE_TRIBE_TYPE = "sp_cache_tribe_type";
    private static final String SP_CACHE_HOT_POST = "sp_cache_hot_post";
    private static final String SP_CACHE_RAD_MAN = "sp_cache_rad_man";
    private static final String SP_CACHE_INTEREST_CIRCLE = "sp_cache_interest_circle";

    private static final int TAB_LEFT = 1;
    private static final int TAB_RIGHT = 2;
    private static final int UPDATE_TRIBE = 10;
    private static final int TRIBES_TYPE_HOT = 98;
    private static final int TRIBES_TYPE_NEWS = 99;

    private static int _id = 1000;
    private static int text_id = 3200;
    private final static int REQUEST_CODE = 1000;
    private final static int RESULT_CODE = 0;

    @ViewInject(R.id.my_concern)
    private View mViewConcernTribe;
    @ViewInject(R.id.ptlv_tribe_my_concer)
    private PullToRefreshListView mLvConcernTribe;
    @ViewInject(R.id.ll_tribe_fragement)
    private LinearLayout mLlTribeFragement;
    @ViewInject(R.id.srl_all_tribe)
    private SwipeRefreshLayout mSrlAllTribe;

    private RelativeLayout mRlAllTribe;
    private CommonBanner mSwitchBanner;

    private AccountInfo mAccountInfo;
    private UserInfo mUserInfo;

    private ArrayList<TribeInfo> mTribes;
    private ArrayList<TribeInfo> mHotTribes;
    private ArrayList<TribeInfo> mNewestTribes;
    private ArrayList<TribeInfo> mConcernTribes;

    private ConcernTribeAdapter mConcernTribeAdapter;
    private TribePostAdapter mPostAdapter;

    private TabController mTabController;
    private int mTypeChoosed = TAB_LEFT;

    private LinearLayout mBuyLayout, mTopBuyLayout;
    private LayoutInflater inflate;

    private ImageView mImageView;
    private float mCurrentCheckedRadioLeft;//当前被选中的RadioButton距离左侧的距离
    private HorizontalScrollView mHorizontalScrollView;//上面的水平滚动控件
    private ArrayList<View> mViews;//用来存放下方滚动的layout(layout_1,layout_2,layout_3)

    LocalActivityManager manager = null;

    private LinearLayout mLlTypetTitle;
    private LinearLayout layout;

    private ArrayList<RadMenInfo> mRecommendCelebritys = new ArrayList<RadMenInfo>();
    private ArrayList<HotPostInfo> mHotPosts = new ArrayList<HotPostInfo>();
    private TribeHotpostAdapter mHotpostAdapter = new TribeHotpostAdapter();
    private LinearLayout mSvRecommend;
    private ExpandableGridView mGvHotPost;
    private LinearLayout mLlTribeList;
    private MyScrollView mScrollView;
    private LinearLayout mLlAllTribe;
    private View mIvAnimTop;
    private boolean isShowAnim = true;
    private TextView mTvMoreHotPost, mTvAllTribe, mTvTribeTypeMore, mTvChangeRadmen;
    private ArrayList<TribeTypeInfo> mTribeTypes;
    private FollowUserManager mFollowManager;

    private int mTribesType;
    private View mAllView;
    private boolean isCreate = true;

    private Timer mTimer;
    private TimerTask mTimerTask;
    private int mFreshTime;
    private long mRefreshTime;
    private int mCount;
    private Handler mFreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TRIBE:
                    MainActivity.showRefreshRed(1, true);
                    break;
                default:
                    break;
            }
        }
    };


    private SharedPreferences mPreferences;
    private boolean isFollowing;
    private int mCurrentRadMenId = -1;

    @Override
    public int getLayoutId() {
        return R.layout.activity_tribe_homepage;
    }

    @Override
    public int getNaviButtonId() {
        return R.id.nb_tribe;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTribes = new ArrayList<TribeInfo>();
        mHotTribes = new ArrayList<TribeInfo>();
        mNewestTribes = new ArrayList<TribeInfo>();
        mConcernTribes = new ArrayList<TribeInfo>();
        mConcernTribeAdapter = new ConcernTribeAdapter();
        mAccountInfo = AccountInfo.getInstance(getActivity());
        mUserInfo = mAccountInfo.getUserInfo();
        inflate = LayoutInflater.from(getActivity().getApplicationContext());
        manager = new LocalActivityManager(getActivity(), true);
        manager.dispatchCreate(savedInstanceState);
        mFreshTime = Constant.getFreshTimeDelay(getActivity());
        mFollowManager = FollowUserManager.getInstance(getActivity());
        mPreferences = DataHelper.getSpForData(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAllView = super.onCreateView(inflater, container, savedInstanceState);
        ViewUtils.inject(this, mAllView);

        mFollowManager.registerOnUserFollowListener(this);
        mIvAnimTop = mInflater.inflate(R.layout.list_item_all_tribe_anim, null);

        mSwitchBanner = (CommonBanner) mAllView.findViewById(R.id.vs_trieb_switcher);
        mLlTribeList = (LinearLayout) mAllView.findViewById(R.id.ll_tribe_list);
        mGvHotPost = (ExpandableGridView) mAllView.findViewById(R.id.gv_selection_post);
        mSvRecommend = (LinearLayout) mAllView.findViewById(R.id.ll_recommend_celebrity);
        mRlAllTribe = (RelativeLayout) mAllView.findViewById(R.id.rl_all_tribe);
        mLlAllTribe = (LinearLayout) mAllView.findViewById(R.id.ll_all_tribe);
        mTvMoreHotPost = (TextView) mAllView.findViewById(R.id.tv_more_hot_post);
        mTvAllTribe = (TextView) mAllView.findViewById(R.id.tv_all_tribe);
        mTvTribeTypeMore = (TextView) mAllView.findViewById(R.id.tv_type_tribe_more);
        mTvChangeRadmen = (TextView) mAllView.findViewById(R.id.tv_change_radmen);

        mTribeTypes = new ArrayList<TribeTypeInfo>();

        mGvHotPost.setAdapter(mHotpostAdapter);

        mSrlAllTribe.setOnRefreshListener(this);
        mTvMoreHotPost.setOnClickListener(this);
        mTvAllTribe.setOnClickListener(this);
        mTvTribeTypeMore.setOnClickListener(this);
        mTvChangeRadmen.setOnClickListener(this);

        mSrlAllTribe.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);

        mTribesType = TRIBES_TYPE_HOT;

        mBuyLayout = (LinearLayout) mAllView.findViewById(R.id.linearLayout01);
        mTopBuyLayout = (LinearLayout) mAllView.findViewById(R.id.linearLayout01_top);

        mScrollView = (MyScrollView) mAllView.findViewById(R.id.my_scroll_view);
        mScrollView.setScrollable(true);
        mScrollView.setOnScrollListener(this);

        mLlTribeFragement.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                onScroll(mScrollView.getScrollY());
            }
        });
        mTabController = new TabController();
        mTabController.setListener(new TabListener() {

            @Override
            public void onTabSelectionChanged(int index, boolean selected, Tab tab) {
                if (!selected) {
                    return;
                }
                switch (index) {
                    case 0:
                        mSrlAllTribe.setVisibility(View.VISIBLE);
                        mViewConcernTribe.setVisibility(View.GONE);
                        mTypeChoosed = TAB_LEFT;
                        break;
                    case 1:
                        mSrlAllTribe.setVisibility(View.GONE);
                        mViewConcernTribe.setVisibility(View.VISIBLE);
                        fetchInterestCircleTribes(true);
                        mTypeChoosed = TAB_RIGHT;
                        startCount();
                        break;
                    default:
                        break;
                }
            }
        });
        mTitleBar
                .showTabs(mTabController, getString(R.string.tribe_title_all), getString(R.string.tribe_title_collect));
        mTitleBar.showRightButton(R.drawable.icon_search, this);
        mTitleBar.showLeftButton(R.drawable.icon_personal, this);

        mPostAdapter = new TribePostAdapter(getActivity());
        mPostAdapter.setIsShowTribeIndetail(true);
        mPostAdapter.setTextClickble(true);
        mPostAdapter.setSpanClickble(true);
        mPostAdapter.setShowNoContent(true);

        mTabController.select(0);
        mLvConcernTribe.setMode(Mode.BOTH);
        mLvConcernTribe.setOnRefreshListener(this);
        mLvConcernTribe.setAdapter(mPostAdapter);
        loadDataFromSp();
        return mAllView;
    }

    private void loadDataFromSp() {
        String hotTopicJson = mPreferences.getString(SP_CACHE_HOT_TOPIC, "[]");
        String hotPostJson = mPreferences.getString(SP_CACHE_HOT_POST, "[]");
        String tribeTypeJson = mPreferences.getString(SP_CACHE_TRIBE_TYPE, "[]");
        String radManJson = mPreferences.getString(SP_CACHE_RAD_MAN, "[]");
        String hotTribeJson = mPreferences.getString(SP_CACHE_HOT_TRIBE, "[]");
        String interestCiarcle = mPreferences.getString(SP_CACHE_INTEREST_CIRCLE, "[]");
        try {
            JSONArray hotTopicJa = new JSONArray(hotTopicJson);
            JSONArray hotPostJa = new JSONArray(hotPostJson);
            JSONArray tribeTypeJa = new JSONArray(tribeTypeJson);
            JSONArray radManJa = new JSONArray(radManJson);
            JSONArray hotTribeJa = new JSONArray(hotTribeJson);
            JSONArray interestCircleJa = new JSONArray(interestCiarcle);
            ArrayList<HotPostInfo> posts = JsonUtil.getArray(hotPostJa, HotPostInfo.TRANSFORMER);
            mHotPosts.clear();
            mHotPosts.addAll(posts);
            if (!mHotPosts.isEmpty()) {
                mHotpostAdapter.notifyDataSetChanged();
            }
            ArrayList<TribeTypeInfo> types = JsonUtil.
                    getArray(tribeTypeJa, TribeTypeInfo.TRANSFORMER);
            if (types != null && types.size() > 0) {
                mTribeTypes.clear();
                mTribeTypes.addAll(types);
                initGroup(mAllView);
                initGroupTop(mAllView);
            }
            ArrayList<RadMenInfo> radMens = JsonUtil.getArray(radManJa, RadMenInfo.TRANSFORMER);
            mRecommendCelebritys.clear();
            mRecommendCelebritys.addAll(radMens);
            if (!mRecommendCelebritys.isEmpty()) {
                fillRecommendCelebrity();
            }
            ArrayList<TribeInfo> tribes = JsonUtil.getArray(hotTribeJa, TribeInfo.TRANSFORMER);
            mTribes.clear();
            mTribes.addAll(tribes);
            if (!mTribes.isEmpty()) {
                fillHotTribesInfo();
            }
            ArrayList<CfPost> interestPosts = JsonUtil.getArray(interestCircleJa, CfPost.TRANSFORMER);
            MainActivity.showRefreshRed(0, false);
            mPostAdapter.reset(interestPosts);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        UpdateView();
        new GetHotTribesTask(0).executeLong();
        if (mLlTypetTitle == null) {
            new GetTribeTypeTask().executeLong();
        }
        new GetRadMenTask().executeLong();
    }

    private void loadInfos() {
        String hotTribeStr = DataHelper.getSpForData(getActivity()).getString(SP_CACHE_HOT_TRIBE, "[]");
        String latestTribeStr = DataHelper.getSpForData(getActivity()).getString(SP_CACHE_LATEST_TRIBE, "[]");
        try {
            ArrayList<TribeInfo> hotTribes = JsonUtil.getArray(new JSONArray(hotTribeStr), TribeInfo.TRANSFORMER);
            ArrayList<TribeInfo> latestTribe = JsonUtil.getArray(new JSONArray(latestTribeStr), TribeInfo.TRANSFORMER);
            if (hotTribes != null && hotTribes.size() > 0) {
                mTribes.clear();
                mTribes.addAll(hotTribes);
                fillHotTribesInfo();
            }
            if (latestTribe != null && latestTribe.size() > 0) {
                mNewestTribes = latestTribe;
//                fillNewestTribeIfo();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        UpdateView();
        new GetTribeTypeTask().executeLong();
    }

    @Override
    public void onTabButtonClicked() {
        super.onTabButtonClicked();
        if (mTypeChoosed == TAB_LEFT) {
//            mPtrListTribe.setRefreshing(Mode.PULL_FROM_START);
        } else {
            mLvConcernTribe.setRefreshing(Mode.PULL_FROM_START);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetHotTribesTask(0).executeLong();
        //new GetTribeTypeTask().executeLong();
        //new GetRadMenTask().executeLong();

        UpdateView();
    }

    private void moveLinetoCenter() {
        LinearLayout rbFirst = (LinearLayout) mAllView.findViewById(_id + _id);
        if (rbFirst == null) {
            return;
        }
        AnimationSet firstAnimationSet = new AnimationSet(true);
        TranslateAnimation rbFirstAnim;
        rbFirstAnim = new TranslateAnimation(mImageView.getLeft() + mImageView.getWidth(), rbFirst.getLeft()
                + (rbFirst.getWidth() - rbFirst.getPaddingRight() - mImageView.getWidth()) / 2, 0f, 0f);
        firstAnimationSet.addAnimation(rbFirstAnim);
        firstAnimationSet.setFillBefore(true);
        firstAnimationSet.setFillAfter(true);
        firstAnimationSet.setDuration(10);

        mImageView.startAnimation(firstAnimationSet);
    }

    private void UpdateView() {
        mSwitchBanner.setParam(CommonBanner.Plate.TribeHomePage, 0);
        new GetHotPostTask().executeLong();
    }

    private void fillHotTribesInfo() {
        mLlTribeList.removeAllViews();
        if (mLlTribeList != null && mTribes.size() > 0) {
            for (int i = 0; i < mTribes.size(); i++) {
                mLlTribeList.addView(hotTribeGetView(mTribes.get(i)));
            }
            if (mTribes.size() >= 4) {
                mTvTribeTypeMore.setVisibility(View.VISIBLE);
            } else {
                mTvTribeTypeMore.setVisibility(View.GONE);
            }
        } else {
            mTvTribeTypeMore.setVisibility(View.GONE);
        }
    }

    private void fillRecommendCelebrity() {
        if (mSvRecommend != null && mRecommendCelebritys.size() > 0) {
            mSvRecommend.removeAllViews();
            for (RadMenInfo radMen : mRecommendCelebritys) {
                mSvRecommend.addView(getRadMenView(radMen));
            }
        }
    }

    private void saveSpData(String key, String value) {
        if (getActivity() != null) {
            DataHelper.getSpForData(getActivity()).edit().putString(key, value).commit();
        }
    }

    private View hotTribeGetView(TribeInfo tribe) {
        HotTribeViewHoder holder;
        View view = mInflater.inflate(R.layout.list_item_hot_tribe, null);
        holder = new HotTribeViewHoder();
        ViewUtils.inject(holder, view);
        view.setTag(holder);
        if (tribe.icon != null && !"".equals(tribe.icon)) {
            Picasso.with(getActivity()).load(tribe.icon).into(holder.mIvTribeLogo);
        }
        holder.mTvTribeName.setText(tribe.tribeName);
        holder.mTvTribeDesc.setText(tribe.tribeDesc);
        holder.mTvPeopleNum.setText(MianLiaoApp.getAppContext().getString(R.string.tribe_is_followed_count_people,
                Utils.getFormatNum(tribe.followCount)));
        holder.mTvPostNum.setText(MianLiaoApp.getAppContext().getString(R.string.tribe_is_followed_count_post,
                Utils.getFormatNum(tribe.threadCount)));
        holder.mTribe = tribe;
        view.setOnClickListener(mHotTribeOnclick);
        return view;
    }

    private android.view.View.OnClickListener mHotTribeOnclick = new android.view.View.OnClickListener() {

        @Override
        public void onClick(View v) {
            HotTribeViewHoder holder = (HotTribeViewHoder) v.getTag();
            TribeInfo tribe = holder.mTribe;
            Intent intent = new Intent(getActivity(), TribeDetailActivity.class);
            intent.putExtra(TribeInfo.INTENT_EXTRA_INFO, tribe);
            startActivity(intent);
        }
    };

    @Override
    public void onRefresh() {
        UpdateView();
        new GetTribeTypeTask().executeLong();
        new GetHotTribesTask(0).executeLong();
    }

    private class ConcernTribeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mConcernTribes.size();
        }

        @Override
        public TribeInfo getItem(int position) {
            return mConcernTribes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ConcernTribeViewHoder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_tribe_concern, parent, false);
                holder = new ConcernTribeViewHoder();
                ViewUtils.inject(holder, convertView);
                convertView.setTag(holder);
            } else {
                holder = (ConcernTribeViewHoder) convertView.getTag();
            }
            TribeInfo tribe = getItem(position);
            if (tribe.icon != null && !tribe.icon.equals("")) {
                Picasso.with(getActivity()).load(tribe.icon).into(holder.mIvTribeLogo);
            }
            holder.mTvTribeName.setText(tribe.tribeName);
            holder.mTvPeopleNum.setText(MianLiaoApp.getAppContext().getString(R.string.tribe_is_followed_count_people,
                    Utils.getFormatNum(tribe.followCount)));
            holder.mTvPostNum.setText(MianLiaoApp.getAppContext().getString(R.string.tribe_is_followed_count_post,
                    Utils.getFormatNum(tribe.threadCount)));
            holder.mTvDayPostCount.setText(getString(R.string.tribe_day_post_count, tribe.dayPostCount));
            holder.mTribe = tribe;
            convertView.setOnClickListener(mConcernTribeClick);
            return convertView;
        }
    }

    private OnClickListener mConcernTribeClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            ConcernTribeViewHoder holder = (ConcernTribeViewHoder) v.getTag();
            TribeInfo tribe = holder.mTribe;
            Intent intent = new Intent(getActivity(), TribeDetailActivity.class);
            intent.putExtra(TribeInfo.INTENT_EXTRA_INFO, tribe);
            startActivity(intent);
        }
    };

    private class HotTribeViewHoder {
        @ViewInject(R.id.piv_tribe_logo)
        ImageView mIvTribeLogo;
        @ViewInject(R.id.tv_tribe_name)
        TextView mTvTribeName;
        @ViewInject(R.id.tv_tribe_describe)
        TextView mTvTribeDesc;
        @ViewInject(R.id.tv_post_num)
        TextView mTvPostNum;
        @ViewInject(R.id.tv_people_num)
        TextView mTvPeopleNum;
        TribeInfo mTribe;
    }

    private class ConcernTribeViewHoder {
        @ViewInject(R.id.piv_tribe_logo)
        ImageView mIvTribeLogo;
        @ViewInject(R.id.tv_tribe_name)
        TextView mTvTribeName;
        @ViewInject(R.id.tv_tribe_describe)
        TextView mTvTribeDesc;
        @ViewInject(R.id.tv_post_num)
        TextView mTvPostNum;
        @ViewInject(R.id.tv_people_num)
        TextView mTvPeopleNum;
        @ViewInject(R.id.tv_num)
        TextView mTvDayPostCount;
        TribeInfo mTribe;
    }

    protected void setTitle() {
        mTitleBar.setTitle(R.string.cf_post_campus);
        mTitleBar.showRightButton(R.drawable.icon_more, this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.btn_right:
                startActivity(SearchActivity.class);
                break;
            case R.id.btn_left:
                MainActivity.showDrawerLayout();
                break;
            case R.id.tv_tribe_more:
                intent.setClass(getActivity(), TribesListActivity.class);
                intent.putExtra(TribesListActivity.MORE_TRIBE_TYPE, 1);
                startActivity(intent);
                break;
            case R.id.tv_user_name:
                UserInfo userInfo = (UserInfo) v.getTag();
                intent.setClass(getActivity(), NewProfileActivity.class);
                intent.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
                startActivity(intent);
                break;
            case R.id.ll_hot_post_item:
                HotPostInfo post = (HotPostInfo) v.getTag();
                intent.setClass(getActivity(), ForumPostDetailActivity.class);
                intent.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA_ID, post.threadId);
                startActivity(intent);
                break;
            case R.id.ll_rad_men:
                RadMenInfo radMen = (RadMenInfo) v.getTag();
                intent.setClass(getActivity(), NewProfileActivity.class);
                UserInfo mUserInfo = new UserInfo();
                mUserInfo.userId = radMen.uid;
                intent.putExtra(UserInfo.INTENT_EXTRA_INFO, mUserInfo);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.tv_attention_count:
                RadMenInfo followRadMen = (RadMenInfo) v.getTag();

                if (followRadMen.isFollow) {
                    mFollowManager.cancleFollow(followRadMen.uid);
                } else {
                    mFollowManager.follow(followRadMen.uid);

                }
                mCurrentRadMenId = followRadMen.uid;
                break;
            case R.id.tv_more_hot_post:
                intent.setClass(getActivity(), HotEssentialActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_all_tribe:
                intent.setClass(getActivity(), TribeClassifyDetailActivity.class);
                intent.putExtra(TribeClassifyDetailActivity.EXT_TRIBE_TYPE, TribeClassifyDetailActivity.EXT_TYPE_ALL_TRIBE);
                startActivity(intent);
                break;
            case R.id.tv_type_tribe_more:
                mTvTribeTypeMore.setVisibility(View.GONE);
                mTribes.clear();
                switch (mTribesType) {
                    case 98:
                        new GetHotTribesTask(1).executeLong();
                        break;
                    case 99:
                        new GetNewTribesTask(1).executeLong();
                        break;
                    default:
                        new GetTypeDetailTask(mTribesType, 1).executeLong();
                        break;
                }
                break;
            case R.id.tv_change_radmen:
                new GetRadMenTask().executeLong();
                break;
            default:
                break;
        }
    }

    private class GetHotTribesTask extends MsTask {

        private int type;

        public GetHotTribesTask(int type) {
            super(getActivity(), MsRequest.TRIBE_HOT_LIST);
            this.type = type;
        }

        @Override
        protected String buildParams() {
            if (type == 0) {
                return new StringBuilder("preview=").append(1).toString();
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(MsResponse response) {
//            mPtrListTribe.onRefreshComplete();
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                if (type == 0) {
                    saveSpData(SP_CACHE_HOT_TRIBE, response.getJsonArray().toString());
                }
                ArrayList<TribeInfo> tribes = JsonUtil.getArray(ja, TribeInfo.TRANSFORMER);
                mTribes.clear();
                mTribes.addAll(tribes);
                fillHotTribesInfo();
            }
        }
    }

    private class GetNewTribesTask extends MsTask {

        private int type;

        public GetNewTribesTask(int type) {
            super(getActivity(), MsRequest.TRIBE_LATEST_TRIBE_LIST);
            this.type = type;
        }

        @Override
        protected String buildParams() {
            if (type == 0) {
                return new StringBuilder("preview=").append(1).toString();
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                ArrayList<TribeInfo> tribes = JsonUtil.getArray(ja, TribeInfo.TRANSFORMER);
                mTribes.clear();
                mTribes.addAll(tribes);
                fillHotTribesInfo();
            }
        }
    }

    private class GetRadMenTask extends MsTask {
        public GetRadMenTask() {
            super(getActivity(), MsRequest.USER_RED_MEN);
        }

//        @Override
//        protected String buildParams() {
//            return new StringBuilder("offset=").append(mRadmenOffset < 0 ? 0 : mRadmenOffset).toString();
//        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mLlAllTribe.removeView(mIvAnimTop);
            isShowAnim = true;
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                saveSpData(SP_CACHE_RAD_MAN, response.getJsonArray().toString());
                ArrayList<RadMenInfo> radMens = JsonUtil.getArray(ja, RadMenInfo.TRANSFORMER);
                mRecommendCelebritys.clear();
                mRecommendCelebritys.addAll(radMens);
                fillRecommendCelebrity();
            }
        }
    }

    private class GetHotPostTask extends MsTask {

        public GetHotPostTask() {
            super(getActivity(), MsRequest.THREAD_HOT_POST);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("preview=").append(1).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                saveSpData(SP_CACHE_HOT_POST, response.getJsonArray().toString());
                JSONArray ja = response.getJsonArray();
                ArrayList<HotPostInfo> posts = JsonUtil.getArray(ja, HotPostInfo.TRANSFORMER);
                mHotPosts.clear();
                mHotPosts.addAll(posts);
                mHotpostAdapter.notifyDataSetChanged();
            }
        }

    }

    private class GetConcernTribesTask extends MsTask {
        private int userId;
        private int mOffset;

        public GetConcernTribesTask(int uid, int offset) {
            super(getActivity(), MsRequest.TRIBE_CONCERN_TRIBE);
            userId = uid;
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("user_id=").append(userId).append("&offset=").append(mOffset).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mLvConcernTribe.onRefreshComplete();
            if (response.isSuccessful()) {
                if (mOffset <= 0) {
                    mConcernTribes.clear();
                }
                ArrayList<TribeInfo> tribes = JsonUtil.getArray(response.getJsonArray(), TribeInfo.TRANSFORMER);
                mConcernTribes.addAll(tribes);
                mConcernTribeAdapter.notifyDataSetChanged();
            }
        }

    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        if (mTypeChoosed == TAB_RIGHT) {
            fetchInterestCircleTribes(true);
        }
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        if (mTypeChoosed == TAB_RIGHT) {
            fetchInterestCircleTribes(false);
        }
    }

    @Override
    public void onScroll(int scrollY) {
        int mBuyLayout2ParentTop = Math.max(scrollY, mBuyLayout.getTop());
        mTopBuyLayout.layout(0, mBuyLayout2ParentTop, mTopBuyLayout.getWidth(),
                mBuyLayout2ParentTop + mTopBuyLayout.getHeight());
//        if (scrollY < -200 && isShowAnim && mIvAnimTop != null) {
//            mLlAllTribe.removeView(mIvAnimTop);
//            mLlAllTribe.addView(mIvAnimTop, 0);
//            isShowAnim = false;
//            new GetTribeTypeTask().executeLong();
//            UpdateView();
//        }
    }

    private void initGroup(final View view) {
        layout = (LinearLayout) view.findViewById(R.id.lay);

        mImageView = (ImageView) view.findViewById(R.id.img1);
        mHorizontalScrollView = (HorizontalScrollView) view.findViewById(R.id.horizontalScrollView);
        mLlTypetTitle = new LinearLayout(getActivity());
        mLlTypetTitle.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mLlTypetTitle.setOrientation(LinearLayout.HORIZONTAL);
        mLlTypetTitle.removeAllViews();
        layout.removeAllViews();
        layout.addView(mLlTypetTitle);
        for (int i = 0; i < mTribeTypes.size(); i++) {
            TribeTypeInfo mTribeType = mTribeTypes.get(i);

            LinearLayout mLayout = new LinearLayout(getActivity());
            mLayout.setBackgroundColor(getResources().getColor(R.color.white));
            LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, Gravity.CENTER);
            mLayout.setLayoutParams(l);
            mLayout.setOrientation(LinearLayout.VERTICAL);
            mLayout.setGravity(Gravity.CENTER);
            mLayout.setPadding(0, 0, getResources().getDimensionPixelSize(R.dimen.tribe_type_item_magin), 0);
            mLayout.setId(_id + i);
            ImageView mTypeLogo = new ImageView(getActivity());
            LinearLayout.LayoutParams ivl = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.tribe_type_icon_width),
                    getResources().getDimensionPixelSize(R.dimen.tribe_type_icon_width));
            LinearLayout.LayoutParams tvl = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
            mTypeLogo.setLayoutParams(ivl);
            if (mTribeType.icon != null && !"".equals(mTribeType.icon)) {
                Picasso.with(getActivity()).load(mTribeType.icon).into(mTypeLogo);
            }
            TextView mTvTypeName = new TextView(getActivity());
            mTvTypeName.setLayoutParams(tvl);
            mTvTypeName.setText(mTribeType.name);
            mTvTypeName.setId(text_id + i);
            mTvTypeName.setVisibility(View.VISIBLE);
            mLayout.setTag(mTribeType);
            if (i == 0) {
                mTvTypeName.setTextColor(0XFF78A8E4);
            } else {
                mTvTypeName.setTextColor(0XFF515151);
            }
            mTvTypeName.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimensionPixelSize(R.dimen.tribe_type_name_size));
            mTvTypeName.setPadding(0, getResources().getDimensionPixelSize(R.dimen.tribe_type_padding), 0, 0);

            if (i == 0) {
                mImageView.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(
                        R.dimen.tribe_type_line_width), 4));
            }
            mLayout.addView(mTypeLogo);
            mLayout.addView(mTvTypeName);
            mLlTypetTitle.addView(mLayout);
        }
        mLlTypetTitle.invalidate();
    }

    private void initGroupTop(final View view) {
        layout = (LinearLayout) view.findViewById(R.id.lay_top);
        mImageView = (ImageView) view.findViewById(R.id.img1_top);
        mHorizontalScrollView = (HorizontalScrollView) view.findViewById(R.id.horizontalScrollView_top);

        /*layout = (LinearLayout) view.findViewById(R.id.lay);
        mImageView = (ImageView)view.findViewById(R.id.img1);
        mHorizontalScrollView = (HorizontalScrollView)view.findViewById(R.id.horizontalScrollView);
        */
        mLlTypetTitle = new LinearLayout(getActivity());
        mLlTypetTitle.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mLlTypetTitle.setOrientation(LinearLayout.HORIZONTAL);

        layout.addView(mLlTypetTitle);
        for (int i = 0; i < mTribeTypes.size(); i++) {
            TribeTypeInfo mTribeType = mTribeTypes.get(i);

            LinearLayout mLayout = new LinearLayout(getActivity());
            mLayout.setBackgroundColor(getResources().getColor(R.color.white));
            LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, Gravity.CENTER);
            mLayout.setLayoutParams(l);
            mLayout.setOrientation(LinearLayout.VERTICAL);
            mLayout.setGravity(Gravity.CENTER);
            mLayout.setPadding(0, 0, getResources().getDimensionPixelSize(R.dimen.tribe_type_item_magin), 0);
            mLayout.setId(_id + _id + i);
            ImageView mTypeLogo = new ImageView(getActivity());
            LinearLayout.LayoutParams ivl = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.tribe_type_icon_width),
                    getResources().getDimensionPixelSize(R.dimen.tribe_type_icon_width));
            LinearLayout.LayoutParams tvl = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT, Gravity.CENTER);
            mTypeLogo.setLayoutParams(ivl);
            if (mTribeType.isHotTribe) {
                mTypeLogo.setImageResource(R.drawable.hot_hover);
            } else if (mTribeType.isNewTribe) {
                mTypeLogo.setImageResource(R.drawable.new_hover);
            } else if (mTribeType.icon != null && !"".equals(mTribeType.icon)) {
                Picasso.with(getActivity()).load(mTribeType.icon).into(mTypeLogo);
            }
            TextView mTvTypeName = new TextView(getActivity());
            mTvTypeName.setLayoutParams(tvl);
            mTvTypeName.setText(mTribeType.name);
            mTvTypeName.setId(text_id + text_id + i);
            mTvTypeName.setVisibility(View.VISIBLE);
            mLayout.setTag(mTribeType);
            if (i == 0) {
                mTvTypeName.setTextColor(0XFF78A8E4);
            } else {
                mTvTypeName.setTextColor(0XFF515151);
            }
            mTvTypeName.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimensionPixelSize(R.dimen.tribe_type_name_size));
            mTvTypeName.setPadding(0, getResources().getDimensionPixelSize(R.dimen.tribe_type_padding), 0, 0);
            mLayout.setOnClickListener(mTypeClickListener);

            if (i == 0) {
                mImageView.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(
                        R.dimen.tribe_type_line_width), 4));
            }
            mLayout.addView(mTypeLogo);
            mLayout.addView(mTvTypeName);
            mLlTypetTitle.addView(mLayout);
        }
        mAllView.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveLinetoCenter();
            }
        }, 500);
    }

    OnClickListener mTypeClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            //Map<String, Object> map = (Map<String, Object>) group.getChildAt(checkedId).getTag();
            mTvTribeTypeMore.setVisibility(View.VISIBLE);
            int layoutId = v.getId();
            //根据ID获取RadioButton的实例 
            LinearLayout rb = (LinearLayout) mAllView.findViewById(layoutId);
            TribeTypeInfo mTribeType = (TribeTypeInfo) rb.getTag();

            mTribesType = mTribeType.type;

            AnimationSet animationSet = new AnimationSet(true);
            TranslateAnimation translateAnimation;
            translateAnimation = new TranslateAnimation(mCurrentCheckedRadioLeft, rb.getLeft() + (rb.getWidth() - rb.getPaddingRight()) / 2 - mImageView.getWidth() / 2, 0f, 0f);
            animationSet.addAnimation(translateAnimation);
            animationSet.setFillBefore(true);
            animationSet.setFillAfter(true);
            animationSet.setDuration(300);

            mImageView.startAnimation(animationSet);//开始上面蓝色横条图片的动画切换
            mCurrentCheckedRadioLeft = rb.getLeft();//更新当前蓝色横条距离左边的距离
            mHorizontalScrollView.smoothScrollTo((int) mCurrentCheckedRadioLeft - (int) getResources().getDimension(R.dimen.rdo2), 0);

            mImageView.setLayoutParams(new LinearLayout.LayoutParams(getResources().
                    getDimensionPixelSize(R.dimen.tribe_type_line_width), 4));

            for (int i = 0; i < mTribeTypes.size(); i++) {
                TextView mTvChecked = (TextView) mAllView.findViewById(text_id + text_id + i);
                if (_id + _id + i == layoutId) {
                    mTvChecked.setTextColor(0XFF78A8E4);
                } else {
                    mTvChecked.setTextColor(0XFF515151);
                }
            }
            if (mTribeType.isHotTribe) {
                new GetHotTribesTask(0).executeLong();
                mTribesType = TRIBES_TYPE_HOT;
            } else if (mTribeType.isNewTribe) {
                mTribesType = TRIBES_TYPE_NEWS;
                new GetNewTribesTask(0).executeLong();
            } else {
                new GetTypeDetailTask(mTribeType.type, 0).executeLong();
            }
        }
    };

    private class TribeHotpostAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mHotPosts.size();
        }

        @Override
        public HotPostInfo getItem(int position) {
            return mHotPosts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = mInflater.inflate(R.layout.list_item_hot_post, parent, false);
            ImageView mIvPostPic = (ImageView) view.findViewById(R.id.iv_post_pic);
            AvatarView mAvatar = (AvatarView) view.findViewById(R.id.av_avatar);
            TextView mTvPostContent = (TextView) view.findViewById(R.id.tv_post_content);
            TextView mTvName = (TextView) view.findViewById(R.id.tv_name);
            TextView mTvSchool = (TextView) view.findViewById(R.id.tv_school);
            TextView mPraiseNum = (TextView) view.findViewById(R.id.tv_praise_num);
            HotPostInfo post = getItem(position);

            if (post.cover != null && !"".equals(post.cover)) {
                Picasso.with(getActivity()).load(post.cover).into(mIvPostPic);
            }
            if (post.userAvatar != null && !"".equals(post.userAvatar)) {
                Picasso.with(getActivity()).load(post.userAvatar).into(mAvatar);
            }
            mTvPostContent.setText(post.title);
            mTvName.setText(post.userNick);
            mTvSchool.setText(post.forum);
            mPraiseNum.setText(post.upCount + "");

            view.setTag(post);
            view.setOnClickListener(TribeTabFragement.this);
            return view;
        }

    }

    public View getRadMenView(RadMenInfo radMen) {
        View view = mInflater.inflate(R.layout.list_item_recommend_celebrity, null);
        AvatarView mAvatar = (AvatarView) view.findViewById(R.id.av_avatar);
        TextView mTvName = (TextView) view.findViewById(R.id.tv_name);
        TextView mTvSchool = (TextView) view.findViewById(R.id.tv_school);
        TextView mTvFollow = (TextView) view.findViewById(R.id.tv_attention_count);
        if (radMen.avatar != null && !"".equals(radMen.avatar)) {
            Picasso.with(getActivity()).load(radMen.avatar).placeholder(R.drawable.pic_face_01).into(mAvatar);
        }
        mTvName.setText(radMen.nickName);
        mTvSchool.setText(radMen.school);
        if (radMen.isFollow) {
            mTvFollow.setText(R.string.tribe_is_followed);
            mTvFollow.setTextColor(0xff848484);
            mTvFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.channel_icon_ok, 0, 0, 0);
            mTvFollow.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.tribe_rad_men_follow_padding));
            mTvFollow.setBackgroundResource(R.drawable.bg_tv_green_circle);
        } else {
            mTvFollow.setText(R.string.tribe_collected_add);
            mTvFollow.setBackgroundResource(R.drawable.bg_tv_rad_circle);
            mTvFollow.setTextColor(0XFFFF9393);
            mTvFollow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        view.setOnClickListener(this);
        mTvFollow.setOnClickListener(this);
        view.setTag(radMen);
        mTvFollow.setTag(radMen);
        return view;
    }

    /**
     * isFollow的返回
     **/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_CODE) {
            boolean flag = data.getExtras().getBoolean("isFollow");


        }

    }

    @Override
    public void onTagClick(TopicInfo topic) {
        Intent intent = new Intent(getActivity(), ForumPostDetailActivity.class);
        intent.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA_ID, topic.threadId);
        intent.putExtra(ForumPostDetailActivity.EXTRL_TOPIC_NAME, topic.name);
        startActivity(intent);
    }

    private class GetTribeTypeTask extends MsTask {

        public GetTribeTypeTask() {
            super(getActivity(), MsRequest.TRIBE_TYPES_LIST);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mSrlAllTribe.setRefreshing(false);
            if (response.isSuccessful()) {
                saveSpData(SP_CACHE_TRIBE_TYPE, response.getJsonArray().toString());
                ArrayList<TribeTypeInfo> types = JsonUtil.
                        getArray(response.getJsonArray(), TribeTypeInfo.TRANSFORMER);
                if (types != null && types.size() > 0) {
                    mTribeTypes.clear();
                    mTribeTypes.addAll(types);
                    initGroup(mAllView);
                    initGroupTop(mAllView);
                    for (TribeTypeInfo typeInfo : types) {
                        if (typeInfo.isHotTribe) {
                            mHotTribes = typeInfo.tribes;
                        } else if (typeInfo.isNewTribe) {
                            mNewestTribes = typeInfo.tribes;
                        }
                    }
                }
            }
        }

    }

    private class GetTypeDetailTask extends MsTask {

        private int tribeType;
        private int type;

        public GetTypeDetailTask(int tribeType, int type) {
            super(getActivity(), MsRequest.TRIBE_LIST_BY_TYPE);
            this.tribeType = tribeType;
            this.type = type;
        }

        @Override
        protected String buildParams() {
            if (type == 0) {
                return new StringBuilder("tribe_type=").append(tribeType)
                        .append("&preview=").append(1).toString();
            } else {
                return new StringBuilder("tribe_type=").append(tribeType).toString();
            }
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mTribes.clear();
                ArrayList<TribeInfo> tribes = JsonUtil.getArray(
                        response.getJsonArray(), TribeInfo.TRANSFORMER);
                mTribes.addAll(tribes);
                fillHotTribesInfo();
            }
        }

    }

    private class GetInterestCircleTask extends MsTask {

        private int offset;

        public GetInterestCircleTask(int offset) {
            super(getActivity(), MsRequest.TRIBE_INTEREST_CIRCLE);
            this.offset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(offset).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mLvConcernTribe.onRefreshComplete();
            if (response.isSuccessful()) {
                ArrayList<CfPost> posts = JsonUtil.getArray(
                        response.getJsonArray(), CfPost.TRANSFORMER);
                if (offset <= 0) {
                    saveSpData(SP_CACHE_INTEREST_CIRCLE, response.getJsonArray().toString());
                    MainActivity.showRefreshRed(0, false);
                    mPostAdapter.reset(posts);
                    mRefreshTime = getCampusTime();
                } else {
                    mPostAdapter.addAll(posts);
                }
            }
        }

    }

    private void fetchInterestCircleTribes(boolean refresh) {
        int size = mPostAdapter.getCount();
        int offset = refresh ? 0 : size;
        new GetInterestCircleTask(offset).executeLong();
    }

    private class GetRefreshPostCount extends MsTask {

        public GetRefreshPostCount() {
            super(getActivity(), MsRequest.CF_REFRESH_COUNT);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("time=").append(mRefreshTime).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONObject jsonObj = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);
                mCount = jsonObj.optInt("count");
                if (mCount > 0) {
                    mFreshHandler.sendEmptyMessage(UPDATE_TRIBE);
                }
            }
        }

    }

    private void startCount() {
        mTimer = new Timer();
        mTimerTask = new TimerTask() {

            @Override
            public void run() {
                refreshCount();
            }

            private void refreshCount() {
                new GetRefreshPostCount().executeLong();
            }
        };

        mTimer.schedule(mTimerTask, mFreshTime, mFreshTime);
    }

    private long getCampusTime() {
        return System.currentTimeMillis() / 1000;
    }

    @Override
    public void onFollowSuccess() {
        changeRadmenList();
    }

    @Override
    public void onFollowFail() {
        isFollowing = false;
    }

    @Override
    public void onCancleFollowSuccess() {
        isFollowing = false;
        changeRadmenList();
    }

    @Override
    public void onCancleFollowFail() {
        isFollowing = false;
    }

    @Override
    public void onGetFollowListSuccess(MsResponse response, int offset) {
    }

    @Override
    public void onGetFollowListFail() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFollowManager.unregisterOnUserFollowListener(this);
    }

    private void changeRadmenList() {
        for (int i = 0; i < mRecommendCelebritys.size(); i++) {
            if (mRecommendCelebritys.get(i).uid == mCurrentRadMenId) {
                mRecommendCelebritys.get(i).isFollow = !mRecommendCelebritys.get(i).isFollow;
            }
        }
        mCurrentRadMenId = -1;
        fillRecommendCelebrity();
    }

}
