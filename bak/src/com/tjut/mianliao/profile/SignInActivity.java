package com.tjut.mianliao.profile;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.SignInDayView;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.CheckIn;
import com.tjut.mianliao.data.SignProgressInfo;
import com.tjut.mianliao.data.SystemInfo;
import com.tjut.mianliao.data.WeekSignInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.explore.BuyVipHomePageActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class SignInActivity extends BaseActivity implements OnClickListener {

    SignInDayView mSignInDayView;
    ClickListener mClickListener;
    ImageView mSignButton;
    TextView mTvSeqenceSign;
    private int mWeekStart, mTodayFlag;

    private boolean mIsNightMode;
    private Settings mSettings;
    private LightDialog mDialog, mNoticeDialog;
    private boolean isLoadOver = false;
    private CheckIn mCheckIn;
    private boolean isRuning = false;
    private TextView mTvSignRecord;
    private View mProgressLin1, mProgressLin2, mProgressLin3, mProgressLin4, mProgressLin5, mProgressLin6;
    private ImageView mIvIsFinish1, mIvIsFinish2, mIvIsFinish3;
    private ProImageView mIvGift1, mIvGift2, mIvGift3;
    private TextView mTvSignTitle1, mTvSignTitle2, mTvSignTitle3;
    private TextView mTvGiftName1, mTvGiftName2, mTvGiftName3;
    private int mContinuousDay;
    private ArrayList<SignProgressInfo> mSignProgressInfo;
    
    private AnimationDrawable anim;
    private AnimationDialog mAnimDialog;
    private boolean mIsShowGiftDialog = false;
    private WeekSignInfo mWeekSignInfo;
    private String mCurrentStr;
    
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_sign_in;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isRuning = true;
        mSettings = Settings.getInstance(this);
        mIsNightMode = mSettings.isNightMode();
        mAnimDialog = new AnimationDialog(this);
        getSystemInfo();
        this.getTitleBar().setTitle("签到中心");
        this.initComponents();
        Utils.showProgressDialog(this, R.string.prof_sign_in_wait);
        runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                new SignInListTask().executeLong();
                new WeekSignInfoTask().executeLong();
            }
        });
        checkDayNightUI();
    }

    private void checkDayNightUI() {
        if (mIsNightMode) {
            findViewById(R.id.sv_signin).setBackgroundResource(R.drawable.bg);
        }
    }

    private void initComponents() {
        mClickListener = new ClickListener();
        mSignInDayView = (SignInDayView) this.findViewById(R.id.sign_in_day_view);
        mSignButton = (ImageView) this.findViewById(R.id.btn_sign_in);
        mTvSignRecord = (TextView) this.findViewById(R.id.tv_sign_record);
        mProgressLin1 = this.findViewById(R.id.progress_lin1);
        mProgressLin2 = this.findViewById(R.id.progress_lin2);
        mProgressLin3 = this.findViewById(R.id.progress_lin3);
        mProgressLin4 = this.findViewById(R.id.progress_lin4);
        mProgressLin5 = this.findViewById(R.id.progress_lin5);
        mProgressLin6 = this.findViewById(R.id.progress_lin6);

        mIvIsFinish1 = (ImageView) this.findViewById(R.id.iv_progress_isfinish1);
        mIvIsFinish2 = (ImageView) this.findViewById(R.id.iv_progress_isfinish2);
        mIvIsFinish3 = (ImageView) this.findViewById(R.id.iv_progress_isfinish3);

        mTvSignTitle1 = (TextView) this.findViewById(R.id.tv_continue_title1);
        mTvSignTitle2 = (TextView) this.findViewById(R.id.tv_continue_title2);
        mTvSignTitle3 = (TextView) this.findViewById(R.id.tv_continue_title3);

        mTvGiftName1 = (TextView) this.findViewById(R.id.tv_gift_name1);
        mTvGiftName2 = (TextView) this.findViewById(R.id.tv_gift_name2);
        mTvGiftName3 = (TextView) this.findViewById(R.id.tv_gift_name3);

        mIvGift1 = (ProImageView) this.findViewById(R.id.iv_gift1);
        mIvGift2 = (ProImageView) this.findViewById(R.id.iv_gift2);
        mIvGift3 = (ProImageView) this.findViewById(R.id.iv_gift3);
        mIvGift1.setOnClickListener(this);

//        mTvSignRecord.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mSignButton.setOnClickListener(mClickListener);
        mTvSignRecord.setOnClickListener(this);
        mTvSeqenceSign = (TextView) this.findViewById(R.id.tv_seqence_sign);
    }


    private class SignInTask extends MsTask {

        public SignInTask() {
            super(SignInActivity.this, MsRequest.CHECK_IN_V2);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mIsShowGiftDialog = true;
                JSONObject jsonObj = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);
                if (jsonObj != null) {
                    mCheckIn = CheckIn.fromJson(jsonObj);
                    mWeekStart = mCheckIn.getWeek_start();
                    mSignProgressInfo = mCheckIn.mSignInfos;
                    updateProgressBar();
                    new WeekSignInfoTask().executeLong();
                }
            } else if (response.code == MsResponse.MS_TASK_SIGN_NUM_UP_TO_TOP) {
                // 已达到签到上限
                showNoticeDialog();
            }
        }
    }

    private class SignInListTask extends MsTask {

        public SignInListTask() {
            super(SignInActivity.this, MsRequest.CHECK_IN_LIST);
        }
            
        @Override
        protected void onPostExecute(MsResponse response) {
            Utils.hidePgressDialog();
            if (response.isSuccessful()) {
                JSONObject jsonObj = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);
                isLoadOver = true;
                if (jsonObj != null) {
                    mCheckIn = CheckIn.fromJson(jsonObj);
                    mWeekStart = mCheckIn.getWeek_start();
                    mSignProgressInfo = mCheckIn.mSignInfos;
                    updateProgressBar();
                }
            }
        }
    }
    
    private class WeekSignInfoTask extends MsTask {

        public WeekSignInfoTask() {
            super(SignInActivity.this, MsRequest.WEEK_CHECK_IN_INFO);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (response.isSuccessful() && response.getJsonObject() != null) {
                mWeekSignInfo = WeekSignInfo.fromJson(response.getJsonObject());
                mContinuousDay = mWeekSignInfo.continuesDay;
                mCurrentStr = mWeekSignInfo.checkInStr;
                mTodayFlag = mWeekSignInfo.currentWeekDay - 1;
                updateUI();
            } else {
                toast("信息获取失败");
            }
        }
    } 

    private void updateUI() {
        mSignInDayView.setContent(mWeekSignInfo, mClickListener);
        mTvSeqenceSign.setText("已连续签到" + mContinuousDay + "天");
        mSignButton.setImageResource(mWeekSignInfo.isSignTodday ? R.drawable.button_sign_already : R.drawable.button_sign_in);
        updateProgressBar();

    }

    private void showSignInDialog(final long time) {
        if (mDialog == null) {
            mDialog = new LightDialog(this);
            mDialog.setTitle(R.string.course_time_clear);
            mDialog.setMessage(R.string.mc_sign_in_notice).setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(SignInActivity.this, BuyVipHomePageActivity.class);
                            startActivity(intent);
                        }
                    });
            ;
        }
        mDialog.show();
    }

    private void showNoticeDialog() {
        if (mNoticeDialog == null) {
            mNoticeDialog = new LightDialog(this);
            mNoticeDialog.setTitle(R.string.course_time_clear);
            mNoticeDialog.setMessage(R.string.mc_sign_in_notice_top).setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mNoticeDialog.hide();
                        }
                    });
            ;
        }
        mNoticeDialog.show();
    }

    class ClickListener implements OnClickListener {

        @Override
        public void onClick(View arg0) {
            if (isLoadOver ) {
                if (mWeekSignInfo != null && !mWeekSignInfo.isSignTodday){
                    updateUIFirst();
                    new SignInTask().executeLong();
                } else if (mWeekSignInfo.isSignTodday){
                    toast("已签到");
                }  else {
                    toast("数据加载失败");
                }
            } else {
                getTitleBar().showProgress();
                toast("数据加载中，请稍候");
            }

        }

    }

    public boolean isVip() {
        UserInfo userInfo = UserInfoManager.getInstance(this).getUserInfo(AccountInfo.getInstance(this).getUserId());
        return userInfo.vip;
    }

    public void updateUIFirst() {
        if (mCheckIn != null) {
            mSignButton.setImageResource(R.drawable.button_sign_already);
        }
    }

    private class SystemInfoTask extends MsTask {

        public SystemInfoTask() {
            super(SignInActivity.this, MsRequest.SYSTEM_INFO);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (isRuning) {
                getTitleBar().hideProgress();
            }
            if (response.isSuccessful()) {
                SystemInfo systemInfo = SystemInfo.fromJson(response.getJsonObject());
                if (systemInfo != null) {
//                    mCheckinRule = systemInfo.checkinRule;
                }
            } else {
                toast("信息获取失败");
            }
        }
    }

    private void getSystemInfo() {
        new SystemInfoTask().executeLong();
    }

    @Override
    protected void onStop() {
        isRuning = false;
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_sign_record:
                Intent intent = new Intent(SignInActivity.this, SignInRecordActivity.class);
                startActivity(intent);
                break;
                

            default:
                break;
        }

    }

    private void updateProgressBar() {
        int mProgressHigh = this.getResources().getDimensionPixelSize(R.dimen.sign_in_progress_hight);
        if (mSignProgressInfo != null && mSignProgressInfo.size() > 2) {
            if (mSignProgressInfo.get(0).day <= 0) {
                mTvSignTitle1.setText("连续" + mSignProgressInfo.get(1).day + "天");
                mTvSignTitle2.setText("连续" + mSignProgressInfo.get(2).day + "天");
                mTvSignTitle3.setText("连续" + mSignProgressInfo.get(3).day + "天");
            } else {
                mTvSignTitle1.setText("连续" + mSignProgressInfo.get(1).day / 7 + "周");
                mTvSignTitle2.setText("连续" + mSignProgressInfo.get(2).day / 7 + "周");
                mTvSignTitle3.setText("连续" + mSignProgressInfo.get(3).day / 7 + "周");
            }

            mTvGiftName1.setText(mSignProgressInfo.get(1).giftName);
            mTvGiftName2.setText(mSignProgressInfo.get(2).giftName);
            mTvGiftName3.setText(mSignProgressInfo.get(3).giftName);
            if (mContinuousDay < mSignProgressInfo.get(1).day) {
                mProgressLin1.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, mContinuousDay -  mSignProgressInfo.get(0).day));
                mProgressLin2.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, mSignProgressInfo.get(1).day - mContinuousDay));
                mProgressLin3.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, 0));
                mProgressLin4.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, 1));
                mProgressLin5.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, 0));
                mProgressLin6.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, 1));
                mTvGiftName1.setTextColor(0XFFFFFFFF);
                mTvGiftName2.setTextColor(0XFFFFFFFF);
                mTvGiftName3.setTextColor(0XFFFFFFFF);
            } else if (mContinuousDay >= mSignProgressInfo.get(1).day && mContinuousDay < mSignProgressInfo.get(2).day) {
                mProgressLin1.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, 1));
                mProgressLin2.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, 0));
                mProgressLin3.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, mContinuousDay - mSignProgressInfo.get(1).day));
                mProgressLin4.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, mSignProgressInfo.get(2).day - mContinuousDay));
                mProgressLin5.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, 0));
                mProgressLin6.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, 1));
                mIvIsFinish1.setImageResource(R.drawable.img_sign_continue);
                mIvGift1.setBackgroundResource(R.drawable.img_sign_gift_bg);
                mTvGiftName1.setTextColor(0XFFD9A447);
                mTvGiftName2.setTextColor(0XFFFFFFFF);
                mTvGiftName3.setTextColor(0XFFFFFFFF);
                mIvGift1.setImage(mSignProgressInfo.get(1).giftIcon, R.drawable.img_sign_gift_text);
                if (mContinuousDay == mSignProgressInfo.get(1).day && mIsShowGiftDialog) {
                    mAnimDialog.setContent(mSignProgressInfo.get(1));
                    mAnimDialog.showAnim();
                    mIsShowGiftDialog = false;
                }
            } else if (mContinuousDay >= mSignProgressInfo.get(2).day && mContinuousDay <= mSignProgressInfo.get(3).day) {
                mProgressLin1.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, 1));
                mProgressLin2.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, 0));
                mProgressLin3.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, 1));
                mProgressLin4.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, 0));
                mProgressLin5.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, mContinuousDay - mSignProgressInfo.get(2).day));
                mProgressLin6.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, mSignProgressInfo.get(3).day - mContinuousDay));
                mIvIsFinish1.setImageResource(R.drawable.img_sign_continue);
                mIvGift1.setBackgroundResource(R.drawable.img_sign_gift_bg);
                mIvIsFinish2.setImageResource(R.drawable.img_sign_continue);
                mIvGift2.setBackgroundResource(R.drawable.img_sign_gift_bg);
                mTvGiftName1.setTextColor(0XFFD9A447);
                mTvGiftName2.setTextColor(0XFFD9A447);
                mTvGiftName3.setTextColor(0XFFFFFFFF);
                mIvGift2.setImage(mSignProgressInfo.get(2).giftIcon, R.drawable.img_sign_gift_text);
                if (mContinuousDay == mSignProgressInfo.get(2).day && mIsShowGiftDialog) {
                    mAnimDialog.setContent(mSignProgressInfo.get(2));
                    mAnimDialog.showAnim();
                    mIsShowGiftDialog = false;
                }
                if (mContinuousDay == mSignProgressInfo.get(3).day) {
                    mIvGift3.setBackgroundResource(R.drawable.img_sign_gift_bg);
                    mIvIsFinish3.setImageResource(R.drawable.img_sign_continue); 
                    mTvGiftName3.setTextColor(0XFFD9A447);
                    mIvGift3.setImage(mSignProgressInfo.get(3).giftIcon, R.drawable.img_sign_gift_text);
                    if (mIsShowGiftDialog) {
                        mAnimDialog.setContent(mSignProgressInfo.get(3));
                        mAnimDialog.showAnim();
                        mIsShowGiftDialog = false;
                    }
                }
                
            }

        }
    }

}
