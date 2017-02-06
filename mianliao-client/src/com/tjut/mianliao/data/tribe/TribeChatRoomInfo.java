package com.tjut.mianliao.data.tribe;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.Utils;

public class TribeChatRoomInfo implements Parcelable{
    
    public static final String INTENT_EXTRA_INFO = "extra_room_info";
    
    public int roomId;
    public String roomName;
    public String tribeName;
    public int createrId;
    public int tribeId;
    public String roomDesc;
    public String roomAvatar;
    public int peopleNum;
    public String ownerNick;
    public String ownerAvatar;
    public int code;
    public long chatId;

    public TribeChatRoomInfo () {}

    public static final JsonUtil.ITransformer<TribeChatRoomInfo> TRANSFORMER = 
            new JsonUtil.ITransformer<TribeChatRoomInfo>() {
        
        @Override
        public TribeChatRoomInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };
    
    public static TribeChatRoomInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        TribeChatRoomInfo roomInfo = new TribeChatRoomInfo();
        roomInfo.roomId = json.optInt("id");
        roomInfo.roomName = json.optString("name");
        roomInfo.createrId = json.optInt("admin_uid");
        roomInfo.tribeId = json.optInt("tribe_id");
        roomInfo.roomDesc = json.optString("description");
        roomInfo.roomAvatar = json.optString("icon");
        roomInfo.peopleNum = json.optInt("member_count");
        roomInfo.tribeName = json.optString("tribe_name");
        roomInfo.ownerNick = json.optString("user_nick");
        roomInfo.ownerAvatar = json.optString("user_avatar");
        roomInfo.code = json.optInt("code");
        roomInfo.chatId = Long.parseLong(json.optString("chat_id"));
        return roomInfo;
    }
    
    public static final Parcelable.Creator<TribeChatRoomInfo> CREATOR = 
            new Creator<TribeChatRoomInfo>() {
                
                @Override
                public TribeChatRoomInfo[] newArray(int size) {
                    return new TribeChatRoomInfo[size];
                }
                
                @Override
                public TribeChatRoomInfo createFromParcel(Parcel source) {
                    return new TribeChatRoomInfo(source);
                }
            };
    

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(roomId);
        dest.writeString(roomName);
        dest.writeInt(createrId);
        dest.writeInt(tribeId);
        dest.writeString(roomDesc);
        dest.writeString(roomAvatar);
        dest.writeInt(peopleNum);
        dest.writeString(tribeName);
        dest.writeString(ownerNick);
        dest.writeString(ownerAvatar);
        dest.writeInt(code);
        dest.writeLong(chatId);
    }
    
    public TribeChatRoomInfo(Parcel in) {
        roomId = in.readInt();
        roomName = in.readString();
        createrId = in.readInt();
        tribeId = in.readInt();
        roomDesc = in.readString();
        roomAvatar = in.readString();
        peopleNum = in.readInt();
        tribeName = in.readString();
        ownerNick = in.readString();
        ownerAvatar = in.readString();
        code = in.readInt();
        chatId = in.readLong();
    }

}
