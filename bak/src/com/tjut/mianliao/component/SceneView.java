package com.tjut.mianliao.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.tjut.mianliao.R;

public class SceneView extends ProImageView {

    private Drawable mOverlayDrawable;

    public SceneView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SceneView);
        for (int i = 0; i < ta.getIndexCount(); i++) {
            int attr = ta.getIndex(i);
            if (attr == R.styleable.SceneView_coverSrc) {
                mOverlayDrawable = getResources().getDrawable(ta.getResourceId(attr, 0));
                break;
            }
        }
        ta.recycle();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mOverlayDrawable != null) {
            mOverlayDrawable.setBounds(0, 0, getWidth(), getHeight());
            mOverlayDrawable.draw(canvas);
        }
    }
}
