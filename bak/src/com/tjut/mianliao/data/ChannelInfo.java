package com.tjut.mianliao.data;

import java.util.ArrayList;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.CfReply;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.Utils;

public class ChannelInfo implements Parcelable {

    public int forumId;
    public String name;
    public int type;
    public String title;
    public String intro;
    public String guid;
    public String icon;
    public String bgImg;
    public String ruleIcon;
    public String ruleTitle;
    public String ruleContent;
    public int threadType;
    public boolean havaRule;
    private ArrayList<ChannelTag> tags = new ArrayList<>();
    public int style;
    public boolean collected;
    public long createOn;
    public int[] moderators;

    public int userCount;
    public int myThreadCount;
    public static final JsonUtil.ITransformer<ChannelInfo> TRANSFORMER =

            new JsonUtil.ITransformer<ChannelInfo>() {
        @Override
        public ChannelInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public ChannelInfo() {
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public int getMyThreadCount() {
        return myThreadCount;
    }

    public void setMyThreadCount(int myThreadCount) {
        this.myThreadCount = myThreadCount;
    }

    public boolean isModerator(CfPost post) {
        if (moderators != null && moderators.length > 0) {
             for (int uid : moderators) {
                 if (post.userInfo.userId == uid) {
                     return true;
                 }
             }
        }
        return false;
    }
    
    public boolean isModerator(CfReply reply) {
        if (moderators != null && moderators.length > 0) {
            for (int uid : moderators) {
                if (reply.userInfo.userId == uid) {
                    return true;
                }
            }
        }
        return false;
    }

    public static ChannelInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.forumId = json.optInt("forum_id");
        channelInfo.name = json.optString("name");
        channelInfo.type = json.optInt("type");
        channelInfo.title = json.optString("title");
        channelInfo.intro = json.optString("intro");
        channelInfo.guid = json.optString("guid");
        channelInfo.icon = json.optString("icon");
        channelInfo.bgImg = json.optString("bg_img");
        channelInfo.ruleIcon = json.optString("rule_icon");
        channelInfo.ruleTitle = json.optString("rule_title");
        channelInfo.ruleContent = json.optString("rule_content");
        channelInfo.threadType = json.optInt("thread_type");
        channelInfo.havaRule = json.optBoolean("have_rule");
        channelInfo.tags = JsonUtil.getArray(json.optJSONArray("tags"), ChannelTag.TRANSFORMER);
        channelInfo.style = json.optInt("style");
        channelInfo.collected = json.optBoolean("collected");
        channelInfo.setUserCount(json.optInt("user_count"));
        channelInfo.setMyThreadCount(json.optInt("my_thread_count"));
        channelInfo.createOn = json.optLong("created_on") * 1000;
        channelInfo.moderators = JsonUtil.getIntArray(json.optJSONArray("admin_uids"));
        return channelInfo;
    }

    public static final Parcelable.Creator<ChannelInfo> CREATOR =
            new Parcelable.Creator<ChannelInfo>() {
        @Override
        public ChannelInfo createFromParcel(Parcel source) {
            return new ChannelInfo(source);
        }

        @Override
        public ChannelInfo[] newArray(int size) {
            return new ChannelInfo[size];
        }
    };

    public void addTag(ChannelTag tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public ArrayList<ChannelTag> getTags() {
        return tags;
    }

    public String getTimeDes() {
        CharSequence time = Utils.getPostShowTimeString(createOn);
        return time + " 创建";
    }

    private ChannelInfo(Parcel source) {
        forumId = source.readInt();
        name = source.readString();
        type = source.readInt();
        title = source.readString();
        intro = source.readString();
        guid = source.readString();
        icon = source.readString();
        bgImg = source.readString();
        ruleIcon = source.readString();
        ruleTitle = source.readString();
        ruleContent = source.readString();
        threadType = source.readInt();
        havaRule = source.readInt() == 0 ? false : true;
        style = source.readInt();
        collected = source.readInt() == 0 ? false : true;
        int count = source.readInt();
        for (int i = 0; i < count; i++) {
            addTag(new ChannelTag(source.readString(), source.readString()));
        }
        userCount = source.readInt();
        myThreadCount = source.readInt();
        createOn = source.readLong();
        int moderatorCount = source.readInt();
        if (moderatorCount > 0) {
            moderators = new int[moderatorCount];
            for (int i = 0; i < moderatorCount; i++) {
                moderators[i] = source.readInt();
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(forumId);
        dest.writeString(name);
        dest.writeInt(type);
        dest.writeString(title);
        dest.writeString(intro);
        dest.writeString(guid);
        dest.writeString(icon);
        dest.writeString(bgImg);
        dest.writeString(ruleIcon);
        dest.writeString(ruleTitle);
        dest.writeString(ruleContent);
        dest.writeInt(threadType);
        dest.writeInt(havaRule ? 1 : 0);
        dest.writeInt(style);
        dest.writeInt(collected ? 1 : 0);
        dest.writeInt(tags.size());
        for (ChannelTag tag : tags) {
            dest.writeString(tag.name);
            dest.writeString(tag.image);
        }
        dest.writeInt(myThreadCount);
        dest.writeInt(userCount);
        dest.writeLong(createOn);
        dest.writeInt(moderators == null ? 0 : moderators.length);
        if (moderators != null) {
            for (int moderator: moderators) {
                dest.writeInt(moderator);
            }
        }
    }

}
