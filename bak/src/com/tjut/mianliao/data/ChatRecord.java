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

    public static final int CHAT_TYPE_TIME = -1;
    public static final int CHAT_TYPE_TEXT = 0;
    public static final int CHAT_TYPE_PICTURE = 1;
    public static final int CHAT_TYPE_VOICE = 2;
    public static final int CHAT_TYPE_LOCATION = 3;
    public static final int CHAT_TYPE_SHARE_REQUEST = 4;
    public static final int CHAT_TYPE_SHARE_RESPONSE = 5;
    public static final int CHAT_TYPE_SHARE_OVER = 6;
    public static final int CHAT_TYPE_BIG_EMOTION = 7;

    public static final int MSG_TYPE_CHAT = 0;
    public static final int MSG_TYPE_ADD_FRIENDS_REQUEST = 1;
    public static final int MSG_TYPE_SYS = 2;
    public static final int MSG_TYPE_PUBLIC_NUMBER = 3;
    public static final int MSG_TYPE_HIGH_CHAT = 4;
    public static final int MSG_TYPE_AT_REPLEY_COMMENT = 5;
    public static final int MSG_TYPE_UP_DOWN = 6;

    public long id;
    public String target;
    public String from;
    public long timestamp;
    public int type;
    public String text;
    public boolean disableEmo;
    public float voiceLength;
    public String filePath;
    public String fileKey;
    public String url;
    public double longitude;
    public double latitude;
    public String address;
    public int msgType;
    public boolean isNightRecord;
    public long publicId;
        
    public boolean voicePlaying;
    public boolean isGroupChat;
    public boolean isChatState;
    public boolean isComposing;
    public boolean isComposingVoice;

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
