package com.tjut.mianliao.contact;

import org.json.JSONArray;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.umeng.analytics.MobclickAgent;

public class AddContactActivity extends BaseActivity implements
		OnClickListener, OnItemClickListener {

	private TextView mTvChangeRecommend;
	private UserInfoAdapter mAdapter;
	private int mTotalOffset;
	private LinearLayout mMagicLinearLayout;
	private Settings mSettings;

	@Override
	protected int getLayoutResID() {
		return R.layout.activity_add_contact;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSettings = Settings.getInstance(this);
		getTitleBar().showTitleText(R.string.adc_add_contact, null);

		findViewById(R.id.ll_nick_search).setOnClickListener(this);
		mTvChangeRecommend = (TextView) findViewById(R.id.tv_change_recommend);
		mTvChangeRecommend.setOnClickListener(this);

		Resources res = getResources();
		View vFaceMatch = findViewById(R.id.rl_face_match);
		vFaceMatch.setOnClickListener(this);
		((ImageView) vFaceMatch.findViewById(R.id.iv_sample_medal))
				.setImageResource(R.drawable.ic_adc_face_match);
		((TextView) vFaceMatch.findViewById(R.id.tv_medal))
				.setText(R.string.adc_face_match);
		TextView tvDesc = (TextView) vFaceMatch
				.findViewById(R.id.tv_medal_info);
		tvDesc.setText(R.string.adc_face_match_desc);
		tvDesc.setTextColor(res.getColor(R.color.adc_face_match_desc));

		View vPersonAround = findViewById(R.id.rl_person_around);
		vPersonAround.setOnClickListener(this);
		((ImageView) vPersonAround.findViewById(R.id.iv_sample_medal))
				.setImageResource(R.drawable.ic_adc_person_around);
		((TextView) vPersonAround.findViewById(R.id.tv_medal))
				.setText(R.string.adc_person_around);
		tvDesc = (TextView) vPersonAround.findViewById(R.id.tv_medal_info);
		tvDesc.setText(R.string.adc_person_around_desc);
		tvDesc.setTextColor(res.getColor(R.color.adc_person_around_desc));

		ListView lvRecommend = (ListView) findViewById(R.id.lv_recommend_result);
		lvRecommend.addFooterView(new View(this));
		lvRecommend.setOnItemClickListener(this);

		mAdapter = new UserInfoAdapter(this,
				R.layout.list_item_friend_recommend) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				view.setBackgroundResource(R.drawable.selector_bg_item);
				View viewPhoto = view.findViewById(R.id.tv_view_photo);
				viewPhoto.setTag(getItem(position));
				viewPhoto.setOnClickListener(AddContactActivity.this);
				return view;
			}
		};
		lvRecommend.setAdapter(mAdapter);
		mMagicLinearLayout = (LinearLayout) findViewById(R.id.ly_bg_contact);

		new FriendRecommendTask(0).execute();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_nick_search:
			startActivity(new Intent(this, NickSearchActivity.class));
			break;

		case R.id.rl_face_match:
			startActivity(new Intent(this, FaceMatchActivity.class));
            MobclickAgent.onEvent(this, MStaticInterface.FACIAL_MATCHING);
			break;

		case R.id.rl_person_around:
			startActivity(new Intent(this, PersonAroundActivity.class));
            MobclickAgent.onEvent(this, MStaticInterface.LOCAL);
			break;

		case R.id.tv_change_recommend:
			new FriendRecommendTask(mTotalOffset).executeLong();
            MobclickAgent.onEvent(this, MStaticInterface.CHANGE);
			break;

		case R.id.tv_view_photo:
			viewProfile((UserInfo) v.getTag());
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		viewProfile((UserInfo) parent.getItemAtPosition(position));
	}

	private void viewProfile(UserInfo user) {
		Intent i = new Intent(this, NewProfileActivity.class);
		i.putExtra(UserInfo.INTENT_EXTRA_INFO, user);
		startActivity(i);
	}

	private class FriendRecommendTask extends
			AdvAsyncTask<Void, Void, MsResponse> {
		private int mOffset;

		private FriendRecommendTask(int offset) {
			mOffset = offset;
		}

		@Override
		protected void onPreExecute() {
			getTitleBar().showProgress();
			mTvChangeRecommend.setEnabled(false);
		}

		@Override
		protected MsResponse doInBackground(Void... params) {
			return HttpUtil.msRequest(getApplicationContext(),
					MsRequest.FIND_BY_SUGGEST, "limit=3&offset=" + mOffset);
		}

		@Override
		protected void onPostExecute(MsResponse response) {
			getTitleBar().hideProgress();
			mTvChangeRecommend.setEnabled(true);

			if (MsResponse.isSuccessful(response)) {
				mAdapter.setNotifyOnChange(false);
				mAdapter.clear();
				JSONArray ja = response.json
						.optJSONArray(MsResponse.PARAM_RESPONSE);
				for (int i = 0; i < ja.length(); i++) {
					UserInfo user = UserInfo.fromJson(ja.optJSONObject(i));
					if (user != null) {
						mAdapter.add(user);
					}
				}
				mTotalOffset += mAdapter.getCount();
				mAdapter.notifyDataSetChanged();
			} else {
				toast(MsResponse
						.getFailureDesc(getApplicationContext(),
								R.string.adc_tst_friend_recommend_failed,
								response.code));
			}
		}
	}
}
