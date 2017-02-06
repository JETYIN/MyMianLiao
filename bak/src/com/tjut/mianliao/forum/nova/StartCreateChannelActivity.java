package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProAvatarView;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.GetImageHelper.ImageResultListener;
import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;

public class StartCreateChannelActivity extends BaseActivity implements OnClickListener,
        DialogInterface.OnClickListener, ImageResultListener {

    private ProAvatarView mAvatarView;
    private EditText mEtName, mEtIntro;
    private TextView mTvType, mTvCreate;
    private ImageView mIvChose;
    private AccountInfo mAccountInfo;
    private UserInfo mUserInfo;

    private int mThreadType;
    private String mName, mIntro, mTag;
    private String mIconImage;

    private LightDialog mChannelClassDialog;
    private GetImageHelper mGetImageHelper;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_start_create_channel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountInfo = AccountInfo.getInstance(this);
        mGetImageHelper = new GetImageHelper(this, this);
        mThreadType = getIntent().getIntExtra(ChoseChannelAttrActivity.EXT_CHANNEL_TYPE, 0);
        mAvatarView = (ProAvatarView) findViewById(R.id.av_avatar);
        mEtName = (EditText) findViewById(R.id.et_channel_name);
        mEtIntro = (EditText) findViewById(R.id.et_channel_intro);
        mTvType = (TextView) findViewById(R.id.tv_channel_type);
        mTvCreate = (TextView) findViewById(R.id.tv_create_channel);
        mIvChose = (ImageView) findViewById(R.id.iv_chose);
        mAvatarView.setCoverVisible(true);
        mAvatarView.setCoverColor(0XFFFFAE3A);
        mUserInfo = mAccountInfo.getUserInfo();
        mAvatarView.setImage(mUserInfo.getAvatar(), mUserInfo.defaultAvatar());
        mIconImage = mUserInfo.getAvatar();
        mAvatarView.setOnClickListener(this);
        mTvCreate.setOnClickListener(this);
        mIvChose.setOnClickListener(this);
        setTitle();

        mEtName.setHintTextColor(Color.GRAY);
        mEtName.setTextColor(0XFFE1E1E1);
        mEtIntro.setHintTextColor(Color.GRAY);
        mEtIntro.setTextColor(0XFFE1E1E1);
    }

    private void setTitle() {
        switch (mThreadType) {
            case CfPost.THREAD_TYPE_PIC_VOICE:
                getTitleBar().setTitle("语言");
                break;
            case CfPost.THREAD_TYPE_PIC_TXT:
                getTitleBar().setTitle("图文");
                break;
            case CfPost.THREAD_TYPE_TXT:
                getTitleBar().setTitle("文字");
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.av_avatar:
                mGetImageHelper.getImage(true, 1);
                break;

            case R.id.iv_chose:
                showChannelClassDialog();
                break;
            case R.id.tv_create_channel:
                if (mTvCreate.isEnabled() && isReady()) {
                    submit();
                } else if (!mTvCreate.isEnabled()) {
                    toast("正在执行上一条任务!");
                }
                break;

            default:
                break;
        }
    }

    private boolean isReady() {
        mName = mEtName.getText().toString().trim();
        mIntro = mEtIntro.getText().toString().trim();
        if (mName.length() < 1) {
            toast("请填写频道名称");
            return false;
        } else if (mIntro.length() < 10) {
            toast("频道简介不能少于10个字!");
            return false;
        } 
        return true;
    }

    private void submit() {
        mName = mEtName.getText().toString().trim();
        mIntro = mEtIntro.getText().toString().trim();
        if (mName != null && !"".equals(mName) &&
                mIntro != null && !"".equals(mIntro) && mTag != null) {
            HashMap<String, String> params = new HashMap<String, String>();
            HashMap<String, String> files = new HashMap<String, String>();

            params.put("thread_type", String.valueOf(mThreadType));
            params.put("name", mName);
            params.put("intro", mIntro);
            params.put("tag", mTag);

            if (!TextUtils.isEmpty(mIconImage)) {
                files.put("icon", mIconImage);
            }
            new CreateChannelTask(params, files).executeLong();
            mTvCreate.setEnabled(false);
        } else {
            toast("请将信息填写完整");
        }
    }

    private void showChannelClassDialog() {
        if (mChannelClassDialog == null) {
            mChannelClassDialog = new LightDialog(this)
            .setTitleLd(R.string.please_choose).setItems(
                    R.array.get_channel_classify_choices, this);
        }
        mChannelClassDialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mChannelClassDialog) {
            switch (which) {
                case 0:
                    mTag = getString(R.string.channel_emotion_tree_hole);
                    mTvType.setText(mTag);
                    break;
                case 1:
                    mTag = getString(R.string.channel_interest_in_anime);
                    mTvType.setText(mTag);
                    break;
                case 2:
                    mTag = getString(R.string.channel_pet_whimsy);
                    mTvType.setText(mTag);
                    break;
                case 3:
                    mTag = getString(R.string.channel_food);
                    mTvType.setText(mTag);
                    break;
                case 4:
                    mTag = getString(R.string.channel_photography_world);
                    mTvType.setText(mTag);
                    break;
                case 5:
                    mTag = getString(R.string.channel_music_movie);
                    mTvType.setText(mTag);
                    break;

                default:
                    mTag = null;
                    break;
            }
        }
    }

    @Override
    public void onImageResult(boolean success, String imageFile, Bitmap bm) {
        if (success) {
            mIconImage = imageFile;
            mAvatarView.setImageBitmap(bm);
        }
    }

    @Override
    public void onImageResult(boolean success, ArrayList<String> images) {
        if (success) {
            mIconImage = images.get(0);
            Bitmap bm = BitmapFactory.decodeFile(images.get(0));
            mAvatarView.setImageBitmap(bm);
        }
    }

    private class CreateChannelTask extends MsMhpTask {

        public CreateChannelTask(HashMap<String, String> parameters,
                HashMap<String, String> files) {
            super(StartCreateChannelActivity.this, MsRequest.CREATE, parameters, files);
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (response.isSuccessful()) {
                ChannelInfo channelInfo = ChannelInfo.fromJson(response.getJsonObject());
                toast("创建成功");
                Intent intent = new Intent(StartCreateChannelActivity.this,
                        ForumChannelDetailActivity.class);
                intent.putExtra(ForumChannelDetailActivity.EXT_DATA, channelInfo);
                setResult(RESULT_DELETED);
                startActivity(intent);
                StartCreateChannelActivity.this.finish();
            } else {
                switch (response.code) {
                    case MsResponse.MS_FAIL_CHANNEL_ICON:
                        toast("创建失败,请选择您要创建的频道的图标");
                        break;
                    default:
                        break;
                }
                mTvCreate.setEnabled(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mGetImageHelper.handleResult(requestCode, data);
    }

}
