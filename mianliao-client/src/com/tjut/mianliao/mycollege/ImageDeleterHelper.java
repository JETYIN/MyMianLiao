package com.tjut.mianliao.mycollege;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class ImageDeleterHelper {

    private static WeakReference<ImageDeleterHelper> sInstanceRef;

    public static synchronized ImageDeleterHelper getInstance() {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        ImageDeleterHelper instance = new ImageDeleterHelper();
        sInstanceRef = new WeakReference<ImageDeleterHelper>(instance);
        return instance;
    }

    public void updateInfo(Activity activity, HashMap<String, String> parameters,
            HashMap<String, String> files, String editImage) {
        new UpdateInfoTask(activity, parameters, files, editImage).executeLong();
    }

    private class UpdateInfoTask extends MsMhpTask {

        private String mEditImage;
        private Activity mActivity;

        public UpdateInfoTask(Activity activity, HashMap<String, String> parameters,
                HashMap<String, String> files, String editImage) {
            super(activity, MsRequest.UPDATE_PROFILE, parameters, files);
            mEditImage = editImage;
            mActivity = activity;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            Utils.logD("UpdateProfileImage", "- update image succ");
            System.out.println();
            if (mEditImage != null) {
                File file = new File(mEditImage);
                if (file.exists()) {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(file);
                    intent.setData(uri);
                    mActivity.sendBroadcast(intent);
                    file.delete();
                    Utils.logD("UpdateProfileImage", "-- delete image succ--" + mEditImage);
                }
            }
        }

    }
}
