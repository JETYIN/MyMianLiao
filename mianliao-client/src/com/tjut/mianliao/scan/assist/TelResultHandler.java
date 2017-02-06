package com.tjut.mianliao.scan.assist;

import android.app.Activity;
import android.content.DialogInterface;

import com.google.zxing.client.result.ParsedResult;
import com.tjut.mianliao.R;
import com.tjut.mianliao.scan.Scanner;
import com.tjut.mianliao.util.Utils;

public class TelResultHandler extends BaseResultHandler {
    public TelResultHandler(Activity activity, Scanner scanner, ParsedResult result) {
        super(activity, scanner, result);
    }

    @Override
    protected void setupButtons() {
        super.setupButtons();
        mMessageDialog.setPositiveButton(R.string.scan_call, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Utils.actionCall(mActivity, mResult.getDisplayResult());
            }
        });
    }
}
