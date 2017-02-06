package com.tjut.mianliao.component;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.MapActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.LatLngWrapper;
import com.tjut.mianliao.data.bounty.BountyTask;
import com.tjut.mianliao.util.Utils;

public class BountyView extends LinearLayout implements OnClickListener {

    private BountyTask mTask;
    private boolean mLayoutInflated;

    public BountyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    public void show(BountyTask task) {
        mTask = task;
        if (!mLayoutInflated) {
            mLayoutInflated = true;
            inflate(getContext(), R.layout.comp_bounty_view, this);
        }
        showContent();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.smv_location:
                showMap();
                break;
            default:
                break;
        }
    }

    private void showContent() {
        Utils.setText(this, R.id.tv_desc, Utils.getRefFriendText(mTask.desc, getContext()));

        FlexibleImageView ivImages = (FlexibleImageView) findViewById(R.id.fiv_images);
        ivImages.setMaxCount(Integer.MAX_VALUE);
        ivImages.setImages(mTask.images);

        if (mTask.location != null) {
            StaticMapView smv = (StaticMapView) findViewById(R.id.smv_location);
            smv.setOnClickListener(this);
            smv.showMap(mTask.location);
        }

        showField(R.id.tv_reward, R.string.btyct_reward, mTask.reward);
        showField(R.id.tv_contact, R.string.btyct_contact, mTask.contact);
        showField(R.id.tv_place, R.string.btyct_place, mTask.place);
        CharSequence deadline = DateUtils.getRelativeTimeSpanString(mTask.reqDeadline * 1000,
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        showField(R.id.tv_deadline, R.string.btyct_deadline, deadline);
    }

    private void showField(int viewId, int resId, CharSequence content) {
        TextView view = (TextView) findViewById(viewId);
        if (TextUtils.isEmpty(content)) {
            view.setVisibility(GONE);
        } else {
            view.setVisibility(VISIBLE);
            view.setText(getContext().getString(resId, content));
        }
    }

    private void showMap() {
        Intent iMap = new Intent(getContext(), MapActivity.class);
        iMap.putExtra(MapActivity.EXTRA_PICK_LOCATION, false);
        iMap.putExtra(MapActivity.EXTRA_LOCATION, new LatLngWrapper(mTask.location));
        getContext().startActivity(iMap);
    }
}
