package com.tjut.mianliao.data.task;

import org.json.JSONObject;

import com.tjut.mianliao.util.JsonUtil;

public class TaskLevel {

    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static TaskLevel fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }

        TaskLevel task_level = new TaskLevel();
        task_level.setId(json.optInt("id"));
        task_level.setName(json.optString("name"));

        return task_level;

    }

    public static final JsonUtil.ITransformer<TaskLevel> TRANSFORMER = new JsonUtil.ITransformer<TaskLevel>() {
        @Override
        public TaskLevel transform(JSONObject json) {
            return fromJson(json);
        }
    };

}
