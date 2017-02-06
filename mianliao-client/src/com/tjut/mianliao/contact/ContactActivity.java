package com.tjut.mianliao.contact;

import java.util.ArrayList;

import org.json.JSONArray;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LetterBarView;
import com.tjut.mianliao.component.LetterBarView.OnLetterSelectListener;
import com.tjut.mianliao.component.SearchView;
import com.tjut.mianliao.contact.ContactsAdapter.OnItemClickWrapperListener;
import com.tjut.mianliao.data.FocusUserInfo;
import com.tjut.mianliao.data.RadMenInfo;
import com.tjut.mianliao.data.contact.UserEntry;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.tribe.TribeInfo;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class ContactActivity extends BaseActivity implements OnClickListener,
		OnItemClickListener, OnTouchListener,
		ContactUpdateCenter.ContactObserver, SearchView.OnSearchTextListener {

	private SearchView mContactsSearchView;
	private ListView mContactsListView;
//	private View mRequestView;

	private UserInfoManager mUserInfoManager;
	private SubscriptionHelper mSubscriptionHelper;
	private ContactsAdapter mContactsAdapter;
	private Settings mSettings;
	private LinearLayout mRootLayout;
	private LetterBarView letterBar;

	private FollowUserManager mFollowUserManager;
	

	@Override
	protected int getLayoutResID() {
		return R.layout.activity_contact;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSettings = Settings.getInstance(this);

		getTitleBar().showTitleText(getString(R.string.contacts, 0), null);
		mUserInfoManager = UserInfoManager.getInstance(this);
		mFollowUserManager = FollowUserManager.getInstance(this);;;;
		mSubscriptionHelper = SubscriptionHelper.getInstance(this);
		mContactsAdapter = new ContactsAdapter(this);

		mContactsListView = (ListView) findViewById(R.id.lv_contacts);
		mContactsListView.setAdapter(mContactsAdapter);
		mContactsListView.setOnTouchListener(this);
		mContactsListView.setOnItemClickListener(mItemClickListener);

		mContactsSearchView = (SearchView) findViewById(R.id.sv_contacts);
		mContactsSearchView.setHint(R.string.contacts_search_hint);
		mContactsSearchView.setOnSearchTextListener(this);
		mRootLayout = (LinearLayout) findViewById(R.id.ll_contacts);
		ContactUpdateCenter.registerObserver(this);
		letterBar = (LetterBarView) findViewById(R.id.letter_bar);
        letterBar.setOnLetterSelectListener(new OnLetterSelectListener() {

            @Override
            public void onLetterSelect(String s) {
                if(s.equalsIgnoreCase("#")) {
                    mContactsListView.setSelection(0);
                } else {
                    if( mContactsAdapter.containsAlpha(s) ) {
                        mContactsListView.setSelection( mContactsAdapter.getAlphaPosition(s) );
                    }
                }
            }
        });
        fillContacts();
	}

	@Override
	protected void onDestroy() {
		ContactUpdateCenter.removeObserver(this);
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mContactsSearchView.hideInput();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_requests:
			startActivity(new Intent(this, NewContactActivity.class));
			break;
		default:
			break;
		}
	}

	private OnItemClickWrapperListener<RadMenInfo> mItemClickListener =
	        new OnItemClickWrapperListener<RadMenInfo>() {

                @Override
                public void onItemClick(RadMenInfo ue, int position, View view, AdapterView<?> parent) {
                    UserInfo user = UserInfoManager.getInstance(ContactActivity.this).getUserInfo(ue.uid);
                    if (user != null) {
                        Intent iProfile = new Intent(ContactActivity.this, NewProfileActivity.class);
                        iProfile.putExtra(UserInfo.INTENT_EXTRA_INFO, user);
                        startActivity(iProfile);
                    }
                }
    };

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (MotionEvent.ACTION_DOWN == event.getAction()) {
			mContactsSearchView.hideInput();
		}
		return false;
	}

	@Override
	public void onContactsUpdated(final ContactUpdateCenter.UpdateType type,
			Object data) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (type == ContactUpdateCenter.UpdateType.UserEntry
						|| type == ContactUpdateCenter.UpdateType.Blacklist) {
//					mContactsAdapter.reset();
				}
				mContactsAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onSearchTextChanged(CharSequence text) {
		if (TextUtils.isEmpty(text)) {
			mContactsAdapter.getFilter().filter(null);
		} else {
			mContactsAdapter.getFilter().filter(text);
		}
	}

//	private class GetContactsTask extends MsTask{
//
//        public GetContactsTask() {
//            super(ContactActivity.this, MsRequest.FRIEND_LIST_FRIENDS);
//        }
//
//        @Override
//        protected void onPostExecute(MsResponse response) {
//            if (response.isSuccessful()) {
//                JSONArray ja = response.getJsonArray();
//                ArrayList<RadMenInfo> users = JsonUtil.getArray(ja, RadMenInfo.TRANSFORMER);
//                getTitleBar().showTitleText(getString(R.string.contacts, users.size()), null);
//                mContactsAdapter.reset(users);
//            }
//        }
//
//	}



	@Override
	protected void onResume() {
		super.onResume();
		fillContacts();
	}

	private void fillContacts () {
		ArrayList<RadMenInfo> mFriends = mFollowUserManager.getFriendList();
		getTitleBar().showTitleText(getString(R.string.contacts, mFriends.size()), null);
		mContactsAdapter.reset(mFriends);
	}
}
