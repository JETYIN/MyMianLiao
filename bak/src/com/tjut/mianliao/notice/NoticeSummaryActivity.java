package com.tjut.mianliao.notice;

import org.json.JSONArray;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.contact.ContactUpdateCenter;
import com.tjut.mianliao.contact.ContactUpdateCenter.ContactObserver;
import com.tjut.mianliao.contact.ContactUpdateCenter.UpdateType;
import com.tjut.mianliao.data.notice.NoticeSummary;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class NoticeSummaryActivity extends BaseActivity implements
        OnItemClickListener, OnRefreshListener<ListView>, ContactObserver {

    private static final int REQUEST_VIEW = 101;

    private NoticeManager mNoticeManager;

    private PullToRefreshListView mPtrListView;

    private SummaryAdapter mAdapter;
    private NoticeSummary mSummary;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_notice_summary;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.ntc_title, null);

        ContactUpdateCenter.registerObserver(this);
        mNoticeManager = NoticeManager.getInstance(this);
        mAdapter = new SummaryAdapter();

        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptrlv_notice);
        mPtrListView.getRefreshableView().addFooterView(new View(this));
        mPtrListView.setOnItemClickListener(this);
        mPtrListView.setOnRefreshListener(this);
        mPtrListView.setAdapter(mAdapter);

        getTitleBar().showProgress();
        fetchSummaries();
    }

    @Override
    protected void onDestroy() {
        ContactUpdateCenter.removeObserver(this);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VIEW && resultCode == RESULT_VIEWED
                && mSummary != null && data != null) {
            mNoticeManager.onNoticeViewed(mSummary, data);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NoticeSummary summary = (NoticeSummary) parent.getItemAtPosition(position);
        if (summary != null) {
            mSummary = summary;
            Intent intent = NoticeSummary.getIntent(this, summary.subzone);
            startActivityForResult(intent, REQUEST_VIEW);
        }
    }

    @Override
    public void onRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchSummaries();
    }

    @Override
    public void onContactsUpdated(UpdateType type, Object data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void fetchSummaries() {
        new SummaryTask().executeLong();
    }

    private class SummaryAdapter extends ArrayAdapter<NoticeSummary> {

        public SummaryAdapter() {
            super(getApplicationContext(), 0, mNoticeManager.getSummaries());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_section, parent, false);
            }
            NoticeSummary nt = getItem(position);

            ((ImageView) view.findViewById(R.id.iv_icon)).setImageResource(nt.iconRes);
            ((TextView) view.findViewById(R.id.tv_name)).setText(nt.nameRes);

            TextView tvCount = (TextView) view.findViewById(R.id.tv_count);
            if (nt.count > 0) {
                tvCount.setVisibility(View.VISIBLE);
                tvCount.setText(String.valueOf(nt.count));
            } else {
                tvCount.setVisibility(View.GONE);
            }

            return view;
        }
    }

    private class SummaryTask extends MsTask {

        public SummaryTask() {
            super(getApplicationContext(), MsRequest.NOTICE_SUMMARY);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mPtrListView.onRefreshComplete();
            if (MsResponse.isSuccessful(response)) {
                JSONArray ja = response.json.optJSONArray(MsResponse.PARAM_RESPONSE);
                if (ja != null) {
                    mNoticeManager.clearTouchFlag();
                    mNoticeManager.updateCount(ja);
                    mAdapter.notifyDataSetChanged();
                }
            } else {
                toast(MsResponse.getFailureDesc(getRefContext(),
                        R.string.ntc_tst_summary_failed, response.code));
            }
        }
    }
}