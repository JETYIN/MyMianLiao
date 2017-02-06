package com.tjut.mianliao.promotion;

import java.lang.ref.WeakReference;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.util.FileDownloader;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class PromotionManager implements FileDownloader.Callback {

    private static final String SP_PROMOTION_LAST_LOADED_ON = "promotion_last_loaded_on";
    private static final String SP_PROMOTION_INFO = "promotion_info";

    private static final long MIN_INTERVAL = 10000;
    private static final long ONE_DAY = 24 * 60 * 60 * 1000;

    private static WeakReference<PromotionManager> sInstanceRef;
    private static long sSplashLastShown;

    private Context mContext;
    private long mLastLoaded;

    private Promotion mPromotion;

    public static synchronized PromotionManager getInstance(Context context) {
        PromotionManager sm = sInstanceRef == null ? null : sInstanceRef.get();
        if (sm == null) {
            sm = new PromotionManager(context.getApplicationContext());
            sInstanceRef = new WeakReference<PromotionManager>(sm);
        }
        return sm;
    }

    private PromotionManager(Context context) {
        mContext = context;
        SharedPreferences sharedPrefs = DataHelper.getSpForData(mContext);
        mLastLoaded = sharedPrefs.getLong(SP_PROMOTION_LAST_LOADED_ON, 0);
        try {
            loadPromotion(new JSONObject(sharedPrefs.getString(SP_PROMOTION_INFO, "{}")));
        } catch (JSONException e) { }

        long interval = Utils.isDebug() ? MIN_INTERVAL : ONE_DAY;
        if (System.currentTimeMillis() > mLastLoaded + interval) {
            new GetSplashTask().executeLong();
        }
    }

    private void loadPromotion(JSONObject json) {
        mPromotion = Promotion.fromJson(json);
        loadImages(mPromotion);
    }

    private void loadImages(Promotion prom) {
        if (prom != null) {
            FileDownloader idl = FileDownloader.getInstance(mContext);
            if (idl.isDownloaded(prom.splashImage)) {
                prom.splashImgFile = idl.getFileName(prom.splashImage);
            } else {
                idl.getFile(prom.splashImage, this, true);
            }
            if (idl.isDownloaded(prom.bannerImage)) {
                prom.bannerImgFile = idl.getFileName(prom.bannerImage);
            } else {
                idl.getFile(prom.splashImage, this, true);
            }
        }
    }

    public boolean isSplashReady() {
        if (sSplashLastShown + MIN_INTERVAL > System.currentTimeMillis()) {
            return false;
        }
        if ((AccountInfo.getInstance(mContext).getUserInfo() != null) && AccountInfo.getInstance(mContext).getUserInfo().isSpecial()) {
            return true;
        }
        if (mPromotion != null && mPromotion.isImageReady()) {
            return true;
        }
        loadImages(mPromotion);
        return false;
    }

    public boolean isBannerReady() {
        if (mPromotion == null) {
            return false;
        } else {
            return mPromotion.isBannerReady();
        }
    }

    public static void setSplashShown() {
        sSplashLastShown = System.currentTimeMillis();
    }

    public Promotion getPromotion() {
        return mPromotion;
    }

    @Override
    public void onResult(boolean success, String url, String fileName) { }

    private class GetSplashTask extends MsTask {

        public GetSplashTask() {
            super(mContext, MsRequest.PROM_TOUCH);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (MsResponse.isSuccessful(response)) {
                mLastLoaded = System.currentTimeMillis();
                JSONObject json = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);

                loadPromotion(json);
                DataHelper.getSpForData(mContext)
                        .edit()
                        .putString(SP_PROMOTION_INFO, json == null ? "{}" : json.toString())
                        .putLong(SP_PROMOTION_LAST_LOADED_ON, mLastLoaded)
                        .commit();
            }
        }
    }
}
