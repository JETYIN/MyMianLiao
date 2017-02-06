package com.tjut.mianliao.forum;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.RefFriendActivity;
import com.tjut.mianliao.component.EmotionPicker;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.Emotion;
import com.tjut.mianliao.util.Utils;

public class ReplyActivity extends BaseActivity implements EmotionPicker.EmotionListener {

    public static final String EXTRA_TITLE_RES = "extra_title";
    public static final String EXTRA_RESULT = "extra_result";

    private EditText mEtDesc;
    private CheckBox mCbEmotion;
    private EmotionPicker mEmotionPicker;

    private LightDialog mDiscardDialog;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_reply;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(
                getIntent().getIntExtra(EXTRA_TITLE_RES, R.string.rpl_title), null);

        mEmotionPicker = (EmotionPicker) findViewById(R.id.ep_emotions);
        mEmotionPicker.setEmotionListener(this);
        mCbEmotion = (CheckBox) findViewById(R.id.cb_input_emotion);
        findViewById(R.id.iv_input_attach).setVisibility(View.GONE);
        findViewById(R.id.iv_input_image).setVisibility(View.GONE);

        mEtDesc = (EditText) findViewById(R.id.et_desc);
        mEtDesc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideEmoPicker();
                }
                mEtDesc.setText(Utils.getRefFriendText(
                        mEtDesc.getText(), getApplicationContext()));
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.et_desc:
                hideEmoPicker();
                break;
            case R.id.iv_input_ref:
                startActivityForResult(new Intent(this, RefFriendActivity.class), 0);
                hideEmoPicker();
                break;
            case R.id.cb_input_emotion:
                Utils.toggleInput(mEtDesc, mEmotionPicker);
                break;
            case R.id.btn_submit:
                formatDesc();
                String result = mEtDesc.getText().toString().trim();
                if (TextUtils.isEmpty(result)) {
                    toast(R.string.rpl_tst_content_empty);
                } else {
                    Intent data = new Intent().putExtra(EXTRA_RESULT, result);
                    setResult(RESULT_OK, data);
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mEmotionPicker.isVisible()) {
            hideEmoPicker();
            return;
        }

        String result = mEtDesc.getText().toString().trim();
        if (TextUtils.isEmpty(result)) {
            super.onBackPressed();
        } else {
            showDiscardDialog();
        }
    }

    private void showDiscardDialog() {
        if (mDiscardDialog == null) {
            mDiscardDialog = new LightDialog(this)
                    .setTitleLd(R.string.qa_discard_title)
                    .setMessage(R.string.qa_discard_message)
                    .setNegativeButton(R.string.qa_discard_continue, null)
                    .setPositiveButton(R.string.qa_discard_quit,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                    .setButtonBackground(DialogInterface.BUTTON_POSITIVE,
                            R.drawable.selector_btn_red);
        }
        mDiscardDialog.show();
    }

    private void formatDesc() {
        int ss = mEtDesc.getSelectionStart();
        mEtDesc.setText(Utils.getRefFriendText(mEtDesc.getText(), this));
        mEtDesc.setSelection(ss);
    }

    private void hideEmoPicker() {
        mEmotionPicker.setVisible(false);
        mCbEmotion.setChecked(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String refs = data.getStringExtra(RefFriendActivity.EXTRA_RESULT);
            int ss = mEtDesc.getSelectionStart();
            mEtDesc.getText().replace(ss, ss, refs);
            formatDesc();
        }
    }

    @Override
    public void onEmotionClicked(Emotion emotion) {
        mEtDesc.getText().insert(
                mEtDesc.getSelectionStart(), emotion.getSpannable(this));
    }

    @Override
    public void onBackspaceClicked() {
        Utils.dispatchDelEvent(mEtDesc);
    }
}
