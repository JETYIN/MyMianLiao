package com.tjut.mianliao.notice;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.text.TextUtils;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.notice.Notice;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.CfReply;
import com.tjut.mianliao.forum.CourseForumActivity;
import com.tjut.mianliao.forum.ReplyActivity;
import com.tjut.mianliao.forum.nova.ForumPostDetailActivity;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class NoticeForumActivity extends NoticeListActivity implements OnClickListener {

    private static final int REQUEST_REPLY = 101;

    private LightDialog mMenuDialog;

    private Notice mNotice;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REPLY && resultCode == RESULT_OK) {
            doReply(data == null ? null : data.getStringExtra(ReplyActivity.EXTRA_RESULT));
        }
    }

    @Override
    protected Item getItem(Notice notice) {
        Item item = super.getItem(notice);
        switch (notice.category) {
            case Notice.CAT_FORUM_REPLY:
            case Notice.CAT_FORUM_REPLY_AT:
                if (notice.forumReply != null && notice.forumReply.targetPost != null) {
                    item.userInfo = notice.forumReply.userInfo;
                    item.time = notice.forumReply.createdOn;
                    CfReply cfReply = notice.forumReply.targetReply;
                    if (notice.category == Notice.CAT_FORUM_REPLY && cfReply != null) {
                        item.category = Utils.getColoredText(
                                getString(R.string.ntc_forum_reply_reply, cfReply.content),
                                cfReply.content, mKeyColor, false);
                    } else {
                        int resId = notice.category == Notice.CAT_FORUM_REPLY
                                ? R.string.ntc_forum_reply_post : R.string.ntc_forum_reply_at;
                        item.category = Utils.getColoredText(
                                getString(resId, notice.forumReply.targetPost.title),
                                notice.forumReply.targetPost.title, mKeyColor, false);
                    }
                    item.desc = notice.forumReply.content;
                }
                break;

            case Notice.CAT_FORUM_INVITE:
            case Notice.CAT_FORUM_POST_SHARE:
                if (notice.forum != null && notice.forumInvitor != null) {
                    item.userInfo = notice.forumInvitor;
                    item.time = notice.time;
                    int resId = notice.category == Notice.CAT_FORUM_INVITE
                            ? R.string.ntc_forum_invite : R.string.ntc_forum_post_share;
                    item.desc = Utils.getColoredText(
                            getString(resId, notice.forum.name),
                            notice.forum.name, mKeyColor, false);
                }
                break;


            case Notice.CAT_FORUM_POST_AT:
                if (notice.forumPost != null) {
                    item.userInfo = notice.forumPost.userInfo;
                    item.time = notice.forumPost.createdOn;
                    item.category = Utils.getColoredText(
                            getString(R.string.ntc_forum_post_at, notice.forumPost.title),
                            notice.forumPost.title, mKeyColor, false);
                    item.desc = notice.forumPost.content;
                }
                break;

            case Notice.CAT_FORUM_POST_UPDATED:
                if (notice.forumPost != null) {
                    item.userInfo = notice.forumPost.userInfo;
                    item.time = notice.time;
                    item.desc = Utils.getColoredText(
                            getString(R.string.ntc_forum_post_updated, notice.forumPost.title),
                            notice.forumPost.title, mKeyColor, false);
                }
                break;

            default:
                break;
        }
        return item;
    }

    @Override
    protected void onItemClick(Notice notice) {
        mNotice = notice;
        switch (notice.category) {
            case Notice.CAT_FORUM_INVITE:
                viewForum(notice.forum);
                break;

            case Notice.CAT_FORUM_POST_SHARE:
            case Notice.CAT_FORUM_POST_AT:
            case Notice.CAT_FORUM_POST_UPDATED:
                viewPost(notice.forumPost);
                break;

            case Notice.CAT_FORUM_REPLY:
            case Notice.CAT_FORUM_REPLY_AT:
                showMenuDialog();
                break;

            default:
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case 0:
                showReplyActivity();
                break;

            case 1:
                viewPost(mNotice.forumReply.targetPost);
                break;

            default:
                break;
        }
    }

    private void showMenuDialog() {
        if (mMenuDialog == null) {
            mMenuDialog = new LightDialog(this);
            mMenuDialog.setTitle(R.string.please_choose);
            mMenuDialog.setItems(R.array.ntc_forum_menu, this);
        }
        mMenuDialog.show();
    }

    private void showReplyActivity() {
        Intent intent = new Intent(this, ReplyActivity.class);
        startActivityForResult(intent, REQUEST_REPLY);
    }

    private void viewForum(Forum forum) {
        Intent intent = new Intent(this, CourseForumActivity.class);
        intent.putExtra(Forum.INTENT_EXTRA_NAME, forum);
        startActivity(intent);
    }

    private void viewPost(CfPost cfPost) {
        Intent intent = new Intent(this, ForumPostDetailActivity.class);
        intent.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA, cfPost);
        intent.putExtra(ForumPostDetailActivity.EXTRL_CHANNEL_INFO, new ChannelInfo());
        startActivity(intent);
    }

    private void doReply(String reply) {
        if (!TextUtils.isEmpty(reply)) {
            new ReplyTask(reply, mNotice.forumReply).executeLong();
        }
    }

    private class ReplyTask extends MsTask {
        private String mContent;
        private CfReply mReply;

        public ReplyTask(String content, CfReply reply) {
            super(getApplicationContext(), MsRequest.CF_REPLY);
            mContent = content;
            mReply = reply;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder().append("reply_id=").append(mReply.replyId)
                    .append("&thread_id=").append(mReply.targetPost.postId)
                    .append("&content=").append(Utils.urlEncode(mContent))
                    .toString();
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (MsResponse.isSuccessful(response)) {
                mReply.targetPost.replyCount++;
                toast(R.string.cf_reply_success);
            } else {
                toast(MsResponse.getFailureDesc(getRefContext(),
                        R.string.cf_reply_failed, response.code));
            }
        }
    }
}