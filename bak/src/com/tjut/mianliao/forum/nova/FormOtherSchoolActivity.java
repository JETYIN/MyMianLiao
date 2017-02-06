package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MainActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.PopupView;
import com.tjut.mianliao.component.PopupView.OnItemClickListener;
import com.tjut.mianliao.component.PopupView.PopupItem;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.RemindDialog;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.theme.ThemeTextView;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class FormOtherSchoolActivity extends BaseActivity {
    
    private int mSchoolId;
    private String mSchoolName;

    private ThemeTextView mTvTitle, mTvLeft;
    private ImageButton mIBtRight;
    private PopupView mTitlePopupView;
    private SquareFragment fragment;
    private boolean mIsNightMode;
    private Settings mSettings;
    private ArrayList<CfPost> mHotTop5Posts;
    private boolean isCollection;
    private int mHotPostCount;
    private LinearLayout mLlTop5Post;
    
    private LinearLayout mLlHotPosts;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_other_school;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = Settings.getInstance(this);
        mIsNightMode = mSettings.isNightMode();
        mHotTop5Posts = new ArrayList<>();
        Bundle bundle = this.getIntent().getExtras();
        mSchoolId = bundle.getInt(Forum.INTENT_EXTRA_SCHOOLID);
        mSchoolName = bundle.getString(Forum.INTENT_EXTRA_SCHOOLNAME);
        isCollection = bundle.getBoolean(Forum.INTENT_EXTRA_ISCLOOECTION);

        fragment=(SquareFragment) this.getFragmentManager().findFragmentById(R.id.frag_square);
        fragment.loadPost(mSchoolId, mSchoolName);

        mTvTitle = (ThemeTextView) this.findViewById(R.id.tv_title);
        mTvTitle.setText(mSchoolName);

        mTvLeft = (ThemeTextView) this.findViewById(R.id.tv_left2);
        mTvLeft.setText("高冷重地\n好想返回");
        mTvLeft.setLines(2);
        mTvLeft.setVisibility(View.VISIBLE);

        mIBtRight = (ImageButton) this.findViewById(R.id.btn_right);
        mIBtRight.setVisibility(View.VISIBLE);
        mIBtRight.setImageResource(R.drawable.icon_more);
        mIBtRight.setOnClickListener(rightBtnClickListener);
        checkDayNightUI();
    }

	private void checkDayNightUI() {
		if (mIsNightMode) {
            findViewById(R.id.rl_other_school).setBackgroundResource(R.drawable.bg);
            getTitleBar().setRightButtonImage(R.drawable.icon_more_black);
            mTvLeft.setTextColor(mIsNightMode ? 0XFFB95167 : Color.WHITE);
        }
	}

    private void showTitlePopupMenu(View anchor) {
        if (mTitlePopupView == null) {
            mTitlePopupView = new PopupView(this);
            if  (mIsNightMode){
            	if (isCollection) {
                    mTitlePopupView.setItems(R.array.cf_roam_college_popup_no_black, mOnItemClicklisten);
                } else {
                    mTitlePopupView.setItems(R.array.cf_roam_college_popup_black, mOnItemClicklisten);
                }
            } else {
            	if (isCollection) {
            		mTitlePopupView.setItems(R.array.cf_roam_college_popup_no, mOnItemClicklisten);
            	} else {
            		mTitlePopupView.setItems(R.array.cf_roam_college_popup, mOnItemClicklisten);
            	}
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
                    if (mIsNightMode) {
                        mTitlePopupView.setItems(R.array.cf_roam_college_popup_no_black, mOnItemClicklisten);
                    }else {
                        mTitlePopupView.setItems(R.array.cf_roam_college_popup_no, mOnItemClicklisten);
                    }
                } else {
                    if (mIsNightMode) {
                        mTitlePopupView.setItems(R.array.cf_roam_college_popup_black, mOnItemClicklisten);
                    } else {
                        mTitlePopupView.setItems(R.array.cf_roam_college_popup, mOnItemClicklisten);
                    } 
                }
                toast(addStatus ? R.string.rc_collect_success : R.string.rc_college_collection_cancel);
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        fragment.onActivityResult(requestCode, resultCode, data);
    }


}
