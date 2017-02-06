package com.tjut.mianliao.bounty;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.NameView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.ProgressButton;
import com.tjut.mianliao.data.bounty.BountyContract;
import com.tjut.mianliao.data.bounty.BountyRating;
import com.tjut.mianliao.data.bounty.BountyTask;
import com.tjut.mianliao.data.bounty.Credits;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class BountyRatingActivity extends BaseActivity implements OnClickListener {

    private RadioGroup mRgRating;
    private EditText mEtRating;
    private ProgressButton mPbSubmit;

    private BountyTask mTask;
    private BountyContract mContract;
    private RatingTask mLastTask;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_bounty_rating;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTask = getIntent().getParcelableExtra(BountyTask.INTENT_EXTRA_NAME);
        if (mTask == null) {
            toast(R.string.bty_tst_not_exist);
            finish();
            return;
        }
        mContract = getIntent().getParcelableExtra(BountyContract.INTENT_EXTRA_NAME);
        if (mContract == null) {
            toast(R.string.btyct_tst_not_exist);
            finish();
            return;
        }

        getTitleBar().showTitleText(R.string.bty_rating_title, null);

        mRgRating = (RadioGroup) findViewById(R.id.rg_rating);
        mEtRating = (EditText) findViewById(R.id.et_rating);
        mPbSubmit = (ProgressButton) findViewById(R.id.pb_submit);
        mPbSubmit.setOnClickListener(this);

        showTask(findViewById(R.id.rl_task));
        showContract(findViewById(R.id.rl_contract));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pb_submit:
                if (mLastTask == null) {
                    submitRating();
                }
                break;

            case R.id.av_avatar:
            case R.id.tv_user_name:
                viewUser((UserInfo) v.getTag());
                break;

            case R.id.tv_credit_level:
                viewCredits((Credits) v.getTag());
                break;

            default:
                break;
        }
    }

    private void showTask(View view) {
        showCredits(view, mTask.userCredit, true);
        view.setBackgroundResource(R.drawable.bg_item_with_top_bottom_lines);
        ((TextView) findViewById(R.id.tv_reward)).setText(mTask.reward);
        ((TextView) findViewById(R.id.tv_time)).setText(Utils.getTimeDesc(mTask.ctime));
        ((TextView) findViewById(R.id.tv_desc)).setText(mTask.desc);
        ((TextView) findViewById(R.id.tv_place)).setText(mTask.place);
        ((TextView) findViewById(R.id.tv_status)).setText(mTask.getStatusDesc());
    }

    private void showContract(View view) {
        if (mTask.isMine(this)) {
            showCredits(view, mContract.userCredit, false);
            Utils.setText(view, R.id.tv_time, getString(R.string.btyct_applyed,
                    Utils.getTimeDesc(mContract.createTime)));
            view.findViewById(R.id.pb_action).setVisibility(View.GONE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void showCredits(View view, Credits credits, boolean isHost) {
        UserInfo userInfo = credits.userInfo;

        ProImageView ivAvatar = (ProImageView) view.findViewById(R.id.av_avatar);
        ivAvatar.setImage(userInfo.getAvatar(), userInfo.defaultAvatar());
        ivAvatar.setOnClickListener(this);
        ivAvatar.setTag(userInfo);

        NameView tvName = (NameView) view.findViewById(R.id.tv_user_name);
        tvName.setText(userInfo.getDisplayName(this));
        tvName.setOnClickListener(this);
        tvName.setTag(userInfo);

        TextView tvCredit = (TextView) view.findViewById(R.id.tv_credit_level);
        tvCredit.setBackgroundResource(isHost ? R.drawable.inset_5_bg_credit_red
                : R.drawable.inset_5_bg_credit_purple);
        Resources res = getResources();
        tvCredit.setPadding(res.getDimensionPixelSize(R.dimen.bty_credit_padding_left), 0,
                res.getDimensionPixelSize(R.dimen.bty_credit_padding_right), 0);
        tvCredit.setText(getString(
                R.string.bty_credit_level, credits.getCreditLevel(isHost)));
        tvCredit.setOnClickListener(this);
        tvCredit.setTag(credits);
    }

    private void submitRating() {
        String comment = mEtRating.getText().toString();
        int rating;
        switch (mRgRating.getCheckedRadioButtonId()) {
            case R.id.rb_bad:
                if (TextUtils.isEmpty(comment)) {
                    toast(R.string.bty_rating_tst_bad_reason);
                    return;
                }
                rating = BountyRating.BAD;
                break;

            case R.id.rb_normal:
                rating = BountyRating.NORMAL;
                break;

            case R.id.rb_good:
            default:
                rating = BountyRating.GOOD;
                break;
        }
        new RatingTask(rating, comment).executeLong();
    }

    private void viewUser(UserInfo userInfo) {
        if (userInfo != null) {
            Intent iUser = new Intent(this, ProfileActivity.class);
            iUser.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
            startActivity(iUser);
        }
    }

    private void viewCredits(Credits credits) {
        if (credits != null) {
            Intent iCredits = new Intent(this, BountyCreditsActivity.class);
            iCredits.putExtra(Credits.INTENT_EXTRA_NAME, credits);
            iCredits.putExtra(BountyCreditsActivity.EXTRA_SHOW_GUEST,
                    credits != mTask.userCredit);
            startActivity(iCredits);
        }
    }

    private class RatingTask extends MsTask {
        private int mRating;
        private String mComment;

        public RatingTask(int rating, String comment) {
            super(getApplicationContext(), MsRequest.BTY_RATING);
            mRating = rating;
            mComment = comment;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("contract_id=").append(mContract.id)
                    .append("&rating=").append(mRating)
                    .append("&comment=").append(Utils.urlEncode(mComment))
                    .toString();
        }

        @Override
        protected void onPreExecute() {
            mPbSubmit.setInProgress(true);
            mLastTask = this;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPbSubmit.setInProgress(false);
            mLastTask = null;
            if (MsResponse.isSuccessful(response)) {
                toast(R.string.bty_rating_tst_success);
                setResult(RESULT_UPDATED, new Intent().putExtra(
                        BountyContract.INTENT_EXTRA_NAME, mContract));
                finish();
            } else {
                toast(MsResponse.getFailureDesc(getRefContext(),
                        R.string.bty_rating_tst_failed, response.code));
            }
        }
    }
}
