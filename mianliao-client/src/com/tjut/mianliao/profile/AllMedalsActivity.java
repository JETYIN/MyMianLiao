package com.tjut.mianliao.profile;

import java.util.ArrayList;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.Medal;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class AllMedalsActivity extends BaseActivity implements
        OnClickListener, OnRefreshListener2<ListView> {

    private PullToRefreshListView mPtrLvMedals;

    private MedalAdapter mAdapter;
    private FetchMedalTask mLastTask;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_all_medals;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.mdl_all_medals, null);
        ((TextView) findViewById(R.id.tv_apply)).setText(
                Html.fromHtml(getString(R.string.mdl_apply_hint)));

        mAdapter = new MedalAdapter(this);

        mPtrLvMedals = (PullToRefreshListView) findViewById(R.id.ptrlv_all_medals);
        int padding = getResources().getDimensionPixelSize(R.dimen.card_margin_horizontal);
        mPtrLvMedals.getRefreshableView().setPadding(padding, 0, padding, 0);
        mPtrLvMedals.setAdapter(mAdapter);
        mPtrLvMedals.setOnRefreshListener(this);
        mPtrLvMedals.setMode(Mode.BOTH);

        getTitleBar().showProgress();
        fetchMedals(true);
    }

    @Override
    public void onClick(View v) {
//        if (v.getId() == R.id.tv_apply) {
//            startActivity(new Intent(this, FeedbackActivity.class));
//        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchMedals(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchMedals(false);
    }

    private void fetchMedals(boolean refresh) {
        if (mLastTask == null) {
            if (Utils.isNetworkAvailable(this)) {
                int offset = refresh ? 0 : mAdapter.getCount();
                new FetchMedalTask(offset).executeLong();
            } else {
                toast(R.string.no_network);
            }
        }
    }

    private class FetchMedalTask extends MsTask {
        private int mOffset;

        public FetchMedalTask(int offset) {
            super(getApplicationContext(), MsRequest.MEDAL_FETCH_ALL);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).toString();
        }

        @Override
        protected void onPreExecute() {
            mLastTask = this;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mLastTask = null;
            getTitleBar().hideProgress();
            mPtrLvMedals.onRefreshComplete();
            if (response.isSuccessful()) {
                ArrayList<Medal> medals = JsonUtil.getArray(
                        response.getJsonArray(), Medal.TRANSFORMER);
                if (mOffset == 0) {
                    mAdapter.reset(medals);
                } else {
                    mAdapter.addAll(medals);
                }
            } else {
                response.showFailInfo(getRefContext(), R.string.mdl_all_medals_tst_failed);
            }
        }
    }
}
