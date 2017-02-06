package com.tjut.mianliao.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import android.graphics.Bitmap;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Bitmap loader is designed to load and cache small size icons such as
 * avatar/medal icons.
 */
public class BitmapLoader {
    private static final String TAG = "BitmapLoader";

    private static final int SUGGESTED_CACHE_SIZE = 6 * 1024; // KB

    // Use 4/5 of max memory. Because it could get OOM if max memory is used.
    private static final int MAX_MEM = (int) (Runtime.getRuntime().maxMemory() / 1024 * 4 / 5);

    private static WeakReference<BitmapLoader> sInstanceRef;

    private static final int CACHE_SIZE = Math.min(MAX_MEM, SUGGESTED_CACHE_SIZE);

    private LruCache<String, Bitmap> mBitmapCache;

    private Hashtable<String, LoadBitmapTask> mLoadingTasks
            = new Hashtable<String, BitmapLoader.LoadBitmapTask>();

    private HashMap<View, String> mLiveViewMap = new HashMap<View, String>();

    public static synchronized BitmapLoader getInstance() {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        BitmapLoader instance = new BitmapLoader();
        sInstanceRef = new WeakReference<BitmapLoader>(instance);
        return instance;
    }

    private BitmapLoader() {
        Utils.logD(TAG, "Bitmap cache size(KB): " + CACHE_SIZE);

        mBitmapCache = new LruCache<String, Bitmap>(CACHE_SIZE) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                int size = bitmap.getByteCount() / 1024;

                Utils.logD(TAG, "Bitmap size(KB): " + size + " (" + key + ")");
                return size;
            }
        };
    }

    public void setBitmap(ImageView iv, String key, Bitmap bm) {
        if (TextUtils.isEmpty(key) || bm == null) {
            return;
        } else {
            removeBitmap(key);
            mBitmapCache.put(key, bm);
            iv.setImageBitmap(bm);
        }
    }

    /**
     * Load and set bitmap to a imageview.
     *
     * @param iv           ImageView which used for displaying image.
     * @param key          Full path of the target image file.
     * @param defaultResId Temp image resource id which is displayed while loading the
     *                     real image.
     */
    public void setBitmap(ImageView iv, String key, int defaultResId) {
        if (TextUtils.isEmpty(key)) {
            mLiveViewMap.remove(iv);
            if (defaultResId > 0) {
                iv.setVisibility(View.VISIBLE);
                iv.setImageResource(defaultResId);
            } else {
                iv.setVisibility(View.GONE);
            }
            return;
        }

        iv.setVisibility(View.VISIBLE);
        Bitmap bm = mBitmapCache.get(key);
        if (bm != null) {
            iv.setImageBitmap(bm);
            return;
        }

        if (defaultResId > 0) {
            iv.setImageResource(defaultResId);
        }

        mLiveViewMap.put(iv, key);
        if (mLoadingTasks.get(key) != null) {
            mLoadingTasks.get(key).getBitmapSetter().addTarget(iv);
        } else {
            LoadBitmapTask lat = new LoadBitmapTask(key, new ImageViewSetter(iv));
            lat.executeQuick();
            mLoadingTasks.put(key, lat);
        }
    }

    public void setBackground(View view, String key, int defaultResId) {
        if (TextUtils.isEmpty(key)) {
            view.setBackgroundResource(defaultResId);
            return;
        }

        Bitmap bm = mBitmapCache.get(key);
        if (bm != null) {
            setBackgroud(view, bm);
            return;
        }

        view.setBackgroundResource(defaultResId);
        mLiveViewMap.put(view, key);
        if (mLoadingTasks.get(key) != null) {
            mLoadingTasks.get(key).getBitmapSetter().addTarget(view);
        } else {
            LoadBitmapTask lat = new LoadBitmapTask(key, new BackgroundSetter(view));
            lat.executeQuick();
            mLoadingTasks.put(key, lat);
        }
    }

    /**
     * Set a compound drawable for a TextView.
     *
     * @param tv
     * @param key      Full path of the target image file.
     * @param position 0: left, 1: top, 2: right, 3: bottom
     * @param bounds   Bounds of the drawable.
     */
    public void setBitmap(TextView tv, String key, int position, Rect bounds) {
        if (TextUtils.isEmpty(key)) {
            return;
        }

        Bitmap bm = mBitmapCache.get(key);
        if (bm != null) {
            setCompoundDrawable(tv, bm, position, bounds);
            return;
        }

        mLiveViewMap.put(tv, key);
        if (mLoadingTasks.get(key) != null) {
            mLoadingTasks.get(key).getBitmapSetter().addTarget(tv);
        } else {
            LoadBitmapTask lat = new LoadBitmapTask(key,
                    new CompoundDrawableSetter(tv, position, bounds));
            lat.executeQuick();
            mLoadingTasks.put(key, lat);
        }
    }

    private void setCompoundDrawable(TextView tv, Bitmap bm, int position, Rect bounds) {
        Drawable[] drawables = tv.getCompoundDrawables();
        Drawable drawable = new BitmapDrawable(tv.getContext().getResources(), bm);
        drawable.setBounds(bounds);
        drawables[position] = drawable;
        tv.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
    }

    /**
     * Decode bitmap and put it to cache.
     *
     * @param key file path of the image.
     * @return Decoded bitmap.
     */
    public Bitmap getBitmap(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        Bitmap bm = mBitmapCache.get(key);
        if (bm != null) {
            return bm;
        }

        bm = Utils.fileToBitmap(key);
        if (bm != null) {
            mBitmapCache.put(key, bm);
        }
        return bm;
    }

    /**
     * When there a new image with the same key, the old bitmap should be
     * removed and recycled.
     *
     * @param key
     */
    public void removeBitmap(String key) {
        Bitmap bm = mBitmapCache.get(key);
        if (bm != null) {
            mBitmapCache.remove(key);
            bm.recycle();
        }
    }

    public void clear() {
        mLiveViewMap.clear();
        mBitmapCache.evictAll();
        mLoadingTasks.clear();
    }

    private class LoadBitmapTask extends AdvAsyncTask<String, Void, Bitmap> {

        private String mKey;
        private BitmapSetter mBitmapSetter;

        public LoadBitmapTask(String key, BitmapSetter bs) {
            mKey = key;
            mBitmapSetter = bs;
        }

        BitmapSetter getBitmapSetter() {
            return mBitmapSetter;
        }

        @Override
        protected void onPreExecute() {
            Utils.logD(TAG, "New load bitmap task: " + mKey);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return Utils.fileToBitmap(mKey);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                Utils.logD(TAG, "Load bitmap success: " + mKey);
                if (mBitmapCache.get(mKey) == null) {
                    mBitmapCache.put(mKey, result);
                }

                mBitmapSetter.setBitmap(mKey, result);
            }
            mLoadingTasks.remove(mKey);

            // Make sure mLiveViewMap is cleared when all download task is finished.
            if (mLoadingTasks.size() == 0) {
                mLiveViewMap.clear();
            }
        }
    }

    abstract class BitmapSetter {
        View mTargetView;

        ArrayList<View> mExtraTargets;

        void addTarget(View tv) {
            if (mExtraTargets == null) {
                mExtraTargets = new ArrayList<View>();
            }
            mExtraTargets.add(tv);
        }

        void setBitmap(String key, Bitmap bitmap) {
            setBitmap(key, mTargetView, bitmap);
            if (mExtraTargets != null) {
                for (View v : mExtraTargets) {
                    setBitmap(key, v, bitmap);
                }
            }
        }

        abstract void setBitmap(String key, View v, Bitmap bm);
    }

    /**
     * Set bitmap to ImageView
     */
    public class ImageViewSetter extends BitmapSetter {

        public ImageViewSetter(ImageView iv) {
            mTargetView = iv;
        }

        @Override
        void setBitmap(String key, View v, Bitmap bm) {
            if (key.equals(mLiveViewMap.get(v))) {
                Utils.logD(TAG, "set bitmap now: " + key);
                ((ImageView) v).setImageBitmap(bm);
                mLiveViewMap.remove(v);
            }
        }
    }

    /**
     * Set bitmap to TextView as CompoundDrawables
     */
    public class CompoundDrawableSetter extends BitmapSetter {
        private int mPosition; // 0: left, 1: top, 2: right, 3: bottom
        private Rect mBounds;

        public CompoundDrawableSetter(TextView tv, int position, Rect bounds) {
            mTargetView = tv;
            mPosition = position;
            mBounds = bounds;
        }

        @Override
        void setBitmap(String key, View v, Bitmap bm) {
            if (key.equals(mLiveViewMap.get(v))) {
                TextView tv = (TextView) v;
                setCompoundDrawable(tv, bm, mPosition, mBounds);
                mLiveViewMap.remove(v);
            }
        }
    }

    /**
     * Set bitmap to View as background
     */
    public class BackgroundSetter extends BitmapSetter {

        public BackgroundSetter(View view) {
            mTargetView = view;
        }

        @Override
        void setBitmap(String key, View v, Bitmap bm) {
            if (key.equals(mLiveViewMap.get(v))) {
                setBackgroud(v, bm);
                mLiveViewMap.remove(v);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void setBackgroud(View view, Bitmap bm) {
        byte[] chunk = bm.getNinePatchChunk();
        if (NinePatch.isNinePatchChunk(chunk)) {
            NinePatchChunk npChunk = NinePatchChunk.deserialize(chunk);
            if (npChunk != null) {
                view.setBackgroundDrawable(new NinePatchDrawable(
                        view.getResources(), bm, chunk, npChunk.getPaddings(), null));
                return;
            }
        }

        view.setBackgroundDrawable(new BitmapDrawable(view.getResources(), bm));
    }
}
