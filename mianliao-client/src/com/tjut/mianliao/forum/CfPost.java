package com.tjut.mianliao.forum;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.Attachment;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.data.mycollege.NoteInfo;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.JsonUtil;

public class CfPost extends CfRecord {
    public static final String INTENT_EXTRA_NAME = "cf_post";

    public static final int THREAD_TYPE_NORMAL = 1;
    public static final int THREAD_TYPE_TXT_VOTE = 2;
    public static final int THREAD_TYPE_PIC_VOTE = 3;
    public static final int THREAD_TYPE_TXT = 4;
    public static final int THREAD_TYPE_PIC_TXT = 5;
    public static final int THREAD_TYPE_PIC_VOICE = 6;
    public static final int THREAD_TYPE_RICH_MEDIA = 7;
    public static final int THREAD_TYPE_VIDEO = 8;
    public static final int THREAD_TYPE_VOICE = 9;

    public static final int FROM_CHANNEL = 40;// 频道发帖
    public static final int FROM_CAMPUS = 5;//校内发帖
    
    public static final int FROM_BANNER = 1;

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_VOTE = 1;
    public static final int TYPE_EVENT = 2;
    private static final int FLAG_RECOMMEND = 1;

    public String title;
    public String image;
    public int stickLvl;
    public int forumId;
    public String forumName;
    public int tribeId;
    public boolean isTribePicked;
    public boolean isTribeHot;
    public boolean isTribeTop;
    public String forumIcon;
    public String publicUrl;
    public int privacy;
    public int favCount;
    public boolean myFav;
    public int type;
    public int threadType;
    public boolean sameSchool;
    public int flag;
    public Vote vote;
    public Event event;
    public Attachment attachment;
    // add by v2.4.3
    public Attachment voice;
    public int voiceLength;
    public int suggestion;
    public int hot;
    private String voicePath;
    public int readCount;

    public int currentPosition;
    public int totalDuration;
    public int forumType;
    
    public int style;
    public int[] moderatorUids;
    public int[] superModeratorUids;
    
    // add while cache use

    public long imageId;
    public long optionId; // 帖子投票选项在数据表中的索引集合
    public String likedUserIds;
    public int schoolId;
    
    public boolean collected;
    public String atUser; // it's mean @user
    public String videoUrl;
    public String videoThumbnail;
    
    public boolean voicePlaying;
    
    public String getVoicePath() {
        return voicePath;
    }

    public void setVoicePath(String voicePath) {
        this.voicePath = voicePath;
    }

    public int from;

    public ArrayList<Image> images;

    public boolean isHotPost;

    public CfPost() {}

    public static CfPost fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        CfPost post = new CfPost();
        CfRecord.fillFromJson(post, json);
        post.title = json.optString("title");
        if (TextUtils.isEmpty(post.title) && !TextUtils.isEmpty(post.content)) {
            if (post.content.length() < 30) {
                post.title = post.content;
                post.content = null;
            } else {
                post.title = post.content.substring(0, Math.min(30, post.content.length()));
            }
        }
        post.image = json.optString("image");
        post.attachment = Attachment.fromJson(json.optJSONObject("attachment"));
        post.stickLvl = json.optInt("stick_lvl");
        post.forumId = json.optInt("forum_id");
        post.forumName = json.optString("forum_name");
        post.tribeId = json.optInt("tribe_id");
        post.isTribePicked = json.optBoolean("is_tribe_picked");
        post.isTribeHot = json.optBoolean("is_tribe_hot");
        post.isTribeTop = json.optBoolean("is_tribe_top");
        post.forumIcon = json.optString("forum_icon");
        post.publicUrl = json.optString("public_url");
        post.privacy = json.optInt("privacy");
        post.sameSchool = json.optBoolean("same_school");
        post.favCount = json.optInt("fav_count");
        post.myFav = json.optBoolean("my_fav");
        post.type = json.optInt("type");
        post.threadType = json.optInt("thread_type");
        post.flag = json.optInt("flag");
        post.vote = Vote.fromJson(json.optJSONObject("vote"));
        post.event = Event.fromJson(json.optJSONObject("event"));
        if ((post.threadType == THREAD_TYPE_TXT_VOTE && post.vote == null) ||
                (post.threadType == THREAD_TYPE_TXT_VOTE && post.event == null)) {
            post.type = TYPE_NORMAL;
        }

        post.images = JsonUtil.getArray(json.optJSONArray("images"), Image.TRANSFORMER);
        // add by v2.4.3
        post.suggestion = json.optInt("suggestion");
        post.hot = json.optInt("hot");
        post.voice = Attachment.fromJson(json.optJSONObject("voice"));
        post.voiceLength = json.optInt("voice_length");
        post.readCount = json.optInt("read_count");
        post.forumType = json.optInt("forum_type");
        post.style = json.optInt("style");
        // add by 3.3.0
        post.collected = json.optBoolean("collected");
        
        //add by 3.4.0
        post.replyTime = json.optLong("reply_time") * 1000;
        post.schoolId = json.optInt("school_id");
        fillVideoInfo(json, post);
        post.schoolId = json.optInt("school_id");
        // add by 4.0.0
        post.moderatorUids = JsonUtil.getIntArray(json.optJSONArray("forum_moderator_uids"));
        post.superModeratorUids = JsonUtil.getIntArray(json.optJSONArray("superModerator_uids"));
        //add by 4.1.0
        post.isHotPost = json.optBoolean("is_hot_thread");
        return post;
    }

    public CfPost copy(CfPost post) {
        CfPost postCopy = new CfPost();
        postCopy.attachment = post.attachment;
        postCopy.atUser = post.atUser;
        postCopy.atUsers = post.atUsers;
        postCopy.avatars = post.avatars;
        postCopy.canDelete = post.canDelete;
        postCopy.collected = post.collected;
        postCopy.content = post.content;
        postCopy.createdOn = post.createdOn;
        postCopy.downCount = post.downCount;
        postCopy.favCount = post.favCount;
        postCopy.flag = post.flag;
        postCopy.forumIcon = post.forumIcon;
        postCopy.forumId = post.forumId;
        postCopy.forumName = post.forumName;
        postCopy.forumType = post.forumType;
        postCopy.from =  post.from;
        postCopy.hot = post.hot;
        postCopy.image = post.image;
        postCopy.imageId = post.imageId;
        postCopy.images = post.images;
        postCopy.isAdmin = post.isAdmin;
        postCopy.likedUserIds = post.likedUserIds;
        postCopy.likedUsers = post.likedUsers;
        postCopy.myDown = post.myDown;
        postCopy.myFav = post.myFav;
        postCopy.myUp = post.myUp;
        postCopy.optionId = post.optionId;
        postCopy.postId = post.postId;
        postCopy.privacy = post.privacy;
        postCopy.publicUrl = post.publicUrl;
        postCopy.readCount = post.readCount;
        postCopy.relation = post.relation;
        postCopy.replyCount = post.replyCount;
        postCopy.replyTime = post.replyTime;
        postCopy.sameSchool = post.sameSchool;
        postCopy.schoolId = post.schoolId;
        postCopy.stickLvl = post.stickLvl;
        postCopy.style = post.style;
        postCopy.suggestion = post.suggestion;
        postCopy.threadType = post.threadType;
        postCopy.title = post.title;
        postCopy.totalDuration = post.totalDuration;
        postCopy.tribeId = post.tribeId;
        postCopy.type = post.type;
        postCopy.upCount = post.upCount;
        postCopy.updatedOn = post.updatedOn;
        postCopy.userInfo = post.userInfo;
        postCopy.videoThumbnail = post.videoThumbnail;
        postCopy.videoUrl = post.videoUrl;
        postCopy.voice = post.voice;
        postCopy.voiceLength = post.voiceLength;
        postCopy.vote = post.vote;
        postCopy.moderatorUids = post.moderatorUids;
        postCopy.superModeratorUids = post.superModeratorUids;
        postCopy.isHotPost = post.isHotPost;
        return postCopy;
    }
    
    private static void fillVideoInfo(JSONObject json, CfPost post) {
        try {
            JSONArray ja = json.getJSONArray("video_url_json");
            if (ja != null && ja.length() > 0) {
                JSONObject jo = (JSONObject) ja.get(0);
                Iterator<String> keys = jo.keys();
                if (keys.hasNext()) {
                    String key = keys.next();
                    String value = jo.optString(key);
                    post.videoThumbnail = key.startsWith("http://") ? key : "http://" + key;
                    post.videoUrl = value.startsWith("http://") ? value : "http://" + value;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isPrivate() {
        return Forum.PRIVACY_PRIVATE == privacy;
    }

    public int getImageCount() {
        return images == null ? 0 : images.size();
    }
    
    public String getBannerImage() {
        if (images != null && images.size() > 0) {
            return AliImgSpec.POST_BANNER.makeUrl(images.get(0).image);
        }
        return null;
    }

    public String getImagePreview(int index) {
        if (images == null || index >= images.size()) {
            return null;
        } else {
            return AliImgSpec.POST_THUMB.makeUrl(images.get(index).image);
        }
    }

    public String getImagePreviewSmall(int index) {
        if (images == null || index >= images.size()) {
            return null;
        } else if (images.size() == 1) {
            return AliImgSpec.POST_THUMB.makeUrl(images.get(0).image);
        } else {
            return AliImgSpec.POST_THUMB_SQUARE.makeUrl(images.get(index).image);
        }
    }

    public boolean isRecommend() {
        return (flag & FLAG_RECOMMEND) != 0;
    }

    public void setRecommend(boolean recommend) {
        if (recommend) {
            flag |= FLAG_RECOMMEND;
        } else {
            flag &= ~FLAG_RECOMMEND;
        }
    }

    public int getTypeStringRes() {
        switch (type) {
            case TYPE_EVENT:
                return R.string.fp_post_type_event;
            case TYPE_VOTE:
                return R.string.fp_post_type_vote;
            default:
                return 0;
        }
    }

    public boolean hasVote() {
        return getVoteOptCount() > 0;
    }

    public int getVoteOptCount() {
        return threadType != THREAD_TYPE_TXT_VOTE|| vote == null
                || vote.options == null ? 0 : vote.options.size();
    }

    public String getVoteOpt(int index) {
        return hasVote() ? vote.options.get(index) : null;
    }

    public boolean hasEvent() {
        return type == TYPE_EVENT && event != null;
    }

    @Override
    public int getId() {
        return postId;
    }

    public boolean isStickLvl() {
        return stickLvl > 0;
    }
    
    public boolean isUserAreSuperModerator() {
        return isUserAreSuperModerator(userInfo.userId);
    }
    
    public boolean isUserAreSuperModerator(int userId) {
        boolean isSuperModerator = false;
        int length = superModeratorUids == null ? 0 : superModeratorUids.length;
        for (int i = 0; i < length; i++) {
            if (superModeratorUids[i] == userId) {
                isSuperModerator = true;
            }
        }
        return isSuperModerator;
    }
    
    public boolean isUserAreModerator() {
        return isUserAreModerator(userInfo.userId);
    }
    
    public boolean isUserAreModerator(int userId) {
        boolean isModerator = false;
        int length = moderatorUids == null ? 0 : moderatorUids.length;
        for (int i = 0; i < length; i++) {
            if (moderatorUids[i] == userId) {
                isModerator = true;
            }
        }
        return isModerator;
    }
    
    public boolean isModerator(int userId) {
        return isUserAreModerator(userId);
    }
    
    @Override
    public String getIdName() {
        return "thread_id";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof CfPost) {
            CfPost other = (CfPost) o;
            return postId == other.postId;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return postId;
    }

    public static final JsonUtil.ITransformer<CfPost> TRANSFORMER =
            new JsonUtil.ITransformer<CfPost>() {
        @Override
        public CfPost transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public static final Parcelable.Creator<CfPost> CREATOR =
            new Parcelable.Creator<CfPost>() {
        @Override
        public CfPost createFromParcel(Parcel in) {
            return new CfPost(in);
        }

        @Override
        public CfPost[] newArray(int size) {
            return new CfPost[size];
        }
    };

    public CfPost(Parcel in) {
        super(in);
        title = in.readString();
        image = in.readString();
        stickLvl = in.readInt();
        forumId = in.readInt();
        forumName = in.readString();
        tribeId = in.readInt();
        isTribePicked = in.readInt() == 0 ? false : true;
        isTribeHot = in.readInt() == 0 ? false : true;
        isTribeTop = in.readInt() == 0 ? false : true;
        forumIcon = in.readString();
        publicUrl = in.readString();
        privacy = in.readInt();
        favCount = in.readInt();
        myFav = in.readByte() != 0;
        type = in.readInt();
        threadType = in.readInt();
        flag = in.readInt();
        if (threadType == THREAD_TYPE_TXT_VOTE || threadType == THREAD_TYPE_PIC_VOTE) {
            vote = new Vote(in);
        } else if (threadType == THREAD_TYPE_TXT_VOTE || threadType == THREAD_TYPE_PIC_VOTE) {
            event = new Event(in);
        }
        attachment = new Attachment(in);
        //   add start //
        suggestion = in.readInt();
        hot = in.readInt();
        voice = new Attachment(in);
        voiceLength = in.readInt();
        //   add end     //
        from = in.readInt();

        int size = in.readInt();
        if (size > 0) {
            images = new ArrayList<Image>(size);
            for (int i = 0; i < size; i++) {
                Image image = in.readParcelable(Image.class.getClassLoader());
                images.add(image);
            }
        }
        
        readCount = in.readInt();
        sameSchool = in.readInt() == 1;
        forumType = in.readInt();
        style = in.readInt();
         // add by 3.3.0
        collected = in.readInt() == 1;
        videoThumbnail = in.readString();
        videoUrl = in.readString();
        
        // add by 3.4.0
        replyTime = in.readLong();
        schoolId = in.readInt();
        // add by 4.0.0
        int moderatorSize = in.readInt();
        if (moderatorSize > 0) {
            moderatorUids = new int[moderatorSize];
            in.readIntArray(moderatorUids);
        }
        int superModeratorSize = in.readInt();
        if (superModeratorSize > 0) {
            superModeratorUids = new int[superModeratorSize];
            in.readIntArray(superModeratorUids);
        }
        //add by 4.1.0
        isHotPost = in.readInt() == 0 ? false : true;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(title);
        dest.writeString(image);
        dest.writeInt(stickLvl);
        dest.writeInt(forumId);
        dest.writeString(forumName);
        dest.writeInt(tribeId);
        dest.writeInt(isTribePicked ? 1 : 0);
        dest.writeInt(isTribeHot ? 1 : 0);
        dest.writeInt(isTribeTop ? 1 : 0);
        dest.writeString(forumIcon);
        dest.writeString(publicUrl);
        dest.writeInt(privacy);
        dest.writeInt(favCount);
        dest.writeByte((byte) (myFav ? 1 : 0));
        dest.writeInt(type);
        dest.writeInt(threadType);
        dest.writeInt(flag);
        if (threadType == THREAD_TYPE_TXT_VOTE || threadType == THREAD_TYPE_PIC_VOTE) {
            vote.writeToParcel(dest);
        } else if (threadType == THREAD_TYPE_TXT_VOTE || threadType == THREAD_TYPE_PIC_VOTE) {
            event.writeToParcel(dest);
        }
        attachment = attachment == null ? new Attachment() : attachment;
        attachment.writeToParcel(dest);

        // add by v 2.4.3
        dest.writeInt(suggestion);
        dest.writeInt(hot);
        voice.writeToParcel(dest);
        dest.writeInt(voiceLength);

        dest.writeInt(from);

        int size = getImageCount();
        dest.writeInt(size);
        if (size > 0) {
            for (Image image : images) {
                dest.writeParcelable(image, flags);
            }
        }
        
        dest.writeInt(readCount);
        dest.writeInt(sameSchool ? 1 : 0);
        dest.writeInt(forumType);
        dest.writeInt(style);
        // add by 3.3.0
        dest.writeInt(collected ? 1 : 0);
        dest.writeString(videoThumbnail);
        dest.writeString(videoUrl);
        //add by 3.4.0
        dest.writeLong(replyTime);
        dest.writeInt(schoolId);
        //add by 4.0.0
        int moderatorSize = moderatorUids == null ? 0 : moderatorUids.length;
        dest.writeInt(moderatorSize);
        if (moderatorSize > 0) {
            dest.writeIntArray(moderatorUids);
        }
        int superModeratorSize = superModeratorUids == null ? 0 : superModeratorUids.length;
        dest.writeInt(superModeratorSize);
        if (superModeratorSize > 0) {
            dest.writeIntArray(superModeratorUids);
        }
        //add by 4.1.0
        dest.writeInt(isHotPost ? 1 : 0);
    }

    protected static void fillFromJson(NoteInfo post, JSONObject json) {
        CfRecord.fillFromJson(post, json);
        post.title = json.optString("title");
//        if(post.title  == null){
//            post.title = "默认";
//        }
        if (TextUtils.isEmpty(post.title) && !TextUtils.isEmpty(post.content)) {
            if (post.content.length() < 30) {
                post.title = "默认";
            } else {
                post.title = post.content.substring(0, Math.min(30, post.content.length()));
            }
        }
        post.image = json.optString("image");
        post.attachment = Attachment.fromJson(json.optJSONObject("attachment"));
        post.stickLvl = json.optInt("stick_lvl");
        post.forumId = json.optInt("forum_id");
        post.forumName = json.optString("forum_name");
        post.forumIcon = json.optString("forum_icon");
        post.publicUrl = json.optString("public_url");
        post.privacy = json.optInt("privacy");
        post.sameSchool = json.optBoolean("same_school");
        post.favCount = json.optInt("fav_count");
        post.myFav = json.optBoolean("my_fav");
        post.type = json.optInt("type");
        post.threadType = json.optInt("thread_type");
        post.flag = json.optInt("flag");
        post.vote = Vote.fromJson(json.optJSONObject("vote"));
        post.event = Event.fromJson(json.optJSONObject("event"));
        if ((post.threadType == THREAD_TYPE_TXT_VOTE && post.vote == null) ||
                (post.threadType == THREAD_TYPE_TXT_VOTE && post.event == null)) {
            post.type = TYPE_NORMAL;
        }

        post.images = JsonUtil.getArray(json.optJSONArray("images"), Image.TRANSFORMER);
        // add by v2.4.3
        post.suggestion = json.optInt("suggestion");
        post.hot = json.optInt("hot");
        post.voice = Attachment.fromJson(json.optJSONObject("voice"));
        post.voiceLength = json.optInt("voice_length");
    }
    
}
