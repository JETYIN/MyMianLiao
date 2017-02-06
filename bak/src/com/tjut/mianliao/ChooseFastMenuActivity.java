package com.tjut.mianliao;

import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.FastEntranceInfo;
import com.tjut.mianliao.settings.Settings;

public class ChooseFastMenuActivity extends BaseActivity implements OnItemClickListener, OnClickListener {

    public static final String EXT_ENTRANCES_INFO = "ext_entrances_info";
    
    private ArrayList<FastEntranceInfo> mAllInfos;
    private ArrayList<FastEntranceInfo> mShowingInfos;
    private ArrayList<FastEntranceInfo> mCheckedInfos;
    
    private int mCheckedCount;
    
    private ListView mLvEntrances;
    private TextView mTvDesc;
    private FastEntrancesCheckedAdapter mAdapter;
    
    private boolean mIsNightMode;
    
    private SharedPreferences mPreferences;
    
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_choose_fast_entrance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle("快捷设置");
        mPreferences = DataHelper.getSpForData(this);
        mIsNightMode = Settings.getInstance(this).isNightMode();
        mCheckedInfos = new ArrayList<>();
        mShowingInfos = getIntent().getParcelableArrayListExtra(EXT_ENTRANCES_INFO);
        getTitleBar().showRightButton(R.drawable.bottom_ok_commit, this);
        mLvEntrances = (ListView) findViewById(R.id.lv_fast_entrances);
        mTvDesc = (TextView) findViewById(R.id.tv_choose_count);
        fillDataFromRes();
        filterDatas();
        mAdapter = new FastEntrancesCheckedAdapter();
        mLvEntrances.setAdapter(mAdapter);
        mLvEntrances.setOnItemClickListener(this);
        updateChoosedInfo();
        checkDayNightUI();
        

    }
    
    private void checkDayNightUI() {
        if (mIsNightMode) {
            findViewById(R.id.mrl_choose_fast_menu).setBackgroundResource(R.drawable.bg);
        }
    }

    private void filterDatas() {
        mCheckedCount = 0;
        if (mShowingInfos != null && mShowingInfos.size() > 0) {
            for (FastEntranceInfo info : mShowingInfos) {
                for (FastEntranceInfo aInfo : mAllInfos) {
                    if (info.name.equals(aInfo.name)) {
                        aInfo.checked = true;
                        mCheckedCount++;
                        mCheckedInfos.add(aInfo);
                    }
                }
            }
        }
    }

    private void updateChoosedInfo() {
        String content = getString(R.string.fast_menu_choose_count, mCheckedCount);
        mTvDesc.setText(content);
    }
    
    private void fillDataFromRes() {
        if (mAllInfos == null) {
            mAllInfos = new ArrayList<>();
        }
        TypedArray ta = getResources().obtainTypedArray(R.array.fast_entrance_list);
        for (int i = 0; i < ta.length(); i += 4) {
            FastEntranceInfo info = new FastEntranceInfo();
            info.name = ta.getString(i);
            info.imgRes = ta.getResourceId(i + 1, 0);
            info.imgResBig = ta.getResourceId(i + 2, 0);
            info.className = ta.getString(i + 3);
            mAllInfos.add(info);
        }
        ta.recycle();
    }

    private boolean toggle(FastEntranceInfo info) {
        info.checked = !info.checked;
        if (info.checked) {
            if (mCheckedCount < 4) {
                mCheckedCount++;
                mCheckedInfos.add(info);
            } else {
                toast("您最多可以选择4个!");
                info.checked = !info.checked;
                return false;
            }
        } else {
            if (mCheckedInfos.contains(info)) {
                mCheckedCount--;
                mCheckedInfos.remove(info);
            }
        }
        updateChoosedInfo();
        return true;
    }
    
    private class FastEntrancesCheckedAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mAllInfos.size();
        }

        @Override
        public FastEntranceInfo getItem(int position) {
            return mAllInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.list_item_checkable_fast_entrance, parent, false);
            } else {
                view = convertView;
            }
            FastEntranceInfo info = getItem(position);
            TextView tvName = (TextView) view.findViewById(R.id.tv_fast_name);
            tvName.setCompoundDrawablesWithIntrinsicBounds(info.imgRes, 0, 0, 0);
            tvName.setText(info.name);
            if (mIsNightMode) {
                tvName.setTextColor(0xffc7c6c6);
            }
            ((CheckBox) view.findViewById(R.id.cb_check)).setChecked(info.checked);
            return view;
        }
        
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FastEntranceInfo info = mAllInfos.get(position);
        if (info != null) {
            if (toggle(info)) {
                ((CheckBox) view.findViewById(R.id.cb_check)).setChecked(info.checked);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_right:
                saveDataAndQuit();
                break;

            default:
                break;
        }
    }

    private void saveDataAndQuit() {
        saveStatus();
        // save data to db
        new SaveDataTask(mCheckedInfos).execute();
        // quit
        Intent data = new Intent();
        data.putExtra(EXT_ENTRANCES_INFO, mCheckedInfos);
        setResult(RESULT_OK, data);
        finish();
    }

    private void saveStatus() {
        boolean setMenu = mPreferences.getBoolean(FastEntranceInfo.SP_SET_FAST_MENU, false);
        if (setMenu) {
            return;
        }
        Editor edit = mPreferences.edit();
        edit.putBoolean(FastEntranceInfo.SP_SET_FAST_MENU, true);
        edit.commit();
    }

    private class SaveDataTask extends AsyncTask<Void, Void, Void>{

        private ArrayList<FastEntranceInfo> mInfos;
        
        public SaveDataTask(ArrayList<FastEntranceInfo> infos) {
            mInfos = infos;
        }
        
        @Override
        protected Void doInBackground(Void... params) {
            DataHelper.deleteFastEntranceInfo(ChooseFastMenuActivity.this);
            DataHelper.insertFastEntranceInfos(ChooseFastMenuActivity.this, mInfos);
            return null;
        }
        
    }
}
