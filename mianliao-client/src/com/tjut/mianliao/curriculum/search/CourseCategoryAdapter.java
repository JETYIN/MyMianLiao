package com.tjut.mianliao.curriculum.search;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.Course;

public class CourseCategoryAdapter extends ArrayAdapter<Course> {

    protected Context mContext;
    protected LayoutInflater mInflater;

    public CourseCategoryAdapter(Context context) {
        super(context, 0);
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void append(List<Course> courseList) {
        super.addAll(courseList);
    }

    public void reset(List<Course> courseList) {
        setNotifyOnChange(false);
        clear();
        append(courseList);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.list_item_course_name, parent, false);
        } else {
            view = convertView;
        }
        Course course = getItem(position);
        ((TextView) view).setText(course.name);

        return view;
    }
}
