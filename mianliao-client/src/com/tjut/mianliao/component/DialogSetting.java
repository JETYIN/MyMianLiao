package com.tjut.mianliao.component;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;

public class DialogSetting extends SettingItem implements DialogInterface.OnClickListener {

    private LightDialog mDialog;

    private Listener mListener;

    public DialogSetting(Context context) {
        this(context, null);
    }

    public DialogSetting(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which != DialogInterface.BUTTON_NEGATIVE && mListener != null) {
            mListener.onCloseDialog(this, which);
        }
    }

    @Override
    protected int getWidgetLayoutResID() {
        return 0;
    }

    @Override
    protected void onSettingKey() {
    }

    @Override
    protected void onSettingItemClick() {
        if (mDialog == null) {
            mDialog = new LightDialog(getContext());
            mDialog.setTitle(mTitle.getText());
            mDialog.setNegativeButton(android.R.string.cancel, this);
            if (mListener != null) {
                mListener.onCreateDialog(this, mDialog);
            }
        }
        if (mListener != null) {
            mListener.onBindDialog(this, mDialog);
        }
        mDialog.show();
    }

    public interface Listener {
        void onCreateDialog(DialogSetting ds, LightDialog dialog);
        void onBindDialog(DialogSetting ds, LightDialog dialog);
        void onCloseDialog(DialogSetting ds, int which);
    }
}
