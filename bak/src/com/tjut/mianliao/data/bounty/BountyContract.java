package com.tjut.mianliao.data.bounty;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.R;
import com.tjut.mianliao.util.JsonUtil;

public class BountyContract implements Parcelable {

    public static final String INTENT_EXTRA_NAME = "BountyContract";

    public static final int STATUS_APPLYING = 0;
    public static final int STATUS_ONGOING = 1;
    public static final int STATUS_HOST_RATED = 2;
    public static final int STATUS_GUEST_RATED = 3;
    public static final int STATUS_FINISHED = 4;

    public int id;
    public int taskId;
    public int status;
    public long createTime;
    public long updateTime;
    public long startTime;
    public long endTime;
    public String message;
    public Credits userCredit;

    public BountyContract() {}

    public static BountyContract fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        BountyContract contract = new BountyContract();

        contract.id = json.optInt("id");
        contract.taskId = json.optInt("task_id");
        contract.status = json.optInt("status");
        contract.createTime = json.optLong("ctime") * 1000;
        contract.updateTime = json.optLong("utime") * 1000;
        contract.startTime = json.optLong("stime") * 1000;
        contract.endTime = json.optLong("etime") * 1000;
        contract.message = json.optString("msg");
        contract.userCredit = Credits.fromJson(json);

        return contract;
    }

    public void setRated(boolean isHost) {
        switch (status) {
            case STATUS_ONGOING:
                status = isHost ? STATUS_HOST_RATED : STATUS_GUEST_RATED;
                break;

            case STATUS_HOST_RATED:
            case STATUS_GUEST_RATED:
                status = STATUS_FINISHED;
                break;

            default:
                break;
        }
    }

    public boolean isOnGoing(boolean isHost) {
        return status == STATUS_ONGOING || status == (
                isHost ? STATUS_GUEST_RATED : STATUS_HOST_RATED);
    }

    public int getStatusDesc() {
        switch (status) {
            case STATUS_ONGOING:
                return R.string.btyct_status_ongoing;

            case STATUS_HOST_RATED:
            case STATUS_GUEST_RATED:
                return R.string.btyct_status_rated;

            case STATUS_FINISHED:
                return R.string.btyct_status_finished;

            case STATUS_APPLYING:
            default:
                return R.string.btyct_status_applying;
        }
    }

    public static final JsonUtil.ITransformer<BountyContract> TRANSFORMER =
            new JsonUtil.ITransformer<BountyContract>() {
        @Override
        public BountyContract transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public static final Parcelable.Creator<BountyContract> CREATOR =
            new Parcelable.Creator<BountyContract>() {
        @Override
        public BountyContract createFromParcel(Parcel in) {
            return new BountyContract(in);
        }

        @Override
        public BountyContract[] newArray(int size) {
            return new BountyContract[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public BountyContract(Parcel in) {
        id = in.readInt();
        taskId = in.readInt();
        status = in.readInt();
        createTime = in.readLong();
        updateTime = in.readLong();
        startTime = in.readLong();
        endTime = in.readLong();
        message = in.readString();
        userCredit = in.readParcelable(Credits.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeInt(taskId);
        out.writeInt(status);
        out.writeLong(createTime);
        out.writeLong(updateTime);
        out.writeLong(startTime);
        out.writeLong(endTime);
        out.writeString(message);
        out.writeParcelable(userCredit, flags);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof BountyContract) {
            BountyContract other = (BountyContract) o;
            return id == other.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
