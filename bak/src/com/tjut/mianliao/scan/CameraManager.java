package com.tjut.mianliao.scan;

import java.util.List;

import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.tjut.mianliao.util.Utils;

/**
 * Camera manager which helps manage camera lifecycle(start, stop, etc). Main
 * purpose is used to scan QR code, Bar code, etc (whichever ZXing lib support).
 * Considering the use case, only back-facing camera with portrait mode(rotation
 * = 90) is supported.
 */
public class CameraManager {
    private static final String TAG = "CameraManager ";

    private Camera mCamera;

    public CameraManager() {
        init();
    }

    public boolean init() {
        mCamera = getCameraInstance();
        if (mCamera == null) {
            return false;
        }

        try {
            mCamera.setDisplayOrientation(90);

            Camera.Parameters parameters = mCamera.getParameters();
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                mCamera.setParameters(parameters);
            }
            return true;
        } catch (Exception e) {
            Utils.logD(TAG, e.getMessage());
        }

        return false;
    }

    public boolean isCameraReady() {
        return mCamera != null;
    }

    public boolean startPreview(SurfaceHolder holder) {
        if (mCamera == null || holder == null) {
            return false;
        }

        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            return true;
        } catch (Exception e) {
            Utils.logD(TAG, "Error starting camera preview: " + e.getMessage());
            return false;
        }
    }

    public void getNextPreview(Camera.PreviewCallback callback) {
        if (mCamera != null) {
            mCamera.setOneShotPreviewCallback(callback);
        }
    }

    public Camera.Size getPreviewSize() {
        return mCamera == null ? null : mCamera.getParameters().getPreviewSize();
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

    public void release() {
        if (mCamera != null) {
            Camera camera = mCamera;
            mCamera = null;
            stopPreview();
            camera.release();

        }
    }

    private static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Utils.logD(TAG, e.getMessage());
        }
        return c; // returns null if camera is unavailable
    }
}
