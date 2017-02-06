package com.tjut.mianliao.forum.nova;

import java.io.File;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.image.VideoPlayerActivity;
import com.tjut.mianliao.util.AliOSSHelper;
import com.tjut.mianliao.util.AliOSSHelper.OnUploadListener;
import com.tjut.mianliao.util.Utils;
import com.tjut.mianliao.videoplay.VideoPlayActivity;

public class ForumVideoPostActivity extends BasePostActivity implements OnUploadListener {

    public static final String EXT_VIDEO_PATH = "ext_video_path";
    public static final String EXT_VIDEO_LENGTH = "ext_video_length";
    public static final String EXT_VIDEO_THUMBNAIL = "ext_video_thumbnail";
    
    private static final String TAG = "ForumVideoPostActivity";
    
    private static final int MSG_SHOW_UPLOADING_DIALOG = 10;
    private static final int MSG_HIDE_UPLOADING_DIALOG = 11;
    
    private AliOSSHelper mAliOSSHelper;
    private LightDialog mCancleDialog;
    
    private ImageView mIvThumbnail; 

    @Override
    protected void showProgress() {
        super.showProgress();
    }

    private ImageView mIvStartPreview;
    private TextView mTvVideoSize;
    private TextView mTvVideoTime;

    private String mVideoPath;
    private String mVideoSize;
    private String mVideoTimeLength;
    private String mVideoThumbnnal;

    private String mVideoUrlKey;
    private String mVideoThumbnailKey;
    
    private boolean mUploadThumbnailSuccess;
    private boolean mUploadVideoSuccess;
    private int mThreadType = CfPost.THREAD_TYPE_VIDEO;
    
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_HIDE_UPLOADING_DIALOG:
                    Utils.hidePgressDialog();
                    if (isUploadFileSuccess()) {
                        submit();
                    }
                    break;
                case MSG_SHOW_UPLOADING_DIALOG:
                    Utils.showProgressDialog(ForumVideoPostActivity.this, R.string.fp_upload_files);
                    break;
                default:
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInflater.inflate(R.layout.post_video_view, mFlFooter);
        mAliOSSHelper = AliOSSHelper.getInstance(this);
        mIvThumbnail = (ImageView) findViewById(R.id.iv_thumbnail);
        mIvStartPreview = (ImageView) findViewById(R.id.iv_start_preview);
        mTvVideoSize = (TextView) findViewById(R.id.tv_video_size);
        mTvVideoTime = (TextView) findViewById(R.id.tv_video_time_length);
        mIvStartPreview.setOnClickListener(this);

        mVideoPath = getIntent().getStringExtra(EXT_VIDEO_PATH);
        mVideoThumbnnal = getIntent().getStringExtra(EXT_VIDEO_THUMBNAIL);
        fillVideoView();
        mHasVideo = true;
    }

    @SuppressLint("NewApi")
    private void fillVideoView() {
        mVideoSize = Utils.getAttSizeString(this, Utils.getFileSize(this, mVideoPath));
        int duration = getIntent().getIntExtra(EXT_VIDEO_LENGTH, 0);
        mVideoTimeLength = Utils.getTimeStrByInt(duration, 1);
        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(mVideoPath, Images.Thumbnails.MINI_KIND);
        BitmapDrawable thumDrawable = new BitmapDrawable(getResources(), thumbnail);
        if (mVideoThumbnnal == null) {
            mVideoThumbnnal = createFileName();
        }
        Utils.saveBitmap(thumbnail, mVideoThumbnnal);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mIvThumbnail.setBackground(thumDrawable);
        } else {
            mIvThumbnail.setBackgroundDrawable(thumDrawable);
        }
        mTvVideoSize.setText(mVideoSize);
        mTvVideoTime.setText(mVideoTimeLength);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        changeSendColor(true);
    }
    
    @Override
    public void onBackPressed() {
        if (mVideoPath != null) {
            showCancleDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                if (!isStateReady()) {
                    return;
                }
                startUploadAndPost();
                break;
            case R.id.iv_start_preview:
                startPlayVideo();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteFileIfExists(mVideoPath);
        deleteFileIfExists(mVideoThumbnnal);
    }

    private void deleteFileIfExists(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);
        if (file.exists()) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(file);
            intent.setData(uri);
            this.sendBroadcast(intent);
            file.delete();
        }
    }
    
    private void startUploadAndPost() {
        if(!Utils.isNetworkAvailable(this)){
            toast(R.string.no_network);
            return;
        }
        mUploadThumbnailSuccess = false;
        mUploadVideoSuccess = false;
        mAliOSSHelper.uploadThumbnail(new File(mVideoThumbnnal), this);
        mAliOSSHelper.uploadVideo(new File(mVideoPath), this);
        Message msg = new Message();
        msg.what = MSG_SHOW_UPLOADING_DIALOG;
        mHandler.sendMessage(msg);
    }
    
    @Override
    protected boolean isStateReady() {
    	return true;
    }
    
    private void showCancleDialog() {
        if (mCancleDialog == null) {
            mCancleDialog = new LightDialog(this);
            mCancleDialog.setTitleLd(R.string.course_time_clear)
                .setMessage(R.string.cf_post_video_cancle_notice_msg)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_OK);
                        finish();
                    }
                });
        }
        mCancleDialog.show();
    }
    
    private void startPlayVideo() {
         Intent intent = new Intent(this, VideoPlayActivity.class);
         intent.putExtra(VideoPlayerActivity.EXT_FILE_PATH, mVideoPath);
         startActivity(intent);
    }
    
    private String createFileName() {
        String basePath = getExternalCacheDir().getAbsolutePath();
        StringBuilder sb = new StringBuilder(basePath);
        sb.append("/video_thumbnail_");
        sb.append(System.currentTimeMillis());
        sb.append(".jpg");
        return sb.toString();
    }
    
    private void post() {
        if (isUploadFileSuccess()) {
            Message msg = new Message();
            msg.what = MSG_HIDE_UPLOADING_DIALOG;
            mHandler.sendMessage(msg);
        }
    }

    private boolean isUploadFileSuccess() {
        return mUploadThumbnailSuccess && mUploadVideoSuccess;
    }

    private String getVideoUrlJson() {
        StringBuilder sb = new StringBuilder("[");
        sb.append("{").append("\"").append(mVideoThumbnailKey)
            .append("\":\"").append(mVideoUrlKey).append("\"}");
        sb.append("]");
        return sb.toString();
    }
    
    @Override
    protected HashMap<String, String> getParams() {
        isContentReady();
        HashMap<String, String> params = super.getParams();
        params.put("thread_type", String.valueOf(mThreadType));
        params.put("video_url_json", getVideoUrlJson());
        return params;
    }

    @Override
    public void onUploadSuccess(File file, byte[] data, String url, String objectKey) {
        if (TextUtils.equals(file.getAbsolutePath(), mVideoThumbnnal)) {
            mUploadThumbnailSuccess = true;
            mVideoThumbnailKey = url;
        } else if (TextUtils.equals(file.getAbsolutePath(), mVideoPath)) {
            mUploadVideoSuccess = true;
            mVideoUrlKey = url;
        }
        post();
        Utils.logD(TAG, "upload file succ ： url = " + url + "--objectKey = " +objectKey);
    }

    @Override
    public void onUploadProgress(File file, byte[] data, int byteCount, int totalSize) {
        Utils.logD(TAG, "upload file ing ： byteCount = " + byteCount + "--totalSize = " +totalSize);
    }

    @Override
    public void onUploadFailure(File file, byte[] data, String errMsg) {
        Utils.logD(TAG, "upload file file errMsg = " +errMsg);
    }

    @Override
    protected boolean setCanRefFriend() {
        return true;
    }
}
