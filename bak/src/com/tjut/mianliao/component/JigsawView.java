package com.tjut.mianliao.component;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tjut.mianliao.R;

public class JigsawView extends LinearLayout {

    private int[] mPieceImageIds = { R.id.iv1, R.id.iv2, R.id.iv3, R.id.iv4, R.id.iv5, R.id.iv6, R.id.iv7 };

    public JigsawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.jigsaw_view, this);

    }

    public void setPieceImage(int index, int resId) {

        ImageView iv = (ImageView) this.findViewById(mPieceImageIds[index]);
        iv.setBackgroundResource(resId); // setImageResource(resId);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public void removePieceImage(int index) {

        ImageView iv = (ImageView) this.findViewById(mPieceImageIds[index]);
        // judge the version_code, avoid NoSuchMethodError
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            iv.setBackground(null);
        } else {
            iv.setBackgroundDrawable(null);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // int width = getDefaultSize(getSuggestedMinimumWidth(),
        // widthMeasureSpec);
        // setMeasuredDimension(100, 100);
    }

}
