package com.tjut.mianliao.curriculum.search;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tjut.mianliao.BaseTask.TaskExecutionListener;
import com.tjut.mianliao.R;
import com.tjut.mianliao.curriculum.CourseManager;
import com.tjut.mianliao.curriculum.CourseUtil;
import com.tjut.mianliao.data.Course;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class CourseAdapter extends CourseCategoryAdapter implements
        OnClickListener, Observer, TaskExecutionListener {

    private CourseManager mCourseManager;
    private CourseSearchManager mSearchManager;

    private String mSearchText;

    public CourseAdapter(Context context) {
        super(context);
        mCourseManager = CourseManager.getInstance(context);
        mCourseManager.addObserver(this);
        mSearchManager = CourseSearchManager.getInstance(context);
        mSearchManager.registerListener(this);
    }

    public void destroy() {
        mCourseManager.deleteObserver(this);
        mSearchManager.unregisterListener(this);
    }

    public void setSearchText(String text) {
        mSearchText = text;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_course, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.tv_course_name);
            holder.classmates = (TextView) convertView.findViewById(R.id.tv_course_classmates);
            holder.teacher = (TextView) convertView.findViewById(R.id.tv_course_teacher);
            holder.entry = (TextView) convertView.findViewById(R.id.tv_course_entry);
            holder.action = (TextView) convertView.findViewById(R.id.tv_course_action);
            holder.progress = convertView.findViewById(R.id.pb_course_action);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Course course = getItem(position);

        int color = mContext.getResources().getColor(R.color.txt_keyword);
        holder.name.setText(Utils.getColoredText(course.name, mSearchText, color));

        holder.classmates.setText(mContext.getString(
                R.string.course_classmates_count, course.classmatesCount));

        holder.teacher.setText(Utils.getColoredText(course.teacher, mSearchText, color));

        holder.entry.setText(CourseUtil.getCourseDesc(course));

        if (course.interacting) {
            holder.progress.setVisibility(View.VISIBLE);
            holder.action.setText("");
        } else {
            holder.progress.setVisibility(View.GONE);
            int bgResId;
            int txtResId;
            int iconResId;
            if (mCourseManager.containsCourse(course.courseId)) {
                bgResId = R.drawable.selector_btn_red;
                txtResId = R.string.course_leave;
                iconResId = R.drawable.ic_course_delete;
            } else {
                bgResId = R.drawable.selector_btn_green;
                txtResId = R.string.course_join;
                iconResId = R.drawable.ic_course_add;
            }
            holder.action.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);
            holder.action.setBackgroundResource(bgResId);
            holder.action.setText(txtResId);
        }
        holder.action.setTag(course);
        holder.action.setOnClickListener(this);

        return convertView;
    }

    @Override
    public void onClick(View v) {
        Course course = (Course) v.getTag();
        if (course != null && !course.interacting
                && v.getId() == R.id.tv_course_action) {
            course.interacting = true;
            notifyDataSetChanged();
            if (mCourseManager.containsCourse(course.courseId)) {
                mSearchManager.startCourseLeaveTask(course);
            } else {
                mSearchManager.startCourseJoinTask(course);
            }
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof CourseManager) {
            notifyDataSetChanged();
        }
    }

    @Override
    public void onPreExecute(int type) {
    }

    @Override
    public void onPostExecute(int type, MsResponse mr) {
        switch (type) {
            case CourseSearchManager.TASK_COURSE_JOIN:
                if (MsResponse.isSuccessful(mr) && mr.value != null) {
                    mCourseManager.addCourse((Course) mr.value);
                }
                break;

            case CourseSearchManager.TASK_COURSE_LEAVE:
                if (MsResponse.isSuccessful(mr) && mr.value != null) {
                    mCourseManager.leaveCourse(((Course) mr.value).courseId);
                }
                break;

            default:
                break;
        }
    }

    private static class ViewHolder {
        TextView name;
        TextView classmates;
        TextView teacher;
        TextView entry;
        TextView action;
        View progress;
    }
}
