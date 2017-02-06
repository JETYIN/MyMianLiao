package com.tjut.mianliao.data.contact;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.RosterPacket.ItemStatus;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;
import org.jivesoftware.smack.util.StringUtils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A full contact is consist of 2 parts:
 *
 * 1) UserEntry: only indicates contact relationship with others;
 *
 * 2) UserInfo: basic user information which are commonly required;
 */
public class UserEntry implements Parcelable{

    public static final String TABLE_NAME = "user_entry";
    public static final String JID = "jid";
    public static final String SUB_TYPE = "sub_type";
    public static final String SUB_STATUS = "sub_status";

    public String jid;
    public ItemType subType;
    public ItemStatus subStatus;
    public String name;
    public String alpha;

    public UserEntry() {}

    public UserEntry(String jid, String subType, String subStatus) {
        this.jid = jid;
        if (subType != null) {
            this.subType = ItemType.valueOf(subType);
        }
        if (subStatus != null) {
            this.subStatus = ItemStatus.fromString(subStatus);
        }
        this.name = StringUtils.parseName(jid);
    }

    public UserEntry(Parcel in) {
        jid = in.readString();
        subType = ItemType.valueOf(in.readString());
        subStatus =  ItemStatus.fromString(in.readString());
        name = in.readString();
        alpha = in.readString();
    }

    public static UserEntry fromRosterEntity(RosterEntry re) {
        UserEntry ue = new UserEntry();
        ue.jid = StringUtils.parseBareAddress(re.getUser());
        ue.subType = re.getType();
        ue.subStatus = re.getStatus();
        ue.name = StringUtils.parseName(ue.jid);
        return ue;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UserEntry) {
            UserEntry ue = (UserEntry) o;
            return jid.equals(ue.jid) && subType == ue.subType && subStatus == ue.subStatus;
        }
        return false;
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(jid);
        dest.writeString(subType == null ? null : subType.toString());
        dest.writeString(subStatus == null ? null : subStatus.toString());
        dest.writeString(name);
        dest.writeString(alpha);
    }

    public static final Parcelable.Creator<UserEntry> CREATOR =
            new Parcelable.Creator<UserEntry>() {
        @Override
        public UserEntry createFromParcel(Parcel in) {
            return new UserEntry(in);
        }

        @Override
        public UserEntry[] newArray(int size) {
            return new UserEntry[size];
        }
    };
}
