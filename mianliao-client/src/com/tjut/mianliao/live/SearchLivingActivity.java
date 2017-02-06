package com.tjut.mianliao.live;

import java.util.ArrayList;

import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.TitleBar;
import com.tjut.mianliao.component.tab.Tab;
import com.tjut.mianliao.component.tab.TabController;
import com.tjut.mianliao.component.tab.TabController.TabListener;
import com.tjut.mianliao.component.tab.TextTab;
import com.tjut.mianliao.contact.FollowUserManager;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.FocusUserInfo;
import com.tjut.mianliao.data.LiveInfo;
import com.tjut.mianliao.data.LiveTopic;
import com.tjut.mianliao.data.SchoolInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.TopicInfo;
import com.tjut.mianliao.forum.nova.TribePostAdapter;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.tribe.TribeDetailActivity;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.Utils;

public class SearchLivingActivity extends BaseActivity implements OnClickListener,
        OnRefreshListener2<ListView>, OnEditorActionListener, TextWatcher, Runnable ,
        FollowUserManager.OnUserFollowListener {

    public final static int SEARCH_TYPE_USER = 0;
    public final static int SEARCH_TYPE_SCHOOL = 1;
    public final static int SEARCH_TYPE_TOPIC = 2;
    private static final long DELAY_MILLS = 500;

    private TextView mTvSearchSchool;
    private TextView mTvSearchTopic;
    private TextView mTvSearchUser;
    private TextTab mTtSchool;
    private TextTab mTtTopic;
    private TextTab mTtUser;
    private TabController mTabController;
    private String mSearchText;
    private PullToRefreshListView mPtrSearch;
//    private PullToRefreshGridView mPtrSearchLiving;
    private int mSearchType = 0;
    private ArrayList<LiveInfo> mSearchLives;
    private ArrayList<FocusUserInfo> mSearchUsers;
    private ArrayList<SchoolInfo> mSearchSchools;
    private ArrayList<LiveTopic> mSearchTopics;
    private TribePostAdapter mPostAdapter;
    private EditText mTvSearch;
    private ImageView mIvSearchClear;
    private View mSearchView;
    private SharedPreferences mPreferences;
    private Handler mHandler;
    private FollowUserManager mFollowUserManager;
    private FocusUserInfo mCurrentUser;


    @Override
    protected int getLayoutResID() {
        return R.layout.activity_living_search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mSettings = Settings.getInstance(this);
        mPreferences = DataHelper.getSpForData(this);
        mFollowUserManager = FollowUserManager.getInstance(this);
        mFollowUserManager.registerOnUserFollowListener(this);
        TitleBar titleBar = getTitleBar();
        mTvSearch = (EditText) titleBar.findViewById(R.id.et_search_content);
        mIvSearchClear = (ImageView) titleBar.findViewById(R.id.iv_search_clear);
        titleBar.showRightText(R.string.search_cancel, this);
        titleBar.showLeftButton(R.drawable.botton_bg_arrow, this);
        mTvSearch.setHintTextColor(0XFFA9A9A9);
        mTvSearch.addTextChangedListener(this);
        mTvSearch.setOnEditorActionListener(this);
        mIvSearchClear.setOnClickListener(this);
        mTvSearchUser = (TextView) findViewById(R.id.tv_search_user);
        mTvSearchSchool = (TextView) findViewById(R.id.tv_search_school);
        mTvSearchTopic = (TextView) findViewById(R.id.tv_search_topic);
        mPtrSearch = (PullToRefreshListView) findViewById(R.id.ptrl_search_people_list);
//        mPtrSearchLiving = (PullToRefreshGridView) findViewById(R.id.ptrg_search_living_list);

        mHandler = new Handler();

        mSearchLives = new ArrayList<>();
        mSearchUsers = new ArrayList<>();
        mSearchSchools = new ArrayList<>();
        mSearchTopics = new ArrayList<>();

        mPostAdapter = new TribePostAdapter(this);
        mPostAdapter.setIsShowTribeIndetail(true);
        mPostAdapter.setIsShowSchoolName(true);
        mPostAdapter.setIsTribePosts(true);
        mPostAdapter.setTextClickble(true);
        mPostAdapter.setSpanClickble(true);

        mTtUser = new TextTab(mTvSearchUser);
        mTtSchool = new TextTab(mTvSearchSchool);
        mTtTopic = new TextTab(mTvSearchTopic);

        mTtUser.setChosen(true);
        mTtSchool.setChosen(false);
        mTtTopic.setChosen(false);

        mTabController = new TabController();
        mTabController.add(mTtUser);
        mTabController.add(mTtSchool);
        mTabController.add(mTtTopic);
        mTabController.setListener(mTabListen);

        mPtrSearch.setVisibility(View.VISIBLE);
        mPtrSearch.setMode(Mode.BOTH);
        mPtrSearch.setOnRefreshListener(this);
        mPtrSearch.setAdapter(mSearchUserAdapter);

//        mPtrSearchLiving.setVisibility(View.GONE);
//        mPtrSearchLiving.setMode(Mode.BOTH);
//        mPtrSearchLiving.setOnRefreshListener(mGvRefreshlisten);
//        mPtrSearchLiving.setAdapter(mSearchLiveAdapter);

        mTvSearch.setOnClickListener(this);
    }

    @Override
    protected TitleBar getTitleBar() {
        TitleBar titleBar = super.getTitleBar();
        mSearchView = mInflater.inflate(R.layout.search_activity_search_bar, null);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
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
            case R.id.ll_tribe_from:
                int tribeId = (int) v.getTag();
                Intent trIntent = new Intent(SearchLivingActivity.this, TribeDetailActivity.class);
                trIntent.putExtra(TribeDetailActivity.EXT_DATA_ID, tribeId);
                startActivity(trIntent);
                break;
            case R.id.ll_search_live:
                LiveInfo liveInfo = (LiveInfo) v.getTag();
                break;
            case R.id.ll_live_info:
                break;
            case R.id.btn_left:
                finish();
                break;
            case R.id.ll_search_school:
                Intent intent = new Intent(SearchLivingActivity.this, LivingListActivity.class);
                if (v.getTag() instanceof SchoolInfo) {
                    SchoolInfo school = (SchoolInfo) v.getTag();
                    intent.putExtra(LivingListActivity.LIVE_SCHOOL_INFO_ID, school.schoolId);
                    intent.putExtra(LivingListActivity.LIVE_SCHOOL_INFO_NAME, school.name);
                    startActivity(intent);
                } else if( v.getTag() instanceof LiveTopic) {
                    LiveTopic topic = (LiveTopic) v.getTag();
                    intent.putExtra(LivingListActivity.LIVE_TOPIC_INFO, topic);
                    startActivity(intent);
                }
                break;
            default:
                break;
        }
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
            FocusUserInfo userInfo = mSearchUsers.get(position);
            TextView tvName = (TextView) view.findViewById(R.id.tv_user_name);
            TextView tvSchool = (TextView) view.findViewById(R.id.tv_user_school);
            ImageView ivGender = (ImageView) view.findViewById(R.id.iv_user_gender);
            AvatarView mAvatar = (AvatarView) view.findViewById(R.id.iv_contact_avatar);
            TextView mTvFollow = (TextView) view.findViewById(R.id.tv_attention_count);
            mTvFollow.setVisibility(View.VISIBLE);
            tvName.setText(userInfo.nickName);
            tvSchool.setText(userInfo.school);
            if (userInfo.avatar != null && !"".equals(userInfo.avatar)) {
                Picasso.with(SearchLivingActivity.this).load(userInfo.avatar)
                        .placeholder(R.drawable.chat_botton_bg_faviconboy).into(mAvatar);
            }
            Picasso.with(SearchLivingActivity.this)
                    .load(userInfo.gender == 0 ? R.drawable.img_girl : R.drawable.img_boy)
                    .into(ivGender);
            if (userInfo.isFollow) {
                mTvFollow.setText(R.string.tribe_is_followed);
                mTvFollow.setTextColor(0xff848484);
                mTvFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.channel_icon_ok, 0, 0, 0);
                mTvFollow.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.tribe_rad_men_follow_padding));
                mTvFollow.setBackgroundResource(R.drawable.bg_tv_green_circle);
            } else {
                mTvFollow.setText(R.string.tribe_collected_add);
                mTvFollow.setBackgroundResource(R.drawable.bg_tv_rad_circle);
                mTvFollow.setTextColor(0XFFFF9393);
                mTvFollow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            view.setTag(userInfo);
            mTvFollow.setTag(userInfo);
            mTvFollow.setOnClickListener(mUserListener);
            view.setOnClickListener(mUserListener);
            return view;
        }
    };

    @Override
    public void onFollowSuccess() {
        for(int i = 0;  i < mSearchUsers.size();i++) {
            if (mSearchUsers.get(i).id == mCurrentUser.id) {
                mSearchUsers.get(i).isFollow = true;
            }
        }
        mSearchUserAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFollowFail() {

    }

    @Override
    public void onCancleFollowSuccess() {
        for(int i = 0;  i < mSearchUsers.size();i++) {
            if (mSearchUsers.get(i).id == mCurrentUser.id) {
                mSearchUsers.get(i).isFollow = false;
            }
        }
        mSearchUserAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCancleFollowFail() {

    }

    @Override
    public void onGetFollowListSuccess(MsResponse response, int offset) {

    }

    @Override
    public void onGetFollowListFail() {

    }

    private OnClickListener mUserListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            FocusUserInfo userInfo = (FocusUserInfo) v.getTag();
            switch (v.getId()) {
                case R.id.rl_user_info:
                    Intent intent = new Intent(SearchLivingActivity.this, NewProfileActivity.class);
                    UserInfo mUserInfo = new UserInfo();
                    mUserInfo.userId = userInfo.id;
                    intent.putExtra(UserInfo.INTENT_EXTRA_INFO, mUserInfo);
                    startActivity(intent);
                    break;
                case R.id.tv_attention_count:
                    mCurrentUser = userInfo;
                    if (userInfo.isFollow) {
                        mFollowUserManager.cancleFollow(userInfo.id);
                    } else {
                        mFollowUserManager.follow(userInfo.id);
                    }
                    break;
            }

        }
    };


    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        switch (mSearchType) {
            case SEARCH_TYPE_USER:
                new SearchUserTask(mSearchText, 0).executeLong();
                break;
            case SEARCH_TYPE_SCHOOL:
                new SearchSchoolTask(mSearchText, 0).executeLong();
                break;
            case SEARCH_TYPE_TOPIC:
                new SearchTopicTask(mSearchText, 0).executeLong();
                break;
            default:
                break;
        }
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        switch (mSearchType) {
            case SEARCH_TYPE_USER:
                new SearchUserTask(mSearchText, mSearchUserAdapter.getCount()).executeLong();
                break;
            case SEARCH_TYPE_SCHOOL:
                new SearchSchoolTask(mSearchText, mSearchSchoolAdapter.getCount()).executeLong();
                break;
            case SEARCH_TYPE_TOPIC:
                new SearchTopicTask(mSearchText, mSearchTopicAdapter.getCount()).executeLong();
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
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, DELAY_MILLS);
            mPtrSearch.setVisibility(View.VISIBLE);
        } else {
            mPtrSearch.setVisibility(View.GONE);
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
            return HttpUtil.msRequest(getApplicationContext(), MsRequest.FIND_USER_BY_NICK,
                    "offset=" + mOffset + "&nick=" + Utils.urlEncode(mKey));
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            Utils.hidePgressDialog();
            mPtrSearch.onRefreshComplete();
            Context ctx = getApplicationContext();
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                if (mOffset <= 0) {
                    mSearchUsers.clear();
                }
                ArrayList<FocusUserInfo> mUsers = JsonUtil.getArray(ja, FocusUserInfo.TRANSFORMER);
                mSearchUsers.addAll(mUsers);
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
            case SEARCH_TYPE_USER:
                mSearchType = SEARCH_TYPE_USER;
                if (searchText != null && !searchText.equals("")) {
                    mPtrSearch.setAdapter(mSearchUserAdapter);
                    new SearchUserTask(searchText, 0).executeLong();
                    Utils.showProgressDialog(SearchLivingActivity.this, R.string.forum_search_ing);
                }
                mTtSchool.setChosen(false);
                mTtTopic.setChosen(false);
                break;
            case SEARCH_TYPE_SCHOOL:
                mSearchType = SEARCH_TYPE_SCHOOL;
                if (searchText != null && !searchText.equals("")) {
                    mPtrSearch.setAdapter(mSearchSchoolAdapter);
                    new SearchSchoolTask(mSearchText, 0).executeLong();
                    Utils.showProgressDialog(SearchLivingActivity.this, R.string.forum_search_ing);
                }
                mTtUser.setChosen(false);
                mTtTopic.setChosen(false);
                break;
            case SEARCH_TYPE_TOPIC:
                mSearchType = SEARCH_TYPE_TOPIC;
                if (searchText != null && !searchText.equals("")) {
                    mPtrSearch.setAdapter(mSearchTopicAdapter);
                    new SearchTopicTask(mSearchText, 0).executeLong();
                    Utils.showProgressDialog(SearchLivingActivity.this, R.string.forum_search_ing);
                }
                mTtSchool.setChosen(false);
                mTtUser.setChosen(false);
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
            mIvIslock.setVisibility(View.GONE);
            mTvSchoolName.setText(mSearchSchool.name);
            view.setTag(mSearchSchool);
            view.setOnClickListener(SearchLivingActivity.this);
            return view;
        }

    };

    private BaseAdapter mSearchTopicAdapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return mSearchTopics.size();
        }

        @Override
        public Object getItem(int position) {
            return mSearchTopics.get(position);
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
            LiveTopic mSearchTopic = mSearchTopics.get(position);
            TextView mTvSchoolName = (TextView) view.findViewById(R.id.tv_school_name);
            ImageView mIvIslock = (ImageView) view.findViewById(R.id.iv_is_lock);
            mIvIslock.setVisibility(View.GONE);
            mTvSchoolName.setText(mSearchTopic.name);
            view.setTag(mSearchTopic);
            view.setOnClickListener(SearchLivingActivity.this);
            return view;
        }

    };

    public class SearchSchoolTask extends MsTask {
        private String mName;
        private int mOffset;

        public SearchSchoolTask(String name, int offset) {
            super(SearchLivingActivity.this, MsRequest.SCHOOL_SEARCH );
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
            mPtrSearch.onRefreshComplete();
            if (response.isSuccessful()) {
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

    private class SearchTopicTask extends MsTask {

        private String mName;
        private int mOffset;

        public SearchTopicTask(String name, int offset) {
            super(SearchLivingActivity.this, MsRequest.GET_TOPIC_LIST );
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
            mPtrSearch.onRefreshComplete();
            if (response.isSuccessful()) {
                if (mOffset <= 0) {
                    mSearchTopics.clear();
                }
                JSONArray array = response.getJsonArray();
                mSearchTopics.addAll(JsonUtil.getArray(array, LiveTopic.TRANSFORMER));
                mSearchTopicAdapter.notifyDataSetChanged();
            }
            mSearchText = mName;
        }
    }

    @Override
    protected void onDestroy() {
        mFollowUserManager.unregisterOnUserFollowListener(this);
        super.onDestroy();
    }
}
