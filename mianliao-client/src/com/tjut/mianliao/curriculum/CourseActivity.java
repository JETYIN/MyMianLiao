package com.tjut.mianliao.curriculum;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.NameView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.Course;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.CourseForumActivity;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class CourseActivity extends BaseActivity implements View.OnClickListener,
        View.OnTouchListener {
    private static final String TAG = "CourseActivity";

    public static final String EXTRA_SCHEDULE = "schedule";
    private static final String API_COURSE = "api/course";
    private static final String REQ_GET_CLASSMATES = "get_classmates";
    private static final String REQ_LEAVE_COURSE = "leave_course";
    private static final String REQ_GET_INFO = "get_rating_and_checkin";
    private static final String REQ_RATE = "rating";
    private static final String REQ_CHECKIN = "checkin";

    private static final String API_COURSE_FORUM = "api/cforum";
    private static final String REQ_GET_POSTS = "list_thread_by_forum";

    private static final int REQUEST_CODE = TAG.hashCode();

    private LightDialog mLeaveCourseDialog;

    private int mRatingColor;

    private GridView mGvClassmates;

    private RatingBar mRbRating;

    private Course mCourse;

    private String mSchedule;

    private int mCheckinCount;

    private ArrayList<UserInfo> mClassmates = new ArrayList<UserInfo>();
    private ArrayList<CfPost> mPosts = new ArrayList<CfPost>();

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_course;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCourse = getIntent().getParcelableExtra(Course.INTENT_EXTRA_NAME);
        if (mCourse == null) {
            finish();
            return;
        }
        mSchedule = getIntent().getStringExtra(EXTRA_SCHEDULE);

        mRatingColor = getResources().getColor(R.color.txt_color_red);

        mRbRating = (RatingBar) findViewById(R.id.rb_rating);
        mRbRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (rating < 1) {
                    ratingBar.setRating(1);
                }
            }
        });

        mGvClassmates = (GridView) findViewById(R.id.gv_classmates);
        mGvClassmates.setAdapter(mClassmatesAdapter);
        mGvClassmates.setOnTouchListener(this);

        fillCourseInfo();

        getTitleBar().showTitleText(mCourse.name, null);
        if (!mCourse.authed) {
            getTitleBar().showRightButton(R.drawable.btn_title_bar_edit, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getApplicationContext(), EditCourseActivity.class);
                    i.putExtra(Course.INTENT_EXTRA_NAME, mCourse);
                    startActivityForResult(i, REQUEST_CODE);
                }
            });
        }

        if (mCourse.authUser) {
            findViewById(R.id.btn_leave_course).setVisibility(View.GONE);
        }

        new AdvAsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                getTitleBar().showProgress();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                final MsResponse classmates = HttpUtil.msGet(getApplicationContext(), API_COURSE,
                        REQ_GET_CLASSMATES, "course_id=" + mCourse.courseId);
                if (classmates.code == MsResponse.MS_SUCCESS) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateClassmates(classmates.response);
                        }
                    });
                }

                final MsResponse posts = HttpUtil.msGet(getApplicationContext(), API_COURSE_FORUM,
                        REQ_GET_POSTS, "course_id=" + mCourse.courseId + "&limit=2");
                if (MsResponse.isSuccessful(posts)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject json = new JSONObject(posts.response);
                                JSONArray ja = json.getJSONArray("threads");
                                int length = ja.length();
                                if (length == 0) {
                                    return;
                                }
                                for (int i = 0; i < length; i++) {
                                    CfPost post = CfPost.fromJson(ja.optJSONObject(i));
                                    if (post != null) {
                                        mPosts.add(post);
                                    }
                                }
                                updateForumInfo();
                            } catch (JSONException e) {
                                Utils.logW(TAG, "Failed to parse posts: " + e.getMessage());
                            }
                        }
                    });
                }

                final MsResponse info = HttpUtil.msGet(getApplicationContext(), API_COURSE,
                        REQ_GET_INFO, "course_id=" + mCourse.courseId + "&schedule=" + mSchedule);
                if (info.code == MsResponse.MS_SUCCESS) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject json = new JSONObject(info.response);
                                float ratingAvg = (float) json.getDouble("course_rating_avg");
                                int ratingCount = json.getInt("course_rating_count");
                                int ratingSelf = json.getInt("lesson_self_rating");
                                findViewById(R.id.ll_rating).setVisibility(View.VISIBLE);
                                updateRating(ratingAvg, ratingCount, ratingSelf);
                                mCheckinCount = json.getInt("lesson_check_count");
                                int checkinSelf = json.getInt("lesson_check_timestamp");
                                updateCheckin(checkinSelf > 0);
                            } catch (JSONException e) {
                                Utils.logW(TAG, "Failed to parse course info: " + e.getMessage());
                            }
                        }
                    });
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                getTitleBar().hideProgress();
            }
        }.executeLong();
    }

    private void updateForumInfo() {
        int size = mPosts.size();
        View post1 = findViewById(R.id.ll_post_item_1);
        View post2 = findViewById(R.id.ll_post_item_2);
        switch (size) {
            case 1:
                showPost(post1, mPosts.get(0));
                post2.setVisibility(View.GONE);
                break;
            case 2:
                showPost(post1, mPosts.get(0));
                showPost(post2, mPosts.get(1));
                break;
            default:
                post1.setVisibility(View.GONE);
                post2.setVisibility(View.GONE);
        }
    }

    private void showPost(View parent, CfPost post) {
        parent.setVisibility(View.VISIBLE);
        ((ProImageView) parent.findViewById(R.id.av_avatar))
                .setImage(post.userInfo.getAvatar(), post.userInfo.defaultAvatar());
        NameView tvName = (NameView) parent.findViewById(R.id.tv_user_name);
        tvName.setText(post.userInfo.getDisplayName(this));
        tvName.setMedal(post.userInfo.primaryBadgeImage);

        ((TextView) parent.findViewById(R.id.tv_extra_info)).setText(
                getString(R.string.news_published_on, Utils.getTimeDesc(post.createdOn)));
        ((TextView) parent.findViewById(R.id.tv_desc)).setText(post.content);

        parent.findViewById(R.id.top_right_item).setVisibility(View.GONE);
        parent.findViewById(R.id.tv_like).setVisibility(View.GONE);
    }

    private void updateClassmates(String classmates) {
        int classmatesCount = 0;
        if (classmates != null) {
            try {
                JSONObject json = new JSONObject(classmates);
                classmatesCount = json.optInt("count");
                JSONArray ja = json.optJSONArray("classmates");
                if (classmatesCount > 0 && ja != null && ja.length() > 0) {
                    for (int i = 0; i < ja.length(); i++) {
                        if (mClassmates.size() >= 5) {
                            break;
                        }
                        JSONObject jo = ja.optJSONObject(i);
                        UserInfo info = UserInfo.fromJson(jo);
                        if (info != null) {
                            mClassmates.add(info);
                        }
                    }

                    View llClassmates = findViewById(R.id.ll_classmates);
                    llClassmates.setVisibility(View.VISIBLE);
                    llClassmates.setOnClickListener(CourseActivity.this);
                    mClassmatesAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                Utils.logW(TAG, "Failed to parse classmates info: " + e.getMessage());
            }
        }
        ((TextView) findViewById(R.id.tv_classmates_desc)).setText(
                getString(R.string.course_total_classmates, classmatesCount));
    }

    private void updateRating(float ratingAvg, int ratingCount, int ratingSelf) {
        if (ratingSelf > 0) {
            mRbRating.setRating(ratingSelf);
            mRbRating.setIsIndicator(true);
            findViewById(R.id.btn_rating).setVisibility(View.GONE);
        } else {
            mRbRating.setIsIndicator(false);
            findViewById(R.id.btn_rating).setVisibility(View.VISIBLE);
        }
        String ratingDesc = getString(R.string.course_rating_desc, ratingAvg, ratingCount);
        int end = ratingDesc.indexOf(' ');
        ((TextView) findViewById(R.id.tv_rating_desc)).setText(
                Utils.getColoredText(ratingDesc, mRatingColor, 0, end));
    }

    private void updateCheckin(boolean checkedIn) {
        String checkinDesc = getString(R.string.course_checkin_desc, mCheckinCount);
        ((TextView) findViewById(R.id.tv_checkin_desc)).setText(checkinDesc);
        findViewById(R.id.btn_checkin).setEnabled(!checkedIn);
        findViewById(R.id.iv_checked_in).setVisibility(checkedIn ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGvClassmates.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mCourse = data.getParcelableExtra(Course.INTENT_EXTRA_NAME);
            fillCourseInfo();
            ((TextView) findViewById(R.id.tv_classmates_desc)).setText(
                    getString(R.string.course_total_classmates, 1));
            mClassmates.clear();
            mClassmates.add(AccountInfo.getInstance(this).getUserInfo());
            mClassmatesAdapter.notifyDataSetChanged();
        }
    }

    public void showLeaveCourseDialog() {
        if (mLeaveCourseDialog == null) {
            mLeaveCourseDialog = new LightDialog(this);
            mLeaveCourseDialog.setTitle(R.string.course_leave);
            mLeaveCourseDialog.setMessage(getString(R.string.course_leave_confirm, mCourse.name));
            mLeaveCourseDialog.setNegativeButton(android.R.string.cancel, null);
            mLeaveCourseDialog.setPositiveButton(android.R.string.ok, new DialogInterface
                    .OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new LeaveCourseTask().executeLong();
                }
            });
        }
        mLeaveCourseDialog.show();
    }

    private void fillCourseInfo() {
        setText(R.id.tv_course_name, mCourse.name);
        setText(R.id.tv_teacher, mCourse.teacher);
        setText(R.id.tv_course_desc, CourseUtil.getCourseDesc(mCourse));
    }

    private void setText(int resId, String text) {
        ((TextView) findViewById(resId)).setText(text);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.ll_classmates:
                showClassmates();
                break;

            case R.id.btn_leave_course:
                showLeaveCourseDialog();
                break;

            case R.id.ll_course_forum:
                Intent icf = new Intent(this, CourseForumActivity.class);
                Forum forum = new Forum();
                forum.courseId = mCourse.courseId;
                forum.type = Forum.TYPE_COURSE;
                icf.putExtra(Forum.INTENT_EXTRA_NAME, forum);
                startActivity(icf);
                break;

            case R.id.btn_rating:
                if (mRbRating.getRating() < 1) {
                    return;
                }
                v.setEnabled(false);
                getTitleBar().showProgress();
                new AdvAsyncTask<Void, Void, MsResponse>() {
                    @Override
                    protected MsResponse doInBackground(Void... params) {
                        String req = "course_id=" + mCourse.courseId + "&schedule=" + mSchedule +
                                "&rating=" + (int) mRbRating.getRating();
                        return HttpUtil.msPost(getApplicationContext(), API_COURSE, REQ_RATE, req);
                    }

                    @Override
                    protected void onPostExecute(MsResponse response) {
                        getTitleBar().hideProgress();
                        if (response.code == MsResponse.MS_SUCCESS) {
                            try {
                                JSONObject json = new JSONObject(response.response);
                                float ratingAvg = (float) json.getDouble("course_rating_avg");
                                int ratingCount = json.getInt("course_rating_count");
                                updateRating(ratingAvg, ratingCount, (int) mRbRating.getRating());
                            } catch (JSONException e) {
                                Utils.logW(TAG, "Failed to parse rating response: "
                                        + e.getMessage());
                            }

                        } else {
                            toast(R.string.course_rating_failed);
                            v.setEnabled(true);
                        }
                    }
                }.executeLong();
                break;

            case R.id.btn_checkin:
                v.setEnabled(false);
                getTitleBar().showProgress();
                new AdvAsyncTask<Void, Void, MsResponse>() {
                    @Override
                    protected MsResponse doInBackground(Void... params) {
                        String req = "course_id=" + mCourse.courseId + "&schedule=" + mSchedule;
                        return HttpUtil.msPost(getApplicationContext(), API_COURSE, REQ_CHECKIN,
                                req);
                    }

                    @Override
                    protected void onPostExecute(MsResponse response) {
                        getTitleBar().hideProgress();
                        if (response.code == MsResponse.MS_SUCCESS) {
                            mCheckinCount++;
                            updateCheckin(true);
                        } else {
                            toast(R.string.course_checkin_failed);
                            v.setEnabled(true);
                        }
                    }
                }.executeLong();
                break;

            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.gv_classmates && event.getAction() == MotionEvent.ACTION_UP) {
            showClassmates();
            mGvClassmates.setEnabled(false);
        }
        return false;
    }

    private void showClassmates() {
        Intent i = new Intent(this, ClassmatesActivity.class);
        i.putExtra(Course.INTENT_EXTRA_NAME, mCourse);
        startActivity(i);
    }

    private BaseAdapter mClassmatesAdapter = new BaseAdapter() {
        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public int getCount() {
            return mClassmates.size();
        }

        @Override
        public UserInfo getItem(int position) {
            return mClassmates.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mClassmates.get(position).userId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ProImageView iv = (ProImageView) getLayoutInflater().inflate(R.layout.item_course_avatar,
                    parent, false);
            UserInfo user = getItem(position);
            iv.setImage(user.getAvatar(), user.defaultAvatar());
            return iv;
        }
    };

    private class LeaveCourseTask extends AdvAsyncTask<Void, Void, MsResponse> {
        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
            findViewById(R.id.btn_leave_course).setEnabled(false);
        }

        @Override
        protected MsResponse doInBackground(Void... voids) {
            return HttpUtil.msPost(getApplicationContext(), API_COURSE, REQ_LEAVE_COURSE,
                    "course_id=" + mCourse.courseId);
        }

        @Override
        protected void onPostExecute(MsResponse s) {
            getTitleBar().hideProgress();
            findViewById(R.id.btn_leave_course).setEnabled(true);
            if (s != null && (s.code == MsResponse.MS_SUCCESS
                    || s.code == MsResponse.MS_COURSE_USER_HASNT_JOINT)) {
                CourseManager.getInstance(getApplicationContext()).leaveCourse(mCourse.courseId);
                finish();
            } else {
                toast(R.string.course_tst_failed_get_info);
            }
        }
    }
}
