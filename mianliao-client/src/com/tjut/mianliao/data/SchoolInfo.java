package com.tjut.mianliao.data;

import org.json.JSONObject;

public class SchoolInfo {

	public int schoolId, areaId;
	public String name, pinyin, abbreviation;
	public boolean isCollection;
	public boolean unlock;
	public boolean vip;
	
	public boolean isVip() {
        return vip;
    }

    public void setVip(boolean vip) {
        this.vip = vip;
    }

    public boolean isUnlock() {
		return unlock;
	}

	public void setUnlock(boolean unlock) {
		this.unlock = unlock;
	}

	public int getSchoolId() {
		return schoolId;
	}

	public void setSchool_id(int schoolId) {
		this.schoolId = schoolId;
	}

	public int getArea_id() {
		return areaId;
	}

	public void setArea_id(int areaId) {
		this.areaId = areaId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public boolean isCollection() {
		return isCollection;
	}

	public void setIscollection(boolean isCollection) {
		this.isCollection = isCollection;
	}

	public static SchoolInfo fromJson(JSONObject json) {
		if (json == null) {
			return null;
		}
		SchoolInfo schoolInfo = new SchoolInfo();
		schoolInfo.schoolId = json.optInt("school_id");
		schoolInfo.name = json.optString("name");
		schoolInfo.areaId = json.optInt("area_id");
		schoolInfo.pinyin = json.optString("pinyin");
		schoolInfo.abbreviation = json.optString("abbreviation");
		schoolInfo.isCollection = json.optBoolean("is_collected");
		schoolInfo.unlock = json.optBoolean("unlock");
		schoolInfo.vip = json.optBoolean("vip");
		return schoolInfo;
	}
}
