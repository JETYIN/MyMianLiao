package com.tjut.mianliao.scan.assist;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import com.google.zxing.client.result.ParsedResult;
import com.tjut.mianliao.R;
import com.tjut.mianliao.news.wicket.WicketActivity;
import com.tjut.mianliao.news.wicket.WicketHelper;
import com.tjut.mianliao.news.wicket.WicketRecord;
import com.tjut.mianliao.scan.ScanActivity;
import com.tjut.mianliao.scan.Scanner;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class TicketResultHandler extends BaseResultHandler {

    private MsResponse mResponse;
    private WicketRecord mRecord;

    private boolean mIsChecker = false;

    public TicketResultHandler(Activity activity, Scanner scanner, ParsedResult result) {
        super(activity, scanner, result);
        mIsChecker = activity.getPreferences(0).getBoolean(ScanActivity.SP_IS_CHECKER, false);
    }

    @Override
    public void handle() {
        if (!Utils.isNetworkAvailable(mActivity)) {
            mResponse = new MsResponse();
            mResponse.code = MsResponse.HTTP_NOT_CONNECTED;
            super.handle();
            return;
        }

        final ProgressDialog pd = ProgressDialog.show(mActivity, null,
                mActivity.getString(R.string.scan_handling), true);
        new AdvAsyncTask<Void, Void, MsResponse>() {
            @Override
            protected MsResponse doInBackground(Void... params) {
                return HttpUtil.msPost(mActivity, WicketHelper.TICKET_API, WicketHelper.REQ_CHECKIN,
                        "code=" + Utils.urlEncode(mResult.getDisplayResult()));
            }

            @Override
            protected void onPostExecute(MsResponse response) {
                pd.dismiss();
                mResponse = response;

                if (TextUtils.isEmpty(mResponse.response)) {
                    TicketResultHandler.super.handle();
                    return;
                }

                try {
                    JSONObject json = new JSONObject(mResponse.response);
                    if (json.has("is_checker")) {
                        boolean isChecker = json.getBoolean("is_checker");
                        if (mIsChecker != isChecker) {
                            mIsChecker = isChecker;
                            updateCheckerState();
                        }
                    }

                    mRecord = WicketRecord.fromJson(json);
                    if (mResponse.code == MsResponse.MS_SUCCESS) {
                        mRecord.ticket = mResult.getDisplayResult();
                        WicketHelper.getInstance(mActivity).add(mRecord);
                    }

                    TicketResultHandler.super.handle();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }.executeLong();
    }

    private void updateCheckerState() {
        mActivity.findViewById(R.id.btn_right).setVisibility(mIsChecker ? View.VISIBLE : View.GONE);
        mActivity.getPreferences(0).edit().putBoolean(ScanActivity.SP_IS_CHECKER, mIsChecker).commit();
    }

    @Override
    protected void initMessageDialog() {
        super.initMessageDialog();
        if (mIsChecker) {
            String desc;
            if (mResponse.code == MsResponse.MS_SUCCESS) {
                mMessageDialog.setTitle(R.string.tic_wicket_success);
                desc = mActivity.getString(R.string.tic_desc_success, mRecord.ticket, mRecord.newsTitle);
            } else {
                mMessageDialog.setTitle(R.string.tic_wicket_failed);
                String title = mRecord == null ? "" : mRecord.newsTitle;
                desc = mActivity.getString(R.string.tic_desc_failed, mResult.getDisplayResult(),
                        title, WicketHelper.getFailDesc(mActivity, mResponse.code, mRecord));
            }

            mMessageDialog.setMessage(desc);
        }
    }

    @Override
    protected void setupButtons() {
        super.setupButtons();
        if (mIsChecker) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_NEGATIVE:
                            mActivity.startActivity(new Intent(mActivity, WicketActivity.class));
                            break;
                        case DialogInterface.BUTTON_POSITIVE:
                            mScanner.start();
                            break;
                        default:
                            break;
                    }
                }
            };
            mMessageDialog.setNegativeButton(R.string.tic_view_records, listener);
            mMessageDialog.setPositiveButton(R.string.tic_continue, listener);
            if (mResponse.code != MsResponse.MS_SUCCESS) {
                mMessageDialog.setButtonBackground(DialogInterface.BUTTON_POSITIVE, R.drawable.selector_btn_red);
            }
        }
    }
}
