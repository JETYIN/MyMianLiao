package com.tjut.mianliao;

import java.util.Observable;
import java.util.Observer;

import android.os.Bundle;
import android.view.View;

import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class InactiveActivity extends BaseActivity implements Observer {
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_inactive;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.us_inactive_title, null);
        getTitleBar().hideLeftButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
        UserState.getInstance().addObserver(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        UserState.getInstance().deleteObserver(this);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void checkState(View v) {
        new CheckStateTask().executeLong();
    }

    private void checkUserState(int code) {
        if (code == UserState.NORMAL) {
            Utils.logD(getTag(), "checkUserState:" + code);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        checkUserState((Integer) data);
    }

    private class CheckStateTask extends MsTask {

        public CheckStateTask() {
            super(getApplicationContext(), MsRequest.PING);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.btn_check).setEnabled(false);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            findViewById(R.id.btn_check).setEnabled(true);
            if (!response.isSuccessful()) {
                toast(R.string.us_inactive_title);
            }
        }
    }
}
