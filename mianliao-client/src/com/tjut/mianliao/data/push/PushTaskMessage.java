package com.tjut.mianliao.data.push;

import org.json.JSONObject;

public class PushTaskMessage {

    private String title, content, name;
    long time;
    private int process, credit, unlockSchoolNum;
    private boolean pop;
    
    public int getUnlockSchoolNum() {
        return unlockSchoolNum;
    }

    public void setUnlockSchoolNum(int unlockSchoolNum) {
        this.unlockSchoolNum = unlockSchoolNum;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProcess() {
        return process;
    }

    public void setProcess(int process) {
        this.process = process;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public boolean isPop() {
        return pop;
    }

    public void setPop(boolean pop) {
        this.pop = pop;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public static PushTaskMessage fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }

        PushTaskMessage message = new PushTaskMessage();
        message.content = json.optString("content");
        message.title = json.optString("title");
        message.time = json.optLong("time") * 1000;
        message.pop = json.optBoolean("pop");
        message.name = json.optString("task_name");
        message.process = json.optInt("process");
        message.credit = json.optInt("credit");
        message.unlockSchoolNum = json.optInt("arg");

        return message;
    }

}
