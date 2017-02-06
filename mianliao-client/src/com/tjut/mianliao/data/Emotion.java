package com.tjut.mianliao.data;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;

import com.tjut.mianliao.R;
import com.tjut.mianliao.theme.ThemeDrawableSource;

public class Emotion {

    public String value;
    public int resource;
    public String parseText;
    public String imageName;
    public boolean isBig;
    
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }

    public String getParseText() {
        return parseText;
    }

    public void setParseText(String parseText) {
        this.parseText = parseText;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    private Emotion() {
    }

    public static Emotion fromCodePoint(int codePoint, int resource) {
        return fromString(getValue(codePoint), resource);
    }

    public static Emotion fromString(String value, int resource) {
        Emotion emotion = new Emotion();
        emotion.value = value;
        emotion.resource = resource;
        emotion.imageName = ThemeDrawableSource.getResName(resource);
        return emotion;
    }
 
    public static final String getValue(int codePoint) {
        if (Character.charCount(codePoint) == 1) {
            return String.valueOf((char) codePoint);
        } else {
            return String.valueOf(Character.toChars(codePoint));
        }
    }

    public Object getSpan(Context context, int size) {
        if (resource > 0) {
            Drawable drawable = context.getResources().getDrawable(resource);
            drawable.setBounds(0, 0, size, size);
            return new ImageSpan(drawable);
        }

        if (imageName != null) {
            Drawable drawable = Drawable.createFromPath(getImageName());
            if (drawable == null) {
                return null;
            }
            drawable.setBounds(0, 0, size, size);
            return new ImageSpan(drawable);
        }

        return null;
    }

    public CharSequence getSpannable(Context context) {
        int size = context.getResources().getDimensionPixelSize(R.dimen.emo_size_large);
        return getSpannable(context, size);
    }

    public CharSequence getSpannable(Context context, int size) {

        SpannableStringBuilder ssb;
        Object span;

        String parseText = getParseText();
        if (getImageName() != null && parseText != null && parseText.length() > 0) {
            ssb = new SpannableStringBuilder(parseText);
            span = getSpan(context, size);

            if (span != null) {
                ssb.setSpan(span, 0, getParseText().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return ssb;
        } else {
            if (TextUtils.isEmpty(value) && getImageName() == null) {
                return value;
            }
            ssb = new SpannableStringBuilder(value);
            span = getSpan(context, size);

            if (span != null) {
                ssb.setSpan(span, 0, value.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return ssb;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Emotion) {
            Emotion other = (Emotion) o;
            return TextUtils.equals(value, other.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value == null ? 0 : value.hashCode();
    }
}
