package com.tjut.mianliao.image;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.GalleryViewPagerW;
import com.tjut.mianliao.image.GalleryImageAdapter.OnImageItemClickListener;

public class PreviewImageActivity extends BaseActivity implements View.OnClickListener,
        OnImageItemClickListener {

    public static final int PREVIEW_REQUEST_CODE = 1000;
    public static final int RESULT_PREVIEW_OK = 1001;
    
    public static final String EXTRA_IMAGE_INDEX = "extra_file_index";
    public static final String EXTRA_IMAGE_URL = "extra_file_url";
    public static final String EXTRA_IMAGE_URLS = "extra_file_urls";
    public static final String EXTRA_IMAGE_PATH = "extra_file_path";
    public static final String EXTRA_IMAGES_LIST = "extra_file_path";
    
    private ArrayList<String> mImageUrls;
    private GalleryImageAdapter mAdapter;
    private GalleryViewPagerW mGvpImages;
    private TextView piImages;
    private int mIndex;
    
    @Override
    protected int getBaseLayoutResID() {
        return R.layout.activity_image_preview;
    }
    
    
    @Override
    protected int getLayoutResID() {
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIndex = getIntent().getIntExtra(EXTRA_IMAGE_INDEX, 0);
        mGvpImages = (GalleryViewPagerW) findViewById(R.id.gvp_images);
        Intent intent = getIntent();
        mImageUrls = intent.getStringArrayListExtra(EXTRA_IMAGE_URLS);
        if (mImageUrls == null || mImageUrls.size() == 0) {
            String url = intent.getStringExtra(EXTRA_IMAGE_URL);
            String file = intent.getStringExtra(EXTRA_IMAGE_PATH);
            if (TextUtils.isEmpty(url) && TextUtils.isEmpty(file)) {
                toast(R.string.ia_invalid_image_uri);
                finish();
                return;
            }

            mImageUrls = new ArrayList<String>();
            if (!TextUtils.isEmpty(url)) {
                mImageUrls.add(url);
            }
            if (!TextUtils.isEmpty(file)) {
                mImageUrls.add(file);
            }
        }

        mAdapter = new GalleryImageAdapter(this, mImageUrls);
        mGvpImages.setOffscreenPageLimit(2);
        mGvpImages.setAdapter(mAdapter);
        mAdapter.setOnImageItemClickListener(this);

        piImages = (TextView) findViewById(R.id.tv_pos_count);
        showIndicator();
    }
    

    @Override
    public void onBackPressed() {
        quit();
        super.onBackPressed();
    }
    
    private void showIndicator() {
        int size = mImageUrls.size();
        if (size == 1) {
            piImages.setVisibility(View.GONE);
        } else {
            mIndex = mIndex < size ? mIndex : size;
            piImages.setText(getString(R.string.prof_img_pos_count, mIndex + 1, mImageUrls.size()));
            mGvpImages.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i2) { }

                @Override
                public void onPageSelected(int i) {
                    piImages.setText(getString(R.string.prof_img_pos_count, i + 1, mImageUrls.size()));
                }

                @Override
                public void onPageScrollStateChanged(int i) { }
            });

            if (mIndex > 0 && mIndex < mImageUrls.size()) {
                mGvpImages.setCurrentItem(mIndex);
            }
        }
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_delete:
                deleteImage(mGvpImages.getCurrentItem());
                break;
            default:
                quit();
                break;
        }
    }


    private void deleteImage(int index) {
        if (mImageUrls == null || mImageUrls.size() <= 0) {
            return;
        }
        mImageUrls.remove(index);
        if (mImageUrls.size() == 0) {
            quit();
            return;
        }
        if (mImageUrls.size() > 0) {
            mAdapter = new GalleryImageAdapter(this, mImageUrls);
            mGvpImages.setOffscreenPageLimit(2);
            mGvpImages.setAdapter(mAdapter);
            if (index < mImageUrls.size()) {
                mIndex = index;
            } else {
                mIndex = mImageUrls.size() - 1;
            }
            showIndicator();
        }
    }
    
    private void quit() {
        Intent data = new Intent();
        data.putExtra(EXTRA_IMAGES_LIST, mImageUrls);
        setResult(RESULT_PREVIEW_OK, data);
        finish();
    }


    @Override
    public void onImageClick(boolean isDoubleClick) {
        if (!isDoubleClick) {
            quit();
        }
    }
}
