package com.tjut.mianliao.data.contact;

public class CheckableUserEntry extends UserEntry {

    public boolean checked;

    public CheckableUserEntry(UserEntry ue) {
        jid = ue.jid;
        name = ue.name;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
