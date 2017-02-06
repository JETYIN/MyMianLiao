package com.tjut.mianliao.forum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.CardItemConf;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProAvatarView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.contact.UserRemarkManager;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.feedback.FeedbackActivity;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class EditForumActivity extends BaseActivity implements View.OnClickListener {

    public static final Pattern FORUM_NAME_PATTERN = Pattern.compile("^\\S{3,}$");

    private static final int ACT_GET_ICON = 1;
    private static final int ACT_GET_SCENE = 2;
    private Forum mForum;
    private String[] mPrivacyOptions;

    private ProAvatarView mAvAvatar;
    private ImageView mSvScene;
    private View mBtnAction;

    private GetImageHelper mGetImageHelper;
    private int mCurrentAction;
    private String mIconImage;
    private String mSceneImage;

    private LightDialog mPrivacyDialog;
    private LightDialog mRemindSaveDialog;
    private LightDialog mDisbandConfirmDialog;
    private LightDialog mQuitDialog;

    private int mNewPrivacy;
    private boolean mHasUpdate;
    private SaveForumTask mSaveTask;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_edit_forum;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mForum = getIntent().getParcelableExtra(Forum.INTENT_EXTRA_NAME);
        if (mForum == null) {
            mForum = new Forum();
        }

        TypedArray ta = getResources().obtainTypedArray(R.array.forum_privacy_choices);
        int length = ta == null ? 0 : ta.length();
        mPrivacyOptions = new String[length];
        for (int i = 0; i < length; i++) {
            mPrivacyOptions[i] = ta.getString(i);
        }
        if(ta != null) {
            ta.recycle();
        }
        mNewPrivacy = mForum.privacy;

        initLayout();

        mGetImageHelper = new GetImageHelper(this, getString(R.string.set_avatar),
                new GetImageHelper.ImageSpec(200, 1, 1), new GetImageHelper.ImageResultListener() {
            @Override
            public void onImageResult(boolean success, String imageFile, Bitmap bm) {
                if (success) {
                    String file = GetImageHelper.saveAsTodo(getApplicationContext(), imageFile);
                    if (mCurrentAction == ACT_GET_ICON) {
                        mIconImage = file;
                        mAvAvatar.setImageBitmap(bm);
                    } else {
                        mSceneImage = file;
                        mSvScene.setImageBitmap(bm);
                    }
                }
            }

            @Override
            public void onImageResult(boolean success, ArrayList<String> images) {
                
            }
        });

        if (mForum.isAdmin(this)) {
            new MemberCountTask().executeLong();
        }
    }

    private void initLayout() {
        mAvAvatar = (ProAvatarView) findViewById(R.id.av_forum_icon);
        mSvScene = (ImageView) findViewById(R.id.sv_scene);
        if (mForum.adminUid > 0) {
            findViewById(R.id.ll_admin).setVisibility(View.VISIBLE);
            String userName = UserRemarkManager.getInstance(this).getRemark(mForum.adminUid, mForum.adminName);
            ((CardItemConf) findViewById(R.id.cif_admin)).setContent(userName);
        }
        if (isViewOnly()) {
            getTitleBar().showTitleText(mForum.name, null);
            findViewById(R.id.ll_members).setVisibility(View.VISIBLE);
            EditText etIntro = (EditText) findViewById(R.id.et_intro);
            etIntro.setText(mForum.intro);
            etIntro.setEnabled(false);
            CardItemConf cifPrivacy = (CardItemConf) findViewById(R.id.cif_privacy);
            cifPrivacy.setContent(mPrivacyOptions[mForum.privacy]);
            cifPrivacy.setEnabled(false);
            ((CardItemConf) findViewById(R.id.cif_members))
                    .setContent(String.valueOf(mForum.memberCount));
            mBtnAction = findViewById(R.id.btn_quit);
            mBtnAction.setVisibility(View.VISIBLE);
            showImages();
            mAvAvatar.setEnabled(false);
            mSvScene.setEnabled(false);
            findViewById(R.id.tv_edit).setVisibility(View.GONE);
            return;
        }

        getTitleBar().showRightText(R.string.finish, this);
        mAvAvatar.setCoverVisible(true);
        TextView info = (TextView) findViewById(R.id.tv_edit_forum_info);
        info.setVisibility(View.VISIBLE);
        if (isEdit()) {
            getTitleBar().showTitleText(R.string.ef_title, null);
            mBtnAction = findViewById(R.id.btn_disband);
            mBtnAction.setVisibility(View.VISIBLE);
            findViewById(R.id.ll_members).setVisibility(View.VISIBLE);
            TextView tvTitle = (TextView) findViewById(R.id.tv_forum_title);
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(mForum.name);
            ((EditText) findViewById(R.id.et_intro)).setText(mForum.intro);
            ((CardItemConf) findViewById(R.id.cif_privacy))
                    .setContent(mPrivacyOptions[mForum.privacy]);
            ((CardItemConf) findViewById(R.id.cif_members))
                    .setContent(String.valueOf(mForum.memberCount));
            showImages();
            info.setText(Html.fromHtml(getString(R.string.ef_mianliao_wall_hint)));
        } else {
            findViewById(R.id.ll_title).setVisibility(View.VISIBLE);
            info.setText(R.string.ef_create_forum_hint);
            info.setClickable(false);
            getTitleBar().showTitleText(R.string.ef_create_forum, null);
        }
    }

    private void showMemberRequests() {
        if (mForum.memberRequests > 0 && mForum.isAdmin(this)) {
            String info = getString(R.string.ef_new_member_requests,
                    mForum.memberCount, mForum.memberRequests);
            ((CardItemConf) findViewById(R.id.cif_members)).setContent(info);
        }
    }

    private void showImages() {
        ((ProImageView) findViewById(R.id.av_forum_icon))
                .setImage(mForum.icon, R.drawable.ic_avatar_forum);
        ((ProImageView) findViewById(R.id.sv_scene))
                .setImage(mForum.bgImg, R.drawable.pic_forum_scene);
    }

    private boolean isEdit() {
        return mForum.id > 0;
    }

    private boolean isViewOnly() {
        return mForum.id > 0 && !mForum.isAdmin(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.av_forum_icon:
                mCurrentAction = ACT_GET_ICON;
                mGetImageHelper.getImage();
                break;

            case R.id.sv_scene:
                mCurrentAction = ACT_GET_SCENE;
                mGetImageHelper.getImage(R.string.set_scene_image, GetImageHelper.SCENE_SPEC);
                break;

            case R.id.cif_privacy:
                showPrivacyDialog();
                break;

            case R.id.cif_members:
                Intent i = new Intent(this, ForumMemberActivity.class);
                i.putExtra(Forum.INTENT_EXTRA_NAME, mForum);
                startActivityForResult(i, getIdentity());
                break;

            case R.id.btn_disband:
                showDisbandConfirmDialog();
                break;

            case R.id.btn_quit:
                showQuitDialog();
                break;

            case R.id.tv_right:
                if (getCurrentFocus() != null) {
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                if (!hasUpdate()) {
                    finish();
                } else if (collectAndVerifyInfo()) {
                    saveForum();
                }
                break;

            case R.id.cif_admin:
                UserInfo userInfo = new UserInfo();
                userInfo.name = mForum.adminName;
                userInfo.userId = mForum.adminUid;
                Intent iUser = new Intent(this, NewProfileActivity.class);
                iUser.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
                startActivity(iUser);
                break;

            case R.id.tv_edit_forum_info:
                startActivity(new Intent(this, FeedbackActivity.class));
                break;

            case R.id.ll_qr_card:
                Intent intent = new Intent(this, ForumQrCardActivity.class);
                intent.putExtra(Forum.INTENT_EXTRA_NAME, mForum);
                startActivity(intent);
                break;

            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (!isViewOnly() && hasUpdate()) {
            showRemindSaveDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mGetImageHelper.handleResult(requestCode, data);
        } else if (requestCode == getIdentity() && resultCode == RESULT_UPDATED && data != null) {
            Forum forum = data.getParcelableExtra(Forum.INTENT_EXTRA_NAME);
            if (forum != null) {
                mForum = forum;
                ((CardItemConf) findViewById(R.id.cif_members))
                        .setContent(String.valueOf(mForum.memberCount));
                setResult(RESULT_UPDATED, new Intent().putExtra(Forum.INTENT_EXTRA_NAME, mForum));
                showMemberRequests();
            }
        }
    }

    private boolean hasUpdate() {
        if (!mHasUpdate) {
            mHasUpdate = !TextUtils.isEmpty(mIconImage) || !TextUtils.isEmpty(mSceneImage);
        }
        if (!mHasUpdate) {
            String name = ((EditText) findViewById(R.id.et_title)).getText().toString();
            String intro = ((EditText) findViewById(R.id.et_intro)).getText().toString();
            if (isEdit()) {
                mHasUpdate = !intro.equals(mForum.intro == null ? "" : mForum.intro) ||
                        mNewPrivacy != mForum.privacy;
            } else {
                mHasUpdate = !TextUtils.isEmpty(name) || !TextUtils.isEmpty(intro) ||
                        mNewPrivacy > 0;
            }
        }

        return mHasUpdate;
    }

    /**
     * @return true if has update
     */
    private boolean collectAndVerifyInfo() {
        mForum.intro = ((EditText) findViewById(R.id.et_intro)).getText().toString();
        mForum.privacy = mNewPrivacy;
        if (!isEdit()) {
            mForum.name = ((EditText) findViewById(R.id.et_title)).getText().toString();
            if (!FORUM_NAME_PATTERN.matcher(mForum.name).matches()) {
                toast(R.string.ef_name_invalid);
                return false;
            }
        }
        if (TextUtils.isEmpty(mForum.intro)) {
            toast(R.string.ef_intro_empty);
            return false;
        }
        return true;
    }

    private void saveForum() {
        if (mSaveTask != null) {
            toast(R.string.handling_last_task);
            return;
        }

        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> files = new HashMap<String, String>();
        if (isEdit()) {
            params.put("forum_id", String.valueOf(mForum.id));
        } else {
            params.put("type", String.valueOf(mForum.type));
        }

        params.put("name", mForum.name);
        params.put("intro", mForum.intro);
        params.put("privacy", String.valueOf(mForum.privacy));

        if (!TextUtils.isEmpty(mIconImage)) {
            files.put("logo", mIconImage);
        }
        if (!TextUtils.isEmpty(mSceneImage)) {
            files.put("bg_img", mSceneImage);
        }

        new SaveForumTask(params, files).executeLong();
    }

    private void showPrivacyDialog() {
        if (mPrivacyDialog == null) {
            mPrivacyDialog = new LightDialog(this);
            mPrivacyDialog.setTitle(R.string.ef_privacy);
            mPrivacyDialog.setItems(R.array.forum_privacy_choices, new DialogInterface
                    .OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mNewPrivacy = which;
                    ((CardItemConf) findViewById(R.id.cif_privacy))
                            .setContent(mPrivacyOptions[which]);
                }
            });
        }
        mPrivacyDialog.show();
    }

    private void showRemindSaveDialog() {
        if (mRemindSaveDialog == null) {
            mRemindSaveDialog = new LightDialog(this);
            mRemindSaveDialog.setTitle(R.string.prof_change_not_saved);
            mRemindSaveDialog.setMessage(R.string.ef_change_unsaved);
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            if (collectAndVerifyInfo()) {
                                saveForum();
                            }
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            finish();
                            break;
                        default:
                            break;
                    }
                }
            };

            mRemindSaveDialog.setNegativeButton(R.string.prof_discard, listener)
                    .setPositiveButton(R.string.prof_save, listener);
        }
        mRemindSaveDialog.show();
    }

    private void showDisbandConfirmDialog() {
        if (mDisbandConfirmDialog == null) {
            mDisbandConfirmDialog = new LightDialog(this);
            mDisbandConfirmDialog.setTitle(R.string.confirm);
            mDisbandConfirmDialog.setMessage(R.string.ef_disband_confirm);
            mDisbandConfirmDialog.setPositiveButton(android.R.string.ok, new DialogInterface
                    .OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new DisbandTask().executeLong();
                }
            });
            mDisbandConfirmDialog.setNegativeButton(android.R.string.cancel, null);
        }
        mDisbandConfirmDialog.show();
    }

    private void showQuitDialog() {
        if (mQuitDialog == null) {
            mQuitDialog = new LightDialog(this);
            mQuitDialog.setTitle(R.string.ef_act_view);
            mQuitDialog.setMessage(R.string.ef_act_quit_confirm);
            mQuitDialog.setNegativeButton(android.R.string.cancel, null);
            mQuitDialog.setPositiveButton(android.R.string.ok, new DialogInterface
                    .OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new QuitForumTask().executeLong();
                }
            });
        }
        mQuitDialog.show();
    }

    private class SaveForumTask extends MsMhpTask {

        public SaveForumTask(HashMap<String, String> parameters, HashMap<String, String> files) {
            super(getApplicationContext(), isEdit() ? MsRequest.EDIT_FORUM : MsRequest.CREATE_FORUM,
                    parameters, files);
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
                toast(isEdit() ? R.string.ef_edit_forum_success : R.string.ef_create_forum_success);
                Forum forum = Forum.fromJson(response.json
                        .optJSONObject(MsResponse.PARAM_RESPONSE));
                Intent iResult = new Intent();
                iResult.putExtra(Forum.INTENT_EXTRA_NAME, forum);
                setResult(RESULT_OK, iResult);
                finish();
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        isEdit() ? R.string.ef_edit_forum_failed : R.string.ef_create_forum_failed,
                        response.code));
            }
        }
    }

    private class MemberCountTask extends MsTask {

        public MemberCountTask() {
            super(getApplicationContext(), MsRequest.LIST_MEMBER_REQUEST);
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected String buildParams() {
            return "forum_id=" + mForum.id + "&count=1";
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (MsResponse.isSuccessful(response)) {
                mForum.memberRequests = response.json.optInt(MsResponse.PARAM_RESPONSE);
                showMemberRequests();
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.forum_member_tst_requests_failed, response.code));
            }
        }
    }

    private class DisbandTask extends MsTask {

        public DisbandTask() {
            super(getApplicationContext(), MsRequest.DISBAND_FORUM);
        }

        @Override
        protected String buildParams() {
            return "forum_id=" + mForum.id;
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
            mBtnAction.setEnabled(false);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().showProgress();
            mBtnAction.setEnabled(true);

            if (MsResponse.isSuccessful(response)) {
                toast(R.string.ef_disband_forum_success);
                Intent i = new Intent();
                i.putExtra(Forum.INTENT_EXTRA_NAME, mForum);
                setResult(RESULT_DELETED, i);
                finish();
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.ef_disband_forum_failed, response.code));
            }
        }
    }

    private class QuitForumTask extends MsTask {
        public QuitForumTask() {
            super(getApplicationContext(), MsRequest.CF_QUIT_FORUM);
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
            mBtnAction.setEnabled(false);
        }

        @Override
        protected String buildParams() {
            return "forum_id=" + mForum.id;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mBtnAction.setEnabled(true);
            if (MsResponse.isSuccessful(response)) {
                toast(R.string.ef_act_quit_success);
                mForum.isMember = false;
                mForum.memberCount--;
                Intent i = new Intent();
                i.putExtra(Forum.INTENT_EXTRA_NAME, mForum);
                setResult(BaseActivity.RESULT_OK, i);
                finish();
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.ef_act_quit_failed, response.code));
            }
        }
    }
}
