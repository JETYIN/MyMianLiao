package com.tjut.mianliao.chat;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import com.tjut.mianliao.R;

public class RecordingDialog extends Dialog {

    public RecordingDialog(Context context, int theme) {
        super(context, theme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_recording);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

}
