package com.tjut.mianliao.chat;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.MapActivity;
import com.tjut.mianliao.NotificationHelper;
import com.tjut.mianliao.NotificationHelper.NotificationType;
import com.tjut.mianliao.R;
import com.tjut.mianliao.cocos2dx.CocosAvatarView;
import com.tjut.mianliao.cocos2dx.CocosAvatarView.OnAvatarLoadedListener;
import com.tjut.mianliao.component.ChatExtPicker;
import com.tjut.mianliao.component.ChatExtPicker.ChatExtItem;
import com.tjut.mianliao.component.EmotionPicker;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.MlEditText;
import com.tjut.mianliao.component.MlWebView;
import com.tjut.mianliao.component.MlWebView.WebViewListener;
import com.tjut.mianliao.component.ProListView;
import com.tjut.mianliao.contact.ContactUpdateCenter;
import com.tjut.mianliao.contact.ContactUpdateCenter.UpdateType;
import com.tjut.mianliao.contact.UserEntryManager;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.ChatRecord;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.Emotion;
import com.tjut.mianliao.data.GroupInfo;
import com.tjut.mianliao.data.GroupMember;
import com.tjut.mianliao.data.HighTheme;
import com.tjut.mianliao.data.IMResource;
import com.tjut.mianliao.data.LatLngWrapper;
import com.tjut.mianliao.data.UserGroupMap;
import com.tjut.mianliao.data.contact.UserEntry;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.tribe.TribeChatRoomInfo;
import com.tjut.mianliao.explore.TriggerEventTask;
import com.tjut.mianliao.im.AvatarMarketActivity;
import com.tjut.mianliao.im.GroupChatManager;
import com.tjut.mianliao.im.GroupChatManager.GroupChatListener;
import com.tjut.mianliao.im.IMAudioManager;
import com.tjut.mianliao.im.IMResourceManager;
import com.tjut.mianliao.im.ShareLocationActivity;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.tribe.TribeChatManager;
import com.tjut.mianliao.tribe.TribeChatManager.TribeChatListener;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.GetImageHelper.ImageResultListener;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.StringUtils;
import com.tjut.mianliao.util.Utils;
import com.tjut.mianliao.xmpp.ChatHelper;
import com.tjut.mianliao.xmpp.ChatHelper.MessageReceiveListener;
import com.tjut.mianliao.xmpp.ChatHelper.MessageSendListener;
import com.tjut.mianliao.xmpp.ConnectionManager;
import com.tjut.mianliao.xmpp.ConnectionManager.ConnectionObserver;
import com.umeng.analytics.MobclickAgent;

public class ChatActivity extends BaseActivity implements ConnectionObserver, MessageReceiveListener,
        MessageSendListener, OnAvatarLoadedListener, WebViewListener, TextWatcher, ImageResultListener,
        TribeChatListener, EmotionPicker.EmotionListener, ChatExtPicker.ChatExtListener,
        IMResourceManager.IMResourceListener, GroupChatListener, ContactUpdateCenter.ContactObserver, OnTouchListener,
        View.OnClickListener, DialogInterface.OnClickListener {

    public static final String EXTRA_CHAT_TARGET = "extra_chat_target";
    public static final String EXTRA_SHOW_PROFILE = "extra_show_profile";
    public static final String EXTRA_CHAT_ISGOUPCHAT = "extra_chat_isgoupchat";
    public static final String EXTRA_CHAT_IMAGEURLS = "extra_chat_imageurls";
    public static final String EXTRA_GROUPCHAT_JID = "extra_groupchat_jid";
    public static final String EXTRA_GROUPCHAT_ID = "extra_groupchat_id";
    public static final String EXTRA_TRIBE_ID = "extra_tribe_id";
    public static final String EXTRA_TRIBE_ROOM_INFO = "extra_tribe_room_info";

    public static final String SP_USE_AVATAR = "sp_use_avatar";

    private static final String ANIM_INPUTING_TEXT = "wx";
    private static final String ANIM_RECORDING_VOICE = "yuyin";

    private static final int MSG_VOICE = 10;
    private static final int MSG_STOP_ARMATURE = 20;
    private static final int MSG_TEXT_CHANGE = 30;
    private static final int REQUEST_CODE = 101;

    private static final long STOP_ARMATURE_DELAY_MILLIS = 5000L;
    private static final long TEXT_CHANGE_DELAY_MILLIS = 3000L;

    private static final int DEFAULT_NUM_CHAT_LOAD = 25;

    private static final int MAP_REQUEST = 101;

    private static final int TIME_COUNT_MAX = 60;
    private static final int CANCLLE_SEND_VOICE_MIN_DISTANCE = 100;

    private AccountInfo mAccountInfo;

    private MlEditText mMessageEditor;

    private MlWebView mWvChooseRes;
    private ProListView mChatListView;
    private CocosAvatarView mCocosAvatarView;
    private int mCocosAvatarSize;

    private View mListHeaderView;

    private UserInfo mChatTargetInfo;

    private String mChatTarget;

    private ConnectionManager mConnectionManager;

    private ChatHelper mChatHelper;

    private ChatExtPicker mChatExtPicker;

    private EmotionPicker mEmotionPicker;

    private UserInfoManager mUserInfoManager;

    private UnreadMessageHelper mUnreadMessageHelper;

    private TextView mTvVoiceButton;

    private IMAudioManager mImAudioManager;

    private long mStartTime;

    private long mStopTime;

    private RecordingDialog mRecordDialog;

    private TextView mRecordTime;

    private Timer mTimer;

    private int mTimeCount = 0;

    private boolean mStopRecord, mCancleRecord, mFinishRecord;

    private boolean mIsGroupChat;

    private TimerTask mTask;

    private float mStartX;

    private float mStartY;

    private int mVoiceLength;

    private GetImageHelper mGetImageHelper;

    private ProgressBar mProgressBar, mProgressBarRed;

    private TextView mMsgShow;

    private String newFileName;

    private LightDialog mLocationDialog, mPictureDialog;

    private LatLng mLatLng;

    private ChatAdapter mChatAdapter;

    private IMResourceManager mIMResManager;

    private String mTempGroupName = "", mUserIds = "";

    private GroupChatManager mGroupChatManager;
    private TribeChatManager mTribeChatManager;
    private NotificationHelper mNotificationHelper;

    private String mGroupId;
    private String mCurrentAnimName;
    private ChatHandler mHandler;
    private String mTopicRoomId;
    private Settings mSettings;

    private SharedPreferences mPreferences;
    private boolean mSendAvatar;
    private ArrayList<GroupMember> mMembers = new ArrayList<GroupMember>();
    private UserGroupMap ucp;
    private String mTopicRoomName;
    private boolean mMoreInfoClickble;
    private boolean mIsPaused;

    private LightDialog mPromitDialog;
    private boolean mIsHighGroupChat = false;
    private TribeChatRoomInfo mTribeRoomInfo;

    private boolean mIsTribeChat = false;
    private int mTribeId;

    private Handler mInitHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            initChat();
        }

        ;
    };

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_chat;
    }

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = DataHelper.getSpForData(this);
        mSendAvatar = mPreferences.getBoolean(SP_USE_AVATAR, false);
        mHandler = new ChatHandler(this);
        mSettings = Settings.getInstance(this);
        resetVoiceRecord();
        initFirst();
        mInitHandler.sendMessage(Message.obtain());
        mNotificationHelper.clearNotification(NotificationType.CHAT);
    }

    private void initChat() {
        mTribeRoomInfo = getIntent().getParcelableExtra(EXTRA_TRIBE_ROOM_INFO);
        if (mTribeRoomInfo != null) {
            mTribeId = mTribeRoomInfo.tribeId;
            if (mTribeId != 0) {
                mIsTribeChat = true;
                getTitleBar().setTitle(mTribeRoomInfo.roomName);
            }
            mGroupId = mTribeRoomInfo.roomId + "tribe";
        } else {
            mIsTribeChat = false;
        }
        mImAudioManager = IMAudioManager.getInstance(this);
        mRecordDialog = new RecordingDialog(this, R.style.Translucent_NoTitle);
        mRecordDialog.setCancelable(false);
        mRecordDialog.setCanceledOnTouchOutside(false);
        mRecordTime = (TextView) mRecordDialog.findViewById(R.id.tv_record_time);
        mProgressBar = (ProgressBar) mRecordDialog.findViewById(R.id.pb_circle);
        mProgressBarRed = (ProgressBar) mRecordDialog.findViewById(R.id.pb_circle_red);
        mMsgShow = (TextView) mRecordDialog.findViewById(R.id.tv_show_msg);

        mMessageEditor = (MlEditText) findViewById(R.id.et_message);
        mMessageEditor.setText(mAccountInfo.loadChatDraft(mChatTarget));
        mMessageEditor.addTextChangedListener(this);

        ArrayList<UserEntry> users = getIntent().getParcelableArrayListExtra(AddFriendToGroupActivity.EXTRA_RESULT);

        // Try fetch user info
        if (mChatTargetInfo == null) {
            mUserInfoManager.acquireUserInfo(mChatTarget);
            getTitleBar().showTitleText(R.string.group_chat_title, null);
        } else {
            getTitleBar().showTitleText(mChatTargetInfo.getDisplayName(this), null);
        }

        getTitleBar().showLeftButton(R.drawable.botton_bg_arrow, this);

        if (mSettings.allowEnterToSend()) {
            mMessageEditor.setMlImeOptions(EditorInfo.IME_ACTION_SEND);
            mMessageEditor.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEND
                            || (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                        sendMessage();
                        return true;
                    }
                    return false;
                }
            });
        }

        mChatExtPicker.setChatExtListener(this);
        mEmotionPicker.setEmotionListener(this);
        mMessageEditor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mEmotionPicker.setVisible(false);
                    mChatExtPicker.setVisible(false);
                }
            }
        });

        int themeId = this.getIntent().getIntExtra(HighTheme.BUNDLE_EXTRA, 0);
        mTopicRoomName = getIntent().getStringExtra(HighTheme.BUNDLE_EXTRA_ROOM_NAME);

        if (themeId != 0) {
            mIsGroupChat = true;
            mIsHighGroupChat = true;
            onStartHighTopChat(themeId);
            getTitleBar().showRightButton(R.drawable.icon_group, this);
        }

       /* mTribeRoomInfo = getIntent().getParcelableExtra(EXTRA_TRIBE_ROOM_INFO);
        if (mTribeRoomInfo != null) {
            mTribeId = mTribeRoomInfo.tribeId;
            if (mTribeId != 0) {
                mIsTribeChat = true;
                getTitleBar().setTitle(mTribeRoomInfo.roomName);
            }
            mGroupId = mTribeRoomInfo.roomId + "tribe";
        } else {
            mIsTribeChat = false;
        }
*/
        mChatListView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Utils.hideInput(mMessageEditor);
                    mEmotionPicker.setVisible(false);
                    mChatExtPicker.setVisible(false);
                }
                return false;
            }
        });

        if (mIsGroupChat) {
            if (mGroupId != null) {
                if (mGroupId.contains("tribe")) {
                    mIsTribeChat = true;
                    String rid = mGroupId.substring(0, mGroupId.indexOf("tribe"));
                    mTribeChatManager.getTribeChatRoomInfo(Integer.parseInt(rid));
                } else {
                    mGroupChatManager.getGroupInfo(mGroupId);
                }

            }
        } else if (users != null && users.size() > 0) {
            mIsGroupChat = true;
            onStartMuc(users);
            getTitleBar().showRightButton(R.drawable.icon_group, this);
        }
        mListHeaderView = mInflater.inflate(R.layout.list_header_chat, mChatListView, false);
        mChatListView.addHeaderView(mListHeaderView);
        if (mIsGroupChat) {
            mMessageEditor.setHint(R.string.cht_input_hint_group);
            mCocosAvatarView.setVisibility(View.GONE);
            mChatExtPicker.updateForGroupChat();
        } else {
            mMessageEditor.setHint(R.string.cht_input_hint);
            mChatListView.addFooterView(mInflater.inflate(R.layout.list_footer_chat, mChatListView, false));
        }
        mChatAdapter = new ChatAdapter(this).setChatTarget(mChatTarget);
        mChatAdapter.isGroupChat(mIsGroupChat);
        mChatListView.setAdapter(mChatAdapter);
        loadChats(0);

        int userId = mAccountInfo.getUserId();
        IMResource resBg = mIMResManager.getUsingResource(IMResource.TYPE_BACKGROUND, userId);
        updateBackground(userId, resBg);

        IMResource resBubble = mIMResManager.getUsingResource(IMResource.TYPE_BUBBLE, userId);
        mChatAdapter.updateBubble(userId, resBubble);
        mIMResManager.fetchUsingRes(userId);

        if (mChatTargetInfo != null) {
            userId = mChatTargetInfo.userId;
            resBubble = mIMResManager.getUsingResource(IMResource.TYPE_BUBBLE, userId);
            mChatAdapter.updateBubble(userId, resBubble);
            mIMResManager.fetchUsingRes(userId);
        }
        getTitleBar().showRightButton(mIsGroupChat ? R.drawable.icon_group : R.drawable.icon_persen, this);
    }

    private void initFirst() {
        mGroupChatManager = GroupChatManager.getInstance(this);
        mGroupChatManager.registerGroupChatListener(this);

        mTribeChatManager = TribeChatManager.getInstance(this);
        mTribeChatManager.registerTribeChatListener(this);
        mNotificationHelper = NotificationHelper.getInstance(this);

        mConnectionManager = ConnectionManager.getInstance(this);
        mConnectionManager.registerConnectionObserver(this);
        mUnreadMessageHelper = UnreadMessageHelper.getInstance(this);
        mChatHelper = ChatHelper.getInstance(this);
        mChatHelper.registerReceiveListener(this);
        mChatHelper.registerSendListener(this);
        ContactUpdateCenter.registerObserver(this);

        mIMResManager = IMResourceManager.getInstance(this);
        mIMResManager.registerIMResourceListener(this);
        mIMResManager.updateAvatarAction(false);

        mGetImageHelper = new GetImageHelper(this, this);

        mChatExtPicker = (ChatExtPicker) findViewById(R.id.cep_extention);
        mEmotionPicker = (EmotionPicker) findViewById(R.id.ep_emotions);

        mIsGroupChat = getIntent().getBooleanExtra(EXTRA_CHAT_ISGOUPCHAT, false);

        mAccountInfo = AccountInfo.getInstance(this);

        mGroupId = getIntent().getStringExtra(EXTRA_GROUPCHAT_ID);
        mChatTarget = getIntent().getStringExtra(EXTRA_CHAT_TARGET);
        mTribeId = getIntent().getIntExtra(EXTRA_TRIBE_ID, -1);
        mUserInfoManager = UserInfoManager.getInstance(this);
        // mUserInfoManager.loadUserInfo();
        mChatTargetInfo = mUserInfoManager.getUserInfo(mChatTarget);
        mWvChooseRes = (MlWebView) findViewById(R.id.wv_choose_res);
        mWvChooseRes.setWebViewListener(this);

        mTvVoiceButton = (TextView) findViewById(R.id.tv_voice);
        mTvVoiceButton.setOnTouchListener(this);

        mCocosAvatarView = (CocosAvatarView) findViewById(R.id.cav_avatar);
        CocosAvatarView.setOnAvatarLoadedListener(this);

        mChatListView = ((ProListView) findViewById(R.id.lv_chat));
        mCocosAvatarSize = getResources().getDimensionPixelSize(R.dimen.cocos_avatar_size);
    }

    private void setSendAvatarStatus() {
        Editor edit = mPreferences.edit();
        edit.putBoolean(SP_USE_AVATAR, true);
        edit.commit();
        mSendAvatar = true;
        new TriggerEventTask(this, "avatar_chat").executeLong();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        mConnectionManager.confirmConnect();
        mUnreadMessageHelper.setMessageTarget(mChatTarget);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsPaused) {
            if (mChatTargetInfo == null) {
                mUserInfoManager.acquireUserInfo(mChatTarget);
            }
        }
        mIsPaused = false;
        if (!mIsGroupChat) {
            mWvChooseRes.loadUrl(getChooseUrl(IMResource.TYPE_CHARACTER_ACTION));
            mCocosAvatarView.onResume(this);
            updateAvatar();
        } else if (mGroupId != null) {
            if (mGroupId.contains("tribe")) {
                mIsTribeChat = true;
                String rid = mGroupId.substring(0, mGroupId.indexOf("tribe"));
                mTribeChatManager.getTribeChatRoomInfo(Integer.parseInt(rid));
            } else if (mTopicRoomId == null) {
                mGroupChatManager.getGroupInfo(mGroupId);
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsPaused = true;
        if (!mIsGroupChat) {
            mIMResManager.stopArmature(0);
            mCocosAvatarView.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCancleRecord = true;
        if (!mFinishRecord) {
            mImAudioManager.stopRecord();
            mImAudioManager.deleteAudio(mImAudioManager.getFilePath());
            initVoiceRecord();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        CocosAvatarView.setOnAvatarLoadedListener(null);
        mChatAdapter.destroy();
        mChatHelper.unregisterReceiveListener(this);
        mChatHelper.unregisterSendListener(this);
        ContactUpdateCenter.removeObserver(this);
        mConnectionManager.unregisterConnectionObserver(this);
        mUnreadMessageHelper.setMessageTarget(null);
        mIMResManager.unregisterIMResourceListener(this);
        mAccountInfo.saveChatDraft(mChatTarget, mMessageEditor.getText());
        mGroupChatManager.unregisterGroupChatListener(this);
        mTribeChatManager.unregisterTribeChatListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            mGetImageHelper.handleResult(requestCode, data);
        }
        if (resultCode == RESULT_OK && requestCode == MAP_REQUEST) {
            LatLngWrapper wrapper = data.getParcelableExtra(MapActivity.EXTRA_LOCATION);
            String address = data.getStringExtra(MapActivity.LOCATION_ADDRESS);
            mLatLng = wrapper.latLng;
            sendLocationMessage(mLatLng, address);
        }
        if (resultCode == RESULT_DELETED) {
            setResult(RESULT_DELETED);
            this.finish();
        }
        if (requestCode == REQUEST_CODE) {
            mChatAdapter.removeAll();
            loadChats(0);
        }
        if (resultCode == RESULT_UPDATED) {
            ArrayList<UserEntry> users = data.getParcelableArrayListExtra(AddFriendToGroupActivity.EXTRA_RESULT);
            mIsGroupChat = true;
            onStartMuc(users);
            getTitleBar().showRightButton(R.drawable.icon_group_info, this);
        }
    }

    @Override
    public void onBackPressed() {

        if (mEmotionPicker.isVisible()) {
            mEmotionPicker.setVisible(false);
        } else if (mChatExtPicker.isVisible()) {
            mChatExtPicker.setVisible(false);
        }

        if (mTopicRoomId != null || mIsHighGroupChat) {
            showPromitDialog();
        } else if (mIsTribeChat) {
            setResult(RESULT_BACK);
            finish();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public void onConnectionUpdated(int state) {
        switch (state) {
            case ConnectionManager.EMCHAT_DISCONNECTED:
                getTitleBar().hideProgress();
                getTitleBar().showLeftText(R.string.disconnected, null);
                break;
            case ConnectionManager.EMCHAT_CONNECTED:
                getTitleBar().hideProgress();
                getTitleBar().hideLeftText();
                break;
            default:
                break;
        }
    }

    @Override
    public void onContactsUpdated(final UpdateType type, final Object data) {
        if (mIsPaused) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (type) {
                    case Unsubscribe:
                        if (mChatTarget.equals(data)) {
                            finish();
                        }
                        break;

                    case UserInfo:
                        mChatTargetInfo = mUserInfoManager.getUserInfo(mChatTarget);
                        if (mChatTargetInfo != null) {
                            String name = mChatTargetInfo.getDisplayName(ChatActivity.this);
                            getTitleBar().showTitleText(name, null);
                            mIMResManager.changeSuit(mChatTargetInfo);
                            mIMResManager.showAvatar(mCocosAvatarSize, false);
                            mIMResManager.fetchUsingRes(mChatTargetInfo.userId);
                        } else {
                            // toast("用户信息获取失败!即将退出聊天界面");
                            mUserInfoManager.acquireUserInfo(mChatTarget);
                        }
                        break;

                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void onChatExtClicked(ChatExtItem item) {
        if (item == null) {
            return;
        }

        switch (item.title) {
            case R.string.cht_ext_picture:
                // showPictureDialog();
                mGetImageHelper.getImage(true, 9);
                MobclickAgent.onEvent(this, MStaticInterface.PHOTO);
                break;

            case R.string.cht_ext_location:
                showLocationDialog();
                MobclickAgent.onEvent(this, MStaticInterface.POSITION);
                break;

            case R.string.cht_ext_emotion:
                viewMarket(IMResource.TYPE_EMOTION_PACKAGE);
                MobclickAgent.onEvent(this, MStaticInterface.CLICK_EXPRESSION);
                break;

            case R.string.cht_ext_bubble:
                viewMarket(IMResource.TYPE_BUBBLE);
                MobclickAgent.onEvent(this, MStaticInterface.BUBBLE);
                break;

            case R.string.cht_ext_background:
                viewMarket(IMResource.TYPE_BACKGROUND);
                MobclickAgent.onEvent(this, MStaticInterface.BACKGROUND);
                break;

            case R.string.cht_ext_character:
                viewMarket(IMResource.TYPE_CHARACTER_ACTION);
                MobclickAgent.onEvent(this, MStaticInterface.ROLE);
                break;

            default:
                break;
        }
    }

    @Override
    public void onEmotionClicked(Emotion emotion) {
        if (emotion.isBig) {
            sendBigEmotion(emotion.getSpannable(this).toString());
        } else {
            mMessageEditor.getText().insert(mMessageEditor.getSelectionStart(), emotion.getSpannable(this));
        }
    }

    @Override
    public void onBackspaceClicked() {
        Utils.dispatchDelEvent(mMessageEditor);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_right:
                if (mIsGroupChat) {
                    if (mTribeRoomInfo != null) {
                        viewGroupInfo();
                    }
                } else {
                    viewProfile();
                }
                break;
            case R.id.btn_left:
                onBackPressed();
                break;
            case R.id.et_message:
                mEmotionPicker.setVisible(false);
                mChatExtPicker.setVisible(false);
                break;
            case R.id.iv_extention:
                if (mTvVoiceButton.isShown()) {
                    mChatExtPicker.setVisible(!mChatExtPicker.isVisible());
                } else {
                    if (mEmotionPicker.isVisible()) {
                        mEmotionPicker.setVisible(false);
                        mChatExtPicker.setVisible(true);
                    } else {
                        Utils.toggleInput(mMessageEditor, mChatExtPicker);
                    }
                }
                break;

            case R.id.iv_keyboard:
                mEmotionPicker.setVisible(false);
                mChatExtPicker.setVisible(false);
                View rlInput = findViewById(R.id.rl_input);
                ImageView ivKeyboard = (ImageView) findViewById(R.id.iv_keyboard);
                if (mTvVoiceButton.isShown()) {
                    mTvVoiceButton.setVisibility(View.GONE);
                    rlInput.setVisibility(View.VISIBLE);
                    ivKeyboard.setImageResource(R.drawable.button_ic_voice);
                    Utils.showInput(mMessageEditor);
                } else {
                    mTvVoiceButton.setVisibility(View.VISIBLE);
                    rlInput.setVisibility(View.GONE);
                    ivKeyboard.setImageResource(R.drawable.button_ic_key);
                    Utils.hideInput(mMessageEditor);
                }
                break;

            case R.id.iv_emotion:
                if (mChatExtPicker.isVisible()) {
                    mChatExtPicker.setVisible(false);
                    mEmotionPicker.setVisible(true);
                } else {
                    Utils.toggleInput(mMessageEditor, mEmotionPicker);
                }
                break;

            case R.id.tv_send:
                sendMessage();
                break;

            case R.id.tv_more_chats:
                loadChats(mChatAdapter.getItem(0).timestamp);
                break;

            default:
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mLocationDialog) {
            switch (which) {
                case 0:
                    Intent iMap = new Intent(this, MapActivity.class);
                    startActivityForResult(iMap, ChatActivity.MAP_REQUEST);
                    break;

                case 1:
                    if (mIsGroupChat) {
                        toast(getString(R.string.group_chat_not_support));
                        return;
                    }
                    Intent iShareLoc = new Intent(this, ShareLocationActivity.class);
                    iShareLoc.putExtra(EXTRA_CHAT_TARGET, mChatTarget);
                    startActivity(iShareLoc);
                    break;

                default:
                    break;
            }
        } else if (dialog == mPictureDialog) {
            switch (which) {
                case 0:
                    mGetImageHelper.getImageGallery();
                    break;
                case 1:
                    mGetImageHelper.getImageCamera();
                    break;
                default:
                    break;
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mFinishRecord = false;
                mCancleRecord = false;
                initVoiceRecord();
                mStartX = event.getX();
                mStartY = event.getY();
                mImAudioManager.startRecord();
                mStartTime = System.currentTimeMillis();
                mRecordDialog.show();
                mTimer = new Timer();
                mChatAdapter.stopPlayVoice();
                mChatAdapter.notifyDataSetChanged();
                startVoiceRecord();
                mTvVoiceButton.setText(R.string.cht_unpressed_to_end);
                break;

            case MotionEvent.ACTION_UP:
                if (!mFinishRecord) {
                    mTvVoiceButton.setText(R.string.cht_pressed_to_record);
                    mVoiceLength = 60;
                    if (!mStopRecord) {
                        mImAudioManager.stopRecord();
                        mRecordDialog.hide();
                        mVoiceLength = mTimeCount - 1;
                    }
                    mStopTime = System.currentTimeMillis();
                    if (mStopTime - mStartTime < 1000) {
                        toast(R.string.cht_tst_voice_too_short);
                        mImAudioManager.deleteAudio(mImAudioManager.getFilePath());
                        mCancleRecord = true;
                    }
                    if (mCancleRecord) {
                        // cancle sending voice file and delete the record
                        mImAudioManager.deleteAudio(mImAudioManager.getFilePath());
                    } else {
                        // send voice message
                        sendVoice();
                    }
                    if (mTimer != null) {
                        mTimer.cancel();
                    }
                    stopVoiceRecord();
                    mTimeCount = 0;
                    resetVoiceRecord();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                float instance = caclInstance(mStartX, mStartY, event.getX(), event.getY());
                mCancleRecord = instance > CANCLLE_SEND_VOICE_MIN_DISTANCE;
                mHandler.sendEmptyMessage(MSG_VOICE);
                break;

            case MotionEvent.ACTION_CANCEL:
                mImAudioManager.stopRecord();
                mImAudioManager.deleteAudio(mImAudioManager.getFilePath());
                mCancleRecord = true;
                initVoiceRecord();
                stopVoiceRecord();
                resetVoiceRecord();
                mTimeCount = 0;
                break;

            default:
                break;
        }
        return true;
    }

    @Override
    public void onMessageReceived(ChatRecord record) {
        processRecord(record);
    }

    @Override
    public void onMessageReceiveFailed(ChatRecord record) {

    }

    @Override
    public void onMessagePreSend(ChatRecord record) {
        processRecord(record);
    }

    @Override
    public void onMessageSent(ChatRecord record) {
    }

    @Override
    public void onMessageSendFailed(ChatRecord record) {

    }

    @Override
    public void onAvatarLoaded() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIMResManager.onAvatarLoaded();
                updateAvatar();
            }
        });
    }

    @Override
    public void onUsingResUpdated(int userId, int type, IMResource res) {
        switch (type) {
            case IMResource.TYPE_BACKGROUND:
                updateBackground(userId, res);
                break;
            case IMResource.TYPE_BUBBLE:
                mChatAdapter.updateBubble(userId, res);
                break;
            case IMResource.TYPE_CHARACTER_ACCESSORY:
                mIMResManager.changeSuit(userId, res);
                mIMResManager.showAvatar(mCocosAvatarSize, false);
                break;
            default:
                break;
        }
    }

    @Override
    public void onGroupChatSuccess(int type, Object obj) {
        switch (type) {
            case GroupChatManager.GET_GROUP_INFO:
                ucp = (UserGroupMap) obj;
                GroupInfo groupInfo = new GroupInfo(String.valueOf(ucp.id), ucp.name, ucp.chatId);
                if (DataHelper.loadGroupInfo(this, String.valueOf(ucp.id)) != null) {
                    DataHelper.updateGroupInfo(this, groupInfo);
                } else {
                    DataHelper.insertGroupInfo(this, groupInfo);
                }
                mGroupChatManager.getGroupMemers(mGroupId, 0);
                mMoreInfoClickble = true;
                break;

            case GroupChatManager.CREATE_GROUP:
                mGroupId = ((UserGroupMap) obj).id + "";
                mChatTarget = createRoomJid(String.valueOf(((UserGroupMap) obj).chatId));
                mGroupChatManager.addUsers(mGroupId, mUserIds);
                mChatAdapter.setChatTarget(mChatTarget);
                if (mTopicRoomId == null) {
                    mChatHelper.sendText(mChatTarget, getString(R.string.group_chat_start_message), mGroupId);
                }
                DataHelper.insertGroupInfo(this, new GroupInfo(String.valueOf(((UserGroupMap) obj).id),
                        mTempGroupName, ((UserGroupMap) obj).chatId));
                mMoreInfoClickble = true;
                getTitleBar().showRightButton(R.drawable.icon_group, this);
                break;

            case GroupChatManager.ADD_USER:
                mGroupChatManager.getGroupInfo(mGroupId);
                mMoreInfoClickble = true;
                break;
            case GroupChatManager.START_TOPIC:
                UserGroupMap groupMap = (UserGroupMap) obj;
                getTitleBar().showTitleText(mTopicRoomName, null);
                getTitleBar().showLeftButton(R.drawable.botton_bg_arrow, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPromitDialog();
                    }
                });
                mGroupId = groupMap.id + "";
                mTopicRoomId = mGroupId;
                mTempGroupName = groupMap.name;
                mChatTarget = createRoomJid(mGroupId);
                mGroupChatManager.addUsers(mGroupId, mUserIds);
                mChatAdapter.setChatTarget(mChatTarget);
                DataHelper.insertGroupInfo(this, new GroupInfo(String.valueOf(((UserGroupMap) obj).id),
                        mTempGroupName, ((UserGroupMap) obj).chatId));
                mChatAdapter.showMyName(true);
                mChatAdapter.isGroupChat(true);
                mMoreInfoClickble = true;
                break;
            case GroupChatManager.QUIT:
                if (mTopicRoomId != null) {
                    mUnreadMessageHelper.deleteChat(mGroupId + "@groupchat." + Utils.getChatServerDomain());
                    finish();
                }
                break;
            case GroupChatManager.GET_GROUP_MEMBERS:
                if (mTopicRoomId != null) {
                    return;
                }
                mMembers.clear();
                ArrayList<GroupMember> members = (ArrayList<GroupMember>) obj;
                mMembers.addAll(members);
                String mGroupName = (ucp.name).equals(getString(R.string.cht_unnamed)) ? buildGroupName() : ucp.name;
                getTitleBar().showTitleText(
                        (ucp.name).equals(getString(R.string.cht_unnamed)) ? buildGroupName() : ucp.name + "("
                                + mMembers.size() + ")", null);
                groupInfo = new GroupInfo(String.valueOf(ucp.id), mGroupName, ucp.chatId);
                if (DataHelper.loadGroupInfo(this, String.valueOf(ucp.id)) != null) {
                    DataHelper.updateGroupInfo(this, groupInfo);
                } else {
                    DataHelper.insertGroupInfo(this, groupInfo);
                }
                mMoreInfoClickble = true;
                break;
            default:
                break;
        }
    }

    @Override
    public void onGroupChatFailed(int type) {

    }

    @Override
    public boolean shouldOverrideUrlLoading(String url) {
        if (url.startsWith(MsRequest.IMRW_CLICK.getToLocalUrl())) {
            String colon = mMessageEditor.getText().toString();
            Uri uri = Uri.parse(url);
            String sn = uri.getQueryParameter("sn");
            String key = mIMResManager.getAvatarActionKey(sn);
            if (!TextUtils.isEmpty(key)) {
                mMessageEditor.append(key);
                mMessageEditor.append(colon);
                sendMessage();

                MobclickAgent.onEvent(this, MStaticInterface.TRIGGER_EXPRESSION);
                if (!mSendAvatar) {
                    setSendAvatarStatus();
                }
            }
            mChatHelper.sendChatState(mChatTarget, false, false);
            return true;
        }
        return false;
    }

    @Override
    public void onPageStarted(String url) {
    }

    @Override
    public void onPageFinished(String url) {
    }

    @Override
    public void onPageReceivedError() {
        mWvChooseRes.loadUrl(getChooseUrl(IMResource.TYPE_CHARACTER_ACTION));
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!mIsGroupChat) {
            boolean showWvChooseRes = TextUtils.equals(s, Utils.COLON_EN) || TextUtils.equals(s, Utils.COLON_CN);
            mWvChooseRes.setVisibility(showWvChooseRes ? View.VISIBLE : View.GONE);
            if (showWvChooseRes) {
                Utils.hideInput(mMessageEditor);
            }
        }
        if (mChatTarget == null) {
            return;
        }

        View tvSend = findViewById(R.id.tv_send);
        View ivEmo = findViewById(R.id.iv_extention);
        if (TextUtils.isEmpty(s)) {
            tvSend.setVisibility(View.GONE);
            ivEmo.setVisibility(View.VISIBLE);
        } else {
            tvSend.setVisibility(View.VISIBLE);
            ivEmo.setVisibility(View.GONE);
        }

        if (!mHandler.hasMessages(MSG_TEXT_CHANGE)) {
            mChatHelper.sendChatState(mChatTarget, !(TextUtils.isEmpty(s)), false);
        }
        mHandler.sendEmptyMessageDelayed(MSG_TEXT_CHANGE, TEXT_CHANGE_DELAY_MILLIS);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onImageResult(boolean success, String imageFile, Bitmap bm) {
        if (success) {
            createImageAndSendMessage(imageFile);
        } else {
            toast(R.string.qa_handle_image_failed);
        }
    }

    private void createImageAndSendMessage(String imageFile) {
        newFileName = createFileName(ChatActivity.this) + getFilePostfix(imageFile);
        Utils.copy(imageFile, newFileName);
        sendPicture(newFileName);
    }

    @Override
    public void onImageResult(boolean success, final ArrayList<String> images) {
        if (success) {
            for (int i = 0; i < images.size(); i++) {
                final String image = images.get(i);
                mChatListView.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        createImageAndSendMessage(image);
                    }
                }, i * 200);
            }
        } else {
            toast(R.string.qa_handle_image_failed);
        }
    }

    @Override
    public void onGetImResourceSuccess(int type, int requestCode, ArrayList<IMResource> mResources) {
    }

    @Override
    public void onUnzipSuccess() {
    }

    @Override
    public void onUseResSuccess(IMResource res) {
    }

    @Override
    public void onUnuseResSuccess() {
    }

    @Override
    public void onAddResSuccess() {
    }

    @Override
    public void onAddResFail(int code) {
    }

    private void processRecord(ChatRecord record) {
        if (record.isTarget(mChatTarget)) {
            if (record.isChatState) {
                processChatState(record);
            } else if (mChatHelper.processRecord(record)) {
                if (!record.isFrom(mAccountInfo.getAccount())) {
                    handleMsgStopArmature();
                }
                mChatAdapter.add(record);
                mChatListView.setSelection(mChatListView.getCount() - 1);
                mIMResManager.playArmature(record);
            }
        }
    }

    private void processChatState(ChatRecord record) {
        if (record.isComposing) {
            getTitleBar().showSubTitleText(
                    record.isComposingVoice ? getString(R.string.chat_loading_voice)
                            : getString(R.string.chat_loading_txt));
            String animName = record.isComposingVoice ? ANIM_RECORDING_VOICE : ANIM_INPUTING_TEXT;
            if (!mHandler.hasMessages(MSG_STOP_ARMATURE) || !TextUtils.equals(mCurrentAnimName, animName)) {
                mIMResManager.playArmature(0, animName);
                mCurrentAnimName = animName;
            }
            mHandler.removeMessages(MSG_STOP_ARMATURE);
            mHandler.sendEmptyMessageDelayed(MSG_STOP_ARMATURE, STOP_ARMATURE_DELAY_MILLIS);
        } else {
            getTitleBar().hideSubTitleText();
            handleMsgStopArmature();
        }
    }

    private void updateBackground(int userId, IMResource res) {
        if (mAccountInfo.getUserId() == userId) {
            if (res == null) {
                mChatListView.setBackgroundResource(0);
            } else {
                mChatListView.setBackground(res.urls[0][0], 0);
            }
        }
    }

    private void updateAvatar() {
        mIMResManager.changeSuit(mAccountInfo.getUserInfo());
        mIMResManager.changeSuit(mChatTargetInfo);
        mIMResManager.showAvatar(mCocosAvatarSize, false);
    }

    private void onStartHighTopChat(int themeId) {
        mGroupChatManager.enterTopic(themeId);
    }

    private void onStartMuc(ArrayList<UserEntry> users) {
        StringBuilder sbName = new StringBuilder(mAccountInfo.getUserInfo().getDisplayName(this));
        StringBuilder sbIds = new StringBuilder();
        boolean firstTime = true;
        for (UserEntry ue : users) {
            UserInfo uinfo = mUserInfoManager.getUserInfo(ue.jid);
            if (firstTime) {
                firstTime = false;
            } else {
                sbIds.append(Utils.COMMA_DELIMITER);
            }
            if (uinfo == null) {
                mUserInfoManager.acquireUserInfo(ue.jid);
            } else {
                sbName.append("、").append(uinfo.getDisplayName(this));
                sbIds.append(uinfo.userId);
            }
        }
        mTempGroupName = sbName.toString();
        if (mTempGroupName.length() > 20) {
            mTempGroupName = mTempGroupName.substring(0, 19) + "……";
        }
        mUserIds = sbIds.toString();
        getTitleBar().showTitleText(mTempGroupName + "(" + (users.size() + 1) + ")", null);

        mGroupChatManager.createGroupChat(mTempGroupName, mUserIds);
        if (mChatAdapter != null) {
            mChatAdapter.removeAll();
            mChatAdapter.notifyDataSetChanged();
        }
    }

    private void sendMessage() {
        if (!isTargetNotEmpty()) {
            return;
        }
        String message = mMessageEditor.getText().toString().trim();
        if (Utils.isNetworkAvailable(ChatActivity.this)) {
            if (TextUtils.isEmpty(message)) {
                toast(R.string.rpl_tst_content_empty);
            } else {
                mMessageEditor.setText("");
                mHandler.removeMessages(MSG_TEXT_CHANGE);
                if (mChatTarget == null) {
                    return;
                }
                mChatHelper.sendText(mChatTarget, message, mGroupId);
                mChatHelper.sendChatState(mChatTarget, false, false);
            }
        } else {
            toast(getString(R.string.cht_network_is_not_available));
        }
    }

    private void sendBigEmotion(String message) {
        if (!isTargetNotEmpty()) {
            return;
        }
        if (Utils.isNetworkAvailable(ChatActivity.this)) {
            if (TextUtils.isEmpty(message)) {
                toast(R.string.rpl_tst_content_empty);
            } else {
                if (mChatTarget == null) {
                    return;
                }
                mChatHelper.sendEmtionText(mChatTarget, message, mGroupId);
            }
        } else {
            toast(getString(R.string.cht_network_is_not_available));
        }
    }

    private void sendVoice() {
        if (!isTargetNotEmpty()) {
            return;
        }
        String voiceFilePath = mImAudioManager.getFilePath();
        mChatHelper.sendVoice(mChatTarget, voiceFilePath, mVoiceLength, mGroupId);
    }

    private void sendPicture(String imageFile) {
        if (!isTargetNotEmpty()) {
            return;
        }
        mChatHelper.sendPicture(mChatTarget, imageFile, mGroupId);
    }

    private void sendLocationMessage(LatLng latLng, String address) {
        if (!isTargetNotEmpty()) {
            return;
        }
        mChatHelper.sendLocation(mChatTarget, latLng.longitude, latLng.latitude, address, mGroupId);
    }

    private void viewGroupInfo() {
        if (mIsTribeChat) {
            mMoreInfoClickble = true;
        }
        if (!mMoreInfoClickble) {
            toast(getString(R.string.cht_Access_to_information));
            return;
        }
        Intent gciIntent = new Intent(ChatActivity.this, GroupChatInfoActivity.class);
        gciIntent.putExtra(EXTRA_GROUPCHAT_JID, mChatTarget);
        gciIntent.putExtra(EXTRA_GROUPCHAT_ID, mGroupId);
        gciIntent.putExtra(EXTRA_CHAT_ISGOUPCHAT, mIsGroupChat);
        gciIntent.putExtra(EXTRA_CHAT_IMAGEURLS, mChatAdapter.getImageUrls());
        if (mTribeRoomInfo != null) {
            gciIntent.putExtra(EXTRA_TRIBE_ROOM_INFO, mTribeRoomInfo);
        }
        startActivityForResult(gciIntent, REQUEST_CODE);
    }

    private void viewMarket(int type) {
        String params = new StringBuilder("type=").append(type).toString();
        String url = HttpUtil.getUrl(this, MsRequest.IMRW_RESOURCE_LIST, params);
        Intent iMarket = new Intent(this, AvatarMarketActivity.class);
        if (type == IMResource.TYPE_EMOTION_PACKAGE) {
            iMarket.putExtra(AvatarMarketActivity.EXT_TYPE, type);
        }
        if (type == IMResource.TYPE_CHARACTER_ACTION) {
            iMarket.putExtra(AvatarMarketActivity.EXTRA_SHOW_AVATAR, true);
            url += "&max_ver=" + mIMResManager.getAvatarVersion(true);
        }
        iMarket.putExtra(AvatarMarketActivity.URL, url);
        startActivity(iMarket);
    }

    private String getChooseUrl(int type) {
        String params = new StringBuilder("type=").append(type).toString();
        return HttpUtil.getUrl(this, MsRequest.IMRW_CHOOSE, params);
    }

    private void checkMore() {
        if (mChatAdapter.getCount() > 0) {
            ChatRecord first = mChatAdapter.getItem(0);
            mListHeaderView.findViewById(R.id.tv_more_chats).setVisibility(
                    DataHelper.hasMoreChats(this, mChatTarget, first.timestamp) ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * @param fileName
     * @return file's postfix like '.jpg/.png'
     */
    private String getFilePostfix(String fileName) {
        int index = fileName.lastIndexOf(".");
        return fileName.substring(index, fileName.length());
    }

    @SuppressLint("SimpleDateFormat")
    private String createFileName(Context mContext) {
        String basePath = Utils.getMianLiaoDir().getAbsolutePath();
        String account = AccountInfo.getInstance(mContext).getAccount();
        String imageFileName = basePath + "/" + account;
        File voiceFile = new File(imageFileName);
        if (voiceFile.isDirectory() || voiceFile.mkdir()) {
            imageFileName += "/image_" + System.currentTimeMillis();
        }
        return imageFileName;
    }

    private void showLocationDialog() {
        if (mLocationDialog == null) {
            mLocationDialog = new LightDialog(this).setTitleLd(R.string.please_choose).setItems(
                    R.array.cht_share_location, this);
        }
        mLocationDialog.show();
    }

    private void showPictureDialog() {
        if (mPictureDialog == null) {
            mPictureDialog = new LightDialog(this).setTitleLd(R.string.please_choose).setItems(
                    R.array.get_image_choices, this);
        }
        mPictureDialog.show();
    }

    private void loadChats(long timestamp) {
        ArrayList<ChatRecord> records = DataHelper.loadChatRecords(this, mChatTarget, timestamp, DEFAULT_NUM_CHAT_LOAD);
        records = recordFilter(records);
        mChatListView.setSelection(mChatAdapter.addAll(records));
        checkMore();
    }

    private ArrayList<ChatRecord> recordFilter(ArrayList<ChatRecord> records) {
        ArrayList<ChatRecord> filterRecord = new ArrayList<>();
        for (ChatRecord record : records) {
            if (!record.isNightRecord) {
                filterRecord.add(record);
            }
        }
        return filterRecord;
    }

    private void startVoiceRecord() {
        mHandler.removeMessages(MSG_TEXT_CHANGE);
        mTask = new TimerTask() {
            @Override
            public void run() {
                if (!mStopRecord) {
                    if (mTimeCount % 3 == 0) {
                        mChatHelper.sendChatState(mChatTarget, true, true);
                    }
                    mTimeCount++;
                    if (mTimeCount > TIME_COUNT_MAX) {
                        stopVoiceRecord();
                    }
                    mHandler.sendEmptyMessage(MSG_VOICE);
                }
            }
        };
        mTimer.schedule(mTask, 0, 1000);
    }

    private void stopVoiceRecord() {
        mStopRecord = true;
        mChatHelper.sendChatState(mChatTarget, false, true);
    }

    private void resetVoiceRecord() {
        mStopRecord = false;
        mCancleRecord = false;
        mFinishRecord = true;
        mTimer = null;
        mTask = null;
    }

    private float caclInstance(float oldX, float oldY, float newX, float newY) {
        float poorX = oldX - newX;
        float poorY = oldY - newY;
        return (float) Math.sqrt(poorX * poorX + poorY * poorY);
    }

    private String createRoomJid(String groupId) {
        return groupId + "@groupchat." + Utils.getChatServerDomain();
    }

    private void initVoiceRecord() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBarRed.setVisibility(View.GONE);
        Drawable drawable = getResources().getDrawable(R.drawable.circle_icon_talking);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mMsgShow.setCompoundDrawables(null, drawable, null, null);
        mMsgShow.setText(R.string.cht_move_up_to_cancle);
        mTvVoiceButton.setText(R.string.cht_pressed_to_record);
        if (mTimer != null) {
            mTimer.cancel();
        }
        mVoiceLength = 0;
        mRecordDialog.hide();
    }

    private boolean isMyFriend() {
        return UserEntryManager.getInstance(this).isFriend(mChatTarget);
    }

    private boolean isTargetNotEmpty() {
        if (mIsGroupChat) {
            return mChatTarget != null;
        }
        return mChatTarget != null && mChatTargetInfo != null;
    }

    private void viewProfile() {
        if (!isTargetNotEmpty()) {
            toast(R.string.prof_user_not_exist);
            return;
        }
        Intent iProfile = new Intent(this, NewProfileActivity.class);
        iProfile.putExtra(UserInfo.INTENT_EXTRA_INFO, mChatTargetInfo);
        iProfile.putExtra(NewProfileActivity.EXTRA_SHOW_CHAT_BUTTON, true);
        startActivity(iProfile);
    }

    private void handleMsgVoice() {
        if (mTimeCount > 0) {
            mRecordTime.setText((mTimeCount - 1) + "\"");
            if (mStopRecord) {
                mImAudioManager.stopRecord();
                mRecordDialog.hide();
                mTimer.cancel();
                mVoiceLength = mTimeCount - 1;
                sendVoice();
                resetVoiceRecord();
                mTimeCount = 0;
            }
        }

        int drawableId, textId;
        if (mCancleRecord) {
            mProgressBar.setVisibility(View.GONE);
            mProgressBarRed.setVisibility(View.VISIBLE);
            drawableId = R.drawable.circle_icon_cancel;
            textId = R.string.cht_unpressed_to_cancle;
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBarRed.setVisibility(View.GONE);
            drawableId = R.drawable.circle_icon_talking;
            textId = R.string.cht_move_up_to_cancle;
        }
        mMsgShow.setCompoundDrawablesWithIntrinsicBounds(0, drawableId, 0, 0);
        mMsgShow.setText(textId);
    }

    private void handleMsgStopArmature() {
        mIMResManager.stopArmature(0);
        mHandler.removeMessages(MSG_STOP_ARMATURE);
    }

    private void handleMsgTextChange() {
        if (!isTargetNotEmpty()) {
            return;
        }
        if (mHandler.hasMessages(MSG_TEXT_CHANGE)) {
            mChatHelper.sendChatState(mChatTarget, true, false);
            mHandler.removeMessages(MSG_TEXT_CHANGE);
            mHandler.sendEmptyMessageDelayed(MSG_TEXT_CHANGE, TEXT_CHANGE_DELAY_MILLIS);
        } else {
            mChatHelper.sendChatState(mChatTarget, false, false);
        }
    }

    private static class ChatHandler extends Handler {
        private WeakReference<ChatActivity> mActivityRef;

        public ChatHandler(ChatActivity activity) {
            mActivityRef = new WeakReference<ChatActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ChatActivity activity = mActivityRef.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_VOICE:
                        activity.handleMsgVoice();
                        break;
                    case MSG_STOP_ARMATURE:
                        activity.handleMsgStopArmature();
                        break;
                    case MSG_TEXT_CHANGE:
                        activity.handleMsgTextChange();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public String buildGroupName() {
        String mGroupName = "";
        for (int i = 0; i < (mMembers.size() - 1); i++) {
            mGroupName = mGroupName + mMembers.get(i).userInfo.getDisplayName(ChatActivity.this) + "、";
        }
        mGroupName = mGroupName + mMembers.get(mMembers.size() - 1).userInfo.getDisplayName(ChatActivity.this);
        return mGroupName;
    }

    private void showPromitDialog() {
        if (mPromitDialog == null) {
            mPromitDialog = new LightDialog(ChatActivity.this).setTitleLd(getString(R.string.fe_remind_msg))
                    .setMessage(getString(R.string.cht_exit_point))
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mGroupChatManager.quit(mTopicRoomId);
                            DataHelper.deleteChatRecords(ChatActivity.this, mChatTarget);
                            finish();
                        }
                    });
        }
        mPromitDialog.show();
    }

    @Override
    public void onSuccess(int type, Object obj) {
        switch (type) {
            case TribeChatManager.TYPE_TRIBE_ROOM_INFO:
                if (obj instanceof TribeChatRoomInfo) {
                    mIsTribeChat = true;
                    mTribeRoomInfo = (TribeChatRoomInfo) obj;
                    getTitleBar().setTitle(mTribeRoomInfo.roomName);
                    GroupInfo groupInfo = new GroupInfo(mTribeRoomInfo.roomId + "tribe",
                            mTribeRoomInfo.roomName, mTribeRoomInfo.chatId);
                    if (DataHelper.loadGroupInfo(this, mTribeRoomInfo.roomId + "tribe") != null) {
                        DataHelper.updateGroupInfo(this, groupInfo);
                    } else {
                        DataHelper.insertGroupInfo(this, groupInfo);
                    }
                    mMoreInfoClickble = true;
                }
                break;

            default:
                break;
        }

    }

    @Override
    public void onFail(int type) {
    }

}
