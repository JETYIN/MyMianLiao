package com.tjut.mianliao.component;

import android.content.Context;
import android.util.AttributeSet;

public class GalleryItemView extends AvatarView {
    public GalleryItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
