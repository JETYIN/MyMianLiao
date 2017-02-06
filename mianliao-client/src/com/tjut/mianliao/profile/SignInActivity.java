package com.tjut.mianliao.profile;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
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
import com.tjut.mianliao.live.MyLiveListActivity;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class SignInActivity extends BaseActivity implements OnClickListener, AnimationListener {

    SignInDayView mSignInDayView;
    ImageView mSignButton, mSignButtonFinish;
    TextView mTvSeqenceSign;
    Animation alphaAnimation;

    private LightDialog mNoticeDialog;
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

    private WeekSignInfo mWeekSignInfo;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_sign_in;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isRuning = true;
        getTitleBar().setTitle(getString(R.string.prof_sign_in_center));
        Utils.showProgressDialog(this, R.string.prof_sign_in_wait);
//        alphaAnimation = AnimationUtils.loadAnimation(this, R.anim.sign_button_anim);//加载动画资源文件  

        alphaAnimation = new AlphaAnimation( 1, 0 );
        alphaAnimation.setDuration( 500 );
        alphaAnimation.setInterpolator( new LinearInterpolator( ) );
        
        alphaAnimation.setAnimationListener(this);
        
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initComponents();
                new SignInListTask().executeLong();
                new WeekSignInfoTask().executeLong();
            }
        });
    }

    private void initComponents() {
        mSignInDayView = (SignInDayView) this.findViewById(R.id.sign_in_day_view);
        mSignButton = (ImageView) this.findViewById(R.id.btn_sign_in);
        mSignButtonFinish = (ImageView) this.findViewById(R.id.btn_sign_in_finish);
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

        // mTvSignRecord.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mSignButton.setOnClickListener(this);
        mTvSignRecord.setOnClickListener(this);
        mTvSeqenceSign = (TextView) this.findViewById(R.id.tv_seqence_sign);
        // mTvSeqenceSign.postDelayed(new Runnable() {
        //
        // @Override
        // public void run() {
        // mAnimDialog = new AnimationDialog(SignInActivity.this);
        // }
        // }, 2000);
    }

    private class SignInTask extends MsTask {

        public SignInTask() {
            super(SignInActivity.this, MsRequest.CHECK_IN_V2);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONObject jsonObj = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);
                if (jsonObj != null) {
                    mCheckIn = CheckIn.fromJson(jsonObj);
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
        	
        	if(!SignInActivity.this.isFinishing()){
        		Utils.hidePgressDialog();
        	}
            if (response.isSuccessful()) {
                JSONObject jsonObj = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);
                isLoadOver = true;
                if (jsonObj != null) {
                    mCheckIn = CheckIn.fromJson(jsonObj);
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
                updateUI();
            } else {
                toast(SignInActivity.this.getString(R.string.prof_failed_gain_message));
            }
        }
    }

    private void updateUI() {
        mSignInDayView.setContent(mWeekSignInfo, this);
        mTvSeqenceSign.setText(this.getString(R.string.prof_has_sign,mContinuousDay));
        mSignButton.setVisibility(mWeekSignInfo.isSignTodday ? View.GONE : View.VISIBLE);
        mSignButtonFinish.setVisibility(mWeekSignInfo.isSignTodday ? View.VISIBLE : View.GONE);
        updateProgressBar();

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


    public boolean isVip() {
        UserInfo userInfo = UserInfoManager.getInstance(this).getUserInfo(AccountInfo.getInstance(this).getUserId());
        return userInfo.vip;
    }

    public void updateUIFirst() {
        mSignButton.startAnimation( alphaAnimation );
//        showAlphaAnimation(mSignButton, 1.0f, 0.0f, 500);
//        mSignButtonFinish.setVisibility(View.VISIBLE);
//        showAlphaAnimation(mSignButtonFinish, 0.0f, 1.0f, 500);
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
                    // mCheckinRule = systemInfo.checkinRule;
                }
            } else {
                toast(SignInActivity.this.getString(R.string.prof_failed_gain_information));
            }
        }
    }

    private void getsysteminfo() {
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
                Intent intent = new Intent(SignInActivity.this, MyLiveListActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_sign_in:
                if (isLoadOver) {
                    if (mWeekSignInfo != null && !mWeekSignInfo.isSignTodday) {
                        updateUIFirst();
                        new SignInTask().executeLong();
                    } else {
                        toast(SignInActivity.this.getString(R.string.prof_load_failed));
                    }
                } else {
                    getTitleBar().showProgress();
                    toast(SignInActivity.this.getString(R.string.prof_wait_for_load));
                }
                break;
            case R.id.btn_sign_in_finish:
                if (mWeekSignInfo.isSignTodday) {
                    toast(SignInActivity.this.getString(R.string.prof_signed));
                }
                break;
            default:
                break;
        }

    }

    private void setTextViewData(TextView tv, int day) {
        if (day >= 14 && day % 7 == 0) {
            tv.setText(getString(R.string.prof_continuous_week, day / 7));
        } else {
            tv.setText(getString(R.string.prof_continuous_day, day));
        }
    }
    
    private int getDefaultDay(int day) {
        if (day == 0) {
            return 3;
        }
        if (day == 3) {
            return 7;
        }
        if (day == 7) {
            return 10;
        }
        if (day == 10) {
            return 14;
        }
        if (day >= 14) {
            return 7 * (day / 7 + 1 );
        }
        return day;
    }
    
    private void updateProgressBar() {
        int mProgressHigh = this.getResources().getDimensionPixelSize(R.dimen.sign_in_progress_hight);
        if (mSignProgressInfo != null && mSignProgressInfo.size() > 2) {
            setTextViewData(mTvSignTitle1, mSignProgressInfo.get(1).day);
            setTextViewData(mTvSignTitle2, mSignProgressInfo.get(2).day);
            if (mSignProgressInfo.size() >= 3) {
                setTextViewData(mTvSignTitle3, mSignProgressInfo.get(3).day);
            } else {
                setTextViewData(mTvSignTitle3, getDefaultDay(mSignProgressInfo.get(2).day));
            }

            mTvGiftName1.setText(mSignProgressInfo.get(1).giftName);
            mTvGiftName2.setText(mSignProgressInfo.get(2).giftName);
            mTvGiftName3.setText(mSignProgressInfo.get(3).giftName);
            if (mContinuousDay < mSignProgressInfo.get(1).day) {
                mProgressLin1.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, mContinuousDay
                        - mSignProgressInfo.get(0).day));
                mProgressLin2.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh,
                        mSignProgressInfo.get(1).day - mContinuousDay));
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
                mProgressLin3.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, mContinuousDay
                        - mSignProgressInfo.get(1).day));
                mProgressLin4.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh,
                        mSignProgressInfo.get(2).day - mContinuousDay));
                mProgressLin5.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, 0));
                mProgressLin6.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, 1));
                mIvIsFinish1.setImageResource(R.drawable.img_sign_continue);
                mIvGift1.setBackgroundResource(R.drawable.img_sign_gift_bg);
                mTvGiftName1.setTextColor(0XFFD9A447);
                mTvGiftName2.setTextColor(0XFFFFFFFF);
                mTvGiftName3.setTextColor(0XFFFFFFFF);
                mIvGift1.setImage(mSignProgressInfo.get(1).giftIcon, R.drawable.img_sign_gift_text);
                // if (mContinuousDay == mSignProgressInfo.get(1).day &&
                // mIsShowGiftDialog) {
                // mAnimDialog.setContent(mSignProgressInfo.get(1));
                // mAnimDialog.showAnim();
                // mIsShowGiftDialog = false;
                // }
            } else if (mContinuousDay >= mSignProgressInfo.get(2).day && mContinuousDay <= mSignProgressInfo.get(3).day) {
                mProgressLin1.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, 1));
                mProgressLin2.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, 0));
                mProgressLin3.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, 1));
                mProgressLin4.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, 0));
                mProgressLin5.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh, mContinuousDay
                        - mSignProgressInfo.get(2).day));
                mProgressLin6.setLayoutParams(new LinearLayout.LayoutParams(0, mProgressHigh,
                        mSignProgressInfo.get(3).day - mContinuousDay));
                mIvIsFinish1.setImageResource(R.drawable.img_sign_continue);
                mIvGift1.setBackgroundResource(R.drawable.img_sign_gift_bg);
                mIvIsFinish2.setImageResource(R.drawable.img_sign_continue);
                mIvGift2.setBackgroundResource(R.drawable.img_sign_gift_bg);
                mTvGiftName1.setTextColor(0XFFD9A447);
                mTvGiftName2.setTextColor(0XFFD9A447);
                mTvGiftName3.setTextColor(0XFFFFFFFF);
                mIvGift2.setImage(mSignProgressInfo.get(2).giftIcon, R.drawable.img_sign_gift_text);
                // if (mContinuousDay == mSignProgressInfo.get(2).day &&
                // mIsShowGiftDialog) {
                // mAnimDialog.setContent(mSignProgressInfo.get(2));
                // mAnimDialog.showAnim();
                // mIsShowGiftDialog = false;
                // }
                if (mContinuousDay == mSignProgressInfo.get(3).day) {
                    mIvGift3.setBackgroundResource(R.drawable.img_sign_gift_bg);
                    mIvIsFinish3.setImageResource(R.drawable.img_sign_continue);
                    mTvGiftName3.setTextColor(0XFFD9A447);
                    mIvGift3.setImage(mSignProgressInfo.get(3).giftIcon, R.drawable.img_sign_gift_text);
                    // if (mIsShowGiftDialog) {
                    // mAnimDialog.setContent(mSignProgressInfo.get(3));
                    // mAnimDialog.showAnim();
                    // mIsShowGiftDialog = false;
                    // }
                }

            }

        }
    }

    private void showAlphaAnimation(View view, float fromAlpha, float toAlpha, int duration) {
        Animation alphaAnimation = new AlphaAnimation(fromAlpha, toAlpha);
        alphaAnimation.setDuration(1000);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mSignButton.setVisibility(View.GONE);
            }
        });
        view.startAnimation(alphaAnimation);
    }

    @Override
    public void onAnimationStart(Animation animation) {
        
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        mSignButton.setVisibility(View.GONE);
        mSignButtonFinish.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        
    }

}
