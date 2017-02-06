package com.tjut.mianliao.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.SignProgressInfo;
import com.tjut.mianliao.data.WeekSignInfo;

public class SignInDayView extends LinearLayout {

    private int[] mLlSignInfoIds = { R.id.ll_sign_info1, R.id.ll_sign_info2, R.id.ll_sign_info3, R.id.ll_sign_info4,
            R.id.ll_sign_info5, R.id.ll_sign_info6, R.id.ll_sign_info7 };

    private int[] mGetKernelTvIds = { R.id.tv_get_kernel1, R.id.tv_get_kernel2, R.id.tv_get_kernel3,
            R.id.tv_get_kernel4, R.id.tv_get_kernel5, R.id.tv_get_kernel6, R.id.tv_get_kernel7 };
    
    private int[] mContinueIvIds = {R.id.iv_is_continue1, R.id.iv_is_continue2, R.id.iv_is_continue3, 
            R.id.iv_is_continue4, R.id.iv_is_continue5, R.id.iv_is_continue6, R.id.iv_is_continue7};
    
    private int[] mIsSignIvIds = {R.id.iv_is_sign1, R.id.iv_is_sign2, R.id.iv_is_sign3,
            R.id.iv_is_sign4, R.id.iv_is_sign5, R.id.iv_is_sign6, R.id.iv_is_sign7};
    
    private int[] mIsTodayIvIds = {R.id.iv_today_signin1, R.id.iv_today_signin2, R.id.iv_today_signin3,
            R.id.iv_today_signin4, R.id.iv_today_signin5, R.id.iv_today_signin6, R.id.iv_today_signin7};
    
    private int[] mIvGiftIds = {R.id.iv_gift_1, R.id.iv_gift_2, R.id.iv_gift_3, 
            R.id.iv_gift_4, R.id.iv_gift_5, R.id.iv_gift_6, R.id.iv_gift_7};

    private LayoutInflater mLayoutInflater;

    private WeekSignInfo mCurrentCheckIn;
    
    public SignInDayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mLayoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflate(context, R.layout.sign_in_day_view, this);
    }


    private void removeFillLayout(int index) {
        LinearLayout subLayout = (LinearLayout) mLayoutInflater.inflate(R.layout.sign_in_view_sub_layout, null);
        subLayout.removeAllViews();
    }           

    private void removeFillButton(int index) {
        // RelativeLayout layout = (RelativeLayout)
        // findViewById(mContentLayoutIds[index]);
        // layout.removeAllViews();
    }

    private void fillWithLayout(int index, SignProgressInfo progressInfo, boolean isContinue) {

        LinearLayout mLlSignInfo = (LinearLayout) findViewById(mLlSignInfoIds[index]);
        TextView mTvGetKernel = (TextView) findViewById(mGetKernelTvIds[index]);
        ImageView mIvContinue = (ImageView) findViewById(mContinueIvIds[index]);
        ImageView mIvIsSign = (ImageView) findViewById(mIsSignIvIds[index]);
        ImageView mIvIsToday = (ImageView) findViewById(mIsTodayIvIds[index]);
        ProImageView mIvGift = (ProImageView) findViewById(mIvGiftIds[index]);
        if (progressInfo != null) {
            mTvGetKernel.setText(progressInfo.rewardDescription);
        }
        mTvGetKernel.setVisibility(View.VISIBLE);
        mLlSignInfo.setBackgroundResource(R.drawable.img_bg_sign);
        if (isContinue) {
            mIvContinue.setImageResource(R.drawable.img_bg_sign_continue);
            mIvContinue.setVisibility(View.VISIBLE);
        } else {
            mIvContinue.setImageResource(R.drawable.img_bg_sign_uncontinue);
            mIvContinue.setVisibility(View.VISIBLE);
        }
        if (progressInfo != null) {
            mIvGift.setImage(progressInfo.giftIcon, R.drawable.img_sign_kernel);
        } else {
            mIvGift.setImageResource(R.drawable.img_sign_kernel);
        }
        mIvIsSign.setVisibility(View.VISIBLE);
        mIvIsToday.setVisibility(View.GONE);

    }

    private void fillWithBlack(int index, SignProgressInfo progressInfo) {
        LinearLayout mLlSignInfo = (LinearLayout) findViewById(mLlSignInfoIds[index]);
        TextView mTvGetKernel = (TextView) findViewById(mGetKernelTvIds[index]);
        ImageView mIvContinue = (ImageView) findViewById(mContinueIvIds[index]);
        ImageView mIvIsSign = (ImageView) findViewById(mIsSignIvIds[index]);
        ProImageView mIvGift = (ProImageView) findViewById(mIvGiftIds[index]);
        mTvGetKernel.setVisibility(View.INVISIBLE);
        mIvContinue.setVisibility(View.VISIBLE);
        mLlSignInfo.setBackgroundResource(R.drawable.img_bg_sign_no);
        mIvContinue.setImageResource(R.drawable.img_bg_sign_uncontinue);
        if (progressInfo != null) {
            mIvGift.setImage(progressInfo.giftIcon, R.drawable.img_sign_kernel);
        } else {
            mIvGift.setImageResource(R.drawable.img_sign_kernel);
        }
        mIvIsSign.setVisibility(View.GONE);
    }
    
    private void fillWithNull(int index, SignProgressInfo progressInfo, boolean mIsToday) {
        LinearLayout mLlSignInfo = (LinearLayout) findViewById(mLlSignInfoIds[index]);
        TextView mTvGetKernel = (TextView) findViewById(mGetKernelTvIds[index]);
        ImageView mIvContinue = (ImageView) findViewById(mContinueIvIds[index]);
        ImageView mIvIsSign = (ImageView) findViewById(mIsSignIvIds[index]);
        ImageView mIvIsToday = (ImageView) findViewById(mIsTodayIvIds[index]);
        ProImageView mIvGift = (ProImageView) findViewById(mIvGiftIds[index]);
        mTvGetKernel.setVisibility(View.INVISIBLE);
        mLlSignInfo.setBackgroundResource(R.drawable.img_bg_sign);
        mIvContinue.setVisibility(View.GONE);
        mIvIsSign.setVisibility(View.GONE);
        if (mIsToday) {
            mIvIsToday.setVisibility(View.VISIBLE);
        } else {
            mIvIsToday.setVisibility(View.GONE); 
        }
        if (progressInfo != null) {
            mIvGift.setImage(progressInfo.giftIcon, R.drawable.img_sign_kernel);
        } else {
            mIvGift.setImageResource(R.drawable.img_sign_kernel);
        }

    }

    public void setContent(WeekSignInfo weekSignInfo, OnClickListener listener) {

        mCurrentCheckIn = weekSignInfo;
       
        int todayNum = mCurrentCheckIn.currentWeekDay - 1; 

        String contentStr = mCurrentCheckIn.checkInStr;
        boolean isContinue = false;
        boolean isToday = false;
        int kernelNum;
        int continueNum = 0;
        SignProgressInfo mProgressInfo = null;
        for (int i = 0; i < 7; i++) {
            if (i == todayNum) {
                isToday = true;
                
            } else {
                isToday = false;
            }
            this.removeFillButton(i);
            this.removeFillLayout(i);
            if (mCurrentCheckIn.ProgressInfos != null && mCurrentCheckIn.ProgressInfos.size() > i){
                mProgressInfo = mCurrentCheckIn.ProgressInfos.get(continueNum);
            } else {
                kernelNum = 1; 
            }
            if (contentStr.charAt(i) == '1') {
                isContinue = true; 
                this.fillWithLayout(i, mProgressInfo, isContinue);
                continueNum++;
            } else if (contentStr.charAt(i) == '0') {
                this.fillWithBlack(i, mProgressInfo);
                continueNum = 0;
            } else if (contentStr.charAt(i) == '2') {
                this.fillWithNull(i, mProgressInfo, isToday);
                continueNum++;
            }
        }

    }
}
