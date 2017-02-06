package com.tjut.mianliao.curriculum;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
import com.tjut.mianliao.data.Course;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class ClassmatesActivity extends BaseActivity implements OnItemClickListener,
        OnRefreshListener2<ListView> {
    private static final String TAG = "ClassmatesActivity";

    private Course mCourse;

    private List<UserInfo> mClassmates;

    private ClassmatesAdapter mAdapter;

    private PullToRefreshListView mLvClassmates;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_classmates;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCourse = getIntent().getParcelableExtra(Course.INTENT_EXTRA_NAME);
        if (mCourse == null) {
            finish();
            return;
        }

        getTitleBar().showTitleText(R.string.classmates_title, null);

        mLvClassmates = (PullToRefreshListView) findViewById(R.id.ptrlv_classmates);
        mLvClassmates.setOnItemClickListener(this);
        mLvClassmates.setOnRefreshListener(this);

        mClassmates = new ArrayList<UserInfo>();
        mAdapter = new ClassmatesAdapter();
        mLvClassmates.setAdapter(mAdapter);

        fetchClassmates(true);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserInfo ui = (UserInfo) parent.getItemAtPosition(position);
        if (ui != null) {
            if (ui.userId > 0) {
            		Intent i = new Intent(getApplicationContext(), NewProfileActivity.class);
            		i.putExtra(UserInfo.INTENT_EXTRA_INFO, ui);
            		startActivity(i);
            } else {
                toast(R.string.classmates_user_not_activated);
            }
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchClassmates(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchClassmates(false);
    }

    private void fetchClassmates(boolean refresh) {
        int offset = refresh ? 0 : mClassmates.size();
        new ClassmatesTask(offset).executeLong();
    }

    private class ClassmatesAdapter extends BaseAdapter {

        public ClassmatesAdapter() { }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = getLayoutInflater().inflate(
                        R.layout.list_item_classmate, parent, false);
            }
            UserInfo info = getItem(position);

            NameView tvName = (NameView) view.findViewById(R.id.tv_classmate_name);
            tvName.setText(info.getDisplayName(getApplicationContext()));
            tvName.setMedal(info.primaryBadgeImage);

            StringBuilder desc = new StringBuilder(info.department);
            if (!TextUtils.isEmpty(info.startYear) && !"0".equals(info.startYear)
                    && !"null".equals(info.startYear)) {
                String grade = getString(R.string.prof_desc_grade, info.startYear);
                desc.insert(0, " ").insert(0, grade);
            }
            ((TextView) view.findViewById(R.id.tv_classmate_department)).setText(desc);

            ((TextView) view.findViewById(R.id.tv_classmate_desc)).setText(info.shortDesc);

            ((ImageView) view.findViewById(
                    R.id.iv_classmate_gender)).setImageResource(info.getGenderIcon());

            ((ProImageView) view.findViewById(R.id.iv_classmate_avatar))
                    .setImage(info.getAvatar(), info.defaultAvatar());

            return view;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public UserInfo getItem(int position) {
            return mClassmates.get(position);
        }

        @Override
        public int getCount() {
            return mClassmates.size();
        }
    }

    private class ClassmatesTask extends AdvAsyncTask<Void, Void, List<UserInfo>> {

        private static final String API_COURSE = "api/course";

        private static final String ACTION_GET_CLASSMATES_DETAIL = "get_classmates_detail";

        private static final String PARAM_COURSE_ID = "course_id=";

        private static final String PARAM_OFFSET = "offset=";

        private int mOffset;

        public ClassmatesTask(int offset) {
            mOffset = offset;
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected List<UserInfo> doInBackground(Void... params) {
            List<UserInfo> result = null;
            MsResponse mr = HttpUtil.msGet(getApplicationContext(),
                    API_COURSE, ACTION_GET_CLASSMATES_DETAIL, buildParams());
            if (MsResponse.isSuccessful(mr)) {
                try {
                    JSONArray jsonArray = new JSONArray(mr.response);
                    result = new ArrayList<UserInfo>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        UserInfo info = UserInfo.fromJson(jsonArray.getJSONObject(i));
                        if (info != null) {
                            result.add(info);
                        }
                    }
                } catch (JSONException e) {
                    Utils.logE(TAG, new StringBuilder("JSONException in ").append(API_COURSE)
                            .append("/").append(ACTION_GET_CLASSMATES_DETAIL)
                            .append(": ").append(e.getMessage()).toString());
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(List<UserInfo> result) {
            getTitleBar().hideProgress();
            if (result != null) {
                if (mOffset == 0) {
                    mClassmates.clear();
                }
                mClassmates.addAll(result);
                mAdapter.notifyDataSetChanged();
                mLvClassmates.onRefreshComplete();
                mLvClassmates.setMode(result.size() < mSettings.getPageCount()
                        ? Mode.PULL_FROM_START : Mode.BOTH);
            } else {
                mLvClassmates.onRefreshComplete();
            }
        }

        private String buildParams() {
            return new StringBuilder(PARAM_COURSE_ID).append(mCourse.courseId)
                    .append("&").append(PARAM_OFFSET).append(mOffset)
                    .toString();
        }
    }
}
