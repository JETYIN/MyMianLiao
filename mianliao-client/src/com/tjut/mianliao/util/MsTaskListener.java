package com.tjut.mianliao.util;

public interface MsTaskListener {

    public void onPreExecute(MsTaskType type);
    public void onPostExecute(MsTaskType type, MsResponse response);

    public enum MsTaskType {
        FORUM_PUBLISH_POST,
        FORUM_EDIT_POST,
        FORUM_STICK_POST,
        FORUM_RECOMMEND_POST,
        FORUM_LIKE_POST,
        FORUM_LIKE_REPLY,
        FORUM_HATE_POST,
        FORUM_HATE_REPLY,
        FORUM_COMMENT_POST,
        FORUM_COMMENT_REPLY,
        FORUM_DELETE_POST,
        FORUM_DELETE_REPLY,
        FORUM_NOTE_POST,
        FORUM_EDIT_NOTE,
        FORUM_DELETE_NOTE,
        FORUM_COLLECT_POST,
        FORUM_STICK_POST_V4,
        FORUM_DELETE_POST_V4
    }
}
