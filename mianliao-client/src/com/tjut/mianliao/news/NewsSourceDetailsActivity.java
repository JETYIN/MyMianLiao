package com.tjut.mianliao.news;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.BaseTask.TaskExecutionListener;
import com.tjut.mianliao.BaseTask.TaskType;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.CardItem;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.News;
import com.tjut.mianliao.data.NewsSource;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class NewsSourceDetailsActivity extends BaseActivity implements
        TaskExecutionListener, OnClickListener,
        OnItemClickListener, OnRefreshListener<ListView> {

    private static final int NEWS_SHOWN_LIMIT = 2;

    private ProImageView mIvAvatar;
    private TextView mTvName;
    private TextView mTvDesc;
    private TextView mTvFollowed;
    private View mLlAction;
    private TextView mTvAction;
    private View mPbAction;
    private CardItem mCiAbout;
    private View mLlQrCard;
    private View mTvViewMore;

    private NewsManager mNewsManager;
    private NewsSource mSource;
    private PullToRefreshListView mNewsListView;
    private NewsAdapter mNewsAdapter;

    private boolean mRefreshNews;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_news_source_details;
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

        getTitleBar().showTitleText(R.string.news_source_details, null);
        mNewsManager = NewsManager.getInstance(this);
        mNewsManager.registerTaskListener(this);

        mNewsListView = (PullToRefreshListView) findViewById(R.id.ptrlv_news_source);
        mNewsListView.setOnItemClickListener(this);
        mNewsListView.setOnRefreshListener(this);

        ListView listView = mNewsListView.getRefreshableView();
        mTvViewMore = mInflater.inflate(
                R.layout.news_source_details_footer, listView, false);
        listView.addFooterView(mTvViewMore);
        mTvViewMore.setOnClickListener(this);

        View header = mInflater.inflate(
                R.layout.news_source_details_header, listView, false);
        listView.addHeaderView(header);
        mIvAvatar = (ProImageView) header.findViewById(R.id.av_avatar);
        mTvName = (TextView) header.findViewById(R.id.tv_name);
        setBasic();
        mTvDesc = (TextView) header.findViewById(R.id.tv_desc);
        mTvFollowed = (TextView) header.findViewById(R.id.tv_followed);
        mLlAction = header.findViewById(R.id.ll_action);
        mLlAction.setOnClickListener(this);
        mTvAction = (TextView) header.findViewById(R.id.tv_action);
        mPbAction = header.findViewById(R.id.pb_action);

        mNewsAdapter = new NewsAdapter(this);
        mNewsAdapter.setCountLimit(NEWS_SHOWN_LIMIT);
        mNewsListView.setAdapter(mNewsAdapter);

        getTitleBar().showProgress();
        refresh();
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
    public void onPreExecute(int type) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPostExecute(int type, MsResponse mr) {
        switch (type) {
            case TaskType.NEWS_SOURCE_PROFILE:
                if (MsResponse.isSuccessful(mr)) {
                    setDetails();
                    mRefreshNews = true;
                    mNewsManager.startSourceNewsTask(mSource, 0);
                } else {
                    getTitleBar().hideProgress();
                    mNewsListView.onRefreshComplete();
                }
                break;

            case TaskType.NEWS_SOURCE_FOLLOW:
                if (MsResponse.isSuccessful(mr)
                        && mr.value != null && mSource != mr.value) {
                    NewsSource source = (NewsSource) mr.value;
                    mSource.followed = source.followed;
                    if (source.followed) {
                        mSource.followerCount++;
                    } else {
                        mSource.followerCount--;
                    }

                }
                setAction();
                break;

            case TaskType.NEWS_SOURCE_NEWS:
                if (mRefreshNews) {
                    mRefreshNews = false;
                    getTitleBar().hideProgress();
                    if (MsResponse.isSuccessful(mr) && mr.value != null) {
                        List<News> newsList = (List<News>) mr.value;
                        mNewsAdapter.reset(newsList);
                        mTvViewMore.setVisibility(View.VISIBLE);
                    }
                    mNewsListView.onRefreshComplete();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mLlAction && !mSource.following) {
            mSource.following = true;
            setAction();
            mNewsManager.startSourceFollowTask(mSource);
        } else if (v == mCiAbout) {
            Intent intent = new Intent(this, NewsSourceAboutActivity.class);
            intent.putExtra(NewsSource.INTENT_EXTRA_NAME, mSource);
            startActivity(intent);
        } else if (v == mLlQrCard) {
            Intent intent = new Intent(this, NewsSourceQrCardActivity.class);
            intent.putExtra(NewsSource.INTENT_EXTRA_NAME, mSource);
            startActivity(intent);
        } else if (v == mTvViewMore) {
            Intent intent = new Intent(this, NewsSourceNewsActivity.class);
            intent.putExtra(NewsSource.INTENT_EXTRA_NAME, mSource);
            startActivity(intent);
        }
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
    public void onRefresh(PullToRefreshBase<ListView> refreshView) {
        refresh();
    }

    private void refresh() {
        mNewsManager.startSourceProfileTask(mSource);
    }

    private void setDetails() {
        setBasic();

        findViewById(R.id.ll_about).setVisibility(View.VISIBLE);
        mCiAbout = (CardItem) findViewById(R.id.ci_about);
        mCiAbout.setOnClickListener(this);
        mLlQrCard = findViewById(R.id.ll_qrcard);
        mLlQrCard.setOnClickListener(this);

        mTvDesc.setVisibility(View.VISIBLE);
        mTvDesc.setText(R.string.news_source_authed);
        mCiAbout.setContent(mSource.description);

        mTvFollowed.setVisibility(View.VISIBLE);
        mLlAction.setVisibility(mSource.isSchoolSource() ? View.VISIBLE : View.GONE);
        setAction();
    }

    private void setBasic() {
        mIvAvatar.setImage(mSource.avatar, R.drawable.ic_news_source_avatar);
        mTvName.setText(mSource.name);
    }

    private void setAction() {
        if (mSource.following) {
            mPbAction.setVisibility(View.VISIBLE);
            mTvAction.setVisibility(View.GONE);
        } else {
            mPbAction.setVisibility(View.GONE);
            mTvAction.setVisibility(View.VISIBLE);
            if (mSource.followed) {
                mLlAction.setBackgroundResource(R.drawable.selector_btn_blue);
                mTvAction.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mTvAction.setText(R.string.news_source_unfollow);
            } else {
                mLlAction.setBackgroundResource(R.drawable.selector_btn_red);
                mTvAction.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_news_source_follow, 0, 0, 0);
                mTvAction.setText(R.string.news_source_follow);
            }
            mTvFollowed.setText(Utils.getColoredText(
                    getString(R.string.news_source_followed_count, mSource.followerCount),
                    String.valueOf(mSource.followerCount),
                    getResources().getColor(R.color.news_marked)));
        }
    }
}
