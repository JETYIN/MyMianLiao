package com.tjut.mianliao.component.nova;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.Vote;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class VoteView extends LinearLayout {

    private static final int[] sColors = {
        0xfffdb6b6, 0xff99ccf8, 0xffbba8e6, 0xff84e0d4
    };

    private static final int[] sCircleColors = {
        0XFFFFDEDE, 0XFFE1EFFF, 0XFFDAD4FF, 0XFFC1F7E2
    };
    
    private LayoutInflater mInflater;
    private TextView mTvCount;
    private OptionAdapter mAdapter;
    private CfPost mPost;
    private int mTotalCount;
    private boolean mIsVoting;
    private GridView mGvOption;
    private Context mContext;

    public VoteView(Context context) {
        super(context);
        mContext = context;
        init(context);
    }

    public VoteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(context);
    }

    public void show(CfPost post) {
        if (post.hasVote()) {
            mPost = post;
            showVote();
        }
    }

    private void showVote() {
        Vote vote = mPost.vote;
        vote.verifyResult();
        mTotalCount = 0;
        for (int i = 0; i < vote.result.length; i++) {
            mTotalCount += vote.result[i];
        }
        float progress = 0;
        for (int i = 0; i < vote.result.length; i++) {
            if (vote.progress == null) {
                vote.progress = new float[vote.result.length];
            }
            if (i < vote.result.length - 1) {
                if (vote.result[i] == 0) {
                    vote.progress[i] = 0f;
                } else {
                    String prog = String.format("%.0f", vote.result[i] * 100f / mTotalCount);
                    vote.progress[i] = Float.parseFloat(prog);
                    progress += vote.progress[i];
                }
            } else {
                vote.progress[i] = 100 - progress;
            }
        }
        
        if (mPost.vote.enabled) {
            mTvCount.setText(mContext.getString(R.string.fp_post_type_vote));
        } else {
            mTvCount.setText(mContext.getString(R.string.fp_vote_people_num, mTotalCount));
        }
//        mTvCount.setVisibility(mTotalCount == 0 ? View.GONE : View.VISIBLE);
        mGvOption.setNumColumns(mAdapter.getCount());
        mAdapter.notifyDataSetChanged();
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        mInflater = LayoutInflater.from(context);
        mInflater.inflate(R.layout.vote_view, this);
        mTvCount = (TextView) findViewById(R.id.tv_count);
        mTvCount.setVisibility(View.VISIBLE);
        mAdapter = new OptionAdapter();
        mGvOption = (GridView) findViewById(R.id.gv_option);
        mGvOption.setAdapter(mAdapter);
        mGvOption.setGravity(Gravity.CENTER_HORIZONTAL);
        mGvOption.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mPost.vote.enabled && !mIsVoting) {
                    new VoteTask(position).executeLong();
                }
            }
        });
    }

    private class OptionAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mPost == null ? 0 : mPost.getVoteOptCount();
        }

        @Override
        public String getItem(int position) {
            return mPost.getVoteOpt(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.vote_option, parent, false);
            } else {
                view = convertView;
            }
            if (view instanceof SuperVoteOption) {
                SuperVoteOption option = (SuperVoteOption) view;
                if (mPost.vote.myVote != null && position != mPost.vote.myVote[0]) {
                    option.setColor(0XFFD2D2D2);
                    option.setCircleColor(0XFFD2D2D2);
                } else {
                    option.setColor(sColors[position]);
                    option.setCircleColor(sCircleColors[position]);
                    if (mPost.vote.myVote != null) {
//                        option.setLineShow(true);
                    }
                }
                option.setContent(getItem(position));
                option.setProgressShown(!mPost.vote.enabled);
                option.setProgressTextColor(mPost.vote.myVote != null
                        && position == mPost.vote.myVote[0] ? Color.RED : Color.WHITE);
                option.setProgress(mTotalCount == 0 ? 0 : mPost.vote.progress[position]);
                option.setLineShow(mPost.vote.myVote != null && mPost.vote.myVote[0] == position);
            }
            return view;
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
            mIsVoting = true;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("thread_id=").append(mPost.postId)
                    .append("&vote=").append(mVote)
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mIsVoting = false;
            if (response.isSuccessful()) {
                JSONObject json = response.getJsonObject();
                mPost.vote.enabled = json.optBoolean("enable");
                mPost.vote.myVote = JsonUtil.getIntArray(json.optJSONArray("my_vote"));
                mPost.vote.myVoteTime = json.optLong("my_vote_time");
                mPost.vote.result = JsonUtil.getIntArray(json.optJSONArray("result"));
                showVote();
            } else {
                response.showFailInfo(getRefContext(), R.string.fv_vote_failed);
            }
        }
    }
}
