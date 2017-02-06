package com.tjut.mianliao.component;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.util.BitmapLoader;
import com.tjut.mianliao.util.FileDownloader;
import com.tjut.mianliao.util.Utils;

/**
 * Customized view which can load drawable asynced (as medals).
 */
public class NameView extends LinearLayout implements FileDownloader.Callback {

    private TextView mTextView;
    private ArrayList<ImageView> mImageViews;

    private FileDownloader mImageDownloader;
    private BitmapLoader mBitmapLoader;

    private int mMaxWidth;

    public NameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mImageDownloader = FileDownloader.getInstance(context);
        mBitmapLoader = BitmapLoader.getInstance();
        setGravity(Gravity.CENTER_VERTICAL);
        setOrientation(HORIZONTAL);
        Resources res = getResources();
        mMaxWidth = res.getDimensionPixelSize(R.dimen.mdl_name_max_width);

        mTextView = new TextView(context, attrs);
        addView(mTextView, new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mTextView.setMaxLines(1);
        mTextView.setSingleLine(true);
        mTextView.setEllipsize(TruncateAt.END);

        int count = res.getInteger(R.integer.max_primary_medals);
        int size = res.getDimensionPixelSize(R.dimen.mdl_size_after_name);
        int spacing = res.getDimensionPixelSize(R.dimen.mdl_name_spacing);
        mImageViews = new ArrayList<ImageView>(count);
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ScaleType.FIT_XY);
            imageView.setVisibility(GONE);
            LayoutParams params = new LayoutParams(size, size);
            params.setMargins(spacing, 0, 0, 0);
            addView(imageView, params);
            mImageViews.add(imageView);
        }
    }

    public void setText(CharSequence text) {
        mTextView.setText(text);
    }

    public void setText(int resid) {
        mTextView.setText(resid);
    }

    public void setMedal(String value) {
        mTextView.setMaxWidth(Integer.MAX_VALUE);

        for (ImageView iv : mImageViews) {
            iv.setVisibility(GONE);
        }

        if (!TextUtils.isEmpty(value)) {
            String[] urls = TextUtils.split(value, Utils.COMMA_DELIMITER);
            for (int i = 0; i < urls.length; i++) {
                if (i < mImageViews.size()) {
                    mImageViews.get(i).setTag(urls[i]);
                    mImageDownloader.getFile(urls[i], this, false);
                }
            }
        }
    }

    @Override
    public void onResult(boolean success, String url, String fileName) {
        if (success) {
            for (ImageView iv : mImageViews) {
                if (url.equals(iv.getTag())) {
                    Bitmap bm = mBitmapLoader.getBitmap(fileName);
                    Drawable d = new BitmapDrawable(getResources(), bm);
                    iv.setImageDrawable(d);
                    iv.setVisibility(VISIBLE);
                    mTextView.setMaxWidth(mMaxWidth);
                    break;
                }
            }
        }
    }
}
