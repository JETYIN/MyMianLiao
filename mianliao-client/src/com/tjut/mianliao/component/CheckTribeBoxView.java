package com.tjut.mianliao.component;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.BannedInfo;

public class CheckTribeBoxView extends FrameLayout implements OnItemClickListener {

    private LayoutInflater mInflater;
    
    private ListView mListView;
    
    private CheckbleAdapter mAdapter;
    
    private ArrayList<BannedInfo> mBannedInfos;
    
    public CheckTribeBoxView(Context context) {
        this(context, null);
    }

    public CheckTribeBoxView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckTribeBoxView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public View getView(ArrayList<BannedInfo> tribeInfos) {
        if (tribeInfos == null) {
            return this;
        }
        mBannedInfos = tribeInfos;
        mAdapter.notifyDataSetChanged();
        return this;
    }
    
    
    private void init(Context context) {
        mInflater = LayoutInflater.from(context);
        mInflater.inflate(R.layout.lv_checkbox_view, this);
        mListView = (ListView) findViewById(R.id.lv_checkbel);
        mBannedInfos = new ArrayList<>();
        mAdapter = new CheckbleAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }
    
    public String getCheckedIds() {
        ArrayList<BannedInfo> tribeInfos = new ArrayList<>();
        for (BannedInfo info : mBannedInfos) {
            if (info.checked)
                tribeInfos.add(info);
        }
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (BannedInfo info : tribeInfos) {
            if (isFirst) {
                sb.append(info.tribeId);
                isFirst = false;
            } else {
                sb.append(",").append(info.tribeId);
            }
        }
        return sb.toString();
    }
    
    public String getUnCheckedIds() {
        ArrayList<BannedInfo> tribeInfos = new ArrayList<>();
        for (BannedInfo info : mBannedInfos) {
            if (!info.checked)
                tribeInfos.add(info);
        }
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (BannedInfo info : tribeInfos) {
            if (isFirst) {
                sb.append(info.tribeId);
                isFirst = false;
            } else {
                sb.append(",").append(info.tribeId);
            }
        }
        return sb.toString();
    }
    
    public ArrayList<BannedInfo> getBannedInfos() {
        return mBannedInfos;
    }
    
    private class CheckbleAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mBannedInfos.size();
        }

        @Override
        public BannedInfo getItem(int position) {
            return mBannedInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_checkble_view, parent, false);
            }
            BannedInfo info = getItem(position);
            TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
            tvName.setText(info.tribeName);
            ((CheckBox) convertView.findViewById(R.id.cb_check)).setChecked(info.checked);
            return convertView;
        }
        
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        toggle(mBannedInfos.get(position));
    }

    private void toggle(BannedInfo tribeInfo) {
        tribeInfo.checked = !tribeInfo.checked;
        mAdapter.notifyDataSetChanged();
    }
    
}
