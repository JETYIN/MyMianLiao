package com.tjut.mianliao;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.lang.ObjectUtils.Null;
import org.lasque.tusdk.core.TuSdk;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.letv.proxy.LeCloudProxy;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nineoldandroids.view.ViewHelper;
import com.squareup.picasso.Picasso;
import com.tencent.android.tpush.XGPushManager;
import com.tjut.mianliao.RedDot.RedDotType;
import com.tjut.mianliao.chat.UnreadMessageHelper;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.CustomViewPager;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.NaviButton;
import com.tjut.mianliao.component.SideSlipView;
import com.tjut.mianliao.component.SideSlipView.OnMenuToggleListener;
import com.tjut.mianliao.contact.SubscriptionHelper;
import com.tjut.mianliao.contact.SubscriptionHelper.NewFriendsRequestListener;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.contact.UserRemarkManager;
import com.tjut.mianliao.curriculum.CurriculumActivity;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.nova.CollectedPostActivity;
import com.tjut.mianliao.forum.nova.ForumMyPostActivity;
import com.tjut.mianliao.im.IMResourceManager;
import com.tjut.mianliao.login.LoginActivity;
import com.tjut.mianliao.login.LoginRegistActivity;
import com.tjut.mianliao.login.UserExtLoginManager;
import com.tjut.mianliao.main.TabFragment;
import com.tjut.mianliao.main.TabFragmentAdapter;
import com.tjut.mianliao.mycollege.TakeNoticesActivity;
import com.tjut.mianliao.profile.FriendDynamicsActivity;
import com.tjut.mianliao.profile.FollowListActivity;
import com.tjut.mianliao.profile.MyReplyActivity;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.SignInActivity;
import com.tjut.mianliao.profile.UserQrCardActivity;
import com.tjut.mianliao.promotion.PromotionManager;
import com.tjut.mianliao.scan.ScanActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.settings.SettingsActivity;
import com.tjut.mianliao.task.TaskActivity;
import com.tjut.mianliao.tribe.GameZoneActivity;
import com.tjut.mianliao.update.UpdateManager;
import com.tjut.mianliao.update.UpdateManager.OnCheckFinishedListener;
import com.tjut.mianliao.util.BitmapLoader;
import com.tjut.mianliao.util.LocationHelper;
import com.tjut.mianliao.util.LocationHelper.LocationObserver;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.NewsGuidDialog;
import com.tjut.mianliao.util.Utils;
import com.tjut.mianliao.xmpp.ConnectionManager;
import com.umeng.analytics.MobclickAgent;

public class MainActivity extends BaseFragmentActivity implements OnClickListener,
        LocationObserver, Observer, MsTaskListener, NewFriendsRequestListener,
        OnMenuToggleListener {

    public static final String SP_IS_FIRST_MAIN = "sp_main";

    public static final String EXTRA_ACTIVE_TAB_INDEX = "extra_active_tab_index";
    public static final String EXTRA_FRAGMENT_TAB_INDEX = "extra_fragment_tab_index";
    public static final String EXTRA_POST_TAB_INDEX = "extra_post_tab_index";
    public static int mSubIndex, mChannelIndex;
    public static boolean mShouldChange, mToNewsPost;

    private static final long POLLING_MILLIS = 20000;

    private CustomViewPager mVpMain;
    private TabFragmentAdapter mFragmentAdapter;
    private MsTaskManager mTaskManager;
    private ImageView mIvSuccess;

    // keep a reference of the loader, so it doesn't get recycled when main
    // activity is still alive.
    @SuppressWarnings("unused")
    private BitmapLoader mAvatarLoader;

    private AccountInfo mAccount;
    private UserInfo mUserInfo;

    @ViewInject(R.id.tv_name)
    private TextView mTvName;
    @ViewInject(R.id.tv_account)
    private TextView mTvAccount;
    @ViewInject(R.id.tv_posts_count)
    private TextView mTvPostCount;
    @ViewInject(R.id.tv_follow_count)
    private TextView mTvFollowCount;
    @ViewInject(R.id.tv_fans_count)
    private TextView mTvFansCount;
    @ViewInject(R.id.iv_avatar)
    private AvatarView mAvarartView;
    @ViewInject(R.id.iv_gender)
    private ImageView mIvGender;

    private RedDot mRedDot;
    private UnreadMessageHelper mUnreadMessageHelper;
    private SubscriptionHelper mSubscriptionHelper;
    private LightDialog mLogoutDialog;
    private UserExtLoginManager mUserExtLoginManager;

    private UpdateManager mUpdateManager;
    private OnCheckFinishedListener mCheckUpdateListener = new OnCheckFinishedListener() {
    };

    private LocationHelper mLocationHelper;
    private SharedPreferences mPreferences;
    private NewsGuidDialog mGuidDialog;

    public static boolean mMenuShow;

    private static ArrayList<TabFragment> mTabFragments;

    private Handler mHandler = new Handler();
    private static DrawerLayout mDrawerLayout;
    private boolean mIsScale = false;

    private static NaviButton mPostButton;
    private static NaviButton mTribeButton;

    private Runnable mPollingRunnable = new Runnable() {
        @Override
        public void run() {
            mUnreadMessageHelper.updateRedDot();
            mHandler.postDelayed(this, POLLING_MILLIS);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        reset();
        setContentView(R.layout.activity_main);
        ViewUtils.inject(this);
        LoginStateHelper.xmppInit(this);
        startService(new Intent(this, MianLiaoService.class));
        mPreferences = DataHelper.getSpForData(this);

        mLocationHelper = LocationHelper.getInstance(this);
        mLocationHelper.addObserver(this);
        mUnreadMessageHelper = UnreadMessageHelper.getInstance(this);
        mSubscriptionHelper = SubscriptionHelper.getInstance(this);
        mUserExtLoginManager = UserExtLoginManager.getInstance(this);
        IMResourceManager.getInstance(this);

        mAvatarLoader = BitmapLoader.getInstance();
        mUpdateManager = new UpdateManager(this);
        mAccount = AccountInfo.getInstance(this);
        mUserInfo = mAccount.getUserInfo();
        mTaskManager = MsTaskManager.getInstance(this);
        mTaskManager.registerListener(this);
        Settings settings = Settings.getInstance(this);
        if (settings.allowDailyCourseAlarm()) {
            AlarmHelper.getInstance(this).setCourseAlarm(settings.getDailyCourseAlarmDay(),
                    settings.getDailyCourseAlarmHour(), settings.getDailyCourseAlarmMinute());
        }
        mIvSuccess = (ImageView) findViewById(R.id.iv_post_success);
        mPostButton = (NaviButton) findViewById(R.id.nb_forum);
        mTribeButton = (NaviButton) findViewById(R.id.nb_tribe);

        mFragmentAdapter = new TabFragmentAdapter(getSupportFragmentManager(), findViewById(R.id.ll_footbar));
        mVpMain = (CustomViewPager) findViewById(R.id.vp_main);
        mVpMain.setOffscreenPageLimit(3);
        mVpMain.setAdapter(mFragmentAdapter);
        mVpMain.setOnPageChangeListener(mFragmentAdapter);
        mVpMain.setPagingEnabled(false);
        activeXTab(getIntent());

        if (PromotionManager.getInstance(this).isSplashReady()) {
            startActivity(new Intent(this, SplashActivity.class)
                    .putExtra(SplashActivity.EXTRA_SHOW_ONLY, true));
            overridePendingTransition(0, 0);
        }
/**this代表observe，注册成为监听者,observeble将回调update方法**/
        mRedDot = RedDot.getInstance();
        mRedDot.addObserver(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.ssv_view);
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);
        initEvents();
        updateUserInfo();
        new FetchUserTask().executeLong();
        mTabFragments = new ArrayList<>();
    }

    public static void addTabFragment(TabFragment fragment) {
        if (fragment != null && !mTabFragments.contains(fragment)) {
            mTabFragments.add(fragment);
        }
    }

    private void reset() {
        UserInfoManager.getInstance(this).clear();
        UserRemarkManager.getInstance(this).clear();
        UserRemarkManager.getInstance(this).update();
        UserInfoManager.getInstance(this);
    }

    private void registerXG() {
        AccountInfo accountInfo = AccountInfo.getInstance(this);
        String account = accountInfo.getAccount();
        if (account != null) {
            XGPushManager.registerPush(this, account.toLowerCase());
        }
    }

    private void showGuidDialog(String spKey) {
        boolean isFirst = mPreferences.getBoolean(spKey, false);
        if (isFirst) {
            mGuidDialog = new NewsGuidDialog(this, R.style.Translucent_NoTitle);
            mGuidDialog.showGuidImage(getGuidImageRes(spKey), spKey);
        }
    }

    private int[] getGuidImageRes(String spKey) {
        int[] imgRes = null;
        if (spKey.equals(SP_IS_FIRST_MAIN)) {
            imgRes = new int[]{R.drawable.guid_profile, R.drawable.guid_search,
                    R.drawable.guid_video_post};
        }
        return imgRes;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!mAccount.isLoggedIn()) {
            gotoLogin();
            return;
        } else if (!checkUserState(UserState.getInstance().getCode())) {
            return;
        }

        showGuidDialog(SP_IS_FIRST_MAIN);
        CheckinHelper.checkin(this, null);

        UserState.getInstance().addObserver(this);

        mCheckUpdateListener.isAlive = true;
        mUpdateManager.checkForUpdate(false, mCheckUpdateListener);
        mHandler.post(mPollingRunnable);
    }

    private void gotoLogin() {
        Intent i = new Intent(this, LoginRegistActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        UserState.getInstance().deleteObserver(this);
        mCheckUpdateListener.isAlive = false;
        mHandler.removeCallbacks(mPollingRunnable);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        activeXTab(intent);
    }

    @Override
    protected void onDestroy() {
        LoginStateHelper.xmppExit(this);
        mLocationHelper.removeObserver(this);
        mLocationHelper.removeUpdates(false);
        mRedDot.deleteObserver(this);
        stopService(new Intent(this, MianLiaoService.class));
        mTaskManager.unregisterListener(this);
        ConnectionManager.getInstance(this).exit();
        LeCloudProxy.destory();
        LeCloudProxy.destory();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Do not quit directly to improve user experience.
        moveTaskToBack(true);
    }

    static boolean isSdkInited = false;

    @Override
    protected void onResume() {
        super.onResume();
        new FetchUserTask().executeLong();
        if (!isSdkInited) {
            mDrawerLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new InitTask().execute();
                    isSdkInited = true;
                }
            }, 50);
        }
    }

    class InitTask extends AsyncTask<Null, Null, Boolean> {

        @Override
        protected Boolean doInBackground(Null... params) {
            mLocationHelper.requestUpdates(false);
            registerXG();
            TuSdk.init(MainActivity.this, MianLiaoApp.sTuSdkKey);
            // it should be false while in release version
            TuSdk.enableDebugLog(false);
            return null;
        }

    }

    public static void showDrawerLayout() {
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    public static void hideDrawerLayout() {
        mDrawerLayout.closeDrawer(Gravity.LEFT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nb_forum:
            case R.id.nb_chat:
            case R.id.nb_tribe:
            case R.id.nb_live:
                int index = mFragmentAdapter.onTabButtonClicked(mFragmentAdapter.getTab(v.getId()));
                mVpMain.setCurrentItem(index);
                break;
            case R.id.tv_game_center:
                startActivity(GameZoneActivity.class);
                break;
            case R.id.mianliao_tool:
                startActivity(MianLiaoToolActivity.class);
                break;
            case R.id.tv_collected_post:
                startActivity(CollectedPostActivity.class);
                break;
            case R.id.tv_sign_in:
                startActivity(SignInActivity.class);
                break;
            case R.id.tv_task_center:
                startActivity(TaskActivity.class);
                break;
            case R.id.ll_scan:
                startActivity(ScanActivity.class);
                break;
            case R.id.ll_setting:
                startActivityForResult(SettingsActivity.class);
                break;
            case R.id.ll_qr_mine:
                viewQrCard();
                break;
            case R.id.iv_avatar:
            case R.id.rl_user_info:
                showProfile();
                break;
            case R.id.tv_friends_dyna:
                startActivity(FriendDynamicsActivity.class);
                break;
            case R.id.tv_comments:
                startActivity(MyReplyActivity.class);
                break;
            case R.id.ll_posts:
                startActivity(ForumMyPostActivity.class);
                break;
            case R.id.ll_follow_count:
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
        Intent intent = new Intent(this, FollowListActivity.class);
        intent.putExtra(FollowListActivity.EXT_FOLLOW_TYPE, type);
        startActivity(intent);
    }

    private void viewQrCard() {
        Intent iQrCode = new Intent(this, UserQrCardActivity.class);
        iQrCode.putExtra(UserInfo.INTENT_EXTRA_INFO, mUserInfo);
        startActivity(iQrCode);
        MobclickAgent.onEvent(this, MStaticInterface.CODE);
    }

    private void showProfile() {
        Intent intent = new Intent(this, NewProfileActivity.class);
        intent.putExtra(UserInfo.INTENT_EXTRA_INFO, mUserInfo);
        startActivity(intent);
    }

    private void startActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    private void startActivityForResult(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivityForResult(intent, 100);
    }

    private void activeXTab(Intent intent) {
        int tabIndex = intent.getIntExtra(EXTRA_ACTIVE_TAB_INDEX, -1);
        mSubIndex = intent.getIntExtra(EXTRA_FRAGMENT_TAB_INDEX, 0);
        mChannelIndex = intent.getIntExtra(EXTRA_POST_TAB_INDEX, 0);
        if (mSubIndex > 0 || mChannelIndex > 0) {
            mShouldChange = true;
        }
        if (tabIndex < 0) {
            tabIndex = Settings.getInstance(this).getDefaultTabIndex();
        }
        mVpMain.setCurrentItem(tabIndex);
    }

    private void updateBadge(final RedDotType type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int btnId;
                switch (type) {
                    case CHAT:
                        btnId = R.id.nb_chat;
                        break;
                    case MY_COLLEGE:
                        //btnId = R.id.nb_tribe;
                        break;
                    default:
                        btnId = 0;
                        break;
                }
                /*if (btnId == R.id.nb_chat) {
                    ((NaviButton) findViewById(btnId)).updateBadge(mUnreadMessageHelper.getTotalCount());
                }*/
            }
        });
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.activity_slide_right_in, R.anim.activity_slide_left_out);
    }

    private boolean checkUserState(int code) {
        switch (code) {
            case UserState.NORMAL:
                return true;
            case UserState.INACTIVE:
                onUserBanned();
                break;
            case UserState.INVALID_TOKEN:
                onTokenInvalid();
                break;
            default:
                break;
        }
        return false;
    }

    public void onUserBanned() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), InactiveActivity.class));
            }
        });
    }

    public void onTokenInvalid() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LightDialog logoutNotice = new LightDialog(MainActivity.this);
                logoutNotice.setTitle(R.string.lgi_login_expired);
                logoutNotice.setMessage(R.string.lgi_login_expired_desc);
                logoutNotice.setPositiveButton(android.R.string.ok, null);
                logoutNotice.setCancelable(false);
                logoutNotice.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        gotoLogin();
                    }
                });
                logoutNotice.show();
            }
        });
    }

    @Override
    public void onReceiveLocation() {
        new MsTask(this, MsRequest.UPDATE_INFO) {
            @Override
            protected String buildParams() {
                return new StringBuilder("location=").append(mLocationHelper.getCurrentLocString()).toString();
            }
        }.executeLong();
    }

    /**
     * 观察者回调
     **/
    @Override
    public void update(Observable observable, Object data) {
        if (data != null) {
            if (observable instanceof RedDot) {
                updateBadge((RedDotType) data);
            } else if (observable instanceof UserState) {
                checkUserState((Integer) data);
            }
        }
    }

    @Override
    public void onPreExecute(MsTaskType type) {
    }

    @Override
    public void onPostExecute(MsTaskType type, MsResponse response) {
        switch (type) {
            case FORUM_PUBLISH_POST:
                showPostSuccess();
                mToNewsPost = true;
                break;
            default:
                break;
        }
    }

    private void showPostSuccess() {
        mIvSuccess.setVisibility(View.VISIBLE);
        mIvSuccess.postDelayed(new Runnable() {

            @Override
            public void run() {
                mIvSuccess.setVisibility(View.GONE);
            }
        }, 2000);
    }

    private class FetchUserTask extends MsTask {

        public FetchUserTask() {
            super(MainActivity.this, MsRequest.USER_FULL_INFO);
        }

        @Override
        protected String buildParams() {
            return "user_id=" + mAccount.getUserId();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                UserInfo user = UserInfo.fromJson(response.getJsonObject());
                UserInfoManager.getInstance(MainActivity.this).saveUserInfo(user);
                mUserInfo = user;
                updateUserInfo();
            }
        }
    }

    public void updateUserInfo() {
        if (!mUserInfo.avatarFull.isEmpty()) {
            Picasso.with(this)
                    .load(Utils.getImagePreviewSmall(mUserInfo.avatarFull))
                    .placeholder(mUserInfo.defaultAvatar())
                    .into(mAvarartView);
        } else {
            Picasso.with(this)
                    .load(mUserInfo.defaultAvatar())
                    .into(mAvarartView);
        }
        mTvAccount.setText(getString(R.string.main_account_str, mUserInfo.account));
        mTvName.setText(mUserInfo.getDisplayName(this));
        mIvGender.setImageResource(mUserInfo.getGenderIcon());
        mTvFollowCount.setText(String.valueOf(mUserInfo.followCount));
        mTvFansCount.setText(String.valueOf(mUserInfo.fansCount));
        mTvPostCount.setText(String.valueOf(mUserInfo.postCount));
    }

    @Override
    public void onNewRequest(String target) {
        ((NaviButton) findViewById(R.id.nb_chat)).updateBadge(mSubscriptionHelper.getCount());
    }

    public static void showRefreshRed(int type, boolean isShow) {
        switch (type) {
            case 0:
                if (isShow) {
                    mPostButton.showRefreshRed();
                } else {
                    mPostButton.hideRefreshRed();
                }
                break;
            case 1:
                if (isShow) {
                    mTribeButton.showRefreshRed();
                } else {
                    mTribeButton.hideRefreshRed();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onMenuToggle(int status) {
        mMenuShow = status == SideSlipView.STATUS_MENU_OPEN;
    }

    private void initEvents() {
        mDrawerLayout.setDrawerListener(new DrawerListener() {
            @Override
            public void onDrawerStateChanged(int newState) {
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                View mContent = mDrawerLayout.getChildAt(0);
                View mMenu = drawerView;
                float scale = 1 - slideOffset;
                float rightScale, leftScale;

                if (mIsScale) {
                    rightScale = 0.8f + scale * 0.2f;
                    leftScale = 1 - 0.3f * scale;
                } else {
                    rightScale = 1.0f;
                    leftScale = 1.0f;
                }
                ViewHelper.setScaleX(mMenu, leftScale);
                ViewHelper.setScaleY(mMenu, leftScale);
                ViewHelper.setAlpha(mMenu, 0.6f + 0.4f * (1 - scale));
                ViewHelper.setTranslationX(mContent, mMenu.getMeasuredWidth() * (1 - scale));
                ViewHelper.setPivotX(mContent, 0);
                ViewHelper.setPivotY(mContent, mContent.getMeasuredHeight() / 2);
                mContent.invalidate();
                ViewHelper.setScaleX(mContent, rightScale);
                ViewHelper.setScaleY(mContent, rightScale);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (TabFragment fragment : mTabFragments) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
        if (resultCode == SettingsActivity.RESULT_RELOGIN) {
            startActivity(LoginRegistActivity.class);
        }
    }

}
