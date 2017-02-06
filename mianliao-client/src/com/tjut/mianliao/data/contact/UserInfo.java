package com.tjut.mianliao.data.contact;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.tjut.mianliao.R;
import com.tjut.mianliao.contact.UserRemarkManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.BannedInfo;
import com.tjut.mianliao.data.Medal;
import com.tjut.mianliao.data.Photo;
import com.tjut.mianliao.sidebar.PinyinUtil;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 * A full contact is consist of 2 parts:
 * <p/>
 * 1) UserEntry: only indicates contact relationship with others;
 * <p/>
 * 2) UserInfo: basic user information which are commonly required;
 */
public class UserInfo implements Parcelable {

    public static final String INTENT_EXTRA_INFO = "extra_user_info";

    public static final int EDU_TYPE_ZK = 0;

    public static final int AVATAR_MAX_SIZE = 800;

    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 0;

    public static final int TYPE_OTHER = 0;
    public static final int TYPE_STUDENT = 1;
    public static final int TYPE_TEACHER = 2;
    public static final int TYPE_COMMON = 3;
    public static final int TYPE_BUSINESS = 4;
    public static final int TYPE_NEWBIE = 5;

    public static final int DEGREE_DIPLOMA = 0;
    public static final int DEGREE_BACHELOR = 1;
    public static final int DEGREE_MASTER = 2;

    public static final String AMOUNT = "amount";

    public static final String TABLE_NAME = "user_info";
    public static final String USER_ID = "uid";
    public static final String GUID = "guid";
    public static final String TYPE = "user_type";
    public static final String IDS = "ids";
    public static final String ACCOUNT = "account";
    public static final String NAME = "name";
    public static final String NICKNAME = "nick";
    public static final String PHONE = "phone";
    public static final String EMAIL = "email";
    public static final String EMAIL_VERIFIED = "email_v";
    public static final String GENDER = "gender";
    public static final String SHORT_DESC = "short_desc";
    public static final String AVATAR = "avatar";
    public static final String AVATAR_FULL = "avatar_full";
    public static final String BG_IMG = "bg_img";
    public static final String PRIMARY_BADGE = "primary_badge";
    public static final String PRIMARY_BADGE_IMAGE = "primary_badge_image";
    public static final String LAST_UPDATE = "last_update";

    public static final String SCHOOL_ID = "school_id";
    public static final String SCHOOL = "school";
    public static final String DEPARTMENT_ID = "department_id";
    public static final String DEPARTMENT = "department";
    public static final String START_YEAR = "start_year";

    public static final String FACE_ID = "face_id";
    public static final String DISTANCE = "distance";
    public static final String TIME_DELTA = "time_delta";

    public static final String POINTS = "points";

    public static final String MEDALS = "badges";
    public static final String PHOTOS = "photos";
    public static final String DEGREE = "education";

    public static final String DOWN_COUNT = "down_count";
    public static final String UP_COUNT = "up_count";
    public static final String FLOWER_COUNT = "flower_count";
    public static final String EGG_COUNT = "egg_count";
    public static final String EDUCATION = "education";
    public static final String OTHER_SCHOLL_ID = "other_school_id";
    public static final String IDENTITY_STATUS = "education";
    public static final String VIP = "vip";
    public static final String GOLD = "price";
    public static final String CREDIT = "credit";
    public static final String POST_COUNT = "thread_count";
    public static final String VIP_END_TIME = "vip_endtime";
    public boolean isUpload;
    public int amount;
    public int userId;
    public String guid;
    public String account;
    public int type;
    public boolean ids;
    public String jid;
    public String name;
    public String nickname;
    public String phone;
    public String email;
    public boolean emailVerified;
    public int gender;
    public String shortDesc;
    private String avatar;
    public String avatarFull;
    public String bgImg;
    public String primaryBadgeImage;
    public String lastUpdate;

    public int schoolId;
    public String school;
    public int departmentId;
    public String department;
    public String startYear;

    public String faceId;
    public int distance;
    public int timeDelta;
    public int degree;

    public int points;
    public int flowerCount;
    public int eggCount;
    public int education;
    public int upCount;
    public int downCount;
    public int otherSchollId;
    public int identityStatus;
    public int postCount;
    public int gold;
    public int credit;
    public boolean vip;
    public long vipEndTime;
    public String alpha;

    public ArrayList<String> relations;
    private ArrayList<Medal> mMedals;
    public ArrayList<Photo> photos;

    // add
    public boolean isCanDel;
    // add by 4.1.0
    public int followCount;
    public int fansCount;

    // debug
    private boolean mShowSchoolOnly = true;

    public long visitTime;

    public boolean isModerator;

    public ArrayList<BannedInfo> bannedInfos;
    public ArrayList<BannedInfo> managedInfos;

    public UserInfo() {
    }

    public void copy(UserInfo info) {
        amount = info.amount;
        userId = info.userId;
        guid = info.guid;
        account = info.account;
        type = info.type;
        ids = info.ids;
        jid = info.jid;
        name = info.name;
        nickname = info.nickname;
        phone = info.phone;
        email = info.email;
        emailVerified = info.emailVerified;
        gender = info.gender;
        shortDesc = info.shortDesc;
        setAvatar(info.getAvatar());
        avatarFull = info.avatarFull;
        bgImg = info.bgImg;
        primaryBadgeImage = info.primaryBadgeImage;
        lastUpdate = info.lastUpdate;
        schoolId = info.schoolId;
        school = info.school;
        departmentId = info.departmentId;
        department = info.department;
        startYear = info.startYear;
        faceId = info.faceId;
        distance = info.distance;
        timeDelta = info.timeDelta;
        points = info.points;
        degree = info.degree;
        vip = info.vip;
        upCount = info.upCount;
        downCount = info.downCount;
        education = info.education;
        flowerCount = info.flowerCount;
        eggCount = info.eggCount;
        otherSchollId = info.otherSchollId;
        gold = info.gold;
        credit = info.credit;
        identityStatus = info.identityStatus;
        postCount = info.postCount;
        vipEndTime = info.vipEndTime;
        alpha = info.alpha;
        followCount = info.followCount;
        fansCount = info.fansCount;
    }

    public static String getDegreeName(Context context, int degree) {
        String degreeName = "";

        int[] degreeIds = {R.string.prof_degree_diploma, R.string.prof_degree_diploma,
                R.string.prof_degree_diploma};

        if (degree > (degreeIds.length - 1) || degree < 0) {
            return degreeName;
        } else {
            return context.getString(degreeIds[degree]);
        }

    }


    public String getDegreeName(Context context) {
        return getDegreeName(context, degree);
    }

    public static boolean isFemale(int userGender) {
        return userGender == UserInfo.GENDER_FEMALE;
    }

    public static int getDefaultAvatar(int userGender) {
        return isFemale(userGender) ? R.drawable.chat_botton_bg_favicongirl : R.drawable.chat_botton_bg_faviconboy;
    }

    public static int getGenderResId(int userGender) {
        return isFemale(userGender) ? R.string.prof_female : R.string.prof_male;
    }

    public int defaultAvatar() {
        return getDefaultAvatar(gender);
    }

    public int getGenderIcon() {
        return isFemale(gender) ? R.drawable.img_girl : R.drawable.img_boy;
    }

    public boolean isMine(Context context) {
        int myUid = AccountInfo.getInstance(context).getUserId();
        return myUid > 0 && myUid == userId;
    }

    public boolean isVerified() {
        return ids || type == TYPE_STUDENT || type == TYPE_TEACHER || type == TYPE_BUSINESS
                || type == TYPE_NEWBIE;
    }

    public boolean hasFaceId() {
        return !TextUtils.isEmpty(faceId);
    }

    public boolean hasUserId() {
        return userId > 0;
    }

    public boolean hasGuid() {
        return !TextUtils.isEmpty(guid);
    }

    public int getTypeIcon() {
        switch (type) {
            case TYPE_STUDENT:
            case TYPE_NEWBIE:
                return R.drawable.ic_id_student;
            case TYPE_TEACHER:
                return R.drawable.ic_id_teacher;
            case TYPE_BUSINESS:
                return R.drawable.ic_id_business;
            case TYPE_COMMON:
                return R.drawable.ic_id_official;
            default:
                return 0;
        }
    }

    public boolean canUpdatePassword() {
        return !ids;
    }

    public String getNickname() {
        return TextUtils.isEmpty(nickname) ? name : nickname;
    }

    public String getRemark(Context context) {
        return UserRemarkManager.getInstance(context).getRemark(userId, null);
    }

    public String getDisplayName(Context context) {
        return UserRemarkManager.getInstance(context).getRemark(this);
    }

    public String getAlpha(Context context) {
        return PinyinUtil.getPinyin(getDisplayName(context)).toUpperCase(Locale.getDefault());
    }

    public void setAccount(String account) {
        this.account = account;
        jid = buildJid(account);
    }

    public String deletePhotos(ArrayList<String> delUrls) {
        StringBuilder sb = new StringBuilder();
        int[] ids = new int[delUrls.size()];
        for (int i = 0; i < delUrls.size(); i++) {
            for (Photo photo : photos) {
                if (delUrls.get(i).equals(photo.image)) {
                    if (photo.id > 0) {
                        if (sb.length() > 0) {
                            sb.append(',');
                        }
                        sb.append(String.valueOf(photo.id));
                    }
                    ids[i] = photo.id;
                }
            }
        }
        removePhotos(ids);
        return sb.toString();
    }

    private void removePhotos(int[] ids) {
        ArrayList<Photo> ps = new ArrayList<>();
        for (int id : ids) {
            for (Photo photo : photos) {
                if (photo.id == id) {
                    ps.add(photo);
                }
            }
        }
        photos.removeAll(ps);
    }

    public static String buildJid(String account) {
        return account == null ? null : account.toLowerCase() + Utils.getJidSuffix();
    }

    public void setName(String name) {
        this.name = TextUtils.isEmpty(name) ? account : name;
    }

    public boolean editableEquals(UserInfo other) {
        return TextUtils.equals(nickname, other.nickname) && TextUtils.equals(phone, other.phone)
                && TextUtils.equals(email, other.email) && TextUtils.equals(shortDesc, other.shortDesc);
    }

    public static UserInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        UserInfo user = new UserInfo();
        fillBasicInfo(user, json);

        user.amount = json.optInt(AMOUNT);
        user.guid = json.optString(GUID);
        user.ids = json.optInt(IDS) != 0;
        user.nickname = json.optString(NICKNAME);
        user.avatarFull = json.optString(AVATAR_FULL);
        user.bgImg = json.optString(BG_IMG);
        user.phone = json.optString(PHONE);
        user.email = json.optString(EMAIL);
        user.emailVerified = json.optInt(EMAIL_VERIFIED) != 0;
        user.lastUpdate = json.optString(LAST_UPDATE);
        user.schoolId = json.optInt(SCHOOL_ID);
        user.departmentId = json.optInt(DEPARTMENT_ID);
        user.department = json.optString(DEPARTMENT);
        user.startYear = json.optString(START_YEAR);
        user.faceId = json.optString(FACE_ID);
        user.distance = json.optInt(DISTANCE);
        user.timeDelta = json.optInt(TIME_DELTA);
        user.points = json.optInt(POINTS);
        user.degree = json.optInt(DEGREE);
        user.vip = json.optBoolean(VIP);
        user.upCount = json.optInt(UP_COUNT);
        user.downCount = json.optInt(DOWN_COUNT);
        user.flowerCount = json.optInt(FLOWER_COUNT);
        user.eggCount = json.optInt(EGG_COUNT);
        user.education = json.optInt(EDUCATION);
        user.identityStatus = json.optInt(IDENTITY_STATUS);
        user.otherSchollId = json.optInt(OTHER_SCHOLL_ID);
        user.gold = json.optInt(GOLD);
        user.credit = json.optInt(CREDIT);
        user.postCount = json.optInt(POST_COUNT);
        user.vipEndTime = json.optLong(VIP_END_TIME);

        JSONArray ja = json.optJSONArray("relations");
        int length = ja == null ? 0 : ja.length();
        if (length > 0) {
            user.relations = new ArrayList<String>();
            for (int i = 0; i < length; i++) {
                user.relations.add(ja.optString(i));
            }
        }

        ja = json.optJSONArray(MEDALS);
        length = ja == null ? 0 : ja.length();
        if (length > 0) {
            user.mMedals = new ArrayList<Medal>();
            for (int i = 0; i < length; i++) {
                Medal medal = Medal.fromJson(ja.optJSONObject(i));
                if (medal != null) {
                    user.mMedals.add(medal);
                }
            }
        }

        ja = json.optJSONArray(PHOTOS);
        length = ja == null ? 0 : ja.length();
        if (length > 0) {
            user.photos = new ArrayList<Photo>();
            for (int i = 0; i < length; i++) {
                Photo photo = Photo.fromJson(ja.optJSONObject(i));
                if (photo != null) {
                    if (photo.isAvatar) {
//                        user.photos.add(0, photo);
                    } else {
                        user.photos.add(photo);
                    }
                }
            }
        }
        user.vip = json.isNull(VIP) ? json.optBoolean("user_vip") : json.optBoolean(VIP);
        user.isModerator = json.optBoolean("is_moderator");
        user.bannedInfos = JsonUtil.getArray(json.optJSONArray("is_banned"), BannedInfo.TRANSFORMER);
        user.managedInfos = JsonUtil.getArray(json.optJSONArray("manage_section"), BannedInfo.TRANSFORMER);
        user.followCount = json.optInt("follow_count");
        user.fansCount = json.optInt("fans_count");
        return user;
    }

    public String getLatestBadge() {
        if (primaryBadgeImage == null || "".equals(primaryBadgeImage)) {
            return null;
        }
        String[] urls = TextUtils.split(primaryBadgeImage, Utils.COMMA_DELIMITER);
        if (urls.length > 0) {
            return urls[0];
        } else {
            return null;
        }
    }

    private static void fillBasicInfo(UserInfo user, JSONObject json) {
        user.setAccount(json.optString(ACCOUNT));
        user.userId = json.isNull(USER_ID) ? json.optInt("user_id") : json.optInt(USER_ID);
        user.setName(json.isNull(NAME) ? json.optString("user_name") : json.optString(NAME));
        user.setAvatar(AliImgSpec.USER_AVATAR.makeUrl(json.isNull(AVATAR) ?
                json.optString("user_avatar") : json.optString(AVATAR)));
        user.gender = json.isNull(GENDER) ? json.optInt("user_gender") : json.optInt(GENDER);
        user.shortDesc = json.isNull(SHORT_DESC) ?
                json.optString("user_description") : json.optString(SHORT_DESC);
        user.school = json.isNull(SCHOOL) ? json.optString("user_school") : json.optString(SCHOOL);
        user.account = json.isNull(ACCOUNT) ? json.optString("user_account") : json.optString(ACCOUNT);

        JSONArray ja;
        if (json.isNull("primary_badges")) {
            if (json.isNull("badges")) {
                ja = json.optJSONArray("user_badges");
            } else {
                ja = json.optJSONArray("badges");
            }
        } else {
            ja = json.optJSONArray("primary_badges");
        }
        user.primaryBadgeImage = Utils.join(Utils.COMMA_DELIMITER, ja);
        String type = json.isNull("user_type") ? json.optString("type") : json.optString("user_type");
        user.type = typeToInt(type);
    }

    public String getPrimaryRelation() {
        return relations == null || relations.isEmpty() ? null : relations.get(0);
    }

    public String getRelationship() {
        String real = relations == null || relations.isEmpty() ? null : relations.get(0);
        return "好友".equals(real) ? real : "";
    }

    public ArrayList<Medal> getPrimaryMedals() {
        ArrayList<Medal> medals = new ArrayList<Medal>();
        if (medalCount() != 0) {
            for (Medal medal : mMedals) {
                if (medal.isPrimary()) {
                    medals.add(medal);
                }
            }
        }
        return medals;
    }

    public void setPrimaryMedals(ArrayList<Medal> primaryMedals) {
        if (primaryMedals == null || medalCount() == 0) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        Collections.sort(primaryMedals, Medal.PRIMARY_COMPARATOR);
        for (Medal medal : mMedals) {
            int index = primaryMedals.indexOf(medal);
            if (index == -1) {
                medal.primary = 0;
            } else {
                medal.primary = primaryMedals.get(index).primary;
                if (firstTime) {
                    firstTime = false;
                } else {
                    sb.append(Utils.COMMA_DELIMITER);
                }
                sb.append(medal.imageUrl);
            }
        }
        primaryBadgeImage = sb.toString();
    }

    public int medalCount() {
        return mMedals == null ? 0 : mMedals.size();
    }

    public ArrayList<Medal> getMedals() {
        return mMedals;
    }

    public int photoCount() {
        return photos == null ? 0 : photos.size();
    }

    public ArrayList<Photo> getPhotos() {
        return photos;
    }

    public String getSchoolAndGrade(Context ctx) {
        StringBuilder sb = new StringBuilder(school == null ? "" : school);
        if (!TextUtils.isEmpty(startYear) && !"0".equals(startYear)) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(ctx.getString(R.string.prof_desc_grade, startYear));
        }
        return sb.toString();
    }

    public String getSchool(Context ctx) {
        StringBuilder sb = new StringBuilder(school == null ? "" : school);
        return sb.toString();
    }

    public String getEduInfo(Context ctx) {
        if (isSpecial()) {
            if (shortDesc != null && shortDesc.split(",").length > 1) {
                return shortDesc.split(",")[0];
            } else {
                return department;
            }
        }

        StringBuilder sb = new StringBuilder(school == null ? "" : school);
        if (mShowSchoolOnly) {
            return sb.toString();
        }
        if (!TextUtils.isEmpty(startYear) && !"0".equals(startYear)) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(ctx.getString(R.string.prof_desc_grade, startYear));
        }
        if (!TextUtils.isEmpty(department)) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(department);
        }
        return sb.toString();
    }

    public boolean isSpecial() {
        return departmentId == 36220 || departmentId == 36321;
    }

    public boolean isModerator() {
        return managedInfos != null && managedInfos.size() > 0;
    }

    public static final JsonUtil.ITransformer<UserInfo> TRANSFORMER = new JsonUtil.ITransformer<UserInfo>() {
        @Override
        public UserInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public static final Parcelable.Creator<UserInfo> CREATOR = new Parcelable.Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    public UserInfo(Parcel in) {
        amount = in.readInt();
        userId = in.readInt();
        guid = in.readString();
        account = in.readString();
        type = in.readInt();
        ids = in.readInt() != 0;
        jid = in.readString();
        name = in.readString();
        nickname = in.readString();
        phone = in.readString();
        email = in.readString();
        emailVerified = in.readInt() != 0;
        gender = in.readInt();
        shortDesc = in.readString();
        setAvatar(in.readString());
        avatarFull = in.readString();
        bgImg = in.readString();
        primaryBadgeImage = in.readString();
        lastUpdate = in.readString();

        schoolId = in.readInt();
        school = in.readString();
        departmentId = in.readInt();
        department = in.readString();
        startYear = in.readString();

        faceId = in.readString();
        distance = in.readInt();
        timeDelta = in.readInt();

        points = in.readInt();
        vip = in.readInt() == 0 ? false : true;
        upCount = in.readInt();
        downCount = in.readInt();
        education = in.readInt();
        flowerCount = in.readInt();
        eggCount = in.readInt();
        otherSchollId = in.readInt();
        identityStatus = in.readInt();
        gold = in.readInt();
        credit = in.readInt();
        postCount = in.readInt();
        vipEndTime = in.readLong();
        alpha = in.readString();

        if (relations == null) {
            relations = new ArrayList<String>();
        }
        in.readStringList(relations);

        int size = in.readInt();
        if (size > 0) {
            mMedals = new ArrayList<Medal>(size);
            for (int i = 0; i < size; i++) {
                Medal medal = in.readParcelable(Medal.class.getClassLoader());
                mMedals.add(medal);
            }
        }

        size = in.readInt();
        if (size > 0) {
            photos = new ArrayList<Photo>(size);
            for (int i = 0; i < size; i++) {
                Photo photo = in.readParcelable(Photo.class.getClassLoader());
                photos.add(photo);
            }
        }
        isModerator = in.readInt() == 1;
        int bannedSize = in.readInt();
        bannedInfos = new ArrayList<>();
        for (int i = 0; i < bannedSize; i++) {
            BannedInfo bannedInfo = new BannedInfo(in.readString(), in.readInt(), in.readInt() == 1);
            bannedInfos.add(bannedInfo);
        }
        int managedSize = in.readInt();
        managedInfos = new ArrayList<>();
        for (int i = 0; i < managedSize; i++) {
            BannedInfo bannedInfo = new BannedInfo(in.readString(), in.readInt(), in.readInt() == 1);
            managedInfos.add(bannedInfo);
        }
        followCount = in.readInt();
        fansCount = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(amount);
        dest.writeInt(userId);
        dest.writeString(guid);
        dest.writeString(account);
        dest.writeInt(type);
        dest.writeInt(ids ? 1 : 0);
        dest.writeString(jid);
        dest.writeString(name);
        dest.writeString(nickname);
        dest.writeString(phone);
        dest.writeString(email);
        dest.writeInt(emailVerified ? 1 : 0);
        dest.writeInt(gender);
        dest.writeString(shortDesc);
        dest.writeString(getAvatar());
        dest.writeString(avatarFull);
        dest.writeString(bgImg);
        dest.writeString(primaryBadgeImage);
        dest.writeString(lastUpdate);

        dest.writeInt(schoolId);
        dest.writeString(school);
        dest.writeInt(departmentId);
        dest.writeString(department);
        dest.writeString(startYear);

        dest.writeString(faceId);
        dest.writeInt(distance);
        dest.writeInt(timeDelta);

        dest.writeInt(points);
        dest.writeInt(vip ? 1 : 0);
        dest.writeInt(upCount);
        dest.writeInt(downCount);
        dest.writeInt(education);
        dest.writeInt(flowerCount);
        dest.writeInt(eggCount);
        dest.writeInt(otherSchollId);
        dest.writeInt(identityStatus);
        dest.writeInt(gold);
        dest.writeInt(credit);
        dest.writeInt(postCount);
        dest.writeLong(vipEndTime);
        dest.writeString(alpha);
        dest.writeStringList(relations);


        // write medals
        int size = medalCount();
        dest.writeInt(size);
        if (size > 0) {
            for (Medal medal : mMedals) {
                dest.writeParcelable(medal, flags);
            }
        }

        // write photos
        size = photoCount();
        dest.writeInt(size);
        if (size > 0) {
            for (Photo photo : photos) {
                dest.writeParcelable(photo, flags);
            }
        }
        dest.writeInt(isModerator ? 1 : 0);
        int bannedSize = bannedInfos == null ? 0 : bannedInfos.size();
        dest.writeInt(bannedSize);
        for (int i = 0; i < bannedSize; i++) {
            dest.writeString(bannedInfos.get(i).tribeName);
            dest.writeInt(bannedInfos.get(i).tribeId);
            dest.writeInt(bannedInfos.get(i).checked ? 1 : 0);
        }
        int managedSize = managedInfos == null ? 0 : managedInfos.size();
        dest.writeInt(managedSize);
        for (int i = 0; i < managedSize; i++) {
            dest.writeString(managedInfos.get(i).tribeName);
            dest.writeInt(managedInfos.get(i).tribeId);
            dest.writeInt(managedInfos.get(i).checked ? 1 : 0);
        }
        dest.writeInt(followCount);
        dest.writeInt(fansCount);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof UserInfo) {
            UserInfo other = (UserInfo) o;
            return userId == other.userId;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return userId;
    }

    private static int typeToInt(String s) {
        if ("s".equals(s)) {
            return TYPE_STUDENT;
        } else if ("t".equals(s)) {
            return TYPE_TEACHER;
        } else if ("b".equals(s)) {
            return TYPE_BUSINESS;
        } else if ("c".equals(s)) {
            return TYPE_COMMON;
        } else if ("n".equals(s)) {
            return TYPE_NEWBIE;
        } else { // "x"
            return TYPE_OTHER;
        }
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean getIsUpload() {
        return isUpload;
    }

    public void setIsupload(boolean flag) {

        this.isUpload = flag;
    }
}
