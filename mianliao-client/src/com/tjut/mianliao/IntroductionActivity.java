package com.tjut.mianliao;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.forum.PostStreamTabFragment;
import com.tjut.mianliao.forum.RoamCollegeView;
import com.tjut.mianliao.login.LoginRegistActivity;
import com.tjut.mianliao.util.Utils;

public class IntroductionActivity extends Activity implements Callback,
		OnClickListener {

	public static final String PREF_NAME = "introduction";
	public static final String PREF_KEY = "introduction_version";

	private static final String TAG = "IntroductionActivity";

	private ViewPager mViewPager;
	private TextView mTvVersion;
	private PageAdapter mAdapter = new PageAdapter();

	private SharedPreferences mPreferences;
	private VideoView mVideoView;
	private RelativeLayout mRlVideo;
	private SurfaceHolder mSurfaceHolder;
    private PackageInfo mPackageInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPreferences = DataHelper.getSpForData(this);
		setContentView(R.layout.activity_introduction);
		mTvVersion = (TextView) findViewById(R.id.tv_version);
		mViewPager = (ViewPager) findViewById(R.id.vp_introduction);
		mVideoView = (VideoView) findViewById(R.id.video_introduction);
		mRlVideo = (RelativeLayout) findViewById(R.id.rl_video);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setPageMargin(0);
		mViewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
		mPackageInfo = Utils.getPackageInfo(this);
        if (mPackageInfo != null) {
            mTvVersion.setText(getString(R.string.ml_version_desc, mPackageInfo.versionName));
        }
		resetFirstGuidStatus();
		SurfaceHolder holder = mVideoView.getHolder();
		holder.addCallback(this); // holder加入回调接口
		// setType必须设置，要不出错.
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	protected void onResume() {
		Uri uri = Uri.parse("android.resource://" + getPackageName() + "/"
				+ R.raw.introduction_video);
		if (uri != null) {
			mRlVideo.setVisibility(View.VISIBLE);
			mVideoView.setVideoURI(uri);
			mVideoView.start();
			mVideoView
					.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
						public void onCompletion(MediaPlayer mp) {
							mVideoView.start();
						}
					});
		} else {
			mRlVideo.setVisibility(View.GONE);
		}
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
	    if (mVideoView != null) {
	        mVideoView.stopPlayback();
	    }
	    super.onDestroy();
	}

	private void markIntroductionShown() {
		getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
				.putInt(PREF_KEY, mPackageInfo.versionCode).commit();
	}

	private void resetFirstGuidStatus() {
		Editor editor = mPreferences.edit();
		editor.putBoolean(MainActivity.SP_IS_FIRST_MAIN, true);
		editor.putBoolean(PostStreamTabFragment.SP_IS_FIRST_ROAM, true);
		editor.putBoolean(RoamCollegeView.SP_IS_FIRST, true);
		editor.commit();
	}

	private class PageAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ImageView view = new ImageView(IntroductionActivity.this);
			view.setScaleType(ImageView.ScaleType.FIT_XY);
			Integer resId = getItem(position);
			view.setImageResource(resId);
			container.addView(view);

			if (position == 3) {
				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						markIntroductionShown();

						Intent i;
						if (AccountInfo.getInstance(getApplicationContext())
								.isLoggedIn()) {
							i = new Intent(getApplicationContext(),
									MainActivity.class);
						} else {
							i = new Intent(getApplicationContext(),
									LoginRegistActivity.class);
						}

						startActivity(i);
						finish();
						overridePendingTransition(android.R.anim.fade_in,
								android.R.anim.fade_out);
					}
				});
			}
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object view) {
			container.removeView((View) view);
		}

		public Integer getItem(int position) {
			switch (position) {
			case 0:
//				return R.drawable.introduce_page_1;
			case 1:
//				return R.drawable.introduce_page_2;
			case 2:
//				return R.drawable.introduce_page_3;
			case 3:
//				return R.drawable.introduce_page_4;
			default:
				return 0;
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Utils.logD(TAG, "surface created:" + holder);
		mSurfaceHolder = holder;
	}

	@Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Utils.logD(TAG, "surface changed:" + holder);
        mSurfaceHolder = holder;
        if (mSurfaceHolder.getSurface() == null) {
            return;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Utils.logD(TAG, "surface destroyed:" + holder);
        mSurfaceHolder = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_start:
                markIntroductionShown();
                Intent i;
                if (AccountInfo.getInstance(this).isLoggedIn()) {
                    i = new Intent(this, MainActivity.class);
                } else {
                    i = new Intent(this, LoginRegistActivity.class);
                }

                startActivity(i);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            default:
                break;
        }
    }
}
