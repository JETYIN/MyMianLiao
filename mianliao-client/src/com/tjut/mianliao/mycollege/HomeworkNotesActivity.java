package com.tjut.mianliao.mycollege;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.EmotionPicker;
import com.tjut.mianliao.component.EmotionPicker.EmotionListener;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.RichMlEditText;
import com.tjut.mianliao.component.RichMlEditText.OnAtDelClicklistener;
import com.tjut.mianliao.curriculum.CourseManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.Course;
import com.tjut.mianliao.data.Emotion;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.mycollege.NoteInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.MultiImageHelper;
import com.tjut.mianliao.image.PreviewImageActivity;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.BitmapLoader;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.GetImageHelper.ImageResultListener;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.Utils;

public class HomeworkNotesActivity extends BaseActivity implements OnAtDelClicklistener,
                MsTaskListener, OnClickListener {
    public static final String EXT_HOMEWORK_NOTE = "ext_homework_note";
    public static final String EXT_EDIT_NOTE = "ext_edit_note";
    private static final int MAX_LENGTH = 500;
    
    private TextView mSubjectTitle;
    private ImageView mSubjectItem;
    private String mSubject;
    private boolean isEdit;
    private NoteInfo edNoteInfo;
    private LightDialog mSubchoiceDialog;
    private String mNoteType;
    private String mImageIds;
    private ArrayList<Image> mDelImages;
    private ArrayList<Course> mCourses = new ArrayList<>();
    private ArrayList<String> mCourseNames = new ArrayList<>();
    public static final String EXT_OTHER_SCHOOL_ID = "other_school_id";
    public static final  String EXT_IS_SECOND_TRADE = "is_second_trade";
    
    private static final int REQUEST_REF = 101;
    private static final int REQUEST_REF_SQUARE = 102;
    private static final int REQUEST_REF_CHANNEL = 103;
    public static final int REQUEST_TOPIC = 345;

    private FrameLayout mFlHeader;
    private FrameLayout mFlbody;
    private FrameLayout mFlFooter;
    private LinearLayout mLlBaseBody;
    private RichMlEditText mEtContent;
    private ImageView mIvRefFriend;
    private TextView mTvContentNum;
    private ImageView  mIvPic;
    private ImageView mCbEmotion;
    private GridView mGvImages;
    private EmotionPicker mEmotionPicker;

    private GetImageHelper mGetImageHelper;
    private MultiImageHelper mMIH;
    private ImageAdapter mImageAdapter;
    private MsTaskManager mTaskManager;

    private LightDialog mDiscardDialog;
    private LightDialog mUploadingDialog;
    private LightDialog mNoticeDialog;
    private LightDialog mMaxDialog;
    private RemoveAllListener mListener;

    private Forum mForum;
    private CfPost mPost;
    private String mDesc;

    private boolean mIsCancleChooseImage;
    
    private AccountInfo mAccountInfo;
    private boolean meIsVip;
    private boolean mHasCallback = true;
    private ArrayList<UserInfo> mRefFriends = new ArrayList<UserInfo>();
    

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_warm_memo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountInfo = AccountInfo.getInstance(this);
        meIsVip = mAccountInfo.getUserInfo().vip;
        mForum = getIntent().getParcelableExtra(Forum.INTENT_EXTRA_NAME);
        if (mForum == null || !mForum.isValid()) {
            toast(R.string.cf_forum_not_exist);
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
        mCbEmotion = (ImageView) findViewById(R.id.cb_input_emotion);
        mTvContentNum = (TextView) findViewById(R.id.tv_content_num);
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
                mEtContent.setText(Utils.getRefFriendText(mEtContent.getText(), HomeworkNotesActivity.this));
            }
        });

        mEtContent.addTextChangedListener(mTextWatcher);
        mEmotionPicker.setEmotionListener(new EmotionListener() {
            @Override
            public void onEmotionClicked(Emotion emotion) {
                mEtContent.getText()
                        .insert(mEtContent.getSelectionStart(), emotion.getSpannable(HomeworkNotesActivity.this));
            }

            @Override
            public void onBackspaceClicked() {
                Utils.dispatchDelEvent(mEtContent);
            }
        });

        mTaskManager = MsTaskManager.getInstance(this);
        mTaskManager.registerListener(this);
        
        View v = mInflater.inflate(R.layout.notes_subjectchoose, mFlHeader);
        mInflater.inflate(R.layout.normal_post_footer, mFlFooter);
        mSubjectTitle = (TextView) findViewById(R.id.tv_subject_choose);
        mSubjectItem = (ImageView) findViewById(R.id.bt_subjectItem);
        mGvImages = (GridView) findViewById(R.id.gv_gallery);
        mGvImages.setAdapter(mImageAdapter);
        mGvImages.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Image image = (Image) parent.getItemAtPosition(position);
                chooseImage(image, position);
            }
        });
        Intent intent = getIntent();
        String editis = intent.getStringExtra("Editis");
        if (editis.equals("false")) {
            isEdit = false;
            getTitleBar().setTitle(intent.getStringExtra("mTitle"));
            mNoteType = intent.getStringExtra("NOTE_TYPE");
            mSubjectTitle.setText(getString(R.string.take_note_choose_sub));
        } else {
            edNoteInfo = intent.getParcelableExtra(EXT_EDIT_NOTE);
            isEdit = true;
            if (edNoteInfo.images != null && edNoteInfo.images.size() > 0) {
                mMIH.resetImages(edNoteInfo.images);
                mGvImages.setVisibility(View.VISIBLE);
                mImageAdapter.notifyDataSetChanged();
            }

            switch (edNoteInfo.noteType) {
                case 1:
                    mNoteType = "NOTE";
                    break;
                case 2:
                    mNoteType = "HOMEWORK";
                    break;
                default:
                    break;
            }

            getTitleBar().setTitle(R.string.edit);
            mSubjectTitle.setText(edNoteInfo.course);
            mEtContent.setText(edNoteInfo.content);
        }
        mEtContent.setShouldWatcher(false);
        mSubjectItem.setOnClickListener(this);
        new CourseTask(CourseManager.getInstance(this).getSemester()).executeLong();
        getTitleBar().showRightText(R.string.finish, this);
        mCbEmotion.setVisibility(View.VISIBLE);
        mIvPic.setVisibility(View.VISIBLE);
        mCbEmotion.setEnabled(true);
        mIvPic.setEnabled(true);
    }
    
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
            mTvContentNum.setText(getString(R.string.note_content_length,
                    MAX_LENGTH - s.length()));
            if (s.length() >= 1) {
                changeSendColor(true);
                if (mBefore == 0 && mCount > 0) {
                    lastChar = s.toString().substring(mStart, mStart + mCount);
                } else {
                    lastChar = "";
                }
                
            } else {
                changeSendColor(false);
            }
             
        }
    }; 
    
    private void changeSendColor(boolean isChange) {
        if (isChange) {
            getTitleBar().setRightTextColor(0XFF78A8E4);
        } else {
            getTitleBar().setRightTextColor(0XFFBEC9E1);
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

    private List<Image> getImagesByUrls(ArrayList<String> urls) {
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
		case R.id.rl_msub_choose:
			showSubDialog();
			break;
		case R.id.iv_pic:
            mGvImages.setVisibility(View.VISIBLE);
            mIvPic.setEnabled(true);
            mGetImageHelper.getImage(true, 9 - mMIH.getImages().size());
            break;
         case R.id.et_content:
             hideEmoPicker();
             break;
         case R.id.cb_input_emotion:
             Utils.toggleInput(mEtContent, mEmotionPicker);
             break;

         case R.id.tv_right:
             if (!isStateReady() && !hasUpdate()) {
                 return;
             }
             submit();
             break;
		default:
			break;
		}
    }

    private void showSubDialog() {
        if (mSubchoiceDialog == null) {
            mSubchoiceDialog = new LightDialog(this).setTitleLd(R.string.please_choose)
                    .setItems(mCourseNames,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSubjectTitle.setText(mCourseNames.get(which));
                            mSubject = mCourseNames.get(which);
                        }
                    });
        }
        mSubchoiceDialog.show();
    }

    private class CourseTask extends AdvAsyncTask<Void, Void, MsResponse> {
        private int mSemester;

        public CourseTask(int semester) {
            mSemester = semester;
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected MsResponse doInBackground(Void... params) {
            MsResponse mr = HttpUtil.msRequest(getApplicationContext(),
                    MsRequest.GET_MY_COURSES, "semester="
                    + mSemester);
            return mr;
        }

        @Override
        protected void onPostExecute(MsResponse mr) {
            getTitleBar().hideProgress();
            if (MsResponse.isSuccessful(mr)) {
                try {
                    JSONArray ja = new JSONArray(mr.response);
                    int size = ja.length();
                    mCourses.clear();
                   mCourseNames.add("默认");
                    for (int i = 0; i < size; i++) {
                        Course c = Course.fromJson(ja.optJSONObject(i));
                        if (c != null) {
                            mCourses.add(c);
                            mCourseNames.add(c.name);
                        }
                    }
                } catch (Exception e) {
                }
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.course_tst_failed_get_init_data,
                        mr.code));
            }
        }
    }

    protected void submit() {
        if (Utils.isNetworkAvailable(this)) {
            mTaskManager.startPostNoteTask(isEdit, getParams(), getFiles());
        } else {
            toast(R.string.no_network);
        }
    }


    protected HashMap<String, String> getParams() {
        HashMap<String, String> param = new HashMap<>();
        if (edNoteInfo != null) {
            param.put("id", String.valueOf(edNoteInfo.postId));
        }
        param.put("course", mSubject);
        param.put("content", mDesc);
        if (isEdit) {
            if (edNoteInfo.images != null) {
                param.put("image_ids", getImageId());
            }
        }
        if (mNoteType.equals("NOTE")) {
            param.put("note_type", String.valueOf(NoteInfo.TYPE_PHOTO));
        } else {
            param.put("note_type", String.valueOf(NoteInfo.TYPE_HOMEWORK));
        }

        if (mGvImages.isShown() && mMIH.hasUpdate()) {
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
            param.put("images", sb.toString());
        }

        return param;
    }

    @Override
    public void onPreExecute(MsTaskType type) {
        switch (type) {
            case FORUM_NOTE_POST:
                getTitleBar().showProgress();
                mEtContent.setEnabled(false);
                break;
            case FORUM_EDIT_NOTE:
                getTitleBar().showProgress();
                mEtContent.setEnabled(false);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPostExecute(MsTaskType type, MsResponse response) {
        switch (type) {
            case FORUM_NOTE_POST:
                getTitleBar().hideProgress();
                mEtContent.setEnabled(true);
                if (response.isSuccessful() && response.getJsonObject() != null) {
                    NoteInfo notes = NoteInfo.fromJson(response.getJsonObject());
                    Intent data = new Intent();
                    data.putExtra(EXT_HOMEWORK_NOTE, notes);
                    setResult(RESULT_OK, data);
                    finish();
                }
                break;
            case FORUM_EDIT_NOTE:
                getTitleBar().hideProgress();
                mEtContent.setEnabled(true);
                if (response.isSuccessful() && response.getJsonObject() != null) {
                    NoteInfo notes = NoteInfo.fromJson(response.getJsonObject());
                    Intent data = new Intent();
                    data.putExtra(EXT_HOMEWORK_NOTE, notes);
                    setResult(RESULT_OK, data);
                    finish();
                }
                break;
            default:
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PreviewImageActivity.PREVIEW_REQUEST_CODE){
            ArrayList<String> urls = data.getStringArrayListExtra(
                    PreviewImageActivity.EXTRA_IMAGES_LIST);
            if (urls != null) {
                mMIH.resetImages(getImagesByUrls(urls));
                mMIH.setDeletedImages(getDeleteImg(urls));
            }
            mImageAdapter.notifyDataSetChanged();
 
        } else {
            if (resultCode == RESULT_OK) {
                mGetImageHelper.handleResult(requestCode, data);
            }
        }
    }
    
    private List<Image> getDeleteImg(ArrayList<String> urls) {
        if (edNoteInfo == null) {
            return null;
        }
        ArrayList<Image> allImages = edNoteInfo.images;
        List<Image> images = getImagesByUrls(urls);
        if (images == null || images.size() == 0) {
            return null;
        }
        ArrayList<Image> imgs = new ArrayList<>();
        for (Image img : images) {
            if (allImages.contains(img)) {
                imgs.add(img);
            }
        }
        allImages.removeAll(imgs);
        edNoteInfo.images.addAll(imgs);
        return allImages;
    }
    
    private String getImageId() {
        StringBuilder sb = new StringBuilder();
        ArrayList<Image> images = mMIH.getImages();
        if (images == null || images.size() == 0) {
            return "";
        }
        boolean isFirst = true;
        for (Image img : images) {
            if (isFirst) {
                sb.append(img.id);
                isFirst = false;
            } else {
                sb.append(",").append(img.id);
            }
        }
        return sb.toString();
    }

    protected boolean setCanRefFriend() {
        return false;
    }
    
    private void deleteImage(String imageFile) {
        if (mMIH.delImage(imageFile))
            mImageAdapter.notifyDataSetChanged();
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
        return (mGvImages != null && mGvImages.isShown() && mMIH.hasUpdate());
    }

    protected void hideEmoPicker() {
        mEmotionPicker.setVisible(false);
    }

    protected void showImage(Image image, Bitmap bm) {
        mImageAdapter.notifyDataSetChanged();
    }

    protected void showImage(ArrayList<Image> images) {
        mImageAdapter.notifyDataSetChanged();
    }

    protected void chooseImage(Image image, int position) {
        if (!mEtContent.isEnabled()) {
            return;
        }
        mMIH.setPending(image);
        if (image != null) {
            Intent intent = new Intent(HomeworkNotesActivity.this, PreviewImageActivity.class);
            intent.putExtra(PreviewImageActivity.EXTRA_IMAGE_INDEX, position);
            intent.putStringArrayListExtra(
                    PreviewImageActivity.EXTRA_IMAGE_URLS, getImageUrls());
            startActivityForResult(intent, PreviewImageActivity.PREVIEW_REQUEST_CODE);
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
            Intent intent = new Intent(HomeworkNotesActivity.this, PreviewImageActivity.class);
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
                    holder.pivPhoto.setImageResource(R.drawable.icon_photo_add);
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
                holder.ivDel.setTag(item.image);
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
    
    private class ViewHolder{
        @ViewInject(R.id.giv_photo)
        ProImageView pivPhoto;
        @ViewInject(R.id.iv_del)
        ImageView ivDel;
    }
    
    public interface RemoveAllListener{
        void hasRemoveAll();
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

}
