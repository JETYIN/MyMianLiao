package com.tjut.mianliao.component.nova;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tjut.mianliao.R;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.nova.ForumPostAdapter;
import com.tjut.mianliao.im.IMAudioManager;
import com.tjut.mianliao.im.IMAudioManager.VoicePlayingListener;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.FileDownloader;
import com.tjut.mianliao.util.FileDownloader.Callback;

public class VoiceView extends LinearLayout implements OnClickListener, VoicePlayingListener,
		OnCompletionListener {

    private Context mContext;
    
	private LayoutInflater mInflater;

	private IMAudioManager mAudioManager;
	
	private FileDownloader mFileDownloader;
	
	private LinearLayout mLlVoiceView;
	
	private TextView mTvVOiceTime;
	
	private ImageView mVoiceAnima;
	
	private AnimationDrawable mAnimationDrawable;
	
	private CfPost mCurrentPost;
	
	private boolean mIsPlaying;
	
	private boolean mIsPaused;
	
	private boolean mIsNightMode;
	
	private int mPosition = -1;
	
	private ForumPostAdapter mPostAdapter;
	
	public VoiceView(Context context) {
		this(context, null);
	}

	public VoiceView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VoiceView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
	    mContext = context.getApplicationContext();
		mInflater = LayoutInflater.from(context);
		mInflater.inflate(R.layout.post_voice_view, this);
		mIsNightMode = Settings.getInstance(context).isNightMode();
		mAudioManager = IMAudioManager.getInstance(context);
		mAudioManager.registerVoicePlayListener(this);
		mFileDownloader = FileDownloader.getInstance(context);
		mLlVoiceView = (LinearLayout) findViewById(R.id.ll_voice_view);
		mTvVOiceTime = (TextView) findViewById(R.id.tv_voice_time_play);
		mVoiceAnima = (ImageView) findViewById(R.id.iv_voice_anim);
		mAnimationDrawable = (AnimationDrawable) mVoiceAnima.getDrawable();
		mLlVoiceView.setOnClickListener(this);
		if (mIsNightMode) {
		    mLlVoiceView.setBackgroundResource(R.drawable.bg_item_voice_view_black);
		}
	}

	public void show(CfPost post){
		mCurrentPost = post;
		mTvVOiceTime.setText(mContext.getString(R.string.fp_voice_length, post.voiceLength));
		mFileDownloader.getFile(mCurrentPost.voice.url, new Callback() {
	          @Override
	          public void onResult(boolean success, String url, String fileName) {
	              if (url.equals(mCurrentPost.voice.url)) {
	                  mCurrentPost.setVoicePath(fileName);
	              }
	          }
		}, false);
	}

	public void setPostPosition(int position) {
	    mPosition = position;
	}
	
	public void setPostAdapter(ForumPostAdapter adapter) {
	    mPostAdapter = adapter;
	}
	
    public void onDestroy() {
        if (mIsPlaying) {
            stopVoicePlay();
        }
        mAudioManager.unregisterVoicePlayingListener(this);
    }
    
    public void destroyView(CfPost post) {
        if (mCurrentPost == post && mIsPlaying) {
            stopVoicePlay();
        }
    }

    public void stopVoicePlay() {
        mIsPlaying = false;
        mIsPaused = false;
        stopPlay();
        stopAnim();
    }

    private void stopPlay() {
        if (mAudioManager.hasPlayer()) {
            mAudioManager.stopPlayAudio();
        }
    }

	private void stopAnim() {
		mAnimationDrawable.stop();
        mAnimationDrawable.selectDrawable(0);
	}
	

    private void startVoicePlay() {
        if (mPostAdapter != null) {
            mAudioManager.registerVoicePlayListener(this);
        }
        if (!mIsPlaying) {
            mIsPlaying = true;
            if (mIsPaused) {
                mAudioManager.continuePlayAudio();
                mIsPaused = false;
                mAnimationDrawable.start();
                if(mPostAdapter != null) {
                    mPostAdapter.setVoicePlayingPostPosition(mPosition);
                }
                return;
            }
            if (mCurrentPost.getVoicePath() != null) {
                if (mPostAdapter != null) {
                    mAudioManager.startPlayAudio(mCurrentPost, this);
                } else {
                    mAudioManager.startPlayAudio(mCurrentPost);
                }
                mAnimationDrawable.start();
                if(mPostAdapter != null) {
                    mPostAdapter.setVoicePlayingPostPosition(mPosition);
                }
            } else {
                Toast.makeText(getContext(), "文件下载中，请稍后", Toast.LENGTH_SHORT).show();
            }
        } else {
            mIsPlaying = false;
            if (!mIsPaused) {
                mIsPaused = true;
                mAudioManager.pausePlayAudio();
            } else {
                mIsPaused = false;
                mAudioManager.stopPlayAudio();
            }
            stopAnim();
            if (mPostAdapter != null) {
                mPostAdapter.setVoicePlayingPostPosition(-1);
            }
        }
    }

    
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_voice_view:
			startVoicePlay();
			break;

		default:
			break;
		}
	}

	@Override
	public void onVoicePlaying(CfPost post) {
		if (post != mCurrentPost) {
		    stopVoicePlay();
        }
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		mIsPlaying = false;
		stopAnim();
		mPostAdapter.setVoicePlayingPostPosition( - 1);
	}

}
