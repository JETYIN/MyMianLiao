package com.tjut.mianliao.bounty;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.tab.Tab;
import com.tjut.mianliao.component.tab.TabController;
import com.tjut.mianliao.component.tab.TabController.TabListener;
import com.tjut.mianliao.component.tab.TextTab;
import com.tjut.mianliao.data.bounty.BountyTask;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class BountyListActivity extends BaseActivity implements
        OnRefreshListener2<ListView>, OnItemClickListener, TabListener {

    public static final String EXTRA_DEFAULT_TAB = "extra_default_tab";

    public static final int TAB_PUBLISHED = 0;
    public static final int TAB_ACCEPTED = 1;
    public static final int TAB_FAVORITED = 2;

    private PullToRefreshListView mPtrlvBounty;
    private BountyAdapter mAdapter;
    private TabController mTabController;

    private ArrayList<MsRequest> mRequests;
    private ArrayList<ArrayList<BountyTask>> mBounties;

    private ListBountyTask mLastTask;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_bounty_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.bty_my_task, null);

        mRequests = new ArrayList<MsRequest>();
        mRequests.add(MsRequest.BTY_LIST_MY_TASK);
        mRequests.add(MsRequest.BTY_LIST_MY_SIGNED_TASK);
        mRequests.add(MsRequest.BTY_LIST_MY_FAV_TASK);

        mBounties = new ArrayList<ArrayList<BountyTask>>();
        mBounties.add(new ArrayList<BountyTask>());
        mBounties.add(new ArrayList<BountyTask>());
        mBounties.add(new ArrayList<BountyTask>());

        mAdapter = new BountyAdapter(this);
        mPtrlvBounty = (PullToRefreshListView) findViewById(R.id.ptrlv_bounty);
        mPtrlvBounty.setOnRefreshListener(this);
        mPtrlvBounty.setMode(Mode.BOTH);
        mPtrlvBounty.setAdapter(mAdapter);
        mPtrlvBounty.setOnItemClickListener(this);

        mTabController = new TabController();
        mTabController.add(new TextTab((TextView) findViewById(R.id.tv_published)));
        mTabController.add(new TextTab((TextView) findViewById(R.id.tv_accepted)));
        mTabController.add(new TextTab((TextView) findViewById(R.id.tv_favorited)));
        int tab = getIntent().getIntExtra(EXTRA_DEFAULT_TAB, 0);
        if (tab < 0 || tab > 2) {
            tab = 0;
        }
        mTabController.select(tab);
        mTabController.setListener(this);

        getTitleBar().showProgress();
        mAdapter.setData(mBounties.get(tab));
        fetchBounties(true);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchBounties(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchBounties(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        viewTask((BountyTask) parent.getItemAtPosition(position));
    }

    @Override
    public void onTabSelectionChanged(int index, boolean selected, Tab tab) {
        if (!selected) {
            return;
        }
        mAdapter.setData(mBounties.get(index));
        mAdapter.notifyDataSetChanged();
        if (mPtrlvBounty.isRefreshing()) {
            fetchBounties(true);
        } else {
            mPtrlvBounty.setRefreshing(Mode.PULL_FROM_START);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == getIdentity() && data != null) {
            BountyTask task = data.getParcelableExtra(BountyTask.INTENT_EXTRA_NAME);
            if (resultCode == RESULT_DELETED) {
                removeTask(task);
            } else if (resultCode == RESULT_UPDATED) {
                if (mTabController.getCurrent() == TAB_FAVORITED) {
                    removeTask(task);
                } else {
                    updateTask(task);
                }
            }
            setResult(resultCode, data);
        }
    }

    private void fetchBounties(boolean refresh) {
        MsRequest request = mRequests.get(mTabController.getCurrent());
        int offset = refresh ? 0 : mAdapter.getCount();
        new ListBountyTask(request, offset).executeLong();
    }

    private void viewTask(BountyTask task) {
        if (task != null) {
            Intent iDetails = new Intent(this, BountyDetailsActivity.class);
            iDetails.putExtra(BountyTask.INTENT_EXTRA_NAME, task);
            startActivityForResult(iDetails, getIdentity());
        }
    }

    private void removeTask(BountyTask task) {
        if (mAdapter.getData().remove(task)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void updateTask(BountyTask task) {
        int index = mAdapter.getData().indexOf(task);
        if (index != -1) {
            mAdapter.getData().set(index, task);
            mAdapter.notifyDataSetChanged();
        }
    }

    private class ListBountyTask extends MsTask {
        private int mOffset;

        public ListBountyTask(MsRequest request, int offset) {
            super(getApplicationContext(), request);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).toString();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mLastTask != null) {
                mLastTask.cancel(false);
            }
            mLastTask = this;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            getTitleBar().hideProgress();
            mPtrlvBounty.onRefreshComplete();
            mLastTask = null;

            if (response.isSuccessful()) {
                ArrayList<BountyTask> bounties = mAdapter.getData();
                if (mOffset == 0) {
                    bounties.clear();
                }
                bounties.addAll(JsonUtil.getArray(
                        response.getJsonArray(), BountyTask.TRANSFORMER));
                mAdapter.notifyDataSetChanged();
            } else {
                response.showFailInfo(getRefContext(), R.string.bty_get_task_list_failed);
            }
        }
    }
}
