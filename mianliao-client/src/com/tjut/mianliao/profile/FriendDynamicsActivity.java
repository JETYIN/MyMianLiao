package com.tjut.mianliao.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.FocusTribe;
import com.tjut.mianliao.data.FocusUser;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.nova.ForumPostAdapter;
import com.tjut.mianliao.forum.nova.ForumPostAdapter.NoContentClickListener;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

/**
 * Show friends dynamic, and it contains some recommend information, just like user or tribe
 *
 * @author YoopWu
 */
public class FriendDynamicsActivity extends BaseActivity implements OnRefreshListener2<ListView>,
        NoContentClickListener {

    private static final String SP_GET_FRIENDS_DYNC_TIME = "sp_get_friends_dync_time";
    private static final String SP_FRIEND_DYNC_POSTS = "sp_friend_dync_posts";

    @ViewInject(R.id.ptr_friend_dyncmic)
    private PullToRefreshListView mPtrFriendsDync;

    private ForumPostAdapter mAdapter;

    private SharedPreferences mPreferences;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_friends_dyncmic;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        getTitleBar().showTitleText(R.string.post_friend_dyna, null);
        mPreferences = DataHelper.getSpForData(this);
        mPtrFriendsDync.setMode(Mode.BOTH);
        mPtrFriendsDync.setOnRefreshListener(this);
        mAdapter = new ForumPostAdapter(this);
        mAdapter.setActivity(this);
        mAdapter.showOtherSchool();
        mAdapter.setTextClickble(true);
        mAdapter.setSpanClickble(true);
        mAdapter.setOnNoContentListener(this);
        mPtrFriendsDync.setAdapter(mAdapter);
        loadData();
    }

    private void loadData() {
        String jsonA = mPreferences.getString(SP_FRIEND_DYNC_POSTS, "[]");
        try {
            JSONArray jsonArray = new JSONArray(jsonA);
            ArrayList<CfPost> posts = JsonUtil.getArray(jsonArray, CfPost.TRANSFORMER);
            if (posts != null && posts.size() > 0) {
                mAdapter.reset(posts);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mPtrFriendsDync.setRefreshing(Mode.PULL_FROM_START);
        new GetFocusContent().executeLong();
    }

    private boolean isDayFirst() {
        long oldTime = mPreferences.getLong(SP_GET_FRIENDS_DYNC_TIME, 0);
        long nowTime = System.currentTimeMillis();
        return Utils.isSameDay(oldTime, nowTime);
    }

    private void saveFriendDyncTime() {
        mPreferences.edit().putLong(SP_GET_FRIENDS_DYNC_TIME, System.currentTimeMillis()).commit();
    }

    private void savePostInfo(String json) {
        mPreferences.edit().putString(SP_FRIEND_DYNC_POSTS, json).commit();
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        new GetFriendsDyncTask(0).executeLong();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        new GetFriendsDyncTask(mAdapter.getPostCount()).executeLong();
    }

    private class GetFriendsDyncTask extends MsTask {

        private int mOffset;
        private boolean mRefresh;

        public GetFriendsDyncTask(int offset) {
            super(FriendDynamicsActivity.this, MsRequest.LIST_USER_BEHAVIOR);
            mOffset = offset;
            mRefresh = offset == 0;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset)
                    .append("&cookie=").append(isDayFirst() ? 1 : 0)
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrFriendsDync.onRefreshComplete();
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                ArrayList<CfPost> posts = JsonUtil.getArray(ja, CfPost.TRANSFORMER);
                if (mRefresh) {
                    mAdapter.reset(posts);
                    saveFriendDyncTime();
                    savePostInfo(ja.toString());
                } else {
                    mAdapter.addAll(posts);
                }

            }
        }

    }

    private class GetFocusContent extends MsTask {
        public GetFocusContent () {
            super(FriendDynamicsActivity.this, MsRequest.LIST_RECOMMEND_USER_TRIBE);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONObject json = response.getJsonObject();
                ArrayList<FocusUser> focusUsers = JsonUtil.getArray(
                        json.optJSONArray("follow_user"), FocusUser.TRANSFORMER);
                ArrayList<FocusTribe> tribeInfos = JsonUtil.getArray(
                        json.optJSONArray("follow_tribe"), FocusTribe.TRANSFORMER);
                mAdapter.addRecommedTribes(getRecommendTribeInfo(tribeInfos));
                mAdapter.addRecommedUsers(getRecommendUserInfo(focusUsers));
            }
        }
    }

    public ArrayList<Map<Integer, FocusUser>> getRecommendUserInfo(ArrayList<FocusUser> focusUsers) {
        if (focusUsers == null) {
            return null;
        }
        ArrayList<Map<Integer, FocusUser>> recUsers = new ArrayList<>();
        for (int i = 0; i < focusUsers.size(); i++) {
            Map<Integer, FocusUser> userMap = new HashMap<>();
            userMap.put(2 * i + 1, focusUsers.get(i));
            recUsers.add(userMap);
        }
        return recUsers;
    }

    public ArrayList<Map<Integer, FocusTribe>> getRecommendTribeInfo(ArrayList<FocusTribe> tribeInfos) {
        if (tribeInfos == null) {
            return null;
        }
        ArrayList<Map<Integer, FocusTribe>> recTribes = new ArrayList<>();
        for (int i = 0; i < tribeInfos.size(); i++) {
            Map<Integer, FocusTribe> tribeMap = new HashMap<>();
            tribeMap.put(2 * i + 1, tribeInfos.get(i));
            recTribes.add(tribeMap);
        }
        return recTribes;
    }

    @Override
    public void onNoContentClick() {
        new GetFriendsDyncTask(0).executeLong();
        new GetFocusContent().executeLong();
    }
}
