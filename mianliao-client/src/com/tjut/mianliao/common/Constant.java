package com.tjut.mianliao.common;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.tjut.mianliao.MianLiaoApp;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.util.Utils;

@SuppressLint("SimpleDateFormat")
public class Constant {
    
    public static final String SP_FRESH_TIME = "sp_fresh_time";
    /**趣拍key、参数**/
    public static final String APP_KEY = "20713707870378e";
    public static final String APP_SECRET = "f363e3832846473baf6feebb27eb7402";
    /**上传视频时创建的文件夹名**/
    public static final String space = UUID.randomUUID().toString().replace("-","");
    /**进行鉴权操作后保存的token**/
    public static String accessToken;
    /**
     * 默认时长
     */
    public static  int DEFAULT_DURATION_LIMIT = 600;
    /**最小时长**/
    public static  float DEFAULT_MIN_DURATION_LIMIT = 2;
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
    
    /**
     * 自动获取最新帖子数量默认延时时长
     */
    public static int DEFAULT_REFRESH_TIME_DELAY = 1000 * 60 * 5; // 5Min
    
    /**
     * 自动获取最新帖子数量默认最小延时时长
     */
    public static int DEFAULT_REFRESH_TIME_DELAY_MIN = 1000 * 60 * 2; // 2Min
    

    public static final int COLOR_SUPER_MODERATOR = 0xffc731ff;
    
    public static final int COLOR_MODERATOR = 0xff00c6ff;
    
    public static final int COLOR_NORMAL = 0xff515151;

    public static int getFreshTimeDelay(Context context){
        SharedPreferences preferences = DataHelper.getSpForData(context);
        int freshTime = preferences.getInt(SP_FRESH_TIME, DEFAULT_REFRESH_TIME_DELAY);
        freshTime = freshTime <= DEFAULT_REFRESH_TIME_DELAY_MIN ? DEFAULT_REFRESH_TIME_DELAY_MIN : freshTime;
        return freshTime;
    }
    
    private static String getVideoPath() {
    	String basePath = null;
    	String account=null;
    	String timeNow=null;
        /**mfile创建在硬盘上的MianLiao文件夹**/
		File mFile = Utils.getMianLiaoDir();
		if (mFile != null) {
			basePath = mFile.getAbsolutePath();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
			account = AccountInfo.getInstance(MianLiaoApp.getAppContext())
					.getAccount();
            /**在MianLiao文件下创建文件名为用户的文件夹**/
			File file = new File(basePath + "/" + account);
			if (!file.exists()) {
				file.mkdirs();
			}
			timeNow = sdf.format(new Date());
    		
    	}
    	
		return basePath + "/" + account + "/" + timeNow;
    }

    public static final String getVideoPath(String filePath) {
        return BASE_VIDEOPATH + Utils.getFilePostfix(filePath);
    }
    
    public static final String getThumPath(String imagePath) {
        return BASE_VIDEOPATH + Utils.getFilePostfix(imagePath);
    }
}
