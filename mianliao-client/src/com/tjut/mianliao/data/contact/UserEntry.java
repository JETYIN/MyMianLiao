package com.tjut.mianliao.data.contact;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.StringUtils;
import com.tjut.mianliao.util.Utils;

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
    public String name;
    public String alpha;

    public UserEntry() {}

    public UserEntry(String jid) {
        this.jid = jid;
        this.name = StringUtils.parseName(jid);
    }

    public UserEntry(Parcel in) {
        jid = in.readString();
        name = in.readString();
        alpha = in.readString();
    }


    public static String parseJid(String jid){
        int index = jid.indexOf("@");
        jid = jid.replace(jid.substring(index, jid.length()), Utils.getJidSuffix());
        return jid;
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

    public static UserEntry fromRosterEntity(String userName) {
        UserEntry entry = new UserEntry();
        entry.name = userName;
        entry.jid = userName + Utils.getJidSuffix();
        return entry;
    }
}
