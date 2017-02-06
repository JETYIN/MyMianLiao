package com.tjut.mianliao.scan;

import static com.tjut.mianliao.R.id.scv_preview_cover;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.news.wicket.WicketActivity;

public class ScanActivity extends BaseActivity {

    public static final String SP_IS_CHECKER = "is_checker";

    private Scanner mScanner;
    private boolean mIsChecker = false;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_scan;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTitleBar().showTitleText(R.string.more_scan, null);
        getTitleBar().showRightButton(R.drawable.btn_title_bar_ticket, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScanActivity.this, WicketActivity.class));
            }
        });

        mIsChecker = getPreferences(0).getBoolean(SP_IS_CHECKER, false);
        if (!mIsChecker) {
            getTitleBar().hideRightButton();
        }

        SurfaceView sv = (SurfaceView) findViewById(R.id.sv_preview);
        ScanCoverView scv = (ScanCoverView) findViewById(scv_preview_cover);
        ScanIndicatorView siv = (ScanIndicatorView) findViewById(R.id.siv_scan_indicator); 

        mScanner = new Scanner(this, sv, scv, siv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScanner.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScanner.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScanner.destroy();
    }
}