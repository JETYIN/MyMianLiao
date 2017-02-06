package com.tjut.mianliao.data.task;

import org.json.JSONObject;

public class FullTask {

    private SubTask dailyInfo, rookieInfo, collegeInfo;

    public SubTask getDailyInfo() {
        return dailyInfo;
    }

    public SubTask getRookieInfo() {
        return rookieInfo;
    }

    public SubTask getCollegeInfo() {
        return collegeInfo;
    }

    public static FullTask fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }

        FullTask info = new FullTask();
        info.dailyInfo = SubTask.fromJson(json.optJSONObject("daily"));
        info.rookieInfo = SubTask.fromJson(json.optJSONObject("newbie"));
        info.collegeInfo = SubTask.fromJson(json.optJSONObject("high_school"));

        return info;
    }

}
