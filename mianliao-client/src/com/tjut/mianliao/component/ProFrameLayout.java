package com.tjut.mianliao.component;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.tjut.mianliao.util.BitmapLoader;
import com.tjut.mianliao.util.FileDownloader;

public class ProFrameLayout extends FrameLayout implements FileDownloader.Callback {

    private int mDefaultBg;
    private String mBgUrl;

    public ProFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setBackground(String url, int defaultBg) {
        mBgUrl = url;
        mDefaultBg = defaultBg;
        setBackgroundResource(mDefaultBg);
        if (getContext() != null) {
            FileDownloader.getInstance(getContext()).getFile(url, this, false);
        }
    }

    @Override
    public void onResult(boolean success, String url, String fileName) {
        if (success && TextUtils.equals(mBgUrl, url)) {
            BitmapLoader.getInstance().setBackground(this, fileName, mDefaultBg);
        }
    }


}
