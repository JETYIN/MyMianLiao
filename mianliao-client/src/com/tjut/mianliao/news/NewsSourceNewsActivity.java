package com.tjut.mianliao.news;

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
import com.tjut.mianliao.BaseTask.TaskExecutionListener;
import com.tjut.mianliao.BaseTask.TaskType;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.News;
import com.tjut.mianliao.data.NewsSource;
import com.tjut.mianliao.util.MsResponse;

public class NewsSourceNewsActivity extends BaseActivity implements
        OnItemClickListener, OnRefreshListener2<ListView>, TaskExecutionListener {

    private NewsManager mNewsManager;

    private PullToRefreshListView mNewsListView;
    private NewsAdapter mNewsAdapter;

    private NewsSource mSource;
    private boolean mRefresh;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_news_source_news;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSource = getIntent().getParcelableExtra(NewsSource.INTENT_EXTRA_NAME);
        if (mSource == null) {
            toast(R.string.news_source_tst_not_found);
            finish();
            return;
        }
        getTitleBar().showTitleText(mSource.name, null);

        mNewsManager = NewsManager.getInstance(this);
        mNewsManager.registerTaskListener(this);

        mNewsListView = (PullToRefreshListView) findViewById(R.id.ptrlv_news);
        mNewsListView.setOnItemClickListener(this);
        mNewsListView.setOnRefreshListener(this);
        mNewsListView.setMode(Mode.BOTH);

        mNewsAdapter = new NewsAdapter(this);
        mNewsListView.setAdapter(mNewsAdapter);

        getTitleBar().showProgress();
        fetchNews(true);
    }

    @Override
    protected void onDestroy() {
        if (mNewsAdapter != null) {
            mNewsAdapter.destroy();
        }
        if (mNewsManager != null) {
            mNewsManager.unregisterTaskListener(this);
        }
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        News news = (News) parent.getItemAtPosition(position);
        if (news != null) {
            Intent intent = new Intent(this, NewsDetailsActivity.class);
            intent.putExtra(NewsDetailsActivity.EXTRA_VIEW_SOURCE, false);
            intent.putExtra(News.INTENT_EXTRA_NAME, new News(news));
            startActivity(intent);
        }
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

    @SuppressWarnings("unchecked")
    @Override
    public void onPostExecute(int type, MsResponse mr) {
        if (TaskType.NEWS_SOURCE_NEWS == type) {
            getTitleBar().hideProgress();
            mNewsListView.onRefreshComplete();
            if (MsResponse.isSuccessful(mr) && mr.value != null) {
                List<News> newsList = (List<News>) mr.value;
                if (mRefresh) {
                    mNewsAdapter.reset(newsList);
                } else {
                    mNewsAdapter.append(newsList);
                }
            }
        }
    }

    private void fetchNews(boolean refresh) {
        mRefresh = refresh;
        int offset = refresh ? 0 : mNewsAdapter.getCount();
        mNewsManager.startSourceNewsTask(mSource, offset);
    }
}
