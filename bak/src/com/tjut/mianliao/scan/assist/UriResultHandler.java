package com.tjut.mianliao.scan.assist;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;

import com.google.zxing.client.result.ParsedResult;
import com.tjut.mianliao.BrowserActivity;
import com.tjut.mianliao.MianLiaoApp;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.scan.Scanner;
import com.tjut.mianliao.util.Utils;

public class UriResultHandler extends BaseResultHandler {
    
    public UriResultHandler(Activity activity, Scanner scanner, ParsedResult result) {
        super(activity, scanner, result);
    }

    @Override
    protected void setupButtons() {
        super.setupButtons();
        mMessageDialog.setPositiveButton(R.string.scan_open, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewUrl();
            }
        });
    }
    
    protected void viewUrl() {
        Intent intent = new Intent(mActivity, BrowserActivity.class);
        intent.putExtra(BrowserActivity.URL, getFinalVisUrl());
        mActivity.startActivity(intent);
    }

    private String getFinalVisUrl() {
        String url = mResult.getDisplayResult();
        StringBuilder sb = new StringBuilder(url);
        if (Utils.URL_PATTERN.matcher(url).matches()) {
            AccountInfo info = AccountInfo.getInstance(MianLiaoApp.getAppContext());
            sb.append("&token=").append(info.getToken()).append("&source=ml")
                .append("&uid=").append(String.valueOf(info.getUserId()));
        }
        return sb.toString();
    }
}
