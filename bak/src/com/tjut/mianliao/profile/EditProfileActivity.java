package com.tjut.mianliao.profile;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProAvatarView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.FaceInfo;
import com.tjut.mianliao.data.Photo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.FaceManager;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class EditProfileActivity extends BaseActivity implements OnClickListener {

    public static final String EXTRA_FULL_USER_INFO = "extra_full_user_info";

    private EditText mEtNickname;
    private EditText mEtPhone;
    private EditText mEtEmail;
    private EditText mEtShortDesc;

    private UserInfo mUserInfo;

    private LightDialog mRemindSaveDialog;

    private UpdateTask mUpdateTask;

    private GetImageHelper mGetImageHelper;
    private PhotoManager mPhotoManager;
    private LightDialog mEditPhotoDialog;

    private String mSceneImageFile;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_edit_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTitleBar().showTitleText(R.string.prof_edit_profile, null);
        getTitleBar().showRightButton(R.drawable.btn_title_bar_confirm, new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAllUpdate();
            }
        });

        mUserInfo = getIntent().getParcelableExtra(EXTRA_FULL_USER_INFO);

        findViewById(R.id.tv_edit).setVisibility(View.VISIBLE);
        mEtNickname = (EditText) findViewById(R.id.et_nickname);
        mEtPhone = (EditText) findViewById(R.id.et_phone);
        mEtEmail = (EditText) findViewById(R.id.et_email);
        if (mUserInfo.emailVerified) {
            mEtEmail.setEnabled(false);
            findViewById(R.id.tv_email_verified).setVisibility(View.VISIBLE);
        }
        mEtShortDesc = (EditText) findViewById(R.id.et_short_desc);

        final ImageView ivScene = (ImageView) findViewById(R.id.iv_scene);
        ivScene.setOnClickListener(this);
        mGetImageHelper = new GetImageHelper(this, "",
                GetImageHelper.AVATAR_SPEC, new GetImageHelper.ImageResultListener() {
            @Override
            public void onImageResult(boolean success, String imageFile, Bitmap bm) {
                if (success) {
                    if (mPhotoManager.hasTodo()) {
                        mPhotoManager.addPhoto(imageFile);
                    } else {
                        mSceneImageFile = GetImageHelper.saveAsTodo(EditProfileActivity.this, imageFile);
                        ivScene.setImageBitmap(bm);
                    }
                } else {
                    toast(R.string.prof_failed_save_picture);
                }
            }

            @Override
            public void onImageResult(boolean success, ArrayList<String> images) {
                // TODO Auto-generated method stub
                
            }
        });

        ProAvatarView avatarView = (ProAvatarView) findViewById(R.id.av_avatar);
        avatarView.setCoverVisible(true);
        avatarView.setOnClickListener(this);
        mPhotoManager = new PhotoManager(this, mUserInfo, avatarView, true);
        GridView gvGallery = (GridView) findViewById(R.id.gv_gallery);
        gvGallery.setAdapter(mPhotoManager.getAdapter());
        gvGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Photo photo = (Photo) parent.getItemAtPosition(position);
                if (photo != null) {
                    mPhotoManager.setTodo(photo);
                    showEditPhotoDialog();
                } else {
                    mPhotoManager.setTodo(null, PhotoManager.TYPE_PHOTO);
                    mGetImageHelper.getImage();
                }
            }
        });

        showUserInfo();
        showScene();
    }

    private void showUserInfo() {
        mEtNickname.setText(mUserInfo.nickname);
        mEtPhone.setText(mUserInfo.phone);
        mEtEmail.setText(mUserInfo.email);
        mEtShortDesc.setText(mUserInfo.shortDesc);

        ((TextView) findViewById(R.id.tv_name)).setText(mUserInfo.getDisplayName(this));
        ((TextView) findViewById(R.id.tv_edu_info)).setText(mUserInfo.getEduInfo(this));

        ImageView ivGender = (ImageView) findViewById(R.id.iv_gender);
        ivGender.setImageResource(mUserInfo.getGenderIcon());

        ((ImageView) findViewById(R.id.iv_type_icon)).setImageResource(mUserInfo.getTypeIcon());

        if (mUserInfo.canUpdatePassword()) {
            findViewById(R.id.rl_edit_pwd).setVisibility(View.VISIBLE);
        }
    }

    private void showScene() {
        ((ProImageView) findViewById(R.id.iv_scene))
                .setImage(mUserInfo.bgImg, R.drawable.pic_prof_scene);
    }

    private void collectUpdates() {
        mUserInfo.nickname = mEtNickname.getText().toString();
        mUserInfo.email = mEtEmail.getText().toString();
        mUserInfo.phone = mEtPhone.getText().toString();
        mUserInfo.shortDesc = mEtShortDesc.getText().toString();
    }

    private boolean hasUpdate() {
        return hasTextUpdate() || mPhotoManager.hasUpdate() || !TextUtils.isEmpty(mSceneImageFile);
    }

    private boolean hasTextUpdate() {
        return !AccountInfo.getInstance(this).getUserInfo().editableEquals(mUserInfo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mGetImageHelper.handleResult(requestCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        collectUpdates();
        if (hasUpdate()) {
            showRemindSaveDialog();
        } else {
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPhotoManager.destroy();
        if (mUpdateTask != null) {
            mUpdateTask.cancel(false);
        }
    }

    private void showEditPhotoDialog() {
        if (mEditPhotoDialog != null) {
            mEditPhotoDialog.show();
            return;
        }
        mEditPhotoDialog = new LightDialog(this);
        mEditPhotoDialog.setTitle(R.string.please_choose);
        mEditPhotoDialog.setItems(R.array.prof_edit_photo_menu, new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // set avatar
                        mPhotoManager.setAvatar();
                        break;
                    case 1: // add a photo
                        mGetImageHelper.getImage();
                        break;
                    case 2: // delete
                        mPhotoManager.deleteTodo(true);
                        break;
                    default:
                        break;
                }
            }
        });

        mEditPhotoDialog.show();
    }

    private void showRemindSaveDialog() {
        if (mRemindSaveDialog != null) {
            mRemindSaveDialog.show();
            return;
        }
        mRemindSaveDialog = new LightDialog(this);
        mRemindSaveDialog.setTitle(R.string.prof_change_not_saved);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        saveAllUpdate();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        finish();
                        break;
                    default:
                        break;
                }
            }
        };
        mRemindSaveDialog.setMessage(R.string.prof_change_not_saved_desc)
                .setNegativeButton(R.string.prof_discard, listener)
                .setPositiveButton(R.string.prof_save, listener);
        mRemindSaveDialog.show();
    }

    private boolean verifyInput() {
        if (!Utils.verifyNickname(mUserInfo.nickname)) {
            toast(R.string.reg_format_nickname);
            return false;
        }
        if (!TextUtils.isEmpty(mUserInfo.email) && !Utils.verifyEmail(mUserInfo.email)) {
            toast(R.string.prof_email_malformed);
            return false;
        }
        return true;
    }

    private void updateUserFace(MsResponse response) {
        String url = response.json.optJSONObject(
                MsResponse.PARAM_RESPONSE).optString(UserInfo.AVATAR);
        if (!url.equals(mUserInfo.getAvatar())) {
            MsResponse faceMR = FaceManager.getInstance(this).addUserFaceBG(url);
            FaceInfo face = (FaceInfo) faceMR.value;
            String faceId = MsResponse.isSuccessful(faceMR)
                    && face != null ? face.id : "";
            faceMR = HttpUtil.msRequest(this, MsRequest.UPDATE_INFO, "face_id=" + faceId);
            faceMR.value = face;
            response.value = faceMR;
        }
    }

    private void removeOldFace(MsResponse mr) {
        if (mr != null) {
            String deleteFaceId = null;
            final FaceInfo face = (FaceInfo) mr.value;
            if (MsResponse.isSuccessful(mr)) {
                deleteFaceId = mUserInfo.faceId;
                mUserInfo.faceId = face == null ? "" : face.id;
            } else if (face != null) {
                deleteFaceId = face.id;
            }
            FaceManager.getInstance(this).removeUserFace(deleteFaceId);
        }
    }

    public void saveAllUpdate() {
        if (mUpdateTask != null) {
            return;
        }

        collectUpdates();

        // Just finish if there's no updates
        if (!hasUpdate()) {
            finish();
            return;
        }

        if (!verifyInput()) {
            return;
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(UserInfo.PHONE, mUserInfo.phone);
        parameters.put(UserInfo.NICKNAME, mUserInfo.nickname);
        parameters.put(UserInfo.EMAIL, mUserInfo.email);
        parameters.put(UserInfo.SHORT_DESC, mUserInfo.shortDesc);

        HashMap<String, String> files = new HashMap<String, String>();
        if (mPhotoManager.hasUpdate()) {
            int size = mPhotoManager.getNewPhotos().size();
            String avatarFile = null;
            if (size > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < size; i++) {
                    String file = "new_photo_" + i;
                    if (sb.length() > 0) {
                        sb.append(',');
                    }
                    sb.append(file);
                    Photo photo = mPhotoManager.getNewPhotos().get(i);
                    files.put(file, photo.file);
                    if (photo == mPhotoManager.getAvatar()) {
                        avatarFile = file;
                    }
                }
                parameters.put(UserInfo.PHOTOS, sb.toString());
            }
            if (avatarFile != null) {
                parameters.put("avatar_file", avatarFile);
            } else if (mPhotoManager.getAvatar() != null) {
                parameters.put("avatar_id", String.valueOf(mPhotoManager.getAvatar().id));
            }
            String deletePhotos = mPhotoManager.getDeletePhotos();
            if (!TextUtils.isEmpty(deletePhotos)) {
                parameters.put("delete_photos", deletePhotos);
            }
        }

        if (!TextUtils.isEmpty(mSceneImageFile)) {
            files.put("bg_img", mSceneImageFile);
        }

        new UpdateTask(parameters, files).executeLong();
    }

    private class UpdateTask extends MsMhpTask {

        public UpdateTask(HashMap<String, String> parameters, HashMap<String, String> files) {
            super(getApplicationContext(), MsRequest.UPDATE_PROFILE, parameters, files);
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
            mUpdateTask = this;
        }

        @Override
        protected MsResponse doInBackground(Void... params) {
            MsResponse response = super.doInBackground(params);
            if (MsResponse.isSuccessful(response)) {
                updateUserFace(response);
            }
            return response;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mUpdateTask = null;

            if (MsResponse.isSuccessful(response)) {
                JSONObject json = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);
                mUserInfo = UserInfo.fromJson(json);
                removeOldFace((MsResponse) response.value);
                UserInfoManager.getInstance(getApplicationContext()).saveUserInfo(mUserInfo);
                Intent i = new Intent();
                i.putExtra(UserInfo.INTENT_EXTRA_INFO, mUserInfo);
                setResult(RESULT_UPDATED, i);
                finish();
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.prof_failed_update_profile, response.code));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.av_avatar:
                mPhotoManager.setTodo(mPhotoManager.getAvatar(), PhotoManager.TYPE_AVATAR);
                mGetImageHelper.getImage();
                break;
            case R.id.cic_update_password:
                Intent i = new Intent(getApplicationContext(), UpdatePasswordActivity.class);
                startActivity(i);
                break;
            case R.id.iv_scene:
                mPhotoManager.setTodo(null, PhotoManager.TYPE_UNDEFINED);
                mGetImageHelper.getImage(R.string.set_scene_image, GetImageHelper.SCENE_SPEC);
                break;
            default:
                break;
        }
    }
}
