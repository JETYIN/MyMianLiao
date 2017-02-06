package com.tjut.mianliao.component;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.Property;

public class PropertiesView extends LinearLayout {

    private ArrayList<PropertyItem> mRecycledViews;
    private int mLineSpacing;

    public PropertiesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        mRecycledViews = new ArrayList<PropertyItem>();
        setLineSpacing(context.getResources()
                .getDimensionPixelSize(R.dimen.contact_name_margin_top));
    }

    public void setLineSpacing(int spacing) {
        mLineSpacing = spacing;
    }

    public void show(ArrayList<Property> properties) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            if (view instanceof PropertyItem) {
                mRecycledViews.add((PropertyItem) view);
            }
        }
        removeAllViews();

        if (properties != null) {
            boolean firstTime = true;
            for (Property p : properties) {
                PropertyItem pi = mRecycledViews.isEmpty()
                        ? new PropertyItem(getContext()) : mRecycledViews.remove(0);
                pi.setPropName(p.key);
                pi.setPropInfo(p.value);
                if (firstTime) {
                    firstTime = false;
                } else {
                    pi.setPadding(0, mLineSpacing, 0, 0);
                }
                addView(pi);
            }
        }
    }
}
