package com.tjut.mianliao.tribe;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.duanqu.qupai.utils.FourCC;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.MainActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.SearchActivity;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.CommonBanner;
import com.tjut.mianliao.component.ExpandableGridView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.tab.Tab;
import com.tjut.mianliao.component.tab.TabController;
import com.tjut.mianliao.component.tab.TabController.TabListener;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.cache.CacheChannelInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.tribe.TribeInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.nova.ChannelInfoManager;
import com.tjut.mianliao.forum.nova.ChannelInfoManager.ChannelInfoListener;
import com.tjut.mianliao.forum.nova.ChannelInfoNightAdapter;
import com.tjut.mianliao.forum.nova.ForumPostAdapter;
import com.tjut.mianliao.forum.nova.ForumPostDetailActivity;
import com.tjut.mianliao.forum.nova.NewChannelInfoAdapter;
import com.tjut.mianliao.main.TabFragment;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.theme.ThemeLineView;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class TribeTabFragement extends TabFragment implements OnClickListener, ChannelInfoListener,
        OnRefreshListener2<ListView> {

    private static final String NAME = "TribeFragement";
    private static final String SP_CACHE_LATEST_POSTS = "sp_cache_latest_posts";
    private static final String SP_CACHE_HOT_TRIBE = "sp_cache_hot_tribe";
    private static final String SP_CACHE_LATEST_TRIBE = "sp_cache_latest_tribe";
    
    private static final int TAB_LEFT = 1;
    private static final int TAB_RIGHT = 2;
    
    @ViewInject(R.id.ll_newest_post)
    private LinearLayout mLlNewestPost;
    @ViewInject(R.id.ll_hot_tribe)
    private LinearLayout mLlHotTribe;
    @ViewInject(R.id.gv_newest_tribe)
    private ExpandableGridView mGvNewestTribe;
    @ViewInject(R.id.vs_trieb_switcher)
    private CommonBanner mTribeBanner;
    @ViewInject(R.id.ptrlv_tribe_tab)
    private PullToRefreshListView mPtrListTribe;
    @ViewInject(R.id.channel_home)
    private View mViewChannel;
    @ViewInject(R.id.my_concern)
    private View mViewConcernTribe;
    @ViewInject(R.id.lv_channel_suggest)
    private ListView mLvSuggest;
    @ViewInject(R.id.lv_channel_latest)
    private ListView mLvLatest;
    @ViewInject(R.id.ll_channel_suggest)
    private View mViewChannelSuggest;
    @ViewInject(R.id.ptlv_tribe_my_concer)
    private PullToRefreshListView mLvConcernTribe;
    @ViewInject(R.id.ll_tribe_fragement)
    private LinearLayout mLlTribeFragement;
    
    private CommonBanner mSwitchBanner;

    private ChannelInfoManager mChannelInfoManager;
    private AccountInfo mAccountInfo;
    private UserInfo mUserInfo;

    private ArrayList<TribeInfo> mHotTribes;
    private ArrayList<TribeInfo> mNewestTribes;
    private ArrayList<TribeInfo> mConcernTribes;
    private ArrayList<CfPost> mNewestPosts;

    private NewestTribeAdapter mNewestTribeAdapter;

    private ChannelInfoNightAdapter mNightAdapter;
    private NewChannelInfoAdapter mNewChannelAdapter;
    private ConcernTribeAdapter mConcernTribeAdapter;

    private TabController mTabController;
    private boolean mIsNightMode;
    private int mTypeChoosed = TAB_LEFT;
    
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
        mIsNightMode = Settings.getInstance(getActivity()).isNightMode();
        mChannelInfoManager = ChannelInfoManager.getInstance(getActivity());
        mChannelInfoManager.registerChannelInfoListener(this);
        mNightAdapter = new ChannelInfoNightAdapter(getActivity());
        mNewChannelAdapter = new NewChannelInfoAdapter(getActivity());
        mHotTribes = new ArrayList<TribeInfo>();
        mNewestTribes = new ArrayList<TribeInfo>();
        mConcernTribes = new ArrayList<TribeInfo>();
        mNewestPosts = new ArrayList<CfPost>();
        mNewestTribeAdapter = new NewestTribeAdapter();
        mConcernTribeAdapter = new ConcernTribeAdapter();
        mAccountInfo = AccountInfo.getInstance(getActivity());
        mUserInfo = mAccountInfo.getUserInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ViewUtils.inject(this, view);
        
        mPtrListTribe.setMode(Mode.PULL_FROM_START);
        mPtrListTribe.setOnRefreshListener(this);
        mPtrListTribe.setAdapter(new ForumPostAdapter(getActivity()).setShowNoContent(false));
        ListView listView = mPtrListTribe.getRefreshableView();
        View mHeaderView = mInflater.inflate(R.layout.activity_all_tribe, listView, false);
        listView.addHeaderView(mHeaderView);
        mLlHotTribe = (LinearLayout) mHeaderView.findViewById(R.id.ll_hot_tribe);
        mGvNewestTribe = (ExpandableGridView) mHeaderView.findViewById(R.id.gv_newest_tribe);
        mLlNewestPost = (LinearLayout) mHeaderView.findViewById(R.id.ll_newest_post);
        mHeaderView.findViewById(R.id.tv_newest_post_more).setOnClickListener(this);
        mHeaderView.findViewById(R.id.tv_newest_tribe_more).setOnClickListener(this);
        mHeaderView.findViewById(R.id.tv_hot_tribe_more).setOnClickListener(this);
        mHeaderView.findViewById(R.id.ll_game_zone).setOnClickListener(this);
        mHeaderView.findViewById(R.id.ll_fresh_topic).setOnClickListener(this);
        mHeaderView.findViewById(R.id.ll_all_items).setOnClickListener(this);
        mHeaderView.findViewById(R.id.ll_hot_post).setOnClickListener(this);
        mSwitchBanner = (CommonBanner) mHeaderView.findViewById(R.id.vs_trieb_switcher);
        
        mLvSuggest.setAdapter(mNightAdapter);
        mLvLatest.setAdapter(mNewChannelAdapter);
        mTabController = new TabController();
        checkDayNightUI();
        mTabController.setListener(new TabListener() {

            @Override
            public void onTabSelectionChanged(int index, boolean selected, Tab tab) {
                if (!selected) {
                    return;
                }
                switch (index) {
                    case 0:
                        if (mIsNightMode) {
                            mViewChannelSuggest.setVisibility(View.VISIBLE);
                            mLvLatest.setVisibility(View.GONE);
                            fetchChannel(false);
                        } else {
                            mPtrListTribe.setVisibility(View.VISIBLE);
                            mViewConcernTribe.setVisibility(View.GONE);
                        }
                        mTypeChoosed = TAB_LEFT;
                        break;
                    case 1:
                        if (mIsNightMode) {
                            mViewChannelSuggest.setVisibility(View.GONE);
                            mLvLatest.setVisibility(View.VISIBLE);
                            fetchChannel(true);
                        } else {
                            mPtrListTribe.setVisibility(View.GONE);
                            mViewConcernTribe.setVisibility(View.VISIBLE);
                            fetchConcernTribes(true);
                        }
                        mTypeChoosed = TAB_RIGHT;
                        break;
                    default:
                        break;
                }
            }
        });
        mTabController.select(0);
        mGvNewestTribe.setAdapter(mNewestTribeAdapter);
        mLvConcernTribe.setMode(Mode.BOTH);
        mLvConcernTribe.setOnRefreshListener(this);
        mLvConcernTribe.setAdapter(mConcernTribeAdapter);
        loadInfos();
        return view;
    }

    private void loadInfos() {
        String hotTribeStr = DataHelper.getSpForData(getActivity())
                .getString(SP_CACHE_HOT_TRIBE, "[]");
        String latestPostsStr = DataHelper.getSpForData(getActivity())
                .getString(SP_CACHE_LATEST_POSTS, "[]");
        String latestTribeStr = DataHelper.getSpForData(getActivity())
                .getString(SP_CACHE_LATEST_TRIBE, "[]");
        try {
            ArrayList<TribeInfo> hotTribes = JsonUtil.getArray(
                    new JSONArray(hotTribeStr), TribeInfo.TRANSFORMER);
            ArrayList<CfPost> latestPosts = JsonUtil.getArray(
                    new JSONArray(latestPostsStr), CfPost.TRANSFORMER);
            ArrayList<TribeInfo> latestTribe = JsonUtil.getArray(
                    new JSONArray(latestTribeStr), TribeInfo.TRANSFORMER);
            if (hotTribes != null && hotTribes.size() > 0) {
                mHotTribes = hotTribes;
                fillHotTribesInfo();
            }
            if (latestPosts != null && latestPosts.size() > 0) {
                mNewestPosts = latestPosts;
                fillLatestPostsInfo();
            }
            if (latestTribe != null && latestTribe.size() > 0) {
                mNewestTribes.clear();
                mNewestTribes.addAll(latestTribe);
                mNewestTribeAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        UpdateView();
    }
    
    private void checkDayNightUI() {
        if (mIsNightMode) {
            mViewChannel.setVisibility(View.VISIBLE);
            mTitleBar.showTabs(mTabController, getString(R.string.tribe_title_suggest),
                    getString(R.string.tribe_title_latest));
            mPtrListTribe.setVisibility(View.GONE);
            mLvSuggest.setVisibility(View.VISIBLE);
            loadChannelInfo(false);
            mLlTribeFragement.setBackgroundResource(R.drawable.bg);
        } else {
            mViewChannel.setVisibility(View.GONE);
            mTitleBar.showTabs(mTabController, getString(R.string.tribe_title_all),
                    getString(R.string.tribe_title_collect));
            mPtrListTribe.setVisibility(View.VISIBLE);
            mLlTribeFragement.setBackgroundColor(0XFFF2F2F2);
        }
        mTitleBar.showRightButton(R.drawable.icon_search, this);
        mTitleBar.showLeftButton(R.drawable.icon_personal, this);
    }

    @Override
    public void onTabButtonClicked() {
        super.onTabButtonClicked();
        if (mTypeChoosed == TAB_LEFT) {
            if (mIsNightMode) {
                fetchChannel(false);
            } else {
                mPtrListTribe.setRefreshing(Mode.PULL_FROM_START);
            }
        } else {
            if (mIsNightMode) {
                fetchChannel(true);
            } else {
                mLvConcernTribe.setRefreshing(Mode.PULL_FROM_START);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        UpdateView();
    }
    
    private void loadChannelInfo(boolean newChannel) {
        if (newChannel) {
            ArrayList<ChannelInfo> channelLatest = DataHelper.loadChannelInfos(getActivity(),
                    CacheChannelInfo.TYPE_CHANNEL_LATEST);
            if (channelLatest != null && channelLatest.size() > 0) {
                mNewChannelAdapter.setChannelInfo(channelLatest);
                mNewChannelAdapter.notifyDataSetChanged();
            }
        } else {
            ArrayList<ChannelInfo> channelOfficial = DataHelper.loadChannelInfos(getActivity(),
                    CacheChannelInfo.TYPE_CHANNEL_OFFCIAL_NIGHT);
            ArrayList<ChannelInfo> channelUser = DataHelper.loadChannelInfos(getActivity(),
                    CacheChannelInfo.TYPE_CHANNEL_USER_NIGHT);
            mNightAdapter.setChannelInfo(channelOfficial, channelUser);
            mNightAdapter.notifyDataSetChanged();
        }
    }

    private void fetchChannel(boolean newChannel) {
        if (!newChannel) {
            mChannelInfoManager.getChannelList();
        } else {
            mChannelInfoManager.getNewChannelLists();
        }
    }

    private void UpdateView() {
        new GetLatestPostsTask().executeLong();
        new GetHotTribesTask().executeLong();
        new GetLatestTribesTask().executeLong();
        fetchConcernTribes(true);
        mSwitchBanner.setParam(CommonBanner.Plate.TribeHomePage, 0);
    }

    private View newestPostGetView(CfPost post) {
        View view = mInflater.inflate(R.layout.list_item_newest_post, null);
        NewestPostViewHolder holder;
        holder = new NewestPostViewHolder();
        ViewUtils.inject(holder, view);
        view.setTag(holder);
        UserInfo mUserInfo = post.userInfo;
        Picasso.with(getActivity()).load(mUserInfo.getAvatar()).into(holder.mUserAvatar);
        holder.mIvUserGender.setImageResource(mUserInfo.gender == 0 ? R.drawable.pic_bg_woman : R.drawable.pic_bg_man);
        holder.mTvUserName.setText(mUserInfo.name);
        holder.mTvPostContent.setText(post.content);
        holder.mTvPostTime.setText(Utils.getTimeString(1, post.createdOn));
        holder.mTvTribeName.setText(post.forumName);
        holder.mPost = post;
        holder.mUserAvatar.setTag(post.userInfo);
        holder.mTvUserName.setTag(post.userInfo);
        holder.mUserAvatar.setOnClickListener(mNewestPostOnclick);
        holder.mTvUserName.setOnClickListener(mNewestPostOnclick);
        view.setOnClickListener(mNewestPostOnclick);
        return view;
    }

    
    private class NewestPostViewHolder {
        @ViewInject(R.id.av_avatar)
        AvatarView mUserAvatar;
        @ViewInject(R.id.iv_type_icon)
        ImageView mIvTypeIcon;
        @ViewInject(R.id.iv_vip_bg)
        ImageView mIvVipBg;
        @ViewInject(R.id.tv_user_name)
        TextView mTvUserName;
        @ViewInject(R.id.iv_user_gender)
        ProImageView mIvUserGender;
        @ViewInject(R.id.tv_tribe_name)
        TextView mTvTribeName;
        @ViewInject(R.id.tv_post_content)
        TextView mTvPostContent;
        @ViewInject(R.id.tv_post_time)
        TextView mTvPostTime;
        CfPost mPost;

    }

    
    private String getSmallImageUrl(String url) {
        return AliImgSpec.POST_PHOTO.makeUrlSingleImg(url);
    }

    private void fillLatestPostsInfo() {
        if (mNewestPosts != null && mNewestPosts.size() > 0) {
            mLlNewestPost.removeAllViews();
            for (int i = 0; i < mNewestPosts.size(); i++) {
                mLlNewestPost.addView(newestPostGetView(mNewestPosts.get(i)));
            }
        }
    }

    private void fillHotTribesInfo() {
        if (mHotTribes != null && mHotTribes.size() > 0) {
            mLlHotTribe.removeAllViews();
            for (int i = 0; i < mHotTribes.size(); i++) {
                mLlHotTribe.addView(hotTribeGetView(mHotTribes.get(i)));
            }
        }
    }

    private void saveSpData(String key, String value) {
        if (getActivity() != null) {
            DataHelper.getSpForData(getActivity()).edit().putString(key, value).commit();
        }
    }

    private OnClickListener mNewestPostOnclick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            NewestPostViewHolder holder = (NewestPostViewHolder) v.getTag();
            CfPost post = holder.mPost;
            Intent in = new Intent(getActivity(), ForumPostDetailActivity.class);
            in.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA, post);
            in.putExtra(ForumPostDetailActivity.EXTRL_IS_FORM_TRIBE, true);
            startActivity(in);
        }
    };

    private class NewestTribeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mNewestTribes.size();
        }

        @Override
        public TribeInfo getItem(int position) {
            return mNewestTribes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NewestTribeViewHoder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_newest_tribe, parent, false);
                holder = new NewestTribeViewHoder();
                ViewUtils.inject(holder, convertView);
                convertView.setTag(holder);
            } else {
                holder = (NewestTribeViewHoder) convertView.getTag();
            }
            switch (position) {
                case 0:
                    holder.mLineLeft.setVisibility(View.VISIBLE);
                    holder.mLineTop.setVisibility(View.VISIBLE);
                    holder.mLineRight.setVisibility(View.GONE);
                    holder.mLineBottom.setVisibility(View.GONE);
                    break;
                case 1:
                    holder.mLineLeft.setVisibility(View.GONE);
                    holder.mLineTop.setVisibility(View.GONE);
                    holder.mLineRight.setVisibility(View.VISIBLE);
                    holder.mLineBottom.setVisibility(View.GONE);
                    break;
                case 2:
                    holder.mLineLeft.setVisibility(View.GONE);
                    holder.mLineTop.setVisibility(View.GONE);
                    holder.mLineRight.setVisibility(View.GONE);
                    holder.mLineBottom.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
            TribeInfo tribe = getItem(position);
            if (tribe.icon != null && !"".equals(tribe.icon)) {
            	Picasso.with(getActivity()).load(tribe.icon).into(holder.mIvTribeLogo);
            }
            holder.mTvTribeName.setText(tribe.tribeName);
            holder.mTvPeopleNum.setText(getString(R.string.tribe_is_followed_count_people,
                    tribe.followCount));
            holder.mTvPostNum.setText(getString(R.string.tribe_is_followed_count_post,
                    tribe.threadCount));
            holder.mTribe = tribe;
            convertView.setOnClickListener(mNewestTribeOnclick);
            return convertView;
        }

    }

    private android.view.View.OnClickListener mNewestTribeOnclick =
            new android.view.View.OnClickListener() {

        @Override
        public void onClick(View v) {
            NewestTribeViewHoder holder = (NewestTribeViewHoder) v.getTag();
            TribeInfo tribe = holder.mTribe;
            Intent intent = new Intent(getActivity(), TribeDetailActivity.class);
            intent.putExtra(TribeInfo.INTENT_EXTRA_INFO, tribe);
            startActivity(intent);
        }
    };

    private View hotTribeGetView(TribeInfo tribe) {
        HotTribeViewHoder holder;
        View view = mInflater.inflate(R.layout.list_item_hot_tribe, null);
        holder = new HotTribeViewHoder();
        ViewUtils.inject(holder, view);
        view.setTag(holder);
        Picasso.with(getActivity()).load(tribe.icon).into(holder.mIvTribeLogo);
        holder.mTvTribeName.setText(tribe.tribeName);
        holder.mTvTribeDesc.setText(tribe.tribeDesc);
        holder.mTvPeopleNum.setText(getActivity().getString(R.string.tribe_is_followed_count_people,
                tribe.followCount));
        holder.mTvPostNum.setText(getActivity().getString(R.string.tribe_is_followed_count_post,
                tribe.threadCount));
        holder.mTvUpCount.setText(getActivity().getString(R.string.tribe_homepage_up_count_desc, 
                tribe.upCount));
        holder.mTribe = tribe;
        view.setOnClickListener(mHotTribeOnclick);
        return view;
    }

    private android.view.View.OnClickListener mHotTribeOnclick =
            new android.view.View.OnClickListener() {

        @Override
        public void onClick(View v) {
            HotTribeViewHoder holder = (HotTribeViewHoder) v.getTag();
            TribeInfo tribe = holder.mTribe;
            Intent intent = new Intent(getActivity(), TribeDetailActivity.class);
            intent.putExtra(TribeInfo.INTENT_EXTRA_INFO, tribe);
            startActivity(intent);
        }
    };

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
            holder.mTvPeopleNum.setText(getString(R.string.tribe_is_followed_count_people,
                    tribe.followCount));
            holder.mTvPostNum.setText(getString(R.string.tribe_is_followed_count_post,
                    tribe.threadCount));
            holder.mTvDayPostCount.setText(getString(R.string.tribe_day_post_count,
                    tribe.dayPostCount));
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
        ProImageView mIvTribeLogo;
        @ViewInject(R.id.tv_tribe_name)
        TextView mTvTribeName;
        @ViewInject(R.id.tv_tribe_describe)
        TextView mTvTribeDesc;
        @ViewInject(R.id.tv_post_num)
        TextView mTvPostNum;
        @ViewInject(R.id.tv_people_num)
        TextView mTvPeopleNum;
        @ViewInject(R.id.tv_up_num)
        TextView mTvUpCount;
        TribeInfo mTribe;
    }

    private class NewestTribeViewHoder {
        @ViewInject(R.id.piv_tribe_logo)
        ProImageView mIvTribeLogo;
        @ViewInject(R.id.tv_tribe_name)
        TextView mTvTribeName;
        @ViewInject(R.id.tv_post_num)
        TextView mTvPostNum;
        @ViewInject(R.id.tv_people_num)
        TextView mTvPeopleNum;
        @ViewInject(R.id.line_horizontal_left)
        ThemeLineView mLineLeft;
        @ViewInject(R.id.line_horizontal_right)
        ThemeLineView mLineRight;
        @ViewInject(R.id.line_vertical_top)
        ThemeLineView mLineTop;
        @ViewInject(R.id.line_vertical_bottom)
        ThemeLineView mLineBottom;
        TribeInfo mTribe;
    }

    private class ConcernTribeViewHoder {
        @ViewInject(R.id.piv_tribe_logo)
        ProImageView mIvTribeLogo;
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
            case R.id.ll_hot_post:
                startActivity(HotEssentialActivity.class);
                break;
            case R.id.ll_all_items:
                intent.setClass(getActivity(), TribeTypeHomeActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_fresh_topic:
                intent.setClass(getActivity(), TribeLatestTopicActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_game_zone:
                intent.setClass(getActivity(), GameZoneActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_newest_post_more:
                startActivity(TribeLatestPostsActivity.class);
                break;
            case R.id.tv_newest_tribe_more:
                intent.setClass(getActivity(), TribesListActivity.class);
                intent.putExtra(TribesListActivity.MORE_TRIBE_TYPE, 1);
                startActivity(intent);
                break;
            case R.id.tv_hot_tribe_more:
                intent.setClass(getActivity(), TribesListActivity.class);
                intent.putExtra(TribesListActivity.MORE_TRIBE_TYPE, 0);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onGetChannelInfoSuccess(ArrayList<ChannelInfo> offcialChannelInfos,
            ArrayList<ChannelInfo> userChannelInfos) {
        updateChannelDbInfo(CacheChannelInfo.TYPE_CHANNEL_OFFCIAL_NIGHT, offcialChannelInfos);
        updateChannelDbInfo(CacheChannelInfo.TYPE_CHANNEL_USER_NIGHT, userChannelInfos);
        mNightAdapter.setChannelInfo(offcialChannelInfos, userChannelInfos);
        mNightAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetNewChannelListsSuccess(ArrayList<ChannelInfo> channels) {
        updateChannelDbInfo(CacheChannelInfo.TYPE_CHANNEL_LATEST, channels);
        mNewChannelAdapter.setChannelInfo(channels);
        mNewChannelAdapter.notifyDataSetChanged();
    }

    private void updateChannelDbInfo(int type, ArrayList<ChannelInfo> channels) {
        DataHelper.deleteChannelInfo(getActivity(), type);
        ArrayList<CacheChannelInfo> infos = new ArrayList<>();
        for (ChannelInfo info : channels) {
            CacheChannelInfo cacheChannelInfo = new CacheChannelInfo(info);
            cacheChannelInfo.dayType = type;
            infos.add(cacheChannelInfo);
        }
        DataHelper.insertCacheChannelInfo(getActivity(), infos);
    }

    private class GetLatestPostsTask extends MsTask {

        public GetLatestPostsTask() {
            super(getActivity(), MsRequest.TRIBE_LATEST_POST_LIST);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("preview=").append(1).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrListTribe.onRefreshComplete();
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                ArrayList<CfPost> posts = JsonUtil.getArray(ja, CfPost.TRANSFORMER);
                saveSpData(SP_CACHE_LATEST_POSTS, ja.toString());
                mNewestPosts.clear();
                mNewestPosts.addAll(posts);
                fillLatestPostsInfo();

            }
        }

    }
    
    private class GetHotTribesTask extends MsTask {

        public GetHotTribesTask() {
            super(getActivity(), MsRequest.TRIBE_HOT_LIST);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("preview=").append(1).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrListTribe.onRefreshComplete();
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                ArrayList<TribeInfo> tribes = JsonUtil.getArray(ja, TribeInfo.TRANSFORMER);
                saveSpData(SP_CACHE_HOT_TRIBE, ja.toString());
                mHotTribes.clear();
                mHotTribes.addAll(tribes);
                fillHotTribesInfo();
            }
        }
    }

    private class GetLatestTribesTask extends MsTask {

        public GetLatestTribesTask() {
            super(getActivity(), MsRequest.TRIBE_LATEST_TRIBE_LIST);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("preview=").append(1).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrListTribe.onRefreshComplete();
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                ArrayList<TribeInfo> tribes = JsonUtil.getArray(ja, TribeInfo.TRANSFORMER);
                saveSpData(SP_CACHE_LATEST_TRIBE, ja.toString());
                mNewestTribes.clear();
                mNewestTribes.addAll(tribes);
                mNewestTribeAdapter.notifyDataSetChanged();
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
            return new StringBuilder("uid=").append(userId)
                    .append("&offset=").append(mOffset)
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mLvConcernTribe.onRefreshComplete();
            if (response.isSuccessful()) {
                if (mOffset <= 0) {
                    mConcernTribes.clear();
                }
                ArrayList<TribeInfo> tribes = JsonUtil.getArray(
                        response.getJsonArray(), TribeInfo.TRANSFORMER);
                mConcernTribes.addAll(tribes);
                mConcernTribeAdapter.notifyDataSetChanged();
            }
        }

    }

    private void fetchConcernTribes(boolean refresh) {
        int size = mConcernTribeAdapter.getCount();
        int offset = refresh ? 0 : size;
        new GetConcernTribesTask(mUserInfo.userId, offset).executeLong();
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        if (mTypeChoosed == TAB_LEFT) {
            UpdateView();
        } else {
            fetchConcernTribes(true);
        }
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchConcernTribes(false);
    }
}
