package com.tjut.mianliao.image;

import java.io.File;
import java.util.ArrayList;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.GalleryViewPagerW;
import com.tjut.mianliao.image.GalleryImageAdapter.OnImageItemClickListener;
import com.tjut.mianliao.util.FileDownloader;
import com.tjut.mianliao.util.Utils;

public class ImageActivity extends BaseActivity implements View.OnClickListener,
        OnImageItemClickListener {

    public static final String EXTRA_IMAGE_URL = "extra_file_url";
    public static final String EXTRA_IMAGE_URLS = "extra_file_urls";
    public static final String EXTRA_IMAGE_PATH = "extra_file_path";
    public static final String EXTRA_IMAGE_INDEX = "extra_file_index";
    public static final String EXTRA_IS_SHOW_OPER = "extra_is_show_oper";
    public static final String EXTRA_DELETE_IMAGE_URL = "extra_delete_image_url";
    public static final String EXTRA_AVATAR_RUL = "extra_avatar_url";
    public static final String EXTRA_DELETE_IMAGE_LOC_URL = "extra_delete_image_loc_url";

    private ArrayList<String> mImageUrls;

    private GalleryViewPagerW mGvpImages;
    private ImageView mIvDele, mIlSetAvatar;
    private LinearLayout mLlSetAvatar;
    private boolean mIsShowOperBtn;
    private ArrayList<String> mDeleteImageUrl;
    private ArrayList<String> mLocDeleteImageUrl;
    private String mAvatartUrl;
    private GalleryImageAdapter mAdapter;
    private TextView piImages;
    private int mIndex, mHeaderIndex = -1, mCurrentIndex;

    @Override
    protected int getLayoutResID() {
        return 0;
    }

    @Override
    protected int getBaseLayoutResID() {
        return R.layout.activity_image;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsShowOperBtn = getIntent().getBooleanExtra(EXTRA_IS_SHOW_OPER, false);
        mIndex = getIntent().getIntExtra(EXTRA_IMAGE_INDEX, 0);
        mGvpImages = (GalleryViewPagerW) findViewById(R.id.gvp_images);
        mIlSetAvatar = (ImageView) findViewById(R.id.iv_todo_avatar);
        mLlSetAvatar = (LinearLayout) findViewById(R.id.ll_todo_avatar);
        mIvDele = (ImageView) findViewById(R.id.iv_delete);
        mLlSetAvatar.setVisibility(mIsShowOperBtn ? View.VISIBLE : View.GONE);
        mIvDele.setVisibility(mIsShowOperBtn ? View.VISIBLE : View.GONE);
        mLocDeleteImageUrl = new ArrayList<>();
        if (mIsShowOperBtn) {
            mDeleteImageUrl = new ArrayList<>();
        }
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
                    mCurrentIndex = i;
                    updateUI();
                }

                @Override
                public void onPageScrollStateChanged(int i) { }
            });

            if (mIndex > 0 && mIndex < mImageUrls.size()) {
                mGvpImages.setCurrentItem(mIndex);
            }
        }
        updateUI();
    }

    private void updateUI() {
        if (mCurrentIndex == mHeaderIndex) {
            // change the icon
            mIlSetAvatar.setImageResource(R.drawable.icon_header_choosed);
        } else {
            // do same with before
            mIlSetAvatar.setImageResource(R.drawable.icon_header_choose);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_save:

                if (!Utils.isExtStorageAvailable()) {
                    toast(R.string.storage_not_available);
                    return;
                }

                FileDownloader imageDownloader = FileDownloader.getInstance(this);
                int index = mGvpImages.getCurrentItem();
                String url = mImageUrls.get(index);

                if (!url.startsWith("http")) {
                    toast(R.string.prof_local_file_noneed_save);
                    return;
                }
                if (imageDownloader.isDownloaded(url)) {
                    String fileName = imageDownloader.getFileName(url);
                    int dotPosition = fileName.lastIndexOf('.');
                    String fileExt = dotPosition > 0 ? fileName.substring(dotPosition) : ".jpg";
                    String newName = new StringBuilder(Utils.getMianLiaoDir().getAbsolutePath())
                            .append("/image_").append(Utils.generateIdentify(fileName))
                            .append("_").append(index).append(fileExt).toString();
                    if (Utils.copy(fileName, newName)) {
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                Uri.fromFile(new File(newName))));
                        toast(getString(R.string.ia_image_saved, newName));
                    } else {
                        toast(R.string.ia_image_save_failed);
                    }
                } else {
                    toast(R.string.ia_downloading);
                }
                break;
            case R.id.iv_delete:
                deleteImage(mGvpImages.getCurrentItem());
                break;
            case R.id.ll_todo_avatar:
            case R.id.iv_todo_avatar:
            case R.id.tv_todo_avatar:
                mAvatartUrl = mImageUrls.get(mGvpImages.getCurrentItem());
                if (mHeaderIndex == mGvpImages.getCurrentItem()) {
                    mHeaderIndex = -1;
                    mAvatartUrl = null;
                } else {
                    mHeaderIndex = mGvpImages.getCurrentItem();
                    mCurrentIndex = mGvpImages.getCurrentItem();
                }
                showIndicator();
                break;
            case R.id.gvp_images:
            default:
                quit();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        quit();
        super.onBackPressed();
    }

    private void quit() {
        if (mIsShowOperBtn) {
            Intent data = new Intent();
            data.putExtra(EXTRA_DELETE_IMAGE_URL, mDeleteImageUrl);
            data.putExtra(EXTRA_DELETE_IMAGE_LOC_URL, mLocDeleteImageUrl);
            data.putExtra(EXTRA_AVATAR_RUL, mAvatartUrl);
            setResult(RESULT_OK, data);
        }
        finish();
    }

    private void deleteImage(int index) {
        if (mImageUrls == null || mImageUrls.size() <= 0) {
            return;
        }
        String delUrl = mImageUrls.get(index);
        if (delUrl.startsWith("http")) {
            mDeleteImageUrl.add(delUrl);
        } else {
            mLocDeleteImageUrl.add(delUrl);
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

    @Override
    public void onImageClick(boolean isDoubleClick) {
        if (!isDoubleClick) {
            quit();
        }
    }
    
}