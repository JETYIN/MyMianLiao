package com.tjut.mianliao.news;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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
import com.tjut.mianliao.component.SearchView;
import com.tjut.mianliao.component.SearchView.OnSearchTextListener;
import com.tjut.mianliao.data.NewsSource;
import com.tjut.mianliao.util.MsResponse;

public class NewsSourcesSearchActivity extends BaseActivity implements
        OnClickListener, OnItemClickListener, OnRefreshListener2<ListView>,
        OnTouchListener, OnSearchTextListener, TaskExecutionListener {

    private NewsManager mNewsManager;

    private SearchView mSourceSearchView;
    private View mSourceSearchBtn;
    private PullToRefreshListView mSourcesListView;
    private NewsSourceAdapter mSourceAdapter;

    private boolean mRefresh;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_news_sources_search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.news_sources_title, null);

        mNewsManager = NewsManager.getInstance(this);
        mNewsManager.registerTaskListener(this);

        mSourceSearchView = (SearchView) findViewById(R.id.sv_news_sources);
        mSourceSearchView.setHint(R.string.news_sources_search_hint);
        mSourceSearchView.setOnSearchTextListener(this);
        mSourceSearchBtn = findViewById(R.id.iv_news_sources_search);
        mSourceSearchBtn.setOnClickListener(this);

        mSourcesListView = (PullToRefreshListView) findViewById(R.id.ptrlv_news_sources);
        mSourcesListView.setOnItemClickListener(this);
        mSourcesListView.setOnRefreshListener(this);
        mSourcesListView.getRefreshableView().setOnTouchListener(this);

        mSourceAdapter = new NewsSourceAdapter(this);
        mSourceAdapter.setActionEnabled(true);
        mSourcesListView.setAdapter(mSourceAdapter);

        fetchSources(true);
    }

    @Override
    protected void onDestroy() {
        mSourceAdapter.destroy();
        mNewsManager.unregisterTaskListener(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v == mSourceSearchBtn) {
            mSourceSearchView.hideInput();
            fetchSources(true);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NewsSource source = (NewsSource) parent.getItemAtPosition(position);
        if (source != null) {
            Intent intent = new Intent(this, NewsSourceDetailsActivity.class);
            intent.putExtra(NewsSource.INTENT_EXTRA_NAME, source);
            startActivity(intent);
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchSources(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchSources(false);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            mSourceSearchView.hideInput();
        }
        return false;
    }

    @Override
    public void onSearchTextChanged(CharSequence text) {
    }

    @Override
    public void onPreExecute(int type) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPostExecute(int type, MsResponse mr) {
        switch (type) {
            case TaskType.NEWS_SOURCES_SEARCH:
                getTitleBar().hideProgress();
                mSourceSearchBtn.setEnabled(true);
                if (MsResponse.isSuccessful(mr) && mr.value != null) {
                    List<NewsSource> sourceList = (List<NewsSource>) mr.value;
                    if (mRefresh) {
                        mSourceAdapter.reset(sourceList);
                    } else {
                        mSourceAdapter.append(sourceList);
                    }
                    mSourcesListView.onRefreshComplete();
                    mSourcesListView.setMode(sourceList.size() < mSettings.getPageCount()
                            ? Mode.PULL_FROM_START : Mode.BOTH);
                } else {
                    mSourcesListView.onRefreshComplete();
                }
                break;

            case TaskType.NEWS_SOURCE_FOLLOW:
                if (MsResponse.isSuccessful(mr) && mr.value != null) {
                    mSourceAdapter.update((NewsSource) mr.value);
                }
                break;

            default:
                break;
        }
    }

    private void fetchSources(boolean refresh) {
        getTitleBar().showProgress();
        mSourceSearchBtn.setEnabled(false);
        mRefresh = refresh;
        String keywords = mSourceSearchView.getSearchText();
        mSourceAdapter.setKeywords(keywords);
        int offset = refresh ? 0 : mSourceAdapter.getCount();
        mNewsManager.startSourcesSearchTask(keywords, offset);
    }
}
