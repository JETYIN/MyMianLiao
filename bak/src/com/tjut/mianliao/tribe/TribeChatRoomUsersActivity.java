package com.tjut.mianliao.tribe;

import java.util.ArrayList;

import android.R.bool;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nineoldandroids.view.ViewHelper;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.contact.UserEntryManager;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.ChatRecord;
import com.tjut.mianliao.data.contact.UserEntry;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.tribe.TribeChatRoomInfo;
import com.tjut.mianliao.data.tribe.TribeInfo;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;


public class TribeChatRoomUsersActivity extends BaseActivity implements OnRefreshListener2<ListView>{
    
    @ViewInject(R.id.ptr_chat_room_user)
    private PullToRefreshListView mPtrRoomUser;
    
    private ArrayList<UserInfo> mUsers;
    private UserEntryManager mUserEntryManger;
    private UserInfoManager mUserInfoManager;
    private boolean mIsNightMode;
    private CharSequence mFilterConstraint;
    private TribeChatRoomInfo mRoomInfo;
    private UserInfo mUser;
    
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_chat_room_users;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mIsNightMode = Settings.getInstance(this).isNightMode();
        mRoomInfo = getIntent().getParcelableExtra(TribeChatRoomInfo.INTENT_EXTRA_INFO);
        getTitleBar().setTitle(R.string.tribe_member_list);
        mUserEntryManger = UserEntryManager.getInstance(this);
        mUserInfoManager = UserInfoManager.getInstance(this);
        mUsers = new ArrayList<UserInfo>();
        mUser = AccountInfo.getInstance(this).getUserInfo();
        mPtrRoomUser.setMode(Mode.BOTH);
        mPtrRoomUser.setOnRefreshListener(this);
        mPtrRoomUser.setAdapter(mUsersAdapter);
        fetchTribes(true);
        checkDayNightUI();
    }
    
    private void checkDayNightUI() {
        if (mIsNightMode) {
            mPtrRoomUser.setBackgroundResource(R.drawable.bg);
        }
    }

    private BaseAdapter mUsersAdapter = new BaseAdapter() {
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_contact, parent, false);
            }
            UserInfo userInfo = getItem(position);
            TextView tvName = (TextView) view.findViewById(R.id.tv_contact_name);
            TextView tvInfo = (TextView) view.findViewById(R.id.tv_short_desc);
            ProImageView ivMedal = (ProImageView) view.findViewById(R.id.iv_medal);
            ImageView ivTypeIcon = (ImageView) view.findViewById(R.id.iv_type_icon);
            if (userInfo != null) {
                view.findViewById(R.id.iv_vip_bg).setVisibility(userInfo.vip && !mIsNightMode ?
                        View.VISIBLE : View.GONE);
            } else {
                mUserInfoManager.acquireUserInfo(userInfo.jid);
                view.findViewById(R.id.iv_vip_bg).setVisibility(View.GONE);
            }
            String avatar = null;
            int avatarId;
            String name;
            String shortDesc = null;
            if (userInfo != null) {
                avatarId = userInfo.defaultAvatar();
                name = userInfo.getDisplayName(TribeChatRoomUsersActivity.this);
                avatar = userInfo.getAvatar();
                shortDesc = userInfo.shortDesc;
                if (!mIsNightMode && userInfo.getLatestBadge() != null &&
                        userInfo.getLatestBadge().startsWith("http")) {
                    ivMedal.setVisibility(View.VISIBLE);
                    ivMedal.setImage(userInfo.getLatestBadge(), R.drawable.ic_medal_empty);
                } else {
                    ivMedal.setVisibility(View.GONE);
                }
                // update type icon;it while show in day time,or it should hide
                int resIcon = userInfo.getTypeIcon();
                if (!mIsNightMode && resIcon > 0) {
                    ivTypeIcon.setImageResource(resIcon);
                    ivTypeIcon.setVisibility(View.VISIBLE);
                } else {
                    ivTypeIcon.setVisibility(View.GONE);
                }
            } else {
                avatarId = R.drawable.chat_botton_bg_faviconboy;
                name = userInfo.name;
            }

            checkDayNightUI(tvInfo);
            
            ((ProImageView) view.findViewById(R.id.iv_contact_avatar)).setImage(avatar, avatarId);

            tvName.setText(name);

            tvInfo.setText(shortDesc);

            int statId = mUserEntryManger.getPresence(userInfo.jid) ?
                    R.drawable.ic_status_online : R.drawable.ic_status_offline;
            view.findViewById(R.id.v_connection_state).setBackgroundResource(statId);
            view.setTag(userInfo);
            view.setOnClickListener(mUserClickListen);
            return view;
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public UserInfo getItem(int position) {
            return mUsers.get(position);
        }
        
        @Override
        public int getCount() {
            return mUsers.size();
        }
    };
    
    private OnClickListener mUserClickListen = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            UserInfo mUserInfo = (UserInfo) v.getTag(); 
            Intent intent = new Intent(TribeChatRoomUsersActivity.this, ProfileActivity.class);
            intent.putExtra(UserInfo.INTENT_EXTRA_INFO, mUserInfo);
            startActivity(intent);
        }
    };
    
    private void checkDayNightUI(TextView tvInfo) {
        if (mIsNightMode) {
            tvInfo.setVisibility(View.GONE);
        }
    }
    
    private class getGroupUsersTask extends MsTask {
        private int mOffset;
        public getGroupUsersTask(int offset) {
            super(TribeChatRoomUsersActivity.this, MsRequest.TRIBE_GET_CHAT_ROOM_USERS);
            mOffset = offset;
        }
        
        @Override
        protected String buildParams() 
        {
            return new StringBuilder("uid=").append(mUser.userId)
                    .append("&tribe_id=").append(mRoomInfo.tribeId)
                    .append("&room_id=").append(mRoomInfo.roomId)
                    .toString();
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrRoomUser.onRefreshComplete();
            if (response.isSuccessful()) {
                mUsers.clear();
                ArrayList<UserInfo> users = JsonUtil.getArray(
                        response.getJsonArray(), UserInfo.TRANSFORMER);
                mUsers.addAll(users);
                mUsersAdapter.notifyDataSetChanged();
            }
        }
        
    }
    
    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchTribes(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchTribes(false);
    }
    
    
    private void fetchTribes(boolean refresh) {
        int size = mUsersAdapter.getCount();
        int offset = refresh ? 0 : size;
        new getGroupUsersTask(offset).executeLong();
    }
    
}

