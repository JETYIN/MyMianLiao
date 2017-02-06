package com.tjut.mianliao.tribe;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.ExpandableGridView;
import com.tjut.mianliao.component.ProAvatarView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.tribe.TribeMenInfo;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.theme.ThemeLineView;
import com.tjut.mianliao.util.AliImgSpec;
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
    @ViewInject(R.id.iv_no_content_chief)
    private ImageView mIvNoContentChief;
    @ViewInject(R.id.iv_no_content_charm)
    private ImageView mIvNoContentCharm;
    @ViewInject(R.id.iv_no_content_up)
    private ImageView mIvNoContentUp;
    
    private ArrayList<TribeMenInfo> mTribeChiefs;
    private ArrayList<TribeMenInfo> mCharmUsers;
    private ArrayList<TribeMenInfo> mPowerUsers;
    private ArrayList<TribeMenInfo> mTribeMens;
    private TribeChiefAdapter mTribeChiefAdapter;
    private TopListAdapter mCharmListAdapter, mPowerListAdapter;
    private int mTribeId;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_tribe_celebrity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
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
    }

    private void updataView() {
        new GetTribeChiefTask(0, 4).executeLong();
        new GetTribeChiefTask(1, 4).executeLong();
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
            
            if (chief.mMedals.size() > 0 && chief.mMedals.get(0).medalPic != null) {
                Picasso.with(TribeCelebrityActivity.this)
                .load(chief.mMedals.get(0).medalPic)
                .into(holder.mIvMrdal1);
            }
            if (chief.mMedals.size() > 1 && chief.mMedals.get(1).medalPic != null) {
                Picasso.with(TribeCelebrityActivity.this)
                .load(chief.mMedals.get(1).medalPic)
                .into(holder.mIvMrdal2);
            }
            if (chief.mMedals.size() > 2 && chief.mMedals.get(2).medalPic != null) {
                Picasso.with(TribeCelebrityActivity.this)
                .load(chief.mMedals.get(2).medalPic)
                .into(holder.mIvMrdal3);
            } 
            
            chifeIsShowLine(getCount(), position, holder);
            
            if (!TextUtils.isEmpty(chief.avatar)) {
                holder.mIvAvata.setImage(chief.avatar , getDefaultAvatar(chief));
            } else {
                Picasso.with(TribeCelebrityActivity.this)
                    .load(getDefaultAvatar(chief))
                    .into(holder.mIvAvata);
            }
            holder.mTvUserName.setText(chief.nickName);
            holder.mIvUserGander.setImageResource(chief.sex == 0 ? 
                    R.drawable.img_girl : R.drawable.img_boy);
            holder.mTvSchoolName.setText(chief.schoolName);
            holder.mTribeMen = chief;
            convertView.setOnClickListener(mTribeChiefClickListen);
            return convertView;
        }

    }
    
    private int getDefaultAvatar(TribeMenInfo chief) {
        return chief.sex == 0 ? R.drawable.chat_botton_bg_favicongirl : R.drawable.chat_botton_bg_faviconboy;
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
        ProAvatarView mIvAvata;
        @ViewInject(R.id.tv_user_name)
        TextView mTvUserName;
        @ViewInject(R.id.iv_user_gender)
        ImageView mIvUserGander;
        @ViewInject(R.id.line_right)
        View mLineRight;
        @ViewInject(R.id.tv_school_name)
        TextView mTvSchoolName;
        @ViewInject(R.id.iv_medal1)
        ImageView mIvMrdal1;
        @ViewInject(R.id.iv_medal2)
        ImageView mIvMrdal2;
        @ViewInject(R.id.iv_medal3)
        ImageView mIvMrdal3;
        
        TribeMenInfo mTribeMen;
    }

    private OnClickListener mTribeChiefClickListen = new OnClickListener() {

        @Override
        public void onClick(View v) {
            TribeChiefViewHoder hodle = (TribeChiefViewHoder) v.getTag();
            TribeMenInfo tribeMen = hodle.mTribeMen;
            Intent intent = new Intent(TribeCelebrityActivity.this, NewProfileActivity.class);
            UserInfo user = new UserInfo();
            user.userId = tribeMen.uid;
            intent.putExtra(UserInfo.INTENT_EXTRA_INFO, user);
            startActivity(intent);
        }
    };

   
    
    public class TopListAdapter extends BaseAdapter {
        
        int mType;

        public void resetDatas(ArrayList<TribeMenInfo> users, int type){
            mTribeMens.clear();
            mTribeMens.addAll(users); 
            mType = type;
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
            
            if (chief.isModerator) {
                holder.mIvIsModerator.setImageResource(R.drawable.icon_muster_section);
                if (chief.mMedals.size() > 0 && chief.mMedals.get(0).medalPic != null) {
                    Picasso.with(TribeCelebrityActivity.this)
                    .load(chief.mMedals.get(0).medalPic)
                    .into(holder.mIvMrdal1);
                }
                if (chief.mMedals.size() > 1 && chief.mMedals.get(1).medalPic != null) {
                    Picasso.with(TribeCelebrityActivity.this)
                    .load(chief.mMedals.get(1).medalPic)
                    .into(holder.mIvMrdal2);
                }
                if (chief.mMedals.size() > 2 && chief.mMedals.get(2).medalPic != null) {
                    Picasso.with(TribeCelebrityActivity.this)
                    .load(chief.mMedals.get(2).medalPic)
                    .into(holder.mIvMrdal3);
                } 
            } else {
                
                if (chief.mMedals.size() > 0 && chief.mMedals.get(0).medalPic != null) {
                    Picasso.with(TribeCelebrityActivity.this)
                    .load(chief.mMedals.get(0).medalPic)
                    .into(holder.mIvIsModerator);
                }
                if (chief.mMedals.size() > 1 && chief.mMedals.get(1).medalPic != null) {
                    Picasso.with(TribeCelebrityActivity.this)
                    .load(chief.mMedals.get(1).medalPic)
                    .into(holder.mIvMrdal1);
                }
                if (chief.mMedals.size() > 2 && chief.mMedals.get(2).medalPic != null) {
                    Picasso.with(TribeCelebrityActivity.this)
                    .load(chief.mMedals.get(2).medalPic)
                    .into(holder.mIvMrdal2);
                } 
                if (chief.mMedals.size() > 3 && chief.mMedals.get(3).medalPic != null) {
                    Picasso.with(TribeCelebrityActivity.this)
                    .load(chief.mMedals.get(3).medalPic)
                    .into(holder.mIvMrdal3);
                }
                if (chief.mMedals.size() > 3) {
                    Picasso.with(TribeCelebrityActivity.this)
                        .load(chief.mMedals.get(3).medalPic)
                        .into(holder.mIvMrdal3);
                } 
            }
            
            if (!TextUtils.isEmpty(chief.avatar)) {
                holder.mIvAvata.setImage(chief.avatar , getDefaultAvatar(chief));
            } else {
                Picasso.with(TribeCelebrityActivity.this)
                    .load(getDefaultAvatar(chief))
                    .into(holder.mIvAvata);
            }
            holder.mTvUserName.setText(chief.nickName);
            holder.mIvUserGander.setImageResource(chief.sex == 0 ? 
                    R.drawable.img_girl : R.drawable.img_boy);
            holder.mTvSchoolName.setText(chief.schoolName);
            if (mType == 0) {
                holder.mTvPraise.setText(R.string.has_gain_good);
                holder.mIvPromiseLogo.setImageResource(R.drawable.buttom_like);
                holder.mTvPraiseNum.setText(String.valueOf(chief.praise));
            } else {
                holder.mTvPraise.setText(R.string.has_up_num);
                holder.mIvPromiseLogo.setImageResource(R.drawable.up);
                holder.mTvPraiseNum.setText(String.valueOf(chief.assist));
            }
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
        ProAvatarView mIvAvata;
        @ViewInject(R.id.tv_user_name)
        TextView mTvUserName;
        @ViewInject(R.id.iv_user_gender)
        ImageView mIvUserGander;
        @ViewInject(R.id.tv_school_name)
        TextView mTvSchoolName;
        @ViewInject(R.id.tv_num_praise)
        TextView mTvPraiseNum;
        @ViewInject(R.id.line_horizontal_left)
        View mLineLeft;
        @ViewInject(R.id.line_horizontal_right)
        View mLineRight;
        @ViewInject(R.id.line_vertical_top)
        View mLineTop;
        @ViewInject(R.id.line_vertical_bottom)
        View mLineBottom;
        @ViewInject(R.id.iv_medal1)
        ImageView mIvMrdal1;
        @ViewInject(R.id.iv_medal2)
        ImageView mIvMrdal2;
        @ViewInject(R.id.iv_medal3)
        ImageView mIvMrdal3;
        @ViewInject(R.id.iv_is_moderator)
        ImageView mIvIsModerator;
        @ViewInject(R.id.tv_praise)
        TextView mTvPraise;
        @ViewInject(R.id.iv_promise_logo)
        ImageView mIvPromiseLogo;
        
        TribeMenInfo mTribeMen;
    }

    private OnClickListener mTopListClickListen = new OnClickListener() {

        @Override
        public void onClick(View v) {
            TopListViewHoder hodle = (TopListViewHoder) v.getTag();
            TribeMenInfo tribeMen = hodle.mTribeMen;
            Intent intent = new Intent(TribeCelebrityActivity.this, NewProfileActivity.class);
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
                if (users != null && users.size() > 0) {
                    switch (mType) {
                        case 0:
                            mIvNoContentChief.setVisibility(View.GONE);
                            mTribeChiefs.clear();
                            mTribeChiefs.addAll(users);
                            mTribeChiefAdapter.notifyDataSetChanged();
                            break;
                        case 1:
                            mIvNoContentCharm.setVisibility(View.GONE);
                            mCharmUsers.clear();
                            mCharmUsers.addAll(users);
                            mCharmListAdapter.resetDatas(mCharmUsers, 0);
                            new GetTribeChiefTask(2, 4).executeLong();
                            break;
                        case 2:
                            mIvNoContentUp.setVisibility(View.GONE);
                            mPowerUsers.clear();
                            mPowerUsers.addAll(users);
                            mPowerListAdapter.resetDatas(mPowerUsers, 1);
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
