package com.tjut.mianliao.component;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.tjut.mianliao.util.BitmapLoader;
import com.tjut.mianliao.util.FileDownloader;

/**
 * Pro ImageView which can load bitmap asynced. And since a LruCache is used for the bitmap, please
 * handle cases when the image is too large (for example, larger than 600X600).
 */
public class ProImageView extends ImageView implements FileDownloader.Callback {

    private int mDefaultImage;
    private String mImageUrl;

    private boolean mSetBackground;
    
    public ProImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void setBackground(String url, int defaultImage) {
        mSetBackground = true;
        mImageUrl = url;
        mDefaultImage = defaultImage;
        setBackgroundResource(mDefaultImage);
        if (getContext() != null && url != null && url.length() > 0) {
            FileDownloader.getInstance(getContext()).getFile(url, this, false);
        } else {
            BitmapLoader.getInstance().setBackground(this, null, mDefaultImage);
        }
    }
    
    public void setImage(String url, int defaultImage) {
        mImageUrl = url;
        mDefaultImage = defaultImage;
        setImageResource(mDefaultImage);
        if (getContext() != null && url != null && url.length() > 0) {
            FileDownloader.getInstance(getContext()).getFile(url, this, false);
        } else {
            BitmapLoader.getInstance().setBitmap(this, null, mDefaultImage);
        }
    }

    @Override
    public void onResult(boolean success, String url, String fileName) {
        if (success && TextUtils.equals(mImageUrl, url)) {
            if (mSetBackground) {
                BitmapLoader.getInstance().setBackground(this, fileName, mDefaultImage);
            } else {
                BitmapLoader.getInstance().setBitmap(this, fileName, mDefaultImage);
            }
        }
    }
}
