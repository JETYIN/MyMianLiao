package com.tjut.mianliao.data.explore;

public class EmotionsInfo {

    public static final String TABLE_NAME = "emotion_info";

    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String URL = "url";
    public static final String ZIP_PATH = "zipPath";
    public static final String PATH = "path";
    public static final String IS_USING = "isUsing";

    public long id;
    public String name;
    public String url;
    public String zipPath;
    public String path;
    public boolean isUsing;

    public EmotionsInfo() {
    }

    public EmotionsInfo(String name, String url, String zipPath, String path, boolean isUsing) {
        this.name = name;
        this.url = url;
        this.zipPath = zipPath;
        this.path = path;
        this.isUsing = isUsing;
    }
}
