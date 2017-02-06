package com.tjut.mianliao.data;

import org.json.JSONObject;

import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.CfReply;
import com.tjut.mianliao.util.JsonUtil;

public class MyReplyInfo {
    
    public CfPost targetPost;
    public CfReply targetReply, reply;

    
    public MyReplyInfo() {
    }
    
    public static MyReplyInfo fromJson(JSONObject json) {
        
        MyReplyInfo myReplyInfo = new MyReplyInfo();
        myReplyInfo.reply = CfReply.fromJson(json);

        JSONObject replyTargetObject = json.optJSONObject("target_reply");
        if (replyTargetObject != null) {
            myReplyInfo.targetReply = CfReply.fromJson(replyTargetObject);
        }

        JSONObject threaTargetObject = json.optJSONObject("target_thread");
        if (threaTargetObject != null) {
            myReplyInfo.targetPost = CfPost.fromJson(threaTargetObject);
        }

        return myReplyInfo;

    }
    
    
    public static final JsonUtil.ITransformer<MyReplyInfo> TRANSFORMER =
            new JsonUtil.ITransformer<MyReplyInfo>() {
        @Override
        public MyReplyInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };
    
    public CfReply getReply() {
        return reply;
    }
    
    public CfPost getTargetPost() {
        return targetPost;
    }
    public CfReply getTargetReply() {
        return targetReply;
    }
}
