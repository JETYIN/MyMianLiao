package com.tjut.mianliao.forum;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.CardItemConf;

public class EventEditHelper implements View.OnClickListener, TimePicker.Callback {

    private static final int REQ_START_AT = 1;
    private static final int REQ_REG_DEADLINE = 2;

    private Context mContext;
    private ViewStub mStub;
    private View mRootView;
    private CfPost mPost;
    private TimePicker mTimePicker;
    private DateFormat mDateFormat;

    private boolean mEnabled = false;
    private Event mEvent;

    private CardItemConf mCifStartAt;
    private CardItemConf mCifRegDeadline;
    private EditText mEtFeature;
    private EditText mEtContact;
    private EditText mEtLocation;
    private EditText mEtQuota;
    private EditText mEtCost;

    public EventEditHelper(Context context, CfPost post, ViewStub stub) {
        mContext = context;
        mPost = post;
        mStub = stub;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
        if (mEnabled && mRootView == null) {
            init();
        }
        if (mRootView != null) {
            mRootView.setVisibility(mEnabled ? View.VISIBLE : View.GONE);
        }
    }

    private void init() {
        mTimePicker = new TimePicker(mContext);
        mTimePicker.setCallback(this);
        mDateFormat = DateFormat.getDateTimeInstance();

        mRootView = mStub.inflate();
        mCifStartAt = (CardItemConf) mRootView.findViewById(R.id.cif_start_at);
        mCifRegDeadline = (CardItemConf) mRootView.findViewById(R.id.cif_reg_deadline);
        mEtFeature = (EditText) mRootView.findViewById(R.id.et_feature);
        mEtContact = (EditText) mRootView.findViewById(R.id.et_contact);
        mEtLocation = (EditText) mRootView.findViewById(R.id.et_location);
        mEtQuota = (EditText) mRootView.findViewById(R.id.et_quota);
        mEtCost = (EditText) mRootView.findViewById(R.id.et_cost);

        mCifStartAt.setOnClickListener(this);
        mCifRegDeadline.setOnClickListener(this);

        if (mPost.hasEvent()) {
            mEvent = mPost.event.copy();
            mEtFeature.setText(mEvent.feature);
            mEtLocation.setText(mEvent.location);
            mEtContact.setText(mEvent.contact);
            mEtQuota.setText(mEvent.quota);
            mEtCost.setText(mEvent.cost);
            if (mEvent.startAt > 0) {
                mCifStartAt.setContent(mDateFormat.format(new Date(mEvent.startAt * 1000)));
            }
            if (mEvent.regDeadline > 0) {
                mCifRegDeadline.setContent(mDateFormat.format(new Date(mEvent.regDeadline * 1000)));
            }
        } else {
            mPost.event = new Event();
            mEvent = new Event();
        }
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public boolean hasUpdate() {
        if (!mEnabled) {
            return false;
        }
        return !mEvent.equals(mPost.event);
    }

    public boolean isReady() {
        if (mEnabled) {
            collectInfo();
        }
        return true;
    }

    public Event getResult() {
        return mEvent;
    }

    private void collectInfo() {
        mEvent.feature = mEtFeature.getText().toString();
        mEvent.contact = mEtContact.getText().toString();
        mEvent.location = mEtLocation.getText().toString();
        mEvent.quota = mEtQuota.getText().toString();
        mEvent.cost = mEtCost.getText().toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cif_start_at:
                mTimePicker.pick(mEvent.startAt * 1000, REQ_START_AT);
                break;
            case R.id.cif_reg_deadline:
                mTimePicker.pick(mEvent.regDeadline * 1000, REQ_REG_DEADLINE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onResult(Calendar time, int requestCode) {
        switch (requestCode) {
            case REQ_START_AT:
                mEvent.startAt = time.getTimeInMillis() / 1000;
                mCifStartAt.setContent(mDateFormat.format(time.getTime()));
                break;
            case REQ_REG_DEADLINE:
                mEvent.regDeadline = time.getTimeInMillis() / 1000;
                mCifRegDeadline.setContent(mDateFormat.format(time.getTime()));
                break;
            default:
                break;
        }
    }
}
