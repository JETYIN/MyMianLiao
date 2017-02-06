package com.tjut.mianliao.news;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.CardItem;
import com.tjut.mianliao.data.NewsSource;
import com.tjut.mianliao.util.Utils;

public class NewsSourceAboutActivity extends BaseActivity implements OnClickListener {

    private CardItem mCiPhone;
    private CardItem mCiEmail;
    private CardItem mCiWeb;

    private NewsSource mSource;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_news_source_about;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSource = getIntent().getParcelableExtra(NewsSource.INTENT_EXTRA_NAME);
        if (mSource == null) {
            toast(R.string.news_source_tst_not_found);
            finish();
            return;
        }

        getTitleBar().showTitleText(R.string.news_source_about, null);

        CardItem ci = (CardItem) findViewById(R.id.ci_about);
        ci.setContent(mSource.description);

        ci = (CardItem) findViewById(R.id.ci_phone);
        ci.setContent(mSource.phone);
        if (TextUtils.isEmpty(mSource.phone)) {
            ci.setButtonVisibility(View.GONE);
        } else {
            ci.setButtonListener(this);
        }
        mCiPhone = ci;

        ci = (CardItem) findViewById(R.id.ci_email);
        ci.setContent(mSource.email);
        if (TextUtils.isEmpty(mSource.email)) {
            ci.setButtonVisibility(View.GONE);
        } else {
            ci.setButtonListener(this);
        }
        mCiEmail = ci;

        ci = (CardItem) findViewById(R.id.ci_web);
        ci.setContent(mSource.web);
        if (TextUtils.isEmpty(mSource.web)) {
            ci.setButtonVisibility(View.GONE);
        } else {
            ci.setButtonListener(this);
        }
        mCiWeb = ci;

        ci = (CardItem) findViewById(R.id.ci_address);
        ci.setContent(mSource.address);
    }

    @Override
    public void onClick(View v) {
        if (v == mCiPhone.getButton()) {
            Utils.actionCall(this, mSource.phone);
        } else if (v == mCiEmail.getButton()) {
            Utils.actionSendTo(this, mSource.email);
        } else if (v == mCiWeb.getButton()) {
            Uri webUri = Uri.parse(mSource.web);
            if (webUri.getScheme() == null) {
                webUri = Uri.parse("http://" + mSource.web);
            }
            Utils.actionView(this, webUri, null, 0);
        }
    }
}
