package com.tjut.mianliao;

import android.os.Bundle;

import com.tjut.mianliao.component.MlWebView;

public class SimpleWebActivity extends BaseActivity {

    private MlWebView mWebView;
    public static final String EXTRA_URL = "extra_url";
    private String mUrl;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_simple_web;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUrl = getIntent().getStringExtra(EXTRA_URL);
        this.fillComponents();
    }

    private void fillComponents() {
        mWebView = (MlWebView) this.findViewById(R.id.wv_browser);
        mWebView.loadUrl(mUrl);
    }

}
