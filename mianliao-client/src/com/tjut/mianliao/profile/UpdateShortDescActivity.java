package com.tjut.mianliao.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class UpdateShortDescActivity extends BaseActivity implements TextWatcher, OnClickListener {

    public static final String EXT_USER_DES = "ext_user_des";
    
    private EditText mEtContent;
    private TextView mTvCount;
    private ImageView mIvClear;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_update_desc;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String shortDes = getIntent().getStringExtra(EXT_USER_DES);
        getTitleBar().setTitle(R.string.prof_short_desc);
        getTitleBar().showRightText(R.string.prof_save, this);
        mEtContent = (EditText) findViewById(R.id.et_content);
        mTvCount = (TextView) findViewById(R.id.tv_size_count);
//        mIvClear = (ImageView) findViewById(R.id.iv_clear);
        mEtContent.addTextChangedListener(this);
//        mIvClear.setOnClickListener(this);
        if (shortDes != null) {
            mEtContent.setText(shortDes);
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}


    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mTvCount.setText(getString(R.string.prof_update_desc_count_style, s.length()));
    }


    @Override
    public void afterTextChanged(Editable s) {}


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                new UpdateShortDescTask(getDesc()).executeLong();
                break;
            case R.id.iv_clear:
                clear();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        quit();
        super.onBackPressed();
    }

    private String getDesc() {
        return mEtContent.getText().toString().trim();
    }

    private void clear() {
        mEtContent.setText("");
        mTvCount.setText(getString(R.string.prof_update_desc_count_style, 0));
    }

    private void quit() {
        Intent data = new Intent();
        data.putExtra(NewProfileActivity.EXTRA_USER_DESC, getDesc());
        setResult(RESULT_UPDATED, data);
        finish();
    }

    private class UpdateShortDescTask extends MsTask{

        private String mDesc;

        public UpdateShortDescTask(String desc) {
            super(UpdateShortDescActivity.this, MsRequest.UPDATE_PROFILE);
            mDesc = desc;
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected String buildParams() {
            return "short_desc=" + Utils.urlEncode(mDesc);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (response.isSuccessful()) {
                quit();
            } else {
                toast(UpdateShortDescActivity.this.getString(R.string.prof_change_failed_personal));
            }
        }

    }
}
