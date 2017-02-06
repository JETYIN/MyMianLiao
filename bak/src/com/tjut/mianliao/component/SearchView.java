package com.tjut.mianliao.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.tjut.mianliao.R;
import com.tjut.mianliao.settings.Settings;

public class SearchView extends LinearLayout {

    private TextView mTvSearchCondition;
    private ImageView mIvSearchClear;
    private EditText mEtSearchContent;
    private InputMethodManager mIMManager;
    private LinearLayout mRootLayout;

    private OnSearchConditionListener mOnSearchConditionListener;
    private OnSearchTextListener mOnSearchTextListener;

    private OnClearIconClickListener mOnClearIconClickListener;

    public void setOnClearIconClickListener(OnClearIconClickListener mOnClearIconClickListener) {
        this.mOnClearIconClickListener = mOnClearIconClickListener;
    }

    public interface OnClearIconClickListener {
        void onClickClearIcon();
    }

    public interface OnSearchConditionListener {
        public void onSearchConditionClicked();
    }

    public interface OnSearchTextListener {
        public void onSearchTextChanged(CharSequence text);
    }

    public SearchView(Context context) {
        this(context, null);
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.search_view, this, true);

        mRootLayout = (LinearLayout) findViewById(R.id.root_layout);
        mTvSearchCondition = (TextView) findViewById(R.id.tv_search_condition);
        mIvSearchClear = (ImageView) findViewById(R.id.iv_search_clear);
        mEtSearchContent = (EditText) findViewById(R.id.et_search_content);
        mIMManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        mTvSearchCondition.setOnClickListener(mOnClickListener);
        mIvSearchClear.setOnClickListener(mOnClickListener);
        mEtSearchContent.setOnEditorActionListener(mOnEditorActionListener);
        mEtSearchContent.addTextChangedListener(mTextWatcher);
        
        this.fillCustomStyle(context, attrs);

    }

    private void fillCustomStyle(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SearchView);
        int bgResid = ta.getResourceId(R.styleable.SearchView_search_view_bg, 0);
        boolean changeBg = ta.getBoolean(R.styleable.SearchView_change_bg, true);

        int rootLayoutHeight = ta.getDimensionPixelSize(R.styleable.SearchView_search_view_height, 0);

        ta.recycle();

        if (Settings.getInstance(context).isNightMode() && changeBg) {
            	mRootLayout.setBackgroundResource(R.drawable.bg_search_bar_black);
        } else {
            if (bgResid != 0) {
            	mRootLayout.setBackgroundResource(bgResid);
            }
        }

        if (rootLayoutHeight != 0) {
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, rootLayoutHeight);

            mRootLayout.setLayoutParams(param);
        }

    }

    public void hideClearIncon() {
        mIvSearchClear.setVisibility(View.GONE);
    }
    
    public void showClearIncon() {
        mIvSearchClear.setVisibility(View.VISIBLE);
    }
    
    public void setSearchViewBg(int resId) {
        mRootLayout.setBackgroundResource(resId);
    }

    public void setSearchViewBgColor(int color) {
        mRootLayout.setBackgroundColor(color);
    }

    public void setOnSearchConditionListener(OnSearchConditionListener listener) {
        mOnSearchConditionListener = listener;
    }

    public void setOnSearchTextListener(OnSearchTextListener listener) {
        mOnSearchTextListener = listener;
    }

    public void setHint(int resid) {
        mEtSearchContent.setHint(resid);
    }

    public void setHint(CharSequence hint) {
        mEtSearchContent.setHint(hint);
    }

    public void setSearchText(CharSequence text) {
        mEtSearchContent.setText(text);
        if (text != null) {
            mEtSearchContent.setSelection(text.length());
        }
    }

    public String getSearchText() {
        return mEtSearchContent.getText().toString();
    }

    public void setCondition(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            mTvSearchCondition.setVisibility(GONE);
        } else {
            mTvSearchCondition.setText(text);
            mTvSearchCondition.setVisibility(VISIBLE);
        }
    }

    public void hideInput() {
        mEtSearchContent.clearFocus();
        post(mHideImeRunnable);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus && TextUtils.isEmpty(mEtSearchContent.getText())) {
            hideInput();
        }
    }

    private void onConditionClicked() {
        if (mOnSearchConditionListener != null) {
            mOnSearchConditionListener.onSearchConditionClicked();
        }
    }

    private void onClearClicked() {
        if (mOnClearIconClickListener != null) {
            mOnClearIconClickListener.onClickClearIcon();
        }
        mEtSearchContent.setText("");
        hideInput();
    }

    private void onTextChanged(CharSequence text) {
        mIvSearchClear.setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE);
        if (mOnSearchTextListener != null) {
            mOnSearchTextListener.onSearchTextChanged(text);
        }
    }

    private Runnable mHideImeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mIMManager != null) {
                mIMManager.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    };

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mTvSearchCondition) {
                onConditionClicked();
            } else if (v == mIvSearchClear) {
                onClearClicked();
            }
        }
    };

    private OnEditorActionListener mOnEditorActionListener = new OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            hideInput();
            return true;
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            SearchView.this.onTextChanged(s);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    
    public void setMaxLength(int maxLength) {
        mEtSearchContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
    } 
}
