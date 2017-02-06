package com.tjut.mianliao.live;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
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
import com.letv.recorder.controller.VideoRecordDevice;
import com.letv.universal.iplay.EventPlayProxy;
import com.letv.universal.iplay.ISplayer;
import com.letv.universal.iplay.OnPlayStateListener;
import com.letv.universal.play.util.PlayerParamsHelper;
import com.letv.universal.widget.ReSurfaceView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnTouch;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.anim.AnimUtils;
import com.tjut.mianliao.anim.GiftAnimView;
import com.tjut.mianliao.anim.GiftControllerInfo;
import com.tjut.mianliao.anim.HeartLayout;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.LiveDialog;
import com.tjut.mianliao.component.LiveGiftPicker;
import com.tjut.mianliao.component.RichMlEditText;
import com.tjut.mianliao.contact.FollowUserManager;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.ChatRecord;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.GiftInfo;
import com.tjut.mianliao.data.HostUserInfo;
import com.tjut.mianliao.data.LiveAdminStatusInfo;
import com.tjut.mianliao.data.LiveInfo;
import com.tjut.mianliao.data.LiveMemberInfo;
import com.tjut.mianliao.data.LivingMenuInfo;
import com.tjut.mianliao.data.RequestInfo;
import com.tjut.mianliao.data.ShareInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.im.GroupChatManager;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.util.AliOSSHelper;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.SnsHelper;
import com.tjut.mianliao.util.Utils;
import com.tjut.mianliao.xmpp.ChatHelper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.lasque.tusdk.core.listener.AnimationListenerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by YoopWu on 2016/6/20 0020.
 */
public class LivingRecordActivity extends BaseActivity implements SurfaceHolder.Callback,
        View.OnClickListener, GroupChatManager.GroupChatListener, ChatHelper.MessageSendListener,
        ChatHelper.MessageReceiveListener, SoftKeyboardHelper.SoftKeyboardStateListener,
        VideoRecorderDeviceListener, PullToRefreshBase.OnRefreshListener2<ListView>,
        FollowUserManager.OnUserFollowListener, AliOSSHelper.OnUploadListener {

    private static final String TAG = "Live";

    public static final String LE_ACTIVITY_ID = "le_activity_id";
    public static final String LE_CAHT_ID = "le_chat_id";
    public static final String LE_LIVE_INFO = "le_live_info";
    public static final String LE_LIVE_GID = "le_live_gid";

    private static final int LIVING_END = 10;
    private static final int UPDATE_END_VIEW = 11;
    private static final int FOLLOW_REFRESH = 33;
    /**
     * 管理员list
     **/
    private List managerList = new ArrayList();
    @ViewInject(R.id.fr_user_add_manager)
    private FrameLayout frMangerView;
    @ViewInject(R.id.fr_user_add_manager_no)
    private FrameLayout frNoManagerView;

    private SoftKeyboardHelper mSoftKeyboardHelper;

    @ViewInject(R.id.bessal_view)
    private HeartLayout mHeartLayout;
    @ViewInject(R.id.tv_send_dm)
    private TextView mTvSendDm;
    @ViewInject(R.id.tv_send)
    private TextView mTvSend;
    @ViewInject(R.id.et_msg)
    private RichMlEditText mMessageEditor;
    @ViewInject(R.id.list_live_msg)
    private ListView mListView;
    @ViewInject(R.id.danmu_rl)
    private XDanmuView mDanmuView;
    @ViewInject(R.id.ll_reply)
    private LinearLayout mLlReplyInput;
    @ViewInject(R.id.ll_operate)
    private LinearLayout mLlOperate;
    @ViewInject(R.id.tv_send_dm)
    private CheckBox mCbDanmu;
    @ViewInject(R.id.surface_view)
    private SurfaceView mSurfaceView;
    @ViewInject(R.id.gift_anim_view)
    private GiftAnimView mGiftAnimView;
    @ViewInject(R.id.gift_picker)
    private LiveGiftPicker mGiftPicker;
    @ViewInject(R.id.fl_root)
    private FrameLayout mFlRoot;
    @ViewInject(R.id.follow_gridview)
    private TwoWayGridView mGridView;
    @ViewInject(R.id.iv_avatar)
    private ImageView mIvAvatar;
    @ViewInject(R.id.tv_name)
    private TextView mTvName;
    @ViewInject(R.id.ll_open_menu)
    private LinearLayout mLlMenu;
    @ViewInject(R.id.iv_open_operate)
    private ImageView mIvOpenMenu;
    @ViewInject(R.id.tv_date_today)
    private TextView mTvDateDay;
    @ViewInject(R.id.iv_blur)
    private FrameLayout mFlBlur;
    // gift list info
    @ViewInject(R.id.fl_gifts_list_info)
    private FrameLayout mFlGiftInfoView;
    @ViewInject(R.id.ptrlv_gifts_list)
    private PullToRefreshListView mPtrGiftList;
    @ViewInject(R.id.tv_no_gift_notice)
    private TextView mTvNoGiftsNotice;
    @ViewInject(R.id.gv_request_list)
    private TwoWayGridView mGvRequestList;
    @ViewInject(R.id.ll_connection_list)
    private LinearLayout mLlConnectionList;
    @ViewInject(R.id.rl_2nd_container)
    private RelativeLayout mRl2ndContainer;
    @ViewInject(R.id.lv_menu)
    private ListView mMenuList;
    @ViewInject(R.id.tv_manager)
    private TextView mTvManager;
    @ViewInject(R.id.fr_userinformation_botoom)
    private FrameLayout mFrView;
    @ViewInject(R.id.tv_follow_user)
    private TextView mTvFollow;
    @ViewInject(R.id.tv_profile)
    private TextView mTvtoProfile;
    @ViewInject(R.id.fl_connection_container)
    private FrameLayout mFlConnectionContainer;
    @ViewInject(R.id.ll_close_connection_menu)
    private LinearLayout mLlCloseConnMenu;
    @ViewInject(R.id.ll_wait_live_ui)
    private LinearLayout mLlWaitingUi;
    @ViewInject(R.id.ll_follow_profile)
    private LinearLayout mLlButton;
    @ViewInject(R.id.tv_follow_num)
    private TextView mTvFollowNum;
    @ViewInject(R.id.tv_fans_num)
    private TextView mTvFansNum;
    @ViewInject(R.id.tv_contribute_num)
    private TextView mTvContributeNum;
    @ViewInject(R.id.tv_username)
    private TextView mTvUserName;
    @ViewInject(R.id.tv_user_school)
    private TextView mTvSchool;
    @ViewInject(R.id.user)
    private TextView mTvUserContent;
    @ViewInject(R.id.iv_gerden)
    private ImageView mIvUserGerden;
    @ViewInject(R.id.iv_type_icon)
    private ImageView mIvUserType;
    @ViewInject(R.id.tv_managment)
    private TextView mTvManageMent;
    @ViewInject(R.id.gv_share)
    private GridView mGvShare;
    @ViewInject(R.id.tv_income)
    private TextView mTvIncome;
    @ViewInject(R.id.ll_income)
    private LinearLayout mLlIncome;

    private ImageView mIvFriendCircle;
    private ImageView mIvWeichat;
    private ImageView mIvWeibo;
    private ImageView mIvQQ;
    private ImageView mIvQQZone;

    private LetvPublisher mPublisher;
    private LiveChatAdapter mAdapter;
    private ChatHelper mChatHelper;
    private GroupChatManager mGroupChatManager;
    private UserInfoManager mUserInfoManager;
    private UserInfo mUserInfo;

    private String mActivityId;
    private String mLeUid;
    private String mLeSecretKey;

    private LiveInfo mLiveInfo;

    private LightDialog mQuitDialog;

    private String mGroupId;
    private String mChatId;
    private String mChatTarget;

    private boolean mIsFlashOn;
    private boolean mIsMenuOpen;

    private int mCameraPosition = 0;

    private ArrayList<LiveMemberInfo> mFocusUsers;
    private HeadAdapter mHeadAdapter;
    private int mTotalIncome;
    private HostUserInfo mHostUserInfo;
    private boolean mIsGiftListViewShow;
    private ArrayList<GiftInfo> mGifts;
    private GiftAdapter mGiftAdapter;
    private Bitmap mBgMap;
    private LiveEndInfo mLiveEndInfo;
    private ArrayList<RequestInfo> mRequestList;
    private int mConnectionNum = 0;
    private String mConnetionActivityId;
    private RequestInfo mCurrentRequest;
    private ConnectionAdapter mConAdapter;
    private boolean isConnectionListShow;
    private Bundle mBundle;
    private PlayContext mPlayContext;
    private ISplayer mPlayer;
    private ReSurfaceView mSurfaceView2;
    private MenuAdapter mMenuAdapter;
    private UserInfo mCurrentUser;
    private int mCurrentUserId;
    private LiveAdminStatusInfo mCurrentStatusInfo;
    private FollowUserManager mFollowuserManager;
    private AliOSSHelper mAliOSSHelper;
    private RequestInfo mRequest;

    private Timer mStreamCheckTimer;
    private TimerTask mStreamCheckTimerTask;
    private LiveDialog mSilenceDialog;

    private ShareInfo mShareInfo;
    private ArrayList<ShareInfo> mShareList;
    private ShareAdapter mShareAdapter;
    private SnsHelper mSnsHelp;

    private int mCurrentShareType = CreateLiveRoomActivity.SHARE_TYPE_CIRCLE_OF_FRIENDS;

    private Handler mFreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LIVING_END:
                    new GetEndInfoTask().executeLong();
                    break;
                case UPDATE_END_VIEW:
                    mFlBlur.addView(getLivingEndView());
                    break;
                case FOLLOW_REFRESH:
                    if (mCurrentStatusInfo.isFollow) {
                        mTvFollow.setText(getString(R.string.news_source_unfollow));
                    } else {
                        mTvFollow.setText(getString(R.string.news_source_follow));
                    }
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
        return R.layout.activity_live_record;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mUserInfoManager = UserInfoManager.getInstance(this);
        mUserInfo = AccountInfo.getInstance(this).getUserInfo();
        mActivityId = getIntent().getStringExtra(LE_ACTIVITY_ID);
        mLiveInfo = getIntent().getParcelableExtra(LE_LIVE_INFO);
        mChatId = getIntent().getStringExtra(LE_CAHT_ID);
        mGroupId = getIntent().getStringExtra(LE_LIVE_GID);
        mFollowuserManager = FollowUserManager.getInstance(this);
        mFollowuserManager.registerOnUserFollowListener(this);
        mAliOSSHelper = AliOSSHelper.getInstance(this);
        mSnsHelp = SnsHelper.getInstance();

        mTvFollow.setOnClickListener(this);
        mTvtoProfile.setOnClickListener(this);
        mTvManager.setOnClickListener(this);
        mLlIncome.setOnClickListener(this);

        mFocusUsers = new ArrayList<>();
        mGifts = new ArrayList<>();
        mRequestList = new ArrayList<>();

        mHeadAdapter = new HeadAdapter();
        mGiftAdapter = new GiftAdapter();
        mConAdapter = new ConnectionAdapter();

        mGvRequestList.setAdapter(mConAdapter);
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

        mPtrGiftList.setAdapter(mGiftAdapter);
        mPtrGiftList.setMode(PullToRefreshBase.Mode.BOTH);
        mPtrGiftList.setOnRefreshListener(this);

        if (mLiveInfo == null) {
            finish();
            return;
        }
        createTarget();
        mChatHelper = ChatHelper.getInstance(this);
        mChatHelper.registerReceiveListener(this);
        mChatHelper.registerSendListener(this);
        mGroupChatManager = GroupChatManager.getInstance(this);
        mGroupChatManager.registerGroupChatListener(this);
        mActivityId = mLiveInfo.activityId;
        mLeUid = Utils.getLeUid();
        mLeSecretKey = Utils.getLeSecretKey();
        LetvPublisher.init(mActivityId, mLeUid, mLeSecretKey);
        mPublisher = LetvPublisher.getInstance();
        mPublisher.initPublisher(this);
        mPublisher.getRecorderContext().setUseLanscape(false);
        mSurfaceView.setZOrderOnTop(false);
        mPublisher.setCameraView(mSurfaceView);
        mSurfaceView.getHolder().addCallback(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mAdapter = new LiveChatAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    doOnTouch();
                return false;
            }
        });

        fetchLiveMembersList(true);
        mTvDateDay.setText(Utils.getTimeString(9, System.currentTimeMillis()));
        new GetHostInfoTask().executeLong();
        /**软盘监听**/
        mSoftKeyboardHelper = new SoftKeyboardHelper(findViewById(R.id.fl_root));
        mSoftKeyboardHelper.addSoftKeyboardStateListener(this);
        if (!TextUtils.isEmpty(mLiveInfo.prevUrl)) {
            String fileName = getFIleNameByUrl(mLiveInfo.prevUrl);
            if (HttpUtil.downLoad(this, mLiveInfo.prevUrl, fileName)) {
                mBgMap = BitmapFactory.decodeFile(fileName);
                blur(mFlBlur);
            }
        } else {
            mBgMap = BitmapFactory.decodeResource(getResources(), R.drawable.splash_an);
            blur(mFlBlur);
        }
        mFlBlur.setVisibility(View.GONE);
    }

    private String getFIleNameByUrl(String url) {
        return Utils.getMianLiaoDir().getAbsolutePath() + "/" + System.currentTimeMillis()
                + Utils.getFilePostfix(url);
    }

    @OnTouch({R.id.list_live_msg})
    public void onTouch(View v) {
        doOnTouch();
    }

    private void createTarget() {
        mChatTarget = mChatId + "@groupchat." + Utils.getChatServerDomain();
    }

    public void showExitDialog() {
        if (mQuitDialog == null) {
            mQuitDialog = new LightDialog(this);
            mQuitDialog.setTitle("提示");
            mQuitDialog.setMessage("确定要结束直播吗?")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            endLive();
                            stopPlayer();
                            closeConnection();
                        }
                    });
        }
        mQuitDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fl_root:
            case R.id.surface_view:
            case R.id.bessal_view:
            case R.id.danmu_rl:
                doOnTouch();
                break;
            case R.id.tv_send:
                if (mCbDanmu.isChecked()) {
                    sendMessage(ChatRecord.CHAT_TYPE_DANMU_MSG);
                } else {
                    sendMessage(ChatRecord.CHAT_TYPE_CHAT_MSG);
                }
                toggleInputAndOperate(false);
                break;
            case R.id.ll_flash_light:
                switchFlashFlag();
                break;
            case R.id.ll_camera:
                switchCamera();
                break;
            case R.id.iv_back:
                showExitDialog();
                break;
            case R.id.iv_message:
                toggleInputAndOperate(true);
                break;
            case R.id.ll_share:
                share();
                break;
            case R.id.iv_gift_lists:
                mIsGiftListViewShow = true;
                toggleGiftsShowInfo();
                break;
            case R.id.iv_open_operate:
                mIsMenuOpen = !mIsMenuOpen;
                toggleMenuShow();
                break;
            case R.id.ll_manager:
                // 管理员
                if (managerList.size() == 0) {
                    showBottomView(frNoManagerView);
                } else showBottomView(frMangerView);
                hideBottomView(mLlOperate);
                break;
            case R.id.tv_back:
                finish();
                break;
            case R.id.ll_connection_user:
                Utils.logD(TAG, "同意连线，连线中。。。");
                RequestInfo request = (RequestInfo) v.getTag();
                new ConnectionTask(request).executeLong();
                break;
            case R.id.tv_manager:
                mMenuList.setVisibility(View.VISIBLE);
                mMenuAdapter = new MenuAdapter(LivingMenuInfo.getLivingRecordMenu(mCurrentStatusInfo));
                mMenuList.setAdapter(mMenuAdapter);
                mMenuAdapter.notifyDataSetChanged();
                break;
            case R.id.iv_avatar:
                mCurrentUserId = mLiveInfo.uid;
                UserInfo user = mUserInfoManager.getUserInfo(mLiveInfo.uid);
                if (user != null) {
                    mCurrentUser = user;
                    new GetCurrentUserAdminStatus().executeLong();
                }
                break;
            case R.id.tv_follow_user:
                if (mCurrentStatusInfo.isFollow) {
                    mFollowuserManager.cancleFollow(mCurrentUser.userId);
                } else {
                    mFollowuserManager.follow(mCurrentUser.userId);
                }
                break;
            case R.id.tv_profile:
                Intent iProfile = new Intent(LivingRecordActivity.this, NewProfileActivity.class);
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
                Intent incomeIntent = new Intent(LivingRecordActivity.this, LiveGainRankActivity.class);
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

    private void doOnTouch() {
        if (!mFlGiftInfoView.isShown() && !mLlOperate.isShown()) {
            mHeartLayout.addHeart();
        }
        if (mGvShare.getVisibility() == View.VISIBLE) {
            mGvShare.setVisibility(View.GONE);

        }
        if (frNoManagerView.isShown()) {
            hideBottomView(frNoManagerView);
            showBottomView(mLlOperate);
        }
        if (frMangerView.isShown()) {
            hideBottomView(frMangerView);
            showBottomView(mLlOperate);
        }
        toggleInputAndOperate(false);
        if (isConnectionListShow) {
            mLlConnectionList.setVisibility(View.GONE);
            isConnectionListShow = false;
        }
        if (mIsMenuOpen) {
            mIsMenuOpen = false;
            toggleMenuShow();
        }
        if (mIsGiftListViewShow) {
            toggleGiftsShowInfo();
        }
        mSnsHelp.closeShareBoard();
        if (mFlGiftInfoView.isShown()) {
            hideBottomView(mFlGiftInfoView);
        }
        closeUserInfo();
        mLlMenu.setVisibility(View.VISIBLE);
    }

    private void closeUserInfo() {
        if (mFrView.getVisibility() == View.VISIBLE) {
            Animation downAnim = AnimationUtils.loadAnimation(this, R.anim.live_down_animation);
            downAnim.setAnimationListener(new AnimationListenerAdapter() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    mFrView.setVisibility(View.GONE);
                }
            });
            mFrView.startAnimation(downAnim);
        }

        if (mFrView.getVisibility() == View.VISIBLE) {
            hideBottomView(mFrView);
        }
        mSnsHelp.closeShareBoard();
    }

    private void toggleGiftsShowInfo() {
        if (mIsGiftListViewShow) {
            Animation upAnim = AnimationUtils.loadAnimation(this, R.anim.live_up_animation);
            mFlGiftInfoView.startAnimation(upAnim);
            fetchGifts(true);
            mFlGiftInfoView.setVisibility(View.VISIBLE);
            mLlMenu.setVisibility(View.GONE);
            mListView.setVisibility(View.GONE);
            // getGiftList
        } else {
            Animation downAnim = AnimationUtils.loadAnimation(this, R.anim.live_down_animation);
            downAnim.setAnimationListener(new AnimationListenerAdapter() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    mFlGiftInfoView.setVisibility(View.GONE);
                    mLlMenu.setVisibility(View.VISIBLE);
                    mListView.setVisibility(View.VISIBLE);
                }
            });
            mFlGiftInfoView.startAnimation(downAnim);
        }
    }

    private void toggleMenuShow() {
        Animation upAnim = AnimationUtils.loadAnimation(this, R.anim.live_up_animation);
        upAnim.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (mIsMenuOpen) {
                    mLlMenu.setVisibility(View.GONE);
                    mLlOperate.setVisibility(View.VISIBLE);
                } else {
                    mLlMenu.setVisibility(View.VISIBLE);
                    mLlOperate.setVisibility(View.GONE);
                }
            }
        });
        Animation downAnim = AnimationUtils.loadAnimation(this, R.anim.live_down_animation);
        downAnim.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (mIsMenuOpen) {
                    mLlMenu.setVisibility(View.GONE);
                    mLlOperate.setVisibility(View.VISIBLE);
                } else {
                    mLlMenu.setVisibility(View.VISIBLE);
                    mLlOperate.setVisibility(View.GONE);
                }
            }
        });
        if (mIsMenuOpen) {
            mIvOpenMenu.setImageResource(R.drawable.img_living_uparrow);
            mLlMenu.startAnimation(downAnim);
            mLlOperate.startAnimation(upAnim);
            mLlOperate.setVisibility(View.VISIBLE);
        } else {
//            mIvOpenMenu.setImageResource(R.drawable.live_close_operate);
            mLlOperate.startAnimation(downAnim);
            mLlMenu.startAnimation(upAnim);
            mLlMenu.setVisibility(View.VISIBLE);
        }
    }

    private void share() {
//        mGvShare.setVisibility(View.VISIBLE);
//        mLlOperate.setVisibility(View.GONE);
//        mShareInfo = new ShareInfo(LivingRecordActivity.this);
//        mShareList = mShareInfo.getShareList();
//        mShareAdapter = new ShareAdapter();
//        mGvShare.setAdapter(mShareAdapter);
//        mShareAdapter.notifyDataSetChanged();
        mSnsHelp.openShareBoard(this, mLiveInfo, "");
    }

    private void switchFlashFlag() {
        VideoRecordDevice device = mPublisher.getVideoRecordDevice();
        if (device == null) {
            return;
        }
        if (mIsFlashOn) {
            mIsFlashOn = false;
            device.setFlashFlag(false);
//            mIvFlash.setImageResource(R.drawable.icon_flashlight_on);
        } else {
            mIsFlashOn = true;
            device.setFlashFlag(true);
//            mIvFlash.setImageResource(R.drawable.icon_flashlight_off);
        }
    }

    private void switchCamera() {
        VideoRecordDevice device;
        device = mPublisher.getVideoRecordDevice();
        if (device == null) {
            return;
        }
        int count = mPublisher.getVideoRecordDevice().getCamera().getNumberOfCameras();
        if (count == 1) {
            return;
        }
        mCameraPosition = mCameraPosition == 0 ? 1 : 0;
        if (device.checkCameraAvaiable(mCameraPosition)) {
            device.switchCamera(mCameraPosition);
        }
    }

    private void toggleInputAndOperate(boolean showInput) {
        mMenuList.setVisibility(View.GONE);
        mCbDanmu.setChecked(false);
        if (showInput) {
            Utils.showInput(mMessageEditor);
            mLlReplyInput.setVisibility(View.VISIBLE);
            mLlMenu.setVisibility(View.GONE);
        } else {
            Utils.hideInput(mMessageEditor);
            mLlReplyInput.setVisibility(View.GONE);
            mLlMenu.setVisibility(View.VISIBLE);
        }
    }

    private void sendMessage(int msgType) {
        if (TextUtils.isEmpty(mChatTarget)) {
            return;
        }
        String message = mMessageEditor.getText().toString().trim();
        if (Utils.isNetworkAvailable(this)) {
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


    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    private void endLive() {
        new EndLiveTask().executeLong();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mPublisher != null) {
            mPublisher.stopPublish();
        }
    }

    @Override
    protected void onDestroy() {
        endLive();
        super.onDestroy();
        if (mPublisher != null) {
            mPublisher.release();
        }
        if (mPlayContext != null) {
            mPlayContext.destory();
        }
        mChatHelper.unregisterReceiveListener(this);
        mChatHelper.unregisterSendListener(this);
        mGroupChatManager.unregisterGroupChatListener(this);
        mFollowuserManager.unregisterOnUserFollowListener(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mPublisher.isRecording()) {
            return;
        }
        if (mPublisher.getVideoRecordDevice() != null) {
            mPublisher.getVideoRecordDevice().setVideoRecorderDeviceListener(this);
            //获取摄像头实例，并且给视频绑定surfaceHolder，必须保证surfaceHodler可用
            mPublisher.getVideoRecordDevice().bindingSurface(holder);
            //开启摄像头预览
            mPublisher.getVideoRecordDevice().start();
            //获取机位信息，***必须在开启预览之后才能获取*** infoCallback 是请求机位回调
            System.out.println("-------------------开始获取机位信息");
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

    private LetvRecorderCallback<ArrayList<LivesInfo>> mInfoCallback =
            new LetvRecorderCallback<ArrayList<LivesInfo>>() {
                @Override
                public void onFailed(int i, String s) {
                    System.out.println("-------------------获取机位信息失败" + s);
                }

                @Override
                public void onSucess(ArrayList<LivesInfo> livesInfos) {
                    Utils.logD(TAG, System.currentTimeMillis() + "succ");
                    //获取机位信息成功后，选中第一个机位
                    mPublisher.selectMachine(0);
                    //开始推流
                    System.out.println("-------------------获取机位信息成功，开始推流");
                    mPublisher.publish();
                }
            };

    @Override
    public void onGroupChatSuccess(int type, Object obj) {
        switch (type) {
            case GroupChatManager.CREATE_GROUP:

                break;
            default:
                break;
        }
    }

    @Override
    public void onGroupChatFailed(int type) {

    }

    @Override
    public void onSoftKeyboardOpened(int keyboardHeightInPx) {
    }

    @Override
    public void onSoftKeyboardClosed() {
        toggleInputAndOperate(false);
        mIsMenuOpen = false;
        toggleMenuShow();
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
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchGifts(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchGifts(false);
    }

    @Override
    public void onFollowSuccess() {
        mCurrentStatusInfo.isFollow = true;
        mTvFollow.setText(getString(R.string.news_source_unfollow));
    }

    @Override
    public void onFollowFail() {
        mCurrentStatusInfo.isFollow = false;
    }

    @Override
    public void onCancleFollowSuccess() {
        mCurrentStatusInfo.isFollow = false;
        mTvFollow.setText(getString(R.string.news_source_follow));

    }

    @Override
    public void onCancleFollowFail() {
        mCurrentStatusInfo.isFollow = true;
    }

    @Override
    public void onGetFollowListSuccess(MsResponse response, int offset) {

    }

    @Override
    public void onGetFollowListFail() {

    }


    private class EndLiveTask extends MsTask {

        public EndLiveTask() {
            super(LivingRecordActivity.this, MsRequest.END_LIVE);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("live_id=").append(mLiveInfo.id)
                    .append("&activity_id=").append(mLiveInfo.activityId)
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                DataHelper.deleteChatRecords(LivingRecordActivity.this, mChatTarget);
                mFreshHandler.sendEmptyMessage(LIVING_END);
            }
        }
    }

    private View getLivingEndView() {
        View view = mInflater.inflate(R.layout.item_living_record_end, null);
        TextView mTvBack = (TextView) view.findViewById(R.id.tv_back);
        TextView mTvIncom = (TextView) view.findViewById(R.id.tv_incom_num);
        TextView mTvTimeLong = (TextView) view.findViewById(R.id.tv_time_long);
        TextView mTvAudienceNum = (TextView) view.findViewById(R.id.tv_audience_num);
        mIvFriendCircle = (ImageView) view.findViewById(R.id.iv_weichat_friend);
        mIvWeichat = (ImageView) view.findViewById(R.id.iv_weichat);
        mIvWeibo = (ImageView) view.findViewById(R.id.iv_weibo);
        mIvQQ = (ImageView) view.findViewById(R.id.iv_qq);
        mIvQQZone = (ImageView) view.findViewById(R.id.iv_qq_zone);
        mIvFriendCircle.setOnClickListener(LivingRecordActivity.this);
        mIvWeichat.setOnClickListener(LivingRecordActivity.this);
        mIvWeibo.setOnClickListener(LivingRecordActivity.this);
        mIvQQ.setOnClickListener(LivingRecordActivity.this);
        mIvQQZone.setOnClickListener(LivingRecordActivity.this);
        if (mLiveEndInfo != null) {
            mTvAudienceNum.setText(Utils.getColoredText(getString(R.string.living_spectator_num,
                    mLiveEndInfo.spectatorNum + ""), mLiveEndInfo.spectatorNum + "", 0XFF4DE8B3));
            mTvIncom.setText(Utils.getColoredText(getString(R.string.living_incom,
                    mLiveEndInfo.income + ""), mLiveEndInfo.income + "", 0XFFFFAC5A));
            mTvTimeLong.setText(getString(R.string.living_time_long, mLiveEndInfo.time));
        }
        mTvBack.setOnClickListener(this);
        return view;
    }


    @Override
    public void onMessageReceived(ChatRecord record) {
        if (record.isTarget(mChatTarget)) {
            operateChatRecord(record);
        }
    }

    private void operateChatRecord(ChatRecord record) {
        switch (record.type) {
            case ChatRecord.CHAT_TYPE_DANMU_MSG:
                Utils.logD(TAG, "receive danmu massage");
                mDanmuView.addDanmuItemView(createDanmuItemView(record));
                break;
            case ChatRecord.CHAT_TYPE_CHAT_MSG:
                mAdapter.add(record);
                break;
            case ChatRecord.CHAT_TYPE_NORMAL_GIFT:
                LiveGift giftInfo = mGiftPicker.getLiveGiftInfo(record.giftId);
                mTotalIncome = mTotalIncome + giftInfo.price;
                mTvIncome.setText(mTotalIncome + "");
                GiftControllerInfo giftAnimView = createGiftAnimView(giftInfo);
                mGiftAnimView.addGift(giftAnimView);
                mAdapter.add(record);
                break;
            case ChatRecord.CHAT_TYPE_SPECIAL_GIFT:
                showSpecialGiftAnim();
                if (record.animType == ChatRecord.MSG_ANIM_TYPE_FIRE) {

                } else if (record.animType == ChatRecord.MSG_ANIM_TYPE_SPECIAL) {

                }
                mAdapter.add(record);
                break;
            case ChatRecord.CHAT_TYPE_FOLLOW_MSG:
                mAdapter.add(record);
                break;
            case ChatRecord.CHAT_TYPE_PRAISE_MSG:
                if (!mGiftPicker.isVisible()) {
                    mHeartLayout.addHeart();
                }
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
            case ChatRecord.CHAT_TYPE_SEND_CONNECTION_REQUEST:
//                Utils.logD(TAG, "收到连线请求");
//                mConnectionNum++;
//                mTvConnectionNum.setText(mConnectionNum + "");
                break;
            case ChatRecord.CHAT_TYPE_CANCEL_CONNECTION_REQUEST:
//                Utils.logD(TAG, "收到取消连线请求");
//                mConnectionNum--;
//                mTvConnectionNum.setText(mConnectionNum + "");
                break;
            case ChatRecord.CHAT_TYPE_CLOSE_CONNECTION:
                Utils.logD(TAG, "关闭连线");
                break;
            default:
                break;
        }
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
        Picasso.with(this).load(mUserInfo.getAvatar())
                .placeholder(mUserInfo.defaultAvatar())
                .into(ivAvatar);
        Picasso.with(this).load(gift.icon).into(ivGift);
        if (userInfo != null) {
            tvUser.setText(userInfo.getDisplayName(this));
        } else {
            tvUser.setText("一个朋友");
        }
        CharSequence content = getString(R.string.gift_desc, gift.name);
        CharSequence coloredText = Utils.getColoredText(content, gift.name, 0xffff73ae, false);
        tvGift.setText(coloredText);
        tvCount.setText("X1");
        info.allView = view;
        info.allView = view;
        info.giftView = ivGift;
        info.countView = tvCount;
        info.giftId = gift.giftId;
        info.userId = userId == 0 ? mUserInfo.userId : userId;
        info.giftCount = 1;
        return info;
    }

    @Override
    public void onMessageReceiveFailed(ChatRecord record) {

    }

    @Override
    public void onMessagePreSend(ChatRecord record) {

    }

    @Override
    public void onMessageSent(ChatRecord record) {
        if (record.isTarget(mChatTarget)) {
            if (record.type == ChatRecord.CHAT_TYPE_CHAT_MSG) {
                mAdapter.add(record);
            }
        }
    }

    @Override
    public void onMessageSendFailed(ChatRecord record) {

    }

    private class GetAudienceListTask extends MsTask {
        int mOffset;

        public GetAudienceListTask(int offset) {
            super(LivingRecordActivity.this, MsRequest.FIND_AUDIENCE_LIST);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).append("&live_id=").append(mLiveInfo.id).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ArrayList<LiveMemberInfo> follows = JsonUtil.getArray(response.getJsonArray(), LiveMemberInfo.TRANSFORMER);
                if (mOffset <= 0) {
                    mFocusUsers.clear();
                }
                mFocusUsers.addAll(follows);
                mHeadAdapter.notifyDataSetChanged();
            }
        }
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
                Picasso.with(LivingRecordActivity.this)
                        .load(user.avatar)
                        .into(avatar);
            } else {
                Picasso.with(LivingRecordActivity.this)
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
                }
            });
            return convertView;
        }
    }

    /**
     * 管理员列表适配器
     **/
    class ManaListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return managerList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_manager_list, parent, false);
            }
            getItem(position);
            return null;
        }
    }

    private void fetchLiveMembersList(boolean isRefresh) {
        int offset = isRefresh ? 0 : mHeadAdapter.getCount();
        new GetAudienceListTask(offset).executeLong();
    }

    private class GetHostInfoTask extends MsTask {

        public GetHostInfoTask() {
            super(LivingRecordActivity.this, MsRequest.GET_HOST_USER_INFO);
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
                    Picasso.with(LivingRecordActivity.this).load(mHostUserInfo.avatar).into(mIvAvatar);
                } else {
                    Picasso.with(LivingRecordActivity.this).load(mHostUserInfo.gender == 0 ?
                            R.drawable.chat_botton_bg_favicongirl : R.drawable.chat_botton_bg_faviconboy)
                            .into(mIvAvatar);
                }
                mTotalIncome = mHostUserInfo.gold;
                mTvIncome.setText(mHostUserInfo.gold + "");
                mTvName.setText(mHostUserInfo.nickName);
            }
        }
    }

    private class GiftAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mGifts.size();
        }

        @Override
        public GiftInfo getItem(int i) {
            return mGifts.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            View v = null;
            if (view == null) {
                v = mInflater.inflate(R.layout.list_item_living_gift, viewGroup, false);
            } else {
                v = view;
            }
            GiftInfo gift = getItem(position);
            TextView mTvGift = (TextView) v.findViewById(R.id.tv_gift_content);
            TextView mtvTime = (TextView) v.findViewById(R.id.tv_time);
            TextView mtvWheatNum = (TextView) v.findViewById(R.id.tv_wheat_num);
            String content = getString(R.string.live_gift_list_content, gift.senderName,
                    gift.giftNum, gift.giftName);
            CharSequence cs = Utils.getColoredText(content, gift.senderName, 0XFFFFCC00);
            mTvGift.setText(Utils.getColoredText(cs, gift.giftName, 0XFF4EFF6E));
            mtvWheatNum.setText(Utils.getColoredText(getString(R.string.live_gift_values,
                    "+" + gift.giftTotalValue), "+" + gift.giftTotalValue, 0XFFFFCC00));
            mtvTime.setText(Utils.getPostShowTimeString(gift.giftTime));
            return v;
        }
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

    private class GetGiftListTask extends MsTask {
        int mOffset;

        public GetGiftListTask(int offset) {
            super(LivingRecordActivity.this, MsRequest.GET_GIFT_LIST);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("live_id=").append(mLiveInfo.id).append("&offset=").append(mOffset).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrGiftList.onRefreshComplete();
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                ArrayList<GiftInfo> gifts = JsonUtil.getArray(ja, GiftInfo.TRANSFORMER);
                if (mOffset <= 0) {
                    mGifts.clear();
                }
                mGifts.addAll(gifts);
                if (mGifts != null && mGifts.size() > 0) {
                    mPtrGiftList.setVisibility(View.VISIBLE);
                    mTvNoGiftsNotice.setVisibility(View.GONE);
                    mGiftAdapter.notifyDataSetChanged();

                } else {
                    mPtrGiftList.setVisibility(View.GONE);
                    mTvNoGiftsNotice.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void fetchGifts(boolean refresh) {
        int offset = refresh ? 0 : mGiftAdapter.getCount();
        new GetGiftListTask(offset).executeLong();
    }

    private class GetEndInfoTask extends MsTask {

        public GetEndInfoTask() {
            super(LivingRecordActivity.this, MsRequest.GET_LIVE_END_INFO);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("live_id=").append(mLiveInfo.id).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mFlBlur.setVisibility(View.VISIBLE);
            if (response.isSuccessful()) {
                JSONObject json = response.getJsonObject();
                mLiveEndInfo = new LiveEndInfo();
                mLiveEndInfo.spectatorNum = json.optInt("menNumbers");
                mLiveEndInfo.time = json.optString("time");
                mLiveEndInfo.income = json.optInt("income");
                mLiveEndInfo.preview = json.optString("prev_url");
            }
            mFreshHandler.sendEmptyMessage(UPDATE_END_VIEW);
        }
    }

    class LiveEndInfo {
        public int spectatorNum;
        public String time;
        public int income;
        public String preview;
    }

    private class GetConnectionList extends MsTask {

        public GetConnectionList() {
            super(LivingRecordActivity.this, MsRequest.LIVE_CONNECTION_LIST);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("live_id=").append(mLiveInfo.id).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mRequestList = JsonUtil.getArray(response.getJsonArray(), RequestInfo.TRANSFORMER);
                mLlConnectionList.setVisibility(View.VISIBLE);
                isConnectionListShow = true;
                mConAdapter.notifyDataSetChanged();
            } else {
            }
        }
    }

    private class ConnectionTask extends MsTask {

        public ConnectionTask(RequestInfo request) {
            super(LivingRecordActivity.this, MsRequest.LIVE_CONNECTION);
            mRequest = request;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("connection_id=").append(mRequest.connectionId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mConnetionActivityId = response.getJsonObject().optString("activity_id");
                mCurrentRequest = mRequest;
                startStreamCheck();
            }
        }
    }

    private class ConnectionAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mRequestList.size();
        }

        @Override
        public RequestInfo getItem(int i) {
            return mRequestList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = mInflater.inflate(R.layout.list_item_connection_user, viewGroup, false);
            AvatarView mAvatar = (AvatarView) v.findViewById(R.id.iv_avatar);
            TextView mTvName = (TextView) v.findViewById(R.id.tv_name);
            RequestInfo request = getItem(i);
            if (!TextUtils.isEmpty(request.requestAvatar)) {
                Picasso.with(LivingRecordActivity.this)
                        .load(request.requestAvatar)
                        .into(mAvatar);
            }
            mTvName.setText(request.requestNick);
            v.setTag(request);
            v.setOnClickListener(LivingRecordActivity.this);
            return v;
        }
    }


    private void startPullStream() {
        mFlConnectionContainer.setVisibility(View.VISIBLE);
//        mLlWaitingUi.setVisibility(View.VISIBLE);
        startPlayLive();
    }

    private void startPlayLive() {
        mBundle = new Bundle();
        mBundle.putInt(PlayProxy.PLAY_MODE, EventPlayProxy.PLAYER_ACTION_LIVE);
        mBundle.putString(PlayProxy.PLAY_ACTIONID, mConnetionActivityId);
        mBundle.putBoolean(PlayProxy.PLAY_USEHLS, false);
        mPlayContext = new PlayContext(this);
        mPlayContext.setUsePlayerProxy(true);
        createOnePlayer(null);
        mPlayer.setParameter(mPlayer.getPlayerId(), mBundle);
        mPlayer.setOnPlayStateListener(mPlayerStateListener);
    }

    private void createOnePlayer(Surface surface) {
        mPlayer = new LetvPlayer();
        mPlayer.setPlayContext(mPlayContext); //关联playContext
        mPlayer.setParameter(mPlayer.getPlayerId(), mBundle);
        mPlayer.init();
        mPlayer.setOnPlayStateListener(mPlayerStateListener);
        mPlayer.setDisplay(surface);
        mPlayer.prepareAsync();
    }


    private void initNormalVideoView() {
        if (mSurfaceView2 == null || !(mSurfaceView2 instanceof ReSurfaceView)) {
            ReSurfaceView videoView = new ReSurfaceView(this);
            videoView.setVideoContainer(null);
            this.mSurfaceView2 = videoView;
            addVideoView();
        }
    }

    private void addVideoView() {
        mSurfaceView2.getHolder().addCallback(new SurfaceHolder.Callback() {

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
        });
        mSurfaceView2.setZOrderOnTop(true);
        mSurfaceView2.setZOrderMediaOverlay(true);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        RelativeLayout videoContainer = (RelativeLayout) findViewById(R.id.rl_2nd_container);
        videoContainer.addView(mSurfaceView2, params);
    }


    private void stopPlayer() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
    }

    private OnPlayStateListener mPlayerStateListener = new OnPlayStateListener() {
        @Override
        public void videoState(int state, Bundle bundle) {
            Utils.logD(TAG, "video state :" + state);
            if (mPlayer == null) {
                return;
            }
            switch (state) {
                case ISplayer.MEDIA_EVENT_VIDEO_SIZE:
                    if (mSurfaceView2 != null && mPlayer != null) {
                    }
                    break;
                case ISplayer.MEDIA_EVENT_PREPARE_COMPLETE:
                    if (mPlayer != null) {
//                        mLlWaitingUi.setVisibility(View.GONE);
                        mPlayer.start();
                        Utils.logD(TAG, "开始播放直播视频...");
                    }
                    break;
                case EventPlayProxy.PROXY_WATING_SELECT_ACTION_LIVE_PLAY:// 当收到该事件后，用户可以选择优先播放的活动直播
                    ActionInfo actionInfo = mPlayContext.getActionInfo();
//                    // 查找正在播放的直播 或者 可以秒转点播的直播信息
//                    System.out.println("actionInfo == " + actionInfo == null);
                    if (actionInfo == null) {
                        return;
                    }
                    com.lecloud.entity.LiveInfo liveInfo = actionInfo.getFirstCanPlayLiveInfo();
//                    LiveInfo liveInfo = actionInfo.getFirstCanPlayLiveInfo();
                    if (liveInfo != null) {
                        mPlayContext.setLiveId(liveInfo.getLiveId());
                    }
                    break;
                case ISplayer.PLAYER_EVENT_PREPARE_VIDEO_VIEW:
                    initNormalVideoView();
                    break;
                case ISplayer.MEDIA_EVENT_PLAY_COMPLETE:
                    //直播结束
//                    new GetEndInfoTask().executeLong();
                    break;
                case EventPlayProxy.PROXY_REQUEST_ERROR:
                    break;
                default:
                    break;
            }
        }
    };

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

    private void getStreamStatus(String actId) {
        new GetStreamStatus(actId).executeLong();
    }

    private class MenuAdapter extends BaseAdapter {

        private ArrayList<LivingMenuInfo> mMenuList;

        public MenuAdapter(ArrayList<LivingMenuInfo> menuList) {
            mMenuList = menuList;
        }

        @Override
        public int getCount() {
            return mMenuList.size();
        }

        @Override
        public LivingMenuInfo getItem(int i) {
            return mMenuList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = mInflater.inflate(R.layout.list_item_menu_info, viewGroup, false);
            TextView mTvMenuContent = (TextView) v.findViewById(R.id.tv_menu_count);
            mTvMenuContent.setText(getString(getItem(i).menuContent));
            v.setTag(getItem(i).type);
            v.setOnClickListener(mMunuClickListen);
            return v;
        }
    }

    private View.OnClickListener mMunuClickListen = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mMenuList.setVisibility(View.GONE);
            switch ((int) view.getTag()) {
                case LivingMenuInfo.MENU_TYPE_CHECK_PROFILE:
                    Intent iProfile = new Intent(LivingRecordActivity.this, NewProfileActivity.class);
                    UserInfo userInfo = new UserInfo();
                    mUserInfo.userId = mCurrentUser.userId;
                    iProfile.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
                    startActivity(iProfile);
                    break;
                case LivingMenuInfo.MENU_TYPE_SET_MANAGER:
                    new SetAdminTask().executeLong();
                    break;
                case LivingMenuInfo.MENU_TYPE_CANCEL_SET_MANAGER:
                    new RemoveAdminTask().executeLong();
                    break;
                case LivingMenuInfo.MENU_TYPE_Gag:
                    showSilenceDialog();
                    break;
                case LivingMenuInfo.MENU_TYPE_CANCEL_Gag:
                    new CancelShutupTask().executeLong();
                    break;
                case LivingMenuInfo.MENU_TYPE_CANCEL:
                    mMenuList.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    };

    private class GetCurrentUserAdminStatus extends MsTask {

        public GetCurrentUserAdminStatus() {
            super(LivingRecordActivity.this, MsRequest.LIVE_GET_ADMIN_STATUS);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("live_id=").append(mLiveInfo.id).append("&query_uid=").append(mCurrentUser.userId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mCurrentStatusInfo = LiveAdminStatusInfo.fromJson(response.getJsonObject());
                showBottomView(mFrView);
                hideBottomView(mLlMenu);
            }
        }
    }

    private void showBottomView(View v) {
        if (v.getVisibility() != View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.live_up_animation);
            v.startAnimation(animation);
            v.setVisibility(View.VISIBLE);
        }
        if (mCurrentUserId == mLiveInfo.uid) {
            mTvManager.setVisibility(View.GONE);
            mLlButton.setVisibility(View.GONE);
        } else {
            mTvManager.setVisibility(View.VISIBLE);
            mLlButton.setVisibility(View.VISIBLE);
        }
        if (mCurrentUser != null) {
            mTvFansNum.setText(mCurrentUser.fansCount + "");
            mTvFollowNum.setText(mCurrentUser.followCount + "");
//        mTvContributeNum.setText();
            mTvUserName.setText(mCurrentUser.getNickname());
        }
        if (mCurrentStatusInfo != null && mCurrentStatusInfo.isAdmin) {
            mTvManageMent.setVisibility(View.VISIBLE);
        } else {
            mTvManageMent.setVisibility(View.GONE);
        }
//        mTvFollow.setText(getString(mCurrentStatusInfo.isFollow ? R.string.news_source_unfollow : R.string.tribe_collected_add));
        if (mCurrentStatusInfo != null && mCurrentStatusInfo.isFollow) {
            mTvFollow.setText("取消关注");
        } else {
            mTvFollow.setText("+关注");
        }
    }

    private void hideBottomView(View v) {
        if (v.getVisibility() == View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.live_down_animation);
            v.startAnimation(animation);
            v.setVisibility(View.GONE);
        }
    }

    private class AddShutupTask extends MsTask {

        public AddShutupTask() {
            super(LivingRecordActivity.this, MsRequest.LIVE_MAKE_SILENCE);
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


    private class CancelShutupTask extends MsTask {

        public CancelShutupTask() {
            super(LivingRecordActivity.this, MsRequest.LIVE_CANCEL_MAKE_SILENCE);
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

    private class GetStreamStatus extends MsTask {

        private String activityId;

        public GetStreamStatus(String actId) {
            super(LivingRecordActivity.this, MsRequest.ACTIVITY_STREAM_STATUS);
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

                } else {
                    if (activityId.equals(mConnetionActivityId)) {
                        stopStreamCheck();
                        startPullStream();
                    }
                }
            }
        }
    }

    private class SetAdminTask extends MsTask {

        public SetAdminTask() {
            super(LivingRecordActivity.this, MsRequest.LIVE_ADD_ADMIN);

        }

        @Override
        protected String buildParams() {
            return new StringBuilder("admin_uid=").append(mCurrentUser.userId).toString();
        }


        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mCurrentStatusInfo.isAdmin = true;
            } else {
                mCurrentStatusInfo.isAdmin = false;
            }
        }
    }

    private class CloseConnectionTask extends MsTask {

        public CloseConnectionTask() {
            super(LivingRecordActivity.this, MsRequest.LIVE_CONNECTION_CLOSE);
        }

        @Override
        protected String buildParams() {
            return "connection_id=" + mRequest.connectionId;
        }


        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                closeConnectionWindow();
            }
        }
    }

    private class RemoveAdminTask extends MsTask {

        public RemoveAdminTask() {
            super(LivingRecordActivity.this, MsRequest.LIVE_DELETE_ADMIN);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("admin_uid=").append(mCurrentUser.userId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mCurrentStatusInfo.isAdmin = false;
            } else {
                mCurrentStatusInfo.isAdmin = true;
            }
        }
    }

    @Override
    public void onUploadSuccess(File file, byte[] data, String url, String objectKey) {
        //上传阿里云成功
    }

    @Override
    public void onUploadProgress(File file, byte[] data, int byteCount, int totalSize) {

    }

    @Override
    public void onUploadFailure(File file, byte[] data, String errMsg) {
        //上传阿里云失败
    }

    private void closeConnection() {
        if (mRequest == null) {
            return;
        }
        new CloseConnectionTask().executeLong();
    }

    private void closeConnectionWindow() {
        mFlConnectionContainer.setVisibility(View.GONE);
        stopPlayer();
    }

    private void showSilenceDialog() {
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
            Picasso.with(LivingRecordActivity.this).load(R.drawable.bottom_qq).into(mIvShareIcon);
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

}
