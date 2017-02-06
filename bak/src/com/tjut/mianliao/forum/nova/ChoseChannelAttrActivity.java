package com.tjut.mianliao.forum.nova;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.forum.CfPost;

public class ChoseChannelAttrActivity extends BaseActivity implements OnClickListener {

    public static final String EXT_CHANNEL_TYPE = "ext_channel_type";
    private static final int REQUEST_CODE = 100;

    private TextView mTvVoice, mTvPic, mTvText;
    private int mType;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_chose_channel_attribute;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle("频道属性");
        mTvVoice = (TextView) findViewById(R.id.tv_voice);
        mTvPic = (TextView) findViewById(R.id.tv_pic_text);
        mTvText = (TextView) findViewById(R.id.tv_text);
        mTvVoice.setOnClickListener(this);
        mTvPic.setOnClickListener(this);
        mTvText.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_voice:
                mType = CfPost.THREAD_TYPE_PIC_VOICE;
                startCreate();
                break;
            case R.id.tv_pic_text:
                mType = CfPost.THREAD_TYPE_PIC_TXT;
                startCreate();
                break;
            case R.id.tv_text:
                mType = CfPost.THREAD_TYPE_TXT;
                startCreate();
                break;

            default:
                break;
        }
    }

    private void startCreate() {
        Intent intent = new Intent(this, StartCreateChannelActivity.class);
        intent.putExtra(EXT_CHANNEL_TYPE, mType);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_DELETED) {
            setResult(RESULT_DELETED);
            this.finish();
        }
    }

}
