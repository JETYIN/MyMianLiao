package com.tjut.mianliao;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.forum.PostStreamTabFragment;
import com.tjut.mianliao.forum.RoamCollegeView;

public class LoadResActivity extends Activity implements Callback {

	private static final String TAG = "LoadResActivity";
	private TextView mTvVersion;
	private VideoView mVideoView;
	private SurfaceHolder mSurfaceHolder;
	private SharedPreferences mPreferences;
	private FrameLayout mFlStart;
    private PackageInfo mPackageInfo;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	    super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_load_res_anim);
		mPreferences = DataHelper.getSpForData(this);
		mTvVersion = (TextView) findViewById(R.id.tv_version);
		mVideoView = (VideoView) findViewById(R.id.video_introduction);
		mFlStart = (FrameLayout) findViewById(R.id.fl_start);
		mPackageInfo = PackageUtil.getPackageInfo(this);
		if (mPackageInfo != null) {
		    mTvVersion.setText(getString(R.string.ml_version_desc, mPackageInfo.versionName));
		}
		resetFirstGuidStatus();
		SurfaceHolder holder = mVideoView.getHolder();
		holder.addCallback(this); // holder加入回调接口
		// setType必须设置，要不出错.
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		new LoadDexTask().execute();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Uri uri = Uri.parse("android.resource://" + getPackageName() + "/"
		        + R.raw.introduction_video);
		if (uri != null) {
		    mVideoView.setVideoURI(uri);
		    mVideoView.start();
		    mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
		        public void onCompletion(MediaPlayer mp) {
		            mVideoView.start();
		        }
		    });
		}
	}

    private void markIntroductionShown() {
        getSharedPreferences(IntroductionActivity.PREF_NAME, MODE_PRIVATE)
            .edit()
            .putInt(IntroductionActivity.PREF_KEY, mPackageInfo.versionCode)
            .commit();
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mVideoView != null) {
			mVideoView.stopPlayback();
		}
	}

	private void resetFirstGuidStatus() {
		Editor editor = mPreferences.edit();
		editor.putBoolean(MainActivity.SP_IS_FIRST_MAIN, true);
		editor.putBoolean(PostStreamTabFragment.SP_IS_FIRST_ROAM, true);
		editor.putBoolean(RoamCollegeView.SP_IS_FIRST, true);
		editor.commit();
	}

	class LoadDexTask extends AsyncTask {
		@Override
		protected Object doInBackground(Object[] params) {
			try {
				MultiDex.install(getApplication());
				Log.d("loadDex", "install finish");
				((MianLiaoApp) getApplication()).init();
			} catch (Exception e) {
				Log.e("loadDex", e.getLocalizedMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object o) {
			showBtnWithAnim();
		}
	}

	private void showBtnWithAnim() {
		Animation animation = AnimationUtils.loadAnimation(this,
				R.anim.anim_start_up);
		mFlStart.startAnimation(animation);
		mFlStart.setVisibility(View.VISIBLE);
	}

	private void exit() {
        markIntroductionShown();
		((MianLiaoApp) getApplication()).installFinish(getApplication());
		finish();
		System.exit(0);
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.iv_start:
			exit();
			break;
		default:
			break;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surface created:" + holder);
		mSurfaceHolder = holder;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(TAG, "surface changed:" + holder);
		mSurfaceHolder = holder;
		if (mSurfaceHolder.getSurface() == null) {
			return;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surface destroyed:" + holder);
		mSurfaceHolder = null;
	}
}
