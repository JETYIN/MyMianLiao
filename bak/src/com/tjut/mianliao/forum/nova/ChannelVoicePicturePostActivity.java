package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.im.IMAudioManager;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.GetImageHelper.ImageResultListener;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.Utils;

public class ChannelVoicePicturePostActivity extends BaseActivity implements OnTouchListener,
        ImageResultListener, MsTaskListener, OnCompletionListener, OnClickListener {

    public static final String EXT_PIC_PATH = "ext_pic_path";
    private static final int CANCLLE_SEND_VOICE_MIN_DISTANCE = 100;

    private static final int MAX_RECORD_TIME = 2 * 60;
    private static final int MSG_VOICE = 10;

    private IMAudioManager mImAudioManager;
    private String mImagePath;

    private int mForumId;
    private TextView mTvTime, mTvRecord, mTvChangePic, mTvPlay;
    private ImageView mIvDel;
    private ProgressBar mProgressBar;
    private ImageView mIvPhoto;
    protected LinearLayout mLlProgress;

    private boolean mStopRecord, mCancleRecord, mFinishRecord = true;
    private boolean mHasVoiceFile, mIsPlaying;

    private float mStartX;
    private float mStartY;
    private long mStartTime;
    private long mStopTime;

    private TimerTask mTask;
    private Timer mTimer;
    private int mTimeCount = 0;
    private String mFilePath;
    private  LightDialog mDiscardDialog, mUploadingDialog;
    private GetImageHelper mImageHelper;
    private MsTaskManager mTaskManager;
    private ChannelInfo mChannelInfo;

    private boolean mIsPosting;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_VOICE) {
                if (mCancleRecord) {
                    mTvRecord.setText(R.string.cht_unpressed_to_cancle);
                } else {
                    mTvRecord.setText(R.string.cht_move_up_to_cancle);
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
    protected int getLayoutResID() {
        return R.layout.activity_channel_neight_voice_picture;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(mChannelInfo.name);
        getTitleBar().showRightButton(R.drawable.bottom_ok_commit, this);
        mImAudioManager = IMAudioManager.getInstance(this);
        mImageHelper = new GetImageHelper(this, this);
        mTaskManager = MsTaskManager.getInstance(this);
        mTaskManager.registerListener(this);
        mImagePath = getIntent().getStringExtra(EXT_PIC_PATH);
        mTvTime = (TextView) findViewById(R.id.tv_record_time);
        mTvRecord = (TextView) findViewById(R.id.tv_show_msg);
        mTvChangePic = (TextView) findViewById(R.id.tv_change_pic);
        mTvPlay = (TextView) findViewById(R.id.tv_start_play);
        mIvDel = (ImageView) findViewById(R.id.iv_del);
        mIvPhoto = (ImageView) findViewById(R.id.iv_photo);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_circle);
        mLlProgress = (LinearLayout) findViewById(R.id.ll_loading_progress);
        mTvChangePic.setVisibility(View.VISIBLE);
        mTvRecord.setOnTouchListener(this);
        if (mImagePath != null) {
            mIvPhoto.setImageBitmap(Utils.fileToBitmap(mImagePath));
        }
        initVoiceRecord();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTvChangePic.postDelayed(new Runnable() {
            @Override
            public void run() {
                mTvChangePic.setVisibility(View.GONE);
            }
        }, 2000);
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
        } else if (mImagePath != null || mFilePath != null) {
            showDiscardDialog();
        }
    }

    protected HashMap<String, String> getParams() {
        HashMap<String, String> params = new HashMap<>();
        params.put("forum_id", String.valueOf(mForumId));
        params.put("thread_type", String.valueOf(CfPost.THREAD_TYPE_PIC_VOICE));
        params.put("voice_length", String.valueOf(mTimeCount - 1));
        params.put("images", "image");
        return params;
    }

    protected HashMap<String, String> getFiles() {
        HashMap<String, String> files = new HashMap<>();
        files.put("voice", mFilePath);
        files.put("image", mImagePath);
        return files;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_photo:
                int visible = mTvChangePic.getVisibility();
                if (visible == View.VISIBLE) {
                    mTvChangePic.setVisibility(View.GONE);
                } else {
                    mTvChangePic.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.tv_change_pic:
                mImageHelper.getImage(true, 1);
                break;
            case R.id.tv_start_play:
                if (!mHasVoiceFile) {
                    return;
                } else if (!mIsPlaying) {
                    playVoice();
                } else {
                    stopVoiceRecord();
                    mImAudioManager.stopPlayAudio();
                    mIsPlaying = false;
                    mTvPlay.setText(R.string.channel_click_to_playing);
                    mTvPlay.setCompoundDrawablesWithIntrinsicBounds(
                            0, R.drawable.button_play, 0, 0);
                }
                break;
            case R.id.iv_del:
                if (!mHasVoiceFile) {
                    return;
                } else {
                    deleteVoiceFile();
                    initVoiceRecord();
                    toast("文件删除成功!");
                }
                break;
            case R.id.btn_right:
                if (!isStateReady()) {
                    return;
                }
                submit();
                break;

            default:
                break;
        }

    }
    
    protected void showProgress() {
        mLlProgress.setVisibility(View.VISIBLE);
    }
    
    protected void hideProgress() {
        mLlProgress.setVisibility(View.GONE);
    }

    protected void showDiscardDialog() {
        if (mDiscardDialog == null) {
            mDiscardDialog = new LightDialog(this)
            .setTitleLd(R.string.qa_discard_title)
            .setMessage(R.string.qa_discard_message)
            .setButtonBackground(DialogInterface.BUTTON_POSITIVE,
                    R.drawable.selector_btn_red)
            .setNegativeButton(R.string.qa_discard_continue, null)
            .setPositiveButton(R.string.qa_discard_quit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }
        mDiscardDialog.show();
    }


    protected void showUploadingDialog() {
        if (mUploadingDialog == null) {
            mUploadingDialog = new LightDialog(this)
            .setTitleLd(R.string.qa_upload_title)
            .setMessage(R.string.qa_upload_message)
            .setButtonBackground(DialogInterface.BUTTON_POSITIVE,
                    R.drawable.selector_btn_red)
            .setNegativeButton(R.string.qa_upload_wait, null)
            .setPositiveButton(R.string.qa_upload_quit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }
        mUploadingDialog.show();
    }


    protected boolean isStateReady() {
        if (mIsPosting) {
            toast(R.string.handling_last_task);
            return false;
        }
        return mImagePath != null && mFilePath != null;
    }

    protected void submit() {
        if (Utils.isNetworkAvailable(this)) {
            mTaskManager.startForumPostTask(false, getParams(), getFiles());
        } else {
            toast(R.string.no_network);
        }
    }

    private void initVoiceRecord() {
        mTvTime.setVisibility(View.INVISIBLE);
        mTvTime.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.pic_record_start, 0, 0, 0);
        mProgressBar.setVisibility(View.GONE);
        mTvPlay.setVisibility(View.GONE);
        mIvDel.setVisibility(View.GONE);
        mTvRecord.setVisibility(View.VISIBLE);
        mTvRecord.setText(R.string.cht_pressed_to_record);
        initRecord();
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
        mTvTime.setVisibility(View.VISIBLE);
        mTvRecord.setVisibility(View.VISIBLE);
        mTvRecord.setText(R.string.cht_move_up_to_cancle);
        mTimer = new Timer();
        mImAudioManager.startRecord();
        startCount();
    }

    private void stopVoiceRecord() {
        if (!mStopRecord) {
            mImAudioManager.stopRecord();
            mFilePath = mImAudioManager.getFilePath();
        }
        mHasVoiceFile = true;
        mStopRecord = true;
        initRecord();
        mTvPlay.setCompoundDrawablesWithIntrinsicBounds(
                0, R.drawable.button_play, 0, 0);
        mTvTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pic_record_stop, 0, 0, 0);

        mProgressBar.setVisibility(View.GONE);
        mTvRecord.setVisibility(View.GONE);
        mTvPlay.setVisibility(View.VISIBLE);
        mIvDel.setVisibility(View.VISIBLE);
    }

    private void playVoice() {
        mIsPlaying = true;
        mImAudioManager.startPlayAudio(mImAudioManager.getFilePath());
        mTvPlay.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.button_stop, 0, 0);
        mTvPlay.setText(R.string.channel_click_to_stop_playing);
        mImAudioManager.setOnCompletionListener(this);
    }

    private void deleteVoiceFile() {
        mImAudioManager.deleteAudio(mImAudioManager.getFilePath());
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
                        initVoiceRecord();
                        toast("文件删除成功!");
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
                float instance = caclInstance(mStartX, mStartY, event.getX(), event.getY());
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

    private float caclInstance(float oldX, float oldY, float newX, float newY) {
        float poorX = oldX - newX;
        float poorY = oldY - newY;
        return (float) Math.sqrt(poorX * poorX + poorY * poorY);
    }

    @Override
    public void onImageResult(boolean success, String imageFile, Bitmap bm) {
        String file = GetImageHelper.saveAsTodo(this, imageFile);
        Image image = new Image(file, GetImageHelper.zoomOutImage(file, Image.PREVIEW_SIZE));
        bm = Utils.fileToBitmap(image.file);
        mIvPhoto.setImageBitmap(bm);
        mImagePath = file;
    }

    @Override
    public void onImageResult(boolean success, ArrayList<String> images) {
        if (success) {
            String file = GetImageHelper.saveAsTodo(this, images.get(0));
            Bitmap bm = BitmapFactory.decodeFile(images.get(0));
            Image image = new Image(file, GetImageHelper.zoomOutImage(file, Image.PREVIEW_SIZE));
            bm = Utils.fileToBitmap(image.file);
            mIvPhoto.setImageBitmap(bm);
            mImagePath = file;
        }
    }

    @Override
    public void onPreExecute(MsTaskType type) {
        switch (type) {
            case FORUM_PUBLISH_POST:
            case FORUM_EDIT_POST:
                mIsPosting = true;
                showProgress();
                break;

            default:
                break;
        }
    }

    @Override
    public void onPostExecute(MsTaskType type, MsResponse response) {
        hideProgress();
        if (type == MsTaskType.FORUM_PUBLISH_POST) {
            mIsPosting = false;
            if (response.value instanceof CfPost) {
                setResult(RESULT_OK);
                finish();
            } else {
                toast("发帖失败，请重试!");
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mTvPlay.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.button_play, 0, 0);
        mTvPlay.setText(R.string.channel_click_to_playing);
        mIsPlaying = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mImageHelper.handleResult(requestCode, data);
    }

}
