package com.tjut.mianliao.data.notice;

import org.json.JSONObject;

import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.Medal;
import com.tjut.mianliao.data.News;
import com.tjut.mianliao.data.NewsComment;
import com.tjut.mianliao.data.bounty.BountyContract;
import com.tjut.mianliao.data.bounty.BountyTask;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.CfReply;
import com.tjut.mianliao.qa.Answer;
import com.tjut.mianliao.qa.Question;

public class Notice {

    public static final int CAT_SYS_MSG = 1;
    public static final int CAT_SYS_MEDAL = 2;

    public static final int CAT_QA_QUEST_ANSWER = 3;
    public static final int CAT_QA_ANSWER_CHOSEN = 4;
    public static final int CAT_QA_QUEST_AT = 12;
    public static final int CAT_QA_ANSWER_AT = 13;

    public static final int CAT_NEWS_CMT_REPLY = 5;
    public static final int CAT_NEWS_CMT_AT = 9;

    public static final int CAT_FORUM_REPLY = 6;
    public static final int CAT_FORUM_INVITE = 7;
    public static final int CAT_FORUM_POST_SHARE = 8;
    public static final int CAT_FORUM_POST_AT = 10;
    public static final int CAT_FORUM_REPLY_AT = 11;
    public static final int CAT_FORUM_POST_UPDATED = 18;

    public static final int CAT_BOUNTY_REQUEST = 14;
    public static final int CAT_BOUNTY_AT = 15;
    public static final int CAT_BOUNTY_GUEST_RATED = 16;
    public static final int CAT_BOUNTY_HOST_RATED = 17;

    public int category;
    public long time;
    public JSONObject data;

    public SysMsg sysMsg;
    public Medal sysMedal;

    public Question qaQuest;
    public Answer qaAnswer;

    public News news;
    public NewsComment newsComment;

    public CfPost forumPost;
    public CfReply forumReply;
    public Forum forum;
    public UserInfo forumInvitor;

    public BountyTask btyTask;
    public BountyContract btyContract;
    public int btyRating;
    public String btyRatingCmt;

    public static Notice fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        Notice notice = new Notice();
        notice.category = json.optInt("category");
        notice.time = json.optLong("time") * 1000;
        notice.data = json.optJSONObject("data");
        notice.parseData();
        return notice;
    }

    private void parseData() {
        switch (category) {
            case CAT_SYS_MSG:
                sysMsg = SysMsg.fromJson(data);
                break;

            case CAT_SYS_MEDAL:
                sysMedal = Medal.fromJson(data);
                break;

            case CAT_QA_QUEST_ANSWER:
            case CAT_QA_ANSWER_CHOSEN:
            case CAT_QA_ANSWER_AT:
                qaAnswer = Answer.fromJson(data);
                qaQuest = Question.fromJson(data.optJSONObject("question"));
                break;

            case CAT_QA_QUEST_AT:
                qaQuest = Question.fromJson(data);
                break;

            case CAT_NEWS_CMT_REPLY:
            case CAT_NEWS_CMT_AT:
                newsComment = NewsComment.fromJson(data);
                news = News.fromJson(data.optJSONObject("broadcast"));
                break;

            case CAT_FORUM_REPLY:
            case CAT_FORUM_REPLY_AT:
                forumReply = CfReply.fromJson(data);
                break;

            case CAT_FORUM_INVITE:
            case CAT_FORUM_POST_SHARE:
                forumPost = CfPost.fromJson(data.optJSONObject("thread"));
                forum = Forum.fromJson(data.optJSONObject("forum"));
                forumInvitor = UserInfo.fromJson(data.optJSONObject("invitor"));
                break;

            case CAT_FORUM_POST_AT:
            case CAT_FORUM_POST_UPDATED:
                forumPost = CfPost.fromJson(data);
                break;

            case CAT_BOUNTY_AT:
                btyTask = BountyTask.fromJson(data);
                break;

            case CAT_BOUNTY_REQUEST:
            case CAT_BOUNTY_GUEST_RATED:
            case CAT_BOUNTY_HOST_RATED:
                btyContract = BountyContract.fromJson(data);
                btyTask = BountyTask.fromJson(data.optJSONObject("task"));
                btyRating = data.optInt("rating");
                btyRatingCmt = data.optString("comment");
                break;

            default:
                break;
        }
    }
}
