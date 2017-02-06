package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.nova.VoiceView;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.CfReply;
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

public class ForumMyPostActivity extends BaseActivity implements OnRefreshListener2<ListView>,
        OnClickListener, MsTaskListener, DialogInterface.OnClickListener {

    public static final String EXT_SHOW_SUBMENU = "ext_show_submenu";
    private static final String EXT_POSTS_STRING = "ext_posts_string"; 

    private ArrayList<VoiceView> mVoiceViews;

    private PullToRefreshListView mPtrListView;
    private TribePostAdapter mPostAdapter;
    private CfPost mCurrentPost, mLastPost;
    private MsTaskManager mTaskManager;
    private SnsHelper mSnsHelper;
    private boolean mClickble = true;
    private LightDialog mMenuDialog;
    private CfReply mCurrentReply;
    private long mCurrentMills;
    private boolean mIsSamePost;
    private SharedPreferences mPreferences;
    
    @Override
    protected int getLayoutResID() {
        return R.layout.forum_my_post_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVoiceViews = new ArrayList<>();
        mPreferences = DataHelper.getSpForData(this); 
        getTitleBar().setTitle(getString(R.string.cf_post_mine));
        mTaskManager = MsTaskManager.getInstance(this);
        mTaskManager.registerListener(this);
        mSnsHelper = SnsHelper.getInstance();
        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptrlv_post_stream);
        mPtrListView.setMode(Mode.BOTH);
        mPtrListView.setOnRefreshListener(this);

        mPostAdapter = new TribePostAdapter(this);
        mPostAdapter.setActivity(this);
        mPostAdapter.showOtherSchool();
        mPostAdapter.setIsShowTribeIndetail(true);
        mPostAdapter.setTextClickble(true);
        mPostAdapter.setSpanClickble(true);
        mPostAdapter.setShowNoContent(true);
        mPtrListView.setAdapter(mPostAdapter);
        loadData();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mClickble = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTaskManager.unregisterListener(this);
        for (VoiceView voiceView : mVoiceViews) {
            voiceView.onDestroy();
        }
    }

    private void fetchPosts(boolean refresh) {
        new ListMyPostTask(refresh).executeLong();
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchPosts(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchPosts(false);
    }


    private class ListMyPostTask extends MsTask {
        private int mOffset;
        private boolean refresh;

        public ListMyPostTask(boolean refresh) {
            super(ForumMyPostActivity.this, MsRequest.CF_LIST_MY_POSTS);
            this.refresh = refresh;
            mOffset = refresh ? 0 : mPostAdapter.getCount();

        }

        @Override
        protected void onPreExecute() {
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
                String json = ja.toString();
                ArrayList<CfPost> posts = JsonUtil.getArray(ja, CfPost.TRANSFORMER);
                if (refresh) {
                    saveMyPosts(json);
                    mPostAdapter.reset(posts);
                } else {
                    mPostAdapter.addAll(posts);         
                }
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.av_avatar:
            case R.id.tv_name:
            	if (mClickble) {
            		mClickble = false;
            		showProfileActivity(((CfPost) (v.getTag())).userInfo);
            	}
                break;
            case R.id.ll_like_normal:
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

            case R.id.ll_dislike_normal:
                mLastPost = mCurrentPost;
                mCurrentMills = System.currentTimeMillis();
                mCurrentPost = (CfPost) v.getTag();
                if (mLastPost == null) {
                    mIsSamePost = true;
                } else {
                    mIsSamePost = mCurrentPost.postId == mLastPost.postId;
                }
                System.out.println("--- >hate -- " + mCurrentPost.postId + " -- " + mIsSamePost + mCurrentPost.upCount+"--"+mCurrentPost.downCount);
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
            case R.id.ll_channel_post_comment:
                mCurrentReply = (CfReply) v.getTag();
                mCurrentPost = mCurrentReply.targetPost;
                showMenuDialog();
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

    private void showMenuDialog() {
        if (mMenuDialog == null) {
            mMenuDialog = new LightDialog(this).setItems(R.array.myreply_item_menu, this);
        }
        mMenuDialog.show();
    }


    private void showProfileActivity (UserInfo mUserInfo) {
    	Intent intent = new Intent(ForumMyPostActivity.this, NewProfileActivity.class);
        intent.putExtra(UserInfo.INTENT_EXTRA_INFO, mUserInfo);
        startActivity(intent);
    }

    private void showForumPostDetail() {
        Intent cpdIntent = new Intent(this, ForumPostDetailActivity.class);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA, mCurrentPost);
        startActivity(cpdIntent);
    }

    private void share() {
        mSnsHelper.openShareBoard(this, mCurrentPost);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSnsHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPreExecute(MsTaskType type) {
        getTitleBar().showProgress();
    }

    @Override
    public void onPostExecute(MsTaskType type, MsResponse response) {
        getTitleBar().hideProgress();
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

    @Override
    public void onClick(DialogInterface dialog, int which) {
         switch (which) {
            case 0:
                showResource();
                break;
            case 1:
                delete();
                break;
            default:
                break;
        }
    }

    private void showResource() {
        if (mCurrentPost == null) {
            toast("该帖子已经被删除");
        } else {
        	showPostDetail();
        }
    }

    private void showPostDetail() {
        Intent intent = new Intent(this, ForumPostDetailActivity.class);
        intent.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA, mCurrentPost);
        startActivity(intent);
    }

    private void delete() {
        mTaskManager.startForumDeleteTask(mCurrentReply);
    }
    
    private void saveMyPosts(String posts) {
        Editor editor = mPreferences.edit();
        editor.putString(EXT_POSTS_STRING, posts);
        editor.commit();
    }
    
    private void loadData() {
        ArrayList<CfPost> mPosts = new ArrayList<CfPost>();
        String json = mPreferences.getString(EXT_POSTS_STRING, "[]");
        try {
            JSONArray ja = new JSONArray(json);
            mPosts = JsonUtil.getArray(ja, CfPost.TRANSFORMER);
            if (mPosts != null && mPosts.size() > 0) {
                mPostAdapter.reset(mPosts);
                fetchPosts(true);
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mPtrListView.setRefreshing(Mode.PULL_FROM_START);
    }

}
