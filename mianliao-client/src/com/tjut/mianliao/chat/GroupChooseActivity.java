package com.tjut.mianliao.chat;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.SearchView;
import com.tjut.mianliao.component.SearchView.OnClearIconClickListener;
import com.tjut.mianliao.component.SearchView.OnSearchTextListener;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.GroupInfo;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class GroupChooseActivity extends BaseActivity implements OnItemClickListener {

    private SearchView mETSearch;
    private ListView mLvGroup;
    private GroupInfo mGroupInfo;
    private ArrayList<GroupInfo> mGroupList;
    private boolean mIsSearching = false;
    private String mSearchStr;
    private ArrayList<GroupInfo> mDisplayChats = new ArrayList<>();
    private LinearLayout mRootLayout;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_choice_group;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGroupList = new ArrayList<>();
        getTitleBar().setTitle(R.string.Group_management);
        mETSearch = (SearchView) findViewById(R.id.sv_search);
        mLvGroup = (ListView) findViewById(R.id.lv_group_list);
        mLvGroup.setAdapter(mAdapter);
        mLvGroup.setOnItemClickListener(this);
        mETSearch = (SearchView) findViewById(R.id.sv_search);
        mETSearch.setOnSearchTextListener(mSearchListener);
        mETSearch.setOnClearIconClickListener(mSearchClearListener);
        mRootLayout = (LinearLayout) findViewById(R.id.ll_choose_group);
        refreshData();
    }
    
    @Override
    protected void onResume() {
        refreshData();     
        super.onResume();
    }

    private void refreshData() {
        new GetMygroupList().executeLong();
    }

    private class GetMygroupList extends MsTask {

        public GetMygroupList() {
            super(GroupChooseActivity.this, MsRequest.LIST_MY_GROUP);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ArrayList<GroupInfo> infos = JsonUtil.getArray(response.getJsonArray(), GroupInfo.TRANSFORMER);
                if (infos != null) {
                    mGroupList = infos;
                    mDisplayChats = infos;
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private BaseAdapter mAdapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return mDisplayChats.size();
        }

        @Override
        public GroupInfo getItem(int position) {
            return mDisplayChats.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.list_item_choiceggroup, parent, false);
            } else {
                view = convertView;
            }
            if (position >= (getCount() - 1)) {
            	view.findViewById(R.id.view_line).setVisibility(View.GONE);
            } else {
            	view.findViewById(R.id.view_line).setVisibility(View.VISIBLE);
            }
            GroupInfo mGroupInfo = getItem(position);
            TextView mGroupName = (TextView) view.findViewById(R.id.tv_group_name);
            GroupInfo gif = DataHelper.loadGroupInfo(GroupChooseActivity.this,
                    String.valueOf( mGroupInfo.jid));
            mGroupName.setText(mGroupInfo.groupName.equals(getString(R.string.cht_unnamed)) ?
                    gif.groupName : mGroupInfo.groupName);
            return view;
        }

    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GroupInfo cr = (GroupInfo) parent.getItemAtPosition(position);
        if (cr != null) {
            Intent i = new Intent(GroupChooseActivity.this, ChatActivity.class);
            i.putExtra(ChatActivity.EXTRA_CHAT_TARGET, getGroupJid(cr.jid));
            i.putExtra(ChatActivity.EXTRA_SHOW_PROFILE, true);
            i.putExtra(ChatActivity.EXTRA_CHAT_ISGOUPCHAT, true);
            i.putExtra(ChatActivity.EXTRA_GROUPCHAT_ID, cr.id);
            startActivity(i);
        }
    }

    private String getGroupJid(String jid) {
        return jid + "@groupchat.dev.tjut.cc";
    }

    OnSearchTextListener mSearchListener = new OnSearchTextListener() {

        @Override
        public void onSearchTextChanged(CharSequence text) {
            if (TextUtils.isEmpty(text)) {
                mIsSearching = false;
            } else {
                mSearchStr = text.toString();
                mIsSearching = true;
            }

            processSearch();
        }

    };

    OnClearIconClickListener mSearchClearListener = new OnClearIconClickListener() {

        @Override
        public void onClickClearIcon() {
            mIsSearching = false;

        }

    };

    private void processSearch() {
        if (!mIsSearching) {
            mDisplayChats = mGroupList;
            return;
        }
        mDisplayChats = new ArrayList<GroupInfo>();
        for (GroupInfo record : mGroupList) {
            if ((!TextUtils.isEmpty(record.target) && record.target.contains(mSearchStr))
                    || record.groupName.contains(mSearchStr)) {
                mDisplayChats.add(record);
            }
        }

    }
}
