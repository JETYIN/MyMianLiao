package com.tjut.mianliao.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.BrowserActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.contact.CheckableContactsActivity;
import com.tjut.mianliao.contact.ContactUpdateCenter;
import com.tjut.mianliao.contact.ContactUpdateCenter.UpdateType;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.GroupInfo;
import com.tjut.mianliao.data.GroupMember;
import com.tjut.mianliao.data.IMResource;
import com.tjut.mianliao.data.UserGroupMap;
import com.tjut.mianliao.data.contact.UserEntry;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.tribe.TribeChatRoomInfo;
import com.tjut.mianliao.im.AvatarMarketActivity;
import com.tjut.mianliao.im.GroupChatManager;
import com.tjut.mianliao.im.GroupChatManager.GroupChatListener;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.tribe.TribeChatManager;
import com.tjut.mianliao.tribe.TribeChatManager.TribeChatListener;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.StringUtils;
import com.tjut.mianliao.util.Utils;

public class GroupChatInfoActivity extends BaseActivity implements OnItemClickListener, OnClickListener,
        ContactUpdateCenter.ContactObserver, GroupChatListener, TribeChatListener {

    private static final int ADD_FRIENDS_QUEST = 100;
    public static final int EXT_UPDATE_GINFO = 1000;
    public static final String ADD_FRIEND = "add_friend";
    public static final String IS_TO_TOP = "is_to_top_";
    private String mGroupSizeURL = "/assets/pages/userGroup/max_member.html";

    private GridView mGrideView;
    private UserInfoManager mUserInfoManager;
    private UnreadMessageHelper mUnreadMessageHelper;
    private String mChatTarget;
    private boolean mIsOwner, mHide = true, isGroup;
    private ArrayList<GroupMember> mMembers = new ArrayList<GroupMember>();
    private GroupUserAdapter mGuAdapter;
    private GroupChatManager mGroupChatManager;
    private TribeChatManager mTribeChatManager;
    private GroupMember mMember;
    private String mGroupId, mTopTargetId;
    private long mChatId;
    private int mGroupAdminUid;
    private ArrayList<GroupMember> mTobeAddUsers = new ArrayList<GroupMember>();
    private LightDialog mDialog;
    private UserInfo mUserInfo;

    private TextView mTvGroupName, mTvNickName;
    private ImageView mIvToTop;
    private String mGroupName, mNickName;
    private ArrayList<String> mImageUrls;
    private SharedPreferences mPreferences;
    private Settings mSettings;
    private LinearLayout mMagicLinearLayout;
    private ArrayList<UserInfo> mExceptUsers;
    private int mTribeId;
    private int mUserId;
    private TribeChatRoomInfo mRoomInfo; 
    
    private boolean mIsTribeChat;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_group_chat_info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = Settings.getInstance(this);
        mExceptUsers = new ArrayList<>();
        getTitleBar().setTitle(getString(R.string.group_chat_info_title));
        mUserInfoManager = UserInfoManager.getInstance(this);
        mGroupChatManager = GroupChatManager.getInstance(this);
        mTribeChatManager = TribeChatManager.getInstance(this);
        mGroupChatManager.registerGroupChatListener(this);
        mTribeChatManager.registerTribeChatListener(this);
        mUnreadMessageHelper = UnreadMessageHelper.getInstance(this);
        mUserInfo = AccountInfo.getInstance(this).getUserInfo();
        mChatTarget = getIntent().getStringExtra(ChatActivity.EXTRA_GROUPCHAT_JID);
        mGroupId = getIntent().getStringExtra(ChatActivity.EXTRA_GROUPCHAT_ID);
        mRoomInfo = getIntent().getParcelableExtra(ChatActivity.EXTRA_TRIBE_ROOM_INFO);
        if (mRoomInfo != null) {
            mTribeId = mRoomInfo.tribeId;
            mChatId = mRoomInfo.chatId;
            if (mTribeId != 0) {
                new getGroupUsersTask().executeLong();
                mIsTribeChat = true;
                if (mIsTribeChat && mUserId == mRoomInfo.createrId) {
                    mIsOwner = true;
                }
            }
        }  
        
        
        isGroup = getIntent().getBooleanExtra(ChatActivity.EXTRA_CHAT_ISGOUPCHAT, false);
        mImageUrls = getIntent().getStringArrayListExtra(ChatActivity.EXTRA_CHAT_IMAGEURLS);
        mMembers.clear();

        mUserId = AccountInfo.getInstance(this).getUserId();
        
        mTvGroupName = (TextView) findViewById(R.id.tv_group_name);
        mTvNickName = (TextView) findViewById(R.id.tv_my_nickname);
        mIvToTop = (ImageView) findViewById(R.id.iv_gc_top);
        
        if (mTribeId != 0)
            setTribeChatData();

        if ( !mIsTribeChat && mChatTarget != null && isGroup && mGroupId != null) {
            mGroupChatManager.getGroupInfo(mGroupId);
            mTopTargetId = mGroupId;
            mChatId = Long.parseLong(StringUtils.parseName(mChatTarget));
        } else {
            UserInfo info = mUserInfoManager.getUserInfo(mChatTarget);
            mExceptUsers.add(info);
            mMembers.add(new GroupMember(null, info));
            mTopTargetId = StringUtils.parseName(mChatTarget);
        }
        mGrideView = (GridView) findViewById(R.id.gv_user_info);
        mGuAdapter = new GroupUserAdapter();
        mGrideView.setAdapter(mGuAdapter);
        mGrideView.setOnItemClickListener(this);
        updateView();
        mPreferences = DataHelper.getSpForData(this);
        boolean isTop = mPreferences.getBoolean(IS_TO_TOP + mTopTargetId, false);
        mIvToTop.setImageResource(isTop ? R.drawable.switch_on : R.drawable.switch_off);
        mMagicLinearLayout = (LinearLayout) findViewById(R.id.ly_bg_char_info);
    }
    
    private void setTribeChatData () {
        new getGroupUsersTask().executeLong();
        mIsTribeChat = true;
        mTvGroupName.setText(mRoomInfo.roomName);
        if (mIsTribeChat && mUserId == mRoomInfo.createrId) {
            mIsOwner = true;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(EXT_UPDATE_GINFO);
        super.onBackPressed();
    }

    private void getExceptUsers() {
        mExceptUsers.clear();
        for (GroupMember member : mMembers) {
            mExceptUsers.add(member.userInfo);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGroupChatManager.unregisterGroupChatListener(this);
        mTribeChatManager.unregisterTribeChatListener(this);
    }

    private void updateView() {
        // findViewById(R.id.ll_group_info).setVisibility(
        // mIsOwner && mChatTarget != null ? View.VISIBLE : View.GONE);
        findViewById(R.id.rl_group_name).setVisibility(mIsOwner && mChatTarget != null ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_delete).setVisibility(mGroupId != null ? View.VISIBLE : View.GONE);
        findViewById(R.id.rl_set_nickname).setVisibility(mGroupId != null ? View.VISIBLE : View.GONE);
        ((TextView) findViewById(R.id.tv_report)).setText(mGroupId != null ? getString(R.string.cht_report_group)
                : getString(R.string.report));
        
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position < mMembers.size()) {
            UserInfo userInfo = mMembers.get(position).userInfo;
            if (userInfo != null) {

            }
            int target = userInfo.userId;
            if (mHide) {
                viewProfile(userInfo);
            } else {
                if (mTribeId != 0) {
                    if (mUserId == mRoomInfo.createrId) {
                        mTribeChatManager.removeFriendChatRoom(mUserId, mRoomInfo.tribeId, mGroupId, target);
                    }
                } else {
                    mGroupChatManager.removeUser(mGroupId, String.valueOf(target));
                }
                mMember = mMembers.get(position);
                getTitleBar().showProgress();
            }
        } else if (position == mMembers.size()) {
            if (!mHide) {
                mHide = true;
            }
            Intent intent = new Intent(this, AddFriendToGroupActivity.class);
            if (!isGroup) {
                intent.putExtra(ChatActivity.EXTRA_CHAT_TARGET, mChatTarget);
                intent.putExtra(CheckableContactsActivity.EXTRA_EXCEPT_INFOS, mExceptUsers);
                startActivityForResult(intent, ADD_FRIENDS_QUEST);
            } else {
                intent.putExtra(ADD_FRIEND, true);
                getExceptUsers();
                intent.putExtra(CheckableContactsActivity.EXTRA_EXCEPT_INFOS, mExceptUsers);
                startActivityForResult(intent, ADD_FRIENDS_QUEST);
            }
        } else if (position == mMembers.size() + 1) {
            if (mHide && mIsOwner) {
                mHide = false;
            } else {
                mHide = true;
            }
        } else if (position > mMembers.size() + 1) {
            mHide = true;
        }
        mGuAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ADD_FRIENDS_QUEST) {
            ArrayList<UserEntry> users = data.getParcelableArrayListExtra(AddFriendToGroupActivity.EXTRA_RESULT);
            mTobeAddUsers.clear();
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
                if (mTribeId != 0) {
                    mTribeChatManager.AddFriendChatRoom(
                            mUserId, mTribeId, mGroupId, userIds.toString());
                } else {
                    mGroupChatManager.addUsers(mGroupId, userIds.toString());
                }
                getTitleBar().showProgress();
            }
            
        }
        if (resultCode == RESULT_UPDATED && requestCode == ADD_FRIENDS_QUEST) {
            setResult(RESULT_UPDATED, data);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_gc_top:
                placeToTop();
                break;
            // case R.id.rl_group_size:
            // viewGroupSize();
            // break;
            case R.id.btn_delete:
                showQuitDialog();
                break;
            case R.id.rl_group_name:
                if (mIsTribeChat) {
                    if (mIsOwner) {
                        showUpdateGroupNameDialog();
                    }
                } else {
                    showUpdateGroupNameDialog();
                }
                break;
            case R.id.rl_group_chat_picture:
                viewPicture();
                break;
            case R.id.rl_set_nickname:
                if (mIsTribeChat) {
                    toast(R.string.tribe_not_import);
                } else {
                    showUpdateNickNameDialog();
                }
                break;
            case R.id.rl_group_delete_record:
                showDelChatRecordDialog();
                break;
            case R.id.rl_group_report:
                viewReport();
                break;
            case R.id.rl_set_chatbackground:
                viewMarket(IMResource.TYPE_BACKGROUND);
                break;
            default:
                break;
        }
    }

    private void placeToTop() {
        boolean isTop = mPreferences.getBoolean(IS_TO_TOP + mTopTargetId, false);
        mIvToTop.setImageResource(isTop ? R.drawable.switch_off : R.drawable.switch_on);
        isTop = !isTop;
        saveDateToSp(IS_TO_TOP + mTopTargetId, isTop);
    }

    private void saveDateToSp(String key, boolean value) {
        Editor editor = mPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private void viewReport() {
        Intent intent = new Intent(this, GroupReportActivity.class);
        intent.putExtra(GroupReportActivity.IS_GROUP_CHAT, isGroup);
        intent.putExtra(GroupReportActivity.OBJ_ID, isGroup ? mGroupId
                : mUserInfoManager.getUserInfo(mChatTarget).userId);
        startActivity(intent);
    }

    private void viewGroupSize() {
        Intent gsIntent = new Intent(this, BrowserActivity.class);
        String url = new StringBuilder(Utils.getServerAddress()).append(mGroupSizeURL).toString();
        gsIntent.putExtra(BrowserActivity.URL, url);
        gsIntent.putExtra(BrowserActivity.TITLE, getString(R.string.cht_group_size));
        startActivity(gsIntent);
    }

    private void viewMarket(int type) {
        String params = new StringBuilder("type=").append(type).toString();
        String url = HttpUtil.getUrl(this, MsRequest.IMRW_RESOURCE_LIST, params);
        Intent iMarket = new Intent(this, AvatarMarketActivity.class);
        iMarket.putExtra(AvatarMarketActivity.URL, url);
        if (type == IMResource.TYPE_CHARACTER_ACTION) {
            iMarket.putExtra(AvatarMarketActivity.EXTRA_SHOW_AVATAR, true);
        }
        startActivity(iMarket);
    }

    private void viewPicture() {
        if (mImageUrls.size() > 0) {
            Utils.viewImages(this, mImageUrls, 0);
        } else {
            toast(R.string.cht_no_picture);
        }
    }

    private void viewProfile(UserInfo userInfo) {
        if (userInfo == null) {
            toast(R.string.prof_user_not_exist);
            return;
        }
        Intent iProfile = new Intent(this, NewProfileActivity.class);
        iProfile.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
        iProfile.putExtra(NewProfileActivity.EXTRA_SHOW_CHAT_BUTTON, false);
        startActivity(iProfile);
    }

    @Override
    public void onContactsUpdated(final UpdateType type, Object data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (type == ContactUpdateCenter.UpdateType.UserEntry) {
                    mGuAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private class GroupUserAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (!isGroup) {
                return 2;
            }
            int count = mIsOwner ? mMembers.size() + 2 : mMembers.size() + 1;
            return (int) (Math.ceil(count / 4.0)) * 4;
        }

        @Override
        public GroupMember getItem(int position) {
            return position < mMembers.size() ? mMembers.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView != null) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = mInflater.inflate(R.layout.item_group_people_info, parent, false);
                holder = new ViewHolder();
                holder.photo = (ProImageView) convertView.findViewById(R.id.iv_avatar);
                holder.ivDel = (ImageView) convertView.findViewById(R.id.iv_delete);
                holder.name = (TextView) convertView.findViewById(R.id.tv_user_name);
                convertView.setTag(holder);
            }
            GroupMember member = getItem(position);
            holder.name.setText(null);
            if (position < mMembers.size()) {
                if (member != null && member.userInfo != null) {
                    holder.photo.setBackgroundResource(0);
//                    Picasso.with(GroupChatInfoActivity.this)
//                            .load(member.userInfo.getAvatar())
//                            .placeholder(member.userInfo.defaultAvatar())
//                            .into(holder.photo);
                    holder.photo.setImage(member.userInfo.getAvatar(), member.userInfo.defaultAvatar());
                    holder.photo.setScaleType(ScaleType.CENTER_CROP);
                    boolean noNick = member.nickName == null || "".equals(member.nickName);
                    holder.name.setText(noNick ? member.userInfo.getDisplayName(GroupChatInfoActivity.this) : member.nickName);
                }
                holder.photo.setVisibility(View.VISIBLE);
                holder.ivDel.setVisibility(mHide ? View.INVISIBLE : View.VISIBLE);
                if (position == 0) {
                    holder.ivDel.setVisibility(View.INVISIBLE);
                }
            } else if (position == mMembers.size()) {
                // photo.setBackgroundResource(R.drawable.selector_group_operator);
                holder.photo.setImage(null, R.drawable.botton_bg_chat_add);
                holder.photo.setScaleType(ScaleType.CENTER);
                holder.ivDel.setVisibility(View.INVISIBLE);
                holder.photo.setVisibility(mHide ? View.VISIBLE : View.INVISIBLE);
            } else if (position >= mMembers.size() + 1) {
                // photo.setBackgroundResource(R.drawable.selector_group_operator);
                holder.photo.setImage(null, R.drawable.botton_bg_chat_delete);
                holder.photo.setScaleType(ScaleType.CENTER);
                holder.ivDel.setVisibility(View.INVISIBLE);
                boolean show = position == mMembers.size() + 1;
                if (show) {
                    holder.photo.setVisibility(mHide && mIsOwner && mMembers.size() > 1 ? View.VISIBLE : View.INVISIBLE);
                } else {
                    holder.photo.setVisibility(View.INVISIBLE);
                }
            }

            return convertView;
        }

    }

    private void showQuitDialog() {
        new LightDialog(this).setTitleLd(R.string.course_time_clear).setMessage(R.string.cht_group_quit_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mIsTribeChat && mTribeId != 0) {
                            if (mUserId != mRoomInfo.createrId) {
                                mTribeChatManager.exitChatRoom(mUserId, mTribeId, getRoomId());
                            } else {
                                mTribeChatManager.deleteChatRoom(mUserId, mTribeId, getRoomId());
                            }
                        } else {
                            mGroupChatManager.quit(mGroupId);
                            getTitleBar().showProgress();
                        }
                    }
                }).show();
    }
    
    private String getRoomId() {
        int index = mGroupId.indexOf("tribe");
        String roomId = mGroupId;
        if (index != -1) {
            roomId = mGroupId.substring(0, index);
        }
        return roomId;
    }

    private void showDelChatRecordDialog() {
        new LightDialog(this).setTitleLd(R.string.course_time_clear).setMessage(R.string.cht_group_del_records_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isGroup) {
                            mUnreadMessageHelper.deleteChat(mGroupId + "@groupchat." + Utils.getChatServerDomain());
                        } else {
                            mUnreadMessageHelper.deleteChat(mChatTarget);
                        }
                        toast(R.string.group_chat_delete_chatrecord_success);
                    }
                }).show();
    }

    private void showUpdateGroupNameDialog() {
        final View view = mInflater.inflate(R.layout.edittext_view, null);
        final EditText text = (EditText) view.findViewById(R.id.et_content);
        text.setHint(R.string.please_input_gorup_name);
        mDialog = new LightDialog(this);
        mDialog.setView(view).setTitle(R.string.update_gorup_name);
        mDialog.setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mGroupName = text.getText().toString().trim();
                        if (mIsTribeChat) {
                            new changeChatRoomTask(mGroupName).executeLong();
                        } else {
                            mGroupChatManager.updateGroupName(mGroupId, mGroupName);
                        }
                        getTitleBar().showProgress();
                        mDialog.hide();
                    }
                }).show();
    }
    
    private class changeChatRoomTask extends MsMhpTask {
        
        String mRoomName;
        
        public changeChatRoomTask(String roomName) {
            super(GroupChatInfoActivity.this, MsRequest.TRIBE_CHANGE_ROOM_INFO,
                    getParams(roomName), null);
            mRoomName = roomName;
        }
        

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (response.isSuccessful()) {
                mTvGroupName.setText(mRoomName);
            } else {
                response.showInfo(GroupChatInfoActivity.this, 
                        response.getFailureDesc(response.code));
            }
            
        }

    }
    private HashMap<String, String> getParams(String roomName) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("uid", String.valueOf(
                AccountInfo.getInstance(GroupChatInfoActivity.this).getUserInfo().userId));
        params.put("tribe_id", String.valueOf(mTribeId));
        params.put("room_id", String.valueOf(mRoomInfo.roomId));
        params.put("name", roomName);
        return params;
    }

    private void showUpdateNickNameDialog() {
        final View view = mInflater.inflate(R.layout.edittext_view, null);
        final EditText text = (EditText) view.findViewById(R.id.et_content);
        text.setHint(R.string.please_input_nickname);
        mDialog = new LightDialog(this);
        mDialog.setView(view).setTitle(R.string.update_my_nickname);
        mDialog.setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mNickName = text.getText().toString().trim();
                        mGroupChatManager.updateNickName(mGroupId, mNickName);
                        getTitleBar().showProgress();
                        mDialog.hide();
                    }
                }).show();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onGroupChatSuccess(int type, Object obj) {
        switch (type) {
            case GroupChatManager.REMOVE_USER:
                mMembers.remove(mMember);
                mGuAdapter.notifyDataSetChanged();
                getTitleBar().hideProgress();
                break;

            case GroupChatManager.ADD_USER:
                mGuAdapter.notifyDataSetChanged();
                mMembers.addAll(mTobeAddUsers);
                mTobeAddUsers.clear();
                getTitleBar().hideProgress();
                break;

            case GroupChatManager.GET_GROUP_INFO:
                UserGroupMap ucp = (UserGroupMap) obj;
                mNickName = (!"".equals(ucp.myNickName) && ucp.myNickName != null) ? ucp.myNickName : mUserInfo
                        .getDisplayName(this);
                mGroupAdminUid = ucp.adminUid;
                mIsOwner = AccountInfo.getInstance(this).getUserId() == mGroupAdminUid;
                updateView();
                mTvGroupName.setText(ucp.name);
                mTvNickName.setText(mNickName);
                mGroupChatManager.getGroupMemers(mGroupId, 0);
                break;

            case GroupChatManager.GET_GROUP_MEMBERS:
                mMembers.clear();
                ArrayList<GroupMember> members = (ArrayList<GroupMember>) obj;
                mMembers.addAll(members);
                Collections.sort(mMembers, new Comparator<GroupMember>() {
                    @Override
                    public int compare(GroupMember lhs, GroupMember rhs) {
                        if (lhs.userInfo.userId == mGroupAdminUid) {
                            return -1;
                        } else if (rhs.userInfo.userId == mGroupAdminUid) {
                            return 1;
                        }
                        return 0;
                    }
                });
                mGuAdapter.notifyDataSetChanged();
                getTitleBar().hideProgress();
                break;

            case GroupChatManager.QUIT:
                mUnreadMessageHelper.deleteChat(mChatId + "@groupchat." + Utils.getChatServerDomain());
                getTitleBar().hideProgress();
                setResult(RESULT_DELETED);
                this.finish();
                break;

            case GroupChatManager.EDIT_GROUP:
                getTitleBar().hideProgress();
                mTvGroupName.setText(mGroupName);
                DataHelper.updateGroupInfo(this, new GroupInfo(mGroupId, mGroupName, mChatId));
                break;

            case GroupChatManager.EDIT_MEMBER:
                getTitleBar().hideProgress();
                mTvNickName.setText(mNickName);
                mGroupChatManager.getGroupMemers(mGroupId, 0);
                break;
            default:
                break;
        }
    }

    @Override
    public void onGroupChatFailed(int type) {
        getTitleBar().hideProgress();
        switch (type) {
            case GroupChatManager.GET_GROUP_INFO:
                toast(R.string.get_members_info_failed);
                break;
            default:
                break;
        }
    }
    
    private class ViewHolder {
        ProImageView photo;
        ImageView ivDel;
        TextView name;
    }

    @Override
    public void onSuccess(int type, Object obj) {
        switch (type) {
            case TribeChatManager.TYPE_DEL_ROOM:
                mUnreadMessageHelper.deleteChat(mChatId + "@groupchat." + Utils.getChatServerDomain());
                setResult(RESULT_DELETED);
                finish();
                break;
            case TribeChatManager.TYPE_EXIT_ROOM:
                mUnreadMessageHelper.deleteChat(getRoomId() + "@groupchat." + Utils.getChatServerDomain());
                setResult(RESULT_DELETED);
                finish();
                break;
            case TribeChatManager.TYPE_ADD_USERS:
                mMembers.addAll(mTobeAddUsers);
                mGuAdapter.notifyDataSetChanged();
                mTobeAddUsers.clear();
                getTitleBar().hideProgress();
            case TribeChatManager.TYPE_REMOVE_USER:
                mMembers.remove(mMember);
                mGuAdapter.notifyDataSetChanged(); 
                break;
            default:
                break;
        }
    }

    @Override
    public void onFail(int type) {
        
    }
    
    private class getGroupUsersTask extends MsTask {

        public getGroupUsersTask() {
            super(GroupChatInfoActivity.this,MsRequest.TRIBE_GET_CHAT_ROOM_USERS);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("uid=").append(mUserId)
                    .append("&tribe_id=").append(mRoomInfo.tribeId)
                    .append("&room_id=").append(mRoomInfo.roomId)
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                getTitleBar().hideProgress();
                mMembers.clear();
                ArrayList<UserInfo> users = JsonUtil.getArray(
                        response.getJsonArray(), UserInfo.TRANSFORMER);
                for (UserInfo user : users) {
                    GroupMember member = new GroupMember();
                    member.userInfo = user;
                    member.nickName = user.nickname;
                    mMembers.add(member);
                }
                mGuAdapter.notifyDataSetChanged();
            }
        }
    }
    
}
