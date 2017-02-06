package com.tjut.mianliao.forum;

import java.util.ArrayList;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ConfirmDialog;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.NameView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.contact.UserRemarkManager;
import com.tjut.mianliao.data.MenuItem;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.MenuHelper;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.PoliceHelper;
import com.tjut.mianliao.util.SnsHelper;
import com.tjut.mianliao.util.Utils;

public class ReplyAdapter extends BaseAdapter implements MsTaskListener, View.OnClickListener,
        DialogInterface.OnClickListener {

    private static final int IDENTITY = Utils.generateIdentify("ReplyAdapter");

    private Activity mActivity;
    private MsTaskManager mTaskManager;
    private UserRemarkManager mRemarkManager;
    private int mNameColor;

    private LightDialog mMenuDialog;
    private MenuHelper mMenuHelper;

    private ArrayList<CfReply> mReplies;
    private CfReply mCurrentReply;
    private CfPost mPost;

    public ReplyAdapter(Activity activity) {
        mActivity = activity;
        mTaskManager = MsTaskManager.getInstance(activity);
        mTaskManager.registerListener(this);
        mRemarkManager = UserRemarkManager.getInstance(mActivity);
        mNameColor = activity.getResources().getColor(R.color.txt_lightgray);
        mReplies = new ArrayList<CfReply>();
    }

    public void destroy() {
        SnsHelper.getInstance().closeShareBoard();
        mTaskManager.unregisterListener(this);
    }

    public ReplyAdapter setPost(CfPost post) {
        mPost = post;
        return this;
    }

    public void addAll(ArrayList<CfReply> replies) {
        mReplies.addAll(replies);
        notifyDataSetChanged();
    }

    public void reset(ArrayList<CfReply> replies) {
        mReplies.clear();
        addAll(replies);
    }

    public void add(CfReply reply) {
        mReplies.add(0, reply);
        notifyDataSetChanged();
    }

    public void remove(CfReply reply) {
        if (mReplies.remove(reply)) {
            notifyDataSetChanged();
        }
    }

    public void update(CfReply reply) {
        int index = mReplies.indexOf(reply);
        if (index != -1) {
            mReplies.remove(index);
            mReplies.add(index, reply);
            notifyDataSetChanged();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IDENTITY && resultCode == Activity.RESULT_OK && data != null) {
//            mTaskManager.startForumCommentTask(mCurrentReply, data.getStringExtra(ReplyActivity.EXTRA_RESULT));
        }
    }

    @Override
    public void onClick(View v) {
        mCurrentReply = (CfReply) v.getTag();
        switch (v.getId()) {
            case R.id.ll_reply:
                showMenuDialog();
                break;

            case R.id.av_avatar:
            case R.id.tv_name:
                showProfileActivity();
                break;

            default:
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mMenuDialog) {
            onMenuDialogClick(which);
        }
    }

    @Override
    public void onPreExecute(MsTaskType type) {
    }

    @Override
    public void onPostExecute(MsTaskType type, MsResponse response) {
        switch (type) {
            case FORUM_LIKE_REPLY:
                if (response.value instanceof CfReply) {
                    update((CfReply) response.value);
                }
                break;

            case FORUM_COMMENT_POST:
            case FORUM_COMMENT_REPLY:
                if (response.isSuccessful()) {
                    CfReply reply = CfReply.fromJson(response.getJsonObject());
                    if (reply != null) {
                        reply.targetPost = mPost;
                        add(reply);
                    }
                }
                break;

            case FORUM_DELETE_REPLY:
                if (response.value instanceof CfReply) {
                    remove((CfReply) response.value);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public int getCount() {
        return mReplies.size();
    }

    @Override
    public CfReply getItem(int position) {
        return mReplies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mActivity.getLayoutInflater().inflate(R.layout.list_item_reply, parent, false);
        }
        CfReply reply = getItem(position);
        view.setOnClickListener(this);
        view.setTag(reply);

        TextView tvLikesCount = (TextView) view.findViewById(R.id.tv_likes_count);
        if (reply.upCount > 0) {
            tvLikesCount.setVisibility(View.VISIBLE);
            tvLikesCount.setText(mActivity.getString(R.string.news_liked_count, reply.upCount));
        } else {
            tvLikesCount.setVisibility(View.GONE);
        }

        updateUser(view, reply);
        updateContent(view, reply);

        return view;
    }

    private void updateUser(View view, CfReply reply) {
        ProImageView ivAvatar = (ProImageView) view.findViewById(R.id.av_avatar);
        ivAvatar.setImage(reply.userInfo.getAvatar(), reply.userInfo.defaultAvatar());
        ivAvatar.setOnClickListener(this);
        ivAvatar.setTag(reply);

        NameView tvName = (NameView) view.findViewById(R.id.tv_name);
        tvName.setText(reply.userInfo.getDisplayName(mActivity));
        tvName.setMedal(reply.userInfo.primaryBadgeImage);
        tvName.setOnClickListener(this);
        tvName.setTag(reply);

        Utils.setText(view, R.id.tv_intro, reply.getTimeAndRelation());
    }

    private void updateContent(View view, CfReply reply) {
        CharSequence content = reply.content;
        if (reply.targetReplyId > 0) {
            String replyName = mRemarkManager.getRemark(reply.targetUid, reply.targetReplyName);
            content = Utils.getColoredText(mActivity.getString(R.string.news_comment_content, replyName, content),
                    replyName, mNameColor, false);
        }
        Utils.setText(view, R.id.tv_desc, Utils.getRefFriendText(content, mActivity));
    }

    private void showMenuDialog() {
        if (mMenuDialog == null) {
            mMenuHelper = new MenuHelper(mActivity, R.array.cf_record_menu);
            mMenuHelper.setEnabled(R.integer.mi_cf_share, false);
            mMenuHelper.setEnabled(R.integer.mi_cf_stick_thread, false);
            mMenuHelper.setEnabled(R.integer.mi_cf_show_post, false);
            mMenuHelper.setEnabled(R.integer.mi_cf_show_forum, false);
            mMenuDialog = new LightDialog(mActivity).setTitleLd(R.string.please_choose).setItems(mMenuHelper.getMenu(),
                    this);
        }
        mMenuHelper.update(R.integer.mi_cf_like,
                mActivity.getString(mCurrentReply.myUp ? R.string.cancel_like : R.string.like));
        mMenuHelper.setEnabled(R.integer.mi_cf_delete, mCurrentReply.canDelete);
        mMenuDialog.show();
    }

    private void onMenuDialogClick(int which) {
        MenuItem item = mMenuHelper.get(which);
        switch (item.id) {
            case R.integer.mi_cf_like:
                if (!mCurrentReply.liking) {
                    mTaskManager.startForumLikeTask(mCurrentReply);
                }
                break;

            case R.integer.mi_cf_reply:
                if (!mCurrentReply.replying) {
                    showReplyActivity();
                }
                break;

            case R.integer.mi_cf_report:
                PoliceHelper.reportForumPost(mActivity, mCurrentReply.postId);
                break;

            case R.integer.mi_cf_delete:
                ConfirmDialog.show(mActivity, R.string.cf_delete_reply_confirm, new Runnable() {
                    @Override
                    public void run() {
                        mTaskManager.startForumDeleteTask(mCurrentReply);
                    }
                });
                break;

            default:
                break;
        }
    }

    private void showReplyActivity() {
        Intent iReply = new Intent(mActivity, ReplyActivity.class);
        mActivity.startActivityForResult(iReply, IDENTITY);
    }

    private void showProfileActivity() {
        Intent iProfile = new Intent(mActivity, ProfileActivity.class);
        iProfile.putExtra(UserInfo.INTENT_EXTRA_INFO, mCurrentReply.userInfo);
        mActivity.startActivity(iProfile);
    }
}
