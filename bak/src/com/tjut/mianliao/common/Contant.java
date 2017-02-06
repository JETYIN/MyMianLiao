package com.tjut.mianliao.common;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import com.tjut.mianliao.MianLiaoApp;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.util.Utils;

@SuppressLint("SimpleDateFormat")
public class Contant {

    /**
     * 默认时长
     */
    public static  int DEFAULT_DURATION_LIMIT = 20;
    /**
     * 默认码率
     */
    public static  int DEFAULT_BITRATE =2000 * 1000;
    /**
     * 默认Video保存路径
     */
    public static String VIDEOPATH = getVideoPath() + ".mp4";
    
    public static  String BASE_VIDEOPATH = getVideoPath();
    /**
     * 默认缩略图保存路径
     */
    public static  String THUMBPATH =  BASE_VIDEOPATH + ".jpg";
    /**
     * 水印本地路径，文件必须为rgba格式的PNG图片
     */
    public static  String WATER_MARK_PATH ="assets://Qupai/watermark/mianliao_log.png";
    
    private static String getVideoPath() {
        String basePath = Utils.getMianLiaoDir().getAbsolutePath();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String account = AccountInfo.getInstance(MianLiaoApp.getAppContext()).getAccount();
        File file = new File(basePath + "/" + account);
        if (!file.exists()) {
            file.mkdirs();
        }
        String timeNow = sdf.format(new Date());
        return basePath + "/" + account + "/" + timeNow;
    }
    
    public static final String getVideoPath(String filePath) {
        return BASE_VIDEOPATH + Utils.getFilePostfix(filePath);
    }
    
    public static final String getThumPath(String imagePath) {
        return BASE_VIDEOPATH + Utils.getFilePostfix(imagePath);
    }
}
