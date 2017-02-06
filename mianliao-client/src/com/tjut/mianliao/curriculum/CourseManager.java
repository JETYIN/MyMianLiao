package com.tjut.mianliao.curriculum;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Observable;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.SparseArray;

import com.tjut.mianliao.curriculum.widget.CourseWidgetHelper;
import com.tjut.mianliao.data.Course;
import com.tjut.mianliao.data.Course.Entry;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class CourseManager extends Observable {
    private static final String TAG = "CourseManager";

    public static final String TYPE_REFRESH_COURSE = "type_refresh_course";
    public static final String TYPE_UPDATE_WEEK = "type_update_week";

    private static final String SHARED_PREF_NAME = "courses";

    private static final String SP_CURRENT_SEMESTER = "current_semester";
    private static final String SP_BASE_WEEK = "base_week";
    private static final String SP_BASE_TIME = "base_time";
    private static final String SP_INIT_WEEK = "init_week";
    private static final String SP_INIT_TIME = "init_time";

    private static final long ONE_WEEK = 7 * 24 * 60 * 60 * 1000; // a week

    private static WeakReference<CourseManager> sInstanceRef;

    private Context mAppCtx;

    private SharedPreferences mSharedPrefs;

    private int mCurrentSemester;

    private int mBaseWeek;
    private long mBaseTime;
    private int mInitWeek;
    private long mInitTime;

    private SparseArray<Course> mCourses = new SparseArray<Course>();

    private CourseManager(Context appCtx) {
        mAppCtx = appCtx;
        mSharedPrefs = mAppCtx.getSharedPreferences(SHARED_PREF_NAME, 0);
        mCurrentSemester = mSharedPrefs.getInt(SP_CURRENT_SEMESTER, 0);
        mBaseWeek = mSharedPrefs.getInt(SP_BASE_WEEK, 0);
        mBaseTime = mSharedPrefs.getLong(SP_BASE_TIME, 0);
        mInitWeek = mSharedPrefs.getInt(SP_INIT_WEEK, 0);
        mInitTime = mSharedPrefs.getLong(SP_INIT_TIME, 0);

        if (mBaseWeek > 0 && mBaseTime > 0) {
            loadCourses();
        }
    }

    public int getCurrentWeek() {
        return getWeek(false);
    }

    public int getInitWeek() {
        return getWeek(true);
    }

    private int getWeek(boolean initial) {
        int baseWeek;
        long baseTime;
        if (initial) {
            if (mInitWeek == 0) {
                new AdvAsyncTask<Void, Void, MsResponse>() {
                    @Override
                    protected MsResponse doInBackground(Void... params) {
                        return HttpUtil.msGet(mAppCtx, "api/course", "current_semester", "");
                    }

                    @Override
                    protected void onPostExecute(MsResponse result) {
                        if (result.code == MsResponse.MS_SUCCESS) {
                            try {
                                mInitWeek = new JSONObject(result.response).optInt("week");
                                mInitTime = getWeekStart();
                                mSharedPrefs.edit().putLong(SP_INIT_TIME, mInitTime)
                                        .putInt(SP_INIT_WEEK, mInitWeek)
                                        .commit();
                                notifyUpdate(TYPE_UPDATE_WEEK);
                            } catch (JSONException e) {
                                Utils.logE(TAG, "Get init week error: " + e.getMessage());
                            }
                        }
                    };
                }.executeLong();
                baseWeek = mBaseWeek;
                baseTime = mBaseTime;
            } else {
                baseWeek = mInitWeek;
                baseTime = mInitTime;
            }
        } else {
            baseWeek = mBaseWeek;
            baseTime = mBaseTime;
        }

        if (baseWeek == 0) {
            return 1;
        }
        long now = System.currentTimeMillis();
        int result = (int) ((now - baseTime) / ONE_WEEK + baseWeek);
        if (result < 1) {
            result = 1;
        } else if (!initial && result > CourseUtil.MAX_WEEK) {
            result = CourseUtil.MAX_WEEK;
        }
        return result;
    }

    public static synchronized CourseManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        CourseManager cm = new CourseManager(context.getApplicationContext());
        sInstanceRef = new WeakReference<CourseManager>(cm);
        return cm;
    }

    public Course getCourse(int courseId) {
        return mCourses.get(courseId);
    }

    /**
     * @param weekDay Monday (1) to Sunday (7)
     */
    public ArrayList<Course.Entry> getEntriesFor(int weekDay) {
        int currentWeek = getCurrentWeek();
        ArrayList<Course.Entry> entries = new ArrayList<Course.Entry>();

        int size = mCourses.size();
        for (int i = 0; i < size; i++) {
            Course course = mCourses.valueAt(i);
            for (Course.Entry entry : course.getEntries()) {
                if (entry.weekday == weekDay && CourseUtil.containsWeek(entry.weeks, currentWeek)) {
                    entries.add(entry);
                }
            }
        }

        return entries;
    }

    public boolean hasOverlap(Course course, int prevCourseId) {
        int size = mCourses.size();
        for (int i = 0; i < size; i++) {
            Course c = mCourses.valueAt(i);
            if (c.courseId != prevCourseId && c.overlap(course)) {
                return true;
            }
        }
        return false;
    }

    public void setInitWeek(int week) {
        if (getInitWeek() != week) {
            mInitWeek = week;
            mInitTime = getWeekStart();
            mSharedPrefs.edit().putLong(SP_INIT_TIME, mInitTime)
                    .putInt(SP_INIT_WEEK, mInitWeek)
                    .commit();
            notifyUpdate(TYPE_UPDATE_WEEK);
        }
    }

    public void setCurrentWeek(int week) {
        if (getCurrentWeek() != week) {
            mBaseWeek = week;
            mBaseTime = getWeekStart();
            mSharedPrefs.edit().putLong(SP_BASE_TIME, mBaseTime)
                    .putInt(SP_BASE_WEEK, mBaseWeek)
                    .commit();
            notifyUpdate();
        }
    }

    private long getWeekStart() {
        Calendar cld = Calendar.getInstance();
        cld.set(Calendar.HOUR_OF_DAY, 0);
        cld.clear(Calendar.MINUTE);
        cld.clear(Calendar.SECOND);
        cld.clear(Calendar.MILLISECOND);
        cld.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return cld.getTimeInMillis();
    }

    public int getSemester() {
        return mCurrentSemester;
    }

    public void changeSemester(int semester) {
        if (mCurrentSemester != semester && semester > 0) {
            mCurrentSemester = semester;
            mSharedPrefs.edit().putInt(SP_CURRENT_SEMESTER, mCurrentSemester).commit();
            mCourses.clear();
            loadCourses();
            notifyUpdate();
        }
    }

    public void notifyUpdate() {
        notifyUpdate(null);
    }

    public void notifyUpdate(String data) {
        setChanged();
        notifyObservers(data);

        // The life cycle of widget is unpredictable, so it's better to always try to notify
        // widgets if the course is updated.
        CourseWidgetHelper.updateWidget(mAppCtx);
    }

    /**
     * @return Recent semesters.
     */
    public int[] getSemesters() {
        Calendar cld = Calendar.getInstance();
        int year = cld.get(Calendar.YEAR);
        int firstYear = year - 2;
        int lastYear = year + 2;
        int size = (lastYear - firstYear) * 2;
        int[] semesters = new int[size];
        int j = 0;
        for (int i = firstYear; i < lastYear; i++) {
            semesters[j++] = i * 100 + 1;
            semesters[j++] = i * 100 + 2;
        }
        return semesters;
    }

    /**
     * Don't modify the data directly.
     */
    public SparseArray<Course> getCourses() {
        return mCourses;
    }

    public List<Course> getDailyCourses(int weekDay) {
        ArrayList<Course> courses = new ArrayList<Course>();
        for (int i = 0; i < mCourses.size(); i++) {
            Course c = mCourses.valueAt(i);
            for (Entry entry : c.getEntries()) {
                if (entry.weekday == weekDay
                        && CourseUtil.containsWeek(entry.weeks, getInitWeek())) {
                    courses.add(c);
                    break;
                }
            }
        }
        return courses;
    }

    public boolean containsCourse(int courseId) {
        return mCourses.get(courseId) != null;
    }

    private void loadCourses() {
        for (Course course : DataHelper.loadCourses(mAppCtx, mCurrentSemester)) {
            mCourses.put(course.courseId, course);
        }
    }

    public void clearCourses() {
        mCourses.clear();
        setChanged();
    }

    public void refreshCourses() {
        notifyUpdate(TYPE_REFRESH_COURSE);
    }

    public void initCourses(int semester, int week, final Collection<Course> courses) {
        mCurrentSemester = semester;
        mBaseWeek = week;
        mInitWeek = week;
        mBaseTime = getWeekStart();
        mInitTime = mBaseTime;
        mSharedPrefs.edit().putLong(SP_BASE_TIME, mBaseTime)
                .putInt(SP_BASE_WEEK, mBaseWeek)
                .putLong(SP_INIT_TIME, mInitTime)
                .putInt(SP_INIT_WEEK, mInitWeek)
                .putInt(SP_CURRENT_SEMESTER, mCurrentSemester)
                .commit();

        if (courses != null && courses.size() > 0) {
            for (Course course : courses) {
                if (mCourses.get(course.courseId) == null) {
                    course.semester = mCurrentSemester;
                    mCourses.put(course.courseId, course);
                    DataHelper.insertCourse(mAppCtx, course);
                }
            }
        }

        setChanged();
    }

    public void addCourse(Course course) {
        addCourse(course, true);
    }

    public void addCourse(Course course, boolean notifyUpdate) {
        if (course.courseId > 0 && mCourses.get(course.courseId) == null) {
            course.semester = mCurrentSemester;
            mCourses.put(course.courseId, course);
            DataHelper.insertCourse(mAppCtx, course);
            if (notifyUpdate) {
                notifyUpdate();
            } else {
                setChanged();
            }
        }
    }

    public void leaveCourse(int courseId) {
        Course course = mCourses.get(courseId);
        if (course != null) {
            mCourses.remove(courseId);
            DataHelper.deleteCourse(mAppCtx, courseId);
            notifyUpdate();
        }
    }

    public void clear() {
        mBaseWeek = 0;
        mCurrentSemester = 0;
        mBaseTime = 0;
        mInitWeek = 0;
        mInitTime = 0;
        mSharedPrefs.edit().clear().commit();
        mCourses.clear();
        deleteObservers();
        sInstanceRef.clear();
    }
}
