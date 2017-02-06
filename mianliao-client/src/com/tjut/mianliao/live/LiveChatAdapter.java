package com.tjut.mianliao.live;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.contact.ContactUpdateCenter;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.contact.UserRemarkManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.ChatRecord;
import com.tjut.mianliao.data.contact.UserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YoopWu on 2016/6/15 0015.
 */
public class LiveChatAdapter extends BaseAdapter implements ContactUpdateCenter.ContactObserver {

    private static final int COLOR_SYSTEM_MSG = 0xff58ff9f;
    private static final int COLOR_GIFT_MSG = 0xffff859f;
    private static final int COLOR_CHAT_MSG = 0xffffd562;
    private static final int COLOR_COME_MSG = 0xffffffff;

    private LayoutInflater mInflater;

    private Context mContext;

    private List<ChatRecord> mChatMessages;

    private AccountInfo mAccountInfo;

    private UserInfoManager mUserInfoManager;

    private boolean mIsLastViewVisiable;

    public LiveChatAdapter(Context context) {
        mContext = context.getApplicationContext();
        mChatMessages = new ArrayList<ChatRecord>();
        mInflater = LayoutInflater.from(context);
        mUserInfoManager = UserInfoManager.getInstance(context);
        mAccountInfo = AccountInfo.getInstance(context);
        ContactUpdateCenter.registerObserver(this);
    }

    public void reset(List<ChatRecord> messages) {
        if (messages == null) {
            return;
        }
        mChatMessages.clear();
        append(messages);
    }

    public void append(List<ChatRecord> messages) {
        if (messages == null) {
            return;
        }
        mChatMessages.addAll(messages);
        notifyDataSetChanged();
    }

    public void add(ChatRecord record) {
        if (record != null) {
            mChatMessages.add(record);
            notifyDataSetChanged();
        }
    }

    public void setLastViewIsVisible(boolean visible) {
        mIsLastViewVisiable = visible;
    }

    @Override
    public int getCount() {
        return mChatMessages.size();
    }

    @Override
    public ChatRecord getItem(int position) {
        return mChatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_live_message_normal, parent, false);
        }
        ChatRecord record = getItem(position);
        TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
        TextView tvMsg = (TextView) convertView.findViewById(R.id.tv_msg_content);
        UserInfo userInfo = null;
        if (record.isFrom(mAccountInfo.getAccount())) {
            userInfo = mAccountInfo.getUserInfo();
        } else {
            userInfo = mUserInfoManager.getUserInfo(record.from);
        }
        if (userInfo != null) {
            tvName.setText(userInfo.getDisplayName(mContext) + ": ");
        } else {
            mUserInfoManager.acquireUserInfo(record.from);
        }
        tvMsg.setText(record.text);
        int mHeight = convertView.getMeasuredHeight();
        if (getCount() > 6 && position + 5 >= getCount() - 1 && mIsLastViewVisiable) {
            Animation mAnimation = new TranslateAnimation(0f, 0f, mHeight, 0f);
            mAnimation.setDuration(500);
            convertView.setAnimation(mAnimation);
        } else {
            convertView.setAnimation(null);
        }
        if (record.type == ChatRecord.CHAT_TYPE_SYS_MSG) {
            tvMsg.setTextColor(COLOR_SYSTEM_MSG);
            tvName.setTextColor(COLOR_SYSTEM_MSG);
        } else if (record.type == ChatRecord.CHAT_TYPE_SPECIAL_GIFT ||
                record.type == ChatRecord.CHAT_TYPE_NORMAL_GIFT) {
            tvMsg.setTextColor(COLOR_GIFT_MSG);
            tvName.setTextColor(COLOR_CHAT_MSG);
        } else if (record.type == ChatRecord.CHAT_TYPE_COMEIN_MSG) {
            tvMsg.setTextColor(COLOR_COME_MSG);
            tvName.setTextColor(COLOR_CHAT_MSG);
        } else {
            tvMsg.setTextColor(COLOR_COME_MSG);
            tvName.setTextColor(COLOR_CHAT_MSG);
        }
        return convertView;
    }


    protected AnimationSet getAnimation() {
        int duration=300;
        AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.8f, 1.0f);
        animation.setDuration(duration);
        set.addAnimation(animation);

        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                1.0f, Animation.RELATIVE_TO_SELF, .0f);
        animation.setDuration(duration);
        set.addAnimation(animation);
        set.setFillAfter(true);

        return set;
    }

    @Override
    public void onContactsUpdated(ContactUpdateCenter.UpdateType type, Object data) {
        switch (type) {
            case UserInfo:
                notifyDataSetChanged();
                break;
            default:break;
        }
    }
}
