package com.tjut.mianliao.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.JsonUtil;

public class Forum implements Parcelable {

    public static final String INTENT_EXTRA_NAME = "forum";
    public static final String INTENT_EXTRA_DELETED = "forum_deleted";
    public static final String INTENT_EXTRA_GUID = "forum_guid";
    public static final String INTENT_EXTRA_SCHOOLID="school_id";
    public static final String INTENT_EXTRA_SCHOOLNAME="school_name";
    public static final String INTENT_EXTRA_ISROAMING="is_roaming";
    public static final String INTENT_EXTRA_ISCLOOECTION="is_collection";

    public static final int TYPE_SCHOOL = 0;
    public static final int TYPE_COURSE = 1;
    public static final int TYPE_SQUARE = 2;
    public static final int TYPE_SCHOOL_USER = 3;
    public static final int TYPE_SQUARE_USER = 4;
    public static final int TYPE_DEFAULT = 5;
    public static final int TYPE_SECOND_TRADE = 7;

    public static final int PRIVACY_PUBLIC = 0;
    public static final int PRIVACY_STRANGER_READONLY = 1;
    public static final int PRIVACY_PRIVATE = 2;

    private static final int FLAG_PROM = 1;
    private static final int FLAG_VIP = 2;

    public static final Forum DEFAULT_FORUM = new Forum() {{
        type = TYPE_DEFAULT;
    }};

    public static final Forum SECOND_TRADE_FORUM = new Forum() {{
        type = TYPE_SECOND_TRADE;
    }};

    public static final Forum JOB_FORUM = new Forum() {{
        id = 2;
        type = TYPE_SQUARE_USER;
    }};

    public static final String ID = "forum_id";
    public static final String GUID = "guid";
    public static final String NAME = "name";
    public static final String INTRO = "intro";
    public static final String TYPE = "type";
    public static final String ADMIN_UID = "admin_uid";
    public static final String ADMIN_NAME = "admin_user";
    public static final String ALLOW_ANONY = "allow_anony";
    public static final String PRIVACY = "privacy";
    public static final String MEMBER_COUNT = "member_count";
    public static final String THREAD_COUNT = "thread_count";
    public static final String ICON = "icon";
    public static final String BG_IMG = "bg_img";
    public static final String IS_MEMBER = "is_member";
    public static final String UNREAD_COUNT = "unread_count";
    public static final String FLAG = "flag";
    public static final String IS_LISTENING = "is_circle_listening";

    public int id;
    public String guid;
    public String name;
    public String intro;
    public int type = -1;
    public int adminUid;
    public String adminName;
    public boolean allowAnony;
    public int privacy;
    public int memberCount;
    public int threadCount;
    public String icon;
    public String bgImg;
    public boolean isMember;
    public int postCountToday;
    public int flag;
    public boolean isListening;

    public int courseId;
    public int memberRequests;

    public Forum() {}

    public Forum(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public static Forum fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        Forum forum = new Forum();
        forum.id = json.optInt(ID);
        forum.guid = json.optString(GUID);
        forum.name = json.optString(NAME);
        forum.intro = json.optString(INTRO);
        forum.type = json.optInt(TYPE);
        forum.adminUid = json.optInt(ADMIN_UID);
        forum.adminName = json.optString(ADMIN_NAME);
        forum.allowAnony = json.optBoolean(ALLOW_ANONY);
        forum.privacy = json.optInt(PRIVACY);
        forum.memberCount = json.optInt(MEMBER_COUNT);
        forum.threadCount = json.optInt(THREAD_COUNT);
        forum.icon = AliImgSpec.FORUM_ICON.makeUrl(json.optString(ICON));
        forum.bgImg = AliImgSpec.FORUM_BG.makeUrl(json.optString(BG_IMG));
        forum.isMember = json.optBoolean(IS_MEMBER);
        forum.postCountToday = json.optInt(UNREAD_COUNT);
        forum.flag = json.optInt(FLAG);
        forum.isListening = json.optBoolean(IS_LISTENING);
        return forum;
    }

    public String getIdName() {
        return id > 0 ? ID : "course_id";
    }

    public int getId() {
        return id > 0 ? id : courseId;
    }

    public boolean hasValidId() {
        return type == TYPE_DEFAULT || id > 0 || courseId > 0;
    }

    public boolean hasGuid() {
        return !TextUtils.isEmpty(guid);
    }

    public boolean canPost() {
        return isMember || privacy == PRIVACY_PUBLIC;
    }

    public boolean isValid() {
        return type == TYPE_DEFAULT
        		|| type == TYPE_SECOND_TRADE
        		|| hasValidId()
        		|| hasGuid();
    }

    public boolean isPublic() {
        return type == TYPE_SQUARE || type == TYPE_SQUARE_USER;
    }

    public boolean isUserForum() {
        return type == TYPE_SCHOOL_USER || type == TYPE_SQUARE_USER;
    }

    public boolean isAdmin(Context context) {
        int userId = AccountInfo.getInstance(context).getUserId();
        return userId > 0 && userId == adminUid;
    }

    public boolean isVip() {
        return (flag & FLAG_VIP) != 0;

    }

    public boolean isProm() {
        return (flag & FLAG_PROM) != 0;
    }

    public JSONObject toJson() {
        JSONObject jo = new JSONObject();
        try {
            jo.put(ID, id);
            jo.put(GUID, guid);
            jo.put(NAME, name);
            jo.put(INTRO, intro);
            jo.put(TYPE, type);
            jo.put(ADMIN_UID, adminUid);
            jo.put(ADMIN_NAME, adminName);
            jo.put(ALLOW_ANONY, allowAnony);
            jo.put(PRIVACY, privacy);
            jo.put(MEMBER_COUNT, memberCount);
            jo.put(THREAD_COUNT, threadCount);
            jo.put(ICON, icon);
            jo.put(BG_IMG, bgImg);
            jo.put(IS_MEMBER, isMember);
            jo.put(UNREAD_COUNT, postCountToday);
            jo.put(FLAG, flag);
            jo.put(IS_LISTENING, isListening);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo;
    }

    public static final Parcelable.Creator<Forum> CREATOR = new Parcelable.Creator<Forum>() {
        @Override
        public Forum createFromParcel(Parcel in) {
            return new Forum(in);
        }

        @Override
        public Forum[] newArray(int size) {
            return new Forum[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public Forum(Parcel in) {
        id = in.readInt();
        guid = in.readString();
        name = in.readString();
        intro = in.readString();
        type = in.readInt();
        adminUid = in.readInt();
        adminName = in.readString();
        allowAnony = in.readInt() == 1;
        privacy = in.readInt();
        memberCount = in.readInt();
        threadCount = in.readInt();
        icon = in.readString();
        bgImg = in.readString();
        isMember = in.readInt() == 1;
        postCountToday = in.readInt();
        flag = in.readInt();
        isListening = in.readInt() == 1;

        courseId = in.readInt();
        memberRequests = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(guid);
        dest.writeString(name);
        dest.writeString(intro);
        dest.writeInt(type);
        dest.writeInt(adminUid);
        dest.writeString(adminName);
        dest.writeInt(allowAnony ? 1 : 0);
        dest.writeInt(privacy);
        dest.writeInt(memberCount);
        dest.writeInt(threadCount);
        dest.writeString(icon);
        dest.writeString(bgImg);
        dest.writeInt(isMember ? 1 : 0);
        dest.writeInt(postCountToday);
        dest.writeInt(flag);
        dest.writeInt(isListening ? 1 : 0);

        dest.writeInt(courseId);
        dest.writeInt(memberRequests);
    }

    public static final JsonUtil.ITransformer<Forum> TRANSFORMER =
            new JsonUtil.ITransformer<Forum>() {
                @Override
                public Forum transform(JSONObject json) {
                    return fromJson(json);
                }
            };

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Forum) {
            Forum other = (Forum) o;
            return id == other.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
