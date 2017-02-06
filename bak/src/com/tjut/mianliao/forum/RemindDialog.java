package com.tjut.mianliao.forum;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.settings.Settings;

public class RemindDialog extends Dialog {
    private Context mContext;
    private boolean mIsNightMode;
    private LinearLayout mLlRoamDialog;
    private TextView mDialogTitle, mDialogContent;
    private ImageView mDialogLogo;

    public RemindDialog(Context context) {
        this(context, R.style.Translucent_NoTitle);
    }

    public RemindDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
        mIsNightMode = Settings.getInstance(mContext).isNightMode();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_roamcollege_remind);
        mLlRoamDialog = (LinearLayout) findViewById(R.id.ll_roam_dialog);
        mDialogTitle = (TextView) findViewById(R.id.tv_rmdialog_title);
        mDialogContent = (TextView) findViewById(R.id.tv_rmdialog_content);
        mDialogLogo = (ImageView) findViewById(R.id.iv_rmdialog_logo);
        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        if (mIsNightMode) {
            mLlRoamDialog.setBackgroundResource(R.drawable.bg_roamcollege_dialog_black);
        } else {
            mLlRoamDialog.setBackgroundResource(R.drawable.bg_roamcollege_dialog);
        }
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    public void setDialogTitle(String mTitle) {
        mDialogTitle.setText(mTitle);
    }

    public void setDialogContent(String mContent) {
        mDialogContent.setText(mContent);
    }

    public void setDialogLogo(int mResId) {
        mDialogLogo.setVisibility(View.VISIBLE);
        mDialogLogo.setImageResource(mResId);
    }
    
    public void hideDialogIcon() {
        mDialogLogo.setVisibility(View.GONE);
    }
}
