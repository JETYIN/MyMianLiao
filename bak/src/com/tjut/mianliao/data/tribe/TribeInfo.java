package com.tjut.mianliao.data.tribe;

import org.json.JSONObject;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.R;
import com.tjut.mianliao.util.JsonUtil;

public class TribeInfo implements Parcelable{
	
	public static final String INTENT_EXTRA_INFO = "extra_tribe_info";
    
    public final static int TYPE_GAME = 0;
    public final static int TYPE_EMOTION = 1;
    public final static int TYPE_STUDY = 2;
    public final static int TYPE_CARTOON = 3;
    public final static int TYPE_FUNNY = 4;
    public final static int TYPE_MOVEMENT = 5;
    public final static int TYPE_TRAVEL = 6;
    public final static int TYPE_MUSIC = 7;
    public final static int TYPE_MOVIE = 8;
    public final static int TYPE_READ = 9;
    public final static int TYPE_DIGITAL = 10;
    public final static int TYPE_PHOTOGRAPHY = 11;
    public final static int TYPE_OTHER = 12;
    
    public int tribeId;
    public int tribeFid;
    public int tribeType;
    public String tribeName;
    public int threadCount;
    public int followCount;
    public String tribeDesc;
    public String icon;
    public boolean collected;
    public String rule;
    public int dayPostCount;
    public int upCount;
    
    public TribeInfo () {}

    public static final JsonUtil.ITransformer<TribeInfo> TRANSFORMER =
            new JsonUtil.ITransformer<TribeInfo>() {
        
        @Override
        public TribeInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };
    
    public static final Parcelable.Creator<TribeInfo> CREATOR = 
            new Creator<TribeInfo>() {
                
                @Override
                public TribeInfo[] newArray(int size) {
                    return new TribeInfo[size];
                }
                
                @Override
                public TribeInfo createFromParcel(Parcel source) {
                    return new TribeInfo(source);
                }
            };
    
    @Override
    public int describeContents() {
        return 0;
    }

    
    public static TribeInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        TribeInfo tribeInfo = new TribeInfo();
        tribeInfo.tribeId = json.optInt("id");
        tribeInfo.tribeFid = json.optInt("fid");
        tribeInfo.tribeType = json.optInt("tribe_type");
        tribeInfo.tribeName = json.optString("tribe_name");
        tribeInfo.threadCount = json.optInt("thread_count");
        tribeInfo.followCount = json.optInt("follow_count");
        tribeInfo.tribeDesc = json.optString("description");
        tribeInfo.icon = json.optString("icon");
        tribeInfo.collected = json.optBoolean("is_follow");
        tribeInfo.rule = json.optString("rule_content");
        tribeInfo.dayPostCount = json.optInt("today_thread_count");
        tribeInfo.upCount = json.optInt("up_count");
        return tribeInfo;
        
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(tribeId);
        dest.writeInt(tribeFid);
        dest.writeInt(tribeType);
        dest.writeString(tribeName);
        dest.writeInt(threadCount);
        dest.writeInt(followCount);
        dest.writeString(tribeDesc);
        dest.writeString(icon);
        dest.writeInt(collected ? 1 : 0);
        dest.writeString(rule);
        dest.writeInt(dayPostCount);
        dest.writeInt(upCount);
    }
    
    public TribeInfo(Parcel in) {
        tribeId = in.readInt();
        tribeFid = in.readInt();
        tribeType = in.readInt();
        tribeName = in.readString();
        threadCount = in.readInt();
        followCount = in.readInt();
        tribeDesc= in.readString();
        icon = in.readString();
        collected = in.readInt() == 1;
        rule = in.readString();
        dayPostCount = in.readInt();
        upCount = in.readInt();
    }
    
    public String getTypeName (Context mContext, int Type) {
        String mTypeName;
        switch (Type) {
            case TribeInfo.TYPE_GAME:
                mTypeName = mContext.getString(R.string.tribe_type_game);
                break;
            case TribeInfo.TYPE_EMOTION:
                mTypeName = mContext.getString(R.string.tribe_type_emotion);
                break;
            case TribeInfo.TYPE_STUDY:
                mTypeName = mContext.getString(R.string.tribe_type_study);
                break;
            case TribeInfo.TYPE_CARTOON:
                mTypeName = mContext.getString(R.string.tribe_type_cartoon);
                break;
            case TribeInfo.TYPE_FUNNY:
                mTypeName = mContext.getString(R.string.tribe_type_funny);
                break;
            case TribeInfo.TYPE_MOVEMENT:
                mTypeName = mContext.getString(R.string.tribe_type_movement);
                break;
            case TribeInfo.TYPE_TRAVEL:
                mTypeName = mContext.getString(R.string.tribe_type_travel);
                break;
            case TribeInfo.TYPE_MUSIC:
                mTypeName = mContext.getString(R.string.tribe_type_music);
                break;
            case TribeInfo.TYPE_MOVIE:
                mTypeName = mContext.getString(R.string.tribe_type_movie);
                break;
            case TribeInfo.TYPE_READ:
                mTypeName = mContext.getString(R.string.tribe_type_read);
                break;
            case TribeInfo.TYPE_DIGITAL:
                mTypeName = mContext.getString(R.string.tribe_type_digital);
                break;
            case TribeInfo.TYPE_PHOTOGRAPHY:
                mTypeName = mContext.getString(R.string.tribe_type_photo);
                break;
            default:
                mTypeName = mContext.getString(R.string.tribe_type_other);
                break;
        }
        return mTypeName;
    }

}
