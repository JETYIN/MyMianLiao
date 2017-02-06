package com.tjut.mianliao.forum.nova;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.nova.BasePostActivity.RemoveAllListener;
import com.tjut.mianliao.im.IMAudioManager;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.Utils;

public class NormalPostActivity extends BasePostActivity implements OnTouchListener,
        OnCompletionListener, RemoveAllListener {

    private static final String TAG = "NormalPostActivity";

    public static final String EXT_PIC_PATH = "ext_pic_path";

    private static final int CANCLLE_SEND_VOICE_MIN_DISTANCE = 100;
    public static final int REQUEST_VIDEO = 104;

    private static final int STATUS_PIC = 1;
    private static final int STATUS_VOICE = 2 ;

    private static final int MAX_RECORD_TIME = 2 * 60;
    private static final int MSG_VOICE = 10;

    private IMAudioManager mImAudioManager;

    private View mVoiceRecordView;
    private View mViewRecordTime;
    private TextView mTvTime, mTvNoticeMsg;
    private ImageView mIvDel, mIvRecordFlag, mIvPlay;
    private ProgressBar mProgressBar;
    protected LinearLayout mLlProgress;
    private ImageView mIvPic, mIvVoice;
    private LightDialog mCancleDialog;
    
    private AnimationDrawable mAnimationDrawableLeft;
    private AnimationDrawable mAnimationDrawableRight;

    @Override
    public void onDelClick(int index) {
        super.onDelClick(index);
    }

    private boolean mStopRecord, mCancleRecord, mFinishRecord = true;
    private boolean mHasVoiceFile, mIsPlaying;
    private boolean mIsPosting;

    private float mStartX;
    private float mStartY;
    private long mStartTime;

    private long mStopTime;

    private Timer mTimer;
    private TimerTask mTask;

    private int mPostType = STATUS_PIC;
    private int mTimeCount = 0;

    private String mFilePath;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_VOICE) {
                if (mCancleRecord) {
                    if (mStopRecord) {
                        mTvNoticeMsg.setText(R.string.cht_pressed_to_record);
                    } else {
                        mTvNoticeMsg.setText(R.string.cht_unpressed_to_cancle);
                    }
                } else if (!mStopRecord) {
                    mTvNoticeMsg.setText(R.string.cht_unpressed_to_finish);
                }
                if (mTimeCount > 0) {
                    if (mStopRecord) {
                        stopVoiceRecord();
                    }
                    mTvTime.setText(Utils.getTimeStrByInt(mTimeCount - 1, 2));
                }
            }
        }

    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInflater.inflate(R.layout.normal_post_footer, mFlFooter);
        mVoiceRecordView = findViewById(R.id.ll_voice_record);
        mListener = this;
        initView();
    }

    private void initView() {
        mImAudioManager = IMAudioManager.getInstance(this);
        mTaskManager = MsTaskManager.getInstance(this);
        mTaskManager.registerListener(this);
        mGvImages = (GridView) findViewById(R.id.gv_gallery);
        mViewRecordTime = findViewById(R.id.ll_record_time);
        mTvTime = (TextView) findViewById(R.id.tv_record_time);
        mTvNoticeMsg = (TextView) findViewById(R.id.tv_show_msg);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_circle);
        mLlProgress = (LinearLayout) findViewById(R.id.ll_loading_progress);
        mIvDel = (ImageView) findViewById(R.id.iv_del);
        mIvPic = (ImageView) findViewById(R.id.iv_pic);
        mIvVoice = (ImageView) findViewById(R.id.iv_voice);
        mIvRecordFlag = (ImageView) findViewById(R.id.iv_record_flag);
        mIvPlay = (ImageView) findViewById(R.id.iv_play);
        mIvPic.setVisibility(View.VISIBLE);
        mIvVoice.setVisibility(View.VISIBLE);
        mIvRecordFlag.setOnTouchListener(this);
        mGvImages.setVisibility(View.GONE);
        initVoiceRecord();
        ImageView ivAnimLeft = (ImageView) findViewById(R.id.iv_anim_left);
        ImageView ivAnimRight = (ImageView) findViewById(R.id.iv_anim_right);
        mAnimationDrawableLeft = (AnimationDrawable) ivAnimLeft.getDrawable();
        mAnimationDrawableRight = (AnimationDrawable) ivAnimRight.getDrawable();
        
        mGvImages.setAdapter(mImageAdapter);
        mGvImages.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Image image = (Image) parent.getItemAtPosition(position);
                chooseImage(image, position);
            }
        });
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        mCancleRecord = true;
        if (!mFinishRecord) {
            mImAudioManager.stopRecord();
            mImAudioManager.deleteAudio(mImAudioManager.getFilePath());
            initVoiceRecord();
        }
    }

    @Override
    public void onBackPressed() {
        if (mIsPosting) {
            showUploadingDialog();
        } else if (mFilePath != null) {
            showDiscardDialog();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (hasPicture()) {
            mGvImages.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mVoiceRecordView.setVisibility(View.GONE);
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_play:
                if (!mHasVoiceFile) {
                    return;
                } else if (!mIsPlaying) {
                    playVoice();
                    startAnim();
                } else {
                    mImAudioManager.stopPlayAudio();
                    mIsPlaying = false;
                    mTvNoticeMsg.setText(R.string.channel_click_to_playing);
                    mIvRecordFlag.setVisibility(View.GONE);
                    mIvPlay.setImageResource(R.drawable.note_bg_button_stop);
                    mTvNoticeMsg.setVisibility(View.VISIBLE);
                    stopAnim();
                }
                break;
            case R.id.iv_del:
                if (!mHasVoiceFile) {
                    return;
                } else {
                    deleteVoiceFile();
                    initVoiceRecord();
                    changeSendColor(false);
                    toast("文件删除成功!");
                }
                break;
            case R.id.tv_right:
                if (!isStateReady()) {
                    toast(R.string.fp_tst_content_empty);
                    return;
                }
                submit();
                break;
            case R.id.iv_pic:
                mPostType = STATUS_PIC;
                checkStatusUI();
                // choose image
                mGetImageHelper.getImage(true, 9 - mMIH.getImages().size());
                break;
            case R.id.iv_voice:
                mPostType = STATUS_VOICE;
                checkStatusUI();
                break;
            case R.id.iv_close:
            	hideVoiceRecordView();
            	if (!mHasVoiceFile) {
            		resetBtnStatus();
            	}
                break;
            default:
                super.onClick(v);
        }

    }

	private void hideVoiceRecordView() {
		Animation loadAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_post_voice_out);
		loadAnimation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mVoiceRecordView.setVisibility(View.GONE);
			}
		});
		mVoiceRecordView.startAnimation(loadAnimation);
	}

    @Override
	protected boolean isStateReady() {
		return isContentReady() || hasVoice() || hasPicture();
	}

    private boolean hasVoice() {
    	return mHasVoiceFile;
    }
    
    private void showCancleDialog() {
        if (mCancleDialog == null) {
            mCancleDialog = new LightDialog(this);
            mCancleDialog.setTitleLd(R.string.course_time_clear)
                    .setMessage(R.string.cf_post_video_cancle_notice_msg)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
        }
        mCancleDialog.show();
    }


    private void checkStatusUI() {
        switch (mPostType) {
            case STATUS_PIC:
                hideViewIfVisible(mGvImages);
                if (hasPicture()) {
                    mGvImages.setVisibility(View.VISIBLE);
                } else {
                    mGvImages.setVisibility(View.GONE);
                }
                mVoiceRecordView.setVisibility(View.GONE);
                mIvPic.setEnabled(true);
                mIvVoice.setEnabled(true);
                if (mHasVoiceFile) {
                    mVoiceRecordView.setVisibility(View.VISIBLE);
                    mPostType = STATUS_VOICE;
                }
                break;
            case STATUS_VOICE:
                hideViewIfVisible(mVoiceRecordView);
                mIvPic.setEnabled(true);
                mIvVoice.setEnabled(true);
                break;
            default:
                break;
        }
        hideEmoPicker();
    }

	private void hideViewIfVisible(View view) {
		if (view.getVisibility() == View.VISIBLE) {
			view.setVisibility(View.GONE);
		} else if (view.getVisibility() == View.GONE) {
		    view.setVisibility(View.VISIBLE);
		}
	}

    @Override
    protected boolean setCanRefFriend() {
        return true;
    }

    private void initVoiceRecord() {
        initRecord();
        mViewRecordTime.setVisibility(View.INVISIBLE);
        mTvTime.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.pic_circle_red_record_time_flag, 0, 0, 0);
        mProgressBar.setVisibility(View.GONE);
        mIvRecordFlag.setImageResource(R.drawable.note_bg_button_tape);
        mIvDel.setVisibility(View.GONE);
        mTvNoticeMsg.setVisibility(View.VISIBLE);
        mTvNoticeMsg.setText(R.string.cht_pressed_to_record);
        mIvRecordFlag.setVisibility(View.VISIBLE);
        mIvPlay.setVisibility(View.GONE);
        mStopRecord = false;
        mHasVoiceFile = false;
        mTimeCount = 0;
    }

    private void resetVoiceRecord() {
        mStopRecord = false;
        mCancleRecord = false;
        mFinishRecord = true;
        mTimer = null;
        mTask = null;
    }


    private void initRecord() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = null;
        mTask = null;
    }

    private void startVoiceRecord() {
        mProgressBar.setVisibility(View.VISIBLE);
        mViewRecordTime.setVisibility(View.VISIBLE);
        mTvNoticeMsg.setVisibility(View.VISIBLE);
        mTvNoticeMsg.setText(R.string.cht_unpressed_to_finish);
        mIvPlay.setVisibility(View.GONE);
        mIvRecordFlag.setImageResource(R.drawable.note_bg_button_recording);
        mIvRecordFlag.setVisibility(View.VISIBLE);
        mTimer = new Timer();
        /**开始录制并创建文件夹**/
        mImAudioManager.startRecord();
        startCount();
        startAnim();
    }

    private void startAnim() {
        mAnimationDrawableLeft.start();
        mAnimationDrawableRight.start();
    }

    private void stopVoiceRecord() {
        if (!mStopRecord) {
            mImAudioManager.stopRecord();
        }
        mFilePath = mImAudioManager.getFilePath();
        mHasVoiceFile = mImAudioManager.hasFile();
        mStopRecord = true;
        initRecord();
        mIvRecordFlag.setImageResource(R.drawable.note_bg_button_tape);
        mIvPlay.setImageResource(R.drawable.note_bg_button_stop);
        mTvTime.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.pic_circle_red_record_time_finish_flag, 0, 0, 0);

        mProgressBar.setVisibility(View.GONE);
        if (mHasVoiceFile) {
            mTvNoticeMsg.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTvNoticeMsg.setText(R.string.channel_click_to_playing);
                }
            }, 1000);
            mIvPlay.setVisibility(View.VISIBLE);
            mIvRecordFlag.setVisibility(View.GONE);
            mIvVoice.setImageResource(R.drawable.note_bg_button_listen);
        } else {
            mTvNoticeMsg.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTvNoticeMsg.setText(R.string.cht_pressed_to_record);
                }
            }, 1000);
            mIvPlay.setVisibility(View.GONE);
            mIvRecordFlag.setVisibility(View.VISIBLE);
        }
        changeSendColor(true);
        mIvDel.setVisibility(View.VISIBLE);
        stopAnim();
    }

    private void stopAnim() {
        mAnimationDrawableLeft.stop();
        mAnimationDrawableRight.stop();
    }

    private void playVoice() {
        mIsPlaying = true;
        mImAudioManager.startPlayAudio(mImAudioManager.getFilePath());
        mTvNoticeMsg.setVisibility(View.INVISIBLE);
        mIvPlay.setImageResource(R.drawable.note_bg_button_start);
        mIvPlay.setVisibility(View.VISIBLE);
        mIvRecordFlag.setVisibility(View.GONE);
        mImAudioManager.setOnCompletionListener(this);
    }

    private void deleteVoiceFile() {
        mImAudioManager.deleteAudio(mImAudioManager.getFilePath());
        mHasVoiceFile = false;
        mIvVoice.setImageResource(R.drawable.note_bg_button_voice);
    }

    private void startCount() {
        mTask = new TimerTask() {
            @Override
            public void run() {
                if (!mStopRecord) {
                    mTimeCount++;
                    if (mTimeCount > MAX_RECORD_TIME) {
                        mStopRecord = true;
                    }
                    mHandler.sendEmptyMessage(MSG_VOICE);
                }
            }
        };
        mTimer.schedule(mTask, 0, 1000);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mFinishRecord = false;
                mCancleRecord = false;
                initVoiceRecord();
                mStartX = event.getX();
                mStartY = event.getY();
                mStartTime = System.currentTimeMillis();
                startVoiceRecord();
                break;

            case MotionEvent.ACTION_UP:
                if (!mFinishRecord) {
                    if (!mStopRecord) {
                        mImAudioManager.stopRecord();
                    }
                    mStopTime = System.currentTimeMillis();
                    if (mStopTime - mStartTime < 1000) {
                        toast(R.string.cht_tst_voice_too_short);
                        mImAudioManager.deleteAudio(mImAudioManager.getFilePath());
                        mCancleRecord = true;
                    }
                    if (mCancleRecord) {
                        deleteVoiceFile();
                        stopVoiceRecord();
                        initVoiceRecord();
                        toast("文件删除成功!");
                        changeSendColor(false);
                        return mCancleRecord;
                    }
                    if (mTimer != null) {
                        mTimer.cancel();
                    }
                    stopVoiceRecord();
                    resetVoiceRecord();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                float instance = calcInstance(mStartX, mStartY, event.getX(), event.getY());
                mCancleRecord = instance > CANCLLE_SEND_VOICE_MIN_DISTANCE;
                mHandler.sendEmptyMessage(MSG_VOICE);
                break;

            case MotionEvent.ACTION_CANCEL:
                mImAudioManager.stopRecord();
                mImAudioManager.deleteAudio(mImAudioManager.getFilePath());
                mCancleRecord = true;
                initVoiceRecord();
                stopVoiceRecord();
                resetVoiceRecord();
                break;

            default:
                break;
        }
        return true;
    }

    private float calcInstance(float oldX, float oldY, float newX, float newY) {
        float poorX = oldX - newX;
        float poorY = oldY - newY;
        return (float) Math.sqrt(poorX * poorX + poorY * poorY);
    }

    @Override
    protected HashMap<String, String> getParams() {
        HashMap<String, String> params = super.getParams();
        params.put("thread_type", String.valueOf(getThreadType()));
        switch (mPostType) {
            case STATUS_VOICE:
                params.put("voice_length", String.valueOf(mTimeCount - 1));
                break;
            default:
                break;
        }
        return params;
    }

    @Override
    protected HashMap<String, String> getFiles() {
        HashMap<String, String> files = super.getFiles();
        if (mPostType == STATUS_VOICE) {
            files.put("voice", mFilePath);
        }
        return files;
    }

    private int getThreadType() {
        switch (mPostType) {
            case STATUS_VOICE:
                return CfPost.THREAD_TYPE_VOICE;
            case STATUS_PIC:
            default:
                return CfPost.THREAD_TYPE_NORMAL;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GetImageHelper.REQUEST_IMAGE_CODE &&
                resultCode == RESULT_CANCELED) {
            if (mMIH.getImages() == null || mMIH.getImages().size() <= 0) {
                resetBtnStatus();
                mGvImages.setVisibility(View.GONE);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mIvPlay.setImageResource(R.drawable.note_bg_button_stop);
        mTvNoticeMsg.setText(R.string.channel_click_to_playing);
        mTvNoticeMsg.setVisibility(View.VISIBLE);
        mIvVoice.setImageResource(R.drawable.note_bg_button_voice);
        mIsPlaying = false;
        stopAnim();
    }

	@Override
    public void hasRemoveAll() {
        resetBtnStatus();
        switch (mPostType) {
            case STATUS_PIC:
                mGvImages.setVisibility(View.GONE);
                changeSendColor(false);
                break;
            case STATUS_VOICE:

                break;
            default:
                break;
        }
    }

	private void resetBtnStatus() {
        mIvPic.setEnabled(true);
        mIvVoice.setEnabled(true);
	}
}
