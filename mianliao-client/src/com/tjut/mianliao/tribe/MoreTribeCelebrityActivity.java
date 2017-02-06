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
import android.widget.RelativeLayout;
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
    @ViewInject(R.id.view_no_content)
    private View mViewNoContent;

    private ArrayList<TribeMenInfo> mCelebritys;
    private int mType, mTribeId;
    private TopListAdapter mAdapter;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_more_tribe_celebrity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
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
            if (!TextUtils.isEmpty(chief.avatar)) {
                holder.mIvAvata.setImage(chief.avatar , getDefaultAvatar(chief));
            } else {
                Picasso.with(MoreTribeCelebrityActivity.this)
                    .load(getDefaultAvatar(chief))
                    .into(holder.mIvAvata);
            }
            if (chief.isModerator) {
                holder.mIvIsModerator.setImageResource(R.drawable.icon_muster_section);
                if (chief.mMedals.size() > 0 && chief.mMedals.get(0).medalPic != null) {
                    Picasso.with(MoreTribeCelebrityActivity.this)
                    .load(chief.mMedals.get(0).medalPic)
                    .into(holder.mIvMrdal1);
                }
                if (chief.mMedals.size() > 1 && chief.mMedals.get(1).medalPic != null) {
                    Picasso.with(MoreTribeCelebrityActivity.this)
                    .load(chief.mMedals.get(1).medalPic)
                    .into(holder.mIvMrdal2);
                }
                if (chief.mMedals.size() > 2 && chief.mMedals.get(2).medalPic != null) {
                    Picasso.with(MoreTribeCelebrityActivity.this)
                    .load(chief.mMedals.get(2).medalPic)
                    .into(holder.mIvMrdal3);
                } 
            } else {
                
                if (chief.mMedals.size() > 0 && chief.mMedals.get(0).medalPic != null) {
                    Picasso.with(MoreTribeCelebrityActivity.this)
                    .load(chief.mMedals.get(0).medalPic)
                    .into(holder.mIvIsModerator);
                }
                if (chief.mMedals.size() > 1 && chief.mMedals.get(1).medalPic != null) {
                    Picasso.with(MoreTribeCelebrityActivity.this)
                    .load(chief.mMedals.get(1).medalPic)
                    .into(holder.mIvMrdal1);
                }
                if (chief.mMedals.size() > 2 && chief.mMedals.get(2).medalPic != null) {
                    Picasso.with(MoreTribeCelebrityActivity.this)
                    .load(chief.mMedals.get(2).medalPic)
                    .into(holder.mIvMrdal2);
                } 
                if (chief.mMedals.size() > 3 && chief.mMedals.get(3).medalPic != null) {
                    Picasso.with(MoreTribeCelebrityActivity.this)
                    .load(chief.mMedals.get(3).medalPic)
                    .into(holder.mIvMrdal3);
                }
                if (chief.mMedals.size() > 3) {
                    Picasso.with(MoreTribeCelebrityActivity.this)
                        .load(chief.mMedals.get(3).medalPic)
                        .into(holder.mIvMrdal3);
                } 
            }
            if (mType == 1) {
                holder.mTvPraise.setText(R.string.has_gain_good); 
                holder.mIvPromiseLogo.setImageResource(R.drawable.buttom_like);
                holder.mTvPraiseNum.setText(String.valueOf(chief.praise));
            } else {
                holder.mIvPromiseLogo.setImageResource(R.drawable.up);
                holder.mTvPraise.setText(R.string.has_up_num);
                holder.mTvPraiseNum.setText(String.valueOf(chief.assist));
            }
            holder.mTvUserName.setText(chief.nickName);
            holder.mIvUserGander.setImageResource(chief.sex == 0 ? 
                    R.drawable.img_girl : R.drawable.img_boy);
            holder.mTvSchoolName.setText(chief.schoolName);
           
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
    
    private int getDefaultAvatar(TribeMenInfo chief) {
        return chief.sex == 0 ? R.drawable.chat_botton_bg_favicongirl : R.drawable.chat_botton_bg_faviconboy;
    }

    
    private OnClickListener mTopListClickListen = new OnClickListener() {

        @Override
        public void onClick(View v) {
            TopListViewHoder hodle = (TopListViewHoder) v.getTag();
            TribeMenInfo tribeMen = hodle.mTribeMen;
            Intent intent = new Intent(MoreTribeCelebrityActivity.this, NewProfileActivity.class);
            UserInfo user = new UserInfo();
            user.userId = tribeMen.uid;
            intent.putExtra(UserInfo.INTENT_EXTRA_INFO, user);
            startActivity(intent);
        }
    };

    private class TopListViewHoder {
        @ViewInject(R.id.tv_num_praise)
        TextView mTvPraiseNum;
        @ViewInject(R.id.av_avatar)
        ProAvatarView mIvAvata;
        @ViewInject(R.id.tv_user_name)
        TextView mTvUserName;
        @ViewInject(R.id.iv_user_gender)
        ImageView mIvUserGander;
        @ViewInject(R.id.tv_school_name)
        TextView mTvSchoolName;
        @ViewInject(R.id.line_right)
        View mLineRight;
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
                    if (users.size() > 0) {
                        mViewNoContent.setVisibility(View.GONE);
                    }
                }
            }
        }

    }

}
