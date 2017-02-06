package com.tjut.mianliao.explore;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.CommonBanner;
import com.tjut.mianliao.component.ProAvatarView;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.explore.VCenterListInfo;
import com.tjut.mianliao.im.AvatarMarketActivity;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class VipCenterActivity extends BaseActivity implements OnClickListener {

    private AccountInfo mAccountInfo;
    private UserInfo mUserInfo;
    private boolean mIsGetUserInfo;

    private TextView mTvOpenVip;
    private TextView mTvBecomeVip;
    private TextView mTvVipEndtime;
    private ProAvatarView mIvVipavatar;
    private ImageView mVipIcon;
    private CommonBanner mVsSwitcher;
    private long vipEndTime;
    
    private LinearLayout mLvHot, mLvIntro;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_vip_center;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(getString(R.string.pay_vip_center));
        mAccountInfo = AccountInfo.getInstance(this);
        mTvOpenVip = (TextView) findViewById(R.id.tv_open_vip);
        mTvBecomeVip = (TextView) findViewById(R.id.tv_become_vip);
        mIvVipavatar = (ProAvatarView) findViewById(R.id.iv_vip_avatar);
        mTvVipEndtime = (TextView) findViewById(R.id.tv_due_date);
        mVipIcon = (ImageView) findViewById(R.id.iv_vip_icon);
        mVsSwitcher = (CommonBanner) findViewById(R.id.vs_switcher);
        mVsSwitcher.setParam(CommonBanner.Plate.VipMain, 0);
        mLvHot = (LinearLayout) findViewById(R.id.lv_vip_function);
        mLvIntro = (LinearLayout) findViewById(R.id.lv_intro);
        getUserInfo();
        new GetVipCenterInfo().executeLong();
    }
    
    @Override
    protected void onResume() {
    	getUserInfo();
    	super.onResume();
    	
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_vip_open:
            case R.id.ll_become_vip:
                openVip(false);
                break;
            case R.id.rl_vip_year:
                openVip(true);
                break;
//            case R.id.rl_fun_introduce:
//                Intent intent = new Intent(VipCenterActivity.this,  );
            case R.id.ll_intro_info:
                VCenterListInfo info = (VCenterListInfo) v.getTag();
                viewWeb(info.id);
                break;
            default:
                break;
        }
    }

    private void updateView() {
        mTvOpenVip.setText(mUserInfo.vip ? getString(R.string.pay_vip_continue)
                : getString(R.string.pay_become_vip));
        mTvBecomeVip.setText(mUserInfo.vip ?  getString(R.string.pay_vip_continue)
                : getString(R.string.pay_become_vip));
        mVipIcon.setImageResource(mUserInfo.vip ? 
        		R.drawable.bottom_icon_smallcrown_on : R.drawable.bottom_icon_smallcrown_off);
    }

    private void getUserInfo() {
        new FetchUserTask().executeLong();
    }

    private class FetchUserTask extends MsTask {

        public FetchUserTask() {
            super(VipCenterActivity.this, MsRequest.USER_FULL_INFO);
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
                if (mUserInfo.vip) {
                    mTvOpenVip.setVisibility(View.GONE);
                    vipEndTime = mUserInfo.vipEndTime *1000;
                    mTvVipEndtime.setText(getString(R.string.vip_end_time_desc,
                            Utils.getTimeString(5, vipEndTime)));
                }
                mIvVipavatar.setImage(mUserInfo.getAvatar(), mUserInfo.defaultAvatar());
                mIsGetUserInfo = false;
                updateView();
            }
        }
    }

    private void openVip(boolean vipYear) {
        if (mIsGetUserInfo) {
            toast(R.string.vip_udpate_data_ing);
            return;
        }
        Intent intent = new Intent(this, BuyVipHomePageActivity.class);
        intent.putExtra(BuyVipHomePageActivity.EXT_PAY_YEAR, vipYear);
        intent.putExtra(BuyVipHomePageActivity.EXT_USER_INFO, mUserInfo);
        startActivity(intent);
    }
    
    private void viewWeb(int id) {
        String params = new StringBuilder("id=").append(id).toString();
        String url = HttpUtil.getUrl(this, MsRequest.IMRW_VIP_CENTER, params);
        Intent iMarket = new Intent(this, AvatarMarketActivity.class);
        iMarket.putExtra(AvatarMarketActivity.URL, url);
        startActivity(iMarket);
    }

    
    private class GetVipCenterInfo extends MsTask{

        public GetVipCenterInfo() {
            super(VipCenterActivity.this, MsRequest.IMUR_VIP_CENTER_LIST);
        }
     
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONObject json = response.getJsonObject();
                try {
                    ArrayList<VCenterListInfo> hotInfos = JsonUtil.getArray(
                            json.getJSONArray("hot"), VCenterListInfo.TRANSFORMER);
                    ArrayList<VCenterListInfo> introInfos = JsonUtil.getArray(
                            json.getJSONArray("ext"), VCenterListInfo.TRANSFORMER);
                    if (hotInfos != null) {
                        fillData(hotInfos, 1);
                    }
                    if (introInfos != null) {
                        fillData(introInfos, 2);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        
    }
    

    public void fillData(ArrayList<VCenterListInfo> infos, int type) {
        switch (type) {
            case 1:
                for (int i = 0; i < infos.size(); i++) {
                    mLvHot.addView(getView(infos.get(i), i == infos.size() - 1));
                }
                break;
            case 2:
                for (int i = 0; i < infos.size(); i++) {
                    mLvIntro.addView(getView(infos.get(i), i == infos.size() - 1));
                }
                break;
            default:
                break;
        }
    }

    private View getView(VCenterListInfo info, boolean showLine) {
        View view = mInflater.inflate(R.layout.list_item_vip_intro_info, null);
        TextView tvName = (TextView) view.findViewById(R.id.tv_name);
        TextView tvLabel = (TextView) view.findViewById(R.id.tv_label);
        tvName.setText(info.name);
        tvLabel.setText(info.label == null || "null".equals(info.label) ? "" : info.label);
        if (showLine) {
            view.findViewById(R.id.tl_line).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.tl_line).setVisibility(View.VISIBLE);
        }
        view.setTag(info);
        view.setOnClickListener(this);
        return view;
    }

}
