package com.tjut.mianliao.live;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jess.ui.TwoWayAbsListView;
import com.jess.ui.TwoWayGridView;
import com.lecloud.entity.ActionInfo;
import com.letv.controller.LetvPlayer;
import com.letv.controller.PlayContext;
import com.letv.controller.PlayProxy;
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
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.LiveDialog;
import com.tjut.mianliao.component.LiveGiftPicker;
import com.tjut.mianliao.contact.ContactUpdateCenter;
import com.tjut.mianliao.contact.FollowUserManager;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.ChatRecord;
import com.tjut.mianliao.data.HostUserInfo;
import com.tjut.mianliao.data.LiveAdminStatusInfo;
import com.tjut.mianliao.data.LiveInfo;
import com.tjut.mianliao.data.LiveMemberInfo;
import com.tjut.mianliao.data.LivingMenuInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.explore.GoldDepositsActivity;
import com.tjut.mianliao.profile.NewProfileActivity;
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

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by YoopWu on 2016/7/8 0008.
 */
public class ReplayLiveActivity extends BaseActivity implements OnPlayStateListener,
        SurfaceHolder.Callback, View.OnClickListener, SeekBar.OnSeekBarChangeListener,
        TwoWayAbsListView.OnScrollListener, LiveGiftPicker.OnLiveGiftClickListener,
        ChatHelper.MessageReceiveListener, FollowUserManager.OnUserFollowListener {

    public static final String EXT_VU = "ext_vu";
    private static final String TAG = "ReplayLiveActivity";

    @ViewInject(R.id.fl_root)
    private FrameLayout mFlRoot;
    @ViewInject(R.id.list_live_msg)
    private ListView mMsgListView;
    @ViewInject(R.id.gift_anim_view)
    private GiftAnimView mGiftAnimView;
    @ViewInject(R.id.iv_avatar)
    private AvatarView mAvatarView;
    @ViewInject(R.id.iv_type_icon)
    private ImageView mIvTypeIcon;
    @ViewInject(R.id.tv_name)
    private TextView mTvName;
    @ViewInject(R.id.tv_follow_count)
    private TextView mTvVisitCount;
    @ViewInject(R.id.iv_living_follow)
    private ImageView mIvFollow;
    @ViewInject(R.id.follow_gridview)
    private TwoWayGridView mGvVisitor;
    @ViewInject(R.id.sb_replay)
    private SeekBar mSeekBar;
    @ViewInject(R.id.tv_time)
    private TextView mTvTime;
    @ViewInject(R.id.live_gift_emotions)
    private LiveGiftPicker mGiftPicker;
    @ViewInject(R.id.tv_date_today)
    private TextView mTvDateDay;
    @ViewInject(R.id.ll_operate)
    private LinearLayout mLlOperate;
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
    @ViewInject(R.id.fr_userinformation_botoom)
    private FrameLayout frView;
    @ViewInject(R.id.tv_follow_user)
    private TextView mTvFollow;
    @ViewInject(R.id.tv_profile)
    private TextView mTvtoProfile;
    @ViewInject(R.id.tv_manager)
    private TextView mTvManager;
    @ViewInject(R.id.tv_income)
    private TextView mTvIncome;
    @ViewInject(R.id.ll_income)
    private LinearLayout mLlIncome;

    private Bundle mBundle;
    private PlayContext mPlayContext;
    private ISplayer mPlayer;
    private SurfaceView mSurfaceView;
    private LiveInfo mLiveInfo;

    private LiveChatAdapter mAdapter;
    private HeadAdapter mHeadAdapter;
    private ChatHelper mChatHelper;

    private int mTotalIncome;
    private HostUserInfo mHostUserInfo;
    private boolean isFollow;

    private ArrayList<LiveMemberInfo> mFocusUsers;

    private String mLiveVu;

    private Timer mTimer;
    private TimerTask mTimerTask;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            updateVideoPlayingInfo();
        }
    };
    private LiveGift mChoosedGift;
    private int mGiftCount;
    private UserInfo mUserInfo;
    private UserInfoManager mUserInfoManager;
    private int mShowGiftNum;
    private int mTimerCount;
    private int mGold;
    private String mGroupId;
    private String mChatId;
    private String mChatTarget;
    private boolean mIsMe;

    private SnsHelper mSnsHelp;

    private UserInfo mCurrentUser;
    private int mCurrentUserId;
    private LiveAdminStatusInfo mCurrentStatusInfo, mStatusInfo;
    private FollowUserManager mFollowUserManager;
    private LiveDialog mMoneyDialog;

    private void updateVideoPlayingInfo() {
        if (mPlayer != null) {
            long duration = mPlayer.getDuration();
            long currentPosition = mPlayer.getCurrentPosition();
            if (duration == 0) {
                mSeekBar.setProgress(0);
                mTvTime.setText("00:00/00:00");
            } else {
                Utils.logD(TAG, "progress=" + (int) ((currentPosition  * 1.0 / duration) * 100));
                mSeekBar.setProgress((int) ((currentPosition  * 1.0 / duration) * 100));
                mTvTime.setText(formatTime(duration, currentPosition));
            }
        }
    }

    @Override
    protected int getLayoutResID() {
        return 0;
    }

    @Override
    protected int getBaseLayoutResID() {
        return R.layout.activity_replay_live;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mUserInfoManager = UserInfoManager.getInstance(this);
        mChatHelper = ChatHelper.getInstance(this);
        mChatHelper.registerReceiveListener(this);
        mSnsHelp = SnsHelper.getInstance();
        mFollowUserManager = FollowUserManager.getInstance(this);
        mFollowUserManager.registerOnUserFollowListener(this);
        mFocusUsers = new ArrayList<>();
        mLiveVu = getIntent().getStringExtra(EXT_VU);
        mLiveInfo = getIntent().getParcelableExtra(LivingActivity.DATA_LIVE_INFO);
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

        mUserInfo = AccountInfo.getInstance(this).getUserInfo();
        mIsMe = mLiveInfo.uid == mUserInfo.userId;
        mBundle = new Bundle();
        mBundle.putInt(PlayProxy.PLAY_MODE, EventPlayProxy.PLAYER_VOD);// 点播业务
        mBundle.putString(PlayProxy.PLAY_UUID, Utils.getLiveUu());
        mBundle.putString(PlayProxy.PLAY_VUID, mLiveVu);
        mBundle.putString(PlayProxy.PLAY_USERKEY, Utils.getLeSecretKey());
        mPlayContext = new PlayContext(this);
        mPlayContext.setUsePlayerProxy(true);
        createOnePlayer(null);
        mPlayer.setParameter(mPlayer.getPlayerId(), mBundle);
        mPlayer.setOnPlayStateListener(this);
        mAdapter = new LiveChatAdapter(this);
        mMsgListView.setAdapter(mAdapter);
        mGvVisitor.setOnScrollListener(this);
        mHeadAdapter = new HeadAdapter();
        mGvVisitor.setAdapter(mHeadAdapter);
        mGiftPicker.fetchLiveGift();
        mGiftPicker.registerGiftClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
        mTvFollow.setOnClickListener(this);
        mTvtoProfile.setOnClickListener(this);
        mLlIncome.setOnClickListener(this);
        updateView();
        enterLive();
        if (mIsMe) {
            findViewById(R.id.iv_gift).setVisibility(View.GONE);
        }
    }

    private void updateView() {
        new GetIncomeTask().executeLong();
        new GetHostInfoTask().executeLong();
        fetchLiveMembersList(true);
    }

    private void fetchLiveMembersList(boolean isRefresh) {
        int offset = isRefresh ? 0 : mHeadAdapter.getCount();
        new GetAudienceListTask(offset).executeLong();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gift_anim_view:
            case R.id.list_live_msg:
            case R.id.videoContainer:
            case R.id.fl_root:
                hideGiftPickerAnimation();
                mSnsHelp.closeShareBoard();
                if (frView.getVisibility() == View.VISIBLE) {
                    hideAllView();
                }
                showButtonParmAnimation();
                break;
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_share:
                mSnsHelp.openShareBoard(this, mLiveInfo, "");
                break;
            case R.id.iv_play:
//                showMoneyDialog();
                break;
            case R.id.iv_gift:
                showOrHideGiftPicker(true);
                break;
            case R.id.iv_avatar:
                mCurrentUserId = mLiveInfo.uid;
                UserInfo user = mUserInfoManager.getUserInfo(mLiveInfo.uid);
                if (user != null) {
                    mCurrentUser = user;
                    new GetCurrentUserAdminStatus().executeLong();
                }
                break;
            case R.id.tv_profile:
                Intent iProfile = new Intent(ReplayLiveActivity.this, NewProfileActivity.class);
                UserInfo userInfo = new UserInfo();
                mUserInfo.userId = mCurrentUser.userId;
                iProfile.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
                startActivityForResult(iProfile, 0);
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
            case R.id.ll_income:
                Intent incomeIntent = new Intent(ReplayLiveActivity.this, LiveGainRankActivity.class);
                startActivity(incomeIntent);
                break;
            default:
                break;
        }
    }

    private void showOrHideGiftPicker(boolean showGiftPicker) {
        if (showGiftPicker)
            showGiftPickerAnimation();
        else
            hideGiftPickerAnimation();
    }

    private String formatTime(long duration, long currentTime) {
        int durSec = (int) (duration / 1000 % 60);
        int durMin = (int) (duration / 1000 / 60 % 60);
        int durHour = (int) (duration / 1000 / 60 / 60 % 60);
        int curSec = (int) (currentTime / 1000 % 60);
        int curMin = (int) (currentTime / 1000 / 60 % 60);
        int curHour = (int) (currentTime / 1000 / 60 / 60 % 60);
        return String.format("%02d:%02d:%02d/%02d:%02d:%02d", curHour, curMin, curSec, durHour,
                durMin, durSec);
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
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        RelativeLayout videoContainer = (RelativeLayout) findViewById(R.id.videoContainer);
        videoContainer.addView(mSurfaceView, params);
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
                    startPlayTimer();
                    mPlayer.start();
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
                stopPlayerTimer();
                break;
            case EventPlayProxy.PROXY_REQUEST_ERROR:

                break;
            default:
                break;
        }
    }

    private void stopPlayerTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            mTimerTask = null;
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


    private void stopPlayer() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void startPlayTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            mTimerTask = null;
        }
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendMessage(Message.obtain());
            }
        };
        mTimer.schedule(mTimerTask, 0, 10);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            seekToPlay(progress);
        }
    }

    private void seekToPlay(int progress) {
        long currentPosition = mPlayer.getDuration() * progress / 100;
        mPlayer.seekTo(currentPosition);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onScrollStateChanged(TwoWayAbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            fetchLiveMembersList(false);
        }
    }

    @Override
    public void onScroll(TwoWayAbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    private void showGiftPickerAnimation() {
        if (mGiftPicker.getVisibility() == View.VISIBLE) {
            return;
        }
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.live_up_animation);
        mGiftPicker.startAnimation(animation);
        mGiftPicker.setVisible(true);
        mGold = mUserInfo.gold;
//        mTvMyGold.setText(mUserInfo.gold + "");
        hideButtonParmAnimation();
    }

    private void hideGiftPickerAnimation() {
        if (mGiftPicker.getVisibility() != View.VISIBLE) {
            return;
        }
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.live_down_animation);
        mGiftPicker.startAnimation(animation);
        mGiftPicker.setVisible(false);
        showButtonParmAnimation();
    }

    private void hideButtonParmAnimation() {
        if (mLlOperate.getVisibility() != View.VISIBLE) {
            return;
        }
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.live_down_animation);
        mLlOperate.startAnimation(animation);
        mLlOperate.setVisibility(View.INVISIBLE);
    }

    private void showButtonParmAnimation() {
        if (mLlOperate.getVisibility() == View.VISIBLE) {
            return;
        }
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.live_up_animation);
        mLlOperate.startAnimation(animation);
        mLlOperate.setVisibility(View.VISIBLE);
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
        Picasso.with(ReplayLiveActivity.this).load(mUserInfo.getAvatar())
                .placeholder(mUserInfo.defaultAvatar())
                .into(ivAvatar);
        Picasso.with(ReplayLiveActivity.this).load(gift.icon).into(ivGift);
        if (userInfo != null) {
            tvUser.setText(userInfo.getDisplayName(ReplayLiveActivity.this));
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


    private void sendGift(int giftId, int count) {
        new SendGiftTask(giftId, count).executeLong();
    }


    @Override
    public void onGiftClick(LiveGift gift) {
        {
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

    private void stopCount() {
        mTimer.cancel();
        mTimer = null;
        mTimerTask = null;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGiftPicker.hideCountView();
            }
        });
        sendGift(mChoosedGift.giftId, mGiftCount);
        mGiftAnimView.removeGiftCountInfo(mChoosedGift, mUserInfo.userId);
        mGiftCount = 0;
    }

    private void createTarget() {
        mChatTarget = mChatId + "@groupchat." + Utils.getChatServerDomain();
    }

    private void enterLive() {
        new EnterLiveTask().executeLong();
    }

    @Override
    public void onMessageReceived(ChatRecord record) {
        if (record.isTarget(mChatTarget)) {
            operateChatRecord(record);
        }
    }

    private void operateChatRecord(final ChatRecord record) {
        switch (record.type) {
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
            case ChatRecord.CHAT_TYPE_SHARE_MSG:
                mAdapter.add(record);
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

    @Override
    public void onMessageReceiveFailed(ChatRecord record) {

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

    private class SendGiftTask extends MsTask {

        private int mGiftId;
        private int mGiftNumber;

        public SendGiftTask(int giftId, int count) {
            super(ReplayLiveActivity.this, MsRequest.LIVE_SEND_GIFT);
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
                Picasso.with(ReplayLiveActivity.this)
                        .load(user.avatar)
                        .into(avatar);
            } else {
                Picasso.with(ReplayLiveActivity.this)
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
//                    hideBottomView(mLlBottom);
                    hideAllView();
                }
            });
            return convertView;
        }
    }

    private void hideAllView() {
//        if (mLlReplyInput.isShown()) {
//            hideBottomView(mLlReplyInput);
//        }
//        if (mTvConnection.isShown()) {
//            hideBottomView(mTvConnection);
//        }
        if (mGiftPicker.isShown()) {
            hideBottomView(mGiftPicker);
        }
        if (frView.getVisibility() == View.VISIBLE) {
            hideBottomView(frView);
        }
    }

    private void hideBottomView(View v) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.live_down_animation);
        v.startAnimation(animation);
        v.setVisibility(View.INVISIBLE);

    }

    private class GetIncomeTask extends MsTask {

        public GetIncomeTask() {
            super(ReplayLiveActivity.this, MsRequest.LIVE_INCOME);
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
//                mTodayIncome = json.optInt("today_income");
                mTvIncome.setText(mTotalIncome + "");
            }
        }
    }

    private class GetHostInfoTask extends MsTask {

        public GetHostInfoTask() {
            super(ReplayLiveActivity.this, MsRequest.GET_HOST_USER_INFO);
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
                    Picasso.with(ReplayLiveActivity.this).load(mHostUserInfo.avatar).into(mAvatarView);
                } else {
                    Picasso.with(ReplayLiveActivity.this).load(mHostUserInfo.gender == 0 ?
                            R.drawable.chat_botton_bg_favicongirl : R.drawable.chat_botton_bg_faviconboy)
                            .into(mAvatarView);
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

    private class GetAudienceListTask extends MsTask {
        int mOffset;

        public GetAudienceListTask(int offset) {
            super(ReplayLiveActivity.this, MsRequest.FIND_AUDIENCE_LIST);
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


    private class EnterLiveTask extends MsTask {

        public EnterLiveTask() {
            super(ReplayLiveActivity.this, MsRequest.ENTER_LIVE);
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
            }
        }
    }

    private void exitLive() {
        new ExitLiveTask().executeLong();
        finish();
    }

    private class ExitLiveTask extends MsTask {

        public ExitLiveTask() {
            super(ReplayLiveActivity.this, MsRequest.EXIT_LIVE);
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

    private class GetCurrentUserAdminStatus extends MsTask {

        public GetCurrentUserAdminStatus() {
            super(ReplayLiveActivity.this, MsRequest.LIVE_GET_ADMIN_STATUS);
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

    private void showBottomView(View v) {
        hideButtonParmAnimation();
        mTvManager.setVisibility(View.GONE);

        if (mCurrentUserId == AccountInfo.getInstance(this).getUserId()) {
            mLlButton.setVisibility(View.GONE);
        } else {
            mLlButton.setVisibility(View.VISIBLE);
        }
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFollowUserManager.unregisterOnUserFollowListener(this);
    }

    private void showMoneyDialog () {
        if (mMoneyDialog == null) {
            mMoneyDialog = new LiveDialog(ReplayLiveActivity.this);
            mMoneyDialog.setText(getString(R.string.live_money_notice));
            mMoneyDialog.setPositiveButton(R.string.live_to_recharge, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(ReplayLiveActivity.this, GoldDepositsActivity. class);
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
