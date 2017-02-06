package com.tjut.mianliao.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tjut.mianliao.chat.CacheNewsInfo;
import com.tjut.mianliao.data.cache.CacheBannerInfo;
import com.tjut.mianliao.data.cache.CacheChannelInfo;
import com.tjut.mianliao.data.cache.CacheImageInfo;
import com.tjut.mianliao.data.cache.CachePostInfo;
import com.tjut.mianliao.data.cache.CacheRoamSchoolInfo;
import com.tjut.mianliao.data.cache.CacheUserInfo;
import com.tjut.mianliao.data.cache.CacheVoteInfo;
import com.tjut.mianliao.data.contact.UserEntry;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.explore.EmotionsInfo;
import com.tjut.mianliao.data.mycollege.MatchJobInfo;
import com.tjut.mianliao.data.mycollege.SearchTagInfo;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

    protected static final String DB_NAME = "tjutim";
    private static final int DB_VERSION = 18; 

    private static final String CREATE_COURSE = new StringBuilder("CREATE TABLE ")
            .append(Course.TABLE_NAME).append(" (")
            .append(Course.COURSE_ID).append(" INTEGER,")
            .append(Course.SEMESTER).append(" INTEGER,")
            .append(Course.AUTHED).append(" INTEGER,")
            .append(Course.AUTH_USER).append(" INTEGER,")
            .append(Course.NAME).append(" VARCHAR(100),")
            .append(Course.TEACHER).append(" VARCHAR(100),")
            .append(Course.Entry.CLASSROOM).append(" VARCHAR(100),")
            .append(Course.Entry.WEEKDAY).append(" INTEGER,")
            .append(Course.Entry.PERIOD_START).append(" INTEGER,")
            .append(Course.Entry.PERIOD_END).append(" INTEGER,")
            .append(Course.Entry.WEEKS).append(" INTEGER")
            .append(")").toString();

    private static final String CREATE_USERENTRY = new StringBuilder("CREATE TABLE ")
            .append(UserEntry.TABLE_NAME).append(" (")
            .append(UserEntry.JID).append(" VARCHAR(255),")
            .append(UserEntry.SUB_TYPE).append(" VARCHAR(20),")
            .append(UserEntry.SUB_STATUS).append(" VARCHAR(20)")
            .append(")").toString();

    private static final String CREATE_USERINFO = new StringBuilder("CREATE TABLE ")
            .append(UserInfo.TABLE_NAME).append(" (")
            .append(UserInfo.USER_ID).append(" INTEGER,")
            .append(UserInfo.GUID).append(" CHAR(32),")
            .append(UserInfo.ACCOUNT).append(" VARCHAR(255),")
            .append(UserInfo.TYPE).append(" INTEGER,")
            .append(UserInfo.IDS).append(" INTEGER,")
            .append(UserInfo.NAME).append(" VARCHAR(255),")
            .append(UserInfo.NICKNAME).append(" VARCHAR(255),")
            .append(UserInfo.PHONE).append(" VARCHAR(50),")
            .append(UserInfo.EMAIL).append(" VARCHAR(100),")
            .append(UserInfo.EMAIL_VERIFIED).append(" INTEGER,")
            .append(UserInfo.GENDER).append(" INTEGER,")
            .append(UserInfo.SHORT_DESC).append(" VARCHAR(255),")
            .append(UserInfo.AVATAR).append(" VARCHAR(255),")
            .append(UserInfo.AVATAR_FULL).append(" VARCHAR(255),")
            .append(UserInfo.BG_IMG).append(" VARCHAR(255),")
            .append(UserInfo.PRIMARY_BADGE).append(" INTEGER,")
            .append(UserInfo.PRIMARY_BADGE_IMAGE).append(" VARCHAR(255),")
            .append(UserInfo.SCHOOL_ID).append(" INTEGER,")
            .append(UserInfo.SCHOOL).append(" VARCHAR(255),")
            .append(UserInfo.DEPARTMENT_ID).append(" INTEGER,")
            .append(UserInfo.DEPARTMENT).append(" VARCHAR(255),")
            .append(UserInfo.START_YEAR).append(" VARCHAR(255),")
            .append(UserInfo.FACE_ID).append(" VARCHAR(255),")
            .append(UserInfo.LAST_UPDATE).append(" VARCHAR(255)")
            .append(")").toString();

    private static final String CREATE_CHATRECORD = new StringBuilder("CREATE TABLE ")
            .append(ChatRecord.TABLE_NAME).append(" (")
            .append(ChatRecord.ID).append(" INTEGER PRIMARY KEY ASC,")
            .append(ChatRecord.TARGET).append(" VARCHAR(255),")
            .append(ChatRecord.FROM).append(" VARCHAR(255),")
            .append(ChatRecord.TIMESTAMP).append(" INTEGER,")
            .append(ChatRecord.TYPE).append(" INTEGER,")
            .append(ChatRecord.MESSAGE).append(" TEXT,")
            .append(ChatRecord.VOICE_LENGTH).append(" FLOAT,")
            .append(ChatRecord.VOICE_FILE).append(" VARCHAR(255),")
            .append(ChatRecord.PICTURE).append(" VARCHAR(255),")
            .append(ChatRecord.CALL_INFO).append(" VARCHAR(255),")
            .append(ChatRecord.DISABLE_EMO).append(" BOOLEAN,")
            .append(ChatRecord.FILE_PATH).append(" VARCHAR(255),")
            .append(ChatRecord.URL).append(" VARCHAR(255),")
            .append(ChatRecord.LONGITUDE).append(" DOUBLE,")
            .append(ChatRecord.LATITUDE).append(" DOUBLE,")
            .append(ChatRecord.ADDRESS).append(" VARCHAR(255),")
            .append(ChatRecord.ISGROUPCHAT).append(" BOOLEAN,")
            .append(ChatRecord.MSG_TYPE).append(" INTEGER,")
            .append(ChatRecord.PUBLIC_ID).append(" INTEGER,")
            .append(ChatRecord.IS_NIGHT_RECORD).append(" BOOLEAN,")
            .append(ChatRecord.GROUP_ID).append(" VARCHAR(255)")
            .append(")").toString();

    private static final String CREATE_GROUPINFO = new StringBuilder("CREATE TABLE ")
            .append(GroupInfo.TABLE_NAME).append(" (")
            .append(GroupInfo.ID).append(" INTEGER PRIMARY KEY ASC,")
            .append(GroupInfo.JID).append(" VARCHAR(255),")
            .append(GroupInfo.GROUP_NAME).append(" VARCHAR(255)")
            .append(")").toString();

    private static final String CREATE_EMOTION_INFO = new StringBuilder("CREATE TABLE ")
            .append(EmotionsInfo.TABLE_NAME).append(" (")
            .append(EmotionsInfo.ID).append(" INTEGER PRIMARY KEY ASC,")
            .append(EmotionsInfo.NAME).append(" VARCHAR(255),")
            .append(EmotionsInfo.URL).append(" VARCHAR(255),")
            .append(EmotionsInfo.ZIP_PATH).append(" VARCHAR(255),")
            .append(EmotionsInfo.PATH).append(" VARCHAR(255),")
            .append(EmotionsInfo.IS_USING).append(" BOOLEAN")
            .append(")").toString();

    private static final String CREATE_SEARCH_TAG_INFO = new StringBuilder("CREATE TABLE ")
            .append(SearchTagInfo.TABLE_NAME).append(" (")
            .append(SearchTagInfo.ID).append(" INTEGER PRIMARY KEY ASC,")
            .append(SearchTagInfo.TAGS).append(" VARCHAR(255),")
            .append(SearchTagInfo.TIME).append(" INTEGER,")
            .append(SearchTagInfo.TAG_STR).append(" VARCHAR(255),")
            .append(SearchTagInfo.TAG_ID).append(" VARCHAR(255)")
            .append(")").toString();

    private static final String CREATE_MATCH_JOB_INFO = new StringBuilder("CREATE TABLE ")
            .append(MatchJobInfo.TABLE_NAME).append(" (")
            .append(MatchJobInfo.ID).append(" INTEGER PRIMARY KEY ASC,")
            .append(MatchJobInfo.JOB_ID).append(" INTEGER,")
            .append(MatchJobInfo.JOB_TITLE).append(" VARCHAR(255),")
            .append(MatchJobInfo.PUBLISH_TIME).append(" INTEGER,")
            .append(MatchJobInfo.SALARY).append(" VARCHAR(255),")
            .append(MatchJobInfo.CROP_NAME).append(" VARCHAR(255),")
            .append(MatchJobInfo.LOCAL_CITY).append(" VARCHAR(255),")
            .append(MatchJobInfo.CROP_LOGO).append(" VARCHAR(255)")
            .append(")").toString();

    private static final String CREATE_CACHE_BANNER = new StringBuilder("CREATE TABLE ")
            .append(CacheBannerInfo.TABLE_NAME).append(" (")
            .append(CacheBannerInfo._ID).append(" INTEGER PRIMARY KEY ASC,")
            .append(CacheBannerInfo.ID).append(" INTEGER,")
            .append(CacheBannerInfo.PLATE).append(" INTEGER,")
            .append(CacheBannerInfo.IMAGE).append(" VARCHAR(255),")
            .append(CacheBannerInfo.TYPE).append(" INTEGER,")
            .append(CacheBannerInfo.DATA).append(" VARCHAR(255)")
            .append(")").toString();
    
    private static final String CREATE_CACHE_CHANNEL = new StringBuilder("CREATE TABLE ")
            .append(CacheChannelInfo.TABLE_NAME).append(" (")
            .append(CacheChannelInfo.ID).append(" INTEGER PRIMARY KEY ASC,")
            .append(CacheChannelInfo.FORUM_ID).append(" INTEGER,")
            .append(CacheChannelInfo.NAME).append(" VARCHAR(255),")
            .append(CacheChannelInfo.TYPE).append(" INTEGER,")
            .append(CacheChannelInfo.TITLE).append(" VARCHAR(255),")
            .append(CacheChannelInfo.INTRO).append(" VARCHAR(255),")
            .append(CacheChannelInfo.ICON).append(" VARCHAR(255),")
            .append(CacheChannelInfo.BG_IMG).append(" VARCHAR(255),")
            .append(CacheChannelInfo.RULE_ICON).append(" VARCHAR(255),")
            .append(CacheChannelInfo.RULE_TITLE).append(" VARCHAR(255),")
            .append(CacheChannelInfo.RULE_CONTENT).append(" VARCHAR(255),")
            .append(CacheChannelInfo.THREAD_TYPE).append(" INTEGER,")
            .append(CacheChannelInfo.HAVA_RULE).append(" BOOLEAN,")
            .append(CacheChannelInfo.STYLE).append(" INTEGER,")
            .append(CacheChannelInfo.DAY_TYPE).append(" INTEGER")
            .append(")").toString();
    
    private static final String CREATE_CACHE_NEWS = new StringBuilder("CREATE TABLE ")
        .append(CacheNewsInfo.TABLE_NAME).append(" (")
        .append(CacheNewsInfo._ID).append(" INTEGER PRIMARY KEY ASC,")
        .append(CacheNewsInfo.ID).append(" INTEGER,")
        .append(CacheNewsInfo.URL).append(" VARCHAR(255),")
        .append(CacheNewsInfo.CONTENT_URL).append(" VARCHAR(255),")
        .append(CacheNewsInfo.PUBLIC_URL).append(" VARCHAR(255),")
        .append(CacheNewsInfo.TITLE).append(" VARCHAR(255),")
        .append(CacheNewsInfo.SUMMARY).append(" VARCHAR(255),")
        .append(CacheNewsInfo.COVER).append(" VARCHAR(255),")
        .append(CacheNewsInfo.THUMBNAIL).append(" VARCHAR(255),")
        .append(CacheNewsInfo.CREATE_TIME).append(" DOUBLE,")
        .append(CacheNewsInfo.SOURCE_ID).append(" INTEGER,")
        .append(CacheNewsInfo.SOURCE_NAME).append(" VARCHAR(255),")
        .append(CacheNewsInfo.FAVORITE).append(" BOOLEAN,")
        .append(CacheNewsInfo.LIKED).append(" BOOLEAN,")
        .append(CacheNewsInfo.LIKED_COUNT).append(" INTEGER,")
        .append(CacheNewsInfo.COMMENTED_COUNT).append(" INTEGER,")
        .append(CacheNewsInfo.TYPE).append(" INTEGER")
        .append(")").toString();
    
    private static final String CREATE_CACHE_IMAGE = new StringBuilder("CREATE TABLE ")
            .append(CacheImageInfo.TABLE_NAME).append(" (")
            .append(CacheImageInfo.ID).append(" INTEGER PRIMARY KEY ASC,")
            .append(CacheImageInfo.COUNT).append(" INTEGER,")
            .append(CacheImageInfo.URL1).append(" VARCHAR(255),")
            .append(CacheImageInfo.URL2).append(" VARCHAR(255),")
            .append(CacheImageInfo.URL3).append(" VARCHAR(255),")
            .append(CacheImageInfo.URL4).append(" VARCHAR(255),")
            .append(CacheImageInfo.URL5).append(" VARCHAR(255),")
            .append(CacheImageInfo.URL6).append(" VARCHAR(255),")
            .append(CacheImageInfo.URL7).append(" VARCHAR(255),")
            .append(CacheImageInfo.URL8).append(" VARCHAR(255),")
            .append(CacheImageInfo.URL9).append(" VARCHAR(255)")
            .append(")").toString();
    
    private static final String CREATE_CACHE_SCHOOL = new StringBuilder("CREATE TABLE ")
            .append(CacheRoamSchoolInfo.TABLE_NAME).append(" (")
            .append(CacheRoamSchoolInfo.ID).append(" INTEGER PRIMARY KEY ASC,")
            .append(CacheRoamSchoolInfo.SCHOOL_ID).append(" INTEGER,")
            .append(CacheRoamSchoolInfo.AREA_ID).append(" INTEGER,")
            .append(CacheRoamSchoolInfo.NAME).append(" VARCHAR(255),")
            .append(CacheRoamSchoolInfo.PINYIN).append(" VARCHAR(255),")
            .append(CacheRoamSchoolInfo.ABBREVIATION).append(" VARCHAR(255),")
            .append(CacheRoamSchoolInfo.IS_COLLECTION).append(" BOOLEAN,")
            .append(CacheRoamSchoolInfo.UNLOCK).append(" BOOLEAN,")
            .append(CacheRoamSchoolInfo.VIP).append(" BOOLEAN")
            .append(")").toString();
    
    private static final String CREATE_CACHE_VOTE = new StringBuilder("CREATE TABLE ")
            .append(CacheVoteInfo.TABLE_NAME).append(" (")
            .append(CacheVoteInfo.ID).append(" INTEGER PRIMARY KEY ASC,")
            .append(CacheVoteInfo.COUNT).append(" INTEGER,")
            .append(CacheVoteInfo.OPT1).append(" VARCHAR(255),")
            .append(CacheVoteInfo.OPT2).append(" VARCHAR(255),")
            .append(CacheVoteInfo.OPT3).append(" VARCHAR(255),")
            .append(CacheVoteInfo.OPT4).append(" VARCHAR(255)")
            .append(")").toString();
    
    private static final String CREATE_CACHE_POST = new StringBuilder("CREATE TABLE ")
            .append(CachePostInfo.TABLE_NAME).append(" (")
            .append(CachePostInfo.ID).append(" INTEGER PRIMARY KEY ASC,")
            .append(CachePostInfo.POST_ID).append(" INTEGER,")
            .append(CachePostInfo.CONTENT).append(" VARCHAR(255),")
            .append(CachePostInfo.CREATED_ON).append(" INTEGER,")
            .append(CachePostInfo.REPLY_COUNT).append(" INTEGER,")
            .append(CachePostInfo.UP_COUNT).append(" INTEGER,")
            .append(CachePostInfo.DOWN_COUNT).append(" INTEGER,")
            .append(CachePostInfo.MY_UP).append(" BOOLEAN,")
            .append(CachePostInfo.MY_DOWN).append(" BOOLEAN,")
            .append(CachePostInfo.IMAGE_IDS).append(" VARCHAR(255),")
            .append(CachePostInfo.LIKED_USER_IDS).append(" VARCHAR(255),")
            .append(CachePostInfo.FORUM_ID).append(" INTEGER,")
            .append(CachePostInfo.PUBLIC_URL).append(" VARCHAR(255),")
            .append(CachePostInfo.TYPE).append(" INTEGER,")
            .append(CachePostInfo.THREAD_TYPE).append(" INTEGER,")
            .append(CachePostInfo.SAME_SCHOOL).append(" BOOLEAN,")
            .append(CachePostInfo.VOICE_LENGTH).append(" INTEGER,")
            .append(CachePostInfo.HOT).append(" BOOLEAN,")
            .append(CachePostInfo.READ_COUNT).append(" INTEGER,")
            .append(CachePostInfo.FORUM_TYPE).append(" INTEGER,")
            .append(CachePostInfo.STYLE).append(" INTEGER,")
            .append(CachePostInfo.VOICE_PATH).append(" VARCHAR(255),")
            .append(CachePostInfo.END_TIME).append(" INTEGER,")
            .append(CachePostInfo.ENABLED).append(" BOOLEAN,")
            .append(CachePostInfo.MY_VOTE).append(" VARCHAR(255),")
            .append(CachePostInfo.MY_VOTE_TIME).append(" INTEGER,")
            .append(CachePostInfo.OPTIONS).append(" VARCHAR(255),")
            .append(CachePostInfo.RESULT).append(" VARCHAR(255),")
            .append(CachePostInfo.USER_ID).append(" INTEGER,")
            .append(CachePostInfo.IS_NIGHT_POST).append(" BOOLEAN,")
            .append(CachePostInfo.SCHOOL_ID).append(" INTEGER,")
            .append(CachePostInfo.STICKLVL).append(" INTEGER,")
            .append(CachePostInfo.TITLE).append(" VARCHAR(255),") 
            .append(CachePostInfo.VIDEO_THUMBNAIL).append(" VARCHAR(255),") 
            .append(CachePostInfo.VIDEO_URL).append(" VARCHAR(255),") 
            .append(CachePostInfo.REPLY_TIME).append(" INTEGER,") 
            .append(CachePostInfo.MODERATOR_UIDS).append(" VARCHAR(255),") 
            .append(CachePostInfo.SUPER_MODERATOR_UIDS).append(" VARCHAR(255)") 
            .append(")").toString();
    
    private static final String CREATE_CACHE_USERINFO = new StringBuilder("CREATE TABLE ")
            .append(CacheUserInfo.TABLE_NAME).append(" (")
            .append(CacheUserInfo.ID).append(" INTEGER PRIMARY KEY ASC,")
            .append(CacheUserInfo.ACCOUNT).append(" VARCHAR(255),")
            .append(CacheUserInfo.TYPE).append(" INTEGER,")
            .append(CacheUserInfo.IDS).append(" INTEGER,")
            .append(CacheUserInfo.NAME).append(" VARCHAR(255),")
            .append(CacheUserInfo.NICKNAME).append(" VARCHAR(255),")
            .append(CacheUserInfo.GENDER).append(" INTEGER,")
            .append(CacheUserInfo.SHORT_DESC).append(" VARCHAR(255),")
            .append(CacheUserInfo.AVATAR).append(" VARCHAR(255),")
            .append(CacheUserInfo.AVATAR_FULL).append(" VARCHAR(255),")
            .append(CacheUserInfo.BG_IMG).append(" VARCHAR(255),")
            .append(CacheUserInfo.PRIMARY_BADGE_IMAGE).append(" VARCHAR(255),")
            .append(CacheUserInfo.SCHOOL_ID).append(" INTEGER,")
            .append(CacheUserInfo.SCHOOL).append(" VARCHAR(255),")
            .append(CacheUserInfo.DEPARTMENT_ID).append(" INTEGER,")
            .append(CacheUserInfo.DEPARTMENT).append(" VARCHAR(255),")
            .append(CacheUserInfo.USER_ID).append(" INTEGER,")
            .append(CacheUserInfo.AVATAR_NIGHT).append(" VARCHAR(255),")
            .append(CacheUserInfo.NICK_NIGHT).append(" VARCHAR(255),")
            .append(CacheUserInfo.VIP).append(" BOOLEAN")
            .append(")").toString();
    
    private static final String CREATE_FAST_ENTRANCE_INFO = new StringBuilder("CREATE TABLE ")
            .append(FastEntranceInfo.TABLE_NAME).append(" (")
            .append(FastEntranceInfo.ID).append(" INTEGER PRIMARY KEY ASC,")
            .append(FastEntranceInfo.IMG_RES).append(" INTEGER,")
            .append(FastEntranceInfo.NAME).append(" VARCHAR(255),")
            .append(FastEntranceInfo.CLASS_NAME).append(" VARCHAR(255)")
            .append(")").toString();
    
    private static String[] sCreateSqls = new String[] {
        CREATE_COURSE,
        CREATE_USERENTRY,
        CREATE_USERINFO,
        CREATE_CHATRECORD,
        CREATE_GROUPINFO,
        CREATE_EMOTION_INFO,
        CREATE_SEARCH_TAG_INFO,
        CREATE_MATCH_JOB_INFO,
        CREATE_CACHE_BANNER,
        CREATE_CACHE_CHANNEL,
        CREATE_CACHE_IMAGE,
        CREATE_CACHE_POST,
        CREATE_CACHE_SCHOOL,
        CREATE_CACHE_USERINFO,
        CREATE_CACHE_VOTE,
        CREATE_CACHE_NEWS,
        CREATE_FAST_ENTRANCE_INFO
    };

    private static final String[] UPGRADE_1_2 = new String[] {
        new StringBuilder("CREATE TABLE ").append(Course.TABLE_NAME).append(" (")
                .append(Course.COURSE_ID).append(" INTEGER,")
                .append(Course.SEMESTER).append(" INTEGER,")
                .append(Course.NAME).append(" VARCHAR(100),")
                .append(Course.TEACHER).append(" VARCHAR(100),")
                .append(Course.Entry.CLASSROOM).append(" VARCHAR(100),")
                .append(Course.Entry.WEEKDAY).append(" INTEGER,")
                .append(Course.Entry.PERIOD_START).append(" INTEGER,")
                .append(Course.Entry.PERIOD_END).append(" INTEGER,")
                .append(Course.Entry.WEEKS).append(" INTEGER)")
                .toString(),
        new StringBuilder("ALTER TABLE ").append(UserInfo.TABLE_NAME).append(" ADD ")
                .append(UserInfo.TYPE).append(" INTEGER")
                .toString()
    };

    private static final String[] UPGRADE_2_3 = new String[] {
        new StringBuilder("ALTER TABLE ").append(UserInfo.TABLE_NAME).append(" ADD ")
                .append(UserInfo.NICKNAME).append(" VARCHAR(255)")
                .toString(),
        new StringBuilder("ALTER TABLE ").append(UserInfo.TABLE_NAME).append(" ADD ")
                .append(UserInfo.GUID).append(" CHAR(32)")
                .toString(),
        new StringBuilder("ALTER TABLE ").append(UserInfo.TABLE_NAME).append(" ADD ")
                .append(UserInfo.AVATAR_FULL).append(" VARCHAR(255)")
                .toString()
    };

    private static final String[] UPGRADE_3_4 = new String[] {
        new StringBuilder("ALTER TABLE ").append(UserInfo.TABLE_NAME).append(" ADD ")
                .append(UserInfo.PRIMARY_BADGE_IMAGE).append(" VARCHAR(255)")
                .toString()
    };

    private static final String[] UPGRADE_4_5 = new String[] {
        new StringBuilder("ALTER TABLE ").append(Course.TABLE_NAME).append(" ADD ")
                .append(Course.AUTHED).append(" INTEGER")
                .toString(),
        new StringBuilder("ALTER TABLE ").append(Course.TABLE_NAME).append(" ADD ")
                .append(Course.AUTH_USER).append(" INTEGER")
                .toString()
    };

    private static final String[] UPGRADE_5_6 = new String[] {
        new StringBuilder("ALTER TABLE ").append(UserInfo.TABLE_NAME).append(" ADD ")
                .append(UserInfo.SCHOOL_ID).append(" INTEGER")
                .toString(),
       new StringBuilder("ALTER TABLE ").append(UserInfo.TABLE_NAME).append(" ADD ")
                .append(UserInfo.SCHOOL).append(" VARCHAR(255)")
                .toString(),
       new StringBuilder("ALTER TABLE ").append(UserInfo.TABLE_NAME).append(" ADD ")
                .append(UserInfo.DEPARTMENT_ID).append(" INTEGER")
                .toString(),
       new StringBuilder("ALTER TABLE ").append(UserInfo.TABLE_NAME).append(" ADD ")
                .append(UserInfo.DEPARTMENT).append(" VARCHAR(255)")
                .toString(),
       new StringBuilder("ALTER TABLE ").append(UserInfo.TABLE_NAME).append(" ADD ")
                .append(UserInfo.START_YEAR).append(" VARCHAR(255)")
                .toString()
    };

    private static final String[] UPGRADE_6_7 = new String[] {
        new StringBuilder("ALTER TABLE ").append(UserInfo.TABLE_NAME).append(" ADD ")
                .append(UserInfo.FACE_ID).append(" VARCHAR(255)")
                .toString(),
        new StringBuilder("ALTER TABLE ").append(UserInfo.TABLE_NAME).append(" ADD ")
                .append(UserInfo.BG_IMG).append(" VARCHAR(255)")
                .toString()
    };

    private static final String[] UPGRADE_7_8 = new String[] {
        new StringBuilder("ALTER TABLE ").append(UserInfo.TABLE_NAME).append(" ADD ")
                .append(UserInfo.IDS).append(" INTEGER")
                .toString()
    };

    private static final String[] UPGRADE_8_9 = new String[] {
        new StringBuilder("ALTER TABLE ").append(UserInfo.TABLE_NAME).append(" ADD ")
                .append(UserInfo.EMAIL_VERIFIED).append(" INTEGER")
                .toString()
    };

    private static final String[] UPGRADE_9_10 = new String[] {
            CREATE_GROUPINFO,
            new StringBuilder("ALTER TABLE ").append(ChatRecord.TABLE_NAME).append(" ADD ")
                    .append(ChatRecord.FILE_PATH).append(" VARCHAR(255)").toString(),
            new StringBuilder("ALTER TABLE ").append(ChatRecord.TABLE_NAME).append(" ADD ")
                .append(ChatRecord.URL).append(" VARCHAR(255)").toString(),
            new StringBuilder("ALTER TABLE ").append(ChatRecord.TABLE_NAME).append(" ADD ")
                    .append(ChatRecord.LONGITUDE).append(" DOUBLE").toString(),
            new StringBuilder("ALTER TABLE ").append(ChatRecord.TABLE_NAME).append(" ADD ")
                .append(ChatRecord.LATITUDE).append(" DOUBLE").toString(),
            new StringBuilder("ALTER TABLE ").append(ChatRecord.TABLE_NAME).append(" ADD ")
                    .append(ChatRecord.ISGROUPCHAT).append(" BOOLEAN").toString(),
            new StringBuilder("ALTER TABLE ").append(ChatRecord.TABLE_NAME).append(" ADD ")
                .append(ChatRecord.ADDRESS).append(" VARCHAR(255)").toString()
    };

    private static final String [] UPGRADE_10_11 = new String[] {
        CREATE_EMOTION_INFO, CREATE_SEARCH_TAG_INFO , CREATE_MATCH_JOB_INFO,
        new StringBuilder("ALTER TABLE ").append(ChatRecord.TABLE_NAME).append(" ADD ")
                .append(ChatRecord.MSG_TYPE).append(" INTEGER").toString(),
        new StringBuilder("ALTER TABLE ").append(ChatRecord.TABLE_NAME).append(" ADD ")
            .append(ChatRecord.IS_NIGHT_RECORD).append(" BOOLEAN").toString()
    };

    private static final String [] UPGRADE_11_12 = new String[] {
        CREATE_CACHE_BANNER, CREATE_CACHE_CHANNEL, CREATE_CACHE_IMAGE, CREATE_CACHE_POST
        , CREATE_CACHE_SCHOOL, CREATE_CACHE_USERINFO, CREATE_CACHE_VOTE
    };
    
    private static final String [] UPGRADE_12_13 = new String[] {
        CREATE_CACHE_NEWS,
        new StringBuilder("ALTER TABLE ").append(ChatRecord.TABLE_NAME).append(" ADD ")
            .append(ChatRecord.PUBLIC_ID).append(" BOOLEAN").toString()
    };
    
    private static final String [] UPGRADE_13_14 = new String[] {
        new StringBuilder("ALTER TABLE ").append(CacheUserInfo.TABLE_NAME).append(" ADD ")
            .append(CacheUserInfo.AVATAR_NIGHT).append(" VARCHAR(255)").toString(),
        new StringBuilder("ALTER TABLE ").append(CacheUserInfo.TABLE_NAME).append(" ADD ")
            .append(CacheUserInfo.NICK_NIGHT).append(" VARCHAR(255)").toString()
    };
    
    private static final String [] UPGRADE_14_15 = new String[] {
        CREATE_FAST_ENTRANCE_INFO  
    };
    
    private static final String [] UPGRADE_15_16 = new String[] {
        new StringBuilder("ALTER TABLE ").append(CachePostInfo.TABLE_NAME).append(" ADD ")
            .append(CachePostInfo.STICKLVL).append(" INTEGER").toString(),
        new StringBuilder("ALTER TABLE ").append(CachePostInfo.TABLE_NAME).append(" ADD ")
            .append(CachePostInfo.TITLE).append(" VARCHAR(255)").toString(),
        new StringBuilder("ALTER TABLE ").append(CachePostInfo.TABLE_NAME).append(" ADD ")
            .append(CachePostInfo.VIDEO_URL).append(" VARCHAR(255)").toString(),
        new StringBuilder("ALTER TABLE ").append(CachePostInfo.TABLE_NAME).append(" ADD ")
            .append(CachePostInfo.VIDEO_THUMBNAIL).append(" VARCHAR(255)").toString(),
        new StringBuilder("ALTER TABLE ").append(CachePostInfo.TABLE_NAME).append(" ADD ")
            .append(CachePostInfo.REPLY_TIME).append(" INTEGER").toString()
    };
    
    private static final String[] UPGRADE_16_17 = new String[] {
        new StringBuilder("ALTER TABLE ").append(CachePostInfo.TABLE_NAME).append(" ADD ")
            .append(CachePostInfo.MODERATOR_UIDS).append(" VARCHAR(255)").toString(),
        new StringBuilder("ALTER TABLE ").append(CachePostInfo.TABLE_NAME).append(" ADD ")
            .append(CachePostInfo.SUPER_MODERATOR_UIDS).append(" VARCHAR(255)").toString()
    };
    
    private static final String[] UPGRADE_17_18 = new String[] {
        new StringBuilder("ALTER TABLE ").append(ChatRecord.TABLE_NAME).append(" ADD ")
            .append(ChatRecord.GROUP_ID).append(" VARCHAR(255)").toString()
    };
    
    private static String[][] sUpdateSqls = new String[][] {
        UPGRADE_1_2, 
        UPGRADE_2_3,
        UPGRADE_3_4,
        UPGRADE_4_5,
        UPGRADE_5_6,
        UPGRADE_6_7,
        UPGRADE_7_8,
        UPGRADE_8_9,
        UPGRADE_9_10,
        UPGRADE_10_11,
        UPGRADE_11_12,
        UPGRADE_12_13,
        UPGRADE_13_14,
        UPGRADE_14_15,
        UPGRADE_15_16,
        UPGRADE_16_17,
        UPGRADE_17_18
    };

    public DatabaseOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String sql : sCreateSqls) {
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Execute each version upgrade sql
        for (int i = oldVersion; i < newVersion; i++) {
            if ((i - 1) >= sUpdateSqls.length) {
                return;
            }
            String[] updates = sUpdateSqls[i - 1];
            for (int j = 0; j < updates.length; j++) {
                try {
                    db.execSQL(updates[j]);
                } catch (Exception e) {
                }
            }
        }
    }

    public static void createGroupInfoTable(SQLiteDatabase db) {
        db.execSQL(CREATE_GROUPINFO);
    }
}
