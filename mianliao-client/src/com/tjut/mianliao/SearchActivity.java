package com.tjut.mianliao;

import java.util.ArrayList;

import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.ExpandableGridView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.TitleBar;
import com.tjut.mianliao.component.tab.Tab;
import com.tjut.mianliao.component.tab.TabController;
import com.tjut.mianliao.component.tab.TabController.TabListener;
import com.tjut.mianliao.component.tab.TextTab;
import com.tjut.mianliao.contact.UserEntryManager;
import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.SchoolInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.tribe.TribeInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.nova.FormOtherSchoolActivity;
import com.tjut.mianliao.forum.nova.TribePostAdapter;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.tribe.TribeDetailActivity;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class SearchActivity extends BaseActivity implements OnClickListener,
        OnRefreshListener2<ListView>, OnEditorActionListener, TextWatcher, 
        OnFocusChangeListener, Runnable {
    
    public final static int SEARCH_TYPE_SCHOOL = 0;
    public final static int SEARCH_TYPE_TRIBE = 1;
    public final static int SEARCH_TYPE_POST = 2;
    public final static int SEARCH_TYPE_USER = 3;
    private static final long DELAY_MILLS = 500;
    
    private TextView mTvSearchHistory;
    private TextView mTvSearchSchool;
    private TextView mTvSearchTribe;
    private TextView mTvSearchPost;
    
    private TextView mTvSearchUser;
    private TextTab mTtSchool;
    private TextTab mTtTribe;
    private TextTab mTtPost;
    private TextTab mTtUser;
    private TabController mTabController;
    private String mSearchText;
    private PullToRefreshListView mPtrSearchResults;
    private ListView mPtrSearchHistory;
    private int mSearchType = 0;
    private ArrayList<SchoolInfo> mSearchSchools;
    private ArrayList<TribeInfo> mSearchTribes;
    private ArrayList<ChannelInfo> mSearchChannels;
    private ArrayList<UserInfo> mSearchUsers;
    private ArrayList<String> mSearchHistorys;
    private TribePostAdapter mPostAdapter;
    private ExpandableGridView mGvHotSearch;
    private LinearLayout mLlHotSearch;
    private ArrayList<TribeInfo> mHotSearchTribes;
    private EditText mTvSearch;
    private ImageView mIvSearchClear;
    private View mSearchView;
    private LinearLayout mLlSearchHistory;
    private TextView mTvClearHistory, mTvSearchHot;
    private SharedPreferences mPreferences;
    private String mSchoolHistory, mTribeHistory, mUserHistory, mPostHistory;
    private ArrayList<String> mSchoolHistorys, mTribeHistorys, mPostHistorys, mUserHistorys;
    private Handler mHandler;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mSettings = Settings.getInstance(this);
        mPreferences = DataHelper.getSpForData(this);
        TitleBar titleBar = getTitleBar();
        mTvSearch = (EditText) titleBar.findViewById(R.id.et_search_content);
        mIvSearchClear = (ImageView) titleBar.findViewById(R.id.iv_search_clear);
        titleBar.showRightText(R.string.search_cancel, this);
        titleBar.showLeftButton(R.drawable.botton_bg_arrow, this);
        mTvSearch.setHintTextColor(0XFFA9A9A9);
        mTvSearch.addTextChangedListener(this);
        mTvSearch.setOnFocusChangeListener(this);
        mTvSearch.setOnEditorActionListener(this);
        mIvSearchClear.setOnClickListener(this);
        mTvSearchHistory = (TextView) findViewById(R.id.tv_search_history);
        mTvSearchSchool = (TextView) findViewById(R.id.tv_search_school);
        mTvSearchTribe = (TextView) findViewById(R.id.tv_search_tribe);
        mTvSearchPost = (TextView) findViewById(R.id.tv_search_post);
        mTvSearchUser = (TextView) findViewById(R.id.tv_search_user);
        mPtrSearchResults = (PullToRefreshListView) findViewById(R.id.ptrlv_search_result);
        mPtrSearchHistory = (ListView) findViewById(R.id.ptrlv_search_history);
        mGvHotSearch = (ExpandableGridView) findViewById(R.id.gv_search_hot);
        mLlHotSearch = (LinearLayout) findViewById(R.id.ll_search_hot);
        mLlSearchHistory = (LinearLayout) findViewById(R.id.ll_search_history);
        mTvClearHistory = (TextView) findViewById(R.id.tv_clear_history);
        mTvSearchHot = (TextView) findViewById(R.id.tv_search_hot);
        
        mHandler = new Handler();
        
        mSchoolHistorys = new ArrayList<String>();
        mTribeHistorys = new ArrayList<String>();
        mPostHistorys = new ArrayList<String>();
        mUserHistorys = new ArrayList<String>();
        mSearchHistorys = new ArrayList<String>();
        
        mHotSearchTribes = new ArrayList<TribeInfo>();
        mSearchSchools = new ArrayList<SchoolInfo>();
        mSearchTribes = new ArrayList<TribeInfo>();
        mSearchChannels = new ArrayList<ChannelInfo>();
        mSearchUsers = new ArrayList<UserInfo>();
        
        mPostAdapter = new TribePostAdapter(this);
        mPostAdapter.setIsShowTribeIndetail(true);
        mPostAdapter.setIsShowSchoolName(true);
        mPostAdapter.setIsTribePosts(true);
        mPostAdapter.setTextClickble(true);
        mPostAdapter.setSpanClickble(true);
        
        mTtSchool = new TextTab(mTvSearchSchool);
        mTtTribe = new TextTab(mTvSearchTribe);
        mTtPost = new TextTab(mTvSearchPost);
        mTtUser = new TextTab(mTvSearchUser);

        mTtSchool.setChosen(true);
        mTtTribe.setChosen(false);
        mTtPost.setChosen(false);
        mTtUser.setChosen(false);

        mTabController = new TabController();
        mTabController.add(mTtSchool);
        mTabController.add(mTtTribe);
        mTabController.add(mTtPost);
        mTabController.add(mTtUser);
        mTabController.setListener(mTabListen);
        mPtrSearchResults.setAdapter(mSearchSchoolAdapter);
        mPtrSearchHistory.setAdapter(mSearchHistoryAdapter);
        mGvHotSearch.setAdapter(mSearchHotAdapter);
        mPtrSearchResults.setVisibility(View.GONE);
        mPtrSearchResults.setMode(Mode.BOTH);
        mPtrSearchResults.setOnRefreshListener(this);
        mLlHotSearch.setVisibility(View.GONE);
        mTvSearch.setOnClickListener(this);
        mTvClearHistory.setOnClickListener(this);
        mLlSearchHistory.setVisibility(View.GONE);
        mTvClearHistory.setVisibility(View.GONE);
        mTvSearchHistory.setVisibility(View.GONE);
        getHistory();
    }
    
    private void getHistory() {
        mSchoolHistory = mPreferences.getString("school", "");
        mTribeHistory = mPreferences.getString("tribe", "");
        mPostHistory = mPreferences.getString("post", "");
        mUserHistory = mPreferences.getString("user", "");
        getHistoryList(mSchoolHistory, 0);
        getHistoryList(mTribeHistory, 1);
        getHistoryList(mPostHistory, 2);
        getHistoryList(mUserHistory, 3);
        mSearchHistorys = getHistoryList();
        mSearchHistoryAdapter.notifyDataSetChanged();
        if (getHistoryList().size() <= 0) {
            mTvClearHistory.setVisibility(View.GONE);
            mTvSearchHistory.setVisibility(View.GONE);
        } else {
            mTvClearHistory.setVisibility(View.VISIBLE);
            mTvSearchHistory.setVisibility(View.VISIBLE);
        }
        new HotSearchTribeTask().executeLong();
    }

    @Override
    protected TitleBar getTitleBar() {
        TitleBar titleBar = super.getTitleBar();
        mSearchView = mInflater.inflate(R.layout.search_activity_search_bar, null);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        int marginRight = getResources().getDimensionPixelOffset(R.dimen.search_margin_right);
        lp.setMargins(marginRight, 0, marginRight, 0);
        mSearchView.setLayoutParams(lp);
        titleBar.addView(mSearchView);
        return titleBar;
    }

    TabListener mTabListen = new TabListener() {

        @Override
        public void onTabSelectionChanged(int index, boolean selected, Tab tab) {
            if (!selected) {
                return;
            }
            showSearchResult(index, mSearchText);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_right:
                mTvSearch.setText("");
                break;
            case R.id.tv_clear_history:
                getHistoryList().clear();
                mSearchHistorys = getHistoryList();
                mSearchHistoryAdapter.notifyDataSetChanged();
                mTvClearHistory.setVisibility(View.GONE);
                mTvSearchHistory.setVisibility(View.GONE);
                break;
            case R.id.btn_left:
                setHistoryStr();
                Editor editor = mPreferences.edit();
                editor.putString("school",mSchoolHistory);
                editor.putString("tribe",mTribeHistory);
                editor.putString("post",mPostHistory);
                editor.putString("user",mUserHistory);
                editor.commit();
                finish();
                break;
            case R.id.ll_tribe_from:
                int tribeId = (int) v.getTag();
                Intent trIntent = new Intent(SearchActivity.this, TribeDetailActivity.class);
                trIntent.putExtra(TribeDetailActivity.EXT_DATA_ID, tribeId);
                startActivity(trIntent);
                break;
            default:
                break;
        }
    }

    private BaseAdapter mSearchSchoolAdapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return mSearchSchools.size();
        }

        @Override
        public Object getItem(int position) {
            return mSearchSchools.get(position);
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
                view = mInflater.inflate(R.layout.list_item_search_school, parent, false);
            }
            SchoolInfo mSearchSchool = mSearchSchools.get(position);
            TextView mTvSchoolName = (TextView) view.findViewById(R.id.tv_school_name);
            ImageView mIvIslock = (ImageView) view.findViewById(R.id.iv_is_lock);
            mTvSchoolName.setText(mSearchSchool.name);
            mIvIslock.setImageResource(mSearchSchool.isUnlock() ? R.drawable.icon_search_unlock
                    : R.drawable.icon_search_lock);
            view.setTag(mSearchSchool);
            view.setOnClickListener(mSchoolListener);
            return view;
        }

    };
    
    private OnClickListener mSchoolListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            SchoolInfo mSchool = (SchoolInfo) v.getTag();
            if (mSchool.isUnlock()) {
                Intent intent  = new Intent(SearchActivity.this, FormOtherSchoolActivity.class);
                intent.putExtra(Forum.INTENT_EXTRA_SCHOOLID, mSchool.schoolId);
                intent.putExtra(Forum.INTENT_EXTRA_SCHOOLNAME, mSchool.name);
                intent.putExtra(Forum.INTENT_EXTRA_ISCLOOECTION, mSchool.isCollection);
                startActivity(intent);
            } else {
                toast(R.string.search_school_is_lock);
            }
        } 
    };

    private BaseAdapter mSearchTribeAdapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return mSearchTribes.size();
        }

        @Override
        public TribeInfo getItem(int position) {
            return mSearchTribes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolderChannel viewHolder;
            if (convertView != null) {
                viewHolder = (ViewHolderChannel) convertView.getTag();
            } else {
                convertView = mInflater.inflate(R.layout.list_item_search_channel, parent, false);
                viewHolder = new ViewHolderChannel();
                viewHolder.mIvTribeIcon = (ProImageView) convertView.findViewById(R.id.iv_channel_icon);
                viewHolder.mTvTribeName = (TextView) convertView.findViewById(R.id.tv_channel_name);
                viewHolder.mTvTribeDesc = (TextView) convertView.findViewById(R.id.tv_channel_desc);
                convertView.setTag(viewHolder);
            }
            TribeInfo mTribeInfo = mSearchTribes.get(position);
            viewHolder.mIvTribeIcon.setImage(mTribeInfo.icon, 0);
            viewHolder.mTvTribeName.setText(mTribeInfo.tribeName);
            viewHolder.mTvTribeDesc.setText(mTribeInfo.tribeDesc);
            viewHolder.mTribe = mTribeInfo;
            convertView.setOnClickListener(mTeribeClickListener);
            return convertView;
        }

    };
    
    
    private OnClickListener mTeribeClickListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            ViewHolderChannel viewHolder;
            viewHolder = (ViewHolderChannel) v.getTag();
            TribeInfo tribe = viewHolder.mTribe;
            Intent intent  = new Intent(SearchActivity.this, TribeDetailActivity.class);
            intent.putExtra(TribeInfo.INTENT_EXTRA_INFO, tribe);
            startActivity(intent);
        }
    };

    private class ViewHolderChannel {
        ProImageView mIvTribeIcon;
        TextView mTvTribeName;
        TextView mTvTribeDesc;
        TribeInfo mTribe;
        ChannelInfo mChannelInfo;
    }

    private BaseAdapter mSearchUserAdapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return mSearchUsers.size();
        }

        @Override
        public Object getItem(int position) {
            return mSearchUsers.get(position);
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
                view = mInflater.inflate(R.layout.list_item_search_user, parent, false);
            }
            UserInfo userInfo = mSearchUsers.get(position);
            TextView tvName = (TextView) view.findViewById(R.id.tv_user_name);
            TextView tvSchool = (TextView) view.findViewById(R.id.tv_user_school);
            ProImageView ivMedal = (ProImageView) view.findViewById(R.id.iv_user_medal);
            ImageView ivGender = (ImageView) view.findViewById(R.id.iv_user_gender);
            AvatarView mAvatar = (AvatarView) view.findViewById(R.id.iv_contact_avatar);
            ImageView ivTypeIcon = (ImageView) view.findViewById(R.id.iv_type_icon);
            ImageView ivVip = (ImageView) view.findViewById(R.id.iv_vip_bg);
            tvName.setText(userInfo.nickname);
            tvSchool.setText(userInfo.school);
            Picasso.with(SearchActivity.this).load(userInfo.getAvatar())
                .placeholder(R.drawable.chat_botton_bg_faviconboy).into(mAvatar);
            if (userInfo.getLatestBadge() != null) {
                Picasso.with(SearchActivity.this).load(userInfo.getLatestBadge()).into(ivMedal);
            } else {
                ivMedal.setVisibility(View.GONE);
            }
            Picasso.with(SearchActivity.this)
                .load(userInfo.gender == 0 ? R.drawable.img_girl : R.drawable.img_boy)
                .into(ivGender);
            int typeIcon = userInfo.getTypeIcon();
            if (ivTypeIcon != null) {
                if (typeIcon > 0){
                    ivTypeIcon.setImageResource(typeIcon);
                    ivTypeIcon.setVisibility(View.VISIBLE);
                } else {
                    ivTypeIcon.setVisibility(View.GONE);
                }
            }
            if (userInfo.vip){
                ivVip.setVisibility(View.VISIBLE);
            } else {
                ivVip.setVisibility(View.GONE);
            }
            view.setTag(userInfo);
            view.setOnClickListener(mUserListener);
            return view;
        }
    };
    
    private OnClickListener mUserListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            UserInfo userInfo = (UserInfo) v.getTag();
            Intent intent = new Intent(SearchActivity.this, NewProfileActivity.class);
            intent.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
            startActivity(intent);
        }
    };

    private BaseAdapter mSearchHistoryAdapter = new  BaseAdapter() {

        @Override
        public int getCount() {
            return mSearchHistorys.size();
        }

        @Override
        public String getItem(int position) {
            return mSearchHistorys.get(position);
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
                view = mInflater.inflate(R.layout.list_item_search_history, parent, false);
            }
            String mSearchContent = getItem(position);
            TextView mTvSearchContent = (TextView) view.findViewById(R.id.tv_search_content);
            ImageView mIvClearHistory = (ImageView) view.findViewById(R.id.iv_clear_history);
            mTvSearchContent.setText(getItem(position));
            mIvClearHistory.setTag(position);
            view.setTag(getItem(position));
            mTvSearchContent.setText(mSearchContent);
            mIvClearHistory.setOnClickListener(mClearHistoryListener);
            view.setOnClickListener(mClearHistoryListener);
            return view;
        }

    };
    
    private OnClickListener mClearHistoryListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_clear_history:
                    int i = (int) v.getTag();
                    getHistoryList().remove(i);
                    mSearchHistorys = getHistoryList();
                    mSearchHistoryAdapter.notifyDataSetChanged();
                    if (mSearchHistoryAdapter.getCount() <= 0) {
                        mTvClearHistory.setVisibility(View.GONE);
                        mTvSearchHistory.setVisibility(View.GONE);
                    } else {
                        mTvClearHistory.setVisibility(View.VISIBLE);
                        mTvSearchHistory.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.rl_history:
                    String searchString = (String) v.getTag();
                    mTvSearch.setText(searchString);
                    Spannable spanText = (Spannable)mTvSearch.getText();
                    Selection.setSelection(spanText, searchString.length());
                    mLlSearchHistory.setVisibility(View.GONE);
                    mTvClearHistory.setVisibility(View.GONE);
                    mTvSearchHistory.setVisibility(View.GONE);
                    mPtrSearchResults.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };
    
    private void search(int type, String name) {
        getTitleBar().showProgress();
        switch (type) {
            case 0:
                new SearchSchoolTask(name, 0).executeLong();
                break;
            case 1:
                new SearchTribeTask(name, 0).executeLong();
                break;
            case 2:
                new SearchPostTask(name, 0).executeLong();
                break;
            case 3:
                new SearchUserTask(name, 0).executeLong();
                break;
            default:
                break;
        }
    }

    public class SearchSchoolTask extends MsTask {
        private String mName;
        private int mOffset;

        public SearchSchoolTask(String name, int offset) {
            super(SearchActivity.this, MsRequest.SCHOOL_SEARCH );
            mName = name;
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder("offset=").append(mOffset)
                    .append("&name=").append(Utils.urlEncode(mName));
            return sb.toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            Utils.hidePgressDialog();
            mPtrSearchResults.onRefreshComplete();
            if (response.isSuccessful()) {
                boolean isRepeat = false;
                for (int i = 0; i < mSchoolHistorys.size(); i++) {
                    if (mName.equals(mSchoolHistorys.get(i))) {
                        isRepeat = true;
                    }
                } 
                if (!isRepeat) {
                    mSchoolHistorys.add(mName);
                }
                if (mOffset <= 0) {
                    mSearchSchools.clear();
                }
                JSONArray array = response.getJsonArray();
                if (array != null && array.length() > 0) {
                    for (int i = 0; i < array.length(); i++) {
                        SchoolInfo fs = SchoolInfo.fromJson(array.optJSONObject(i));
                        mSearchSchools.add(fs);
                    }
                    mSearchSchoolAdapter.notifyDataSetChanged();

                } else {
                    toast(R.string.search_result_null);
                }
            }
            mSearchText = mName;
        }
    }
    
    public class SearchTribeTask extends MsTask {
        private String mName;
        private int mOffset;
        
        public SearchTribeTask(String name, int offset) {
            super(SearchActivity.this, MsRequest.TRIBE_SEARCH );
            mName = name;
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder("searchStr=").append(Utils.urlEncode(mName))
                    .append("&offset=").append(mOffset);
            return sb.toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            Utils.hidePgressDialog();
            mPtrSearchResults.onRefreshComplete();
            if (response.isSuccessful()) {
                boolean isRepeat = false;
                for (int i = 0; i < mTribeHistorys.size(); i++) {
                    if (mName.equals(mTribeHistorys.get(i))) {
                        isRepeat = true;
                    }
                } 
                if (!isRepeat) {
                    mTribeHistorys.add(mName);
                }
                ArrayList<TribeInfo> tribes = JsonUtil.getArray(response.getJsonArray(), TribeInfo.TRANSFORMER);
                if (mOffset <= 0) {
                    mSearchTribes.clear();
                }
                if (tribes.size() <= 0) {
                    toast(R.string.search_result_null);
                }
                mSearchTribes.addAll(tribes);
                mSearchTribeAdapter.notifyDataSetChanged();

            }
            mSearchText = mName;
        }
    }
    
    
    public class SearchPostTask extends MsTask {
        private String mName;
        private int mOffset;

        public SearchPostTask(String src, int offset) {
            super(SearchActivity.this, MsRequest.LIST_SEARCH_POST );
            mName = src;
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder("searchStr=").append(Utils.urlEncode(mName))
                    .append("&offset=").append(mOffset);
            return sb.toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            Utils.hidePgressDialog();
            mPtrSearchResults.onRefreshComplete();
            if (response.isSuccessful()) {
                boolean isRepeat = false;
                for (int i = 0; i < mPostHistorys.size(); i++) {
                    if (mName.equals(mPostHistorys.get(i))) {
                        isRepeat = true;
                        break;
                    }
                } 
                if (!isRepeat) {
                    mPostHistorys.add(mSearchText);
                }
                ArrayList<CfPost> posts = JsonUtil.getArray(response.getJsonArray(), CfPost.TRANSFORMER);
                if (posts.size() <= 0) {
                    toast(R.string.search_result_null);
                }
                if (mOffset <= 0) {
                    mPostAdapter.reset(posts);
                } else {
                    mPostAdapter.addAll(posts);
                }
            }
            mSearchText = mName;
        }
    }
    
    public class HotSearchTribeTask extends MsTask {

        public HotSearchTribeTask() {
            super(SearchActivity.this, MsRequest.TRIBE_HOT_SEARCH );
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ArrayList<TribeInfo> tribes = JsonUtil.getArray(response.getJsonArray(), TribeInfo.TRANSFORMER);
                mHotSearchTribes.addAll(tribes);
                if (mHotSearchTribes.size() != 4){
                    mGvHotSearch.setVisibility(View.GONE);
                    mTvSearchHot.setVisibility(View.GONE);
                } else {
                    mTvSearchHot.setVisibility(View.VISIBLE);
                    mGvHotSearch.setVisibility(View.VISIBLE);
                    mSearchHotAdapter.notifyDataSetChanged();
                }

            }
        }
    }

    private BaseAdapter mSearchHotAdapter = new BaseAdapter() {

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HotSearchViewHoder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_hot_search, parent, false);
                holder = new HotSearchViewHoder();
                ViewUtils.inject(holder, convertView);
                convertView.setTag(holder);
            } else {
                holder = (HotSearchViewHoder) convertView.getTag();
            }
            TribeInfo tribe = getItem(position);
            holder.mTribe = tribe;
            holder.mTvTribeName.setText(tribe.tribeName);
            switch (position) {
                case 0:
                    holder.mLineLeft.setVisibility(View.VISIBLE);
                    holder.mLineTop.setVisibility(View.VISIBLE);
                    holder.mLineRight.setVisibility(View.GONE);
                    holder.mLineBottom.setVisibility(View.GONE);
                    break;
                case 1:
                    holder.mLineLeft.setVisibility(View.GONE);
                    holder.mLineTop.setVisibility(View.GONE);
                    holder.mLineRight.setVisibility(View.VISIBLE);
                    holder.mLineBottom.setVisibility(View.GONE);
                    break;
                case 2:
                    holder.mLineLeft.setVisibility(View.GONE);
                    holder.mLineTop.setVisibility(View.GONE);
                    holder.mLineRight.setVisibility(View.GONE);
                    holder.mLineBottom.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
            convertView.setOnClickListener(mHotSearchListen);
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public TribeInfo getItem(int position) {
            return mHotSearchTribes.get(position);
        }

        @Override
        public int getCount() {
            return mHotSearchTribes.size();
        }
    };
    
    private OnClickListener mHotSearchListen = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            HotSearchViewHoder holder = (HotSearchViewHoder) v.getTag();
            Intent intent  = new Intent(SearchActivity.this, TribeDetailActivity.class);
            intent.putExtra(TribeInfo.INTENT_EXTRA_INFO, holder.mTribe);
            startActivity(intent);
        }
    };

    private class HotSearchViewHoder {
        @ViewInject(R.id.tv_tribe_name)
        TextView mTvTribeName;
        @ViewInject(R.id.line_horizontal_left)
        View mLineLeft;
        @ViewInject(R.id.line_horizontal_right)
        View mLineRight;
        @ViewInject(R.id.line_vertical_top)
        View mLineTop;
        @ViewInject(R.id.line_vertical_bottom)
        View mLineBottom;
        TribeInfo mTribe;
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        switch (mSearchType) {
            case SEARCH_TYPE_SCHOOL:
                new SearchSchoolTask(mSearchText, 0).executeLong();
                break;
            case SEARCH_TYPE_TRIBE:
                new SearchTribeTask(mSearchText, 0).executeLong();
                break;
            case SEARCH_TYPE_POST:
                new SearchPostTask(mSearchText, 0).executeLong();
                break;
            case SEARCH_TYPE_USER:
                new SearchUserTask(mSearchText, 0).executeLong();
                break;
            default:
                break;
        }
       
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        switch (mSearchType) {
            case SEARCH_TYPE_SCHOOL:
                new SearchSchoolTask(mSearchText, mSearchSchoolAdapter.getCount()).executeLong();
                break;
            case SEARCH_TYPE_TRIBE:
                new SearchTribeTask(mSearchText, mSearchTribeAdapter.getCount()).executeLong();
                break;
            case SEARCH_TYPE_POST:
                new SearchPostTask(mSearchText, mPostAdapter.getCount()).executeLong();
                break;
            case SEARCH_TYPE_USER:
                new SearchUserTask(mSearchText, mSearchUserAdapter.getCount()).executeLong();
                break;
            default:
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mSearchText = mTvSearch.getText().toString();
        if (mSearchText != null && !mSearchText.equals("")) {
            mLlSearchHistory.setVisibility(View.GONE);
            mTvClearHistory.setVisibility(View.GONE);
            mTvSearchHistory.setVisibility(View.GONE);
            mPtrSearchResults.setVisibility(View.VISIBLE);
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, DELAY_MILLS);
        } else {
            mLlSearchHistory.setVisibility(View.VISIBLE);
            if (getHistoryList().size() <= 0) {
                mTvClearHistory.setVisibility(View.GONE);
                mTvSearchHistory.setVisibility(View.GONE);
            } else {
                mTvClearHistory.setVisibility(View.VISIBLE);
                mTvSearchHistory.setVisibility(View.VISIBLE);
            }
            mSearchHistorys = getHistoryList();
            mSearchHistoryAdapter.notifyDataSetChanged();
            mPtrSearchResults.setVisibility(View.GONE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
    
    private class SearchUserTask extends AdvAsyncTask<Void, Void, MsResponse> {
        private String mKey;
        private int mOffset;

        private SearchUserTask(String key, int offset) {
            mKey = key;
            mOffset = offset;
        }

        @Override
        protected MsResponse doInBackground(Void... params) {
            return HttpUtil.msRequest(getApplicationContext(), MsRequest.FIND_BY_NICK,
                    "offset="+ mOffset+ "&nick=" + Utils.urlEncode(mKey));
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            Utils.hidePgressDialog();
            mPtrSearchResults.onRefreshComplete();
            Context ctx = getApplicationContext();
            if (response.isSuccessful()) {
                boolean isRepeat = false;
                for (int i = 0; i < mUserHistorys.size(); i++) {
                    if (mKey.equals(mUserHistorys.get(i))) {
                        isRepeat = true;
                    }
                } 
                if (!isRepeat) {
                    mUserHistorys.add(mKey);
                }
                JSONArray ja = response.getJsonArray();
                int length = ja == null ? 0 : ja.length();
                if (mOffset <= 0) {
                    mSearchUsers.clear();
                }
                if (length > 0) {
                    UserEntryManager uem = UserEntryManager.getInstance(ctx);
                    for (int i = 0; i < length; i++) {
                        UserInfo user = UserInfo.fromJson(ja.optJSONObject(i));
                        if (user != null && !uem.isFriend(user.jid) && !user.isMine(ctx)) {
                            mSearchUsers.add(user);
                        }
                    }
                } else {
                    toast(R.string.search_result_null);
                }
                mSearchUserAdapter.notifyDataSetChanged();
            } else {
                toast(MsResponse.getFailureDesc(ctx, R.string.adc_search_failed, response.code));
            }
            mSearchText = mKey;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        mTvSearch.clearFocus();
        return true;
    }
    
    @Override
    public void run() {
        showSearchResult(mSearchType, mSearchText);
    }
    

    private void showSearchResult(int type, String searchText) {
        switch (type) {
            case SEARCH_TYPE_SCHOOL:
                mSearchType = SEARCH_TYPE_SCHOOL;
                mLlHotSearch.setVisibility(View.GONE);
                mPtrSearchResults.setAdapter(mSearchSchoolAdapter);
                if (searchText != null && !searchText.equals("")) {
                    new SearchSchoolTask(searchText, 0).executeLong();
                    Utils.showProgressDialog(SearchActivity.this, R.string.forum_search_ing);
                }
                mTtTribe.setChosen(false);
                mTtPost.setChosen(false);
                mTtUser.setChosen(false);
                break;
            case SEARCH_TYPE_TRIBE:
                mSearchType = SEARCH_TYPE_TRIBE;
                mLlHotSearch.setVisibility(View.VISIBLE);
                if (searchText != null && !searchText.equals("")) {
                    mPtrSearchResults.setAdapter(mSearchTribeAdapter);
                    new SearchTribeTask(searchText, 0).executeLong();
                    Utils.showProgressDialog(SearchActivity.this, R.string.forum_search_ing);
                } 
                mTtSchool.setChosen(false);
                mTtPost.setChosen(false);
                mTtUser.setChosen(false);
                break;
            case SEARCH_TYPE_POST:
                mSearchType = SEARCH_TYPE_POST;
                mLlHotSearch.setVisibility(View.GONE);
                mPtrSearchResults.setAdapter(mPostAdapter);
                if (searchText != null && !searchText.equals("")) {
                    new SearchPostTask(searchText, 0).executeLong();
                    Utils.showProgressDialog(SearchActivity.this, R.string.forum_search_ing);
                }
                mTtSchool.setChosen(false);
                mTtTribe.setChosen(false);
                mTtUser.setChosen(false);
                break;
            case SEARCH_TYPE_USER:
                mSearchType = SEARCH_TYPE_USER;
                mLlHotSearch.setVisibility(View.GONE);
                mPtrSearchResults.setAdapter(mSearchUserAdapter);
                
                if (searchText != null && !searchText.equals("")) {
                    new SearchUserTask(searchText, 0).executeLong();
                    Utils.showProgressDialog(SearchActivity.this, R.string.forum_search_ing);
                }
                mTtSchool.setChosen(false);
                mTtTribe.setChosen(false);
                mTtPost.setChosen(false);
                break;
            default:
                break;
        }
        mSearchHistorys = getHistoryList();
        mSearchHistoryAdapter.notifyDataSetChanged();
        if (getHistoryList().size() <= 0) {
            mTvClearHistory.setVisibility(View.GONE);
            mTvSearchHistory.setVisibility(View.GONE);
        } else {
            mTvClearHistory.setVisibility(View.VISIBLE);
            mTvSearchHistory.setVisibility(View.VISIBLE);
        }
    }
    
    private void getHistoryList(String str, int type) {
        String [] temp = null;  
        if (!str.equals("")) {
            temp = str.split("-");  
        }
        if (temp != null && temp.length > 0) {
            for (int i = 0; i < temp.length; i++) {
                switch (type) {
                    case 0:
                        mSchoolHistorys.add(temp[i]);
                        break;
                    case 1:
                        mTribeHistorys.add(temp[i]);
                        break;
                    case 2:
                        mPostHistorys.add(temp[i]);
                        break;
                    case 3:
                        mUserHistorys.add(temp[i]);
                        break;
                    default:
                        break;
                }
            }
        } 
    }

    private void setHistoryStr() {
        mSchoolHistory = "";
        mTribeHistory = "";
        mPostHistory = "";
        mUserHistory = "";
        for (int i = 0; i < mSchoolHistorys.size(); i++) {
            if (i < (mSchoolHistorys.size() - 1)) {
                mSchoolHistory = mSchoolHistory + mSchoolHistorys.get(i) + "-";
            } else {
                mSchoolHistory = mSchoolHistory + mSchoolHistorys.get(i);
            }
        }
        for (int i = 0; i < mTribeHistorys.size(); i++) {
            if (i < (mTribeHistorys.size() - 1)) {
                mTribeHistory = mTribeHistory + mTribeHistorys.get(i) + "-";
            } else {
                mTribeHistory = mTribeHistory + mTribeHistorys.get(i);
            }
        }
        for (int i = 0; i < mPostHistorys.size(); i++) {
            if (i < (mPostHistorys.size() - 1)) {
                mPostHistory = mPostHistory + mPostHistorys.get(i) + "-";
            } else {
                mPostHistory = mPostHistory + mPostHistorys.get(i);
            }
        }
        for (int i = 0; i < mUserHistorys.size(); i++) {
            if (i < (mUserHistorys.size() - 1)) {
                mUserHistory = mUserHistory + mUserHistorys.get(i) + "-";
            } else {
                mUserHistory = mUserHistory + mUserHistorys.get(i);
            }
        }
    }
    
    private ArrayList<String> getHistoryList() {
        switch (mSearchType) {
            case 0:
                return mSchoolHistorys;
            case 1:
                return mTribeHistorys;
            case 2:
                return mPostHistorys;
            case 3:
                return mUserHistorys;
            default:
                return null;
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setHistoryStr();
            Editor editor = mPreferences.edit();
            editor.putString("school",mSchoolHistory);
            editor.putString("tribe",mTribeHistory);
            editor.putString("post",mPostHistory);
            editor.putString("user",mUserHistory);
            editor.commit();
            finish();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
    
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            mLlSearchHistory.setVisibility(View.VISIBLE);
            if (getHistoryList().size() <= 0) {
                mTvClearHistory.setVisibility(View.GONE);
                mTvSearchHistory.setVisibility(View.GONE);
            } else {
                mTvClearHistory.setVisibility(View.VISIBLE);
                mTvSearchHistory.setVisibility(View.VISIBLE);
            }
        } else {
            mLlSearchHistory.setVisibility(View.GONE);
            mTvClearHistory.setVisibility(View.GONE);
            mTvSearchHistory.setVisibility(View.GONE);
        }
        
    }

}
