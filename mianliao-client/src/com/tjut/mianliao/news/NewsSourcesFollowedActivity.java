package com.tjut.mianliao.news;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.tjut.mianliao.data.NewsSource;
import com.tjut.mianliao.util.MsResponse;

public class NewsSourcesFollowedActivity extends BaseActivity implements
        OnClickListener, OnItemClickListener,
        OnRefreshListener2<ListView>, TaskExecutionListener {

    private NewsManager mNewsManager;

    private PullToRefreshListView mSourcesListView;
    private NewsSourceAdapter mSourceAdapter;

    private boolean mRefresh;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_news_sources_followed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.news_sources_followed, null);
        getTitleBar().showRightButton(R.drawable.btn_title_bar_add_ns, this);

        mNewsManager = NewsManager.getInstance(this);
        mNewsManager.registerTaskListener(this);

        mSourcesListView = (PullToRefreshListView) findViewById(R.id.ptrlv_news_sources);
        mSourcesListView.setOnItemClickListener(this);
        mSourcesListView.setOnRefreshListener(this);

        mSourceAdapter = new NewsSourceAdapter(this);
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
        if (v.getId() == R.id.btn_right) {
            Intent intent = new Intent(this, NewsSourcesSearchActivity.class);
            startActivity(intent);
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
    public void onPreExecute(int type) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPostExecute(int type, MsResponse mr) {
        switch (type) {
            case TaskType.NEWS_SOURCES_FOLLOWED:
                getTitleBar().hideProgress();
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
                    NewsSource source = (NewsSource) mr.value;
                    if (!source.followed) {
                        mSourceAdapter.remove(source);
                    } else if (mSourceAdapter.getPosition(source) == -1) {
                        mSourceAdapter.add(source);
                    }
                }
                break;

            default:
                break;
        }
    }

    private void fetchSources(boolean refresh) {
        getTitleBar().showProgress();
        mRefresh = refresh;
        int offset = refresh ? 0 : mSourceAdapter.getCount();
        mNewsManager.startSourcesFollowedTask(offset);
    }
}
