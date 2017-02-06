package com.tjut.mianliao.tribe;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

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
import com.tjut.mianliao.theme.ThemeLineView;
import com.tjut.mianliao.theme.ThemeTextView;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class TribeCelebrityActivity extends BaseActivity implements OnClickListener{

    public static final String EXT_TRIBE_ID = "ext_tribe_id";

    @ViewInject(R.id.gv_tribe_chief)
    ExpandableGridView mGvTribeChief;
    @ViewInject(R.id.gv_charm_list)
    ExpandableGridView mGvCharmList;
    @ViewInject(R.id.gv_power_list)
    ExpandableGridView mGvPowerList;
    
    private ArrayList<TribeMenInfo> mTribeChiefs;
    private ArrayList<TribeMenInfo> mCharmUsers;
    private ArrayList<TribeMenInfo> mPowerUsers;
    private ArrayList<TribeMenInfo> mTribeMens;
    private TribeChiefAdapter mTribeChiefAdapter;
    private TopListAdapter mCharmListAdapter, mPowerListAdapter;
    private int mTribeId;
    private boolean mIsNightMode;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_tribe_celebrity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mIsNightMode = Settings.getInstance(this).isNightMode();
        getTitleBar().setTitle(R.string.tribe_celbrity);
        mTribeId = getIntent().getIntExtra(EXT_TRIBE_ID, 0);
        mTribeChiefs = new ArrayList<TribeMenInfo>();
        mCharmUsers = new ArrayList<TribeMenInfo>();
        mPowerUsers = new ArrayList<TribeMenInfo>();
        mTribeMens = new ArrayList<TribeMenInfo>();
        mTribeChiefAdapter = new TribeChiefAdapter();
        mCharmListAdapter = new TopListAdapter();
        mPowerListAdapter = new TopListAdapter();
        mGvTribeChief.setAdapter(mTribeChiefAdapter);
        mGvCharmList.setAdapter(mCharmListAdapter);
        mGvPowerList.setAdapter(mPowerListAdapter);
        updataView();
        checkDayNightUI();
    }

    private void checkDayNightUI() {
        if (mIsNightMode) {
            findViewById(R.id.sc_content).setBackgroundResource(R.drawable.bg);
        }
    }

    private void updataView() {
        new GetTribeChiefTask(0, 4).executeLong();
        new GetTribeChiefTask(1, 4).executeLong();
        new GetTribeChiefTask(2, 4).executeLong();
    }

    private class TribeChiefAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mTribeChiefs.size();
        }

        @Override
        public TribeMenInfo getItem(int position) {
            return mTribeChiefs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TribeChiefViewHoder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_tribe_chief, parent, false);
                holder = new TribeChiefViewHoder();
                ViewUtils.inject(holder, convertView);
                convertView.setTag(holder);
            } else {
                holder = (TribeChiefViewHoder) convertView.getTag();
            }
            TribeMenInfo chief = getItem(position);
            chifeIsShowLine(getCount(), position, holder);
            Picasso.with(TribeCelebrityActivity.this)
            	.load(chief.avatar)
            	.into(holder.mIvAvata);
            holder.mTvUserName.setText(chief.nickName);
            holder.mIvUserGander.setImageResource(chief.sex == 0 ? 
                    R.drawable.img_girl : R.drawable.img_boy);
            holder.mTvSchoolName.setText(chief.schoolName);
            holder.mTribeMen = chief;
            convertView.setOnClickListener(mTribeChiefClickListen);
            return convertView;
        }
    }

    private void chifeIsShowLine(int size, int position, TribeChiefViewHoder holder) {
        if (position % 2 > 0) {
            holder.mLineRight.setVisibility(View.GONE);
        } else {
            holder.mLineRight.setVisibility(View.VISIBLE);
        }
    }

    private class TribeChiefViewHoder {
        @ViewInject(R.id.av_avatar)
        AvatarView mIvAvata;
        @ViewInject(R.id.tv_user_name)
        ThemeTextView mTvUserName;
        @ViewInject(R.id.iv_user_gender)
        ProImageView mIvUserGander;
        @ViewInject(R.id.line_right)
        ThemeLineView mLineRight;
        @ViewInject(R.id.tv_school_name)
        ThemeTextView mTvSchoolName;
        
        TribeMenInfo mTribeMen;
    }

    private OnClickListener mTribeChiefClickListen = new OnClickListener() {

        @Override
        public void onClick(View v) {
            TribeChiefViewHoder hodle = (TribeChiefViewHoder) v.getTag();
            TribeMenInfo tribeMen = hodle.mTribeMen;
            Intent intent = new Intent(TribeCelebrityActivity.this, ProfileActivity.class);
            UserInfo user = new UserInfo();
            user.userId = tribeMen.uid;
            intent.putExtra(UserInfo.INTENT_EXTRA_INFO, user);
            startActivity(intent);
        }
    };

   
    
    public class TopListAdapter extends BaseAdapter {

        public void resetDatas(ArrayList<TribeMenInfo> users){
            mTribeMens.clear();
            mTribeMens.addAll(users); 
            notifyDataSetChanged();
        }
        
        @Override
        public int getCount() {
            return mTribeMens.size();
        }

        @Override
        public TribeMenInfo getItem(int position) {
            return mTribeMens.get(position);
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
            Picasso.with(TribeCelebrityActivity.this).load(chief.avatar).into(holder.mIvAvata);
            holder.mTvUserName.setText(chief.nickName);
            holder.mIvUserGander.setImageResource(chief.sex == 0 ? 
                    R.drawable.img_girl : R.drawable.img_boy);
            holder.mTvSchoolName.setText(chief.schoolName);
            holder.mTvPraiseNum.setText(String.valueOf(chief.praise));
            holder.mTribeMen = chief;
            holder.mTribeMen = chief;
            convertView.setOnClickListener(mTopListClickListen);
            switch (position) {
                case 0:
                    holder.mLineLeft.setVisibility(View.VISIBLE);
                    holder.mLineTop.setVisibility(View.VISIBLE);
                    holder.mLineRight.setVisibility(View.GONE);
                    holder.mLineBottom.setVisibility(View.GONE);
                    break;
                case 1:
                    holder.mLineLeft.setVisibility(View.GONE);
                    holder.mLineTop.setVisibility(View.GONE);
                    holder.mLineRight.setVisibility(View.VISIBLE);
                    holder.mLineBottom.setVisibility(View.GONE);
                    break;
                case 2:
                    holder.mLineLeft.setVisibility(View.GONE);
                    holder.mLineTop.setVisibility(View.GONE);
                    holder.mLineRight.setVisibility(View.GONE);
                    holder.mLineBottom.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
            return convertView;
        }

    }

    private class TopListViewHoder {
        @ViewInject(R.id.av_avatar)
        AvatarView mIvAvata;
        @ViewInject(R.id.tv_user_name)
        ThemeTextView mTvUserName;
        @ViewInject(R.id.iv_user_gender)
        ProImageView mIvUserGander;
        @ViewInject(R.id.tv_school_name)
        ThemeTextView mTvSchoolName;
        @ViewInject(R.id.tv_num_praise)
        ThemeTextView mTvPraiseNum;
        @ViewInject(R.id.line_horizontal_left)
        View mLineLeft;
        @ViewInject(R.id.line_horizontal_right)
        View mLineRight;
        @ViewInject(R.id.line_vertical_top)
        View mLineTop;
        @ViewInject(R.id.line_vertical_bottom)
        View mLineBottom;
        TribeMenInfo mTribeMen;
    }

    private OnClickListener mTopListClickListen = new OnClickListener() {

        @Override
        public void onClick(View v) {
            TopListViewHoder hodle = (TopListViewHoder) v.getTag();
            TribeMenInfo tribeMen = hodle.mTribeMen;
            Intent intent = new Intent(TribeCelebrityActivity.this, ProfileActivity.class);
            UserInfo user = new UserInfo();
            user.userId = tribeMen.uid;
            intent.putExtra(UserInfo.INTENT_EXTRA_INFO, user);
            startActivity(intent);
        }
    };

    private class GetTribeChiefTask extends MsTask {

        private int mType;
        private int mLimit;

        public GetTribeChiefTask(int type, int limit) {
            super(TribeCelebrityActivity.this, MsRequest.TRIBE_LIST_MEN_RANK);
            mType = type;
            mLimit = limit;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("rank_type=").append(mType)
                    .append("&limit=").append(mLimit).append("&tribe_id=")
                    .append(mTribeId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                final ArrayList<TribeMenInfo> users = JsonUtil.getArray(
                        response.getJsonArray(), TribeMenInfo.TRANSFORMER);
                if (users != null) {
                    switch (mType) {
                        case 0:
                            mTribeChiefs.clear();
                            mTribeChiefs.addAll(users);
                            mTribeChiefAdapter.notifyDataSetChanged();
                            break;
                        case 1:
                            mCharmUsers.clear();
                            mCharmUsers.addAll(users);
                            mCharmListAdapter.resetDatas(mCharmUsers);
                            break;
                        case 2:
                            mPowerUsers.clear();
                            mPowerUsers.addAll(users);
                            mPowerListAdapter.resetDatas(mPowerUsers);
                            break;
                        default:
                            break;
                    }
                }
            }
        }

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(TribeCelebrityActivity.this, MoreTribeCelebrityActivity.class);
        intent.putExtra(EXT_TRIBE_ID, mTribeId);
        switch (v.getId()) {
            case R.id.ll_title_charm_list:
                intent.putExtra(MoreTribeCelebrityActivity.CELEBRITY_TYPE, 1);
                startActivity(intent);
                break;
            case R.id.ll_title_power_list:
                intent.putExtra(MoreTribeCelebrityActivity.CELEBRITY_TYPE, 2);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
    

}
