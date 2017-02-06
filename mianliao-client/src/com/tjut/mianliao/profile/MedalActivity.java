package com.tjut.mianliao.profile;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ConfirmDialog;
import com.tjut.mianliao.component.ProgressButton;
import com.tjut.mianliao.data.Medal;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.SnsHelper;

public class MedalActivity extends BaseActivity implements View.OnClickListener {

    public static final String EXTRA_PRIMARY_MEDALS = "extra_primary_medals";

    private UserInfo mUserInfo;
    private ArrayList<Medal> mPrimaryMedals;
    private MedalAdapter mAdapter;
    private PrimaryMedalTask mLastTask;
    private int mMaxMedalsCount;

    private ProgressButton mPbAction;

    private SnsHelper mSnsShareHelper;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_medal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = Settings.getInstance(this);

        mUserInfo = getIntent().getParcelableExtra(UserInfo.INTENT_EXTRA_INFO);
        if (mUserInfo == null || mUserInfo.medalCount() == 0) {
            toast(R.string.mdl_no_medal);
            finish();
            return;
        }
        mMaxMedalsCount = getResources().getInteger(R.integer.max_primary_medals);
        mPrimaryMedals = mUserInfo.getPrimaryMedals();

        mSnsShareHelper = SnsHelper.getInstance();

        getTitleBar().showTitleText(getString(
                R.string.mdl_title, mUserInfo.getDisplayName(this)), null);
        getTitleBar().showRightButton(R.drawable.btn_title_bar_badges, this);

        mPbAction = (ProgressButton) findViewById(R.id.pb_action);
        mPbAction.setOnClickListener(this);

        ListView lvMedals = (ListView) findViewById(R.id.lv_medals);
        lvMedals.addHeaderView(mInflater.inflate(R.layout.list_header_medals, lvMedals, false));
        lvMedals.addFooterView(new View(this));

        boolean actionEnabled = mUserInfo.isMine(this);
        TextView tvHint = (TextView) findViewById(R.id.tv_hint);
        if (actionEnabled) {
            tvHint.setText(getString(R.string.mdl_set_hint, mMaxMedalsCount));
        } else {
            tvHint.setVisibility(View.GONE);
            findViewById(R.id.fl_action).setVisibility(View.GONE);
        }

        mAdapter = new MedalAdapter(this);
        mAdapter.setActionEnabled(actionEnabled);
        mAdapter.reset(mUserInfo.getMedals());
        lvMedals.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
//        if (mSnsShareHelper != null) {
//            mSnsShareHelper.closeShareBoard();
//        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSnsShareHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_right:
                Intent iMedal = new Intent(this, AllMedalsActivity.class);
                startActivity(iMedal);
                break;

            case R.id.iv_pick:
                if (v.getTag() != null && v.getTag() instanceof Medal) {
                    Medal medal = (Medal) v.getTag();
                    if (medal.isPrimary()) {
                        mPrimaryMedals.remove(medal);
                        medal.primary = 0;
                        mAdapter.notifyDataSetChanged();
                    } else if (mPrimaryMedals.size() < mMaxMedalsCount) {
                        mPrimaryMedals.add(medal);
                        medal.primary = mPrimaryMedals.size();
                        mAdapter.notifyDataSetChanged();
                    } else {
                        toast(getString(R.string.mdl_set_tst_max_num, mMaxMedalsCount));
                    }
                }
                break;

            case R.id.iv_share:
                if (v.getTag() != null && v.getTag() instanceof Medal) {
                    mSnsShareHelper.openShareBoard(this, (Medal) v.getTag());
                }
                break;

            case R.id.pb_action:
                if (mLastTask == null) {
                    if (mPrimaryMedals.isEmpty()) {
                        ConfirmDialog.show(this, R.string.mdl_unset_hint, new Runnable() {
                            @Override
                            public void run() {
                                new PrimaryMedalTask("0").executeLong();
                            }
                        });
                    } else {
                        StringBuilder ids = new StringBuilder();
                        for (Medal medal : mPrimaryMedals) {
                            ids.append(medal.id).append(",");
                        }
                        new PrimaryMedalTask(ids.toString()).executeLong();
                    }
                }
                break;

            default:
                break;
        }
    }

    private class PrimaryMedalTask extends MsTask {
        private String mIds;

        public PrimaryMedalTask(String ids) {
            super(getApplicationContext(), MsRequest.MEDAL_SET_PRIMARY_BADGES);
            mIds = ids;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("badge_ids=").append(mIds).toString();
        }

        @Override
        protected void onPreExecute() {
            mPbAction.setInProgress(true);
            mLastTask = this;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPbAction.setInProgress(false);
            mLastTask = null;
            if (response.isSuccessful()) {
                ArrayList<Medal> medals = JsonUtil.getArray(
                        response.getJsonArray(), Medal.TRANSFORMER);
                setResult(RESULT_UPDATED, new Intent().putParcelableArrayListExtra(
                        EXTRA_PRIMARY_MEDALS, medals));
                finish();
            } else {
                response.showFailInfo(getRefContext(), R.string.mdl_set_tst_failed);
            }
        }
    }
}
