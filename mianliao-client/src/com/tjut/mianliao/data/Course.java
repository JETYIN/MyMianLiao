package com.tjut.mianliao.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.tjut.mianliao.curriculum.cell.CellLayout.Cell;
import com.tjut.mianliao.util.Utils;

public class Course implements Parcelable {
    private static final String TAG = "Course";
    public static final String INTENT_EXTRA_NAME = TAG;

    public static final String TABLE_NAME = "course";

    public static final String COURSE_ID = "course_id";
    public static final String SEMESTER = "semester";
    public static final String AUTHED = "authed"; // True if it is a official course.
    public static final String AUTH_USER = "auth_user"; // True if it's not a "extra" course.
    public static final String NAME = "name";
    public static final String TEACHER = "teacher";
    public static final String CLASSMATES_COUNT = "classmates_count";
    public static final String ENTRIES = "entries";

    private ArrayList<Entry> mEntries = new ArrayList<Entry>();

    public int courseId;
    public int semester;
    public boolean authed;
    public boolean authUser;
    public String name;
    public String teacher;

    public int classmatesCount;
    public boolean interacting;

    public static final Course fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        Course course = new Course();
        course.courseId = json.optInt("id");
        course.semester = json.optInt(SEMESTER);
        course.authed = json.optInt(AUTHED) == 1;
        course.authUser = json.optInt(AUTH_USER) == 1;
        course.name = json.optString(NAME);
        course.teacher = json.optString(TEACHER);
        course.classmatesCount = json.optInt(CLASSMATES_COUNT);
        JSONArray ja = json.optJSONArray(ENTRIES);
        int length = ja.length();
        for (int i = 0; i < length; i++) {
            JSONObject jo = ja.optJSONObject(i);
            if (jo != null) {
                Entry entry = new Entry();
                entry.classroom = jo.optString(Entry.CLASSROOM);
                entry.weeks = jo.optInt(Entry.WEEKS);
                entry.weekday = jo.optInt(Entry.WEEKDAY);
                entry.periodStart = jo.optInt(Entry.P1);
                entry.periodEnd = jo.optInt(Entry.P2);
                course.addEntry(entry);
            }
        }
        return course;
    }

    private Course() {
    }

    /**
     * Make a deep copy of the origin course.
     */
    public static Course copy(Course course) {
        Course c = new Course();
        c.courseId = course.courseId;
        c.semester = course.semester;
        c.name = course.name;
        c.teacher = course.teacher;
        c.classmatesCount = course.classmatesCount;

        for (Entry entry : course.getEntries()) {
            c.addEntry(Entry.copy(entry));
        }

        return c;
    }

    public Course(int semester) {
        this(0, semester, null, null, null, 0, 0, 0, 0);
    }

    public Course(int semester, String name) {
        this(0, semester, name, null, null, 0, 0, 0, 0);
    }

    public Course(int semester, int weekDay, int periodStart, int periodEnd) {
        this(0, semester, null, null, null, weekDay, periodStart, periodEnd, 0);
    }

    public Course(int courseId, int semester, String name, String teacher, String classroom,
            int weekDay, int periodStart, int periodEnd, int weeks) {
        this.courseId = courseId;
        this.semester = semester;
        this.name = name;
        this.teacher = teacher;
        addEntry(new Entry(classroom, weeks, weekDay, periodStart, periodEnd));
    }

    public void addEntry(Entry entry) {
        if (!mEntries.contains(entry)) {
            mEntries.add(entry);
            entry.mCourse = this;
        }
    }

    public ArrayList<Entry> getEntries() {
        // Make sure there's at least one entry
        if (mEntries.size() == 0) {
            mEntries.add(new Entry());
        }
        return mEntries;
    }

    public void removeEntry(Entry entry) {
        mEntries.remove(entry);
        entry.mCourse = null;
    }

    public boolean overlap(Course course) {
        for (Entry entry : course.getEntries()) {
            for (Entry entry2 : mEntries) {
                if (entry.overlap(entry2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public JSONObject toJson() {
        JSONObject jo = new JSONObject();
        try {
            jo.put(SEMESTER, semester);
            jo.put(NAME, name);
            jo.put(TEACHER, teacher);
            jo.put(CLASSMATES_COUNT, classmatesCount);
            JSONArray ja = new JSONArray();
            for (Entry entry : getEntries()) {
                JSONObject joEntry = new JSONObject();
                joEntry.put(Entry.CLASSROOM, entry.classroom);
                joEntry.put(Entry.WEEKS, entry.weeks);
                joEntry.put(Entry.WEEKDAY, entry.weekday);
                joEntry.put(Entry.P1, entry.periodStart);
                joEntry.put(Entry.P2, entry.periodEnd);
                ja.put(joEntry);
            }
            jo.put(ENTRIES, ja);
        } catch (JSONException e) {
            Utils.logW(TAG, "Error formatting course json: " + e.getMessage());
        }

        return jo;
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    /**
     * Only compares the content of the course.
     */
    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Course) {
            Course course = (Course) o;
            if (semester == course.semester && TextUtils.equals(name, course.name)
                    && TextUtils.equals(teacher, course.teacher)) {
                if (mEntries.size() == course.getEntries().size()) {
                    Collections.sort(mEntries, mEntryComparator);
                    Collections.sort(course.getEntries(), mEntryComparator);
                    int size = mEntries.size();
                    for (int i = 0; i < size; i++) {
                        if (!mEntries.get(i).equals(course.getEntries().get(i))) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Course(Parcel in) {
        this.courseId = in.readInt();
        this.semester = in.readInt();
        this.name = in.readString();
        this.teacher = in.readString();
        this.authed = in.readInt() == 1;
        this.authUser = in.readInt() == 1;
        this.classmatesCount = in.readInt();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            addEntry(new Entry(in.readString(),
                    in.readInt(), in.readInt(), in.readInt(), in.readInt()));
        }
    }

    public static final Parcelable.Creator<Course> CREATOR = new Parcelable.Creator<Course>() {
        @Override
        public Course createFromParcel(Parcel in) {
            return new Course(in);
        }

        @Override
        public Course[] newArray(int size) {
            return new Course[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(courseId);
        dest.writeInt(semester);
        dest.writeString(name);
        dest.writeString(teacher);
        dest.writeInt(authed ? 1 : 0);
        dest.writeInt(authUser ? 1 : 0);
        dest.writeInt(classmatesCount);
        dest.writeInt(mEntries.size());
        for (Entry entry : mEntries) {
            dest.writeString(entry.classroom);
            dest.writeInt(entry.weeks);
            dest.writeInt(entry.weekday);
            dest.writeInt(entry.periodStart);
            dest.writeInt(entry.periodEnd);
        }
    }

    private Comparator<Entry> mEntryComparator = new Comparator<Entry>() {
        @Override
        public int compare(Entry lhs, Entry rhs) {
            if (lhs.weeks != rhs.weeks) {
                return lhs.weeks > rhs.weeks ? 1 : -1;
            }
            if (lhs.weekday != rhs.weekday) {
                return lhs.weekday > rhs.weekday ? 1 : -1;
            }
            if (lhs.periodStart != rhs.periodStart) {
                return lhs.periodStart > rhs.periodStart ? 1 : -1;
            }
            if (lhs.periodEnd != rhs.periodEnd) {
                return lhs.periodEnd > rhs.periodEnd ? 1 : -1;
            }

            return 0;
        }
    };

    public static class Entry {
        public static final String CLASSROOM = "classroom";
        public static final String WEEKDAY = "weekday";
        public static final String PERIOD_START = "period_start";
        public static final String PERIOD_END = "period_end";
        public static final String WEEKS = "weeks";
        public static final String P1 = "p1";
        public static final String P2 = "p2";

        private Course mCourse;

        public String classroom;
        public int weeks;
        public int weekday;
        public int periodStart;
        public int periodEnd;

        public Entry() {}

        public Entry(String classroom, int weeks, int weekday, int periodStart, int periodEnd) {
            this.classroom = classroom;
            this.weeks = weeks;
            this.weekday = weekday;
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
        }

        public static Entry copy(Entry entry) {
            return new Entry(entry.classroom,
                    entry.weeks, entry.weekday, entry.periodStart, entry.periodEnd);
        }

        public Course getCourse() {
            return mCourse;
        }

        public boolean overlap(Cell cell) {
            return weekday == cell.col
                    && ((cell.rowStart >= periodStart && cell.rowStart <= periodEnd)
                    || (cell.rowEnd >= periodStart && cell.rowEnd <= periodEnd));
        }

        public boolean overlap(Entry entry) {
            return (weeks & entry.weeks) > 0
                    && weekday == entry.weekday
                    && (getPeriodBi() & entry.getPeriodBi()) > 0;
        }

        /**
         * Get binary representation of period for overlap comparing.
         */
        public int getPeriodBi() {
            return ((1 << periodEnd) - 1) ^ ((1 << periodStart - 1) - 1);
        }

        @Override
        public int hashCode() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof Entry) {
                Entry entry = (Entry) o;
                return TextUtils.equals(classroom, entry.classroom)
                        && weeks == entry.weeks
                        && weekday == entry.weekday
                        && periodStart == entry.periodStart
                        && periodEnd == entry.periodEnd;
            }
            return false;
        }

        public String getDesc() {
            return mCourse.name + " @" + classroom;
        }
    }
}
