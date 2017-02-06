package com.tjut.mianliao.profile;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

import org.json.JSONObject;

/**
 * Created by j_hao on 2016/7/20.
 */
public class AccountBill implements Parcelable {
    public int rechargeId;
    public int uid;
    public int rechargeAmount;
    public long time;
    public int status;
    public String ip;
    public int type;
    public int number;
    //

    public int withdrawalId;
    public int withdrawalAmount;
    public int withdrawelStatus;
    public long withdrawelTime;
    public int withdrawelConsumeGrain;
    //

    public int consumptionId;
    public int consumptioncoinType;
    public long consumptionTime;
    public int consumptionCost;
    public int consumptionReceiveUid;
    public String consumptionDescription;


    public AccountBill() {
    }

    public static final JsonUtil.ITransformer<AccountBill> TRANSFORMER =
            new JsonUtil.ITransformer<AccountBill>() {

                @Override
                public AccountBill transform(JSONObject json) {
                    return fromJson(json);
                }

            };

    public static AccountBill fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        AccountBill accountBill = new AccountBill();
        accountBill.rechargeId = json.optInt("recharge_id");
        accountBill.uid = json.optInt("uid");
        accountBill.rechargeAmount = json.optInt("recharge_amount");
        accountBill.time = json.optLong("time");
        accountBill.status = json.optInt("status");
        accountBill.type = json.optInt("type");
        accountBill.number = json.optInt("number");
        accountBill.ip = json.optString("ip");
        //
        accountBill.withdrawalId = json.optInt("withdrawal_id");
        accountBill.withdrawalAmount = json.optInt("withdraw_amount");
        accountBill.withdrawelStatus = json.optInt("status");
        accountBill.withdrawelTime = json.optLong("time");
        accountBill.withdrawelConsumeGrain = json.optInt("consume_grain");
        //
        accountBill.consumptioncoinType = json.optInt("coin_type");
        accountBill.consumptionCost = json.optInt("cost");
        accountBill.consumptionReceiveUid = json.optInt("receive_uid");
        accountBill.consumptionId = json.optInt("consumption_id");
        accountBill.consumptionTime = json.optLong("time");
        accountBill.consumptionDescription = json.optString("description");
        return accountBill;
    }

    public static final Creator<AccountBill> CREATOR = new Creator<AccountBill>() {
        @Override
        public AccountBill createFromParcel(Parcel in) {
            return new AccountBill(in);
        }

        @Override
        public AccountBill[] newArray(int size) {
            return new AccountBill[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    protected AccountBill(Parcel in) {
        rechargeId = in.readInt();
        uid = in.readInt();
        rechargeAmount = in.readInt();
        time = in.readLong();
        status = in.readInt();
        ip = in.readString();
        type = in.readInt();
        number = in.readInt();
        //
        withdrawalId = in.readInt();
        withdrawelStatus = in.readInt();
        withdrawalAmount = in.readInt();
        withdrawelTime = in.readLong();
        withdrawelConsumeGrain = in.readInt();
        //
        consumptionId = in.readInt();
        consumptioncoinType = in.readInt();
        consumptionTime = in.readLong();
        consumptionCost = in.readInt();
        consumptionReceiveUid = in.readInt();
        consumptionDescription = in.readString();

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(rechargeId);
        dest.writeInt(uid);
        dest.writeInt(rechargeAmount);
        dest.writeString(ip);
        dest.writeLong(time);
        dest.writeInt(status);
        dest.writeInt(type);
        dest.writeInt(number);
        //
        dest.writeInt(withdrawalId);
        dest.writeInt(withdrawelStatus);
        dest.writeInt(withdrawalAmount);
        dest.writeInt(withdrawelConsumeGrain);
        dest.writeLong(withdrawelTime);
        //
        dest.writeInt(consumptionId);
        dest.writeInt(consumptioncoinType);
        dest.writeInt(consumptionCost);
        dest.writeInt(consumptionReceiveUid);
        dest.writeLong(consumptionTime);
        dest.writeString(consumptionDescription);
    }


}
