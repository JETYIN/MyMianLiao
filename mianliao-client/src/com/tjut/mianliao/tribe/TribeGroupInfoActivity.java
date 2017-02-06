package com.tjut.mianliao.tribe;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.chat.AddFriendToGroupActivity;
import com.tjut.mianliao.chat.ChatActivity;
import com.tjut.mianliao.chat.GroupChatInfoActivity;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.GroupMember;
import com.tjut.mianliao.data.contact.UserEntry;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.tribe.TribeChatRoomInfo;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.tribe.TribeChatManager.TribeChatListener;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class TribeGroupInfoActivity extends BaseActivity implements
		OnClickListener, TribeChatListener {

	public static final int ADD_FRIEND_TO_TRIBE_CHAT = 301;
	public static final int FOR_CHAT = 335;

	@ViewInject(R.id.gv_group_member)
	private GridView mGvGroupMember;
	@ViewInject(R.id.iv_master_avatar)
	private AvatarView mIVMasterAvatar;
	@ViewInject(R.id.tv_master_name)
	private TextView mTvMasterName;
	@ViewInject(R.id.tv_tribe_name)
	private TextView mTvTribeName;
	@ViewInject(R.id.tv_room_desc)
	private TextView mTvRoomDesc;
	@ViewInject(R.id.iv_avatar)
	private AvatarView mIvAvatar;

	private UserInfoManager mUserInfoManager;
	private TribeChatManager mTribeChatManager;
	private GroupMenberAdapter mAdapter;
	private TribeChatRoomInfo mRoomInfo;
	private ArrayList<UserInfo> mGroupUsers;
	private ArrayList<UserEntry> mAddUsers;
	private ArrayList<GroupMember> mTobeAddUsers;
	private UserInfoManager uim;

	private int mUserId;

	@Override
	protected int getLayoutResID() {
		return R.layout.activity_tribe_group_info;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		mRoomInfo = getIntent().getParcelableExtra(
				TribeChatRoomInfo.INTENT_EXTRA_INFO);
		mUserId = AccountInfo.getInstance(this).getUserId();
		mTribeChatManager = TribeChatManager.getInstance(this);
		mTribeChatManager.registerTribeChatListener(this);
		mUserInfoManager = UserInfoManager.getInstance(this);
		uim = UserInfoManager.getInstance(this);
		mAddUsers = new ArrayList<UserEntry>();
		mTobeAddUsers = new ArrayList<GroupMember>();
		getTitleBar().setTitle(mRoomInfo.roomName);
		mTvTribeName.setText(mRoomInfo.tribeName);
		mTvMasterName.setText(mRoomInfo.ownerNick);
		mTvRoomDesc.setText(mRoomInfo.roomDesc);
		if (mRoomInfo.roomAvatar != null && !mRoomInfo.roomAvatar.equals("")) {
			Picasso.with(this).load(mRoomInfo.roomAvatar)
					.placeholder(R.drawable.chat_botton_bg_faviconboy)
					.into(mIvAvatar);
		}
		mGroupUsers = new ArrayList<UserInfo>();
		mAdapter = new GroupMenberAdapter();
		mGvGroupMember.setAdapter(mAdapter);
		new getGroupUsersTask().executeLong();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mTribeChatManager.unregisterTribeChatListener(this);
	}

	private class GroupMenberAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mGroupUsers.size() > 5 ? 5 : mGroupUsers.size();
		}

		@Override
		public UserInfo getItem(int position) {
			return mGroupUsers.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = mInflater.inflate(R.layout.list_item_group_member,
						parent, false);
			} else {
				view = convertView;
			}
			UserInfo user = getItem(position);
			AvatarView mUserAvatar = (AvatarView) view
					.findViewById(R.id.iv_member_avatar);
			Picasso.with(TribeGroupInfoActivity.this).load(user.getAvatar())
					.placeholder(R.drawable.chat_botton_bg_faviconboy)
					.into(mUserAvatar);
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.setClass(TribeGroupInfoActivity.this,
							TribeChatRoomUsersActivity.class);
					intent.putExtra(TribeChatRoomInfo.INTENT_EXTRA_INFO,
							mRoomInfo);
					startActivity(intent);
				}
			});
			return view;
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.tv_go_chat:
			getTitleBar().showProgress();
			mTribeChatManager.accessChatRoom(mUserId, mRoomInfo.tribeId,
					String.valueOf(mRoomInfo.roomId));
			break;
		case R.id.rl_title_group_member:
		case R.id.gv_group_member:
		case R.id.iv_to_member_list:
			intent.setClass(TribeGroupInfoActivity.this,
					TribeChatRoomUsersActivity.class);
			intent.putExtra(TribeChatRoomInfo.INTENT_EXTRA_INFO, mRoomInfo);
			startActivity(intent);
			break;
		case R.id.rl_invite_friend:
			intent.setClass(TribeGroupInfoActivity.this,
					AddFriendToGroupActivity.class);
			intent.putExtra(GroupChatInfoActivity.ADD_FRIEND, true);
			startActivityForResult(intent, ADD_FRIEND_TO_TRIBE_CHAT);
			break;
		case R.id.rl_master:
			intent.setClass(TribeGroupInfoActivity.this, NewProfileActivity.class);
			UserInfo user = new UserInfo();
			user.userId = mRoomInfo.createrId;
			intent.putExtra(UserInfo.INTENT_EXTRA_INFO, user);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	@Override
	public void onSuccess(int type, Object obj) {
		switch (type) {
		case TribeChatManager.TYPE_ADD_ROOM:
			getTitleBar().hideProgress();
			Intent intent = new Intent(this, ChatActivity.class);
			intent.putExtra(ChatActivity.EXTRA_CHAT_TARGET,
					createTribeChatTarget(mRoomInfo.roomId));
			intent.putExtra(ChatActivity.EXTRA_TRIBE_ROOM_INFO, mRoomInfo);
			intent.putExtra(ChatActivity.EXTRA_CHAT_ISGOUPCHAT, true);
			startActivityForResult(intent, FOR_CHAT);
			break;
		case TribeChatManager.TYPE_ADD_USERS:
			getTitleBar().hideProgress();
			toast(R.string.tribe_add_success);
			break;
		default:
			break;
		}
	}

	private class getGroupUsersTask extends MsTask {

		public getGroupUsersTask() {
			super(TribeGroupInfoActivity.this,
					MsRequest.TRIBE_GET_CHAT_ROOM_USERS);
		}

		@Override
		protected String buildParams() {
			return new StringBuilder("uid=").append(mUserId)
					.append("&tribe_id=").append(mRoomInfo.tribeId)
					.append("&room_id=").append(mRoomInfo.roomId).toString();
		}

		@Override
		protected void onPostExecute(MsResponse response) {
			if (response.isSuccessful()) {
				mGroupUsers.clear();
				ArrayList<UserInfo> users = JsonUtil.getArray(
						response.getJsonArray(), UserInfo.TRANSFORMER);
				if (users.size() <= 5) {
					mGroupUsers.addAll(users);
				} else {
					for (int i = 0; i < 5; i++) {
						mGroupUsers.add(users.get(i));
					}
				}
				mAdapter.notifyDataSetChanged();
			} else {
				response.showInfo(TribeGroupInfoActivity.this,
						response.getFailureDesc(response.code));
			}
		}
	}

	private String createTribeChatTarget(long roomId) {
		return roomId + "@groupchat." + Utils.getChatServerDomain();
	}

	@Override
	public void onFail(int type) {

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ADD_FRIEND_TO_TRIBE_CHAT && resultCode == RESULT_OK) {
			ArrayList<UserEntry> users = data
					.getParcelableArrayListExtra(AddFriendToGroupActivity.EXTRA_RESULT);
			// ArrayList<UserInfo>
			mAddUsers.clear();
			mAddUsers.addAll(users);
			if ((users.size() + mGroupUsers.size()) <= 100) {
				for (UserEntry user : users) {
					mGroupUsers.add(uim.getUserInfo(user.jid));
				}
				mAdapter.notifyDataSetChanged();
			} else {
				toast(R.string.tribe_people_too_much);
			}
			String userIds = getUserIds(mAddUsers);
			if (!userIds.equals("")) {
				getTitleBar().showProgress();
				mTribeChatManager.AddFriendChatRoom(mUserId, mRoomInfo.tribeId,
						String.valueOf(mRoomInfo.roomId), userIds.toString());
			}
			mTobeAddUsers.clear();
		} else if (requestCode == FOR_CHAT
				&& resultCode == ChatActivity.RESULT_DELETED) {
			System.out.println("------------- For Chat --> RESULT_DELETED");
			finish();
		} else if (requestCode == FOR_CHAT
				&& resultCode == ChatActivity.RESULT_BACK) {
			System.out.println("------------- For Chat --> RESULT_BACK");
			finish();
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

}
