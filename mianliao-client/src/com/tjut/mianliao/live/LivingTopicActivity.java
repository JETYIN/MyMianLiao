package com.tjut.mianliao.live;

import java.util.ArrayList;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.TitleBar;
import com.tjut.mianliao.forum.TopicInfo;

import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;


public class LivingTopicActivity extends BaseActivity implements OnItemClickListener, OnClickListener,
        OnRefreshListener2<ListView>, TextWatcher {

    public static final String TOPIC_INFO = "topic_info";
    private int LIMIT = 20;
    private EditText mEditText;
    private PullToRefreshListView mPtrSearchTopic, mPtrSuggestTopic;
    private LinearLayout mLlSuggest;
    private View topicView;
    private String mSearchStr;
    private ArrayList<Topics> topicsList = new ArrayList<>();
    private LivingTopicAdapter mSearchTopicAdapter, mSuggestTopicAdapter;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_live_search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TitleBar titleBar = getTitleBar();
        mEditText = (EditText) titleBar.findViewById(R.id.et_topic_content);
        titleBar.showRightText(R.string.search_cancel, this);
        titleBar.setRightTextColor(Color.parseColor("#B2ffffff"));
        titleBar.setBackgroundColor(Color.parseColor("#B21a090e"));

        mPtrSearchTopic = (PullToRefreshListView) findViewById(R.id.ptrlv_topic_search);
        mPtrSuggestTopic = (PullToRefreshListView) findViewById(R.id.ptrlv_topic_result);
        mLlSuggest = (LinearLayout) findViewById(R.id.ll_show_suggest);

        mSuggestTopicAdapter = new LivingTopicAdapter(this);
        mPtrSuggestTopic.setAdapter(mSuggestTopicAdapter);
        mSearchTopicAdapter = new LivingTopicAdapter(this);
        mPtrSearchTopic.setAdapter(mSearchTopicAdapter);
        mPtrSearchTopic.setVisibility(View.GONE);
        mPtrSearchTopic.setOnItemClickListener(this);
        mPtrSuggestTopic.setOnItemClickListener(this);
        mPtrSuggestTopic.setMode(Mode.BOTH);
        mPtrSuggestTopic.setOnRefreshListener(this);
        mEditText.addTextChangedListener(this);
        fetchTopics(true);
    }

    @Override
    protected TitleBar getTitleBar() {
        TitleBar titleBar = super.getTitleBar();
        topicView = mInflater.inflate(R.layout.activity_topic_edittext, null);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        int marginRight = getResources().getDimensionPixelOffset(R.dimen.search_margin_right);
        lp.setMargins(marginRight, 0, marginRight, 0);
        topicView.setLayoutParams(lp);
        titleBar.addView(topicView);
        return titleBar;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mSearchStr = getEditString();
        if (mSearchStr != null && !mSearchStr.equals("")) {
            mPtrSearchTopic.setVisibility(View.VISIBLE);
            mLlSuggest.setVisibility(View.GONE);
            /**输入不为空进行任务**/
            new searchTopicTask(mSearchStr).executeLong();
        } else {
            mLlSuggest.setVisibility(View.VISIBLE);
            mPtrSearchTopic.setVisibility(View.GONE);

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private String getEditString() {
        return mEditText.getText().toString();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Topics mTpInfo = (Topics) parent.getItemAtPosition(position);

        if (mTpInfo != null) {
            Intent data = new Intent();
            data.putExtra(TOPIC_INFO, mTpInfo);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private void fetchTopics(boolean refresh) {
        int size = topicsList.size() - 1;
        int offset = refresh ? 0 : size;
        new getTopicTask(offset).executeLong();
    }


    private class getTopicTask extends MsTask {
        int mOffset;

        public getTopicTask(int offest) {
            super(LivingTopicActivity.this, MsRequest.LIST_MAIN_TOPICS);
            mOffset = offest;
        }

        @Override
        protected String buildParams() {

            return new StringBuilder("offset=").append(mOffset).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrSuggestTopic.onRefreshComplete();
            if (response.isSuccessful()) {
                topicsList = JsonUtil.getArray(response.getJsonArray(), Topics.TRANSFORMER);
                Log.e("getTopic", response.getJsonArray().toString());
                if (topicsList != null && topicsList.size() > 0) {
                    mSuggestTopicAdapter.setData(topicsList, false);
                }
            } else {
                toast("数据获取异常");
            }

        }
    }

    private class searchTopicTask extends AdvAsyncTask<Void, Void, MsResponse> {
        String searchStr;

        public searchTopicTask(String str) {
            searchStr = str;

        }

        @Override
        protected MsResponse doInBackground(Void... params) {

            String prm = new StringBuilder
                    ("key=").append(Utils.urlEncode(searchStr).toString()).
                    toString();
            return HttpUtil.msRequest(getApplicationContext(), MsRequest.LIST_TOPICS, prm);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrSuggestTopic.onRefreshComplete();
            if (response.isSuccessful()) {
                try {
                    JSONArray ja = response.getJsonArray();
                    topicsList.clear();
                    boolean isNew = true;
                    Topics mTpInfo = new Topics();
                    mTpInfo.name = searchStr;
                    topicsList.add(0, mTpInfo);
                    if (ja != null) {
                        for (int i = 0; i < ja.length(); i++) {
                            Topics mTp = Topics.fromJson(ja.getJSONObject(i));
                            topicsList.add(mTp);
                        }
                    }

                    ArrayList<Topics> mTopicInfos = new ArrayList<Topics>();
                    mTopicInfos.addAll(topicsList);
                    if (mTopicInfos.size() > 1 && mTopicInfos.get(1).name.equals(searchStr)) {
                        topicsList.remove(0);
                        isNew = false;
                    }
                    mSearchTopicAdapter.setkeyWord(searchStr);
                    mSearchTopicAdapter.setData(topicsList, isNew);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
             else {
                toast("数据获取异常");
            }

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                mEditText.setText("");
                break;
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

    @Override
    protected void onPause() {
        super.onPause();
        hideSoftKey();
    }

    private void hideSoftKey() {
        InputMethodManager imm = (InputMethodManager)
                this.getSystemService(this.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchTopics(true);
    }

}
