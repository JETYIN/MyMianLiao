package com.tjut.mianliao.profile;

import java.util.ArrayList;

import org.apache.commons.lang.text.StrBuilder;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.RefFriendActivity;
import com.tjut.mianliao.component.EmotionPicker;
import com.tjut.mianliao.component.EmotionPicker.EmotionListener;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.MLLinkMovementMethod;
import com.tjut.mianliao.component.RichEmotionTextView;
import com.tjut.mianliao.component.RichMlEditText;
import com.tjut.mianliao.component.RichMlEditText.OnAtDelClicklistener;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.Emotion;
import com.tjut.mianliao.data.InboxMessage;
import com.tjut.mianliao.data.MyReplyInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.CfReply;
import com.tjut.mianliao.forum.nova.BasePostActivity;
import com.tjut.mianliao.forum.nova.ForumPostDetailActivity;
import com.tjut.mianliao.forum.nova.MessageRemindManager.MessageType;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.PoliceHelper;
import com.tjut.mianliao.util.Utils;

public class MyReplyActivity extends BaseActivity implements OnRefreshListener2<ListView>,
        DialogInterface.OnClickListener, OnClickListener, EmotionListener,
        OnFocusChangeListener, MsTaskListener, OnAtDelClicklistener{
    
    public static final String EXT_MESSAGE_TYPE = "ext_message_type";
    
    protected static final int MAX_REPLY_LEN = Integer.MAX_VALUE;

    PullToRefreshListView mPtrListView;
    ArrayList<MyReplyInfo> mMessages;
    private int mNameColor;
    private RemindAdapter mAdapter;
    private LightDialog mMenuDialog;
    private CfPost mTargetCfPost;
    private CfReply mTargetCfreply;

    private RichMlEditText mMessageEditor;
    private EmotionPicker mEmotionPicker;
    private boolean mIsReplyPost;
    private boolean mIsShowSchoolName = false;
    private MyReplyInfo mCurrentMsg;
    private MsTaskManager mTaskManager;
    private ImageView mIvCommentSuc;
    private LinearLayout mLlInput;
    private ImageView mIvEmotion;
    private SharedPreferences mPreferences;
    private ArrayList<UserInfo> mRefFriends;
    private boolean mHasCallback = true;
    
    private View mViewNoContent;
    private FrameLayout mViewParent;
    
    private static final int REQUEST_REF_SQUARE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTaskManager = MsTaskManager.getInstance(this);
        mTaskManager.registerListener(this);
        mPreferences = DataHelper.getSpForData(this);

        mViewNoContent = mInflater.inflate(R.layout.view_no_content, null);
        mViewParent = (FrameLayout) findViewById(R.id.fl_content);
        mIvCommentSuc = (ImageView) findViewById(R.id.iv_comment_suc);
        mLlInput = (LinearLayout) findViewById(R.id.ll_input);
        mMessageEditor = (RichMlEditText) findViewById(R.id.et_message);
        mMessageEditor.addTextChangedListener(mTextWatcher);
        mEmotionPicker = (EmotionPicker) findViewById(R.id.ep_emotions);
        mIvEmotion = (ImageView) findViewById(R.id.iv_extention);

        mEmotionPicker.setEmotionListener(this);
        mMessageEditor.setOnFocusChangeListener(this);

        getTitleBar().setTitle(getString(R.string.post_my_comments));
        mNameColor = getResources().getColor(R.color.channel_post_reply_name);
        mMessages = new ArrayList<MyReplyInfo>();
        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptrlv_post_stream);

        mPtrListView.setMode(Mode.BOTH);
        mPtrListView.setOnRefreshListener(this);
        mAdapter = new RemindAdapter();
        mPtrListView.setAdapter(mAdapter);
        mPtrListView.setRefreshing(Mode.PULL_FROM_START);
        mViewNoContent.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                reload();
            }
        });
    }

    private void hideNoMessage() {
        if (mViewParent != null && mViewNoContent != null) {
            mViewParent.removeView(mViewNoContent);
        }
    }
    
    private void showNoMessage() {
        if (mViewParent != null && mViewNoContent != null) {
            mViewParent.removeView(mViewNoContent);
            resetNoContentView();
            mViewParent.addView(mViewNoContent);
        }
    }
    
    private void resetNoContentView() {
        mViewNoContent.findViewById(R.id.iv_notice).setVisibility(View.VISIBLE);
        mViewNoContent.findViewById(R.id.pb_progress).setVisibility(View.GONE);
        
    }
    
    private void reload() {
        mViewNoContent.findViewById(R.id.iv_notice).setVisibility(View.INVISIBLE);
        mViewNoContent.findViewById(R.id.pb_progress).setVisibility(View.VISIBLE);
        getRemind(true);
    }

    private void toggleInput(boolean show) {
        mLlInput.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_remind_forum;
    }


    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        getRemind(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        getRemind(false);
    }

    private class getMyReplyTask extends MsTask {

        private int mOffset;
        
        public getMyReplyTask(int offset) {
            super(MyReplyActivity.this, MsRequest.THREAD_MY_REPLY);
            mOffset = offset;
        }
        
        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).toString();
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mPtrListView.onRefreshComplete();
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                ArrayList<MyReplyInfo> messages = JsonUtil.getArray(ja, MyReplyInfo.TRANSFORMER);
                if (mOffset <= 0) {
                    mMessages.clear();
                    mMessages.addAll(messages);
                    if (messages != null && messages.size() > 0) {
                        hideNoMessage();
                    } else {
                        showNoMessage();
                    }
                } else {
                    mMessages.addAll(messages);
                }
                mAdapter.notifyDataSetChanged();
            } else {
                showNoMessage();
            }
        
        }
    }
    
    private void getRemind(boolean refresh) {
        int offset = refresh ? 0 : mAdapter.getCount();
        new getMyReplyTask(offset).executeLong();
    }
    
    private class RemindAdapter extends BaseAdapter {

        private UserInfo userInfo;

        @Override
        public int getCount() {
            return mMessages.size();
        }

        @Override
        public MyReplyInfo getItem(int position) {
            return mMessages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_channel_post_comments, parent, false);
            }
            final MyReplyInfo msg = getItem(position);
            ImageView avart = (ImageView) view.findViewById(R.id.av_avatar);
            TextView name = (TextView) view.findViewById(R.id.tv_name);
            ImageView gender = (ImageView) view.findViewById(R.id.iv_gender);
            ImageView ivMedal = (ImageView) view.findViewById(R.id.iv_medal);
            view.findViewById(R.id.tv_location).setVisibility(View.GONE);
            view.findViewById(R.id.iv_more).setVisibility(View.GONE);
            view.findViewById(R.id.rv_comments).setVisibility(View.GONE);
            RichEmotionTextView content = (RichEmotionTextView) view.findViewById(R.id.tv_desc);
            content.setMovementMethod(MLLinkMovementMethod.getInstance());
            content.setTopicSpanClickble(true);
            TextView replyContent = (TextView) view.findViewById(R.id.tv_reply_content);
            TextView tvLoc = (TextView) view.findViewById(R.id.tv_location);
            ImageView ivTypeIcon = (ImageView) view.findViewById(R.id.iv_type_icon);
            View viewVip = view.findViewById(R.id.iv_vip);
            View viewVipBg = view.findViewById(R.id.iv_vip_bg);
            view.findViewById(R.id.tv_floor).setVisibility(View.GONE);
            
            replyContent.setBackgroundColor(0xFFF6F6F6);
            replyContent.setTextColor(0XFF2E2E2E);
            view.findViewById(R.id.view_line).setBackgroundColor(0XFFEAEAEA);
            replyContent.setVisibility(View.VISIBLE);
            tvLoc.setVisibility(View.VISIBLE);
            
            view.setOnClickListener(MyReplyActivity.this);
            view.setTag(msg);
            
            CfReply reply = msg.getReply();
            if (reply != null) {
                CfPost post = msg.getTargetPost();
                if (reply != null) {
                    userInfo = reply.userInfo;
                } else {
                    if (post != null) {
                        userInfo = post.userInfo;
                    }
                }
                Picasso.with(MyReplyActivity.this)
                    .load(userInfo.getAvatar())
                    .placeholder(userInfo.defaultAvatar())
                    .into(avart);
                name.setText(userInfo.getDisplayName(MyReplyActivity.this));
                gender.setImageResource(userInfo.gender == 0 ? R.drawable.img_girl : R.drawable.img_boy);
                if (userInfo.getLatestBadge() != null && userInfo.getLatestBadge().startsWith("http")) {
                    ivMedal.setVisibility(View.VISIBLE);
                    Picasso.with(MyReplyActivity.this)
                        .load(userInfo.getLatestBadge())
                        .placeholder(R.drawable.ic_medal_empty)
                        .into(ivMedal);
                } else {
                    ivMedal.setVisibility(View.GONE);
                }
                Utils.setText(view, R.id.tv_intro, reply != null ? reply.getTimeAndRelation() :
                    post.getTimeAndRelation());
                tvLoc.setText(userInfo.school);
                mIsShowSchoolName = true;
                avart.setOnClickListener(MyReplyActivity.this);
                avart.setTag(userInfo);
                // update type icon ,it while show in day time ,or it should hide
                int resIcon = userInfo.getTypeIcon();
                if (resIcon > 0) {
                    ivTypeIcon.setImageResource(resIcon);
                    ivTypeIcon.setVisibility(View.VISIBLE);
                } else {
                    ivTypeIcon.setVisibility(View.GONE);
                }
                // update vip bg
                if (viewVip != null) {
                    viewVip.setVisibility(
                            userInfo.vip ? View.VISIBLE : View.GONE);
                }
                if (viewVipBg != null) {
                    viewVipBg.setVisibility(
                            userInfo.vip ? View.VISIBLE : View.INVISIBLE);
                }
            }
            CharSequence cnt = "";
            String strReContent;
            boolean showReply = true;
            CfPost atPost;
            CfReply cfReply;
            if (msg.targetReply != null) {
                CfReply reReply = msg.getTargetReply();
                if (reReply == null) {
                    cnt = getString(R.string.post_reply_was_deleted);
                    strReContent = "";
                } else {
                    cnt = getString(R.string.news_comment_reply_nospace,reReply.userInfo.getNickname());
                    strReContent = ": " + reReply.content;
                }
                content.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                content.setText(reply);
                cnt = Utils.getColoredText(
                        MyReplyActivity.this.getString(R.string.channel_remind_msg, cnt, strReContent), cnt,
                        mNameColor, false);
                replyContent.setText(cnt);
                showReply = true;
            } else {
                CfPost erPost = msg.getTargetPost();
                if (erPost == null) {
                    cnt = getString(R.string.post_reply_was_deleted);
                    strReContent = "";
                } else {
                    cnt = getString(R.string.news_comment_comment,erPost.userInfo.getNickname());
                    strReContent = ": " + erPost.content;
                }
                content.setText(reply);
                content.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                cnt = Utils.getColoredText(
                        MyReplyActivity.this.getString(R.string.channel_remind_msg, cnt, strReContent), cnt,
                        mNameColor, false);
                replyContent.setText(cnt);
                showReply = true;
            }
            replyContent.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    mCurrentMsg = msg;
                    mTargetCfPost = mCurrentMsg.getTargetPost();
                    mTargetCfreply = mCurrentMsg.getTargetReply();
                    showResource();                            
                }
            });
            
            return view;
        }

    }

    private void showMenuDialog() {
        if (mMenuDialog == null) {
            mMenuDialog = new LightDialog(this);
        }
        if (showReplyItem()) {
            mMenuDialog.setItems(R.array.remind_item_menu, this);
        } else {
            mMenuDialog.setItems(R.array.remind_item_menu_sub, this);
        }
        mMenuDialog.show();
    }

    private boolean showReplyItem() {
        return true;
    }
    
    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case 0:
                if (showReplyItem()) {
                    reply();
                    toggleInput(true);
                } else {
                    showResource();
                }
                break;
            case 1:
                if (showReplyItem()) {
                    showResource();
                } else {
                    report();
                }
                break;
            case 2:
                report();
                break;
            default:
                break;
        }
    }

    private void report() {
        PoliceHelper.reportForumPost(this, mTargetCfPost.postId);
    }

    private void showResource() {
        if (mTargetCfPost == null) {
            toast(getString(R.string.post_was_deleted));
        } else {
            showPostDetail();
        }
    }

    private void showPostDetail() {
        Intent intent = new Intent(this, ForumPostDetailActivity.class);
        intent.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA, mTargetCfPost);
        intent.putExtra(ForumPostDetailActivity.EXTRL_SHOW_ISSHOW_SCHOOL, mIsShowSchoolName);
        startActivity(intent);
    }

    private void reply() {
        if (mTargetCfreply != null) {
            if (mTargetCfPost == null) {
                toast(getString(R.string.post_reply_was_deleted));
            } else {
                toReply();
            }
            mIsReplyPost = false;
        } else {
            if (mTargetCfPost == null) {
                toast(getString(R.string.post_reply_was_deleted));
            } else {
                toReply();
            }
            mIsReplyPost = false;
        }
    }

    private void toReply() {
        CfReply reply = mCurrentMsg.getReply();
        if (reply != null) {
            UserInfo userInfo = reply.userInfo;
            mMessageEditor.setHint(getString(R.string.post_hint_reply_content,
                    userInfo.getDisplayName(this)));
            mMessageEditor.requestFocus();
            Utils.showInput(mMessageEditor);
        } else {
            toast(getString(R.string.post_reply_was_deleted));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.av_avatar:
                UserInfo userInfo = (UserInfo) v.getTag();
                showProfile(userInfo);
                break;
            case R.id.iv_extention:
                checkIcon();
                Utils.toggleInput(mMessageEditor, mEmotionPicker);
                break;
            case R.id.tv_send:
                
                if (mMessageEditor.getText().toString().length() > MAX_REPLY_LEN) {
                    toast(getString(R.string.post_input_length_to_long));
                } else {
                    String message = mMessageEditor.getText().toString().trim();
                    if (TextUtils.isEmpty(message)) {
                        toast(R.string.rpl_tst_content_empty);
                    } else {
                        startComment(message, mRefFriends);
                        mMessageEditor.setText("");
                        mMessageEditor.setHint(R.string.post_reply_hit);
                        Utils.hideInput(mMessageEditor);
                        mIsReplyPost = true;
                        toggleInput(false);
                    }
                }
                break;

            case R.id.et_message:
                mEmotionPicker.setVisible(false);
                break;

            case R.id.iv_reply:
                mCurrentMsg = (MyReplyInfo) v.getTag();
                mTargetCfPost = mCurrentMsg.getTargetPost();
                mTargetCfreply = mCurrentMsg.getTargetReply();
                reply();
                toggleInput(true);
                break;
            case R.id.ll_channel_post_comment:
                mCurrentMsg = (MyReplyInfo) v.getTag();
                mTargetCfPost = mCurrentMsg.targetPost;
                mTargetCfreply = mCurrentMsg.targetReply;
//                showMenuDialog();
                showResource();                  
                break;
            default:
                break;
        }
    }
    
    private void showProfile(UserInfo userInfo) {
        if (userInfo == null) {
            toast(getString(R.string.prof_user_not_exist));
        } else {
            Intent iProfile = new Intent(this, NewProfileActivity.class);
            iProfile.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
            startActivity(iProfile);
        }
    }
    
//    private void saveDataToSp(String jsonData) {
//        Editor editor = mPreferences.edit();
//        if (mMessageType == MessageType.TYPE_AT_USER.ordinal()) {
//            editor.putString(SP_REMIND_JSON_DAY_AT, jsonData);
//        } else {
//            editor.putString(SP_REMIND_JSON_DAY_LIKE, jsonData);
//        }
//        editor.commit();
//    }

    private void checkIcon() {
        if (mEmotionPicker.isShown()) {
            mIvEmotion.setImageResource(R.drawable.button_ic_key);
        } else {
            mIvEmotion.setImageResource(R.drawable.button_emotion);
        }
    }


    private void startComment(String content, ArrayList<UserInfo> refUserInfos) {
        if (mIsReplyPost) {
        } else {
            CfReply parentReply = mCurrentMsg.getTargetReply() == null ?
                    mCurrentMsg.getReply() : mCurrentMsg.getTargetReply();
                    parentReply.targetPost = mCurrentMsg.targetPost;
            mTaskManager.startChannelPostReplyTask(parentReply,
                    mCurrentMsg.getReply(), content, refUserInfos);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            Utils.showInput(mMessageEditor);
        } else {
            mEmotionPicker.setVisible(false);
            Utils.hideInput(mMessageEditor);
        }
    }

    @Override
    public void onEmotionClicked(Emotion emotion) {
        mMessageEditor.getText().insert(mMessageEditor.getSelectionStart(), emotion.getSpannable(this));
    }

    @Override
    public void onBackspaceClicked() {
        Utils.dispatchDelEvent(mMessageEditor);
    }

    @Override
    public void onPreExecute(MsTaskType type) {
        getTitleBar().showProgress();
    }

    @Override
    public void onPostExecute(MsTaskType type, MsResponse response) {
        getTitleBar().hideProgress();
        switch (type) {
            case FORUM_COMMENT_POST:
            case FORUM_COMMENT_REPLY:
                if (response.isSuccessful()) {
                    showCommmentSucc();
                    getRemind(true);
                } else {
                    switch (response.code) {
                        case MsResponse.FAIL_HAS_BEEN_BANNED:
                            toast(R.string.no_speak_toast);
                            break;
                        default:
                            break;
                    }
                }
                break;
            default:
                break;
        }
    }


    private void showCommmentSucc() {
        mIvCommentSuc.setImageResource(R.drawable.img_sucssece_comment);
        mIvCommentSuc.setVisibility(View.VISIBLE);
        mIvCommentSuc.postDelayed(new Runnable() {

            @Override
            public void run() {
                mIvCommentSuc.setVisibility(View.GONE);
            }
        }, 2000);
    }
    
private TextWatcher mTextWatcher = new TextWatcher() {
        
        CharSequence lastChar;
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() >= 1) {
                lastChar = s.toString().substring(s.length() - 1);
                if ("@".equals(lastChar) && mHasCallback) {
                    startActivityForResult(new Intent(MyReplyActivity.this, RefFriendActivity.class), REQUEST_REF_SQUARE);
                    mHasCallback = false;
                }
            }
        }
    }; 
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REF_SQUARE && resultCode == RESULT_OK) {
            String refs = data.getStringExtra(RefFriendActivity.EXTRA_RESULT);
            ArrayList<UserInfo> datas = data.getParcelableArrayListExtra(RefFriendActivity.EXTRA_USERINFOS);
            if (mRefFriends == null || mRefFriends.size() == 0) {
                mRefFriends = datas; 
            } else {
                mRefFriends.addAll(datas);
            }
            int ss = mMessageEditor.getSelectionStart();
            Editable editable = mMessageEditor.getText();
            Editable s = editable.replace(ss - 1, editable.length(), refs);
            mMessageEditor.setText(s);
            mHasCallback = true;
        } else if (requestCode == REQUEST_REF_SQUARE && resultCode == RESULT_CANCELED) {
            int ss = mMessageEditor.getSelectionStart();
            Editable editable = mMessageEditor.getText();
            Editable s = editable.replace(ss - 1, editable.length(), "");
            mMessageEditor.setText(s);
            mHasCallback = true;
        }
    }

    @Override
    public void onDelClick(int index) {
        if (index < mRefFriends.size()) {
            mRefFriends.remove(index);
        }
    };
}