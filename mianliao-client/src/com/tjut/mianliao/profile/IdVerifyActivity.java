package com.tjut.mianliao.profile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.job.ResumeActivity;
import com.tjut.mianliao.mycollege.ImageDeleterHelper;
import com.tjut.mianliao.util.BitmapLoader;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class IdVerifyActivity extends BaseActivity {

    private static final Pattern STU_NUMBER_PATTERN = Pattern.compile("^\\w{2,100}$");
    private static final int RESPENSE_CODE = 66;

    private GetImageHelper mGetImageHelper;
    private CharSequence mStuNumber;
    private EditText mEtStuNum;
    private String mStuCardFile;

    private AuthTask mLastTask;
    private ImageDeleterHelper mImageDeleterHelp;
    private String mImagePath;
    private ArrayList<String> mEditImages;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_id_verify;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.iv_title, null);
        mEtStuNum = (EditText) findViewById(R.id.et_stu_number);
        mImageDeleterHelp = ImageDeleterHelper.getInstance();
        mEditImages = new ArrayList<String>();
        mEtStuNum.setFocusable(true);
        mEtStuNum.setFocusableInTouchMode(true);
        mEtStuNum.clearFocus();
        mGetImageHelper = new GetImageHelper(this, null,
                GetImageHelper.SQUARE_SPEC, new GetImageHelper.ImageResultListener() {
            @Override
            public void onImageResult(boolean success, String imageFile, Bitmap bm) {
                if (success) {
                    Bitmap scaledBm = Bitmap.createScaledBitmap(bm, 132, 132, false);
                    bm.recycle();
                    mStuCardFile = imageFile;
                    mImagePath = imageFile;
                    mImageDeleterHelp.updateInfo(IdVerifyActivity.this, getParameters(), getFiles(), imageFile);
                    BitmapLoader.getInstance().setBitmap((ImageView) findViewById(R.id.iv_stu_card),
                            imageFile, scaledBm);
                    if (mImagePath.contains("LSQ_2")) {
                        mEditImages.add(mImagePath);
                    }
                } else {
                    mStuCardFile = null;
                }
            }

            @Override
            public void onImageResult(boolean success, ArrayList<String> images) {
                if (success) {
                    String imageFile = images.get(0);
                    if (imageFile != null) {
                        mImagePath = imageFile;
                        Bitmap bm = null;
                        Bitmap scaledBm = null;
                        try {
                            bm = BitmapFactory.decodeFile(imageFile);
                            scaledBm = Bitmap.createScaledBitmap(bm, 132, 132, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        bm.recycle();
                        mStuCardFile = imageFile;
                        BitmapLoader.getInstance().setBitmap((ImageView) findViewById(R.id.iv_stu_card),
                                imageFile, scaledBm);
                        if (mImagePath.contains("LSQ_2")) {
                            mEditImages.add(mImagePath);
                        }
                    }
                } else {
                    mStuCardFile = null;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mGetImageHelper.handleResult(requestCode, data);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit:
                if (mLastTask == null && verifyInfo()) {
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("number", mStuNumber.toString());
                    params.put("image", mStuCardFile);
                    HashMap<String, String> files = new HashMap<String, String>();
                    files.put("image", mStuCardFile);
                    new AuthTask(params, files).executeLong();
                }
                break;
            case R.id.rl_stu_card:
                mGetImageHelper.getImage(true, 1);
                break;
            default:
                break;
        }
    }

    private boolean verifyInfo() {
        mStuNumber = mEtStuNum.getText();
        if (!STU_NUMBER_PATTERN.matcher(mStuNumber).matches()) {
            toast(R.string.iv_stu_number_invalid);
            return false;
        }
        if (TextUtils.isEmpty(mStuCardFile)) {
            toast(R.string.iv_missing_stu_card);
            return false;
        }
        return true;
    }

    private class AuthTask extends MsMhpTask {
        public AuthTask(HashMap<String, String> params, HashMap<String, String> files) {
            super(getApplicationContext() == null ? IdVerifyActivity.this : getApplicationContext(),
                    MsRequest.VERIFY_REQUEST, params, files);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getTitleBar().showProgress();
            findViewById(R.id.et_stu_number).setEnabled(false);
            findViewById(R.id.rl_stu_card).setEnabled(false);
            mLastTask = this;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            getTitleBar().hideProgress();
            findViewById(R.id.et_stu_number).setEnabled(true);
            findViewById(R.id.rl_stu_card).setEnabled(true);
            mLastTask = null;
            if (mEditImages != null) {
                for (String editImage : mEditImages) {
                    File file = new File(editImage);
                    if (file.exists()) {
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri uri = Uri.fromFile(file);
                        intent.setData(uri);
                        IdVerifyActivity.this.sendBroadcast(intent);
                        file.delete();
                    }
                }
            }
            if (MsResponse.isSuccessful(response)) {
                Toast.makeText(getApplicationContext(), R.string.iv_upload_succeeded,
                        Toast.LENGTH_LONG).show();
                /**资料上传成功，返回**/
                quit();
                finish();
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.iv_upload_failed, response.code));
            }
        }
    }

    private void quit() {
        Intent data = new Intent();
        data.putExtra("isupload",true);
        setResult(RESPENSE_CODE, data);
        new UserInfo().setIsupload(true);
    }

    @Override
    protected void onResume() {
        Utils.hideInput(mEtStuNum);
        super.onResume();
        mEtStuNum.setFocusable(true);
        mEtStuNum.setFocusableInTouchMode(true);
        mEtStuNum.clearFocus();
    }

    private HashMap<String, String> getParameters() {
        HashMap<String, String> param = new HashMap<String, String>();
        param.put("id_avatar", mImagePath);
        return param;

    }

    private HashMap<String, String> getFiles() {
        HashMap<String, String> files = new HashMap<String, String>();
        files.put("id_avatar", mImagePath);
        return files;
    }
}