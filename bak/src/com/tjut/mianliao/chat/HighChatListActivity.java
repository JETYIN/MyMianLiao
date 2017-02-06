package com.tjut.mianliao.chat;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.CommonBanner;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.HighTheme;
import com.tjut.mianliao.util.DensityUtil;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class HighChatListActivity extends BaseActivity implements OnClickListener {

    private PullToRefreshListView mPtrListView;
    ArrayList<HighTheme> mHighThemeList;
    HighChatAdapter mHighChatAdapter;
    HashMap<View, HighTheme> mViewThemeMap = new HashMap<View, HighTheme>();
    private CommonBanner mVsSwitcher;
    private LinearLayout mMagicLinearLayout;
    private HighChatDialog mHighChatDialog;
    
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_highchat;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initComponents();
        getTitleBar().setTitle(R.string.high_chat_title);
        // getTitleBar().showRightButton(R.drawable.icon_more_black, this);
        mVsSwitcher = (CommonBanner) findViewById(R.id.vs_switcher);
        mVsSwitcher.setParam(CommonBanner.Plate.HighGroupChat, 0);
        mMagicLinearLayout = (LinearLayout) findViewById(R.id.ly_bg_highchat);
    }

    private void initComponents() {
        mPtrListView = (PullToRefreshListView) this.findViewById(R.id.ptrlv_high_chat_list);
        mHighChatAdapter = new HighChatAdapter();
        mPtrListView.setMode(Mode.DISABLED);
        mPtrListView.setAdapter(mHighChatAdapter);
        new ListHighThemeTask(0, 20).executeLong();
    }

    private class HighChatAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mHighThemeList == null) {
                return 0;
            } else {
                return mHighThemeList.size() / 2;

            }
        }

        @Override
        public Object getItem(int arg0) {

            return null;
        }

        @Override
        public long getItemId(int arg0) {

            return 0;
        }

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {

            View view = null;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_high_chat, parent, false);
            }
            // 1.retrieve views
            View leftLayout = view.findViewById(R.id.left_layout);
            View rightLayout = view.findViewById(R.id.right_layout);
            rightLayout.setVisibility(View.INVISIBLE);
            leftLayout.setOnClickListener(null);
            rightLayout.setOnClickListener(null);

            ProImageView leftIv = (ProImageView) view.findViewById(R.id.iv_left);

            ProImageView rightIv = (ProImageView) view.findViewById(R.id.iv_right);

            TextView leftName = (TextView) view.findViewById(R.id.tv_left);
            TextView rightName = (TextView) view.findViewById(R.id.tv_right );
            TextView leftIntro = (TextView) view.findViewById(R.id.tv_left_intro);
            TextView rightIntro = (TextView) view.findViewById(R.id.tv_right_intro);

            // 2.retrieve highTheme
            HighTheme leftTheme = mHighThemeList.get(index * 2);
            HighTheme rightTheme = null;

            if (mHighThemeList.size() > index * 2 + 1) {
                rightTheme = mHighThemeList.get(index * 2 + 1);
                if (leftTheme.isViewMoveFinished()) {
                    moveLayout(leftLayout, 164, 0, true);
                    moveLayout(rightLayout, 164, 0, false);

                } else if (rightTheme.isViewMoveFinished()) {
                    moveLayout(leftLayout, -164, 0, false);
                    moveLayout(rightLayout, -164, 0, true);
                }
            }

            // 3.process basing highTheme check
            if (rightTheme == null) {

                rightLayout.setTag(null);
                mViewThemeMap.put(leftLayout, leftTheme);
                leftLayout.setOnClickListener(mHighThemeItemClickListener);

                return view;
            } else {
                leftName.setText(leftTheme.getName());
                rightName.setText(rightTheme.getName());
                leftIntro.setText(leftTheme.getIntro());
                rightIntro.setText(rightTheme.getIntro());

                leftIv.setImage(leftTheme.getIcon(), R.drawable.ic_vs);
                rightIv.setImage(rightTheme.getIcon(), R.drawable.ic_vs);

                leftIv.setBackgroundResource(R.drawable.highchat_avatar_bg);
                rightIv.setBackgroundResource(R.drawable.highchat_avatar_bg);

                rightLayout.setVisibility(View.VISIBLE);

                mViewThemeMap.put(leftLayout, leftTheme);
                mViewThemeMap.put(rightLayout, rightTheme);

                SoftReference<View> rightLayoutSoftRef = new SoftReference<View>(rightLayout);
                SoftReference<View> leftLayoutSoftRef = new SoftReference<View>(leftLayout);

                leftLayout.setTag(rightLayoutSoftRef);
                rightLayout.setTag(leftLayoutSoftRef);

                leftLayout.setOnClickListener(mHighThemeItemClickListener);
                rightLayout.setOnClickListener(mHighThemeItemClickListener);
                return view;

            }

        }

    }

    OnClickListener mHighThemeItemClickListener = new OnClickListener() {

        @SuppressWarnings("unchecked")
        @Override
        public void onClick(View view) {
            HighTheme theme = mViewThemeMap.get(view);
            if (theme.isViewMoveFinished()) {

                if (HighChatHelper.isChecked(HighChatListActivity.this, theme.getIndex())) {
                    startChatActivity(theme);

                } else {
                	showConfirmDialog();
                }

                return;
            }

            if (view.getId() == R.id.left_layout) {
                View leftView = view;
                SoftReference<View> rightViewSf = (SoftReference<View>) view.getTag();
                if (rightViewSf == null) {
                    startChatActivity(theme);
                    return;
                }

                View rightView = rightViewSf.get();

                moveLayout(leftView, 60, 500, true);
                moveLayout(rightView, 60, 500, false);

            } else if (view.getId() == R.id.right_layout) {

                View rightView = view;

                SoftReference<View> leftViewSf = (SoftReference<View>) view.getTag();
                View leftView = leftViewSf.get();

                moveLayout(leftView, -60, 500, false);
                moveLayout(rightView, -60, 500, true);

            }
        }

    };

    private void startChatActivity(HighTheme theme) {
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra(HighTheme.BUNDLE_EXTRA, theme.getId());
        i.putExtra(HighTheme.BUNDLE_EXTRA_ROOM_NAME, theme.getName());
        startActivity(i);

    }

    private void moveLayout(View view, int deltaDpX, int duration, boolean actionFlag) {
        Animation translateAnimation = new TranslateAnimation(0,
                DensityUtil.dip2px(HighChatListActivity.this, deltaDpX), 0, 0);
        translateAnimation.setFillAfter(true);
        translateAnimation.setDuration(duration);   
        view.startAnimation(translateAnimation);
        translateAnimation.setAnimationListener(new MyAnimationListener(view, actionFlag));

    }

    private class MyAnimationListener implements AnimationListener {

        private View view;
        private boolean isMoveFinished;

        public MyAnimationListener(View view, boolean finished) {
            this.view = view;
            isMoveFinished = finished;
        }

        @Override
        public void onAnimationEnd(Animation animation) {

            HighTheme theme = mViewThemeMap.get(view);
            theme.setViewMoveFinished(isMoveFinished);

        }

        @Override
        public void onAnimationRepeat(Animation arg0) {

        }

        @Override
        public void onAnimationStart(Animation arg0) {

        }
    };

    private class ListHighThemeTask extends MsTask {
        private int mOffset;
        private int mLimit;

        public ListHighThemeTask(int offset, int limit) {
            super(HighChatListActivity.this, MsRequest.THEMELIST);
            mOffset = offset;
            mLimit = limit;

        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).append("&limit=").append(mLimit).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);

            if (response.isSuccessful()) {
                JSONArray array = response.json.optJSONArray((MsResponse.PARAM_RESPONSE));

                mHighThemeList = JsonUtil.getArray(array, HighTheme.TRANSFORMER);

                // set index for theme basing pos
                int start_index = 0;

                for (HighTheme theme : mHighThemeList) {
                    theme.setIndex(start_index++);
                }
                // //////

                mHighChatAdapter.notifyDataSetChanged();

            }
        }
    }

    @Override
    public void onClick(View v) {

    }
    private void showConfirmDialog () {
    	if (mHighChatDialog == null) {
    		mHighChatDialog = new HighChatDialog(HighChatListActivity.this, R.style.Translucent_NoTitle);
    	}
    	mHighChatDialog.show();
    }
    
    
}
