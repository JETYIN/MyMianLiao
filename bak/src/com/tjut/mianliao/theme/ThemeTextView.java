package com.tjut.mianliao.theme;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.tjut.mianliao.settings.Settings;

public class ThemeTextView extends TextView {

	public ThemeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		if (!Settings.getInstance(context).isNightMode()) {
			return;
		}

		float pxSize = this.getTextSize();

		float scale = context.getResources().getDisplayMetrics().density;

		float sp = pxSize / scale;

		if (sp > 14) {
			this.setTextColor(0xffbebebe);
		} else {
			this.setTextColor(0xff979797);

		}

	}
}
