package com.tjut.mianliao.mycollege;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.mycollege.TagInfo;

public class TagAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    private ArrayList<TagInfo> mChosedTags;

    public TagAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mChosedTags = new ArrayList<TagInfo>();
    }

    public void setTags(ArrayList<TagInfo> tags) {
        mChosedTags.clear();
        mChosedTags.addAll(tags);
    }

    public void remove(TagInfo tag) {
        mChosedTags.remove(tag);
    }

    public void add(TagInfo tag) {
        mChosedTags.add(tag);
    }

    @Override
    public int getCount() {
        return mChosedTags.size();
    }

    @Override
    public TagInfo getItem(int position) {
        return mChosedTags.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(R.layout.list_item_tag, parent, false);
        }
        TagInfo tag = getItem(position);
        TextView tv = (TextView) view.findViewById(R.id.tv_tag);
        tv.setText("#" + tag.name);
        if (!tag.isSelected) {
            tv.setTextColor(Color.RED);
            tv.setBackgroundResource(0);
        } else {
            tv.setTextColor(Color.WHITE);
            tv.setBackgroundColor(Color.RED);
        }
        return view;
    }

}