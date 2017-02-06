package com.tjut.mianliao.data.task;

import org.json.JSONObject;

import com.tjut.mianliao.util.JsonUtil;

public class Task {

    private int id, process, max, credit;
    private long bg_color;
    private String name, icon;
    private boolean is_finish;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProcess() {
        return process;
    }

    public void setProcess(int process) {
        this.process = process;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public long getBgColor() {
        return bg_color;
    }

    public void setBg_color(long bg_color) {
        this.bg_color = bg_color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isFinish() {
        return is_finish;
    }

    public void setIs_finish(boolean is_finish) {
        this.is_finish = is_finish;
    }

    public static Task fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }

        Task task = new Task();
        task.setId(json.optInt("id"));
        task.setName(json.optString("name"));
        task.setProcess(json.optInt("process"));
        task.setMax(json.optInt("max"));
        task.setIs_finish(json.optBoolean("is_finish"));
        task.setCredit(json.optInt("credit"));
        task.setBg_color(json.optLong("bg_color"));
        task.setIcon(json.optString("icon"));

        return task;

    }

    public static final JsonUtil.ITransformer<Task> TRANSFORMER = new JsonUtil.ITransformer<Task>() {
        @Override
        public Task transform(JSONObject json) {
            return fromJson(json);
        }
    };

}