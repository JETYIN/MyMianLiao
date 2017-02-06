package com.tjut.mianliao.forum.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.R;

public class SchoolListDialog extends RelativeLayout implements OnClickListener, OnRefreshListener2<ListView> {

    TextView mTitleTv, mTvProvince;
    PullToRefreshListView mListView;
    ImageView mDialogIv;
    OnRefreshListener mListener;

    public SchoolListDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.dialog_school_list, this);
        mTitleTv = (TextView) this.findViewById(R.id.school_list_dialog_title);
        mTvProvince = (TextView) findViewById(R.id.school_list_dialog_province);
        mListView = (PullToRefreshListView) this.findViewById(R.id.school_list_dialog_list);
        mDialogIv = (ImageView) this.findViewById(R.id.school_list_dialog_iv);
        mListView.setOnRefreshListener(this);
        mListView.setMode(Mode.DISABLED);
        this.setOnClickListener(this);
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }

    public void setDialogImage(int resId) {
        mDialogIv.setImageResource(resId);
    }

    public PullToRefreshListView getPullList() {
        return mListView;
    }

    public void setTitle(int textId) {
        mTitleTv.setText(textId);

    }

    public void setProvince(int textId) {
        mTvProvince.setText(textId);
    }
    
    public void setListAdatper(BaseAdapter adapter) {
        mListView.setAdapter(adapter);
    }

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        mListView.setOnItemClickListener(listener);

    }

    public ListView getListView()
    {
        return mListView.getRefreshableView();
    }

    @Override
    public void onClick(View arg0) {
        this.setVisibility(View.GONE);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        if (mListener != null) {
            mListener.onRefreshStart();
        }
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        if (mListener != null) {
            mListener.onRefreshEnd();
        }
    }

    public interface OnRefreshListener{
        void onRefreshStart();
        void onRefreshEnd();
    }
}
