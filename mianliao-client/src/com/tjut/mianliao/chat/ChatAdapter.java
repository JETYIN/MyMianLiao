package com.tjut.mianliao.chat;

import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tjut.mianliao.MapActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.EmotionTextView;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProFrameLayout;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.StaticMapView;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.ChatRecord;
import com.tjut.mianliao.data.IMResource;
import com.tjut.mianliao.data.LatLngWrapper;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.im.IMAudioManager;
import com.tjut.mianliao.im.IMResourceManager;
import com.tjut.mianliao.im.ShareLocationActivity;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.Utils;
import com.tjut.mianliao.xmpp.ChatHelper;

public class ChatAdapter extends BaseAdapter implements OnCompletionListener,
        View.OnClickListener, DialogInterface.OnClickListener {

    private enum ViewType {
        CHAT_ME,
        CHAT,
        CHAT_TIME,
        CHAT_INFO
    }

    private static final long MAX_SESSION_INTERVAL = 15 * 60 * 1000;

    private Context mContext;
    private LayoutInflater mInflater;
    private ChatHelper mChatHelper;
    private IMAudioManager mImAudioManager;
    private AccountInfo mAccountInfo;
    private LightDialog mShareLocDialog;

    private String mChatTarget;
    private ArrayList<String> mImageUrls;
    private ArrayList<ChatRecord> mRecords;
    private ChatRecord mPlayingChatRecord;

    private UserInfoManager mUserInfoManager;
    private IMResourceManager mIMResManager;
    private IMResource[] mIMResBubbles;
    private int[][] mDefaultBubbles, mDefaultColors, mColors;
    private boolean isGroupChat, mShowMyName;
    private int mBigEmotionSize, mNormalEmotionSize;

    public ChatAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mAccountInfo = AccountInfo.getInstance(context);
        mImAudioManager = IMAudioManager.getInstance(context);
        mChatHelper = ChatHelper.getInstance(context);
        mUserInfoManager = UserInfoManager.getInstance(context);
        mIMResManager = IMResourceManager.getInstance(context);
        mImageUrls = new ArrayList<String>();
        mRecords = new ArrayList<ChatRecord>();
        mIMResBubbles = new IMResource[2];
        
        mDefaultBubbles = new int[][] {
                new int[] {
                        R.drawable.bubble_me,
                        R.drawable.bubble_me_img
                },
                new int[] {
                        R.drawable.bubble_other,
                        R.drawable.bubble_other_img
                }
        };

        Resources res = context.getResources();
        int colorMe = res.getColor(R.color.chat_item_me);
        int colorMeDisabled = res.getColor(R.color.chat_item_me_disabled);
        int colorOther = res.getColor(R.color.chat_item_other);
        int colorOtherDisabled = res.getColor(R.color.chat_item_other_disabled);
        
        mBigEmotionSize = res.getDimensionPixelSize(R.dimen.big_emo_size);
        mNormalEmotionSize = res.getDimensionPixelSize(R.dimen.emo_size_medium);
        
        mDefaultColors = new int[][] {
                new int[] { colorMe, colorMeDisabled },
                new int[] { colorOther, colorOtherDisabled }
        };
        mColors = new int[][] {
                new int[] { colorMe, colorMeDisabled },
                new int[] { colorOther, colorOtherDisabled }
        };
    }

    public void isGroupChat(boolean groupChat) {
        isGroupChat = groupChat;
    }
    
    public void showMyName(boolean showMyName) {
        mShowMyName = showMyName;
    }
    
    public void destroy() {
        stopPlayVoice();
    }

    public ChatAdapter setChatTarget(String target) {
        mChatTarget = target;
        return this;
    }

    public void add(ChatRecord record) {
        addImageUrl(record, mImageUrls);
        mRecords.add(record);
        notifyDataSetChanged();
    }

    public int addAll(ArrayList<ChatRecord> list) {
        long timestamp = 0;
        ArrayList<String> imageUrls = new ArrayList<String>();
        ArrayList<ChatRecord> records = new ArrayList<ChatRecord>();
        for (ChatRecord cr : list) {
            addImageUrl(cr, imageUrls);

            if (cr.timestamp - timestamp > MAX_SESSION_INTERVAL) {
                timestamp = cr.timestamp;
                records.add(ChatRecord.createTimeRecord(timestamp));
            }
            records.add(cr);
        }
        mImageUrls.addAll(0, imageUrls);
        mRecords.addAll(0, records);
        notifyDataSetChanged();
        return records.size();
    }

    public void removeAll() {
        mRecords.clear();
    }

    public void updateBubble(int userId, IMResource res) {
        int index = mAccountInfo.getUserId() == userId ? 0 : 1;
        mIMResBubbles[index] = res;
        mColors[index][0] = mDefaultColors[index][0];
        mColors[index][1] = mDefaultColors[index][1];

        if (res != null) {
            int i = 0;
            for (String s : res.sn.split("\\|")) {
                try {
                    mColors[index][i] = Color.parseColor(s);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                i++;
                if (i > 1) {
                    break;
                }
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_msg_text:
                mIMResManager.playArmature((ChatRecord) v.getTag());
                break;
            case R.id.ll_msg_picture:
                viewPicture((ChatRecord) v.getTag());
                break;
            case R.id.ll_msg_voice:
                operateVoice((ChatRecord) v.getTag());
                break;
            case R.id.ll_msg_loc:
                viewLocation((ChatRecord) v.getTag());
                break;
            case R.id.ll_msg_share_loc:
                showShareLocDialog();
                break;
            case R.id.iv_avatar:
                showProfile((UserInfo) v.getTag());
                break;
            default:
                break;
        }
    }
    
    private void showProfile(UserInfo userInfo) {
        Intent iProfile = new Intent(mContext, NewProfileActivity.class);
        iProfile.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
        mContext.startActivity(iProfile);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mShareLocDialog) {
            Intent intent = new Intent(mContext, ShareLocationActivity.class);
            intent.putExtra(ChatActivity.EXTRA_CHAT_TARGET, mChatTarget);
            mContext.startActivity(intent);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlayingChatRecord.voicePlaying = false;
        mPlayingChatRecord = null;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mRecords.size();
    }

    @Override
    public ChatRecord getItem(int position) {
        return mRecords.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return ViewType.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        ChatRecord record = getItem(position);
        switch (record.type) {
            case ChatRecord.CHAT_TYPE_TIME:
                return ViewType.CHAT_TIME.ordinal();
            case ChatRecord.CHAT_TYPE_SHARE_OVER:
                return ViewType.CHAT_INFO.ordinal();
            default:
                return record.isFrom(mAccountInfo.getAccount())
                        ? ViewType.CHAT_ME.ordinal() : ViewType.CHAT.ordinal();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewType type = ViewType.values()[getItemViewType(position)];
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = inflateView(type, parent);
        }

        if (view == null) {
            return null;
        }

        ChatRecord record = getItem(position);
        switch (type) {
            case CHAT_ME:
                updateRecordView(view, record, true);
                break;
            case CHAT:
                updateRecordView(view, record, false);
                break;
            case CHAT_TIME:
                Utils.setText(view, R.id.tv_time,
                        Utils.getTimeDesc(mContext, record.timestamp));
                break;
            default:
                break;
        }

        return view;
    }

    private View inflateView(ViewType type, ViewGroup parent) {
        switch (type) {
            case CHAT_ME:
                return mInflater.inflate(R.layout.list_item_chat_me, parent, false);
            case CHAT:
                return mInflater.inflate(R.layout.list_item_chat, parent, false);
            case CHAT_TIME:
                return mInflater.inflate(R.layout.list_item_chat_time, parent, false);
            case CHAT_INFO:
                return mInflater.inflate(R.layout.list_item_chat_info, parent, false);
            default:
                return null;
        }
    }

    private void updateRecordView(View view, ChatRecord record, boolean isMe) {
        updateUserView(view, record, isMe);
        updateBubbleView(view, record, isMe);

        View llMsgText = view.findViewById(R.id.ll_msg_text);
        llMsgText.setVisibility(View.GONE);
        View llMsgPicture = view.findViewById(R.id.ll_msg_picture);
        llMsgPicture.setVisibility(View.GONE);
        View llMsgVoice = view.findViewById(R.id.ll_msg_voice);
        llMsgVoice.setVisibility(View.GONE);
        View llMsgLoc = view.findViewById(R.id.ll_msg_loc);
        llMsgLoc.setVisibility(View.GONE);
        View llMsgShareLoc = view.findViewById(R.id.ll_msg_share_loc);
        llMsgShareLoc.setVisibility(View.GONE);
        
        switch (record.type) {
            case ChatRecord.CHAT_TYPE_TEXT:
                updateTextView(llMsgText, record, isMe);
                break;
            case ChatRecord.CHAT_TYPE_PICTURE:
                updatePictureView(llMsgPicture, record, isMe);
                break;
            case ChatRecord.CHAT_TYPE_VOICE:
                updateVoiceView(llMsgVoice, record, isMe);
                break;
            case ChatRecord.CHAT_TYPE_LOCATION:
                updateLocView(llMsgLoc, record, isMe);
                break;
            case ChatRecord.CHAT_TYPE_SHARE_REQUEST:
                updateShareLocView(llMsgShareLoc, record, isMe);
                break;
            case ChatRecord.CHAT_TYPE_BIG_EMOTION:
                updateTextView(llMsgText, record, isMe);
                break;
            default:
                break;
        }
    }

    private void updateUserView(View view, ChatRecord record, boolean isMe) {
        UserInfo user = isMe ? mAccountInfo.getUserInfo() : mUserInfoManager.getUserInfo(
                record.isGroupChat ? record.from : mChatTarget);
        ProImageView ivAvatar = (ProImageView) view.findViewById(R.id.iv_avatar);
        TextView tvName = (TextView) view.findViewById(R.id.tv_name);
        TextView tvNameMe = (TextView) view.findViewById(R.id.tv_name_me);
        
        if (tvName != null && user != null) {
            tvName.setText(user.getDisplayName(mContext));
            tvName.setVisibility(isGroupChat ? View.VISIBLE : View.GONE);
        }
        if (tvNameMe != null && user != null) {
            tvNameMe.setText(user.getDisplayName(mContext));
            tvNameMe.setVisibility(mShowMyName ? View.VISIBLE : View.GONE);
        }
        if (user != null) {
            view.findViewById(R.id.iv_vip_bg).setVisibility(user.vip ?
                    View.VISIBLE : View.GONE);
        } else {
            view.findViewById(R.id.iv_vip_bg).setVisibility(View.GONE);
        }
        ivAvatar.setTag(user);
        ivAvatar.setOnClickListener(this);
        if (user == null) {
            mUserInfoManager.acquireUserInfo(record.from);
            ivAvatar.setImageResource(R.drawable.chat_botton_bg_faviconboy);
        } else {
            ivAvatar.setImage(user.getAvatar(), user.defaultAvatar());
        }
    }

    private void updateBubbleView(View view, ChatRecord record, boolean isMe) {
        ProFrameLayout flChat = (ProFrameLayout) view.findViewById(R.id.fl_chat);
        int index = isMe ? 0 : 1;
        IMResource resBubble = mIMResBubbles[index];
        String url;
        int defaultBg;
        switch (record.type) {
            case ChatRecord.CHAT_TYPE_LOCATION:
            case ChatRecord.CHAT_TYPE_PICTURE:
                url = resBubble == null ? null : resBubble.urls[index][1];
                defaultBg = mDefaultBubbles[index][1];
                break;
            case ChatRecord.CHAT_TYPE_BIG_EMOTION:
                url = null;
                defaultBg = 0;
                break;
            default:
                url = resBubble == null ? null : resBubble.urls[index][0];
                defaultBg = mDefaultBubbles[index][0];
                break;
        }
        if (url == null && defaultBg == 0) {
            flChat.setBackgroundColor(Color.TRANSPARENT);
        } else {
            flChat.setBackground(url, defaultBg);
        }
    }

    private void updateTextView(View view, ChatRecord record, boolean isMe) {
        view.setVisibility(View.VISIBLE);
        view.setOnClickListener(this);
        view.setTag(record);

        EmotionTextView tvText = (EmotionTextView) view.findViewById(R.id.tv_msg_text);
        if (record.type == ChatRecord.CHAT_TYPE_BIG_EMOTION) {
            tvText.setEmotionSize(mBigEmotionSize);
        } else {
            tvText.setEmotionSize(mNormalEmotionSize);
        }
        tvText.setText(record.text);
        setTextColor(tvText, isMe, view.isEnabled());
    }

    private void updatePictureView(View view, ChatRecord record, boolean isMe) {
        view.setVisibility(View.VISIBLE);
        view.setOnClickListener(this);
        view.setTag(record);

        ProImageView ivPicture = (ProImageView) view.findViewById(R.id.iv_msg_picture);
        if (record.hasUrl()) {
            Picasso.with(mContext)
            .load(record.getThumbUrl())
            .placeholder(R.drawable.bg_img_loading)
            .into(ivPicture);
        } else if (record.isFileExist()) {
            ivPicture.setImageBitmap(Utils.fileToBitmap(record.filePath));
        }
    }

    private void updateVoiceView(View view, ChatRecord record, boolean isMe) {
        view.setVisibility(View.VISIBLE);
        view.setOnClickListener(this);
        view.setTag(record);

        TextView tvVoice = (TextView) view.findViewById(R.id.tv_msg_voice);
        tvVoice.setText(mContext.getString(
                R.string.cht_msg_voice, (int) record.voiceLength));
        setTextColor(tvVoice, isMe, view.isEnabled());

        ImageView ivVoice = (ImageView) view.findViewById(R.id.iv_msg_voice);
        ImageView ivVoiceMe = (ImageView) view.findViewById(R.id.iv_msg_voice_me);
        AnimationDrawable animVoice;
        if (isMe) {
            ivVoice.setVisibility(View.GONE);
            ivVoiceMe.setVisibility(View.VISIBLE);
            animVoice = (AnimationDrawable) ivVoiceMe.getDrawable();
        } else {
            ivVoiceMe.setVisibility(View.GONE);
            ivVoice.setVisibility(View.VISIBLE);
            animVoice = (AnimationDrawable) ivVoice.getDrawable();
        }

        if (record.voicePlaying) {
            animVoice.start();
        } else {
            animVoice.stop();
            animVoice.selectDrawable(0);
        }
    }

    private void updateLocView(View view, ChatRecord record, boolean isMe) {
        view.setVisibility(View.VISIBLE);
        view.setOnClickListener(this);
        view.setTag(record);

        TextView tvAddr = (TextView) view.findViewById(R.id.tv_msg_addr);
        tvAddr.setText(record.address);
        setTextColor(tvAddr, isMe, view.isEnabled());

        StaticMapView smvMap = (StaticMapView) view.findViewById(R.id.smv_msg_map);
        smvMap.setMarkerResource(R.drawable.map_ic_small_place);
        smvMap.showMap(record.longitude, record.latitude);
    }

    private void updateShareLocView(View view, ChatRecord record, boolean isMe) {
        view.setVisibility(View.VISIBLE);
        view.setOnClickListener(this);
        view.setTag(record);
        view.setEnabled(!mChatHelper.isNotSharingLoc());

        TextView tvShareLoc = (TextView) view.findViewById(R.id.tv_msg_share_loc);
        setTextColor(tvShareLoc, isMe, view.isEnabled());

        ImageView ivShareLoc = (ImageView) view.findViewById(R.id.iv_msg_share_loc);
        ImageView ivShareLocMe = (ImageView) view.findViewById(R.id.iv_msg_share_loc_me);
        if (isMe) {
            ivShareLoc.setVisibility(View.GONE);
            ivShareLocMe.setVisibility(View.VISIBLE);
        } else {
            ivShareLocMe.setVisibility(View.GONE);
            ivShareLoc.setVisibility(View.VISIBLE);
        }
    }

    private void setTextColor(TextView tv, boolean isMe, boolean enabled) {
        tv.setTextColor(mColors[isMe ? 0 : 1][enabled ? 0 : 1]);
    }

    private void showShareLocDialog() {
        if (mShareLocDialog == null) {
            mShareLocDialog = new LightDialog(mContext)
                    .setTitleLd(R.string.cht_request_dialog_title)
                    .setMessage(R.string.cht_request_dialog_content)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, this);
        }
        mShareLocDialog.show();
    }

    private void operateVoice(ChatRecord record) {
        boolean changed = stopPlayVoice();
        if (mPlayingChatRecord != record) {
            changed |= startPlayVoice(record);
        } else {
            mPlayingChatRecord = null;
        }
        if (changed) {
            notifyDataSetChanged();
        }
    }

    private void addImageUrl(ChatRecord record, ArrayList<String> imageUrls) {
        if (record.type == ChatRecord.CHAT_TYPE_PICTURE) {
            if (record.hasUrl()) {
                imageUrls.add(record.url);
            } else if (record.isFileExist()) {
                imageUrls.add(record.filePath);
            } 
        }
    }  

    private void viewPicture(ChatRecord record) {
        if (record != null) {
            boolean hasUrl = record.hasUrl();
            String url = hasUrl ? record.url : record.filePath;
            int index = mImageUrls.indexOf(url);
            Utils.viewImages(mContext, mImageUrls, index);
        }
    }

    private void viewLocation(ChatRecord record) {
        if (record != null) {
            Intent iMap = new Intent(mContext, MapActivity.class);
            iMap.putExtra(MapActivity.EXTRA_PICK_LOCATION, false);
            iMap.putExtra(MapActivity.EXTRA_LOCATION,
                    new LatLngWrapper(record.latitude, record.longitude));
            mContext.startActivity(iMap);
        }
    }

    private boolean startPlayVoice(ChatRecord record) {
        if (!record.isFileExist()) {
            return false;
        }
        mImAudioManager.startPlayAudio(record.filePath);
        mImAudioManager.setOnCompletionListener(this);
        record.voicePlaying = true;
        mPlayingChatRecord = record;
        return true;
    }

    public boolean stopPlayVoice() {
        if (mPlayingChatRecord == null || !mPlayingChatRecord.voicePlaying) {
            return false;
        }
        mImAudioManager.stopPlayAudio();
        mPlayingChatRecord.voicePlaying = false;
        return true;

    }

    public ArrayList<String> getImageUrls() {
        return mImageUrls;
    }
}
