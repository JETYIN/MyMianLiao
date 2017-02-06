package com.tjut.mianliao.image;

import java.io.File;

import ru.truba.touchgallery.TouchView.TouchImageView;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.tjut.mianliao.R;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.FileDownloader;
import com.tjut.mianliao.util.Utils;

public class GalleryImageView extends RelativeLayout implements FileDownloader.Callback {
    private static final String TAG = "GalleryImageView";
    protected TouchImageView mImageView;
    protected ProgressBar mProgressBar;
    private String mImageUrl;

    public GalleryImageView(Context ctx) {
        super(ctx);
        mImageView = new TouchImageView(ctx);
        addView(mImageView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mImageView.setVisibility(GONE);

        mProgressBar = new ProgressBar(ctx, null, android.R.attr.progressBarStyle);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mProgressBar.setLayoutParams(params);
        mProgressBar.setIndeterminate(true);
        addView(mProgressBar);
    }

    public void setImage(String image) {
        mImageUrl = image;
        FileDownloader imgDlder = FileDownloader.getInstance(getContext());
        if (image.startsWith("http")) {
            if (getContext() != null) {
                imgDlder.getFile(image, this, false);
            }
        } else if (fileExists(image)) {
            showImage(image);
        }
    }

    private boolean fileExists(String image) {
        return !TextUtils.isEmpty(image) && new File(image).isFile();
    }

    private void showImage(final String file) {
        new AdvAsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return Utils.fileToBitmap(file);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    mImageView.setVisibility(VISIBLE);
                    mProgressBar.setVisibility(GONE);
                    mImageView.setImageBitmap(bitmap);
                    Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.image_fade_in);
                    mImageView.startAnimation(anim);
                } else {
                    Utils.logD(TAG, "decode bitmap failed : " + file);
                }
            }
        }.executeQuick();
    }

    @Override
    public void onResult(boolean success, String url, String fileName) {
        if (mImageUrl.equals(url)) {
            if (success) {
                showImage(fileName);
            } else {
                mImageView.setVisibility(VISIBLE);
                mProgressBar.setVisibility(GONE);
                mImageView.setImageResource(R.drawable.bg_back_circle);
            }
        }
    }

    public void setScaleType(ImageView.ScaleType scaleType) {
        mImageView.setScaleType(scaleType);
    }

    public TouchImageView getImageView() {
        return mImageView;
    }
}
