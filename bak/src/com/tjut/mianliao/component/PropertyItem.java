package com.tjut.mianliao.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;

public class PropertyItem extends LinearLayout {
    private TextView mTvName;
    private TextView mTvInfo;

    public PropertyItem(Context context) {
        this(context, null);
    }

    public PropertyItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.item_property, this, true);
        mTvName = (TextView) findViewById(R.id.tv_prop_name);
        mTvInfo = (TextView) findViewById(R.id.tv_prop_info);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PropertyItem, 0, 0);
        int count = ta.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = ta.getIndex(i);
            switch (attr) {
                case R.styleable.PropertyItem_propName:
                    mTvName.setText(ta.getText(i));
                    break;
                case R.styleable.PropertyItem_propInfo:
                    mTvInfo.setText(ta.getText(i));
                    break;
                default:
                    break;
            }
        }
        ta.recycle();
    }

    public void setPropName(CharSequence name) {
        mTvName.setText(name);
    }

    public void setPropInfo(CharSequence info) {
        mTvInfo.setText(info);
    }
}
