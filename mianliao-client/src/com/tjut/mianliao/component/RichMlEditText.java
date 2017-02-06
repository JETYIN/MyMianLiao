package com.tjut.mianliao.component;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.android.bba.common.util.Util;
import com.baidu.mapapi.map.Text;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.AtInfo;
import com.tjut.mianliao.util.EmotionManager;
import com.tjut.mianliao.util.Utils;

public class RichMlEditText extends MlEditText {

    public static final Pattern TOPIC_MATCH_PATTERN = Pattern.compile("#[^#]+#");

    private static final int sChangedColor = 0xff7cbadb;

    /**
     * It used to judge the {@link RichMlEditText} whether or not
     * to listen for changes in content 
     */
    private boolean mShouldWatcher;
    
    /**
     * It used to judge if has a {@link TextWatcher}
     */
    private boolean mHasTextWatcher;
    
    /**
     * It used to judge the {@link RichMlEditText} whether or not
     * to match the {@code #topic#}
     */
    private boolean mShouldMatchTopic;
    
    /**
     * It used to judge the {@link RichMlEditText} whether or not
     * to match the {@code @friend}
     */
    private boolean mShouldMatchAt;

    private Context mContext;
    private EmotionManager mEmotionManager;
    private MLTextWatcher mWatcher;
    
    private int mSelection;
    private int mEmotionSize;
    private ArrayList<AtInfo> mAtInfos = new ArrayList<AtInfo>();
    private ClickableText mClickableText;
    
    private OnAtDelClicklistener mDelClicklistener;

    public RichMlEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context.getApplicationContext();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RichMlEditText);
        mShouldWatcher = ta.getBoolean(R.styleable.RichMlEditText_shouldWatcherTextChanged, true);
        mShouldMatchTopic = ta.getBoolean(R.styleable.RichMlEditText_shouldWatcherTopic, false);
        mShouldMatchAt = ta.getBoolean(R.styleable.RichMlEditText_shouldWatcherAt, false);
        ta.recycle();
        mWatcher = new MLTextWatcher();
        changeWatcherStatus();
        mEmotionManager = EmotionManager.getInstance(context);
        mEmotionSize = context.getResources().getDimensionPixelSize(R.dimen.emo_size_small);
        setLongClickable(true);
    }

    private void changeWatcherStatus() {
        if (mShouldWatcher) {
            if (!mHasTextWatcher) {
                addTextChangedListener(mWatcher);
                mHasTextWatcher = true;
            }
        } else {
            if (mHasTextWatcher) {
                removeTextChangedListener(mWatcher);
                mHasTextWatcher = false;
            } 
        }
    }

    /**
     * Call this method, the {@link RichMlEditText} will match the topic
     * Pattern with {@code "(#|＃)[^#＃]+(#|＃)"} , the span will be colored while matched
     * or will be normal
     */
    public void setShoulMatcherTopic() {
        mShouldMatchTopic = true;
    }
        
    /**
     * Call this method, the {@link RichMlEditText} will match the topic
     * Pattern with {@code "(@|﹫|＠)\\S+"} , the span will be colored while
     * matched or will be normal
     */
    public void setShouldMatcherAt() {
        mShouldMatchAt = true;
    }
    
    /**
     * Call this method to set the emotion size, and the default size see
     * {@code com.tjut.mianliao.R.dimen.emo_size_medium }
     * @param size
     */
    public void setEmotionSize(int size) {
        mEmotionSize = size;
        setText(getText());
    }
    
    public void setOnAtDelClicklistener(OnAtDelClicklistener listener) {
        mDelClicklistener = listener;
    }

    public CharSequence getParseText(CharSequence text) {
        return mEmotionManager.parseEmotion(text, mEmotionSize);
    }

    public void setContent(String content) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(content);
        if (mShouldMatchTopic) {
            content = matchTopic(content, ssb);
        }
        content = matchURL(content, ssb);
        if (mShouldMatchAt) {
            matchAt(content, ssb);
        }
        setText(getParseText(ssb));
        if (mSelection < 0) {
            mSelection = 0;
        }
        setSelection(mSelection); 
        addTextChangedListener(mWatcher);
    }

    private void matchAt(String content, SpannableStringBuilder ssb) {
        Matcher mRefMatcher = Utils.REF_FRIEND_PATTERN.matcher(content);
        int refLastIndex = 0;
        while (mRefMatcher.find()) {
            String ts = mRefMatcher.group();
            int index = content.indexOf(ts, refLastIndex);
            int length = ts.length();
            ts = ts.charAt(0) + ts.substring(1, length - 1).trim() + ts.charAt(length - 1);
            ssb.replace(index, index + ts.length(), getClickbleSpanText(ts));
            content = ssb.toString();
            refLastIndex = index + 1;
        }
    }

    private String matchTopic(String content, SpannableStringBuilder ssb) {
        Matcher matcher = Utils.TOPIC_MATCH_PATTERN.matcher(content);
        int lastIndex = 0;
        while (matcher.find()) {
            String ts = matcher.group();
            int index = content.indexOf(ts, lastIndex);
//            int length = ts.length();
//            char last = ts.charAt(length - 1);
//            ts = ts.charAt(0) + ts.substring(1, length - 1).trim() + last;
            ssb.replace(index, index + ts.length(), getClickbleSpanText(ts));
            content = ssb.toString();
            lastIndex = index + 1;
        }
        return content;
    }

    private String matchURL(String content, SpannableStringBuilder ssb) {
        Matcher mURLMacher = Utils.URL_MATCH_PATTERN.matcher(content);
        int URLLastIndex = 0;
        while (mURLMacher.find()) {
            String ts = mURLMacher.group();
            int index = content.indexOf(ts, URLLastIndex);
            ssb.replace(index, index + ts.length(), getClickbleSpanText(ts));
            content = ssb.toString();
            URLLastIndex = index + 1;
        }
        return  content;
    }

    public SpannableString getClickbleSpanText(String content) {
        SpannableString spanableInfo = new SpannableString(content); 
        mClickableText = new ClickableText();
        mClickableText.setContent(content);
        spanableInfo.setSpan(mClickableText, 0, content.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  
        return spanableInfo;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int selectionEnd = getSelectionEnd();
            int index = getAtContentIndex(selectionEnd);
            if (index <= getText().toString().length()) {
                setSelection(index);
            }
        }
        return result;
    }
    
    private int getAtContentIndex(int selectionEnd) {
        int currentIndex = selectionEnd;
        String content = getText().toString();
        Matcher mRefMatcher = Utils.REF_FRIEND_PATTERN.matcher(content);
        int refLastIndex = 0;
        mAtInfos.clear();
        int numIndex = 0;
        while (mRefMatcher.matches()) {
            AtInfo mAt = new AtInfo();
            String ts = mRefMatcher.group();
            int index = content.indexOf(ts, refLastIndex);
            refLastIndex = index + 1;
            mAt.AtIndex = index;
            mAt.AtContent = ts;
            mAt.index = numIndex;
            mAtInfos.add(mAt);
            numIndex++;
        }
        
        for (AtInfo mAt : mAtInfos) {
            if (selectionEnd > mAt.AtIndex && selectionEnd < mAt.AtIndex + mAt.AtContent.length() + 1) {
                currentIndex = mAt.AtIndex + mAt.AtContent.length() + 1;
            }
        }
        return currentIndex;
    }

    /**
     * Call this method can set {@link RichMlEditText} whether or not 
     * listen for changes in content.
     * <p> See {@link #changeWatcherStatus()}.
     * @param watcher
     */
    public void setShouldWatcher(boolean watcher) {
        mShouldWatcher = watcher;
        changeWatcherStatus();
    }

    private class ClickableText extends ClickableSpan implements OnClickListener {
        private String content;

        public void setContent(String content) {
            this.content = content;
        }

        @SuppressLint("NewApi")
        @Override
        public void onClick(View v) {
            final TextView tv = (TextView) v;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                final int highlightColor = tv.getHighlightColor();
                tv.setHighlightColor(Color.TRANSPARENT);
                postDelayed(new Runnable() {
                    public void run() {
                        tv.setHighlightColor(highlightColor);
                    }
                }, 20);
            } else {
                tv.setHighlightColor(Color.TRANSPARENT);
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(sChangedColor);
            ds.setUnderlineText(false);
        }
    }

    private class MLTextWatcher implements TextWatcher {
        CharSequence lastChar;
        String mBeforeString;
        String mEndString = null;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            mBeforeString = s.toString();
            Matcher mRefMatcher = Utils.REF_FRIEND_PATTERN.matcher(mBeforeString);
            int refLastIndex = 0;
            mAtInfos.clear();
            int numIndex = 0;
            while (mRefMatcher.find()) {
                AtInfo mAt = new AtInfo();
                String ts = mRefMatcher.group();
                int index = mBeforeString.indexOf(ts, refLastIndex);
                refLastIndex = index + 1;
                mAt.AtIndex = index;
                mAt.AtContent = ts;
                mAt.index = numIndex;
                mAtInfos.add(mAt);
                numIndex++;
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mSelection = start + count;
            lastChar = s.toString().substring(start, start + count);
            if (mBeforeString != null && mBeforeString.length() > 0) {
                SpannableStringBuilder ssb = new SpannableStringBuilder(s.toString());
                if (before == 1) {
                    int mCursorIndex = getSelectionStart();
                    int mCurrentAtIndex = -1;
                    int mCurrentAtPosition = -1;
                    if (mAtInfos != null && mAtInfos.size() > 0) {
                        for (int i = 0; i < mAtInfos.size(); i++) {
                            if (mCursorIndex > mAtInfos.get(i).AtIndex) {
                                mCurrentAtIndex = mAtInfos.get(i).AtIndex;
                                mCurrentAtPosition = i;
                            } else {
                                break;
                            }
                        }
                    }
                    if (mCurrentAtIndex != -1 && mCurrentAtPosition != -1) {
                        AtInfo at = mAtInfos.get(mCurrentAtPosition);
                        if (mCursorIndex <= at.AtIndex + at.AtContent.length()) {
                            ssb.replace(at.AtIndex, at.AtIndex + at.AtContent.length(), "");
                            mEndString = ssb.toString();
                            mSelection -= (mCursorIndex - mCurrentAtIndex);
                            if (mDelClicklistener != null) {
                                mDelClicklistener.onDelClick(at.index);
                            }
                        }
                    }

                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            System.out.println("!Utils.isNumber(lastChar) = " + !Utils.isNumber(lastChar));
            System.out.println("shouldMatchContent(lastChar) = " + shouldMatchContent(lastChar));
            if (lastChar != null && ((!Utils.isNumber(lastChar) && shouldMatchContent(lastChar)) || isMatchURL(s.toString()))) {
                removeTextChangedListener(this);
                if (mEndString != null) {
                    setContent(mEndString);
                    mEndString = null;
                } else {
                    setContent(s.toString());
                }
            }
        }
        
    }
    
    private boolean shouldMatchContent(CharSequence input) {
        return Utils.isMatcherTopic(input) || Utils.isMatcherAt(input) ||
                Utils.isMlSpecSymnol(input) || "".equals(input);
    }

    public abstract interface OnAtDelClicklistener{
        public void  onDelClick(int index);
    }

    private boolean isMatchURL(String content) {
        Matcher mURLMacher = Utils.URL_MATCH_PATTERN.matcher(content);
        while (mURLMacher.find()) {
            return  true;
        }
        return  false;
    }
}
