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
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
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
import com.tjut.mianliao.util.Utils;

public class LatestVisitorActivity extends BaseActivity implements
		OnRefreshListener2<ListView> {

	@ViewInject(R.id.tv_total_num)
	private TextView mTvTotalNum;
	@ViewInject(R.id.ptr_latest_visitor)
	private PullToRefreshListView mPtrLatestVisitor;

	private ArrayList<UserInfo> mVistors;

	private UserInfo mUserInfo;

	private int mScanTotle;

	private int mScanToday;

	@Override
	protected int getLayoutResID() {
		return R.layout.activity_latest_visitor;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		mUserInfo = getIntent().getParcelableExtra(UserInfo.INTENT_EXTRA_INFO);
		getTitleBar().setTitle(R.string.prof_visitor_latest);
		mVistors = new ArrayList<UserInfo>();
		mPtrLatestVisitor.setAdapter(mVisitorAdapter);
		mPtrLatestVisitor.setMode(Mode.BOTH);
		mPtrLatestVisitor.setOnRefreshListener(this);
		mPtrLatestVisitor.setRefreshing(Mode.PULL_FROM_START);
		updateVisitCountInfo();
	}

	public void updateVisitCountInfo() {
		mTvTotalNum.setText(getString(R.string.prof_visitor_latest_desc,
				mScanTotle, mScanToday));
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		fetVisitorsInfo(true);
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		fetVisitorsInfo(false);
	}

	private void fetVisitorsInfo(boolean refresh) {
		int offset = refresh ? 0 : mVisitorAdapter.getCount();
		new GetVisitorsTask(offset).executeLong();
		if (refresh) {
			new GetVisitorInfoTask().executeLong();
		}
	}

	private BaseAdapter mVisitorAdapter = new BaseAdapter() {

		@Override
		public int getCount() {
			return mVistors.size();
		}

		@Override
		public UserInfo getItem(int position) {
			return mVistors.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.list_item_latest_visitor, parent, false);
				holder = new ViewHolder();
				ViewUtils.inject(holder, convertView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			UserInfo user = getItem(position);
			holder.mUser = user;
			Picasso.with(LatestVisitorActivity.this).load(user.getAvatar())
					.placeholder(R.drawable.chat_botton_bg_faviconboy)
					.into(holder.mIvAvatar);
			Picasso.with(LatestVisitorActivity.this)
					.load(user.gender == 0 ? R.drawable.img_girl
							: R.drawable.img_boy).into(holder.mIvGender);
			holder.mTvName.setText(user
					.getDisplayName(LatestVisitorActivity.this));
			holder.mTvSchool.setText(user.school);
			holder.mTvDate.setText(Utils.getTimeString(8, user.visitTime));
			holder.mUser = user;
			convertView.setOnClickListener(mVistorListen);
			return convertView;
		}
	};

	private OnClickListener mVistorListen = new OnClickListener() {

		@Override
        public void onClick(View v) {
            ViewHolder mHolder = (ViewHolder) v.getTag();
            UserInfo user = mHolder.mUser;
            Intent intent = new Intent(LatestVisitorActivity.this, NewProfileActivity.class);
            intent.putExtra(UserInfo.INTENT_EXTRA_INFO, user);
            startActivity(intent);
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
		@ViewInject(R.id.tv_date)
		TextView mTvDate;
		UserInfo mUser;
	}

	private class GetVisitorsTask extends MsTask {

		private int mOffset;

		public GetVisitorsTask(int offset) {
			super(LatestVisitorActivity.this, MsRequest.USER_GET_VISITORS);
			mOffset = offset;
		}

		@Override
		protected String buildParams() {
			return new StringBuilder("query_uid=").append(mUserInfo.userId)
					.append("&last_index=").append(mOffset).toString();
		}

		@Override
		protected void onPostExecute(MsResponse response) {
			mPtrLatestVisitor.onRefreshComplete();
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
					if (mOffset == 0) {
						mVistors.clear();
					}
					mVistors.addAll(users);
					mVisitorAdapter.notifyDataSetChanged();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private class GetVisitorInfoTask extends MsTask {

		public GetVisitorInfoTask() {
			super(LatestVisitorActivity.this, MsRequest.USER_VISITOR_INFO);
		}

		@Override
		protected String buildParams() {
			return "query_uid=" + mUserInfo.userId;
		}

		@Override
		protected void onPostExecute(MsResponse response) {
			if (response.isSuccessful()) {
				JSONObject jsonObject = response.getJsonObject();
				if (jsonObject != null) {
					mScanTotle = jsonObject.optInt("all_visit_times");
					mScanToday = response.getJsonObject().optInt(
							"today_visit_times");
					updateVisitCountInfo();
				}
			}
		}

	}

}
