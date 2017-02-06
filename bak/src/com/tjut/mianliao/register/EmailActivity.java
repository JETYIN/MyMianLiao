package com.tjut.mianliao.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.util.Utils;

public class EmailActivity extends BaseActivity{

    private TextView mSendEmail;
    private String mEmail;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_email;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSendEmail = (TextView) findViewById(R.id.tv_to_email);
        Intent intent = getIntent();
        mEmail = intent.getStringExtra("mEmail");
        mSendEmail.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Utils.actionSendTo(EmailActivity.this, mEmail);
            }
        });
    }

}
