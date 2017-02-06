package com.tjut.mianliao.scan.assist;

import android.app.Activity;
import android.content.DialogInterface;

import com.google.zxing.client.result.ParsedResult;
import com.tjut.mianliao.R;
import com.tjut.mianliao.scan.Scanner;
import com.tjut.mianliao.util.Utils;

public class EmailResultHandler extends BaseResultHandler {
    public EmailResultHandler(Activity activity, Scanner scanner, ParsedResult result) {
        super(activity, scanner, result);
    }

    @Override
    protected void setupButtons() {
        super.setupButtons();
        mMessageDialog.setPositiveButton(R.string.scan_send_email, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    Utils.actionSendTo(mActivity, mResult.getDisplayResult());
                }
            }
        });
    }
}
