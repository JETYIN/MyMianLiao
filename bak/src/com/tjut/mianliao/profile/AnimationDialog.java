package com.tjut.mianliao.profile;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.data.SignProgressInfo;

public class AnimationDialog extends Dialog implements android.view.View.OnClickListener{
    private AnimationDrawable mFirstAnim;
    private RelativeLayout RlShowGift;
    private ImageView mIvGift;
    private ImageView mIvCloose;
    private Context mContext;
    private TextView mTvNumKernel, mTvGiftName;
    private AvatarView mIvGiftContent, mIvKernelContent;
    private LinearLayout mLlKernelInfo;
    private LinearLayout mLlKernelImg, mLlGiftImg;

    public AnimationDialog(Context context) {
        this(context,R.style.ProgressDialog);
    }

    public AnimationDialog(Context context, int Theme) {
        super(context, Theme);
        mContext = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_animation_dailog);
        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        mIvGift = (ImageView) findViewById(R.id.iv_gift);
        mIvCloose = (ImageView) findViewById(R.id.iv_cloose);
        RlShowGift = (RelativeLayout) findViewById(R.id.rl_show_gift);
        mTvGiftName = (TextView) findViewById(R.id.tv_gift_name);
        mTvNumKernel = (TextView) findViewById(R.id.tv_kernel_num);
        mIvGiftContent = (AvatarView) findViewById(R.id.priv_gift);
        mIvKernelContent = (AvatarView) findViewById(R.id.priv_kernel);
        mLlKernelInfo = (LinearLayout) findViewById(R.id.ll_kernel);
        mLlKernelImg = (LinearLayout) findViewById(R.id.ll_kernel_img);
        mLlGiftImg  = (LinearLayout) findViewById(R.id.ll_gift_img);
        mIvGift.setClickable(false);
        mIvGift.setImageResource(R.anim.gift_click);
        mIvCloose.setOnClickListener(this);
        mFirstAnim = (AnimationDrawable) mIvGift.getDrawable();
        setCancelable(true);
        setCanceledOnTouchOutside(false);
    }

    public void showAnim() {
        show();
        palyAnim();
    }
    
    private void palyAnim() {
        mFirstAnim.start();
        mIvGift.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                Animation loadAnimation = AnimationUtils.loadAnimation(mContext, android.R.anim.fade_out);
                mIvGift.startAnimation(loadAnimation);  
                loadAnimation.setAnimationListener(new AnimationListener() {
                    
                    @Override
                    public void onAnimationStart(Animation animation) {}
                    
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                    
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mIvGift.setVisibility(View.GONE);
                        RlShowGift.setVisibility(View.VISIBLE);
                    }
                });
                System.out.println("---- > fadeOut" );
            }
        }, (mFirstAnim.getNumberOfFrames() + 1) * mFirstAnim.getDuration(0));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_cloose:
                 this.dismiss();
            default:
                break;
        }
    }
    
    public void setContent (SignProgressInfo mSignProgrssInfo) {
        mTvGiftName.setText(mSignProgrssInfo.rewardDescription);
        mIvGiftContent.setImage(mSignProgrssInfo.giftIcon, R.drawable.img_sign_gift_text);
        if (mSignProgrssInfo.kernelNum > 0) {
            mTvNumKernel.setText(String.valueOf(mSignProgrssInfo.kernelNum));
            mLlKernelInfo.setVisibility(View.VISIBLE);
            mLlKernelImg.setVisibility(View.VISIBLE);
        } else {
            mLlKernelInfo.setVisibility(View.GONE);
            mLlKernelImg.setVisibility(View.GONE);
        }
    }
    
}
