package com.tjut.mianliao.component.nova;

import com.tjut.mianliao.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;

public class SuperVoteOption extends LinearLayout{
    
    private VoteOption mVoteOption;
    private int mOvalMargin;
    private TextView mTvProgress;
    private ImageView mIvMyChoice;
    

    public SuperVoteOption(Context context) {
        super(context);
        
        mVoteOption = new VoteOption(context);
        init(context);
    }
    
    public SuperVoteOption(Context context, AttributeSet attrs) {
        super(context, attrs);
        mVoteOption = new VoteOption(context);
        init(context);
    }
    
    public void setContent(String content) {
        mVoteOption.setContent(content);
    }

    public void setColor(int color) {
       mVoteOption.setColor(color);
       mIvMyChoice.setBackgroundColor(color);
    }
    
    public void setCircleColor(int color) {
        mVoteOption.setCircleColor(color);
     }

    public void setProgress(float progress) {
       mVoteOption.setProgress(progress);
       progress = Math.max(0, Math.min(100, progress));
       mTvProgress.setText(String.format("%.0f%%", progress));
    }

    public void setProgressTextColor(int color) {
        mVoteOption.setProgressTextColor(color);
    }

    public void setProgressShown(boolean shown) {
        mVoteOption.setProgressShown(shown);
        if (shown) {
            mTvProgress.setVisibility(View.VISIBLE);
        } else {
            mTvProgress.setVisibility(View.GONE);
        }
    }
    
    public void setLineShow (boolean show) {
        if (show) {
            mIvMyChoice.setVisibility(View.VISIBLE);
        } else {
            mIvMyChoice.setVisibility(View.INVISIBLE);
        }
    }
    
    
    @SuppressLint("NewApi")
    private void init(Context context) {
        setOrientation(LinearLayout.VERTICAL);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int contentHigh = context.getResources().getDimensionPixelSize(R.dimen.vote_content_high);
        int tvProgressHigh = context.getResources().getDimensionPixelSize(R.dimen.vote_progress_high);
        int markWidth = context.getResources().getDimensionPixelSize(R.dimen.vote_mark_width);
        int markHigh = context.getResources().getDimensionPixelSize(R.dimen.vote_mark_high);
        
        mOvalMargin = (int) (4 * metrics.density);
        LayoutParams params = new LayoutParams(contentHigh, contentHigh);
        addView(mVoteOption, params);
        
        mTvProgress = new TextView(context);
        LayoutParams mTvParams = new LayoutParams(LayoutParams.MATCH_PARENT, tvProgressHigh);
        mTvProgress.setGravity(Gravity.CENTER);
        mTvProgress.setTextColor(0XFFC0C0C0);
        mTvProgress.setTextSize(9);
        mTvProgress.setText("0%");
        addView(mTvProgress, mTvParams); 
        
        LayoutParams mIvParams = new LayoutParams(markWidth, markHigh);
        mIvParams.gravity =  Gravity.CENTER;
        mIvMyChoice = new ImageView(context);
        addView(mIvMyChoice, mIvParams);
        
    }

}
