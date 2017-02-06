package com.tjut.mianliao.contact;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.LocationHelper;
import com.tjut.mianliao.util.LocationHelper.LocationObserver;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class PersonAroundActivity extends BaseActivity implements
		OnItemClickListener, OnRefreshListener2<ListView>, LocationObserver {

	private View mWaitingView;
	private PullToRefreshListView mUserListView;

	private UserInfoAdapter mAdapter;

	private LocationHelper mLocationHelper;
	private FrameLayout mMagicFrameLayout;
	private Settings mSettings;

	@Override
	protected int getLayoutResID() {
		return R.layout.activity_person_around;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSettings = Settings.getInstance(this);
		
		getTitleBar().showTitleText(R.string.adc_person_around, null);

		mWaitingView = findViewById(R.id.waiting_view);

		mUserListView = (PullToRefreshListView) findViewById(R.id.ptrlv_person_around);
		mUserListView.getRefreshableView().addFooterView(new View(this));
		mUserListView.setOnItemClickListener(this);
		mUserListView.setOnRefreshListener(this);

		mAdapter = new UserInfoAdapter(this, R.layout.list_item_person_around) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				UserInfo user = getItem(position);
				View view = super.getView(position, convertView, parent);
				view.setBackgroundResource(R.drawable.selector_bg_item);
				TextView tvTimeDelta = (TextView) view
						.findViewById(R.id.tv_time_delta);
				tvTimeDelta.setText(DateUtils.getRelativeTimeSpanString(
						-user.timeDelta * 1000, 0, DateUtils.MINUTE_IN_MILLIS));

				TextView tvDistance = (TextView) view
						.findViewById(R.id.tv_distance);
				String distance = user.distance > 1000 ? getString(
						R.string.around_distance_km, user.distance / 1000.0)
						: getString(R.string.around_distance_meter,
								user.distance);
				tvDistance.setText(Utils.getColoredText(
						getString(R.string.around_distance_desc, distance),
						distance, getKeyColor()));

				return view;
			}
		};
		mUserListView.setAdapter(mAdapter);

		mLocationHelper = LocationHelper.getInstance(this);
		if (mSettings.allowGpsHint() && !mLocationHelper.isGpsEnabled()) {
			Utils.showGpsHintDialog(this);
		}

		getTitleBar().showProgress();
		if (mLocationHelper.getCurrentLoc() == null) {
			mWaitingView.setVisibility(View.VISIBLE);
			mLocationHelper.addObserver(this);
		} else {
			fetchAroundPerson(true);
		}
		mMagicFrameLayout = (FrameLayout) findViewById(R.id.fl_bg_person_around);
		mMagicFrameLayout.setBackgroundColor(0xfff2f2f2);
		mUserListView.setBackgroundColor(Color.WHITE);
}

	@Override
	protected void onResume() {
		super.onResume();
		mLocationHelper.requestUpdates(true);
	}

	@Override
	protected void onPause() {
		mLocationHelper.removeUpdates(true);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mLocationHelper.removeObserver(this);
		super.onDestroy();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		UserInfo user = (UserInfo) parent.getItemAtPosition(position);
		Intent i = new Intent(this, NewProfileActivity.class);
		i.putExtra(UserInfo.INTENT_EXTRA_INFO, user);
		startActivity(i);
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		fetchAroundPerson(true);
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		fetchAroundPerson(false);
	}

	@Override
	public void onReceiveLocation() {
		mLocationHelper.removeObserver(this);
		fetchAroundPerson(true);
	}

	private void fetchAroundPerson(boolean refresh) {
		if (mLocationHelper.getCurrentLoc() == null) {
			mUserListView.onRefreshComplete();
		} else {
			int offset = refresh ? 0 : mAdapter.getCount();
			new PersonAroundTask(offset).executeLong();
		}
	}

	private class PersonAroundTask extends MsTask {
		private int mOffset;

		public PersonAroundTask(int offset) {
			super(getApplicationContext(), MsRequest.FIND_BY_LOCATION);
			mOffset = offset;
		}

		@Override
		protected String buildParams() {
			return new StringBuilder("offset=").append(mOffset)
					.append("&location=")
					.append(mLocationHelper.getCurrentLocString()).toString();
		}

		@Override
		protected void onPostExecute(MsResponse response) {
			getTitleBar().hideProgress();
			mUserListView.onRefreshComplete();
			mWaitingView.setVisibility(View.GONE);
			if (response.isSuccessful()) {
				mAdapter.setNotifyOnChange(false);
				if (mOffset == 0) {
					mAdapter.clear();
				}
				mAdapter.addAll(JsonUtil.getArray(response.getJsonArray(),
						UserInfo.TRANSFORMER));
				mAdapter.notifyDataSetChanged();
			} else {
				response.showFailInfo(getRefContext(),
						R.string.around_tst_search_failed);
			}
		}
	}
}
