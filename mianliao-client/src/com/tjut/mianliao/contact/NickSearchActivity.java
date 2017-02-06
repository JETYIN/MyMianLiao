package com.tjut.mianliao.contact;

import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.SearchView;
import com.tjut.mianliao.component.SearchView.OnSearchTextListener;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class NickSearchActivity extends BaseActivity implements Runnable, OnSearchTextListener,
        OnItemClickListener {

    private static final long DELAY_MILLS = 500;

    private Handler mHandler;
    private String mSearchKey;

    private UserInfoAdapter mAdapter;
    private LinearLayout mMagicLinearLayout;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_search_generic;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.adc_nick_search, null);
        ((TextView) findViewById(R.id.tv_search_hint)).setText(R.string.adc_search_hint);

        mHandler = new Handler();

        SearchView svContact = (SearchView) findViewById(R.id.sv_search);
        svContact.setHint(R.string.adc_input_hint);
        svContact.setOnSearchTextListener(this);

        PullToRefreshListView ptrlvContacts = (PullToRefreshListView) findViewById(R.id.lv_search_result);
        ptrlvContacts.setMode(PullToRefreshBase.Mode.DISABLED);
        ptrlvContacts.getRefreshableView().addFooterView(new View(this));
        ptrlvContacts.setOnItemClickListener(this);

        mAdapter = new UserInfoAdapter(this, false);
        ptrlvContacts.setAdapter(mAdapter);
        mMagicLinearLayout = (LinearLayout) findViewById(R.id.ll_bg_search_generic);
    }

    @Override
    public void onSearchTextChanged(CharSequence text) {
        getTitleBar().showProgress();
        mSearchKey = text.toString();
        mHandler.removeCallbacks(this);
        mHandler.postDelayed(this, DELAY_MILLS);
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(mSearchKey)) {
            mAdapter.clear();
            getTitleBar().hideProgress();
            return;
        }

        new SearchTask(mSearchKey).executeLong();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserInfo user = (UserInfo) parent.getItemAtPosition(position);
        Intent i = new Intent(this, NewProfileActivity.class);
        i.putExtra(UserInfo.INTENT_EXTRA_INFO, user);
        startActivity(i);
    }

    private class SearchTask extends AdvAsyncTask<Void, Void, MsResponse> {
        private String mKey;

        private SearchTask(String key) {
            mKey = key;
        }

        @Override
        protected MsResponse doInBackground(Void... params) {
            return HttpUtil.msRequest(getApplicationContext(), MsRequest.FIND_BY_NICK,
                    "limit=40&nick=" + Utils.urlEncode(mKey));
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            getTitleBar().hideProgress();
            Context ctx = getApplicationContext();
            if (MsResponse.isSuccessful(response)) {
                JSONArray ja = response.json.optJSONArray(MsResponse.PARAM_RESPONSE);
                int length = ja == null ? 0 : ja.length();
                mAdapter.setNotifyOnChange(false);
                mAdapter.clear();
                if (length > 0) {
                    UserEntryManager uem = UserEntryManager.getInstance(ctx);
                    for (int i = 0; i < length; i++) {
                        UserInfo user = UserInfo.fromJson(ja.optJSONObject(i));
                        if (user != null && !uem.isFriend(user.jid) && !user.isMine(ctx)) {
                            mAdapter.add(user);
                        }
                    }
                }
                mAdapter.setKeyword(mKey);
                mAdapter.notifyDataSetChanged();
            } else {
                toast(MsResponse.getFailureDesc(ctx, R.string.adc_search_failed, response.code));
            }
        }
    }
}
