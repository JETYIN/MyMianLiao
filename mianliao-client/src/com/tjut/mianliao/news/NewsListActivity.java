package com.tjut.mianliao.news;

import java.util.ArrayList;

import org.json.JSONArray;

import android.content.Intent;
import android.os.AsyncTask;
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
import com.tjut.mianliao.NotificationHelper;
import com.tjut.mianliao.NotificationHelper.NotificationType;
import com.tjut.mianliao.R;
import com.tjut.mianliao.chat.UnreadMessageHelper;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.News;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class NewsListActivity extends BaseActivity implements
        OnItemClickListener, OnRefreshListener2<ListView> {

    public static final String EXTRA_LIST_TYPE = "extra_list_type";
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_ID = "extra_id";

    private PullToRefreshListView mPtrListView;

    private NewsAdapter mAdapter;

    private long broadcasterId = 0;
    private ArrayList<News> mNews = new ArrayList<News>();
 
    @Override
    protected int getLayoutResID() {
        return R.layout.main_tab_news;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationHelper.getInstance(this).clearNotification(NotificationType.NOTICE);
        broadcasterId = getIntent().getLongExtra(EXTRA_ID, 0);
        String name = getIntent().getStringExtra(EXTRA_NAME);
        getTitleBar().showTitleText(name, null);
        mAdapter = new NewsAdapter(this);

        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptrlv_news);
        mPtrListView.setOnItemClickListener(this);
        mPtrListView.setOnRefreshListener(this);
        mPtrListView.setMode(Mode.BOTH);

        mPtrListView.setAdapter(mAdapter);
        UnreadMessageHelper.getInstance(this).setMessageTarget(
                UnreadMessageHelper.getPublicNumTarget(broadcasterId));

        mNews = DataHelper.loadPublicNumInfos(this);
        if (mNews != null && mNews.size() != 0 ) {
            mAdapter.reset(mNews);
            fetchNews(true);
        } else {
            mPtrListView.setRefreshing(Mode.PULL_FROM_START);
        } 
    }

    @Override
    protected void onDestroy() {
        mAdapter.destroy();
        super.onDestroy();
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        News news = (News) parent.getItemAtPosition(position);
        if (news != null) {
            Intent iDetails = new Intent(this, NewsDetailsActivity.class);
            iDetails.putExtra(News.INTENT_EXTRA_NAME, new News(news));
            startActivity(iDetails);
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

    private class getBroadcasterTask extends MsTask {

        private int mOffset;
        
        public getBroadcasterTask(int offsett) {
            super(NewsListActivity.this, MsRequest.NEWS_BROADCAST);
            mOffset = offsett;
        }
        
        @Override
        protected String buildParams() {
            return new StringBuilder("broadcaster_id=").append(broadcasterId)
            .append("&offset=").append(mOffset)
            .toString();
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            if (response.isSuccessful()) {
                if (mOffset <= 0) {
                    mNews.clear();
                }
                JSONArray ja = response.getJsonArray();
                if (ja != null) {
                    try {
                        News mNew;
                        for (int i = 0; i < ja.length(); i++){
                            mNew = News.fromJson(ja.getJSONObject(i));
                            mNews.add(mNew);
                        }
                        if (mOffset <= 0) {
                            mAdapter.reset(mNews);
                            new SaveDataTask(mNews).execute();
                        } else {
                            mAdapter.addAll(mNews);
                        }
                        mAdapter.notifyDataSetChanged();  
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
    } 

    private class SaveDataTask extends AsyncTask<Void, Void, Boolean>{

        ArrayList<News> mNews;
        
        public SaveDataTask(ArrayList<News> news) {
            mNews = news;
        }
        
        @Override
        protected Boolean doInBackground(Void... params) {
            DataHelper.deletePublicNumInfo(NewsListActivity.this);
            DataHelper.insertPublicNumInfo(NewsListActivity.this, mNews);
            return false;
        }
        
    }
    
    private void fetchNews(boolean refresh) {
        int size = mAdapter.getCount();
        int offset = refresh ? 0 : size;
        new getBroadcasterTask(offset).executeLong();
    }
}
