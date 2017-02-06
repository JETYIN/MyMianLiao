package com.tjut.mianliao.chat;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.tjut.mianliao.R;
import com.tjut.mianliao.contact.CheckableContactsActivity;
import com.tjut.mianliao.data.contact.UserEntry;

public class AddFriendToGroupActivity extends CheckableContactsActivity {

    public static final String EXTRA_RESULT = "extra_result";
    
    @Override
    protected int getTitleResID() {
        return R.string.add_friends_to_group;
    }
    
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGroupManage.setVisibility(View.VISIBLE);
        mGroupManage.setOnClickListener(this);
        mCbAll.setVisibility(View.GONE);
    }
    
     @Override 
    protected void doAction(List<UserEntry> checkedUsers) {
        ArrayList<UserEntry> users = new ArrayList<UserEntry>();
        users.addAll(checkedUsers);
        String type = null;
        for (UserEntry ue : users) {
            type = null;
        }
        boolean isGroup = getIntent().getBooleanExtra(GroupChatInfoActivity.ADD_FRIEND, false);
        String mChatTarget = getIntent().getStringExtra(ChatActivity.EXTRA_CHAT_TARGET);
        if (isGroup) {
            Intent data = new Intent();
            data.putParcelableArrayListExtra(EXTRA_RESULT, users);
            setResult(RESULT_OK, data);
        } else {
            Intent data = new Intent(this, ChatActivity.class);
            if (mChatTarget != null) {
                users.add(new UserEntry(mChatTarget));
            }
            data.putParcelableArrayListExtra(EXTRA_RESULT, users);
            data.putExtra(ChatActivity.EXTRA_SHOW_PROFILE, true);
            if (isSetResult) {
                startActivity(data);
            } else {
                setResult(RESULT_UPDATED, data);
            }
        }
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_groupchat_manage){
            Intent intent = new Intent(AddFriendToGroupActivity.this,GroupChooseActivity.class);
            startActivity(intent);
        } else {
            super.onClick(v);
        }
    }

}
