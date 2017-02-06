package com.tjut.mianliao.data;

import org.json.JSONObject;

import com.tjut.mianliao.util.JsonUtil;

public class HighTheme {

    private int index;
    private int id;
    private String name, intro, icon;
    private boolean isViewMoveFinished;
    public static final String BUNDLE_EXTRA = "bundle_high_theme";
    public static final String BUNDLE_EXTRA_ROOM_NAME = "bundle_high_room_name";

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isViewMoveFinished() {
        return isViewMoveFinished;
    }

    public void setViewMoveFinished(boolean isViewMoveFinished) {
        this.isViewMoveFinished = isViewMoveFinished;
    }

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

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public static HighTheme fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }

        HighTheme theme = new HighTheme();
        theme.setId(json.optInt("id"));
        theme.setName(json.optString("name"));
        theme.setIcon(json.optString("icon"));
        theme.setIntro(json.optString("intro"));

        return theme;

    }

    public static final JsonUtil.ITransformer<HighTheme> TRANSFORMER = new JsonUtil.ITransformer<HighTheme>() {
        @Override
        public HighTheme transform(JSONObject json) {
            return fromJson(json);
        }
    };
}
