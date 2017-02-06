package com.tjut.mianliao.contact;

import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;

import com.tjut.mianliao.data.contact.UserEntry;

public class ContactsBlacklistAdapter extends ContactsAdapter {

    public ContactsBlacklistAdapter(Context context) {
        super(context);
    }

    @Override
    public Collection<UserEntry> getContacts() {
        ArrayList<UserEntry> contacts = new ArrayList<UserEntry>();
        for (String jid : mUserEntryManger.getBlacklist()) {
            UserEntry ue = mUserEntryManger.getUserEntry(jid);
            if (ue == null) {
                ue = new UserEntry(jid);
            }
            contacts.add(ue);
        }
        return contacts;
    }
}
