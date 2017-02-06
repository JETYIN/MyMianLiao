package com.tjut.mianliao.component;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;

import org.w3c.dom.Text;

@SuppressLint("ResourceAsColor")
public class LiveXGDialog extends Dialog {

    private Context mContext;
    private TextView mTextview;
    private TextView bTextView;
    private LinearLayout mLinearLayout;
    private LinearLayout mRlContent;
    private boolean mAutoDismiss = true;

    public LiveXGDialog(Context context) {
        this(context, R.style.Translucent_NoTitle);
    }

    public LiveXGDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.item_live_wait);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        mTextview = (TextView) findViewById(R.id.title);

    }



    public void setText(String text) {
        mTextview.setText(text);
    }

    public LiveXGDialog setPositiveButton(String txt,
                                        final OnClickListener onPositiveClicked) {
        TextView positive = (TextView) findViewById(R.id.tv_know);
        if (txt != null && !"".equals(txt.trim())) {
            positive.setText(txt);
        }
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onPositiveClicked != null) {
                    onPositiveClicked
                            .onClick(LiveXGDialog.this, BUTTON_POSITIVE);
                }
                if (mAutoDismiss) {
                    dismiss();
                }
            }
        });
        return this;
    }

    public LiveXGDialog setButtonBackground(int buttonId, int resId) {
        switch (buttonId) {
            case BUTTON_NEGATIVE:
                findViewById(R.id.tv_cancel).setBackgroundResource(resId);
                break;
            case BUTTON_POSITIVE:
                findViewById(R.id.tv_know).setBackgroundResource(resId);
                break;
            default:
                break;
        }
        return this;
    }

    public LiveXGDialog setPositiveButton(int resId,
                                        OnClickListener onPositiveClicked) {
        return setPositiveButton(mContext.getString(resId), onPositiveClicked);
    }

    public LiveXGDialog setNegativeButton(String txt,
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
                            .onClick(LiveXGDialog.this, BUTTON_NEGATIVE);
                }
                if (mAutoDismiss) {
                    dismiss();
                }
            }
        });
        return this;
    }

    public LiveXGDialog setNegativeButton(int resId,
                                        OnClickListener onNegativeClicked) {
        return setNegativeButton(mContext.getString(resId), onNegativeClicked);
    }

    /**
     * @param autoDismiss If true, the dialog will be dismissed once user clicked
     *                    positive/negative button, or clicked on adapter item. Default
     *                    is true;
     */
    public LiveXGDialog setAutoDismiss(boolean autoDismiss) {
        mAutoDismiss = autoDismiss;
        return this;
    }

}
