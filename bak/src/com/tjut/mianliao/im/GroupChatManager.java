package com.tjut.mianliao.im;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.tjut.mianliao.data.GroupMember;
import com.tjut.mianliao.data.UserGroupMap;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class GroupChatManager {

    public static final int GET_GROUP_LIST = 1;
    public static final int CREATE_GROUP = 2;
    public static final int DELETE_GROUP = 3;
    public static final int REMOVE_USER = 4;
    public static final int ADD_USER = 5;
    public static final int GET_GROUP_INFO = 6;
    public static final int GET_GROUP_MEMBERS = 7;
    public static final int QUIT = 8;
    public static final int EDIT_GROUP = 9;
    public static final int EDIT_MEMBER = 10;
    public static final int START_TOPIC = 11;

    private static WeakReference<GroupChatManager> sInstanceRef;
    private List<GroupChatListener> mGroupChatListeners;
    private Context mContext;

    public static synchronized GroupChatManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        GroupChatManager instance = new GroupChatManager(context);
        sInstanceRef = new WeakReference<GroupChatManager>(instance);
        return instance;
    }

    private GroupChatManager(Context context) {
        mContext = context.getApplicationContext();
        mGroupChatListeners = new CopyOnWriteArrayList<GroupChatListener>();
    }

    public void registerGroupChatListener(GroupChatListener listener) {
        if (listener != null && !mGroupChatListeners.contains(listener)) {
            mGroupChatListeners.add(listener);
        }
    }

    public void unregisterGroupChatListener(GroupChatListener listener) {
        mGroupChatListeners.remove(listener);
    }

    public void createGroupChat(String groupName, String uIds) {
        new CreateGroupTask(groupName, uIds).executeLong();
    }

    public void getMyGroupList() {
        new GetMyGroupsTask().executeLong();
    }

    public void getGroupInfo(String groupId) {
        new GetGroupInfoTask(groupId).executeLong();
    }

    public void getGroupMemers(String groupId, int exceptAdmin) {
        new GetGroupMembersTask(groupId, exceptAdmin).executeLong();
    }

    public void deleteGroup(String groupId) {
        new DeleteGroupTask(groupId).executeLong();
    }

    public void removeUser(String groupId, String uid) {
        new RemoveUserTask(groupId, uid).executeLong();
    }

    public void addUsers(String groupId, String userIds) {
        new AddUserTask(groupId, userIds).executeLong();
    }

    public void quit(String groupId) {
        new QuitTask(groupId).executeLong();
    }

    public void updateGroupName(String groupId, String groupName) {
        new EditGroupTask(groupId, groupName).executeLong();
    }

    public void updateNickName(String groupId, String nickName) {
        new EditMemberTask(groupId, nickName).executeLong();
    }

    public void enterTopic(int themeId) {
        new EnterTopicTask(themeId).executeLong();
    }

    class CreateGroupTask extends MsTask {

        private String name, ids;

        public CreateGroupTask(String name, String ids) {
            super(mContext, MsRequest.CREATE_GROUP);
            this.name = name;
            this.ids = ids;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("name=").append(Utils.urlEncode(name)).append("&user_ids=").append(ids).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            if (response.isSuccessful()) {
                JSONObject json = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);
                UserGroupMap ucp = UserGroupMap.fromJson(json);
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatSuccess(CREATE_GROUP, ucp);
                }
            } else {
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatFailed(CREATE_GROUP);
                }
            }
        }
    }

    class GetMyGroupsTask extends MsTask {

        public GetMyGroupsTask() {
            super(mContext, MsRequest.LIST_MY_GROUP);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                ArrayList<UserGroupMap> ucps = JsonUtil.getArray(ja, UserGroupMap.TRANSFORMER);
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatSuccess(GET_GROUP_LIST, ucps);
                }
            } else {
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatFailed(GET_GROUP_LIST);
                }
            }
        }
    }

    class GetGroupInfoTask extends MsTask {

        private String groupId;

        public GetGroupInfoTask(String groupId) {
            super(mContext, MsRequest.FIND_GROUP_BY_ID);
            this.groupId = groupId;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("id=").append(groupId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            if (response.isSuccessful()) {
                UserGroupMap ucp = UserGroupMap.fromJson(response.getJsonObject());
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatSuccess(GET_GROUP_INFO, ucp);
                }
            } else {
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatFailed(GET_GROUP_INFO);
                }
            }
        }
    }

    class GetGroupMembersTask extends MsTask {

        private String groupId;
        private int exceptAdmin;

        public GetGroupMembersTask(String groupId, int exceptAdmin) {
            super(mContext, MsRequest.GROUP_LIST_MEMBER);
            this.groupId = groupId;
            this.exceptAdmin = exceptAdmin;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("group_id=").append(groupId).append("&except_admin=")
                    .append(String.valueOf(exceptAdmin)).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                ArrayList<GroupMember> members = JsonUtil.getArray(ja, GroupMember.TRANSFORMER);
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatSuccess(GET_GROUP_MEMBERS, members);
                }
            } else {
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatFailed(GET_GROUP_MEMBERS);
                }
            }
        }
    }

    class DeleteGroupTask extends MsTask {

        private String jid;

        public DeleteGroupTask(String jid) {
            super(mContext, MsRequest.DELETE_GROUP);
            this.jid = jid;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("id=").append(jid).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            if (response.isSuccessful()) {
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatSuccess(DELETE_GROUP, null);
                }
            } else {
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatFailed(DELETE_GROUP);
                }
            }
        }
    }

    class RemoveUserTask extends MsTask {

        private String groupId, userId;

        public RemoveUserTask(String groupId, String userId) {
            super(mContext, MsRequest.REMOVE_USER);
            this.groupId = groupId;
            this.userId = userId;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("group_id=").append(groupId).append("&user_id=").append(userId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            if (response.isSuccessful()) {
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatSuccess(REMOVE_USER, null);
                }
            } else {
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatFailed(REMOVE_USER);
                }
            }
        }
    }

    class AddUserTask extends MsTask {

        private String groupId, userId;

        public AddUserTask(String groupId, String userId) {
            super(mContext, MsRequest.ADD_USER);
            this.groupId = groupId;
            this.userId = userId;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("group_id=").append(groupId).append("&user_ids=").append(userId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            if (response.isSuccessful()) {
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatSuccess(ADD_USER, null);
                }
            } else {
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatFailed(ADD_USER);
                }
            }
        }
    }

    class QuitTask extends MsTask {

        private String groupId;

        public QuitTask(String groupId) {
            super(mContext, MsRequest.QUIT);
            this.groupId = groupId;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("group_id=").append(groupId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatSuccess(QUIT, null);
                }
            } else {
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatFailed(QUIT);
                }
            }
        }
    }

    class EditGroupTask extends MsTask {

        private String groupId, groupName;

        public EditGroupTask(String groupId, String groupName) {
            super(mContext, MsRequest.EDIT_GROUP);
            this.groupId = groupId;
            this.groupName = groupName;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("group_id=").append(groupId).append("&name=").append(Utils.urlEncode(groupName))
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            if (response.isSuccessful()) {
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatSuccess(EDIT_GROUP, null);
                }
            } else {
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatFailed(EDIT_GROUP);
                }
            }
        }
    }

    class EditMemberTask extends MsTask {

        private String groupId, nickName;

        public EditMemberTask(String groupId, String nickName) {
            super(mContext, MsRequest.EDIT_MEMBER);
            this.groupId = groupId;
            this.nickName = nickName;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("group_id=").append(groupId).append("&nick=").append(Utils.urlEncode(nickName))
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            if (response.isSuccessful()) {
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatSuccess(EDIT_MEMBER, null);
                }
            } else {
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatFailed(EDIT_MEMBER);
                }
            }
        }
    }

    class EnterTopicTask extends MsTask {

        private int themeId;

        public EnterTopicTask(int themeId) {
            super(mContext, MsRequest.ENTER_TOPIC);
            this.themeId = themeId;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("theme_id=").append(themeId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            if (response.isSuccessful()) {
                UserGroupMap ucp = UserGroupMap.fromJson(response.getJsonObject());
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatSuccess(START_TOPIC, ucp);
                }
            } else {
                for (GroupChatListener listener : mGroupChatListeners) {
                    listener.onGroupChatFailed(START_TOPIC);
                }
            }
        }
    }

    public interface GroupChatListener {
        void onGroupChatSuccess(int type, Object obj);

        void onGroupChatFailed(int type);
    }
}
