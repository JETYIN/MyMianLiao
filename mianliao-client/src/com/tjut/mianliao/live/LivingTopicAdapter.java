package com.tjut.mianliao.live;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.util.Utils;

public class LivingTopicAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Topics> mTopicInfos = new ArrayList<Topics>();
    private LayoutInflater mInflater;
    private String mKeyWord;
    private boolean mIsNew;

    public LivingTopicAdapter(Context context) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setData(ArrayList<Topics> TopicInfos, boolean isNew) {
        mIsNew = isNew;
        mTopicInfos = TopicInfos;
        notifyDataSetChanged();
    }

    public void setkeyWord (String keyWord) {
        mKeyWord = keyWord;
    }

    @Override
    public int getCount() {
        return mTopicInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mTopicInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Topics mTpInfo = (Topics) getItem(position);
        if (position == 0 && mIsNew == true) {
            View view = mInflater.inflate(R.layout.list_item_new_topic, parent, false);
            TextView mTvTilte;
            mTvTilte = (TextView) view.findViewById(R.id.tv_new_title);
            mTvTilte.setText(Utils.getColoredText("#"+mTpInfo.name+"#", mKeyWord, 0XFF32BBBC));
            return view;
        }
        else {
            View view = mInflater.inflate(R.layout.item_live_topic, parent, false);
            TextView mTvTilte,mTvHotIndex;
            mTvTilte = (TextView) view.findViewById(R.id.tv_title);
            mTvHotIndex = (TextView) view.findViewById(R.id.tv_hot_index);
            mTvTilte.setText(Utils.getColoredText("#"+mTpInfo.name+"#", mKeyWord, 0XFF32BBBC));
            mTvHotIndex.setText("100");
            return view;
        }
    }
}
