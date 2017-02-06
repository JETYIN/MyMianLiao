package com.tjut.mianliao.component;

import java.io.File;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.Attachment;
import com.tjut.mianliao.util.Utils;

public class AttachmentView extends LinearLayout {

    private View mTopDivider;
    private View mBottomDivider;
    private TextView mTvLabel;
    private TextView mTvInfo;

    private String mName;
    private int mNameColor;
    private boolean mNameUnderlined;

    private long mSize;
    private int mSizeColor;
    private boolean mSizeEnabled;

    public AttachmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = context.getResources();

        inflate(context, R.layout.comp_attachment_view, this);
        mTopDivider = findViewById(R.id.v_top_divider);
        mBottomDivider = findViewById(R.id.v_bottom_divider);
        mTvLabel = (TextView) findViewById(R.id.tv_att_label);
        mTvInfo = (TextView) findViewById(R.id.tv_att_info);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TextView);
        int textSize = ta.getDimensionPixelSize(R.styleable.TextView_textSize,
                res.getDimensionPixelSize(R.dimen.att_view_text_size));
        mTvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        mTvInfo.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        ta.recycle();

        ta = context.obtainStyledAttributes(attrs, R.styleable.AttachmentView);
        setDrawable(ta.getResourceId(R.styleable.AttachmentView_drawable, 0));
        if (ta.hasValue(R.styleable.AttachmentView_label)) {
            setLabel(ta.getString(R.styleable.AttachmentView_label));
        }

        setLabelColor(ta.getColor(R.styleable.AttachmentView_labelColor,
                res.getColor(R.color.txt_green)));
        setNameColor(ta.getColor(R.styleable.AttachmentView_nameColor,
                res.getColor(R.color.att_view_name_color)));
        setSizeColor(ta.getColor(R.styleable.AttachmentView_sizeTextColor,
                res.getColor(R.color.att_view_size_color)));

        setNameUnderlined(ta.getBoolean(
                R.styleable.AttachmentView_nameUnderlined, true));
        setSizeEnabled(ta.getBoolean(
                R.styleable.AttachmentView_sizeEnabled, true));
        setTopDividerEnabled(ta.getBoolean(
                R.styleable.AttachmentView_topDividerEnabled, true));
        setTopDividerEnabled(ta.getBoolean(
                R.styleable.AttachmentView_bottomDividerEnabled, true));
        ta.recycle();
    }

    public void show(Attachment att) {
        if (att == null || TextUtils.isEmpty(att.url)) {
            setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
            setSize(att.size);
            setName(att.name);
        }
    }

    public void show(File file) {
        if (file == null) {
            setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
            setSize(file.length());
            setName(file.getName());
        }
    }

    public void setDrawable(int resId) {
        mTvLabel.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0);
    }

    public void setLabel(String label) {
        mTvLabel.setText(label);
    }

    public void setLabelColor(int color) {
        mTvLabel.setTextColor(color);
    }

    public void setName(String name) {
        if (!TextUtils.equals(mName, name)) {
            mName = name;
            updateInfo();
        }
    }

    public void setNameColor(int color) {
        if (mNameColor != color) {
            mNameColor = color;
            updateInfo();
        }
    }

    public void setNameUnderlined(boolean underlined) {
        if (mNameUnderlined != underlined) {
            mNameUnderlined = underlined;
            updateInfo();
        }
    }

    public void setSize(long size) {
        if (mSize != size) {
            mSize = size;
            updateInfo();
        }
    }

    public void setSizeColor(int color) {
        if (mSizeColor != color) {
            mSizeColor = color;
            updateInfo();
        }
    }

    public void setSizeEnabled(boolean enabled) {
        if (mSizeEnabled != enabled) {
            mSizeEnabled = enabled;
            updateInfo();
        }
    }

    public void setTopDividerEnabled(boolean enabled) {
        mTopDivider.setVisibility(enabled ? VISIBLE : GONE);
    }

    public void setBottomDividerEnabled(boolean enabled) {
        mBottomDivider.setVisibility(enabled ? VISIBLE : GONE);
    }

    private void updateInfo() {
        if (TextUtils.isEmpty(mName)) {
            mTvInfo.setText(mName);
            return;
        }

        CharSequence name = Utils.getColoredText(mName, mNameColor, 0, mName.length());
        if (mNameUnderlined) {
            name = Utils.getUnderlinedText(name, 0, name.length());
        }

        SpannableStringBuilder info = SpannableStringBuilder.valueOf(name);
        if (mSizeEnabled) {
            String sizeStr = Utils.getAttSizeString(getContext(), mSize);
            info.append(" ").append(
                    Utils.getColoredText(sizeStr, mSizeColor, 0, sizeStr.length()));
        }

        mTvInfo.setText(info);
    }
}
