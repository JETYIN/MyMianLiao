package com.tjut.mianliao.data.tribe;

import org.json.JSONObject;

import com.tjut.mianliao.util.JsonUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class TribeMenInfo implements Parcelable {

	public int uid;
	public int assist;
	public int praise;
	public String nickName;
	public String schoolName;
	public int sex;
	public String avatar;
	

	public static final JsonUtil.ITransformer<TribeMenInfo> TRANSFORMER =
			new JsonUtil.ITransformer<TribeMenInfo>() {
				
				@Override
				public TribeMenInfo transform(JSONObject json) {
					return fromJson(json);
				}
			};
			
	public static final Parcelable.Creator<TribeMenInfo> CREATOR =
			new Creator<TribeMenInfo>() {
				
				@Override
				public TribeMenInfo[] newArray(int size) {
					return new TribeMenInfo[size];
				}
				
				@Override
				public TribeMenInfo createFromParcel(Parcel source) {
					return new TribeMenInfo(source);
				}
			};
	
	public TribeMenInfo(Parcel source) {
		uid = source.readInt();
		assist = source.readInt();
		praise = source.readInt();
		nickName = source.readString();
		schoolName = source.readString();
		sex = source.readInt();
		avatar = source.readString();
	}
	
	public TribeMenInfo() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	protected static TribeMenInfo fromJson(JSONObject json) {
		if (json == null) {
			return null;
		}
		TribeMenInfo men = new TribeMenInfo();
		men.uid = json.optInt("uid");
		men.assist = json.optInt("assist");
		men.praise = json.optInt("praise");
		men.nickName = json.optString("nickname");
		men.schoolName = json.optString("school_name");
		men.sex = json.optInt("sex");
		men.avatar = json.optString("avatar");
		return men;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(uid);
		dest.writeInt(assist);
		dest.writeInt(praise);
		dest.writeString(nickName);
		dest.writeString(schoolName);
		dest.writeInt(sex);
		dest.writeString(avatar);
	}

}
