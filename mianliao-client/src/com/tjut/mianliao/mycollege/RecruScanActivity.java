package com.tjut.mianliao.mycollege;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.job.RecruitInfo;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class RecruScanActivity extends BaseActivity implements OnRefreshListener2<ListView> {

    private enum ViewType {
        TYPE_TIME, TYPE_HEADER, TYPE_JOB
    }

    private static final String TYPE_HEADER = "header";
    private static final String TYPE_TIME = "time";

    private static final int COLOR_PERSONAL = 0xFFEAEBB7;
    private static final int COLOR_GRAY = 0xFFF7F7F7;
    private static final int COLOR_NORMAL = 0xFFFFFFFF;
    private static final int COLOR_TRANSPARENT_WHITE = 0x4DFFFFFF;
    private static final int COLOR_TRANSPARENT_TITLE = 0x4DBACCD6;
 
    private PullToRefreshListView mPtrJobs;
    private ArrayList<RecruitInfo> mRecruitInfos;
    private ArrayList<Object> mObjInfos;
    private RecruJobsAdapter mAdapter;
    private boolean mShouldCircle;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_recruitment_scan;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(getString(R.string.job_recru_scan_title));
        mRecruitInfos = new ArrayList<>();
        mObjInfos = new ArrayList<>();
        mPtrJobs = (PullToRefreshListView) findViewById(R.id.lv_job_scan);
        mAdapter = new RecruJobsAdapter();
        mPtrJobs.setAdapter(mAdapter);
        mPtrJobs.setOnRefreshListener(this);
        mPtrJobs.setMode(Mode.BOTH);
        mPtrJobs.setRefreshing(Mode.PULL_FROM_START);
    }

    private void addSpliteData() {
        mObjInfos.add(TYPE_TIME);
        mObjInfos.add(TYPE_HEADER);
    }

    private void fetchRecruInfo(boolean refresh) {
        int offset = refresh ? 0 : mRecruitInfos.size();
        new GetRecruJobsTask(offset).executeLong();
    }

    private void fillRecruitInfo() {
        mObjInfos.clear();
        int len = mRecruitInfos.size();
        boolean isFirst = true;
        for (int i = 0; i < len; i++) {
            if (isFirst) {
                isFirst = false;
                addSpliteData();
            }
            mObjInfos.add(mRecruitInfos.get(i));
            if ((i+1) < len) {
                long timeL = mRecruitInfos.get(i).time;
                long timeR = mRecruitInfos.get(i + 1).time;
                if (!Utils.isSameDay(timeR * 1000, timeL * 1000)) {
                    addSpliteData();
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private View inflateView(ViewType type, ViewGroup parent) {
        switch (type) {
            case TYPE_HEADER:
                return mInflater.inflate(R.layout.item_job_scan_header, parent, false);
            case TYPE_JOB:
                return mInflater.inflate(R.layout.list_item_recru_view, parent, false);
            case TYPE_TIME:
                return mInflater.inflate(R.layout.item_recru_job_time, parent, false);
            default:
                return null;
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchRecruInfo(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchRecruInfo(false);
    }

    private void updateTime(View view, RecruitInfo job) {
        ((TextView) view).setText(Utils.getTimeString(2, job.time));
    }

    private void updateJobInfo(View view, RecruitInfo job, int position) {
        TextView tvName = (TextView) view.findViewById(R.id.tv_position);
        TextView tvCorp = (TextView) view.findViewById(R.id.tv_corp);
        TextView tvTime = (TextView) view.findViewById(R.id.tv_invite_time);
        tvName.setText(job.title);
        tvCorp.setText(job.company);
        tvTime.setText(Utils.getTimeString(1, job.time));
        if (!mShouldCircle) {
            if (job.isPersonal()) {
                view.setBackgroundColor(COLOR_PERSONAL);
            } else if (position %2 == 0) {
                view.setBackgroundColor(COLOR_GRAY);
            } else {
                view.setBackgroundColor(COLOR_NORMAL);
            }
        } else {
            if (job.isPersonal()) {
                view.setBackgroundResource(R.drawable.bg_job_view_personal);
            } else if (position %2 == 0) {
                view.setBackgroundResource(R.drawable.bg_job_view_gray);
            } else {
                view.setBackgroundResource(R.drawable.bg_job_view_normal);
            }
        }
    }

    private class  GetRecruJobsTask extends MsTask{

        private int mOffset;

        public GetRecruJobsTask(int offset) {
            super(RecruScanActivity.this, MsRequest.LIST_MY_RECRUIT_LIST);
            mOffset = offset;
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected String buildParams() {
            return "offset=" + mOffset;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrJobs.onRefreshComplete();
            getTitleBar().hideProgress();
            if (response.isSuccessful()) {
                ArrayList<RecruitInfo> infos = JsonUtil.getArray(
                        response.getJsonArray(), RecruitInfo.TRANSFORMER);
                if (infos != null && infos.size() > 0) {
                    if (mOffset == 0) {
                        mRecruitInfos = infos;
                    } else {
                        mRecruitInfos.addAll(infos);
                    }
                    fillRecruitInfo();
                }
            }
        }
    }

    private class RecruJobsAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mObjInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return mObjInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return ViewType.values().length;
        }

        @Override
        public int getItemViewType(int position) {
            Object obj = getItem(position);
            if (obj instanceof String) {
                if (TYPE_HEADER.equals((String) obj)) {
                    return ViewType.TYPE_HEADER.ordinal();
                } else {
                    return ViewType.TYPE_TIME.ordinal();
                }
            } else {
                return ViewType.TYPE_JOB.ordinal();
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewType type = ViewType.values()[getItemViewType(position)];
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = inflateView(type, parent);
            }
            if (view == null) {
                return null;
            }

            Object obj = getItem(position);
            switch (type) {
                case TYPE_JOB:
                    if (position < mObjInfos.size() - 1) {
                        Object o = getItem(position + 1);
                        if (o instanceof RecruitInfo) {
                            mShouldCircle = false;
                        } else {
                            mShouldCircle = true;
                        }
                    } else {
                        mShouldCircle = true;
                    }
                    updateJobInfo(view, (RecruitInfo) obj, position);
                    break;
                case TYPE_TIME:
                    updateTime(view, (RecruitInfo) getItem(position + 2));
                    break;
                case TYPE_HEADER:
                default:
                    break;
            }
            return view;
        }
    }
}
