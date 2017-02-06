package com.tjut.mianliao.contact;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.SearchView;
import com.tjut.mianliao.component.SearchView.OnSearchTextListener;
import com.tjut.mianliao.contact.ContactUpdateCenter.ContactObserver;
import com.tjut.mianliao.contact.ContactUpdateCenter.UpdateType;
import com.tjut.mianliao.data.contact.UserEntry;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;

public class ContactsBlacklistActivity extends BaseActivity implements
        OnItemClickListener, OnTouchListener, ContactObserver, OnSearchTextListener {

    private SearchView mContactsSearchView;
    private ListView mContactsListView;
    private ContactsBlacklistAdapter mContactsAdapter;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_contacts_blacklist;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.setting_contacts_blacklist, null);

        mContactsAdapter = new ContactsBlacklistAdapter(this);
        mContactsListView = (ListView) findViewById(R.id.lv_contacts_blacklist);
        mContactsListView.setAdapter(mContactsAdapter);
        mContactsListView.setOnItemClickListener(this);
        mContactsListView.setOnTouchListener(this);

        mContactsSearchView = (SearchView) findViewById(R.id.sv_contacts_blacklist);
        mContactsSearchView.setHint(R.string.contacts_search_hint);
        mContactsSearchView.setOnSearchTextListener(this);

        ContactUpdateCenter.registerObserver(this);
    }

    @Override
    protected void onStop() {
        mContactsSearchView.hideInput();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        ContactUpdateCenter.removeObserver(this);
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserEntry ue = (UserEntry) parent.getItemAtPosition(position);
        if (ue != null) {
            UserInfo ui = UserInfoManager.getInstance(this).getUserInfo(ue.jid);
            Intent intent = new Intent(this, NewProfileActivity.class);
            intent.putExtra(UserInfo.INTENT_EXTRA_INFO, ui);
            startActivity(intent);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            mContactsSearchView.hideInput();
        }
        return false;
    }

    @Override
    public void onContactsUpdated(final UpdateType type, Object data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (type == UpdateType.Blacklist) {
//                    mContactsAdapter.reset();
                }
//                mContactsAdapter.sort();
                mContactsAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onSearchTextChanged(CharSequence text) {
        mContactsAdapter.getFilter().filter(TextUtils.isEmpty(text) ? null : text);
    }
}
