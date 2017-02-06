package com.tjut.mianliao.forum.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;

public class PlanetCollegeView extends LinearLayout implements OnClickListener {

    public interface Area {

        int West = 10;
        int East = 11;
        int North = 12;
        int South = 13;
        int Center = 14;
        
    }


    private ImageView mTipView, mStarView, mIVStatus;

    private TextView mTvName;

    private int plateId;

    private boolean isUnlocked;

    StarClickListener mStarClickListener;

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean isUnlocked) {
        this.isUnlocked = isUnlocked;
    }

    public int getPlateId() {
        return plateId;
    }

    public void setPlateId(int plateId) {
        this.plateId = plateId;
    }

    public void setStarClickListener(StarClickListener listener) {
        this.mStarClickListener = listener;
    }

    public PlanetCollegeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.planet_college, this);
//        mTipView = (ImageView) this.findViewById(R.id.iv_tips);
        mStarView = (ImageView) this.findViewById(R.id.iv_planet);

//        mIVStatus = (ImageView) this.findViewById(R.id.iv_unlock);

        mTvName = (TextView) this.findViewById(R.id.tv_planet_name);

//        mTipView.setOnClickListener(this);
        mStarView.setOnClickListener(this);

        this.deployCustomAttributes(context, attrs);
    }

    public void setTitle(int textId) {
        mTvName.setText(textId);
    }
    public void setTitleColor(int mColor) {
    	mTvName.setTextColor(mColor);
    }
    public void setTitleBackground(int mBackground) {
    	mTvName.setBackgroundResource(mBackground);
    } 
    public void setTitleSize(int mTextSize) {
    	mTvName.setTextSize(mTextSize);
    }
    public void setStatusImage(int resId) {

//        mIVStatus.setImageResource(resId);

    }
    public void setPlanImage(int mPlanRes) {
    	mStarView.setImageResource(mPlanRes);
    }
    public void setSpace() {
    	 RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mStarView.getLayoutParams();
         params.setMargins(0, -200, 0, 0);
         mStarView.setLayoutParams(params);
         requestLayout();
    }
    
    private void deployCustomAttributes(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PlantCollegeView);

        int plantImageResId = ta.getResourceId(R.styleable.PlantCollegeView_planet_star_img, 0);
        int titleBGResId = ta.getResourceId(R.styleable.PlantCollegeView_planet_title_bg_img, 0);

        float tipTextSize = ta.getDimension(R.styleable.PlantCollegeView_search_view_tip_textsize, 0);
        int unlockIconMarginLeft = ta.getDimensionPixelSize(R.styleable.PlantCollegeView_search_unlock_marginLeft, 0);
        int mPlanetTitleMagintop = ta.getDimensionPixelSize(R.styleable.PlantCollegeView_planet_title_magintop, 0);
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mIVStatus.getLayoutParams();
//        params.setMargins(unlockIconMarginLeft, 0, 0, 0);
//        mIVStatus.setLayoutParams(params);
        
        mStarView.setImageResource(plantImageResId);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mStarView.getLayoutParams();
        params.setMargins(0, mPlanetTitleMagintop, 0, 0);
        mStarView.setLayoutParams(params);

        mTvName.setTextSize(TypedValue.COMPLEX_UNIT_PX,tipTextSize);
//        mTipView.setImageResource(titleBGResId);

        ta.recycle();
    }

    public PlanetCollegeView(Context context) {
        super(context);
    }

    @Override
    public void onClick(View arg0) {
        mStarClickListener.onStartClick(plateId);
    }


    public interface StarClickListener {
        void onStartClick(int planetId);
    }

}
