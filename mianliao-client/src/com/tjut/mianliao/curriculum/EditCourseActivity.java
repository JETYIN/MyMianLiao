package com.tjut.mianliao.curriculum;

import java.util.ArrayList;

import org.json.JSONArray;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.curriculum.edit.PeriodPicker;
import com.tjut.mianliao.curriculum.edit.WeekPicker;
import com.tjut.mianliao.data.Course;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class EditCourseActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "EditCourseActivity";

    public static final String API_COURSE = "api/course";
    public static final String REQ_JOIN_COURSE = "join_course";
    public static final String REQ_ADD_COURSE = "add_course";

    private static final int MAX_PERIOD = 5;

    private LightDialog mWeeksDialog;
    private WeekPicker mWeekPicker;

    private LightDialog mPeriodDialog;
    private PeriodPicker mPeriodPicker;

    private LinearLayout mLlContainer;
    private View mBtnAddPeriod;

    private EditText mEtName;
    private EditText mEtTeacher;
    private EditText mEtRoom;

    private CourseManager mCourseManager;

    private Course mCourse;

    private Course mPreviewCourse;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_edit_course;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mCourseManager = CourseManager.getInstance(this);

        mCourse = intent.getParcelableExtra(Course.INTENT_EXTRA_NAME);
        if (mCourse == null) {
            finish();
        }

        mPreviewCourse = Course.copy(mCourse);
        mBtnAddPeriod = findViewById(R.id.btn_add_period);
        mBtnAddPeriod.setOnClickListener(this);
        mLlContainer = (LinearLayout) findViewById(R.id.ll_container);

        mEtName = (EditText) findViewById(R.id.et_name);
        mEtTeacher = (EditText) findViewById(R.id.et_teacher);
        mEtRoom = (EditText) findViewById(R.id.et_room);
        TextView tvPeriod = (TextView) findViewById(R.id.tv_period);
        TextView tvWeeks = (TextView) findViewById(R.id.tv_weeks);

        mEtName.setText(mPreviewCourse.name);
        mEtTeacher.setText(mPreviewCourse.teacher);

        ArrayList<Course.Entry> entries = mPreviewCourse.getEntries();
        Course.Entry firstEntry = entries.get(0);
        mEtRoom.setText(firstEntry.classroom);
        tvPeriod.setText(CourseUtil.getPeriodDesc(firstEntry));
        tvWeeks.setText(CourseUtil.getWeekDesc(firstEntry.weeks));

        int size = entries.size();
        if (size > 1) {
            for (int i = 1; i < size; i++) {
                addEntry(entries.get(i));
            }
        }

        getTitleBar().showRightButton(R.drawable.bottom_ok_commit, new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        getTitleBar().showTitleText(R.string.cur_edit_course, null);
    }

    private void addEntry(Course.Entry entry) {
        View view = createEntryEditor(entry);
        mLlContainer.addView(view, mLlContainer.getChildCount() - 1);
        mPreviewCourse.addEntry(entry);
        if (mPreviewCourse.getEntries().size() >= MAX_PERIOD) {
            mBtnAddPeriod.setVisibility(View.GONE);
        }
    }

    private View createEntryEditor(Course.Entry entry) {
        View editor = getLayoutInflater().inflate(R.layout.item_course_period, mLlContainer, false);
        editor.setTag(entry);
        View btnDelete = editor.findViewById(R.id.btn_delete);
        btnDelete.setTag(entry);
        btnDelete.setOnClickListener(this);
        View llPeriod = editor.findViewById(R.id.ll_period);
        llPeriod.setTag(entry);
        llPeriod.setOnClickListener(this);
        View llWeeks = editor.findViewById(R.id.ll_weeks);
        llWeeks.setTag(entry);
        llWeeks.setOnClickListener(this);

        // Fill init value
        EditText etRoom = (EditText) editor.findViewById(R.id.et_room);
        etRoom.setText(entry.classroom);
        TextView tvWeeks = (TextView) editor.findViewById(R.id.tv_weeks);
        tvWeeks.setText(CourseUtil.getWeekDesc(entry.weeks));
        TextView tvPeriod = (TextView) editor.findViewById(R.id.tv_period);
        tvPeriod.setText(CourseUtil.getPeriodDesc(entry));

        return editor;
    }

    private void saveData() {
        collectData();
        if (verifyData()) {
            if (mCourse.courseId > 0 && mPreviewCourse.equals(mCourse)) {
                joinCourse();
            } else {
                int prevCourseId = mCourseManager.containsCourse(mCourse.courseId) ? mCourse.courseId : 0;
                addCourse(prevCourseId);
            }
        } else {
            toast(R.string.course_tst_fill_all_field);
        }
    }

    private void addCourse(final int preCourseId) {
        Utils.logD(TAG, "Add course: " + mCourse.name);
        getTitleBar().showProgress();
        getTitleBar().setRightButtonEnabled(false);
        new AdvAsyncTask<Void, Void, MsResponse>() {
            @Override
            protected MsResponse doInBackground(Void... params) {
                JSONArray ja = new JSONArray();
                ja.put(mPreviewCourse.toJson());
                Utils.logD(TAG, ja.toString());
                return HttpUtil.msPost(getApplicationContext(), API_COURSE, REQ_ADD_COURSE,
                        "prev_course_id=" + preCourseId + "&json=" + Utils.urlEncode(ja.toString()));
            }

            @Override
            protected void onPostExecute(MsResponse response) {
                getTitleBar().hideProgress();
                getTitleBar().setRightButtonEnabled(true);
                int courseId = 0;
                try {
                    courseId = Integer.parseInt(response.response);
                } catch (NumberFormatException e) {}

                if (courseId > 0) {
                    mPreviewCourse.courseId = courseId;
                    if (response.code == MsResponse.MS_SUCCESS) {
                        mCourseManager.addCourse(mPreviewCourse);
                        if (mCourseManager.getCourse(mCourse.courseId) != null) {
                            mCourseManager.leaveCourse(mCourse.courseId);
                            toast(R.string.course_tst_course_updated);
                            Intent i = new Intent();
                            i.putExtra(Course.INTENT_EXTRA_NAME, mPreviewCourse);
                            setResult(Activity.RESULT_OK, i);
                        } else {
                            toast(R.string.course_tst_course_joint);
                        }
                        finish();
                        return;
                    }
                }
                toast(getString(R.string.course_tst_failed_join_course,
                        MsResponse.getFailureDesc(getApplicationContext(), response.code)));
            }
        }.executeLong();
    }

    private void joinCourse() {
        if (mCourseManager.getCourse(mCourse.courseId) != null) {
            // Already joint the course and nothing changed, so just finish.
            finish();
            return;
        }

        Utils.logD(TAG, "Join course: " + mCourse.courseId + " " + mCourse.name);
        getTitleBar().showProgress();
        getTitleBar().setRightButtonEnabled(false);
        new AdvAsyncTask<Void, Void, MsResponse>() {
            @Override
            protected MsResponse doInBackground(Void... params) {
                return HttpUtil.msPost(getApplicationContext(), API_COURSE, REQ_JOIN_COURSE,
                        "course_id=" + mCourse.courseId);
            }

            @Override
            protected void onPostExecute(MsResponse response) {
                getTitleBar().hideProgress();
                getTitleBar().setRightButtonEnabled(true);
                if (response.code == MsResponse.MS_SUCCESS
                        || response.code == MsResponse.MS_COURSE_ALREADY_JOINT) {
                    toast(R.string.course_tst_course_joint);
                    mCourseManager.addCourse(mCourse);
                    finish();
                } else {
                    toast(getString(R.string.course_tst_failed_join_course,
                            MsResponse.getFailureDesc(getApplicationContext(), response.code)));
                }
            }
        }.executeLong();
    }

    private void collectData() {
        ArrayList<Course.Entry> entries = mPreviewCourse.getEntries();
        final int size = entries.size();
        mPreviewCourse.name = mEtName.getText().toString().trim();
        mPreviewCourse.teacher = mEtTeacher.getText().toString().trim();
        Course.Entry first = entries.get(0);
        first.classroom = mEtRoom.getText().toString().trim();

        if (size > 1) {
            for (int i = 0; i < mLlContainer.getChildCount(); i++) {
                View v = mLlContainer.getChildAt(i);
                if (v.getId() == R.id.rl_edit_period && v.getTag() != null && v.getTag() instanceof Course.Entry) {
                    EditText etRoom = (EditText) v.findViewById(R.id.et_room);
                    Course.Entry entry = (Course.Entry) v.getTag();
                    entry.classroom = etRoom.getText().toString().trim();
                }
            }
        }
    }

    private boolean verifyData() {
        if (mPreviewCourse.name == null || mPreviewCourse.name.length() == 0) {
            return false;
        }

        for (Course.Entry entry : mPreviewCourse.getEntries()) {
            if (entry.weekday == 0 || entry.weeks == 0 || entry.periodEnd == 0 || entry.periodStart == 0) {
                return false;
            }
        }

        return true;
    }

    public void editWeeks(View v) {
        getWeeksDialog((TextView) v.findViewById(R.id.tv_weeks), mPreviewCourse.getEntries().get(0)).show();
    }

    private LightDialog getWeeksDialog(TextView tvWeeks, Course.Entry entry) {
        if (mWeeksDialog == null) {
            mWeekPicker = new WeekPicker(this);

            mWeeksDialog = new LightDialog(this);
            mWeeksDialog.setAutoDismiss(false);
            mWeeksDialog.setTitle(R.string.course_choose_weeks);
            mWeeksDialog.setView(mWeekPicker.getRootView());
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_NEGATIVE:
                            mWeekPicker.cancelEdit();
                            dialog.dismiss();
                            break;

                        case DialogInterface.BUTTON_POSITIVE:
                            if (mWeekPicker.hasOverlap(mPreviewCourse.getEntries())) {
                                toast(R.string.course_tst_period_overlapped);
                            } else {
                                mWeekPicker.finishEdit();
                                dialog.dismiss();
                            }
                            break;

                        default:
                            break;
                    }
                }
            };
            mWeeksDialog.setNegativeButton(android.R.string.cancel, listener);
            mWeeksDialog.setPositiveButton(android.R.string.ok, listener);
        }
        mWeekPicker.setTarget(tvWeeks, entry);
        return mWeeksDialog;
    }

    public void editPeriod(View v) {
        getPeriodDialog((TextView) v.findViewById(R.id.tv_period), mPreviewCourse.getEntries().get(0)).show();
    }

    private LightDialog getPeriodDialog(TextView tvPeriod, Course.Entry entry) {
        if (mPeriodDialog == null) {
            mPeriodPicker = new PeriodPicker(this);

            mPeriodDialog = new LightDialog(this);
            mPeriodDialog.setAutoDismiss(false);
            mPeriodDialog.setTitle(R.string.course_choose_periods);
            mPeriodDialog.setView(mPeriodPicker.getRootView());
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_NEGATIVE:
                            mPeriodPicker.cancelEdit();
                            dialog.dismiss();
                            break;

                        case DialogInterface.BUTTON_POSITIVE:
                            if (mPeriodPicker.hasOverlap(mPreviewCourse.getEntries())) {
                                toast(R.string.course_tst_period_overlapped);
                            } else {
                                mPeriodPicker.finishEdit();
                                dialog.dismiss();
                            }
                            break;

                        default:
                            break;
                    }

                }
            };
            mPeriodDialog.setNegativeButton(android.R.string.cancel, listener);
            mPeriodDialog.setPositiveButton(android.R.string.yes, listener);
        }
        mPeriodPicker.setTarget(tvPeriod, entry);
        return mPeriodDialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_period:
                addEntry(new Course.Entry());
                break;

            case R.id.ll_period:
                if (v.getTag() != null && v.getTag() instanceof Course.Entry) {
                    getPeriodDialog((TextView) v.findViewById(R.id.tv_period), (Course.Entry) v.getTag()).show();
                }
                break;

            case R.id.ll_weeks:
                if (v.getTag() != null && v.getTag() instanceof Course.Entry) {
                    getWeeksDialog((TextView) v.findViewById(R.id.tv_weeks), (Course.Entry) v.getTag()).show();
                }
                break;

            case R.id.btn_delete:
                ViewParent parent = v.getParent();
                if (parent instanceof View && ((View) parent).getId() == R.id.rl_edit_period) {
                    mLlContainer.removeView((View) parent);
                    if (v.getTag() != null && v.getTag() instanceof Course.Entry) {
                        Course.Entry entry = (Course.Entry) v.getTag();
                        mPreviewCourse.removeEntry(entry);
                        if (mPreviewCourse.getEntries().size() < MAX_PERIOD) {
                            mBtnAddPeriod.setVisibility(View.VISIBLE);
                        }
                    }
                }
                break;

            default:
                break;
        }
    }
}
