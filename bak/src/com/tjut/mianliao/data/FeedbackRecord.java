package com.tjut.mianliao.data;

import org.json.JSONObject;

import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.JsonUtil;

public class FeedbackRecord {

    public static final String TIME = "time";
    public static final String IMAGE = "image";
    public static final String CONTENT = "content";
    public static final String IS_REPLY = "is_reply";

    public long time;
    public String image;
    public String thumb;
    public String content;
    public boolean isReply;

    public static FeedbackRecord fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        FeedbackRecord record = new FeedbackRecord();
        record.time = json.optLong(TIME) * 1000;
        record.image = json.optString(IMAGE);
        record.thumb = AliImgSpec.FEEDBACK_THUMB.makeUrl(record.image);
        record.content = json.optString(CONTENT);
        record.isReply = json.optInt(IS_REPLY) == 1;
        return record;
    }

    public static final JsonUtil.ITransformer<FeedbackRecord> TRANSFORMER =
            new JsonUtil.ITransformer<FeedbackRecord>() {
                @Override
                public FeedbackRecord transform(JSONObject json) {
                    return fromJson(json);
                }
            };
}
