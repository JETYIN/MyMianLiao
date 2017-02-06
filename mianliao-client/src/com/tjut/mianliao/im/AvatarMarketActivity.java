package com.tjut.mianliao.im;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout.LayoutParams;

import com.tjut.mianliao.BrowserActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.cocos2dx.CocosAvatarView;
import com.tjut.mianliao.cocos2dx.CocosAvatarView.OnAvatarLoadedListener;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.IMResource;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.explore.BuyVipHomePageActivity;
import com.tjut.mianliao.explore.EmotionManagementActivity;
import com.tjut.mianliao.explore.EmotionsDetailActivity;
import com.tjut.mianliao.im.IMResourceManager.IMResourceListener;
import com.tjut.mianliao.question.QuestionActivity;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class AvatarMarketActivity extends BrowserActivity implements OnClickListener, DialogInterface.OnClickListener,
        IMResourceListener, OnAvatarLoadedListener {

    public static final String EXTRA_SHOW_AVATAR = "extra_show_avatar";
    public static final String EXT_TYPE = "ext_type";

    private CocosAvatarView mCocosAvatarView;
    private int mCocosAvatarSize;

    private IMResourceManager mIMResManager;
    private AccountInfo mAccountInfo;
    private UserInfo mUserInfo;
    private LightDialog mPayDialog, mShowInfoDialog;

    private boolean mShowAvatar;
    private int mResType;
    private int mResId;
    private int mOptType;
    private boolean mSetOnExit;
    private int mType, mResPrice, mResCredit;
    private int mVipPrice, mVipCredit;
    private String mResSn, mResUrl;
    private boolean mResVip, mResUse, mResAdd;
    private boolean mIsGetUserInfo;
    private boolean mBuyVip, mClickble;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_avatar_market;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIMResManager = IMResourceManager.getInstance(this);
        mIMResManager.registerIMResourceListener(this);
        mAccountInfo = AccountInfo.getInstance(this);
        mUserInfo = mAccountInfo.getUserInfo();
        mCocosAvatarSize = getResources().getDimensionPixelSize(R.dimen.cocos_avatar_size);
        mCocosAvatarView = (CocosAvatarView) findViewById(R.id.cav_avatar);
        mCocosAvatarView.setOnAvatarLoadedListener(this);
        mShowAvatar = getIntent().getBooleanExtra(EXTRA_SHOW_AVATAR, false);
        mType = getIntent().getIntExtra(EXT_TYPE, 0);
        if (mShowAvatar) {
            mCocosAvatarView.setVisibility(View.VISIBLE);
        } else {
            LayoutParams params = (LayoutParams) mBrowser.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            mBrowser.setLayoutParams(params);
        }
        if (mType == IMResource.TYPE_EMOTION_PACKAGE) {
            getTitleBar().showRightText(R.string.explore_emotion_mine, this);
        }
        getUserInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mShowAvatar) {
            mCocosAvatarView.onResume(this);
            mIMResManager.showAvatar(mCocosAvatarSize, true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mShowAvatar) {
            mCocosAvatarView.onPause();
        }
        if (mSetOnExit) {
            mIMResManager.setUsingRes(mResId, mResType, mOptType);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIMResManager.unregisterIMResourceListener(this);
    }

    @Override
    public boolean shouldOverrideUrlLoading(String url) {
        if (processUrl(url)) {
            return true;
        }
        return super.shouldOverrideUrlLoading(url);
    }

    private void getUserInfo() {
        new FetchUserTask().executeLong();
    }

    private boolean processUrl(String url) {
        Uri uri = Uri.parse(url);
        if (url.startsWith(MsRequest.IMRW_GAME_CLICK.getToLocalUrl())) {
            startActivity(QuestionActivity.class);
            return true;
        }
        if (url.startsWith(MsRequest.IMRW_CLICK.getToLocalUrl())) {
            mResId = getIntQueryParameter(uri, "id");
            mResType = getIntQueryParameter(uri, "type");
            mResPrice = getIntQueryParameter(uri, "price");
            mResSn = uri.getQueryParameter("sn");
            mResUrl = uri.getQueryParameter("url");
            mResVip = uri.getBooleanQueryParameter("vip", false);
            mResAdd = uri.getBooleanQueryParameter("add", false);
            mResUse = uri.getBooleanQueryParameter("use", false);
            mResCredit = getIntQueryParameter(uri, "credit");
            mVipPrice = getIntQueryParameter(uri, "vip_price");
            mVipCredit = getIntQueryParameter(uri, "vip_credit");
            switch (mResType) {
                case IMResource.TYPE_CHARACTER_ACTION:
                    mIMResManager.playArmature(1, mResSn);
                    break;

                case IMResource.TYPE_CHARACTER_ACCESSORY:
                    mIMResManager.changeSuit(1, mResSn, mResUrl);
                    break;

                case IMResource.TYPE_EMOTION_PACKAGE:
                    showDetail(mResId);
                    break;

                default:
                    break;
            }
            if (shouldPayForRes() && !mIsGetUserInfo) {
                setOptType();
                showPayDialog();
            }
            if (mResType == IMResource.TYPE_BACKGROUND || mResType == IMResource.TYPE_COURSE_BACKGROUD
                    || mResType == IMResource.TYPE_BUBBLE) {
                mSetOnExit = true;
                mOptType = IMResourceManager.OPT_USE;
            } else {
                mSetOnExit = false;
            }
            return true;
        } else if (url.startsWith(MsRequest.IMRW_VIP_CLICK.getToLocalUrl())) {
            openVip();
            return true;
        }
        return false;
    }

    private void startActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    private void showPayDialog() {
        if (mPayDialog == null) {
            mPayDialog = new LightDialog(this).setTitleLd("付费提示").setNegativeButton(android.R.string.cancel, this)
                    .setPositiveButton(android.R.string.ok, this);
        }
        if (mResVip) {
            if (mUserInfo.vip) {
                mPayDialog.setMessage(getString(R.string.pay_resource_vip_msg, getVipPriceStr(), getResTypeName()));
                mBuyVip = false;
            } else {
                mPayDialog.setMessage("该" + getResTypeName() + "为VIP专享，立刻升级成为VIP?");
                mBuyVip = true;
            }
        } else if (mResPrice == 0 && mResCredit == 0) {
            mPayDialog.setMessage("你可以免费获取该" + getResTypeName());
        } else {
            if (mUserInfo.vip) {
                mPayDialog.setMessage(getString(R.string.pay_resource_vip_msg, getVipPriceStr(), getResTypeName()));
            } else {
                mPayDialog.setMessage(getString(R.string.pay_resource_novip_msg, getPriceStr(), getResTypeName()));
            }
        }
        mPayDialog.show();
    }

    private void showInfoDialog(String msg, boolean clickble) {
        if (mShowInfoDialog == null) {
            mShowInfoDialog = new LightDialog(this).setTitleLd("提示信息").setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, this);
        }
        mClickble = clickble;
        mShowInfoDialog.setMessage(msg);
        mShowInfoDialog.show();
    }

    private String getVipPriceStr() {
        String price = "";
        if (mVipCredit > 0) {
            price = mVipCredit + "麦粒";
        } else if (mVipPrice > 0) {
            price = mVipPrice + "金币";
        }
        return price;
    }

    private String getPriceStr() {
        String price = "";
        if (mResCredit > 0) {
            price = mResCredit + "麦粒";
        } else if (mResPrice > 0) {
            price = mResPrice + "金币";
        }
        return price;
    }

    private String getResTypeName() {
        switch (mResType) {
            case IMResource.TYPE_EMOTION_PACKAGE:
                return "表情包";
            case IMResource.TYPE_BACKGROUND:
                return "聊天背景";
            case IMResource.TYPE_BUBBLE:
                return "聊天气泡";
            case IMResource.TYPE_CHARACTER_ACCESSORY:
                return "角色小人装扮";
            case IMResource.TYPE_COURSE_BACKGROUD:
                return "课程表背景";
            default:
                return "";
        }
    }

    private boolean shouldPayForRes() {
        switch (mResType) {
            case IMResource.TYPE_EMOTION_PACKAGE:
                return false;
            default:
                if (mResAdd) {
                    return false;
                }
                return true;
        }
    }

    private void setOptType() {
        if (!mResAdd) {
            mOptType = IMResourceManager.OPT_ADD;
        } else if (!mResUse) {
            mOptType = IMResourceManager.OPT_USE;
        } else {
            mOptType = IMResourceManager.OPT_UNUSE;
        }
    }

    private void showDetail(int mResId) {
        Intent intent = new Intent(this, EmotionsDetailActivity.class);
        intent.putExtra(EmotionsDetailActivity.EXT_RES_ID, mResId);
        startActivity(intent);
    }

    private int getIntQueryParameter(Uri uri, String key) {
        int value = 0;
        try {
            value = Integer.parseInt(uri.getQueryParameter(key));
        } catch (NumberFormatException e) {
        }
        return value;
    }

    private class FetchUserTask extends MsTask {

        public FetchUserTask() {
            super(AvatarMarketActivity.this, MsRequest.USER_FULL_INFO);
        }

        @Override
        protected String buildParams() {
            return "user_id=" + mAccountInfo.getUserId();
        }

        @Override
        protected void onPreExecute() {
            mIsGetUserInfo = true;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (response.isSuccessful()) {
                mUserInfo = UserInfo.fromJson(response.getJsonObject());
                mIsGetUserInfo = false;
            }
        }
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(this, EmotionManagementActivity.class));
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mPayDialog) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    if (mBuyVip) {
                        openVip();
                    } else {
                        mIMResManager.AddImResource(mResId);
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    refresh();
                    break;
                default:
                    break;
            }

        } else if (dialog == mShowInfoDialog) {
            if (mClickble) {
                openVip();
            }
        }
    }

    private void openVip() {
        Intent intent = new Intent(this, BuyVipHomePageActivity.class);
        intent.putExtra(BuyVipHomePageActivity.EXT_USER_INFO, mUserInfo);
        startActivity(intent);
    }

    @Override
    public void onUsingResUpdated(int userId, int type, IMResource res) {
    }

    @Override
    public void onGetImResourceSuccess(int type, int requestCode, ArrayList<IMResource> mResources) {
    }

    @Override
    public void onUnzipSuccess() {
    }

    @Override
    public void onUseResSuccess(IMResource res) {
    }

    @Override
    public void onUnuseResSuccess() {
    }

    @Override
    public void onAddResSuccess() {
        refresh();
    }

    @Override
    public void onAddResFail(int code) {
        switch (code) {
            case MsResponse.MS_FAIL_IM_USER_RESOURCE_NOT_EXIST:
                showInfoDialog("该资源不存在", false);
                break;
            case MsResponse.MS_FAIL_TRADE_PRICE_NOT_ENOUGH:
                showInfoDialog("您的账户余额不足", false);
                break;
            case MsResponse.MS_FAIL_TRADE_CREDIT_NOT_ENOUGH:
                showInfoDialog("您的账户积分不足", false);
                break;
            case MsResponse.MS_FAIL_TRADE_RESOURCE_NEED_VIP:
                showInfoDialog("该产品只提供给会员使用,是否立刻升级成为会员？", true);
                break;
            default:
                break;
        }

    }

    @Override
    public void onAvatarLoaded() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIMResManager.onAvatarLoaded();
                mIMResManager.changeSuit(mUserInfo);
                mIMResManager.showAvatar(mCocosAvatarSize, true);
            }
        });
    }

    @Override
    public void onPageReceivedError() {
        super.onPageReceivedError();
        mCocosAvatarView.setVisibility(View.GONE);
    }
}
