package com.tjut.mianliao.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

import org.json.JSONObject;

/**
 * Created by Silva on 2016/7/7.
 */
public class RequestInfo implements Parcelable{

//    connection_id:int(连线请求的id)
//    request_uid:int(发起连线请求的uid)
//    live_id:int
//    is_accept:是否已接受连线
//    request_nick:string(发起请求者的昵称)
//    request_avatar:string(发起请求者头像)
//    request_gender:int(发起请求者性别)

    public int connectionId;
    public int requestUid;
    public int liveId;
    public int requestGender;
    public boolean isAccept;
    public String requestNick;
    public String requestAvatar;


    public RequestInfo() {}

    protected RequestInfo(Parcel in) {
        connectionId = in.readInt();
        requestUid = in.readInt();
        liveId = in.readInt();
        requestGender = in.readInt();
        isAccept = in.readInt() == 0 ? false : true;
        requestNick = in.readString();
        requestAvatar = in.readString();
    }

    public static final Creator<RequestInfo> CREATOR = new Creator<RequestInfo>() {
        @Override
        public RequestInfo createFromParcel(Parcel in) {
            return new RequestInfo(in);
        }

        @Override
        public RequestInfo[] newArray(int size) {
            return new RequestInfo[size];
        }
    };

    public static final JsonUtil.ITransformer<RequestInfo> TRANSFORMER = new JsonUtil.ITransformer<RequestInfo>(){
        @Override
        public RequestInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public static RequestInfo fromJson(JSONObject json) {
        if (json == null) {
            return  null;
        }

        RequestInfo requestInfo = new RequestInfo();
        requestInfo.connectionId = json.optInt("connection_id");
        requestInfo.requestUid = json.optInt("request_uid");
        requestInfo.liveId = json.optInt("live_id");
        requestInfo.requestGender = json.optInt("request_gender");
        requestInfo.isAccept = json.optBoolean("is_accept");
        requestInfo.requestNick = json.optString(" request_nick");
        requestInfo.requestAvatar = json.optString("request_avatar");
        return requestInfo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(connectionId);
        parcel.writeInt(requestUid);
        parcel.writeInt(liveId);
        parcel.writeInt(requestGender);
        parcel.writeInt(isAccept ? 1 : 0);
        parcel.writeString(requestNick);
        parcel.writeString(requestAvatar);
    }
}
