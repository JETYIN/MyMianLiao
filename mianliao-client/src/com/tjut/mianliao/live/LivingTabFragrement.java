package com.tjut.mianliao.live;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.tjut.mianliao.MainActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.MyScrollView;
import com.tjut.mianliao.component.MyScrollView.OnScrollListener;
import com.tjut.mianliao.component.TopicTagView;
import com.tjut.mianliao.data.LiveTopic;
import com.tjut.mianliao.main.TabFragment;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

import java.util.ArrayList;

/**
 * Created by Silva on 2016/7/4.
 */
public class LivingTabFragrement extends TabFragment implements View.OnClickListener, OnScrollListener,
        SwipeRefreshLayout.OnRefreshListener {
    
    private static final String TAG = "LivingTabFragrement";

    @ViewInject(R.id.vp_type_living)
    private ViewPager mViewPager;
    @ViewInject(R.id.tabs)
    private TabLayout mTabs;
    @ViewInject(R.id.tabs_top)
    private TabLayout mTabsTop;
    @ViewInject(R.id.my_living_scroll)
    private MyScrollView mScrollView;
    @ViewInject(R.id.srl_living)
    private SwipeRefreshLayout mSwipLayout;
    @ViewInject(R.id.ttv_recommend_living)
    private TopicTagView mTtvLiving;
    @ViewInject(R.id.ll_top_distance)
    private LinearLayout mLlTop;

    private int mTopicTvPadding;
    private int mMesureViewHeight;

    private ArrayList<String> mTitleList = new ArrayList<>();

    private PagerAdapter mPageAdapter;

    private onRefreshCompleteListener mRefreshLitener;

    @Override
    public int getLayoutId() {
        return R.layout.main_tab_living;
    }

    @Override
    public int getNaviButtonId() {
        return R.id.nb_live;
    }

    @Override
    public String getName() {
        return "LivingTabFragrement";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ViewUtils.inject(this, view);
        mTitleBar.setTitle("直播");
        mTopicTvPadding = getResources().getDimensionPixelOffset(R.dimen.live_homepage_topic_padding);
        view.findViewById(R.id.iv_start_live).setOnClickListener(this);
        mLlTop.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        mScrollView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        mTabsTop.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        mMesureViewHeight = mLlTop.getMeasuredHeight();
        Utils.logD(TAG, "top mesure height = " + mTabsTop.getMeasuredHeight());
        Utils.logD(TAG, "scrollview mesure height = " + mScrollView.getMeasuredHeight());
        ViewGroup.LayoutParams params = mViewPager.getLayoutParams();
        params.height = mScrollView.getMeasuredHeight() - mTabsTop.getMeasuredHeight() * 2;
        Utils.logD(TAG, "change viewpager mesure height = " + params.height);
        mViewPager.setLayoutParams(params);
        mScrollView.setOnScrollListener(this);
        mTitleBar.showRightButton(R.drawable.icon_search, this);
        mTitleBar.showLeftButton(R.drawable.icon_personal, this);
        //当布局的状态或者控件的可见性发生改变回调的接口
        mSwipLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                onScroll(mScrollView.getScrollY());
            }
        });
        mSwipLayout.setOnRefreshListener(this);
        mTitleList.add("社团");
        mTitleList.add("课堂");
        mTitleList.add("个人秀");
        mPageAdapter = new PagerAdapter(getFragmentManager());
        mPageAdapter.addFragment(new OrgLiveFragment(OrgLiveFragment.TYPE_ORG)
                .setRefreshListener(mRefreshListener)
                .setNullViewHeight(mMesureViewHeight));
        mPageAdapter.addFragment(new OrgLiveFragment(OrgLiveFragment.TYPE_CLASS)
                .setRefreshListener(mRefreshListener)
                .setNullViewHeight(mMesureViewHeight));
        mPageAdapter.addFragment(new OrgLiveFragment(OrgLiveFragment.TYPE_PERSON)
                .setRefreshListener(mRefreshListener)
                .setNullViewHeight(mMesureViewHeight));
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mPageAdapter);

        mTabs.setTabMode(TabLayout.MODE_FIXED);
        mTabs.addTab(mTabs.newTab().setText(mTitleList.get(0)));//添加tab选项卡
        mTabs.addTab(mTabs.newTab().setText(mTitleList.get(1)));
        mTabs.addTab(mTabs.newTab().setText(mTitleList.get(2)));
        mTabs.setupWithViewPager(mViewPager);//将TabLayout和ViewPager关联起来。
        mTabs.setTabsFromPagerAdapter(mPageAdapter);//给Tabs设置适配器

        mTabsTop.setTabMode(TabLayout.MODE_FIXED);
        mTabsTop.addTab(mTabsTop.newTab().setText(mTitleList.get(0)));//添加tab选项卡
        mTabsTop.addTab(mTabsTop.newTab().setText(mTitleList.get(1)));
        mTabsTop.addTab(mTabsTop.newTab().setText(mTitleList.get(2)));
        mTabsTop.setupWithViewPager(mViewPager);//将TabLayout和ViewPager关联起来。
        mTabsTop.setTabsFromPagerAdapter(mPageAdapter);//给Tabs设置适配器

        mTtvLiving.setLineMargin(getActivity().getResources().
                getDimensionPixelSize(R.dimen.live_homepage_topic_line_magin));
        mTtvLiving.setItemMargins(getActivity().getResources().
                getDimensionPixelSize(R.dimen.live_homepage_topic_item_magin));
        mTtvLiving.setMarginTopWithItemMargin(false);
        mViewPager.setOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(mTabs) {
                    @Override
                    public void onPageSelected(int position) {
                        mPageAdapter.refresh();
                    }
                }
        );
        new GetTopicTask().executeLong();
        return view;

    }

    public void toggleRefreshing(boolean enabled) {
        if (mSwipLayout != null) {
            mSwipLayout.setEnabled(enabled);
        }
    }

    private void fillRecommendLiving(ArrayList<LiveTopic> tags) {
        mTtvLiving.removeAllViews();
        for (LiveTopic tag : tags) {
            mTtvLiving.addView(getCommentLivingView(tag));
        }
    }

    @Override
    public void onScroll(int scrollY) {
        if (scrollY < mMesureViewHeight) {
            mScrollView.setIsCirticalPoint(false);
            mScrollView.setScrollable(true);
            if (scrollY == 0) {
                mScrollView.setIsCirticalPoint(true);
                mPageAdapter.viewFirstItem();
            }
        } else if (scrollY == mMesureViewHeight) {
            mScrollView.setIsCirticalPoint(true);
        } else {
            mScrollView.setIsCirticalPoint(false);
            mScrollView.setScrollable(false);
        }
        int mBuyLayout2ParentTop = Math.max(scrollY, mTabs.getTop());
        mTabsTop.layout(0, mBuyLayout2ParentTop, mTabsTop.getWidth(),
                mBuyLayout2ParentTop + mTabsTop.getHeight());
    }


    private void showLiveingList(LiveTopic topic) {
        Intent intent = new Intent(getActivity(), LivingListActivity.class);
        intent.putExtra(LivingListActivity.LIVE_TOPIC_INFO, topic);
        startActivity(intent);
    }

    private class GetTopicTask extends MsTask {
        public GetTopicTask() {
            super(getActivity(), MsRequest.LIST_MAIN_TOPICS);
        }

        @Override
        protected String buildParams() {
            return "offset=0&limit=4";
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ArrayList<LiveTopic> infos = JsonUtil.getArray(response.getJsonArray(),
                        LiveTopic.TRANSFORMER);
                if (infos != null && infos.size() > 0) {
                    fillRecommendLiving(infos);
                }
            }
        }
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        private ArrayList<OrgLiveFragment> fragments = new ArrayList<>();

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(OrgLiveFragment fragment) {
            fragments.add(fragment);
        }

        public void refresh() {
            fragments.get(mViewPager.getCurrentItem()).refresh();
        }

        public void viewFirstItem() {
            for (OrgLiveFragment fragment : fragments) {
                fragment.viewFirstItem();
            }
        }

        @Override
        public Fragment getItem(int position) {
            OrgLiveFragment fragment = fragments.get(position);
            fragment.refresh();
            return fragment;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitleList.get(position);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_hot_post_item:
                break;
            case R.id.btn_right:
                startActivity(SearchLivingActivity.class);
                break;
            case R.id.btn_left:
                MainActivity.showDrawerLayout();
                break;
            case R.id.iv_start_live:
                startActivity(CreateLiveRoomActivity.class);
                break;
            case R.id.tv_topic:
                LiveTopic topic = (LiveTopic) view.getTag();
                showLiveingList(topic);
                break;
            default:
                break;
        }

    }

    private View getCommentLivingView(LiveTopic tag) {
        View view = mInflater.inflate(R.layout.list_item_topic_tag, null);
        TextView tv = (TextView) view.findViewById(R.id.tv_topic);
        Drawable drawable = getDrawable(tag);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            tv.setBackground(drawable);
        } else {
            tv.setBackgroundDrawable(drawable);
        }
        tv.setText("#" + tag.name + "#");
        tv.setOnClickListener(this);
        tv.setTextColor(Color.WHITE);
        tv.setTag(tag);
        return view;
    }

    private Drawable getDrawable(LiveTopic tag) {
        int strokWidth = 1;
        int cornerWith = mTopicTvPadding;
        int strokColor = Color.parseColor(new StringBuilder("#").append(tag.color).toString());
        int cornerColor = Color.parseColor(new StringBuilder("#").append(tag.color).toString());
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(cornerWith);
        gd.setColor(strokColor);
        gd.setStroke(strokWidth, cornerColor);
        return gd;
    }

    private onRefreshCompleteListener mRefreshListener = new onRefreshCompleteListener() {
        @Override
        public void onRefreshComplete() {
            mSwipLayout.setRefreshing(false);
            mPageAdapter.viewFirstItem();
            mScrollView.setIsCirticalPoint(true);
        }
    };

    @Override
    public void onRefresh() {
        mPageAdapter.refresh();
        new GetTopicTask().executeLong();
    }

    public interface onRefreshCompleteListener {
        void onRefreshComplete();
    }
}
