package com.tjut.mianliao.mycollege;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.FileDownloader;
import com.tjut.mianliao.util.FileDownloader.Callback;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.GetImageHelper.ImageResultListener;
import com.tjut.mianliao.util.Utils;

public class UpdateProfileImageActiivity extends BaseActivity implements OnClickListener,
        ImageResultListener, Callback {

    public static final String EXT_USER_INFO = "ext_user_info";
    public static final String EXT_CHANGE_TYPE = "ext_change_type";
    public static final String EXT_IMAGE_PATH = "ext_image_path";

    public static final int TYPE_BG = 1;
    public static final int TYPE_AVATAR = 2;

    private UserInfo mUserInfo;
    private int mType;

    private TextView mTvChange;
    private ProImageView mPivPic;
    private GetImageHelper mGetImageHelper;
    private String mImagePath;
    private String mEditImage;
    private ImageDeleterHelper mImageDeleterHelp;

    @Override
    protected int getBaseLayoutResID() {
        return R.layout.activity_update_profile_image;
    }

    @Override
    protected int getLayoutResID() {
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGetImageHelper = new GetImageHelper(this, this);
        mImageDeleterHelp = ImageDeleterHelper.getInstance();
        mUserInfo = getIntent().getParcelableExtra(EXT_USER_INFO);
        mType = getIntent().getIntExtra(EXT_CHANGE_TYPE, 0);
        mTvChange = (TextView) findViewById(R.id.tv_change);
        mPivPic = (ProImageView) findViewById(R.id.piv_image);
        if (mUserInfo.isMine(this)) {
            mTvChange.setVisibility(View.VISIBLE);
            if (mType == TYPE_AVATAR) {
                mTvChange.setText(R.string.prof_update_avatar);
            } else {
                mTvChange.setText(R.string.prof_update_bg);
            }
        }
        mPivPic.setImage(mType == TYPE_AVATAR ? Utils.getImagePreviewSmall(mUserInfo.avatarFull) :
            mUserInfo.bgImg, mType == TYPE_AVATAR ? mUserInfo.defaultAvatar() :
            R.drawable.pic_prof_scene);
        String url = mType == TYPE_AVATAR ? mUserInfo.avatarFull : mUserInfo.bgImg;
        FileDownloader.getInstance(this).getFile(url, this, false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.piv_image:
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.tv_change:
                mGetImageHelper.getImage(true, 1);
                break;
            case R.id.tv_save:
                save();
                break;
            default:
                break;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void save() {
        if (!Utils.isExtStorageAvailable()) {
            toast(R.string.storage_not_available);
            return;
        }
        FileDownloader imageDownloader = FileDownloader.getInstance(this);
        String url = mType == TYPE_AVATAR ? mUserInfo.avatarFull : mUserInfo.bgImg;
        if (url == null || !url.startsWith("http")) {
            return;
        }
        if (imageDownloader.isDownloaded(url)) {
        	    
            String fileName = imageDownloader.getFileName(url);
            int dotPosition = fileName.lastIndexOf('.');
            String fileExt = dotPosition > 0 ? fileName.substring(dotPosition) : ".jpg";
            String newName = new StringBuilder(Utils.getMianLiaoDir().getAbsolutePath())
                    .append("/image_").append(Utils.generateIdentify(fileName))
                    .append("_").append(0).append(fileExt).toString();
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
    }

    private HashMap<String, String> getParameters() {
        HashMap<String, String> param = new HashMap<String, String>();
        if (mType == TYPE_BG) {
            param.put("bg_img", mImagePath);
        } else {
            param.put("avatar", mImagePath);
        }
        return param;
        
    }

    private HashMap<String, String> getFiles() {
        HashMap<String, String> files =  new HashMap<String, String>();
//        String image =  mGetImageHelper.saveAsTodo(this, mImagePath);
        if (mType == TYPE_BG) {
            files.put("bg_img", mImagePath);
        } else {
            files.put("avatar", mImagePath);
        }
        return files;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mGetImageHelper.handleResult(requestCode, data);
    }

    @Override
    public void onImageResult(boolean success, String imageFile, Bitmap bm) {
        // save image to server
        mImagePath = imageFile;
        mImageDeleterHelp.updateInfo(this, getParameters(), getFiles(), mEditImage);
        Intent data = new Intent();
        data.putExtra(EXT_IMAGE_PATH, imageFile);
        setResult(RESULT_UPDATED, data);
        finish();
    }

    @Override
    public void onImageResult(boolean success, ArrayList<String> images) {
        if (success) {
         // save image to server
            mImagePath = images.get(0);
            if (mImagePath.contains("LSQ_2")) {
                mEditImage = mImagePath;
            }
            mImageDeleterHelp.updateInfo(this, getParameters(), getFiles(), mEditImage);
//            new UpdateInfoTask(getParameters(), getFiles()).executeLong();
            Intent data = new Intent();
            data.putExtra(EXT_IMAGE_PATH, images.get(0));
            setResult(RESULT_UPDATED, data);
            finish();
        }
    }

    @Override
    public void onResult(boolean success, String url, String fileName) {}
    
}
