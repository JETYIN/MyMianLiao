package com.tjut.mianliao.data.task;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.tjut.mianliao.util.JsonUtil;

public class SubTask {
    private List<TaskLevel> level;

    public List<TaskLevel> getLevel() {
        return level;
    }

    private List<Task> info;

    public List<Task> getInfo() {
        return info;
    }

    public static SubTask fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }

        SubTask taskLevelInfo = new SubTask();
        JSONArray array = json.optJSONArray("tasks");
        JSONArray levelArray = json.optJSONArray("level");
        taskLevelInfo.info = JsonUtil.getArray(array, Task.TRANSFORMER);
        taskLevelInfo.level = JsonUtil.getArray(levelArray, TaskLevel.TRANSFORMER);
        return taskLevelInfo;

    }

}
