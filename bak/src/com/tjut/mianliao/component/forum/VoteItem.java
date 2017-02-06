package com.tjut.mianliao.component.forum;

import java.text.NumberFormat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.util.Utils;

public class VoteItem extends LinearLayout {

    private TextView mTvDesc;
    private ImageView mIvCheck;

    public VoteItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.comp_vote, this, true);

        mTvDesc = (TextView) findViewById(R.id.tv_desc);
        mIvCheck = (ImageView) findViewById(R.id.iv_check);

    }

    public void setInfo(String desc) {
        mTvDesc.setText(desc);
    }

    public void setInfo(String desc, int votes, int totalVotes) {
        String content;
        if (votes == 0) {
            content = getContext().getString(R.string.fv_vote_desc_format_zero, desc);
        } else {
            content = getContext().getString(R.string.fv_vote_desc_format, desc, votes,
                    NumberFormat.getPercentInstance().format(((double) votes) / totalVotes));
        }
        mTvDesc.setText(Utils.getColoredText(
                content, 0xff8e8e8e, desc.length(), content.length()));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mIvCheck.setEnabled(enabled);
    }

    public void setChecked(boolean checked) {
        mIvCheck.setImageResource(checked ? R.drawable.selector_vote_checked : R.drawable.ic_vote_check);
    }
}
