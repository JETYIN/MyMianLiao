package com.tjut.mianliao.job;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.CardItemConf;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.Option;
import com.tjut.mianliao.data.Resume;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.mycollege.ImageDeleterHelper;
import com.tjut.mianliao.register.RegInfo;
import com.tjut.mianliao.util.BitmapLoader;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.GetImageHelper.ImageResultListener;
import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class ResumeActivity extends BaseActivity implements View.OnClickListener, DialogInterface.OnClickListener,
        ImageResultListener {

    private static final int BIRTH_YEAR_START = Calendar.getInstance().get(Calendar.YEAR) - 15;
    private static final int GRADUATE_YEAR_START = Calendar.getInstance().get(Calendar.YEAR) + 5;
    private static int[] sBirthYears = new int[40];
    private static int[] sGraduateYears = new int [16];
    private LightDialog mEdubackDialog;
    RegInfo mRegInfo = RegInfo.getInstance();
    private int mEduback;
    private String mEditImage;
    private String mImagePath;
    private ImageDeleterHelper mImageDeleterHelp;

    static {
        for (int i = 0; i < sBirthYears.length; i++) {
            sBirthYears[i] = BIRTH_YEAR_START - i;
        }
    }
    static {
        for (int i = 0; i < sGraduateYears.length; i++){
            sGraduateYears[i] = GRADUATE_YEAR_START - i;
        }
    }

    private ProImageView mIvPhoto;
    private EditText mEtName, mEtHightLight;
    private CardItemConf mCifGender;
    private CardItemConf mCifBirthYear;
    private CardItemConf mCifGraduation;
    private CardItemConf mCifEduBack;
    private EditText mEtEmail, mEtMajor;
    private TextView mTvSchool, mTvetLength;

    private View mBtnSave;

    private OptionManager mOptionManager;
    private GetImageHelper mGetImageHelper;

    private LightDialog mGenderDialog;
    private LightDialog mBirthYearDialog;
    private LightDialog mRemindSaveDialog;
    private LightDialog mGraduationDialog;

    private List<Option> mCategories;

    private String mPhotoImage;
    private Resume mOriginalResume;
    private Resume mModifiedResume;
    private SaveResumeTask mSaveTask;
    private ArrayList<String> mEditImages;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_resume;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageDeleterHelp = ImageDeleterHelper.getInstance();
        mEtMajor = (EditText) findViewById(R.id.et_major);
        mTvSchool = (TextView) findViewById(R.id.tv_school);
//        mTvDegree = (TextView) findViewById(R.id.tv_degree);
        mEtHightLight = (EditText) findViewById(R.id.et_highlight);
        mTvetLength = (TextView) findViewById(R.id.tv_et_lenth);
        mIvPhoto = (ProImageView) findViewById(R.id.iv_photo);
        mEtName = (EditText) findViewById(R.id.et_name);
        mEtEmail = (EditText) findViewById(R.id.et_email);

        mCifGender = (CardItemConf) findViewById(R.id.cif_gender);
        mCifEduBack = (CardItemConf) findViewById(R.id.cif_edu_back);
        mCifBirthYear = (CardItemConf) findViewById(R.id.cif_birth_year);
        mCifGraduation = (CardItemConf) findViewById(R.id.cif_graduation);
        
        mEditImages = new ArrayList<String>();

        mCifGender.showDrawImg(false);
        mCifEduBack.showDrawImg(false);
        mCifBirthYear.showDrawImg(false);
        mCifGraduation.showDrawImg(false);

        mCifBirthYear.setContent(R.string.please_choose);
        mCifGraduation.setContent(R.string.please_choose);
        mBtnSave = findViewById(R.id.btn_save);
        mOptionManager = OptionManager.getInstance(this);
        mGetImageHelper = new GetImageHelper(this, null, GetImageHelper.AVATAR_SPEC, this);
        if (mRegInfo.eduback == null) {
            ((CardItemConf) findViewById(R.id.cif_edu_back)).setContent(getString(R.string.prof_degree_bachelor));
        } else {
            ((CardItemConf) findViewById(R.id.cif_edu_back)).setContent(mRegInfo.eduback);
        }
        mCategories = mOptionManager.getCategories();
        for (int i = 0; i < mCategories.size(); i++) {
            if (mCategories.get(i).id == 0) {
                mCategories.remove(i);
                break;
            }
        }

        mOriginalResume = getIntent().getParcelableExtra(Resume.INTENT_EXTRA_NAME);
        if (mOriginalResume == null) {
            getTitleBar().showTitleText(R.string.rsm_title_mine, null);
            findViewById(R.id.ll_resume).setVisibility(View.GONE);
            new LoadResumeTask().executeLong();
        } else {
            getTitleBar().showTitleText(getString(R.string.rsm_title, mOriginalResume.realName), null);
            findViewById(R.id.ll_photo).setEnabled(false);
            mEtName.setEnabled(false);
            mCifGender.setEnabled(false);
            mCifBirthYear.setEnabled(false);
            mCifGraduation.setEnabled(false);
            mEtEmail.setEnabled(false);
            mBtnSave.setVisibility(View.GONE);
            mEtName.setHint(null);
            mEtEmail.setHint(null);
            updateContent();
        }
        mEtHightLight.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTvetLength.setText(s.length() + "/140");
                if(s.length() >= 139){
                    mEtHightLight.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void fillNoneEditView() {
        UserInfo userInfo = AccountInfo.getInstance(getApplicationContext()).getUserInfo();
//        mTvDegree.setText(userInfo.getDegreeName(this));
        mTvSchool.setText(userInfo.school);

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
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_photo:
                mGetImageHelper.getImage(true, 1);
                break;

            case R.id.cif_gender:
                showGenderDialog();
                break;

            case R.id.cif_birth_year:
                showBirthYearDialog();
                break;
            case R.id.cif_graduation:
                showDegreeDialog();
                break;

            case R.id.btn_save:
                collectUpdates();
                saveResume();
                break;
            case R.id.cif_edu_back:
                showDialog();
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mGenderDialog) {
            mModifiedResume.gender = which;
            mCifGender.setContent(getDescGender(which));
            if (TextUtils.isEmpty(mPhotoImage) && TextUtils.isEmpty(mModifiedResume.photo)) {
                mIvPhoto.setImageResource(UserInfo.getDefaultAvatar(which));
            }
        } else if (dialog == mBirthYearDialog) {
            int birthYear = sBirthYears[which];
            mModifiedResume.birthYear = birthYear;
            mCifBirthYear.setContent(getDescBirthYear(birthYear));
        } else if (dialog == mRemindSaveDialog) {
            if (DialogInterface.BUTTON_POSITIVE == which) {
                saveResume();
            } else {
                finish();
            }
        } else if (dialog == mGraduationDialog) {
            int graduateyears = sGraduateYears[which];
            mModifiedResume.graduationYear = graduateyears;
            mCifGraduation.setContent(getDescBirthYear(graduateyears));

        }
    }

    @Override
    public void onImageResult(boolean success, String imageFile, Bitmap bm) {
        if (success) {
            mPhotoImage = imageFile;
            BitmapLoader.getInstance().setBitmap(mIvPhoto, imageFile, bm);
            if (mPhotoImage.contains("LSQ_2")) {
                mEditImages.add(mPhotoImage);
            }
        } else {
            toast(R.string.prof_failed_save_picture);
        }
    }

    @Override
    public void onImageResult(boolean success, ArrayList<String> images) {
        if (success) {
            String imageFile = images.get(0);
            Bitmap bm = BitmapFactory.decodeFile(imageFile);
            mPhotoImage = imageFile;
            mImagePath = images.get(0);
            if (mImagePath.contains("LSQ_2")) {
                mEditImages.add(mImagePath);
            }
            BitmapLoader.getInstance().setBitmap(mIvPhoto, imageFile, bm);
        } else {
            toast(R.string.prof_failed_save_picture);
        }
    }
    
    private HashMap<String, String> getParameters() {
        HashMap<String, String> param = new HashMap<String, String>();
        param.put("photo", mImagePath);
        return param;
        
    }

    private HashMap<String, String> getFiles() {
        HashMap<String, String> files =  new HashMap<String, String>();
//        String image =  mGetImageHelper.saveAsTodo(this, mImagePath);
        files.put("photo", mImagePath);
        return files;
    }

    private String getDescGender(int gender) {
        return getString(UserInfo.getGenderResId(gender));
    }

    private String getDescBirthYear(int birthYear) {
        return birthYear == 0 ? getString(R.string.please_choose) :
        	getString(R.string.reg_start_year_desc, birthYear);
    }

    private void collectUpdates() {
        if (mModifiedResume != null) {
            mModifiedResume.realName = mEtName.getText().toString();
            mModifiedResume.email = mEtEmail.getText().toString();
            mModifiedResume.intro = mEtHightLight.getText().toString();
            mModifiedResume.major = mEtMajor.getText().toString();
            
        }
    }

    private boolean verifyUpdates() {
        if (!Utils.verifyNickname(mModifiedResume.realName)) {
            toast(R.string.rsm_tst_incorrect_real_name);
            return false;
        }
        if (mModifiedResume.birthYear == 0) {
            toast(R.string.rsm_tst_empty_birth_year);
            return false;
        } 
        if (mModifiedResume.major.equals("")) {
        	toast(R.string.rsm_tst_empty_major);
            return false;
        }
        if (mModifiedResume.graduationYear == 0) {
        	 toast(R.string.rsm_tst_empty_graduation_year);
             return false;
        }
      
        if (!TextUtils.isEmpty(mModifiedResume.email) && !Utils.verifyEmail(mModifiedResume.email)) {
            toast(R.string.rsm_tst_incorrect_email);
            return false;
        }
        if (TextUtils.isEmpty(mModifiedResume.intro)) {
            toast(R.string.rsm_intro_hint);
            return false;
        }
        return true;
    }

    private boolean hasUpdate() {
        if (mOriginalResume == null || mModifiedResume == null) {
            return false;
        }
        return !TextUtils.isEmpty(mPhotoImage) || !mOriginalResume.contentEquals(mModifiedResume);
    }

    private void updateContent() {
        mIvPhoto.setImage(mOriginalResume.photo, UserInfo.getDefaultAvatar(mOriginalResume.gender));
        mEtName.setText(mOriginalResume.realName);
        mCifGender.setContent(getDescGender(mOriginalResume.gender));
        mCifBirthYear.setContent(getDescBirthYear(mOriginalResume.birthYear));
        mCifGraduation.setContent(getDescBirthYear(mOriginalResume.graduationYear));
        mEtEmail.setText(mOriginalResume.email);
        mEtMajor.setText(mOriginalResume.major);
        mEtHightLight.setText(mOriginalResume.intro);
        fillNoneEditView();

    }

    private void saveResume() {
        if (mSaveTask != null) {
            toast(R.string.handling_last_task);
            return;
        }

        if (!verifyUpdates()) {
            return;
        }

        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Resume.REALNAME, mModifiedResume.realName);
        params.put(Resume.GENDER, String.valueOf(mModifiedResume.gender));
        params.put(Resume.BIRTH_YEAR, String.valueOf(mModifiedResume.birthYear));
        params.put(Resume.EMAIL, mModifiedResume.email);
        params.put(Resume.INTRO, mModifiedResume.intro);
        params.put(Resume.MAJOR, mModifiedResume.major);
        params.put(Resume.GRADUATION_TIME, String.valueOf(mModifiedResume.graduationYear));
        params.put(Resume.EDUBACK,String.valueOf(mEduback));

        HashMap<String, String> files = new HashMap<String, String>();
        if (mPhotoImage != null) {
            files.put(Resume.PHOTO, mPhotoImage);
        }

        new SaveResumeTask(params, files).executeLong();
    }

    private void showGenderDialog() {
        if (mGenderDialog == null) {
            mGenderDialog = new LightDialog(this);
            mGenderDialog.setTitle(R.string.please_choose);
            mGenderDialog.setItems(R.array.reg_choose_gender_menu, this);
        }
        mGenderDialog.show();
    }

    private void showDegreeDialog() {

        if (mGraduationDialog == null) {
            String[] yearDescs = new String[sGraduateYears.length];
            for (int i = 0; i < sGraduateYears.length; i++) {
                yearDescs[i] = getDescBirthYear(sGraduateYears[i]);
            }
            mGraduationDialog = new LightDialog(this);
            mGraduationDialog.setTitle(R.string.please_choose);
            mGraduationDialog.setItems(yearDescs, this).setNegativeButton(android.R.string.cancel, null);
        }
        mGraduationDialog.show();
    }

    private void showBirthYearDialog() {
        if (mBirthYearDialog == null) {
            String[] yearDescs = new String[sBirthYears.length];
            for (int i = 0; i < sBirthYears.length; i++) {
                yearDescs[i] = getDescBirthYear(sBirthYears[i]);
            }
            mBirthYearDialog = new LightDialog(this);
            mBirthYearDialog.setTitle(R.string.please_choose);
            mBirthYearDialog.setItems(yearDescs, this).setNegativeButton(android.R.string.cancel, null);
        }
        mBirthYearDialog.show();
    }

    private void showRemindSaveDialog() {
        if (mRemindSaveDialog == null) {
            mRemindSaveDialog = new LightDialog(this);
            mRemindSaveDialog.setTitle(R.string.prof_change_not_saved);
            mRemindSaveDialog.setMessage(R.string.ef_change_unsaved).setNegativeButton(R.string.prof_discard, this)
                    .setPositiveButton(R.string.prof_save, this);
        }
        mRemindSaveDialog.show();
    }

    private class LoadResumeTask extends MsTask {

        public LoadResumeTask() {
            super(getApplicationContext(), MsRequest.JOB_MY_RESUME);
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (response.code == MsResponse.MS_JOB_RESUME_NOT_EXIST) {
                mOriginalResume = Resume.fromUserInfo(AccountInfo.getInstance(getApplicationContext()).getUserInfo());
            } else if (MsResponse.isSuccessful(response)) {
                mOriginalResume = Resume.fromJson(response.json.optJSONObject(MsResponse.PARAM_RESPONSE));
                if (mOriginalResume == null) {
                    toast(R.string.rsm_tst_load_failed);
                }
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(), R.string.rsm_tst_load_failed, response.code));
            }
            if (mOriginalResume != null) {
                findViewById(R.id.ll_resume).setVisibility(View.VISIBLE);
                updateContent();
                mModifiedResume = mOriginalResume.copy();
            }
        }
    }

    private class SaveResumeTask extends MsMhpTask {

        public SaveResumeTask(HashMap<String, String> params, HashMap<String, String> files) {
            super(getApplicationContext(), mModifiedResume.id > 0 ? MsRequest.JOB_EDIT_RESUME
                    : MsRequest.JOB_CREATE_RESUME, params, files);
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
            mSaveTask = this;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mSaveTask = null;
            if (MsResponse.isSuccessful(response)) {
                finish();
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(), R.string.rsm_tst_save_failed, response.code));
            }
        }
    }
    private void showDialog() {
        if (mEdubackDialog == null) {
            mEdubackDialog = new LightDialog(this).setTitleLd(R.string.reg_choose_edu_back).setItems(
                    R.array.education_background, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TypedArray ta = getResources().obtainTypedArray(R.array.education_background);
                            RegInfo.getInstance().eduback = ta.getString(which);
                            ((CardItemConf) findViewById(R.id.cif_edu_back)).setContent(mRegInfo.eduback);
                            if (mRegInfo.eduback.equals("本科")) {
                                mEduback = 1;
                            } else if (mRegInfo.eduback.equals("大专")) {
                                mEduback = 0;
                            } else if (mRegInfo.eduback.equals("研究生")) {
                                mEduback = 2;
                            }
                        }

                    });
        }
        mEdubackDialog.show();
    }
    
    @Override
    protected void onDestroy() {
        if (mEditImages != null) {
            for (String editImage : mEditImages) {
                File file = new File(editImage);
                if (file.exists()) {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(file);
                    intent.setData(uri);
                    this.sendBroadcast(intent);
                    file.delete();
                }
            }
        }
        super.onDestroy();
    }
}
