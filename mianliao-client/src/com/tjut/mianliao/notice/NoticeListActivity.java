package com.tjut.mianliao.notice;

import org.json.JSONArray;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.NameView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.notice.Notice;
import com.tjut.mianliao.data.notice.NoticeSummary;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public abstract class NoticeListActivity extends BaseActivity implements
        OnItemClickListener, OnRefreshListener2<ListView>, OnClickListener {

    private static final int DESC_MAX_LINES = 2;

    protected PullToRefreshListView mPtrListView;

    protected NoticeManager mNoticeManager;
    protected ItemAdapter mAdapter;

    protected NoticeSummary mSummary;
    protected int mKeyColor;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_notice_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNoticeManager = NoticeManager.getInstance(this);
        mSummary = mNoticeManager.getSummary(
                getIntent().getIntExtra(NoticeSummary.SUBZONE, 0));
        if (mSummary == null) {
            finish();
            return;
        }

        getTitleBar().showTitleText(mSummary.nameRes, null);
        mKeyColor = getResources().getColor(R.color.txt_keyword);

        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptrlv_notice);
        int padding = getResources().getDimensionPixelSize(R.dimen.card_margin_horizontal);
        mPtrListView.getRefreshableView().setPadding(padding, 0, padding, 0);
        mPtrListView.setOnItemClickListener(this);
        mPtrListView.setOnRefreshListener(this);
        mPtrListView.setMode(Mode.BOTH);

        mAdapter = new ItemAdapter();
        mPtrListView.setAdapter(mAdapter);

        getTitleBar().showProgress();
        fetchNotices(true);
    }

    protected Item getItem(Notice notice) {
        Item item = new Item();
        item.notice = notice;
        return item;
    }

    protected void onItemClick(Notice notice) { }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Item item = (Item) parent.getItemAtPosition(position);
        if (item != null && item.notice != null) {
            onItemClick(item.notice);
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchNotices(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchNotices(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.av_avatar:
            case R.id.tv_user_name:
                viewUser((UserInfo) v.getTag());
                break;

            default:
                break;
        }
    }

    private void fetchNotices(boolean refresh) {
        int offset = refresh ? 0 : mAdapter.getCount();
        new NoticeTask(offset).executeLong();
    }

    private void viewUser(UserInfo userInfo) {
        if (userInfo != null) {
            Intent iUser = new Intent(this, NewProfileActivity.class);
            iUser.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
            startActivity(iUser);
        }
    }

    protected static class Item {
        public UserInfo userInfo;
        public String avatar;
        public int defaultAvatar;
        public String title;
        public long time;
        public CharSequence category;
        public CharSequence desc;
        public Notice notice;
    }

    protected class ItemAdapter extends ArrayAdapter<Item> {

        public ItemAdapter() {
            super(getApplicationContext(), 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_notice, parent, false);
            }
            Item data = getItem(position);
            UserInfo userInfo = data.userInfo;

            ProImageView ivAvatar = (ProImageView) view.findViewById(R.id.av_avatar);
            NameView tvName = (NameView) view.findViewById(R.id.tv_user_name);
            if (userInfo == null) {
                ivAvatar.setImage(data.avatar, data.defaultAvatar);
                tvName.setText(data.title);
            } else {
                ivAvatar.setImage(userInfo.getAvatar(), userInfo.defaultAvatar());
                ivAvatar.setOnClickListener(NoticeListActivity.this);
                ivAvatar.setTag(userInfo);
                tvName.setText(userInfo.getDisplayName(getContext()));
                tvName.setMedal(userInfo.primaryBadgeImage);
                tvName.setOnClickListener(NoticeListActivity.this);
                tvName.setTag(userInfo);
            }

            String time = getString(R.string.news_published_on,
                    Utils.getTimeDesc(data.time));
            ((TextView) view.findViewById(R.id.tv_extra_info)).setText(time);

            TextView tvCategory = (TextView) view.findViewById(R.id.tv_category);
            TextView tvDesc = (TextView) view.findViewById(R.id.tv_desc);
            if (TextUtils.isEmpty(data.category)) {
                tvCategory.setVisibility(View.GONE);
                tvDesc.setMaxLines(Integer.MAX_VALUE);
            } else {
                tvCategory.setVisibility(View.VISIBLE);
                tvCategory.setText(data.category);
                tvDesc.setMaxLines(DESC_MAX_LINES);
            }
            tvDesc.setText(Utils.getRefFriendText(data.desc, getContext()));

            return view;
        }
    }

    private class NoticeTask extends MsTask {
        private int mOffset;

        public NoticeTask(int offset) {
            super(getApplicationContext(), MsRequest.NOTICE_LIST);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder().append("offset=").append(mOffset)
                    .append("&subzone=").append(mSummary.subzone)
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mPtrListView.onRefreshComplete();
            if (MsResponse.isSuccessful(response)) {
                mAdapter.setNotifyOnChange(false);
                if (mOffset == 0) {
                    mAdapter.clear();
                }
                JSONArray ja = response.getJsonArray();
                if (ja != null && ja.length() > 0) {
                    for (int i = 0; i < ja.length(); i++) {
                        Notice notice = Notice.fromJson(ja.optJSONObject(i));
                        if (notice != null) {
                            mAdapter.add(getItem(notice));
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
                mNoticeManager.setNoticeViewed(NoticeListActivity.this, 0);
            } else {
                toast(MsResponse.getFailureDesc(getRefContext(),
                        R.string.ntc_tst_list_failed, response.code));
            }
        }
    }
}