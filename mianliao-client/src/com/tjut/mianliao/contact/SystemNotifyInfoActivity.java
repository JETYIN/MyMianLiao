package com.tjut.mianliao.contact;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.NotificationHelper;
import com.tjut.mianliao.NotificationHelper.NotificationType;
import com.tjut.mianliao.R;
import com.tjut.mianliao.chat.UnreadMessageHelper;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.push.PushMessage;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class SystemNotifyInfoActivity extends BaseActivity implements
		OnRefreshListener2<ListView> {

	private PullToRefreshListView mLvSysInfo;
	private ArrayList<PushMessage> mPushMessages;
	private NotifyInfoAdapter mAdapter;
	private View mViewNoContent;
	private FrameLayout mViewParent;

	@Override
	protected int getLayoutResID() {
		return R.layout.activity_notice_list;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NotificationHelper.getInstance(this).clearNotification(NotificationType.NOTICE);
		getTitleBar().setTitle(getString(R.string.ntc_sys_msg));
		mPushMessages = new ArrayList<>();
		UnreadMessageHelper.getInstance(this).setMessageTarget(UnreadMessageHelper.TARGET_SYS_INFO);

		mViewNoContent = mInflater.inflate(R.layout.view_no_content, null);
		mViewParent = (FrameLayout) findViewById(R.id.fram_system);

		mLvSysInfo = (PullToRefreshListView) findViewById(R.id.ptrlv_notice);
		mLvSysInfo.setMode(Mode.BOTH);
		mLvSysInfo.setOnRefreshListener(this);
		mAdapter = new NotifyInfoAdapter();
		mLvSysInfo.setAdapter(mAdapter);
		mLvSysInfo.setRefreshing(Mode.PULL_FROM_START);
		fetchData(true);
		mViewNoContent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				reload();
			}
		});
	}

	private void judgeView() {
		removeMessage();
		if (mPushMessages == null || mPushMessages.size() == 0) {
			showNoMessage();
		}
	}

	private void removeMessage() {
		if (mViewParent != null && mViewNoContent != null) {
			mViewParent.removeView(mViewNoContent);
		}
	}

	private void showNoMessage() {
		resetNoContentView();
		mViewParent.addView(mViewNoContent);

	}

	private void resetNoContentView() {
		mViewNoContent.findViewById(R.id.iv_notice).setVisibility(View.VISIBLE);
		mViewNoContent.findViewById(R.id.pb_progress).setVisibility(View.GONE);

	}

	private void reload() {
		mViewNoContent.findViewById(R.id.iv_notice).setVisibility(
				View.INVISIBLE);
		mViewNoContent.findViewById(R.id.pb_progress).setVisibility(
				View.VISIBLE);
		fetchData(true);
	}

	private void fetchData(boolean refresh) {
		new GetSystemNotifyTask(refresh).executeLong();
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		fetchData(true);
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		fetchData(false);
	}

	private class GetSystemNotifyTask extends MsTask {

		private boolean mRefresh;
		private int mOffset;

		public GetSystemNotifyTask(boolean refresh) {
			super(SystemNotifyInfoActivity.this, MsRequest.PUSH_LIST);
			mRefresh = refresh;
			mOffset = refresh ? 0 : mAdapter.getCount();
		}

		@Override
		protected String buildParams() {
			return "offset=" + mOffset;
		}

		@Override
		protected void onPostExecute(MsResponse response) {
			mLvSysInfo.onRefreshComplete();
			if (response.isSuccessful()) {
				ArrayList<PushMessage> messages = JsonUtil.getArray(
						response.getJsonArray(), PushMessage.TRANSFORMER);
				if (messages != null) {
					if (mRefresh) {
						mPushMessages = messages;
					} else {
						mPushMessages.addAll(messages);
					}
				}
				judgeView();
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	private class NotifyInfoAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mPushMessages.size();
		}

		@Override
		public PushMessage getItem(int position) {
			return mPushMessages.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = mInflater.inflate(R.layout.list_item_system_notify_info,
						parent, false);
			} else {
				view = convertView;
			}

			PushMessage msg = getItem(position);

			ProImageView avatar = (ProImageView) view
					.findViewById(R.id.iv_avatar);
			TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
			TextView tvContent = (TextView) view.findViewById(R.id.tv_content);
			TextView tvTime = (TextView) view.findViewById(R.id.tv_time);

			avatar.setImageResource(R.drawable.chat_pic_bg_inform);
			if (msg.getPushTaskMessage() != null) {
				tvTitle.setText(msg.getPushTaskMessage().getTitle());
				tvContent.setText(msg.getPushTaskMessage().getContent());
				tvTime.setText(Utils.getPostShowTimeString(msg
						.getPushTaskMessage().getTime()));
			}
			return view;
		}

	}
}
