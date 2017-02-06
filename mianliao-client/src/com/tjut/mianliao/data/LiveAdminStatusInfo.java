package com.tjut.mianliao.data;

        import android.os.Parcel;
        import android.os.Parcelable;

        import com.tjut.mianliao.util.JsonUtil;

        import org.json.JSONObject;

/**
 * Created by Silva on 2016/7/15.
 */
public class LiveAdminStatusInfo implements Parcelable {

    public boolean isInblickList;
    public boolean isAdmin;
    public boolean isShutUp;
    public boolean isFollow;

        public LiveAdminStatusInfo() {}

        public static final JsonUtil.ITransformer<LiveAdminStatusInfo> TRANSFORMER =
                new JsonUtil.ITransformer<LiveAdminStatusInfo>(){
                    @Override
                    public LiveAdminStatusInfo transform(JSONObject json) {
                        return fromJson(json);
                    }


                };

        public static LiveAdminStatusInfo fromJson(JSONObject json) {
            if (json == null) {
                return null;
            }
            LiveAdminStatusInfo info = new LiveAdminStatusInfo();
            info.isInblickList = json.optBoolean("is_in_blacklist");
            info.isFollow = json.optBoolean("is_follow_user");
            info.isAdmin = json.optBoolean("is_admin");
            info.isShutUp = json.optBoolean("is_shut_up");
            return info;
        }

        public static final Creator<LiveAdminStatusInfo> CREATOR =
                new Creator<LiveAdminStatusInfo>() {
                    @Override
                    public LiveAdminStatusInfo createFromParcel(Parcel source) {
                        return new LiveAdminStatusInfo(source);
                    }

                    @Override
                    public LiveAdminStatusInfo[] newArray(int size) {
                        return new LiveAdminStatusInfo[size];
                    }
                };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(isAdmin ? 1 : 0);
            dest.writeInt(isFollow ? 1 : 0);
            dest.writeInt(isInblickList ? 1 : 0);
            dest.writeInt(isShutUp ? 1 : 0);
        }

        public LiveAdminStatusInfo(Parcel source) {
            isAdmin = source.readInt() == 1;
            isFollow = source.readInt() == 1;
            isInblickList = source.readInt() == 1;
            isShutUp = source.readInt() == 1;
        }

}
