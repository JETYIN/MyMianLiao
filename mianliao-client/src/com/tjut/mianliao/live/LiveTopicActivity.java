package com.tjut.mianliao.live;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.view.annotation.event.OnItemClick;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.PopupView;
import com.tjut.mianliao.component.TitleBar;
import com.tjut.mianliao.forum.TopicAdapter;
import com.tjut.mianliao.forum.TopicInfo;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by j_hao on 2016/7/7.
 */

public class LiveTopicActivity extends BaseActivity implements View.OnClickListener, TextView.OnEditorActionListener
        , PullToRefreshBase.OnRefreshListener2<ListView>, TextWatcher,
        View.OnFocusChangeListener,AdapterView.OnItemClickListener {

    private PullToRefreshListView ptrTopic,ptrtopicSearch;
    private EditText mEditText;
    private View topicView;

    private String searchText;
    private ArrayList<TopicInfo> topicInfoList = new ArrayList<TopicInfo>();
    private ArrayList<TopicInfo> mSearchTopicInfo = new ArrayList<TopicInfo>();
    private ArrayList<TopicInfo> mHotInfo = new ArrayList<TopicInfo>();
    private ArrayList<TopicInfo> mMoreInfo = new ArrayList<TopicInfo>();

    private int mSchoolId, mForumId, mForumType;

    private TopicAdapter mSearchTopicAdapter, mSuggestTopicAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TitleBar titleBar = getTitleBar();
        mEditText = (EditText) titleBar.findViewById(R.id.et_topic_content);
        initView();
        titleBar.showRightText(R.string.search_cancel, this);
        titleBar.showLeftButton(R.drawable.botton_bg_arrow, this);
        mEditText.setHintTextColor(0XFFA9A9A9);
        mEditText.addTextChangedListener(this);
        mEditText.setOnFocusChangeListener(this);
        mEditText.setOnEditorActionListener(this);
        fetchTopics(true);
    }

    private void initView() {
        ptrTopic = (PullToRefreshListView) findViewById(R.id.ptrlv_topic_result);
        ptrtopicSearch= (PullToRefreshListView) findViewById(R.id.ptrlv_topic_search);
        ptrTopic.setMode(PullToRefreshBase.Mode.BOTH);
        ptrTopic.setOnRefreshListener(this);
        ptrtopicSearch.setMode(PullToRefreshBase.Mode.BOTH);
        ptrtopicSearch.setOnRefreshListener(this);

        mSearchTopicAdapter = new TopicAdapter(this);
        mSuggestTopicAdapter = new TopicAdapter(this);
        ptrtopicSearch.setAdapter(mSearchTopicAdapter);
        ptrTopic.setAdapter(mSuggestTopicAdapter);
        ptrTopic.setOnItemClickListener(this);
        ptrtopicSearch.setOnItemClickListener(this);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_live_search;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_right:
                mEditText.setText("");
                break;
        }
    }

    private String getEditString() {
        return mEditText.getText().toString();
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
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchTopics(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchTopics(false);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        searchText = getEditString();
        if (searchText != null && !searchText.equals("")) {
            ptrTopic.setVisibility(View.GONE);
            /**输入不为空**/
        }

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private void fetchTopics(boolean refresh) {
        int size = mMoreInfo.size() - 1;
        int offset = refresh ? 0 : size;
        new SuggestTopicTask(offset).executeLong();
    }



    private class SuggestTopicTask extends MsTask {
        int mOffset;

        public SuggestTopicTask(int offest) {
            super(LiveTopicActivity.this, MsRequest.CF_TOPIC_SUGGESTED);
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
            ptrTopic.onRefreshComplete();
            if (response.isSuccessful()) {
                JSONObject jo = response.getJsonObject();
                try {
                    if (jo != null) {
                        JSONArray jahot = response.getJsonObject().optJSONArray("hot");
                        topicInfoList.clear();
                        if (mOffset <= 0) {
                            mHotInfo.clear();
                        }
                        TopicInfo mHotTitle = new TopicInfo();
                        mHotTitle.name = "热门话题";
                        topicInfoList.add(mHotTitle);
                        if (jahot != null) {
                            for (int i = 0; i < jahot.length(); i++) {
                                TopicInfo mTp = TopicInfo.fromJson(jahot.getJSONObject(i));
                                mHotInfo.add(mTp);
                            }
                        }
                        topicInfoList.addAll(mHotInfo);
                        TopicInfo mMoreTitle = new TopicInfo();
                        mMoreTitle.name = "更多话题";
                        topicInfoList.add(mMoreTitle);
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
                        topicInfoList.addAll(mMoreInfo);
                        mSuggestTopicAdapter.setData(topicInfoList, false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
