package com.tjut.mianliao.component;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.tjut.mianliao.R;

public class PushMessageDialog extends Dialog {
    private Context mContext;
    private int mButtonHeight;
    private boolean mAutoDismiss = true;
    public PushMessageDialog(Context context) {
        super(context);
        mContext = context;

        setContentView(R.layout.list_item_mess_dialog);
        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    @Override
    public void setTitle(CharSequence title) {
        ((TextView) findViewById(R.id.tv_title)).setText(title);
    }
    public PushMessageDialog setContent(CharSequence content) {
        ((TextView)findViewById(R.id.tv_content)).setText(content);
        return this;
    }

    public PushMessageDialog setSkipText(String text, final OnClickListener onPositiveClicked) {

        TextView mTvSkip = ((TextView)findViewById(R.id.tv_for_skip));
        mTvSkip.setText(text);
        mTvSkip.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (onPositiveClicked != null) {
                    onPositiveClicked.onClick(PushMessageDialog.this, BUTTON_POSITIVE);
                }
                if (mAutoDismiss) {
                    dismiss();
                }
            }
        });
        return this;
    }
}
