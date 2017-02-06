package com.tjut.mianliao.forum.nova;

import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.nova.VoteEditView;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.ChooseTopicActivity;
import com.tjut.mianliao.forum.TopicInfo;

public class TxtVotePostActivity extends BasePostActivity {

    public static final int REQUEST_TOPIC = 345;

    private VoteEditView mVoteEditView;
    private int mSchoolId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSchoolId = getIntent().getIntExtra(EXT_OTHER_SCHOOL_ID, 0);
        mInflater.inflate(R.layout.post_vote_view, mFlFooter);
        mVoteEditView = (VoteEditView) findViewById(R.id.vote_edit);
        mIvRefFriend.setVisibility(View.VISIBLE);
        mIvTopic.setVisibility(View.VISIBLE);
        mIvTopic.setOnClickListener(this);
        mPost.threadType = CfPost.THREAD_TYPE_TXT_VOTE;
        if (mGvImages != null) {
        	mGvImages.setVisibility(View.GONE);
        }
        mVoteEditView.setVisibility(View.VISIBLE);
    }

    @Override
    protected boolean setCanRefFriend() {
        return true;
    }

    @Override
    protected boolean isStateReady() {
        boolean result = super.isStateReady();
        if (result && mPost.threadType == CfPost.THREAD_TYPE_TXT_VOTE) {
            result = mVoteEditView.hasOptions() && mVoteEditView.isOptEnough();
            if (!result) {
                toast(R.string.fp_tst_vote_option_less);
            }
        }
        return result;
    }

    @Override
    protected boolean hasUpdate() {
        boolean result = super.hasUpdate();
        if (!result && mPost.threadType == CfPost.THREAD_TYPE_TXT_VOTE) {
            result = mVoteEditView.hasOptions();
        }
        return result;
    }

    @Override
    protected HashMap<String, String> getParams() {
        HashMap<String, String> params = super.getParams();
        params.put("options", mVoteEditView.getOptions());
        if (mSchoolId > 0) {
            params.put("other_school_id", String.valueOf(mSchoolId));
        }
        return params;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_topic:
                Intent intent = new Intent(TxtVotePostActivity.this, ChooseTopicActivity.class);
                if (mSchoolId > 0) {
                    intent.putExtra(ChooseTopicActivity.SCHOOL_ID, mSchoolId);
                }
                intent.putExtra(ChooseTopicActivity.FORUM_TYPE, mForum.type);
                startActivityForResult(intent, REQUEST_TOPIC);
                break;
            case R.id.tv_right:
                if (!isStateReady()) {
                    return;
                }
                submit();
                break;
            default:
            	super.onClick(v);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TOPIC && resultCode == RESULT_OK) {
            TopicInfo mTpInfo = data.getParcelableExtra(ChooseTopicActivity.TOPIC_INFO);
            String mTopicString = "#" + mTpInfo.name + "#";
            mTopicString = mTopicString.replaceAll("(@|﹫|＠)", "");
            int index = mEtContent.getSelectionStart();
            Editable edit = mEtContent.getEditableText();
            edit.insert(index, mTopicString);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
