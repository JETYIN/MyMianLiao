package com.tjut.mianliao.util;

import android.text.TextUtils;

/**
 * Helper class to build image url depends on Ali image service.
 */
public class AliImgSpec {

    private static final String TAG = "AliImgSpec";

    public static final int sDisplayWidth = Utils.getDisplayWidth();
    public static final int sDisplayHeight = Utils.getDisplayHeight();
    public static final int sDisplayCellPlexls = Utils.getDisplayCellPixels();
    
    public static final AliImgSpec USER_AVATAR = new AliImgSpec(150, 150, true);

    public static final AliImgSpec POST_BANNER = new AliImgSpec(600, 300, true);
//    public static final AliImgSpec POST_PHOTO = new AliImgSpec((int) (350 * 1.5), (int) (250 * 1.5), false);
    public static final AliImgSpec POST_PHOTO = new AliImgSpec(sDisplayWidth, sDisplayCellPlexls * 240, false);
    public static final AliImgSpec POST_THUMB = new AliImgSpec(250, 350, false);
    public static final AliImgSpec POST_THUMB_SQUARE = new AliImgSpec(600, 600, true);
    public static final AliImgSpec TAKE_NOTE_IMAGE = new AliImgSpec(50, 60, true);
    public static final AliImgSpec FORUM_BG = new AliImgSpec(300, 100, true);
    public static final AliImgSpec FORUM_ICON = USER_AVATAR;

    public static final AliImgSpec QA_THUMB = POST_THUMB;

    public static final AliImgSpec CORP_AVATAR = USER_AVATAR;

    public static final AliImgSpec NEWS_AVATAR = USER_AVATAR;
    public static final AliImgSpec NEWS_THUMB = new AliImgSpec(200, 200, true);
    public static final AliImgSpec NEWS_COVER = new AliImgSpec(600, 300, true);

    public static final AliImgSpec FEEDBACK_THUMB = new AliImgSpec(225, 150, true);

    public static final AliImgSpec CHAT_THUMB = new AliImgSpec(300, 200, false);

    public static final String EXT_JPG = ".jpg";
    private static final String SUFFIX_CUT = "70q_1e_1c" + EXT_JPG;
    private static final String SUFFIX_NORMAL = "70q" + EXT_JPG;
    private static final float MAX_WH_RATIO = 1.5f;

    public int width;
    public int height;
    public boolean fixedSize;

    private AliImgSpec(int width, int height, boolean fixedSize) {
        this.width = width;
        this.height = height;
        this.fixedSize = fixedSize;
    }

    public String makeUrl(String src) {
        if (TextUtils.isEmpty(src)) {
            return null;
        }
        int index = src.indexOf("@");
        String baseUrl = index > 0 ? src.substring(0, index) : src;
        int srcW = 0, srcH = 0;
        if (!fixedSize && baseUrl.indexOf("w_") > 0
                && baseUrl.indexOf("h_") > 0 && baseUrl.indexOf("/") > 0) {
            try {
                srcW = Integer.parseInt(baseUrl.substring(baseUrl.lastIndexOf("/") + 1,
                        baseUrl.indexOf("w_")));
                srcH = Integer.parseInt(baseUrl.substring(baseUrl.indexOf("w_") + 2,
                        baseUrl.indexOf("h_")));
                if (srcW > 0 && srcH > 0 && srcW < width && srcH < height) {
                    if (((float) srcH / (float) srcW) < MAX_WH_RATIO) {
                        return baseUrl;
                    } else {
                        return baseUrl + "@60p_" + srcW + "w_" + (int) (srcW * MAX_WH_RATIO)
                                + "h_" + SUFFIX_CUT;
                    }
                }
            } catch (NumberFormatException e) {
                Utils.logD(TAG, e.getMessage());
            }
        }
        if (fixedSize || srcH > srcW) {
            return baseUrl + "@60p_" + width + "w_" + height + "h_" + SUFFIX_CUT;
        } else {
            return baseUrl + "@60p_" + width + "w_" + height + "h_" + SUFFIX_NORMAL;
        }
    }
    
    public String makeUrlSingleImg(String src) {
        return src + "@"+ width + "x" + height + "-5rc_70q.jpg";
    }
    
    public String makeUrlSingleImg(String src, int quaity, int pre) {
        return src + "@" + width + "x" + height + "-5rc_" + quaity + "q_" + pre + "p.jpg";
    }
}
