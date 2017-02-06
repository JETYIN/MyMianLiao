package com.tjut.mianliao.forum;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.util.Utils;

public class TopicAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<TopicInfo> mTopicInfos = new ArrayList<TopicInfo>();
    private LayoutInflater mInflater;
    private String mKeyWord;
    private boolean mIsNew;
    
    public TopicAdapter(Context context) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
    }

   public void setData(ArrayList<TopicInfo> TopicInfos, boolean isNew) {
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
        TopicInfo mTpInfo = (TopicInfo) getItem(position);
        if (position == 0 && mIsNew == true) {
            View view = mInflater.inflate(R.layout.list_item_new_topic, parent, false);
            TextView mTvTilte;
            mTvTilte = (TextView) view.findViewById(R.id.tv_new_title);
            mTvTilte.setText(Utils.getColoredText("#"+mTpInfo.name+"#", mKeyWord, 0XFF32BBBC));
            return view;
        } 
//        else if (mTpInfo.dateType == 2 || mTpInfo.dateType == 3){
//            View view = mInflater.inflate(R.layout.list_item_topic_title, parent, false);
//            TextView mTvTopicClass;
//            mTvTopicClass = (TextView) view.findViewById(R.id.tv_topic_class);
//            if (mTpInfo.dateType == 2) {
//                mTvTopicClass.setText("热门话题");
//                mTvTopicClass.setCompoundDrawablesWithIntrinsicBounds
//                (mContext.getResources().getDrawable(R.drawable.icon_subject_hot), null, null, null);
//            } else {
//                mTvTopicClass.setText("更多话题"); 
//                mTvTopicClass.setCompoundDrawablesWithIntrinsicBounds
//                (mContext.getResources().getDrawable(R.drawable.icon_subject), null, null, null);
//            }
//            return view;
//        } 
        else {
            View view = mInflater.inflate(R.layout.list_item_topic, parent, false);
            TextView mTvTilte, mTvHotIndex;
            mTvTilte = (TextView) view.findViewById(R.id.tv_title);
            mTvHotIndex = (TextView) view.findViewById(R.id.tv_hot_index);
            String mHotNum ;
            if (mTpInfo.listOrder > 99) {
                mHotNum = "99+";
            } else {
                mHotNum = String.valueOf(mTpInfo.listOrder);
            }
            mTvHotIndex.setText("热度："+ mHotNum);
            mTvTilte.setText(Utils.getColoredText("#"+mTpInfo.name+"#", mKeyWord, 0XFF32BBBC));
            return view;
        }
    }
}
