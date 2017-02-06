package com.tjut.mianliao.scan.assist;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.text.TextUtils;

import com.google.zxing.client.result.ParsedResult;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.scan.Scanner;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class UsaHandler extends BaseResultHandler {

    private String mGuid;

    public UsaHandler(Activity activity, Scanner scanner,
            ParsedResult result) {
        super(activity, scanner, result);
    }

    @Override
    public void handle() {
        if (!Utils.isNetworkAvailable(mActivity)) {
            showResultDialog(mActivity.getString(R.string.prom_get_info_failed),
                    mActivity.getString(R.string.disconnected));
            return;
        }
        mGuid = mResult.getDisplayResult().replace(MlParser.URI_PREFIX_USA, "");
        new PregrabTask().executeLong();
    }

    private void showGrabDialog(String title, String desc, boolean enabled) {
        LightDialog grabDialog = new LightDialog(mActivity);
        grabDialog.setOnCancelListener(this);
        if (enabled) {
            grabDialog.setNegativeButton(android.R.string.cancel, mBaseListener);
            grabDialog.setPositiveButton(android.R.string.ok, new DialogInterface
                    .OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new GrabTask().executeLong();
                }
            });
        } else {
            grabDialog.setNegativeButton(android.R.string.ok, mBaseListener);
        }
        grabDialog.setTitle(title);
        grabDialog.setMessage(desc);
        grabDialog.show();
    }

    private void showResultDialog(String title, String info) {
        LightDialog resultDialog = new LightDialog(mActivity);
        resultDialog.setOnCancelListener(this);
        resultDialog.setTitle(title);
        resultDialog.setMessage(info);
        resultDialog.setPositiveButton(android.R.string.ok, mBaseListener);
        resultDialog.show();
    }

    private DialogInterface.OnClickListener mBaseListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mScanner.start();
        }
    };

    private class PregrabTask extends MsTask {
        private ProgressDialog mPd;

        public PregrabTask() {
            super(mActivity, MsRequest.USA_PREGRAB);
        }

        @Override
        protected String buildParams() {
            return "guid=" + Utils.urlEncode(mGuid);
        }

        @Override
        protected void onPreExecute() {
            mPd = ProgressDialog.show(mActivity, null,
                    mActivity.getString(R.string.scan_handling), true);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            mPd.dismiss();
            if (MsResponse.isSuccessful(response)) {
                JSONObject json = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);
                String title = json.optString("name");
                String desc = json.optString("info");
                String tip = json.optString("tip");
                if (!TextUtils.isEmpty(tip)) {
                    desc += "\n\n" + tip;
                }
                boolean enabled = json.optInt("en") == 1;
                showGrabDialog(title, desc, enabled);
            } else {
                showResultDialog(mActivity.getString(R.string.prom_get_info_failed),
                        MsResponse.getFailureDesc(mActivity, response.code));
            }
        }

    }

    private class GrabTask extends MsTask {
        private ProgressDialog mPd;

        public GrabTask() {
            super(mActivity, MsRequest.USA_GRAB);
        }

        @Override
        protected void onPreExecute() {
            mPd = ProgressDialog.show(mActivity, null,
                    mActivity.getString(R.string.scan_handling), true);
        }

        @Override
        protected String buildParams() {
            return "guid=" + Utils.urlEncode(mGuid);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPd.dismiss();
            if (MsResponse.isSuccessful(response)) {
                JSONObject json = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);
                String title = json.optString("name");
                String tip = json.optString("tip");
                showResultDialog(title, tip);
            } else {
                showResultDialog(mActivity.getString(R.string.prom_get_info_failed),
                        MsResponse.getFailureDesc(mActivity, response.code));
            }
        }
    }
}
