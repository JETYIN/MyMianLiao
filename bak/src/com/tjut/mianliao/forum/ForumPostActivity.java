package com.tjut.mianliao.forum;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.RefFriendActivity;
import com.tjut.mianliao.component.AttachmentView;
import com.tjut.mianliao.component.EmotionPicker;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.Emotion;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.BitmapLoader;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.Utils;

public class ForumPostActivity extends BaseActivity implements
        View.OnClickListener, AdapterView.OnItemClickListener, MsTaskListener,
        DialogInterface.OnClickListener, EmotionPicker.EmotionListener {

    private static final int REQUEST_REF    = 101;
    private static final int REQUEST_ATTACH = 102;

    private static final int MAX_ATTACHMENT_SIZE = 5 * 1024 * 1024;

    private static final String ATTACHMENT_USED = "attachment_used";

    private TextView mTvType;
    private EditText mEtDesc;
    private CheckBox mCbEmotion;
    private EmotionPicker mEmotionPicker;
    private MultiImageHelper mMIH;

    private MsTaskManager mTaskManager;
    private GetImageHelper mGetImageHelper;

    private VoteEditHelper mVoteEditHelper;
    private EventEditHelper mEventEditHelper;

    private Forum mForum;
    private CfPost mPost;
    private String mDesc;

    private LightDialog mChooseDialog;
    private LightDialog mDiscardDialog;
    private LightDialog mUploadingDialog;
    private LightDialog mPostTypeDialog;
    private LightDialog mVoteConfirmDialog;
    private LightDialog mAttLimitDialog;
    private LightDialog mAttOptionDialog;

    private GridView mGvImages;
    private ImageAdapter mImageAdapter;

    private File mAttachment;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_forum_post;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mForum = getIntent().getParcelableExtra(Forum.INTENT_EXTRA_NAME);
        if (mForum == null || !mForum.isValid()) {
            toast(R.string.cf_forum_not_exist);
            finish();
            return;
        }
        mPost = getIntent().getParcelableExtra(CfPost.INTENT_EXTRA_NAME);
        if (mPost == null) {
            mPost = new CfPost();
            mPost.content = "";
        } else if (mPost.type == CfPost.TYPE_VOTE) {
            toast(R.string.fv_vote_not_editable);
            finish();
            return;
        }

        mTaskManager = MsTaskManager.getInstance(this);
        mTaskManager.registerListener(this);

        mMIH = new MultiImageHelper(this);
        mImageAdapter = new ImageAdapter();
        mGvImages = (GridView) findViewById(R.id.gv_gallery);
        mGvImages.setAdapter(mImageAdapter);
        mGvImages.setOnItemClickListener(this);

        mEmotionPicker = (EmotionPicker) findViewById(R.id.ep_emotions);
        mEmotionPicker.setEmotionListener(this);
        mCbEmotion = (CheckBox) findViewById(R.id.cb_input_emotion);

        mTvType = (TextView) findViewById(R.id.tv_post_type);
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

        mGetImageHelper = new GetImageHelper(this, new GetImageHelper.ImageResultListener() {
            @Override
            public void onImageResult(boolean success, String imageFile, Bitmap bm) {
                if (success) {
                    mMIH.addImage(imageFile);
                    showImages();
                } else {
                    toast(R.string.qa_handle_image_failed);
                }
            }

            @Override
            public void onImageResult(boolean success, ArrayList<String> images) {
                if (success) {
                    mMIH.addImage(images);
                    showImages();
                } else {
                    toast(R.string.qa_handle_image_failed);
                }
            }
        });

        if (isEdit()) {
            getTitleBar().showTitleText(R.string.edit, null);
            mEtDesc.setText(mPost.content);
            mMIH.addImages(mPost.images);
            findViewById(R.id.ll_post_type).setEnabled(false);
        } else {
            getTitleBar().showTitleText(R.string.cf_post, null);
        }

        mVoteEditHelper = new VoteEditHelper(this, mPost,
                (LinearLayout) findViewById(R.id.ll_vote_options),
                (LinearLayout) findViewById(R.id.ll_vote_properties));
        mEventEditHelper = new EventEditHelper(this, mPost,
                (ViewStub) findViewById(R.id.vs_event));

        if (mPost.hasVote()) {
            mVoteEditHelper.setEnabled(true);
            mTvType.setText(R.string.fp_post_type_vote);
        } else if (mPost.hasEvent()) {
            mEventEditHelper.setEnabled(true);
            mTvType.setText(R.string.fp_post_type_event);
        } else {
            mTvType.setText(R.string.fp_post_type_normal);
        }
    }

    private void showImages() {
        findViewById(R.id.iv_input_image).setVisibility(
                mImageAdapter.reachMaxCount() ? View.GONE : View.VISIBLE);
        if (mMIH.getImages().isEmpty()) {
            mGvImages.setVisibility(View.GONE);
        } else {
            mGvImages.setVisibility(View.VISIBLE);
            mImageAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        if (mMIH != null) {
            mMIH.destroy();
        }
        if (mTaskManager != null) {
            mTaskManager.unregisterListener(this);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.et_desc:
                hideEmoPicker();
                break;

            case R.id.iv_input_attach:
                if (hasAttachmentUsed()) {
                    pickAttachment();
                } else {
                    showAttachmentLimitDialog();
                }
                hideEmoPicker();
                break;

            case R.id.iv_input_ref:
                startActivityForResult(new Intent(this, RefFriendActivity.class), REQUEST_REF);
                hideEmoPicker();
                break;

            case R.id.iv_input_image:
                mGetImageHelper.getImage(true, 9 - mMIH.getImages().size());
                hideEmoPicker();
                break;

            case R.id.cb_input_emotion:
                Utils.toggleInput(mEtDesc, mEmotionPicker);
                break;

            case R.id.btn_submit:
                if (!isStateReady() || !hasUpdate()) {
                    return;
                }

                if (mVoteEditHelper.isEnabled()) {
                    showVoteConfirmDialog();
                } else {
                    submit();
                }
                break;

            case R.id.ll_post_type:
                showPostTypeDialog();
                break;

            case R.id.av_att:
                showAttOptionDialog();
                break;

            default:
                break;
        }
    }

    private void pickAttachment() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        if (intentHanlderExists(intent)) {
            startActivityForResult(intent, REQUEST_ATTACH);
        } else {
            toast(R.string.att_install_file_picker);
        }
    }

    private boolean intentHanlderExists(Intent intent) {
        final PackageManager packageManager = getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private void submit() {
        if (Utils.isNetworkAvailable(this)) {
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put(mForum.getIdName(), String.valueOf(mForum.getId()));
            parameters.put("forum_type", String.valueOf(mForum.type));
            parameters.put("thread_id", String.valueOf(mPost.postId));
            parameters.put("content", mDesc);
            if (mVoteEditHelper.isEnabled()) {
                parameters.put("type", String.valueOf(CfPost.TYPE_VOTE));
                parameters.put("endtime", String.valueOf(mVoteEditHelper.getEndtime() / 1000));
                parameters.put("anony_vote", String.valueOf(mVoteEditHelper.getAnonyVote()));
                parameters.put("options", mVoteEditHelper.getOptions());
            }
            if (mEventEditHelper.isEnabled()) {
                parameters.put("type", String.valueOf(CfPost.TYPE_EVENT));
                Event event = mEventEditHelper.getResult();
                parameters.put(Event.REG_DEADLINE, String.valueOf(event.regDeadline));
                parameters.put(Event.START_AT, String.valueOf(event.startAt));
                parameters.put(Event.FEATURE, event.feature);
                parameters.put(Event.LOCATION, event.location);
                parameters.put(Event.CONTACT, event.contact);
                parameters.put(Event.QUOTA, event.quota);
                parameters.put(Event.COST, event.cost);
            }

            HashMap<String, String> files = null;
            files = new HashMap<String, String>();
            if (mMIH.hasUpdate()) {
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
                        String file = "new_image_" + i;
                        files.put(file, image.file);
                        sb.append(file);
                    }
                }
                parameters.put("images", sb.toString());
            }

            if (mAttachment != null) {
                files.put("attachment", mAttachment.getPath());
            }

            mTaskManager.startForumPostTask(isEdit(), parameters, files);
        } else {
            toast(R.string.no_network);
        }
    }

    private boolean hasUpdate() {
        return mMIH.hasUpdate() ||
                !mPost.content.equals(mEtDesc.getText().toString()) ||
                mVoteEditHelper.hasUpdate() ||
                mEventEditHelper.hasUpdate();
    }

    private boolean isEdit() {
        return mPost.postId > 0;
    }

    private boolean isStateReady() {
        if (!mEtDesc.isEnabled()) {
            toast(R.string.handling_last_task);
            return false;
        }

        mDesc = mEtDesc.getText().toString().trim();
        if (TextUtils.isEmpty(mDesc)) {
            toast(R.string.fp_tst_content_empty);
            return false;
        }

        if (!mVoteEditHelper.isReady()) {
            return false;
        }

        if (!mEventEditHelper.isReady()) {
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (mEmotionPicker.isVisible()) {
            hideEmoPicker();
            return;
        }

        if (!mEtDesc.isEnabled()) {
            showUploadingDialog();
        } else if (hasUpdate()) {
            showDiscardDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showChooseDialog() {
        if (mChooseDialog == null) {
            mChooseDialog = new LightDialog(this)
                    .setTitleLd(R.string.please_choose)
                    .setItems(R.array.fp_image_choices, this);
        }
        mChooseDialog.show();
    }

    private void showAttOptionDialog() {
        if (mAttOptionDialog == null) {
            mAttOptionDialog = new LightDialog(this)
                    .setTitleLd(R.string.please_choose)
                    .setItems(R.array.fp_attachment_choices, this);
        }
        mAttOptionDialog.show();
    }

    private void showDiscardDialog() {
        if (mDiscardDialog == null) {
            mDiscardDialog = new LightDialog(this)
                    .setTitleLd(R.string.qa_discard_title)
                    .setMessage(R.string.qa_discard_message)
                    .setNegativeButton(R.string.qa_discard_continue, null)
                    .setPositiveButton(R.string.qa_discard_quit, this)
                    .setButtonBackground(DialogInterface.BUTTON_POSITIVE,
                            R.drawable.selector_btn_red);
        }
        mDiscardDialog.show();
    }

    private void showUploadingDialog() {
        if (mUploadingDialog == null) {
            mUploadingDialog = new LightDialog(this)
                    .setTitleLd(R.string.qa_upload_title)
                    .setMessage(R.string.qa_upload_message)
                    .setNegativeButton(R.string.qa_upload_wait, null)
                    .setPositiveButton(R.string.qa_upload_quit, this)
                    .setButtonBackground(DialogInterface.BUTTON_POSITIVE,
                            R.drawable.selector_btn_red);
        }
        mUploadingDialog.show();
    }

    private void showPostTypeDialog() {
        if (mPostTypeDialog == null) {
            mPostTypeDialog = new LightDialog(this)
                    .setTitleLd(R.string.fp_post_type)
                    .setItems(R.array.fp_post_type_choices, this);
        }
        mPostTypeDialog.show();
    }

    private void showVoteConfirmDialog() {
        if (mVoteConfirmDialog == null) {
            mVoteConfirmDialog = new LightDialog(this)
                    .setTitleLd(R.string.fv_vote_confirm)
                    .setMessage(R.string.fv_vote_confirm_desc)
                    .setNegativeButton(R.string.qa_discard_continue, null)
                    .setPositiveButton(android.R.string.ok, this);
        }
        mVoteConfirmDialog.show();
    }

    private void hideEmoPicker() {
        mEmotionPicker.setVisible(false);
        mCbEmotion.setChecked(false);
    }

    private void showAttachmentLimitDialog() {
        mAttLimitDialog = new LightDialog(this)
                    .setTitleLd(R.string.att_limit_dialog_title)
                    .setMessage(R.string.att_limit_dialog_message)
                    .setPositiveButton(R.string.att_limit_dialog_confirm, this);
        mAttLimitDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REF) {
            if (resultCode == RESULT_OK) {
                String refs = data.getStringExtra(RefFriendActivity.EXTRA_RESULT);
                int ss = mEtDesc.getSelectionStart();
                int color = getResources().getColor(R.color.ref_friend);
                mEtDesc.getText().replace(ss, ss,
                        Utils.getColoredText(refs, refs, color));
            }
        } else if (requestCode == REQUEST_ATTACH) {
            if (data != null) {
                Uri uri = data.getData();
                String path = Utils.getPath(this, uri);
                if (TextUtils.isEmpty(path)) {
                    toast(R.string.att_error_invalid);
                    return;
                }

                File file = new File(path);
                if (file.isFile()) {
                    if (file.length() > MAX_ATTACHMENT_SIZE) {
                        toast(R.string.att_error_reach_limit);
                    } else {
                        mAttachment = file;
                        showAttachment();
                    }
                } else {
                    toast(R.string.att_error_invalid);
                }
            }
        } else if (resultCode == RESULT_OK) {
            mGetImageHelper.handleResult(requestCode, data);
        }
    }

    private void showAttachment() {
        ((AttachmentView) findViewById(R.id.av_att)).show(mAttachment);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Image image = (Image) parent.getItemAtPosition(position);
        mMIH.setPending(image);
        if (image != null) {
            showChooseDialog();
        } else {
            mGetImageHelper.getImage();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mUploadingDialog) {
            finish();
        } else if (dialog == mDiscardDialog) {
            finish();
        } else if (dialog == mChooseDialog) {
            switch (which) {
                case 0:
                    mGetImageHelper.getImageGallery();
                    break;
                case 1:
                    mGetImageHelper.getImageCamera();
                    break;
                case 2:
                    mMIH.deletePending();
                    showImages();
                    break;
                default:
                    break;
            }
        } else if (dialog == mPostTypeDialog) {
            switch (which) {
                case 1:
                    mVoteEditHelper.setEnabled(true);
                    mEventEditHelper.setEnabled(false);
                    mTvType.setText(R.string.fp_post_type_vote);
                    break;
                case 2:
                    mVoteEditHelper.setEnabled(false);
                    mEventEditHelper.setEnabled(true);
                    mTvType.setText(R.string.fp_post_type_event);
                    break;
                case 0:
                default:
                    mVoteEditHelper.setEnabled(false);
                    mEventEditHelper.setEnabled(false);
                    mTvType.setText(R.string.fp_post_type_normal);
                    break;
            }
        } else if (dialog == mVoteConfirmDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                submit();
            }
        } else if (dialog == mAttLimitDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                setAttachmentUsed();
                pickAttachment();
            }
        } else if (dialog == mAttOptionDialog) {
            switch (which) {
                case 0:
                    pickAttachment();
                    break;
                case 1:
                    mAttachment = null;
                    showAttachment();
                    break;
                default:
                    break;
            }
        }
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
    public void onPreExecute(MsTaskType type) {
        switch (type) {
            case FORUM_PUBLISH_POST:
            case FORUM_EDIT_POST:
                getTitleBar().showProgress();
                mEtDesc.setEnabled(false);
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
                getTitleBar().hideProgress();
                mEtDesc.setEnabled(true);
                if (response.value instanceof CfPost) {
                    CfPost post = (CfPost) response.value;
                    setResult(isEdit() ? RESULT_UPDATED : RESULT_OK,
                            new Intent().putExtra(CfPost.INTENT_EXTRA_NAME, post));
                    finish();
                }
                break;

            default:
                break;
        }
    }

    public boolean hasAttachmentUsed() {
        SharedPreferences preferences = DataHelper.getSpForData(this);
        return preferences.getBoolean(ATTACHMENT_USED, false);
    }

    public void setAttachmentUsed() {
        SharedPreferences preferences = DataHelper.getSpForData(this);
        preferences.edit().putBoolean(ATTACHMENT_USED, true).commit();
    }

    private class ImageAdapter extends BaseAdapter {

        private static final int MAX_SIZE = 9;
        private ArrayList<Image> mImages;

        private ImageAdapter() {
            mImages = mMIH.getImages();
        }

        public boolean reachMaxCount() {
            return mImages.size() == MAX_SIZE;
        }

        @Override
        public int getCount() {
            return mImages.size() < MAX_SIZE ? mImages.size() + 1 : mImages.size();
        }

        @Override
        public Object getItem(int position) {
            return position == mImages.size() ? null : mImages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
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
            ProImageView view;
            int viewType = getItemViewType(position);
            if (convertView != null && convertView instanceof ProImageView) {
                view = (ProImageView) convertView;
            } else {
                view = (ProImageView) getLayoutInflater().inflate(R.layout.grid_item_photo,
                        parent, false);
                if (viewType == 1) {
                    view.setScaleType(ImageView.ScaleType.CENTER);
                    view.setImageResource(R.drawable.btn_add_photo);
                    view.setBackgroundResource(R.drawable.selector_btn_add_photo);
                }
            }
            if (viewType == 1) {
                return view;
            }

            String url = mImages.get(position).image;
            if (!TextUtils.isEmpty(url)) {
                view.setImage(AliImgSpec.POST_THUMB_SQUARE.makeUrl(url), R.drawable.bg_img_loading);
            } else {
                BitmapLoader.getInstance().setBitmap(view, mImages.get(position).fileThumb, 0);
            }
            return view;
        }
    }
}
