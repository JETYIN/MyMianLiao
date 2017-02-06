package com.tjut.mianliao.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.tjut.mianliao.R;
import com.tjut.mianliao.im.IMVideoManager;


/**
 *  this class will help us get the local video, and
 *  we should get the local path with video, and 
 *  return to the used area
 *
 */
public class GetLocalVideoHelper {
    
    private static final int REQUEST_GET_VIDEO = 91;

    private static final String TAG = "GetLocalVideoHelper";

    protected static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 10M
    
    private IMVideoManager mVideoManager;
    
    private VideoResultListener mListener;
    
    private Activity mActivity;
    
    private String mTempThumbnailFile;

    public GetLocalVideoHelper(Activity activity,VideoResultListener listener){
        mActivity = activity;
        mListener = listener;
        mVideoManager = IMVideoManager.getInstance(activity);
    }
    
    public void getLocalVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        mActivity.startActivityForResult(intent, REQUEST_GET_VIDEO);
    }
    
    public boolean handleResult(int requestCode, Intent data) {
        if (!checkStorage()) {
            return false;
        }
        if (requestCode == REQUEST_GET_VIDEO) {
            if (data != null && data.getData() != null) {
                String videoPath = data.getData().getPath();
                Utils.logD(TAG, "get local video path : " + videoPath);
                mTempThumbnailFile = createFileName();
                Bitmap bitmap = mVideoManager.getThumilbnail(videoPath);
                if (isFileTooLarge(videoPath)) {
                    mListener.onVideoResult(false, videoPath, mTempThumbnailFile);
                    return true;
                }
                if (saveBitmap(bitmap, mTempThumbnailFile)) {
                    mListener.onVideoResult(true, videoPath, mTempThumbnailFile);
                }
            }
        }
        return true;
    }
    
    private boolean isFileTooLarge(String filePath) {
        return new File(filePath).length() > MAX_FILE_SIZE;
    }
    
    private String createFileName() {
        String basePath = mActivity.getExternalCacheDir().getAbsolutePath();
        StringBuilder sb = new StringBuilder(basePath);
        sb.append("/video_thumbnail_");
        sb.append(System.currentTimeMillis());
        sb.append(".jpg");
        return sb.toString();
    }

    public static boolean saveBitmap(Bitmap bitmap, String fileName) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fileName);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
            return true;
        } catch (IOException e) {
            Utils.logE(TAG, "save bitmap error : " + e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }
    
    private boolean checkStorage() {
        if (!Utils.isExtStorageAvailable()) {
            Toast.makeText(mActivity, R.string.storage_not_available, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    
    public interface VideoResultListener {
        public void onVideoResult(boolean success, String videoFile, String thumbnailFile);
    }
}