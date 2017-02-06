package com.tjut.mianliao.mycollege;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.BaseTask;
import com.tjut.mianliao.BaseTask.TaskExecutionListener;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.News;
import com.tjut.mianliao.news.NewsAdapter;
import com.tjut.mianliao.news.NewsDetailsActivity;
import com.tjut.mianliao.news.NewsManager;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.TrackingUtil;

public class MyFavoriteActivity extends BaseActivity implements TaskExecutionListener, OnRefreshListener2<ListView>,
        OnItemClickListener {

    private PullToRefreshListView mPtrListView;
    private boolean mRefresh;
    private NewsAdapter mAdapter;
    private NewsManager mNewsManager;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_myfavorite;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptrlv_news);
        getTitleBar().setTitle(R.string.news_category_fav);
        mNewsManager = NewsManager.getInstance(this);
        mAdapter = new NewsAdapter(this);
        mPtrListView.setMode(Mode.BOTH);
        mPtrListView.setOnRefreshListener(this);
        mPtrListView.setOnItemClickListener(this);
        mPtrListView.setAdapter(mAdapter);
        mNewsManager.registerTaskListener(this);
        fetchNews(true);
    }

    private void fetchNews(boolean refresh) {
        mRefresh = refresh;
        int size = mAdapter.getCount();
        long time = refresh || size == 0 ? 0 : mAdapter.getItem(size - 1).createTime;
        mNewsManager.startFetchFavoriteTask(0);
    }

    @Override
    protected void onDestroy() {
        mPtrListView.setOnRefreshListener((OnRefreshListener2<ListView>) null);
        mNewsManager.unregisterTaskListener(this);
        mAdapter.destroy();
        mPtrListView = null;
        super.onDestroy();
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchNews(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchNews(false);
    }

    @Override
    public void onPreExecute(int type) {

    }

    @Override
    public void onPostExecute(int type, MsResponse mr) {
        if (mPtrListView == null) {
            return;
        } else {
            mPtrListView.onRefreshComplete();
        }
        switch (type) {
            case BaseTask.TaskType.NEWS_FETCH_MY_FAV:
                if (MsResponse.isSuccessful(mr) && mr.value != null) {
                    List<News> newsList = (List<News>) mr.value;

                    if (mRefresh) {
                        mAdapter.reset(newsList);
                    } else {
                        mAdapter.append(newsList);
                    }
                    mAdapter.notifyDataSetChanged();
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

}
