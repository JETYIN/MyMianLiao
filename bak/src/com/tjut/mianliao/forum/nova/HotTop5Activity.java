package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;

public class HotTop5Activity extends BaseActivity {

    public static final String EXT_HOT_TOP5_POSTS = "ext_hot_top5_posts";
    public static final String EXT_SCHOOL_NAME= "ext_school_name";
    
	private static final int[] sResFlag = new int[]{R.string.campus_hot_champion,
			R.string.campus_hot_runner_up, R.string.campus_hot_second_runner_up,
			R.string.campus_hot_fourth, R.string.campus_hot_fifth};
	private static final int[] sResIcon = new int[]{R.drawable.square_bg_pic_first,
			R.drawable.square_bg_pic_second, R.drawable.square_bg_pic_third,
			R.drawable.square_bg_pic_hot, R.drawable.square_bg_pic_hot};

	@ViewInject(R.id.ptr_top_5)
	private PullToRefreshListView mPtrHotTop5;

	private HotTop5PostAdapter mHotPostAdapter;
	
	private ArrayList<CfPost> mCfPosts;
	private String mSchoolName;

	@Override
	protected int getLayoutResID() {
		return R.layout.activity_hot_top5;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		getTitleBar().setTitle(R.string.campus_hot_top_five_title);
		mCfPosts = getIntent().getParcelableArrayListExtra(EXT_HOT_TOP5_POSTS);
		mSchoolName = getIntent().getStringExtra(EXT_SCHOOL_NAME);
		mHotPostAdapter = new HotTop5PostAdapter(this);
		mPtrHotTop5.setAdapter(mHotPostAdapter);
		mPtrHotTop5.setMode(Mode.DISABLED);
		if (mCfPosts != null) {
		    mHotPostAdapter.reset(mCfPosts);
		}
		if (mSchoolName != null && !"".equals(mSchoolName)){
		    getTitleBar().setTitle(mSchoolName + getString(R.string.campus_hot_top_5));
		} 
	}

	private class HotTop5PostAdapter extends ForumPostAdapter{

		public HotTop5PostAdapter(Activity context) {
			super(context);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			
			return view;
		}
		
		@Override
		public View updateHeaderView(View view, CfPost post, int position) {
		    View mRootView = view;
		    FrameLayout mFlHeader = (FrameLayout) view.findViewById(R.id.fl_header_content);
		    if (mFlHeader != null) {
		        if (mFlHeader.getChildCount() > 0) {
		            mFlHeader.removeViewAt(0);
		        }
		        mInflater.inflate(R.layout.item_hot_flag, mFlHeader);
		        TextView mTvFlag =  (TextView)view.findViewById(R.id.tv_flag);
	            mTvFlag.setText(sResFlag[position]);
	            mTvFlag.setCompoundDrawablesWithIntrinsicBounds(sResIcon[position], 0, 0, 0);
		    }
		    return mRootView;
		}
	}

}
