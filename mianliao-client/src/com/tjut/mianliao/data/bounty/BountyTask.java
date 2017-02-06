package com.tjut.mianliao.data.bounty;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.baidu.mapapi.model.LatLng;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.JsonUtil;

public class BountyTask implements Parcelable {

    public static final int STATE_ONGOING = 1;
    public static final int STATE_EXPIRED = 2;
    public static final int STATE_CLAIMED = 3;

    public static final String INTENT_EXTRA_NAME = "BountyTask";

    public static final String ID = "id";
    public static final String CTIME = "ctime";
    public static final String REQ_TYPE = "req_type";
    public static final String REQ_DEADLINE = "req_deadline";
    public static final String QUOTA = "quota";
    public static final String TITLE = "title";
    public static final String DESC = "desc";
    public static final String REWARD = "reward";
    public static final String PLACE = "place";
    public static final String CONTACT = "contact";
    public static final String REST = "rest";
    public static final String MY_FAV = "my_fav";
    public static final String IMAGES = "images";
    public static final String LOCATION = "location";
    public static final String DISTANCE = "distance";

    public int id;
    public long ctime;
    public int reqType;
    public long reqDeadline;
    public int quota;
    public String title;
    public String desc;
    public String reward;
    public String place;
    public String contact;
    public int rest;
    public boolean myFav;
    public ArrayList<Image> images;
    public LatLng location;
    public int distance;
    public Credits userCredit;

    public BountyTask() {}

    public static final BountyTask fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        BountyTask bountyTask = new BountyTask();

        bountyTask.id = json.optInt(ID);
        bountyTask.ctime = json.optLong(CTIME) * 1000;
        bountyTask.reqType = json.optInt(REQ_TYPE);
        bountyTask.reqDeadline = json.optLong(REQ_DEADLINE);
        bountyTask.quota = json.optInt(QUOTA);
        bountyTask.title = json.optString(TITLE);
        bountyTask.desc = json.optString(DESC);
        bountyTask.reward = json.optString(REWARD);
        bountyTask.place = json.optString(PLACE);
        bountyTask.contact = json.optString(CONTACT);
        bountyTask.rest = json.optInt(REST);
        bountyTask.myFav = json.optBoolean(MY_FAV);
        bountyTask.images = JsonUtil.getArray(json.optJSONArray(IMAGES), Image.TRANSFORMER);
        bountyTask.distance = json.optInt(DISTANCE);
        JSONArray ja = json.optJSONArray(LOCATION);
        if (ja != null && ja.length() == 2) {
            bountyTask.location = new LatLng(ja.optDouble(1), ja.optDouble(0));
        }
        bountyTask.userCredit = Credits.fromJson(json);

        return bountyTask;
    }

    public boolean isMine(Context context) {
        return userCredit.userInfo.isMine(context);
    }

    public int getStatus() {
        if (rest <= 0) {
            return STATE_CLAIMED;
        } else if (reqDeadline * 1000 < System.currentTimeMillis()) {
            return STATE_EXPIRED;
        } else {
            return STATE_ONGOING;
        }
    }

    public int getStatusDesc() {
        switch (getStatus()) {
            case STATE_CLAIMED:
                return R.string.bty_state_claimed;
            case STATE_EXPIRED:
                return R.string.bty_state_expired;
            case STATE_ONGOING:
            default:
                return R.string.bty_state_ongoing;
        }
    }

    public int getImageCount() {
        return images == null ? 0 : images.size();
    }

    public String getImagePreviewSmall(int index) {
        int count = getImageCount();
        if (count == 0 || index >= count) {
            return null;
        } else if (count == 1) {
            return AliImgSpec.POST_THUMB.makeUrl(images.get(0).image);
        } else {
            return AliImgSpec.POST_THUMB_SQUARE.makeUrl(images.get(index).image);
        }
    }

    public String getBannerImage() {
        if (images != null && images.size() > 0) {
            return AliImgSpec.POST_BANNER.makeUrl(images.get(0).image);
        }
        return null;
    }

    public static final JsonUtil.ITransformer<BountyTask> TRANSFORMER =
            new JsonUtil.ITransformer<BountyTask>() {
                @Override
                public BountyTask transform(JSONObject json) {
                    return fromJson(json);
                }
            };

    public static final Parcelable.Creator<BountyTask> CREATOR =
            new Parcelable.Creator<BountyTask>() {
                @Override
                public BountyTask createFromParcel(Parcel in) {
                    return new BountyTask(in);
                }

                @Override
                public BountyTask[] newArray(int size) {
                    return new BountyTask[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    public BountyTask(Parcel in) {
        id = in.readInt();
        ctime = in.readLong();
        reqType = in.readInt();
        reqDeadline = in.readLong();
        quota = in.readInt();
        title = in.readString();
        desc = in.readString();
        reward = in.readString();
        place = in.readString();
        contact = in.readString();
        rest = in.readInt();
        myFav = in.readInt() != 0;
        distance = in.readInt();
        if (in.readByte() == 1) {
            location = new LatLng(in.readDouble(), in.readDouble());
        }

        int size = in.readInt();
        if (size > 0) {
            images = new ArrayList<Image>(size);
            for (int i = 0; i < size; i++) {
                Image image = in.readParcelable(Image.class.getClassLoader());
                images.add(image);
            }
        }

        userCredit = in.readParcelable(Credits.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeLong(ctime);
        out.writeInt(reqType);
        out.writeLong(reqDeadline);
        out.writeInt(quota);
        out.writeString(title);
        out.writeString(desc);
        out.writeString(reward);
        out.writeString(place);
        out.writeString(contact);
        out.writeInt(rest);
        out.writeInt(myFav ? 1 : 0);
        out.writeInt(distance);
        out.writeInt(location != null ? 1 : 0);
        if (location != null) {
            out.writeDouble(location.latitude);
            out.writeDouble(location.longitude);
        }

        int size = images == null ? 0 : images.size();
        out.writeInt(size);
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                out.writeParcelable(images.get(i), flags);
            }
        }

        out.writeParcelable(userCredit, flags);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof BountyTask) {
            BountyTask other = (BountyTask) o;
            return id == other.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
