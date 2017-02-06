package com.tjut.mianliao.profile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.R;
import com.tjut.mianliao.chat.ChatActivity;
import com.tjut.mianliao.chat.GroupReportActivity;
import com.tjut.mianliao.component.CheckTribeBoxView;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.PopupView;
import com.tjut.mianliao.component.PopupView.OnItemClickListener;
import com.tjut.mianliao.component.PopupView.PopupItem;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.contact.SubscriptionHelper;
import com.tjut.mianliao.contact.SubscriptionHelper.SubRequestListener;
import com.tjut.mianliao.contact.UserEntryManager;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.BannedInfo;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.data.Medal;
import com.tjut.mianliao.data.Photo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.explore.GoldDepositsActivity;
import com.tjut.mianliao.forum.nova.ForumMyPostActivity;
import com.tjut.mianliao.forum.nova.UserPostActivity;
import com.tjut.mianliao.image.ImageActivity;
import com.tjut.mianliao.mycollege.UpdateProfileImageActiivity;
import com.tjut.mianliao.task.TaskActivity;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.FaceManager;
import com.tjut.mianliao.util.FileDownloader;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.GetImageHelper.ImageResultListener;
import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;
import com.tjut.mianliao.xmpp.ConnectionManager;
import com.umeng.analytics.MobclickAgent;

public class ProfileActivity extends BaseActivity implements OnClickListener, SubRequestListener, ImageResultListener,
        OnItemClickListener {

    public static final String EXTRA_USER_GUID = "extra_user_guid";
    public static final String EXTRA_USER_FACE_ID = "extra_user_face_id";
    public static final String EXTRA_SHOW_CHAT_BUTTON = "extra_show_chat_button";
    public static final String EXTRA_NICK_NAME = "extra_nick_name";
    public static final String EXTRA_USER_DESC = "extra_user_desc";

    private static final int REQUEST_UPDATE = 100;
    private static final int REQUEST_SET_BADGE = 200;
    private static final int REQUEST_SET_REMARK = 300;
    private static final int REQUEST_UPDATE_BG = 400;
    private static final int REQUEST_UPDATE_AVATAR = 500;
    protected static final int SHOW_IMAGE_REQUEST = 101;
    protected static final int REQUEST_UPDATE_NICK = 102;
    protected static final int REQUEST_UPDATE_DESC = 103;
    private static final int REQUEST_GOLD_DEPOS_CODE = 105;
    private static final int REQUEST_MY_MEDALS_CODE = 600;

    private LightDialog mMenuDialog;

    private ConnectionManager mConnectionManager;
    private UserEntryManager mUserEntryManager;
    private UserInfoManager mUserInfoManager;
    private SubscriptionHelper mSubscriptionHelper;
    private GetImageHelper mGetImageHelper;

    private LightDialog mRemoveContactDialog;
    private LightDialog mBlockContactDialog;
    private LightDialog mBannedDialog;

    private UserInfo mUserInfo;
    private UserInfo mMyInfo;
    private MsTask mLastTask;

    private View mBtnAddContact;

    private PhotoManager mPhotoManager;

    private PrimaryMedalAdapter mMedalAdapter;
    private String mAvatarUrl;
    private String mDelPhotoIds;
    private boolean mHasUpdate;
    private TextView mTvPointNum;
    private TextView mTvGoldNum;
    private TextView mTvPoint;
    private TextView mTvGold;

    private PopupView mPopMenu;

    private boolean isMe, mScanUser;
    private ProImageView ivAvatar;
    private TextView mTvTotalWealth;
    private ImageView mIvEnter;
    
    private int mScanTotle;
    private int mUpCount;
    private TextView mTvFlowerCount;
    private TextView mTvUpCount;
    private boolean mHasUpdateImage;
    private boolean mIsModerator;
    
    private ArrayList<String> mEditImages;
    private CheckTribeBoxView mCheckTribeBoxView;
    private ArrayList<BannedInfo> mShowBannedInfos;
    private LayoutInflater mInflate;
    
    @Override
    protected int getLayoutResID() {
        return 0;
    }

    @Override
    protected int getBaseLayoutResID() {
        return R.layout.activity_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEditImages = new ArrayList<>();
        mConnectionManager = ConnectionManager.getInstance(this);
        mUserEntryManager = UserEntryManager.getInstance(this);
        mUserInfoManager = UserInfoManager.getInstance(this);
        mSubscriptionHelper = SubscriptionHelper.getInstance(this);
        mSubscriptionHelper.registerRequestListener(this);
        mGetImageHelper = new GetImageHelper(this, this);
        mTvTotalWealth = (TextView) findViewById(R.id.tv_total_wealth);
        mIvEnter = (ImageView) findViewById(R.id.iv_enter);
        mInflate = LayoutInflater.from(this);
        
        mUserInfo = getUserInfo();
        AccountInfo account = AccountInfo.getInstance(this);
        mMyInfo = account.getUserInfo();
        isMe = mUserInfo.isMine(this)
                || (mUserInfo.userId == 0 && mUserInfo.faceId != null && mUserInfo.faceId
                        .equals(account.getUserInfo().faceId));
        if (isMe) {
            mIvEnter.setVisibility(View.VISIBLE);
            findViewById(R.id.ll_identity_card).setVisibility(View.VISIBLE);
            findViewById(R.id.iv_menu).setVisibility(View.GONE);
            mTvTotalWealth.setText(this.getString(R.string.prof_my_wealth));
        } else {
            mIvEnter.setVisibility(View.GONE);
            if ((!mScanUser && Utils.isMianLiaoService(mUserInfo))) {
                findViewById(R.id.iv_menu).setVisibility(View.GONE);
            } else {
                findViewById(R.id.iv_menu).setVisibility(View.VISIBLE);
            }
            if (getIntent().getBooleanExtra(EXTRA_SHOW_CHAT_BUTTON, true)) {
                findViewById(R.id.btn_chat).setVisibility(View.VISIBLE);
            }
            mTvTotalWealth.setText(this.getString(R.string.prof_he_wealth));
            hideRightIcon();
            showAddContactButton();
        }

        if (!mScanUser && Utils.isMianLiaoService(mUserInfo) && !isMe) {
            findViewById(R.id.btn_chat).setVisibility(View.VISIBLE);
        }

        mBtnAddContact = findViewById(R.id.btn_add_contact);

        if (Utils.isMianLiaoService(mUserInfo) && !mScanUser) {
            mBtnAddContact.setVisibility(View.GONE);
        }
        ivAvatar = (ProImageView) findViewById(R.id.av_avatar);
        ivAvatar.setOnClickListener(this);
        mPhotoManager = new PhotoManager(this, mUserInfo, ivAvatar, isMe ? true : false);
        GridView gvGallery = (GridView) findViewById(R.id.gv_gallery);

        gvGallery.setAdapter(mPhotoManager.getAdapter());
        gvGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Photo photo = (Photo) parent.getItemAtPosition(position);
                if (photo == null) {
                    mPhotoManager.setTodo(null, PhotoManager.TYPE_PHOTO);
                    mGetImageHelper.getImage(true, 10 - mPhotoManager.getPhotoSize());
                } else {
                    if (isMe) {
                        Intent intent = new Intent(ProfileActivity.this, ImageActivity.class)
                                .putExtra(ImageActivity.EXTRA_IMAGE_INDEX, position)
                                .putExtra(ImageActivity.EXTRA_IS_SHOW_OPER, true)
                                .putStringArrayListExtra(ImageActivity.EXTRA_IMAGE_URLS, getImageUrls());
                        startActivityForResult(intent, SHOW_IMAGE_REQUEST);
                    } else {
                        Utils.viewImages(ProfileActivity.this, getImageUrls(), position);
                    }
                }
            }
        });

        mMedalAdapter = new PrimaryMedalAdapter(this);
        ((GridView) findViewById(R.id.gv_medal)).setAdapter(mMedalAdapter);
        showMedals();
        showBasicUserInfo();
        if (mUserInfo.hasUserId() || mUserInfo.hasFaceId() || mUserInfo.hasGuid()) {
            fetchUserInfo();
        }
        if (!isMe) {
            visit();
        }
        new GetVisitorInfoTask().executeLong();
        new GetPraiseInfoTask().executeLong();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mHasUpdateImage) {
            mHasUpdateImage = false;
            mGetImageHelper = new GetImageHelper(this, this);
        }
    }
    
    private void hideRightIcon() {
        findViewById(R.id.iv_right_point).setVisibility(View.GONE);
        findViewById(R.id.iv_right_gold).setVisibility(View.GONE);
        findViewById(R.id.iv_right_individualy).setVisibility(View.GONE);
    }

    private void visit() {
        new VisitTask().executeLong();
    }
    
    @Override
    protected void onDestroy() {
        if (mLastTask != null) {
            mLastTask.cancel(false);
        }
        if (mPhotoManager.hasUpdate() || mHasUpdate) {
            updatePhoto();
        } else {
            deleteEditImages();
        }
        updateAvatar();
        mSubscriptionHelper.unregisterRequestListener(this);
        super.onDestroy();
    }

    private void updateAvatar() {
        if (mAvatarUrl != null && !"".equals(mAvatarUrl)) {
            new UpdateAvatarTask(getParameters(), getFiles()).executeLong();
        }
    }

    private ArrayList<String> getImageUrls() {
        ArrayList<String> urls = new ArrayList<String>();
        if (mUserInfo.getPhotos() != null) {
            for (Image image : mUserInfo.getPhotos()) {
                urls.add(image.image);
            }
        }
        if (mPhotoManager.getNewPhotos() != null) {
            for (Photo photo : mPhotoManager.getNewPhotos()) {
                urls.add(photo.file);
            }
        }
        return urls;
    }

    private UserInfo getUserInfo() {
        UserInfo info = getIntent().getParcelableExtra(UserInfo.INTENT_EXTRA_INFO);
        if (info != null && info.hasUserId()) {
            UserInfo local = mUserInfoManager.getUserInfo(info.userId);
            return local == null ? info : local;
        }

        info = new UserInfo();
        info.guid = getIntent().getStringExtra(EXTRA_USER_GUID);
        info.faceId = getIntent().getStringExtra(EXTRA_USER_FACE_ID);
        if (info.hasFaceId() || info.hasGuid()) {
            info.name = getString(R.string.loading);
            mScanUser = true;
            findViewById(R.id.iv_menu).setVisibility(View.GONE);
        } else {
            findViewById(R.id.iv_menu).setVisibility(View.GONE);
            info.name = getString(R.string.prof_user_not_exist);
        }

        findViewById(R.id.btn_chat).setEnabled(false);
        // getTitleBar().setRightButtonEnabled(false);

        return info;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mGetImageHelper.handleResult(requestCode, data);
        if (resultCode == RESULT_UPDATED && requestCode == REQUEST_SET_BADGE) {
            ArrayList<Medal> primaryMedals = data.getParcelableArrayListExtra(MedalActivity.EXTRA_PRIMARY_MEDALS);
            mUserInfo.setPrimaryMedals(primaryMedals);
            mUserInfoManager.saveUserInfo(mUserInfo);
            setResult(RESULT_UPDATED);
            showMedals();
        } else if (resultCode == RESULT_UPDATED && requestCode == REQUEST_UPDATE) {
            mUserInfo = data.getParcelableExtra(UserInfo.INTENT_EXTRA_INFO);
            showBasicUserInfo();
            mPhotoManager.setUserInfo(mUserInfo);
            setResult(RESULT_UPDATED);
        } else if (resultCode == RESULT_UPDATED && requestCode == REQUEST_SET_REMARK) {
            showBasicUserInfo();
        } else if (resultCode == RESULT_UPDATED && requestCode == REQUEST_UPDATE_BG) {
            String imagePath = data.getStringExtra(UpdateProfileImageActiivity.EXT_IMAGE_PATH);
            if (imagePath != null && imagePath.length() > 0) {
                ((ProImageView) findViewById(R.id.iv_scene)).setImageBitmap(Utils.fileToBitmap(imagePath));
            }
        } else if (resultCode == RESULT_UPDATED && requestCode == REQUEST_UPDATE_AVATAR) {
            String imagePath = data.getStringExtra(UpdateProfileImageActiivity.EXT_IMAGE_PATH);
            if (imagePath != null && imagePath.length() > 0) {
                ProImageView avatar = (ProImageView) findViewById(R.id.av_avatar);
                avatar.setImageBitmap(Utils.fileToBitmap(imagePath));
            }
        } else if (resultCode == RESULT_OK && requestCode == SHOW_IMAGE_REQUEST) {
            ArrayList<String> delUrls = data.getStringArrayListExtra(ImageActivity.EXTRA_DELETE_IMAGE_URL);
            ArrayList<String> delLocUrls = data.getStringArrayListExtra(ImageActivity.EXTRA_DELETE_IMAGE_LOC_URL);
            mAvatarUrl = data.getStringExtra(ImageActivity.EXTRA_AVATAR_RUL);
            if (delUrls != null && delUrls.size() > 0) {
                mDelPhotoIds = mUserInfo.deletePhotos(delUrls);
            }
            if (mAvatarUrl != null && !"".equals(mAvatarUrl)) {
                mUserInfo.setAvatar(mAvatarUrl);
                ivAvatar.setImage(mUserInfo.getAvatar(), mUserInfo.defaultAvatar());
            }
            if (delLocUrls != null && delLocUrls.size() > 0) {
                mPhotoManager.removeAddedPhoto(delLocUrls);
            }
            if ((delUrls != null && delUrls.size() > 0) || (mAvatarUrl != null && !"".equals(mAvatarUrl))) {
                mHasUpdate = true;
            } else {
                mHasUpdate = false;
            }
            mUserInfoManager.updateUserInfo(mUserInfo);
            mPhotoManager.updatePhotos();
        } else if (resultCode == RESULT_UPDATED && requestCode == REQUEST_UPDATE_NICK) {
            String nick = data.getStringExtra(EXTRA_NICK_NAME);
            if (nick != null && nick.length() > 0) {
                updateNickView(nick);
            }
        } else if (resultCode == RESULT_UPDATED && requestCode == REQUEST_UPDATE_DESC) {
            String desc = data.getStringExtra(EXTRA_USER_DESC);
            updateShortDesc(desc);
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_GOLD_DEPOS_CODE) {
            mTvGoldNum.setText(String.valueOf(mUserInfo.gold));
        } else if (resultCode == RESULT_UPDATED && requestCode == REQUEST_MY_MEDALS_CODE) {
            ArrayList<Medal> primaryMedals = data.getParcelableArrayListExtra(MedalActivity.EXTRA_PRIMARY_MEDALS);
            showMedals(primaryMedals);
            mUserInfo.setPrimaryMedals(primaryMedals);
            mUserInfoManager.saveUserInfo(mUserInfo);
        }
    }

    private void updateShortDesc(String desc) {
        if (desc != null || desc.length() > 0) {
            mUserInfo.shortDesc = desc;
        } else {
            mUserInfo.shortDesc = "";
        }
        mUserInfoManager.updateUserInfo(mUserInfo);
        ((TextView) findViewById(R.id.tv_indev_sign)).setText(desc == null ? getString(R.string.prof_no_short_desc)
                : desc);
    }

    private void updateNickView(String nick) {
        mUserInfo.nickname = nick;
        mUserInfoManager.updateUserInfo(mUserInfo);
        setText(R.id.tv_name, nick);
    }

    private void fetchUserInfo() {
        if (!Utils.isNetworkAvailable(this)) {
            toast(R.string.network_error);
            findViewById(R.id.iv_menu).setVisibility(View.GONE);
            return;
        }

        new FetchUserTask().executeLong();
    }

    private void showBasicUserInfo() {
        // getTitleBar().showTitleText(mUserInfo.getNickname(), null);
        ((TextView) findViewById(R.id.tv_edu_info)).setText(mUserInfo.getEduInfo(this));
        if (mUserInfo.isSpecial() && mUserInfo.shortDesc != null && mUserInfo.shortDesc.split(",").length > 1) {
            ((TextView) findViewById(R.id.tv_indev_sign)).setText(mUserInfo.shortDesc.split(",")[1]);
        } else {
            ((TextView) findViewById(R.id.tv_indev_sign)).setText((mUserInfo.shortDesc == null || ""
                    .equals(mUserInfo.shortDesc)) ? getString(R.string.prof_no_short_desc) : mUserInfo.shortDesc);
        }

        findViewById(R.id.iv_vip_bg).setVisibility(mUserInfo.vip ? View.VISIBLE : View.INVISIBLE);

        TextView tvName = (TextView) findViewById(R.id.tv_name);
        tvName.setText(mUserInfo.getNickname());
        ImageView ivVip = (ImageView) findViewById(R.id.iv_vip);
        ivVip.setVisibility(mUserInfo.vip ? View.VISIBLE : View.GONE);

        ((ImageView) findViewById(R.id.iv_type_icon)).setImageResource(mUserInfo.getTypeIcon());
        if (!mUserInfo.isVerified() && mUserInfo.isMine(this)) {
            findViewById(R.id.rl_id_verify).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.rl_id_verify).setVisibility(View.GONE);
        }

        if (mUserInfo.isMine(this)) {
            ((TextView) findViewById(R.id.tv_account)).setText(mUserInfo.account);
        }

        ImageView ivGender = (ImageView) findViewById(R.id.iv_gender);
        ivGender.setImageResource(mUserInfo.getGenderIcon());

        String pointDes;
        String goldDes;
        mTvPoint = (TextView) findViewById(R.id.tv_point);
        mTvGold = (TextView) findViewById(R.id.tv_gold);
        if (mUserInfo.isMine(this)) {
            pointDes = getString(R.string.points_count_text, mUserInfo.credit);
            goldDes = getString(R.string.points_gold_text, mUserInfo.gold);
            mTvPoint.setText(R.string.points_count_mine_title);
            mTvGold.setText(R.string.points_gold_mine_title);
        } else {
            pointDes = getString(R.string.points_count_text, mUserInfo.credit);
            goldDes = getString(R.string.points_gold_text, mUserInfo.gold);
            mTvPoint.setText(R.string.points_count_other_title);
            mTvGold.setText(R.string.points_gold_other_title);
        }

        mTvPointNum = (TextView) findViewById(R.id.tv_point_count);
        mTvGoldNum = (TextView) findViewById(R.id.tv_gold_count);
        
        mTvPointNum.setText(pointDes);
        mTvGoldNum.setText(goldDes);

        TextView tvPostCount = (TextView) findViewById(R.id.tv_post_count);
        mTvUpCount = (TextView) findViewById(R.id.tv_won_up_count);
        LinearLayout llWonupCount = (LinearLayout) findViewById(R.id.ll_fans_count);
        mTvFlowerCount = (TextView) findViewById(R.id.tv_visitor_count);
        TextView tvVisitorpCount = (TextView) findViewById(R.id.tv_visitor_count);
        tvPostCount.setText(String.valueOf(mUserInfo.postCount));
        updateVisitCountInfo();
        mTvUpCount.setOnClickListener(this);
        tvPostCount.setOnClickListener(this);
        mTvFlowerCount.setOnClickListener(this);
        llWonupCount.setOnClickListener(this);
        tvVisitorpCount.setOnClickListener(this);
        showScene();
    }

    private void showScene() {
        if (!TextUtils.isEmpty(mUserInfo.bgImg)) {
            ((ProImageView) findViewById(R.id.iv_scene)).setImage(mUserInfo.bgImg, R.drawable.pic_prof_scene);
        }
    }

    private void showMedals() {
        if (mUserInfo != null && mUserInfo.getMedals() != null && mUserInfo.getMedals().size() != 0) {
            ArrayList<Medal> showMedals = new ArrayList<Medal>();
            for (int i = 0; i < mUserInfo.getMedals().size(); i++) {
                if (mUserInfo.getMedals().get(i).isPrimary()) {
                    showMedals.add(mUserInfo.getMedals().get(i));
                }
            }
            for (int i = 0; i < showMedals.size(); i++) {
                for (int j = 0; j < (showMedals.size() - (i + 1)); j++) {
                    Medal tempLeft = new Medal();
                    Medal tempRight = new Medal();
                    if (showMedals.get(j).primary < (showMedals.get(j + 1).primary)) {
                        tempLeft = showMedals.get(j);
                        tempRight = showMedals.get(j + 1);
                        showMedals.remove(j);
                        showMedals.add(j, tempRight);
                        showMedals.remove(j + 1);
                        showMedals.add(j + 1, tempLeft);
                    }
                }
            }

            mMedalAdapter.reset(showMedals);
        }
    }

    private void showMedals(ArrayList<Medal> Medals) {
        ArrayList<Medal> showMedals = new ArrayList<Medal>();
        for (int i = 0; i < Medals.size(); i++) {
            if (Medals.get(i).isPrimary()) {
                showMedals.add(Medals.get(i));
            }
        }
        mMedalAdapter.reset(showMedals);
    }

    private void showMenu() {
        if (mMenuDialog == null) {
            mMenuDialog = new LightDialog(this);
            mMenuDialog.setTitle(mUserInfo.getNickname());
        }

        String[] items = new String[2];
        items[0] = getString(mUserEntryManager.isFriend(mUserInfo.jid) ? R.string.prof_contact_remove
                : R.string.prof_contact_add);
        items[1] = getString(mUserEntryManager.isBlacklisted(mUserInfo.jid) ? R.string.prof_blacklist_remove
                : R.string.prof_blacklist_add);
        mMenuDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if (mUserEntryManager.isFriend(mUserInfo.jid)) {
                            showRemoveContactDialog();
                        } else {
                            subscribe();
                        }
                        break;

                    case 1:
                        if (mUserEntryManager.isBlacklisted(mUserInfo.jid)) {
                            blacklist(false);
                        } else {
                            showBlockContactDialog();
                        }
                        break;

                    default:
                        break;
                }
            }
        });

        mMenuDialog.show();
    }

    private void showRemoveContactDialog() {
        if (mRemoveContactDialog == null) {
            mRemoveContactDialog = new LightDialog(this);
            mRemoveContactDialog.setTitle(R.string.prof_contact_remove);
            mRemoveContactDialog.setMessage(R.string.prof_contact_remove_desc);
            mRemoveContactDialog.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    unsubscribe();
                }
            });
            mRemoveContactDialog.setNegativeButton(android.R.string.cancel, null);
        }
        mRemoveContactDialog.show();
    }

    private void showBlockContactDialog() {
        if (mBlockContactDialog == null) {
            mBlockContactDialog = new LightDialog(this);
            mBlockContactDialog.setTitle(R.string.prof_blacklist_add);
            mBlockContactDialog.setMessage(R.string.prof_blacklist_add_desc);
            mBlockContactDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new BlacklistTask(true).executeLong();
                }
            });
            mBlockContactDialog.setNegativeButton(android.R.string.cancel, null);
        }
        mBlockContactDialog.show();
    }
    
    private void showBannedDialog() {
        if (mBannedDialog == null) {
            mBannedDialog = new LightDialog(this);
            mBannedDialog.setTitle(R.string.please_choose);
            mCheckTribeBoxView = new CheckTribeBoxView(this);
            mBannedDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String checkedIds = mCheckTribeBoxView.getCheckedIds();
                    String unCheckedIds = mCheckTribeBoxView.getUnCheckedIds();
                    banned(checkedIds, unCheckedIds);
                }
            });
            mBannedDialog.setNegativeButton(android.R.string.cancel, null);
        }
        mBannedDialog.setView(mCheckTribeBoxView.getView(getCheckbleTribeInfo()));
        mBannedDialog.show();
    }

    private ArrayList<BannedInfo> getCheckbleTribeInfo() {
        if (mShowBannedInfos != null) {
            return mShowBannedInfos;
        }
        mShowBannedInfos = new ArrayList<>();
        ArrayList<BannedInfo> mManagedInfos = mMyInfo.managedInfos;
        ArrayList<BannedInfo> userBannedInfos = mUserInfo.bannedInfos;
        if (mManagedInfos != null && mManagedInfos.size() > 0) {
            mShowBannedInfos = mManagedInfos;
        }
        if (userBannedInfos != null && userBannedInfos.size() > 0) {
            for (BannedInfo bannedInfo : userBannedInfos) {
                for (BannedInfo info : mShowBannedInfos) {
                    if (info.tribeId == bannedInfo.tribeId) {
                        info.checked = true;
                    }
                }
            }
        }
        return mShowBannedInfos;
    }
    
    private void subscribe() {
        if (mConnectionManager.chatServerConnected()) {
            // getTitleBar().showProgress();
            mBtnAddContact.setEnabled(false);
            mSubscriptionHelper.addContact(mUserInfo.jid);
        } else {
            toast(R.string.disconnected);
        }
    }

    private void unsubscribe() {
        if (mConnectionManager.chatServerConnected()) {
            // getTitleBar().showProgress();
            mSubscriptionHelper.deleteContact(mUserInfo.jid);
        } else {
            toast(R.string.disconnected);
        }
    }

    private void blacklist(boolean addTo) {
        if (mConnectionManager.chatServerConnected()) {
            new BlacklistTask(addTo).executeLong();
        } else {
            toast(R.string.disconnected);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_medal_card:
            case R.id.ll_medal:
                if (isMe) {
                    Intent iMedal = new Intent(this, MyMedalsActivity.class);
                    iMedal.putExtra(MyMedalsActivity.MY_MADELS, mUserInfo.getMedals());
                    startActivityForResult(iMedal, REQUEST_MY_MEDALS_CODE);
                }
                break;

            case R.id.btn_chat:
                if (!TextUtils.isEmpty(mUserInfo.jid)) {
                    Intent i = new Intent(this, ChatActivity.class);
                    i.putExtra(ChatActivity.EXTRA_CHAT_TARGET, mUserInfo.jid);
                    startActivity(i);
                }
                break;

            case R.id.rl_qr_card:
                Intent iQrCode = new Intent(this, UserQrCardActivity.class);
                iQrCode.putExtra(UserInfo.INTENT_EXTRA_INFO, mUserInfo);
                startActivity(iQrCode);
                MobclickAgent.onEvent(this, MStaticInterface.CODE);
                break;

            case R.id.av_avatar:
                changeImage(UpdateProfileImageActiivity.TYPE_AVATAR);
                MobclickAgent.onEvent(this, MStaticInterface.FACE1);
                mHasUpdateImage = true;
                break;

            case R.id.rl_id_verify:
                Intent iAuth = new Intent(this, IdVerifyActivity.class);
                startActivity(iAuth);
                MobclickAgent.onEvent(this, MStaticInterface.AUTHENTICATION);
                break;

            case R.id.btn_add_contact:
                if (!TextUtils.isEmpty(mUserInfo.jid)) {
                    subscribe();
                }
                break;

            case R.id.cif_remark:
                Intent i = new Intent(this, RemarkActivity.class);
                i.putExtra(UserInfo.INTENT_EXTRA_INFO, mUserInfo);
                startActivityForResult(i, REQUEST_SET_REMARK);
                break;

            case R.id.rl_point_me:
                if (mUserInfo.isMine(this)) {
                    Intent intent = new Intent(this, TaskActivity.class);
                    startActivity(intent);
                    MobclickAgent.onEvent(this, MStaticInterface.WHEAT);
                }
                break;

            case R.id.rl_gold_me:
                if (mUserInfo.isMine(this)) {
                    Intent intent = new Intent(this, GoldDepositsActivity.class);
                    startActivityForResult(intent, REQUEST_GOLD_DEPOS_CODE);
                    MobclickAgent.onEvent(this, MStaticInterface.GOLD);
                }
                break;

            case R.id.ll_follow_count_info:
                if (mUserInfo.isMine(this)) {
                    startActivity(ForumMyPostActivity.class);
                } else {
                    Intent intent = new Intent(this, UserPostActivity.class);
                    intent.putExtra(UserPostActivity.EXT_USER_INFO, mUserInfo);
                    startActivity(intent);
                }
                break;

            case R.id.rl_indiv_sign:
                if (mUserInfo.isMine(this)) {
                    Intent shortDesIntent = new Intent(this, UpdateShortDescActivity.class);
                    shortDesIntent.putExtra(UpdateShortDescActivity.EXT_USER_DES, mUserInfo.shortDesc);
                    startActivityForResult(shortDesIntent, REQUEST_UPDATE_DESC);
                    MobclickAgent.onEvent(this, MStaticInterface.SIGNATURE);
                }
                break;

            case R.id.iv_scene:
                changeImage(UpdateProfileImageActiivity.TYPE_BG);
                MobclickAgent.onEvent(this, MStaticInterface.BACKGROUND);
                break;

            case R.id.tv_name:
                if (mUserInfo.isMine(this)) {
                    Intent intent = new Intent(this, UpdateNickActivity.class);
                    intent.putExtra(UpdateNickActivity.EXT_USER_NICK, mUserInfo.getNickname());
                    startActivityForResult(intent, REQUEST_UPDATE_NICK);
                    MobclickAgent.onEvent(this, MStaticInterface.USER);
                }
                break;
            case R.id.iv_back:
                findViewById(R.id.iv_back).setEnabled(false);
                onBackPressed();
                break;
            case R.id.iv_menu: 
                showPopupMenu(v);
                break;
            case R.id.tv_post_count:
                if (!isMe) {
                    startActivity(UserPostActivity.class);
                }
                break;
            case R.id.tv_won_up_count:
            case R.id.ll_fans_count:
                Intent intent = new Intent(this, PraiseUserActivity.class);
                intent.putExtra(UserInfo.INTENT_EXTRA_INFO, mUserInfo);
                startActivity(intent);
                break;
            case R.id.tv_visitor_count:
                Intent in = new Intent(this, LatestVisitorActivity.class);
                in.putExtra(UserInfo.INTENT_EXTRA_INFO, mUserInfo);
                startActivity(in);
                break;
            default:
                break;
        }
    }

    private void startActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(ForumMyPostActivity.EXT_SHOW_SUBMENU, true);
        startActivity(intent);
    }

    private void updatePhoto() {
        HashMap<String, String> params = new HashMap<>();
        HashMap<String, String> files = new HashMap<>();
        if (mPhotoManager.hasUpdate() || mHasUpdate) {
            int size = mPhotoManager.getNewPhotos().size();
            String avatarFile = null;
            if (size > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < size; i++) {
                    String file = "new_photo_" + i;
                    if (sb.length() > 0) {
                        sb.append(',');
                    }
                    sb.append(file);
                    Photo photo = mPhotoManager.getNewPhotos().get(i);
                    files.put(file, photo.file);
                    if (photo == mPhotoManager.getAvatar()) {
                        avatarFile = file;
                    }
                }
                params.put(UserInfo.PHOTOS, sb.toString());
            }
            if (avatarFile != null) {
                params.put("avatar_file", avatarFile);
            } else if (mPhotoManager.getAvatar() != null) {
                params.put("avatar_id", String.valueOf(mPhotoManager.getAvatar().id));
            }
            if (!TextUtils.isEmpty(mDelPhotoIds)) {
                params.put("delete_photos", mDelPhotoIds);
            }
        }
        new UpdatePhotoTask(params, files).executeLong();
    }

    private void changeImage(int type) {
        Intent intent = new Intent(this, UpdateProfileImageActiivity.class);
        intent.putExtra(UpdateProfileImageActiivity.EXT_CHANGE_TYPE, type);
        intent.putExtra(UpdateProfileImageActiivity.EXT_USER_INFO, mUserInfo);
        startActivityForResult(intent, type == UpdateProfileImageActiivity.TYPE_AVATAR ? REQUEST_UPDATE_AVATAR
                : REQUEST_UPDATE_BG);
    }

    @Override
    public void onSubscribe(boolean success) {
        // getTitleBar().hideProgress();
        mBtnAddContact.setEnabled(true);
        if (success) {
            mBtnAddContact.setVisibility(View.GONE);
            toast(R.string.adc_request_sent);
        } else {
            toast(R.string.adc_request_sent_failed);
        }
    }

    @Override
    public void onUnsubscribe(boolean success) {
        // getTitleBar().hideProgress();
        if (success) {
            mBtnAddContact.setVisibility(View.VISIBLE);
            toast(R.string.prof_contact_remove_success);
        } else {
            toast(R.string.prof_contact_remove_failed);
        }
    }

    private class FetchUserTask extends MsTask {

        public FetchUserTask() {
            super(getApplicationContext(), mUserInfo.hasUserId() || !mUserInfo.hasFaceId() ? MsRequest.USER_FULL_INFO
                    : MsRequest.FIND_BY_FACE_ID);
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder();
            if (mUserInfo.hasUserId()) {
                sb.append("user_id=").append(mUserInfo.userId);
            } else if (mUserInfo.hasFaceId()) {
                sb.append("face_id=").append(mUserInfo.faceId);
            } else {
                sb.append("guid=").append(mUserInfo.guid);
            }
            boolean isFriend = mUserInfo.isMine(getRefContext()) || mUserEntryManager.isFriend(mUserInfo.jid);
            return sb.append("&is_friend=").append(isFriend ? 1 : 0).toString();
        }

        @Override
        protected void onPreExecute() {
            mLastTask = this;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mLastTask = null;
            if (response.isSuccessful()) {
                findViewById(R.id.iv_menu).setVisibility(View.GONE);
                mUserInfo = UserInfo.fromJson(response.getJsonObject());
                mIsModerator = mUserInfo.isModerator();
                mUserInfoManager.saveUserInfo(mUserInfo);

                showBasicUserInfo();
                mPhotoManager.setUserInfo(mUserInfo);
                showMedals();
                if (!mUserInfo.isMine(ProfileActivity.this) && !Utils.isMianLiaoService(mUserInfo)) {
                    showAddContactButton();
                }

                findViewById(R.id.btn_chat).setEnabled(true);
                if (Utils.isMianLiaoService(mUserInfo) && !isMe) {
                    findViewById(R.id.iv_menu).setVisibility(View.GONE);
                    findViewById(R.id.btn_chat).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.iv_menu).setVisibility(isMe ? View.GONE : View.VISIBLE);
                }
            } else {
                if (!mUserInfo.hasUserId()) {
                    // getTitleBar().showTitleText(R.string.prof_user_not_exist,
                    // null);
                    findViewById(R.id.iv_menu).setVisibility(View.GONE);
                    ((TextView) findViewById(R.id.tv_name)).setText(R.string.prof_user_not_exist);
                }
                if (response.code == MsResponse.MS_USER_NOT_EXIST) {
                    FaceManager.getInstance(getRefContext()).removeUserFace(mUserInfo.faceId);
                }
                response.showFailInfo(getRefContext(), R.string.prof_user_fetch_failed);
            }
        }
    }

    private class GetVisitorInfoTask extends MsTask{

        public GetVisitorInfoTask() {
            super(ProfileActivity.this, MsRequest.USER_VISITOR_INFO);
        }
        
        @Override
        protected String buildParams() {
            return "query_uid=" + mUserInfo.userId;
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONObject object = response.getJsonObject();
                if (object != null) {
                    mScanTotle = object.optInt("all_visit_times");  
                    updateVisitCountInfo();
                }
            }
        }
        
    }
    
    private class GetPraiseInfoTask extends MsTask{
        
        public GetPraiseInfoTask() {
            super(ProfileActivity.this, MsRequest.USER_PRAISE_INFO);
        }
        
        @Override
        protected String buildParams() {
            return "query_uid=" + mUserInfo.userId;
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mUpCount = response.getJsonObject().optInt("praise_times");
                updatePraiseCountInfo();
            }
        }
        
    }

    private void showAddContactButton() {
        if (mUserInfo.jid != null && !mUserEntryManager.isFriend(mUserInfo.jid)) {
            findViewById(R.id.btn_add_contact).setVisibility(View.VISIBLE);
        }
    }

    public void updateVisitCountInfo() {
        mTvFlowerCount.setText(String.valueOf(mScanTotle));
    }
    
    public void updatePraiseCountInfo() {
        mTvUpCount.setText(String.valueOf(mUpCount));
    }

    private class BlacklistTask extends AdvAsyncTask<Void, Void, Boolean> {
        private boolean mAddTo;

        public BlacklistTask(boolean addTo) {
            mAddTo = addTo;
        }

        @Override
        protected void onPreExecute() {
            // getTitleBar().showProgress();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return mUserEntryManager.changeBlacklist(mUserInfo.jid, mAddTo);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (mAddTo) {
                if (success) {
                    toast(R.string.prof_blacklist_add_success);
                } else {
                    toast(R.string.prof_blacklist_add_failed);
                }
            } else {
                if (success) {
                    toast(R.string.prof_blacklist_remove_success);
                } else {
                    toast(R.string.prof_blacklist_remove_failed);
                }
            }
            // getTitleBar().hideProgress();
        }
    }
    
    private class VisitTask extends MsTask{

        public VisitTask() {
            super(ProfileActivity.this, MsRequest.USER_VISIT);
        }
        
        @Override
        protected String buildParams() {
            return "visitor_uid=" + mUserInfo.userId;
        }
        
    }

    private class PrimaryMedalAdapter extends MedalAdapter {
        private int mMaxCount;
        private int mColorPrimay;
        private int mColorEmpty;

        public PrimaryMedalAdapter(Context context) {
            super(context);
            Resources res = getResources();
            mMaxCount = res.getInteger(R.integer.max_primary_medals);
            mColorPrimay = res.getColor(R.color.mdl_primary);
            mColorEmpty = res.getColor(R.color.mdl_empty);
        }

        @Override
        public boolean isEmpty() {
            return super.getCount() == 0;
        }

        @Override
        public int getCount() {
            return mMaxCount;
        }

        @Override
        public Medal getItem(int position) {
            return position < super.getCount() ? super.getItem(position) : null;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_medal_primary, parent, false);
            }
            ProImageView ivMedal = (ProImageView) view.findViewById(R.id.iv_medal);

            Medal medal = getItem(position);
            if (medal == null) {
                ivMedal.setImageResource(R.drawable.ic_medal_empty);
            } else {
                ivMedal.setImage(medal.imageUrl, R.drawable.ic_medal_empty);
            }

            view.setOnClickListener(ProfileActivity.this);
            return view;
        }
    }

    @Override
    public void onImageResult(boolean success, String imageFile, Bitmap bm) {
        if (success) {
            if (mPhotoManager.hasTodo()) {
                mPhotoManager.addPhoto(imageFile);
            }
        } else {
            toast(R.string.prof_failed_save_picture);
        }
    }

    @Override
    public void onImageResult(boolean success, ArrayList<String> images) {
        if (success)  {
            if (mPhotoManager.hasTodo()) {
                for (String imageFile : images) {
                    mPhotoManager.addPhoto(imageFile);
                    if (imageFile.contains("LSQ_2")) {
                        mEditImages.add(imageFile);
                    }                
                }
            }
        } else {
            toast(R.string.prof_failed_save_picture);
        }
    }

    private class UpdatePhotoTask extends MsMhpTask{

        public UpdatePhotoTask(HashMap<String, String> parameters, HashMap<String, String> files) {
            super(ProfileActivity.this, MsRequest.UPDATE_PROFILE, parameters, files);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPhotoManager.destroy();
            deleteEditImages();
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
    
    private void showPopupMenu(View anchor) {
        if ((mUserEntryManager.isFriend(mUserInfo.jid)) && (mUserEntryManager.isBlacklisted(mUserInfo.jid))) {
            if (!mIsModerator && mMyInfo.isModerator()) {
                mPopMenu = new PopupView(ProfileActivity.this).setItems(R.array.profile_menu_popup_del_del_nospeak, this);
            } else {
                mPopMenu = new PopupView(ProfileActivity.this).setItems(R.array.profile_menu_popup_del_del, this);
            }
        } else if ((mUserEntryManager.isFriend(mUserInfo.jid)) && (!(mUserEntryManager.isBlacklisted(mUserInfo.jid)))) {
            if (!mIsModerator && mMyInfo.isModerator()) {
                mPopMenu = new PopupView(ProfileActivity.this).setItems(R.array.profile_menu_popup_del_ad_nospeak, this);
            } else {
                mPopMenu = new PopupView(ProfileActivity.this).setItems(R.array.profile_menu_popup_del_ad, this);
            }
        } else if ((!(mUserEntryManager.isFriend(mUserInfo.jid))) && (mUserEntryManager.isBlacklisted(mUserInfo.jid))) {
            if (!mIsModerator && mMyInfo.isModerator()) {
                mPopMenu = new PopupView(ProfileActivity.this).setItems(R.array.profile_menu_popup_ad_del_nospeak, this);
            } else {
                mPopMenu = new PopupView(ProfileActivity.this).setItems(R.array.profile_menu_popup_ad_del, this);
            }
        } else {
            if (!mIsModerator && mMyInfo.isModerator()) {
                mPopMenu = new PopupView(ProfileActivity.this).setItems(R.array.profile_menu_popup_ad_ad_nospeak, this);
            } else {
                mPopMenu = new PopupView(ProfileActivity.this).setItems(R.array.profile_menu_popup_ad_ad, this);
            }
        }
        mPopMenu.showAsDropDown(anchor, false);
    }

    @Override
    public void onItemClick(int position, PopupItem item) {
        switch (position) {
            case 0:
                if (!mIsModerator && mMyInfo.isModerator()) {
                    // 禁言
                    showBannedDialog();
//                    banned();
                } else {
                    startActivity(new Intent(ProfileActivity.this, GroupReportActivity.class));
                }
                break;

            case 1:
                if (mUserEntryManager.isFriend(mUserInfo.jid)) {
                    showRemoveContactDialog();
                } else {
                    subscribe();
                }
                break;
            case 2:
                if (mUserEntryManager.isBlacklisted(mUserInfo.jid)) {
                    blacklist(false);
                } else {
                    showBlockContactDialog();
                }
                break;
            default:
                break;
        }
    }

    private class UpdateAvatarTask extends MsMhpTask {

        public UpdateAvatarTask(HashMap<String, String> parameters, HashMap<String, String> files) {
            super(ProfileActivity.this, MsRequest.UPDATE_PROFILE, parameters, files);
        }

    }

    private HashMap<String, String> getFiles() {
        HashMap<String, String> files = new HashMap<String, String>();
        files.put("avatar", FileDownloader.getInstance(this).getFileName(mAvatarUrl));
        return files;
    }

    private HashMap<String, String> getParameters() {
        HashMap<String, String> param = new HashMap<String, String>();
        param.put("avatar", FileDownloader.getInstance(this).getFileName(mAvatarUrl));
        return param;
    }
    
    private void banned(String bannedIds, String unBannedIds) {
        new BannedTask(bannedIds, unBannedIds).executeLong();
    }
    
    private class BannedTask extends MsTask{
        
        private String mBannedIds, mUnbannedIds;

        public BannedTask(String bannedIds, String unBannedIds) {
            super(ProfileActivity.this, MsRequest.CF_BANNED);
            mBannedIds = bannedIds;
            mUnbannedIds = unBannedIds;
        }
        
        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder();
            sb.append("uid_banned=").append(mUserInfo.userId)
                .append("&liftingBan=").append(mUnbannedIds)
                .append("&banned=").append(mBannedIds);
            return sb.toString();
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mShowBannedInfos = mCheckTribeBoxView.getBannedInfos();
                toast(R.string.prof_banned_success);
            }
        }
    }
    
    

}
