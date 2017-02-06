package com.tjut.mianliao.profile;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.map.Text;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.contact.FollowUserManager;
import com.tjut.mianliao.contact.FollowUserManager.OnUserFollowListener;
import com.tjut.mianliao.data.FocusUserInfo;
import com.tjut.mianliao.data.RadMenInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.tribe.TribeInfo;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

/**
 * 显示我关注的人列表或关注我的人列表
 * @author YoopWu
 *
 */
public class FollowListActivity extends BaseActivity implements OnClickListener,
        OnRefreshListener2<ListView>, OnUserFollowListener{
    
    public static final String EXT_FOLLOW_TYPE = "ext_follow_type";
    
    public static final int FOLLOW_ME = 1; // 我关注的
    public static final int COLLECTED_ME = 2; //关注我的
    
    @ViewInject(R.id.tv_total_num)
    private TextView mTvTotalNum;
    @ViewInject(R.id.ptr_follow_list)
    private PullToRefreshListView mPtrFollowList;
    
    private ArrayList<FocusUserInfo> mFollows;
    private int totalFollowNum;
    private int mStrResId;
    private MsRequest mRequest;
    
    
    private FollowUserManager mFollowManager;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_my_follow_list;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        int intExtra = getIntent().getIntExtra(EXT_FOLLOW_TYPE, 0);
        mTvTotalNum.setVisibility(View.INVISIBLE);
        if (intExtra == FOLLOW_ME) {
            getTitleBar().setTitle(R.string.prof_follow_me_title);
            mRequest = MsRequest.FRIEND_MY_FOLLOW_LIST;
            mStrResId = R.string.prof_follows_desc;
        } else {
            getTitleBar().setTitle(R.string.prof_collected_me_title);
            mRequest = MsRequest.FRIEND_LIST_FANS;
            mStrResId = R.string.prof_followed_me_desc;
        }
        mFollowManager = FollowUserManager.getInstance(this);
        mFollowManager.registerOnUserFollowListener(this);
        mFollows = new ArrayList<FocusUserInfo>();
        mPtrFollowList.setAdapter(mFollowsAdapter);
        mPtrFollowList.setMode(Mode.BOTH);
        mPtrFollowList.setOnRefreshListener(this);
        mPtrFollowList.setRefreshing(Mode.PULL_FROM_START);
    }

    private void updateTotalNum(int num) {
        mTvTotalNum.setVisibility(View.VISIBLE);
        mTvTotalNum.setText(getString(mStrResId, num));

    }
    
    private void fetchFollowsInfo(boolean refresh) {
        int offset = refresh ? 0 : mFollowsAdapter.getCount();
        mFollowManager.getFollows(offset, mRequest);
    }
    
    private BaseAdapter mFollowsAdapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return mFollows.size();
        }

        @Override
        public FocusUserInfo getItem(int position) {
            return mFollows.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_my_follow, parent, false);
                holder = new ViewHolder();
                ViewUtils.inject(holder, convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            FocusUserInfo user = getItem(position);
            holder.mUser = user;
            if (TextUtils.isEmpty(user.avatar)) {
                Picasso.with(FollowListActivity.this)
                .load(user.defaultAvatar())
                .into(holder.mIvAvatar);
            } else {
                Picasso.with(FollowListActivity.this)
                    .load(user.avatar)
                    .placeholder(R.drawable.chat_botton_bg_faviconboy)
                    .into(holder.mIvAvatar);
            }
            Picasso.with(FollowListActivity.this)
                .load(user.gender == 0 ? R.drawable.img_girl : R.drawable.img_boy)
                .into(holder.mIvGender);
            
            holder.mTvName.setText(user.nickName);
            holder.mTvSchool.setText(user.school);
            if (user.isFollow) {
                holder.mTvIsFollow.setText(R.string.tribe_is_followed);
                holder.mTvIsFollow.setTextColor(0XFF848484);
                holder.mTvIsFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.channel_icon_ok, 0, 0, 0);
                holder.mTvIsFollow.setCompoundDrawablePadding(
                        getResources().getDimensionPixelSize(R.dimen.tribe_rad_men_follow_padding));
                holder.mTvIsFollow.setBackgroundResource(R.drawable.bg_tv_green_circle);
            } else {
                holder.mTvIsFollow.setText(R.string.tribe_collected_add);
                holder.mTvIsFollow.setBackgroundResource(R.drawable.bg_tv_rad_circle);
                holder.mTvIsFollow.setTextColor(0XFFFF9393);
                holder.mTvIsFollow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            holder.mUser = user;
            holder.mTvIsFollow.setTag(holder);
            convertView.setOnClickListener(FollowListActivity.this);
            holder.mTvIsFollow.setOnClickListener(FollowListActivity.this);
            return convertView;
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
        @ViewInject(R.id.tv_attention_count)
        TextView mTvIsFollow;
        FocusUserInfo mUser;
    }
    @Override
    public void onClick(View v) {
        ViewHolder mHolder = (ViewHolder) v.getTag();
        FocusUserInfo user = mHolder.mUser;
        Intent intent  = new Intent();
        switch (v.getId()) {
            case R.id.rl_follow_item:
                intent.setClass(FollowListActivity.this, NewProfileActivity.class);
                UserInfo mUserInfo = new UserInfo();
                mUserInfo.userId = user.id;
                intent.putExtra(UserInfo.INTENT_EXTRA_INFO, mUserInfo);
                startActivity(intent);
                break;
            case R.id.tv_attention_count:
                if (user.isFollow) {
                    mFollowManager.cancleFollow(user.id);
                } else {
                    mFollowManager.follow(user.id);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchFollowsInfo(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchFollowsInfo(false);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFollowManager.unregisterOnUserFollowListener(FollowListActivity.this);
    }

    @Override
    public void onFollowSuccess() {
        mFollowManager.getFollows(0, mRequest);
    }

    @Override
    public void onFollowFail() {
        
    }

    @Override
    public void onCancleFollowSuccess() {
        mFollowManager.getFollows(0, mRequest);
    }

    @Override
    public void onCancleFollowFail() {
        
    }

    @Override
    public void onGetFollowListSuccess(MsResponse response, int offset) {
        mPtrFollowList.onRefreshComplete();
        try {
            JSONObject jsonObj = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);
            totalFollowNum = jsonObj.optInt("count");
            JSONArray ja = jsonObj.getJSONArray("users");
            ArrayList<FocusUserInfo> follows = JsonUtil.getArray(ja, FocusUserInfo.TRANSFORMER);
            if (offset <= 0) {
                mFollows.clear();
            }
            mFollows.addAll(follows);
            mFollowsAdapter.notifyDataSetChanged();
            updateTotalNum(totalFollowNum);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGetFollowListFail() {
        
    }
}
