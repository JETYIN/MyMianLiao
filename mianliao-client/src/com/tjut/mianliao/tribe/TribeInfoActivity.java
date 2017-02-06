package com.tjut.mianliao.tribe;

import java.util.ArrayList;

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
import com.tjut.mianliao.data.tribe.TribeInfo;
import com.tjut.mianliao.data.tribe.TribeMenInfo;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class TribeInfoActivity extends BaseActivity {

    @ViewInject(R.id.tv_tribe_name)
    TextView mTvTribeName;
    @ViewInject(R.id.tv_tribe_desc)
    TextView mTvTribeDesc;
    @ViewInject(R.id.tv_tribe_type)
    TextView mTvTribeType;
    @ViewInject(R.id.tv_tribe_rule)
    TextView mTvTribeRule;
    @ViewInject(R.id.gv_tribe_chief)
    ExpandableGridView mGvTribeChief;

    private ArrayList<TribeMenInfo> mTribeChiefs;
    private TribeInfo mTribeInfo;
    
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_tribe_info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mTribeInfo = getIntent().getParcelableExtra(TribeInfo.INTENT_EXTRA_INFO);
        getTitleBar().setTitle(R.string.tribe_info);
        mTribeChiefs = new ArrayList<TribeMenInfo>();
        mGvTribeChief.setAdapter(mChiefAdapter);
        mTvTribeName.setText(mTribeInfo.tribeName);
        mTvTribeDesc.setText(mTribeInfo.tribeDesc);
        mTvTribeType.setText(mTribeInfo.getTypeName(this, mTribeInfo.tribeType));
        mTvTribeRule.setText(mTribeInfo.rule);
        new GetTribeChiefTask(0, 20, mTribeInfo.tribeId).executeLong();
    }

    private BaseAdapter mChiefAdapter = new BaseAdapter() {

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
            if (!TextUtils.isEmpty(chief.avatar)) {
                Picasso.with(TribeInfoActivity.this)
                    .load(AliImgSpec.USER_AVATAR.makeUrl(chief.avatar))
                    .placeholder(getDefaultAvatar(chief))
                    .into(holder.mIvAvata);
            } else {
                Picasso.with(TribeInfoActivity.this)
                    .load(getDefaultAvatar(chief))
                    .into(holder.mIvAvata);
            }
            holder.mTvUserName.setText(chief.nickName);
            holder.mIvUserGander.setImageResource(chief.sex == 0 ?
                    R.drawable.ic_female : R.drawable.ic_male);
            holder.mTvSchoolName.setText(chief.schoolName);
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
    };

    private int getDefaultAvatar(TribeMenInfo chief) {
        return chief.sex == 0 ? R.drawable.chat_botton_bg_favicongirl : R.drawable.chat_botton_bg_faviconboy;
    }
    
    private OnClickListener mTopListClickListen = new OnClickListener() {

        @Override
        public void onClick(View v) {
            TribeChiefViewHoder holder = (TribeChiefViewHoder) v.getTag();
        }
    };

    private class TribeChiefViewHoder {
        @ViewInject(R.id.av_avatar)
        AvatarView mIvAvata;
        @ViewInject(R.id.tv_user_name)
        TextView mTvUserName;
        @ViewInject(R.id.iv_user_gender)
        ImageView mIvUserGander;
        @ViewInject(R.id.tv_school_name)
        TextView mTvSchoolName;
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
    
    private class GetTribeChiefTask extends MsTask {

        private int mType;
        private int mLimit;
        private int mTribeId;

        public GetTribeChiefTask(int type, int limit, int tribeId) {
            super(TribeInfoActivity.this, MsRequest.TRIBE_LIST_MEN_RANK);
            mType = type;
            mLimit = limit;
            mTribeId = tribeId;
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
                ArrayList<TribeMenInfo> users = JsonUtil.getArray(
                        response.getJsonArray(), TribeMenInfo.TRANSFORMER);
                if (users != null) {
                    mTribeChiefs.clear();
                    mTribeChiefs.addAll(users);
                    mChiefAdapter.notifyDataSetChanged();
                }
            }
        }

    }

}