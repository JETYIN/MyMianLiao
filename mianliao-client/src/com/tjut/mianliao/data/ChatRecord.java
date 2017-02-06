package com.tjut.mianliao.data;

import java.io.File;

import android.text.TextUtils;

import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.FileDownloader;

public class ChatRecord implements FileDownloader.Callback {
    public static final String TABLE_NAME = "chat_record";

    public static final String ID = "_id";
    public static final String TARGET = "target";
    public static final String FROM = "from_id";
    public static final String TIMESTAMP = "timestamp";
    public static final String TYPE = "typ_id";
    public static final String MESSAGE = "message";
    public static final String TEXT = "text";
    public static final String DISABLE_EMO = "disable_emo";
    /* @Deprecated */public static final String PICTURE = "picture";
    public static final String VOICE_LENGTH = "voice_length";
    /* @Deprecated */public static final String VOICE_FILE = "voice_file";
    /* @Deprecated */public static final String CALL_INFO = "call_info";
    public static final String FILE_PATH = "file_path";
    public static final String FILE_KEY = "file_key";
    public static final String URL = "url";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String ADDRESS = "address";
    public static final String ISGROUPCHAT = "isGroupChat";
    public static final String MSG_TYPE = "msgType";
    public static final String IS_NIGHT_RECORD = "isNightRecord";
    public static final String PUBLIC_ID = "public_id";
    public static final String GROUP_ID = "group_id";

    public static final int CHAT_TYPE_TIME = -1;
    public static final int CHAT_TYPE_TEXT = 0;
    public static final int CHAT_TYPE_PICTURE = 1;
    public static final int CHAT_TYPE_VOICE = 2;
    public static final int CHAT_TYPE_LOCATION = 3;
    public static final int CHAT_TYPE_SHARE_REQUEST = 4;
    public static final int CHAT_TYPE_SHARE_RESPONSE = 5;
    public static final int CHAT_TYPE_SHARE_OVER = 6;
    public static final int CHAT_TYPE_BIG_EMOTION = 7;

    public static final int CHAT_TYPE_DANMU_MSG = 20; // 直播弹幕消息
    public static final int CHAT_TYPE_CHAT_MSG = 21; // 直播普通消息
    public static final int CHAT_TYPE_SPECIAL_GIFT = 22; // 直播特殊礼物消息
    public static final int CHAT_TYPE_NORMAL_GIFT = 23; // 直播普通礼物消息
    public static final int CHAT_TYPE_PRAISE_MSG = 24; // 直播点赞消息
    public static final int CHAT_TYPE_FOLLOW_MSG = 25; // 直播关注主播消息
    public static final int CHAT_TYPE_SHARE_MSG = 26; // 直播分享成功消息
    public static final int CHAT_TYPE_SYS_MSG = 27; // 系统公告/通知消息
    public static final int CHAT_TYPE_COMEIN_MSG = 28; // XX人进入直播间消息
    public static final int CHAT_TYPE_EXIT_MSG = 29; // XX人进入直播间消息
    public static final int CHAT_TYPE_CLOSE_CONNECTION = 31;// XX关闭直播连线；
    public static final int CHAT_TYPE_SEND_CONNECTION_REQUEST = 32; // XX发送连接请求
    public static final int CHAT_TYPE_CANCEL_CONNECTION_REQUEST = 33; // XX取消连接请求
    public static final int CHAT_TYPE_CONNECTION_SUCCESS = 34; // XX连线
    public static final int CHAT_TYPE_SET_MAGEMENT = 35; // XX设为管理员
    public static final int CHAT_TYPE_SILIENCE = 36; // XX被禁言
    public static final int CHAT_TYPE_CANCEL_SILENCE = 37; // XX解除禁言
    public static final int CHAT_TYPE_CANCEL_SET_MAGEMENT = 38; // XX删除管理员

    public static final int MSG_TYPE_CHAT = 0;
    public static final int MSG_TYPE_ADD_FRIENDS_REQUEST = 1;
    public static final int MSG_TYPE_SYS = 2;
    public static final int MSG_TYPE_PUBLIC_NUMBER = 3;
    public static final int MSG_TYPE_HIGH_CHAT = 4;
    public static final int MSG_TYPE_AT_REPLEY_COMMENT = 5;
    public static final int MSG_TYPE_UP_DOWN = 6;

    public static final int MSG_ANIM_TYPE_NORMAL = 1;
    public static final int MSG_ANIM_TYPE_SPECIAL= 2;
    public static final int MSG_ANIM_TYPE_FIRE = 3;

    public long id;
    public String target;
    public String from;
    public long timestamp;
    public int type;
    public String text;
    public boolean disableEmo;
    public int voiceLength;
    public String filePath;
    public String fileKey;
    public String url;
    public double longitude;
    public double latitude;
    public String address;
    public int msgType;
    public boolean isNightRecord;
    public long publicId;
    public String groupId;
        
    public boolean voicePlaying;
    public boolean isGroupChat;
    public boolean isChatState;
    public boolean isComposing;
    public boolean isComposingVoice;

    // add by 4.1.0
    public int giftId;
    public int animType;
    public int giftCount;
    public String activityId;

    public int silenceUserId;

    public static ChatRecord createTimeRecord(long timestamp) {
        ChatRecord record = new ChatRecord();
        record.type = CHAT_TYPE_TIME;
        record.timestamp = timestamp;
        return record;
    }

    public boolean isFrom(String account) {
        return TextUtils.equals(from, account);
    }

    public boolean isTarget(String account) {
        return TextUtils.equals(target, account);
    }

    public boolean isFileExist() {
        return hasFilePath() && new File(filePath).isFile();
    }

    public boolean hasId() {
        return id > 0;
    }

    public boolean hasFilePath() {
        return !TextUtils.isEmpty(filePath);
    }

    public boolean hasUrl() {
        return !TextUtils.isEmpty(url);
    }

    public boolean needUpload() {
        return isFileExist() && !hasUrl();
    }

    public boolean needDownload() {
        return hasUrl() && !isFileExist();
    }

    public String getThumbUrl() {
        return AliImgSpec.CHAT_THUMB.makeUrl(url);
    }

    @Override
    public void onResult(boolean success, String url, String fileName) {
        if (success && TextUtils.equals(this.url, url)) {
            this.filePath = fileName;
        }
    }

}
