package com.tjut.mianliao.data.cache;

import com.tjut.mianliao.forum.CfPost;

public class CachePostInfo extends CfPost{
    
    public static final String TABLE_NAME = "cache_post";
    
    public static final String ID = "_id" ;
    public static final String POST_ID = "postId";
    public static final String CONTENT = "content";
    public static final String CREATED_ON = "createdOn";
    public static final String REPLY_COUNT = "replyCount";
    public static final String UP_COUNT = "upCount";
    public static final String DOWN_COUNT = "downCount";
    public static final String MY_UP = "myUp";
    public static final String MY_DOWN = "myDown";
    public static final String IMAGE_IDS = "imageId";
    public static final String LIKED_USER_IDS = "likedUserIds";
    public static final String TITLE = "title";
    public static final String FORUM_ID = "forumId";
    public static final String PUBLIC_URL = "publicUrl";
    public static final String TYPE = "type";
    public static final String THREAD_TYPE = "threadType";
    public static final String SAME_SCHOOL = "sameSchool";
    public static final String VOICE_LENGTH = "voiceLength";
    public static final String HOT = "hot";
    public static final String READ_COUNT = "readCount";
    public static final String FORUM_TYPE = "forumType";
    public static final String STYLE = "style";
    public static final String VOICE_PATH = "voicePath";
    public static final String IS_NIGHT_POST = "isNightPost";
    public static final String REALTION = "relation";
    public static final String SCHOOL_ID = "schoolId";
    public static final String STICKLVL = "stickLvl";
    public static final String REPLY_TIME = "replyTime";
    public static final String VIDEO_THUMBNAIL = "videoThumbnail";
    public static final String VIDEO_URL = "videoUrl";
    
    // vote info
    public static final String END_TIME = "endTime";
    public static final String ENABLED = "enabled";
    public static final String MY_VOTE = "myVote";
    public static final String MY_VOTE_TIME = "myVoteTime";
    public static final String OPTIONS = "optionId";
    public static final String RESULT = "result";
    
    // user info
    public static final String USER_ID = "uid";
    
    // moderatorUids
    public static final String MODERATOR_UIDS = "moderatorUids";
    public static final String SUPER_MODERATOR_UIDS = "superModeratorUids";
    
    public int _id;
    public boolean isNightPost; // 用来区分黑白帖子
    
    public CachePostInfo(CfPost post) {
        postId = post.postId;
        content = post.content;
        createdOn = post.createdOn;
        replyCount = post.replyCount;
        upCount = post.upCount;
        downCount = post.downCount;
        myUp = post.myUp;
        myDown = post.myDown;
        userInfo = post.userInfo;
        likedUsers = post.likedUsers;
        relation = post.relation;
        title = post.title;
        forumId = post.forumId;
        publicUrl = post.publicUrl;
        type = post.type;
        threadType = post.threadType;
        sameSchool = post.sameSchool;
        vote = post.vote;
        voice = post.voice;
        voiceLength = post.voiceLength;
        hot = post.hot;
        readCount = post.readCount;
        style = post.style;
        stickLvl = post.stickLvl;
        replyTime = post.replyTime;
        videoThumbnail = post.videoThumbnail;
        videoUrl = post.videoUrl;
        moderatorUids = post.moderatorUids;
        superModeratorUids = post.superModeratorUids;
    }
    
    public String getVoteOfMineStr() {
        StringBuilder sb = new StringBuilder();
        if (vote == null || vote.myVote == null || vote.myVote.length == 0) {
            return "";
        }
        boolean isFirst = true;
        int[] mv = vote.myVote;
        for (int i = 0; i < mv.length; i++) {
            if (isFirst) {
                sb.append(mv[i]);
                isFirst = false;
            } else {
                sb.append(",").append(mv[i]);
            }
        }
        return sb.toString();
    }

    public String getVoteResultStr() {
        StringBuilder sb = new StringBuilder();
        if (vote == null || vote.result == null || vote.result.length == 0) {
            return "";
        }
        boolean isFirst = true;
        int[] result = vote.result;
        for (int i = 0; i < result.length; i++) {
            if (isFirst) {
                sb.append(result[i]);
                isFirst = false;
            } else {
                sb.append(",").append(result[i]);
            }
        }
        return sb.toString();
    }
    
    public String getSuperModeratorUids() {
        return getModeratorUids(superModeratorUids);
    }
    
    public String getModeratorUids() {
        return getModeratorUids(moderatorUids);
    }
    
    private String getModeratorUids(int[] uids) {
        StringBuilder sb = new StringBuilder();
        if (uids == null || uids.length == 0) {
            return "";
        }
        boolean isFirst = true;
        int[] result = uids;
        for (int i = 0; i < uids.length; i++) {
            if (isFirst) {
                sb.append(result[i]);
                isFirst = false;
            } else {
                sb.append(",").append(result[i]);
            }
        }
        return sb.toString();
    }
}
