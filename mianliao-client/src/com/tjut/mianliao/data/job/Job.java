package com.tjut.mianliao.data.job;

import java.util.ArrayList;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.data.Property;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.JsonUtil;

public class Job implements Parcelable {

    public static final String INTENT_EXTRA_NAME = "job";

    public static final int OFFER_STAT_INVITING = 100;
    public static final int OFFER_STAT_INVITE_ACCEPTED = 101;
    public static final int OFFER_STAT_APPLYING = 200;
    public static final int OFFER_STAT_APPLY_ACCEPTED = 201;

    public static final int CLS_ONLINE = 0;
    public static final int CLS_OFFLINE = 1;
    public static final int CLS_MAIL = 2;

    public static final String JOB_ID = "job_id";
    public static final String CORP_ID = "corp_id";
    public static final String C_TIME = "ctime";
    public static final String U_TIME = "utime";
    public static final String STATUS = "status";
    public static final String CLS = "cls";
    public static final String LOC_CITY_ID = "loc_city_id";
    public static final String LOC_DIST_ID = "loc_dist_id";
    public static final String CATEGORY = "category";
    public static final String TYPE = "type";
    public static final String QUOTA = "quota";
    public static final String TITLE = "title";
    public static final String INTRO = "intro";
    public static final String SALARY = "salary";
    public static final String ATTACHMENT = "attachment";
    public static final String LOC_CITY_NAME = "loc_city_name";
    public static final String LOC_DIST_NAME = "loc_dist_name";
    public static final String CATEGORY_NAME = "category_name";
    public static final String TYPE_NAME = "type_name";
    public static final String CORP_NAME = "corp_name";
    public static final String CORP_RANK = "corp_rank";
    public static final String CORP_LOGO = "corp_logo";
    public static final String OFFER_STATUS = "offer_status";
    public static final String OFFER_ID = "offer_id";
    public static final String PROPERTIES = "properties";
    public static final String TAGS = "job_tags";
    public static final String TAG = "job_tags_name";
    public static final String SHARE_COUNT = "share_count";

    public int id;
    public int corpId;
    public long cTime;
    public long uTime;
    public int status;
    public int cls;
    public int locCityId;
    public int locDistId;
    public int category;
    public int type;
    public String quota;
    public String title;
    public String intro;
    public String salary;
    public String attachment;
    public String locCityName;
    public String locDistName;
    public String categoryName;
    public String typeName;
    public String corpName;
    public String corpLogo;
    public int corpRank;
    public int offerStatus;
    public int offerId;
    public ArrayList<Property> properties;

    public boolean applying;
    public String tags;
    public String tag;
    public int shareCount;

    public Job() {}

    public static Job fromJson(JSONObject json) {
        if (json == null || json.optInt(JOB_ID) == 0) {
            return null;
        }
        Job job = new Job();
        job.id = json.optInt(JOB_ID);
        job.corpId = json.optInt(CORP_ID);
        job.cTime = json.optLong(C_TIME) * 1000;
        job.uTime = json.optLong(U_TIME) * 1000;
        job.status = json.optInt(STATUS);
        job.cls = json.optInt(CLS);
        job.locCityId = json.optInt(LOC_CITY_ID);
        job.locDistId = json.optInt(LOC_DIST_ID);
        job.category = json.optInt(CATEGORY);
        job.type = json.optInt(TYPE);
        job.quota = json.optString(QUOTA);
        job.title = json.optString(TITLE);
        job.intro = json.optString(INTRO);
        job.salary = json.optString(SALARY);
        job.attachment = json.optString(ATTACHMENT);
        job.locCityName = json.optString(LOC_CITY_NAME);
        job.locDistName = json.optString(LOC_DIST_NAME);
        job.categoryName = json.optString(CATEGORY_NAME);
        job.typeName = json.optString(TYPE_NAME);
        job.corpName = json.optString(CORP_NAME);
        job.corpLogo = AliImgSpec.CORP_AVATAR.makeUrl(json.optString(CORP_LOGO));
        job.corpRank = json.optInt(CORP_RANK);
        job.offerStatus = json.optInt(OFFER_STATUS);
        job.offerId = json.optInt(OFFER_ID);
        job.properties = JsonUtil.getArray(json.optJSONArray(PROPERTIES), Property.TRANSFORMER);
        job.tags = json.optString(TAGS);
        job.tag = json.optString(TAG);
        job.shareCount = json.optInt(SHARE_COUNT);
        return job;
    }

    public boolean isApplied() {
        return offerStatus == OFFER_STAT_APPLYING || offerStatus == OFFER_STAT_APPLY_ACCEPTED ||
                offerStatus == OFFER_STAT_INVITE_ACCEPTED;
    }

    public boolean isInvited() {
        return offerStatus == OFFER_STAT_INVITING;
    }

    public String getLocDesc() {
        return locCityName + " " + locDistName;
    }

    public boolean isOffline() {
        return cls == CLS_OFFLINE;
    }

    public static final JsonUtil.ITransformer<Job> TRANSFORMER = new JsonUtil.ITransformer<Job>() {
        @Override
        public Job transform(JSONObject json) {
            return fromJson(json);
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Job> CREATOR = new Creator<Job>() {
        @Override
        public Job createFromParcel(Parcel source) {
            return new Job(source);
        }

        @Override
        public Job[] newArray(int size) {
            return new Job[size];
        }
    };

    public Job(Parcel in) {
        id = in.readInt();
        corpId = in.readInt();
        cTime = in.readLong();
        uTime = in.readLong();
        status = in.readInt();
        cls = in.readInt();
        locCityId = in.readInt();
        locDistId = in.readInt();
        category = in.readInt();
        type = in.readInt();
        title = in.readString();
        quota = in.readString();
        intro = in.readString();
        salary = in.readString();
        attachment = in.readString();
        locCityName = in.readString();
        locDistName = in.readString();
        categoryName = in.readString();
        typeName = in.readString();
        corpName = in.readString();
        corpLogo = in.readString();
        corpRank = in.readInt();
        offerStatus = in.readInt();
        offerId = in.readInt();

        int size = in.readInt();
        if (size > 0) {
            properties = new ArrayList<Property>();
            for (int i = 0; i < size; i++) {
                Property p = in.readParcelable(Property.class.getClassLoader());
                properties.add(p);
            }
        }
        tags = in.readString();
        tag = in.readString();
        shareCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(corpId);
        dest.writeLong(cTime);
        dest.writeLong(uTime);
        dest.writeInt(status);
        dest.writeInt(cls);
        dest.writeInt(locCityId);
        dest.writeInt(locDistId);
        dest.writeInt(category);
        dest.writeInt(type);
        dest.writeString(title);
        dest.writeString(quota);
        dest.writeString(intro);
        dest.writeString(salary);
        dest.writeString(attachment);
        dest.writeString(locCityName);
        dest.writeString(locDistName);
        dest.writeString(categoryName);
        dest.writeString(typeName);
        dest.writeString(corpName);
        dest.writeString(corpLogo);
        dest.writeInt(corpRank);
        dest.writeInt(offerStatus);
        dest.writeInt(offerId);

        int size = properties == null ? 0 : properties.size();
        dest.writeInt(size);
        if (size > 0) {
            for (Property p : properties) {
                dest.writeParcelable(p, flags);
            }
        }
        dest.writeString(tags);
        dest.writeString(tag);
        dest.writeInt(shareCount);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Job) {
            Job other = (Job) o;
            return id == other.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
