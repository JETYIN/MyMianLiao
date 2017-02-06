package com.tjut.mianliao.component;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;

@SuppressLint("ResourceAsColor")
public class LiveDialog extends Dialog {

    private Context mContext;
    private TextView mTextview;
    private TextView bTextView;
    private TextView rightTextView;
    private LinearLayout mLinearLayout;
    private LinearLayout mRlContent;
    private boolean mAutoDismiss = true;

    public LiveDialog(Context context) {
        this(context, R.style.Translucent_NoTitle);
    }

    public LiveDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.item_live_confirmation_dialog);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        mTextview = (TextView) findViewById(R.id.title);
        bTextView = (TextView) findViewById(R.id.tv_konw);
        mLinearLayout = (LinearLayout) findViewById(R.id.ll_bottom);
        rightTextView = (TextView) findViewById(R.id.tv_confirm);
    }

    public void setTVSee() {
        bTextView.setVisibility(View.VISIBLE);
        mLinearLayout.setVisibility(View.INVISIBLE);

    }

    public void setRightButtonText(String text) {
        rightTextView.setText(text);
    }

    public void getRightButtonColor(String color) {
        rightTextView.setTextColor(Color.parseColor("#99ffffff"));

    }

    public void setText(String text) {
        mTextview.setText(text);
    }

    public LiveDialog setPositiveButton(String txt,
                                        final OnClickListener onPositiveClicked) {
        TextView positive = (TextView) findViewById(R.id.tv_confirm);
        if (txt != null && !"".equals(txt.trim())) {
            positive.setText(txt);
        }
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onPositiveClicked != null) {
                    onPositiveClicked
                            .onClick(LiveDialog.this, BUTTON_POSITIVE);
                }
                if (mAutoDismiss) {
                    dismiss();
                }
            }
        });
        return this;
    }

    public LiveDialog setButtonBackground(int buttonId, int resId) {
        switch (buttonId) {
            case BUTTON_NEGATIVE:
                findViewById(R.id.tv_cancel).setBackgroundResource(resId);
                break;
            case BUTTON_POSITIVE:
                findViewById(R.id.tv_confirm).setBackgroundResource(resId);
                break;
            default:
                break;
        }
        return this;
    }

    public LiveDialog setPositiveButton(int resId,
                                        OnClickListener onPositiveClicked) {
        return setPositiveButton(mContext.getString(resId), onPositiveClicked);
    }

    public LiveDialog setNegativeButton(String txt,
                                        final OnClickListener onNegativeClicked) {
        TextView negative = (TextView) findViewById(R.id.tv_cancel);
        if (txt != null && !"".equals(txt.trim())) {
            negative.setText(txt);
        }
        negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onNegativeClicked != null) {
                    onNegativeClicked
                            .onClick(LiveDialog.this, BUTTON_NEGATIVE);
                }
                if (mAutoDismiss) {
                    dismiss();
                }
            }
        });
        return this;
    }

    public LiveDialog setNegativeButton(int resId,
                                        OnClickListener onNegativeClicked) {
        return setNegativeButton(mContext.getString(resId), onNegativeClicked);
    }

    /**
     * @param autoDismiss If true, the dialog will be dismissed once user clicked
     *                    positive/negative button, or clicked on adapter item. Default
     *                    is true;
     */
    public LiveDialog setAutoDismiss(boolean autoDismiss) {
        mAutoDismiss = autoDismiss;
        return this;
    }

}
