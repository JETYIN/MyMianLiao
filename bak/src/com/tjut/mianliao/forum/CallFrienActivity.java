package com.tjut.mianliao.forum;

import java.util.List;

import com.tjut.mianliao.R;
import com.tjut.mianliao.contact.CheckableContactsActivity;
import com.tjut.mianliao.data.contact.UserEntry;

public class CallFrienActivity extends CheckableContactsActivity{

    @Override
    protected int getTitleResID() {
        return R.string.cf_call_friend_title;
    }

    @Override
    protected void doAction(List<UserEntry> checkedUsers) {
        
    }

}
