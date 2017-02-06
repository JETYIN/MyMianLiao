package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

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
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.CfReply;
import com.tjut.mianliao.forum.nova.MessageRemindManager.MessageType;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.PoliceHelper;
import com.tjut.mianliao.util.Utils;

public class ForumRemindActivity extends BaseActivity implements OnRefreshListener2<ListView>,
        DialogInterface.OnClickListener, OnClickListener, EmotionListener,
        OnFocusChangeListener, MsTaskListener, OnAtDelClicklistener{
    
    public static final String EXT_MESSAGE_TYPE = "ext_message_type";
    
    private static final String SP_REMIND_JSON_DAY_LIKE = "sp_remind_inbox_json_day_like";
    private static final String SP_REMIND_JSON_NIGHT_LIKE = "sp_remind_inbox_json_night_like";
    private static final String SP_REMIND_JSON_DAY_AT = "sp_remind_inbox_json_day_at";
    private static final String SP_REMIND_JSON_NIGHT_AT = "sp_remind_inbox_json_night_at";
    
    protected static final int MAX_REPLY_LEN = Integer.MAX_VALUE;

    PullToRefreshListView mPtrListView;
    ArrayList<InboxMessage> mMessages;
    private int mNameColor;
    private RemindAdapter mAdapter;
    private LightDialog mMenuDialog;
    private CfPost mTargetCfPost;

    private RichMlEditText mMessageEditor;
    private EmotionPicker mEmotionPicker;
    private boolean mIsReplyPost;
    private boolean mIsShowSchoolName = false;
    private InboxMessage mCurrentMsg;
    private MsTaskManager mTaskManager;
    private ImageView mIvCommentSuc;
    private LinearLayout mLlInput;
    private ImageView mIvEmotion;
    private SharedPreferences mPreferences;
    private ArrayList<UserInfo> mRefFriends;
    private boolean mHasCallback = true;
    
    private View mViewNoContent;
    private FrameLayout mViewParent;
    private int mMessageType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTaskManager = MsTaskManager.getInstance(this);
        mTaskManager.registerListener(this);
        mPreferences = DataHelper.getSpForData(this);
        mMessageType = getIntent().getIntExtra(EXT_MESSAGE_TYPE, -1);

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

        getTitleBar().setTitle(getString(R.string.fe_remind_msg));
        mNameColor = getResources().getColor(R.color.channel_post_reply_name);
        mMessages = new ArrayList<InboxMessage>();
        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptrlv_post_stream);

        mPtrListView.setMode(Mode.BOTH);
        mPtrListView.setOnRefreshListener(this);
        mAdapter = new RemindAdapter();
        mPtrListView.setAdapter(mAdapter);
        loadData();
        mViewNoContent.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                reload();
            }
        });
    }

	private void loadData() {
	    String json;
        if (mMessageType == MessageType.TYPE_AT_USER.ordinal()) {
            json = mPreferences.getString(SP_REMIND_JSON_DAY_AT, "[]");
        } else {
            json = mPreferences.getString(SP_REMIND_JSON_DAY_LIKE, "[]");
        }
	    ArrayList<InboxMessage> messages = null;
	    try {
            JSONArray ja = new JSONArray(json);
            messages = JsonUtil.getArray(ja, InboxMessage.TRANSFORMER);
            mMessages.addAll(messages);
            mAdapter.notifyDataSetChanged();
	    } catch (JSONException e) {
            e.printStackTrace();
        }
        mPtrListView.setRefreshing(Mode.PULL_FROM_START);
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

    private void getRemind(boolean refresh) {
        new ListInboxMessageTask(refresh).executeLong();
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        getRemind(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        getRemind(false);
    }

    private class ListInboxMessageTask extends MsTask {

        private boolean refresh;
        private int offset;

        public ListInboxMessageTask(boolean refresh) {
            super(ForumRemindActivity.this, MsRequest.INBOX_LIST);
            this.refresh = refresh;
            offset = refresh ? 0 : mMessages.size();
        }

        @Override
        protected void onPreExecute() {
//            getTitleBar().showProgress();
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder();
            sb.append("offset=").append(offset);
            if (mMessageType == MessageType.TYPE_NOTICE.ordinal()) {
                sb.append("&for_hate_like=1");
            } else {
                sb.append("&for_hate_like_except=1");
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mPtrListView.onRefreshComplete();
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                if (ja != null) {
                    saveDataToSp(ja.toString());
                }
                ArrayList<InboxMessage> messages = JsonUtil.getArray(ja, InboxMessage.TRANSFORMER);
                if (refresh) {
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

    private class RemindAdapter extends BaseAdapter {

        private UserInfo userInfo;

        @Override
        public int getCount() {
            return mMessages.size();
        }

        @Override
        public InboxMessage getItem(int position) {
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
            final InboxMessage msg = getItem(position);
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
            ImageView ivReply = (ImageView) view.findViewById(R.id.iv_reply);
            ivReply.setVisibility(View.VISIBLE);
            ivReply.setTag(msg);
            
            view.setOnClickListener(ForumRemindActivity.this);
            view.setTag(msg);
            
            CfReply reply = msg.getReply();
            boolean atFriend = isAtFriend(msg);
            if (reply != null || atFriend) {
                CfPost post = msg.getTargetPost();
                if (reply != null) {
                    userInfo = reply.userInfo;
                } else {
                    if (post != null) {
                        userInfo = post.userInfo;
                    }
                }
                Picasso.with(ForumRemindActivity.this)
                    .load(userInfo.getAvatar())
                    .placeholder(userInfo.defaultAvatar())
                    .into(avart);
                name.setText(userInfo.getDisplayName(ForumRemindActivity.this));
                gender.setImageResource(userInfo.gender == 0 ? R.drawable.img_girl : R.drawable.img_boy);
                if (userInfo.getLatestBadge() != null && userInfo.getLatestBadge().startsWith("http")) {
                    ivMedal.setVisibility(View.VISIBLE);
                    Picasso.with(ForumRemindActivity.this)
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
                avart.setOnClickListener(ForumRemindActivity.this);
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
            switch (msg.getMessageType()) {
                case InboxMessage.HATE_POST:
                    CfPost hatePost = msg.getTargetPost();
                    if (hatePost == null) {
                        cnt = getString(R.string.post_was_deleted);
                        strReContent = "";
                    } else {
                        switch (hatePost.style) {
                            case 0:
                                cnt = getString(R.string.post_who_dislike_my_post);
                                content.setCompoundDrawablesWithIntrinsicBounds(R.drawable.buttom_hate_hover, 0, 0, 0);
                                break;
                            case 1:
                                cnt = getString(R.string.post_pound_me_eggs);
                                content.setCompoundDrawablesWithIntrinsicBounds(R.drawable.button_egg_pressed, 0, 0, 0);
                                break;
                            default:
                                break;
                        }
                        strReContent = ": " + hatePost.content;
                    }
                    
                    content.setText("");
                    cnt = Utils.getColoredText(
                            ForumRemindActivity.this.getString(R.string.channel_remind_msg, cnt, strReContent), cnt,
                            mNameColor, false);
                    replyContent.setText(cnt);
                    showReply = false;
                    break;
                case InboxMessage.LIKE_COMMENT:
                    CfReply likeReply = msg.getTargetReply();
                    if (likeReply == null) {
                        cnt = getString(R.string.post_reply_was_deleted);
                        strReContent = "";
                    } else {
                        cnt = getString(R.string.post_who_like_my_reply);
                        strReContent = ": " + likeReply.content;
                    }
                    content.setCompoundDrawablesWithIntrinsicBounds(R.drawable.buttom_like_hover, 0, 0, 0);
                    content.setText("");
                    cnt = Utils.getColoredText(
                            ForumRemindActivity.this.getString(R.string.channel_remind_msg, cnt, strReContent), cnt,
                            mNameColor, false);
                    replyContent.setText(cnt);
                    showReply = false;
                    break;
                case InboxMessage.LIKE_POST:
                    CfPost likePost = msg.getTargetPost();
                    if (likePost == null) {
                        cnt = getString(R.string.post_was_deleted); 
                        strReContent = "";
                    } else {
                        switch (likePost.style) {
                            case 0:
                                cnt = getString(R.string.post_who_like_my_post);
                                content.setCompoundDrawablesWithIntrinsicBounds(R.drawable.buttom_like_hover, 0, 0, 0);
                                break;
                            case 1:
                                cnt = getString(R.string.post_give_me_flower);
                                content.setCompoundDrawablesWithIntrinsicBounds(R.drawable.button_flower_pressed, 0, 0, 0);
                                break;
                            default:
                                break;
                        }
                        strReContent = ": " + likePost.content;
                    }
                    content.setText("");
                    cnt = Utils.getColoredText(
                            ForumRemindActivity.this.getString(R.string.channel_remind_msg, cnt, strReContent), cnt,
                            mNameColor, false);
                    replyContent.setText(cnt);
                    showReply = false;
                    break;
                case InboxMessage.REPLY_COMMENT:
                    CfReply reReply = msg.getTargetReply();
                    if (reReply == null) {
                        cnt = getString(R.string.post_reply_was_deleted);
                        strReContent = "";
                    } else {
                        cnt = getString(R.string.post_who_reply_my_reply);
                        strReContent = ": " + reReply.content;
                    }
                    content.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    content.setText(reply);
                    cnt = Utils.getColoredText(
                            ForumRemindActivity.this.getString(R.string.channel_remind_msg, cnt, strReContent), cnt,
                            mNameColor, false);
                    replyContent.setText(cnt);
                    showReply = true;
                    break;
                case InboxMessage.REPLY_POST:
                    CfPost erPost = msg.getTargetPost();
                    if (erPost == null) {
                        cnt = getString(R.string.post_reply_was_deleted);
                        strReContent = "";
                    } else {
                        cnt = getString(R.string.post_who_reply_my_post);
                        strReContent = ": " + erPost.content;
                    }
                    content.setText(reply);
                    content.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    cnt = Utils.getColoredText(
                            ForumRemindActivity.this.getString(R.string.channel_remind_msg, cnt, strReContent), cnt,
                            mNameColor, false);
                    replyContent.setText(cnt);
                    showReply = true;
                    break;
                case InboxMessage.COMMENT_REPLIED_AT:// 评论的评论@
                    cfReply = msg.getReply();
                    cnt = getString(R.string.post_who_at_me);
                    CharSequence key;
                    if (cfReply != null) {
                        key = getString(R.string.post_who_mention_of_me_at_reply);
                        strReContent = key + cfReply.content ;
                    } else {
                        key = getString(R.string.post_reply_reply_was_deleted);
                        strReContent = key.toString();
                    }
                    content.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    content.setText(cnt);
                    replyContent.setText(Utils.getColoredText(strReContent, key, mNameColor, false));
                    showReply = true;
                    break;
                case InboxMessage.COMMENT_FORUM_THREAD_AT:// 帖子@
                    atPost = msg.getTargetPost();
                    cnt = getString(R.string.post_who_at_me);
                    if (atPost != null) {
                        key = getString(R.string.post_who_mention_of_me_at_post);
                        strReContent = key + atPost.content;
                    } else {
                        key = getString(R.string.post_was_deleted);
                        strReContent = key.toString();
                    }
            
                    content.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    content.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    content.setText(cnt);
                    replyContent.setText(Utils.getColoredText(strReContent, key, mNameColor, false));
                    showReply = false;
                    break;
                case InboxMessage.COMMENT_FORUM_REPLY_AT:// 评论@
                    cfReply = msg.getReply();
                    cnt = getString(R.string.post_who_at_me);
                    if (cfReply != null) {
                        key = getString(R.string.post_who_mention_of_me);
                        strReContent = key + cfReply.content ;
                    } else {
                        key = getString(R.string.post_reply_was_deleted);
                        strReContent = key.toString();
                    }
                    content.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    content.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    content.setText(cnt);
                    replyContent.setText(Utils.getColoredText(strReContent, key, mNameColor, false));
                    showReply = true;
                    break;
                    
                default:
                    break;
            }
            
            replyContent.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    mCurrentMsg = msg;
                    mTargetCfPost = mCurrentMsg.getTargetPost();
                    showResource();                            
                }
            });
            
            ivReply.setVisibility(showReply && !isAtFriend(msg)? View.VISIBLE : View.GONE);
            return view;
        }

        private boolean isAtFriend(InboxMessage msg) {
            return msg.getMessageType() == InboxMessage.COMMENT_FORUM_REPLY_AT ||
                    msg.getMessageType() == InboxMessage.COMMENT_FORUM_THREAD_AT ||
                    msg.getMessageType() == InboxMessage.COMMENT_REPLIED_AT;
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
        switch (mCurrentMsg.messageType) {
            case InboxMessage.HATE_POST:
            case InboxMessage.LIKE_COMMENT:
            case InboxMessage.LIKE_POST:
            case InboxMessage.COMMENT_FORUM_REPLY_AT:
            case InboxMessage.COMMENT_FORUM_THREAD_AT:
            case InboxMessage.COMMENT_REPLIED_AT:
                return false;
            default:
                return true;
        }

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
        switch (mCurrentMsg.messageType) {
            case InboxMessage.HATE_POST:
                if (mTargetCfPost == null) {
                    toast(getString(R.string.post_was_deleted));
                } else {
                    toReply();
                }
                mIsReplyPost = true;
                break;
            case InboxMessage.LIKE_COMMENT:
                if (mTargetCfPost == null) {
                    toast(getString(R.string.post_reply_was_deleted));
                } else {
                    toReply();
                }
                mIsReplyPost = false;
                break;
            case InboxMessage.LIKE_POST:
                if (mTargetCfPost == null) {
                    toast(getString(R.string.post_was_deleted));
                } else {
                    toReply();
                }
                mIsReplyPost = true;
                break;
            case InboxMessage.REPLY_COMMENT:
                if (mTargetCfPost == null) {
                    toast(getString(R.string.post_reply_was_deleted));
                } else {
                    toReply();
                }
                mIsReplyPost = false;
                break;
            case InboxMessage.REPLY_POST:
                if (mTargetCfPost == null) {
                    toast(getString(R.string.post_reply_was_deleted));
                } else {
                    toReply();
                }
                mIsReplyPost = false;
                break;
            default:
                break;
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
                mCurrentMsg = (InboxMessage) v.getTag();
                mTargetCfPost = mCurrentMsg.getTargetPost();
                reply();
                toggleInput(true);
                break;
            case R.id.ll_channel_post_comment:
                mCurrentMsg = (InboxMessage) v.getTag();
                mTargetCfPost = mCurrentMsg.targetPost;
                showMenuDialog();                   
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
    
    private void saveDataToSp(String jsonData) {
        Editor editor = mPreferences.edit();
        if (mMessageType == MessageType.TYPE_AT_USER.ordinal()) {
            editor.putString(SP_REMIND_JSON_DAY_AT, jsonData);
        } else {
            editor.putString(SP_REMIND_JSON_DAY_LIKE, jsonData);
        }
        editor.commit();
    }

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
                    startActivityForResult(new Intent(ForumRemindActivity.this, RefFriendActivity.class), BasePostActivity.REQUEST_REF_SQUARE);
                    mHasCallback = false;
                }
            }
        }
    }; 
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BasePostActivity.REQUEST_REF_SQUARE && resultCode == RESULT_OK) {
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
        } else if (requestCode == BasePostActivity.REQUEST_REF_SQUARE && resultCode == RESULT_CANCELED) {
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