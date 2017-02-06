package com.tjut.mianliao;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class DayNightNoticeDialog extends BaseActivity{

    private TextView mBtnOk;
    
    @Override
    protected int getBaseLayoutResID() {
        return R.layout.dialog_day_night_notice;
    }
    
    @Override
    protected int getLayoutResID() {
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBtnOk = (TextView) findViewById(R.id.tv_ok);
        mBtnOk.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
