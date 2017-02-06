package com.tjut.mianliao.forum;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.tab.Tab;
import com.tjut.mianliao.component.tab.TabController;
import com.tjut.mianliao.component.tab.TextTab;
import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.forum.nova.ForumPostDetailActivity;
import com.tjut.mianliao.image.ImageActivity;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class ForumEventsActivity extends BaseActivity implements
        OnClickListener, OnItemClickListener, OnRefreshListener2<ListView>, TabController.TabListener {

    public static final String EXTRA_DEFAULT_TAB = "extra_default_tab";

    public static final int TAB_NEW = 0;
    public static final int TAB_OWNED = 1;
    public static final int TAB_ATTENDED = 2;

    private PullToRefreshListView mPtrListView;

    private ArrayList<CfPost> mNewPosts = new ArrayList<CfPost>();
    private ArrayList<CfPost> mOwnedPosts = new ArrayList<CfPost>();
    private ArrayList<CfPost> mAttendedPosts = new ArrayList<CfPost>();
    private ArrayList<CfPost> mPosts = new ArrayList<CfPost>();

    private MsRequest mCurrentRequest;
    private int mSuggestedCount;

    private TabController mTabController;

    private ArrayList<MsRequest> mInitedTasks = new ArrayList<MsRequest>();

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_forum_events;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.fe_list, null);

        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptrlv_events);
        mPtrListView.setOnItemClickListener(this);
        mPtrListView.setOnRefreshListener(this);
        mPtrListView.setMode(Mode.BOTH);
        mPtrListView.setAdapter(mAdapter);

        mTabController = new TabController();
        mTabController.setListener(this);
        mTabController.add(new TextTab((TextView) findViewById(R.id.tv_latest)));
        mTabController.add(new TextTab((TextView) findViewById(R.id.tv_owned)));
        mTabController.add(new TextTab((TextView) findViewById(R.id.tv_attended)));
        int tab = getIntent().getIntExtra(EXTRA_DEFAULT_TAB, 0);
        if (tab < 0 || tab > 2) {
            tab = 0;
        }
        mTabController.select(tab);

        getTitleBar().showProgress();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != getIdentity() || data == null) {
            return;
        }

        CfPost post = data.getParcelableExtra(CfPost.INTENT_EXTRA_NAME);
        if (post == null) {
            return;
        }

        int size = mPosts.size();
        switch (resultCode) {
            case RESULT_UPDATED:
            case RESULT_DELETED:
                for (int i = 0; i < size; i++) {
                    if (mPosts.get(i).postId == post.postId) {
                        mPosts.remove(i);
                        if (resultCode == RESULT_UPDATED) {
                            mPosts.add(i, post);
                        }
                        mAdapter.notifyDataSetChanged();
                        break;
                    }
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_thumb:
                if (v.getTag() != null && v.getTag() instanceof String) {
                    Intent ivi = new Intent(this, ImageActivity.class);
                    ivi.putExtra(ImageActivity.EXTRA_IMAGE_URL, (String) v.getTag());
                    startActivity(ivi);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CfPost post = (CfPost) parent.getItemAtPosition(position);
        if (post != null) {
            Intent ifpd = new Intent(this, ForumPostDetailActivity.class);
            ifpd.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA, post);
            ifpd.putExtra(ForumPostDetailActivity.EXTRL_CHANNEL_INFO, new ChannelInfo());
            startActivityForResult(ifpd, getIdentity());
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchEvents(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchEvents(false);
    }

    private void fetchEvents(boolean refresh) {
        int shift = mCurrentRequest == MsRequest.CFE_LIST_LATEST ? mSuggestedCount : 0;
        int offset = refresh ? 0 : mPosts.size() - shift;
        new FetchEventsTask(mCurrentRequest, offset).executeLong();
    }

    private void updateContent(MsRequest request, ArrayList<CfPost> target) {
        mPosts = target;
        if (!mInitedTasks.contains(request)) {
            mInitedTasks.add(request);
            mCurrentRequest = request;
            mPtrListView.setRefreshing();
            fetchEvents(true);
        } else if (mCurrentRequest == request) {
            return;
        }

        mCurrentRequest = request;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTabSelectionChanged(int index, boolean selected, Tab tab) {
        if (!selected) {
            return;
        }
        switch (index) {
            case TAB_OWNED:
                updateContent(MsRequest.CFE_LIST_OWNED, mOwnedPosts);
                break;
            case TAB_ATTENDED:
                updateContent(MsRequest.CFE_LIST_ATTENDED, mAttendedPosts);
                break;
            case TAB_NEW:
            default:
                updateContent(MsRequest.CFE_LIST_LATEST, mNewPosts);
                break;
        }
    }

    private BaseAdapter mAdapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return mPosts.size();
        }

        @Override
        public CfPost getItem(int position) {
            return mPosts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).postId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.list_item_cf_event, parent, false);
            } else {
                view = convertView;
            }
            CfPost post = getItem(position);
            Event event = post.event;

            view.findViewById(R.id.iv_prom).setVisibility(
                    event.suggested ? View.VISIBLE : View.GONE);

            TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
            tvTitle.setText(post.title);
            tvTitle.setCompoundDrawablesWithIntrinsicBounds(event.getTitleIcon(), 0, 0, 0);

            Utils.setText(view, R.id.tv_source, post.forumName);
            Utils.setText(view, R.id.tv_like, String.valueOf(post.upCount));
            Utils.setText(view, R.id.tv_reply, String.valueOf(post.replyCount));

            String imgUrl = post.getImagePreview(0);
            if (TextUtils.isEmpty(imgUrl)) {
                imgUrl = post.forumIcon;
            }
            ProImageView ivThumb = (ProImageView) view.findViewById(R.id.iv_thumb);
            ivThumb.setImage(imgUrl, R.drawable.pic_event_list_default);
            ivThumb.setTag(TextUtils.isEmpty(imgUrl) ? null : imgUrl);
            ivThumb.setOnClickListener(ForumEventsActivity.this);

            return view;
        }
    };

    private class FetchEventsTask extends MsTask {
        private int mOffset;
        private ArrayList<CfPost> mTargetList;

        public FetchEventsTask(MsRequest request, int offset) {
            super(getApplicationContext(), request);
            mOffset = offset;
            if (request == MsRequest.CFE_LIST_OWNED) {
                mTargetList = mOwnedPosts;
            } else if (request == MsRequest.CFE_LIST_ATTENDED) {
                mTargetList = mAttendedPosts;
            } else {
                mTargetList = mNewPosts;
            }
        }

        @Override
        protected String buildParams() {
            return "offset=" + mOffset;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            getTitleBar().hideProgress();
            mPtrListView.onRefreshComplete();

            if (MsResponse.isSuccessful(response)) {
                if (mOffset == 0) {
                    mTargetList.clear();
                }
                if (getRequest() == MsRequest.CFE_LIST_LATEST) {
                    JSONObject json = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);
                    if (mOffset == 0) {
                        addEvent(json.optJSONArray("suggested"), true);
                        mSuggestedCount = mTargetList.size();
                    }
                    addEvent(json.optJSONArray("list"), false);
                } else {
                    addEvent(response.json.optJSONArray(MsResponse.PARAM_RESPONSE), false);
                }
                mAdapter.notifyDataSetChanged();
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.fe_list_tst_fetch_failed, response.code));
            }
        }

        private void addEvent(JSONArray ja, boolean suggested) {
            if (ja != null && ja.length() > 0) {
                for (int i = 0; i < ja.length(); i++) {
                    CfPost post = CfPost.fromJson(ja.optJSONObject(i));
                    if (post != null && post.hasEvent()) {
                        post.event.suggested = suggested;
                        mTargetList.add(post);
                    }
                }
            }
        }
    }
}
