package com.tjut.mianliao.theme;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import com.tjut.mianliao.R;
import com.tjut.mianliao.settings.Settings;

public class ThemeLineView extends View {

	private static final int COLOR_NORMAL = 0xffe6e6e6;
	private static final int COLOR_NIGHT = 0xff272235;
	
	private int mNormalBgColor = 0xffb2b2b2;
	private int mSecondBgColor = 0xff493d60;

	private boolean mReadAttrColor = false;
	
	public ThemeLineView(Context context, AttributeSet attrs) {
		super(context, attrs);

		int colorId = attrs.getAttributeResourceValue(
				"http://schemas.android.com/apk/res/android", "background", -1);

		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ThemeLineView);
		mReadAttrColor = ta.getBoolean(R.styleable.ThemeLineView_read_attr_color, false);
		ta.recycle();

		if (!Settings.getInstance(context).isNightMode()) {
			if (mReadAttrColor) {
				setBackgroundColor(mNormalBgColor);
			} else {
				if (colorId != -1) {
					this.setBackgroundColor(colorId);
				} else {
					this.setBackgroundColor(COLOR_NORMAL);
				}
			}
		} else {
			if (mReadAttrColor) {
				this.setBackgroundColor(mSecondBgColor);
			} else {
				this.setBackgroundColor(COLOR_NIGHT);
			}
		}

	}

}
