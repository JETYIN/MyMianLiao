package com.tjut.mianliao.profile;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nineoldandroids.view.ViewHelper;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.R;
import com.tjut.mianliao.chat.GroupReportActivity;
import com.tjut.mianliao.component.CheckTribeBoxView;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.PagerSlidingTabStrip;
import com.tjut.mianliao.component.PopupView;
import com.tjut.mianliao.component.PopupView.OnItemClickListener;
import com.tjut.mianliao.component.PopupView.PopupItem;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.contact.FollowUserManager;
import com.tjut.mianliao.contact.UserEntryManager;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.BannedInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.mycollege.UpdateProfileImageActiivity;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.FaceManager;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;
import com.umeng.analytics.MobclickAgent;

public class ProfileFragment extends Fragment implements OnClickListener, OnItemClickListener {

    public static final String EXTRA_NICK_NAME = "extra_nick_name";

    public static final int RESULT_UPDATED = 1;
    private static final int REQUEST_SET_REMARK = 300;
    private static final int REQUEST_UPDATE_BG = 400;
    private static final int REQUEST_UPDATE_AVATAR = 500;
    protected static final int SHOW_IMAGE_REQUEST = 101;
    protected static final int REQUEST_UPDATE_NICK = 102;
    protected static final int REQUEST_UPDATE_DESC = 103;

    public static int sStickyTopToTab;
    public static int sStickyTopToViewPager;
    public static int sStickyBarHeight;
    public static int sViewPagerHeight;

    @ViewInject(R.id.iv_back)
    private ImageView mIvBack;
    @ViewInject(R.id.iv_menu)
    private ImageView mIvMenu;
    @ViewInject(R.id.iv_scene)
    private ImageView mIvSene;
    @ViewInject(R.id.av_avatar)
    private ProImageView mIvAvatar;
    @ViewInject(R.id.tv_edu_info)
    private TextView mTvEduInfo;
    @ViewInject(R.id.iv_vip_bg)
    private ImageView mIvVipBg;
    @ViewInject(R.id.tv_name)
    private TextView mTvName;
    @ViewInject(R.id.iv_vip)
    private ImageView mIvVip;
    @ViewInject(R.id.iv_type_icon)
    private ImageView mIvTypeIcon;
    @ViewInject(R.id.iv_gender)
    private ImageView mIvGender;
    @ViewInject(R.id.tv_follow_count)
    private TextView mTvFollowCount;
    @ViewInject(R.id.tv_fans_count)
    private TextView mTvFansCount;
    @ViewInject(R.id.tv_visitor_count)
    private TextView mTvVisitorCount;
    @ViewInject(R.id.ll_follow_count_info)
    private LinearLayout mLlFollowInfo;
    @ViewInject(R.id.ll_fans_count)
    private LinearLayout mLlFansInfo;

    private UserEntryManager mUserEntryManager;
    private UserInfoManager mUserInfoManager;
    private FragmentManager mFragmentManager;
    private ViewPager mViewPager;
    private LinearLayout mStickyView;

    private StickyScrollCallBack mScrollCallBack;
    private PagerSlidingTabStrip mTabLayout;

    private LightDialog mRemoveContactDialog;
    private LightDialog mBlockContactDialog;
    private LightDialog mBannedDialog;

    private CheckTribeBoxView mCheckTribeBoxView;
    private ArrayList<BannedInfo> mShowBannedInfos;

    private AccountInfo mAccountInfo;
    private UserInfo mUserInfo;
    private UserInfo mInfoMine;

    private PopupView mPopMenu;

    private boolean mIsModerator;
    private boolean mIsFirstResume = true;
    private boolean mIsMe;
    
    private int mScanTotle;
    private int mUpCount;
    
    private Activity mParentActivity;
    
    
    private FollowUserManager mFollowManager;

    public ProfileFragment(FragmentManager fragmentManager) {
        this.mFragmentManager = fragmentManager;
    }
    public ProfileFragment(){}
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mParentActivity = activity;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserInfoManager = UserInfoManager.getInstance(getActivity());
        mUserEntryManager = UserEntryManager.getInstance(getActivity());
        mAccountInfo = AccountInfo.getInstance(getActivity());
        mFollowManager = FollowUserManager.getInstance(getActivity());
        mInfoMine = mAccountInfo.getUserInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_profile, container, false);
        ViewUtils.inject(this, rootView);
        initView(rootView);
        mUserInfo = getUserInfo();
        mIsMe = mUserInfo.isMine(getActivity());
        showBasicUserInfo();
        if (mUserInfo.hasUserId() || mUserInfo.hasFaceId() || mUserInfo.hasGuid()) {
            fetchUserInfo();
        }
        new GetVisitorInfoTask().executeLong();
        new GetPraiseInfoTask().executeLong();
        return rootView;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mIsFirstResume) {
            mIsFirstResume = false;
        } else {
            mUserInfo = mUserInfoManager.getUserInfo(mUserInfo.userId);
            notifyFragment();
        }
    }

    private void fetchUserInfo() {
        if (!Utils.isNetworkAvailable(getActivity())) {
            toast(R.string.network_error);
            return;
        }

        new FetchUserTask().executeLong();
    }

    private UserInfo getUserInfo() {
        UserInfo info = getArguments().getParcelable(UserInfo.INTENT_EXTRA_INFO);
        if (info != null && info.hasUserId()) {
            UserInfo local = mUserInfoManager.getUserInfo(info.userId);
            return local == null ? info : local;
        }

        info = new UserInfo();
        info.guid = getArguments().getString(NewProfileActivity.EXTRA_USER_GUID);
        info.faceId = getArguments().getString(NewProfileActivity.EXTRA_USER_FACE_ID);
        if (info.hasFaceId() || info.hasGuid()) {
            info.name = getString(R.string.loading);
            mIvMenu.setVisibility(View.GONE);
        } else {
            mIvMenu.setVisibility(View.GONE);
            info.name = getString(R.string.prof_user_not_exist);
        }
        return info;
    }

    private void initView(View view) {
        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
        mStickyView = (LinearLayout) view.findViewById(R.id.sticky_view);
        mTabLayout = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
        mViewPager.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        mStickyView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        sStickyTopToViewPager = mStickyView.getMeasuredHeight();
        sStickyTopToTab = mStickyView.getChildAt(0).getMeasuredHeight();
        sStickyBarHeight = sStickyTopToViewPager - sStickyTopToTab;
        sViewPagerHeight = mViewPager.getMeasuredHeight();
        mStickyView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        sStickyTopToViewPager = mStickyView.getMeasuredHeight();
        sStickyTopToTab = mStickyView.getChildAt(0).getMeasuredHeight();
        mScrollCallBack = new StickyScrollCallBack() {

            @Override
            public void onScrollChanged(int scrollY) {
                processStickyTranslateY(scrollY);
            }

            @Override
            public int getCurrentViewpagerItem() {
                return mViewPager.getCurrentItem();
            }
        };
        mDetailFragment = new PDetailFragment();
        mMyLiveFragment = new MyLiveFragment();
        mPostsFragment = new UserPostsFragment();
        mDetailFragment.setScrollCallBack(mScrollCallBack);
        mMyLiveFragment.setScrollCallBack(mScrollCallBack);
        mPostsFragment.setScrollCallBack(mScrollCallBack);
        mAdapter = new ViewPagerFragmentAdapter(mFragmentManager);
        mAdapter.addFragment(mDetailFragment);
        mAdapter.addFragment(mMyLiveFragment);
        mAdapter.addFragment(mPostsFragment);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mAdapter);

        ViewPagerStateListener pagerStateListener = new ViewPagerStateListener() {

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == 1) {
                    downSelect = mViewPager.getCurrentItem();
                    mDetailFragment.invalidScroll();
                    mPostsFragment.invalidScroll();

                    if (downSelect == 0) {
                        int tempH1 = mDetailFragment.getStickyHeight();
                        int stickyH2 = 0;
                        if (tempH1 > sStickyTopToTab) {
                            stickyH2 = sStickyTopToTab;
                        } else {
                            stickyH2 = tempH1;
                        }
                        mMyLiveFragment.setStickyH(stickyH2);
                        mDetailFragment.setStickyH(stickyH2);
                    } else if (downSelect == 1) {
                        int tempH2 = mMyLiveFragment.getStickyHeight();
                        int stickyH3 = 0;
                        if (tempH2 > sStickyTopToTab) {
                            stickyH3 = sStickyTopToTab;
                        } else {
                            stickyH3 = tempH2;
                        }
                        mDetailFragment.setStickyH(stickyH3);
                        mPostsFragment.setStickyH(stickyH3);
                    } else {
                        int tempH3 = mPostsFragment.getStickyHeight();
                        int stickyH1 = 0;
                        if (tempH3 > sStickyTopToTab) {
                            stickyH1 = sStickyTopToTab;
                        } else {
                            stickyH1 = tempH3;
                        }
                        mMyLiveFragment.setStickyH(stickyH1);
                        mDetailFragment.setStickyH(stickyH1);
                    }

                }
            }
        };

        mTabLayout.setViewPagerStateListener(pagerStateListener);

        mTabLayout.setViewPager(mViewPager);

    }

    private void setOnClickListener() {
        mTvFollowCount.setOnClickListener(this);
        mTvFansCount.setOnClickListener(this);
        mTvVisitorCount.setOnClickListener(this);
        mIvSene.setOnClickListener(this);
        mTvName.setOnClickListener(this);
        mIvMenu.setOnClickListener(this);
        mIvAvatar.setOnClickListener(this);
        mIvBack.setOnClickListener(this);
        mLlFansInfo.setOnClickListener(this);
        mLlFollowInfo.setOnClickListener(this);
    }

    public void updateVisitCountInfo() {
        mTvVisitorCount.setText(String.valueOf(mScanTotle));
    }

    private void showBasicUserInfo() {
        // getTitleBar().showTitleText(mUserInfo.getNickname(), null);
        mTvEduInfo.setText(mUserInfo.getEduInfo(getActivity()));
        mIvVipBg.setVisibility(mUserInfo.vip ? View.VISIBLE : View.INVISIBLE);

        mTvName.setText(mUserInfo.getNickname());
        mIvVip.setVisibility(mUserInfo.vip ? View.VISIBLE : View.GONE);
        mIvGender.setImageResource(mUserInfo.getGenderIcon());

        mIvTypeIcon.setImageResource(mUserInfo.getTypeIcon());
        mTvFollowCount.setText(String.valueOf(mUserInfo.followCount));
        mTvFansCount.setText(String.valueOf(mUserInfo.fansCount));
        mIvMenu.setVisibility(mIsMe || mUserInfo.isModerator() ? View.GONE : View.VISIBLE);
        setOnClickListener();
        showScene();
    }

    private void showScene() {
        if (!TextUtils.isEmpty(mUserInfo.bgImg)) {
            Picasso.with(getActivity()).load(mUserInfo.bgImg).placeholder(R.drawable.pic_prof_scene).into(mIvSene);
        }
    }

    private int downSelect = 0;
    private int lastProcessStickyTranslateY = 0;
    private PDetailFragment mDetailFragment;
    private UserPostsFragment mPostsFragment;
    private MyLiveFragment mMyLiveFragment;
    private ViewPagerFragmentAdapter mAdapter;

    protected void processStickyTranslateY(int translateY) {
        if (translateY == Integer.MIN_VALUE || translateY == lastProcessStickyTranslateY) {
            return;
        }
        lastProcessStickyTranslateY = translateY;
        mStickyView.setTranslationY(translateY);
    }

    private class FetchUserTask extends MsTask {

        public FetchUserTask() {
            super(getActivity(), mUserInfo.hasUserId() || !mUserInfo.hasFaceId() ? MsRequest.USER_FULL_INFO
                    : MsRequest.FIND_BY_FACE_ID);
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder();
            if (mUserInfo.hasUserId()) {
                sb.append("user_id=").append(mUserInfo.userId);
            } else if (mUserInfo.hasFaceId()) {
                sb.append("face_id=").append(mUserInfo.faceId);
            } else {
                sb.append("guid=").append(mUserInfo.guid);
            }
            boolean isFriend = mUserInfo.isMine(getRefContext()) || mUserEntryManager.isFriend(mUserInfo.jid);
            return sb.append("&is_friend=").append(isFriend ? 1 : 0).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mIvMenu.setVisibility(View.GONE);
                mUserInfo = UserInfo.fromJson(response.getJsonObject());
                mIsModerator = mUserInfo.isModerator();
                mUserInfoManager.saveUserInfo(mUserInfo);

                showBasicUserInfo();
                notifyFragment();
            } else {
                if (!mUserInfo.hasUserId()) {
                    // getTitleBar().showTitleText(R.string.prof_user_not_exist,
                    // null);
                    mIvMenu.setVisibility(View.GONE);
                    mTvName.setText(R.string.prof_user_not_exist);
                } else {
                    notifyFragment();
                }
                if (response.code == MsResponse.MS_USER_NOT_EXIST) {
                    FaceManager.getInstance(getRefContext()).removeUserFace(mUserInfo.faceId);
                }
                response.showFailInfo(getRefContext(), R.string.prof_user_fetch_failed);
            }
        }
    }

    private class GetVisitorInfoTask extends MsTask {

        public GetVisitorInfoTask() {
            super(getActivity(), MsRequest.USER_VISITOR_INFO);
        }

        @Override
        protected String buildParams() {
            return "query_uid=" + mUserInfo.userId;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONObject object = response.getJsonObject();
                if (object != null) {
                    mScanTotle = object.optInt("all_visit_times");
                    updateVisitCountInfo();
                }
            }
        }

    }

    private class GetPraiseInfoTask extends MsTask {

        public GetPraiseInfoTask() {
            super(getActivity(), MsRequest.USER_PRAISE_INFO);
        }

        @Override
        protected String buildParams() {
            return "query_uid=" + mUserInfo.userId;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mUpCount = response.getJsonObject().optInt("praise_times");
                // updatePraiseCountInfo();
            }
        }

    }

    private void toast(int resId) {
        Toast.makeText(getActivity(), getResources().getString(resId), Toast.LENGTH_SHORT).show();
    }

    public interface StickyScrollCallBack {
        public void onScrollChanged(int scrollY);

        public int getCurrentViewpagerItem();
    }

    public interface ViewPagerStateListener {
        public void onPageScrollStateChanged(int state);
    }

    private void notifyFragment() {
        mDetailFragment.updateUserInfo(mUserInfo, mIvAvatar);
        mPostsFragment.updateUserInfo(mUserInfo);
        mMyLiveFragment.updateUserInfo(mUserInfo);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                mIvBack.setEnabled(false);
                mParentActivity.onBackPressed();
                break;
            case R.id.iv_menu:
                showPopupMenu(v);
                break;
            case R.id.tv_name:
                if (mUserInfo.isMine(getActivity())) {
                    Intent intent = new Intent(getActivity(), UpdateNickActivity.class);
                    intent.putExtra(UpdateNickActivity.EXT_USER_NICK, mUserInfo.getNickname());
                    startActivityForResult(intent, REQUEST_UPDATE_NICK);
                    MobclickAgent.onEvent(getActivity(), MStaticInterface.USER);
                }
                break;
            case R.id.tv_visitor_count:
                Intent in = new Intent(getActivity(), LatestVisitorActivity.class);
                in.putExtra(UserInfo.INTENT_EXTRA_INFO, mUserInfo);
                startActivity(in);
                break;
            case R.id.iv_scene:
                changeImage(UpdateProfileImageActiivity.TYPE_BG);
                MobclickAgent.onEvent(getActivity(), MStaticInterface.BACKGROUND);
                break;
            case R.id.av_avatar:
                changeImage(UpdateProfileImageActiivity.TYPE_AVATAR);
                MobclickAgent.onEvent(getActivity(), MStaticInterface.FACE1);
                break;
            case R.id.ll_follow_count_info:
                viewFollowList(FollowListActivity.FOLLOW_ME);
                break;
            case R.id.ll_fans_count:
                viewFollowList(FollowListActivity.COLLECTED_ME);
                break;

            default:
                break;
        }
    }
    private void viewFollowList(int type) {
        Intent intent = new Intent(getActivity(), FollowListActivity.class);
        intent.putExtra(FollowListActivity.EXT_FOLLOW_TYPE, type);
        startActivity(intent);
    }
    private void showPopupMenu(View anchor) {
        if ((mFollowManager.isFollow(mUserInfo.userId + "")) && (mUserEntryManager.isBlacklisted(mUserInfo.jid))) {
            if (!mIsModerator && mInfoMine.isModerator()) {
                mPopMenu = new PopupView(getActivity()).setItems(R.array.profile_menu_popup_del_del_nospeak, this);
            } else {
                mPopMenu = new PopupView(getActivity()).setItems(R.array.profile_menu_popup_del_del, this);
            }
        } else if ((mFollowManager.isFollow(mUserInfo.userId + "")) && (!(mUserEntryManager.isBlacklisted(mUserInfo.jid)))) {
            if (!mIsModerator && mInfoMine.isModerator()) {
                mPopMenu = new PopupView(getActivity()).setItems(R.array.profile_menu_popup_del_ad_nospeak, this);
            } else {
                mPopMenu = new PopupView(getActivity()).setItems(R.array.profile_menu_popup_del_ad, this);
            }
        } else if ((!mFollowManager.isFollow(mUserInfo.userId + "")) && (mUserEntryManager.isBlacklisted(mUserInfo.jid))) {
            if (!mIsModerator && mInfoMine.isModerator()) {
                mPopMenu = new PopupView(getActivity()).setItems(R.array.profile_menu_popup_ad_del_nospeak, this);
            } else {
                mPopMenu = new PopupView(getActivity()).setItems(R.array.profile_menu_popup_ad_del, this);
            }
        } else {
            if (!mIsModerator && mInfoMine.isModerator()) {
                mPopMenu = new PopupView(getActivity()).setItems(R.array.profile_menu_popup_ad_ad_nospeak, this);
            } else {
                mPopMenu = new PopupView(getActivity()).setItems(R.array.profile_menu_popup_ad_ad, this);
            }
        }
        mPopMenu.showAsDropDown(anchor, false);
    }

    private void changeImage(int type) {
        Intent intent = new Intent(getActivity(), UpdateProfileImageActiivity.class);
        intent.putExtra(UpdateProfileImageActiivity.EXT_CHANGE_TYPE, type);
        intent.putExtra(UpdateProfileImageActiivity.EXT_USER_INFO, mUserInfo);
        startActivityForResult(intent, type == UpdateProfileImageActiivity.TYPE_AVATAR ? REQUEST_UPDATE_AVATAR
                : REQUEST_UPDATE_BG);
    }

    @Override
    public void onItemClick(int position, PopupItem item) {
        switch (position) {
            case 0:
                if (!mIsModerator && mInfoMine.isModerator()) {
                    // 禁言
                    showBannedDialog();
                    // banned();
                } else {
                    startActivity(new Intent(getActivity(), GroupReportActivity.class));
                }
                break;

            case 1:
                boolean isFriend = mUserEntryManager.isFriend(mUserInfo.jid);
                if (mFollowManager.isFollow(mUserInfo.userId + "")) {
                    mFollowManager.cancleFollow(mUserInfo.userId);
                } else {
                    mFollowManager.follow(mUserInfo.userId);
                }
                break;
            case 2:
                if (mUserEntryManager.isBlacklisted(mUserInfo.jid)) {
                    blacklist(false);
                } else {
                    showBlockContactDialog();
                }
                break;
            default:
                break;
        }
    }

    private void blacklist(boolean addTo) {
        // if (mConnectionManager.isXmppConnected()) {
        // new BlacklistTask(addTo).executeLong();
        // } else {
        // toast(R.string.disconnected);
        // }
    }

    private void showBlockContactDialog() {
        if (mBlockContactDialog == null) {
            mBlockContactDialog = new LightDialog(getActivity());
            mBlockContactDialog.setTitle(R.string.prof_blacklist_add);
            mBlockContactDialog.setMessage(R.string.prof_blacklist_add_desc);
            mBlockContactDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new BlacklistTask(true).executeLong();
                }
            });
            mBlockContactDialog.setNegativeButton(android.R.string.cancel, null);
        }
        mBlockContactDialog.show();
    }

    private class BlacklistTask extends AdvAsyncTask<Void, Void, Boolean> {
        private boolean mAddTo;

        public BlacklistTask(boolean addTo) {
            mAddTo = addTo;
        }

        @Override
        protected void onPreExecute() {
            // getTitleBar().showProgress();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return mUserEntryManager.changeBlacklist(mUserInfo.jid, mAddTo);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (mAddTo) {
                if (success) {
                    toast(R.string.prof_blacklist_add_success);
                } else {
                    toast(R.string.prof_blacklist_add_failed);
                }
            } else {
                if (success) {
                    toast(R.string.prof_blacklist_remove_success);
                } else {
                    toast(R.string.prof_blacklist_remove_failed);
                }
            }
            // getTitleBar().hideProgress();
        }
    }

    private void showBannedDialog() {
        if (mBannedDialog == null) {
            mBannedDialog = new LightDialog(getActivity());
            mBannedDialog.setTitle(R.string.please_choose);
            mCheckTribeBoxView = new CheckTribeBoxView(getActivity());
            mBannedDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String checkedIds = mCheckTribeBoxView.getCheckedIds();
                    String unCheckedIds = mCheckTribeBoxView.getUnCheckedIds();
                    banned(checkedIds, unCheckedIds);
                }
            });
            mBannedDialog.setNegativeButton(android.R.string.cancel, null);
        }
        mBannedDialog.setView(mCheckTribeBoxView.getView(getCheckbleTribeInfo()));
        mBannedDialog.show();
    }

    private ArrayList<BannedInfo> getCheckbleTribeInfo() {
        if (mShowBannedInfos != null) {
            return mShowBannedInfos;
        }
        mShowBannedInfos = new ArrayList<>();
        ArrayList<BannedInfo> mManagedInfos = mInfoMine.managedInfos;
        ArrayList<BannedInfo> userBannedInfos = mUserInfo.bannedInfos;
        if (mManagedInfos != null && mManagedInfos.size() > 0) {
            mShowBannedInfos = mManagedInfos;
        }
        if (userBannedInfos != null && userBannedInfos.size() > 0) {
            for (BannedInfo bannedInfo : userBannedInfos) {
                for (BannedInfo info : mShowBannedInfos) {
                    if (info.tribeId == bannedInfo.tribeId) {
                        info.checked = true;
                    }
                }
            }
        }
        return mShowBannedInfos;
    }

    private void banned(String bannedIds, String unBannedIds) {
        new BannedTask(bannedIds, unBannedIds).executeLong();
    }

    private class BannedTask extends MsTask {

        private String mBannedIds, mUnbannedIds;

        public BannedTask(String bannedIds, String unBannedIds) {
            super(getActivity(), MsRequest.CF_BANNED);
            mBannedIds = bannedIds;
            mUnbannedIds = unBannedIds;
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder();
            sb.append("uid_banned=").append(mUserInfo.userId).append("&liftingBan=").append(mUnbannedIds)
                    .append("&banned=").append(mBannedIds);
            return sb.toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mShowBannedInfos = mCheckTribeBoxView.getBannedInfos();
                toast(R.string.prof_banned_success);
            }
        }
    }

    private void updateNickView(String nick) {
        mUserInfo.nickname = nick;
        mUserInfoManager.updateUserInfo(mUserInfo);
        mTvName.setText(nick);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPostsFragment.onActivityResult(requestCode, resultCode, data);
        mDetailFragment.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_UPDATED && requestCode == REQUEST_SET_REMARK) {
            showBasicUserInfo();
        } else if (resultCode == RESULT_UPDATED && requestCode == REQUEST_UPDATE_BG) {
            String imagePath = data.getStringExtra(UpdateProfileImageActiivity.EXT_IMAGE_PATH);
            if (imagePath != null && imagePath.length() > 0) {
                mIvSene.setImageBitmap(Utils.fileToBitmap(imagePath));
            }
        } else if (resultCode == RESULT_UPDATED && requestCode == REQUEST_UPDATE_AVATAR) {
            String imagePath = data.getStringExtra(UpdateProfileImageActiivity.EXT_IMAGE_PATH);
            if (imagePath != null && imagePath.length() > 0) {
                mIvAvatar.setImageBitmap(Utils.fileToBitmap(imagePath));
            }
        } else if (resultCode == RESULT_UPDATED && requestCode == REQUEST_UPDATE_NICK) {
            String nick = data.getStringExtra(EXTRA_NICK_NAME);
            if (nick != null && nick.length() > 0) {
                updateNickView(nick);
            }
        }
    }
}
