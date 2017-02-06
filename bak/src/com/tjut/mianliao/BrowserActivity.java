package com.tjut.mianliao;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import com.tjut.mianliao.component.MlWebView;
import com.tjut.mianliao.util.Utils;

public class BrowserActivity extends BaseActivity implements MlWebView.WebViewListener {

    public static final String TITLE = "title";
    public static final String URL = "url";

    protected MlWebView mBrowser;
    private String mTitle;
    private View mViewNotice;
    private FrameLayout mParentView;
    private boolean mDialogShow;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_browser;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String url = getIntent().getStringExtra(URL);
        if (TextUtils.isEmpty(url)) {
            toast(R.string.invalid_url);
            finish();
            return;
        }

        mTitle = getIntent().getStringExtra(TITLE);
        getTitleBar().showTitleText(mTitle, null);

        mParentView = (FrameLayout) findViewById(R.id.fl_content);
        mBrowser = (MlWebView) findViewById(R.id.wv_browser);
        mViewNotice = mInflater.inflate(R.layout.activity_no_internet, null);
        mBrowser.setWebViewListener(this);
        mBrowser.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (mBrowser.canGoBack()) {
            mBrowser.goBack();
        } else {
            super.onBackPressed();
            mBrowser.setWebViewListener(null);
        }
    }

    public void refresh() {
        mBrowser.reload();
    }

    @Override
    public boolean shouldOverrideUrlLoading(String url) {
        return false;
    }

    @Override
    public void onPageStarted(String url) {
        if (!mDialogShow) {
            Utils.showProgressDialog(this, R.string.cf_loading);
            mDialogShow = true;
        }
    }

    @Override
    public void onPageFinished(String url) {
        Utils.hidePgressDialog();
        updateTitle();
    }
    
    private void updateTitle() {
        if (TextUtils.isEmpty(mTitle) || mBrowser.canGoBack()) {
            getTitleBar().showTitleText(mBrowser.getTitle(), null);
        } else {
            getTitleBar().showTitleText(mTitle, null);
        }
    }

    @Override
    public void onPageReceivedError() {
        if (mParentView != null && mViewNotice != null) {
            mParentView.removeView(mViewNotice);
            mParentView.addView(mViewNotice);
        }
    }
}