package com.tjut.mianliao.news.wicket;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.BrowserActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class WicketActivity extends BaseActivity implements View.OnClickListener {

    private ArrayList<WicketRecord> mRecords;

    private WicketHelper mWicketHelper;
    private EditText mEtTicket;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_wicket;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.tic_title, null);
        mWicketHelper = WicketHelper.getInstance(this);

        mRecords = mWicketHelper.getRecords();

        mEtTicket = (EditText) findViewById(R.id.et_ticket);

        ListView lvHistory = (ListView) findViewById(R.id.lv_wicket_record);
        lvHistory.setAdapter(mRecordAdapter);
        lvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WicketRecord record = (WicketRecord) mRecordAdapter.getItem(position);
                if (record != null && !TextUtils.isEmpty(record.url)) {
                    Intent history = new Intent(getApplicationContext(), BrowserActivity.class);
                    history.putExtra(BrowserActivity.URL, Utils.getServerAddress() + record.url);
                    history.putExtra(BrowserActivity.TITLE, record.newsTitle);
                    startActivity(history);
                }
            }
        });
    }

	@Override
    public void onClick(final View v) {
        if (v.getId() == R.id.btn_check) {
            final String ticket = mEtTicket.getText().toString().toUpperCase(Locale.US);
            if (!TextUtils.isEmpty(ticket) && ticket.matches(WicketHelper.TICKET_REGEX)) {
                v.setEnabled(false);
                mEtTicket.setEnabled(false);
                getTitleBar().showProgress();
                new AdvAsyncTask<Void, Void, MsResponse>() {
                    @Override
                    protected MsResponse doInBackground(Void... params) {
                        return HttpUtil.msPost(WicketActivity.this, WicketHelper.TICKET_API, WicketHelper.REQ_CHECKIN,
                                "code=" + Utils.urlEncode(ticket));
                    }

                    @Override
                    protected void onPostExecute(MsResponse response) {
                        v.setEnabled(true);
                        getTitleBar().hideProgress();
                        mEtTicket.setEnabled(true);
                        WicketRecord record = WicketRecord.fromJsonString(response.response);
                        if (response.code == MsResponse.MS_SUCCESS) {
                            mEtTicket.setText("");
                            record.ticket = ticket;
                            mWicketHelper.add(record);
                            mRecordAdapter.notifyDataSetChanged();
                            showInfo(R.string.tic_wicket_success);
                        } else {
                            showInfo(WicketHelper.getFailDesc(getApplicationContext(), response.code, record));
                        }

                    }
                }.executeLong();
            } else {
                showInfo(R.string.tic_input_correct_ticket);
            }
        }
    }

    private void showInfo(int resId) {
        showInfo(getString(resId));
    }

    private void showInfo(String msg) {
        toast(msg);
    }

    private BaseAdapter mRecordAdapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return mRecords.size();
        }

        @Override
        public WicketRecord getItem(int position) {
            return mRecords.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = getLayoutInflater().inflate(R.layout.list_item_wicket_record, parent, false);
            }
            WicketRecord record = getItem(position);
            ((TextView) view.findViewById(R.id.tv_news_title)).setText(record.newsTitle);
            ((TextView) view.findViewById(R.id.tv_time)).setText(WicketHelper.formatTime(record.checkedOn * 1000));
            ((TextView) view.findViewById(R.id.tv_ticket)).setText(record.ticket);

            return view;
        }
    };
}