package com.tjut.mianliao.im;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.util.Log;

import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.util.Utils;

@SuppressLint("SimpleDateFormat")
public class IMAudioManager {
    
    private static final String TAG = "IMAudioManager";

    private static WeakReference<IMAudioManager> sInstanceRef;

    private String mPathName;

    private Context mContext;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private List<VoicePlayingListener> mListeners;
    
    private boolean mIsRunning;

    public static synchronized IMAudioManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        IMAudioManager instance = new IMAudioManager(context);
        sInstanceRef = new WeakReference<IMAudioManager>(instance);
        return instance;
    }

    private IMAudioManager(Context context) {
        mContext = context.getApplicationContext();
        mListeners = new CopyOnWriteArrayList<>();
    }

    public void registerVoicePlayListener(VoicePlayingListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void unregisterVoicePlayingListener(VoicePlayingListener listener) {
        mListeners.remove(listener);
    }

    private void initFilePath() {
        String basePath = Utils.getMianLiaoDir().getAbsolutePath();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String account = AccountInfo.getInstance(mContext).getAccount();
        String timeNow = sdf.format(new Date());
        mPathName = basePath + "/" + account;
        File voiceFile = new File(mPathName);
        if (voiceFile.isDirectory() || voiceFile.mkdir()) {
            mPathName += "/audio_" + timeNow + ".amr";
        }
    }

    public String getFilePath() {
        return mPathName;
    }

    public void startRecord() {
        // get file real path
        initFilePath();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setAudioEncodingBitRate(16);
        /**在录制的时候将语音缓存在sd卡上的文件中**/
        mRecorder.setOutputFile(mPathName);
        try {
            mRecorder.prepare();
            mRecorder.start(); // Recording is now started
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "record prepare() failed");
        }
        mIsRunning = true;
    }

    public void stopRecord() {
        if (mRecorder == null && mIsRunning) {
            return;
        }
        try {
            mRecorder.stop();
            mRecorder.reset(); // You can reuse the object by going back to
            // setAudioSource() step
            mRecorder.release(); // Now the object cannot be reused
            mRecorder = null;
        } catch (Exception e) {
        }
    }

    public void startPlayAudio(CfPost post) {
        if (post != null) {
            startPlayAudio(post.getVoicePath());
            for (VoicePlayingListener listener : mListeners) {
                listener.onVoicePlaying(post);
            }
        }
    }

    public void startPlayAudio(String filePath) {
        if (hasPlayer()) {
            stopPlayAudio();
        }
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(filePath);
            Utils.logD(TAG, "Start playing, which path:" + filePath);
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "play voice prepare failed");
        }
    }
    
    public void startPlayAudio(String filePath, OnCompletionListener listener) {
        if (hasPlayer()) {
            stopPlayAudio();
        }
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(filePath);
            Utils.logD(TAG, "Start playing, which path:" + filePath);
            mPlayer.prepare();
            mPlayer.start();
            setOnCompletionListener(listener);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "play voice prepare failed");
        }
    }

    public void startPlayAudio(CfPost post, OnCompletionListener listener) {
        if (post != null) {
            startPlayAudio(post.getVoicePath(), listener);
            for (VoicePlayingListener listen : mListeners) {
                listen.onVoicePlaying(post);
            }
        }
    }
    
    public int getTotalTime() {
        if (mPlayer == null) {
            return 0;
        }
        return mPlayer.getDuration();
    }

    public int getCurrentPostion() {
        if (mPlayer == null) {
            return 0;
        }
        return mPlayer.getCurrentPosition();
    }

    public void pausePlayAudio() {
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    public void continuePlayAudio() {
        if (mPlayer == null) {
            return;
        }
        try {
            mPlayer.start();
        } catch (Exception e) {
        }
    }
    
    public void stopPlayAudio() {
        if (mPlayer != null) {
            try {
                mPlayer.stop();
            } catch (Exception e) {
            }
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
    }

    public boolean hasFile() {
        return new File(mPathName).exists();
    }

    public boolean deleteAudio(String filePath) {
        File file = new File(filePath);
        if (file.isFile()) {
            return file.delete();
        }
        return false;
    }

    public int getAmplitude() {
        return mRecorder.getMaxAmplitude();
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    public boolean hasPlayer() {
        return mPlayer != null;
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        if (mPlayer != null) {
            mPlayer.setOnCompletionListener(listener);
        }
    }

    public interface VoicePlayingListener{
        void onVoicePlaying(CfPost post);
    }

}
