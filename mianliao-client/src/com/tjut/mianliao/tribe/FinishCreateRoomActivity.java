package com.tjut.mianliao.tribe;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.chat.AddFriendToGroupActivity;
import com.tjut.mianliao.chat.GroupChatInfoActivity;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.TitleBar;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.GroupMember;
import com.tjut.mianliao.data.contact.UserEntry;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.tribe.TribeChatRoomInfo;
import com.tjut.mianliao.data.tribe.TribeInfo;
import com.tjut.mianliao.tribe.TribeChatManager.TribeChatListener;
import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class FinishCreateRoomActivity extends BaseActivity implements OnClickListener,
        TribeChatListener {
    
    @ViewInject(R.id.iv_room_avatar)
    private AvatarView mIvRoomAvatar;
    @ViewInject(R.id.et_room_desc)
    private EditText mEtRoomDesc;
    @ViewInject(R.id.rl_invite_friend)
    private RelativeLayout mRlInviteFriend;
    @ViewInject(R.id.tv_tribe_name)
    private TextView mTvTribeName;
    @ViewInject(R.id.tv_room_name)
    private TextView mTvRoomName;

    private ArrayList<UserEntry> mAddUsers;
    private ArrayList<GroupMember> mTobeAddUsers;
    
    private UserInfoManager mUserInfoManager;
    private TribeChatManager mTribeChatManager;
    private TribeChatRoomInfo mChatRoomInfo;
    private TitleBar mTitleBar;
    private UserInfo mUser;
    
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_finish_create_room;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mTitleBar = getTitleBar();
        mTitleBar.setTitle(R.string.tribe_create_successful);
        mUserInfoManager = UserInfoManager.getInstance(this);
        mTribeChatManager = TribeChatManager.getInstance(this);
        mTribeChatManager.registerTribeChatListener(this);
        mAddUsers = new ArrayList<UserEntry>();
        mTobeAddUsers = new ArrayList<GroupMember>();
        mUser = AccountInfo.getInstance(this).getUserInfo();
        Intent intent = getIntent();
        mChatRoomInfo = intent.getParcelableExtra(ChooseRoomAvatarActivity.EXT_CHAT_ROOM_DATA);
        mTitleBar.showRightText(R.string.finish, this);
        Picasso.with(this).load(mChatRoomInfo.roomAvatar).
            placeholder(R.drawable.chat_pic_bg_wechat).into(mIvRoomAvatar);
        mRlInviteFriend.setOnClickListener(this);
        mTvRoomName.setText(mChatRoomInfo.roomName);
        mTvTribeName.setText(mChatRoomInfo.tribeName);
    }
//
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                if (!getUserIds(mAddUsers).equals("")) {
                    mTribeChatManager.AddFriendChatRoom(mUser.userId, mChatRoomInfo.tribeId,
                            String.valueOf(mChatRoomInfo.roomId), getUserIds(mAddUsers));
                } else {
                    mChatRoomInfo.roomDesc = mEtRoomDesc.getText().toString();
                    new changeChatRoomTask().executeLong();
                }
                break;
            case R.id.rl_invite_friend:
                Intent intent = new Intent();
                intent.setClass(FinishCreateRoomActivity.this, AddFriendToGroupActivity.class);
                intent.putExtra(GroupChatInfoActivity.ADD_FRIEND, true);
                startActivityForResult(intent, TribeGroupInfoActivity.ADD_FRIEND_TO_TRIBE_CHAT);
                break;
            default:
                break;
        }
    }

    private class changeChatRoomTask extends MsMhpTask {

        public changeChatRoomTask() {
            super(FinishCreateRoomActivity.this, MsRequest.TRIBE_CHANGE_ROOM_INFO,
                    getParams(mChatRoomInfo), getFiles(mChatRoomInfo));
        }
        
        @Override
        protected void onPreExecute() {
            Utils.showProgressDialog(FinishCreateRoomActivity.this, R.string.tribe_edit_chat_room_ing);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                TribeInfo mTribe = new TribeInfo();
                mTribe.tribeId = mChatRoomInfo.tribeId;
                mTribe.tribeName = mChatRoomInfo.tribeName;
                Intent intent = new Intent();
                intent.putExtra(TribeChatRoomActivity.EXT_TRIBE_DATA, mTribe);
                setResult(RESULT_UPDATED, intent);
                finish();
            } else {
                response.showInfo(FinishCreateRoomActivity.this, 
                        response.getFailureDesc(response.code));
            }
            Utils.hidePgressDialog();
        }

    }

    private HashMap<String, String> getParams(TribeChatRoomInfo chatRoomInfo) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("uid", String.valueOf(
                AccountInfo.getInstance(FinishCreateRoomActivity.this).getUserInfo().userId));
        params.put("tribe_id", String.valueOf(chatRoomInfo.tribeId));
        params.put("room_id", String.valueOf(chatRoomInfo.roomId));
        params.put("name", chatRoomInfo.roomName);
        params.put("description", chatRoomInfo.roomDesc);
        return params;
    }

    private HashMap<String, String> getFiles(TribeChatRoomInfo chatRoomInfo) {
        if (TextUtils.isEmpty(chatRoomInfo.roomAvatar)) {
            return null;
        }
        HashMap<String, String> files = new HashMap<String, String>();
        files.put("icon", chatRoomInfo.roomAvatar);
        return files;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TribeGroupInfoActivity.ADD_FRIEND_TO_TRIBE_CHAT 
                && resultCode == RESULT_OK) {
            ArrayList<UserEntry> users = 
                    data.getParcelableArrayListExtra(AddFriendToGroupActivity.EXTRA_RESULT);
            mAddUsers.clear();
            mAddUsers.addAll(users);
            mTobeAddUsers.clear();
        }
    }

    private String getUserIds(ArrayList<UserEntry> users) {
        if (users != null && users.size() > 0) {
            StringBuilder userIds = new StringBuilder();
            boolean firstTime = true;
            for (UserEntry ue : users) {
                UserInfo uinfo = mUserInfoManager.getUserInfo(ue.jid);
                mTobeAddUsers.add(new GroupMember(null, uinfo));
                if (firstTime) {
                    firstTime = false;
                } else {
                    userIds.append(Utils.COMMA_DELIMITER);
                }
                userIds.append(uinfo.userId);
            }
            return userIds.toString();
        }
        return "";
    }

    @Override
    public void onSuccess(int type, Object obj) {
        switch (type) {
            case TribeChatManager.TYPE_ADD_USERS:
                mChatRoomInfo.roomDesc = mEtRoomDesc.getText().toString();
                new changeChatRoomTask().executeLong();
                break;
            default:
                break;
        }
    }

    @Override
    public void onFail(int type) {
        switch (type) {
            case TribeChatManager.TYPE_ADD_USERS:
                toast(this.getString(R.string.tribe_upload_the_user_failed));
                break;
            default:
                break;
        }
    }

}
