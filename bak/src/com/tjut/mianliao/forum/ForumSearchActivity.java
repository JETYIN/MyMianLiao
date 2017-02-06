package com.tjut.mianliao.forum;

import java.util.ArrayList;

import org.json.JSONArray;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.SearchView;
import com.tjut.mianliao.component.SearchView.OnSearchTextListener;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class ForumSearchActivity extends BaseActivity implements
        Runnable, OnSearchTextListener, OnClickListener,
        OnItemClickListener, OnRefreshListener2<ListView> {

    private static final int REQUEST_CREATE = 100;
    private static final int REQUEST_VIEW   = 200;

    private static final long DELAY_MILLS = 500;

    private PullToRefreshListView mPtrListView;
    private TextView mTvSearchHint;
    private TextView mTvRecommendHint;
    private TextView mTvChangeRecommend;

    private ForumAdapter mAdapter;

    private Handler mHandler;
    private String mSearchKey;
    private Forum mForum;
    private Intent mResultIntent;
    private ArrayList<Forum> mForumsDeleted;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_forum_search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mForum = getIntent().getParcelableExtra(Forum.INTENT_EXTRA_NAME);

        mForumsDeleted = new ArrayList<Forum>();
        mResultIntent = new Intent().putParcelableArrayListExtra(
                Forum.INTENT_EXTRA_DELETED, mForumsDeleted);

        getTitleBar().showTitleText(R.string.cf_add_more, null);
        getTitleBar().showRightText(R.string.create, this);
        mHandler = new Handler();

        SearchView searchView = (SearchView) findViewById(R.id.sv_search);
        searchView.setHint(R.string.forum_search_input_hint);
        searchView.setOnSearchTextListener(this);

        mTvSearchHint = (TextView) findViewById(R.id.tv_search_hint);
        mTvRecommendHint = (TextView) findViewById(R.id.tv_recommend_hint);
        mTvChangeRecommend = (TextView) findViewById(R.id.tv_change_recommend);

        mPtrListView = (PullToRefreshListView) findViewById(R.id.lv_result);
        mPtrListView.getRefreshableView().addFooterView(new View(this));
        mPtrListView.setOnRefreshListener(this);
        mPtrListView.setOnItemClickListener(this);

        mAdapter = new ForumAdapter();
        mPtrListView.setAdapter(mAdapter);

        recommendForum();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CREATE:
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_UPDATED, mResultIntent.putExtras(data));
                    finish();
                }
                break;

            case REQUEST_VIEW:
                if (data == null) {
                    break;
                }
                Forum forum = data.getParcelableExtra(Forum.INTENT_EXTRA_NAME);
                switch (resultCode) {
                    case RESULT_DELETED:
                        mAdapter.remove(forum);
                        mForumsDeleted.add(forum);
                        setResult(RESULT_UPDATED, mResultIntent);
                        break;

                    case RESULT_UPDATED:
                        if (mAdapter.update(forum)) {
                            setResult(RESULT_UPDATED, mResultIntent.putExtras(data));
                        }
                        break;

                    default:
                        break;
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void run() {
        mAdapter.clear();
        if (TextUtils.isEmpty(mSearchKey)) {
            recommendForum();
        } else {
            searchForum(true);
        }
    }

    @Override
    public void onSearchTextChanged(CharSequence text) {
        getTitleBar().showProgress();
        mSearchKey = text.toString();
        mHandler.removeCallbacks(this);
        mHandler.postDelayed(this, DELAY_MILLS);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                createForum();
                break;

            case R.id.tv_change_recommend:
                recommendForum();
                break;

            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        viewForum((Forum) parent.getItemAtPosition(position));
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        searchForum(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        searchForum(false);
    }

    private void recommendForum() {
        new RecommendTask().executeLong();
    }

    private void searchForum(boolean refresh) {
        int offset = refresh ? 0 : mAdapter.getCount();
        new SearchTask(mSearchKey, offset).executeLong();
    }

    private void createForum() {
        Intent ief = new Intent(this, EditForumActivity.class);
        ief.putExtra(Forum.INTENT_EXTRA_NAME, mForum);
        startActivityForResult(ief, REQUEST_CREATE);
    }

    private void viewForum(Forum forum) {
        Intent icf = new Intent(this, CourseForumActivity.class);
        icf.putExtra(Forum.INTENT_EXTRA_NAME, forum);
        startActivityForResult(icf, REQUEST_VIEW);
    }

    private class ForumAdapter extends ArrayAdapter<Forum> {
        private int mKeyColor;
        private String mKeyword;

        public ForumAdapter() {
            super(getApplicationContext(), 0);
            mKeyColor = getResources().getColor(R.color.txt_keyword);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_forum_search, parent, false);
            }
            Forum forum = getItem(position);

            ((ProImageView) view.findViewById(R.id.iv_icon))
                    .setImage(forum.icon, R.drawable.ic_avatar_forum);

            ImageView ivBadge = (ImageView) view.findViewById(R.id.iv_badge);
            if (forum.isVip()) {
                ivBadge.setVisibility(View.VISIBLE);
                ivBadge.setImageResource(R.drawable.ic_vip);
            } else if (forum.isUserForum()) {
                ivBadge.setVisibility(View.VISIBLE);
                ivBadge.setImageResource(R.drawable.ic_forum_badge);
            } else {
                ivBadge.setVisibility(View.GONE);
            }

            Utils.setText(view, R.id.tv_name, Utils.getColoredText(
                    forum.name, mKeyword, mKeyColor));
            Utils.setText(view, R.id.tv_member, getContext().getString(
                    R.string.forum_member_count, forum.memberCount));
            Utils.setText(view, R.id.tv_thread, getContext().getString(
                    R.string.forum_thread_count, forum.threadCount));

            return view;
        }

        public void setKeyword(String keyword) {
            mKeyword = keyword;
        }

        public void update(MsResponse response, boolean refresh) {
            setNotifyOnChange(false);
            if (refresh) {
                clear();
            }
            JSONArray ja = response.getJsonArray();
            int length = ja == null ? 0 : ja.length();
            if (length > 0) {
                for (int i = 0; i < length; i++) {
                    Forum forum = Forum.fromJson(ja.optJSONObject(i));
                    if (forum != null) {
                        add(forum);
                    }
                }
            }
            notifyDataSetChanged();
        }

        public boolean update(Forum forum) {
            int index = getPosition(forum);
            if (index == -1) {
                return false;
            }
            setNotifyOnChange(false);
            remove(forum);
            insert(forum, index);
            notifyDataSetChanged();
            return true;
        }
    }

    private class RecommendTask extends MsTask {
        private RecommendTask() {
            super(getApplicationContext(), MsRequest.RECOMMENDED_FORUM);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("type=").append(mForum.type).toString();
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
            mTvSearchHint.setVisibility(View.GONE);
            mTvRecommendHint.setVisibility(View.VISIBLE);
            mTvChangeRecommend.setVisibility(View.VISIBLE);
            mTvChangeRecommend.setEnabled(false);
            mPtrListView.setMode(Mode.DISABLED);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mTvChangeRecommend.setEnabled(true);
            if (MsResponse.isSuccessful(response)) {
                mAdapter.setKeyword(null);
                mAdapter.update(response, true);
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.forum_recommended_tst_failed, response.code));
            }
        }
    }

    private class SearchTask extends MsTask {
        private String mName;
        private int mOffset;

        private SearchTask(String name, int offset) {
            super(getApplicationContext(), MsRequest.SEARCH_FORUM);
            mName = name;
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("type=").append(mForum.type)
                    .append("&name=").append(Utils.urlEncode(mName))
                    .append("&offset=").append(mOffset)
                    .toString();
        }

        @Override
        protected void onPreExecute() {
            mTvSearchHint.setVisibility(View.VISIBLE);
            mTvRecommendHint.setVisibility(View.GONE);
            mTvChangeRecommend.setVisibility(View.GONE);
            mPtrListView.setMode(Mode.BOTH);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mPtrListView.onRefreshComplete();
            if (MsResponse.isSuccessful(response)) {
                mAdapter.setKeyword(mName);
                mAdapter.update(response, mOffset == 0);
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.forum_search_tst_failed, response.code));
            }
        }
    }
}
