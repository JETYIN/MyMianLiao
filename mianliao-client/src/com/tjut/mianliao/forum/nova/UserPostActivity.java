package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;

import org.json.JSONArray;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.R;
import com.tjut.mianliao.contact.UserEntryManager;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.SnsHelper;
import com.tjut.mianliao.xmpp.ConnectionManager;
import com.umeng.analytics.MobclickAgent;

public class UserPostActivity extends BaseActivity implements OnClickListener,
        OnRefreshListener2<ListView>, MsTaskListener {

    public static final String EXT_USER_INFO = "ext_user_info";
    
    private MsTaskManager mTaskManager;
    private SnsHelper mSnsHelper;
    private UserInfo mUserInfo;

    private PullToRefreshListView mPtrListView;
    private TribePostAdapter mPostAdapter;
    private CfPost mCurrentPost, mLastPost;

    private View mViewNoContent;
    private FrameLayout mViewParent;
    
    private long mCurrentMills;
    private boolean mIsSamePost;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_user_post;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserInfo = getIntent().getParcelableExtra(EXT_USER_INFO);
        if (mUserInfo == null) {
            finish();
            return;
        }
        getTitleBar().setTitle(getString(R.string.prof_ones_post, mUserInfo.getDisplayName(this)));
        mTaskManager = MsTaskManager.getInstance(this);
        mTaskManager.registerListener(this);
        mViewNoContent = mInflater.inflate(R.layout.view_no_content, null);
        mViewParent = (FrameLayout) findViewById(R.id.view_parent);
        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptrlv_user_posts);
        mPtrListView.setMode(Mode.BOTH);
        mPtrListView.setOnRefreshListener(this);
        mPostAdapter = new TribePostAdapter(this);
        mPostAdapter.setIsShowTribeIndetail(true);
        mPostAdapter.setActivity(this);
        mPostAdapter.showOtherSchool();
        mPostAdapter.setTextClickble(true);
        mPostAdapter.setSpanClickble(true);
        mPostAdapter.setShowNoContent(false);
        mPtrListView.setAdapter(mPostAdapter);
        fetchPost(true);
        mViewNoContent.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                reload();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPostAdapter.stopVoicePlay();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTaskManager != null) {
            mTaskManager.unregisterListener(this);
        }
        if (mPostAdapter != null) {
            mPostAdapter.destroy();
        }
    }

    private void fetchPost(boolean refresh) {
        new ListUserPostTask(refresh, 20).executeLong();
    }
    

    private void hideNoMessage() {
        if (mViewParent != null && mViewNoContent != null) {
            mViewParent.removeView(mViewNoContent);
        }
    }
    
    private void showNoMessage() {
        if (mViewParent != null && mViewNoContent != null) {
            mViewParent.removeView(mViewNoContent);
            resetNoContentView();
            mViewParent.addView(mViewNoContent);
        }
    }
    
    private void resetNoContentView() {
        mViewNoContent.findViewById(R.id.iv_notice).setVisibility(View.VISIBLE);
        mViewNoContent.findViewById(R.id.pb_progress).setVisibility(View.GONE);
        
    }
    
    private void reload() {
        mViewNoContent.findViewById(R.id.iv_notice).setVisibility(View.INVISIBLE);
        mViewNoContent.findViewById(R.id.pb_progress).setVisibility(View.VISIBLE);
        fetchPost(true);
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

    private class ListUserPostTask extends MsTask {
        private int mOffset;
        private int mLimit;
        private boolean refresh;

        public ListUserPostTask(boolean refresh, int limit) {
            super(UserPostActivity.this, MsRequest.CF_LIST_USER_POSTS);
            this.refresh = refresh;
            mLimit = limit;
            mOffset = refresh ? 0 : mPostAdapter.getCount();

        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset)
                    .append("&user_id=").append(mUserInfo.userId)
                    .append("&limit=").append(mLimit)
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrListView.onRefreshComplete();
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                ArrayList<CfPost> posts = JsonUtil.getArray(ja, CfPost.TRANSFORMER);
                if (refresh) {
                    mPostAdapter.reset(posts);
                    if (posts != null && posts.size() > 0) {
                        hideNoMessage();
                    } else {
                        showNoMessage();
                    }
                } else {
                    mPostAdapter.addAll(posts);
                }
            } else {
                showNoMessage();
            }
        }
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
}
