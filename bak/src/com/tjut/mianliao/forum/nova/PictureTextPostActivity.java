package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.EmotionPicker;
import com.tjut.mianliao.component.EmotionPicker.EmotionListener;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.data.Emotion;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.GetImageHelper.ImageResultListener;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.Utils;

public class PictureTextPostActivity extends BaseActivity implements ImageResultListener,
        OnClickListener, MsTaskListener{

    protected static final int MAX_CONTENT_LEN = 140;

    public static final String EXT_PIC_PATH = "ext_pic_path";

    private ImageView mIvPhoto;
    private String mImagePath;
    private int mForumId;
    private TextView mTvChangePic;
    private GetImageHelper mImageHelper;
    private EmotionPicker mEmotionPicker;
    private TextView mTvContentLen;
    private CheckBox mCbEmotion;
    private EditText mEtContent;
    private LightDialog mUploadingDialog, mDiscardDialog;
    private MsTaskManager mTaskManager;
    private String mContent;
    private ChannelInfo mChannelInfo;
    private LinearLayout mLlProgress;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_picture_text_post;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mForumId = mChannelInfo.forumId;
        getTitleBar().setTitle(mChannelInfo.name);
        getTitleBar().showRightButton(R.drawable.bottom_ok_commit, this);
        mImagePath = getIntent().getStringExtra(EXT_PIC_PATH);
        mIvPhoto = (ImageView) findViewById(R.id.iv_photo);
        mTvChangePic = (TextView) findViewById(R.id.tv_change_pic);
        mEmotionPicker = (EmotionPicker) findViewById(R.id.ep_emotions);
        mTvContentLen = (TextView) findViewById(R.id.tv_content_length);
        mCbEmotion = (CheckBox) findViewById(R.id.cb_input_emotion);
        mEtContent = (EditText) findViewById(R.id.et_content);
        mLlProgress = (LinearLayout) findViewById(R.id.ll_loading_progress);
        mImageHelper = new GetImageHelper(this, this);
        mTaskManager = MsTaskManager.getInstance(this);
        mTaskManager.registerListener(this);
        if (mImagePath != null) {
            mIvPhoto.setImageBitmap(Utils.fileToBitmap(mImagePath));
        }

        mEtContent.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideEmoPicker();
                }
                mEtContent.setText(Utils.getRefFriendText(
                        mEtContent.getText(), PictureTextPostActivity.this));
            }
        });

        mEtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTvContentLen.setText(String.valueOf(MAX_CONTENT_LEN - s.length()));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mEtContent.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                hideEmoPicker();
            }
        });

        mEmotionPicker.setEmotionListener(new EmotionListener() {
            @Override
            public void onEmotionClicked(Emotion emotion) {
                mEtContent.getText().insert(mEtContent.getSelectionStart(),
                        emotion.getSpannable(PictureTextPostActivity.this));
            }

            @Override
            public void onBackspaceClicked() {
                Utils.dispatchDelEvent(mEtContent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mTvChangePic.postDelayed(new Runnable() {

            @Override
            public void run() {
                mTvChangePic.setVisibility(View.GONE);
            }
        }, 2000);
    }

    @Override
    public void onBackPressed() {
        if (mEmotionPicker.isVisible()) {
            hideEmoPicker();
            return;
        }

        mContent = mEtContent.getText().toString().trim();
        if (!mEtContent.isEnabled()) {
            showUploadingDialog();
        } else if (!TextUtils.isEmpty(mContent) || !TextUtils.isEmpty(mImagePath)) {
            showDiscardDialog();
        } else {
            super.onBackPressed();
        }
    }

    protected void showProgress() {
        mLlProgress.setVisibility(View.VISIBLE);
    }
    
    protected void hideProgress() {
        mLlProgress.setVisibility(View.GONE);
    }

    protected HashMap<String, String> getParams() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("forum_id", String.valueOf(mForumId));
        parameters.put("thread_type", String.valueOf(CfPost.THREAD_TYPE_PIC_TXT));
        parameters.put("content", mContent);
        parameters.put("images", "image");
        return parameters;
    }

    protected HashMap<String, String> getFiles() {
        HashMap<String, String> files = new HashMap<String, String>();
        files.put("image", mImagePath);
        return files;
    }


    protected void submit() {
        if (Utils.isNetworkAvailable(this)) {
            mTaskManager.startForumPostTask(false, getParams(), getFiles());
        } else {
            toast(R.string.no_network);
        }
    }

    protected boolean isStateReady() {
        if (!mEtContent.isEnabled()) {
            toast(R.string.handling_last_task);
            return false;
        }

        mContent = mEtContent.getText().toString().trim();
        if (TextUtils.isEmpty(mContent)) {
            toast(R.string.fp_tst_content_empty);
            return false;
        }
        return true;
    }

    protected void showUploadingDialog() {
        if (mUploadingDialog == null) {
            mUploadingDialog = new LightDialog(this)
            .setTitleLd(R.string.qa_upload_title)
            .setMessage(R.string.qa_upload_message)
            .setButtonBackground(DialogInterface.BUTTON_POSITIVE,
                    R.drawable.selector_btn_red)
            .setNegativeButton(R.string.qa_upload_wait, null)
            .setPositiveButton(R.string.qa_upload_quit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }
        mUploadingDialog.show();
    }

    protected void showDiscardDialog() {
        if (mDiscardDialog == null) {
            mDiscardDialog = new LightDialog(this)
            .setTitleLd(R.string.qa_discard_title)
            .setMessage(R.string.qa_discard_message)
            .setButtonBackground(DialogInterface.BUTTON_POSITIVE,
                    R.drawable.selector_btn_red)
            .setNegativeButton(R.string.qa_discard_continue, null)
            .setPositiveButton(R.string.qa_discard_quit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }
        mDiscardDialog.show();
    }

    protected void hideEmoPicker() {
        mEmotionPicker.setVisible(false);
        mCbEmotion.setChecked(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_photo:
                int visible = mTvChangePic.getVisibility();
                if (visible == View.VISIBLE) {
                    mTvChangePic.setVisibility(View.GONE);
                } else {
                    mTvChangePic.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.tv_change_pic:
                mImageHelper.getImage(true, 1);
                break;
            case R.id.cb_input_emotion:
                Utils.toggleInput(mEtContent, mEmotionPicker);
                break;
            case R.id.btn_right:
                if (!isStateReady()) {
                    return;
                }
                submit();
                break;
            default:
                break;
        }
    }

    @Override
    public void onImageResult(boolean success, String imageFile, Bitmap bm) {
        String file = GetImageHelper.saveAsTodo(this, imageFile);
        Image image = new Image(file, GetImageHelper.zoomOutImage(file, Image.PREVIEW_SIZE));
        bm = Utils.fileToBitmap(image.file);
        mIvPhoto.setImageBitmap(bm);
        mImagePath = file;
    }

    @Override
    public void onImageResult(boolean success, ArrayList<String> images) {
        if (success) {
            String imageFile = images.get(0);
            Bitmap bm = BitmapFactory.decodeFile(imageFile);
            String file = GetImageHelper.saveAsTodo(this, imageFile);
            Image image = new Image(file, GetImageHelper.zoomOutImage(file, Image.PREVIEW_SIZE));
            bm = Utils.fileToBitmap(image.file);
            mIvPhoto.setImageBitmap(bm);
            mImagePath = file;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mImageHelper.handleResult(requestCode, data);
    }

    @Override
    public void onPreExecute(MsTaskType type) {
        switch (type) {
            case FORUM_PUBLISH_POST:
            case FORUM_EDIT_POST:
                showProgress();
                mEtContent.setEnabled(false);
                break;

            default:
                break;
        }
    }

    @Override
    public void onPostExecute(MsTaskType type, MsResponse response) {
        getTitleBar().hideProgress();
        switch (type) {
            case FORUM_PUBLISH_POST:
                hideProgress();
                mEtContent.setEnabled(true);
                if (response.value instanceof CfPost) {
                    setResult(RESULT_OK);
                    finish();
                }
                break;

            default:
                break;
        }
    }

}
