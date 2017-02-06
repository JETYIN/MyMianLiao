package com.tjut.mianliao.bounty;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.tjut.mianliao.component.NameView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.tab.Tab;
import com.tjut.mianliao.component.tab.TabController;
import com.tjut.mianliao.component.tab.TabController.TabListener;
import com.tjut.mianliao.component.tab.TextTab;
import com.tjut.mianliao.data.bounty.BountyRating;
import com.tjut.mianliao.data.bounty.Credit;
import com.tjut.mianliao.data.bounty.Credits;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class BountyCreditsActivity extends BaseActivity implements
        OnClickListener, TabListener, OnRefreshListener2<ListView> {

    public static final String EXTRA_SHOW_GUEST = "extra_show_guest";

    private static final int TAB_HOST = 0;
    private static final int TAB_GUEST = 1;

    private PullToRefreshListView mPtrlvRating;
    private TabController mTabController;
    private RatingAdapter mAdapter;

    private Credits mCredits;
    private FetchRatingsTask mLastTask;
    private boolean mShowGuest;
    private ArrayList<ArrayList<BountyRating>> mRatings;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_bounty_credits;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCredits = getIntent().getParcelableExtra(Credits.INTENT_EXTRA_NAME);
        if (mCredits == null) {
            toast(R.string.bty_credits_tst_not_exist);
            finish();
            return;
        }

        mShowGuest = getIntent().getBooleanExtra(EXTRA_SHOW_GUEST, false);
        getTitleBar().showTitleText(R.string.bty_credits_title, null);
        ((NameView) findViewById(R.id.tv_name)).setText(
                mCredits.userInfo.getDisplayName(this));

        mRatings = new ArrayList<ArrayList<BountyRating>>();
        mRatings.add(new ArrayList<BountyRating>());
        mRatings.add(new ArrayList<BountyRating>());

        mAdapter = new RatingAdapter();
        mPtrlvRating = (PullToRefreshListView) findViewById(R.id.ptrlv_rating);
        mPtrlvRating.setOnRefreshListener(this);
        mPtrlvRating.setMode(Mode.BOTH);
        mPtrlvRating.setAdapter(mAdapter);

        mTabController = new TabController();
        mTabController.add(new TextTab((TextView) findViewById(R.id.tv_host)));
        mTabController.add(new TextTab((TextView) findViewById(R.id.tv_guest)));
        mTabController.select(mShowGuest ? TAB_GUEST : TAB_HOST);
        mTabController.setListener(this);

        showCredit();
        new GetCreditTask().executeLong();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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

    @Override
    public void onTabSelectionChanged(int index, boolean selected, Tab tab) {
        if (!selected) {
            return;
        }
        mShowGuest = index == TAB_GUEST;
        showCredit();
        mAdapter.reset(mRatings.get(index));
        if (mPtrlvRating.isRefreshing()) {
            fetchRatings(true);
        } else {
            mPtrlvRating.setRefreshing(Mode.PULL_FROM_START);
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchRatings(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchRatings(false);
    }

    private void showCredit() {
        TextView tvCredit = (TextView) findViewById(R.id.tv_level);
        tvCredit.setBackgroundResource(mShowGuest ? R.drawable.inset_5_bg_credit_purple :
                R.drawable.inset_5_bg_credit_red);
        Resources res = getResources();
        tvCredit.setPadding(res.getDimensionPixelSize(R.dimen.bty_credit_padding_left), 0,
                res.getDimensionPixelSize(R.dimen.bty_credit_padding_right), 0);
        tvCredit.setText(getString(
                R.string.bty_credit_level, mCredits.getCreditLevel(!mShowGuest)));

        Credit credit = mShowGuest ? mCredits.guest : mCredits.host;
        ((TextView) findViewById(R.id.tv_good)).setText(
                getString(R.string.bty_credits_good, credit.ratingP));
        ((TextView) findViewById(R.id.tv_normal)).setText(
                getString(R.string.bty_credits_normal, credit.ratingM));
        ((TextView) findViewById(R.id.tv_bad)).setText(
                getString(R.string.bty_credits_bad, credit.ratingN));
    }

    private void fetchRatings(boolean refresh) {
        int offset = refresh ? 0 : mAdapter.getCount();
        new FetchRatingsTask(offset).executeLong();
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
            iCredits.putExtra(EXTRA_SHOW_GUEST, !mShowGuest);
            startActivity(iCredits);
        }
    }

    private class RatingAdapter extends ArrayAdapter<BountyRating> {

        public RatingAdapter() {
            super(getApplicationContext(), 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_bounty_rating, parent, false);
            }
            BountyRating br = getItem(position);
            Credits credits = br.userCredit;
            UserInfo userInfo = credits.userInfo;

            ProImageView ivAvatar = (ProImageView) view.findViewById(R.id.av_avatar);
            ivAvatar.setImage(userInfo.getAvatar(), userInfo.defaultAvatar());
            ivAvatar.setOnClickListener(BountyCreditsActivity.this);
            ivAvatar.setTag(userInfo);

            NameView tvName = (NameView) view.findViewById(R.id.tv_user_name);
            tvName.setText(userInfo.getDisplayName(getContext()));
            tvName.setOnClickListener(BountyCreditsActivity.this);
            tvName.setTag(userInfo);

            TextView tvCredit = (TextView) view.findViewById(R.id.tv_credit_level);
            tvCredit.setBackgroundResource(mShowGuest ? R.drawable.inset_5_bg_credit_red :
                    R.drawable.inset_5_bg_credit_purple);
            Resources res = getResources();
            tvCredit.setPadding(res.getDimensionPixelSize(R.dimen.bty_credit_padding_left), 0,
                    res.getDimensionPixelSize(R.dimen.bty_credit_padding_right), 0);
            tvCredit.setText(getString(
                    R.string.bty_credit_level, credits.getCreditLevel(mShowGuest)));
            tvCredit.setOnClickListener(BountyCreditsActivity.this);
            tvCredit.setTag(credits);

            Utils.setText(view, R.id.tv_time, Utils.getTimeDesc(br.time));

            ((ImageView) view.findViewById(R.id.iv_rating))
                    .setImageResource(br.getRatingImage());

            TextView tvComment = (TextView) view.findViewById(R.id.tv_comment);
            if (TextUtils.isEmpty(br.comment)) {
                tvComment.setVisibility(View.GONE);
            } else {
                tvComment.setVisibility(View.VISIBLE);
                tvComment.setText(br.comment);
            }

            return view;
        }

        public void reset(ArrayList<BountyRating> ratings) {
            setNotifyOnChange(false);
            clear();
            addAll(ratings);
            notifyDataSetChanged();
        }
    }

    private class GetCreditTask extends MsTask {

        public GetCreditTask() {
            super(getApplicationContext(), MsRequest.BTY_GET_CREDIT);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("user_id=").append(mCredits.userInfo.userId)
                    .toString();
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONObject json = response.getJsonObject();
                mCredits.host = Credit.fromJson(json.optJSONObject(Credits.HOST));
                mCredits.guest = Credit.fromJson(json.optJSONObject(Credits.GUEST));
                showCredit();
                fetchRatings(true);
            } else {
                getTitleBar().hideProgress();
                response.showFailInfo(getRefContext(), R.string.bty_credits_tst_failed);
            }
        }
    }

    private class FetchRatingsTask extends MsTask {
        private int mOffset;

        public FetchRatingsTask(int offset) {
            super(getApplicationContext(), MsRequest.BTY_LIST_RATING);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("user_id=").append(mCredits.userInfo.userId)
                    .append("&is_host=").append(mShowGuest ? 0 : 1)
                    .append("&offset=").append(mOffset)
                    .toString();
        }

        @Override
        protected void onPreExecute() {
            if (mLastTask != null) {
                mLastTask.cancel(false);
            }
            mLastTask = this;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mPtrlvRating.onRefreshComplete();
            mLastTask = null;

            if (response.isSuccessful()) {
                ArrayList<BountyRating> ratings = mRatings.get(mShowGuest ? 1 : 0);
                if (mOffset == 0) {
                    ratings.clear();
                }
                ratings.addAll(JsonUtil.getArray(
                        response.getJsonArray(), BountyRating.TRANSFORMER));
                mAdapter.reset(ratings);
            } else {
                response.showFailInfo(getRefContext(), R.string.bty_credits_tst_ratings_failed);
            }
        }
    }
}
