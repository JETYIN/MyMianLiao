package com.tjut.mianliao.tribe;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.ExpandableGridView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.tribe.TribeMenInfo;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.theme.ThemeTextView;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class MoreTribeCelebrityActivity extends BaseActivity {

    public final static String CELEBRITY_TYPE = "celebrity_type";
    public final static int CELEBRITY_TYPE_POWER = 2;
    public final static int CELEBRITY_TYPE_CHARM = 1;

    @ViewInject(R.id.gv_tribe_celebrity)
    private ExpandableGridView mGvCelebrity;
    @ViewInject(R.id.ll_title_charm_list)
    private RelativeLayout mRlCharmTitle;
    @ViewInject(R.id.ll_title_power_list)
    private RelativeLayout mRlPowerTitle;

    private ArrayList<TribeMenInfo> mCelebritys;
    private int mType, mTribeId;
    private TopListAdapter mAdapter;
    private boolean mIsNightMode;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_more_tribe_celebrity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mIsNightMode = Settings.getInstance(this).isNightMode();
        getTitleBar().setTitle(R.string.tribe_celbrity);
        mCelebritys = new ArrayList<TribeMenInfo>();
        mTribeId = getIntent().getIntExtra(TribeCelebrityActivity.EXT_TRIBE_ID, 0);
        mType = getIntent().getIntExtra(CELEBRITY_TYPE, 1);
        mAdapter = new TopListAdapter();
        mGvCelebrity.setAdapter(mAdapter);
        if (mType == CELEBRITY_TYPE_CHARM) {
            mRlCharmTitle.setVisibility(View.VISIBLE);
            mRlPowerTitle.setVisibility(View.GONE);
        } else {
            mRlCharmTitle.setVisibility(View.GONE);
            mRlPowerTitle.setVisibility(View.VISIBLE);
        }
        new GetTribeChiefTask().executeLong();
        checkDayNightUI();
    }

    private void checkDayNightUI() {
        if (mIsNightMode) {
            findViewById(R.id.ll_content).setBackgroundResource(R.drawable.bg);
        }
    }

    public class TopListAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return mCelebritys.size();
        }

        @Override
        public TribeMenInfo getItem(int position) {
            return mCelebritys.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TopListViewHoder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_top_list, parent, false);
                holder = new TopListViewHoder();
                ViewUtils.inject(holder, convertView);
                convertView.setTag(holder);
            } else {
                holder = (TopListViewHoder) convertView.getTag();
            }
            TribeMenInfo chief = getItem(position);
            Picasso.with(MoreTribeCelebrityActivity.this).load(chief.avatar).into(holder.mIvAvata);
            holder.mTvUserName.setText(chief.nickName);
            holder.mIvUserGander.setImageResource(chief.sex == 0 ? 
                    R.drawable.ic_female : R.drawable.ic_male);
            holder.mTvSchoolName.setText(chief.schoolName);
            holder.mTribeMen = chief;
            holder.mTribeMen = chief;
            convertView.setOnClickListener(mTopListClickListen);
            if (position % 2 == 0) {
                holder.mLineRight.setVisibility(View.VISIBLE);
            } else {
                holder.mLineRight.setVisibility(View.VISIBLE);
            }
            return convertView;
        }

    }

    private OnClickListener mTopListClickListen = new OnClickListener() {

        @Override
        public void onClick(View v) {
            TopListViewHoder hodle = (TopListViewHoder) v.getTag();
            TribeMenInfo tribeMen = hodle.mTribeMen;
            Intent intent = new Intent(MoreTribeCelebrityActivity.this, ProfileActivity.class);
            UserInfo user = new UserInfo();
            user.userId = tribeMen.uid;
            intent.putExtra(UserInfo.INTENT_EXTRA_INFO, user);
            startActivity(intent);
        }
    };

    private class TopListViewHoder {
        @ViewInject(R.id.av_avatar)
        AvatarView mIvAvata;
        @ViewInject(R.id.tv_user_name)
        ThemeTextView mTvUserName;
        @ViewInject(R.id.iv_user_gender)
        ProImageView mIvUserGander;
        @ViewInject(R.id.tv_school_name)
        ThemeTextView mTvSchoolName;
        @ViewInject(R.id.line_right)
        View mLineRight;
        TribeMenInfo mTribeMen;
    }

    private class GetTribeChiefTask extends MsTask {

        public GetTribeChiefTask() {
            super(MoreTribeCelebrityActivity.this, MsRequest.TRIBE_LIST_MEN_RANK);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("rank_type=").append(mType).append("&tribe_id=").append(mTribeId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ArrayList<TribeMenInfo> users = JsonUtil.getArray(response.getJsonArray(), TribeMenInfo.TRANSFORMER);
                if (users != null) {
                    mCelebritys.clear();
                    mCelebritys.addAll(users);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }

    }

}
