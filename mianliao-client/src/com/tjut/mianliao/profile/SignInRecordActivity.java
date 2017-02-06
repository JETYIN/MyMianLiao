package com.tjut.mianliao.profile;

import java.util.ArrayList;

import org.json.JSONArray;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.SignRecordInfo;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class SignInRecordActivity extends BaseActivity implements OnRefreshListener2<ListView> {
    
    private PullToRefreshListView mPtrSignRedord;
    private ArrayList<SignRecordInfo> mSignRecords = new ArrayList<SignRecordInfo>();
    private boolean mRefresh;
    
    private View mViewNoContent;
    private FrameLayout mViewParent;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_sign_in_record;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(getString(R.string.prof_continuous_checkins));
        mViewNoContent = mInflater.inflate(R.layout.view_no_content, null);
        mViewParent = (FrameLayout) findViewById(R.id.view_parent);
        mPtrSignRedord = (PullToRefreshListView) findViewById(R.id.ptr_sign_record);
        mPtrSignRedord.setAdapter(mSignRecordAdapter);
        mPtrSignRedord.setMode(Mode.BOTH);
        mPtrSignRedord.setOnRefreshListener(this);
        Utils.showProgressDialog(this, R.string.prof_sign_in_wait);
        fetchSignRecord(true);
        mViewNoContent.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                reload();
            }
        });

    }
    
    private void hideNoMessage() {
        if (mViewParent != null && mViewNoContent != null) {
            mViewParent.removeView(mViewNoContent);
        }
    }
    
    private void showNoMessage() {
        if (mViewParent != null && mViewNoContent != null) {
            mViewParent.removeView(mViewNoContent);
            resetNoContentView();
            mViewParent.addView(mViewNoContent);
        }
    }
    
    private void resetNoContentView() {
        mViewNoContent.findViewById(R.id.iv_notice).setVisibility(View.VISIBLE);
        mViewNoContent.findViewById(R.id.pb_progress).setVisibility(View.GONE);
        
    }
    
    private void reload() {
        mViewNoContent.findViewById(R.id.iv_notice).setVisibility(View.INVISIBLE);
        mViewNoContent.findViewById(R.id.pb_progress).setVisibility(View.VISIBLE);
        fetchSignRecord(true);
    }
    
    private BaseAdapter mSignRecordAdapter  = new BaseAdapter() {
        
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public Object getItem(int position) {
            return mSignRecords.get(position);
        }
        
        @Override
        public int getCount() {
            return mSignRecords.size();
        }   
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_sign_record, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.mIvGiftIcon = (ProImageView) convertView.findViewById(R.id.iv_gift_icon);
                viewHolder.mTvFormToTime = (TextView) convertView.findViewById(R.id.tv_form_to_time);
                viewHolder.mTvContinueRecord = (TextView) convertView.findViewById(R.id.tv_continue_record);
                viewHolder.mTvGiftStart = (TextView) convertView.findViewById(R.id.tv_gift_start);
                viewHolder.mTvGetgiftTime = (TextView) convertView.findViewById(R.id.tv_getgift_time);
                viewHolder.mTvGiftMiddle = (TextView) convertView.findViewById(R.id.tv_gift_middle);
                viewHolder.mTvGiftEnd = (TextView) convertView.findViewById(R.id.tv_gift_end);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            SignRecordInfo mSignRecordInfo = mSignRecords.get(position);
            Picasso.with(SignInRecordActivity.this)
                .load(mSignRecordInfo.rewardIcon)
                .placeholder(R.drawable.img_sign_gift_text)
                .into(viewHolder.mIvGiftIcon);
            viewHolder.mTvFormToTime.setText(Utils.getTimeString(7,
                    mSignRecordInfo.startTime)+ "~" +  Utils.getTimeString(7, mSignRecordInfo.endTime));
            viewHolder.mTvContinueRecord.setText(getString(R.string.prof_registration_day,mSignRecordInfo.continuousDay));
            viewHolder.mTvGiftStart.setText(getString(R.string.prof_has_gain,mSignRecordInfo.rewardName));
            viewHolder.mTvGetgiftTime.setText(Utils.getTimeString(2, mSignRecordInfo.endTime));
            return convertView;
        }
    };

    private class ViewHolder {
        ProImageView mIvGiftIcon;
        TextView mTvFormToTime;
        TextView mTvContinueRecord;
        TextView mTvGiftStart;
        TextView mTvGetgiftTime;
        TextView mTvGiftMiddle;
        TextView mTvGiftEnd;
    }

    private class getSignRecordTask extends MsTask { 
        private int mOffset;

        public getSignRecordTask(int offset) {
            super(SignInRecordActivity.this, MsRequest.CHECK_IN_RECORD);
            mOffset = offset;
        }
        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).toString();
        }
        @Override
        protected void onPostExecute(MsResponse response) {
            Utils.hidePgressDialog();
            mPtrSignRedord.onRefreshComplete();
            if (response.isSuccessful()) {
                try {
                    JSONArray ja = response.getJsonArray();
                    ArrayList<SignRecordInfo> records = JsonUtil.getArray(ja, SignRecordInfo.TRANSFORMER);
                    if (mRefresh) {
                        mSignRecords.clear();
                        if (records != null && records.size() > 0) {
                            hideNoMessage();
                        } else {
                            showNoMessage();
                        }
                    }
                    mSignRecords.addAll(records);
                    mSignRecordAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                showNoMessage();
            }
        }
        
    }


    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchSignRecord(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchSignRecord(false);
    }
   
    private void fetchSignRecord(boolean refresh) {
        mRefresh = refresh;
        int size = mSignRecordAdapter.getCount();
        int offset = refresh ? 0 : size;
        new getSignRecordTask(offset).executeLong();
    }
}
