package com.tjut.mianliao.curriculum.search;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.tjut.mianliao.BaseTask;
import com.tjut.mianliao.BaseTask.TaskExecutionListener;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.Course;
import com.tjut.mianliao.data.Course.Entry;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class CourseSearchManager implements TaskExecutionListener {

    public static final int TASK_COURSE_JOIN = 0x01;
    public static final int TASK_COURSE_LEAVE = 0x02;
    public static final int TASK_COURSE_CATEGORY = 0x03;
    public static final int TASK_COURSE_SEARCH = 0x04;

    private static WeakReference<CourseSearchManager> sInstanceRef;

    private Context mContext;
    private List<TaskExecutionListener> mListeners;

    public static synchronized CourseSearchManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        CourseSearchManager instance = new CourseSearchManager(context);
        sInstanceRef = new WeakReference<CourseSearchManager>(instance);
        return instance;
    }

    private CourseSearchManager(Context context) {
        mContext = context.getApplicationContext();
        mListeners = new ArrayList<TaskExecutionListener>();
    }

    @Override
    public void onPreExecute(int type) {
        for (TaskExecutionListener listener : mListeners) {
            listener.onPreExecute(type);
        }
    }

    @Override
    public void onPostExecute(int type, MsResponse mr) {
        switch (type) {
            case TASK_COURSE_CATEGORY:
            case TASK_COURSE_SEARCH:
                if (!MsResponse.isSuccessful(mr)) {
                    toast(MsResponse.getFailureDesc(mContext,
                            R.string.course_tst_search_error, mr.code));
                }
                break;

            case TASK_COURSE_JOIN:
                if (mr.value != null) {
                    ((Course) mr.value).interacting = false;
                    if (MsResponse.isSuccessful(mr)) {
                        toast(R.string.course_tst_join_success);
                    } else if (MsResponse.MS_COURSE_ALREADY_JOINT == mr.code) {
                        toast(R.string.course_tst_join_already_joint);
                    } else {
                        toast(MsResponse.getFailureDesc(mContext,
                                R.string.course_tst_join_failed, mr.code));
                    }
                }
                break;

            case TASK_COURSE_LEAVE:
                if (mr.value != null) {
                    ((Course) mr.value).interacting = false;
                    if (MsResponse.isSuccessful(mr)) {
                        toast(R.string.course_tst_leave_success);
                    } else if (MsResponse.MS_COURSE_USER_HASNT_JOINT == mr.code) {
                        toast(R.string.course_tst_leave_hasnt_joint);
                    } else {
                        toast(MsResponse.getFailureDesc(mContext,
                                R.string.course_tst_leave_failed, mr.code));
                    }
                }
                break;

            default:
                break;
        }

        for (TaskExecutionListener listener : mListeners) {
            listener.onPostExecute(type, mr);
        }
    }

    public void registerListener(TaskExecutionListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(0, listener);
        }
    }

    public void unregisterListener(TaskExecutionListener listener) {
        if (listener != null) {
            mListeners.remove(listener);
        }
    }

    public void startCourseJoinTask(Course course) {
        new CourseTask(mContext, TASK_COURSE_JOIN).setCourse(course)
                .setListener(this).executeLong();
    }

    public void startCourseLeaveTask(Course course) {
        new CourseTask(mContext, TASK_COURSE_LEAVE).setCourse(course)
                .setListener(this).executeLong();
    }

    public BaseTask startCourseSearchTask(Course course, int offset) {
        Entry entry = course.getEntries().get(0);
        int type = TextUtils.isEmpty(course.name) && entry.weekday == 0
                && entry.periodStart == 0 ? TASK_COURSE_CATEGORY : TASK_COURSE_SEARCH;
        BaseTask task = new CourseTask(mContext, type).setCourse(course)
                .setOffset(offset).setListener(this);
        task.executeLong();
        return task;
    }

    public void clear() {
        mListeners.clear();
        sInstanceRef.clear();
    }

    private List<Course> toCategoryList(JSONArray array, Course course) {
        int semester = course == null ? 0 : course.semester;
        List<Course> categories = new ArrayList<Course>();
        for (int i = 0; i < array.length(); i++) {
            String name = array.optString(i);
            if (!TextUtils.isEmpty(name)) {
                categories.add(new Course(semester, name));
            }
        }
        return categories;
    }

    private List<Course> toCourseList(JSONArray array) {
        List<Course> courses = new ArrayList<Course>();
        for (int i = 0; i < array.length(); i++) {
            Course c = Course.fromJson(array.optJSONObject(i));
            if (c != null) {
                courses.add(c);
            }
        }
        return courses;
    }

    private void toast(int resId) {
        Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show();
    }

    private void toast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    private class CourseTask extends BaseTask {
        private static final String API_COURSE = "api/course";

        private static final String ACTION_COURSE_JOIN = "join_course";
        private static final String ACTION_COURSE_LEAVE = "leave_course";
        private static final String ACTION_COURSE_CATEGORY = "list_course";
        private static final String ACTION_COURSE_SEARCH = "search_course";

        private static final String PARAM_COURSE_ID = "course_id=";
        private static final String PARAM_COURSE_SEMESTER = "semester=";
        private static final String PARAM_COURSE_NAME = "name=";
        private static final String PARAM_COURSE_TIME = "time=";

        private Course mCourse;

        public CourseTask(Context context, int type) {
            super(context, type);
        }

        public CourseTask setCourse(Course course) {
            mCourse = course;
            return this;
        }

        @Override
        protected boolean isGet() {
            switch (mType) {
                case TASK_COURSE_JOIN:
                case TASK_COURSE_LEAVE:
                    return false;

                default:
                    return true;
            }
        }

        @Override
        protected String getApi() {
            return API_COURSE;
        }

        @Override
        protected String getAction() {
            switch (mType) {
                case TASK_COURSE_JOIN:
                    return ACTION_COURSE_JOIN;

                case TASK_COURSE_LEAVE:
                    return ACTION_COURSE_LEAVE;

                case TASK_COURSE_CATEGORY:
                    return ACTION_COURSE_CATEGORY;

                case TASK_COURSE_SEARCH:
                    return ACTION_COURSE_SEARCH;

                default:
                    return null;
            }
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder();
            switch (mType) {
                case TASK_COURSE_JOIN:
                case TASK_COURSE_LEAVE:
                    sb.append(PARAM_COURSE_ID).append(mCourse == null ? 0 : mCourse.courseId);
                    break;

                case TASK_COURSE_CATEGORY:
                case TASK_COURSE_SEARCH:
                    if (mCourse != null) {
                        sb.append(PARAM_COURSE_SEMESTER).append(mCourse.semester)
                                .append("&").append(PARAM_OFFSET).append(mOffset);
                        if (TASK_COURSE_SEARCH == mType) {
                            sb.append("&").append(PARAM_COURSE_NAME)
                                    .append(Utils.urlEncode(mCourse.name))
                                    .append("&").append(PARAM_COURSE_TIME);
                            Entry entry = mCourse.getEntries().get(0);
                            if (entry.weekday != 0 && entry.periodStart != 0) {
                                sb.append(Utils.urlEncode(new StringBuilder().append(entry.weekday)
                                        .append("|").append(entry.periodStart).toString()));
                            }
                        }
                    }
                    break;

                default:
                    break;
            }
            return sb.toString();
        }

        @Override
        protected Object getResponseValue(MsResponse mr) throws Exception {
            switch (mType) {
                case TASK_COURSE_JOIN:
                case TASK_COURSE_LEAVE:
                    return mCourse;

                case TASK_COURSE_CATEGORY:
                    if (MsResponse.isSuccessful(mr)) {
                        return toCategoryList(new JSONArray(mr.response), mCourse);
                    }
                    break;

                case TASK_COURSE_SEARCH:
                    if (MsResponse.isSuccessful(mr)) {
                        return toCourseList(new JSONArray(mr.response));
                    }
                    break;

                default:
                    break;
            }
            return null;
        }
    }
}
