package com.tjut.mianliao;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class ProgressDialog extends Dialog {
    
    private Context mContext;
    private TextView mTvContent;

    public ProgressDialog(Context context) {
        this(context, R.style.ProgressDialog);
    }

    public ProgressDialog(Context context, int Theme) {
        super(context, Theme);
        mContext = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_progress_dialog);
        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        mTvContent = (TextView) findViewById(R.id.tv_progress_content);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
    }

    public void setDialogContent(String mContent) {
        mTvContent.setText(mContent);
    }

    public void setDialogContent(int mResId) {
        mTvContent.setText(mContext.getString(mResId));
    }

    public void hideDialog() {
        dismiss();
    }

    public void setTextColor(int color) {
        mTvContent.setTextColor(color);
    }
}
