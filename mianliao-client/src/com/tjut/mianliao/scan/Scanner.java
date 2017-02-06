package com.tjut.mianliao.scan;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.tjut.mianliao.R;
import com.tjut.mianliao.scan.assist.ScanResultAssist;
import com.tjut.mianliao.util.AdvAsyncTask;

public class Scanner implements Camera.PreviewCallback, SurfaceHolder.Callback {

    private int mScanWidth;
    private int mScanHeight;

    private MediaPlayer mMediaPlayer;

    private Activity mActivity;
    private CameraManager mCameraManager;
    private SurfaceHolder mHolder;
    private ScanCoverView mScvCover;
    private ScanIndicatorView mIndicator;

    private int mPreviewWidth;
    private int mPreviewHeight;
    private Rect mScanRect = new Rect();

    private boolean mActiveFlag = false;

    @SuppressWarnings("deprecation")
    public Scanner(Activity activity, SurfaceView preview, ScanCoverView cover, ScanIndicatorView indicator) {
        mActivity = activity;

        mCameraManager = new CameraManager();
        if (!mCameraManager.isCameraReady()) {
            Toast.makeText(mActivity, R.string.scan_camera_not_available, Toast.LENGTH_SHORT).show();
            mActivity.finish();
            return;
        }

        mMediaPlayer = MediaPlayer.create(mActivity, R.raw.scan_notice);

        Resources res = mActivity.getResources();
        mScanWidth = res.getDimensionPixelSize(R.dimen.scan_box_width);
        mScanHeight = res.getDimensionPixelSize(R.dimen.scan_box_height);

        mHolder = preview.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mScvCover = cover;
        mIndicator = indicator;
    }

    public void start() {
        if (!mCameraManager.isCameraReady() && !mCameraManager.init()) {
            Toast.makeText(mActivity, R.string.scan_camera_not_available, Toast.LENGTH_SHORT).show();
            mActivity.finish();
            return;
        }

        mActiveFlag = true;

        mScvCover.setCoverActive(true);
        mIndicator.start();

        decodeNextPreview();
    }

    private void decodeNextPreview() {
        try {
            mCameraManager.getNextPreview(this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mActivity, R.string.scan_camera_not_available, Toast.LENGTH_SHORT).show();
            mActivity.finish();
        }
    }

    public void stop() {
        if (!mActiveFlag) {
            return;
        }
        mActiveFlag = false;
        mScvCover.setCoverActive(false);
        mIndicator.stop();
    }

    public void destroy() {
        mActiveFlag = false;
        mCameraManager.release();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        if (mHolder != null) {
            mHolder = null;
        }
    }

    public boolean isActive() {
        return mActiveFlag;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) { }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null || !mCameraManager.isCameraReady()) {
            return;
        }

        mCameraManager.stopPreview();
        if (!mCameraManager.startPreview(mHolder)) {
            return;
        }

        prepareDecodeArea(width, height);

        setupCover(width, height);

        start();
    }

    private void prepareDecodeArea(int width, int height) {
        // It's a tricky thing because when you rotated the camera, it doesn't pass rotated preview data.
        // So you have to map the screen area to the actual preview area.
        Camera.Size size = mCameraManager.getPreviewSize();
        mPreviewWidth = size.width;
        mPreviewHeight = size.height;

        int winHeight = mScanWidth * mPreviewHeight / width;
        int winWidth = mScanHeight * mPreviewWidth / height;

        if (size.width > winWidth) {
            mScanRect.left = (size.width - winWidth) / 2;
            mScanRect.right = mScanRect.left + winWidth;
        } else {
            mScanRect.left = 0;
            mScanRect.right = size.width;
        }

        if (size.height > winHeight) {
            mScanRect.top = (size.height - winHeight) / 2;
            mScanRect.bottom = mScanRect.top + winHeight;
        } else {
            mScanRect.top = 0;
            mScanRect.bottom = size.height;
        }
    }

    private void setupCover(int width, int height) {
        RectF ra = new RectF();
        if (width > mScanWidth) {
            ra.left = (width - mScanWidth) / 2f;
            ra.right = ra.left + mScanWidth;
        } else {
            ra.left = 0;
            ra.right = width;
        }

        if (height > mScanHeight) {
            ra.top = (height - mScanHeight) / 2f;
            ra.bottom = ra.top + mScanHeight;
        } else {
            ra.top = 0;
            ra.bottom = height;
        }

        mScvCover.setScanRect(ra);
        mScvCover.setCoverActive(true);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCameraManager.release();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (isActive() && mCameraManager.isCameraReady()) {
            new DecodeTask().executeQuick(data);
        }
    }

    private class DecodeTask extends AdvAsyncTask<byte[], Void, Result> {

        @Override
        protected Result doInBackground(byte[]... params) {
            if (params == null || params.length == 0 || params[0] == null || params[0].length == 0) {
                return null;
            }

            // Stupid camera doesn't passed rotated preview data when you rotated it.
            // To get barcode decoded we need to rotate the src, because we've rotated the camera by 90 degrees.
            byte[] src = params[0];
            byte[] rotatedData = new byte[src.length];
            for (int y = 0; y < mPreviewHeight; y++) {
                for (int x = 0; x < mPreviewWidth; x++) {
                    rotatedData[x * mPreviewHeight + mPreviewHeight - y - 1] = src[x + y * mPreviewWidth];
                }
            }

            // Since the source is rotated, switch the width/height for the parameters.
            final PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(rotatedData, mPreviewHeight,
                    mPreviewWidth, mScanRect.top, mScanRect.left, mScanRect.height(), mScanRect.width(), false);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result rawResult = null;
            try {
                rawResult = new MultiFormatReader().decodeWithState(bitmap);
            } catch (NotFoundException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e1) {
                e1.printStackTrace();
            }

            return rawResult;
        }

        @Override
        protected void onPostExecute(Result s) {
            if (!isActive()) {
                return;
            }

            if (s == null) {
                decodeNextPreview();
            } else {
                stop();
                mMediaPlayer.start();
                ScanResultAssist.handle(mActivity, Scanner.this, s);
            }
        }
    }
}
