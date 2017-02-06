package com.tjut.mianliao.promotion;

import org.json.JSONObject;

import android.text.TextUtils;

public class Promotion {

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String HAS_INFO = "has_info";
    private static final String SPLASH_IMAGE = "splash_image";
    private static final String BANNER_IMAGE = "banner_image";

    public int id;
    public String name;
    public boolean hasInfo;
    public String splashImage;
    public String bannerImage;

    public String splashImgFile;
    public String bannerImgFile;

    public boolean isImageReady() {
        return !TextUtils.isEmpty(splashImgFile) &&
                (TextUtils.isEmpty(bannerImage) || !TextUtils.isEmpty(bannerImgFile));
    }

    public boolean isBannerReady() {
        return !TextUtils.isEmpty(bannerImgFile);
    }

    public static Promotion fromJson(JSONObject json) {
        if (json == null || json.optInt(ID) == 0) {
            return null;
        }
        Promotion prom = new Promotion();
        prom.id = json.optInt(ID);
        prom.name = json.optString(NAME);
        prom.hasInfo = json.optInt(HAS_INFO) == 1;
        prom.splashImage = json.optString(SPLASH_IMAGE);
        prom.bannerImage = json.optString(BANNER_IMAGE);

        return prom;
    }
}
