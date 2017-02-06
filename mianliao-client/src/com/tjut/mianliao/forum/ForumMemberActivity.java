package com.tjut.mianliao.forum;

import java.util.ArrayList;

import org.json.JSONArray;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.SearchView;
import com.tjut.mianliao.component.SearchView.OnSearchTextListener;
import com.tjut.mianliao.contact.UserInfoAdapter;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class ForumMemberActivity extends BaseActivity implements
        View.OnClickListener, DialogInterface.OnClickListener,
        OnItemClickListener, OnTouchListener, OnSearchTextListener,
        OnRefreshListener2<ListView>, Runnable {
    private static final long DELAY_MILLS = 500;

    private SearchView mSearchView;
    private PullToRefreshListView mListView;
    private View mRequestView;

    private LightDialog mMenuDialog;
    private UserInfoAdapter mAdapter;
    private Forum mForum;
    private UserInfo mSelectedUser;
    private boolean mIsAdmin;
    private Handler mHandler;
    private String mSearchKey;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_search_generic;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mForum = getIntent().getParcelableExtra(Forum.INTENT_EXTRA_NAME);
        mIsAdmin = mForum.isAdmin(this);
        mHandler = new Handler();

        getTitleBar().showTitleText(R.string.forum_member_title, null);
        getTitleBar().showRightText(R.string.invite, this);
        findViewById(R.id.fl_search_hint).setVisibility(View.GONE);

        mSearchView = (SearchView) findViewById(R.id.sv_search);
        mSearchView.setHint(R.string.forum_member_input_hint);
        mSearchView.setOnSearchTextListener(this);

        mListView = (PullToRefreshListView) findViewById(R.id.lv_search_result);
        mListView.setMode(Mode.BOTH);
        mRequestView = mInflater.inflate(R.layout.new_requests, mListView, false);
        ((TextView) mRequestView.findViewById(
                R.id.tv_requests)).setText(R.string.forum_new_request_title);
        mRequestView.setOnClickListener(this);
        setRequestView();

        FrameLayout wrapper = new FrameLayout(this);
        wrapper.addView(mRequestView);
        mListView.getRefreshableView().addHeaderView(wrapper);
        mListView.getRefreshableView().setOnTouchListener(this);
        mListView.setOnItemClickListener(this);
        mListView.setOnRefreshListener(this);

        mAdapter = new UserInfoAdapter(this, R.layout.list_item_forum_member) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                UserInfo user = getItem(position);
                Utils.setText(view, R.id.tv_short_desc, user.shortDesc);
                view.findViewById(R.id.iv_menu).setVisibility(
                        shouldShowMenu(user) ? View.VISIBLE : View.GONE);
                return view;
            }
        };
        mListView.setAdapter(mAdapter);

        getTitleBar().showProgress();
        fetchMembers(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSearchView.hideInput();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_UPDATED && requestCode == getIdentity()) {
            mForum = data.getParcelableExtra(Forum.INTENT_EXTRA_NAME);
            setRequestView();
            setResult(RESULT_UPDATED, new Intent().putExtra(
                    Forum.INTENT_EXTRA_NAME, mForum));

            ArrayList<UserInfo> acceptedInfos = data.getParcelableArrayListExtra(
                    NewForumMemberActivity.EXTRA_REQUESTS_ACCEPTED);
            if (acceptedInfos != null && !acceptedInfos.isEmpty()) {
                mAdapter.setNotifyOnChange(false);
                for (UserInfo info : acceptedInfos) {
                    mAdapter.add(info);
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mRequestView) {
            Intent i = new Intent(this, NewForumMemberActivity.class);
            i.putExtra(Forum.INTENT_EXTRA_NAME, mForum);
            startActivityForResult(i, getIdentity());
        } else {
            switch (v.getId()) {
                case R.id.tv_right:
                    ArrayList<UserInfo> exceptInfos = new ArrayList<UserInfo>();
                    for (int i = 0; i < mAdapter.getCount(); i++) {
                        exceptInfos.add(mAdapter.getItem(i));
                    }
                    Intent iInvite = new Intent(this, ForumInviteMemberActivity.class);
                    iInvite.putExtra(Forum.INTENT_EXTRA_NAME, mForum);
                    iInvite.putParcelableArrayListExtra(
                            ForumInviteMemberActivity.EXTRA_EXCEPT_INFOS, exceptInfos);
                    startActivity(iInvite);
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mMenuDialog) {
            switch (which) {
                case 0:
                    new RemoveMemberTask(mSelectedUser).executeLong();
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserInfo user = (UserInfo) parent.getItemAtPosition(position);
        if (shouldShowMenu(user)) {
            mSelectedUser = user;
            showMenuDialog();
        } else {
            Intent i = new Intent(this, NewProfileActivity.class);
            i.putExtra(UserInfo.INTENT_EXTRA_INFO, user);
            startActivity(i);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            mSearchView.hideInput();
        }
        return false;
    }

    @Override
    public void onSearchTextChanged(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            setRequestView();
        } else {
            mRequestView.setVisibility(View.GONE);
        }
        getTitleBar().showProgress();
        mSearchKey = text.toString();
        mHandler.removeCallbacks(this);
        mHandler.postDelayed(this, DELAY_MILLS);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchMembers(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchMembers(false);
    }

    @Override
    public void run() {
        fetchMembers(true);
    }

    private void setRequestView() {
        if (!mIsAdmin || mForum.memberRequests == 0) {
            mRequestView.setVisibility(View.GONE);
        } else {
            mRequestView.setVisibility(View.VISIBLE);
            ((TextView) mRequestView.findViewById(
                    R.id.tv_count)).setText(String.valueOf(mForum.memberRequests));
        }
    }

    private boolean shouldShowMenu(UserInfo user) {
        return mIsAdmin && user != null && user.userId != mForum.adminUid;
    }

    private void showMenuDialog() {
        if (mMenuDialog == null) {
            mMenuDialog = new LightDialog(this);
            mMenuDialog.setTitle(R.string.please_choose);
            mMenuDialog.setItems(R.array.forum_member_menu, this);
        }
        mMenuDialog.show();
    }

    private void fetchMembers(boolean refresh) {
        int offset = refresh ? 0 : mAdapter.getCount();
        new FetchMembersTask(mSearchKey, offset).executeLong();
    }

    private class FetchMembersTask extends MsTask {
        private String mKey;
        private int mOffset;

        FetchMembersTask(String key, int offset) {
            super(getApplicationContext(), MsRequest.LIST_MEMBER);
            mKey = key;
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("forum_id=").append(mForum.id)
                    .append("&nick=").append(Utils.urlEncode(mKey))
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
                    mAdapter.setKeyword(mKey);
                    mAdapter.notifyDataSetChanged();
                }
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.forum_member_tst_members_failed, response.code));
            }
        }
    }

    private class RemoveMemberTask extends MsTask {
        private UserInfo mUser;

        public RemoveMemberTask(UserInfo user) {
            super(getApplicationContext(), MsRequest.REMOVE_MEMBER);
            mUser = user;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("forum_id=").append(mForum.id)
                    .append("&member_uid=").append(mUser.userId).toString();
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (MsResponse.isSuccessful(response)) {
                mAdapter.remove(mUser);
                mForum.memberCount--;
                setResult(RESULT_UPDATED, new Intent().putExtra(
                        Forum.INTENT_EXTRA_NAME, mForum));
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.forum_member_tst_remove_failed, response.code));
            }
        }
    }
}
