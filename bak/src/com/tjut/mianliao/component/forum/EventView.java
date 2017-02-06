package com.tjut.mianliao.component.forum;

import java.text.DateFormat;
import java.util.Date;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProgressButton;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.Event;
import com.tjut.mianliao.forum.PostActorsActivity;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class EventView extends LinearLayout implements View.OnClickListener {

    private boolean mLayoutInflated = false;
    private ProgressButton mPbSubmit;
    private CfPost mPost;
    private Activity mActivity;

    public EventView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    public void show(CfPost post) {
        setVisibility(VISIBLE);
        mPost = post;
        if (!mLayoutInflated) {
            mLayoutInflated = true;
            inflate(getContext(), R.layout.comp_forum_event_view, this);
        }
        showEventInfo();
    }

    private void showEventInfo() {
        Event event = mPost.event;
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String startAt = event.startAt == 0 ?
                null : dateFormat.format(new Date(event.startAt * 1000));
        showField(R.id.tv_start_at, startAt);
        showField(R.id.tv_location, event.location);
        showField(R.id.tv_feature, event.feature);
        showField(R.id.tv_contact, event.contact);
        showField(R.id.tv_cost, event.cost);

        String regCountDesc;
        if (TextUtils.isEmpty(event.quota) || "0".equals(event.quota)) {
            regCountDesc = getContext().getString(R.string.fe_reg_count_desc_unlimited,
                    event.regCount);
        } else {
            regCountDesc = getContext().getString(R.string.fe_reg_count_desc, event.regCount,
                    event.quota);
        }
        Utils.setText(this, R.id.tv_reg_count, regCountDesc);

        mPbSubmit = (ProgressButton) findViewById(R.id.pb_submit);
        mPbSubmit.setVisibility(event.enabled ? VISIBLE : GONE);
        TextView tvPart = (TextView) findViewById(R.id.tv_participants);
        tvPart.setVisibility(event.enabled ? GONE : VISIBLE);
        if (event.enabled) {
            mPbSubmit.setText(R.string.fe_register);
            mPbSubmit.setOnClickListener(this);
        } else {
            tvPart.setOnClickListener(this);
        }
    }

    private void showField(int viewId, String content) {
        TextView view = (TextView) findViewById(viewId);
        view.setVisibility(TextUtils.isEmpty(content) ? GONE : VISIBLE);
        view.setText(content);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pb_submit:
                if (!mPbSubmit.isInProgress()) {
                    new EventRegTask().executeLong();
                }
                break;
            case R.id.tv_participants:
                Intent i = new Intent(getContext(), PostActorsActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(CfPost.INTENT_EXTRA_NAME, mPost);
                getContext().startActivity(i);
                break;
            default:
                break;
        }
    }

    private class EventRegTask extends MsTask {

        public EventRegTask() {
            super(getContext(), MsRequest.POST_EXTRA_ACTION);
        }

        @Override
        protected void onPreExecute() {
            mPbSubmit.setInProgress(true);
        }

        @Override
        protected String buildParams() {
            return "thread_id=" + mPost.postId;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPbSubmit.setInProgress(false);
            if (response.isSuccessful()) {
                // update event
                JSONObject json = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);

                mPost.event.enabled = json.optBoolean(Event.ENABLED);
                mPost.event.regCount = json.optInt(Event.REG_COUNT);

                String errMsg = json.optString("err_msg");
                if (!TextUtils.isEmpty(errMsg)) {
                    Toast.makeText(getContext(), errMsg, Toast.LENGTH_SHORT).show();
                }

                showEventInfo();
                if (mActivity != null) {
                    Intent i = new Intent();
                    i.putExtra(CfPost.INTENT_EXTRA_NAME, mPost);
                    mActivity.setResult(BaseActivity.RESULT_UPDATED, i);
                }
            } else {
                Toast.makeText(getRefContext(), MsResponse.getFailureDesc(getRefContext(),
                        R.string.fe_tst_register_failed, response.code), Toast.LENGTH_SHORT).show();
            }

        }
    }
}
