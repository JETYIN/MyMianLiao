package com.tjut.mianliao.tribe;

import java.util.ArrayList;

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
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.CommonBanner;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.tribe.TribeInfo;
import com.tjut.mianliao.im.AvatarMarketActivity;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.umeng.analytics.MobclickAgent;

public class GameZoneActivity extends BaseActivity implements
		OnRefreshListener2<ListView>, OnClickListener {

	@ViewInject(R.id.tv_mini_game_name)
	private TextView mTvMiniGame;
	@ViewInject(R.id.iv_mini_game_logo)
	private ProImageView mIvMiniGameLogo;
	@ViewInject(R.id.tv_mini_game_people_num)
	private TextView mTvMiniGamePeopleNum;
	@ViewInject(R.id.iv_go_mini_game)
	private ImageView mIvGoMiniGame;
	@ViewInject(R.id.ptlv_game_tribes)
	private PullToRefreshListView mLvGameTribe;
	@ViewInject(R.id.vs_switcher)
	private CommonBanner mSwitchBanner;

	private ArrayList<TribeInfo> mTribes;
	private GameTribesAdapter mAdapter;

	@Override
	protected int getLayoutResID() {
		return R.layout.activity_game_zone;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		getTitleBar().setTitle(R.string.tribe_game_zone);
		mTribes = new ArrayList<TribeInfo>();
		mAdapter = new GameTribesAdapter();
		mLvGameTribe.setMode(Mode.BOTH);
		mLvGameTribe.setOnRefreshListener(this);
		mLvGameTribe.setAdapter(mAdapter);
		mLvGameTribe.setRefreshing(Mode.PULL_FROM_START);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		fetchTribes(true);
	}

	private class GameTribesAdapter extends BaseAdapter {

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
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.list_item_hot_tribe,
						parent, false);
				holder = new ViewHolder();
				ViewUtils.inject(holder, convertView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			TribeInfo tribe = getItem(position);
			if (tribe.icon != null && !tribe.icon.equals("")) {
				Picasso.with(GameZoneActivity.this).load(tribe.icon)
						.into(holder.mIvTribeLogo);
			}
			holder.mTvTribeName.setText(tribe.tribeName);
			holder.mTvTribeDesc.setText(tribe.tribeDesc);
			holder.mTvPeopleNum
					.setText(getString(R.string.tribe_is_followed_count_people,
							tribe.followCount));
			holder.mTvPostNum.setText(getString(
					R.string.tribe_is_followed_count_post, tribe.threadCount));
			holder.mTribe = tribe;
			convertView.setOnClickListener(mTribeOnclick);

			return convertView;
		}

	}

	private OnClickListener mTribeOnclick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();
			TribeInfo tribe = holder.mTribe;
			Intent intent = new Intent(GameZoneActivity.this,
					TribeDetailActivity.class);
			intent.putExtra(TribeInfo.INTENT_EXTRA_INFO, tribe);
			startActivity(intent);
		}
	};

	private class ViewHolder {
		@ViewInject(R.id.piv_tribe_logo)
		ProImageView mIvTribeLogo;
		@ViewInject(R.id.tv_tribe_name)
		TextView mTvTribeName;
		@ViewInject(R.id.tv_tribe_describe)
		TextView mTvTribeDesc;
		@ViewInject(R.id.tv_post_num)
		TextView mTvPostNum;
		@ViewInject(R.id.tv_people_num)
		TextView mTvPeopleNum;
		TribeInfo mTribe;
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		fetchTribes(true);
		fetchBanner();
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		fetchTribes(false);
	}

	private void fetchTribes(boolean refresh) {
		int size = mAdapter.getCount();
		int tribeId = refresh ? 0 : mTribes.get(size - 1).tribeId;
		new GetGameTribeTask(TribeInfo.TYPE_GAME, tribeId).executeLong();
	}

	private void fetchBanner() {
		mSwitchBanner.setParam(CommonBanner.Plate.GameZone, 0);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_go_mini_game:
		case R.id.iv_go_mini_game:
			viewMarket(MsRequest.IMRW_GAME);
			MobclickAgent.onEvent(GameZoneActivity.this, MStaticInterface.GAME);
			break;
		case R.id.tv_tribe_more:
			fetchTribes(false);
			break;
		default:
			break;
		}
	}

	private void viewMarket(MsRequest request) {
		Intent iMarket = new Intent(GameZoneActivity.this,
				AvatarMarketActivity.class);
		String url = HttpUtil.getUrl(GameZoneActivity.this, request, "");
		iMarket.putExtra(AvatarMarketActivity.URL, url);
		startActivity(iMarket);
	}

	private class GetGameTribeTask extends MsTask {

		private int mType;
		private int mTribeId;
		private boolean mRefresh;

		public GetGameTribeTask(int type, int tribeId) {
			super(GameZoneActivity.this, MsRequest.TRIBE_LIST_BY_TYPE);
			mType = type;
			mTribeId = tribeId;
			mRefresh = tribeId == 0;
		}

		@Override
		protected String buildParams() {
			return new StringBuilder().append("tribe_type=").append(mType)
					.append("&tribe_id=").append(mTribeId).toString();
		}

		@Override
		protected void onPostExecute(MsResponse response) {
			mLvGameTribe.onRefreshComplete();
			if (response.isSuccessful()) {
				ArrayList<TribeInfo> tribes = JsonUtil.getArray(
						response.getJsonArray(), TribeInfo.TRANSFORMER);
				if (tribes != null) {
					if (mRefresh) {
						mTribes.clear();
					}
					mTribes.addAll(tribes);
					mAdapter.notifyDataSetChanged();
				}
			}
		}
	}

}
