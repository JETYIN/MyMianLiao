package com.tjut.mianliao.qa;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.RefFriendActivity;
import com.tjut.mianliao.component.EmotionPicker;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.Emotion;
import com.tjut.mianliao.image.ImageActivity;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class PostActivity extends BaseActivity implements EmotionPicker.EmotionListener,
        View.OnClickListener, DialogInterface.OnClickListener {

    public static final String EXTRA_POST_TYPE = "extra_post_type";

    private static final int REQUEST_REF = 101;
    public static final int POST_ASK = 201;
    public static final int POST_ANSWER = 202;

    private PostTask mLastPostTask;
    private int mPostType;

    private EditText mEtDesc;
    private ImageView mIvImage;
    private CheckBox mCbEmotion;
    private EmotionPicker mEmotionPicker;

    private Question mQuestion;
    private String mDesc;
    private String mImageFile;

    private GetImageHelper mGetImageHelper;

    private LightDialog mChooseDialog;

    private LightDialog mDiscardDialog;
    private LightDialog mUploadingDialog;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_reply;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPostType = getIntent().getIntExtra(EXTRA_POST_TYPE, 0);

        if (mPostType == POST_ANSWER) {
            mQuestion = getIntent().getParcelableExtra(Question.INTENT_EXTRA_NAME);
            findViewById(R.id.tv_title).setVisibility(View.GONE);
        } else {
            mPostType = POST_ASK;
        }

        mEmotionPicker = (EmotionPicker) findViewById(R.id.ep_emotions);
        mEmotionPicker.setEmotionListener(this);
        mCbEmotion = (CheckBox) findViewById(R.id.cb_input_emotion);
        findViewById(R.id.iv_input_attach).setVisibility(View.GONE);

        mIvImage = (ImageView) findViewById(R.id.iv_image);
        mEtDesc = (EditText) findViewById(R.id.et_desc);
        mEtDesc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideEmoPicker();
                }
                mEtDesc.setText(Utils.getRefFriendText(
                        mEtDesc.getText(), getApplicationContext()));
            }
        });

        if (mPostType == POST_ASK) {
            getTitleBar().showTitleText(R.string.qa_ask, null);
            mEtDesc.setHint(R.string.qa_post_hint_question);
        } else {
            getTitleBar().showTitleText(R.string.qa_answer, null);
            mEtDesc.setHint(R.string.qa_post_hint_answer);
        }

        mGetImageHelper = new GetImageHelper(this, new GetImageHelper.ImageResultListener() {
            @Override
            public void onImageResult(boolean success, String imageFile, Bitmap bm) {
                if (success) {
                    mIvImage.setVisibility(View.VISIBLE);
                    mIvImage.setImageBitmap(bm);
                    mImageFile = imageFile;
                } else {
                    toast(R.string.qa_handle_image_failed);
                }
            }

            @Override
            public void onImageResult(boolean success, ArrayList<String> images) {
                
            }
        });
    }

    @Override
    public void onEmotionClicked(Emotion emotion) {
        mEtDesc.getText().insert(
                mEtDesc.getSelectionStart(), emotion.getSpannable(this));
    }

    @Override
    public void onBackspaceClicked() {
        Utils.dispatchDelEvent(mEtDesc);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_image:
                showChooseDialog();
                break;

            case R.id.et_desc:
                hideEmoPicker();
                break;

            case R.id.iv_input_ref:
                startActivityForResult(new Intent(this, RefFriendActivity.class), REQUEST_REF);
                hideEmoPicker();
                break;

            case R.id.iv_input_image:
                mGetImageHelper.getImage();
                hideEmoPicker();
                break;

            case R.id.cb_input_emotion:
                Utils.toggleInput(mEtDesc, mEmotionPicker);
                break;

            case R.id.btn_submit:
                if (!isStateReady()) {
                    return;
                }

                HashMap<String, String> parameters = new HashMap<String, String>();
                parameters.put("desc", mDesc);
                HashMap<String, String> files = null;
                if (mImageFile != null) {
                    files = new HashMap<String, String>();
                    files.put("image", mImageFile);
                }
                if (Utils.isNetworkAvailable(this)) {
                    if (mPostType == POST_ASK) {
                        new PostTask(MsRequest.QA_ASK, parameters, files).executeLong();
                    } else {
                        int id = mQuestion == null ? 0 : mQuestion.id;
                        parameters.put("id", String.valueOf(id));
                        new PostTask(MsRequest.QA_ANSWER, parameters, files).executeLong();
                    }
                } else {
                    toast(R.string.no_network);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mChooseDialog) {
            switch (which) {
                case 0:
                    Intent preview = new Intent(this, ImageActivity.class);
                    preview.putExtra(ImageActivity.EXTRA_IMAGE_PATH, mImageFile);
                    startActivity(preview);
                    break;
                case 1:
                    mImageFile = null;
                    mIvImage.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        } else if (dialog == mDiscardDialog) {
            finish();
        } else if (dialog == mUploadingDialog) {
            mLastPostTask.cancel(false);
            finish();
        }
    }

    private boolean isStateReady() {
        if (mLastPostTask != null) {
            toast(R.string.qa_upload_title);
            return false;
        }

        mDesc = mEtDesc.getText().toString().trim();
        boolean isEmpty = TextUtils.isEmpty(mDesc);
        if (mPostType == POST_ANSWER) {
            isEmpty &= mImageFile == null;
        }
        if (isEmpty) {
            toast(R.string.qa_desc_empty);
            return false;
        }
        return true;
    }

    private void showChooseDialog() {
        if (mChooseDialog == null) {
            mChooseDialog = new LightDialog(this);
            mChooseDialog.setTitle(R.string.please_choose);
            mChooseDialog.setItems(R.array.qa_image_choices, this);
        }
        mChooseDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (mEmotionPicker.isVisible()) {
            hideEmoPicker();
            return;
        }

        if (mLastPostTask != null) {
            showUploadingDialog();
        } else if (mEtDesc.getText().length() > 0 || mImageFile != null) {
            showDiscardDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showDiscardDialog() {
        if (mDiscardDialog == null) {
            mDiscardDialog = new LightDialog(this);
            mDiscardDialog.setTitle(R.string.qa_discard_title);
            mDiscardDialog.setMessage(R.string.qa_discard_message);
            mDiscardDialog.setNegativeButton(R.string.qa_discard_continue, null);
            mDiscardDialog.setPositiveButton(R.string.qa_discard_quit, this);
            mDiscardDialog.setButtonBackground(DialogInterface.BUTTON_POSITIVE,
                    R.drawable.selector_btn_red);
        }
        mDiscardDialog.show();
    }

    private void showUploadingDialog() {
        if (mUploadingDialog == null) {
            mUploadingDialog = new LightDialog(this);
            mUploadingDialog.setTitle(R.string.qa_upload_title);
            mUploadingDialog.setMessage(R.string.qa_upload_message);
            mUploadingDialog.setNegativeButton(R.string.qa_upload_wait, null);
            mUploadingDialog.setPositiveButton(R.string.qa_upload_quit, this);
            mUploadingDialog.setButtonBackground(DialogInterface.BUTTON_POSITIVE,
                    R.drawable.selector_btn_red);
        }
        mUploadingDialog.show();
    }

    private void hideEmoPicker() {
        mEmotionPicker.setVisible(false);
        mCbEmotion.setChecked(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_REF){
            if (resultCode == RESULT_OK) {
                String refs = data.getStringExtra(RefFriendActivity.EXTRA_RESULT);
                int ss = mEtDesc.getSelectionStart();
                int color = getResources().getColor(R.color.ref_friend);
                mEtDesc.getText().replace(ss, ss,
                        Utils.getColoredText(refs, refs, color));
            }
        }
        if (resultCode == RESULT_OK) {
            mGetImageHelper.handleResult(requestCode, data);
        }
    }

    private class PostTask extends MsMhpTask {

        public PostTask(MsRequest request, HashMap<String, String> parameters,
                HashMap<String, String> files) {
            super(PostActivity.this, request, parameters, files);
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
            mIvImage.setEnabled(false);
            mEtDesc.setEnabled(false);
            mLastPostTask = this;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mIvImage.setEnabled(true);
            mEtDesc.setEnabled(true);
            mLastPostTask = null;

            if (isCancelled()) {
                return;
            }

            if (response.code != MsResponse.MS_SUCCESS) {
                toast(MsResponse.getFailureDesc(PostActivity.this,
                        R.string.qa_upload_failed, response.code));
                return;
            }

            QaRecord record;
            Intent data = new Intent();
            if (getRequest() == MsRequest.QA_ASK) {
                record = new Question();
                data.putExtra(Question.INTENT_EXTRA_NAME, record);
            } else {
                record = new Answer();
                data.putExtra(Answer.INTENT_EXTRA_NAME, record);
            }

            try {
                JSONObject json = new JSONObject(response.response);
                record.id = json.optInt("id");
                if (mImageFile != null) {
                    record.image = json.optString("image");
                    record.thumbnail = AliImgSpec.QA_THUMB.makeUrl(record.image);
                }
            } catch (JSONException e) { }

            record.desc = mDesc;
            record.createdOn = System.currentTimeMillis();
            record.userInfo = AccountInfo.getInstance(getRefContext()).getUserInfo();

            setResult(RESULT_OK, data);
            finish();
        }
    }
}
