package com.tjut.mianliao.promotion;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.forum.CourseForumActivity;
import com.tjut.mianliao.util.FileDownloader;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class PromotionActivity extends BaseActivity implements FileDownloader.Callback {

    private Promotion mPromotion;

    private String mImageUrl;
    private String mDesc;
    private Forum mForum;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_promotion;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPromotion = PromotionManager.getInstance(this).getPromotion();
        if (mPromotion == null) {
            toast(R.string.prom_no_promotion_found);
            finish();
            return;
        }

        getTitleBar().showTitleText(mPromotion.name, null);

        new GetPromotionTask(mPromotion.id).executeLong();
    }

    private void updateContent() {
        ((TextView) findViewById(R.id.tv_desc)).setText(mDesc);
        if (!TextUtils.isEmpty(mImageUrl)) {
            FileDownloader.getInstance(this).getFile(mImageUrl, this, true);
        }
        if (mForum != null) {
            findViewById(R.id.btn_join).setVisibility(View.VISIBLE);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_join:
                Intent ivf = new Intent(this, CourseForumActivity.class);
                ivf.putExtra(Forum.INTENT_EXTRA_NAME, mForum);
                startActivity(ivf);
                break;
            default:
                break;
        }
    }

    @Override
    public void onResult(boolean success, String url, String fileName) {
        if (success && url.equals(mImageUrl)) {
            ImageView ivProm = (ImageView) findViewById(R.id.iv_promotion);
            ivProm.setVisibility(View.VISIBLE);
            ivProm.setImageBitmap(Utils.fileToBitmap(fileName));
        }
    }

    private class GetPromotionTask extends MsTask {
        private int mPromId;

        public GetPromotionTask(int id) {
            super(getApplicationContext(), MsRequest.PROM_INFO);
            mPromId = id;
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected String buildParams() {
            return "id=" + mPromId;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (MsResponse.isSuccessful(response)) {
                JSONObject json = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);
                mImageUrl = json.optString("detail_image");
                mDesc = json.optString("info");
                mForum = Forum.fromJson(json.optJSONObject("forum"));
                updateContent();
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.prom_get_info_failed, response.code));
            }
        }
    }
}