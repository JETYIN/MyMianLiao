package com.tjut.mianliao.forum;

import org.json.JSONObject;

import android.os.Parcel;
import android.text.TextUtils;

import com.tjut.mianliao.R;

public class Event {

    public static final String FEATURE = "feature";
    public static final String LOCATION = "location";
    public static final String CONTACT = "contact";
    public static final String QUOTA = "quota";
    public static final String COST = "cost";
    public static final String REG_DEADLINE = "reg_deadline";
    public static final String REG_COUNT = "reg_count";
    public static final String START_AT = "start_at";
    public static final String ENABLED = "enable";
    public static final String MY_REG = "my_reg";

    public String feature = "";
    public String location = "";
    public String contact = "";
    public String quota = "";
    public String cost = "";
    public long regDeadline;
    public int regCount;
    public long startAt;

    public boolean enabled;
    public boolean myReg;
    public boolean suggested;

    Event() {}

    public Event copy() {
        Event event = new Event();
        event.feature = feature;
        event.location = location;
        event.contact = contact;
        event.quota = quota;
        event.cost = cost;
        event.regDeadline = regDeadline;
        event.regCount = regCount;
        event.startAt = startAt;
        event.enabled = enabled;
        event.myReg = myReg;
        event.suggested = suggested;
        return event;
    }

    public int getTitleIcon() {
        return isOverdue() ? R.drawable.ic_event_overdue : 0;
    }

    public boolean isOverdue() {
        return regDeadline > 0 && regDeadline * 1000 < System.currentTimeMillis();
    }

    static Event fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        Event event = new Event();
        event.feature = json.optString(FEATURE);
        event.location = json.optString(LOCATION);
        event.contact = json.optString(CONTACT);
        event.quota = json.optString(QUOTA);
        event.cost = json.optString(COST);
        event.startAt = json.optLong(START_AT);
        event.regDeadline = json.optLong(REG_DEADLINE);
        event.regCount = json.optInt(REG_COUNT);
        event.enabled = json.optBoolean(ENABLED);
        event.myReg = json.optBoolean(MY_REG);
        return event;
    }

    Event(Parcel in) {
        feature = in.readString();
        location = in.readString();
        contact = in.readString();
        quota = in.readString();
        cost = in.readString();
        regDeadline = in.readLong();
        regCount = in.readInt();
        startAt = in.readLong();
        enabled = in.readInt() == 1;
        myReg = in.readInt() == 1;
        suggested = in.readInt() == 1;
    }

    void writeToParcel(Parcel dest) {
        dest.writeString(feature);
        dest.writeString(location);
        dest.writeString(contact);
        dest.writeString(quota);
        dest.writeString(cost);
        dest.writeLong(regDeadline);
        dest.writeInt(regCount);
        dest.writeLong(startAt);
        dest.writeInt(enabled ? 1 : 0);
        dest.writeInt(myReg ? 1 : 0);
        dest.writeInt(suggested ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Event) {
            Event event = (Event) o;
            return TextUtils.equals(feature, event.feature) &&
                    TextUtils.equals(location, event.location) &&
                    TextUtils.equals(contact, event.contact) &&
                    TextUtils.equals(quota, event.quota) &&
                    TextUtils.equals(cost, event.cost) &&
                    regDeadline == event.regDeadline &&
                    startAt == event.startAt;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
