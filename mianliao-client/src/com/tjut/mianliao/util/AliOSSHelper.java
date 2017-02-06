package com.tjut.mianliao.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;

import org.json.JSONObject;

import android.content.Context;

import com.alibaba.sdk.android.oss.OSSService;
import com.alibaba.sdk.android.oss.OSSServiceProvider;
import com.alibaba.sdk.android.oss.callback.SaveCallback;
import com.alibaba.sdk.android.oss.model.OSSException;
import com.alibaba.sdk.android.oss.model.TokenGenerator;
import com.alibaba.sdk.android.oss.storage.OSSBucket;
import com.alibaba.sdk.android.oss.storage.OSSData;
import com.alibaba.sdk.android.oss.storage.OSSFile;
import com.tjut.mianliao.data.AccountInfo;

public class AliOSSHelper {

    public interface OnUploadListener {
        public void onUploadSuccess(File file, byte[] data, String url, String objectKey);
        public void onUploadProgress(File file, byte[] data, int byteCount, int totalSize);
        public void onUploadFailure(File file, byte[] data, String errMsg);
    }

    private static final String OSS_BUCKET_POST = "tjt-post";
    private static final String OSS_BUCKET_IMAGE = "tjut-image";
    private static final String OSS_URL_HEADER_POST = "tjt-post.oss-cn-hangzhou.aliyuncs.com";
    private static final String OSS_URL_HEADER_IMAGE = "image.tjut.cc/assets/upload/cf";
    
    private static final int TYPE_FILE = 1;
    private static final int TYPE_IMAGE = 2;
    private static final int TYPE_VOICE = 3;
    private static final int TYPE_VIDEO = 4;
    private static final int TYPE_VIDEO_THUMBNAIL = 5;

    private static WeakReference<AliOSSHelper> sInstanceRef;

    private Context mContext;
    private OSSService mOssService;
    
    private int mUserId;

    public static synchronized AliOSSHelper getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        AliOSSHelper instance = new AliOSSHelper(context);
        sInstanceRef = new WeakReference<AliOSSHelper>(instance);
        return instance;
    }

    private AliOSSHelper(Context context) {
        mContext = context.getApplicationContext();
        mOssService = OSSServiceProvider.getService();
        mOssService.setApplicationContext(mContext);
        mOssService.setGlobalDefaultTokenGenerator(new TokenGenerator() {
            @Override
            public String generateToken(String httpMethod, String md5,
                    String type, String date, String ossHeaders, String resource) {
                String content = new StringBuilder(httpMethod).append("\n")
                        .append(md5).append("\n")
                        .append(type).append("\n")
                        .append(date).append("\n")
                        .append(ossHeaders).append(resource)
                        .toString();
                String params = new StringBuilder("content=")
                        .append(Utils.urlEncode(content)).toString();
                MsResponse response = HttpUtil.msGet(mContext, MsRequest.OSS_TOKEN, params);

                return response.isSuccessful()
                        ? response.getJsonObject().optString("token") : null;
            }
        });
        AccountInfo accountInfo = AccountInfo.getInstance(context);
        mUserId = accountInfo.getUserId();
    }

    public void uploadFile(File file, OnUploadListener listener) {
        upload(TYPE_FILE, file, listener);
    }

    public void uploadFile(byte[] data, OnUploadListener listener) {
        upload(TYPE_FILE, data, listener);
    }

    public void uploadImage(File file, OnUploadListener listener) {
        upload(TYPE_IMAGE, file, listener);
    }

    public void uploadImage(byte[] data, OnUploadListener listener) {
        upload(TYPE_IMAGE, data, listener);
    }

    public void uploadVoice(File file, OnUploadListener listener) {
        upload(TYPE_VOICE, file, listener);
    }

    public void uploadVoice(byte[] data, OnUploadListener listener) {
        upload(TYPE_VOICE, data, listener);
    }

    public void uploadVideo(File file, OnUploadListener listener) {
        upload(TYPE_VIDEO, file, listener);
    }
    
    public void uploadVideo(byte[] data, OnUploadListener listener) {
        upload(TYPE_VIDEO, data, listener);
    }    
    
    public void uploadThumbnail(File file, OnUploadListener listener) {
        upload(TYPE_VIDEO_THUMBNAIL, file, listener);
    }
    
    public void uploadThumbnail(byte[] data, OnUploadListener listener) {
        upload(TYPE_VIDEO_THUMBNAIL, data, listener);
    }
    
    private void upload(int type, File file, OnUploadListener listener) {
        new UploadTask(type, listener).setFile(file).executeLong();
    }

    private void upload(int type, byte[] data, OnUploadListener listener) {
        new UploadTask(type, listener).setData(data).executeLong();
    }
    
    private boolean shouldRequestServer(int type) {
        switch (type) {
            case TYPE_FILE:
            case TYPE_IMAGE:
            case TYPE_VOICE:
                return true;
            default:
                return false;
        }
    }

    private class UploadTask extends MsTask {
        private int mType;
        private OnUploadListener mListener;
        private File mFile;
        private byte[] mData;
        private boolean mShouldRequestServer;

        public UploadTask(int type, OnUploadListener listener) {
            super(mContext, shouldRequestServer(type) ? MsRequest.IM_PREPARE_UPLOAD : (MsRequest) null);
            mType = type;
            mListener = listener;
            mShouldRequestServer = shouldRequestServer(mType);
        }
        
        @Override
        protected MsResponse doInBackground(Void... params) {
            if (!mShouldRequestServer) {
                MsResponse response = new MsResponse();
                response.code = MsResponse.MS_SUCCESS;
                return response;
            } else {
                return super.doInBackground(params);
            }
        }

        public UploadTask setFile(File file) {
            mFile = file;
            return this;
        }

        public UploadTask setData(byte[] data) {
            mData = data;
            return this;
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder("type=").append(mType);
            if (mFile != null) {
                /**返回文件最后的名字**/
                sb.append("&filename=").append(Utils.urlEncode(mFile.getName()));
                if (mType == TYPE_IMAGE) {
                    int[] size = Utils.getImageSize(mFile.getAbsolutePath());
                    sb.append("&width=").append(size[0]).append("&height=").append(size[1]);
                }
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                OSSBucket ossBucket;
                String objectKey;
                String url;
                if (mShouldRequestServer) {
                    JSONObject json = response.getJsonObject();
                    ossBucket = mOssService.getOssBucket(json.optString("oss_bucket"));
                    objectKey = json.optString("object_key");
                    url = json.optString("url");
                } else {
                    ossBucket = getOssBucket(mType);
                    url = getUrl(mType, mFile.getAbsolutePath());
                    objectKey = getObjectKey(url);
                }
                if (mFile != null) {
                    ossUploadFile(ossBucket, objectKey, url);
                }
                if (mData != null) {
                    ossUploadData(ossBucket, objectKey, url);
                }
            } else if (mListener != null) {
                mListener.onUploadFailure(mFile, mData, "Prepare upload failed");
            }
        }

        private void ossUploadFile(OSSBucket ossBucket, String objectKey, String url) {
            OSSFile ossFile = mOssService.getOssFile(ossBucket, objectKey);
            try {
                ossFile.setUploadFilePath(mFile.getAbsolutePath(), "raw/binary");
            } catch (FileNotFoundException e) {
                if (mListener != null) {
                    mListener.onUploadFailure(mFile, mData, e.getMessage());
                }
                return;
            }
            ossFile.ResumableUploadInBackground(createSaveCallback(url));
        }

        private void ossUploadData(OSSBucket ossBucket, String objectKey, String url) {
            OSSData ossData = mOssService.getOssData(ossBucket, objectKey);
            ossData.setData(mData, "raw/binary");
            ossData.uploadInBackground(createSaveCallback(url));
        }

        private SaveCallback createSaveCallback(final String url) {
            return new SaveCallback() {

                @Override
                public void onSuccess(String objectKey) {
                    if (mListener != null) {
                        mListener.onUploadSuccess(mFile, mData, url, objectKey);
                    }
                }

                @Override
                public void onProgress(String objectKey, int byteCount, int totalSize) {
                    if (mListener != null) {
                        mListener.onUploadProgress(mFile, mData, byteCount, totalSize);
                    }
                }

                @Override
                public void onFailure(String objectKey, OSSException ossException) {
                    if (mListener != null) {
                        mListener.onUploadFailure(mFile, mData, ossException.getMessage());
                    }
                }
            };
        }
    }

    public OSSBucket getOssBucket(int type) {
        switch (type) {
            case TYPE_IMAGE:
            case TYPE_VIDEO_THUMBNAIL:
                return new OSSBucket(OSS_BUCKET_IMAGE);
            case TYPE_VOICE:
            case TYPE_VIDEO:
                return new OSSBucket(OSS_BUCKET_POST);
            default:
                return new OSSBucket(OSS_BUCKET_POST);
        }
    }
    

    public String getUrl(int type, String fileName) {
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case TYPE_IMAGE:
            case TYPE_VIDEO_THUMBNAIL:
                sb.append(OSS_URL_HEADER_IMAGE);
                break;
            case TYPE_VOICE:
                sb.append(OSS_URL_HEADER_POST).append("/sound");
                break;
            case TYPE_VIDEO:
                sb.append(OSS_URL_HEADER_POST).append("/video");
                break;
            default:
                break;
        }
        sb.append("/").append(Utils.getYear());
        sb.append("/").append(Utils.getMonth());
        sb.append("/").append(Utils.getDate());
        sb.append("/").append(mUserId).append("_");
        sb.append(System.currentTimeMillis());
        sb.append(Utils.getFilePostfix(fileName));
        return sb.toString();
    }

    public String getObjectKey(String url) {
        int index = url.indexOf("/");
        return url.substring(index + 1, url.length());
    }
    
}
