package com.tjut.mianliao.component;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.forum.EditForumActivity;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class ForumHeader extends RelativeLayout implements View.OnClickListener {

    private static final int IDENTITY = Utils.generateIdentify("ForumHeader");

    private Activity mActivity;
    private Forum mForum;

    private TextView mTvName;
    private View mHeader;

    private TextView mTvTitle;
    private TextView mTvIntro;
    private TextView mTvMemberCount;
    private TextView mTvPostCount;
    private TextView mBtnAction;
    private TitleBar mTitleBar;

    private ProImageView mIvIcon;
    private ProImageView mIvScene;
    private ImageView mIvBadge;

    public ForumHeader(Context context) {
        this(context, null);
    }

    public ForumHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.forum_header, this);

        mTvName = (TextView) findViewById(R.id.tv_name);
        mHeader = findViewById(R.id.rl_header);

        mBtnAction = (TextView) findViewById(R.id.btn_action);
        mBtnAction.setOnClickListener(this);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvIntro = (TextView) findViewById(R.id.tv_intro);
        mTvMemberCount = (TextView) findViewById(R.id.tv_member_count);
        mTvPostCount = (TextView) findViewById(R.id.tv_posts_count);

        mIvIcon = (ProImageView) findViewById(R.id.av_forum_icon);
        mIvScene = (ProImageView) findViewById(R.id.sv_scene);
        mIvBadge = (ImageView) findViewById(R.id.iv_forum_badge);
    }

    public ForumHeader setActivity(Activity activity) {
        mActivity = activity;
        return this;
    }

    public ForumHeader setTitleBar(TitleBar titleBar) {
        mTitleBar = titleBar;
        return this;
    }

    public void setForum(Forum forum) {
        mForum = forum;
        if (!forum.isUserForum()) {
            mHeader.setVisibility(GONE);
            mTvName.setVisibility(View.VISIBLE);
            mTvName.setText(mForum.name);
            return;
        }

        mTvName.setVisibility(GONE);
        mHeader.setVisibility(VISIBLE);
        if (!forum.isMember) {
            mBtnAction.setText(R.string.ef_act_join);
        } else if (forum.isAdmin(getContext())) {
            mBtnAction.setText(R.string.ef_act_manage);
        } else {
            mBtnAction.setText(R.string.ef_act_view);
        }

        mTvTitle.setText(mForum.name);
        mTvIntro.setText(mForum.intro);
        mTvMemberCount.setText(String.valueOf(mForum.memberCount));
        mTvPostCount.setText(String.valueOf(mForum.threadCount));

        int privacyIcon;
        switch (mForum.privacy) {
            case Forum.PRIVACY_PRIVATE:
                privacyIcon = R.drawable.ic_forum_private;
                break;
            case Forum.PRIVACY_STRANGER_READONLY:
                privacyIcon = R.drawable.ic_forum_read_only;
                break;
            case Forum.PRIVACY_PUBLIC:
            default:
                privacyIcon = R.drawable.ic_forum_open;
                break;
        }
        mTvTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, privacyIcon, 0);

        if (forum.isVip()) {
            mIvBadge.setVisibility(View.VISIBLE);
            mIvBadge.setImageResource(R.drawable.ic_vip);
        } else if (forum.isUserForum()) {
            mIvBadge.setVisibility(View.VISIBLE);
            mIvBadge.setImageResource(R.drawable.ic_forum_badge);
        } else {
            mIvBadge.setVisibility(View.GONE);
        }

        mIvIcon.setImage(mForum.icon, R.drawable.ic_avatar_forum);
        mIvScene.setImage(mForum.bgImg, R.drawable.pic_forum_scene);
    }

    private void toast(int msg) {
        toast(getResources().getString(msg));
    }

    private void toast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_action) {
            if (!mForum.isMember) {
                new JoinForumTask().executeLong();
            } else {
                Intent i = new Intent(getContext(), EditForumActivity.class);
                i.putExtra(Forum.INTENT_EXTRA_NAME, mForum);
                if (mActivity != null) {
                    mActivity.startActivityForResult(i, IDENTITY);
                }
            }
        }
    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IDENTITY) {
            if (resultCode == Activity.RESULT_OK
                    || resultCode == BaseActivity.RESULT_UPDATED) {
                Forum forum = data.getParcelableExtra(Forum.INTENT_EXTRA_NAME);
                if (forum != null) {
                    setForum(forum);
                    mActivity.setResult(BaseActivity.RESULT_UPDATED, data);
                }
                return true;
            } else if (resultCode == BaseActivity.RESULT_DELETED) {
                Intent i = new Intent();
                i.putExtra(Forum.INTENT_EXTRA_NAME, mForum);
                mActivity.setResult(BaseActivity.RESULT_DELETED, i);
                mActivity.finish();
                return true;
            }
        }
        return false;
    }

    private class JoinForumTask extends MsTask {
        public JoinForumTask() {
            super(getContext(), MsRequest.CF_REQUEST_MEMBER);
        }

        @Override
        protected void onPreExecute() {
            mTitleBar.showProgress();
            mBtnAction.setEnabled(false);
        }

        @Override
        protected String buildParams() {
            return "forum_id=" + mForum.id;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mTitleBar.hideProgress();
            mBtnAction.setEnabled(true);
            if (MsResponse.isSuccessful(response)) {
                if (response.json.optJSONObject(MsResponse.PARAM_RESPONSE).optInt("joined") == 1) {
                    mForum.isMember = true;
                    mForum.memberCount++;
                    setForum(mForum);
                    mActivity.setResult(BaseActivity.RESULT_UPDATED,
                            new Intent().putExtra(Forum.INTENT_EXTRA_NAME, mForum));
                    toast(R.string.ef_act_join_success_joint);
                } else {
                    toast(R.string.ef_act_join_success);
                }
            } else {
                toast(MsResponse.getFailureDesc(getContext(), R.string.ef_act_join_failed,
                        response.code));
            }
        }
    }
}
