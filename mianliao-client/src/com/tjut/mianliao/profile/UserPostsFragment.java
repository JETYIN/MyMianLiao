package com.tjut.mianliao.profile;

import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.lidroid.xutils.ViewUtils;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.StickyListView;
import com.tjut.mianliao.component.StickyPtrListView;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.nova.ForumPostAdapter.OnNativeScrollListener;
import com.tjut.mianliao.forum.nova.TribePostAdapter;
import com.tjut.mianliao.profile.ProfileFragment.StickyScrollCallBack;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class UserPostsFragment extends Fragment implements OnRefreshListener2<ListView>, OnNativeScrollListener{
    
    private static final String EXT_POSTS_STRING = "ext_posts_string"; 

    private UserInfo mUserInfo;
    private StickyScrollCallBack mScrollListener;
    private StickyPtrListView mPtrListView;

    private TribePostAdapter mPostAdapter;
    private SharedPreferences mPreferences;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = DataHelper.getSpForData(getActivity());
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_detail_user_posts, null);
        ViewUtils.inject(this,view);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mPtrListView = (StickyPtrListView) view.findViewById(R.id.ptr_list_view);
        TextView nullView = new TextView(getActivity());
        nullView.setHeight(ProfileFragment.sStickyTopToViewPager);
        mPtrListView.getRefreshableView().addHeaderView(nullView);
        mPtrListView.setScrollCallBack(mScrollListener);
        mPtrListView.setMode(Mode.BOTH);
        mPtrListView.setOnRefreshListener(this);
        
        mPostAdapter = new TribePostAdapter(getActivity());
        mPostAdapter.setActivity(getActivity());
        mPostAdapter.showOtherSchool();
        mPostAdapter.setIsShowTribeIndetail(true);
        mPostAdapter.setTextClickble(true);
        mPostAdapter.setSpanClickble(true);
        mPostAdapter.setShowNoContent(true);
        mPostAdapter.setOnNativeScrollListener(this);
        mPtrListView.setAdapter(mPostAdapter);
        loadData();
    }


    private void loadData() {
        ArrayList<CfPost> mPosts = new ArrayList<CfPost>();
        String json = mPreferences.getString(EXT_POSTS_STRING, "[]");
        try {
            JSONArray ja = new JSONArray(json);
            mPosts = JsonUtil.getArray(ja, CfPost.TRANSFORMER);
            if (mPosts != null && mPosts.size() > 0) {
                mPostAdapter.reset(mPosts);
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fetchPosts(boolean refresh) {
        new ListMyPostTask(refresh).executeLong();
    }
    
    public void setScrollCallBack(StickyScrollCallBack scrollListener) {
        mScrollListener = scrollListener;
    }
    
    public void updateUserInfo(UserInfo userInfo) {
        mUserInfo = userInfo;
        if (mUserInfo.isMine(getActivity())) {
            loadData();
        }
        fetchPosts(true);
    }
    

    public void invalidScroll() {
        mPtrListView.invalidScroll();
    }

    public int getFirstViewToTop(){
        return mPtrListView.getRefreshableView().getChildAt(0).getTop();
    }
    
    public int getStickyHeight() {
        int scrollHeight = mPtrListView.getFirstViewScrollTop();
        System.out.println("---------- get Height : post -- " + scrollHeight);
        if (scrollHeight > ProfileFragment.sStickyTopToTab) {
            return ProfileFragment.sStickyTopToTab;
        }
        return scrollHeight;
    }
    
    public void setStickyH(int stickyH) {
        if (Math.abs(stickyH - getStickyHeight()) < 5) {
            return;
        }
     // 判断高度，根据高度来决定是ScrollView向上滚动还是TopView向下滚动，并算出向下滚动的距离
        int scrollContentHeight = ProfileFragment.sViewPagerHeight - ProfileFragment.sStickyBarHeight;
        int contentHeight = mPtrListView.getContentHeight();
        if (scrollContentHeight <= contentHeight) {
            mPtrListView.getRefreshableView().setSelectionFromTop(1, -stickyH);
        } else {
            int distance = scrollContentHeight - contentHeight - ProfileFragment.sStickyTopToTab + 10;
            if (distance < -ProfileFragment.sStickyTopToTab) {
                distance = -ProfileFragment.sStickyTopToTab;
            }
            if (distance > 0) {
                distance = 0;
            }
            mScrollListener.onScrollChanged(distance);
        }
        mPtrListView.getRefreshableView().setSelectionFromTop(1, -stickyH);
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
            super(getActivity(), MsRequest.CF_LIST_USER_POSTS);
            this.refresh = refresh;
            mOffset = refresh ? 0 : mPostAdapter.getCount();

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset)
                    .append("&user_id=").append(mUserInfo.userId)
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrListView.onRefreshComplete();
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                String json = ja.toString();
                ArrayList<CfPost> posts = JsonUtil.getArray(ja, CfPost.TRANSFORMER);
                if (refresh) {
                    if (mUserInfo.isMine(getActivity())) {
                        saveMyPosts(json);
                    }
                    mPostAdapter.reset(posts);
                } else {
                    mPostAdapter.addAll(posts);         
                }
            }
        }
    }
    

    private void saveMyPosts(String posts) {
        Editor editor = mPreferences.edit();
        editor.putString(EXT_POSTS_STRING, posts);
        editor.commit();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            animScrollY(view);
        } else if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            animScrollY(view);
        }
    }
    
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (null != mScrollListener && mScrollListener.getCurrentViewpagerItem() == 0) {
            return;
        }
        if (firstVisibleItem == 0) {
            mScrollListener.onScrollChanged(0);
        } else if (firstVisibleItem == 1) {
            View firstView = view.getChildAt(0);
            if (null != firstView) {
                int firstTop = firstView.getTop();
                if (firstTop < -ProfileFragment.sStickyTopToTab) {
                    firstTop = -ProfileFragment.sStickyTopToTab;
                }
                mScrollListener.onScrollChanged(firstTop);
            }
        } else if (firstVisibleItem < 3) {
            mScrollListener.onScrollChanged(-ProfileFragment.sStickyTopToTab);
        }
    }
    
    private void animScrollY(AbsListView view) {
        int offsetDistance = 0, firstTop = 0;
        if (view.getFirstVisiblePosition() == 0) {
            View firstView = view.getChildAt(0);
            if (firstView != null) {
                firstTop = firstView.getTop();
                if (firstTop < -ProfileFragment.sStickyTopToTab / 2) {
                    offsetDistance = -ProfileFragment.sStickyTopToTab;
                }
            }

            if (firstTop != offsetDistance) {
                new AnimUiThread(firstTop, offsetDistance).start();
            }
        }
    }
    

    class AnimUiThread extends Thread {
        private int fromPos, toPos;

        public AnimUiThread(int fromPos, int toPos) {
            this.fromPos = fromPos;
            this.toPos = toPos;
        }

        @Override
        public void run() {
            int num = 10;
            for (int i = 0; i < num; i++) {
                try {
                    sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int tempPos = fromPos + (toPos - fromPos) * (i + 1) / num;
                Message msg = uiHandler.obtainMessage();
                msg.what = tempPos;
                msg.sendToTarget();
            }
        }
    }
    

    private Handler uiHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int pos = msg.what;
            mPtrListView.getRefreshableView().setSelectionFromTop(0, pos);
        };
    };
}
