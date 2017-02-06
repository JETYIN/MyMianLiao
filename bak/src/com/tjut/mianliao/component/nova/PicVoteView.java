package com.tjut.mianliao.component.nova;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.Vote;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class PicVoteView extends LinearLayout implements OnClickListener {

    private LayoutInflater mInflater;
    private CfPost mPost;
    private TextView mTvShow;
    private ProImageView mPivImg1, mPivImg2;
    private LinearLayout mLlImg1, mLlImg2;
    private TextView mTvCount1, mTvCount2;
    private TextView mTvVote1, mTvVote2;
    private ImageView mIvStatus;
    private FrameLayout mFlVote1, mFlVote2;

    public PicVoteView(Context context) {
        super(context);
        init(context);
    }

    public PicVoteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        mInflater = LayoutInflater.from(context);
        mInflater.inflate(R.layout.pic_vote_view, this);
        mTvShow = (TextView) findViewById(R.id.tv_show);
//        mTvShow.setText("请点击图片进行PK");
        mPivImg1 = (ProImageView) findViewById(R.id.iv_photo1);
        mPivImg2 = (ProImageView) findViewById(R.id.iv_photo2);
        mLlImg1 = (LinearLayout) findViewById(R.id.ll_vote_num1);
        mLlImg2 = (LinearLayout) findViewById(R.id.ll_vote_num2);
        mTvCount1 = (TextView) mLlImg1.findViewById(R.id.tv_vote_num1);
        mTvCount2 = (TextView) mLlImg2.findViewById(R.id.tv_vote_num2);
        mTvVote1 = (TextView) findViewById(R.id.tv_vote1);
        mTvVote2 = (TextView) findViewById(R.id.tv_vote2);
        mIvStatus = (ImageView) findViewById(R.id.iv_status);
        mFlVote1 = (FrameLayout) findViewById(R.id.fl_vote1);
        mFlVote2 = (FrameLayout) findViewById(R.id.fl_vote2);
        mFlVote1.setVisibility(View.GONE);
        mFlVote2.setVisibility(View.GONE);
        mPivImg1.setOnClickListener(this);
        mPivImg2.setOnClickListener(this);
    }

    public void show(CfPost post) {
        if (post.vote != null) {
            mPost = post;
            updateVoteShow();
        }
    }

    private void updateVoteShow() {
        Vote vote = mPost.vote;
        vote.verifyResult();
        Image img1 = mPost.images.get(0);
        Image img2 = mPost.images.get(1);
        mPivImg1.setImage(getImagePreviewSmall(img1.image), R.drawable.bg_img_loading);
        mPivImg2.setImage(getImagePreviewSmall(img2.image), R.drawable.bg_img_loading);
        mLlImg1.setVisibility(mPost.vote.enabled ? View.GONE : View.VISIBLE);
        mLlImg2.setVisibility(mPost.vote.enabled ? View.GONE : View.VISIBLE);
        if (mPost.vote != null && mPost.vote.result != null) {
            mTvCount1.setText(mPost.vote.result[0] + "人");
            mTvCount2.setText(mPost.vote.result[1] + "人");
            
        }
        mIvStatus.setImageResource(mPost.vote.enabled ? R.drawable.ic_vs : R.drawable.img_read);
        if (mPost.vote.myVote != null && mPost.vote.myVote.length > 0) {
            int pos = mPost.vote.myVote[0];
            mTvCount1.setCompoundDrawablesWithIntrinsicBounds(
                    pos == 0 ? R.drawable.buttom_like_hover : R.drawable.buttom_like, 0, 0, 0);
            mTvCount2.setCompoundDrawablesWithIntrinsicBounds(
                    pos == 1 ? R.drawable.buttom_like_hover : R.drawable.buttom_like, 0, 0, 0);
        }
    }

    private String getImagePreviewSmall(String url) {
        return AliImgSpec.POST_THUMB_SQUARE.makeUrl(url);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_photo1:
                if (mPost.vote.enabled) {
                    new VoteTask(0).executeLong();
                }
                break;
            case R.id.iv_photo2:
                if (mPost.vote.enabled) {
                    new VoteTask(1).executeLong();
                }
                break;
            default:
                break;
        }
    }

    private class VoteTask extends MsTask{

        private int mVote;

        public VoteTask(int vote) {
            super(getContext(), MsRequest.POST_EXTRA_ACTION);
            mVote = vote;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder().append("thread_id=").append(mPost.postId)
                    .append("&vote=").append(mVote).toString();
        }

        @SuppressLint("ShowToast")
        @Override
        protected void onPostExecute(MsResponse response) {
            if (MsResponse.isSuccessful(response)) {
                JSONObject json = response.getJsonObject();
                mPost.vote.enabled = json.optBoolean("enable");
                mPost.vote.myVote = JsonUtil.getIntArray(json.optJSONArray("my_vote"));
                mPost.vote.myVoteTime = json.optLong("my_vote_time");
                mPost.vote.result = JsonUtil.getIntArray(json.optJSONArray("result"));
                String errMsg = json.optString("err_msg");
                if (!TextUtils.isEmpty(errMsg)) {
                    Toast.makeText(getContext(), errMsg, Toast.LENGTH_SHORT);
                }
                updateVoteShow();
            } else {
                Toast.makeText(getContext(), MsResponse.getFailureDesc(getRefContext(),
                    R.string.fv_vote_failed, response.code), Toast.LENGTH_SHORT);
            }
        }

    }

}
