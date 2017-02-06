package com.tjut.mianliao.forum.nova;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.common.Constant;
import com.tjut.mianliao.component.PopupView;
import com.tjut.mianliao.component.TitleBar;
import com.tjut.mianliao.component.PopupView.OnItemClickListener;
import com.tjut.mianliao.component.PopupView.PopupItem;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.forum.RemindDialog;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.video.JCVideoPlayer;

public class FormOtherSchoolActivity extends BaseActivity implements OnClickListener {
    
    private static final int UPDATE_POST = 6;
    
    private int mSchoolId;
    private String mSchoolName;

    private TextView mTvTitle, mTvLeft;
    private ImageButton mIBtRight;
    private PopupView mTitlePopupView;
    private SquareFragment fragment;
    private boolean isCollection;
    
    private Timer mTimer;
    private TimerTask mTimerTask;
    private Handler mFreshHandler;
    private int mCount;
    private int mFreshTime;
    private static TitleBar mTitleBar; 

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_other_school;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JCVideoPlayer.isVideoFinish = true;
        Bundle bundle = this.getIntent().getExtras();
        mSchoolId = bundle.getInt(Forum.INTENT_EXTRA_SCHOOLID);
        mSchoolName = bundle.getString(Forum.INTENT_EXTRA_SCHOOLNAME);
        isCollection = bundle.getBoolean(Forum.INTENT_EXTRA_ISCLOOECTION);
        mFreshTime = Constant.getFreshTimeDelay(this);
        mTitleBar = getTitleBar();

        fragment = (SquareFragment) this.getFragmentManager().findFragmentById(R.id.frag_square);
        fragment.loadPost(mSchoolId, mSchoolName);

        mTvTitle = (TextView) this.findViewById(R.id.tv_title);
        mTvTitle.setText(mSchoolName);
        mTvTitle.setOnClickListener(this);

        mTvLeft = (TextView) this.findViewById(R.id.tv_left2);
        mTvLeft.setText("高冷重地\n好想返回");
        mTvLeft.setLines(2);
        mTvLeft.setVisibility(View.VISIBLE);

        mIBtRight = (ImageButton) this.findViewById(R.id.btn_right);
        mIBtRight.setVisibility(View.VISIBLE);
        mIBtRight.setImageResource(R.drawable.icon_more);
        mIBtRight.setOnClickListener(rightBtnClickListener);
        
        mTimer = new Timer();
        
        mFreshHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_POST:
                        Calendar cal = Calendar.getInstance();
                        mTitleBar.showRefreshRed(true);
                        break;
                    default:
                        break;
                }
            }
        };

        mTimerTask = new TimerTask() {

            @Override
            public void run() {
                new GetRefreshPostCount().executeLong();
            } 
        };

        mTimer.schedule(mTimerTask, mFreshTime, 1000);
    }
    
    public static void showRefreshRed(boolean isShow) {
        if (isShow) {
            mTitleBar.showRefreshRed(true);
        } else {
            mTitleBar.showRefreshRed(false);
        }
    }

    private void showTitlePopupMenu(View anchor) {
        if (mTitlePopupView == null) {
            mTitlePopupView = new PopupView(this);
        	if (isCollection) {
        		mTitlePopupView.setItems(R.array.cf_roam_college_popup_no, mOnItemClicklisten);
        	} else {
        		mTitlePopupView.setItems(R.array.cf_roam_college_popup, mOnItemClicklisten);
        	}
        }
        mTitlePopupView.showAsDropDown(anchor, true);
    }
    
     private OnItemClickListener mOnItemClicklisten = new OnItemClickListener() {

        @Override
        public void onItemClick(int position, PopupItem item) {
            switch (position) {
                case 0:
                    new CollectSchoolTask(mSchoolId).executeLong();
                    break;
                case 1:
                    RemindDialog dialog=new RemindDialog(FormOtherSchoolActivity.this);
                    dialog.show();

                    break;
                default:
                    break;
            }
        }
    };

    OnClickListener rightBtnClickListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            showTitlePopupMenu(mIBtRight);
        }

    };

    private class CollectSchoolTask extends MsTask {
        private int mSchoolId;

        public CollectSchoolTask(int school_id) {
            super(FormOtherSchoolActivity.this, MsRequest.SCHOOL_COLLECT);
            mSchoolId = school_id;

        }

        @Override
        protected String buildParams() {
            return new StringBuilder("school_id=").append(mSchoolId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                boolean addStatus = response.getJsonObject().optBoolean("add");
                if (addStatus) {
                    mTitlePopupView.setItems(R.array.cf_roam_college_popup_no, mOnItemClicklisten);
                } else {
                    mTitlePopupView.setItems(R.array.cf_roam_college_popup, mOnItemClicklisten);
                }
                toast(addStatus ? R.string.rc_collect_success : R.string.rc_college_collection_cancel);
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        fragment.onActivityResult(requestCode, resultCode, data);
    }
    
    private class GetRefreshPostCount extends MsTask {

        public GetRefreshPostCount() {
            super(FormOtherSchoolActivity.this, MsRequest.CF_REFRESH_COUNT);
        }
        
        @Override
        protected String buildParams() {
            return new StringBuilder("school_id=").append(mSchoolId)
                    .append("&time=").append(SquareFragment.mRefreshTime).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONObject jsonObj = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);
                mCount = jsonObj.optInt("count");
                if (mCount > 0) {
                    mFreshHandler.sendEmptyMessage(UPDATE_POST);
                }
            }
        }

    }
    
    @Override
    protected void onDestroy() {
        mTimer.cancel();
        mTimerTask = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_title:
                fragment.mPtrListView.setRefreshing(Mode.PULL_FROM_START);
                break;
            default:
                break;
        }
    }
    
    @Override
    protected void onPause() {
        if (JCVideoPlayer.isVideoFinish) {
            JCVideoPlayer.releaseAllVideos();
        }
        JCVideoPlayer.isVideoFinish = true;
        super.onPause();
    }

}
