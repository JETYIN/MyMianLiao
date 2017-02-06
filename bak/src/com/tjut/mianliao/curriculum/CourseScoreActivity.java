package com.tjut.mianliao.curriculum;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
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
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class CourseScoreActivity extends BaseActivity implements OnRefreshListener2<ListView> {

    private static final String TAG = "CourseScoreActivity";

    private PullToRefreshListView mPtrLvScores;
    private ScoreAdapter mAdapter;

    private List<Score> mScores;
    private int mSemester;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_course_score;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.cs_query_score, null);

        mSemester = CourseManager.getInstance(this).getSemester();
        ((TextView) findViewById(R.id.tv_info)).setText(getString(
                R.string.course_score_info, CourseUtil.getSemesterDesc(mSemester)));

        mScores = new ArrayList<Score>();
        mAdapter = new ScoreAdapter();
        mPtrLvScores = (PullToRefreshListView) findViewById(R.id.ptrlv_score);
        mPtrLvScores.setOnRefreshListener(this);
        mPtrLvScores.setAdapter(mAdapter);

        queryScores(true);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        queryScores(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        queryScores(false);
    }

    private void queryScores(boolean refresh) {
        int offset = refresh ? 0 : mAdapter.getCount();
        new ScoreTask(this, offset).executeLong();
    }

    private static class Score {
        public int id;
        public String name;
        public String teacher;
        public String credit;
        public String score;

        public static Score fromJson(JSONObject json) {
            if (json == null) {
                return null;
            }
            Score s = new Score();
            s.id = json.optInt("id");
            s.name = json.optString("name");
            s.teacher = json.optString("teacher");
            s.credit = json.optString("userCredit");
            s.score = json.optString("score");
            return s;
        }
    }

    private class ScoreAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mScores.size();
        }

        @Override
        public Score getItem(int position) {
            return mScores.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_score, parent, false);
            }
            Score s = getItem(position);

            ((TextView) view.findViewById(R.id.tv_name)).setText(s.name);
            ((TextView) view.findViewById(R.id.tv_teacher)).setText(s.teacher);
            ((TextView) view.findViewById(R.id.tv_credit)).setText(s.credit);
            ((TextView) view.findViewById(R.id.tv_score)).setText(s.score);

            int color;
            int score = Integer.parseInt(s.score);
            if (score >= 90) {
                color = getResources().getColor(R.color.course_score_1);
            } else if (score >= 70) {
                color = getResources().getColor(R.color.course_score_2);
            } else if (score >= 50) {
                color = getResources().getColor(R.color.course_score_3);
            } else {
                color = getResources().getColor(R.color.course_score_4);
            }
            ((GradientDrawable) view.findViewById(
                    R.id.ll_score).getBackground()).setColor(color);

            return view;
        }
    }

    private class ScoreTask extends AdvAsyncTask<Void, Void, MsResponse> {
        private static final String API_COURSE = "api/course";
        private static final String ACTION_QUERY_SCORE = "query_score";
        private static final String PARAM_SEMESTER = "semester=";
        private static final String PARAM_OFFSET = "offset=";

        private Context mContext;
        private int mOffset;

        public ScoreTask(Context context, int offset) {
            mContext = context;
            mOffset = offset;
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected MsResponse doInBackground(Void... params) {
            // TODO return HttpUtil.msGet directly when server is ok
            MsResponse mr =
                    HttpUtil.msGet(mContext, API_COURSE, ACTION_QUERY_SCORE, buildParams());
            mr = mockData();
            return mr;
        }

        @Override
        protected void onPostExecute(MsResponse mr) {
            getTitleBar().hideProgress();
            if (MsResponse.isSuccessful(mr)) {
                try {
                    JSONArray ja = new JSONArray(mr.response);
                    if (mOffset == 0) {
                        mScores.clear();
                    }
                    int length = ja.length();
                    for (int i = 0; i < length; i++) {
                        Score s = Score.fromJson(ja.optJSONObject(i));
                        if (s != null) {
                            mScores.add(s);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    mPtrLvScores.onRefreshComplete();
                    mPtrLvScores.setMode(length < mSettings.getPageCount()
                            ? Mode.PULL_FROM_START : Mode.BOTH);
                } catch (JSONException e) {
                    mPtrLvScores.onRefreshComplete();
                    toast(MsResponse.getFailureDesc(mContext,
                            R.string.course_query_score_failed,
                            MsResponse.MS_PARSE_FAILED));
                }
            } else {
                mPtrLvScores.onRefreshComplete();
                toast(MsResponse.getFailureDesc(mContext,
                        R.string.course_query_score_failed, mr.code));
            }
        }

        private String buildParams() {
            return new StringBuilder(PARAM_SEMESTER).append(mSemester)
                    .append("&").append(PARAM_OFFSET).append(mOffset)
                    .toString();
        }

        private MsResponse mockData() {
            JSONArray ja = new JSONArray();
            int pageCount = mSettings.getPageCount();
            int count = Math.min(pageCount, (int) (Math.random() * pageCount * 2));
            try {
                for (int i = 1; i <= count; i++) {
                    JSONObject json = new JSONObject();
                    json.put("id", i);
                    json.put("name", String.format("Name %d", i));
                    json.put("teacher", String.format("Teacher %d", i));
                    json.put("userCredit", String.format("%.1f", Math.random() * 5));
                    json.put("score", String.format("%d", new Random().nextInt(100)));
                    ja.put(json);
                }
            } catch (JSONException e) {
                Utils.logD(TAG, e.getMessage());
            }

            MsResponse mr = new MsResponse();
            mr.code = MsResponse.MS_SUCCESS;
            mr.response = ja.toString();
            return mr;
        }
    }
}
