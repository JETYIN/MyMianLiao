package com.tjut.mianliao.component.nova;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.FlexibleImageView;
import com.tjut.mianliao.component.FlexibleImageView.OnImageClickListener;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.im.IMAudioManager;
import com.tjut.mianliao.im.IMAudioManager.VoicePlayingListener;
import com.tjut.mianliao.image.ImageActivity;
import com.tjut.mianliao.util.FileDownloader;
import com.tjut.mianliao.util.FileDownloader.Callback;
import com.tjut.mianliao.util.Utils;

public class PictureVoiceView extends LinearLayout implements OnClickListener,
        OnCompletionListener, VoicePlayingListener, OnImageClickListener {

    private LayoutInflater mInflater;
    private FlexibleImageView mPivPic;
    private TextView mTvTime;
    private ImageView mIvPlay;
    private SeekBar mSeekBar;
    private Timer mTimer;
    private TimerTask mTask;
    private IMAudioManager mAudioManager;
    private boolean mIsPlaying;
    private boolean mIsPaused;
    private CfPost mCurrentPost;
    private FileDownloader mImageDownloader;
  
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            String content = getContext().getString(
                    R.string.channel_voice_time_style,
                    Utils.getTimeStrByInt((int) mCurrentPost.currentPosition / 1000, 1),
                    Utils.getTimeStrByInt(mCurrentPost.voiceLength, 1));
            mTvTime.setText(content);
            showProgress();
        }

        private void showProgress() {
            int current = mCurrentPost.currentPosition;
            int total = mCurrentPost.totalDuration;
            int progress;
            if (total == 0) {
                progress = 0;
            } else {
                progress = (int) (current * 100 / total);
            }
            mSeekBar.setProgress(progress);
        }

    };

    public PictureVoiceView(Context context) {
        super(context);
        init(context);
    }

    public PictureVoiceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void onDestroy() {
        if (mIsPlaying) {
            stopVoicePlay();
            if (mAudioManager.hasPlayer()) {
                mAudioManager.stopPlayAudio();
            }
        }
        mAudioManager.unregisterVoicePlayingListener(this);
    }
    
    public void destroyView(CfPost post) {
        if (mCurrentPost == post && mIsPlaying) {
            stopVoicePlay();
            if (mAudioManager.hasPlayer()) {
                mAudioManager.stopPlayAudio();
            }
        }
    }

    public void stopVoicePlay() {
        mIsPlaying = false;
        mIsPaused = false;
        mIvPlay.setImageResource(R.drawable.list_button_play);
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mTask = null;
        initVoicePlayStatus();
    }

    private void init(Context context) {
        mInflater = LayoutInflater.from(context);
        mInflater.inflate(R.layout.picture_voice_post, this);
        mAudioManager = IMAudioManager.getInstance(context);
        mAudioManager.registerVoicePlayListener(this);
        mImageDownloader = FileDownloader.getInstance(context);
        mPivPic = (FlexibleImageView) findViewById(R.id.piv_picture);
        mTvTime = (TextView) findViewById(R.id.tv_voice);
        mIvPlay = (ImageView) findViewById(R.id.iv_play);
        mSeekBar = (SeekBar) findViewById(R.id.sb_voice);
        mSeekBar.setMax(100);
        mIvPlay.setOnClickListener(this);
        mPivPic.setOnImageClickListener(this);
        mPivPic.setClickble(false);
    }

    public void show(CfPost currentPost) {
        mCurrentPost = currentPost;
        showPicVoiceView();
    }

    private void showPicVoiceView() {
        String content = getContext().getString(
                R.string.channel_voice_time_style,
                Utils.getTimeStrByInt((int) mCurrentPost.currentPosition / 1000, 1),
                Utils.getTimeStrByInt(mCurrentPost.voiceLength, 1));
        mTvTime.setText(content);
        if (!mCurrentPost.images.isEmpty()) {
            mPivPic.setImages(mCurrentPost.images);
        }
        mImageDownloader.getFile(mCurrentPost.voice.url, new Callback() {
          @Override
          public void onResult(boolean success, String url, String fileName) {
              if (url.equals(mCurrentPost.voice.url)) {
                  mCurrentPost.setVoicePath(fileName);
              }
          }
      }, false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_play:
                startVoicePlay();
                break;
            default:
                break;
        }
    }

    private void startVoicePlay() {
        if (!mIsPlaying) {
            mIsPlaying = true;
            mIvPlay.setImageResource(R.drawable.list_button_stop);
            if (mIsPaused) {
                mAudioManager.continuePlayAudio();
                mIsPaused = false;
                return;
            }
            if (mCurrentPost.getVoicePath() != null) {
                mTimer = new Timer();
                mAudioManager.startPlayAudio(mCurrentPost);
                startCount();
                mAudioManager.setOnCompletionListener(this);
            } else {
                Toast.makeText(getContext(), "文件下载中，请稍后", Toast.LENGTH_SHORT).show();
            }
        } else {
            mIsPlaying = false;
            mIvPlay.setImageResource(R.drawable.list_button_play);
            if (!mIsPaused) {
                mIsPaused = true;
                mAudioManager.pausePlayAudio();
            } else {
                mIsPaused = false;
                mAudioManager.stopPlayAudio();
                mTimer.cancel();
                mTask = null;
                initVoicePlayStatus();
            }
        }
    }

    private void showImage() {
        Intent intent = new Intent(getContext(), ImageActivity.class)
            .putExtra(ImageActivity.EXTRA_IMAGE_URL, mCurrentPost.images.get(0).image);
        getContext().startActivity(intent);
    }

    private void initVoicePlayStatus() {
        if (mCurrentPost != null) {
            mTvTime.setText(getContext().getString(R.string.channel_voice_time_style,
                    Utils.getTimeStrByInt(0, 1),
                    Utils.getTimeStrByInt(mCurrentPost.voiceLength, 1)));
            mSeekBar.setProgress(0);
        }
    }


    private void startCount() {
        mTask = new TimerTask() {
            @Override
            public void run() {
                if (mIsPlaying) {
                    mCurrentPost.currentPosition = mAudioManager.getCurrentPostion();
                    mCurrentPost.totalDuration = mAudioManager.getTotalTime();
                    mHandler.sendEmptyMessage(Message.obtain().what);
                } else if (!mIsPaused) {
                    mCurrentPost.currentPosition = 0;
                }
            }
        };
        mTimer.schedule(mTask, 0, 200);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mIsPlaying) {
            mTimer.cancel();
            mTask = null;
            mIsPlaying = false;
            mIvPlay.setImageResource(R.drawable.list_button_play);
            initVoicePlayStatus();
        }
    }

    @Override
    public void onVoicePlaying(CfPost post) {
        if (post != mCurrentPost) {
            stopVoicePlay();
        }
    }

    @Override
    public void onImageClicked() {
        startVoicePlay();        
    }
}
