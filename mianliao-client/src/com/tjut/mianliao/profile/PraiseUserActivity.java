package com.tjut.mianliao.profile;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class PraiseUserActivity extends BaseActivity {

    @ViewInject(R.id.tv_total_num)
    private TextView mTvTotalNum;
    @ViewInject(R.id.gv_praise_me)
    private PullToRefreshGridView mGvPraiseMe;

    private ArrayList<UserInfo> mPraiseMes;

    private UserInfo mUserInfo;
    
    private int mUpCount;
    
    private int mUpUserCount;
    
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_praise_user;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        getTitleBar().setTitle(R.string.prof_got_up_num);
        mUserInfo = getIntent().getParcelableExtra(UserInfo.INTENT_EXTRA_INFO);
        mPraiseMes = new ArrayList<UserInfo>();
        mGvPraiseMe.setAdapter(mPriaiseAdapter);
        updateUserCountInfo();
        fetchPraiseUser(true);
    }

    private void updateUserCountInfo() {
        mTvTotalNum.setText(getString(R.string.prof_got_up_desc, mUpUserCount, mUpCount));
    }
    
    private void fetchPraiseUser(boolean refresh) {
        int offset = refresh ? 0 : mPriaiseAdapter.getCount();
        new GetPraiseUsersTask(offset).executeLong();
        if (refresh) {
            new GetPraiseTimesTask().executeLong();
        }
    }

    private BaseAdapter mPriaiseAdapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return mPraiseMes.size();
        }

        @Override
        public UserInfo getItem(int position) {
            return mPraiseMes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_praise_user, parent, false);
                holder = new ViewHolder();
                ViewUtils.inject(holder, convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            UserInfo user = getItem(position);
            holder.mUser = user;
            Picasso.with(PraiseUserActivity.this).load(user.getAvatar())
                    .placeholder(R.drawable.chat_botton_bg_faviconboy).into(holder.mIvAvatar);
            Picasso.with(PraiseUserActivity.this).load(user.gender == 0 ? R.drawable.img_girl : R.drawable.img_boy)
                    .into(holder.mIvGender);
            holder.mTvName.setText(user.nickname);
            holder.mTvSchool.setText(user.school);
            if (position % 2 == 0) {
                holder.mLineLeft.setVisibility(View.VISIBLE);
                holder.mLineRight.setVisibility(View.GONE);
            } else {
                holder.mLineLeft.setVisibility(View.GONE);
                holder.mLineRight.setVisibility(View.VISIBLE);
            }
            convertView.setOnClickListener(mVistorListen);
            return convertView;
        }
    };

    private OnClickListener mVistorListen = new OnClickListener() {

        @Override
        public void onClick(View v) {
            ViewHolder mHolder = (ViewHolder) v.getTag();
            UserInfo  user= mHolder.mUser;
            Intent iProfile = new Intent(PraiseUserActivity.this, NewProfileActivity.class);
            iProfile.putExtra(UserInfo.INTENT_EXTRA_INFO, user);
            startActivity(iProfile);
        }
    };

    private class ViewHolder {
        @ViewInject(R.id.iv_avatar)
        AvatarView mIvAvatar;
        @ViewInject(R.id.tv_name)
        TextView mTvName;
        @ViewInject(R.id.tv_school)
        TextView mTvSchool;
        @ViewInject(R.id.iv_gender)
        ImageView mIvGender;
        @ViewInject(R.id.line_left)
        View mLineLeft;
        @ViewInject(R.id.line_right)
        View mLineRight;
        UserInfo mUser;
    }
    
    private class GetPraiseTimesTask extends MsTask{

        public GetPraiseTimesTask() {
            super(PraiseUserActivity.this, MsRequest.USER_PRAISE_INFO);
        }
        
        @Override
        protected String buildParams() {
            return "query_uid=" + mUserInfo.userId;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mUpCount = response.getJsonObject().optInt("praise_times");
                mUpUserCount = response.getJsonObject().optInt("users_count");
                updateUserCountInfo();
            }
        }
        
    }
    
    private class GetPraiseUsersTask extends MsTask{

        private int mOffset;
        
        public GetPraiseUsersTask(int offset) {
            super(PraiseUserActivity.this, MsRequest.USER_GET_PRICERS);
            mOffset = offset;
        }
        
        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder();
            sb.append("query_uid=").append(mUserInfo.userId)
                .append("&last_index=").append(mOffset);
            return sb.toString();
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ArrayList<UserInfo> users = new ArrayList<>();
                JSONArray ja = response.getJsonArray();
                try {
                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject jo = (JSONObject) ja.get(i);
                        JSONObject userJo = (JSONObject) jo.opt("user");
                        UserInfo userInfo = UserInfo.fromJson(userJo);
                        userInfo.visitTime = jo.optLong("time") * 1000;
                        users.add(userInfo);
                    }
                    mPraiseMes.addAll(users);
                    mPriaiseAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        
    }
    

}
