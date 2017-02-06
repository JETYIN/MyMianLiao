package com.tjut.mianliao.image;

import java.io.File;

import ru.truba.touchgallery.TouchView.TouchImageView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    private static final int MAX_BITMAP_EDGE = 720;
    
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
                
                return fileToBitmap(file);
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

    private static Bitmap fileToBitmap(String pathName) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        BitmapFactory.decodeFile(pathName, opts);
        float width = Utils.getDisplayWidth() * 1.0f;
        float height = Utils.getDisplayHeight() * 1.0f;
        float scaleX =  width / opts.outWidth;
        float scaleY = height / opts.outHeight;
        scaleY = scaleY == 0 ? 1 : scaleY;
        if (scaleX / scaleY >= 3.0) {
            opts.inSampleSize = 2;
        } else {
            opts.inSampleSize = 1;
        }
        opts.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, opts);
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
