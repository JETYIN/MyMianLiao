package com.tjut.mianliao.explore;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.IMResource;
import com.tjut.mianliao.data.explore.EmotionsInfo;
import com.tjut.mianliao.im.IMResourceManager;
import com.tjut.mianliao.im.IMResourceManager.IMResourceListener;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.Utils;

public class EmotionManagementActivity extends BaseActivity implements IMResourceListener,
        OnClickListener {

    private LinearLayout mLvUsing, mLvCanAdd;
    private IMResourceManager mResourceManager;
    private AccountInfo mAccountInfo;
    private ArrayList<IMResource> mMyUsingRes;
    private ArrayList<IMResource> mMyCanUseRes;
    private IMResource mResource;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_emotion_management;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = Settings.getInstance(this);

        getTitleBar().setTitle(getString(R.string.explore_emotion_mine));
        mLvUsing = (LinearLayout) findViewById(R.id.lv_using);
        mLvCanAdd = (LinearLayout) findViewById(R.id.lv_can_add);
        mResourceManager = IMResourceManager.getInstance(this);
        mResourceManager.registerIMResourceListener(this);
        mAccountInfo = AccountInfo.getInstance(this);
        mMyUsingRes = new ArrayList<IMResource>();
        mMyCanUseRes = new ArrayList<IMResource>();
        getImResources();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mResourceManager.unregisterIMResourceListener(this);
        mMyUsingRes = null;
        mMyCanUseRes = null;
    }

    private void getImResources() {
        mResourceManager.GetMyUsingResource(
                IMResource.TYPE_EMOTION_PACKAGE, mAccountInfo.getUserId());
        mResourceManager.GetMyImResources(IMResource.TYPE_EMOTION_PACKAGE);
    }

    private void useResource(int id) {
        mResourceManager.UseImResource(id);
    }

    private void unuseResource(int id) {
        mResourceManager.UnseImResource(id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_oper:
                mResource = (IMResource) v.getTag();
                if (mResource.use) {
                    unuseResource(mResource.id);
                } else {
                    useResource(mResource.id);
                }
                getTitleBar().showProgress();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getImResources();
    }

    @Override
    public void onAddResSuccess() {
        mResourceManager.UnzipFile(mResource);
    }

    @Override
    public void onUsingResUpdated(int userId, int type, IMResource res) {}

    @Override
    public void onGetImResourceSuccess(int type, int requestCode, ArrayList<IMResource> mResources) {
        if (requestCode == IMResource.TYPE_EMOTION_PACKAGE) {
            mMyUsingRes = mResources;
        } else if (type == IMResource.TYPE_EMOTION_PACKAGE) {
            mResources.removeAll(mMyUsingRes);
            mMyCanUseRes = mResources;
        }
        fillData();
    }

    @Override
    public void onUnzipSuccess() {}

    @Override
    public void onUseResSuccess(IMResource res) {
        getTitleBar().hideProgress();
        mMyUsingRes.add(res);
        mMyCanUseRes.remove(res);
        fillData();
        EmotionsInfo emotionInfo = new EmotionsInfo(res.name, res.urls[0][0], 
                null, Utils.getElementUnzipfilePath(res.urls[0][0]), res.use);
        DataHelper.updateEmotionInfo(this, emotionInfo);
    }

    @Override
    public void onUnuseResSuccess() {
        getTitleBar().hideProgress();
        mResource.use = false;
        mMyUsingRes.remove(mResource);
        mMyCanUseRes.add(mResource);
        fillData();
        EmotionsInfo emotionInfo = new EmotionsInfo(mResource.name, mResource.urls[0][0],
                null, Utils.getElementUnzipfilePath(mResource.urls[0][0]), false);
        DataHelper.updateEmotionInfo(this, emotionInfo);
    }
    
    private void fillData() {
        mLvCanAdd.removeAllViews();
        mLvUsing.removeAllViews();
        for (IMResource resource : mMyUsingRes) {
            mLvUsing.addView(getUsingView(resource));
        }
        for (IMResource resource : mMyCanUseRes) {
            mLvCanAdd.addView(getUseView(resource));
        }
    }

    private View getUsingView(IMResource res) {
        View view = mInflater.inflate(R.layout.list_item_emotion_management, null);
        ProImageView pivIcon = (ProImageView) view.findViewById(R.id.lv_emotion_icon);
        TextView tvName = (TextView) view.findViewById(R.id.tv_emotion_name);
        TextView tvIntro = (TextView) view.findViewById(R.id.tv_emotion_intro);
        TextView tvOper = (TextView) view.findViewById(R.id.tv_oper);
        tvOper.setTag(res);
        tvOper.setOnClickListener(EmotionManagementActivity.this);
        pivIcon.setImage(res.preview, R.drawable.bg_img_loading);
        tvName.setText(res.name);
        tvIntro.setText(res.intro);
        return view;
    }
    
    private View getUseView(IMResource res) {
        View view = mInflater.inflate(R.layout.list_item_emotion_management, null);
        ProImageView pivIcon = (ProImageView) view.findViewById(R.id.lv_emotion_icon);
        TextView tvName = (TextView) view.findViewById(R.id.tv_emotion_name);
        TextView tvIntro = (TextView) view.findViewById(R.id.tv_emotion_intro);
        TextView tvOper = (TextView) view.findViewById(R.id.tv_oper);
        tvOper.setTag(res);
        tvOper.setOnClickListener(EmotionManagementActivity.this);
        pivIcon.setImage(res.preview, R.drawable.bg_img_loading);
        tvName.setText(res.name);
        tvIntro.setText(res.intro);
        tvOper.setBackgroundResource(R.drawable.bg_btn_emotion_add);
        tvOper.setTextColor(0XFF32BBBC);
        tvOper.setText(R.string.explore_add_to_using);
        return view;
    }

    @Override
    public void onAddResFail(int code) {}

}
