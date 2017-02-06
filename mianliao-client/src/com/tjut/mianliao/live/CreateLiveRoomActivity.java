package com.tjut.mianliao.live;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.BrowserActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LiveDialog;
import com.tjut.mianliao.contact.ContactUpdateCenter;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.LiveInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.im.IMVideoManager;
import com.tjut.mianliao.mycollege.ImageDeleterHelper;
import com.tjut.mianliao.profile.IdVerifyActivity;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.AliOSSHelper;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by j_hao on 2016/6/21.
 */
public class CreateLiveRoomActivity extends BaseActivity implements View.OnClickListener,
        GetImageHelper.ImageResultListener, AliOSSHelper.OnUploadListener, SurfaceHolder.Callback,
        ContactUpdateCenter.ContactObserver, PullToRefreshBase.OnRefreshListener2<ListView>, AdapterView.OnItemClickListener,
        TextWatcher {

    public static final int SHARE_TYPE_CIRCLE_OF_FRIENDS = 332;
    public static final int SHARE_TYPE_WEI_CHAT = 333;
    public static final int SHARE_TYPE_WEI_BO = 334;
    public static final int SHARE_TYPE_QQ = 335;
    public static final int SHARE_TYPE_QQ_ZONE = 336;

    private static final String URL_USER_AGREEMENT = "http://52mianliao.com/wap/agreement.php";
    /**
     * 直播标题，上传封面，开始直播
     **/
    public static final String TOPIC_INFO = "topic_info";
    private static final String TAG = "Live";
    private final static int QUEST_CODE = 0;
    private final static int RSPONSE_CODE = 66;

    /**
     * 未上传、待验证
     **/
    private String topicString;
    private boolean isUploaded = false;
    private static final int MSG_UPLOADING_SUCCESS = 0;
    private static final int MSG_UPLOADING_FAILURE = 1;
    private int mCameraPosition = 1;
    private LiveDialog mLiveDialog;
    private ArrayList<String> mEditImages;
    private ImageDeleterHelper mImageDeleterHelp;
    private GetImageHelper mGetImageHelper;
    private String mImagePath;
    private AliOSSHelper mAliOSSHelper;
    public static final String EXT_IMAGE_PATH = "ext_image_path";
    private User user;
    private IMVideoManager mIMVideoManager;
    private UserInfo userinfo;
    @ViewInject(R.id.tv_start)
    private TextView tvstart;
    @ViewInject(R.id.live_edit_title)
    private EditText mEditText;
    @ViewInject(R.id.sv_iamge)
    private SurfaceView mSurfaceView;
    @ViewInject(R.id.image_one)
    private ImageView ivconfirm;
    @ViewInject(R.id.tv_topic)
    private TextView mTvTopic;
    @ViewInject(R.id.iv_avatar)
    private ImageView mIvLivePreview;
    @ViewInject(R.id.rl_out_view)
    private RelativeLayout rlOut;

    @ViewInject(R.id.ll_activity_topic)
    private LinearLayout llparent;
    @ViewInject(R.id.et_topic_content)
    private EditText etTopic;
    @ViewInject(R.id.tv_topic_cancel)
    private TextView tvCancel;
    @ViewInject(R.id.ptrlv_topic_result)
    private PullToRefreshListView mPtrSuggestTopic;
    @ViewInject(R.id.ptrlv_topic_search)
    private PullToRefreshListView mPtrSearchTopic;
    @ViewInject(R.id.ll_show_suggest)
    private LinearLayout mLlSuggest;
    @ViewInject(R.id.iv_weichat_friend)
    private ImageView mIvFriendCircle;
    @ViewInject(R.id.iv_weichat)
    private ImageView mIvWeichat;
    @ViewInject(R.id.iv_weibo)
    private ImageView mIvWeibo;
    @ViewInject(R.id.iv_qq)
    private ImageView mIvQQ;
    @ViewInject(R.id.iv_qq_zone)
    private ImageView mIvQQZone;

    private LivingTopicAdapter mSearchTopicAdapter, mSuggestTopicAdapter;
    private String mSearchStr;
    private ArrayList<Topics> topicsList = new ArrayList<>();

    private int mCurrentShareType = SHARE_TYPE_CIRCLE_OF_FRIENDS;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPLOADING_SUCCESS:
                    new CreateLiveRoome(user).executeLong();
                    break;
                case MSG_UPLOADING_FAILURE:
                    Toast.makeText(CreateLiveRoomActivity.this, "上传阿里云失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }


    };

    @Override
    protected int getLayoutResID() {
        return 0;
    }

    @Override
    protected int getBaseLayoutResID() {
        return R.layout.activity_create_live;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mEditImages = new ArrayList<String>();
        userinfo = AccountInfo.getInstance(this).getUserInfo();
        user = new User();
        initTv();
        mAliOSSHelper = AliOSSHelper.getInstance(this);
        mGetImageHelper = new GetImageHelper(this, this);
        mImageDeleterHelp = ImageDeleterHelper.getInstance();
        mSurfaceView.getHolder().addCallback(this);
        mIMVideoManager = IMVideoManager.getInstance(this);
        init();
        fetchTopics(true);
    }

    private void init() {
        mSuggestTopicAdapter = new LivingTopicAdapter(this);
        mPtrSuggestTopic.setAdapter(mSuggestTopicAdapter);
        mSearchTopicAdapter = new LivingTopicAdapter(this);
        mPtrSearchTopic.setAdapter(mSearchTopicAdapter);
        mPtrSearchTopic.setVisibility(View.GONE);
        mPtrSearchTopic.setOnItemClickListener(this);
        mPtrSuggestTopic.setOnItemClickListener(this);
        mPtrSuggestTopic.setMode(PullToRefreshBase.Mode.BOTH);
        mPtrSuggestTopic.setOnRefreshListener(this);
        etTopic.addTextChangedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initTv();
    }

    private void initTv() {
        /**判断用户类别，学生，团体**/
        if (userinfo.isVerified()) {
            ivconfirm.setImageResource(R.drawable.button_identification_student);
        }
    }

    /**
     * 底部变为我知道le
     **/
    private void showCheckSucessed() {
        mLiveDialog.setText(getString(R.string.sucess_imformation));
        showSingleButton();
    }

    private void showCheckFailed() {
        mLiveDialog.setText(getString(R.string.fail_and_again));
        showSingleButton();
    }

    private void showSingleButton() {
        mLiveDialog.setTVSee();
        mLiveDialog.setPositiveButton("", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    /**
     * 验证中
     **/
    private void showWaitDialog() {
        mLiveDialog.setText(getString(R.string.contact_us));
        mLiveDialog.setPositiveButton("", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
        mLiveDialog.setNegativeButton("", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void showDialog() {
        showtoIdVerifyDialog();
    }

    /**
     * 认证失败
     **/
    private void CheckFailDialog() {
        mLiveDialog.setText(getString(R.string.check_fail));
        showtoIdVerifyDialog();
    }

    private void showtoIdVerifyDialog() {
        mLiveDialog = new LiveDialog(this);
        mLiveDialog.setPositiveButton("", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                toIdVerify();
            }
        }).show();
        mLiveDialog.setNegativeButton("", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void toIdVerify() {
        Intent intent = new Intent(this, IdVerifyActivity.class);
        startActivityForResult(intent, QUEST_CODE);
    }


    private String getString() {
        return mEditText.getText().toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_avatar:
                /**只能上传1张图片**/
                mGetImageHelper.getImage(true, 1);
                break;
            case R.id.tv_start:
                /**认证期间**/
                if (userinfo.isVerified()) {
                    tvstart.setText(R.string.in_preparation);
                    if (!TextUtils.isEmpty(getString())) {
                        user.houseName = getString();
                    }
                    if (mImagePath != null && !mImagePath.equals("")) {
                        uploadAliOSS();
                    } else {
                        new CreateLiveRoome(user).executeLong();
                    }
                    Utils.showProgressDialog(CreateLiveRoomActivity.this,
                            "正在创建直播。。。");
                    mIMVideoManager.release();
                } else if (isUploaded) {
                    showWaitDialog();
                }

                if (!userinfo.isVerified() && !isUploaded) {
                    showDialog();
                }
                break;
            case R.id.iv_switch_camera:
                switchCamere();
                break;
            case R.id.iv_quit:
                finish();
                break;
            case R.id.tv_live_rule:
                Intent intent = new Intent(getApplicationContext(), BrowserActivity.class);
                intent.putExtra(BrowserActivity.URL, URL_USER_AGREEMENT);
                intent.putExtra(BrowserActivity.TITLE, getString(R.string.user_agreement));
                startActivity(intent);
                /**开始动画**/

                break;
            case R.id.tv_topic:
                if (topicString != null && !topicString.equals("")) {
                    Toast.makeText(CreateLiveRoomActivity.this, "已经有一个话题了", Toast.LENGTH_SHORT).show();
                    break;
                }
                showAnimationTopic(llparent);
                hideAnimationTopic(rlOut);
                break;
            case R.id.tv_topic_cancel:
                etTopic.setText("");
                break;
            case R.id.iv_back:
                hideSoftKey();
                hideAnimationTopic(llparent);
                showAnimationTopic(rlOut);
                break;
            /**点击设置值**/
            case R.id.rl_topic:
                break;
            case R.id.iv_weichat_friend:
                resetShareImage();
                Picasso.with(this).load(R.drawable.icon_friend_hover).into(mIvFriendCircle);
                mCurrentShareType = SHARE_TYPE_CIRCLE_OF_FRIENDS;
                break;
            case R.id.iv_weichat:
                resetShareImage();
                Picasso.with(this).load(R.drawable.icon_weixin_hover).into(mIvWeichat);
                mCurrentShareType = SHARE_TYPE_WEI_CHAT;
                break;
            case R.id.iv_weibo:
                resetShareImage();
                Picasso.with(this).load(R.drawable.icon_weibo_hover).into(mIvWeibo);
                mCurrentShareType = SHARE_TYPE_WEI_BO;
                break;
            case R.id.iv_qq:
                resetShareImage();
                Picasso.with(this).load(R.drawable.icon_qq_hover).into(mIvQQ);
                mCurrentShareType = SHARE_TYPE_QQ;
                break;
            case R.id.iv_qq_zone:
                resetShareImage();
                Picasso.with(this).load(R.drawable.icon_zone_hover).into(mIvQQZone);
                mCurrentShareType = SHARE_TYPE_QQ_ZONE;
                break;
            default:
                break;
        }
    }

    private void resetShareImage () {
        Picasso.with(this).load(R.drawable.icon_friend_index).into(mIvFriendCircle);
        Picasso.with(this).load(R.drawable.icon_weixin_index).into(mIvWeichat);
        Picasso.with(this).load(R.drawable.icon_weibo_index).into(mIvWeibo);
        Picasso.with(this).load(R.drawable.icon_qq_index).into(mIvQQ);
        Picasso.with(this).load(R.drawable.icon_zone_index).into(mIvQQZone);
    }


    private void hideSoftKey() {
        InputMethodManager imm = (InputMethodManager)
                this.getSystemService(this.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etTopic.getWindowToken(), 0);
    }

    private void showAnimationTopic(View v) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_create_live_right);
        v.startAnimation(animation);
        v.setVisibility(View.VISIBLE);
    }

    private void hideAnimationTopic(View v) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_create_live_left);
        v.startAnimation(animation);
        v.setVisibility(View.INVISIBLE);
    }

    private void uploadAliOSS() {
        mAliOSSHelper.uploadImage(new File(mImagePath), this);
    }

    private void switchCamere() {
        if (mCameraPosition == 1) {
            mCameraPosition = 0;
        } else {
            mCameraPosition = 1;
        }
        mIMVideoManager.setCameraPosition(mCameraPosition);
        mIMVideoManager.toggleCamera();
    }

    @Override
    public void onContactsUpdated(ContactUpdateCenter.UpdateType type, Object data) {
        userinfo = AccountInfo.getInstance(this).getUserInfo();
        initTv();
    }

    @Override
    public void onImageResult(boolean success, String imageFile, Bitmap bm) {
        // save image to server
        mImagePath = imageFile;
        Intent data = new Intent();
        Bitmap btm = BitmapFactory.decodeFile(mImagePath);
        BitmapDrawable bd = new BitmapDrawable(btm);
        mIvLivePreview.setBackgroundDrawable(bd);
    }

    @Override
    public void onImageResult(boolean success, ArrayList<String> images) {
        mImagePath = images.get(0);
        Intent data = new Intent();
        Bitmap btm = BitmapFactory.decodeFile(mImagePath);
        BitmapDrawable bd = new BitmapDrawable(btm);
        mIvLivePreview.setImageDrawable(bd);
        mImagePath = images.get(0);
        if (mImagePath.contains("LSQ_2")) {
            mEditImages.add(mImagePath);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteEditImages();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (llparent.isShown()) {
            hideAnimationTopic(llparent);
        }
    }

    private void deleteEditImages() {
        for (String image : mEditImages) {
            File file = new File(image);
            if (file.exists()) {
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                this.sendBroadcast(intent);
                file.delete();
            }
        }
    }


    /**
     * 回调
     **/
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder != null) {
            mIMVideoManager.setSurfaceHolder(holder);
            mIMVideoManager.startPreview(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIMVideoManager.release();
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchTopics(true);
    }

    /**
     * 点击传值到edittext
     **/
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Topics mTpInfo = (Topics) parent.getItemAtPosition(position);
        hideSoftKey();
        hideAnimationTopic(llparent);
        showAnimationTopic(rlOut);
        topicString = "#" + mTpInfo.name + "#";
        user.topic = topicString;
        int index = mEditText.getSelectionStart();
        Editable edit = mEditText.getEditableText();
        edit.insert(index, topicString);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    private String getEditString() {
        return etTopic.getText().toString();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mSearchStr = getEditString();
        if (mSearchStr != null && !mSearchStr.equals("")) {
            mPtrSearchTopic.setVisibility(View.VISIBLE);
            mLlSuggest.setVisibility(View.GONE);
            /**输入不为空进行任务**/
            new searchTopicTask(mSearchStr).executeLong();
        } else {
            mLlSuggest.setVisibility(View.VISIBLE);
            mPtrSearchTopic.setVisibility(View.GONE);

        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }


    class User {
        String imageURL;
        String houseName;
        String topic;
    }

    /**
     * 上传阿里云成功获取的url
     **/
    @Override
    public void onUploadSuccess(File file, byte[] data, String url, String objectKey) {
        Utils.logD(TAG, " on upload success");
        user.imageURL = url;
        Message msg = new Message();
        msg.what = MSG_UPLOADING_SUCCESS;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onUploadProgress(File file, byte[] data, int byteCount, int totalSize) {
        Utils.logD(TAG, " on upload progress " + byteCount + "/" + totalSize);

    }

    @Override
    public void onUploadFailure(File file, byte[] data, String errMsg) {
        Utils.logD(TAG, " on upload fail : " + errMsg);
        Message msg = new Message();
        msg.what = MSG_UPLOADING_FAILURE;
        mHandler.sendMessage(msg);
        Utils.hidePgressDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == QUEST_CODE && resultCode == RSPONSE_CODE) {
            isUploaded = data.getBooleanExtra("isupload", true);
        }
        mGetImageHelper.handleResult(requestCode, data);

    }

    private HashMap<String, String> getParams(User user) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("title", user.houseName);
        params.put("prev_url", user.imageURL);
        return params;
    }

    /**
     * 开启直播
     **/
    private class CreateLiveRoome extends MsMhpTask {
        public CreateLiveRoome(User user) {
            super(CreateLiveRoomActivity.this, MsRequest.CREATE_LIVE_ROOM,
                    getParams(user), null);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            Utils.hidePgressDialog();
            if (response.isSuccessful()) {
                LiveInfo info = LiveInfo.fromJson(response.getJsonObject().optJSONObject("live"));
                Utils.logD(TAG, "" + (info == null));
                String gid = response.getJsonObject().optString("gid");
                String chatId = response.getJsonObject().optString("chat_id");
                if (info == null) {
                    return;
                }
                Intent intent = new Intent(CreateLiveRoomActivity.this, LivingRecordActivity.class);
                intent.putExtra(LivingRecordActivity.LE_ACTIVITY_ID, info.activityId);
                intent.putExtra(LivingRecordActivity.LE_LIVE_INFO, info);
                intent.putExtra(LivingRecordActivity.LE_LIVE_GID, gid);
                intent.putExtra(LivingRecordActivity.LE_CAHT_ID, chatId);
                startActivity(intent);
                finish();
            } else {

                Toast.makeText(CreateLiveRoomActivity.this, "创建失败" + response.code, Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void fetchTopics(boolean refresh) {
        int size = topicsList.size() - 1;
        int offset = refresh ? 0 : size;
        new getTopicTask(offset).executeLong();
    }

    private class getTopicTask extends MsTask {
        int mOffset;

        public getTopicTask(int offest) {
            super(CreateLiveRoomActivity.this, MsRequest.LIST_MAIN_TOPICS);
            mOffset = offest;
        }

        @Override
        protected String buildParams() {

            return new StringBuilder("offset=").append(mOffset).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrSuggestTopic.onRefreshComplete();
            if (response.isSuccessful()) {
                topicsList = JsonUtil.getArray(response.getJsonArray(), Topics.TRANSFORMER);
                Log.e("getTopic", response.getJsonArray().toString());
                if (topicsList != null && topicsList.size() > 0) {
                    mSuggestTopicAdapter.setData(topicsList, false);
                }
            } else {
                toast("数据获取异常");
            }

        }
    }

    private class searchTopicTask extends AdvAsyncTask<Void, Void, MsResponse> {
        String searchStr;

        public searchTopicTask(String str) {
            searchStr = str;

        }

        @Override
        protected MsResponse doInBackground(Void... params) {

            String prm = new StringBuilder
                    ("key=").append(Utils.urlEncode(searchStr).toString()).
                    toString();
            return HttpUtil.msRequest(getApplicationContext(), MsRequest.LIST_TOPICS, prm);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrSuggestTopic.onRefreshComplete();
            if (response.isSuccessful()) {
                try {
                    JSONArray ja = response.getJsonArray();
                    topicsList.clear();
                    boolean isNew = true;
                    Topics mTpInfo = new Topics();
                    mTpInfo.name = searchStr;
                    topicsList.add(0, mTpInfo);
                    if (ja != null) {
                        for (int i = 0; i < ja.length(); i++) {
                            Topics mTp = Topics.fromJson(ja.getJSONObject(i));
                            topicsList.add(mTp);
                        }
                    }

                    ArrayList<Topics> mTopicInfos = new ArrayList<Topics>();
                    mTopicInfos.addAll(topicsList);
                    if (mTopicInfos.size() > 1 && mTopicInfos.get(1).name.equals(searchStr)) {
                        topicsList.remove(0);
                        isNew = false;
                    }
                    mSearchTopicAdapter.setkeyWord(searchStr);
                    mSearchTopicAdapter.setData(topicsList, isNew);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                toast("数据获取异常");
            }

        }
    }
}
