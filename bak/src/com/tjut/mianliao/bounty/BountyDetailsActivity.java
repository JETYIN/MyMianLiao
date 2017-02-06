package com.tjut.mianliao.bounty;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.BountyView;
import com.tjut.mianliao.component.ConfirmDialog;
import com.tjut.mianliao.component.NameView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.ProgressButton;
import com.tjut.mianliao.data.bounty.BountyContract;
import com.tjut.mianliao.data.bounty.BountyTask;
import com.tjut.mianliao.data.bounty.Credits;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class BountyDetailsActivity extends BaseActivity implements
        OnRefreshListener2<ListView>, OnClickListener {

    private static final int ACTION_CANCEL = 1;
    private static final int ACTION_APPLY = 2;
    private static final int ACTION_FINISH = 3;

    private PullToRefreshListView mPtrlvBounty;
    private ProgressButton mPbAction;

    private BountyContractAdapter mAdapter;
    private BountyTask mTask;
    private int mAction;
    private boolean mIsHost;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_bounty_details;
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

        getTitleBar().showTitleText(R.string.btyct_title, null);
        mIsHost = mTask.isMine(this);

        mPbAction = (ProgressButton) findViewById(R.id.pb_action);
        mPbAction.setOnClickListener(this);

        mPtrlvBounty = (PullToRefreshListView) findViewById(R.id.ptrlv_bounty);
        mPtrlvBounty.setOnRefreshListener(this);
        mPtrlvBounty.setMode(Mode.BOTH);

        ListView listView = mPtrlvBounty.getRefreshableView();
        View header = mInflater.inflate(R.layout.bounty_details_header, listView, false);
        showTask(header);

        listView.addHeaderView(header);
        mAdapter = new BountyContractAdapter();
        listView.setAdapter(mAdapter);

        getTitleBar().showProgress();
        fetchContracts(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == getIdentity() && resultCode == RESULT_UPDATED && data != null) {
            BountyContract contract = data.getParcelableExtra(BountyContract.INTENT_EXTRA_NAME);
            int position = mAdapter.getPosition(contract);
            if (position != -1) {
                mAdapter.getItem(position).setRated(mIsHost);
                mAdapter.notifyDataSetChanged();
                updateAction();
            }
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchContracts(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchContracts(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pb_action:
                if (mAction == ACTION_CANCEL) {
                    ConfirmDialog.show(this, R.string.btyct_cancel_confirm, new Runnable() {
                        @Override
                        public void run() {
                            new CancelTask().executeLong();
                        }
                    });
                } else if (mAction == ACTION_APPLY) {
                    ConfirmDialog.show(this, R.string.btyct_disclaimer_title,
                            R.string.btyct_disclaimer_desc, new Runnable() {
                        @Override
                        public void run() {
                            new ApplyTask("").executeLong();
                        }
                    });
                } else {
                    ratingContract((BountyContract) v.getTag());
                }
                break;

            case R.id.av_avatar:
            case R.id.tv_user_name:
                viewUser((UserInfo) v.getTag());
                break;

            case R.id.tv_credit_level:
                viewCredits((Credits) v.getTag());
                break;

            case R.id.btn_right:
                new FavoriteTask().executeLong();
                break;

            default:
                break;
        }
    }

    private void fetchContracts(boolean refresh) {
        int offset = refresh ? 0 : mAdapter.getCount();
        new FetchContractsTask(offset).executeLong();
    }

    private void showTask(View view) {
        showFavorite();
        showCredits(view, mTask.userCredit, true);
        Utils.setText(view, R.id.tv_time, Utils.getTimeDesc(mTask.ctime));
        UserInfo userInfo = mTask.userCredit.userInfo;
        Utils.setText(view, R.id.tv_school, userInfo.school);
        ((ImageView) view.findViewById(R.id.iv_gender))
                .setImageResource(userInfo.getGenderIcon());

        BountyView bountyView = (BountyView) view.findViewById(R.id.bv_bounty);
        bountyView.show(mTask);
    }

    private void showFavorite() {
        getTitleBar().showRightButton(mTask.myFav ? R.drawable.btn_title_bar_unfavorite
                : R.drawable.btn_title_bar_favorite, this);
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
        tvCredit.setBackgroundResource(isHost ? R.drawable.inset_5_bg_credit_red :
                R.drawable.inset_5_bg_credit_purple);
        Resources res = getResources();
        tvCredit.setPadding(res.getDimensionPixelSize(R.dimen.bty_credit_padding_left), 0,
                res.getDimensionPixelSize(R.dimen.bty_credit_padding_right), 0);
        tvCredit.setText(getString(
                R.string.bty_credit_level, credits.getCreditLevel(isHost)));
        tvCredit.setOnClickListener(this);
        tvCredit.setTag(credits);
    }

    private void updateAction() {
        findViewById(R.id.fl_action).setVisibility(
                mIsHost && mAdapter.getCount() > 1 ? View.GONE : View.VISIBLE);
        if (mAdapter.isEmpty()) {
            if (mIsHost) {
                mPbAction.setEnabled(true);
                mPbAction.setText(R.string.btyct_action_cancel);
                mAction = ACTION_CANCEL;
            } else if (mTask.getStatus() == BountyTask.STATE_ONGOING) {
                mPbAction.setEnabled(true);
                mPbAction.setText(R.string.btyct_action_apply);
                mAction = ACTION_APPLY;
            } else {
                mPbAction.setEnabled(false);
                mPbAction.setText(mTask.getStatusDesc());
            }
        } else {
            updateAction(mPbAction, mAdapter.getItem(0));
        }
    }

    private void updateAction(ProgressButton pbAction, BountyContract contract) {
        if (contract.isOnGoing(mIsHost)) {
            pbAction.setEnabled(true);
            pbAction.setText(R.string.btyct_action_finish);
            pbAction.setTag(contract);
            mAction = ACTION_FINISH;
        } else {
            pbAction.setEnabled(false);
            pbAction.setText(contract.getStatusDesc());
        }
    }

    private void updateResult(int resultCode) {
        setResult(resultCode, new Intent().putExtra(
                BountyTask.INTENT_EXTRA_NAME, mTask));
    }

    private void ratingContract(BountyContract contract) {
        if (contract != null) {
            Intent iRating = new Intent(this, BountyRatingActivity.class);
            iRating.putExtra(BountyTask.INTENT_EXTRA_NAME, mTask);
            iRating.putExtra(BountyContract.INTENT_EXTRA_NAME, contract);
            startActivityForResult(iRating, getIdentity());
        }
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

    private class BountyContractAdapter extends ArrayAdapter<BountyContract> {

        public BountyContractAdapter() {
            super(getApplicationContext(), 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_bounty_contract, parent, false);
            }
            BountyContract contract = getItem(position);

            showCredits(view, contract.userCredit, false);

            Utils.setText(view, R.id.tv_time, getString(R.string.btyct_applyed,
                    Utils.getTimeDesc(contract.createTime)));

            ProgressButton pbAction = (ProgressButton) view.findViewById(R.id.pb_action);
            if (mIsHost && getCount() > 1) {
                pbAction.setVisibility(View.VISIBLE);
                pbAction.setOnClickListener(BountyDetailsActivity.this);
                updateAction(pbAction, contract);
            } else {
                pbAction.setVisibility(View.GONE);
            }

            return view;
        }
    }

    private class FetchContractsTask extends MsTask {
        private int mOffset;

        public FetchContractsTask(int offset) {
            super(getApplicationContext(), mIsHost ?
                    MsRequest.BTY_LIST_CONTRACT_BY_TASK : MsRequest.BTY_LIST_MY_SIGNED_CONTRACT);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("task_id=").append(mTask.id)
                    .append("&offset=").append(mOffset)
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mPtrlvBounty.onRefreshComplete();
            if (MsResponse.isSuccessful(response)) {
                mAdapter.setNotifyOnChange(false);
                if (mOffset == 0) {
                    mAdapter.clear();
                }
                mAdapter.addAll(JsonUtil.getArray(
                        response.getJsonArray(), BountyContract.TRANSFORMER));
                mAdapter.notifyDataSetChanged();
                updateAction();
            } else {
                toast(MsResponse.getFailureDesc(getRefContext(),
                        R.string.btyct_tst_fetch_failed, response.code));
            }
        }
    }

    private class FavoriteTask extends MsTask {

        public FavoriteTask() {
            super(getApplicationContext(), MsRequest.BTY_FAV);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("task_id=").append(mTask.id)
                    .append("&fav=").append(mTask.myFav ? 0 : 1)
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
                mTask.myFav = !mTask.myFav;
                showFavorite();
                updateResult(RESULT_UPDATED);
            } else {
                toast(MsResponse.getFailureDesc(getRefContext(),
                        R.string.bty_tst_fav_failed, response.code));
            }
        }
    }

    private class CancelTask extends MsTask {

        public CancelTask() {
            super(getApplicationContext(), MsRequest.BTY_CANCEL);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("task_id=").append(mTask.id).toString();
        }

        @Override
        protected void onPreExecute() {
            mPbAction.setInProgress(true);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPbAction.setInProgress(false);
            if (MsResponse.isSuccessful(response)) {
                updateResult(RESULT_DELETED);
                finish();
            } else {
                toast(MsResponse.getFailureDesc(getRefContext(),
                        R.string.bty_tst_cancel_failed, response.code));
            }
        }
    }

    private class ApplyTask extends MsTask {
        private String mMessage;

        public ApplyTask(String message) {
            super(getApplicationContext(), MsRequest.BTY_APPLY);
            mMessage = message;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("task_id=").append(mTask.id)
                    .append("&msg=").append(mMessage).toString();
        }

        @Override
        protected void onPreExecute() {
            mPbAction.setInProgress(true);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPbAction.setInProgress(false);
            if (MsResponse.isSuccessful(response)) {
                BountyContract contract = BountyContract.fromJson(response.getJsonObject());
                if (contract != null) {
                    mAdapter.add(contract);
                    updateAction(mPbAction, contract);
                }
            } else {
                toast(MsResponse.getFailureDesc(getRefContext(),
                        R.string.bty_tst_apply_failed, response.code));
            }
        }
    }
}
