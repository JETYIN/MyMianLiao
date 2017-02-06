package com.tjut.mianliao.chat;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.Report;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class GroupReportActivity extends BaseActivity implements
OnItemClickListener, OnClickListener {

    public static final String IS_GROUP_CHAT = "is_group_chat";
    public static final String OBJ_ID = "obj_id";

    private ListView mListView;
    private ArrayList<Report> mReports = new ArrayList<Report>();
    private GroupReportAdapter mAdapter;
    private String mReportReason;
    private Button mReportButton;
    private boolean isGroupChat;
    private String mObjId;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_report;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(getString(R.string.chose_report_reason));
        isGroupChat = getIntent().getBooleanExtra(IS_GROUP_CHAT, false);
        mObjId = getIntent().getStringExtra(OBJ_ID);
        if (mObjId == null) {
            mObjId = 0 + "";
        }
        mListView = (ListView) findViewById(R.id.lv_report);
        mReportButton = (Button) findViewById(R.id.btn_report);
        mReportButton.setOnClickListener(this);
        String[] reports = getResources().getStringArray(R.array.cht_report_reasons);
        for (String report : reports) {
            mReports.add(new Report(report, false));
        }
        mAdapter = new GroupReportAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_report:
                if (mReportReason == null) {
                    toast(R.string.chose_report_reason_toast);
                } else {
                    new ReportTask(isGroupChat ? 8 : 7, Integer.parseInt(mObjId),
                            mReportReason).executeLong();
                    getTitleBar().showProgress();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        for (Report report : mReports) {
            report.showButton = false;
            if (report == mReports.get(position)) {
                report.showButton = true;
                mReportReason = report.reason;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private class GroupReportAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mReports.size();
        }

        @Override
        public Report getItem(int position) {
            return mReports.get(position);
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
                view = mInflater.inflate(R.layout.list_item_report, parent, false);
            }

            Report report = getItem(position);

            TextView tv = (TextView) view.findViewById(R.id.tv_report_reason);
            ImageView iv = (ImageView) view.findViewById(R.id.iv_report_btn);
            tv.setText(report.reason);
            iv.setVisibility(report.showButton ? View.VISIBLE : View.INVISIBLE);
            return view;
        }

    }

    private class ReportTask extends MsTask {

        private int type, objId;
        private String reason;

        public ReportTask(int type, int objId, String reason) {
            super(GroupReportActivity.this, MsRequest.POLICE_REPORT);
            this.type = type;
            this.objId = objId;
            this.reason = reason;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("type=").append(type).append("&object_id=")
                    .append(objId).append("&description=").append(Utils.urlEncode(reason))
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            if (response.isSuccessful()) {
                getTitleBar().hideProgress();
                toast(R.string.report_success_toast);
            } else {
                getTitleBar().hideProgress();
//                toast(R.string.report_failed_toast);
            }
        }
    }

}
