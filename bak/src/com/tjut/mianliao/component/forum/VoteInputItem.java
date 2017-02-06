package com.tjut.mianliao.component.forum;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.tjut.mianliao.R;

public class VoteInputItem extends RelativeLayout implements View.OnClickListener {

    private String mOption;
    private EditText mEtOption;
    private ViewGroup mHolder;

    public VoteInputItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.list_item_vote_input, this, true);

        mEtOption = (EditText) findViewById(R.id.et_option);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VoteInputItem);
            boolean showDelete = ta.getBoolean(R.styleable.VoteInputItem_showDelete, true);
            ta.recycle();
            findViewById(R.id.iv_delete).setVisibility(showDelete ? VISIBLE : GONE);
        }
    }

    public VoteInputItem setHolderRef(ViewGroup holder) {
        mHolder = holder;
        return this;
    }

    public boolean hasUpdate() {
        if (TextUtils.isEmpty(mOption)) {
            return !TextUtils.isEmpty(mEtOption.getText());
        } else {
            return !mOption.equals(mEtOption.getText().toString());
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        mEtOption.setEnabled(enabled);
        findViewById(R.id.iv_delete).setVisibility(enabled ? VISIBLE : GONE);
    }

    public boolean isEmpty() {
        String text = mEtOption.getText().toString();
        return TextUtils.isEmpty(text) || text.replace(" ", "").length() == 0;
    }

    public String getOption() {
        return mEtOption.getText().toString();
    }

    public void setOption(String option) {
        mOption = option;
        mEtOption.setText(mOption);
    }

    @Override
    public void onClick(View v) {
        if (mHolder != null) {
            mHolder.removeView(this);
            mHolder = null;
        }
    }
}
