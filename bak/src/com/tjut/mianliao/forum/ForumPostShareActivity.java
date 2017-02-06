package com.tjut.mianliao.forum;

import java.util.List;

import android.os.Bundle;

import com.tjut.mianliao.R;
import com.tjut.mianliao.contact.CheckableContactsActivity;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.contact.UserEntry;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class ForumPostShareActivity extends CheckableContactsActivity {

    private UserInfoManager mUserInfoManager;
    private CfPost mPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPost = getIntent().getParcelableExtra(CfPost.INTENT_EXTRA_NAME);
        mUserInfoManager = UserInfoManager.getInstance(this);
    }

    @Override
    protected int getTitleResID() {
        return R.string.fp_share_title;
    }

    @Override
    protected void doAction(List<UserEntry> checkedUsers) {
        new ShareTask(checkedUsers).executeLong();
    }

    private class ShareTask extends MsTask {
        private List<UserEntry> mUsers;

        public ShareTask(List<UserEntry> users) {
            super(getApplicationContext(), MsRequest.FORUM_POST_SHARE);
            mUsers = users;
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder("thread_id=").append(mPost.postId)
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
                toast(R.string.fp_share_tst_success);
                finish();
            } else {
                getTitleBar().hideProgress();
                getTitleBar().setRightTextEnabled(true);
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.fp_share_tst_failed, response.code));
            }
        };
    }
}
