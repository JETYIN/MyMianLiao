package com.tjut.mianliao.tribe;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import com.tjut.mianliao.component.TitleBar;
import com.tjut.mianliao.data.tribe.TribeInfo;
import com.tjut.mianliao.data.tribe.TribeTypeInfo;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class TribesListActivity extends BaseActivity implements
		OnRefreshListener2<ListView>, OnClickListener {

	public final static String MORE_TRIBE_TYPE = "more_tribe_type";

	@ViewInject(R.id.ptlv_tribe_classify_detail)
	private PullToRefreshListView mPtrClassifyDetail;

	private TribeTypeInfo mTribeTypeInfo;
	private ArrayList<TribeInfo> mTribes;
	private TribesListAdapter mAdapter;

	private TitleBar mTitleBar;
	private int mType;

	@Override
	protected int getLayoutResID() {
		return R.layout.activity_tribe_classify_detail;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		mTribes = new ArrayList<TribeInfo>();
		mTitleBar = getTitleBar();
		Intent intent = getIntent();
		mType = intent.getIntExtra(MORE_TRIBE_TYPE, 0);
		setTitleText(mType);
		mAdapter = new TribesListAdapter();
		mPtrClassifyDetail.setMode(Mode.BOTH);
		mPtrClassifyDetail.setOnRefreshListener(this);
		mPtrClassifyDetail.setAdapter(mAdapter);
		mPtrClassifyDetail.setRefreshing(Mode.PULL_FROM_START);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		fetchTribes(true);

	}

	private void setTitleText(int type) {
		switch (type) {
		case 0:
			mTitleBar.setTitle(R.string.tribe_hot_tribe);
			break;
		case 1:
			mTitleBar.setTitle(R.string.tribe_newest_tribe);
			break;
		default:
			break;
		}
	}

	private class TribesListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mTribes.size();
		}

		@Override
		public TribeInfo getItem(int position) {
			return mTribes.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHoder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.list_item_tribe_classify, parent, false);
				holder = new ViewHoder();
				ViewUtils.inject(holder, convertView);
				convertView.setTag(holder);

			} else {
				holder = (ViewHoder) convertView.getTag();
			}
			TribeInfo tribe = getItem(position);
			Picasso.with(TribesListActivity.this).load(tribe.icon)
					.into(holder.mIvTribeLogo);

			holder.mTvTribeName.setText(tribe.tribeName);
			holder.mTvTribeDesc.setText(tribe.tribeDesc);
			holder.mTvPostNum.setText(getString(
					R.string.tribe_is_followed_count_post, tribe.threadCount));
			holder.mTvPeopleNum.setText(getString(
					R.string.tribe_is_followed_count, tribe.followCount));
			holder.mTribe = tribe;

			holder.mTvIsConcern.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					TribeInfo tribeInfo = (TribeInfo) v.getTag();
					followTribe(tribeInfo);
				}
			});

			holder.mTvIsConcern.setTag(tribe);
			if (tribe.collected) {
				holder.mTvIsConcern
						.setText(getString(R.string.tribe_is_followed));
				holder.mTvIsConcern.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.channel_icon_ok, 0, 0, 0);
				holder.mTvIsConcern
						.setBackgroundResource(R.drawable.bg_tv_gray);
				holder.mTvIsConcern.setTextColor(0xffaae3ff);
				holder.mTvIsConcern.setEnabled(false);
			} else {
				holder.mTvIsConcern
						.setText(getString(R.string.tribe_collected_add));
				holder.mTvIsConcern.setCompoundDrawablesWithIntrinsicBounds(0,
						0, 0, 0);
				holder.mTvIsConcern
						.setBackgroundResource(R.drawable.bg_tv_blue);
				holder.mTvIsConcern.setTextColor(Color.WHITE);
				holder.mTvIsConcern.setEnabled(true);
			}
			convertView.setOnClickListener(TribesListActivity.this);
			return convertView;
		}

	}

	private class ViewHoder {
		@ViewInject(R.id.piv_tribe_logo)
		AvatarView mIvTribeLogo;
		@ViewInject(R.id.tv_tribe_name)
		TextView mTvTribeName;
		@ViewInject(R.id.tv_tribe_describe)
		TextView mTvTribeDesc;
		@ViewInject(R.id.tv_post_num)
		TextView mTvPostNum;
		@ViewInject(R.id.tv_people_num)
		TextView mTvPeopleNum;
		@ViewInject(R.id.tv_is_concern)
		TextView mTvIsConcern;
		@ViewInject(R.id.line_horizonal)
		View mLine;
		TribeInfo mTribe;
	}

	private class GetTribesTask extends MsTask {

		private int mOffset;

		public GetTribesTask(int offset, MsRequest request) {
			super(TribesListActivity.this, request);
			mOffset = offset;
		}

		@Override
		protected String buildParams() {
			return new StringBuilder("offset=").append(mOffset).toString();
		}

		@Override
		protected void onPostExecute(MsResponse response) {
			mPtrClassifyDetail.onRefreshComplete();
			if (response.isSuccessful()) {
				if (mOffset <= 0) {
					mTribes.clear();
				}
				ArrayList<TribeInfo> tribes = JsonUtil.getArray(
						response.getJsonArray(), TribeInfo.TRANSFORMER);
				mTribes.addAll(tribes);
				mAdapter.notifyDataSetChanged();
			}
		}

	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		fetchTribes(true);
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		fetchTribes(false);
	}

	private void followTribe(TribeInfo tribeInfo) {
		new FollowTribeTask(tribeInfo).executeLong();
	}

	private void fetchTribes(boolean refresh) {
		int offset = refresh ? 0 : mAdapter.getCount();
		if (mType == 0) {
			new GetTribesTask(offset, MsRequest.TRIBE_HOT_LIST).executeLong();
		} else {
			new GetTribesTask(offset, MsRequest.TRIBE_LATEST_TRIBE_LIST)
					.executeLong();
		}
	}

	private class FollowTribeTask extends MsTask {

		private TribeInfo mTribeInfo;

		public FollowTribeTask(TribeInfo tribeInfo) {
			super(TribesListActivity.this,
					!tribeInfo.collected ? MsRequest.TRIBE_FOLLOW_WITH
							: MsRequest.TRIBE_CANCEL_FOLLOW);
			mTribeInfo = tribeInfo;
		}

		@Override
		protected String buildParams() {
			return "tribe_id=" + mTribeInfo.tribeId;
		}

		@Override
		protected void onPostExecute(MsResponse response) {
			if (response.isSuccessful()) {
				mTribeInfo.collected = !mTribeInfo.collected;
				mTribeInfo.followCount = mTribeInfo.followCount + 1;
				mAdapter.notifyDataSetChanged();
			}
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_tribe_item:
			ViewHoder holder = (ViewHoder) v.getTag();
			TribeInfo tribe = holder.mTribe;
			Intent intent = new Intent(TribesListActivity.this,
					TribeDetailActivity.class);
			intent.putExtra(TribeInfo.INTENT_EXTRA_INFO, tribe);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

}
