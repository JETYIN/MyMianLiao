package com.tjut.mianliao.data;

import org.json.JSONObject;

import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.CfReply;
import com.tjut.mianliao.util.JsonUtil;

public class InboxMessage {

    public static final int REPLY_POST = 6;
    public static final int REPLY_COMMENT = 18;
    public static final int LIKE_POST = 19;
    public static final int HATE_POST = 20;
    public static final int LIKE_COMMENT = 21;
    public static final int COMMENT_FORUM_THREAD_AT = 10; //   comment replied at
    public static final int COMMENT_FORUM_REPLY_AT = 11;
    public static final int COMMENT_REPLIED_AT = 23;
    public static final int POST_STICKLVL_DEL_BANNED = 28;
    
    public int messageType;

    public CfPost targetPost;

    public CfReply targetReply, reply;

    public InboxMessage() {
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public CfPost getTargetPost() {
        return targetPost;
    }

    public void setTargetPost(CfPost targetPost) {
        this.targetPost = targetPost;
    }

    public CfReply getTargetReply() {
        return targetReply;
    }

    public void setTargetReply(CfReply targetReply) {
        this.targetReply = targetReply;
    }

    public CfReply getReply() {
        return reply;
    }

    public void setReply(CfReply reply) {
        this.reply = reply;
    }

    public static InboxMessage fromJson(JSONObject json) {
        InboxMessage message = new InboxMessage();
        message.messageType = json.optInt("message_type");

        JSONObject replyObject = json.optJSONObject("reply");
        if (replyObject != null) {
            message.reply = CfReply.fromJson(replyObject);
        }

        JSONObject replyTargetObject = json.optJSONObject("target_reply");
        if (replyTargetObject != null) {
            message.targetReply = CfReply.fromJson(replyTargetObject);
        }

        JSONObject threaTargetObject = json.optJSONObject("target_thread");
        if (threaTargetObject != null) {
            message.targetPost = CfPost.fromJson(threaTargetObject);
        }

        return message;

    }

    public static final JsonUtil.ITransformer<InboxMessage> TRANSFORMER =
            new JsonUtil.ITransformer<InboxMessage>() {
        @Override
        public InboxMessage transform(JSONObject json) {
            return fromJson(json);
        }
    };

}
