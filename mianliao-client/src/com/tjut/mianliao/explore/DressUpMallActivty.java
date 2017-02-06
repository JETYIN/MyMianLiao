package com.tjut.mianliao.explore;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.CommonBanner;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.IMResource;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.im.AvatarMarketActivity;
import com.tjut.mianliao.im.IMResourceManager;
import com.tjut.mianliao.im.IMResourceManager.IMResourceListener;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.umeng.analytics.MobclickAgent;
 
public class DressUpMallActivty extends BaseActivity implements OnClickListener,
        IMResourceListener, DialogInterface.OnClickListener {

    public static final String EXT_DATE = "ext_data";

    private GridView mEmotionGridView, mBubbleGridView, mAvatarGridView;
    private ChatEmotionAdapter mEmotionAdapter;
    private ChatBubbleAdapter mBubbleAdapter;
    private ChatAvatarAdapter mAvatarAdapter;
    private ArrayList<IMResource> mImResourceEmotions;
    private ArrayList<IMResource> mImResourceAvatars;
    private ArrayList<IMResource> mImResourceBubbles;
    private IMResourceManager mImResourceManager;
    private IMResource mCurrentResource;
    private LightDialog mPayDialog, mShowInfoDialog;
    private CommonBanner mVsSwitcher;
    private TextView mTvShowVip;
    private TextView mTvOpenVip;

    private boolean misVip;
    private boolean mBuyVip, mClickble;
    private UserInfo mUserInfo;
    private IMResourceManager mIMResManager;
    private int mResId;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_dressup_mall_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = Settings.getInstance(this);
        mUserInfo = AccountInfo.getInstance(this).getUserInfo();
        misVip = AccountInfo.getInstance(this).getUserInfo().vip;
        mIMResManager = IMResourceManager.getInstance(this);
        getTitleBar().setTitle(getString(R.string.explore_dressup_mall));
        mImResourceManager = IMResourceManager.getInstance(this);
        mImResourceManager.registerIMResourceListener(this);
        mImResourceEmotions = new ArrayList<IMResource>();
        mImResourceBubbles = new ArrayList<IMResource>();
        mImResourceAvatars = new ArrayList<IMResource>();
        mTvOpenVip=(TextView) findViewById(R.id.tv_open_vip);
        mTvShowVip = (TextView) findViewById(R.id.tv_show_vip);
        mEmotionGridView = (GridView) findViewById(R.id.gv_chat_emotion);
        mBubbleGridView = (GridView) findViewById(R.id.gv_chat_bubble);
        mAvatarGridView = (GridView) findViewById(R.id.gv_chat_avatar);
        mEmotionGridView.setOnItemClickListener(mEmotionListener);
        mBubbleGridView.setOnItemClickListener(mBubbleListener);
        mAvatarGridView.setOnItemClickListener(mAvatarListener);
        mEmotionAdapter = new ChatEmotionAdapter();
        mBubbleAdapter = new ChatBubbleAdapter();
        mAvatarAdapter = new ChatAvatarAdapter();
        mEmotionGridView.setAdapter(mEmotionAdapter);
        mBubbleGridView.setAdapter(mBubbleAdapter);
        mAvatarGridView.setAdapter(mAvatarAdapter);
        mVsSwitcher = (CommonBanner) findViewById(R.id.vs_switcher);
        mVsSwitcher.setParam(CommonBanner.Plate.DecorateMain, 0);
        
        getImResource();
        showVipIcon();
    }

    private void showVipIcon() {
        if (misVip) {
            mTvShowVip.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.bottom_icon_smallcrown_on, 0, 0, 0);
        } else {
            mTvShowVip.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.bottom_icon_smallcrown_off, 0, 0, 0);
        }
    }

    private void getImResource() {
        mImResourceManager.getImResource(IMResource.TYPE_BUBBLE, 3, false);
        mImResourceManager.getImResource(IMResource.TYPE_EMOTION_PACKAGE, 3, true);
        mImResourceManager.getImResource(IMResource.TYPE_CHARACTER_ACCESSORY, 4, false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_open_vip:
                openVip(false);
                break;
            case R.id.rl_more_emotion:
            case R.id.tv_more_emotion:
                viewMarket(IMResource.TYPE_EMOTION_PACKAGE);
                MobclickAgent.onEvent(this, MStaticInterface.CHAT);
                break;
            case R.id.rl_more_bubble:
            case R.id.tv_more_bubble:
                viewMarket(IMResource.TYPE_BUBBLE);
                MobclickAgent.onEvent(this, MStaticInterface.BUBBLE1);
                break;
            case R.id.rl_more_avatar:
            case R.id.tv_more_avatar:
                viewMarket(IMResource.TYPE_CHARACTER_ACTION);
                MobclickAgent.onEvent(this, MStaticInterface.ROLE1);
                break;
            case R.id.rl_chat_bg:
                viewMarket(IMResource.TYPE_BACKGROUND);
                MobclickAgent.onEvent(this, MStaticInterface.BACKGROUND1);
                break;
            case R.id.rl_course_bg_dress:
                viewMarket(IMResource.TYPE_COURSE_BACKGROUD);
                MobclickAgent.onEvent(this, MStaticInterface.COURSE1);
                break;
            case R.id.btn_positive:
                
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getImResource();
    }

    private void openVip(boolean vipYear) {
        Intent intent = new Intent(this, VipCenterActivity.class);
        MobclickAgent.onEvent(this, MStaticInterface.MEMBER);
        startActivity(intent);
    }
    
    private void openVip() {
        Intent intent = new Intent(this, BuyVipHomePageActivity.class);
        intent.putExtra(BuyVipHomePageActivity.EXT_USER_INFO, mUserInfo);
        startActivity(intent);
    }

    private void showPayDialog(IMResource resource) {
        mCurrentResource = resource;
        if (mPayDialog == null) {
            mPayDialog = new LightDialog(this)
                .setTitleLd(getString(R.string.dressup_mall_pay_attention))
                .setNegativeButton(android.R.string.cancel, this)
                .setPositiveButton(android.R.string.ok, this);
        }
        if (mCurrentResource.vip) {
            if (mUserInfo.vip) {
                mPayDialog.setMessage(getString(R.string.pay_resource_vip_msg,
                        resource.getImResVipPrice(), getTypeStr(resource)));
                mBuyVip = false;
            } else {
                mPayDialog.setMessage(getString(R.string.dressup_mall_toast_to_vip,
                        getTypeStr(resource)));
                mBuyVip = true;
            }
        } else if (resource.price == 0 && resource.credit == 0) {
            mPayDialog.setMessage(getString(R.string.dressup_mall_free_to_get,
                    getTypeStr(resource)));
        } else {
            if (mUserInfo.vip) {
                mPayDialog.setMessage(getString(R.string.pay_resource_vip_msg,
                        resource.getImResVipPrice(), getTypeStr(resource)));
            } else {
                mPayDialog.setMessage(getString(R.string.pay_resource_novip_msg,
                        resource.getImResNormalPrice(), getTypeStr(resource)));
            }
        }
        mPayDialog.show();
    }

    private String getTypeStr(IMResource resource) {
        String type = "";
        if (resource.type == IMResource.TYPE_BUBBLE) {
            type = getString(R.string.explore_chat_bubble);
        } else if (resource.type == IMResource.TYPE_CHARACTER_ACCESSORY) {
            type = getString(R.string.dressup_mall_chat_avatar_zsp);
        }
        return type;
    }

    private void viewMarket(int type) {
        String params = new StringBuilder("type=").append(type).toString();
        String url = HttpUtil.getUrl(this, MsRequest.IMRW_RESOURCE_LIST, params);
        Intent iMarket = new Intent(this, AvatarMarketActivity.class);
        iMarket.putExtra(AvatarMarketActivity.URL, url);
        if (type == IMResource.TYPE_EMOTION_PACKAGE) {
            iMarket.putExtra(AvatarMarketActivity.EXT_TYPE, type);
        }
        if (type == IMResource.TYPE_CHARACTER_ACTION) {
            iMarket.putExtra(AvatarMarketActivity.EXTRA_SHOW_AVATAR, true);
        }
        startActivity(iMarket);
    }

    private class ChatEmotionAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mImResourceEmotions.size();
        }

        @Override
        public IMResource getItem(int position) {
            return mImResourceEmotions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_emotions, parent, false);
            }
            IMResource resource = getItem(position);
            ProImageView piv = (ProImageView) view.findViewById(R.id.iv_emotion_icon);
            Picasso.with(DressUpMallActivty.this)
		    	.load(resource.preview)
		    	.placeholder(R.drawable.bg_img_loading)
		    	.into(piv);
            TextView tvPrice = (TextView) view.findViewById(R.id.tv_emotion_price);
            TextView tvName = (TextView) view.findViewById(R.id.tv_emotion_name);
            tvName.setText(resource.name);
            if (resource.add) {
                tvPrice.setVisibility(View.GONE);
            } else if (resource.isAllFree()) {
            	tvPrice.setText(getString(R.string.pay_free));
            } else if (resource.isVipFree()) {
            	tvPrice.setText(getString(R.string.explore_vip));
            } else {
            	tvPrice.setText(String.valueOf(resource.getImResPrice()));
            }
            if (resource.vipPrice > 0) {
                tvPrice.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.bottom_icon_littlekernel, 0, 0, 0);
            } else if (resource.vipCredit > 0) {
                tvPrice.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.bottom_icon_littlegolden, 0, 0, 0);
            } else {
                tvPrice.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            return view;
        }

    }

    private class ChatBubbleAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mImResourceBubbles.size();
        }

        @Override
        public IMResource getItem(int position) {
            return mImResourceBubbles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_bubbles, parent, false);
            }
            IMResource resource = getItem(position);
            ProImageView piv = (ProImageView) view.findViewById(R.id.iv_bubble_icon);
            Picasso.with(DressUpMallActivty.this)
		    	.load(resource.preview)
		    	.placeholder(R.drawable.bg_img_loading)
		    	.into(piv);
            ImageView ivStatus = (ImageView) view.findViewById(R.id.iv_buy_status);
            ImageView ivVip = (ImageView) view.findViewById(R.id.iv_buy_type);
            TextView tvPrice = (TextView) view.findViewById(R.id.tv_price);
            if (resource.add) {
                tvPrice.setVisibility(View.GONE);
            } else if (resource.isAllFree()) {
            	tvPrice.setText(getString(R.string.pay_free));
            } else if (resource.isVipFree()) {
            	tvPrice.setText(getString(R.string.explore_vip));
            	tvPrice.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
            } else {
                tvPrice.setText(String.valueOf(resource.getImResPricenum()));
                Drawable drawable = getResources().getDrawable(resource.getImResImg());
                tvPrice.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            }
            if (resource.use) {     
                ivStatus.setVisibility(View.VISIBLE);
            } else {
                ivStatus.setVisibility(View.GONE); 
//                tvPrice.setVisibility(resource.vip ? View.GONE : View.VISIBLE);
                ivVip.setVisibility(resource.vip ? View.VISIBLE : View.GONE);
            }
            return view;
        }
    }

    private class ChatAvatarAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mImResourceAvatars.size();
        }

        @Override
        public IMResource getItem(int position) {
            return mImResourceAvatars.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_avatars, parent, false);
            }
            IMResource resource = getItem(position);
            ProImageView piv = (ProImageView) view.findViewById(R.id.iv_emotion_icon);
            Picasso.with(DressUpMallActivty.this)
		    	.load(resource.preview)
		    	.placeholder(R.drawable.bg_img_loading)
		    	.into(piv);
            ImageView ivVip = (ImageView) view.findViewById(R.id.iv_buy_type);
            TextView tvPrice = (TextView) view.findViewById(R.id.tv_price);
            if (resource.isAllFree()) {
                tvPrice.setText(getString(R.string.pay_free));
            } else if (resource.isVipFree()) {
                tvPrice.setText(getString(R.string.explore_vip));
            } else {
                tvPrice.setText(String.valueOf(resource.getImResPricenum()));
                Drawable drawable = getResources().getDrawable(resource.getImResImg());
                tvPrice.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            }
            if (resource.add) {
                tvPrice.setVisibility(View.VISIBLE);
                ivVip.setVisibility(View.GONE);
            } else {
                tvPrice.setVisibility(View.VISIBLE);
                ivVip.setVisibility(resource.vip ? View.VISIBLE : View.GONE);
            }
            return view;
        }

    }

    private OnItemClickListener mEmotionListener  = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(DressUpMallActivty.this, EmotionsDetailActivity.class);
            intent.putExtra(EXT_DATE, mImResourceEmotions.get(position));
            mResId = mImResourceEmotions.get(position).id;
            startActivity(intent);
        }

    };

    private OnItemClickListener mAvatarListener  = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            IMResource resource = mImResourceAvatars.get(position);
            mResId = resource.id;
            Intent intent = new Intent(DressUpMallActivty.this, EmotionsDetailActivity.class);
            intent.putExtra(EXT_DATE, resource);
            startActivity(intent);
//            if (!resource.add) {
//                showPayDialog(mImResourceAvatars.get(position));
//            } else {
//            	Intent intent  = new Intent(DressUpMallActivty.this, EmotionsDetailActivity.class);
//            	startActivity(intent);
//                toast("您已购买该商品！");
//            }
        }

    };

    private OnItemClickListener mBubbleListener  = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            IMResource resource = mImResourceBubbles.get(position);
            mResId = resource.id;
            if (!resource.add) {
                showPayDialog(mImResourceBubbles.get(position));
            } else {
                toast(R.string.dressup_mall_got_sp);
            }
        }

    };

    @Override
    public void onUsingResUpdated(int userId, int type, IMResource res) {}

    @Override
    public void onGetImResourceSuccess(int type, int requestCode, ArrayList<IMResource> mResources) {
        switch (type) {
            case IMResource.TYPE_BUBBLE:
                mImResourceBubbles = mResources;
                mBubbleAdapter.notifyDataSetChanged();
                break;
            case IMResource.TYPE_EMOTION_PACKAGE:
                mImResourceEmotions = mResources;
                mEmotionAdapter.notifyDataSetChanged();
                break;
            case IMResource.TYPE_CHARACTER_ACCESSORY: // avatar
                mImResourceAvatars = mResources;
                mAvatarAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    private String getImagePreviewSmall(String url) {
        return AliImgSpec.POST_THUMB.makeUrl(url);
    }

    @Override
    public void onUnzipSuccess() {}

    @Override
    public void onUseResSuccess(IMResource res) {}

    @Override
    public void onUnuseResSuccess() {}

    @Override
    public void onAddResSuccess() {
        if (mCurrentResource != null) {
            updateData();
        }
    }

    private void updateData() {
        switch (mCurrentResource.type) {
            case IMResource.TYPE_BUBBLE:
                for (IMResource res : mImResourceBubbles) {
                    if (res == mCurrentResource) {
                        res.add = true;
                    }
                }
                mBubbleAdapter.notifyDataSetChanged();
                break;
            case IMResource.TYPE_EMOTION_PACKAGE:
                for (IMResource res : mImResourceEmotions) {
                    if (res == mCurrentResource) {
                        res.add = true;
                    }
                }
                mEmotionAdapter.notifyDataSetChanged();
                break;
            case IMResource.TYPE_CHARACTER_ACCESSORY:
                for (IMResource res : mImResourceAvatars) {
                    if (res == mCurrentResource) {
                        res.add = true;
                    }
                }
                mAvatarAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
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
                default:
                    break;
            }

        } else if (dialog == mShowInfoDialog) {
            if (mClickble) {
                openVip();
            }
        }
    
    }

    @Override
    public void onAddResFail(int code) {
        switch (code) {
            case MsResponse.MS_FAIL_IM_USER_RESOURCE_NOT_EXIST:
                showInfoDialog(getString(R.string.dressup_mall_the_res_not_exiest), false);
                break;
            case MsResponse.MS_FAIL_TRADE_PRICE_NOT_ENOUGH:
                showInfoDialog(getString(R.string.dressup_mall_the_money_low), false);
                break;
            case MsResponse.MS_FAIL_TRADE_CREDIT_NOT_ENOUGH:
                showInfoDialog(getString(R.string.dressup_mall_the_integral_low), false);
                break;
            case MsResponse.MS_FAIL_TRADE_RESOURCE_NEED_VIP:
                showInfoDialog(getString(R.string.dressup_mall_the_vip_can_use), true);
                break;
            default:
                break;
        }
    }
    private void showInfoDialog(String msg, boolean clickble) {
        if (mShowInfoDialog == null) {
            mShowInfoDialog = new LightDialog(this)
                .setTitleLd(R.string.dressup_mall_notice_info)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, this);
        }
        mClickble = clickble;
        mShowInfoDialog.setMessage(msg);
        mShowInfoDialog.show();
    }

}
