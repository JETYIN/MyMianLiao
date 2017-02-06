package com.tjut.mianliao.tribe;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.contact.UserEntryManager;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.tribe.TribeChatRoomInfo;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;


public class TribeChatRoomUsersActivity extends BaseActivity implements OnRefreshListener2<ListView>{
    
    @ViewInject(R.id.ptr_chat_room_user)
    private PullToRefreshListView mPtrRoomUser;
    
    private ArrayList<UserInfo> mUsers;
    private UserEntryManager mUserEntryManger;
    private UserInfoManager mUserInfoManager;
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
    }

    private BaseAdapter mUsersAdapter = new BaseAdapter() {
        
        @Override
        public int getCount() {
            return mUsers.size();
        }

        @Override
        public UserInfo getItem(int position) {
            return mUsers.get(position);
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
                view = mInflater.inflate(R.layout.list_item_contact, parent, false);
            }
            UserInfo userInfo = getItem(position);
            TextView tvName = (TextView) view.findViewById(R.id.tv_contact_name);
            TextView tvInfo = (TextView) view.findViewById(R.id.tv_short_desc);
            ImageView ivMedal = (ImageView) view.findViewById(R.id.iv_medal);
            ImageView ivTypeIcon = (ImageView) view.findViewById(R.id.iv_type_icon);
            if (userInfo != null) {
                view.findViewById(R.id.iv_vip_bg).setVisibility(userInfo.vip ?
                        View.VISIBLE : View.GONE);
            } else {
                mUserInfoManager.acquireUserInfo(userInfo.jid);
                view.findViewById(R.id.iv_vip_bg).setVisibility(View.GONE);
                return null;
            }
            String avatar = userInfo.getAvatar();
            int avatarId = userInfo.defaultAvatar();
            String name = userInfo.getDisplayName(TribeChatRoomUsersActivity.this);;
            String shortDesc = userInfo.shortDesc;
            if (userInfo.getLatestBadge() != null &&
                    userInfo.getLatestBadge().startsWith("http")) {
                ivMedal.setVisibility(View.VISIBLE);
                Picasso.with(TribeChatRoomUsersActivity.this)
                    .load(userInfo.getLatestBadge())
                    .placeholder(R.drawable.ic_medal_empty)
                    .into(ivMedal);
            } else {
                ivMedal.setVisibility(View.GONE);
            }
            // update type icon;it while show in day time,or it should hide
            int resIcon = userInfo.getTypeIcon();
            if (resIcon > 0) {
                ivTypeIcon.setImageResource(resIcon);
                ivTypeIcon.setVisibility(View.VISIBLE);
            } else {
                ivTypeIcon.setVisibility(View.GONE);
            }

            ImageView ivAvatar = (ImageView) view.findViewById(R.id.iv_contact_avatar);
            if (avatar != null) {
                Picasso.with(TribeChatRoomUsersActivity.this)
                    .load(avatar)
                    .placeholder(avatarId)
                    .into(ivAvatar);
            } else {
                Picasso.with(TribeChatRoomUsersActivity.this)
                    .load(avatarId)
                    .into(ivAvatar);
            }

            tvName.setText(name);

            tvInfo.setText(shortDesc);

            view.setTag(userInfo);
            view.setOnClickListener(mUserClickListen);
            return view;
        }
    };
    
    private OnClickListener mUserClickListen = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            UserInfo mUserInfo = (UserInfo) v.getTag(); 
            Intent intent = new Intent(TribeChatRoomUsersActivity.this, NewProfileActivity.class);
            intent.putExtra(UserInfo.INTENT_EXTRA_INFO, mUserInfo);
            startActivity(intent);
        }
    };
    
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

