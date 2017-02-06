package com.tjut.mianliao.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.tjut.mianliao.chat.CacheNewsInfo;
import com.tjut.mianliao.data.BannerInfo.BannerData;
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
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.Vote;
import com.tjut.mianliao.util.Utils;

public class DataHelper {
    private static final String TAG = "DataHelper";
    private static final String NAME_SHARED_PREFS = "light_weight_data";

    private DataHelper() {
    }

    public static ArrayList<UserInfo> loadUserInfos(Context context) {
        /**创建查询到结果的联系人集合**/
        ArrayList<UserInfo> contacts = new ArrayList<UserInfo>();
        // 获取数据库可读权限
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return contacts;
        }

        Cursor c = db.query(UserInfo.TABLE_NAME, null, null, null, null, null, UserInfo.NAME + " ASC");

        int colUserId = c.getColumnIndex(UserInfo.USER_ID);
        int colGuid = c.getColumnIndex(UserInfo.GUID);
        int colAccount = c.getColumnIndex(UserInfo.ACCOUNT);
        int colType = c.getColumnIndex(UserInfo.TYPE);
        int colIds = c.getColumnIndex(UserInfo.IDS);
        int colName = c.getColumnIndex(UserInfo.NAME);
        int colNickname = c.getColumnIndex(UserInfo.NICKNAME);
        int colPhone = c.getColumnIndex(UserInfo.PHONE);
        int colEmail = c.getColumnIndex(UserInfo.EMAIL);
        int colEmailVerified = c.getColumnIndex(UserInfo.EMAIL_VERIFIED);
        int colGender = c.getColumnIndex(UserInfo.GENDER);
        int colShortDesc = c.getColumnIndex(UserInfo.SHORT_DESC);
        int colAvatar = c.getColumnIndex(UserInfo.AVATAR);
        int colFullAvatar = c.getColumnIndex(UserInfo.AVATAR_FULL);
        int colBgImg = c.getColumnIndex(UserInfo.BG_IMG);
        int colPrimaryBadgeImage = c.getColumnIndex(UserInfo.PRIMARY_BADGE_IMAGE);
        int colSchoolId = c.getColumnIndex(UserInfo.SCHOOL_ID);
        int colSchool = c.getColumnIndex(UserInfo.SCHOOL);
        int colDepartmentId = c.getColumnIndex(UserInfo.DEPARTMENT_ID);
        int colDepartment = c.getColumnIndex(UserInfo.DEPARTMENT);
        int colStartYear = c.getColumnIndex(UserInfo.START_YEAR);
        int colFaceId = c.getColumnIndex(UserInfo.FACE_ID);
        int colLastUpdate = c.getColumnIndex(UserInfo.LAST_UPDATE);

        while (c.moveToNext()) {
            UserInfo user = new UserInfo();
            user.userId = c.getInt(colUserId);
            user.guid = c.getString(colGuid);
            user.type = c.getInt(colType);
            user.ids = c.getInt(colIds) != 0;
            user.setAccount(c.getString(colAccount));
            user.setName(c.getString(colName));
            user.nickname = c.getString(colNickname);
            user.phone = c.getString(colPhone);
            user.email = c.getString(colEmail);
            user.emailVerified = c.getInt(colEmailVerified) != 0;
            user.gender = c.getInt(colGender);
            user.shortDesc = c.getString(colShortDesc);
            user.setAvatar(c.getString(colAvatar));
            user.avatarFull = c.getString(colFullAvatar);
            user.bgImg = c.getString(colBgImg);
            user.primaryBadgeImage = c.getString(colPrimaryBadgeImage);
            user.schoolId = c.getInt(colSchoolId);
            user.school = c.getString(colSchool);
            user.departmentId = c.getInt(colDepartmentId);
            user.department = c.getString(colDepartment);
            user.startYear = c.getString(colStartYear);
            user.faceId = c.getString(colFaceId);
            user.lastUpdate = c.getString(colLastUpdate);
            contacts.add(user);
        }
        c.close();
        db.close();

        return contacts;
    }

    public static synchronized void updateUserInfo(Context context, Collection<UserInfo> userInfoList) {
        if (userInfoList == null || userInfoList.size() == 0) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }

        db.beginTransaction();
        try {
            for (UserInfo user : userInfoList) {
                db.update(UserInfo.TABLE_NAME, fillUserInfo(user), UserInfo.USER_ID + "=" + user.userId, null);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public static synchronized void insertUserInfo(Context context, Collection<UserInfo> userInfoList) {
        if (userInfoList == null || userInfoList.size() == 0) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }

        db.beginTransaction();
        try {
            for (UserInfo user : userInfoList) {
                long id = db.insert(UserInfo.TABLE_NAME, null, fillUserInfo(user));
                if (id == -1) {
                    Utils.logW(TAG, "Failed inserting data: " + user.account);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public static synchronized void deleteUserInfo(Context context, Collection<UserInfo> userInfoList) {
        if (userInfoList == null || userInfoList.size() == 0) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }

        db.beginTransaction();
        try {
            for (UserInfo user : userInfoList) {
                db.delete(UserInfo.TABLE_NAME, UserInfo.USER_ID + "='" + user.userId + "'", null);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    private static ContentValues fillUserInfo(UserInfo user) {
        ContentValues cv = new ContentValues();
        cv.put(UserInfo.USER_ID, user.userId);
        cv.put(UserInfo.GUID, user.guid);
        cv.put(UserInfo.ACCOUNT, user.account);
        cv.put(UserInfo.TYPE, user.type);
        cv.put(UserInfo.IDS, user.ids ? 1 : 0);
        cv.put(UserInfo.NAME, user.name);
        cv.put(UserInfo.NICKNAME, user.nickname);
        cv.put(UserInfo.PHONE, user.phone);
        cv.put(UserInfo.EMAIL, user.email);
        cv.put(UserInfo.EMAIL_VERIFIED, user.emailVerified ? 1 : 0);
        cv.put(UserInfo.GENDER, user.gender);
        cv.put(UserInfo.SHORT_DESC, user.shortDesc);
        cv.put(UserInfo.AVATAR, user.getAvatar());
        cv.put(UserInfo.AVATAR_FULL, user.avatarFull);
        cv.put(UserInfo.BG_IMG, user.bgImg);
        cv.put(UserInfo.PRIMARY_BADGE_IMAGE, user.primaryBadgeImage);
        cv.put(UserInfo.SCHOOL_ID, user.schoolId);
        cv.put(UserInfo.SCHOOL, user.school);
        cv.put(UserInfo.DEPARTMENT_ID, user.departmentId);
        cv.put(UserInfo.DEPARTMENT, user.department);
        cv.put(UserInfo.START_YEAR, user.startYear);
        cv.put(UserInfo.FACE_ID, user.faceId);
        cv.put(UserInfo.LAST_UPDATE, user.lastUpdate);
        return cv;
    }

    public static ArrayList<UserEntry> loadUserEntries(Context context) {
        ArrayList<UserEntry> users = new ArrayList<UserEntry>();
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return users;
        }

        Cursor c = db.query(UserEntry.TABLE_NAME, null, null, null, null, null, null);

        int colJid = c.getColumnIndex(UserEntry.JID);

        while (c.moveToNext()) {
            UserEntry user = new UserEntry(c.getString(colJid));
            users.add(user);
        }
        c.close();
        db.close();
        Utils.logD(TAG, "load user entry : " + users.size());
        for (UserEntry user : users) {
            Utils.logD(TAG, "user entry info : jid = " + user.jid);
        }
        return users;
    }

    public static synchronized void updateUserEntries(Context context, Collection<UserEntry> userEntries) {
        if (userEntries == null || userEntries.size() == 0) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }

        db.beginTransaction();
        try {
            for (UserEntry user : userEntries) {
                db.update(UserEntry.TABLE_NAME, fillUserEntry(user), UserEntry.JID + "='" + user.jid + "'", null);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public static synchronized void insertUserEntries(Context context, Collection<UserEntry> userEntries) {
        if (userEntries == null || userEntries.size() == 0) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }

        db.beginTransaction();
        try {
            for (UserEntry user : userEntries) {
                long id = db.insert(UserEntry.TABLE_NAME, null, fillUserEntry(user));
                if (id == -1) {
                    Utils.logW(TAG, "Failed inserting data: " + user.toString());
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public static synchronized void deleteUserEntries(Context context, Collection<UserEntry> userEntries) {
        if (userEntries == null || userEntries.size() == 0) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }

        db.beginTransaction();
        try {
            for (UserEntry user : userEntries) {
                db.delete(UserEntry.TABLE_NAME, UserEntry.JID + "='" + user.jid + "'", null);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    private static ContentValues fillUserEntry(UserEntry user) {
        ContentValues cv = new ContentValues();
        cv.put(UserEntry.JID, user.jid);
        Utils.logD(TAG, "fillUserEntry:" + user.jid);
        return cv;
    }

    public static synchronized void insertCourse(Context context, Course course) {
        if (course == null) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }

        for (Course.Entry entry : course.getEntries()) {
            db.insert(Course.TABLE_NAME, null, fillCourse(entry));
        }
        db.close();
    }

    private static ContentValues fillCourse(Course.Entry entry) {
        ContentValues cv = new ContentValues();
        Course course = entry.getCourse();
        cv.put(Course.COURSE_ID, course.courseId);
        cv.put(Course.SEMESTER, course.semester);
        cv.put(Course.AUTHED, course.authed);
        cv.put(Course.AUTH_USER, course.authUser);
        cv.put(Course.NAME, course.name);
        cv.put(Course.TEACHER, course.teacher);
        cv.put(Course.Entry.CLASSROOM, entry.classroom);
        cv.put(Course.Entry.WEEKDAY, entry.weekday);
        cv.put(Course.Entry.PERIOD_START, entry.periodStart);
        cv.put(Course.Entry.PERIOD_END, entry.periodEnd);
        cv.put(Course.Entry.WEEKS, entry.weeks);
        return cv;
    }

    public static ArrayList<Course> loadCourses(Context context, int semester) {
        ArrayList<Course> courses = new ArrayList<Course>();
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return courses;
        }

        Cursor c = db.query(Course.TABLE_NAME, null, Course.SEMESTER + "=" + semester, null, null, null,
                Course.COURSE_ID);

        int colCourseId = c.getColumnIndex(Course.COURSE_ID);
        int colSemester = c.getColumnIndex(Course.SEMESTER);
        int colAuthed = c.getColumnIndex(Course.AUTHED);
        int colIsExtra = c.getColumnIndex(Course.AUTH_USER);
        int colName = c.getColumnIndex(Course.NAME);
        int colTeacher = c.getColumnIndex(Course.TEACHER);
        int colClassroom = c.getColumnIndex(Course.Entry.CLASSROOM);
        int colWeekDay = c.getColumnIndex(Course.Entry.WEEKDAY);
        int colPeriodStart = c.getColumnIndex(Course.Entry.PERIOD_START);
        int colPeriodEnd = c.getColumnIndex(Course.Entry.PERIOD_END);
        int colWeeks = c.getColumnIndex(Course.Entry.WEEKS);

        Course preCourse = null;
        while (c.moveToNext()) {
            int courseId = c.getInt(colCourseId);
            if (preCourse != null && preCourse.courseId == courseId) {
                preCourse.addEntry(new Course.Entry(c.getString(colClassroom), c.getInt(colWeeks),
                        c.getInt(colWeekDay), c.getInt(colPeriodStart), c.getInt(colPeriodEnd)));
            } else {
                preCourse = new Course(c.getInt(colCourseId), c.getInt(colSemester), c.getString(colName),
                        c.getString(colTeacher), c.getString(colClassroom), c.getInt(colWeekDay),
                        c.getInt(colPeriodStart), c.getInt(colPeriodEnd), c.getInt(colWeeks));
                preCourse.authed = c.getInt(colAuthed) == 1 ? true : false;
                preCourse.authUser = c.getInt(colIsExtra) == 1 ? true : false;

                courses.add(preCourse);
            }
        }

        c.close();
        db.close();
        return courses;
    }

    public static synchronized void deleteCourse(Context context, int courseId) {
        if (courseId == 0) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }

        db.delete(Course.TABLE_NAME, Course.COURSE_ID + "=" + courseId, null);
        db.close();
    }

    public static ChatRecord loadChatRecord(Context context, long id) {
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return null;
        }

        Cursor c = db.query(ChatRecord.TABLE_NAME, null, ChatRecord.ID + "=" + id + "", null, null, null, null, null);
        ArrayList<ChatRecord> chatRecords = new ArrayList<ChatRecord>();

        loadChatRecords(c, chatRecords);

        c.close();
        db.close();
        return chatRecords.size() == 0 ? null : chatRecords.get(0);
    }
    
    public static ChatRecord loadChatRecordbyMsgType(Context context, long msgType, long punlicId) {
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return null;
        }

        Cursor c = db.query(ChatRecord.TABLE_NAME, null, ChatRecord.MSG_TYPE + "='" + msgType + "' and " +
                ChatRecord.PUBLIC_ID +"='" + punlicId + "'", null, null, null, null, null);
        ArrayList<ChatRecord> chatRecords = new ArrayList<ChatRecord>();

        loadChatRecords(c, chatRecords);

        c.close();
        db.close();
        return chatRecords.size() == 0 ? null : chatRecords.get(0);
    }

    public static synchronized boolean hasMoreChats(Context context, String target, long timestamp) {
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return false;
        }

        String whereClause = ChatRecord.TARGET + "='" + target + "' and " + ChatRecord.TIMESTAMP + "<" + timestamp;
        Cursor c = db.query(ChatRecord.TABLE_NAME, null, whereClause, null, null, null, ChatRecord.TIMESTAMP + " DESC",
                "1");
        boolean result = c.getCount() > 0;
        c.close();
        db.close();
        return result;
    }

    public static ArrayList<ChatRecord> loadChatRecords(Context context, String target, long timestamp, int limit) {
        ArrayList<ChatRecord> chatRecords = new ArrayList<ChatRecord>();
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return chatRecords;
        }

        String limitClause = null;
        if (limit > 0) {
            limitClause = String.valueOf(limit);
        }
        String whereClause = ChatRecord.TARGET + "='" + target + "'";
        if (timestamp > 0) {
            whereClause += " and " + ChatRecord.TIMESTAMP + "<" + timestamp;
        }
        Cursor c = db.query(ChatRecord.TABLE_NAME, null, whereClause, null, null, null, ChatRecord.TIMESTAMP + " DESC",
                limitClause);

        loadChatRecords(c, chatRecords);

        c.close();
        db.close();
        return chatRecords;
    }

    public static ArrayList<ChatRecord> loadRecentChats(Context context) {
        ArrayList<ChatRecord> chatRecords = new ArrayList<ChatRecord>();
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return chatRecords;
        }

        String whereClause = ChatRecord.ID + " in (SELECT max(" + ChatRecord.ID + ") FROM " + ChatRecord.TABLE_NAME
                + " GROUP BY " + ChatRecord.TARGET + ")";
        Cursor c = db.query(ChatRecord.TABLE_NAME, null, whereClause, null, null, null, ChatRecord.TIMESTAMP + " ASC");
        loadChatRecords(c, chatRecords);
        c.close();
        db.close();
        return chatRecords;
    }

    public static ChatRecord loadChatRecord(Context context, int type) {
        ArrayList<ChatRecord> chatRecords = new ArrayList<ChatRecord>();
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return null;
        }

        Cursor c = db.query(ChatRecord.TABLE_NAME, null, ChatRecord.MSG_TYPE + "=" + type + "",
                null, null, null, null, null);
        loadChatRecords(c, chatRecords);
        c.close();
        db.close();
        return chatRecords.size() > 0 ? chatRecords.get(0) : null;
    }

    public static synchronized boolean insertChatRecord(Context context, ChatRecord record) {
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return false;
        }

        record.id = db.insert(ChatRecord.TABLE_NAME, null, fillChatRecord(record));
        Utils.logD(TAG, "insertChatRecord: " + record.target);
        db.close();

        if (record.id > 0) {
            return true;
        } else {
            Utils.logW(TAG, "Failed inserting data: " + record.text);
            return false;
        }
    }

    public static synchronized boolean updateChatRecord(Context context, ChatRecord record) {
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return false;
        }

        int result = db.update(ChatRecord.TABLE_NAME, fillChatRecord(record), ChatRecord.ID + "='" + record.id + "'",
                null);
        db.close();

        return result > 0;
    }
    
    public static synchronized boolean updateChatRecordbyMsgType(Context context, ChatRecord record) {
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return false;
        }

        int result = db.update(ChatRecord.TABLE_NAME, fillChatRecord(record), ChatRecord.MSG_TYPE + "='" + record.msgType + "'",
                null);
        db.close();

        return result > 0;
    }

    public static synchronized void deleteChatRecords(Context context, String target) {
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }

        db.delete(ChatRecord.TABLE_NAME, ChatRecord.TARGET + "='" + target + "'", null);
        db.close();
    }

    public static synchronized void deleteChatRecords(Context context, int type) {
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }

        int delete = db.delete(ChatRecord.TABLE_NAME, ChatRecord.MSG_TYPE + "='" + type + "'", null);
        System.out.println("**************"+ delete);
        db.close();
    }

    private static void loadChatRecords(Cursor c, ArrayList<ChatRecord> chatRecords) {
        int colId = c.getColumnIndex(ChatRecord.ID);
        int colTarget = c.getColumnIndex(ChatRecord.TARGET);
        int colFrom = c.getColumnIndex(ChatRecord.FROM);
        int colTimeStamp = c.getColumnIndex(ChatRecord.TIMESTAMP);
        int colType = c.getColumnIndex(ChatRecord.TYPE);
        int colDisableEmo = c.getColumnIndex(ChatRecord.DISABLE_EMO);
        int colText = c.getColumnIndex(ChatRecord.MESSAGE);
        int colVoiceLength = c.getColumnIndex(ChatRecord.VOICE_LENGTH);
        int colFilePath = c.getColumnIndex(ChatRecord.FILE_PATH);
        int colUrl = c.getColumnIndex(ChatRecord.URL);
        int colLongitude = c.getColumnIndex(ChatRecord.LONGITUDE);
        int colLatitude = c.getColumnIndex(ChatRecord.LATITUDE);
        int colAddress = c.getColumnIndex(ChatRecord.ADDRESS);
        int colIsGroupChat = c.getColumnIndex(ChatRecord.ISGROUPCHAT);
        int colMsgType = c.getColumnIndex(ChatRecord.MSG_TYPE);
        int colIsNightRecord = c.getColumnIndex(ChatRecord.IS_NIGHT_RECORD);
        int colPublicId = c.getColumnIndex(ChatRecord.PUBLIC_ID);
        int colChatId = c.getColumnIndex(ChatRecord.GROUP_ID);

        while (c.moveToNext()) {
            ChatRecord record = new ChatRecord();
            record.id = c.getLong(colId);
            record.target = c.getString(colTarget);
            record.from = c.getString(colFrom);
            record.timestamp = c.getLong(colTimeStamp);
            record.type = c.getInt(colType);
            record.text = c.getString(colText);
            record.disableEmo = c.getInt(colDisableEmo) == 1;
            record.voiceLength = c.getInt(colVoiceLength);
            record.filePath = c.getString(colFilePath);
            record.url = c.getString(colUrl);
            record.longitude = c.getDouble(colLongitude);
            record.latitude = c.getDouble(colLatitude);
            record.address = c.getString(colAddress);
            record.isGroupChat = c.getInt(colIsGroupChat) == 1;
            record.msgType = c.getInt(colMsgType);
            record.isNightRecord = c.getInt(colIsNightRecord) == 1;
            record.publicId = c.getLong(colPublicId);
            record.groupId = c.getString(colChatId);
            chatRecords.add(0, record);
        }
    }

    private static ContentValues fillChatRecord(ChatRecord record) {
        ContentValues cv = new ContentValues();
        cv.put(ChatRecord.TARGET, record.target);
        cv.put(ChatRecord.FROM, record.from);
        cv.put(ChatRecord.TIMESTAMP, record.timestamp);
        cv.put(ChatRecord.TYPE, record.type);
        cv.put(ChatRecord.MESSAGE, record.text);
        cv.put(ChatRecord.VOICE_LENGTH, record.voiceLength);
        cv.put(ChatRecord.DISABLE_EMO, record.disableEmo);
        cv.put(ChatRecord.FILE_PATH, record.filePath);
        cv.put(ChatRecord.URL, record.url);
        cv.put(ChatRecord.LONGITUDE, record.longitude);
        cv.put(ChatRecord.LATITUDE, record.latitude);
        cv.put(ChatRecord.ADDRESS, record.address);
        cv.put(ChatRecord.ISGROUPCHAT, record.isGroupChat);
        cv.put(ChatRecord.MSG_TYPE, record.msgType);
        cv.put(ChatRecord.IS_NIGHT_RECORD, record.isNightRecord);
        cv.put(ChatRecord.PUBLIC_ID, record.publicId);
        cv.put(ChatRecord.GROUP_ID, TextUtils.isEmpty(record.groupId) ? "" : record.groupId);
        return cv;
    }

    public static synchronized boolean insertGroupInfo(Context context, GroupInfo groupInfo) {
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return false;
        }
        
        if (!isTableExist(context, GroupInfo.TABLE_NAME)) {
            DatabaseOpenHelper.createGroupInfoTable(db);
        }

        groupInfo.id = db.insert(GroupInfo.TABLE_NAME, null, fillGroupInfo(groupInfo));
        Utils.logD(TAG, "insertGroupInfo: " + groupInfo.jid);
        db.close();

        if (groupInfo.id > 0) {
            Utils.logD(TAG, "Success inserting data: " + groupInfo.groupName);
            return true;
        } else {
            Utils.logW(TAG, "Failed inserting data: " + groupInfo.groupName);
            return false;
        }
    }

    public static synchronized boolean updateGroupInfo(Context context, GroupInfo groupInfo) {
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return false;
        }
        int result = db.update(GroupInfo.TABLE_NAME, fillGroupInfo(groupInfo),
                GroupInfo.JID + "='" + groupInfo.jid + "'", null);
        Utils.logD(TAG, "insertGroupInfo: " + groupInfo.jid);
        db.close();
        return result > 0;
    }

    public static List<EmotionsInfo> queryEmotionInfo(Context context) {
        SQLiteDatabase db = getReadableDatabase(context);

        if (db == null) {
            return null;
        }

        ArrayList<EmotionsInfo> emotions = new ArrayList<EmotionsInfo>();
        if (!db.isDbLockedByCurrentThread()) {
            Cursor cursor = db.query(EmotionsInfo.TABLE_NAME, null, EmotionsInfo.IS_USING + "=?",
                    new String[] { "1" }, null, null, EmotionsInfo.ID + " desc");
            
            loadEmotionInfos(cursor, emotions);
            cursor.close();
            db.close();
        }
        return emotions;
    }

    public static synchronized boolean insertEmotionInfo(Context context, EmotionsInfo emotionInfo){
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return false;
        }

        emotionInfo.id = db.insert(EmotionsInfo.TABLE_NAME, null, fillEmotionInfo(emotionInfo));
        Utils.logD(TAG, "insertEmotionInfo: " + emotionInfo.id);
        db.close();

        if (emotionInfo.id > 0) {
            return true;
        } else {
            Utils.logW(TAG, "Failed inserting data: " + emotionInfo.path);
            return false;
        }
    }

    public static synchronized boolean updateEmotionInfo(Context context, EmotionsInfo emotionInfo) {
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return false;
        }
        int result = db.update(EmotionsInfo.TABLE_NAME, fillEmotionInfo(emotionInfo),
                EmotionsInfo.NAME + "='" + emotionInfo.name + "'", null);
        Utils.logD(TAG, "updateEmotionInfo: " + emotionInfo.name);
        db.close();
        return result > 0;
    }

    public static EmotionsInfo loadEmotionInfo(Context context, String name){
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return null;
        }

        Cursor c = db.query(EmotionsInfo.TABLE_NAME, null, EmotionsInfo.NAME + "='" + name + "'",
                null, null, null, null, null);
        ArrayList<EmotionsInfo> infos = new ArrayList<EmotionsInfo>();

        loadEmotionInfos(c, infos);

        c.close();
        db.close();
        return infos.size() == 0 ? null : infos.get(0);
    }

    public static synchronized long insertMathchJobInfo(Context context, MatchJobInfo job) {
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return 0;
        }

        job.id = db.insert(MatchJobInfo.TABLE_NAME, null, fillJobInfo(job));
        Utils.logD(TAG, "insertJobInfo: " + job.jobTitle + ",id:" + job.id);
        db.close();
        return job.id;
    }

    private static ContentValues fillJobInfo(MatchJobInfo job) {
        ContentValues cv = new ContentValues();
        cv.put(MatchJobInfo.JOB_ID, job.jobId);
        cv.put(MatchJobInfo.JOB_TITLE, job.jobTitle);
        cv.put(MatchJobInfo.PUBLISH_TIME, job.publishTime);
        cv.put(MatchJobInfo.CROP_LOGO, job.corpLogo);
        cv.put(MatchJobInfo.CROP_NAME, job.corpName);
        cv.put(MatchJobInfo.SALARY, job.salary);
        cv.put(MatchJobInfo.LOCAL_CITY, job.localCity);
        return cv;
    }

    public static MatchJobInfo loadMatchJobsInfo(Context context, long id) {
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return null;
        }
        Cursor c = db.query(MatchJobInfo.TABLE_NAME, null, MatchJobInfo.ID + "=" + id + "",
                null, null, null, null, null);
        ArrayList<MatchJobInfo> jobInfos = new ArrayList<MatchJobInfo>();
        loadJobInfos(c, jobInfos);
        c.close();
        db.close();
        return jobInfos.size() == 0 ? null : jobInfos.get(0);
    }

    private static void loadJobInfos(Cursor c, ArrayList<MatchJobInfo> jobInfos) {
        int colId = c.getColumnIndex(MatchJobInfo.ID);
        int colJobId = c.getColumnIndex(MatchJobInfo.JOB_ID);
        int colLog = c.getColumnIndex(MatchJobInfo.CROP_LOGO);
        int colName = c.getColumnIndex(MatchJobInfo.CROP_NAME);
        int colJobTitle = c.getColumnIndex(MatchJobInfo.JOB_TITLE);
        int colTime = c.getColumnIndex(MatchJobInfo.PUBLISH_TIME);
        int colSalary = c.getColumnIndex(MatchJobInfo.SALARY);
        int colLocalCity = c.getColumnIndex(MatchJobInfo.LOCAL_CITY);

        while (c.moveToNext()) {
            MatchJobInfo jobInfo = new MatchJobInfo();
            jobInfo.id = c.getLong(colId);
            jobInfo.jobId = c.getInt(colJobId);
            jobInfo.jobTitle = c.getString(colJobTitle);
            jobInfo.publishTime = c.getLong(colTime);
            jobInfo.corpLogo = c.getString(colLog);
            jobInfo.corpName = c.getString(colName);
            jobInfo.salary = c.getString(colSalary);
            jobInfo.localCity = c.getString(colLocalCity);
            jobInfos.add(jobInfo);
        }
    }

    public static synchronized boolean deleteJobInfo(Context context, String tagIds) {
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return false;
        }
        String [] ids = tagIds.split(Utils.COMMA_DELIMITER);
        db.beginTransaction();
        int count = 0;
        try {
            for (String id : ids) {
                count = db.delete(MatchJobInfo.TABLE_NAME, MatchJobInfo.ID + "=" + id, null);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        db.close();
        return count > 0 ? true : false;
    }

    public static synchronized boolean insertTagInfo(Context context, SearchTagInfo tag) {
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return false;
        }

        tag.id = db.insert(SearchTagInfo.TABLE_NAME, null, fillTagInfo(tag));
        Utils.logD(TAG, "insertTagInfo: " + tag.id);
        db.close();
        if (tag.id > 0) {
            return true;
        } else {
            Utils.logW(TAG, "Failed inserting data: " + tag.tags);
            return false;
        }
    }

    private static ContentValues fillTagInfo(SearchTagInfo tag) {
        ContentValues cv = new ContentValues();
        cv.put(SearchTagInfo.TAGS, tag.tags);
        cv.put(SearchTagInfo.TAG_ID, tag.tagIds);
        cv.put(SearchTagInfo.TAG_STR, tag.tagStr);
        cv.put(SearchTagInfo.TIME, tag.time);
        return cv;
    }

    public static ArrayList<SearchTagInfo> loadTagInfos(Context context, int limit) {
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return null;
        }
        Cursor c = db.query(SearchTagInfo.TABLE_NAME, null, null, null, null, null,
                SearchTagInfo.ID + " DESC ", String.valueOf(limit));
        ArrayList<SearchTagInfo> tagInfos = new ArrayList<SearchTagInfo>();
        loadTagInfos(c, tagInfos);
        c.close();
        db.close();
        return tagInfos;
    }

    public static SearchTagInfo loadTagInfo(Context context, String tags) {
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return null;
        }
        Cursor c = db.query(SearchTagInfo.TABLE_NAME, null,
                SearchTagInfo.TAGS + "=" + "'" + tags + "'", null, null, null, null);
        ArrayList<SearchTagInfo> tagInfos = new ArrayList<SearchTagInfo>();
        loadTagInfos(c, tagInfos);
        c.close();
        db.close();
        return tagInfos.size() == 0 ? null : tagInfos.get(0);
    }

    public static synchronized boolean deleteTagInfo(Context context, long id) {
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return false;
        }
        int count = db.delete(SearchTagInfo.TABLE_NAME, SearchTagInfo.ID + "=" + id, null);
        db.close();
        return count > 0 ? true : false;
    }

    private static void loadTagInfos(Cursor c, ArrayList<SearchTagInfo> tagInfos) {
        int colId = c.getColumnIndex(SearchTagInfo.ID);
        int colTagId = c.getColumnIndex(SearchTagInfo.TAG_ID);
        int colTags = c.getColumnIndex(SearchTagInfo.TAGS);
        int colTime = c.getColumnIndex(SearchTagInfo.TIME);
        int colTagStr = c.getColumnIndex(SearchTagInfo.TAG_STR);
        while (c.moveToNext()) {
            SearchTagInfo info = new SearchTagInfo();
            info.id = c.getLong(colId);
            info.tagIds = c.getString(colTagId);
            info.tags = c.getString(colTags);
            info.time = c.getLong(colTime);
            info.tagStr = c.getString(colTagStr);
            tagInfos.add(info);
        }
        
    }

    public static GroupInfo loadGroupInfo(Context context, String jid) {
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return null;
        }
        
        if (!isTableExist(context, GroupInfo.TABLE_NAME)) {
            DatabaseOpenHelper.createGroupInfoTable(db);
        }

        Cursor c = db.query(GroupInfo.TABLE_NAME, null, GroupInfo.JID + "='" + jid + "'",
                null, null, null, null, null);
        ArrayList<GroupInfo> groupInfos = new ArrayList<GroupInfo>();

        loadGropInfos(c, groupInfos);

        c.close();
        db.close();
        return groupInfos.size() == 0 ? null : groupInfos.get(0);
    }

    private static void loadGropInfos(Cursor c, ArrayList<GroupInfo> groupInfos) {
        int colId = c.getColumnIndex(GroupInfo.ID);
        int colJid = c.getColumnIndex(GroupInfo.JID);
        int colGroupName = c.getColumnIndex(GroupInfo.GROUP_NAME);

        while (c.moveToNext()) {
            GroupInfo groupInfo = new GroupInfo();
            groupInfo.id = c.getInt(colId);
            groupInfo.jid = c.getString(colJid);
            groupInfo.groupName = c.getString(colGroupName);
            groupInfos.add(groupInfo);
        }
    }

    private static void loadEmotionInfos(Cursor c, ArrayList<EmotionsInfo> emotionInfos){
        int colId = c.getColumnIndex(EmotionsInfo.ID);
        int colName = c.getColumnIndex(EmotionsInfo.NAME);
        int colPath = c.getColumnIndex(EmotionsInfo.PATH);
        int colZipPath = c.getColumnIndex(EmotionsInfo.ZIP_PATH);
        int colUrl = c.getColumnIndex(EmotionsInfo.URL);
        int colIsusing = c.getColumnIndex(EmotionsInfo.IS_USING);
        while (c.moveToNext()){
            EmotionsInfo info = new EmotionsInfo();
            info.id = c.getLong(colId);
            info.name = c.getString(colName);
            info.url = c.getString(colUrl);
            info.zipPath = c.getString(colZipPath);
            info.path = c.getString(colPath);
            info.isUsing = c.getInt(colIsusing) == 1;
            emotionInfos.add(info);
        }
    }

    private static ContentValues fillGroupInfo(GroupInfo groupInfo) {
        ContentValues cv = new ContentValues();
        cv.put(GroupInfo.JID, groupInfo.jid);
        cv.put(GroupInfo.GROUP_NAME, groupInfo.groupName);
        return cv;
    }

    private static ContentValues fillEmotionInfo(EmotionsInfo emotion) {
        ContentValues cv = new ContentValues();
        cv.put(EmotionsInfo.URL, emotion.url);
        cv.put(EmotionsInfo.NAME, emotion.name);
        cv.put(EmotionsInfo.ZIP_PATH, emotion.zipPath);
        cv.put(EmotionsInfo.PATH, emotion.path);
        cv.put(EmotionsInfo.IS_USING, emotion.isUsing);
        return cv;
    }

    public static synchronized void insertCacheBannerInfos(Context context, ArrayList<BannerInfo> banners) {
        if (banners == null || banners.size() == 0) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }

        db.beginTransaction();
        try {
            for (BannerInfo banner : banners) {
                long id = db.insert(CacheBannerInfo.TABLE_NAME, null, fillBannerInfo(banner));
                if (id == -1) {
                    Utils.logW(TAG, "Failed inserting data: " + banner.getId());
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        db.close();
    }
    
    public static synchronized void deleteCacheBannerByPlate(Context context, int plate) {
        if (plate < 0) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }

        db.delete(CacheBannerInfo.TABLE_NAME, CacheBannerInfo.PLATE + "='" + plate + "'", null);
        db.close();
    }
    
    public static ArrayList<BannerInfo> loadBannerInfos(Context context, int plate) {
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return null;
        }

        Cursor c = db.query(CacheBannerInfo.TABLE_NAME, null, CacheBannerInfo.PLATE + "='" + plate + "'",
                null, null, null, null, null);
        ArrayList<BannerInfo> banners = new ArrayList<BannerInfo>();

        loadCacheBanners(c, banners);

        c.close();
        db.close();
        return banners;
    }
    
    private static void loadCacheBanners(Cursor c, ArrayList<BannerInfo> banners) {
        int columnId = c.getColumnIndex(CacheBannerInfo.ID);
        int columnPlate = c.getColumnIndex(CacheBannerInfo.PLATE);
        int columnImage = c.getColumnIndex(CacheBannerInfo.IMAGE);
        int columnType = c.getColumnIndex(CacheBannerInfo.TYPE);
        int columnData = c.getColumnIndex(CacheBannerInfo.DATA);
        while (c.moveToNext()) {
            BannerInfo info = new BannerInfo();
            info.id = c.getInt(columnId);
            info.plate = c.getInt(columnPlate);
            info.image = c.getString(columnImage);
            info.data = new BannerData();
            info.data.setType(c.getInt(columnType));
            info.data.setData(c.getString(columnData));
            banners.add(info);
        }
    }
    
    public static synchronized void insertPublicNumInfo (Context context, ArrayList<News> publicNums) {
        if (publicNums == null || publicNums.size() == 0) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }
        db.beginTransaction();
        try {
            for (News publicNum : publicNums) {
                long id = db.insert(CacheNewsInfo.TABLE_NAME, null, fillPublicNewsInfo(publicNum)) ;
                if (id == -1) {
                    Utils.logW(TAG, "Failed inserting data: " + publicNum.title);
                }
             }
             db.setTransactionSuccessful();
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             db.endTransaction();
         }
         db.close();
    }
    
    public static synchronized void deletePublicNumInfo (Context context) {
        
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }
        db.delete(CacheNewsInfo.TABLE_NAME, null, null);
        db.close();
    }
    
    public static ArrayList<News> loadPublicNumInfos (Context context) {
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return null;
        }
        Cursor c = db.query(CacheNewsInfo.TABLE_NAME, null, null, null, null, null, null, null);
        ArrayList<News> PublicNums = new ArrayList<News>();
        
        loadCachePublicNumlInfos(c, PublicNums);
        
        c.close();
        db.close();
        return PublicNums;
    }
    
    private static void loadCachePublicNumlInfos(Cursor c, ArrayList<News> publicNums) {
        int columnId = c.getColumnIndex(CacheNewsInfo.ID);
        int columnUrl = c.getColumnIndex(CacheNewsInfo.URL);
        int columnContentUrl = c.getColumnIndex(CacheNewsInfo.CONTENT_URL);
        int columnPublicUrl = c.getColumnIndex(CacheNewsInfo.PUBLIC_URL);
        int columnTitle = c.getColumnIndex(CacheNewsInfo.TITLE);
        int columnSummary = c.getColumnIndex(CacheNewsInfo.SUMMARY);
        int columnCover = c.getColumnIndex(CacheNewsInfo.COVER);
        int columnThumbnail = c.getColumnIndex(CacheNewsInfo.THUMBNAIL);
        int columnCreeateTime = c.getColumnIndex(CacheNewsInfo.CREATE_TIME);
        int columnSourceId = c.getColumnIndex(CacheNewsInfo.SOURCE_ID);
        int columnSourcename = c.getColumnIndex(CacheNewsInfo.SOURCE_NAME);
        int columnFavorite = c.getColumnIndex(CacheNewsInfo.FAVORITE);
        int columnLiked = c.getColumnIndex(CacheNewsInfo.LIKED);
        int columnLikedCount  = c.getColumnIndex(CacheNewsInfo.LIKED_COUNT);
        int columnCommentedCount  = c.getColumnIndex(CacheNewsInfo.COMMENTED_COUNT);
        int columnType  = c.getColumnIndex(CacheNewsInfo.TYPE);
        while (c.moveToNext()) {
            News pn = new News();
            pn.id = c.getInt(columnId);
            pn.url = c.getString(columnUrl);
            pn.contentUrl = c.getString(columnContentUrl);
            pn.publicUrl = c.getString(columnPublicUrl);
            pn.title = c.getString(columnTitle);
            pn.summary = c.getString(columnSummary);
            pn.cover = c.getString(columnCover);
            pn.thumbnail = c.getString(columnThumbnail);
            pn.createTime = c.getLong(columnCreeateTime);
            pn.sourceId = c.getInt(columnSourceId);
            pn.sourceName = c.getString(columnSourcename);
            pn.favorite = c.getInt(columnFavorite) == 1 ? true : false;
            pn.liked = c.getInt(columnLiked) == 1 ? true : false;
            pn.likedCount = c.getInt(columnLikedCount);
            pn.commentedCount = c.getInt(columnCommentedCount);
            pn.type = c.getInt(columnType);
            publicNums.add(pn);
        }
    }

    public static synchronized void insertCacheRoamSchoolInfo (Context context, ArrayList<SchoolInfo> roamschools) {
        if (roamschools == null || roamschools.size() == 0) {
            return;
        }
        
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }  
        
        db.beginTransaction();
        try {
            for (SchoolInfo roamschool : roamschools) {
               long id = db.insert(CacheRoamSchoolInfo.TABLE_NAME, null, fillRoamschoolInfo(roamschool)) ;
               if (id == -1) {
                   Utils.logW(TAG, "Failed inserting data: " + roamschool.name);
               }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        db.close();
    }
    
    public static synchronized void deleteRoamschoolInfo (Context context, int areaId) {
        if (areaId == 0) {
            return;
        }
        
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }
        db.delete(CacheRoamSchoolInfo.TABLE_NAME, CacheRoamSchoolInfo.AREA_ID + "=" + areaId + "", null);
        db.close();
    }
    
    public static synchronized void deleteRoamschoolInfo (Context context) {
        
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }
        db.delete(CacheRoamSchoolInfo.TABLE_NAME, CacheRoamSchoolInfo.IS_COLLECTION + "=" + 1 + "", null);
        db.close();
    }
    
    public static ArrayList<SchoolInfo> loadRoamschoolInfos (Context context, int areaId) {
        if (areaId == 0) {
            return null;
        }
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return null;
        }
        Cursor c = db.query(CacheRoamSchoolInfo.TABLE_NAME, null, CacheRoamSchoolInfo.AREA_ID + "=" + areaId + "", null, null, null, null, null);
        ArrayList<SchoolInfo> schoolInfos = new ArrayList<SchoolInfo>();
        
        loadCacheRoamschoolInfos(c, schoolInfos);
        
        c.close();
        db.close();
        return schoolInfos;
    }
    
    
     public static boolean getSchoolisUnlock(Context context, String schoolName) {
         
         if (schoolName == null || "".equals(schoolName)) {
             return false;
         }
         SQLiteDatabase db = getWritableDatabase(context);
         if (db == null) {
             return false;
         }
         Cursor c = db.query(CacheRoamSchoolInfo.TABLE_NAME, null, CacheRoamSchoolInfo.NAME + "='" + schoolName + "'", null, null, null, null, null);
         while (c.moveToNext()) {
             int columIsLock = c.getColumnIndex(CacheRoamSchoolInfo.UNLOCK);
             boolean unLock = c.getInt(columIsLock) == 1;
             return unLock;
         }
         c.close();
         db.close();
         return false;
     }
    
    
    public static ArrayList<SchoolInfo> loadRoamschoolInfos (Context context) {
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return null;
        }
        Cursor c = db.query(CacheRoamSchoolInfo.TABLE_NAME, null, CacheRoamSchoolInfo.IS_COLLECTION + "=" + 1 + "", null, null, null, null, null);
        ArrayList<SchoolInfo> schoolInfos = new ArrayList<SchoolInfo>();
        
        loadCacheRoamschoolInfos(c, schoolInfos);
        
        c.close();
        db.close();
        return schoolInfos;
    }
    
    private static void loadCacheRoamschoolInfos (Cursor c, ArrayList<SchoolInfo> schoolInfos) {
        int columnSchoolId = c.getColumnIndex(CacheRoamSchoolInfo.SCHOOL_ID);
        int columnAreaId = c.getColumnIndex(CacheRoamSchoolInfo.AREA_ID);
        int columName = c.getColumnIndex(CacheRoamSchoolInfo.NAME);
        int columPinyin = c.getColumnIndex(CacheRoamSchoolInfo.PINYIN);
        int columAbbreviation = c.getColumnIndex(CacheRoamSchoolInfo.ABBREVIATION);
        int columIscollection = c.getColumnIndex(CacheRoamSchoolInfo.IS_COLLECTION);
        int columIsLock = c.getColumnIndex(CacheRoamSchoolInfo.UNLOCK);
        int columVip = c.getColumnIndex(CacheRoamSchoolInfo.VIP);
        while (c.moveToNext()) {
            SchoolInfo info = new SchoolInfo();
            info.schoolId = c.getInt(columnSchoolId);
            info.areaId = c.getInt(columnAreaId);
            info.name = c.getString(columName);
            info.pinyin = c.getString(columPinyin);
            info.abbreviation = c.getString(columAbbreviation);
            info.isCollection = c.getInt(columIscollection) == 1 ? true :false;
            info.unlock = c.getInt(columIsLock) == 1 ? true : false;
            info.vip = c.getInt(columVip) == 1 ? true :false;
            schoolInfos.add(info);
        }
    }
    
    public static synchronized void insertCacheChannelInfo(Context context, ArrayList<CacheChannelInfo> channels) {
        if (channels == null || channels.size() == 0) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }

        db.beginTransaction();
        try {
            for (CacheChannelInfo channel : channels) {
                long id = db.insert(CacheChannelInfo.TABLE_NAME, null, fillChannelInfo(channel));
                if (id == -1) {
                    Utils.logW(TAG, "Failed inserting data: " + channel.name);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        db.close();
    }
    
    public static synchronized void deleteChannelInfo(Context context, int dayType) {
        if (dayType == 0) {
            return;
        }
        
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return ;
        }

        db.delete(CacheChannelInfo.TABLE_NAME, CacheChannelInfo.DAY_TYPE + "='" + dayType + "'", null);
        db.close();   
    }
    
    public static ArrayList<ChannelInfo> loadChannelInfos(Context context, int dayType) {
        if (dayType == 0) {
            return null;
        }
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return null;
        }

        Cursor c = db.query(CacheChannelInfo.TABLE_NAME, null, CacheChannelInfo.DAY_TYPE + "=" + dayType + "",
                null, null, null, null, null);
        ArrayList<ChannelInfo> channels = new ArrayList<ChannelInfo>();

        loadCacheChannels(c, channels);

        c.close();
        db.close();
        return channels;
    }
    
    public static synchronized void insertPostInfo(Context context, ArrayList<CachePostInfo> posts) {
        if (posts == null || posts.size() == 0) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }
        db.beginTransaction();
        try {
            for (CachePostInfo post : posts) {
                long id = db.insert(CachePostInfo.TABLE_NAME, null, fillPostInfo(post));
                if (id == -1) {
                    Utils.logW(TAG, "Failed inserting data: " + post.getId());
                } else {
                    Utils.logD(TAG, "insert cache post succ : " + post.postId + "-->forumId" +
                            post.forumId + "-->schoolId" + post.schoolId + ",id:" + id);
                }
            }
            Utils.logD(TAG, "insert cache post with:" + posts.size());
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        db.close();
    }
    
    public static synchronized void deletePostInfo(Context context, int forumId) {
        if (forumId <= 0) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }
        db.delete(CachePostInfo.TABLE_NAME, CachePostInfo.FORUM_ID + "='" +forumId + "'", null);
        db.close();
        Utils.logD(TAG, "Delete post info succ, forumId = " + forumId);
    }
    
    public static synchronized void deletePostInfoBySchoolId(Context context, int schoolId) {
        if (schoolId <= 0) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }
        db.delete(CachePostInfo.TABLE_NAME, CachePostInfo.SCHOOL_ID + "='" +schoolId + "' and " +
                CachePostInfo.IS_NIGHT_POST + "='" + 0 + "'", null);
        db.close();
    }
    
    public static ArrayList<CfPost> loadPostsInfo(Context context, long forumId) {
        ArrayList<CfPost> posts = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase(context);
        Cursor c = db.query(CachePostInfo.TABLE_NAME, null, CachePostInfo.IS_NIGHT_POST +"='" + 0 + "' and " 
                + CachePostInfo.FORUM_ID + "='" + forumId + "'", null, null, null, null);
        loadCachePosts(context, c, posts);
        c.close();
        db.close();
        Utils.logD(TAG, "load posts info with total " + posts.size());
        return posts;
    }
    
    public static synchronized ArrayList<CfPost> loadHotPostInfo(Context context, long forumId) {
        ArrayList<CfPost> posts = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase(context);
        String sql = CachePostInfo.IS_NIGHT_POST +"='" + 0 + "' and " 
                + CachePostInfo.FORUM_ID + "='" + forumId + "' and " + CachePostInfo.HOT + "= '1'";
        Cursor c = db.query(CachePostInfo.TABLE_NAME, null, sql, null, null, null, null);
        loadCachePosts(context, c, posts);
        c.close();
        db.close();
        Utils.logD(TAG, "load posts info with total " + posts.size());
        return posts;        
    }
    
    public static ArrayList<CfPost> loadPostsInfoBySchoolId(Context context, long schoolId) {
        ArrayList<CfPost> posts = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase(context);
        Cursor c = db.query(CachePostInfo.TABLE_NAME, null, CachePostInfo.IS_NIGHT_POST +"='" + 0 + "' and " 
                + CachePostInfo.SCHOOL_ID + "='" + schoolId + "'", null, null, null, null);
        loadCachePosts(context, c, posts);
        c.close();
        db.close();
        Utils.logD(TAG, "load posts info with total " + posts.size());
        return posts;
    }

    private static void loadCachePosts(Context context, Cursor c, ArrayList<CfPost> posts) {
        int columnPostId = c.getColumnIndex(CachePostInfo.POST_ID);
        int columnContent = c.getColumnIndex(CachePostInfo.CONTENT);
        int columnCreateOn = c.getColumnIndex(CachePostInfo.CREATED_ON);
        int columnReplyCount = c.getColumnIndex(CachePostInfo.REPLY_COUNT);
        int columnUpCount = c.getColumnIndex(CachePostInfo.UP_COUNT);
        int columnDownCount = c.getColumnIndex(CachePostInfo.DOWN_COUNT);
        int columnMyUp = c.getColumnIndex(CachePostInfo.MY_UP);
        int columnMyDown = c.getColumnIndex(CachePostInfo.MY_DOWN);
        int columnImages = c.getColumnIndex(CachePostInfo.IMAGE_IDS);
        int columnLikedUserIds = c.getColumnIndex(CachePostInfo.LIKED_USER_IDS);
        int columnForumId = c.getColumnIndex(CachePostInfo.FORUM_ID);
        int columnPubicUrl = c.getColumnIndex(CachePostInfo.PUBLIC_URL);
        int columnType = c.getColumnIndex(CachePostInfo.TYPE);
        int columnThreadType = c.getColumnIndex(CachePostInfo.THREAD_TYPE);
        int columnSameSchool = c.getColumnIndex(CachePostInfo.SAME_SCHOOL);
        int columnVoiceLength = c.getColumnIndex(CachePostInfo.VOICE_LENGTH);
        int columnHot = c.getColumnIndex(CachePostInfo.HOT);
        int columnReadCount = c.getColumnIndex(CachePostInfo.READ_COUNT);
        int columnForumType = c.getColumnIndex(CachePostInfo.FORUM_TYPE);
        int columnStyle = c.getColumnIndex(CachePostInfo.STYLE);
        int columnVoicePath = c.getColumnIndex(CachePostInfo.VOICE_PATH);
        int columnEndTime = c.getColumnIndex(CachePostInfo.END_TIME);
        int columnEnabled = c.getColumnIndex(CachePostInfo.ENABLED);
        int columnMyVote = c.getColumnIndex(CachePostInfo.MY_VOTE);
        int columnMyVoteTime = c.getColumnIndex(CachePostInfo.MY_VOTE_TIME);
        int columnOptions = c.getColumnIndex(CachePostInfo.OPTIONS);
        int columnResult = c.getColumnIndex(CachePostInfo.RESULT);
        int columnUserId = c.getColumnIndex(CachePostInfo.USER_ID);
        int columnStickLvl = c.getColumnIndex(CachePostInfo.STICKLVL);
        int columnTitle = c.getColumnIndex(CachePostInfo.TITLE);
        int columnReplyTime = c.getColumnIndex(CachePostInfo.REPLY_TIME);
        int columnVideoThumbmail = c.getColumnIndex(CachePostInfo.VIDEO_THUMBNAIL);
        int columnVideoUrl = c.getColumnIndex(CachePostInfo.VIDEO_URL);
        int columnModeratorUids = c.getColumnIndex(CachePostInfo.MODERATOR_UIDS);
        int columnSuperModeratorUids = c.getColumnIndex(CachePostInfo.SUPER_MODERATOR_UIDS);
        while(c.moveToNext()){
            CfPost post = new CfPost();
            post.postId = c.getInt(columnPostId);
            post.content = c.getString(columnContent);
            post.createdOn = c.getLong(columnCreateOn);
            post.replyCount = c.getInt(columnReplyCount);
            post.upCount = c.getInt(columnUpCount);
            post.downCount = c.getInt(columnDownCount);
            post.myUp = c.getInt(columnMyUp) == 1 ? true : false;
            post.myDown = c.getInt(columnMyDown) == 1 ? true : false;
            post.imageId = c.getInt(columnImages);
            post.images = loadCacheImage(context, c.getInt(columnImages));
            post.forumId = c.getInt(columnForumId);
            post.publicUrl = c.getString(columnPubicUrl);
            post.type = c.getInt(columnType);
            post.threadType = c.getInt(columnThreadType);
            post.sameSchool = c.getInt(columnSameSchool) == 1 ? true : false;
            post.voiceLength = c.getInt(columnVoiceLength);
            post.hot = c.getInt(columnHot);
            post.readCount = c.getInt(columnReadCount);
            post.forumType = c.getInt(columnForumType);
            post.style = c.getInt(columnStyle);
            post.voice = new Attachment();
            post.voice.url = c.getString(columnVoicePath);
            post.vote = new Vote();
            post.vote.endTime = c.getLong(columnEndTime);
            post.vote.enabled = c.getInt(columnEnabled) == 1 ? true : false;
            post.vote.myVote  = string2IntList(c.getString(columnMyVote));
            post.vote.myVoteTime = c.getLong(columnMyVoteTime);
            post.optionId = c.getLong(columnOptions);
            post.vote.options = loadVoteInfo(context, c.getLong(columnOptions));
            post.vote.result = string2IntList(c.getString(columnResult));
            post.userInfo = loadCacheUserInfo(context, c.getLong(columnUserId));
            post.likedUsers = getLikedUser(context, c.getString(columnLikedUserIds));
            post.stickLvl = c.getInt(columnStickLvl);
            post.title = c.getString(columnTitle);
            post.replyTime = c.getLong(columnReplyTime);
            post.videoThumbnail = c.getString(columnVideoThumbmail);
            post.videoUrl = c.getString(columnVideoUrl);
            post.moderatorUids = loadModeratorUidsInfo(c.getString(columnModeratorUids));
            post.superModeratorUids = loadModeratorUidsInfo(c.getString(columnSuperModeratorUids));
            posts.add(post);
        }
    }
    
    private static int[] loadModeratorUidsInfo(String uids) {
        if (TextUtils.isEmpty(uids)) {
            return null;
        }
        String[] uid = uids.split(",");
        int[] uidInts = new int[uid.length];
        for (int i = 0; i < uidInts.length; i++) {
            try {
                uidInts[i] = Integer.parseInt(uid[i]);
            } catch (Exception e) {
                uidInts[i] = 0;
            }
        }
        return uidInts;
    }

    private static ArrayList<UserInfo> getLikedUser(Context context, String ids) {
        ArrayList<UserInfo> users = new ArrayList<>();
        if (ids == null || ids.length() == 0) {
            return users;
        }
        String[] userIds = ids.split(",");
        for (int i = 0; i < userIds.length; i++) {
            UserInfo userInfo = loadCacheUserInfo(context, Integer.parseInt(userIds[i]));
            if (userInfo != null) {
                users.add(userInfo);
            }
        }
        return users;
    }

    public static synchronized long insertCacheImages(Context context, CacheImageInfo image) {
        if (image == null || image.count == 0) {
            return 0;
        }
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return 0;
        }
        long id = db.insert(CacheImageInfo.TABLE_NAME, null, fillCacheImage(image));
        if (id == -1) {
            Utils.logE(TAG, "insert cache image into db error " + image.count);
        }
        db.close();
        return id;
    }
    
    public static synchronized void deleteCacheImage(Context context, ArrayList<String> ids) {
        if (ids == null || ids.size() == 0) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase(context);
        db.beginTransaction();
        try {
            for (String id : ids) {
                db.delete(CacheImageInfo.TABLE_NAME, CacheImageInfo.ID + "='" + id + "'", null);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        db.close();
    }
    
    public static ArrayList<Image> loadCacheImage(Context context, int id) {
        ArrayList<Image> images = new ArrayList<>();
        if (id == 0) {
            return images;
        }
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return images;
        }
        Cursor cursor = db.query(CacheImageInfo.TABLE_NAME, null,
                CacheImageInfo.ID + "='" + id + "'", null, null, null, null);
        loadImages(cursor, images);
        cursor.close();
        db.close();
        return images;
    }
    
    public static synchronized void insertCacheUserInfo(Context context, UserInfo userInfo) {
        if (userInfo == null) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }
        long id = db.insert(CacheUserInfo.TABLE_NAME, null, fillCacheUserInfo(userInfo));
        db.close();
        if (id == -1) {
            Utils.logE(TAG, "insert cache user info error:" + userInfo.account);
        } else {
            Utils.logD(TAG, "insert cache userinfo succ --> id : " + id + ",userId : " + userInfo.userId);
        }
    }
    
    public static UserInfo loadCacheUserInfo(Context context, long uid) {
        if (uid <= 0) {
            return null;
        }
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return null;
        }
        Cursor c = db.query(CacheUserInfo.TABLE_NAME, null, CacheUserInfo.USER_ID + "='" + uid + "'",
                null, null, null, null);
        ArrayList<UserInfo> users = new ArrayList<>();
        loadCacheUserInfo(c, users);
        c.close();
        db.close();
        return users == null || users.size() == 0 ? null : users.get(0);
    }
    
    public static synchronized void updateCacheUserInfo(Context context, UserInfo user) {
        if (user == null) {
            return ;
        }
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return;
        }
        
        int index = db.update(CacheUserInfo.TABLE_NAME, fillCacheUserInfo(user),
                CacheUserInfo.USER_ID + "='" + user.userId + "'", null);
        if (index == -1) {
            Utils.logE(TAG, "update cache user info error:" + user.userId);
        }
        db.close();
    }
    
    private static void loadCacheUserInfo(Cursor c, ArrayList<UserInfo> users) {
        int columnAccount = c.getColumnIndex(CacheUserInfo.ACCOUNT);
        int columnType = c.getColumnIndex(CacheUserInfo.TYPE);
        int columnIds = c.getColumnIndex(CacheUserInfo.IDS);
        int columnName = c.getColumnIndex(CacheUserInfo.NAME);
        int columnNick = c.getColumnIndex(CacheUserInfo.NICKNAME);
        int columnNickNight = c.getColumnIndex(CacheUserInfo.NICK_NIGHT);
        int columnGender = c.getColumnIndex(CacheUserInfo.GENDER);
        int columnDes = c.getColumnIndex(CacheUserInfo.SHORT_DESC);
        int columnAvatar = c.getColumnIndex(CacheUserInfo.AVATAR);
        int columnAvatarNight = c.getColumnIndex(CacheUserInfo.AVATAR_NIGHT);
        int columnAvaFull = c.getColumnIndex(CacheUserInfo.AVATAR_FULL);
        int columnBg = c.getColumnIndex(CacheUserInfo.BG_IMG);
        int columnPbimg = c.getColumnIndex(CacheUserInfo.PRIMARY_BADGE_IMAGE);
        int columnSchoolId = c.getColumnIndex(CacheUserInfo.SCHOOL_ID);
        int columnSchool = c.getColumnIndex(CacheUserInfo.SCHOOL);
        int columnDepId = c.getColumnIndex(CacheUserInfo.DEPARTMENT_ID);
        int columnDepartment = c.getColumnIndex(CacheUserInfo.DEPARTMENT);
        int columnVip = c.getColumnIndex(CacheUserInfo.VIP);
        int columnUserId = c.getColumnIndex(CacheUserInfo.USER_ID);
        while (c.moveToNext()) {
            UserInfo user = new UserInfo();
            user.account = c.getString(columnAccount);
            user.type = c.getInt(columnType);
            user.ids = c.getInt(columnIds) == 1 ? true : false;
            user.name = c.getString(columnName);
            user.nickname = c.getString(columnNick);
            user.gender = c.getInt(columnGender);
            user.shortDesc = c.getString(columnDes);
            user.setAvatar(c.getString(columnAvatar));
            user.avatarFull = c.getString(columnAvaFull);
            user.bgImg = c.getString(columnBg);
            user.primaryBadgeImage = c.getString(columnPbimg);
            user.schoolId = c.getInt(columnSchoolId);
            user.school = c.getString(columnSchool);
            user.departmentId = c.getInt(columnDepId);        
            user.department = c.getString(columnDepartment);
            user.vip = c.getInt(columnVip) == 1 ? true : false;
            user.userId = c.getInt(columnUserId);
            users.add(user);
        }
    }

    private static ContentValues fillCacheUserInfo(UserInfo userInfo) {
        ContentValues value = new ContentValues();
        value.put(CacheUserInfo.ACCOUNT, userInfo.account);
        value.put(CacheUserInfo.TYPE, userInfo.type);
        value.put(CacheUserInfo.IDS, userInfo.ids);
        value.put(CacheUserInfo.NAME, userInfo.name);
        value.put(CacheUserInfo.NICKNAME, userInfo.nickname);
        value.put(CacheUserInfo.GENDER, userInfo.gender);
        value.put(CacheUserInfo.SHORT_DESC, userInfo.shortDesc);
        value.put(CacheUserInfo.AVATAR, userInfo.getAvatar());
        value.put(CacheUserInfo.AVATAR_FULL, userInfo.avatarFull);
        value.put(CacheUserInfo.BG_IMG, userInfo.bgImg);
        value.put(CacheUserInfo.PRIMARY_BADGE_IMAGE, userInfo.primaryBadgeImage);
        value.put(CacheUserInfo.SCHOOL_ID, userInfo.schoolId);
        value.put(CacheUserInfo.SCHOOL, userInfo.school);
        value.put(CacheUserInfo.DEPARTMENT_ID, userInfo.departmentId);
        value.put(CacheUserInfo.DEPARTMENT, userInfo.department);
        value.put(CacheUserInfo.USER_ID, userInfo.userId);
        value.put(CacheUserInfo.VIP, userInfo.vip);
        return value;
    }

    private static void loadImages(Cursor c, ArrayList<Image> images) {
        int columnCount = c.getColumnIndex(CacheImageInfo.COUNT);
        int column1 = c.getColumnIndex(CacheImageInfo.URL1);
        int column2 = c.getColumnIndex(CacheImageInfo.URL2);
        int column3 = c.getColumnIndex(CacheImageInfo.URL3);
        int column4 = c.getColumnIndex(CacheImageInfo.URL4);
        int column5 = c.getColumnIndex(CacheImageInfo.URL5);
        int column6 = c.getColumnIndex(CacheImageInfo.URL6);
        int column7 = c.getColumnIndex(CacheImageInfo.URL7);
        int column8 = c.getColumnIndex(CacheImageInfo.URL8);
        int column9 = c.getColumnIndex(CacheImageInfo.URL9);
        int[] cols = new int[] { column1, column2, column3, column4, column5, column6, column7, column8, column9 };
        while (c.moveToNext()) {
            int count = c.getInt(columnCount);
            if (count == 0) {
                return;
            }
            count = count >= 9 ? 9 : count;
            for (int i = 0; i < count; i++) {
                Image image = new Image();
                image.image = c.getString(cols[i]);
                images.add(image);
            }
        }
    }

    public static synchronized long insertVoteInfo(Context context, CacheVoteInfo vote) {
        if (vote == null) {
            return 0;
        }
        SQLiteDatabase db = getWritableDatabase(context);
        if (db == null) {
            return 0;
        }
        long id = db.insert(CacheVoteInfo.TABLE_NAME, null, fillCacheVoteInfo(vote));
        if (id == -1) {
            Utils.logE(TAG, "insert cache vote info error " + vote.count);
            return 0;
        }
        db.close();
        return id;
    }
    
    public static synchronized void deleteVoteInfo(Context context, ArrayList<String> ids) {
        if (ids == null || ids.size() == 0) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase(context);
        db.beginTransaction();
        try {
            for (String id : ids) {
                db.delete(CacheVoteInfo.TABLE_NAME, CacheVoteInfo.ID + "='" + id + "'", null);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public static ArrayList<String> loadVoteInfo(Context context, long id) {
        ArrayList<String> options = new ArrayList<>();
        if (id <= 0) {
            return options;
        }
        SQLiteDatabase db = getReadableDatabase(context);
        if (db == null) {
            return options;
        }
        Cursor c = db.query(CacheVoteInfo.TABLE_NAME, null, CacheVoteInfo.ID + "='" + id +"'",
                null, null, null, null);
        loadCacheVoteOpts(c, options);
        c.close();
        db.close();
        return options;
    }
    
    private static void loadCacheVoteOpts(Cursor c, ArrayList<String> options) {
        int columnCount = c.getColumnIndex(CacheVoteInfo.COUNT);
        int column1 = c.getColumnIndex(CacheVoteInfo.OPT1);
        int column2 = c.getColumnIndex(CacheVoteInfo.OPT2);
        int column3 = c.getColumnIndex(CacheVoteInfo.OPT3);
        int column4 = c.getColumnIndex(CacheVoteInfo.OPT4);
        int cols[] = new int[] { column1, column2, column3, column4 };
        while (c.moveToNext()) {
            int count = c.getInt(columnCount);
            if (count == 0) {
                return;
            }
            
            for (int i = 0; i < count; i++) {
                String opt = c.getString(cols[i]);
                options.add(opt);
            }
        }
    }

    private static ContentValues fillCacheVoteInfo(CacheVoteInfo vote) {
        ContentValues value = new ContentValues();
        value.put(CacheVoteInfo.COUNT, vote.count);
        value.put(CacheVoteInfo.OPT1, vote.option1);
        value.put(CacheVoteInfo.OPT2, vote.option2);
        value.put(CacheVoteInfo.OPT3, vote.option3);
        value.put(CacheVoteInfo.OPT4, vote.option4);
        return value;
    }

    private static ContentValues fillCacheImage(CacheImageInfo image) {
        ContentValues value = new ContentValues();
        value.put(CacheImageInfo.COUNT, image.count);
        value.put(CacheImageInfo.URL1, image.url1);
        value.put(CacheImageInfo.URL2, image.url2);
        value.put(CacheImageInfo.URL3, image.url3);
        value.put(CacheImageInfo.URL4, image.url4);
        value.put(CacheImageInfo.URL5, image.url5);
        value.put(CacheImageInfo.URL6, image.url6);
        value.put(CacheImageInfo.URL7, image.url7);
        value.put(CacheImageInfo.URL8, image.url8);
        value.put(CacheImageInfo.URL9, image.url9);
        return value;
    }

    private static int[] string2IntList(String string) {
        String[] strs = string.split(",");
        int[] result = new int[strs.length];
        for (int i = 0; i < strs.length; i++) {
            if (strs[i].length() > 0) {
                result[i] = Integer.parseInt(strs[i]);
            } else {
                result[i] = 0;
            }
        }
        return result;
    }

    private static ContentValues fillPostInfo(CachePostInfo post) {
        ContentValues value = new ContentValues();
        value.put(CachePostInfo.POST_ID, post.postId);
        value.put(CachePostInfo.CONTENT, post.content);
        value.put(CachePostInfo.CREATED_ON, post.createdOn);
        value.put(CachePostInfo.REPLY_COUNT, post.replyCount);
        value.put(CachePostInfo.UP_COUNT, post.upCount);
        value.put(CachePostInfo.DOWN_COUNT, post.downCount);
        value.put(CachePostInfo.MY_UP, post.myUp);
        value.put(CachePostInfo.MY_DOWN, post.myDown);
        value.put(CachePostInfo.IMAGE_IDS, post.imageId);
        value.put(CachePostInfo.FORUM_ID, post.forumId);
        value.put(CachePostInfo.PUBLIC_URL, post.publicUrl);
        value.put(CachePostInfo.TYPE, post.type);
        value.put(CachePostInfo.THREAD_TYPE, post.threadType);
        value.put(CachePostInfo.SAME_SCHOOL, post.sameSchool);
        value.put(CachePostInfo.VOICE_LENGTH, post.voiceLength);
        value.put(CachePostInfo.HOT, post.hot);
        value.put(CachePostInfo.READ_COUNT, post.readCount);
        value.put(CachePostInfo.FORUM_TYPE, post.forumType);
        value.put(CachePostInfo.STYLE, post.style);
        value.put(CachePostInfo.VOICE_PATH, post.voice.url);
        value.put(CachePostInfo.IS_NIGHT_POST, post.isNightPost);
        value.put(CachePostInfo.END_TIME, post.vote == null ? 0 : post.vote.endTime);
        value.put(CachePostInfo.ENABLED, post.vote == null ? false : post.vote.enabled);
        value.put(CachePostInfo.MY_VOTE, post.getVoteOfMineStr());
        value.put(CachePostInfo.MY_VOTE_TIME, post.vote == null ? 0 : post.vote.myVoteTime);
        value.put(CachePostInfo.OPTIONS, post.optionId);
        value.put(CachePostInfo.RESULT, post.getVoteResultStr());
        value.put(CachePostInfo.USER_ID, post.userInfo.userId);
        value.put(CachePostInfo.SCHOOL_ID, post.schoolId);
        value.put(CachePostInfo.LIKED_USER_IDS, post.likedUserIds);
        value.put(CachePostInfo.STICKLVL, post.stickLvl);
        value.put(CachePostInfo.TITLE, post.title);
        value.put(CachePostInfo.REPLY_TIME, post.replyTime);
        value.put(CachePostInfo.VIDEO_THUMBNAIL, post.videoThumbnail);
        value.put(CachePostInfo.VIDEO_URL, post.videoUrl);
        value.put(CachePostInfo.MODERATOR_UIDS, post.getModeratorUids());
        value.put(CachePostInfo.SUPER_MODERATOR_UIDS, post.getSuperModeratorUids());
        return value;
    }

    private static void loadCacheChannels(Cursor c, ArrayList<ChannelInfo> channels) {
        int columnBg = c.getColumnIndex(CacheChannelInfo.BG_IMG);
        int columnIcon = c.getColumnIndex(CacheChannelInfo.ICON);
        int columnIntro = c.getColumnIndex(CacheChannelInfo.INTRO);
        int columnName = c.getColumnIndex(CacheChannelInfo.NAME);
        int columnRuleContent = c.getColumnIndex(CacheChannelInfo.RULE_CONTENT);
        int columnRuleIcon = c.getColumnIndex(CacheChannelInfo.RULE_ICON);
        int columnRuleTitle = c.getColumnIndex(CacheChannelInfo.RULE_TITLE);
        int columnTitle = c.getColumnIndex(CacheChannelInfo.TITLE);
        int columnForumId = c.getColumnIndex(CacheChannelInfo.FORUM_ID);
        int columnHavaRule = c.getColumnIndex(CacheChannelInfo.HAVA_RULE);
        int columnStyle = c.getColumnIndex(CacheChannelInfo.STYLE);
        int columnThreadType = c.getColumnIndex(CacheChannelInfo.THREAD_TYPE);
        int columnType = c.getColumnIndex(CacheChannelInfo.TYPE);
        while (c.moveToNext()) {
            ChannelInfo info = new ChannelInfo();
            info.bgImg = c.getString(columnBg);
            info.icon = c.getString(columnIcon);
            info.intro = c.getString(columnIntro);
            info.name = c.getString(columnName);
            info.ruleContent = c.getString(columnRuleContent);
            info.ruleIcon = c.getString(columnRuleIcon);
            info.ruleTitle = c.getString(columnRuleTitle);
            info.title = c.getString(columnTitle);
            info.forumId = c.getInt(columnForumId);
            info.havaRule = c.getInt(columnHavaRule) == 1 ? true : false;
            info.style = c.getInt(columnStyle);
            info.threadType = c.getInt(columnThreadType);
            info.type = c.getInt(columnType);
            channels.add(info);
        }
    }

    private static ContentValues fillChannelInfo(CacheChannelInfo channel) {
        ContentValues values = new ContentValues();
        values.put(CacheChannelInfo.BG_IMG, channel.bgImg);
        values.put(CacheChannelInfo.ICON, channel.icon);
        values.put(CacheChannelInfo.INTRO, channel.intro);
        values.put(CacheChannelInfo.NAME, channel.name);
        values.put(CacheChannelInfo.RULE_CONTENT, channel.ruleContent);
        values.put(CacheChannelInfo.RULE_ICON, channel.ruleIcon);
        values.put(CacheChannelInfo.RULE_TITLE, channel.ruleTitle);
        values.put(CacheChannelInfo.TITLE, channel.title);
        values.put(CacheChannelInfo.FORUM_ID, channel.forumId);
        values.put(CacheChannelInfo.HAVA_RULE, channel.havaRule);
        values.put(CacheChannelInfo.STYLE, channel.style);
        values.put(CacheChannelInfo.THREAD_TYPE, channel.threadType);
        values.put(CacheChannelInfo.TYPE, channel.type);
        values.put(CacheChannelInfo.DAY_TYPE, channel.dayType);
        return values;
    }

    private static ContentValues fillBannerInfo(BannerInfo banner) {
        ContentValues values = new ContentValues();
        values.put(CacheBannerInfo.PLATE, banner.getPlate());
        values.put(CacheBannerInfo.ID, banner.getId());
        values.put(CacheBannerInfo.DATA, banner.getData() == null ? null : banner.getData().getData());
        values.put(CacheBannerInfo.IMAGE, banner.getImage());
        values.put(CacheBannerInfo.TYPE, banner.getData() == null ? null : banner.getData().getType());
        return values;
    }
    
    private static ContentValues fillPublicNewsInfo (News publicNews) {
        ContentValues values = new ContentValues();
        values.put(CacheNewsInfo.ID, publicNews.id);
        values.put(CacheNewsInfo.URL, publicNews.url);
        values.put(CacheNewsInfo.CONTENT_URL, publicNews.contentUrl);
        values.put(CacheNewsInfo.PUBLIC_URL, publicNews.publicUrl);
        values.put(CacheNewsInfo.TITLE, publicNews.title);
        values.put(CacheNewsInfo.SUMMARY, publicNews.summary);
        values.put(CacheNewsInfo.COVER, publicNews.cover);
        values.put(CacheNewsInfo.THUMBNAIL, publicNews.thumbnail);
        values.put(CacheNewsInfo.CREATE_TIME, publicNews.createTime);
        values.put(CacheNewsInfo.SOURCE_ID, publicNews.sourceId);
        values.put(CacheNewsInfo.SOURCE_NAME, publicNews.sourceName);
        values.put(CacheNewsInfo.FAVORITE, publicNews.favorite);
        values.put(CacheNewsInfo.LIKED, publicNews.liked);
        values.put(CacheNewsInfo.LIKED_COUNT, publicNews.likedCount);
        values.put(CacheNewsInfo.COMMENTED_COUNT, publicNews.commentedCount);
        values.put(CacheNewsInfo.TYPE, publicNews.type);
        return values;
    }
    
    private static ContentValues fillRoamschoolInfo (SchoolInfo school) {
        ContentValues values = new ContentValues();
        values.put(CacheRoamSchoolInfo.SCHOOL_ID, school.getSchoolId());
        values.put(CacheRoamSchoolInfo.AREA_ID, school.getArea_id());
        values.put(CacheRoamSchoolInfo.NAME, school.getName());
        values.put(CacheRoamSchoolInfo.PINYIN, school.getPinyin());
        values.put(CacheRoamSchoolInfo.ABBREVIATION, school.getAbbreviation());
        values.put(CacheRoamSchoolInfo.IS_COLLECTION, school.isCollection());
        values.put(CacheRoamSchoolInfo.UNLOCK, school.isUnlock());
        values.put(CacheRoamSchoolInfo.VIP, school.isVip());
        return values;
    }
    
    public static synchronized void cleanAllData(Context context) {
        context.deleteDatabase(DatabaseOpenHelper.DB_NAME);
        context.getSharedPreferences(NAME_SHARED_PREFS, 0).edit().clear().commit();
    }

    /**
     * There are many cases we need to save some light weight data, and it's a
     * good idea to save it into shared preferences in most cases.
     */
    public static SharedPreferences getSpForData(Context ctx) {
        return ctx.getSharedPreferences(NAME_SHARED_PREFS, 0);
    }

    public static SQLiteDatabase getReadableDatabase(Context context) {
        if (context == null) {
            return null;
        }
        try {
            SQLiteDatabase db = new DatabaseOpenHelper(context).getReadableDatabase();
            return db;
        } catch (SQLiteException e) {
            Utils.logE(TAG, e.getMessage());
        }
        return null;
    }

    public static SQLiteDatabase getWritableDatabase(Context context) {
        if (context == null) {
            return null;
        }
        try {
            return new DatabaseOpenHelper(context).getWritableDatabase();
        } catch (SQLiteException e) {
            Utils.logE(TAG, e.getMessage());
        }
        return null;
    }
    
    public static boolean isTableExist(Context context, String tableName) {
        boolean result = false;
        if (tableName == null) {
            return false;
        }
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase(context);
            String sql = "select count(*) as c from sqlite_master " +
                    " where type ='table' and name ='" + tableName.trim() + "' ";
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return result;
    }
}
