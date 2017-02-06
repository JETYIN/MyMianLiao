package com.tjut.mianliao.image;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;


public class VideoPlayerActivity extends BaseActivity implements OnPreparedListener,
                OnCompletionListener, OnClickListener{
    
    public static final String EXT_FILE_PATH = "ext_file_path";
    
    private String mPath;
    /** 声音 */
    private int mMaxVolume;
    /** 当前声音 */
    private int mVolume = -1;
    /** 当前亮度 */
    private float mBrightness = -1f;
    /** 当前缩放模式 */
    private AudioManager mAudioManager;
    private GestureDetector mGestureDetector;

    @ViewInject(R.id.operation_bg)
    private ImageView mOperationBg;
    @ViewInject(R.id.operation_percent)
    private ImageView mOperationPercent;
    @ViewInject(R.id.video_view)
    private VideoView mVideoView;
    @ViewInject(R.id.video_loading)
    private LinearLayout mLlLoading;
    @ViewInject(R.id.operation_volume_brightness)
    private View mVolumeBrightnessLayout;
    @ViewInject(R.id.iv_back)
    private ImageView mIvBack;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_video_player;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        getTitleBar().setVisibility(View.GONE);
        mTopLine.setVisibility(View.GONE);
        mPath = getIntent().getStringExtra(EXT_FILE_PATH);
        if (mPath == null) {
            finish();
            return;
        }
        mGestureDetector = new GestureDetector(this, new MyGestureListener());
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVideoView.setOnPreparedListener(this);
        Uri url = Uri.parse(mPath);
        mVideoView.setMediaController(new MediaController(this));  
        mVideoView.setOnCompletionListener(this); 
        mVideoView.setVideoURI(url);  
        mVideoView.start();  
        mVideoView.requestFocus(); 
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mIvBack.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable(){   

                public void run() {   
                    mIvBack.setVisibility(View.GONE);
                }   

             }, 5000); 
        }
        if (mGestureDetector.onTouchEvent(event))
            return true;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                endGesture();
                break;
        }
       

        return super.onTouchEvent(event);
    }
    
    private void endGesture() {
        mVolume = -1;
        mBrightness = -1f;

        mDismissHandler.removeMessages(0);
        mDismissHandler.sendEmptyMessageDelayed(0, 500);
    }
    
    private Handler mDismissHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mVolumeBrightnessLayout.setVisibility(View.GONE);
        }
    };
    
    
    
    @Override
    public void onPrepared(MediaPlayer mp) {
        mLlLoading.setVisibility(View.GONE);
    }
    
    private class MyGestureListener extends SimpleOnGestureListener {
        

        @SuppressWarnings("deprecation")
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            int y = (int) e2.getRawY();
            Display disp = getWindowManager().getDefaultDisplay();
            int windowWidth = disp.getWidth();
            int windowHeight = disp.getHeight();

            if (mOldX > windowWidth * 4.0 / 5)// 右边滑动
                onVolumeSlide((mOldY - y) / windowHeight);
            else if (mOldX < windowWidth / 5.0)// 左边滑动
                onBrightnessSlide((mOldY - y) / windowHeight);

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }
    
    /**
     * 滑动改变声音大小
     * 
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        if (mVolume == -1) {
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0)
                mVolume = 0;

            // 显示
            mOperationBg.setImageResource(R.drawable.ic_launcher);
            // mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }

        int index = (int) (percent * mMaxVolume) + mVolume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

        // 变更进度�?
        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
        // lp.width = findViewById(R.id.operation_full).getLayoutParams().width
        // * index / mMaxVolume;
        // mOperationPercent.setLayoutParams(lp);
    }
    
    /**
     * 滑动改变亮度
     * 
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (mBrightness < 0) {
            mBrightness = getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f)
                mBrightness = 0.50f;
            if (mBrightness < 0.01f)
                mBrightness = 0.01f;

            // 显示
            mOperationBg.setImageResource(R.drawable.ic_launcher);
            // mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }
        WindowManager.LayoutParams lpa = getWindow().getAttributes();
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f)
            lpa.screenBrightness = 1.0f;
        else if (lpa.screenBrightness < 0.01f)
            lpa.screenBrightness = 0.01f;
        getWindow().setAttributes(lpa);

        // ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
        // lp.width = (int)
        // (findViewById(R.id.operation_full).getLayoutParams().width *
        // lpa.screenBrightness);
        // mOperationPercent.setLayoutParams(lp);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish(); 
                break;
            case R.id.iv_start:
                mVideoView.start();
                break;
            default:
                break;
        }
    }

}
