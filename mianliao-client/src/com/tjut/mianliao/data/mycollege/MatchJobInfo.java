package com.tjut.mianliao.data.mycollege;

import android.os.Parcel;
import android.os.Parcelable;


public class MatchJobInfo implements Parcelable{

    public static final String TABLE_NAME = "match_job_info";

    public static final String ID = "_id";
    public static final String JOB_ID = "job_id";
    public static final String JOB_TITLE = "job_title";
    public static final String PUBLISH_TIME = "publish_time";
    public static final String SALARY = "salary";
    public static final String CROP_NAME = "corp_name";
    public static final String CROP_LOGO = "corp_logo";
    public static final String LOCAL_CITY = "local_city";

    public long id;
    public int jobId;
    public String jobTitle;
    public long publishTime;
    public String salary;
    public String corpName;
    public String corpLogo;
    public String localCity;
    public String tagStr;

    public MatchJobInfo() {}

    public MatchJobInfo(Parcel source) {
        jobId = source.readInt();
        jobTitle = source.readString();
        publishTime = source.readLong();
        salary = source.readString();
        corpName = source.readString();
        corpLogo = source.readString();
        localCity = source.readString();
    }

    public static final Creator<MatchJobInfo> CREATOR =
            new Creator<MatchJobInfo>() {

                @Override
                public MatchJobInfo[] newArray(int size) {
                    return new MatchJobInfo[size];
                }

                @Override
                public MatchJobInfo createFromParcel(Parcel source) {
                    return new MatchJobInfo(source);
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(jobId);
        dest.writeString(jobTitle);
        dest.writeLong(publishTime);
        dest.writeString(salary);
        dest.writeString(corpName);
        dest.writeString(corpLogo);
        dest.writeString(localCity);
    }
}
