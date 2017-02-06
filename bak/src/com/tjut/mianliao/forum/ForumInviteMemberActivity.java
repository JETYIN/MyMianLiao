package com.tjut.mianliao.forum;

import java.util.List;

import android.os.Bundle;

import com.tjut.mianliao.R;
import com.tjut.mianliao.contact.CheckableContactsActivity;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.contact.UserEntry;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class ForumInviteMemberActivity extends CheckableContactsActivity {

    private UserInfoManager mUserInfoManager;
    private Forum mForum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mForum = getIntent().getParcelableExtra(Forum.INTENT_EXTRA_NAME);
        mUserInfoManager = UserInfoManager.getInstance(this);
    }

    @Override
    protected int getTitleResID() {
        return R.string.forum_invite_title;
    }

    @Override
    protected void doAction(List<UserEntry> checkedUsers) {
        new InviteTask(checkedUsers).executeLong();
    }

    private class InviteTask extends MsTask {
        private List<UserEntry> mUsers;

        public InviteTask(List<UserEntry> users) {
            super(getApplicationContext(), MsRequest.FORUM_INVITE);
            mUsers = users;
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder("forum_id=").append(mForum.id)
                    .append("&invitee_uids=");
            for (UserEntry ue: mUsers) {
                UserInfo info = mUserInfoManager.getUserInfo(ue.jid);
                sb.append(info.userId).append(",");
            }
            return sb.toString();
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
            getTitleBar().setRightTextEnabled(false);
        };

        @Override
        protected void onPostExecute(MsResponse response) {
            if (MsResponse.isSuccessful(response)) {
                toast(R.string.forum_invite_tst_success);
                finish();
            } else {
                getTitleBar().hideProgress();
                getTitleBar().setRightTextEnabled(true);
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.forum_invite_tst_failed, response.code));
            }
        };
    }
}
