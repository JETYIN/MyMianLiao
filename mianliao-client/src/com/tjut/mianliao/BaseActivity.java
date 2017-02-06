package com.tjut.mianliao;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.tjut.mianliao.component.TitleBar;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.theme.ThemeLineView;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;
import com.tjut.mianliao.video.JCVideoPlayer;
import com.umeng.analytics.MobclickAgent;

public abstract class BaseActivity extends Activity {
    protected static String TAG;
    public static final int RESULT_UPDATED = 1;
    public static final int RESULT_DELETED = 2;
    public static final int RESULT_VIEWED = 3;
    public static final int RESULT_BACK = 4;

    private String mTag;
    private int mIdentity = -1;

    protected Settings mSettings;

    protected LayoutInflater mInflater;
    protected View mTopLine;

    private TitleBar mTitleBar;

    protected abstract int getLayoutResID();

    protected int getBaseLayoutResID() {
        return R.layout.activity_base;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JCVideoPlayer.isVideoFinish = true;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
        setContentView(getBaseLayoutResID());
        /**获取到当前的tag**/
        TAG = this.getClass().getSimpleName();
        mSettings = Settings.getInstance(this);
        mInflater = getLayoutInflater();
        mTopLine = findViewById(R.id.view_line);


        int layoutResID = getLayoutResID();
        if (layoutResID != 0) {
            ViewStub vs = (ViewStub) findViewById(R.id.vs_content);
            vs.setLayoutResource(getLayoutResID());
            vs.inflate();
        }

        mTitleBar = (TitleBar) findViewById(R.id.rl_title_bar);
        if (mTitleBar != null) {
            mTitleBar.showLeftButton(R.drawable.botton_bg_arrow, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (JCVideoPlayer.isVideoFinish) {
            JCVideoPlayer.releaseAllVideos();
        }
        JCVideoPlayer.isVideoFinish = true;
        MobclickAgent.onPause(this);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_slide_left_in, R.anim.activity_slide_right_out);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.activity_slide_right_in, R.anim.activity_slide_left_out);
    }

    public void toast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    public void toast(CharSequence text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    protected TitleBar getTitleBar() {
        if (mTitleBar == null) {
            mTitleBar = (TitleBar) findViewById(R.id.rl_title_bar);
        }
        return mTitleBar;
    }

    protected String getTag() {
        if (mTag == null) {
            mTag = this.getClass().getSimpleName();
        }
        return mTag;
    }

    protected void setText(int viewId, CharSequence text) {
        ((TextView) findViewById(viewId)).setText(text);
    }

    /**
     * It could be used in most cases which does not require a strict unique id.
     */
    protected int getIdentity() {
        if (mIdentity == -1) {
            mIdentity = Utils.generateIdentify(getTag());
        }
        return mIdentity;
    }
}
