package com.tjut.mianliao.component.nova;

import java.util.ArrayList;

import org.json.JSONArray;

import android.content.Context;
import android.graphics.Color;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tjut.mianliao.R;
import com.tjut.mianliao.util.Utils;

public class VoteEditView extends LinearLayout {

    private static final int[] sBackgrounds = {
        R.drawable.vote_1, R.drawable.vote_2, R.drawable.vote_3, R.drawable.vote_4
    };

    private static final int OPTIONS_COUNT = 4;

    private LayoutInflater mInflater;
    private ArrayList<FrameLayout> mFlOptions;
    private ArrayList<EditText> mEtOptions;
    private int mOptCount;

    public VoteEditView(Context context) {
        super(context);
        init(context);
    }

    public VoteEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public boolean hasOptions() {
        for (EditText etOption : mEtOptions) {
            if (!etOption.getText().toString().trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public String getOptions() {
        mOptCount = 0;
        JSONArray ja = new JSONArray();
        for (EditText etOption : mEtOptions) {
            String option = etOption.getText().toString().trim();
            if (!option.isEmpty()) {
                ja.put(option);
                mOptCount++;
            }
        }
        return ja.toString();
    }

    public boolean isOptEnough() {
        getOptions();
        return mOptCount >= 2;
    }

    private void init(Context context) {
        setBaselineAligned(false);
        mInflater = LayoutInflater.from(context);
        mFlOptions = new ArrayList<FrameLayout>(OPTIONS_COUNT);
        mEtOptions = new ArrayList<EditText>(OPTIONS_COUNT);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int optionSize = (int) (70 * metrics.density);

        for (int i = 0; i < OPTIONS_COUNT; i++) {
            FrameLayout flOption = new FrameLayout(context);
            flOption.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
            mFlOptions.add(flOption);

            EditText etOption = new EditText(context);
            etOption.setBackgroundResource(sBackgrounds[i]);
            etOption.setGravity(Gravity.CENTER);
            etOption.setTextColor(Color.WHITE);
            etOption.setHintTextColor(Color.WHITE);
            etOption.setTextSize(10);
            final String maxLengthDesc = context.getString(R.string.post_max_vote_lenght_desc);
            etOption.setHint(maxLengthDesc);
            // set max_length
            etOption.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
            etOption.setVisibility(GONE);
            etOption.setLayoutParams(new FrameLayout.LayoutParams(
                    optionSize, optionSize, Gravity.CENTER));
            etOption.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        String content = ((EditText) v).getText().toString().trim();
                        if (content == null || "".equals(content)) {
                            ((EditText) v).setHint("");
                            ((EditText) v).setText("");
                        }
                    } else {
                        ((EditText) v).setHint(maxLengthDesc);
                    }
                }
            });
            mEtOptions.add(etOption);
            flOption.addView(etOption);
            addView(flOption);
        }

        // default 2
        mEtOptions.get(0).setVisibility(View.VISIBLE);
        mEtOptions.get(1).setVisibility(View.VISIBLE);

//        TextView tvAdd = new TextView(context);
//        tvAdd.setBackgroundResource(R.drawable.vote_0);
//        tvAdd.setCompoundDrawablePadding((int) (5 * metrics.density));
//        tvAdd.setCompoundDrawablesWithIntrinsicBounds(
//                0, R.drawable.button_add, 0, 0);
//        tvAdd.setGravity(Gravity.CENTER_HORIZONTAL);
//        tvAdd.setPadding(0, (int) (10 * metrics.density), 0, 0);
//        tvAdd.setText(R.string.fp_vote_add_more);
//        tvAdd.setTextColor(0xff34ced9);
//        tvAdd.setTextSize(10);
//        tvAdd.setLayoutParams(new FrameLayout.LayoutParams(
//                optionSize, optionSize, Gravity.CENTER));
        ImageView ivAdd = (ImageView) mInflater.inflate(R.layout.iv_add_option, null);
        ivAdd.setLayoutParams(new FrameLayout.LayoutParams(
                optionSize, optionSize, Gravity.CENTER));
        ivAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = mFlOptions.indexOf(v.getParent());
                if (index != -1) {
                    mEtOptions.get(index).setVisibility(VISIBLE);
                    Utils.showInput(mEtOptions.get(index));
                    mFlOptions.get(index).removeView(v);
                    if (index < OPTIONS_COUNT - 1) {
                        mFlOptions.get(index + 1).addView(v);
                    }
                }
            }
        });
        mFlOptions.get(0).removeView(ivAdd);
        mFlOptions.get(1).removeView(ivAdd);
        mFlOptions.get(2).addView(ivAdd);
    }
}
