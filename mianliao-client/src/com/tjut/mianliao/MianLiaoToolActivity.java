package com.tjut.mianliao;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.tjut.mianliao.component.FocusDialog;
import com.tjut.mianliao.curriculum.CurriculumActivity;
import com.tjut.mianliao.explore.DressUpMallActivty;
import com.tjut.mianliao.mycollege.MicroRecHomeActivity;
import com.tjut.mianliao.mycollege.TakeNoticesActivity;

/**
 * Created by j_hao on 2016/7/11.
 */
public class MianLiaoToolActivity extends BaseActivity implements View.OnClickListener {
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_mianliao_tool;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(R.string.mianliao_tool);
    }

    private void showDialog() {
        new FocusDialog(this)
                .setPositiveButton("", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void startActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_offer:
                startActivity(MicroRecHomeActivity.class);
                break;
            case R.id.rl_dress_mall:
                startActivity(DressUpMallActivty.class);
                break;
            case R.id.rl_class_plan:
                startActivity(CurriculumActivity.class);
                break;
            case R.id.rl_take_note:
                startActivity(TakeNoticesActivity.class);
                break;
            case R.id.rl_more:
            case R.id.tv_more_service:
                showDialog();
                break;
            default:
                break;
        }

    }
}
