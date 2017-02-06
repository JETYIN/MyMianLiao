package com.tjut.mianliao.forum;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.Utils;

public abstract class CfRecord implements Parcelable {

    public int postId;
    public String content;
    public long createdOn;
    public long updatedOn;
    public int replyCount;
    public int upCount;
    public int downCount;
    public boolean myUp;
    public boolean myDown;
    public boolean canDelete;
    public boolean isAdmin;
    public UserInfo userInfo;
    public ArrayList<UserInfo> likedUsers;
    public String relation;
    public ArrayList<AtUser> atUsers; 

    public boolean liking;
    public boolean replying;
    public boolean hating;

    public ArrayList<Image> avatars = new ArrayList<>();

    public CfRecord() {}

    protected static void fillFromJson(CfRecord record, JSONObject json) {
        record.postId = json.optInt("thread_id");
        record.content = json.optString("content");
        record.createdOn = json.optLong("created_on") * 1000;
        record.updatedOn = json.optLong("updated_on") * 1000;
        record.replyCount = json.optInt("reply_count");
        record.upCount = json.optInt("up_count");
        record.downCount = json.optInt("down_count");
        record.myUp = json.optBoolean("my_up");
        record.myDown = json.optBoolean("my_down");            
        record.canDelete = json.optBoolean("can_delete");
        record.isAdmin = json.optBoolean("is_admin");
        record.userInfo = UserInfo.fromJson(json);
        record.likedUsers = JsonUtil.getArray(
                json.optJSONArray("thumb_users"), UserInfo.TRANSFORMER);
        record.relation = record.userInfo.getRelationship();
        try {
            record.atUsers = JsonUtil.getArray(new JSONArray(json.optString("at_users")), AtUser.TRANSFORMER);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getTimeAndRelation() {
        StringBuilder sb = new StringBuilder(Utils.getPostShowTimeString(createdOn));
        String relation = userInfo.getRelationship();
        if (!TextUtils.isEmpty(relation)) {
            sb.append(" · ").append(relation);
        }
        return sb.toString();
    }

    public String getTimeAndSchool(CfPost post) {
        StringBuilder sb = new StringBuilder(Utils.getPostShowTimeString(createdOn));
        boolean sameSchool = post.sameSchool;
        if (!sameSchool) {
            sb.append(" · ").append("外校汪");
        }
        return sb.toString();
    }

    public String getTimeAndSchool(CfReply post) {
        StringBuilder sb = new StringBuilder(Utils.getPostShowTimeString(createdOn));
        boolean sameSchool = post.sameSchool;
        if (!sameSchool) {
            sb.append(" · ").append("外校汪");
        }
        return sb.toString();
    }

    public String getDistanceAndRelation() {
        StringBuilder sb = new StringBuilder(Utils.getDistanceDes(userInfo.distance));
        String relation = userInfo.getRelationship();
        if (!TextUtils.isEmpty(relation)) {
            sb.append(" · ").append(relation);
        }
        return sb.toString();
    }

    public String getLikedUsers(Context context) {
        int size = getLikedUsersCount();
        if (size > 0) {
            StringBuilder sb = new StringBuilder();
            boolean firstTime = true;
            for (UserInfo userInfo : likedUsers) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    sb.append(Utils.COMMA_DELIMITER).append(" ");
                }
                Image image = new Image(null, userInfo.getAvatar());
                if (avatars == null) {
                    avatars = new ArrayList<Image>();
                }
                avatars.add(image);
                sb.append(userInfo.getDisplayName(context));
            }
            if (upCount > size) {
                return context.getString(R.string.cf_liked_users_more, sb, upCount);
            } else {
                return context.getString(R.string.cf_liked_users, sb);
            }
        } else {
            return null;
        }
    }
    
    public void addLikedUser(UserInfo userInfo) {
        if (userInfo == null) {
            return;
        }
        if (likedUsers == null) {
            likedUsers = new ArrayList<>();
        }
        likedUsers.add(userInfo);
    }
    
    public void removeLikedUser(UserInfo userInfo) {
        if (userInfo == null || likedUsers == null) {
            return;
        }
        UserInfo ui = null;
        for (UserInfo info : likedUsers) {
            if (info.account.equals(userInfo.account)) {
                ui = info;
            }
        }
        if (ui != null) {
            likedUsers.remove(ui);
        }
    }

    public int getLikedUsersCount() {
        return likedUsers == null ? 0 : likedUsers.size();
    }

    public void updateMyLike(Context context, boolean liked) {
        myUp = liked;
        UserInfo myUser = AccountInfo.getInstance(context).getUserInfo();
        if (likedUsers != null) {
            likedUsers.remove(myUser);
        }
        if (liked) {
            upCount++;
            if (likedUsers == null) {
                likedUsers = new ArrayList<UserInfo>();
                avatars = new ArrayList<Image>();
            }
            likedUsers.add(0, myUser);
        } else {
            upCount--;
        }
    }

    public void updateMyHate(Context context, boolean hated) {
        myDown = hated;
        if (myDown) {
            downCount++;
        } else {
            downCount--;
        }
    }

    public abstract int getId();

    public abstract String getIdName();

    public CfRecord(Parcel in) {
        postId = in.readInt();
        content = in.readString();
        createdOn = in.readLong();
        updatedOn = in.readLong();
        replyCount = in.readInt();
        upCount = in.readInt();
        downCount = in.readInt();
        myUp = in.readInt() != 0;
        myDown = in.readInt() != 0;
        canDelete = in.readInt() != 0;
        isAdmin = in.readInt() != 0;
        userInfo = in.readParcelable(UserInfo.class.getClassLoader());

        int size = in.readInt();
        if (size > 0) {
            likedUsers = new ArrayList<UserInfo>();
            for (int i = 0; i < size; i++) {
                UserInfo user = in.readParcelable(UserInfo.class.getClassLoader());
                likedUsers.add(user);
            }
        }
        int atSize = in.readInt();
        if (atSize > 0) {
            atUsers = new ArrayList<AtUser>(atSize);
            for (int i = 0; i < atSize; i++) {
                AtUser atUser = new AtUser();
                atUser.nick = in.readString();
                atUser.userId = in.readInt();
                atUsers.add(atUser);
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(postId);
        out.writeString(content);
        out.writeLong(createdOn);
        out.writeLong(updatedOn);
        out.writeInt(replyCount);
        out.writeInt(upCount);
        out.writeInt(downCount);
        out.writeInt(myUp ? 1 : 0);
        out.writeInt(myDown ? 1 : 0);
        out.writeInt(canDelete ? 1 : 0);
        out.writeInt(isAdmin ? 1 : 0);
        out.writeParcelable(userInfo, flags);

        int size = getLikedUsersCount();
        out.writeInt(size);
        if (size > 0) {
            for (UserInfo user : likedUsers) {
                out.writeParcelable(user, flags);
            }
        }

        int atSize  = getAtUserCount();
        out.writeInt(atSize);
        if (atSize > 0) {
            for (AtUser atUser : atUsers) {
                out.writeString(atUser.nick);
                out.writeInt(atUser.userId);
            }
        }
    }
    

    public int getAtUserCount() {
        return atUsers == null ? 0 : atUsers.size();
    }


    public static class AtUser implements Parcelable {
        public int userId;
        public String nick;
        
        public static final JsonUtil.ITransformer<AtUser> TRANSFORMER =
                new JsonUtil.ITransformer<CfPost.AtUser>() {
            
            @Override
            public AtUser transform(JSONObject json) {
                return fromJson(json);
            }
        };
        
        public AtUser() {
            
        }
        public static final AtUser fromJson(JSONObject json) {
            if (json == null) {
                return null;
            }
            
            AtUser atUser = new AtUser();
            atUser.userId = json.optInt("uid");
            atUser.nick = json.optString("nick");
            return atUser;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(userId);
            dest.writeString(nick);
        }
        
        public AtUser(Parcel in) {
            userId = in.readInt();
            nick = in.readString();
        }
        
    }
}
