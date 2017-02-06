package com.tjut.mianliao.update;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.UpgradeDialog;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

/**
 * Check for app update.
 */
public class UpdateManager {
    private static final String TAG = "UpdateManager";

    private static final String UPDATE_API = "api/version";

    private static final String SHARED_PREF_NAME = "update";

    private static final String SP_LAST_CHECK_UPDATE = "last_update";

    private static final long ONE_DAY = 3 * 60 * 60 * 1000;

    private String mApkUrl;

    private Context mContext;

    private SharedPreferences mSharedPrefs;

    private UpgradeDialog mUpdateDialog;

    private int mCurrentVersionCode;

    private long mLastCheckUpdate;

    private boolean mIsDevDebug = true;

    public UpdateManager(Context context) {
        mContext = context;
        mApkUrl = mContext.getString(R.string.app_url);
        mSharedPrefs = mContext.getSharedPreferences(SHARED_PREF_NAME, 0);
        mLastCheckUpdate = mSharedPrefs.getLong(SP_LAST_CHECK_UPDATE, 0);
        mCurrentVersionCode = getVersionNumber(mContext);
    }

    public void checkForUpdate(boolean manualCheck, final OnCheckFinishedListener listener) {
        boolean envOK = true;
        if (mCurrentVersionCode == 0) {
            Utils.logD(TAG, "Can't get current app version!");
            envOK = false;
        }

        // check network connection
        if (envOK && !Utils.isNetworkAvailable(mContext)) {
            Utils.logD(TAG, "No network connection!");
            envOK = false;
        }

        // Check for update no more than 1 time per day
        if (envOK && !manualCheck && System.currentTimeMillis() - mLastCheckUpdate < ONE_DAY) {
            Utils.logD(TAG, "Don't check update too frequently.");
            envOK = false;
        }

        if (!envOK) {
            if (listener != null) {
                listener.onCheckFinished(false, false);
            }
            return;
        }

        mLastCheckUpdate = System.currentTimeMillis();
        mSharedPrefs.edit().putLong(SP_LAST_CHECK_UPDATE, mLastCheckUpdate).commit();

        productUpateCheck(listener);
    }

    private void productUpateCheck(final OnCheckFinishedListener listener) {
        new AdvAsyncTask<Void, Void, MsResponse>() {
            @Override
            protected MsResponse doInBackground(Void... params) {
                return HttpUtil.msGet(mContext, UPDATE_API, "", "");
            }

            @Override
            protected void onPostExecute(MsResponse response) {
                boolean checkUpdateSucceed = response != null && response.code == MsResponse.MS_SUCCESS;
                boolean hasUpdate = false;
                if (checkUpdateSucceed) {
                    try {
                        JSONObject jsonRes = new JSONObject(response.response);
                        int newVerCode = jsonRes.getInt("version_code");
                        if (newVerCode > mCurrentVersionCode) {
                            Utils.logD(TAG, "New version detected: " + newVerCode + " (Current: "
                                    + mCurrentVersionCode + ")");
                            hasUpdate = true;
                            if (listener != null && listener.isAlive) {
                                showUpdateDialog(jsonRes);
                            }
                        }
                    } catch (JSONException e) {}
                }
                if (listener != null) {
                    listener.onCheckFinished(checkUpdateSucceed, hasUpdate);
                }
            }
        }.executeLong();
    }

    private void showUpdateDialog(JSONObject jsonRes) throws JSONException {
        boolean isMajorUpdate = jsonRes.getBoolean("is_major_update");
        String desc = jsonRes.getString("description");
        String version = jsonRes.getString("version_name");

        getUpdateDialog(version, desc, isMajorUpdate).show();
    }

    private UpgradeDialog getUpdateDialog(String version, String desc, boolean isMajor) {
        if (mUpdateDialog == null) {
            mUpdateDialog = new UpgradeDialog(mContext);
            mUpdateDialog.setMessage(desc)
                .setNegativeButton(null, null)
                .setPositiveButton(null,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Utils.actionView(mContext,
                                    mApkUrl, null, Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                    });
        }
        mUpdateDialog.setVersion(version);

        return mUpdateDialog;
    }

    private int getVersionNumber(Context context) {
        PackageInfo pkgInfo = Utils.getPackageInfo(context);
        return pkgInfo == null ? 0 : pkgInfo.versionCode;
    }

    public abstract static class OnCheckFinishedListener {
        /**
         * Because it's checking update in a background thread, so it's
         * important to know if the caller (who calls checkUpdate) is still
         * alive. And when it gets the result, it can decide what to do.
         */
        public boolean isAlive = true;

        public void onCheckFinished(boolean success, boolean hasUpdate) {}
    }
}
