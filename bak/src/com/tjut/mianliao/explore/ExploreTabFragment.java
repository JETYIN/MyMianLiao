package com.tjut.mianliao.explore;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.IMResource;
import com.tjut.mianliao.im.AvatarMarketActivity;
import com.tjut.mianliao.im.IMResourceManager;
import com.tjut.mianliao.im.IMResourceManager.IMResourceListener;
import com.tjut.mianliao.main.TabFragment;
import com.tjut.mianliao.scan.ScanActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.settings.SettingsActivity;
import com.tjut.mianliao.task.TaskActivity;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsRequest;
import com.umeng.analytics.MobclickAgent;

public class ExploreTabFragment extends TabFragment implements OnClickListener,
        IMResourceListener {

    private IMResourceManager mImResourceManager;
    private AccountInfo mAccountInfo;
    private Settings mSettings;
    private boolean mIsNightMode;
    private ScrollView mSvMain;

    private LinearLayout mLlActive, mLlDressUp, mLlVipCenter, mLlGame;

    @Override
    public int getLayoutId() {
        return R.layout.activity_explore_homepage;
    }

    @Override
    public int getNaviButtonId() {
        return R.id.nb_more;
    }

    @Override
    public String getName() {
        return "ExploreTabFragment";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImResourceManager = IMResourceManager.getInstance(getActivity());
        mImResourceManager.registerIMResourceListener(this);
        mAccountInfo = AccountInfo.getInstance(getActivity());
        getImRecourse();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mTitleBar.setTitle(getString(R.string.explore_title));
        mSettings = Settings.getInstance(getActivity());
        mIsNightMode = mSettings.isNightMode();

        mSvMain = (ScrollView) view.findViewById(R.id.sv_explore);

        view.findViewById(R.id.ll_scan).setOnClickListener(this);
        view.findViewById(R.id.ll_gold).setOnClickListener(this);
        view.findViewById(R.id.ll_task).setOnClickListener(this);
        view.findViewById(R.id.ll_setting).setOnClickListener(this);

        mLlActive = (LinearLayout) view.findViewById(R.id.ll_active);
        mLlDressUp = (LinearLayout) view.findViewById(R.id.ll_dressup);
        mLlVipCenter = (LinearLayout) view.findViewById(R.id.ll_vip_center);
        mLlGame = (LinearLayout) view.findViewById(R.id.ll_funny_game);

        mLlActive.setOnClickListener(this);
        mLlDressUp.setOnClickListener(this);
        mLlGame.setOnClickListener(this);
        mLlVipCenter.setOnClickListener(this);
        checkDayNightUI();
        return view;
    }


    private void getImRecourse() {
        mImResourceManager.GetMyImResources(IMResource.TYPE_EMOTION_PACKAGE);
    }
    
    private void updateBg(boolean mIsNight) {
        mLlActive.setBackgroundResource(mIsNight ? R.drawable.pic_bg_activity_center_black :
            R.drawable.pic_bg_activity_center);
        mLlDressUp.setBackgroundResource(mIsNight ? R.drawable.pic_bg_dress_up_black :
            R.drawable.pic_bg_dress_up);
        mLlVipCenter.setBackgroundResource(mIsNight ? R.drawable.pic_bg_vip_center_black :
            R.drawable.pic_bg_vip_center);
        mLlGame.setBackgroundResource(mIsNight ? R.drawable.pic_bg_game_center_black :
            R.drawable.pic_bg_game_center);
    }

	private void checkDayNightUI() {
		updateBg(mIsNightMode);
        if (mIsNightMode) {
            mSvMain.setBackgroundResource(R.drawable.bg);
        }
	}

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_scan:
                startActivity(ScanActivity.class);
                MobclickAgent.onEvent(getActivity(), MStaticInterface.SIGNATURE);
                break;
            case R.id.ll_gold:
                startActivity(GoldDepositsActivity.class);
                MobclickAgent.onEvent(getActivity(), MStaticInterface.COIN_RECHARGE);
                break;
            case R.id.ll_active:
                viewMarket(MsRequest.IMRW_ACTIVITY);
                MobclickAgent.onEvent(getActivity(), MStaticInterface.ACTIVITY_AREA);
                break;
            case R.id.ll_vip_center:
                startActivity(VipCenterActivity.class);
                MobclickAgent.onEvent(getActivity(), MStaticInterface.MEMBER);
                break;
            case R.id.ll_setting:
                startActivity(SettingsActivity.class);
                break;

            case R.id.ll_task:
                startActivity(TaskActivity.class);
                MobclickAgent.onEvent(getActivity(), MStaticInterface.TASK_CENTER);

                break;
            case R.id.ll_dressup:
                startActivity(DressUpMallActivty.class);
                MobclickAgent.onEvent(getActivity(), MStaticInterface.SHOP);
                break;
            case R.id.ll_funny_game:
//                startActivity(FunnyGameActivity.class);
                viewMarket(MsRequest.IMRW_GAME);
                MobclickAgent.onEvent(getActivity(), MStaticInterface.GAME);
                break;
            default:
                break;
        }
    }

    private void viewMarket(MsRequest request) {
        Intent iMarket = new Intent(getActivity(), AvatarMarketActivity.class);
        String url = HttpUtil.getUrl(getActivity(), request, "");
        iMarket.putExtra(AvatarMarketActivity.URL, url);
        startActivity(iMarket);
    }

    @Override
    public void onUsingResUpdated(int userId, int type, IMResource res) {}

    @Override
    public void onGetImResourceSuccess(int type, int requestCode, ArrayList<IMResource> mResources) {
        if (type == IMResource.TYPE_EMOTION_PACKAGE) {
            if (mResources != null && mResources.size() > 0) {
                for (IMResource mResource : mResources) {
                    mImResourceManager.UnzipFile(mResource);
                }
            }
        }      
    }

    @Override
    public void onUnzipSuccess() {}

    @Override
    public void onUseResSuccess(IMResource res) {}

    @Override
    public void onUnuseResSuccess() {}

    @Override
    public void onAddResSuccess() {}

    @Override
    public void onAddResFail(int code) {}
}
