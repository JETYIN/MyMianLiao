package com.tjut.mianliao.data.push;

import org.json.JSONObject;

import com.tjut.mianliao.util.JsonUtil;

public class PushMessage {

    public static final int ALL_TASKS_FINISHED = 5;
    public static final int SINGLE_TASK_FINISHED = 2;
    public static final int FROM_DAY_TO_NIGHT = 3;
    public static final int FROM_NIGHT_TO_DAY = 4;
    public static final int MESSAGE_REMIND = 6;
    public static final int PUBLIC_NUMBER = 7;
    public static final int MESSAGE_REMIND_NO_INBOX = 8;
    public static final int SYSTEM_NOTICE_INFO = 9;

    public static final int MSG_TYPE_POST_RECOMMEND = 24;
    public static final int MSG_TYPE_WEB_ADV = 25;
    public static final int MSG_TYPE_TRIBE_RECOMMEND = 26;
    public static final int MSG_TYPE_TOPIC_RECOMMEND = 27;
    public static final int MSG_TYPE_LIVE_CONNECTION_REQUEST = 29;

    private int category;

    private PushTaskMessage pushTaskMessage;

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public PushTaskMessage getPushTaskMessage() {
        return pushTaskMessage;
    }

    public void setPushTaskMessage(PushTaskMessage pushTaskMessage) {
        this.pushTaskMessage = pushTaskMessage;
    }

    public static PushMessage fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }

        PushMessage message = new PushMessage();
        message.category = json.optInt("category");
        switch (message.category) {
            case ALL_TASKS_FINISHED:
            case SINGLE_TASK_FINISHED:
            case FROM_DAY_TO_NIGHT:
            case FROM_NIGHT_TO_DAY:
            case SYSTEM_NOTICE_INFO:
            case PUBLIC_NUMBER:{
                message.pushTaskMessage = PushTaskMessage.fromJson(json.optJSONObject("data"));
            }
                break;
        }

        return message;
    }

    public static final JsonUtil.ITransformer<PushMessage> TRANSFORMER
            = new JsonUtil.ITransformer<PushMessage>() {

        @Override
        public PushMessage transform(JSONObject json) {
            return fromJson(json);
        }
    };

}
