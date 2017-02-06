package com.tjut.mianliao.util;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

import com.tjut.mianliao.settings.Settings;

/**
 * This is intended to download/manage online image files.
 */
public class FileDownloader {

    private static final String TAG = "FileDownloader";

    public static final String IMAGE_CACHE = "/image_cache/";

    private static WeakReference<FileDownloader> mInstanceRef;

    private Context mContext;

    private String mCacheFolder;
    private Map<String, String> mDownloadedFiles = new Hashtable<String, String>();

    private Map<String, DownloadFileTask> mQueuedTasks = new Hashtable<String, DownloadFileTask>();
    private Map<String, DownloadFileTask> mOnGoingTasks = new Hashtable<String, DownloadFileTask>();

    private DownloadFileTask mCurrentTask;

    public static synchronized FileDownloader getInstance(Context context) {
        if (mInstanceRef != null && mInstanceRef.get() != null) {
            return mInstanceRef.get();
        }

        FileDownloader instance = new FileDownloader(context);
        mInstanceRef = new WeakReference<FileDownloader>(instance);
        return instance;
    }

    public static void destroyInstance() {
        if (mInstanceRef != null) {
            FileDownloader downloader = mInstanceRef.get();
            if (downloader != null) {
                downloader.stopDownloading();
                mInstanceRef.clear();
            }
        }
    }

    private FileDownloader(Context context) {
        // Use app context to avoid activity leaking
        mContext = context.getApplicationContext();
        mCacheFolder = context.getCacheDir().getAbsolutePath() + IMAGE_CACHE;

        File folder = new File(mCacheFolder);
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    mDownloadedFiles.put(f.getName(), f.getAbsolutePath());
                }
            }
        } else if (!folder.mkdir()) {
            Utils.logD(TAG, "make cache folder failed.");
        }
    }

    /**
     * @param url           The url to get the file.
     * @param queueDownload If a download is required, the download will be executed one by
     *                      one if queueDownload is true.
     * @return The absolute path if the file is found; null otherwise.
     */
    public void getFile(String url, Callback callback, boolean queueDownload) {
        if (callback == null || TextUtils.isEmpty(url)) {
            return;
        }
        String key = getKey(url);
        String fileName = mDownloadedFiles.get(key);
        if (!TextUtils.isEmpty(fileName)) {
            // Ensure the file exists in cache folder
            if (fileExists(fileName)) {
                callback.onResult(true, url, fileName);
                return;
            } else {
                File cacheFolder = new File(mCacheFolder);
                if (!cacheFolder.isDirectory() && !cacheFolder.mkdir()) {
                    callback.onResult(false, url, fileName);
                    return;
                }
            }
        }

        if (Settings.getInstance(mContext).downloadPicturesWithWifi() &&
                !Utils.isWifiConnected(mContext)) {
            mQueuedTasks.clear();
            callback.onResult(false, url, fileName);
            return;
        }

        if (!mOnGoingTasks.containsKey(key)) {
            fileName = mCacheFolder + key;
            DownloadFileTask task = new DownloadFileTask(url, fileName, callback);
            mOnGoingTasks.put(key, task);
            if (queueDownload) {
                mQueuedTasks.put(key, task);
                startNextTask();
            } else {
                task.executeLong();
            }
        } else {
            DownloadFileTask task = mOnGoingTasks.get(key);
            task.addCallback(callback);
        }
    }

    /**
     * @param url
     * @return The full file name of the file which downloaded from the url. However, this method
     * doesn't guarantee the file is already downloaded.
     */
    public String getFileName(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        return mCacheFolder + getKey(url);
    }

    public boolean isDownloaded(String url) {
        return mDownloadedFiles.containsKey(getKey(url));
    }

    private boolean fileExists(String fileName) {
        return new File(fileName).isFile();
    }

    private String getKey(String url) {
        int index;
        if (File.separator != null) {
            index = url.lastIndexOf(File.separator);
        } else {
            index = -1;
        }
        if (index != -1) {
            return url.substring(index + 1);
        }
        // baidu map url
        if (url.indexOf("api.map.baidu.com/staticimage") > 0) {
            return url.hashCode() + ".png";
        }
        return url;
    }

    private void startNextTask() {
        if (mCurrentTask == null && mQueuedTasks.size() > 0) {
            for (DownloadFileTask task : mQueuedTasks.values()) {
                mCurrentTask = task;
                mCurrentTask.executeLong();
                break;
            }
        }
    }

    public void stopDownloading() {
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
        mCurrentTask = null;
        mQueuedTasks.clear();
        for (DownloadFileTask task : mOnGoingTasks.values()) {
            task.cancel(true);
        }
        mOnGoingTasks.clear();
    }

    private class DownloadFileTask extends AdvAsyncTask<Void, Void, Boolean> {

        private String mUrl;
        private String mFileName;
        private ArrayList<Callback> mCallbacks = new ArrayList<Callback>();

        protected DownloadFileTask(String url, String fileName, Callback callback) {
            mUrl = url;
            mFileName = fileName;
            addCallback(callback);
            Utils.logD(TAG, "Download prepare:" + mUrl);
        }

        protected void addCallback(Callback callback) {
            if (callback != null) {
                mCallbacks.add(callback);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Utils.logD(TAG, "Download start:" + mUrl);
            return HttpUtil.downLoad(mContext, Utils.getFullUrl(mUrl), mFileName);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            String key = getKey(mUrl);
            if (success) {
                mDownloadedFiles.put(key, mFileName);
            }

            for (Callback callback : mCallbacks) {
                callback.onResult(success, mUrl, mFileName);
            }

            mOnGoingTasks.remove(key);
            if (mQueuedTasks.containsKey(key)) {
                mQueuedTasks.remove(key);
                mCurrentTask = null;
                startNextTask();
            }
        }
    }

    public interface Callback {
        void onResult(boolean success, String url, String fileName);
    }

}
