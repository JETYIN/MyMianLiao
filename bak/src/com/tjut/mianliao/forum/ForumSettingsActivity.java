package com.tjut.mianliao.forum;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.tab.Tab;
import com.tjut.mianliao.component.tab.TabController;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class ForumSettingsActivity extends BaseActivity
        implements TabController.TabListener, ViewPager.OnPageChangeListener, View.OnClickListener {

    private static final String SP_FORUM_LIST   = "forum_list";
    private static final String SP_FORUM_FILTER = "forum_filter";

    private static final int REQUEST_VIEW        = 101;
    private static final int REQUEST_ADD_PUBLIC  = 102;
    private static final int REQUEST_ADD_SCHOOL  = 103;

    private int mOldCategory;
    private int mNewCategory;
    private View mStreamFilter;
    private LightDialog mFilterDialog;

    private ViewPager mViewPager;
    private TabController mTabController;

    private ArrayList<Forum> mSchoolForums = new ArrayList<Forum>();
    private ArrayList<Forum> mPublicForums = new ArrayList<Forum>();
    private Forum mAddMoreSchool;
    private Forum mAddMorePublic;
    private SparseBooleanArray mDirtyItems = new SparseBooleanArray();

    private Forum mPromSchoolForum;
    private boolean mIsPromSchoolListened;
    private Forum mPromPublicForum;
    private boolean mIsPromPublicListened;

    private PullToRefreshListView mSchoolForumList;
    private PullToRefreshListView mPublicForumList;

    private ForumAdapter mSchoolAdapter;
    private ForumAdapter mPublicAdapter;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_forum_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAddMoreSchool = new Forum(Forum.TYPE_SCHOOL_USER, getString(R.string.cf_add_more_school));
        mAddMorePublic = new Forum(Forum.TYPE_SQUARE_USER, getString(R.string.cf_add_more_public));

        mViewPager = (ViewPager) findViewById(R.id.vp_forum_pager);
        mViewPager.setAdapter(new TabPagerAdapter());
        mViewPager.setOnPageChangeListener(this);

        mTabController = new TabController();
        getTitleBar().showTabs(mTabController,
                getString(R.string.cf_school_section), getString(R.string.cf_public_section));
        mTabController.setListener(this);
        mTabController.select(0);
        getTitleBar().showRightText(R.string.fs_my_posts, this);

        loadLocalData();

        mSchoolAdapter = new ForumAdapter(this, mSchoolForums);
        mSchoolForumList = new PullToRefreshListView(this);
        mStreamFilter = LayoutInflater.from(this).inflate(R.layout.forum_stream_filter, null, false);
        mSchoolForumList.getRefreshableView().addHeaderView(mStreamFilter);
        initForumList(mSchoolForumList, mSchoolAdapter);

        mPublicAdapter = new ForumAdapter(this, mPublicForums);
        mPublicForumList = new PullToRefreshListView(this);
        initForumList(mPublicForumList, mPublicAdapter);

        updateFilterView();

        new LoadSettingsTask().executeLong();
    }

    private void loadLocalData() {
        try {
            SharedPreferences sp = DataHelper.getSpForData(this);
            loadForum(new JSONArray(sp.getString(SP_FORUM_LIST, "[]")), true);
            mOldCategory = sp.getInt(SP_FORUM_FILTER, 0);
            mNewCategory = mOldCategory;
        } catch (JSONException e) {
            // Do nothing
        }
    }

    private void updateFilterView() {
        mStreamFilter.findViewById(R.id.tv_today_posts).setVisibility(View.GONE);
        ((ProImageView) mStreamFilter.findViewById(R.id.iv_forum_icon)).setImageResource(R.drawable.icon_stream);
        ((TextView) mStreamFilter.findViewById(R.id.tv_forum_name)).setText(R.string.fs_forum_stream_label);

        mStreamFilter.findViewById(R.id.rl_forum_info).setTag(Forum.DEFAULT_FORUM);

        final TextView spinner = (TextView) mStreamFilter.findViewById(R.id.category_spinner);
        spinner.setVisibility(View.VISIBLE);
        spinner.setText(getFilterNameByPosition(mNewCategory));
        spinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFilterDialog == null) {
                    mFilterDialog = new LightDialog(ForumSettingsActivity.this)
                            .setTitleLd(R.string.please_choose)
                            .setItems(R.array.forum_stream_filter,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mNewCategory = which;
                                            spinner.setText(getFilterNameByPosition(mNewCategory));
                                        }
                                    });
                }
                mFilterDialog.show();
            }
        });

    }

    private String getFilterNameByPosition(int pos) {
        String[] array = getResources().getStringArray(R.array.forum_stream_filter);
        return array[pos];
    }

    private void initForumList(PullToRefreshListView list, ForumAdapter adapter) {
        list.setAdapter(adapter);
        list.setMode(PullToRefreshBase.Mode.DISABLED);
        list.getRefreshableView().setDivider(null);
    }

    @Override
    protected void onStop() {
        super.onStop();

        localSaveForumList();
        uploadSettings();
    }

    private void uploadSettings() {
        for (int i = 0; i < mDirtyItems.size(); i++) {
            new SaveListenStatusTask(
                    mDirtyItems.keyAt(i), mDirtyItems.valueAt(i)).executeLong();
        }

        if (mOldCategory != mNewCategory
                || (mPromPublicForum != null && mIsPromPublicListened != mPromPublicForum.isListening)
                || (mPromSchoolForum != null && mIsPromSchoolListened != mPromSchoolForum.isListening)) {
            new SaveSettingsTask().executeLong();
        }
    }

    private boolean removeForum(Forum forum) {
        return forum != null && forum.id != 0 && getTargetForumList(forum).remove(forum);
    }

    private ArrayList<Forum> getTargetForumList(Forum forum) {
        if (forum.isPublic()) {
            return mPublicForums;
        } else {
            return mSchoolForums;
        }
    }

    private ForumAdapter getTargetForumAdapter(Forum forum) {
        if (forum.isPublic()) {
            return mPublicAdapter;
        } else {
            return mSchoolAdapter;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        Forum forum = data.getParcelableExtra(Forum.INTENT_EXTRA_NAME);
        switch (requestCode) {
            case REQUEST_VIEW:
                if (resultCode == BaseActivity.RESULT_UPDATED && updateForum(forum, false)
                        || resultCode == BaseActivity.RESULT_DELETED && removeForum(forum)) {
                    getTargetForumAdapter(forum).notifyDataSetChanged();
                }
                break;
            case REQUEST_ADD_SCHOOL:
                if (resultCode == BaseActivity.RESULT_UPDATED) {
                    boolean changed = false;
                    ArrayList<Forum> forumsDeleted = data.getParcelableArrayListExtra(
                            Forum.INTENT_EXTRA_DELETED);
                    if (forumsDeleted != null && !forumsDeleted.isEmpty()) {
                        changed = mSchoolForums.removeAll(forumsDeleted);
                    }
                    changed |= updateForum(forum, true);

                    if (changed) {
                        mSchoolAdapter.notifyDataSetChanged();
                    }
                }
                break;
            case REQUEST_ADD_PUBLIC:
                if (resultCode == BaseActivity.RESULT_UPDATED) {
                    boolean changed = false;
                    ArrayList<Forum> forumsDeleted = data.getParcelableArrayListExtra(
                            Forum.INTENT_EXTRA_DELETED);
                    if (forumsDeleted != null && !forumsDeleted.isEmpty()) {
                        changed = mPublicForums.removeAll(forumsDeleted);
                    }
                    changed |= updateForum(forum, true);

                    if (changed) {
                        mPublicAdapter.notifyDataSetChanged();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void updateListeningStatusIfNeeded(Forum forum) {
        if (forum.isProm()) {
            if (forum.isPublic()) {
                forum.isListening = mIsPromPublicListened;
            } else {
                forum.isListening = mIsPromSchoolListened;
            }
        }
    }

    private boolean updateForum(Forum forum, boolean appendIfNotFound) {
        if (forum == null || forum.id == 0) {
            return false;
        }

        updateListeningStatusIfNeeded(forum);
        ArrayList<Forum> forums = getTargetForumList(forum);
        int index = forums.indexOf(forum);
        if (index != -1) {
            forums.remove(index);
            forums.add(index, forum);
            return true;
        } else if (appendIfNotFound) {
            forum.isListening = true;
            forums.add(forums.size() - 1, forum);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.rl_forum_info:
                Forum forum = (Forum) v.getTag();
                if (forum == mAddMoreSchool) {
                    Intent i = new Intent(ForumSettingsActivity.this, ForumSearchActivity.class);
                    i.putExtra(Forum.INTENT_EXTRA_NAME, forum);
                    startActivityForResult(i, REQUEST_ADD_SCHOOL);
                } else if (forum == mAddMorePublic) {
                    Intent i = new Intent(ForumSettingsActivity.this, ForumSearchActivity.class);
                    i.putExtra(Forum.INTENT_EXTRA_NAME, forum);
                    startActivityForResult(i, REQUEST_ADD_PUBLIC);
                } else if (forum != null) {
                    Intent i = new Intent(this, CourseForumActivity.class);
                    i.putExtra(Forum.INTENT_EXTRA_NAME, forum);
                    startActivityForResult(i, REQUEST_VIEW);
                }
                break;
            case R.id.tv_right:
//                startActivity(new Intent(this, MyPostsActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    public void onTabSelectionChanged(int index, boolean selected, Tab tab) {
        if (!selected) {
            return;
        }

        mViewPager.setCurrentItem(index);
    }

    private void loadForum(JSONArray ja, boolean fromLocal) {
        int length = ja == null ? 0 : ja.length();
        mPublicForums.clear();
        mSchoolForums.clear();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                JSONObject json = ja.optJSONObject(i);
                Forum forum = Forum.fromJson(json);
                if (forum.isPublic()) {
                    mPublicForums.add(forum);

                    if (forum.isProm()) {
                        mPromPublicForum = forum;

                        if (!fromLocal) {
                            forum.isListening = mIsPromPublicListened;
                        }
                    }
                } else {
                    mSchoolForums.add(forum);

                    if (forum.isProm()) {
                        mPromSchoolForum = forum;

                        if (!fromLocal) {
                            forum.isListening = mIsPromSchoolListened;
                        }
                    }
                }
            }

            mPublicForums.add(mAddMorePublic);
            mSchoolForums.add(mAddMoreSchool);
        }
    }

    private void localSaveForumList() {
        JSONArray ja = new JSONArray();
        for (Forum forum : mSchoolForums) {
            if (!isAddMore(forum)) {
                ja.put(forum.toJson());
            }
        }
        for (Forum forum : mPublicForums) {
            if (!isAddMore(forum)) {
                ja.put(forum.toJson());
            }
        }

        DataHelper.getSpForData(this).edit()
                .putString(SP_FORUM_LIST, ja.toString())
                .putInt(SP_FORUM_FILTER, mNewCategory)
                .commit();
    }

    private boolean isAddMore(Forum forum) {
        return mAddMoreSchool == forum || mAddMorePublic == forum;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mTabController.select(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class LoadForumsTask extends MsTask {

        public LoadForumsTask() {
            super(getApplicationContext(), MsRequest.LIST_SQUARE_FORUMS);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.request == MsRequest.LIST_SQUARE_FORUMS) {
                if (mSchoolForumList != null) {
                    mSchoolForumList.onRefreshComplete();
                }

                if (mPublicForumList != null) {
                    mPublicForumList.onRefreshComplete();
                }

                if (MsResponse.isSuccessful(response)) {
                    loadForum(response.json.optJSONArray(MsResponse.PARAM_RESPONSE), false);
                    localSaveForumList();
                    mSchoolAdapter.notifyDataSetChanged();
                    mPublicAdapter.notifyDataSetChanged();
                } else {
                    toast(MsResponse.getFailureDesc(ForumSettingsActivity.this,
                            R.string.cf_get_forum_list_failed, response.code));
                }
            }
        }
    }

    private class LoadSettingsTask extends MsTask {

        public LoadSettingsTask() {
            super(getApplicationContext(), MsRequest.FIND_MY_SETTING);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (MsResponse.isSuccessful(response)) {
                JSONObject res = response.getJsonObject();
                if (res.optInt("list_department") == 1) {
                    mOldCategory = 0;
                } else if (res.optInt("list_school") == 1) {
                    mOldCategory = 1;
                } else {
                    mOldCategory = 2;
                }
                mNewCategory = mOldCategory;

                mStreamFilter.setVisibility(View.VISIBLE);

                mIsPromSchoolListened = res.optInt("list_suggested_forum_s") == 0 ? false : true;
                mIsPromPublicListened = res.optInt("list_suggested_forum_i") == 0 ? false : true;

                updateFilterView();
                new LoadForumsTask().executeLong();
            } else {
                toast(MsResponse.getFailureDesc(ForumSettingsActivity.this,
                        R.string.fs_download_settings_failed, response.code));
            }
        }
    }

    private class SaveListenStatusTask extends MsTask {
        private int mForumId;
        private boolean mIsChecked;

        public SaveListenStatusTask(int forumId, boolean isChecked) {
            super(getApplicationContext(), MsRequest.ADD_FORUM_LISTEN);
            mForumId = forumId;
            mIsChecked = isChecked;
        }

        @Override
        protected String buildParams() {
            return "forum_id=" + mForumId + "&add=" + (mIsChecked ? 1 : 0);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (!MsResponse.isSuccessful(response)) {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.fs_save_settings_failed, response.code));
            } else {
                mDirtyItems.delete(mForumId);
            }
        }
    }

    private class SaveSettingsTask extends MsTask {
        public SaveSettingsTask() {
            super(getApplicationContext(), MsRequest.SETTING);
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder();
            if (mOldCategory != mNewCategory) {
                sb.append("list_school=" + (mNewCategory == 1 ? 1 : 0)
                        + "&list_department=" + (mNewCategory == 0 ? 1 : 0) + "&");
            }

            if (mPromPublicForum != null && mIsPromPublicListened != mPromPublicForum.isListening) {
                sb.append("list_suggested_forum_i=" + (mPromPublicForum.isListening ? 1 : 0)  + "&");
            }

            if (mPromSchoolForum != null && mIsPromSchoolListened != mPromSchoolForum.isListening) {
                sb.append("list_suggested_forum_s=" + (mPromSchoolForum.isListening ? 1 : 0));
            }

            return sb.toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (MsResponse.isSuccessful(response)) {
                mOldCategory = mNewCategory;
                if (mPromSchoolForum != null) {
                    mIsPromSchoolListened = mPromSchoolForum.isListening;
                }

                if (mPromPublicForum != null) {
                    mIsPromPublicListened = mPromPublicForum.isListening;
                }
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.cf_get_forum_list_failed, response.code));
            }
        }
    }

    private class ForumAdapter extends ArrayAdapter<Forum> {

        private final Context mContext;

        public ForumAdapter(Context context, List<Forum> list) {
            super(context, R.layout.forum_setting_item, list);
            mContext = context;
        }

        @Override
        public int getItemViewType(int position) {
            return position == getCount() - 1 ? 0 : 1;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.forum_setting_item, parent, false);
            }

            setupItem(view, getItem(position));
            return view;
        }

        private void setupItem(View view, final Forum forum) {
            ViewHolder vh = (ViewHolder) view.getTag(R.id.rl_forum_info);
            if (vh == null) {
                vh = new ViewHolder();
                vh.forumInfoLayout = view;
                vh.forumTodayPosts = (TextView) view.findViewById(R.id.tv_today_posts);
                vh.avatar = ((ProImageView) view.findViewById(R.id.iv_forum_icon));
                vh.forumName = ((TextView) view.findViewById(R.id.tv_forum_name));
                vh.toggle = (CheckBox) view.findViewById(R.id.switch_listen_forum);
                vh.badge = (ImageView) view.findViewById(R.id.iv_forum_badge);
                vh.prom = (ImageView) view.findViewById(R.id.iv_prom);
                view.setTag(R.id.rl_forum_info, vh);
            }

            vh.forumInfoLayout.setTag(forum);
            vh.forumName.setText(forum.name);

            if (isAddMore(forum)) {
                vh.avatar.setImageResource(R.drawable.ic_forum_more);
                vh.forumName.setTextColor(0xffff6b80);
                vh.forumTodayPosts.setVisibility(View.GONE);
                vh.toggle.setVisibility(View.GONE);
            } else {
                vh.avatar.setImage(forum.icon, R.drawable.ic_avatar_forum);
                vh.forumTodayPosts.setText(mContext.getString(R.string.cf_posts_today) + forum.postCountToday);
                setupToggle(vh.toggle, forum);

                if (forum.isVip()) {
                    vh.badge.setVisibility(View.VISIBLE);
                    vh.badge.setImageResource(R.drawable.ic_vip);
                } else if (forum.isUserForum()) {
                    vh.badge.setVisibility(View.VISIBLE);
                    vh.badge.setImageResource(R.drawable.ic_forum_badge);
                } else {
                    vh.badge.setVisibility(View.GONE);
                }

                vh.prom.setVisibility(forum.isProm() ? View.VISIBLE : View.GONE);
            }
        }

        private void setupToggle(final CheckBox toggle, final Forum forum) {
            toggle.setOnCheckedChangeListener(null);
            toggle.setChecked(forum.isListening);
            toggle.setText(forum.isListening ? R.string.fs_listen_on : R.string.fs_listen_off);

            toggle.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!forum.isProm()) {
                        mDirtyItems.put(forum.id, isChecked);
                    }
                    forum.isListening = isChecked;
                    toggle.setText(isChecked ? R.string.fs_listen_on : R.string.fs_listen_off);
                }
            });
        }

        private class ViewHolder {
            View forumInfoLayout;
            ProImageView avatar;
            TextView forumName;
            TextView forumTodayPosts;
            CheckBox toggle;
            ImageView badge;
            ImageView prom;
        }
    }

    private class TabPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = position == 1 ? mPublicForumList : mSchoolForumList;
            container.addView(v);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {
            container.removeView((View) view);
        }
    }
}
