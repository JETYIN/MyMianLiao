package com.tjut.mianliao.live;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.enrique.stackblur.StackBlurManager;
import com.jess.ui.TwoWayAbsListView;
import com.jess.ui.TwoWayGridView;
import com.lecloud.entity.ActionInfo;
import com.letv.controller.LetvPlayer;
import com.letv.controller.PlayContext;
import com.letv.controller.PlayProxy;
import com.letv.recorder.bean.CameraParams;
import com.letv.recorder.bean.LivesInfo;
import com.letv.recorder.callback.LetvRecorderCallback;
import com.letv.recorder.callback.VideoRecorderDeviceListener;
import com.letv.recorder.controller.LetvPublisher;
import com.letv.universal.iplay.EventPlayProxy;
import com.letv.universal.iplay.ISplayer;
import com.letv.universal.iplay.OnPlayStateListener;
import com.letv.universal.play.util.PlayerParamsHelper;
import com.letv.universal.widget.ReSurfaceView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.anim.AnimUtils;
import com.tjut.mianliao.anim.GiftAnimView;
import com.tjut.mianliao.anim.GiftControllerInfo;
import com.tjut.mianliao.anim.HeartLayout;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.LiveDialog;
import com.tjut.mianliao.component.LiveGiftPicker;
import com.tjut.mianliao.component.RichMlEditText;
import com.tjut.mianliao.contact.ContactUpdateCenter;
import com.tjut.mianliao.contact.FollowUserManager;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.ChatRecord;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.HostUserInfo;
import com.tjut.mianliao.data.LiveAdminStatusInfo;
import com.tjut.mianliao.data.LiveInfo;
import com.tjut.mianliao.data.LiveMemberInfo;
import com.tjut.mianliao.data.LivingMenuInfo;
import com.tjut.mianliao.data.ShareInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.push.PushMessage;
import com.tjut.mianliao.explore.GoldDepositsActivity;
import com.tjut.mianliao.forum.nova.MessageRemindManager;
import com.tjut.mianliao.im.GroupChatManager;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.AliOSSHelper;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.ScreenShotTool;
import com.tjut.mianliao.util.SnsHelper;
import com.tjut.mianliao.util.Utils;
import com.tjut.mianliao.xmpp.ChatHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lasque.tusdk.core.listener.AnimationListenerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by YoopWu on 2016/6/21 0021.
 */
public class
        LivingActivity extends BaseActivity implements SurfaceHolder.Callback, OnPlayStateListener,
        GroupChatManager.GroupChatListener, AbsListView.OnScrollListener, HorizontalListView.OnPullRefresh,
        View.OnClickListener, ChatHelper.MessageReceiveListener, ChatHelper.MessageSendListener,
        LiveGiftPicker.OnSendMenuClickLisener, MessageRemindManager.LiveConnectionListener,
        SoftKeyboardHelper.SoftKeyboardStateListener, LiveGiftPicker.OnLiveGiftClickListener,
        VideoRecorderDeviceListener, FollowUserManager.OnUserFollowListener, AliOSSHelper.OnUploadListener,
        ContactUpdateCenter.ContactObserver{

    private static final int[] sAvatarResId = new int[]{R.drawable.pic_face_02,
            R.drawable.pic_face_03, R.drawable.pic_face_04, R.drawable.pic_face_05,
            R.drawable.pic_face_06, R.drawable.pic_face_07, R.drawable.pic_face_08,
            R.drawable.pic_face_09, R.drawable.pic_face_10};

    private static final String TAG = "Live";

    public static final String DATA_LIVE_INFO = "data_live_info";
    private static final int UPDATE_DELAY_TIME = 22;
    private static final int HIDE_GOON_TV = 23;
    private static final int FOLLOW_REFRESH = 33;


    private ReSurfaceView mSurfaceView;
    private SurfaceView mSurfaceView2;
    //    private ImageView mIvFollow;
    private HeartLayout mHeartLayout;
    private TextView mTvSendDm;
    private TextView mTvSend;
    private RichMlEditText mMessageEditor;
    private ListView mListView;
    private XDanmuView mDanmuView;
    private LinearLayout mLlReplyInput;
    private RelativeLayout mLlOperate;
    //private ReSurfaceView mSurfaceView;
    private LinearLayout mLlBottom;
    @ViewInject(R.id.fr_manager_botoom)
    private FrameLayout frmanagerView;

    @ViewInject(R.id.tv_manager)
    private TextView tvManger;
    @ViewInject(R.id.cb_send_dm)
    private CheckBox mCbDanmu;
    @ViewInject(R.id.fl_root)
    private FrameLayout mFlRoot;
    @ViewInject(R.id.rl_2nd_container)
    private RelativeLayout mFl2ndContainer;

    @ViewInject(R.id.iv_avatar)
    private ImageView mIvAvatar;
    private TextView mTvName;
    private TwoWayGridView mGridView;
    private TextView mTvDateDay;
    private TextView mTvIncome;
    private LinearLayout mLlIncome;
    private ImageView mIvFollow;
    private TextView mTvFollowNum;
    private TextView mTvFansNum;
    private TextView mTvContributeNum;
    private TextView mTvUserName;
    private TextView mTvSchool;
    private TextView mTvUserContent;
    private ImageView mIvUserGerden;
    private ImageView mIvUserType;
    private TextView mTvManageMent;
    private GridView mGvShare;
    //
    private LiveGiftPicker mGiftPicker;
    private LinearLayout mLlConnection;
    private TextView mTvConnection;
    private GiftAnimView mGiftAnimView;
    @ViewInject(R.id.iv_blur)
    private FrameLayout mFlBlur;

    private TextView mTvMyGold;
    @ViewInject(R.id.vp_living)
    private ViewPager mVpLivingScreen;
    @ViewInject(R.id.fl_connection_container)
    private FrameLayout mFlConnectionContainer;
    @ViewInject(R.id.ll_close_connection_menu)
    private LinearLayout mLlCloseConnMenu;
    @ViewInject(R.id.ll_wait_live_ui)
    private LinearLayout mLlWaitingUI;

    private TextView mTvTimerCount;
    private ListView mMenuList;
    private TextView mTvManager;
    private FrameLayout frView;
    private TextView mTvFollow;
    private TextView mTvtoProfile;
    private LinearLayout mLlButton;
    private ImageView mIvFriendCircle;
    private ImageView mIvWeichat;
    private ImageView mIvWeibo;
    private ImageView mIvQQ;
    private ImageView mIvQQZone;

    private int mGiftCount;

    private LiveChatAdapter mAdapter;
    private ChatHelper mChatHelper;
    private GroupChatManager mGroupChatManager;
    private UserInfoManager mUserInfoManager;
    private SoftKeyboardHelper mSoftKeyboardHelper;

    private PlayContext mPlayContext, mPlayContext2;
    private ISplayer mPlayer, mPlayer2;
    private LiveInfo mLiveInfo;
    private UserInfo mUserInfo;
    private UserInfo mRoomerInfo;
    private HeadAdapter mHeadAdapter;

    private Bundle mBundle, mBundle2;
    private String mGroupId;
    private String mChatId;
    private String mChatTarget;

    private int mScreenY = 0;
    protected int lastVisibleItem = 0;
    protected long lastScrollTime = 0;
    private int mListViewFirstItem = 0;
    private boolean mNeedScrollLastPostion = true;
    private int mTotalIncome, mTodayIncome;
    private ArrayList<LiveMemberInfo> mFocusUsers;

    private HostUserInfo mHostUserInfo;
    private boolean isFollow;
    private Bitmap mBgMap;
    private TextView mTvFollowHost;
    private LiveEndInfo mLiveEndInfo;
    private int mGold, mShowGiftNum, mCurrentGiftNum;
    private LiveGift mCurrentGift, mShowGift;
    private TimerTask mTimeTask;
    private int mDelayTime = 10;

    private LiveGift mChoosedGift;

    private int mTimerCount = 10;
    private Timer mTimer, mStreamCheckTimer;
    private TimerTask mTimerTask, mStreamCheckTimerTask;
    private ArrayList<View> mViewList;
    private ViewPagerAdapter mPageAdapter;
    private boolean isConnectionVisible;
    private boolean isCRSuc = false;
    //连线
    private LetvPublisher mPublisher;
    private String mActivityId;
    private String mLeUid;
    private String mLeSecretKey;
    private String mConnetionActivityId;
    private int mConnectionId;
    private boolean isReceiveMes = false;
    private MenuAdapter mMenuAdapter;
    private UserInfo mCurrentUser;
    private int mCurrentUserId;
    private LiveAdminStatusInfo mCurrentStatusInfo, mStatusInfo;
    private FollowUserManager mFollowUserManager;
    private AliOSSHelper mAliOSSHelper;
    private LiveDialog mReportDialog;
    private LiveDialog mBlackDialog;
    private LiveDialog mMoneyDialog;
    private ArrayList<Integer> mBlackList;
    private boolean isMeSilence;
    private LiveDialog mSilenceDialog;
    private int mReportId;
    private ShareInfo mShareInfo;
    private ArrayList<ShareInfo> mShareList;
    private ShareAdapter mShareAdapter;
    private SnsHelper mSnsHelp;
    private int mCurrentShareType = CreateLiveRoomActivity.SHARE_TYPE_CIRCLE_OF_FRIENDS;

    private Handler mFreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FOLLOW_REFRESH:
//                    if (mCurrentStatusInfo.isFollow) {
//                        mTvFollow.setText(getString(R.string.news_source_unfollow));
//                    } else {
//                        mTvFollow.setText(getString(R.string.tribe_collected_add));
//                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected int getLayoutResID() {
        return 0;
    }

    @Override
    protected int getBaseLayoutResID() {
        return R.layout.activity_living;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mChatHelper = ChatHelper.getInstance(this);
        mChatHelper.registerReceiveListener(this);
        mChatHelper.registerSendListener(this);
        mGroupChatManager = GroupChatManager.getInstance(this);
        mGroupChatManager.registerGroupChatListener(this);
        mUserInfo = AccountInfo.getInstance(this).getUserInfo();
        mUserInfoManager = UserInfoManager.getInstance(this);
        MessageRemindManager.setLiveConnectionListener(this);
        mFollowUserManager = FollowUserManager.getInstance(this);
        mFollowUserManager.registerOnUserFollowListener(this);
        mAliOSSHelper = AliOSSHelper.getInstance(this);
        ContactUpdateCenter.registerObserver(this);
        mSnsHelp = SnsHelper.getInstance();
        mFocusUsers = new ArrayList<>();
        mLiveInfo = getIntent().getParcelableExtra(DATA_LIVE_INFO);
        Intent intent = getIntent();
        Uri uri = intent.getData();

        if (uri != null) {
            String info = uri.getQueryParameter("info");
            info = "[" + info + "]";
            try {
                JSONArray jsonArray = new JSONArray(info);
                if (jsonArray == null || jsonArray.length() == 0) {
                    return;
                }
                mLiveInfo = LiveInfo.fromJson((JSONObject) jsonArray.get(0));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Utils.logD(TAG, info);
        }

        if (mLiveInfo == null) {
            finish();
            return;
        }
        mRoomerInfo = mUserInfoManager.getUserInfo(mLiveInfo.uid);
        mBundle = new Bundle();
        mBundle.putInt(PlayProxy.PLAY_MODE, EventPlayProxy.PLAYER_ACTION_LIVE);
        mBundle.putString(PlayProxy.PLAY_ACTIONID, mLiveInfo.activityId);
        mBundle.putBoolean(PlayProxy.PLAY_USEHLS, false);
        mPlayContext = new PlayContext(this);
        mPlayContext.setUsePlayerProxy(true);
        createOnePlayer(null);
        mPlayer.setParameter(mPlayer.getPlayerId(), mBundle);
        mPlayer.setOnPlayStateListener(this);
        enterLive();

        if (!TextUtils.isEmpty(mLiveInfo.prevUrl)) {
            String fileName = getFIleNameByUrl(mLiveInfo.prevUrl);
            if (HttpUtil.downLoad(this, mLiveInfo.prevUrl, fileName)) {
                mBgMap = BitmapFactory.decodeFile(fileName);
                blur(mFlBlur);
            } else {
                mBgMap = BitmapFactory.decodeResource(getResources(), R.drawable.splash_an);
                blur(mFlBlur);
            }
        } else {
            mBgMap = BitmapFactory.decodeResource(getResources(), R.drawable.splash_an);
            blur(mFlBlur);
        }

        mViewList = new ArrayList<>();
        mViewList.add(mInflater.inflate(R.layout.item_living_screen_space, null));
        mViewList.add(mInflater.inflate(R.layout.item_living_screen_fill, null));
        mPageAdapter = new ViewPagerAdapter();
        mVpLivingScreen.setAdapter(mPageAdapter);
        mVpLivingScreen.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 1) {
                    updateView();
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        getStreamStatus(mLiveInfo.activityId);
        mVpLivingScreen.setOffscreenPageLimit(2);
        mVpLivingScreen.setAdapter(mPageAdapter);
        mVpLivingScreen.setCurrentItem(1);
        new GetMyAdminStatus().executeLong();
    }

    private void getStreamStatus(String actId) {
        new GetStreamStatus(actId).executeLong();
    }

    private String getFIleNameByUrl(String url) {
        return Utils.getMianLiaoDir().getAbsolutePath() + "/" + System.currentTimeMillis()
                + Utils.getFilePostfix(url);
    }

    private void updateView() {
        new GetIncomeTask().executeLong();
        new GetHostInfoTask().executeLong();
        fetchLiveMembersList(true);
    }

    private void showBasicInfo() {
        if (!TextUtils.isEmpty(mLiveInfo.avatar)) {
            Picasso.with(this)
                    .load(AliImgSpec.USER_AVATAR.makeUrl(mLiveInfo.avatar))
                    .placeholder(UserInfo.getDefaultAvatar(mLiveInfo.gender))
                    .into(mIvAvatar);
        } else {
            Picasso.with(this)
                    .load(UserInfo.getDefaultAvatar(mLiveInfo.gender))
                    .into(mIvAvatar);
        }
        if (mRoomerInfo != null) {
            mTvName.setText(mRoomerInfo.getDisplayName(this));
        }
        mTvDateDay.setText(Utils.getTimeString(9, System.currentTimeMillis()));
        mGiftPicker.registerGiftClickListener(this);
        /**软盘监听**/
        mSoftKeyboardHelper = new SoftKeyboardHelper(findViewById(R.id.fl_root));
        mSoftKeyboardHelper.addSoftKeyboardStateListener(this);
    }

    private void createTarget() {
        mChatTarget = mChatId + "@groupchat." + Utils.getChatServerDomain();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_manager:
                mMenuList.setVisibility(View.VISIBLE);
                if (mCurrentStatusInfo.isAdmin || mCurrentUserId == mLiveInfo.uid) {
                    mMenuAdapter = new MenuAdapter(LivingMenuInfo.getLivingManagerMenu(mCurrentStatusInfo));
                } else {
                    mMenuAdapter = new MenuAdapter(LivingMenuInfo.getLivingNormalMenu(mStatusInfo.isAdmin, mCurrentStatusInfo));
                }
                mMenuList.setAdapter(mMenuAdapter);
                mMenuAdapter.notifyDataSetChanged();
                break;
            case R.id.fl_root:
            case R.id.bessal_view:
            case R.id.danmu_rl:
                new PrimaseHostTask().executeLong();
                //mHeartLayout.addHeart();
                if (!mGiftPicker.isVisible() && !mLlConnection.isShown() && !frView.isShown()) {
                    mHeartLayout.addHeart();
                }
                if (isConnectionVisible) {
                    hideConnectionView();
                }
                if (mGvShare.getVisibility() == View.VISIBLE) {
                    mGvShare.setVisibility(View.GONE);
                }
                mSnsHelp.closeShareBoard();
                toggleInputAndOperate(false);
                break;
            case R.id.tv_send:
                if (mCbDanmu.isChecked()) {
                    sendBarrage();
                } else {
                    new getIsSilenceTask().executeLong();
                }
                break;
            case R.id.iv_message:
                hideBottomView(mLlBottom);
                toggleInputAndOperate(true);
                break;
            case R.id.iv_back:
            case R.id.iv_back_top:
                exitLive();
                break;
            case R.id.iv_gift_normal:
                showGiftPickerAnimation();
                break;
            case R.id.iv_share:
                //hideBottomView(mLlBottom);
//                mGvShare.setVisibility(View.VISIBLE);
//                mLlOperate.setVisibility(View.GONE);
//                mShareInfo = new ShareInfo(LivingActivity.this);
//                mShareList = mShareInfo.getShareList();
//                mShareAdapter = new ShareAdapter();
//                mGvShare.setAdapter(mShareAdapter);
//                mShareAdapter.notifyDataSetChanged();
                mSnsHelp.openShareBoard(this, mLiveInfo, "");
                break;
            case R.id.iv_living_follow:
                new FollowHost().executeLong();
                break;
            case R.id.iv_avatar:
                mCurrentUserId = mLiveInfo.uid;
                UserInfo user = mUserInfoManager.getUserInfo(mLiveInfo.uid);
                if (user != null) {
                    mCurrentUser = user;
                    new GetCurrentUserAdminStatus().executeLong();
                }
                hideAllView();
                showBottomView(frView);
                break;
            case R.id.tv_back:
                exitLive();
                break;
            case R.id.tv_follow_host:
//                new FollowHost().executeLong();
                break;
            case R.id.iv_connection:
                //mLlConnection.setVisibility(View.VISIBLE);
                showConnectionView();
                break;
            case R.id.tv_connection:
                hideBottomView(mLlBottom);
                mTvConnection.setClickable(false);
                if (isCRSuc) {
                    new CancleConnectionTask().executeLong();
                } else {
                    new SendConnectionTask().executeLong();
                }
                break;
            case R.id.tv_follow_user:
                if (!mCurrentStatusInfo.isFollow ) {
                    if (!mCurrentStatusInfo.isInblickList) {
                        mFollowUserManager.follow(mCurrentUser.userId);
                    } else {
                        toast("此人被你拉黑，取消拉黑才能关注");
                    }
                } else {
                    mFollowUserManager.cancleFollow(mCurrentUser.userId);
                }
                break;
            case R.id.tv_profile:
                Intent iProfile = new Intent(LivingActivity.this, NewProfileActivity.class);
                UserInfo userInfo = new UserInfo();
                mUserInfo.userId = mCurrentUser.userId;
                iProfile.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
                startActivityForResult(iProfile, 0);
                break;
            case R.id.iv_close_connection: // close or show live connection
                if (mLlCloseConnMenu.getVisibility() == View.VISIBLE) {
                    mLlCloseConnMenu.setVisibility(View.GONE);
                } else {
                    mLlCloseConnMenu.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.ll_close_conn:
                closeConnection();
                break;
            case R.id.ll_close_conn_cancle:
                mLlCloseConnMenu.setVisibility(View.GONE);
                break;
            case R.id.iv_weichat_friend:
                resetShareImage();
                Picasso.with(this).load(R.drawable.icon_friend_hover).into(mIvFriendCircle);
                mCurrentShareType = CreateLiveRoomActivity.SHARE_TYPE_CIRCLE_OF_FRIENDS;
                break;
            case R.id.iv_weichat:
                resetShareImage();
                Picasso.with(this).load(R.drawable.icon_weixin_hover).into(mIvWeichat);
                mCurrentShareType = CreateLiveRoomActivity.SHARE_TYPE_WEI_CHAT;
                break;
            case R.id.iv_weibo:
                resetShareImage();
                Picasso.with(this).load(R.drawable.icon_weibo_hover).into(mIvWeibo);
                mCurrentShareType = CreateLiveRoomActivity.SHARE_TYPE_WEI_BO;
                break;
            case R.id.iv_qq:
                resetShareImage();
                Picasso.with(this).load(R.drawable.icon_qq_hover).into(mIvQQ);
                mCurrentShareType = CreateLiveRoomActivity.SHARE_TYPE_QQ;
                break;
            case R.id.iv_qq_zone:
                resetShareImage();
                Picasso.with(this).load(R.drawable.icon_zone_hover).into(mIvQQZone);
                mCurrentShareType = CreateLiveRoomActivity.SHARE_TYPE_QQ_ZONE;
                break;
            case R.id.ll_income:
                Intent incomeIntent = new Intent(LivingActivity.this, LiveGainRankActivity.class);
                startActivity(incomeIntent);
                break;
            default:
                break;
        }
    }

    private void resetShareImage () {
        Picasso.with(this).load(R.drawable.icon_friend_index).into(mIvFriendCircle);
        Picasso.with(this).load(R.drawable.icon_weixin_index).into(mIvWeichat);
        Picasso.with(this).load(R.drawable.icon_weibo_index).into(mIvWeibo);
        Picasso.with(this).load(R.drawable.icon_qq_index).into(mIvQQ);
        Picasso.with(this).load(R.drawable.icon_zone_index).into(mIvQQZone);
    }

    private boolean isBlack () {

        return false;
    }

    private void hideAllView() {
        if (mLlReplyInput.isShown()) {
            hideBottomView(mLlReplyInput);
        }
        if (mTvConnection.isShown()) {
            hideBottomView(mTvConnection);
        }
        if (mGiftPicker.isShown()) {
            hideBottomView(mGiftPicker);
        }
        if(mLlBottom.isShown()){
            hideBottomView(mLlBottom);
        }
    }

    private void showBottomView(View v) {
        if (v.getVisibility() != View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.live_up_animation);
            v.startAnimation(animation);
            v.setVisibility(View.VISIBLE);
        }
        if (mCurrentUser != null) {
            mTvFansNum.setText(mCurrentUser.fansCount + "");
            mTvFollowNum.setText(mCurrentUser.followCount + "");
//        mTvContributeNum.setText();
            mTvUserName.setText(mCurrentUser.name);
        }
        if (mCurrentStatusInfo != null) {
            if (mCurrentStatusInfo.isAdmin) {
                mTvManageMent.setVisibility(View.VISIBLE);
            } else {
                mTvManageMent.setVisibility(View.GONE);
            }
            mTvFollow.setText(getString(mCurrentStatusInfo.isFollow ? R.string.news_source_unfollow :
                    R.string.tribe_collected_add));
            if (mCurrentStatusInfo.isFollow) {
                mTvFollow.setText("取消关注");
            } else {
                mTvFollow.setText("+关注");
            }
        }
    }

    private void hideBottomView(View v) {
        if (v.getVisibility() != View.GONE) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.live_down_animation);
            v.startAnimation(animation);
            v.setVisibility(View.GONE);
        }
    }

    private void showBlackDialog () {
        if (mBlackDialog == null) {
            mBlackDialog = new LiveDialog(this);
            mBlackDialog.setText(getString(R.string.live_black_notice));
            mBlackDialog.setPositiveButton(R.string.live_make_black, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new AddBlackTask().executeLong();
                    if (mCurrentStatusInfo.isFollow) {
                        mFollowUserManager.cancleFollow(mCurrentUser.userId);
                    }
                }
            });
            mBlackDialog.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mBlackDialog.dismiss();
                }
            });
        }
        mBlackDialog.show();
    }

    private void showReportDialog () {
        if (mReportDialog == null) {
            mReportDialog = new LiveDialog(this);
            mReportDialog.setText(getString(R.string.live_report_notice));
            mReportDialog.setPositiveButton(R.string.report, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ScreenShotTool.process(LivingActivity.this);
                    byte[] data = Utils.BitmapBytes(ScreenShotTool.snapShotBitmap);
                    System.out.println("ScreenShotTool.snapShotBitmap" + ScreenShotTool.snapShotBitmap);
                    System.out.println("--------------------"  + data);
                    mAliOSSHelper.uploadImage(data, LivingActivity.this);
                }
            });
            mReportDialog.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mReportDialog.dismiss();
                }
            });
        }
        mReportDialog.show();
    }

    private void showGiftPickerAnimation() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.live_up_animation);
        mGiftPicker.startAnimation(animation);
        mGiftPicker.setVisible(true);
        mGold = mUserInfo.gold;
        mTvMyGold.setText(mUserInfo.gold + "");
        hideButtonParmAnimation();
    }

    private void hideGiftPickerAnimation() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.live_down_animation);
        mGiftPicker.startAnimation(animation);
        mGiftPicker.setVisible(false);
        showButtonParmAnimation();
        stopCount();
    }

    private void hideButtonParmAnimation() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.live_down_animation);
        mLlBottom.startAnimation(animation);
        mLlBottom.setVisibility(View.GONE);
    }

    private void showButtonParmAnimation() {
        if (mLlBottom.getVisibility() == View.VISIBLE) {
            return;
        }
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.live_up_animation);
        mLlBottom.startAnimation(animation);
        mLlBottom.setVisibility(View.VISIBLE);
    }

    private void showSpecialGiftAnim() {
        final ImageView iv = new ImageView(this);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        iv.setLayoutParams(lp);
        iv.setImageResource(R.drawable.car_three);
        mFlRoot.addView(iv);
        iv.setVisibility(View.GONE);
        AnimUtils.startSpecialGiftMoveAnim(iv);
    }

    private void toggleInputAndOperate(boolean showInput) {
//        fruserView.setVisibility(View.INVISIBLE);
        if (frView.isShown()) {
            hideBottomView(frView);
            showBottomView(mLlBottom);
        }
        mMenuList.setVisibility(View.GONE);

        if (mGiftPicker.isVisible()) {
            hideGiftPickerAnimation();
            mTvTimerCount.setVisibility(View.GONE);
        }
        if (mLlConnection.isShown()) {
            hideBottomView(mLlConnection);
        }
        mCbDanmu.setChecked(false);
        if (showInput) {
            Utils.showInput(mMessageEditor);
            mLlReplyInput.setVisibility(View.VISIBLE);
            hideBottomView(mLlBottom);
            //mLlOperate.setVisibility(View.GONE);
        } else {
            Utils.hideInput(mMessageEditor);
            mLlReplyInput.setVisibility(View.GONE);
            //mLlOperate.setVisibility(View.VISIBLE);
            showBottomView(mLlBottom);
        }

    }

    private View createDanmuItemView(ChatRecord record) {
        View danmuView = mInflater.inflate(R.layout.item_live_danmu, null);
        TextView tvName = (TextView) danmuView.findViewById(R.id.tv_user_name);
        TextView tvMsg = (TextView) danmuView.findViewById(R.id.tv_msg);
        ImageView ivAvatar = (ImageView) danmuView.findViewById(R.id.av_avatar);
        UserInfo userInfo = mUserInfoManager.getUserInfo(record.from);
        if (userInfo != null) {
            tvName.setText(userInfo.getDisplayName(this));
            Picasso.with(this)
                    .load(userInfo.getAvatar())
                    .placeholder(userInfo.defaultAvatar())
                    .into(ivAvatar);
        } else {
            mUserInfoManager.acquireUserInfo(record.from);
        }
        tvMsg.setText(record.text);
        return danmuView;
    }

    private void sendMessage(int msgType) {
        if (TextUtils.isEmpty(mChatTarget)) {
            return;
        }
        String message = mMessageEditor.getText().toString().trim();
        if (Utils.isNetworkAvailable(LivingActivity.this)) {
            if (TextUtils.isEmpty(message)) {
                toast(R.string.rpl_tst_content_empty);
            } else {
                mMessageEditor.setText("");
                mChatHelper.sendText(mChatTarget, message, mGroupId, msgType);
            }
        } else {
            toast(getString(R.string.cht_network_is_not_available));
        }
    }

    private void sendBarrage() {
        String message = mMessageEditor.getText().toString().trim();
        if (Utils.isNetworkAvailable(LivingActivity.this)) {
            if (TextUtils.isEmpty(message)) {
                toast(R.string.rpl_tst_content_empty);
            } else {
                mMessageEditor.setText("");
                new SendBarrage(message).executeLong();
            }
        } else {
            toast(getString(R.string.cht_network_is_not_available));
        }
    }

    @Override
    public void onBackPressed() {

        if (mGiftPicker.isVisible()) {
            hideGiftPickerAnimation();
            return;
        }
        super.onBackPressed();
        exitLive();
        stopPlayer();
        closeConnection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPlayer != null) {
            mPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayContext != null) {
            mPlayContext.destory();
        }
        if (mPlayContext2 != null) {
            mPlayContext2.destory();
        }
        if (mPublisher != null) {
            mPublisher.release();
        }
        mChatHelper.unregisterReceiveListener(this);
        mChatHelper.unregisterSendListener(this);
        mGroupChatManager.unregisterGroupChatListener(this);
        mSoftKeyboardHelper.removeSoftKeyboardStateListener(this);
        mFollowUserManager.unregisterOnUserFollowListener(this);
        ContactUpdateCenter.removeObserver(this);
    }

    private void createOnePlayer(Surface surface) {
        mPlayer = new LetvPlayer();
        mPlayer.setPlayContext(mPlayContext); //关联playContext
        mPlayer.setParameter(mPlayer.getPlayerId(), mBundle);
        mPlayer.init();
        mPlayer.setOnPlayStateListener(this);
        mPlayer.setDisplay(surface);
        mPlayer.prepareAsync();
    }

    private void createSecondPlayer(Surface surface) {
        mPlayer2 = new LetvPlayer();
        mPlayer2.setPlayContext(mPlayContext2); //关联playContext
        mPlayer2.setParameter(mPlayer2.getPlayerId(), mBundle2);
        mPlayer2.init();
        mPlayer2.setOnPlayStateListener(this);
        mPlayer2.setDisplay(surface);
        mPlayer2.prepareAsync();
    }

    private void initNormalVideoView() {
        if (mSurfaceView == null || !(mSurfaceView instanceof ReSurfaceView)) {
            ReSurfaceView videoView = new ReSurfaceView(this);
            videoView.setVideoContainer(null);
            this.mSurfaceView = videoView;
            addVideoView();
        }
    }

    private void addVideoView() {
        mSurfaceView.getHolder().addCallback(this);
        mSurfaceView.setZOrderOnTop(false);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        RelativeLayout videoContainer = (RelativeLayout) findViewById(R.id.videoContainer);
        videoContainer.addView(mSurfaceView, params);
    }

    private void initStreamVideoView() {
        if (mSurfaceView2 == null || !(mSurfaceView2 instanceof ReSurfaceView)) {
            ReSurfaceView videoView = new ReSurfaceView(this);
            videoView.setVideoContainer(null);
            this.mSurfaceView2 = videoView;
            addStreamVideoView();
        }
    }

    private void addStreamVideoView() {
        mSurfaceView2.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (mPlayer2 != null) {
                    mPlayer2.setDisplay(holder.getSurface());
                } else {
                    createSecondPlayer(holder.getSurface());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mPlayer2 != null) {
                    PlayerParamsHelper.setViewSizeChange(mPlayer2, width, height);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                stopConnPlayer();
            }
        });
        mSurfaceView2.setZOrderOnTop(true);
        mSurfaceView2.setZOrderMediaOverlay(true);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        RelativeLayout videoContainer = (RelativeLayout) findViewById(R.id.rl_2nd_container);
        videoContainer.addView(mSurfaceView2, params);
    }

    private void stopConnPlayer() {
        if (mPlayer2 != null) {
            mPlayer2.stop();
            mPlayer2.reset();
            mPlayer2.release();
            mPlayer2 = null;
        }
    }

    private void enterLive() {
        new EnterLiveTask().executeLong();
    }


    private void exitLive() {
        new ExitLiveTask().executeLong();
        DataHelper.deleteChatRecords(LivingActivity.this, mChatTarget);
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mSurfaceView != null && mSurfaceView instanceof ReSurfaceView) {
            /**
             * 当屏幕旋转的时候，videoView需要全屏居中显示, 如果用户使用自己的view显示视频（比如SurfaceView）,
             * 比较简单的方法是：对surfaceView的layourParams()进行设置。 1）竖屏转横屏的时候，可以占满全屏居中显示；
             * 2）横屏转竖屏时，需要设置layoutParams()恢复之前的显示大小
             *
             */
            ((ReSurfaceView) mSurfaceView).setVideoLayout(-1, 0);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mPlayer != null) {
            mPlayer.setDisplay(holder.getSurface());
        } else {
            createOnePlayer(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mPlayer != null) {
            PlayerParamsHelper.setViewSizeChange(mPlayer, width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPlayer();
    }


    @Override
    public void videoState(int state, Bundle bundle) {
        if (mPlayer == null) {
            return;
        }
        switch (state) {
            case ISplayer.MEDIA_EVENT_VIDEO_SIZE:
                if (mSurfaceView != null && mPlayer != null) {
//                    if (mSurfaceView instanceof ReSurfaceView) {
//                        ((ReSurfaceView) mSurfaceView).onVideoSizeChange(mPlayer.getVideoWidth(), mPlayer.getVideoHeight());
//                    }
                }
                break;
            case ISplayer.MEDIA_EVENT_PREPARE_COMPLETE:
                if (mPlayer != null) {
                    mPlayer.start();
                    Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
                    animation.setAnimationListener(new AnimationListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            super.onAnimationEnd(animation);
                            mFlBlur.setVisibility(View.GONE);
                        }
                    });
                    mFlBlur.startAnimation(animation);
                }
                break;
            case EventPlayProxy.PROXY_WATING_SELECT_ACTION_LIVE_PLAY:// 当收到该事件后，用户可以选择优先播放的活动直播
                ActionInfo actionInfo = mPlayContext.getActionInfo();
                // 查找正在播放的直播 或者 可以秒转点播的直播信息
                com.lecloud.entity.LiveInfo liveInfo = actionInfo.getFirstCanPlayLiveInfo();
//                LiveInfo liveInfo = actionInfo.getFirstCanPlayLiveInfo();
                if (liveInfo != null) {
                    mPlayContext.setLiveId(liveInfo.getLiveId());
                }
                break;
            case ISplayer.PLAYER_EVENT_PREPARE_VIDEO_VIEW:
                initNormalVideoView();
                break;
            case ISplayer.MEDIA_EVENT_PLAY_COMPLETE:
                new GetEndInfoTask().executeLong();
                break;
            case EventPlayProxy.PROXY_REQUEST_ERROR:
                break;
            default:
                break;
        }
    }

    private View getLivingEndView() {
        // blur
        if (!TextUtils.isEmpty(mLiveEndInfo.preview)) {
            String fileName = getFIleNameByUrl(mLiveEndInfo.preview);
            if (HttpUtil.downLoad(this, mLiveEndInfo.preview, fileName)) {
                mBgMap = BitmapFactory.decodeFile(fileName);
                blur(mFlBlur);
            } else {
                mBgMap = BitmapFactory.decodeResource(getResources(), R.drawable.splash_an);
                blur(mFlBlur);
            }
        } else if (!TextUtils.isEmpty(mLiveEndInfo.avatar)) {
            String fileName = getFIleNameByUrl(mLiveEndInfo.avatar);
            if (HttpUtil.downLoad(this, mLiveEndInfo.avatar, fileName)) {
                mBgMap = BitmapFactory.decodeFile(fileName);
                blur(mFlBlur);
            } else {
                mBgMap = BitmapFactory.decodeResource(getResources(), R.drawable.splash_an);
                blur(mFlBlur);
            }
        } else {
            mBgMap = BitmapFactory.decodeResource(getResources(), R.drawable.splash_an);
            blur(mFlBlur);
        }
        // blur end
        View view = mInflater.inflate(R.layout.item_living_end, null);
        TextView mTvAudienceNum = (TextView) view.findViewById(R.id.tv_audience_num);
        TextView mTvTimeLong = (TextView) view.findViewById(R.id.tv_time_long);
        ImageView mIvBackTop = (ImageView) view.findViewById(R.id.iv_back_top);
        mTvFollowHost = (TextView) view.findViewById(R.id.tv_follow_host);
        mIvFriendCircle = (ImageView) view.findViewById(R.id.iv_weichat_friend);
        mIvWeichat = (ImageView) view.findViewById(R.id.iv_weichat);
        mIvWeibo = (ImageView) view.findViewById(R.id.iv_weibo);
        mIvQQ = (ImageView) view.findViewById(R.id.iv_qq);
        mIvQQZone = (ImageView) view.findViewById(R.id.iv_qq_zone);
        mIvFriendCircle.setOnClickListener(LivingActivity.this);
        mIvWeichat.setOnClickListener(LivingActivity.this);
        mIvWeibo.setOnClickListener(LivingActivity.this);
        mIvQQ.setOnClickListener(LivingActivity.this);
        mIvQQZone.setOnClickListener(LivingActivity.this);
        if (isFollow) {
            mTvFollowHost.setText(R.string.tribe_is_followed);
        } else {
            mTvFollowHost.setText(R.string.tribe_concern);
        }
        mIvBackTop.setOnClickListener(this);
        mTvFollowHost.setOnClickListener(this);
        mTvAudienceNum.setText(Utils.getColoredText(getString(R.string.living_spectator_num,
                mLiveEndInfo.spectatorNum + ""), mLiveEndInfo.spectatorNum + "", 0XFF4DE8B3));
        mTvTimeLong.setText(getString(R.string.living_time_long, mLiveEndInfo.time));
        return view;
    }


    private void stopPlayer() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
        if (mPlayer2 != null) {
            mPlayer2.stop();
            mPlayer2.reset();
            mPlayer2.release();
            mPlayer2 = null;
        }
    }

    @Override
    public void onGroupChatSuccess(int type, Object obj) {
        switch (type) {
            case GroupChatManager.ADD_USER:
                break;
            default:
                break;
        }
    }

    @Override
    public void onGroupChatFailed(int type) {

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (lastVisibleItem != firstVisibleItem) {
            long nowTime = System.currentTimeMillis();
            lastScrollTime = nowTime;
            lastVisibleItem = firstVisibleItem;
            int[] location = new int[2];
            if (firstVisibleItem != mListViewFirstItem) {
                if (firstVisibleItem > mListViewFirstItem) {
                    Utils.logD(TAG, "向上滑动");
                    if (firstVisibleItem + visibleItemCount < mAdapter.getCount() - 1) {
                        mNeedScrollLastPostion = false;
                    } else {
                        mNeedScrollLastPostion = true;
                    }
                } else {
                    Utils.logD(TAG, "向下滑动");
                    if (firstVisibleItem - visibleItemCount < mAdapter.getCount() - 1) {
                        mNeedScrollLastPostion = false;
                    } else {
                        mNeedScrollLastPostion = true;
                    }
                }
                mListViewFirstItem = firstVisibleItem;
                mScreenY = location[1];
            } else {
                if (mScreenY > location[1]) {
                    Utils.logD(TAG, "向上滑动");
                    if (firstVisibleItem + visibleItemCount < mAdapter.getCount() - 1) {
                        mNeedScrollLastPostion = false;
                    } else {
                        mNeedScrollLastPostion = true;
                    }
                } else if (mScreenY < location[1]) {
                    Utils.logD(TAG, "向下滑动");
                    if (firstVisibleItem - visibleItemCount < mAdapter.getCount() - 1) {
                        mNeedScrollLastPostion = false;
                    } else {
                        mNeedScrollLastPostion = true;
                    }
                }
                mScreenY = location[1];
            }
            mAdapter.setLastViewIsVisible(mNeedScrollLastPostion);
        }
    }

    @Override
    public void onGiftClick(LiveGift gift) {
        if (gift.type != LiveGift.ANIM_TYPE_NORMAL) {
            hideGiftPickerAnimation();
        } else {
            if (mChoosedGift != null) {
                if (mChoosedGift.giftId == gift.giftId) {
                    mGiftCount++;
                    // 连送
                    GiftControllerInfo giftAnimView = createGiftAnimView(gift);
                    giftAnimView.giftCount = mGiftCount;
                    mGiftAnimView.addGift(giftAnimView);
                } else {
                    // 发送之前的礼物，并添加新的礼物动画
                    sendGift(gift.giftId, mGiftCount);
                    mChoosedGift = gift;
                    mGiftAnimView.removeGiftCountInfo(gift, mUserInfo.userId);
                    mGiftCount = 1;
                    GiftControllerInfo giftAnimView = createGiftAnimView(gift);
                    mGiftAnimView.addGift(giftAnimView);
                }
            } else {
                mGiftCount = 1;
                mChoosedGift = gift;
                // 发送礼物，添加礼物动画
                GiftControllerInfo giftAnimView = createGiftAnimView(gift);
                mGiftAnimView.addGift(giftAnimView);
            }
            startCount();
        }
    }

    private void startCount() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            mTimerTask = null;
        }
        mTimerCount = 10;
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mTimerCount--;
                if (mTimerCount <= 0) {
                    stopCount();
                }
                mHandler.sendMessage(Message.obtain());
            }
        };
        mTimer.schedule(mTimerTask, 0, 1000);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mTvTimerCount.setText(mTimerCount + "");
            if (mTimerCount == 0) {
                mTvTimerCount.setVisibility(View.GONE);
            } else {
                mTvTimerCount.setVisibility(View.VISIBLE);
            }
        }
    };

    private void stopCount() {
        if (mTimer == null) {
            return;
        }
        mTimer.cancel();
        mTimer = null;
        mTimerTask = null;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGiftPicker.hideCountView();
                mTvTimerCount.setVisibility(View.GONE);
            }
        });
        sendGift(mChoosedGift.giftId, mGiftCount);
        mGiftAnimView.removeGiftCountInfo(mChoosedGift, mUserInfo.userId);
        mGiftCount = 0;
    }

    private void blur(View view) {
        float scaleFactor = 4;
        if (mBgMap == null) {
            return;
        }
        Bitmap overlay = Bitmap.createBitmap((int) (mBgMap.getWidth() / scaleFactor),
                (int) (mBgMap.getHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft() / scaleFactor, -view.getTop() / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(mBgMap, 0, 0, paint);
        StackBlurManager manager = new StackBlurManager(overlay);

        Bitmap bitmap = manager.process(20);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(new BitmapDrawable(getResources(), bitmap));
        } else {
            view.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
        }

    }

    private GiftControllerInfo createGiftAnimView(LiveGift gift) {
        return createGiftAnimView(gift, 0);
    }

    private GiftControllerInfo createGiftAnimView(LiveGift gift, int userId) {
        if (gift == null) {
            return null;
        }
        UserInfo userInfo = mUserInfo;
        if (userId != 0) {
            mUserInfo = mUserInfoManager.getUserInfo(userId);
        }
        GiftControllerInfo info = new GiftControllerInfo();
        View view = mInflater.inflate(R.layout.view_gift_anim_layout, null);
        ImageView ivAvatar = (ImageView) view.findViewById(R.id.av_avatar);
        ImageView ivGift = (ImageView) view.findViewById(R.id.iv_gift);
        TextView tvCount = (TextView) view.findViewById(R.id.tv_gift_count);
        TextView tvUser = (TextView) view.findViewById(R.id.tv_user_name);
        TextView tvGift = (TextView) view.findViewById(R.id.tv_gift_name);
        Picasso.with(LivingActivity.this).load(mUserInfo.getAvatar())
                .placeholder(mUserInfo.defaultAvatar())
                .into(ivAvatar);
        Picasso.with(LivingActivity.this).load(gift.icon).into(ivGift);
        if (userInfo != null) {
            tvUser.setText(userInfo.getDisplayName(LivingActivity.this));
        } else {
            tvUser.setText("一个朋友");
        }
        CharSequence content = getString(R.string.gift_desc, gift.name);
        CharSequence coloredText = Utils.getColoredText(content, gift.name, 0xffff73ae, false);
        tvGift.setText(coloredText);
        info.allView = view;
        info.allView = view;
        info.giftView = ivGift;
        info.countView = tvCount;
        info.giftId = gift.giftId;
        info.userId = userId == 0 ? mUserInfo.userId : userId;
        info.giftCount = mShowGiftNum;
        return info;
    }

    @Override
    public void onSoftKeyboardOpened(int keyboardHeightInPx) {

    }

    /**
     * 软键盘关闭时调用
     **/
    @Override
    public void onSoftKeyboardClosed() {
        toggleInputAndOperate(false);

    }

    @Override
    public void onPulllefttoRefresh() {
    }

    private void sendGift(int giftId, int count) {
        new SendGiftTask(giftId, count).executeLong();
    }

    @Override
    public void onMenuClick() {

    }

    @Override
    public void onConnectionRequest(PushMessage message) {
        // 弹框提示有连线请求
    }

    @Override
    public void onSetFps(int i, List<int[]> list, CameraParams cameraParams) {

    }

    @Override
    public void onSetPreviewSize(int i, List<Camera.Size> previewSizes, CameraParams cameraParams) {
        float ratio = Utils.getDisplayHeight() / Utils.getDisplayWidth();
        Camera.Size s = null;
        for (Camera.Size size : previewSizes) {
            Utils.logD(TAG, "可选择选择录制的视频有：宽为:" + size.width + ",高：" + size.height + "；比例：" + ((float) size.width) / size.height);
            if (size.width >= 640 && size.width <= 1000) { //  选择比较低的分辨率，保证推流不卡
                if ((float) (size.width / size.height) == ratio) {
                    s = size;
                }
            }
        }
        cameraParams.setWidth(s.width);
        cameraParams.setHeight(s.height);
        Utils.logD(TAG, "选择录制的视频宽为:" + s.width + ",高：" + s.height);
    }

    @Override
    public void onContactsUpdated(ContactUpdateCenter.UpdateType type, Object data) {
        switch (type) {
            case UserInfo:
                System.out.println("--------------------收到消息提示");
                UserInfo user = mUserInfoManager.getUserInfo(mCurrentUserId);
                if (user != null) {
                    mCurrentUser = user;
                    new GetCurrentUserAdminStatus().executeLong();
                }
                break;
            default:
                break;
        }
    }


    private class EnterLiveTask extends MsTask {

        public EnterLiveTask() {
            super(LivingActivity.this, MsRequest.ENTER_LIVE);
        }

        @Override
        protected String buildParams() {
            return "live_id=" + mLiveInfo.id;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mGroupId = response.getJsonObject().optString("gid");
                mChatId = response.getJsonObject().optString("chat_id");
                int status = response.getJsonObject().optInt("live_status");
                if (status == 0) {
                    exitLive();
                    return;
                }
                // start group chat
                createTarget();
                mGroupChatManager.addUsers(mGroupId, mUserInfo.userId + "");
                // get online_activity_ids
                ArrayList<String> activityIds = JsonUtil.getStringArray(
                        response.getJsonObject().optJSONArray("activities"));
                if (activityIds != null && activityIds.size() > 0) {
                    mConnetionActivityId = activityIds.get(0);
                    startStreamCheck();
                }
            }
        }
    }


    private class ExitLiveTask extends MsTask {

        public ExitLiveTask() {
            super(LivingActivity.this, MsRequest.EXIT_LIVE);
        }

        @Override
        protected String buildParams() {
            return "live_id=" + mLiveInfo.id;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
            }
        }
    }

    private class SendGiftTask extends MsTask {

        private int mGiftId;
        private int mGiftNumber;

        public SendGiftTask(int giftId, int count) {
            super(LivingActivity.this, MsRequest.LIVE_SEND_GIFT);
            mGiftId = giftId;
            mGiftNumber = count;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder()
                    .append("receive_uid=").append(mLiveInfo.uid)
                    .append("&gift_id=").append(mGiftId)
                    .append("&number=").append(mGiftNumber)
                    .append("&live_id=").append(mLiveInfo.id)
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (!response.isSuccessful()) {
                if (response.code == MsResponse.MS_FAIL_TRADE_PRICE_NOT_ENOUGH) {
                    showMoneyDialog();
                }
            }
        }
    }

    private class SendBarrage extends MsTask {
        String message;

        public SendBarrage(String msg) {
            super(LivingActivity.this, MsRequest.BARRAGE_DEDUCT_GOLD);
            message = msg;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder()
                    .append("receive_message=").append(message)
                    .append("&live_id=").append(mLiveInfo.id)
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {

            } else {

            }
        }
    }


    private class CloseConnectionTask extends MsTask {

        public CloseConnectionTask() {
            super(LivingActivity.this, MsRequest.LIVE_CONNECTION_CLOSE);
        }

        @Override
        protected String buildParams() {
            return "connection_id=" + mConnectionId;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                closeConnectionWindow();
            }
        }
    }

    private void closeConnection() {
        new CloseConnectionTask().executeLong();
    }

    private void closeConnectionWindow() {
        mFlConnectionContainer.setVisibility(View.GONE);
        mFl2ndContainer.setVisibility(View.GONE);
        stopPlayer();
    }

    @Override
    public void onMessageReceived(ChatRecord record) {
        if (record.isTarget(mChatTarget)) {
            operateChatRecord(record);
        }
    }

    private void operateChatRecord(final ChatRecord record) {
        switch (record.type) {
            case ChatRecord.CHAT_TYPE_DANMU_MSG:
                Utils.logD(TAG, "get danmu message");
                mDanmuView.addDanmuItemView(createDanmuItemView(record));
                break;
            case ChatRecord.CHAT_TYPE_CHAT_MSG:
                System.out.println("---------------收到消息");
                if (mBlackList == null) {
                    new GetBlackList(true, record).executeLong();
                } else {
                    boolean isInBlack = false;
                    for (int i = 0; i < mBlackList.size(); i++) {
                        if (mBlackList.get(i).intValue() == mUserInfoManager.getUserInfo(record.from).userId) {
                            isInBlack = true;
                        }
                    }
                    if (!isInBlack) {
                        mAdapter.add(record);
                    }
                }
                break;
            case ChatRecord.CHAT_TYPE_NORMAL_GIFT:
                LiveGift giftInfo = mGiftPicker.getLiveGiftInfo(record.giftId);
                mTotalIncome = mTotalIncome + giftInfo.price;
                mTvIncome.setText(mTotalIncome + "");
                mAdapter.add(record);
                GiftControllerInfo giftAnimView = createGiftAnimView(giftInfo);
                if (giftAnimView.userId == mUserInfo.userId) {
                    return;
                }
                mGiftAnimView.addGift(giftAnimView);
                break;
            case ChatRecord.CHAT_TYPE_SPECIAL_GIFT:
                giftInfo = mGiftPicker.getLiveGiftInfo(record.giftId);
                mTotalIncome = mTotalIncome + giftInfo.price;
                mTvIncome.setText(mTotalIncome + "");
                showSpecialGiftAnim();
                if (record.animType == ChatRecord.MSG_ANIM_TYPE_FIRE) {

                } else if (record.animType == ChatRecord.MSG_ANIM_TYPE_SPECIAL) {

                }
                mAdapter.add(record);
                break;
            case ChatRecord.CHAT_TYPE_FOLLOW_MSG:
                if (mTvFollowHost != null) {
                    mTvFollowHost.setText(R.string.tribe_is_followed);
                }
                fetchLiveMembersList(true);
                isFollow = true;
                mAdapter.add(record);
                break;
            case ChatRecord.CHAT_TYPE_PRAISE_MSG:
                mHeartLayout.addHeart();
                break;
            case ChatRecord.CHAT_TYPE_SHARE_MSG:
                mAdapter.add(record);
                break;
            case ChatRecord.CHAT_TYPE_EXIT_MSG:
                fetchLiveMembersList(true);
                break;
            case ChatRecord.CHAT_TYPE_COMEIN_MSG:
                fetchLiveMembersList(true);
                mAdapter.add(record);
                break;
            case ChatRecord.CHAT_TYPE_CLOSE_CONNECTION:
                Utils.logD(TAG, "关闭直播连线");
                // 关闭连线窗口UI
                break;
            case ChatRecord.CHAT_TYPE_CONNECTION_SUCCESS:
                Utils.logD(TAG, "收到连线成功消息");
//                mConnetionActivityId = record.activityId;
//                Utils.logD(TAG, "from : " + record.from + "  - account:" + mUserInfo.account);
//                if (!mUserInfo.account.toLowerCase().equals(StringUtils.parseName(record.from).toLowerCase())) {
//                    startStreamCheck();
//                } else {
//                    createConnectionView();
//                }
                break;
            case ChatRecord.CHAT_TYPE_SET_MAGEMENT:
                mAdapter.add(record);
                mStatusInfo.isAdmin = true;
                break;
            case ChatRecord.CHAT_TYPE_CANCEL_SET_MAGEMENT:
                mAdapter.add(record);
                mStatusInfo.isAdmin = false;
                break;
            case ChatRecord.CHAT_TYPE_SILIENCE:
                isMeSilence = true;
                mAdapter.add(record);
                break;
            case ChatRecord.CHAT_TYPE_CANCEL_SILENCE:
                isMeSilence = false;
                mAdapter.add(record);
                break;
            default:
                break;
        }
    }

    private void startPullStream() {
        mFl2ndContainer.setVisibility(View.VISIBLE);
//        mLlWaitingUI.setVisibility(View.VISIBLE);
        mBundle2 = new Bundle();
        mBundle2.putInt(PlayProxy.PLAY_MODE, EventPlayProxy.PLAYER_ACTION_LIVE);
        mBundle2.putString(PlayProxy.PLAY_ACTIONID, mConnetionActivityId);
        mBundle2.putBoolean(PlayProxy.PLAY_USEHLS, false);
        mPlayContext2 = new PlayContext(this);
        mPlayContext2.setUsePlayerProxy(true);
        createSecondPlayer(null);
        mPlayer2.setParameter(mPlayer2.getPlayerId(), mBundle2);
        mPlayer2.setOnPlayStateListener(mPlayStaeListener);
    }

    private OnPlayStateListener mPlayStaeListener = new OnPlayStateListener() {
        @Override
        public void videoState(int state, Bundle bundle) {
            if (mPlayer2 == null) {
                return;
            }
            switch (state) {
                case ISplayer.MEDIA_EVENT_VIDEO_SIZE:
                    if (mSurfaceView2 != null && mPlayer2 != null) {
                    }
                    break;
                case ISplayer.MEDIA_EVENT_PREPARE_COMPLETE:
                    if (mPlayer2 != null) {
                        mPlayer2.start();
                        mFlConnectionContainer.setVisibility(View.VISIBLE);
                    }
                    break;
                case EventPlayProxy.PROXY_WATING_SELECT_ACTION_LIVE_PLAY:// 当收到该事件后，用户可以选择优先播放的活动直播
                    ActionInfo actionInfo = mPlayContext.getActionInfo();
                    // 查找正在播放的直播 或者 可以秒转点播的直播信息
                    com.lecloud.entity.LiveInfo liveInfo = actionInfo.getFirstCanPlayLiveInfo();
                    if (liveInfo != null) {
                        mPlayContext2.setLiveId(liveInfo.getLiveId());
                    }
                    break;
                case ISplayer.PLAYER_EVENT_PREPARE_VIDEO_VIEW:
                    initStreamVideoView();
                    break;
                case EventPlayProxy.PROXY_REQUEST_ERROR:
                case ISplayer.MEDIA_EVENT_PLAY_COMPLETE:
                    startStreamCheck();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onMessageReceiveFailed(ChatRecord record) {

    }

    @Override
    public void onMessagePreSend(ChatRecord record) {
    }

    @Override
    public void onMessageSent(final ChatRecord record) {
        if (record.isTarget(mChatTarget)) {
            if (record.type == ChatRecord.CHAT_TYPE_CHAT_MSG) {
                mAdapter.add(record);
            }
        }
    }

    @Override
    public void onMessageSendFailed(ChatRecord record) {
    }

    private class HeadAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mFocusUsers.size();
        }

        @Override
        public LiveMemberInfo getItem(int position) {
            return mFocusUsers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_live_user, parent, false);
            }
            final LiveMemberInfo user = getItem(position);
            ImageView avatar = (ImageView) convertView.findViewById(R.id.av_avatar);
            if (user.avatar != null && !"".equals(user.avatar)) {
                Picasso.with(LivingActivity.this)
                        .load(user.avatar)
                        .into(avatar);
            } else {
                Picasso.with(LivingActivity.this)
                        .load(user.gender == 0 ? R.drawable.chat_botton_bg_favicongirl : R.drawable.chat_botton_bg_faviconboy)
                        .into(avatar);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCurrentUserId = user.id;

                    UserInfo userInfo = mUserInfoManager.getUserInfo(user.id);
                    if (userInfo != null) {
                        mCurrentUser = userInfo;
                        new GetCurrentUserAdminStatus().executeLong();
                    }
                    hideAllView();
                    showBottomView(frView);
                }
            });
            return convertView;
        }
    }

    private class GetIncomeTask extends MsTask {

        public GetIncomeTask() {
            super(LivingActivity.this, MsRequest.LIVE_INCOME);
        }

        @Override
        protected String buildParams() {

            return new StringBuilder("host_uid=").append(mLiveInfo.uid).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONObject json = response.getJsonObject();
                mTotalIncome = json.optInt("total_income");
                mTodayIncome = json.optInt("today_income");
                mTvIncome.setText(mTotalIncome + "");
            }
        }
    }

    private class FollowHost extends MsTask {

        public FollowHost() {
            super(LivingActivity.this, isFollow ? MsRequest.CANCEL_HOST : MsRequest.FOLLOW_HOST);
        }

        @Override
        protected String buildParams() {

            return new StringBuilder("follow_uid=").append(mLiveInfo.uid).append("&live_id=").append(mLiveInfo.id).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mIvFollow.setVisibility(View.GONE);
                if (isFollow) {
                    mIvFollow.setVisibility(View.INVISIBLE);
                    if (mTvFollowHost != null) {
                        mTvFollowHost.setText(R.string.tribe_concern);
                    }
                    fetchLiveMembersList(true);
                    isFollow = false;
                }
            }
        }
    }

    private class GetAudienceListTask extends MsTask {
        int mOffset;

        public GetAudienceListTask(int offset) {
            super(LivingActivity.this, MsRequest.FIND_AUDIENCE_LIST);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).append("&live_id=").append(mLiveInfo.id).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ArrayList<LiveMemberInfo> members = JsonUtil.getArray(response.getJsonArray(), LiveMemberInfo.TRANSFORMER);
                if (mOffset <= 0) {
                    mFocusUsers.clear();
                }
                mFocusUsers.addAll(members);
                mHeadAdapter.notifyDataSetChanged();
            }
        }
    }

    private void fetchLiveMembersList(boolean isRefresh) {
        int offset = isRefresh ? 0 : mHeadAdapter.getCount();
        new GetAudienceListTask(offset).executeLong();
    }

    private class GetHostInfoTask extends MsTask {

        public GetHostInfoTask() {
            super(LivingActivity.this, MsRequest.GET_HOST_USER_INFO);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("host_uid=").append(mLiveInfo.uid).toString();
        }


        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONObject json = response.getJsonObject();
                mHostUserInfo = HostUserInfo.fromJson(json);
                if (mHostUserInfo.avatar != null && !"".equals(mHostUserInfo.avatar)) {
                    Picasso.with(LivingActivity.this).load(mHostUserInfo.avatar).into(mIvAvatar);
                } else {
                    Picasso.with(LivingActivity.this).load(mHostUserInfo.gender == 0 ?
                            R.drawable.chat_botton_bg_favicongirl : R.drawable.chat_botton_bg_faviconboy)
                            .into(mIvAvatar);
                }
                mTvName.setText(mHostUserInfo.nickName);
                isFollow = mHostUserInfo.isFollow;
                if (isFollow) {
                    mIvFollow.setVisibility(View.INVISIBLE);
                } else {
                    mIvFollow.setVisibility(View.VISIBLE);
                }

            }
        }
    }

    private class PrimaseHostTask extends MsTask {

        public PrimaseHostTask() {
            super(LivingActivity.this, MsRequest.PRAISE_HOST);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("live_id=").append(mLiveInfo.id).toString();
        }

    }

    private class GetEndInfoTask extends MsTask {

        public GetEndInfoTask() {
            super(LivingActivity.this, MsRequest.GET_LIVE_END_INFO);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("live_id=").append(mLiveInfo.id).toString();
        }


        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONObject json = response.getJsonObject();
                mLiveEndInfo = new LiveEndInfo();
                mLiveEndInfo.spectatorNum = json.optInt("menNumbers");
                mLiveEndInfo.time = json.optString("time");
                mLiveEndInfo.income = json.optInt("income");
                mLiveEndInfo.preview = json.optString("prev_url");
                mLiveEndInfo.avatar = json.optString("avatar");
                mFlBlur.setVisibility(View.VISIBLE);
                mFlBlur.addView(getLivingEndView());
            }
        }
    }

    class LiveEndInfo {
        public int spectatorNum;
        public String time;
        public int income;
        public String preview;
        public String avatar;

    }

    public class ViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {                                                                 //获得size
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(View view, int position, Object object)                       //销毁Item
        {
            ((ViewPager) view).removeView(mViewList.get(position));
        }

        @Override
        public Object instantiateItem(View view, int position)                                //实例化Item
        {
            View itemView = mViewList.get(position);
            ((ViewPager) view).addView(itemView, 0);
            if (position == 1) {
                mHeartLayout = (HeartLayout) itemView.findViewById(R.id.bessal_view);
                mTvSendDm = (TextView) itemView.findViewById(R.id.tv_send_dm);
                mTvSend = (TextView) itemView.findViewById(R.id.tv_send);
                mMessageEditor = (RichMlEditText) itemView.findViewById(R.id.et_msg);
                mListView = (ListView) itemView.findViewById(R.id.list_live_msg);
                mDanmuView = (XDanmuView) itemView.findViewById(R.id.danmu_rl);
                mLlReplyInput = (LinearLayout) itemView.findViewById(R.id.ll_reply);
                mLlBottom = (LinearLayout) itemView.findViewById(R.id.ll_operate);
                mCbDanmu = (CheckBox) itemView.findViewById(R.id.cb_send_dm);
                mIvAvatar = (ImageView) itemView.findViewById(R.id.iv_avatar);
                mTvName = (TextView) itemView.findViewById(R.id.tv_name);
                mGridView = (TwoWayGridView) itemView.findViewById(R.id.follow_gridview);
                mTvDateDay = (TextView) itemView.findViewById(R.id.tv_date_today);
                mTvIncome = (TextView) itemView.findViewById(R.id.tv_income);
                mLlIncome = (LinearLayout) itemView.findViewById(R.id.ll_income);
                mGiftPicker = (LiveGiftPicker) itemView.findViewById(R.id.gift_picker);
                mGiftAnimView = (GiftAnimView) itemView.findViewById(R.id.gift_anim_view);
                mTvMyGold = (TextView) itemView.findViewById(R.id.tv_my_gold);
                mLlConnection = (LinearLayout) itemView.findViewById(R.id.ll_connection);
                mTvConnection = (TextView) itemView.findViewById(R.id.tv_connection);
                mLlOperate = (RelativeLayout) itemView.findViewById(R.id.rl_parent);
                mTvManager = (TextView) itemView.findViewById(R.id.tv_manager);
                mMenuList = (ListView) itemView.findViewById(R.id.lv_menu);
                mTvFollow = (TextView) itemView.findViewById(R.id.tv_follow_user);
                mTvtoProfile = (TextView) itemView.findViewById(R.id.tv_profile);
                mIvFollow = (ImageView) itemView.findViewById(R.id.iv_living_follow);
                mTvFansNum = (TextView) itemView.findViewById(R.id.tv_fans_num);
                mTvFollowNum = (TextView) itemView.findViewById(R.id.tv_follow_num);
                mTvContributeNum = (TextView) itemView.findViewById(R.id.tv_contribute_num);
                mTvUserName = (TextView) itemView.findViewById(R.id.tv_username);
                mTvSchool = (TextView) itemView.findViewById(R.id.tv_user_school);
                mTvUserContent = (TextView) itemView.findViewById(R.id.user);
                mTvManageMent = (TextView) itemView.findViewById(R.id.tv_managment);
                mIvUserGerden = (ImageView) itemView.findViewById(R.id.iv_gerden);
                mIvAvatar = (AvatarView) itemView.findViewById(R.id.av_user_avatar);
                mIvUserType = (ImageView) itemView.findViewById(R.id.iv_type_icon);
                mLlButton = (LinearLayout) itemView.findViewById(R.id.ll_follow_profile);
                mGvShare = (GridView) itemView.findViewById(R.id.gv_share);

                mTvtoProfile.setOnClickListener(LivingActivity.this);

                mTvFollow.setOnClickListener(LivingActivity.this);
                mIvFollow.setOnClickListener(LivingActivity.this);
                mTvConnection.setOnClickListener(LivingActivity.this);
                mTvManager.setOnClickListener(LivingActivity.this);
                frView = (FrameLayout) itemView.findViewById(R.id.fr_userinformation_botoom);
//                fruserView = (FrameLayout) itemView.findViewById(R.id.fr_user_botoom);

                mTvTimerCount = mGiftPicker.getTimerCountView();

                mAdapter = new LiveChatAdapter(LivingActivity.this);
                mListView.setAdapter(mAdapter);
                mListView.setOnScrollListener(LivingActivity.this);
                mLlIncome.setOnClickListener(LivingActivity.this);
                mListView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN)
                            toggleInputAndOperate(false);
                        return false;
                    }
                });
                showBasicInfo();
                mHeadAdapter = new HeadAdapter();
                mGridView.setOnScrollListener(new TwoWayAbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(TwoWayAbsListView view, int scrollState) {
                        if (scrollState == SCROLL_STATE_IDLE) {
                            fetchLiveMembersList(false);
                        }
                    }

                    @Override
                    public void onScroll(TwoWayAbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                    }
                });
                mGridView.setAdapter(mHeadAdapter);
                mGiftPicker.fetchLiveGift();
                mGiftPicker.registerMenuClickListner(LivingActivity.this);
                updateView();

            } else {// 清屏界面

            }
            return itemView;
        }
    }

    private void showConnectionView() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.live_up_animation);
        mLlConnection.startAnimation(animation);
        mLlConnection.setVisibility(View.VISIBLE);
        isConnectionVisible = true;
        hideBottomView(mLlBottom);
    }

    private void hideConnectionView() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.live_down_animation);
        mLlConnection.startAnimation(animation);
        mLlConnection.setVisibility(View.GONE);
        isConnectionVisible = false;
        showButtonParmAnimation();
    }

    private class SendConnectionTask extends MsTask {

        public SendConnectionTask() {
            super(LivingActivity.this, MsRequest.SEND_CONNECTION_REQUEST);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("host_uid=").append(mLiveInfo.uid).append("&live_id=").append(mLiveInfo.id).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mTvConnection.setClickable(true);
            if (response.isSuccessful()) {
                mConnectionId = response.getJsonObject().optInt("connection_id");
                mConnectionHandle.sendEmptyMessage(0);
            } else {
                mConnectionHandle.sendEmptyMessage(1);
            }
        }
    }


    private class CancleConnectionTask extends MsTask {

        public CancleConnectionTask() {
            super(LivingActivity.this, MsRequest.LIVE_CANCLE_CONNECTION_REQUEST);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("connected_uid=").append(mLiveInfo.uid).append("&live_id=").append(mLiveInfo.id).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mTvConnection.setClickable(true);
            if (response.isSuccessful()) {
                mConnectionHandle.sendEmptyMessage(2);
            }
        }
    }

    private Handler mConnectionHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0://申请连线成功
                    isCRSuc = true;
                    mTvConnection.setText("取消连线");
                    break;
                case 1:// 申请连线失败
                    isCRSuc = false;
                    mTvConnection.setText("申请连线");
                    break;
                case 2:// 取消连线成功
                    isCRSuc = false;
                    mTvConnection.setText("申请连线");
                    break;
                default:
                    break;
            }
        }
    };

    private void createConnectionView() {
        mFl2ndContainer.setVisibility(View.VISIBLE);
        mSurfaceView2 = new SurfaceView(this);
        mSurfaceView2.setZOrderOnTop(true);
        mSurfaceView2.setZOrderMediaOverlay(true);
        mFl2ndContainer.addView(mSurfaceView2);
        // start a new publisher
        mLeUid = Utils.getLeUid();
        mLeSecretKey = Utils.getLeSecretKey();
        Utils.logD(TAG, "get activity_id:" + mConnetionActivityId);
        LetvPublisher.init(mConnetionActivityId, mLeUid, mLeSecretKey);
        mPublisher = LetvPublisher.getInstance();
        mPublisher.initPublisher(LivingActivity.this);
        mPublisher.getRecorderContext().setUseLanscape(false);
        mPublisher.setCameraView(mSurfaceView2);
        Utils.logD(TAG, "publishCreateView添加SurfaceView 成功");
        mSurfaceView2.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (mPublisher == null || mPublisher.isRecording()) {
                    return;
                }
                Utils.logD(TAG, "surface created success");
                if (mPublisher.getVideoRecordDevice() != null) {
                    Utils.logD(TAG, "get useful devices succ");
                    mPublisher.getVideoRecordDevice().setVideoRecorderDeviceListener(LivingActivity.this);
                    //获取摄像头实例，并且给视频绑定surfaceHolder，必须保证surfaceHodler可用
                    mPublisher.getVideoRecordDevice().bindingSurface(holder);
                    //开启摄像头预览
                    mPublisher.getVideoRecordDevice().start();
                    //获取机位信息，***必须在开启预览之后才能获取*** infoCallback 是请求机位回调
                    mPublisher.handleMachine(mInfoCallback);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mPublisher.getVideoRecordDevice() != null) {
                    //销毁SurfaceView的同时关闭摄像头预览
                    mPublisher.getVideoRecordDevice().stop();
                }
            }
        });

    }


    private class GetStreamStatus extends MsTask {

        private String activityId;

        public GetStreamStatus(String actId) {
            super(LivingActivity.this, MsRequest.ACTIVITY_STREAM_STATUS);
            activityId = actId;
        }

        @Override
        protected String buildParams() {
            return "activity_id=" + activityId;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                int status = response.getJsonObject().optInt("status");
                Utils.logD("Live", "status = " + status + "--- json= " + response.getJsonObject()
                        .toString());
                if (status == 0) { // 无信号
                    if (activityId.equals(mLiveInfo.activityId)) {
                        showNoticeInfo();
                    }
                } else {
                    if (activityId.equals(mConnetionActivityId)) {
                        stopStreamCheck();
                        startPullStream();
                    }
                }
            }
        }
    }

    private void showNoticeInfo() {
        View view = mInflater.inflate(R.layout.item_living_no_single, null);
        TextView tvExit = (TextView) view.findViewById(R.id.tv_exit);
        tvExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitLive();
                finish();
            }
        });
        mFlBlur.addView(view);
        mFlBlur.setVisibility(View.VISIBLE);
    }

    private LetvRecorderCallback<ArrayList<LivesInfo>> mInfoCallback =
            new LetvRecorderCallback<ArrayList<LivesInfo>>() {
                @Override
                public void onFailed(int i, String s) {
                    Utils.logD(TAG, "获取机位信息失败:" + s);
                }

                @Override
                public void onSucess(ArrayList<LivesInfo> livesInfos) {
                    Utils.logD(TAG, System.currentTimeMillis() + "succ");
                    //获取机位信息成功后，选中第一个机位
                    mPublisher.selectMachine(0);
                    //开始推流
                    mPublisher.publish();
                }
            };

    private class MenuAdapter extends BaseAdapter {

        private ArrayList<LivingMenuInfo> mMenus = new ArrayList<>();

        public MenuAdapter(ArrayList<LivingMenuInfo> menuList) {
            if (menuList == null) {
                return;
            }
            mMenus = menuList;
        }

        @Override
        public int getCount() {
            return mMenus.size();
        }

        @Override
        public LivingMenuInfo getItem(int i) {
            return mMenus.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_menu_info, parent, false);
            }
            TextView mTvMenuContent = (TextView) convertView.findViewById(R.id.tv_menu_count);
            LivingMenuInfo info = getItem(position);
            mTvMenuContent.setText(getString(info.menuContent));
            convertView.setTag(info.type);
            convertView.setOnClickListener(mMunuClickListen);
            return convertView;
        }
    }

    private View.OnClickListener mMunuClickListen = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mMenuList.setVisibility(View.GONE);
            switch ((int) view.getTag()) {
                case LivingMenuInfo.MENU_TYPE_BLACK:
                    showBlackDialog();
                    break;
                case LivingMenuInfo.MENU_TYPE_CANCEL_BLACK:
                    new CancelBlackTask().executeLong();
                    break;
                case LivingMenuInfo.MENU_TYPE_REPORT:
                    mReportId = mCurrentUserId;
                   showReportDialog();
                    break;
                case LivingMenuInfo.MENU_TYPE_SHOW_MANAGER:
                    break;
                case LivingMenuInfo.MENU_TYPE_CANCEL:
                    mMenuList.setVisibility(View.GONE);
                    break;
                case LivingMenuInfo.MENU_TYPE_Gag:
                    showSilenceDialog();
                    break;
                case LivingMenuInfo.MENU_TYPE_CANCEL_Gag:
                    new CancelShutupTask().executeLong();
                    break;
                default:
                    break;
            }
        }
    };

    private class GetCurrentUserAdminStatus extends MsTask {

        public GetCurrentUserAdminStatus() {
            super(LivingActivity.this, MsRequest.LIVE_GET_ADMIN_STATUS);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("live_id=").append(mLiveInfo.id).append("&query_uid=").append(mCurrentUser.userId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mCurrentStatusInfo = LiveAdminStatusInfo.fromJson(response.getJsonObject());
                showBottomView(frView);
            }
        }
    }

    private class GetMyAdminStatus extends MsTask {

        public GetMyAdminStatus() {
            super(LivingActivity.this, MsRequest.LIVE_GET_ADMIN_STATUS);
        }

        @Override
        protected String buildParams() {
            int mId = AccountInfo.getInstance(LivingActivity.this).getUserId();
            return new StringBuilder("live_id=").append(mLiveInfo.id).append("&query_uid=").append(mId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mStatusInfo = LiveAdminStatusInfo.fromJson(response.getJsonObject());
            }
        }
    }

    @Override
    public void onFollowSuccess() {
        mCurrentStatusInfo.isFollow = true;
        mTvFollow.setText(getString(R.string.news_source_unfollow));
    }

    @Override
    public void onFollowFail() {
    }

    @Override
    public void onCancleFollowSuccess() {
        mCurrentStatusInfo.isFollow = false;
        mTvFollow.setText(getString(R.string.tribe_collected_add));

    }

    @Override
    public void onCancleFollowFail() {
    }

    @Override
    public void onGetFollowListSuccess(MsResponse response, int offset) {

    }

    @Override
    public void onGetFollowListFail() {

    }

    private class AddBlackTask extends MsTask {

        public AddBlackTask() {
            super(LivingActivity.this, MsRequest.LIST_ADD_BLACK);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("target_uid=").append(mCurrentUser.userId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mCurrentStatusInfo.isInblickList = true;
                if (mBlackList == null) {
                    mBlackList = new ArrayList<>();
                }
                boolean isInBlack = false;
                for (int i = 0; i < mBlackList.size() ; i++) {
                    if (mCurrentUserId == mBlackList.get(i).intValue()) {
                        isInBlack = true;
                    }
                }
                if (!isInBlack) {
                    mBlackList.add(mCurrentUserId);
                }
            } else {
                mCurrentStatusInfo.isInblickList = false;
            }
        }
    }

    private class CancelBlackTask extends MsTask {

        public CancelBlackTask() {
            super(LivingActivity.this, MsRequest.LIST_REMOVE_BLACK);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("target_uid=").append(mCurrentUser.userId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mCurrentStatusInfo.isInblickList = false;
                if (mBlackList != null) {
                    for (int i = 0; i < mBlackList.size() ; i++) {
                        if (mCurrentUserId == mBlackList.get(i).intValue()) {
                            mBlackList.remove(i);
                            break;
                        }
                    }
                }
            } else {
                mCurrentStatusInfo.isInblickList = true;
            }
        }
    }

    private class Report extends MsTask {

        private String mImageUrl;

        public Report(String url) {
            super(LivingActivity.this, MsRequest.POLICE_REPORT);
            mImageUrl = url;
        }

        @Override
        protected String buildParams() {
//            type: int
//            object_id: int
//            description: string
//            img_url:string（可选，举报照片）
            return new StringBuilder("img_url=").append(mImageUrl).append("&type=").append(7)
                    .append("&object_id=").append(mReportId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {

            if(response.isSuccessful()) {
                toast("感谢你的真实举报，我们将进行相应处理");
            } else {
                toast("举报失败"  +response.code);
            }
        }
    }


    @Override
    public void onUploadSuccess(File file, byte[] data, String url, String objectKey) {
        //上传阿里云成功
        System.out.println("--------------------上传阿里云成功" + url);
        new Report(url).executeLong();
    }

    @Override
    public void onUploadProgress(File file, byte[] data, int byteCount, int totalSize) {
        System.out.println("--------------------上传阿里云失败");
    }

    @Override
    public void onUploadFailure(File file, byte[] data, String errMsg) {
        //上传阿里云失败
    }

    private void startStreamCheck() {
        stopStreamCheck();
        mStreamCheckTimer = new Timer();
        mStreamCheckTimerTask = new TimerTask() {
            @Override
            public void run() {
                getStreamStatus(mConnetionActivityId);
            }
        };
        mStreamCheckTimer.schedule(mStreamCheckTimerTask, 0, 5000);
    }

    private void stopStreamCheck() {
        if (mStreamCheckTimer != null) {
            mStreamCheckTimer.cancel();
            mStreamCheckTimer = null;
            mStreamCheckTimerTask = null;
        }
    }

    private class getIsSilenceTask extends MsTask {

        public getIsSilenceTask() {
            super(LivingActivity.this, MsRequest.LIVE_MY_IS_SILENCE);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("live_id=").append(mLiveInfo.id).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                boolean isMySilence = response.getJsonObject().optBoolean("is_silence");
                if (isMySilence) {
                    toast("本场你已经被禁言，不能发言了");
                } else {
                    sendMessage(ChatRecord.CHAT_TYPE_CHAT_MSG);
                }
            }
        }
    }

    private class GetBlackList extends MsTask {
        private boolean isFromMessage;
        private ChatRecord record;
        public GetBlackList(boolean isFromMessage, ChatRecord record) {
            super(LivingActivity.this, MsRequest.LIST_BLACK_LIST);
            this.isFromMessage = isFromMessage;
            this.record = record;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                int size = ja == null ? 0 : ja.length();
                for (int i = 0; i < size; i++) {
                    mBlackList.add(ja.optInt(i));
                }
                if (isFromMessage) {
                    boolean isInBlack = false;
                    for (int i = 0; i < mBlackList.size(); i++) {
                        if (mBlackList.get(i) == mUserInfoManager.getUserInfo(record.from).userId) {
                            isInBlack = true;
                        }
                    }
                    if (!isInBlack) {
                        mAdapter.add(record);
                    }
                }
            }
        }
    }

    private void showSilenceDialog () {
        if (mSilenceDialog == null) {
            mSilenceDialog = new LiveDialog(this);
            mSilenceDialog.setText(getString(R.string.live_silence_notice));
            mSilenceDialog.setPositiveButton(R.string.you_sure, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new AddShutupTask().executeLong();
                }
            });
            mSilenceDialog.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mSilenceDialog.dismiss();
                }
            });
        }
        mSilenceDialog.show();
    }


    private class AddShutupTask extends MsTask {

        public AddShutupTask() {
            super(LivingActivity.this, MsRequest.LIVE_MAKE_SILENCE);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("silence_uid=").append(mCurrentUser.userId).append("&live_id=").append(mLiveInfo.id).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mCurrentStatusInfo.isShutUp = true;
            }
        }
    }


    private class CancelShutupTask extends  MsTask {

        public CancelShutupTask() {
            super(LivingActivity.this, MsRequest.LIVE_CANCEL_MAKE_SILENCE);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("silence_uid=").append(mCurrentUser.userId).append("&live_id=").append(mLiveInfo.id).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mCurrentStatusInfo.isShutUp = false;
            }
        }
    }

    private class ShareAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mShareList.size();
        }

        @Override
        public ShareInfo getItem(int i) {
            return mShareList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_share, viewGroup, false);
            }
            TextView mTvShareName = (TextView) convertView.findViewById(R.id.tv_share_name);
            ImageView mIvShareIcon = (ImageView) convertView.findViewById(R.id.iv_share_icon);
            mTvShareName.setText(mShareList.get(i).name);
            Picasso.with(LivingActivity.this).load(R.drawable.bottom_qq).into(mIvShareIcon);
            convertView.setTag(mShareList.get(i));
            convertView.setOnClickListener(mShareListener);
            return convertView;
        }
    }

    private View.OnClickListener mShareListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ShareInfo mShareInfo = (ShareInfo) view.getTag();
            switch (mShareInfo.type) {
                case ShareInfo.SHARE_TYPE_CIRCLE_OF_FRIENDS:
                    break;
                case ShareInfo.SHARE_TYPE_WEI_CHAT:
                    break;
                case ShareInfo.SHARE_TYPE_WEI_BO:
                    break;
                case ShareInfo.SHARE_TYPE_QQ:
                    break;
                case ShareInfo.SHARE_TYPE_QQ_ZONE:
                    break;
                case ShareInfo.SHARE_TYPE_TRIBE:
                    break;
                case ShareInfo.SHARE_TYPE_SCHOOL_HOME:
                    break;
                default:
                    break;
            }
        }
    };

    private void showMoneyDialog () {
        if (mMoneyDialog == null) {
            mMoneyDialog = new LiveDialog(LivingActivity.this);
            mMoneyDialog.setText(getString(R.string.live_money_notice));
            mMoneyDialog.setPositiveButton(R.string.live_to_recharge, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(LivingActivity.this, GoldDepositsActivity. class);
                    startActivityForResult(intent, 0);
                }
            });
            mMoneyDialog.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mMoneyDialog.dismiss();
                }
            });
        }
        mMoneyDialog.show();
    }

}
