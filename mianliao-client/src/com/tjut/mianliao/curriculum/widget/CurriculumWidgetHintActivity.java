package com.tjut.mianliao.curriculum.widget;

import android.os.Bundle;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;

public class CurriculumWidgetHintActivity extends BaseActivity {

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_curriculum_widget_hint;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.cur_widget_hint_title, null);
    }
}