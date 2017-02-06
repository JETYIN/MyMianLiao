package com.tjut.mianliao.component;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;

@SuppressLint("ResourceAsColor")
public class UpgradeDialog extends Dialog {

	private Context mContext;

	private RelativeLayout mRlContent;
	private int mButtonHeight;
	private boolean mAutoDismiss = true;

	public UpgradeDialog(Context context) {
		this(context, R.style.Translucent_NoTitle);
	}

	public UpgradeDialog(Context context, int theme) {
		super(context, theme);
		mContext = context;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.upgrade_dialog);
		getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.WRAP_CONTENT);
		setCancelable(false);
		setCanceledOnTouchOutside(false);
		mButtonHeight = context.getResources().getDimensionPixelSize(
				R.dimen.default_btn_size);

	}
	public void setVersion(CharSequence version) {
		((TextView) findViewById(R.id.tv_version)).setText(version);

	}

	public UpgradeDialog setMessage(CharSequence message) {
		((TextView) findViewById(R.id.tv_message)).setText(message);
		return this;
	}

	public UpgradeDialog setMessage(int resId) {
		return setMessage(mContext.getString(resId));
	}

	public UpgradeDialog setPositiveButton(String txt,
			final OnClickListener onPositiveClicked) {
		getRlContent().setPadding(0, 0, 0, mButtonHeight);
		TextView positive = (TextView) findViewById(R.id.btn_positive);
		if (txt != null && !"".equals(txt.trim())) {
			positive.setText(txt);
		}
		positive.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (onPositiveClicked != null) {
					onPositiveClicked.onClick(UpgradeDialog.this,
							BUTTON_POSITIVE);
				}
				if (mAutoDismiss) {
					dismiss();
				}
			}
		});
		return this;
	}

	public UpgradeDialog setButtonBackground(int buttonId, int resId) {
		switch (buttonId) {
		case BUTTON_NEGATIVE:
			findViewById(R.id.btn_negative).setBackgroundResource(resId);
			break;
		case BUTTON_POSITIVE:
			findViewById(R.id.btn_positive).setBackgroundResource(resId);
			break;
		default:
			break;
		}
		return this;
	}

	public UpgradeDialog setPositiveButton(int resId,
			OnClickListener onPositiveClicked) {
		return setPositiveButton(mContext.getString(resId), onPositiveClicked);
	}

	public UpgradeDialog setNegativeButton(String txt,
			final OnClickListener onNegativeClicked) {
		getRlContent().setPadding(0, 0, 0, mButtonHeight);
		TextView negative = (TextView) findViewById(R.id.btn_negative);
		if (txt != null && !"".equals(txt.trim())) {
			negative.setText(txt);
		}
		negative.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (onNegativeClicked != null) {
					onNegativeClicked.onClick(UpgradeDialog.this,
							BUTTON_NEGATIVE);
				}
				if (mAutoDismiss) {
					dismiss();
				}
			}
		});
		return this;
	}

	public UpgradeDialog setNegativeButton(int resId,
			OnClickListener onNegativeClicked) {
		return setNegativeButton(mContext.getString(resId), onNegativeClicked);
	}

	/**
	 * @param autoDismiss
	 *            If true, the dialog will be dismissed once user clicked
	 *            positive/negative button, or clicked on adapter item. Default
	 *            is true;
	 */
	public UpgradeDialog setAutoDismiss(boolean autoDismiss) {
		mAutoDismiss = autoDismiss;
		return this;
	}

	private RelativeLayout getRlContent() {
		if (mRlContent == null) {
			mRlContent = (RelativeLayout) findViewById(R.id.rl_content);
		}
		return mRlContent;
	}
}
