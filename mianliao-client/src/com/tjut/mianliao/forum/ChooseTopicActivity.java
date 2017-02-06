package com.tjut.mianliao.forum;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.SearchView;
import com.tjut.mianliao.component.SearchView.OnSearchTextListener;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class ChooseTopicActivity extends BaseActivity implements OnItemClickListener, OnClickListener,
        OnRefreshListener2<ListView> {

    public static final String TOPIC_INFO = "topic_info";
    public static final String FORUM_ID = "channel_id";
    public static final String SCHOOL_ID = "school_id";
    public static final String FORUM_TYPE = "forum_type";

    private PullToRefreshListView mPtrSearchTopic, mPtrSuggestTopic;
    private LinearLayout mLlSuggest;
    private SearchView mScTopic;
    private String mSearchStr;
    private ArrayList<TopicInfo> mSearchTopicInfo = new ArrayList<TopicInfo>();
    private ArrayList<TopicInfo> mHotInfo = new ArrayList<TopicInfo>();
    private ArrayList<TopicInfo> mMoreInfo = new ArrayList<TopicInfo>();
    private ArrayList<TopicInfo> mSuggestInfo = new ArrayList<TopicInfo>();
    private TopicAdapter mSearchTopicAdapter, mSuggestTopicAdapter;
    private int mSchoolId, mForumId, mForumType;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_choose_topic;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScTopic = (SearchView) findViewById(R.id.sc_potic);
        mPtrSearchTopic = (PullToRefreshListView) findViewById(R.id.ptrlv_topic_search);
        mPtrSuggestTopic = (PullToRefreshListView) findViewById(R.id.ptrlv_topic_suggest);
        mLlSuggest = (LinearLayout) findViewById(R.id.ll_show_suggest);

        getTitleBar().setTitle("选择话题");
        mScTopic.setHint("搜索感兴趣的话题");
        mScTopic.setOnSearchTextListener(mSearchListener);

        mScTopic.setMaxLength(138);
        mSchoolId = getIntent().getIntExtra(SCHOOL_ID, 0);
        mForumId = getIntent().getIntExtra(FORUM_ID, 0);
        mForumType = getIntent().getIntExtra(FORUM_TYPE, 0);

        mSearchTopicAdapter = new TopicAdapter(this);
        mSuggestTopicAdapter = new TopicAdapter(this);
        mPtrSearchTopic.setAdapter(mSearchTopicAdapter);
        mPtrSuggestTopic.setAdapter(mSuggestTopicAdapter);
        mPtrSearchTopic.setVisibility(View.GONE);
        mPtrSearchTopic.setOnItemClickListener(this);
        mPtrSuggestTopic.setOnItemClickListener(this);
        mPtrSuggestTopic.setMode(Mode.BOTH);
        mPtrSuggestTopic.setOnRefreshListener(this);
        fetchTopics(true);
    }

    OnSearchTextListener mSearchListener = new OnSearchTextListener() {

        @Override
        public void onSearchTextChanged(CharSequence text) {
            mSearchStr = text.toString().trim();
            if (mSearchStr == null || mSearchStr.equals("")) {
                mLlSuggest.setVisibility(View.VISIBLE);
                mPtrSearchTopic.setVisibility(View.GONE);
            } else {
                mLlSuggest.setVisibility(View.GONE);
                mPtrSearchTopic.setVisibility(View.VISIBLE);
                new searchTopicTask(mSearchStr).executeLong();
            }
        }

    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TopicInfo mTpInfo = (TopicInfo) parent.getItemAtPosition(position);

        if (mTpInfo != null ) {
            Intent data = new Intent();
            data.putExtra(TOPIC_INFO, mTpInfo);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private class searchTopicTask extends AdvAsyncTask<Void, Void, MsResponse> {
        private String mKey;

        public searchTopicTask(String key) {
            mKey = key;
        }

        @Override
        protected MsResponse doInBackground(Void... params) {

            String prm = new StringBuilder("name=").append(Utils.urlEncode(mKey).toString()).append("&forum_id=")
                    .append(mForumId == 0 ? "" : mForumId).append("&forum_type=")
                    .append(mForumType == 0 ? "" : mForumType).append("&other_school_id=")
                    .append(mSchoolId == 0 ? "" : mSchoolId).toString();
            return HttpUtil.msRequest(getApplicationContext(), MsRequest.CF_TOPIC_SEARCH, prm);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrSuggestTopic.onRefreshComplete();
            if (response.isSuccessful()) {
                try {
                    JSONArray ja = response.getJsonArray();
                    mSearchTopicInfo.clear();
                    boolean isNew = true;
                    TopicInfo mTpInfo = new TopicInfo();
                    mTpInfo.name = mKey;
                    mSearchTopicInfo.add(0, mTpInfo);
                    if (ja != null) {
                        for (int i = 0; i < ja.length(); i++) {
                            TopicInfo mTp = TopicInfo.fromJson(ja.getJSONObject(i));
                            mSearchTopicInfo.add(mTp);
                        }
                    }

                    ArrayList<TopicInfo> mTopicInfos = new ArrayList<TopicInfo>();
                    mTopicInfos.addAll(mSearchTopicInfo);
                    if (mTopicInfos.size() > 1 && mTopicInfos.get(1).name.equals(mKey)) {
                        mSearchTopicInfo.remove(0);
                        isNew = false;
                    }
                    mSearchTopicAdapter.setkeyWord(mKey);
                    mSearchTopicAdapter.setData(mSearchTopicInfo, isNew);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private class SuggestTopicTask extends MsTask {
        int mOffset;

        public SuggestTopicTask(int offest) {
            super(ChooseTopicActivity.this, MsRequest.CF_TOPIC_SUGGESTED);
            mOffset = offest;
        }

        @Override
        protected String buildParams() {

            return new StringBuilder("forum_id=").append(mForumId == 0 ? "" : mForumId).append("&forum_type=")
                    .append(mForumType == 0 ? "" : mForumType).append("&other_school_id=")
                    .append(mSchoolId == 0 ? "" : mSchoolId).append("&offset=").append(mOffset).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrSuggestTopic.onRefreshComplete();
            if (response.isSuccessful()) {
                JSONObject jo = response.getJsonObject();
                try {
                    if (jo != null) {
                        JSONArray jahot = response.getJsonObject().optJSONArray("hot");
                        mSuggestInfo.clear();
                        if (mOffset <= 0) {
                            mHotInfo.clear();
                        }
                        TopicInfo mHotTitle = new TopicInfo();
                        mHotTitle.name = "热门话题";
                        mSuggestInfo.add(mHotTitle);
                        if (jahot != null) {
                            for (int i = 0; i < jahot.length(); i++) {
                                TopicInfo mTp = TopicInfo.fromJson(jahot.getJSONObject(i));
                                mHotInfo.add(mTp);
                            }
                        }
                        mSuggestInfo.addAll(mHotInfo);
                        TopicInfo mMoreTitle = new TopicInfo();
                        mMoreTitle.name = "更多话题";
                        mSuggestInfo.add(mMoreTitle);
                        JSONArray jamore = response.getJsonObject().optJSONArray("more");
                        if (mOffset <= 0) {
                            mMoreInfo.clear();
                        }
                        if (jamore != null) {
                            for (int i = 0; i < jamore.length(); i++) {
                                TopicInfo mTp = TopicInfo.fromJson(jamore.getJSONObject(i));
                                mMoreInfo.add(mTp);
                            }
                        }
                        mSuggestInfo.addAll(mMoreInfo);
                        mSuggestTopicAdapter.setData(mSuggestInfo, false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_topic:
                TopicInfo mTpInfo = (TopicInfo) v.getTag();
                if (mTpInfo != null) {
                    Intent data = new Intent();
                    data.putExtra(TOPIC_INFO, mTpInfo);
                    setResult(RESULT_OK, data);
                    finish();
                }
                break;

            default:
                break;
        }
    }

    private void fetchTopics(boolean refresh) {
        int size = mMoreInfo.size() - 1;
        int offset = refresh ? 0 : size;
        new SuggestTopicTask(offset).executeLong();
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchTopics(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchTopics(false);
    }

}
