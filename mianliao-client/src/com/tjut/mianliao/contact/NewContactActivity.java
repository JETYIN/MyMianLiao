package com.tjut.mianliao.contact;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.NotificationHelper;
import com.tjut.mianliao.NotificationHelper.NotificationType;
import com.tjut.mianliao.R;
import com.tjut.mianliao.chat.UnreadMessageHelper;
import com.tjut.mianliao.component.NameView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.contact.ContactUpdateCenter.ContactObserver;
import com.tjut.mianliao.contact.SubscriptionHelper.SubResponseListener;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.explore.TriggerEventTask;
import com.tjut.mianliao.notice.NoticeManager;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.StringUtils;
import com.tjut.mianliao.xmpp.ConnectionManager;

public class NewContactActivity extends BaseActivity implements
        OnItemClickListener, OnClickListener, ContactObserver, SubResponseListener {

    private UserInfoManager mUserInfoManager;
    private ConnectionManager mConnectionManager;
    private NoticeManager mNoticeManager;
    private SubscriptionHelper mSubscriptionHelper;

    private SubRequestAdapter mAdapter;
    private ArrayList<String> mSubRequests;
    private boolean mDoingRequest;
    private ListView mListView;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_subrequest;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationHelper.getInstance(this).clearNotification(NotificationType.NOTICE);
        getTitleBar().showTitleText(R.string.nc_title, null);
        UnreadMessageHelper.getInstance(this).setMessageTarget(UnreadMessageHelper.TARGET_ADD_FRIEND);

        mUserInfoManager = UserInfoManager.getInstance(this);
        mConnectionManager = ConnectionManager.getInstance(this);
        mNoticeManager = NoticeManager.getInstance(this);
        mSubscriptionHelper = SubscriptionHelper.getInstance(this);
        mSubscriptionHelper.registerResponseListener(this);

        mSubRequests = mSubscriptionHelper.getSubRequests();
        mAdapter = new SubRequestAdapter();
        mListView = (ListView) findViewById(R.id.lv_sub_requests);
        mListView.setAdapter(mAdapter);
//        mListView.setOnItemClickListener(this);

        ContactUpdateCenter.registerObserver(this);
    }

    @Override
    protected void onDestroy() {
        ContactUpdateCenter.removeObserver(this);
        mSubscriptionHelper.unregisterResponseListener(this);
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String jid = (String) parent.getItemAtPosition(position);
        if (jid != null) {
            Intent i = new Intent(this, NewProfileActivity.class);
            i.putExtra(UserInfo.INTENT_EXTRA_INFO, mUserInfoManager.getUserInfo(jid));
            startActivity(i);
        }
    }

    @Override
    public void onClick(View v) {
        if (!mConnectionManager.chatServerConnected()) {
            toast(R.string.disconnected);
            return;
        }
        if (mDoingRequest) {
            toast(R.string.nc_handling_last_request);
            return;
        }
        switch (v.getId()) {
            case R.id.btn_agree:
                onRequestStart();
                String name = StringUtils.parseName((String) v.getTag());
                mSubscriptionHelper.accept(name);
                new TriggerEventTask(this, "add_friend").executeLong();
                break;
            case R.id.btn_reject:
                onRequestStart();
                String username = StringUtils.parseName((String) v.getTag());
                mSubscriptionHelper.reject(username);
                break;
            default:
                break;
        }
    }

    @Override
    public void onContactsUpdated(ContactUpdateCenter.UpdateType type, Object data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSubRequests = mSubscriptionHelper.getSubRequests();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onSubAccept(boolean success) {
        onRequestStop(success);
    }

    @Override
    public void onSubReject(boolean success) {
        onRequestStop(success);
    }

    private void onRequestStart() {
        getTitleBar().showProgress();
        mDoingRequest = true;
    }

    private void onRequestStop(boolean success) {
        getTitleBar().hideProgress();
        mDoingRequest = false;
        if (success) {
            int count = mSubscriptionHelper.getCount();
            mNoticeManager.setNoticeViewed(this, count);
            if (count == 0) {
                finish();
            }
        } else {
            toast(R.string.adc_response_sent_failed);
        }
        mSubscriptionHelper.clearSubFlag();
    }

    private class SubRequestAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSubRequests.size();
        }

        @Override
        public String getItem(int position) {
            return mSubRequests.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_new_request, parent, false);
            }
            String jid = getItem(position);
            UserInfo user = mUserInfoManager.getUserInfo(jid);

            if (user != null) {
                ((ProImageView) view.findViewById(R.id.av_avatar)).setImage(
                        user.getAvatar(), user.defaultAvatar());

                NameView tvName = (NameView) view.findViewById(R.id.tv_name);
                ImageView ivTypeIcon = (ImageView) view.findViewById(R.id.iv_type_icon);
                tvName.setText(user.getDisplayName(getApplicationContext()));
                tvName.setMedal(user.primaryBadgeImage);

                ((TextView) view.findViewById(R.id.tv_short_desc)).setText(
                        getString(R.string.nc_request_desc));

                View btnReject = view.findViewById(R.id.btn_reject);
                btnReject.setTag(jid);
                btnReject.setOnClickListener(NewContactActivity.this);

                View btnAgree = view.findViewById(R.id.btn_agree);
                btnAgree.setTag(jid);
                btnAgree.setOnClickListener(NewContactActivity.this);
                // update type icon; it while show in day time ;or it
                // should hide
                int resIcon = user.getTypeIcon();
                ivTypeIcon.setVisibility(View.GONE);
            }
            return view;
        }
    }
}