package com.tjut.mianliao.component;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.mycollege.TagInfo;

public class TagView extends AutoWrapLinearLayout implements OnClickListener {

    private Context mContext;
    private ArrayList<TagInfo> mTags;
    private LayoutInflater mInflater;
    private ArrayList<TagClickListener> mListeners;
    private boolean mClickable = true;
    private String mFlag = "";

    public TagView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context.getApplicationContext();
        mTags = new ArrayList<TagInfo>();
        mInflater = LayoutInflater.from(context);
        mListeners = new ArrayList<TagClickListener>();
    }

    public void registerTagClickListener(TagClickListener listener) {
        if (!mListeners.contains(listener) && listener != null) {
            mListeners.add(listener);
        }
    }

    public void unrgisterTagClickListener(TagClickListener listener) {
        mListeners.remove(listener);
    }

    public void setFlag(String flag) {
        mFlag =  flag;
    }

    public void setClickables(boolean clickable) {
        this.mClickable = clickable;
    }

    public ArrayList<TagInfo> getTags() {
        return mTags;
    }

    public void updateView(ArrayList<TagInfo> tags) {
        if (tags != null) {
            mTags = tags;
            removeAllViews();
            for (TagInfo tag : mTags) {
                addView(getView(tag));
            }
        }
    }


    public void addTag(TagInfo tag) {
        mTags.add(tag);
        addView(getView(tag));
    }

    public void setTagsBackgroud() {
        for (int i = 0; i < mTags.size(); i++) {

        }
    }

    private View getView(TagInfo tag) {
        View view = mInflater.inflate(R.layout.list_item_tag, null);
        TextView tv = (TextView) view.findViewById(R.id.tv_tag);
        GradientDrawable drawable = (GradientDrawable) tv.getBackground();
        drawable.setColor((int) tag.bgColor);
        tv.setText(tag.name);
        tv.setTag(tag);
        tv.setOnClickListener(this);
        return view;
    }

    public void removeTag(TagInfo tag) {
        mTags.remove(tag);
        updateView(mTags);
    }

    @Override
    public void onClick(View v) {
        if (!mClickable) {
            Toast.makeText(mContext, R.string.mc_flag_too_much, Toast.LENGTH_SHORT).show();
            return;
        }
        TagInfo tag = (TagInfo) v.getTag();
        removeTag(tag);
        for (TagClickListener listener : mListeners) {
            listener.onTagClick(tag, mFlag);
        }
    }

    public interface TagClickListener{
        void onTagClick(TagInfo tag, String flag);
    }
}
