package com.tjut.mianliao.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.util.EmotionManager;

public class EmotionTextView extends TextView {

    private EmotionManager mEmotionManager;
    private int mEmotionSize;

    public EmotionTextView(Context context) {
        this(context, null);
    }

    public EmotionTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mEmotionManager = EmotionManager.getInstance(context);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EmotionTextView);
        int defaultSize = context.getResources().getDimensionPixelSize(R.dimen.emo_size_medium);
        setEmotionSize(ta.getDimensionPixelSize(
                R.styleable.EmotionTextView_emotionSize, defaultSize));
        ta.recycle();
    }

    public void setEmotionSize(int size) {
        mEmotionSize = size;
        setText(getText());
    }
    
    public CharSequence getParseText(CharSequence text) {
        return mEmotionManager.parseEmotion(text, mEmotionSize);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (mEmotionManager != null) {
            text = mEmotionManager.parseEmotion(text, mEmotionSize);
        }
        super.setText(text, type);
    }
}
