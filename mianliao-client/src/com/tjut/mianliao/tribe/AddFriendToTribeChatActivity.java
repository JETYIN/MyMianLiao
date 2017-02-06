package com.tjut.mianliao.tribe;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.tjut.mianliao.R;
import com.tjut.mianliao.chat.AddFriendToGroupActivity;
import com.tjut.mianliao.chat.ChatActivity;
import com.tjut.mianliao.chat.GroupChatInfoActivity;
import com.tjut.mianliao.data.contact.UserEntry;
import com.tjut.mianliao.data.tribe.TribeChatRoomInfo;

public class AddFriendToTribeChatActivity extends AddFriendToGroupActivity {
    
    public static final String EXTRA_RESULT = "extra_result";
    private TribeChatRoomInfo mChatRoomInfo;
    
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(R.string.tribe_add_friend_to_chat);
        mGroupManage.setVisibility(View.GONE);
        mCbAll.setVisibility(View.GONE);
        mChatRoomInfo = getIntent().getParcelableExtra(ChooseRoomAvatarActivity.EXT_CHAT_ROOM_DATA);
    }
    
     @Override
    protected void doAction(List<UserEntry> checkedUsers) {
        ArrayList<UserEntry> users = new ArrayList<UserEntry>();
        users.addAll(checkedUsers);
        String type = null;
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
            data.putExtra(ChooseRoomAvatarActivity.EXT_CHAT_ROOM_DATA, mChatRoomInfo);
            setResult(RESULT_UPDATED, data);
        }
        finish();
    }
}
