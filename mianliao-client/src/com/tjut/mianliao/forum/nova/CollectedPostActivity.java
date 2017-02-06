package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.nova.ForumPostAdapter.NoContentClickListener;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.SnsHelper;
import com.umeng.analytics.MobclickAgent;

public class CollectedPostActivity extends BaseActivity implements OnClickListener,
        OnRefreshListener2<ListView>, MsTaskListener, NoContentClickListener {
    
    private static final String SP_COLLECTED_POST_CACHE = "sp_collected_post_cache";
    
    private MsTaskManager mTaskManager;
    private SnsHelper mSnsHelper;
    private SharedPreferences mPreferences;

    private PullToRefreshListView mPtrListView;
    private TribePostAdapter mPostAdapter;
    private CfPost mCurrentPost, mLastPost;
    
    private long mCurrentMills;
    private boolean mIsSamePost;
    
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_user_post;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(R.string.news_category_fav);
        mTaskManager = MsTaskManager.getInstance(this);
        mTaskManager.registerListener(this);
        mPreferences = DataHelper.getSpForData(this);
        mSnsHelper = SnsHelper.getInstance();
        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptrlv_user_posts);
        mPtrListView.setMode(Mode.BOTH);
        mPtrListView.setOnRefreshListener(this);
        mPostAdapter = new TribePostAdapter(this);
        mPostAdapter.setActivity(this);
        mPostAdapter.showOtherSchool();
        mPostAdapter.setIsShowTribeIndetail(true);
        mPostAdapter.setTextClickble(true);
        mPostAdapter.setSpanClickble(true);
        mPostAdapter.setOnNoContentListener(this);
        mPtrListView.setAdapter(mPostAdapter);
        loadData();
    }

    private void loadData() {
        String cacheJson = mPreferences.getString(SP_COLLECTED_POST_CACHE, "[]");
        try {
            JSONArray array = new JSONArray(cacheJson);
            ArrayList<CfPost> posts = JsonUtil.getArray(array, CfPost.TRANSFORMER);
            if (posts != null && posts.size() > 0) {
                mPostAdapter.reset(posts);
                fetchPost(true);
            } else {
                mPtrListView.setRefreshing(Mode.PULL_FROM_START);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        mPostAdapter.stopVoicePlay();
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTaskManager.unregisterListener(this);
        mPostAdapter.destroy();
    }

    private void fetchPost(boolean refresh) {
        new MyCollectedPostTask(refresh).executeLong();
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.av_avatar:
            case R.id.tv_name:
                showProfileActivity(((CfPost) (v.getTag())).userInfo);
                break;
            case R.id.ll_like_active:
            case R.id.ll_like_normal:
            case R.id.ll_like_night:
                mLastPost = mCurrentPost;
                mCurrentMills = System.currentTimeMillis();
                mCurrentPost = (CfPost) v.getTag();
                if (mLastPost == null) {
                    mIsSamePost = true;
                } else {
                    mIsSamePost = mCurrentPost.postId == mLastPost.postId;
                }
                if (mCurrentPost.myUp) {
                    return;
                }
                if (mCurrentPost.myDown) {
                    mCurrentPost.downCount--;
                }
                mCurrentPost.upCount++;
                mCurrentPost.myDown = false;
                mCurrentPost.myUp = true;
                updateClickedUI(mCurrentPost, v, true);
                MobclickAgent.onEvent(this, MStaticInterface.LIKE);
                break;

            case R.id.ll_dislike_active:
            case R.id.ll_dislike_normal:
            case R.id.ll_dislike_night:
                mLastPost = mCurrentPost;
                mCurrentMills = System.currentTimeMillis();
                mCurrentPost = (CfPost) v.getTag();
                if (mLastPost == null) {
                    mIsSamePost = true;
                } else {
                    mIsSamePost = mCurrentPost.postId == mLastPost.postId;
                }
                if (mCurrentPost.myDown) {
                    return;
                }
                if (mCurrentPost.myUp) {
                    mCurrentPost.upCount--;
                }
                mCurrentPost.downCount++;
                mCurrentPost.myUp = false;
                mCurrentPost.myDown = true;
                updateClickedUI(mCurrentPost, v, false);
                MobclickAgent.onEvent(this, MStaticInterface.UNLIKE);
                break;
            case R.id.ll_share:
            case R.id.ll_share_night:
                share();
                break;
            case R.id.ll_comment_normal:
            case R.id.ll_comment_active:
            case R.id.ll_comment_vote:
            case R.id.ll_channel_post:
                mCurrentPost = (CfPost) v.getTag();
                showForumPostDetail();
                break;
            default:
                break;
        }
    }
    

    public void updateClickedUI(final CfPost post, final View view, final boolean like) {
        update(mCurrentPost);
        if (!mIsSamePost && mLastPost != null) {
            if (like) {
                mTaskManager.startForumLikeTask(mLastPost);
            } else {
                mTaskManager.startForumHateTask(mLastPost);
            }
        }
        view.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (System.currentTimeMillis() - mCurrentMills > 1000) {
                    if (like) {
                        mTaskManager.startForumLikeTask(post);
                    } else {
                        mTaskManager.startForumHateTask(post);
                    }
                }
            }
        }, 1000);
    }
    
    private void showProfileActivity(UserInfo userInfo) {
        Intent intent = new Intent(this, NewProfileActivity.class);
        intent.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
        startActivity(intent);
    }

    private void share() {
        mSnsHelper.openShareBoard(this, mCurrentPost);
    }

    private void showForumPostDetail() {
        Intent cpdIntent = new Intent(this, ForumPostDetailActivity.class);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA, mCurrentPost);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_CHANNEL_INFO, new ChannelInfo());
        startActivity(cpdIntent);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchPost(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchPost(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSnsHelper.onActivityResult(requestCode, resultCode, data);
    }

    private class MyCollectedPostTask extends MsTask {
        private int mOffset;
        private boolean refresh;

        public MyCollectedPostTask(boolean refresh) {
            super(CollectedPostActivity.this, MsRequest.CF_COLLECT_THREAD_LISTS);
            this.refresh = refresh;
            mOffset = refresh ? 0 : mPostAdapter.getCount();
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrListView.onRefreshComplete();
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                ArrayList<CfPost> posts = JsonUtil.getArray(ja, CfPost.TRANSFORMER);
                if (refresh) {
                    mPostAdapter.reset(posts);
                    saveDataToSp(ja.toString());
                } else {
                    mPostAdapter.addAll(posts);
                }
            }
        }
    }

    private void saveDataToSp(String json) {
        SharedPreferences.Editor eidtor = mPreferences.edit();
        eidtor.putString(SP_COLLECTED_POST_CACHE, json).commit();
    }

    @Override
    public void onPostExecute(MsTaskType type, MsResponse response) {
        switch (type) {
            case FORUM_DELETE_POST:
                if (response.value instanceof CfPost) {
                    remove((CfPost) response.value);
                }
                break;
            case FORUM_LIKE_POST:
            case FORUM_HATE_POST:
            case FORUM_COMMENT_POST:
                if (response.value instanceof CfPost) {
                    update((CfPost) response.value);
                }
                break;
            case FORUM_COLLECT_POST:
                if (response.value instanceof CfPost) {
                    CfPost post = (CfPost) response.value;
                    if (!post.collected) {
                        remove(post);
                    }
                }
                break;
            default:
                break;
        }
    }


    public void add(int index, CfPost post) {
        mPostAdapter.add(index, post);
    }
    
    public void remove(CfPost post) {
        mPostAdapter.remove(post);
    }

    public void update(CfPost post) {
       mPostAdapter.update(post);
    }

    @Override
    public void onPreExecute(MsTaskType type) {
        
    }

    @Override
    public void onNoContentClick() {
        fetchPost(true);
    }
}
