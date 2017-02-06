package com.tjut.mianliao.component;

import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

public class MLLinkMovementMethod extends LinkMovementMethod {
    
    private static final int CLICK = 1;
    private static final int UP = 2;
    private static final int DOWN = 3;

    private static MLLinkMovementMethod sInstance;

    public static MLLinkMovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new MLLinkMovementMethod();

        return sInstance;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        if (event.getAction() == CLICK) {
            return super.onTouchEvent(widget, buffer, event);
        }
        return true;
    }
}
