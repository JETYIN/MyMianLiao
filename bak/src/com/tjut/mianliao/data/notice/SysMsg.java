package com.tjut.mianliao.data.notice;

import org.json.JSONObject;

public class SysMsg {
    public int id;
    public String title;
    public String content;

    public static SysMsg fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }

        SysMsg sysMsg = new SysMsg();
        sysMsg.id = json.optInt("id");
        sysMsg.title = json.optString("title");
        sysMsg.content = json.optString("content");

        return sysMsg;
    }
}
