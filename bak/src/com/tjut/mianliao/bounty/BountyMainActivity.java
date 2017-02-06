package com.tjut.mianliao.bounty;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.BrowserActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.ViewSwitcher;
import com.tjut.mianliao.component.ViewSwitcherAdapter;
import com.tjut.mianliao.component.tab.Tab;
import com.tjut.mianliao.component.tab.TabController;
import com.tjut.mianliao.component.tab.TextTab;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.bounty.BountyTask;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.LocationHelper;
import com.tjut.mianliao.util.MenuHelper;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class BountyMainActivity extends BaseActivity implements View.OnClickListener,
        PullToRefreshBase.OnRefreshListener2<ListView>, DialogInterface.OnClickListener,
        AdapterView.OnItemClickListener, TabController.TabListener {

    private static final String TAG = "BountyMainActivity";

    private static final String URL_HELP = Utils.getServerAddress()
            + "assets/pages/bounty/rule.html";
    private static final int REQ_NEW_TASK = 111;
    private static final int REQ_VIEW_TASK = 112;
    private static final int REQ_MY_TASK = 113;

    private static final int SORT_COUNT = 2;
    private static final String[] SORTS = new String[]{"sp_bty_distance", "sp_bty_created_on"};
    private static final String SP_BTY_BANNER = "sp_bty_banner";

    private static final int SORT_CREATED_ON = 0;
    private static final int SORT_DISTANCE = 1;

    private PullToRefreshListView mPtrlvTask;
    private BountyAdapter mBountyAdapter;
    private SwitcherAdapter mVsAdapter;
    private ViewSwitcher mVsBanner;

    private ArrayList<ArrayList<BountyTask>> mBountyLists;
    private int[] mInitedSorts = new int[SORT_COUNT];
    private boolean[] mOngoing = new boolean[SORT_COUNT];

    private LightDialog mMenuDialog;
    private MenuHelper mMenuHelper;

    private LocationHelper mLocationHelper;

    private TabController mTabController;
    private CheckBox mCbOngoing;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_bounty_main;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVsAdapter = new SwitcherAdapter();
        mBountyAdapter = new BountyAdapter(this);
        mLocationHelper = LocationHelper.getInstance(this);

        mTabController = new TabController();

        loadData();

        getBanners();

        getTitleBar().showRightButton(R.drawable.btn_title_bar_more, this);
        getTitleBar().showTitleText(R.string.more_bounty, null);

        findViewById(R.id.iv_new_task).setOnClickListener(this);
        mPtrlvTask = (PullToRefreshListView) findViewById(R.id.ptrlv_task);
        mPtrlvTask.setOnRefreshListener(this);
        ListView lvTask = mPtrlvTask.getRefreshableView();
        lvTask.setAdapter(mBountyAdapter);
        lvTask.setOnItemClickListener(this);

        View headerView = LayoutInflater.from(this).inflate(R.layout.list_header_bounty_tab, lvTask, false);
        lvTask.addHeaderView(headerView);

        mTabController.clear();
        mTabController.add(makeTab(headerView, R.id.tv_sort_date));
        mTabController.add(makeTab(headerView, R.id.tv_sort_distance));
        mTabController.setListener(this);
        mCbOngoing = (CheckBox) headerView.findViewById(R.id.cb_ongoing);
        mCbOngoing.setOnClickListener(this);

        mVsBanner = (ViewSwitcher) headerView.findViewById(R.id.vs_content);
        mVsBanner.setAdapter(mVsAdapter);

        mTabController.select(0);
    }

    private void loadData() {
        SharedPreferences sp = DataHelper.getSpForData(this);
        // init banner
        try {
            JSONArray ja = new JSONArray(sp.getString(SP_BTY_BANNER, "[]"));
            mVsAdapter.setData(JsonUtil.getArray(ja, BountyTask.TRANSFORMER));
        } catch (JSONException e) {
            Utils.logD(TAG, e.getMessage());
        }

        // init list
        mBountyLists = new ArrayList<ArrayList<BountyTask>>();
        for (int i = 0; i < SORT_COUNT; i++) {
            ArrayList<BountyTask> bountyList = new ArrayList<BountyTask>();
            mBountyLists.add(bountyList);
            try {
                JSONArray ja = new JSONArray(sp.getString(SORTS[i], "[]"));
                bountyList.addAll(JsonUtil.getArray(ja, BountyTask.TRANSFORMER));
            } catch (JSONException e) {
                Utils.logD(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSwitcher(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        updateSwitcher(false);
    }

    private void updateSwitcher(boolean start) {
        if (mVsBanner != null) {
            if (start) {
                mVsBanner.start();
            } else {
                mVsBanner.stop();
            }
        }
    }

    private Tab makeTab(View parent, int textViewId) {
        TextTab tab = new TextTab((android.widget.TextView) parent.findViewById(textViewId));
        tab.setBackgroundResource(0, 0);
        return tab;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        showTask((BountyTask) parent.getItemAtPosition(position));
    }

    private void showTask(BountyTask task) {
        if (task != null) {
            Intent iDetails = new Intent(this, BountyDetailsActivity.class);
            iDetails.putExtra(BountyTask.INTENT_EXTRA_NAME, task);
            startActivityForResult(iDetails, REQ_VIEW_TASK);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPtrlvTask.setOnItemClickListener(null);
        mPtrlvTask.setOnRefreshListener((PullToRefreshBase.OnRefreshListener<ListView>) null);
        mPtrlvTask.setAdapter(null);
        mPtrlvTask = null;
        mVsBanner.setAdapter(null);
        mVsBanner = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_right:
                showMenuDialog();
                break;
            case R.id.cb_ongoing:
                if (v instanceof CheckBox) {
                    mOngoing[mTabController.getCurrent()] = ((CheckBox) v).isChecked();
                    mPtrlvTask.setRefreshing(PullToRefreshBase.Mode.PULL_FROM_START);
                }
                break;
            case R.id.iv_image:
                if (v.getTag() != null && v.getTag() instanceof BountyTask) {
                    showTask((BountyTask) v.getTag());
                }
                break;
            case R.id.iv_new_task:
                makeNewTask();
                break;
            default:
                break;
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        getBounties(mTabController.getCurrent(), 0);
        getBanners();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        getBounties(mTabController.getCurrent(), mBountyAdapter.getCount());
    }

    private void showBounties(int sort) {
        if (mInitedSorts[sort] == 0 && getBounties(sort, 0)) {
            mInitedSorts[sort] = 1;
        }
        if (mOngoing[sort] != mCbOngoing.isChecked()) {
            mOngoing[sort] = !mOngoing[sort];
            mPtrlvTask.setRefreshing(PullToRefreshBase.Mode.PULL_FROM_START);
        }

        mBountyAdapter.setData(mBountyLists.get(sort));
        mBountyAdapter.setShowDistance(sort == SORT_DISTANCE);
        mBountyAdapter.notifyDataSetChanged();
    }

    private boolean getBounties(int sort, int offset) {
        if (sort == SORT_DISTANCE && mLocationHelper.getCurrentLoc() == null) {
            toast(R.string.bty_no_location_info);
            return false;
        }
        new ListBountyTask(sort, mOngoing[sort]).setOffset(offset).executeLong();
        return true;
    }

    private void showMenuDialog() {
        if (mMenuDialog == null) {
            mMenuHelper = new MenuHelper(this, R.array.bty_main_menu);
            mMenuDialog = new LightDialog(this)
                    .setTitleLd(R.string.please_choose)
                    .setItems(mMenuHelper.getMenu(), this);
        }
        mMenuDialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (mMenuHelper.get(which).id) {
            case R.integer.mi_bty_new_task:
                makeNewTask();
                break;
            case R.integer.mi_bty_my_task:
                Intent intent = new Intent(this, BountyListActivity.class);
                startActivityForResult(intent, REQ_MY_TASK);
                break;
            case R.integer.mi_bty_help:
                Intent ih = new Intent(this, BrowserActivity.class)
                        .putExtra(BrowserActivity.URL, URL_HELP);
                ih.putExtra(BrowserActivity.TITLE, getString(R.string.bty_help));
                startActivity(ih);
                break;
            default:
                break;
        }
    }

    private void makeNewTask() {
        Intent intent = new Intent(this, BountyPostActivity.class);
        startActivityForResult(intent, REQ_NEW_TASK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_NEW_TASK:
                if (resultCode == Activity.RESULT_OK) {
                    getBounties(SORT_CREATED_ON, 0);
                }
                break;

            case REQ_VIEW_TASK:
            case REQ_MY_TASK:
                if (data != null) {
                    BountyTask task = data.getParcelableExtra(BountyTask.INTENT_EXTRA_NAME);
                    if (resultCode == BaseActivity.RESULT_DELETED) {
                        removeTask(task);
                    } else if (resultCode == BaseActivity.RESULT_UPDATED) {
                        updateTask(task);
                    }
                }
                break;

            default:
                break;
        }
    }

    private void removeTask(BountyTask task) {
        boolean changed = false;
        for (ArrayList<BountyTask> tasks : mBountyLists) {
            changed |= tasks.remove(task);
        }
        if (changed) {
            mBountyAdapter.notifyDataSetChanged();
        }

        if (mVsAdapter.getData().remove(task)) {
            mVsAdapter.notifyDataSetChanged();
        }
    }

    private void updateTask(BountyTask task) {
        boolean changed = false;
        for (ArrayList<BountyTask> tasks : mBountyLists) {
            int index = tasks.indexOf(task);
            if (index != -1) {
                tasks.set(index, task);
                changed = true;
            }
        }
        if (changed) {
            mBountyAdapter.notifyDataSetChanged();
        }

        int index = mVsAdapter.getData().indexOf(task);
        if (index != -1) {
            mVsAdapter.getData().set(index, task);
            mVsAdapter.notifyDataSetChanged();
        }
    }

    private void getBanners() {
        new GetBannerBountyTask().executeLong();
    }

    @Override
    public void onTabSelectionChanged(int index, boolean selected, Tab tab) {
        if (selected) {
            showBounties(index);
        }
    }

    private class SwitcherAdapter extends ViewSwitcherAdapter {

        private ArrayList<BountyTask> mItems;

        private void setData(ArrayList<BountyTask> bannerItems) {
            mItems = bannerItems;
        }

        private ArrayList<BountyTask> getData() {
            return mItems;
        }

        @Override
        public int getCount() {
            return mItems == null ? 0 : mItems.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = LayoutInflater.from(BountyMainActivity.this).inflate(
                        R.layout.switcher_item_forum, parent, false);
            } else {
                view = convertView;
            }
            BountyTask item = mItems.get(position);

            ProImageView ivImage = (ProImageView) view.findViewById(R.id.iv_image);
            ivImage.setImage(item.getBannerImage(), R.drawable.bg_img_loading);
            ivImage.setOnClickListener(BountyMainActivity.this);
            ivImage.setTag(item);
            Utils.setText(view, R.id.tv_info, item.title);
            return view;
        }
    }

    private class ListBountyTask extends MsTask {

        private int mSort;
        private int mShowAll;
        private int mOffset;

        public ListBountyTask(int sort, boolean ongoing) {
            super(BountyMainActivity.this, sort == SORT_DISTANCE
                    ? MsRequest.BTY_LIST_TASK_BY_LOCATION : MsRequest.BTY_LIST_TASK);
            mSort = sort;
            mShowAll = ongoing ? 0 : 1;
        }

        ListBountyTask setOffset(int offset) {
            mOffset = offset;
            return this;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mCbOngoing.setEnabled(false);
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder();
            sb.append("offset=").append(mOffset);
            sb.append("&show_all=").append(mShowAll);
            if (mSort == SORT_DISTANCE) {
                sb.append("&location=").append(mLocationHelper.getCurrentLocString());
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (mPtrlvTask != null) {
                mPtrlvTask.onRefreshComplete();
            }
            mCbOngoing.setEnabled(true);
            if (response.isSuccessful()) {
                if (mOffset == 0) {
                    mBountyLists.get(mSort).clear();
                    DataHelper.getSpForData(BountyMainActivity.this)
                            .edit()
                            .putString(SORTS[mSort], response.response)
                            .apply();
                }

                mBountyLists.get(mSort).addAll(JsonUtil.getArray(response.getJsonArray(),
                        BountyTask.TRANSFORMER));

                if (mTabController.getCurrent() == mSort) {
                    mBountyAdapter.notifyDataSetChanged();
                }
            } else {
                response.showFailInfo(BountyMainActivity.this, R.string.bty_get_task_list_failed);
            }
        }
    }

    private class GetBannerBountyTask extends MsTask {

        public GetBannerBountyTask() {
            super(BountyMainActivity.this, MsRequest.BTY_LIST_SUGGESTED_TASK);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mVsAdapter.setData(JsonUtil.getArray(response.getJsonArray(),
                        BountyTask.TRANSFORMER));
                DataHelper.getSpForData(BountyMainActivity.this)
                        .edit()
                        .putString(SP_BTY_BANNER, response.response)
                        .apply();
                mVsAdapter.notifyDataSetChanged();
            }
        }
    }
}
