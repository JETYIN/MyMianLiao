package com.tjut.mianliao.news;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.BaseTask.TaskExecutionListener;
import com.tjut.mianliao.BaseTask.TaskType;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.CommonBanner;
import com.tjut.mianliao.component.TitleBar;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.News;
import com.tjut.mianliao.mycollege.MyFavoriteActivity;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.TrackingUtil;

public class NewsActivity extends BaseActivity implements OnClickListener, OnItemClickListener,
        OnRefreshListener2<ListView>, TaskExecutionListener {

    private static final String SP_NEWS_LIST = "news_list";

    private PullToRefreshListView mPtrListView;

    private NewsManager mNewsManager;
    private NewsAdapter mAdapter;

    private boolean mRefresh;
    private int mType = NewsManager.TYPE_SCHOOL;

//    private View mHeaderView;
    private TitleBar mTitleBar;
    private CommonBanner mVsSwitcher;

    @Override
    protected int getLayoutResID() {
        return R.layout.main_tab_news;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNewsManager = NewsManager.getInstance(this);
        mNewsManager.registerTaskListener(this);

        mAdapter = new NewsAdapter(this);

        mTitleBar = getTitleBar();
        mTitleBar.setTitle(R.string.news_homepage_title);
        mTitleBar.showRightButton(R.drawable.bottom_collect, this);

        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptrlv_news);
        mPtrListView.setMode(Mode.BOTH);
        mPtrListView.setOnItemClickListener(this);
        mPtrListView.setOnRefreshListener(this);
        mPtrListView.setAdapter(mAdapter);
        loadNews();
        mPtrListView.setRefreshing(Mode.PULL_FROM_START);
    }

    private void loadNews() {
        JSONArray json = null;
        try {
            json = new JSONArray(DataHelper.getSpForData(
                    this).getString(SP_NEWS_LIST, "{}"));
            ArrayList<News> news = JsonUtil.getArray(json , News.TRANSFORMER);
            mAdapter.reset(news);
        } catch (JSONException e) {
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        mNewsManager.unregisterTaskListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mNewsManager.registerTaskListener(this);
    }

    @Override
    public void onDestroy() {
        mAdapter.destroy();
        mNewsManager.unregisterTaskListener(this);
        mPtrListView.setOnItemClickListener(null);
        mPtrListView.setOnRefreshListener((OnRefreshListener<ListView>) null);
        mPtrListView.setAdapter(null);
        mPtrListView = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_right:
                startActivity(new Intent(this, MyFavoriteActivity.class));
                break;

            case R.id.iv_image:
                if (v.getTag() != null && v.getTag() instanceof News) {
                    viewNews((News) v.getTag());
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        News news = (News) parent.getItemAtPosition(position);
        if (news != null) {
            Intent iDetails = new Intent(this, NewsDetailsActivity.class);
            iDetails.putExtra(News.INTENT_EXTRA_NAME, new News(news));
            startActivity(iDetails);
            TrackingUtil.trackNewsTabClickNews(this, news);
        }
    }

    @Override
    public void onPreExecute(int type) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPostExecute(int type, MsResponse mr) {
        switch (type) {
            case TaskType.NEWS_FETCH_LATEST:
                if (mPtrListView == null) {
                    return;
                }
                if (mPtrListView != null) {
                    mPtrListView.onRefreshComplete();
                }
                if (MsResponse.isSuccessful(mr) && mr.value != null) {
                    List<News> newsList = (List<News>) mr.value;
                    JSONArray json = mr.json.optJSONArray(MsResponse.PARAM_RESPONSE);
                    if (mRefresh) {
                        DataHelper.getSpForData(this).edit()
                            .putString(SP_NEWS_LIST, json.toString())
                            .commit();
                        mAdapter.reset(newsList);
                    } else {
                        mAdapter.addAll(newsList);
                        mAdapter.notifyDataSetChanged();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void viewNews(News news) {
        if (news != null) {
            Intent iDetails = new Intent(this, NewsDetailsActivity.class);
            iDetails.putExtra(News.INTENT_EXTRA_NAME, new News(news));
            startActivity(iDetails);
            TrackingUtil.trackNewsTabClickBanner(this, news);
        }
    }

    private void fetchNews(boolean refresh) {
        mRefresh = refresh;
        int size = mAdapter.getCount();
        long time = refresh || size == 0 ? 0 : mAdapter.getItem(size - 1).createTime;
        mNewsManager.startNewsFetchLatestTask(time, mType);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchNews(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchNews(false);
    }
}
