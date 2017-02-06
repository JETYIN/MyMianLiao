package com.tjut.mianliao.scan.assist;

import android.app.Activity;
import android.content.DialogInterface;
import android.widget.Toast;

import com.google.zxing.client.result.ParsedResult;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.scan.Scanner;
import com.tjut.mianliao.util.Utils;

/**
 * Handles the scan result.
 */
public class BaseResultHandler implements DialogInterface.OnCancelListener {

    protected Activity mActivity;
    protected Scanner mScanner;
    protected LightDialog mMessageDialog;
    protected ParsedResult mResult;

    public BaseResultHandler(Activity activity, Scanner scanner, ParsedResult result) {
        mActivity = activity;
        mScanner = scanner;
        mResult = result;
    }

    public void handle() {
        if (mMessageDialog == null) {
            initMessageDialog();
        }
        mMessageDialog.show();
    }

    /**
     * Usually, it should be enough to overwrite setupButtons.
     */
    protected void initMessageDialog() {
        mMessageDialog = new LightDialog(mActivity);
        mMessageDialog.setTitle(R.string.scan_result);
        mMessageDialog.setOnCancelListener(this);
        mMessageDialog.setMessage(mResult.getDisplayResult());
        setupButtons();
    }

    protected void setupButtons() {
        mMessageDialog.setNegativeButton(R.string.scan_copy, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    Utils.copyToClipboard(mActivity, R.string.scan_clip_board_label,
                            mResult.getDisplayResult());
                    Toast.makeText(mActivity, R.string.clip_board_clipped, Toast.LENGTH_SHORT)
                            .show();
                    mScanner.start();
                }
            }
        });
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mScanner.start();
    }
}
