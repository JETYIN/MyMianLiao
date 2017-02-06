package com.tjut.mianliao.points;

import org.json.JSONObject;

import com.tjut.mianliao.R;
import com.tjut.mianliao.util.JsonUtil;

public class Quest {

    private static final String EVENT = "event";
    private static final String SCORE = "score";
    private static final String DONE = "done";
    private static final String QUOTA = "quota";

    public String event;
    public int score;
    public int done;
    public int quota;

    public static Quest fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        Quest quest = new Quest();
        quest.event = json.optString(EVENT);
        quest.score = json.optInt(SCORE);
        quest.done = json.optInt(DONE);
        quest.quota = json.optInt(QUOTA);

        return quest;
    }

    public int getQuestIcon() {
        return done == quota ? R.drawable.ic_quest_done : R.drawable.ic_quest;
    }

    public static final JsonUtil.ITransformer<Quest> TRANSFORMER =
            new JsonUtil.ITransformer<Quest>() {
        @Override
        public Quest transform(JSONObject json) {
            return fromJson(json);
        }
    };
}
