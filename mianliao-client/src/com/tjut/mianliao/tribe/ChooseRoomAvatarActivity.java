package com.tjut.mianliao.tribe;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.TitleBar;
import com.tjut.mianliao.data.tribe.TribeChatRoomInfo;
import com.tjut.mianliao.mycollege.ImageDeleterHelper;
import com.tjut.mianliao.util.AliOSSHelper;
import com.tjut.mianliao.util.AliOSSHelper.OnUploadListener;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.GetImageHelper.ImageResultListener;
import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class ChooseRoomAvatarActivity extends BaseActivity implements OnClickListener, 
            ImageResultListener, OnUploadListener{
    
    private static final int REQUEST_CODE = 1002;

    @ViewInject(R.id.iv_avatar)
    private AvatarView mRoomAvatar;
    @ViewInject(R.id.tv_upload_avatar)
    private TextView mTvUploadAvatar; 
    
    public final static String EXT_CHAT_ROOM_DATA = "ext_chat_room_data";
    
    private static final int MSG_SHOW_UPLOADING_DIALOG = 10;
    private static final int MSG_HIDE_UPLOADING_DIALOG = 11;
    private static final int MSG_UPLOADING_FAILURE = 12;
    
    private GetImageHelper mGetImageHelper;
    private String mImagePath;
    private TribeChatRoomInfo mChatRoomInfo;
    private TitleBar mTitleBar;
    private AliOSSHelper mAliOSSHelper;
    private boolean mSubmitClickable = true;
    private String mEditImage;
    private ArrayList<String> mEditImagesPath;
    private ImageDeleterHelper mImageDeleterHelp;
    private ArrayList<String> mEditImages;
    
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_HIDE_UPLOADING_DIALOG:
                    Utils.hidePgressDialog();
                    createChatRoom(mChatRoomInfo);
                    break;
                case MSG_SHOW_UPLOADING_DIALOG:
                    Utils.showProgressDialog(ChooseRoomAvatarActivity.this, R.string.fp_upload_files);
                    break;
                case MSG_UPLOADING_FAILURE:
                    Utils.hidePgressDialog();
                    break;
                default:
                    break;
            }
        };
    };
    
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_choose_room_avatar;
    }                                         
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mEditImages = new ArrayList<String>();
        mTitleBar = getTitleBar();
        mTitleBar.setTitle(getString(R.string.tribe_upload_group_avatar));
        mTitleBar.showRightText(R.string.fpwd_submit, this);
        mAliOSSHelper = AliOSSHelper.getInstance(this);
        Intent intent = getIntent();
        mChatRoomInfo = intent.getParcelableExtra(EXT_CHAT_ROOM_DATA);
        mGetImageHelper = new GetImageHelper(this, this);
        mTvUploadAvatar.setOnClickListener(this);
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_upload_avatar:
            case R.id.iv_avatar:
                mGetImageHelper.getImage(true, 1);
                break;
            case R.id.tv_right:
                if (!mSubmitClickable) {
                    return;
                }
                if (mImagePath != null && !mImagePath.equals("")) {
                    mAliOSSHelper.uploadImage(new File(mImagePath), this);
                    Message msg = new Message();
                    msg.what = MSG_SHOW_UPLOADING_DIALOG;
                    mHandler.sendMessage(msg);
                } else {
                    createChatRoom(mChatRoomInfo);
                }
                mSubmitClickable = false;
                break;
            default:
                break;
        }
    }

    private void createChatRoom(TribeChatRoomInfo chatRoomInfo) {
        new CreateChatRoomTask(chatRoomInfo).executeLong();
    }
    
    private class CreateChatRoomTask extends MsMhpTask {

        public CreateChatRoomTask(TribeChatRoomInfo chatRoomInfo) {
            super(ChooseRoomAvatarActivity.this,  MsRequest.TRIBE_CREATE_CHAT_ROOM,
            		getParams(mChatRoomInfo), null);
        }
        
        @Override
        protected void onPreExecute() {
            Utils.showProgressDialog(ChooseRoomAvatarActivity.this,
                    R.string.tribe_create_chat_room_ing);
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            Utils.hidePgressDialog();
            if  (response.isSuccessful()) {
                int roomId = response.getJsonObject().optInt("id");
                mChatRoomInfo.roomId = roomId;
                if (mEditImages != null) {
                    for (String editImage : mEditImages) {
                        File file = new File(editImage);
                        if (file.exists()) {
                            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri uri = Uri.fromFile(file);
                            intent.setData(uri);
                            ChooseRoomAvatarActivity.this.sendBroadcast(intent);
                            file.delete();
                        }
                    }
                }
                Intent intent = new Intent(ChooseRoomAvatarActivity.this, FinishCreateRoomActivity.class);
                intent.putExtra(EXT_CHAT_ROOM_DATA, mChatRoomInfo);
                startActivityForResult(intent, REQUEST_CODE);
            } else {
                response.showInfo(ChooseRoomAvatarActivity.this, response.getFailureDesc(response.code));
                mSubmitClickable = true;
            }
        }
        
    }
    
    private HashMap<String, String> getParams(TribeChatRoomInfo chatRoomInfo) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("name", chatRoomInfo.roomName);
        params.put("tribe_id", String.valueOf(chatRoomInfo.tribeId));
        params.put("icon", chatRoomInfo.roomAvatar);
        return params;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteTmpImages();
    }

    private void deleteTmpImages() {
        if(mEditImagesPath != null){
        for (String image : mEditImagesPath) {
            File file = new File(image);
            if (file.exists()) {
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                sendBroadcast(intent);
                file.delete();
                Utils.logD("UpdateProfileImage", "-- delete image succ--" + image);
            }
        }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mGetImageHelper.handleResult(requestCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_UPDATED) {
             setResult(RESULT_UPDATED, data);
             finish();
        }
    }
    
    @Override   
    public void onImageResult(boolean success, String imageFile, Bitmap bm) {
        mImagePath = imageFile;
        Intent data = new Intent();
        Bitmap btm = BitmapFactory.decodeFile(mImagePath);
        mRoomAvatar.setImageBitmap(btm);
    }

    @Override
    public void onImageResult(boolean success, ArrayList<String> images) {
        mImagePath = images.get(0);
        Intent data = new Intent();
        Bitmap btm = BitmapFactory.decodeFile(mImagePath);
        mRoomAvatar.setImageBitmap(btm);
        mImagePath = images.get(0);
        if (mImagePath.contains("LSQ_2")) {
            mEditImages.add(mImagePath);
        }
    }

    private HashMap<String, String> getParameters() {
        HashMap<String, String> param = new HashMap<String, String>();
        param.put("icon", mImagePath);
        return param;
        
    }

    private HashMap<String, String> getFiles() {
        HashMap<String, String> files =  new HashMap<String, String>();
//        String image =  mGetImageHelper.saveAsTodo(this, mImagePath);
        files.put("icon", mImagePath);
        return files;
    }
    
    @Override
    public void onUploadSuccess(File file, byte[] data, String url, String objectKey) {
         mChatRoomInfo.roomAvatar = url;
         Message msg = new Message();
         msg.what = MSG_HIDE_UPLOADING_DIALOG;
         mHandler.sendMessage(msg);
    }

    @Override
    public void onUploadProgress(File file, byte[] data, int byteCount, int totalSize) {
    }

    @Override
    public void onUploadFailure(File file, byte[] data, String errMsg) {
        mSubmitClickable = true;
        Message msg = new Message();
        msg.what = MSG_UPLOADING_FAILURE;
        mHandler.sendMessage(msg);
    }
    
}
