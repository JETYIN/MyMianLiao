package com.tjut.mianliao.forum;

import java.util.ArrayList;

import org.json.JSONArray;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.contact.UserInfoAdapter;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class NewForumMemberActivity extends BaseActivity implements
        OnClickListener, OnItemClickListener, OnRefreshListener2<ListView> {
    public static final String EXTRA_REQUESTS_ACCEPTED = "extra_requests_accepted";

    private PullToRefreshListView mListView;

    private UserInfoAdapter mAdapter;

    private Forum mForum;
    private ArrayList<UserInfo> mAcceptedInfos;
    private Intent mResultIntent;
    private boolean mDoingRequest = false;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_search_generic;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTitleBar().showTitleText(R.string.forum_new_request_title, null);
        findViewById(R.id.sv_search).setVisibility(View.GONE);
        findViewById(R.id.fl_search_hint).setVisibility(View.GONE);

        mForum = getIntent().getParcelableExtra(Forum.INTENT_EXTRA_NAME);
        mAcceptedInfos = new ArrayList<UserInfo>();
        mResultIntent = new Intent()
                .putParcelableArrayListExtra(EXTRA_REQUESTS_ACCEPTED, mAcceptedInfos)
                .putExtra(Forum.INTENT_EXTRA_NAME, mForum);

        mListView = (PullToRefreshListView) findViewById(R.id.lv_search_result);
        mListView.setMode(Mode.BOTH);
        mListView.setOnItemClickListener(this);
        mListView.setOnRefreshListener(this);

        mAdapter = new UserInfoAdapter(this, R.layout.list_item_new_request) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                UserInfo user = getItem(position);
                View view = super.getView(position, convertView, parent);

                View btnReject = view.findViewById(R.id.btn_reject);
                btnReject.setTag(user);
                btnReject.setOnClickListener(NewForumMemberActivity.this);

                View btnAgree = view.findViewById(R.id.btn_agree);
                btnAgree.setTag(user);
                btnAgree.setOnClickListener(NewForumMemberActivity.this);

                return view;
            }
        };
        mListView.setAdapter(mAdapter);

        getTitleBar().showProgress();
        fetchRequests(true);
    }

    @Override
    public void onClick(View v) {
        if (mDoingRequest) {
            toast(R.string.nc_handling_last_request);
            return;
        }
        UserInfo user = (UserInfo) v.getTag();
        if (user != null) {
            switch (v.getId()) {
                case R.id.btn_reject:
                    new RequestTask(user, false).executeLong();
                    break;

                case R.id.btn_agree:
                    new RequestTask(user, true).executeLong();
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserInfo user = (UserInfo) parent.getItemAtPosition(position);
        if (user != null) {
            Intent i = new Intent(this, NewProfileActivity.class);
            i.putExtra(UserInfo.INTENT_EXTRA_INFO, user);
            startActivity(i);
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchRequests(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchRequests(false);
    }

    private void fetchRequests(boolean refresh) {
        int offset = refresh ? 0 : mAdapter.getCount();
        new FetchRequestsTask(offset).executeLong();
    }

    private class FetchRequestsTask extends MsTask {
        private int mOffset;

        public FetchRequestsTask(int offset) {
            super(getApplicationContext(), MsRequest.LIST_MEMBER_REQUEST);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("forum_id=").append(mForum.id)
                    .append("&offset=").append(mOffset).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mListView.onRefreshComplete();
            if (MsResponse.isSuccessful(response)) {
                JSONArray ja = response.json.optJSONArray(MsResponse.PARAM_RESPONSE);
                if (ja.length() > 0) {
                    mAdapter.setNotifyOnChange(false);
                    if (mOffset == 0) {
                        mAdapter.clear();
                    }
                    for (int i = 0; i < ja.length(); i++) {
                        UserInfo user = UserInfo.fromJson(ja.optJSONObject(i));
                        if (user != null) {
                            mAdapter.add(user);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.forum_member_tst_requests_failed, response.code));
            }
        }
    }

    private class RequestTask extends MsTask {
        private UserInfo mUser;
        private boolean mAccept;

        public RequestTask(UserInfo user, boolean accept) {
            super(getApplicationContext(), MsRequest.ACCEPT_MEMBER);
            mUser = user;
            mAccept = accept;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("forum_id=").append(mForum.id)
                    .append("&member_uid=").append(mUser.userId)
                    .append("&accept=").append(mAccept ? 1 : 0).toString();
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
            mDoingRequest = true;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mDoingRequest = false;
            if (MsResponse.isSuccessful(response)) {
                if (mAccept) {
                    mAcceptedInfos.add(mUser);
                    mForum.memberCount++;
                }
                mAdapter.remove(mUser);
                mForum.memberRequests--;
                setResult(RESULT_UPDATED, mResultIntent);
                if (mAdapter.isEmpty()) {
                    finish();
                } else {
                    mAdapter.notifyDataSetChanged();
                }
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.forum_new_request_tst_failed, response.code));
            }
        }
    }
}