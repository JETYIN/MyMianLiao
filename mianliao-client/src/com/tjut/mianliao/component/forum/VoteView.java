package com.tjut.mianliao.component.forum;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProgressButton;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.PostActorsActivity;
import com.tjut.mianliao.forum.Vote;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class VoteView extends LinearLayout implements View.OnClickListener {

    private CfPost mPost;
    private LinearLayout mLlVotes;
    private ProgressButton mPbVote;
    private VoteItem mCurrentVote;

    private Activity mActivity;

    private boolean mLayoutInflated;

    public VoteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    public void show(CfPost post) {
        setVisibility(VISIBLE);
        if (!mLayoutInflated) {
            init();
        }
        mPost = post;
        showVoteInfo();
    }

    private void init() {
        mLayoutInflated = true;
        inflate(getContext(), R.layout.comp_vote_view, this);
        mPbVote = (ProgressButton) findViewById(R.id.pb_vote);
        mPbVote.setOnClickListener(this);
        findViewById(R.id.tv_voters).setOnClickListener(this);
        mLlVotes = (LinearLayout) findViewById(R.id.ll_votes);
    }

    public void showVoteInfo() {
        Vote vote = mPost.vote;
        mPbVote.setVisibility(vote.enabled ? View.VISIBLE : View.GONE);
        mPbVote.setText(getContext().getString(R.string.fv_vote_btn_text,
                DateUtils.getRelativeTimeSpanString(vote.endTime * 1000)));
        mLlVotes.removeAllViews();
        int sum = 0;
        int[] result = vote.result;
        for (int i = 0; i < result.length; i++) {
            sum += result[i];
        }
        int size = vote.options.size();
        int myVote = -1;
        if (vote.myVote != null && vote.myVote.length > 0) {
            myVote = vote.myVote[0];
        }
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < size; i++) {
            String option = vote.options.get(i);
            int votes = result[i];
            VoteItem vi = (VoteItem) inflater.inflate(R.layout.list_item_vote, mLlVotes, false);
            vi.setChecked(myVote == i);
            vi.setEnabled(vote.enabled);
            if (vote.enabled) {
                vi.setInfo(option);
                vi.setOnClickListener(this);
            } else {
                vi.setInfo(option, votes, sum);
            }
            mLlVotes.addView(vi);
        }

        findViewById(R.id.tv_voters).setVisibility(sum > 0 && vote.showVoters() ?
                View.VISIBLE : View.GONE);
    }

    private int getSelectedVote() {
        int count = mLlVotes.getChildCount();
        for (int i = 0; i < count; i++) {
            if (mLlVotes.getChildAt(i) == mCurrentVote) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onClick(View v) {
        if (v instanceof VoteItem) {
            if (mCurrentVote != null) {
                mCurrentVote.setChecked(false);
            }
            mCurrentVote = (VoteItem) v;
            mCurrentVote.setChecked(true);
        }
        switch (v.getId()) {
            case R.id.pb_vote:
                if (v instanceof  ProgressButton && !((ProgressButton) v).isInProgress()) {
                    if (mCurrentVote == null) {
                        toast(R.string.fv_tst_choice_not_made);
                    } else {
                        new VoteTask(getSelectedVote()).executeLong();
                    }
                }
                break;
            case R.id.tv_voters:
                Intent i = new Intent(getContext(), PostActorsActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(CfPost.INTENT_EXTRA_NAME, mPost);
                getContext().startActivity(i);
                break;
            default:
                break;
        }
    }

    private class VoteTask extends MsTask {
        private int mVote;

        public VoteTask(int vote) {
            super(getContext(), MsRequest.POST_EXTRA_ACTION);
            mVote = vote;
        }

        @Override
        protected void onPreExecute() {
            mPbVote.setInProgress(true);
        }

        @Override
        protected String buildParams() {
            return "thread_id=" + mPost.postId + "&vote=" + mVote;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            mPbVote.setInProgress(false);
            if (MsResponse.isSuccessful(response)) {
                Vote vote = mPost.vote;
                JSONObject json = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);
                vote.enabled = json.optBoolean("enable");
                vote.myVote = JsonUtil.getIntArray(json.optJSONArray("my_vote"));
                vote.myVoteTime = json.optLong("my_vote_time");
                vote.result = JsonUtil.getIntArray(json.optJSONArray("result"));
                vote.verifyResult();
                showVoteInfo();

                String errMsg = json.optString("err_msg");
                if (!TextUtils.isEmpty(errMsg)) {
                    toast(errMsg);
                }

                if (mActivity != null) {
                    Intent i = new Intent();
                    i.putExtra(CfPost.INTENT_EXTRA_NAME, mPost);
                    mActivity.setResult(BaseActivity.RESULT_UPDATED, i);
                }
            } else {
                toast(MsResponse.getFailureDesc(getRefContext(),
                        R.string.fv_vote_failed, response.code));
            }
        }
    }

    private void toast(int msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void toast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
