package com.tjut.mianliao.component;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.forum.TopicInfo;

public class TopicTagView extends AutoWrapLinearLayout implements OnClickListener {

    private Context mContext;
    private ArrayList<TopicInfo> mTopics;
    private LayoutInflater mInflater;
    private TagClickListener mListener;
    private boolean mClickable = true;
    private String mFlag = "";

    public TopicTagView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context.getApplicationContext();
        mTopics = new ArrayList<TopicInfo>();
        mInflater = LayoutInflater.from(context);
    }

    public void setTagClickListen (TagClickListener listen) {
        mListener = listen;
    }
    
    public void setFlag(String flag) {
        mFlag =  flag;
    }

    public void setClickables(boolean clickable) {
        this.mClickable = clickable;
    }

    public ArrayList<TopicInfo> getTags() {
        return mTopics;
    }

    public void updateView(ArrayList<TopicInfo> topics) {
        if (topics != null) {
            mTopics = topics;
            removeAllViews();
            for (TopicInfo topic : mTopics) {
                addView(getView(topic));
            }
        }
        invalidate();
    }


    public void addTag(TopicInfo topic) {
        mTopics.add(topic);
        addView(getView(topic));
    }

    public void setTagsBackgroud() {
        for (int i = 0; i < mTopics.size(); i++) {

        }
    }

    private View getView(TopicInfo topic) {
        View view = mInflater.inflate(R.layout.list_item_topic_tag, null);
        TextView tv = (TextView) view.findViewById(R.id.tv_topic);
        tv.setText("#" + topic.name + "#");
        tv.setTag(topic);
        tv.setOnClickListener(this);
        return view;
    }

    public void removeTag(TopicInfo tag) {
        mTopics.remove(tag);
        updateView(mTopics);
    }

    @Override
    public void onClick(View v) {
        TopicInfo topic = (TopicInfo) v.getTag();
        if (mListener != null) {
            mListener.onTagClick(topic);
        }
    }

    public interface TagClickListener{
        void onTagClick(TopicInfo topic);
    }
}
