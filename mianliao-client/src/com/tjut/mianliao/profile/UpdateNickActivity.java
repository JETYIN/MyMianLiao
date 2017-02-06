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

public class UpdateNickActivity extends BaseActivity implements TextWatcher, OnClickListener {
    
    public static final String EXT_USER_NICK = "ext_user_nick";

    private EditText mEtContent;
    private TextView mTvCount;
    private ImageView mIvClear;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_update_nick;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(R.string.prof_nick_name);
        getTitleBar().showRightText(R.string.prof_save, this);
        mEtContent = (EditText) findViewById(R.id.et_content);
        mTvCount = (TextView) findViewById(R.id.tv_size_count);
        mIvClear = (ImageView) findViewById(R.id.iv_clear);
        mEtContent.addTextChangedListener(this);
        mIvClear.setOnClickListener(this);
        String nick = getIntent().getStringExtra(EXT_USER_NICK);
        if (nick != null) {
            mEtContent.setText(nick);
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}


    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mTvCount.setText(getString(R.string.prof_update_nick_count_style, s.length()));
    }


    @Override
    public void afterTextChanged(Editable s) {}


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                if (isReady()) {
                    if (Utils.isHasAt(getNick()) || Utils.isHasTopic(getNick())) {
                        toast(R.string.reg_format_nickname_puppet);
                    } else {
                        new UpdateNickTask(getNick()).executeLong();
                    }
                } else {
                    toast(R.string.prof_update_nick_too_short);
                }
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
        clear();
        quit();
        super.onBackPressed();
    }

    private String getNick() {
        return mEtContent.getText().toString().trim();
    }

    private boolean isReady() {
        return getNick().length() > 1;
    }

    private void clear() {
        mEtContent.setText("");
        mTvCount.setText(getString(R.string.prof_update_nick_count_style, 0));
    }

    private void quit() {
        Intent data = new Intent();
        data.putExtra(NewProfileActivity.EXTRA_NICK_NAME, getNick());
        setResult(RESULT_UPDATED, data);
        finish();
    }

    private class UpdateNickTask extends MsTask{

        private String mNick;

        public UpdateNickTask(String nick) {
            super(UpdateNickActivity.this, MsRequest.UPDATE_PROFILE);
            mNick = nick;
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected String buildParams() {
            return "nick=" + Utils.urlEncode(mNick);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (response.isSuccessful()) {
                quit();
            } else {
                toast(UpdateNickActivity.this.getString(R.string.prof_chage_nickname_failed));
            }
        }

    }
}
