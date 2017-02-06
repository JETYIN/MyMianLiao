package com.tjut.mianliao.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.R;
import com.tjut.mianliao.chat.ChatActivity;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.StickyScrollView;
import com.tjut.mianliao.contact.FollowUserManager;
import com.tjut.mianliao.contact.FollowUserManager.OnUserFollowListener;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.data.Medal;
import com.tjut.mianliao.data.Photo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.tribe.TribeInfo;
import com.tjut.mianliao.explore.GoldDepositsActivity;
import com.tjut.mianliao.image.ImageActivity;
import com.tjut.mianliao.live.LiveGainRankActivity;
import com.tjut.mianliao.live.LiveRank;
import com.tjut.mianliao.profile.ProfileFragment.StickyScrollCallBack;
import com.tjut.mianliao.tribe.TribeClassifyDetailActivity;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.GetImageHelper.ImageResultListener;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class PDetailFragment extends Fragment implements ImageResultListener, OnClickListener, OnUserFollowListener {

    private int wealth;
    public static final String EXTRA_USER_GUID = "extra_user_guid";
    public static final String EXTRA_USER_FACE_ID = "extra_user_face_id";
    public static final String EXTRA_SHOW_CHAT_BUTTON = "extra_show_chat_button";
    public static final String EXTRA_NICK_NAME = "extra_nick_name";
    public static final String EXTRA_USER_DESC = "extra_user_desc";

    public static final int RESULT_UPDATED = 1;
    private static final int REQUEST_SET_BADGE = 200;
    protected static final int SHOW_IMAGE_REQUEST = 101;
    protected static final int REQUEST_UPDATE_NICK = 102;
    protected static final int REQUEST_UPDATE_DESC = 103;
    private static final int REQUEST_GOLD_DEPOS_CODE = 105;
    private static final int REQUEST_MY_MEDALS_CODE = 600;


    @ViewInject(R.id.rl_income_me)
    private RelativeLayout mRLiveGain;
    @ViewInject(R.id.rl_live_rank)
    private RelativeLayout mRLiveRank;
    @ViewInject(R.id.rl_id_verify)
    private RelativeLayout mRlIdVerify;
    @ViewInject(R.id.tv_indev_sign)
    private TextView mTvIndevSign;
    @ViewInject(R.id.tv_account)
    private TextView mTvAccount;
    @ViewInject(R.id.tv_point)
    private TextView mTvPoint;
    @ViewInject(R.id.tv_gold)
    private TextView mTvGold;
    @ViewInject(R.id.tv_point_count)
    private TextView mTvPointCount;
    @ViewInject(R.id.tv_gold_count)
    private TextView mTvGoldCount;
    @ViewInject(R.id.tv_income_count)
    private TextView mTvLingWealth;
    @ViewInject(R.id.tv_income)
    private TextView tvLiveWealth;
    @ViewInject(R.id.gv_gallery)
    private GridView mGridViewPhoto;
    @ViewInject(R.id.gv_medal)
    private GridView mGridViewMedal;
    @ViewInject(R.id.ll_identity_card)
    private LinearLayout mLlIdentityCard;
    @ViewInject(R.id.rl_point_me)
    private RelativeLayout mRlPointMe;
    @ViewInject(R.id.rl_gold_me)
    private RelativeLayout mRlGoldMe;
    @ViewInject(R.id.rl_indiv_sign)
    private LinearLayout mLlShortDes;
    @ViewInject(R.id.btn_chat)
    private Button mBtnChat;
    @ViewInject(R.id.btn_add_contact)
    private Button mBtnAddContact;
    @ViewInject(R.id.ll_profile_detail)
    private LinearLayout mLlProfileDetail;
    @ViewInject(R.id.ll_my_tribe)
    private LinearLayout mLlMyTribe;
    @ViewInject(R.id.iv_right_tribe)
    private ImageView mIvRightTribe;
    @ViewInject(R.id.ll_my_follow_tribe)
    private LinearLayout mllFollTribe;
    @ViewInject(R.id.ll_medal_card)
    private LinearLayout mLlMedalCard;
    @ViewInject(R.id.ll_bottom_button)
    private LinearLayout mLlBottomButton;
    @ViewInject(R.id.gv_income_rank)
    private GridView mGvRank;

    private ProImageView ivAvatar;

    private LayoutInflater mInflater;
    private ArrayList<String> mEditImages;
    private ArrayList<TribeInfo> mFollowTribes;
    private ArrayList<LiveRank> listRank;

    private boolean mIsMe;
    private UserInfo mUserInfo;
    private StickyScrollCallBack mScrollListener;
    private StickyScrollView mScrollView;

    private PhotoManager mPhotoManager;
    private GetImageHelper mGetImageHelper;
    private PrimaryMedalAdapter mMedalAdapter;
    private LiveRankMedalAdapter mLiveRankAdapter;
    private IncomeListAdapter mIncomeAdapter;
    private UserInfoManager mUserInfoManager;


    private String mAvatarUrl;
    private String mDelPhotoIds;
    private boolean mHasUpdate;

    private FollowUserManager mFollowManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEditImages = new ArrayList<>();
        mUserInfoManager = UserInfoManager.getInstance(getActivity());
        mGetImageHelper = new GetImageHelper(getActivity(), this);
        mInflater = LayoutInflater.from(getActivity());
        mFollowTribes = new ArrayList<>();
        listRank = new ArrayList<>();
        mFollowManager = FollowUserManager.getInstance(getActivity());
        mFollowManager.registerOnUserFollowListener(this);
        new LiveTotalWealthTask().executeLong();
        new LiveRankTask().executeLong();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_detail_homepage, null);
        ViewUtils.inject(this, view);
        initView(view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        updatePhoto();
    }

    @Override
    public void onDestroy() {
        mFollowManager.unregisterOnUserFollowListener(this);
        super.onDestroy();
    }

    private void initView(View view) {
        mScrollView = (StickyScrollView) view.findViewById(R.id.scrollview);
        View nullView = view.findViewById(R.id.null_view);
        LayoutParams layoutParams = nullView.getLayoutParams();
        layoutParams.height = ProfileFragment.sStickyTopToViewPager;
        mScrollView.setScrollCallBack(mScrollListener);

    }

    private void showBasicInfo() {
        if (mPhotoManager == null) {
            mPhotoManager = new PhotoManager(getActivity(), mUserInfo, ivAvatar, mIsMe ? true : false);
            mGridViewPhoto.setAdapter(mPhotoManager.getAdapter());
        }
        mGridViewPhoto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Photo photo = (Photo) parent.getItemAtPosition(position);
                if (photo == null) {
                    mPhotoManager.setTodo(null, PhotoManager.TYPE_PHOTO);
                    mGetImageHelper.getImage(true, 10 - mPhotoManager.getPhotoSize());
                } else {
                    if (mIsMe) {
                        Intent intent = new Intent(getActivity(), ImageActivity.class)
                                .putExtra(ImageActivity.EXTRA_IMAGE_INDEX, position)
                                .putExtra(ImageActivity.EXTRA_IS_SHOW_OPER, true)
                                .putStringArrayListExtra(ImageActivity.EXTRA_IMAGE_URLS, getImageUrls());
                        startActivityForResult(intent, SHOW_IMAGE_REQUEST);
                    } else {
                        Utils.viewImages(getActivity(), getImageUrls(), position);
                    }
                }
            }
        });

        mMedalAdapter = new PrimaryMedalAdapter(getActivity());
        mGridViewMedal.setAdapter(mMedalAdapter);
//        mIncomeAdapter =  new IncomeListAdapter();
        mLiveRankAdapter = new LiveRankMedalAdapter();
        mGvRank.setAdapter(mLiveRankAdapter);

        showMedals();
        if (mUserInfo.isSpecial() && mUserInfo.shortDesc != null && mUserInfo.shortDesc.split(",").length > 1) {
            mTvIndevSign.setText(mUserInfo.shortDesc.split(",")[1]);
        } else
            mTvIndevSign.setText((mUserInfo.shortDesc == null || "".equals(mUserInfo.shortDesc)) ?
                    getString(R.string.prof_no_short_desc) : mUserInfo.shortDesc);

        if (!mUserInfo.isVerified() && mUserInfo.isMine(getActivity())) {
            mRlIdVerify.setVisibility(View.VISIBLE);
        } else {
            mRlIdVerify.setVisibility(View.GONE);
        }

        if (mUserInfo.isMine(getActivity())) {
            mTvAccount.setText(mUserInfo.account);
        }

        if (mUserInfo.isMine(getActivity())) {
            mLlIdentityCard.setVisibility(View.VISIBLE);
            mTvPoint.setText(R.string.points_count_mine_title);
            mTvGold.setText(R.string.points_gold_mine_title);
        } else {
            mLlIdentityCard.setVisibility(View.GONE);
            mTvPoint.setText(R.string.points_count_other_title);
            mTvGold.setText(R.string.points_gold_other_title);
            tvLiveWealth.setText(R.string.points_livewealth_other_title);
            showAddContactButton();
        }
        mTvPointCount.setText(getString(R.string.points_count_text, mUserInfo.credit));
        mTvGoldCount.setText(getString(R.string.points_gold_text, mUserInfo.gold));
        setOnClickListener();
    }


    private void updatePhoto() {
        HashMap<String, String> params = new HashMap<>();
        HashMap<String, String> files = new HashMap<>();
        if (mPhotoManager == null) {
            return;
        }
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

    private void setOnClickListener() {
        mRlPointMe.setOnClickListener(this);
        mRlGoldMe.setOnClickListener(this);
        mLlShortDes.setOnClickListener(this);
        mBtnChat.setOnClickListener(this);
        mBtnAddContact.setOnClickListener(this);
        mllFollTribe.setOnClickListener(this);
        mLlMedalCard.setOnClickListener(this);
        mRlIdVerify.setOnClickListener(this);
        mRLiveRank.setOnClickListener(this);
        mRLiveGain.setOnClickListener(this);
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

    public void setScrollCallBack(StickyScrollCallBack scrollListener) {
        mScrollListener = scrollListener;
        if (mScrollView != null)
            mScrollView.setScrollCallBack(mScrollListener);
    }


    public void updateUserInfo(UserInfo userInfo, ProImageView avatar) {
        mUserInfo = userInfo;
        ivAvatar = avatar;
        mIsMe = mUserInfo.isMine(getActivity());
        if (mIsMe) {
            mLlBottomButton.setVisibility(View.GONE);
            mBtnChat.setVisibility(View.GONE);
        } else {
            mLlBottomButton.setVisibility(View.VISIBLE);
            mBtnChat.setVisibility(View.VISIBLE);
        }
        new GetConcernTribesTask(mUserInfo.userId).executeLong();
        if (isAdded()) {
            showBasicInfo();
        }
    }

    public void invalidScroll() {
        mScrollView.invalidScroll();
    }

    public int getContentHeight() {
        return mLlProfileDetail.getHeight();
    }

    public StickyScrollView getScrollView() {
        return mScrollView;
    }

    public int getStickyHeight() {
        int scrollHeight = mScrollView.getScrollY();
        System.out.println("---------- get Height : detail -- " + scrollHeight);
        if (scrollHeight > ProfileFragment.sStickyTopToTab) {
            return ProfileFragment.sStickyTopToTab;
        }
        return scrollHeight;
    }

    public int getFirstViewToTop() {
        return mNullView.getTop();
    }

    public void setStickyH(int stickyH) {
        if (Math.abs(stickyH - getStickyHeight()) < 10) {
            return;
        }
        // 判断高度，根据高度来决定是ScrollView向上滚动还是TopView向下滚动，并算出向下滚动的距离
        int scrollContentHeight = mScrollView.getHeight() - ProfileFragment.sStickyBarHeight;
        int contentHeight = getContentHeight();
        if (scrollContentHeight <= contentHeight) {
            mScrollView.scrollTo(0, stickyH);
        } else {
            int distance = scrollContentHeight - contentHeight - ProfileFragment.sStickyTopToTab + 10;
            if (distance < -ProfileFragment.sStickyTopToTab) {
                distance = -ProfileFragment.sStickyTopToTab;
            }
            if (distance > 0) {
                distance = 0;
            }
            mScrollListener.onScrollChanged(distance);
        }
        mScrollView.scrollTo(0, stickyH);
    }

    private class LiveRankMedalAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return listRank.size();
        }

        @Override
        public LiveRank getItem(int position) {
            return listRank.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LiveRank liveRank = getItem(position);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_medal_primary, parent, false);
            }
            ImageView ivLiveRankAvatar = (ImageView) convertView.findViewById(R.id.iv_medal);
            if (!TextUtils.isEmpty(liveRank.avatar)) {
                Picasso.with(getActivity())
                        .load(liveRank.avatar)
                        .placeholder(R.drawable.pic_face_02)
                        .into(ivLiveRankAvatar);
            }
            return convertView;
        }
    }

    private class PrimaryMedalAdapter extends MedalAdapter {
        private int mMaxCount;
        private int mColorPrimay;
        private int mColorEmpty;

        public PrimaryMedalAdapter(Context context) {
            super(context);
            if (isAdded()) {
                Resources res = getResources();
                mMaxCount = res.getInteger(R.integer.max_primary_medals);
                mColorPrimay = res.getColor(R.color.mdl_primary);
                mColorEmpty = res.getColor(R.color.mdl_empty);
            }
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
            ImageView ivMedal = (ImageView) view.findViewById(R.id.iv_medal);

            Medal medal = getItem(position);
            if (medal == null || TextUtils.isEmpty(medal.imageUrl)) {
                ivMedal.setImageResource(R.drawable.ic_medal_empty);
            } else if (!TextUtils.isEmpty(medal.imageUrl)) {
                Picasso.with(getActivity()).load(medal.imageUrl).into(ivMedal);
            } else {
                ivMedal.setImageResource(R.drawable.ic_medal_empty);
            }

            view.setOnClickListener(mMedalOnClickListener);
            return view;
        }
    }

    private OnClickListener mMedalOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mIsMe) {
                Intent iMedal = new Intent(getActivity(), MyMedalsActivity.class);
                iMedal.putExtra(MyMedalsActivity.MY_MADELS, mUserInfo.getMedals());
                startActivityForResult(iMedal, REQUEST_MY_MEDALS_CODE);
            }
        }
    };
    private View mNullView;


    /**
     * @param success
     * @param imageFile
     * @param bm
     * @deprecated
     */
    @Override
    public void onImageResult(boolean success, String imageFile, Bitmap bm) {
    }

    @Override
    public void onImageResult(boolean success, ArrayList<String> images) {
        if (success) {
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

    private void toast(int resId) {
        Toast.makeText(getActivity(), getResources().getString(resId), Toast.LENGTH_SHORT).show();
    }

    /**
     * 获取直播收获榜头像
     **/

    private class LiveRankTask extends MsTask {

        public LiveRankTask() {
            super(getActivity(), MsRequest.CONTRIBUTE_RANKING);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                listRank = JsonUtil.getArray(response.getJsonArray(), LiveRank.TRANSFORMER);
                if (listRank.size() > 0) {
                    mLiveRankAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    /**
     * 获取个人直播收益
     **/
    private class LiveTotalWealthTask extends MsTask {
        public LiveTotalWealthTask() {
            super(getActivity(), MsRequest.MY_TOTAL_INCOME);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                wealth = response.getJsonObject().optInt("income");
                mTvLingWealth.setText(getActivity().getString(R.string.prof_my_live_gain, wealth));

            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_chat:
                if (!TextUtils.isEmpty(mUserInfo.jid)) {
                    Intent i = new Intent(getActivity(), ChatActivity.class);
                    i.putExtra(ChatActivity.EXTRA_CHAT_TARGET, mUserInfo.jid);
                    startActivity(i);
                }
                break;
            case R.id.btn_add_contact:
                if (mUserInfo != null) {
                    mFollowManager.follow(mUserInfo.userId);
                }
                break;
            case R.id.rl_point_me:
                if (mUserInfo.isMine(getActivity())) {
                    Intent intent = new Intent(getActivity(), MyWheatActivity.class);
                    startActivity(intent);
                    MobclickAgent.onEvent(getActivity(), MStaticInterface.WHEAT);
                }
                break;
            case R.id.rl_live_rank:
                if (mUserInfo.isMine(getActivity())) {
                    Intent intent = new Intent(getActivity(), LiveGainRankActivity.class);
                    intent.putExtra("wealth", wealth);
                    startActivity(intent);
                }
                break;
            case R.id.rl_income_me:
                startActivity(new Intent(getActivity(), MyAcountBillActivity.class));
                /**我的总共直播收益**/
                break;
            case R.id.rl_gold_me:
                if (mUserInfo.isMine(getActivity())) {
                    Intent in = new Intent(getActivity(), GoldDepositsActivity.class);
                    startActivityForResult(in, REQUEST_GOLD_DEPOS_CODE);
                    MobclickAgent.onEvent(getActivity(), MStaticInterface.GOLD);
                }
                break;
            case R.id.rl_indiv_sign:
                if (mUserInfo.isMine(getActivity())) {
                    Intent shortDesIntent = new Intent(getActivity(), UpdateShortDescActivity.class);
                    shortDesIntent.putExtra(UpdateShortDescActivity.EXT_USER_DES, mUserInfo.shortDesc);
                    startActivityForResult(shortDesIntent, REQUEST_UPDATE_DESC);
                    MobclickAgent.onEvent(getActivity(), MStaticInterface.SIGNATURE);
                }
                break;
            case R.id.ll_medal_card:
                if (mIsMe) {
                    Intent iMedal = new Intent(getActivity(), MyMedalsActivity.class);
                    iMedal.putExtra(MyMedalsActivity.MY_MADELS, mUserInfo.getMedals());
                    startActivityForResult(iMedal, REQUEST_MY_MEDALS_CODE);
                }
                break;
            case R.id.ll_my_follow_tribe:
                Intent mintent = new Intent();
                mintent.setClass(getActivity(), TribeClassifyDetailActivity.class);
                mintent.putExtra(TribeClassifyDetailActivity.EXT_TRIBE_BY_UID, mUserInfo.userId);
                startActivity(mintent);
                break;

            case R.id.rl_id_verify:
                Intent iAuth = new Intent(getActivity(), IdVerifyActivity.class);
                startActivity(iAuth);
                break;

            default:
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mGetImageHelper.handleResult(requestCode, data);
        if (resultCode == RESULT_UPDATED && requestCode == REQUEST_SET_BADGE) {
            ArrayList<Medal> primaryMedals = data.getParcelableArrayListExtra(MedalActivity.EXTRA_PRIMARY_MEDALS);
            mUserInfo.setPrimaryMedals(primaryMedals);
            mUserInfoManager.saveUserInfo(mUserInfo);
            showMedals();
        } else if (resultCode == Activity.RESULT_OK && requestCode == SHOW_IMAGE_REQUEST) {
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
        } else if (resultCode == RESULT_UPDATED && requestCode == REQUEST_UPDATE_DESC) {
            String desc = data.getStringExtra(EXTRA_USER_DESC);
            mUserInfo.shortDesc = desc;
            mUserInfoManager.updateUserInfo(mUserInfo);
            mTvIndevSign.setText(desc);
            mTvIndevSign.invalidate();
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_GOLD_DEPOS_CODE) {
            mTvGoldCount.setText(String.valueOf(mUserInfo.gold));
        } else if (resultCode == RESULT_UPDATED && requestCode == REQUEST_MY_MEDALS_CODE) {
            ArrayList<Medal> primaryMedals = data.getParcelableArrayListExtra(MedalActivity.EXTRA_PRIMARY_MEDALS);
            showMedals(primaryMedals);
            mUserInfo.setPrimaryMedals(primaryMedals);
            mUserInfoManager.saveUserInfo(mUserInfo);
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


    private class UpdatePhotoTask extends MsMhpTask {

        public UpdatePhotoTask(HashMap<String, String> parameters, HashMap<String, String> files) {
            super(getActivity(), MsRequest.UPDATE_PROFILE, parameters, files);
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
                getActivity().sendBroadcast(intent);
                file.delete();
            }
        }
    }

    private class GetConcernTribesTask extends MsTask {
        private int userId;

        public GetConcernTribesTask(int uid) {
            super(getActivity(), MsRequest.TRIBE_CONCERN_TRIBE);
            userId = uid;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("user_id=").append(userId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ArrayList<TribeInfo> tribes = JsonUtil.getArray(response.getJsonArray(), TribeInfo.TRANSFORMER);
                mFollowTribes.clear();
                if (tribes.size() > 3) {
                    for (int i = 0; i < 3; i++) {
                        mFollowTribes.add(tribes.get(i));
                    }
                } else {
                    mFollowTribes.addAll(tribes);
                }
                fillMyTribe(mFollowTribes);
            }
        }

    }

    private void fillMyTribe(ArrayList<TribeInfo> tribes) {
        mLlMyTribe.removeAllViews();
        if (tribes.size() <= 0) {
            if (getActivity() != null) {
                TextView text = new TextView(getActivity());
                if (mIsMe) {
                    text.setText(R.string.tribe_not_follow_tribe);
                } else {
                    text.setText(R.string.tribe_he_not_follow_tribe);
                }
                text.setTextColor(0XFF9B9A9A);
                mLlMyTribe.addView(text);
            }
        } else {
            for (TribeInfo tribe : tribes) {
                if (getActivity() != null) {
                    mLlMyTribe.addView(getTribeItem(tribe));
                }
            }
        }
    }

    private View getTribeItem(TribeInfo tribe) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_follow_tribes, null);
        TextView textView = (TextView) view.findViewById(R.id.tv_tribe_profile);
        LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        l.setMargins(0, 0, getResources().getDimensionPixelOffset(R.dimen.tribe_my_tribe_magin), 0);
        view.setLayoutParams(l);
        textView.setText(tribe.tribeName);
        return view;
    }

    private void showAddContactButton() {
        if (mUserInfo.userId != 0 && !mFollowManager.isFollow(mUserInfo.userId + "")) {
            mBtnAddContact.setVisibility(View.VISIBLE);
        } else {
            mBtnAddContact.setVisibility(View.GONE);
        }
    }

    @Override
    public void onFollowSuccess() {
        showAddContactButton();
    }

    @Override
    public void onFollowFail() {

    }

    @Override
    public void onCancleFollowSuccess() {
        showAddContactButton();
    }

    @Override
    public void onCancleFollowFail() {

    }

    @Override
    public void onGetFollowListSuccess(MsResponse response, int offset) {
    }

    @Override
    public void onGetFollowListFail() {

    }

    private class IncomeListAdapter extends BaseAdapter {
        private ArrayList<UserInfo> mIncomeList;

        public IncomeListAdapter(ArrayList<UserInfo> mIncomeList) {
            mIncomeList = mIncomeList;
        }

        @Override
        public int getCount() {
            return mIncomeList.size();
        }

        @Override
        public UserInfo getItem(int i) {
            return mIncomeList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_medal_primary, viewGroup, false);
            }
            ImageView avatar = (ImageView) view.findViewById(R.id.iv_medal);
            UserInfo user = getItem(i);
            if (!TextUtils.isEmpty(user.getAvatar())) {
                Picasso.with(getActivity()).load(user.getAvatar()).into(avatar);
            }
            return view;
        }
    }


}
