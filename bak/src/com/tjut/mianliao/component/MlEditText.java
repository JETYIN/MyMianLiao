package com.tjut.mianliao.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

/**
 * Default EditText doesn't allow multiline text with ime send/done button, etc, so we have to extend it.
 */
public class MlEditText extends EditText {

    private int mOptions;

    public MlEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMlImeOptions(int options) {
        mOptions = options;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection connection = super.onCreateInputConnection(outAttrs);
        if (mOptions > 0) {
            int imeActions = outAttrs.imeOptions & EditorInfo.IME_MASK_ACTION;

            outAttrs.imeOptions ^= imeActions;

            outAttrs.imeOptions |= mOptions;

            if ((outAttrs.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
                outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
            }
        }
        return connection;
    }

}
