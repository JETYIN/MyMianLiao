package com.tjut.mianliao.im;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.util.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class IMVideoManager implements OnErrorListener {

    private static final String TAG = "IMVideoManager";

    private static WeakReference<IMVideoManager> sInstanceRef;

    private String mPathName;

    private Context mContext;
    private Activity mActivity;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;

    private SurfaceHolder mSurfaceHolder;

    private static Camera mCamera;
    private Parameters mParameters;

    private boolean mIsOpenFlashLight;
    private boolean mIsRecording;
    private boolean mIsCameraBack;
    private boolean mHasFile;

    private int mCameraPosition = 1;
    private int mOrientation;
    private int mDegrees;

    public static synchronized IMVideoManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        IMVideoManager instance = new IMVideoManager(context);
        sInstanceRef = new WeakReference<IMVideoManager>(instance);
        return instance;
    }

    private IMVideoManager(Context context) {
        mContext = context.getApplicationContext();
        // get sensor manager
//        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
//        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        sensorManager.registerListener(mSensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME);
        getCameraInstance();
    }

    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    public void onDestroy() {
        mPathName = null;
        release();
    }

    public void onPause() {
        release();
        if (isPlaying()) {
            stopPlayVideo();
        }
    }

    private static Camera getCameraInstance() {
        if (mCamera != null) {
            return mCamera;
        }
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            return mCamera;
        } catch (Exception e) {
            Utils.logD(TAG, e.getMessage());
            return null;
        }
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public boolean isCameraReady() {
        return mCamera != null;
    }

    public boolean startPreview(SurfaceHolder holder) {
        mCamera = getCameraInstance();
        if (mCamera == null || holder == null) {
            return false;
        }
        try {
            setCameraParams();
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void stopPreview() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
            } catch (Exception e) {
                Utils.logD(TAG, e.getMessage());
            }
        }
    }

    public void setSurfaceHolder(SurfaceHolder holder) {
        mSurfaceHolder = holder;
    }

    @SuppressLint("SimpleDateFormat")
    private void initFilePath() {
        String basePath = Utils.getMianLiaoDir().getAbsolutePath();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String account = AccountInfo.getInstance(mContext).getAccount();
        String timeNow = sdf.format(new Date());
        mPathName = basePath + "/" + account;
        File voiceFile = new File(mPathName);
        if (voiceFile.isDirectory() || voiceFile.mkdir()) {
            mPathName += "/video_" + timeNow + ".mp4";
        }
    }

    @SuppressLint("SimpleDateFormat")
    public String getFilePath() {
        return mPathName;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public Bitmap getThumilbnail() {
        return ThumbnailUtils.createVideoThumbnail(mPathName, Images.Thumbnails.MINI_KIND);
    }

    public Bitmap getThumilbnail(String path) {
        return ThumbnailUtils.createVideoThumbnail(path, Images.Thumbnails.MINI_KIND);
    }

    public void startRecord() {
        // get file real path
        initFilePath();
        mCamera = getCameraInstance();
        if (mCamera == null) {
            Toast.makeText(mContext, "相机初始化出错!", Toast.LENGTH_SHORT).show();
            return;
        }
//        setCameraDisplayOrientation(mCameraPosition, mCamera);
        mCamera.setDisplayOrientation(90);
        setCameraParams();
        mCamera.unlock();

        mRecorder = new MediaRecorder();
        mRecorder.setOnErrorListener(this);
        mRecorder.setCamera(mCamera);

        mRecorder.setOrientationHint(90);// 视频旋转90度
        // 设置录制视频源为Camera(相机)
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        // 设置录制的视频编码h263 h264
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        // CamcorderProfile mProfile =
        // CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
        // 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
        mRecorder.setVideoSize(1280, 720);
        // 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
        mRecorder.setVideoFrameRate(60);
        mRecorder.setAudioEncodingBitRate(16);
        mRecorder.setVideoEncodingBitRate(3000000);
        mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        // 设置视频文件输出的路径
        mRecorder.setOutputFile(mPathName);

        try {
            mRecorder.prepare();
            mRecorder.start(); // Recording is now started
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "record prepare() failed");
        }
        mIsRecording = true;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void setCameraDisplayOrientation(int cameraId, Camera camera) {
        // See android.hardware.Camera.setCameraDisplayOrientation for
        // documentation.
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + mDegrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - mDegrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
        System.out.println("-------- result : " + result + ",degress:" + mDegrees);
    }

    public void stopRecord() {
        if (mRecorder == null && mIsRecording) {
            return;
        }
        try {
            mRecorder.stop();
            mRecorder.reset(); // You can reuse the object by going back to
            // setAudioSource() step
            mRecorder.release(); // Now the object cannot be reused
            mRecorder.setOnErrorListener(null);
            mCamera.release();
            mRecorder = null;
            mCamera = null;
            mHasFile = true;
        } catch (Exception e) {
        }
    }

    public void release() {
        if (mCamera == null) {
            return;
        }
        stopPreview();
        mCamera.release();
        mCamera = null;
        if (mRecorder != null) {
            mRecorder.reset();
            mRecorder.release();
            mRecorder.setOnErrorListener(null);
            mRecorder = null;
        }
    }

    public int getTotalTime() {
        if (mPlayer == null) {
            return 0;
        }
        return mPlayer.getDuration();
    }

    public boolean deleteVideo() {
        if (!hasFile()) {
            Toast.makeText(mContext, "您要删除的文件不存在!", Toast.LENGTH_SHORT).show();
            return false;
        }
        File file = new File(mPathName);
        if (file.isFile()) {
            if (file.delete()) {
                mHasFile = false;
                return true;
            }
        }
        return false;
    }

    public boolean deleteVideo(String filePath) {
        File file = new File(filePath);
        if (file.isFile()) {
            if (file.delete()) {
                mHasFile = false;
                return true;
            }
        }
        return false;
    }

    public int getAmplitude() {
        return mRecorder.getMaxAmplitude();
    }

    public boolean isPlaying() {
        return mPlayer != null && mPlayer.isPlaying();
    }

    public boolean hasPlayer() {
        return mPlayer != null;
    }

    public boolean hasFile() {
        if (mPathName == null) {
            return false;
        }
        return mHasFile && new File(mPathName).exists();
    }

    public boolean isOpenFlashLight() {
        return mIsOpenFlashLight;
    }

    public void openFlashLight() {
        mCamera = getCameraInstance();
        mCamera.startPreview();
        mParameters = mCamera.getParameters();
        mParameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(mParameters);
        mIsOpenFlashLight = true;
    }

    public void closeFlashLight() {
        mParameters = mCamera.getParameters();
        mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(mParameters);
        mIsOpenFlashLight = false;
    }

    public void toggleCamera() {
        if (mIsCameraBack) {
            mIsCameraBack = false;
        } else {
            mIsCameraBack = true;
        }

        mCamera = getCameraInstance();
        int cameraCount = 0;
        CameraInfo cameraInfo = new CameraInfo();
        cameraCount = Camera.getNumberOfCameras();

        if (cameraCount == 1) {
            Toast.makeText(mContext, "您的手机不支持摄像头切换", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < cameraCount; i++) {

            Camera.getCameraInfo(i, cameraInfo);
            if (mCameraPosition == 1) {
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                    mCamera = Camera.open(i);
                    try {
                        mCamera.setPreviewDisplay(mSurfaceHolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mCamera.startPreview();
                    mCamera.setDisplayOrientation(90);
                    mCameraPosition = 0;
                    break;
                }
            } else {
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                    mCamera = Camera.open(i);
                    try {
                        mCamera.setPreviewDisplay(mSurfaceHolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mCamera.startPreview();
                    mCamera.setDisplayOrientation(90);
                    mCameraPosition = 1;
                    break;
                }
            }
        }
    }

    public void startPlayVideo() {
        if (!hasFile()) {
            Toast.makeText(mContext, "文件不存在或已损坏!", Toast.LENGTH_SHORT).show();
            return;
        }
        mPlayer = new MediaPlayer();

        try {
            mPlayer.reset();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDisplay(mSurfaceHolder);
            mPlayer.setDataSource(mPathName);
            mPlayer.prepare();
            mPlayer.start();
            Utils.logD("VideoDuration", "get video duration = " + mPlayer.getDuration());
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    public void startPlayVideo(String filePath) {
        if (filePath == null || filePath.length() < 1 || !new File(filePath).exists()) {
            Toast.makeText(mContext, "文件不存在或已损坏!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (hasPlayer()) {
            stopPlayVideo();
        }
        mPlayer = new MediaPlayer();
        try {
            mPlayer.reset();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDisplay(mSurfaceHolder);
            Utils.logD(TAG, "start play video:" + mSurfaceHolder);
            mPlayer.setDataSource(filePath);
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception e) {
            Log.e(TAG, "paly video error : " + e.toString());
            e.printStackTrace();
        }
    }

    public void pausePlayVideo() {
        try {
            mPlayer.pause();
        } catch (Exception e) {
            Utils.logE(TAG, "pause play video error : " + e.getMessage());
        }
    }

    public void setCameraParams() {
        if (mCamera != null) {
            Parameters params = mCamera.getParameters();
            List<String> list = params.getSupportedFocusModes();
            if (list.contains(Parameters.FOCUS_MODE_AUTO)) {
                params.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            params.set("orientation", "portrait");
            mCamera.setParameters(params);
        }
    }

    public void stopPlayVideo() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        if (mPlayer != null) {
            mPlayer.setOnCompletionListener(listener);
        }
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        try {
            if (mr != null)
                mr.reset();
        } catch (Exception e) {
            Utils.logE(TAG, "stopRecord : " + e.getMessage());
        }

    }

    SensorEventListener mSensorEventListener = new SensorEventListener() {

        float x, y, z;

        public void onSensorChanged(SensorEvent e) {
            x = e.values[SensorManager.DATA_X];
            y = e.values[SensorManager.DATA_Y];
            z = e.values[SensorManager.DATA_Z];
            Utils.logD(TAG, "on sensor changed x=" + x + ",y=" + y + ",z=" + z);
            if (y >= 5) {
                mDegrees = 270;
                Utils.logD(TAG, "screen keep  top ");
            }
            if (x >= 5) {
                mDegrees = 0;  // ok
                Utils.logD(TAG, "screen keep  left ");
            }
            if (y <= -5) {
                mDegrees = 180;
                Utils.logD(TAG, "screen keep  bottom ");
            }

            if (x <= -5) {
                mDegrees = 90;
                Utils.logD(TAG, "screen keep  right ");
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    public void setCameraPosition(int tag) {
        mCameraPosition = tag;
    }

    public int getCameraPosition() {
        return mCameraPosition;
    }
}
