package com.tjut.mianliao.tribe;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ExpandableGridView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.tribe.TribeTypeInfo;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.theme.ThemeTextView;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class TribeTypeHomeActivity extends BaseActivity{
   
    @ViewInject(R.id.gv_tribe_type)
    private ExpandableGridView mGvTribeType;
    
    private ArrayList<TribeTypeInfo> mTribeTypes;
    
    private TribeTypeAdapter mAdapter;
    
    private boolean mIsNightMode;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_tribe_type_home;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mIsNightMode = Settings.getInstance(this).isNightMode();
        mTribeTypes = new ArrayList<TribeTypeInfo>();
        getTitleBar().setTitle(R.string.tribe_classify);
        mAdapter = new TribeTypeAdapter();
        mGvTribeType.setAdapter(mAdapter);
        new GetTribeTypeTask().executeLong();
        checkDayNightUI();
        
    }

    private void checkDayNightUI() {
        if (mIsNightMode) {
            mGvTribeType.setBackgroundResource(R.drawable.bg);
        }
    }
    
    private class TribeTypeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mTribeTypes.size();
        }

        @Override
        public TribeTypeInfo getItem(int position) {
            return mTribeTypes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.list_item_tribe_type, parent, false);
            } else {
                view = convertView;
            }
            ProImageView mIvTribeTypeIcon = (ProImageView) view.findViewById(R.id.iv_tribe_type_icon);
            ThemeTextView mTvTribeTypeName = (ThemeTextView) view.findViewById(R.id.tv_tribe_type_name);
            TribeTypeInfo mTribeType = getItem(position);
            mIvTribeTypeIcon.setImage(mTribeType.icon, R.drawable.pic_cover_girl_mid);
            mTvTribeTypeName.setText(mTribeType.name);
            view.setTag(mTribeType);
            view.setOnClickListener(mTypeListen);
            return view;
        }
        
    }
    
    private OnClickListener mTypeListen = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            TribeTypeInfo mType = (TribeTypeInfo) v.getTag();
            Intent intent = new Intent(TribeTypeHomeActivity.this, TribeClassifyDetailActivity.class);
            intent.putExtra(TribeClassifyDetailActivity.EXT_TRIBE_TYPE_DATA, mType);
            startActivity(intent);
        }
    };
    
    private class GetTribeTypeTask extends MsTask {

        public GetTribeTypeTask() {
            super(TribeTypeHomeActivity.this, MsRequest.TRIBE_TYPES_LIST);
        }
        
        @Override
        protected void onPreExecute() {
            Utils.showProgressDialog(TribeTypeHomeActivity.this, R.string.fp_load_datas);
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            Utils.hidePgressDialog();
            if (response.isSuccessful()) {
                ArrayList<TribeTypeInfo> types = JsonUtil.getArray(
                        response.getJsonArray(), TribeTypeInfo.TRANSFORMER);
                mTribeTypes.clear();
                mTribeTypes.addAll(types);
                mAdapter.notifyDataSetChanged();
            }
        }
        
    }

}
