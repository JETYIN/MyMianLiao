package com.tjut.mianliao;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;

import com.tjut.mianliao.contact.CheckableContactsActivity;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.contact.UserEntry;
import com.tjut.mianliao.data.contact.UserInfo;

public class RefFriendActivity extends CheckableContactsActivity {
    public static final String EXTRA_RESULT = "extra_result";
    public static final String EXTRA_USERINFOS = "extra_userinfos";

    @Override
    protected int getTitleResID() {
        return R.string.ref_friends;
    }

    @Override
    protected void doAction(List<UserEntry> checkedUsers) {
        StringBuilder sb = new StringBuilder();
        UserInfoManager uim = UserInfoManager.getInstance(this);
        ArrayList<UserInfo> userInfos = new ArrayList<UserInfo>();
        for (UserEntry ue : checkedUsers) {
            UserInfo userInfo = uim.getUserInfo(ue.jid);
            sb.append("@")
                    .append(userInfo.getNickname())
                    .append(" ");
            userInfos.add(userInfo);
        }

        Intent data = new Intent();
        data.putExtra(EXTRA_RESULT, sb.toString());
        data.putExtra(EXTRA_USERINFOS, userInfos);
        setResult(RESULT_OK, data);
        finish();
    }
}
