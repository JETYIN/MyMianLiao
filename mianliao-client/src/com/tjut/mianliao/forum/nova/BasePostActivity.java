package com.tjut.mianliao.forum.nova;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.RefFriendActivity;
import com.tjut.mianliao.common.TuSdkEditImageHelper;
import com.tjut.mianliao.common.TuSdkEditImageHelper.EditImageListener;
import com.tjut.mianliao.component.EmotionPicker;
import com.tjut.mianliao.component.EmotionPicker.EmotionListener;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.RichMlEditText;
import com.tjut.mianliao.component.RichMlEditText.OnAtDelClicklistener;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.Emotion;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.ChooseTopicActivity;
import com.tjut.mianliao.forum.MultiImageHelper;
import com.tjut.mianliao.forum.TopicInfo;
import com.tjut.mianliao.image.PreviewImageActivity;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.BitmapLoader;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.GetImageHelper.ImageResultListener;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.Utils;

public abstract class BasePostActivity extends BaseActivity implements
        OnClickListener, MsTaskListener, OnAtDelClicklistener {

    private static final String TAG = "BasePostActivity";
    
    public static final String EXT_OTHER_SCHOOL_ID = "other_school_id";
    public static final  String EXT_IS_SECOND_TRADE = "is_second_trade";
    
    protected static final int REQUEST_REF = 101;
    protected static final int REQUEST_REF_SQUARE = 102;
    protected static final int REQUEST_REF_CHANNEL = 103;
    public static final int REQUEST_TOPIC = 345;

    protected FrameLayout mFlHeader;
    protected FrameLayout mFlbody; 
    protected FrameLayout mFlFooter;
    protected LinearLayout mLlBaseBody, mLlOtherOIcon;
    protected RichMlEditText mEtContent;
    protected ImageView mIvRefFriend;
    protected ImageView mIvTopic, mIvPic, mIvVoice, mIvRef;
    protected ImageView mCbEmotion;
    protected GridView mGvImages;
    protected EmotionPicker mEmotionPicker;

    protected GetImageHelper mGetImageHelper;
    protected MultiImageHelper mMIH;
    protected ImageAdapter mImageAdapter;
    protected MsTaskManager mTaskManager;
    protected TuSdkEditImageHelper mEditImageHelper;
    
    protected LightDialog mDiscardDialog;
    protected LightDialog mUploadingDialog;
    protected LightDialog mNoticeDialog;
    protected LightDialog mMaxDialog;
    protected RemoveAllListener mListener;

    protected Forum mForum;
    protected CfPost mPost;
    protected String mDesc;

    protected AccountInfo mAccountInfo;
    protected boolean meIsVip;
     protected boolean mHasCallback = true;
    protected boolean mHasVideo = false;
    private int mSchoolId;
    private ArrayList<UserInfo> mRefFriends = new ArrayList<UserInfo>();
    private ArrayList<String> mEditImages;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_base_post;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEditImages = new ArrayList<>();
        mSchoolId = getIntent().getIntExtra(EXT_OTHER_SCHOOL_ID, 0);
        mAccountInfo = AccountInfo.getInstance(this);
        mEditImageHelper = TuSdkEditImageHelper.getInstance();
        meIsVip = mAccountInfo.getUserInfo().vip;
        mForum = getIntent().getParcelableExtra(Forum.INTENT_EXTRA_NAME);
        if (mForum == null || !mForum.isValid()) {
            toast(R.string.cf_forum_not_exist);
            finish();
            return;
        }
        mPost = getIntent().getParcelableExtra(CfPost.INTENT_EXTRA_NAME);
        if (mPost == null) {
            mPost = new CfPost();
            mPost.threadType = CfPost.THREAD_TYPE_NORMAL;
            mPost.content = "";
        } else if (mPost.type == CfPost.TYPE_VOTE) {
            toast(R.string.fv_vote_not_editable);
            finish();
            return;
        }

        mFlHeader = (FrameLayout) findViewById(R.id.fl_header);
        mFlbody = (FrameLayout) findViewById(R.id.fl_body);
        mFlFooter = (FrameLayout) findViewById(R.id.fl_footer);
        mLlBaseBody = (LinearLayout) findViewById(R.id.ll_base_body);
        mEtContent = (RichMlEditText) findViewById(R.id.et_content);
        mIvRefFriend = (ImageView) findViewById(R.id.iv_input_ref);
        mIvPic =  (ImageView) findViewById(R.id.iv_pic);
        mIvVoice =  (ImageView) findViewById(R.id.iv_voice);
        mIvTopic =  (ImageView) findViewById(R.id.iv_topic);
        mIvRef =  (ImageView) findViewById(R.id.iv_input_ref);
        mCbEmotion = (ImageView) findViewById(R.id.cb_input_emotion);
        mLlOtherOIcon = (LinearLayout) findViewById(R.id.ll_other_icon);
        
        mEmotionPicker = (EmotionPicker) findViewById(R.id.ep_emotions);
        mEtContent.setOnAtDelClicklistener(this);
        mGetImageHelper = new GetImageHelper(this, new ImageResultListener() {
            @Override
            public void onImageResult(boolean success, String imageFile, Bitmap bm) {
                if (success) {
                    showImage(mMIH.addImage(imageFile), bm);
                } else {
                    toast(R.string.qa_handle_image_failed);
                }
            }

            @Override
            public void onImageResult(boolean success, ArrayList<String> images) {
                if (success) {
                    for (String img : images) {
                        if (img.contains("LSQ_2")) {
                            mEditImages.add(img);
                        }
                    }
                    showImage(mMIH.addImage(images));
                    if (images.size() > 0) {
                        changeSendColor(true);
                    }
                    if (mMIH.getImages().size() <= 0 && (images == null || images.size() == 0)) {
                        if (mListener != null) {
                            mListener.hasRemoveAll();
                        }
                    }
                } else {
                    toast(R.string.qa_handle_image_failed);
                }
            }
        });

        mMIH = new MultiImageHelper(this);
        mImageAdapter = new ImageAdapter();
        mEtContent.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideEmoPicker();
                }
                mEtContent.setText(Utils.getRefFriendText(mEtContent.getText(), BasePostActivity.this));
            }
        });

        mEtContent.addTextChangedListener(mTextWatcher);
        mEmotionPicker.setEmotionListener(new EmotionListener() {
            @Override
            public void onEmotionClicked(Emotion emotion) {
                mEtContent.getText().insert(mEtContent.getSelectionStart(),
                        emotion.getSpannable(BasePostActivity.this));
            }

            @Override
            public void onBackspaceClicked() {
                Utils.dispatchDelEvent(mEtContent);
            }
        });

        if (isEdit()) {
            getTitleBar().showTitleText(R.string.edit, null);
            mEtContent.setText(mPost.content);
            mMIH.addImages(mPost.images);
        } else {
            getTitleBar().showTitleText(R.string.cf_post, null);
        }
        getTitleBar().showRightText(R.string.send, this);
        mTaskManager = MsTaskManager.getInstance(this);
        mTaskManager.registerListener(this);
    }
    
    protected abstract boolean setCanRefFriend();
    
    protected TextWatcher mTextWatcher = new TextWatcher() {
        
        CharSequence lastChar;
        int mStart;
        int mBefore;
        int mCount;
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mStart = start;
            mBefore = before;
            mCount = count;
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        
        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() >= 1) {
                changeSendColor(true);
                if (mBefore == 0 && mCount > 0) {
                    lastChar = s.toString().substring(mStart, mStart + mCount);
                } else {
                    lastChar = "";
                }
                
                if (setCanRefFriend() && "@".equals(lastChar) && mHasCallback) {
                    startActivityForResult(new Intent(BasePostActivity.this, RefFriendActivity.class), REQUEST_REF_SQUARE);
                    hideEmoPicker();
                    mHasCallback = false;
                }
            } else {
                if (mMIH.hasNewImages() || mHasVideo) {
                    changeSendColor(true);
                } else {
                    changeSendColor(false);
                }
            }
        }
    }; 
    
    protected void changeSendColor(boolean isChange) {
        if (isChange) {
            getTitleBar().setRightTextColor(0XFF78A8E4);
        } else {
            getTitleBar().setRightTextColor(0XFFBEC9E1);
        }
    }
    
    protected void showProgress() {
    	//Utils.hidePgressDialog();
        Utils.showProgressDialog(this, R.string.cf_posting);
        
    }
    
    protected void hideProgress() {
        Utils.hidePgressDialog();
    }
    
    private ArrayList<String> getImageUrls() {
        ArrayList<String> urls = new ArrayList<String>();
        if (mMIH.getImages() != null) {
            for (Image image : mMIH.getImages()) {
                if (image.file != null) {
                    urls.add(image.file);
                } else if (image.image != null) {
                    urls.add(image.image);
                }
            }
        }
        return urls;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        showEmotionIcon();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        hideEmoPicker();
    }

    @Override
    protected void onDestroy() {
        if (mMIH != null) {
            mMIH.destroy();
        }
        if (mTaskManager != null) {
            mTaskManager.unregisterListener(this);
        }
        mEditImageHelper.clear();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        switch (requestCode) {
            case REQUEST_REF:// @haoyou
                if (resultCode == RESULT_OK) {
                    String refs = data.getStringExtra(RefFriendActivity.EXTRA_RESULT);
                    ArrayList<UserInfo> datas = data.getParcelableArrayListExtra(RefFriendActivity.EXTRA_USERINFOS);
                    if (mRefFriends == null || mRefFriends.size() == 0) {
                        mRefFriends = datas; 
                    } else {
                        mRefFriends.addAll(datas);
                    }
                    int ss = mEtContent.getSelectionStart();
                    Editable s = mEtContent.getText().replace(ss, ss, refs);
                    mEtContent.setText(s);
                    mHasCallback = true;
                } 
                
                break;
            case REQUEST_REF_SQUARE:// @haoyou
                if (resultCode == RESULT_OK) {
                    String refs = data.getStringExtra(RefFriendActivity.EXTRA_RESULT);
                    ArrayList<UserInfo> datas = data.getParcelableArrayListExtra(RefFriendActivity.EXTRA_USERINFOS);
                    if (mRefFriends == null || mRefFriends.size() == 0) {
                        mRefFriends = datas; 
                    } else {
                        mRefFriends.addAll(datas);
                    }
                    int ss = mEtContent.getSelectionStart();
                    Editable editable = mEtContent.getText();
                    int start = ss -1;
                    editable.replace(start, ss, "");
                    Editable s = editable.insert(start, refs);
                    mEtContent.setText(s);
                    mHasCallback = true;
                } else if (resultCode == RESULT_CANCELED) {
                    int ss = mEtContent.getSelectionStart();
                    Editable editable = mEtContent.getText();
                    Editable s = editable.replace(ss - 1, ss, "");
                    mEtContent.setText(s);
                    mHasCallback = true;
                }
                break;
                
            case PreviewImageActivity.PREVIEW_REQUEST_CODE:
                ArrayList<String> urls = data.getStringArrayListExtra(
                        PreviewImageActivity.EXTRA_IMAGES_LIST);
                if (urls != null) {
                    mMIH.resetImages(getImagesByUrls(urls));
                }
                mImageAdapter.notifyDataSetChanged();
                break;
            case REQUEST_TOPIC:
                if (resultCode == RESULT_OK) {
                    TopicInfo mTpInfo = data.getParcelableExtra(ChooseTopicActivity.TOPIC_INFO);
                    String mTopicString = "#" + mTpInfo.name + "#";
                    mTopicString = mTopicString.replaceAll("(@|﹫|＠)", "");
                    int index = mEtContent.getSelectionStart();
                    Editable edit = mEtContent.getEditableText();
                    edit.insert(index, mTopicString);
                }
            default:
                if (resultCode == RESULT_OK) {
                    mGetImageHelper.handleResult(requestCode, data);
                }
                break;
        }
    }

    protected List<Image> getImagesByUrls(ArrayList<String> urls) {
        ArrayList<Image> images = new ArrayList<>();
        for (String url :urls) {
            String file = GetImageHelper.saveAsTodo(this, url);
            Image image = new Image(file, GetImageHelper.zoomOutImage(file, Image.PREVIEW_SIZE));
            images.add(image);
        }
        return images;
    }

    @Override
    public void onBackPressed() {
        if (mEmotionPicker.isVisible()) {
            hideEmoPicker();
            return;
        }

        if (!mEtContent.isEnabled()) {
            showUploadingDialog();
        } else if (hasUpdate()) {
            showDiscardDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_input_ref:
                startActivityForResult(new Intent(this, RefFriendActivity.class), REQUEST_REF);
                hideEmoPicker();
                break;

            case R.id.et_content:
            	hideEmoPicker();
            	break;
            case R.id.cb_input_emotion:
                Utils.toggleInput(mEtContent, mEmotionPicker);
                break;

            case R.id.tv_right:
                if (!isStateReady() || !hasUpdate()) {
                    return;
                }
                submit();
                break;
            case R.id.iv_topic:
                Intent intent = new Intent(BasePostActivity.this, ChooseTopicActivity.class);
                if (mSchoolId > 0) {
                    intent.putExtra(ChooseTopicActivity.SCHOOL_ID, mSchoolId);
                }
                intent.putExtra(ChooseTopicActivity.FORUM_TYPE, mForum.type);
                intent.putExtra(ChooseTopicActivity.FORUM_ID, mForum.id);
                startActivityForResult(intent, REQUEST_TOPIC);
                break;
            default:
                break;
        }
    }

	private void deleteImage(String imageFile) {
		if (mMIH.delImage(imageFile))
			mImageAdapter.notifyDataSetChanged();
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
        switch (type) {
            case FORUM_PUBLISH_POST:
            case FORUM_EDIT_POST:
                deleteTempImageFile(); 
                hideProgress();
                mEtContent.setEnabled(true);
                if (response.value instanceof CfPost) {
                    CfPost post = (CfPost) response.value;
                    setResult(isEdit() ? RESULT_UPDATED : RESULT_OK,
                            new Intent().putExtra(CfPost.INTENT_EXTRA_NAME, post));
                    finish();
                } else if (!response.isSuccessful()) {
                    switch (response.code) {
                        case MsResponse.MS_FAIL_SCHOOL_POST_CAN_NOT_TODAY:
                            showNoticeDialog();
                            break;
                        case MsResponse.MS_FAIL_SCHOOL_POST_DAY_MAX:
                            showMaxdDialog();
                            break;
                        case MsResponse.FAIL_HAS_BEEN_BANNED:
                            toast(R.string.no_speak_toast);
                            break;
                        default:
                            response.showFailInfo(this, R.string.qa_upload_failed);
                            break;
                    }
                }
                break;

            default:
                break;
        }
    }

    private void deleteTempImageFile() {
        ArrayList<Image> images = mMIH.getImages();
        if (images != null && images.size() > 0) {
            for (Image image : images) {
                File file = new File(image.file);
                if (file.exists()) {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(file);
                    intent.setData(uri);
                    this.sendBroadcast(intent);
                    file.delete();
                    Utils.logD(TAG, "delete temp image : " + image.file + "--");
                }
            }
        }
        for (String image : mEditImages) {
            File file = new File(image);
            if (file.exists()) {
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                this.sendBroadcast(intent);
                file.delete();
                Utils.logD(TAG, "delete edit image : " + image + "--");
            }
        }
    }

    protected HashMap<String, String> getParams() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(mForum.getIdName(), String.valueOf(mForum.getId()));
        parameters.put("forum_type", String.valueOf(mForum.type));
        parameters.put("thread_id", String.valueOf(mPost.postId));
        parameters.put("thread_type", String.valueOf(mPost.threadType));
        parameters.put("type", String.valueOf(mPost.type));
        parameters.put("content", mDesc);
        if (mSchoolId > 0) {
            parameters.put("other_school_id", String.valueOf(mSchoolId));
        }
        parameters.put("all_types", String.valueOf(1));
        if (mRefFriends != null && mRefFriends.size() > 0) {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < mRefFriends.size(); i++) {
                json.append(i == 0 ? "{\"uid\":\"" : ",{\"uid\":\"")
                .append(mRefFriends.get(i).userId).append("\",\"nick\":\"")
                .append(mRefFriends.get(i).nickname).append("\"}");
            }
            json.append("]");
            parameters.put("at_users", json.toString()); 
        }
        if (mGvImages != null && mGvImages.isShown() && mMIH.hasUpdate()) {
            StringBuilder sb = new StringBuilder();
            int size = mMIH.getImages().size();
            for (int i = 0; i < size; i++) {
                Image image = mMIH.getImages().get(i);
                if (sb.length() > 0) {
                    sb.append(",");
                }
                if (image.id > 0) {
                    sb.append(image.id);
                } else {
                    sb.append("new_image_").append(i);
                }
            }
            parameters.put("images", sb.toString());
        }

        return parameters;
    }

    protected HashMap<String, String> getFiles() {
        HashMap<String, String> files = new HashMap<String, String>();
        if (mGvImages != null && mGvImages.isShown() && mMIH.hasUpdate()) {
            int size = mMIH.getImages().size();
            for (int i = 0; i < size; i++) {
                Image image = mMIH.getImages().get(i);
                if (image.id == 0) {
                    files.put("new_image_" + i, image.file);
                }
            }
        }
        return files;
    }

    protected void submit() {
        if (Utils.isNetworkAvailable(this)) {
            mTaskManager.startForumPostTask(isEdit(), getParams(), getFiles());
        } else {
            toast(R.string.no_network);
        }
    }

    protected boolean isStateReady() {
        if (!mEtContent.isEnabled()) {
            toast(R.string.handling_last_task);
            return false;
        }

        isContentReady();

        return true;
    }
    
    protected boolean hasPicture() {
		return mMIH.hasNewImages();
	}

    protected boolean isContentReady() {
		mDesc = mEtContent.getText().toString().trim();
        if (TextUtils.isEmpty(mDesc)) {
            return false;
        }
        return true;
	}

    protected boolean hasUpdate() {
        return (mGvImages != null && mGvImages.isShown() && mMIH.hasUpdate())
        		|| !mPost.content.equals(mEtContent.getText().toString());
    }

    protected boolean isEdit() {
        return mPost.postId > 0;
    }

    protected void hideEmoPicker() {
        mEmotionPicker.setVisible(false);
//        mCbEmotion.setEnabled(false);
    }
    
    protected void showEmotionIcon() {
        mCbEmotion.setVisibility(View.VISIBLE);
    }

    protected void showImage(Image image, Bitmap bm) {
        mImageAdapter.notifyDataSetChanged();
    }

    protected void showImage(ArrayList<Image> images) {
        if (mMIH.getImages() != null && mMIH.getImages().size() > 0) {
            mGvImages.setVisibility(View.VISIBLE);
        }
        mImageAdapter.notifyDataSetChanged();
    }

    protected void chooseImage(Image image, final int position) {
        if (!mEtContent.isEnabled()) {
            return;
        }
        mMIH.setPending(image);
        if (image != null) {
//            Intent intent = new Intent(BasePostActivity.this, PreviewImageActivity.class);
//            intent.putExtra(PreviewImageActivity.EXTRA_IMAGE_INDEX, position);
//            intent.putStringArrayListExtra(
//                    PreviewImageActivity.EXTRA_IMAGE_URLS, getImageUrls());
//            startActivityForResult(intent, PreviewImageActivity.PREVIEW_REQUEST_CODE);
            mEditImageHelper.editImageByTuSdk(this, image.file, new EditImageListener() {
                
                @Override
                public void onEditImageResult(boolean succ, String imagePath) {
                    if (succ && imagePath != null && !"".equals(imagePath)) {
                        mMIH.replaceImage(position, imagePath);
                        mImageAdapter.notifyDataSetChanged();
                    }
                }
            });
        } else {
            mGetImageHelper.getImage(true, 9 - mMIH.getImages().size());
        }
    }
    
    protected void chooseImage(Image image) {
        if (!mEtContent.isEnabled()) {
            return;
        }
        mMIH.setPending(image);
        if (image != null) {
            Intent intent = new Intent(BasePostActivity.this, PreviewImageActivity.class);
            intent.putExtra(PreviewImageActivity.EXTRA_IMAGE_INDEX, 0);
            intent.putStringArrayListExtra(
                    PreviewImageActivity.EXTRA_IMAGE_URLS, getUrlByImage(image));
            startActivityForResult(intent, PreviewImageActivity.PREVIEW_REQUEST_CODE);
        } else {
            mGetImageHelper.getImage(true, 9 - mMIH.getImages().size());
        }
    }
    
    private ArrayList<String> getUrlByImage(Image image) {
        ArrayList<String> url = new ArrayList<>();
        url.add(image.file);
        return url;
    }

    protected void showDiscardDialog() {
        if (mDiscardDialog == null) {
            mDiscardDialog = new LightDialog(this).setTitleLd(R.string.qa_discard_title)
                    .setMessage(R.string.qa_discard_message)
                    .setButtonBackground(DialogInterface.BUTTON_POSITIVE, R.drawable.selector_btn_red)
                    .setButtonBackground(DialogInterface.BUTTON_NEGATIVE, R.drawable.selector_btn_blue)
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

    protected void showUploadingDialog() {
        if (mUploadingDialog == null) {
            mUploadingDialog = new LightDialog(this).setTitleLd(R.string.qa_upload_title)
                    .setMessage(R.string.qa_upload_message)
                    .setButtonBackground(DialogInterface.BUTTON_POSITIVE, R.drawable.selector_btn_red)
                    .setButtonBackground(DialogInterface.BUTTON_NEGATIVE, R.drawable.selector_btn_blue)
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
    
    private void showNoticeDialog() {
        if (mNoticeDialog == null) {
            mNoticeDialog = new LightDialog(this).setTitleLd(R.string.plc_prompt_unlock)
                    .setMessage(R.string.plc_prompt_unlock_post_fail)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
        }
        mNoticeDialog.show();
    }

    private void showMaxdDialog() {
        if (mMaxDialog == null) {
            mMaxDialog = new LightDialog(this)
                    .setTitleLd(R.string.plc_prompt_unlock)
                    .setMessage(meIsVip ? R.string.plc_prompt_unlock_vip_max : R.string.plc_prompt_unlock_max)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    finish();
                                }
                            });
        }
        mMaxDialog.show();
    }
    
    protected class ImageAdapter extends BaseAdapter {

        private static final int MAX_SIZE = 9;
        private ArrayList<Image> mImages;

        private ImageAdapter() {
            mImages = mMIH.getImages();
        }
        
        @Override
        public int getCount() {
            return mImages.size() < MAX_SIZE ? mImages.size() + 1 : mImages.size();
        }

        @Override
        public Image getItem(int position) {
            return position == mImages.size() ? null : mImages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return position == mImages.size() ? 1 : 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            int viewType = getItemViewType(position);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.grid_item_photo, parent, false);
                holder = new ViewHolder();
                ViewUtils.inject(holder, convertView);
                holder.ivDel.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        deleteImage((String) v.getTag());
                        if (mMIH.getImages() == null || mMIH.getImages().size() <= 0) {
                            if (mListener != null) {
                                mListener.hasRemoveAll();
                            }
                        }
                    }
                });
                if (viewType == 1) {
                    holder.pivPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
                    holder.pivPhoto.setImageResource(R.drawable.bottom_add_pic);
                    holder.ivDel.setVisibility(View.GONE);
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (viewType == 1) {
                holder.ivDel.setVisibility(View.GONE);
                return convertView;
            }

            Image item = getItem(position);
            if (!TextUtils.isEmpty(item.image)) {
                String thumb = AliImgSpec.POST_THUMB_SQUARE.makeUrl(item.image);
                holder.pivPhoto.setImage(thumb, R.drawable.bg_img_loading);
                holder.ivDel.setTag(thumb);
            } else {
                BitmapLoader.getInstance().setBitmap(holder.pivPhoto, item.fileThumb, 0);
                holder.ivDel.setTag(item.fileThumb);
            }
            holder.ivDel.setVisibility(View.VISIBLE);
            return convertView;
        }
    }
    
    @Override
    public void onDelClick(int index) {
        if (index < mRefFriends.size()) {
            mRefFriends.remove(index);
        }
    }
    
    private class ViewHolder {
        @ViewInject(R.id.giv_photo)
        ProImageView pivPhoto;
        @ViewInject(R.id.iv_del)
        ImageView ivDel;
    }
    
    public interface RemoveAllListener {
        void hasRemoveAll();
    }
    
}