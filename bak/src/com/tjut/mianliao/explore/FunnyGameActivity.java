package com.tjut.mianliao.explore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.R;
import com.tjut.mianliao.question.QuestionActivity;
import com.tjut.mianliao.settings.Settings;
import com.umeng.analytics.MobclickAgent;

public class FunnyGameActivity extends BaseActivity implements OnClickListener {
    private FrameLayout mFmAnswer;
    private ImageView mFgameAd;

    private Settings mSettings;
    private boolean mIsNightMode;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_funnygame;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = Settings.getInstance(this);
        mIsNightMode = mSettings.isNightMode();
        getTitleBar().setTitle("趣游戏");
        mFmAnswer = (FrameLayout) findViewById(R.id.fm_answer);
        mFgameAd = (ImageView) findViewById(R.id.iv_fungame_ad);

        if (mIsNightMode) {
            findViewById(R.id.rl_funny_game).setBackgroundResource(R.drawable.bg);
        } else {
            findViewById(R.id.rl_funny_game).setBackgroundColor(0XFFF2F2F2);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_fun_answer:
                Intent intent = new Intent(FunnyGameActivity.this, QuestionActivity.class);
                // intent.putExtra(Question.TAG, value)
                startActivity(intent);
                MobclickAgent.onEvent(this, MStaticInterface.GAME1);
                break;
            default:
                break;
        }
    }

}
