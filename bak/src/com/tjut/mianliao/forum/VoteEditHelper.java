package com.tjut.mianliao.forum;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.CardItemConf;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.forum.VoteInputItem;

public class VoteEditHelper implements View.OnClickListener, TimePicker.Callback,
        DialogInterface.OnClickListener {
    private static final int MAX_OPTION = 10;

    private TimePicker mTimePicker;

    private Context mContext;
    private LinearLayout mOptionHolder;
    private LinearLayout mPropertyHolder;
    private Button mBtnAdd;

    private LightDialog mVoteTypeDialog;

    private boolean mEnabled = false;
    private boolean mAnonyVote;
    private long mEndTime;

    public VoteEditHelper(Context context, CfPost post, LinearLayout optionHolder,
            LinearLayout propertyHolder) {
        mContext = context;
        mOptionHolder = optionHolder;
        mBtnAdd = (Button) mOptionHolder.findViewById(R.id.btn_add_option);
        mBtnAdd.setOnClickListener(this);

        mPropertyHolder = propertyHolder;

        if (post.postId > 0) {
            if (post.hasVote()) {
                setEnabled(true);
                init(post);
                mBtnAdd.setEnabled(false);
                mBtnAdd.setTextColor(0xFFB6B6B6);
            }
        } else {
            mPropertyHolder.findViewById(R.id.cif_vote_time).setOnClickListener(this);
            mPropertyHolder.findViewById(R.id.cif_vote_type).setOnClickListener(this);
        }

        mTimePicker = new TimePicker(mContext);
        mTimePicker.setCallback(this);
    }

    public void init(CfPost post) {
        mEndTime = post.vote.endTime * 1000;
        updateEndTime();
        mAnonyVote = post.vote.anony;
        updateVoteType();

        int size = post.getVoteOptCount();
        int count = mOptionHolder.getChildCount();
        ArrayList<String> options = post.vote.options;
        for (int i = 0; i < count; i++) {
            View child = mOptionHolder.getChildAt(i);
            if (child instanceof VoteInputItem) {
                VoteInputItem vii = (VoteInputItem) child;
                vii.setOption(options.get(0));
                vii.setEnabled(false);
                break;
            }
        }

        for (int i = 1; i < size; i++) {
            addOption(options.get(i));
        }
    }

    public void addOption(String option) {
        int count = mOptionHolder.getChildCount();
        VoteInputItem child = new VoteInputItem(mContext, null).setHolderRef(mOptionHolder);
        child.findViewById(R.id.iv_delete).setOnClickListener(this);
        child.setOption(option);
        child.setEnabled(TextUtils.isEmpty(option));
        mOptionHolder.addView(child, count - 1);
        updateAddButton();
    }

    private void deleteOption(View view) {
        mOptionHolder.removeView(view);
        updateAddButton();
    }

    private void updateAddButton() {
        boolean maxed = mOptionHolder.getChildCount() - 1 > MAX_OPTION;
        mBtnAdd.setEnabled(!maxed);
        mBtnAdd.setText(maxed ? R.string.fp_vote_add_more_max : R.string.fp_vote_add_more);
        mBtnAdd.setTextColor(maxed ?
                0xFFB6B6B6 : mContext.getResources().getColor(R.color.btn_white_txt));
    }

    public void setAnonyVote(boolean anonyVote) {
        mAnonyVote = anonyVote;
    }

    public int getAnonyVote() {
        return mAnonyVote ? 1 : 0;
    }

    public long getEndtime() {
        return mEndTime;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
        mOptionHolder.setVisibility(mEnabled ? View.VISIBLE : View.GONE);
        mPropertyHolder.setVisibility(mEnabled ? View.VISIBLE : View.GONE);
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public boolean hasUpdate() {
        if (!mEnabled) {
            return false;
        }

        int count = mOptionHolder.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = mOptionHolder.getChildAt(i);
            if (view instanceof VoteInputItem && ((VoteInputItem) view).hasUpdate()) {
                return true;
            }
        }
        return false;
    }

    public boolean isReady() {
        return !mEnabled || (isOptionsReady() && hasExpireOn());
    }

    public boolean isOptionsReady() {
        int count = mOptionHolder.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = mOptionHolder.getChildAt(i);
            if (view instanceof VoteInputItem && ((VoteInputItem) view).isEmpty()) {
                toast(R.string.fp_tst_vote_option_less);
                return false;
            }
        }
        return true;
    }

    public boolean hasExpireOn() {
        if (mEndTime > 0) {
            return true;
        } else {
            toast(R.string.fp_tst_vote_endtime_empty);
            return false;
        }
    }

    private void toast(int msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    public String getOptions() {
        JSONArray ja = new JSONArray();
        int count = mOptionHolder.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = mOptionHolder.getChildAt(i);
            if (view instanceof VoteInputItem) {
                ja.put(((VoteInputItem) view).getOption());
            }
        }
        return ja.toString();
    }

    private void showVoteTypeDialog() {
        if (mVoteTypeDialog == null) {
            mVoteTypeDialog = new LightDialog(mContext)
                    .setTitleLd(R.string.fp_vote_type)
                    .setItems(R.array.fp_vote_type_choices, this);
        }
        mVoteTypeDialog.show();
    }

    private void updateEndTime() {
        ((CardItemConf) mPropertyHolder.findViewById(R.id.cif_vote_time))
                .setContent(DateFormat.getDateTimeInstance().format(new Date(mEndTime)));
    }

    private void updateVoteType() {
        int resId = mAnonyVote ? R.string.fp_vote_type_anony : R.string.fp_vote_type_normal;
        ((CardItemConf) mPropertyHolder.findViewById(R.id.cif_vote_type)).setContent(resId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_option:
                addOption(null);
                break;
            case R.id.iv_delete:
                if (v.getParent() instanceof VoteInputItem) {
                    deleteOption((View) v.getParent());
                }
                break;

            case R.id.cif_vote_type:
                showVoteTypeDialog();
                break;

            case R.id.cif_vote_time:
                mTimePicker.pick(mEndTime, 0);
                break;

            default:
                break;
        }
    }

    @Override
    public void onResult(Calendar time, int requestCode) {
        mEndTime = time.getTimeInMillis();
        updateEndTime();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        setAnonyVote(which == 1);
        updateVoteType();
    }
}
