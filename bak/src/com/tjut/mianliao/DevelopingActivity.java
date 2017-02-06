package com.tjut.mianliao;

import android.os.Bundle;

public class DevelopingActivity extends BaseActivity {

    public static final String EXTRA_TITLE = "title";

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_developing;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        getTitleBar().showTitleText(title, null);
    }
}