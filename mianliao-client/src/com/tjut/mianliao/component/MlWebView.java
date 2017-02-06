package com.tjut.mianliao.component;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MlWebView extends WebView {

    private WebViewListener mListener;

    public MlWebView(Context context) {
        this(context, null);
    }

    @SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
    public MlWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        
        setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        setWebViewClient(new MlWebViewClient());
    }

    public void setWebViewListener(WebViewListener listener) {
        
        mListener = listener;
    }

    private class MlWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return mListener == null ? false : mListener.shouldOverrideUrlLoading(url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (mListener != null) {
                mListener.onPageStarted(url);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (mListener != null) {
                mListener.onPageFinished(url);
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
        
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (mListener != null) {
                mListener.onPageReceivedError();
            }
        }
    };

    public interface WebViewListener {
        public boolean shouldOverrideUrlLoading(String url);
        public void onPageStarted(String url);
        public void onPageFinished(String url);
        public void onPageReceivedError();
    }
}
