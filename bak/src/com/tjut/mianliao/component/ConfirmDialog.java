package com.tjut.mianliao.component;

import android.content.Context;
import android.content.DialogInterface;

import com.tjut.mianliao.R;

public class ConfirmDialog {

    private ConfirmDialog() {}

    public static void show(Context context, int msg, final Runnable action) {
        show(context, R.string.confirm, msg, action);
    }

    public static void show(Context context, int title, int msg, final Runnable action) {
        new LightDialog(context)
                .setTitleLd(title)
                .setMessage(msg)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        action.run();
                    }
                })
                .show();
    }
}
