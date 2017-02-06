package com.tjut.mianliao.profile;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.Medal;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.umeng.analytics.MobclickAgent;

public class MyMedalsActivity extends BaseActivity implements OnClickListener{
    
    private LinearLayout mLlShowingMedal, mLlNoshowingMedal, mLlHavaData, mLlNodata;
    private TextView mTvShowing;
    private ArrayList<Medal> mMadels, mShowMadels, mNoShowMadels;
    public static final String MY_MADELS = "my_madels";
    private PrimaryMedalTask mLastTask;
    private ScrollView mSvMedal;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_mymedals;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShowMadels = new ArrayList<Medal>();
        mNoShowMadels = new ArrayList<Medal>();
        mSvMedal = (ScrollView) findViewById(R.id.sv_my_medal);
        mLlShowingMedal = (LinearLayout) findViewById(R.id.ll_showing);
        mLlNoshowingMedal = (LinearLayout) findViewById(R.id.ll_not_showing);
        mLlHavaData = (LinearLayout) findViewById(R.id.ll_hava_medails);
        mLlNodata = (LinearLayout) findViewById(R.id.ll_not_medails);
        mTvShowing = (TextView) findViewById(R.id.tv_showing);
        getTitleBar().setTitle(this.getString(R.string.prof_gain_awards));
        getTitleBar().showRightButton(R.drawable.bottom_bg_found,this);
        mMadels = getIntent().getParcelableArrayListExtra(MY_MADELS);
        if (mMadels == null || mMadels.size() == 0) {
            mLlNodata.setVisibility(View.VISIBLE);
            mLlHavaData.setVisibility(View.GONE);
        } else {
            splitMadel();
            updataView();
            mLlHavaData.setVisibility(View.VISIBLE);
            mLlNodata.setVisibility(View.GONE);
        }
    }

    private void updataView() {
        mLlShowingMedal.removeAllViews();
        mLlNoshowingMedal.removeAllViews();
        for (int i = 0; i < mShowMadels.size(); i++) {
            mLlShowingMedal.addView(getView(mShowMadels.get(i), 0));
        }
        for (int i = 0; i < mNoShowMadels.size(); i++) {
            mLlNoshowingMedal.addView(getView(mNoShowMadels.get(i), 1));
        }
        mTvShowing.setText(getString(R.string.prof_is_showing,mShowMadels.size()));
    }

    private void splitMadel() {
        for (int i = 0; i < mMadels.size(); i++) {  
            if (mMadels.get(i).isPrimary() && mShowMadels.size() <= 5) {
                mShowMadels.add(mMadels.get(i));
                for (int k = 0; k < mShowMadels.size(); k++) {
                    for (int j = 0; j < (mShowMadels.size() - (k + 1)); j++) {
                        Medal tempLeft = new Medal();
                        Medal tempRight = new Medal();
                        if (mShowMadels.get(j).primary < (mShowMadels.get(j + 1).primary)) {
                            tempLeft = mShowMadels.get(j);
                            tempRight = mShowMadels.get(j + 1);
                            mShowMadels.remove(j);
                            mShowMadels.add(j, tempRight);
                            mShowMadels.remove(j + 1);
                            mShowMadels.add(j + 1, tempLeft);
                        }
                    }
                }
                
            } else {
                mNoShowMadels.add(mMadels.get(i));
            }
        }
    }

    private View getView(Medal mMedal, int Type) {
        View view = mInflater.inflate(R.layout.list_item_my_medails, null);
        ImageView mMedalBt;
        ProImageView mMedalLogo;
        TextView mMedalName, mMedalContent;
        mMedalLogo = (ProImageView) view.findViewById(R.id.iv_medail_logo);
        mMedalBt = (ImageView) view.findViewById(R.id.iv_control_bt);
        mMedalName = (TextView) view.findViewById(R.id.tv_medail_name);
        mMedalContent = (TextView) view.findViewById(R.id.tv_medail_content);

        mMedalLogo.setImage(mMedal.imageUrl, R.drawable.ic_medal_empty);
        mMedalName.setText(mMedal.name);
        mMedalContent.setText(mMedal.description);
        if (Type == 0) {
            mMedalBt.setImageResource(R.drawable.move);
            mMedalBt.setOnClickListener(mDelClickListen);
            view.setOnClickListener(mDelClickListen);
        } else {
            mMedalBt.setImageResource(R.drawable.repeat);
            mMedalBt.setOnClickListener(mAddClickListen);
            view.setOnClickListener(mAddClickListen);
        }
        mMedalBt.setTag(mMedal);
        view.setTag(mMedal);
        return view;
    }

    OnClickListener mAddClickListen = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rl_my_medal:
                case R.id.iv_control_bt:
                    Medal mMedal = (Medal) v.getTag();
                    mMedal.primary = 1;
                    mShowMadels.add(0, mMedal);
                    mNoShowMadels.remove(mMedal);
                    if (mShowMadels.size() > 5) {
                        mNoShowMadels.add(0,mShowMadels.get(5));
                        mShowMadels.remove(5);
                    }
                    updataView();
                    break;

                default:
                    break;
            }
        }
    };

    OnClickListener mDelClickListen = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rl_my_medal:
                case R.id.iv_control_bt:
                    Medal mMedal = (Medal) v.getTag();
                    mMedal.primary = 0;
                    mNoShowMadels.add(0, mMedal);
                    mShowMadels.remove(mMedal);
                    updataView();
                    break;

                default:
                    break;
            }
        }
    };

    private class PrimaryMedalTask extends MsTask {
        private String mIds;

        public PrimaryMedalTask(String ids) {
            super(getApplicationContext(), MsRequest.MEDAL_SET_PRIMARY_BADGES);
            mIds = ids;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("badge_ids=").append(mIds).toString();
        }

        @Override
        protected void onPreExecute() {
            mLastTask = this;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mLastTask = null;
            if (!response.isSuccessful()) {
                response.showFailInfo(getRefContext(), R.string.mdl_set_tst_failed);
            } 
        }
    }
    @Override
    public void onBackPressed() {
        setResult(RESULT_UPDATED, new Intent().putParcelableArrayListExtra(MedalActivity.EXTRA_PRIMARY_MEDALS, mShowMadels));
        StringBuilder ids = new StringBuilder();
        if (mShowMadels != null) {
            for (Medal medal : mShowMadels) {
                ids.append(medal.id).append(",");
            }
            new PrimaryMedalTask(ids.toString()).executeLong();
            finish();
        } else {
            new PrimaryMedalTask("0").executeLong();
            finish();
        }
    }

    @Override
    public void onClick(View v) {
          switch (v.getId()) {
            case R.id.btn_right:
                Intent iMedal = new Intent(this, AllMedalsActivity.class);
                startActivity(iMedal);
                MobclickAgent.onEvent(this, MStaticInterface.MEDAL);
                break;
            default:
                break;
        }
    }

}