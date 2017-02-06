package com.tjut.mianliao.component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import com.tjut.mianliao.R;

public class CheckBoxSetting extends SettingItem {

    private CompoundButton mCompoundBtn;

    public CheckBoxSetting(Context context) {
        this(context, null);
    }

    public CheckBoxSetting(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCompoundBtn = (CompoundButton) mWidget;
    }

    @Override
    protected int getWidgetLayoutResID() {
        return R.layout.setting_widget_checkbox;
    }

    @Override
    protected void onSettingKey() {
        mCompoundBtn.setChecked(getPersistedBoolean());
    }

    public void setChecked(boolean checked){
        mCompoundBtn.setChecked(checked);
        persistBoolean(checked);
    }

    @Override
    protected void onSettingItemClick() {
        mCompoundBtn.toggle();
        persistBoolean(mCompoundBtn.isChecked());
    }
}
