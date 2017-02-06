package com.tjut.mianliao.profile;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.tjut.mianliao.R;

import java.util.ArrayList;

/**
 * Created by j_hao on 2016/7/19.
 */
public class MyAcountBillActivity extends FragmentActivity implements View.OnClickListener {
    private ArrayList<String> typeList = new ArrayList<>();
    private ListAdapter mPageAdapter;
    @ViewInject(R.id.tablayout_tab)
    private TabLayout mTableLayout;
    @ViewInject(R.id.viewpager)
    private ViewPager mViewPager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account_bill);
        ViewUtils.inject(this);
        buildDate();
    }

    private void buildDate() {
        typeList.add(getString(R.string.income_recodes));
        typeList.add(getString(R.string.resume_recodes));
        typeList.add(getString(R.string.withdrawel_recodes));
        typeList.add(getString(R.string.recharge_recodes));

        mPageAdapter = new ListAdapter(getSupportFragmentManager());

        mPageAdapter.addFragment(new MyAccountFragment(MyAccountFragment.INCOME_RECORD));

        mPageAdapter.addFragment(new MyAccountFragment(MyAccountFragment.CONSUME_RECORD));

        mPageAdapter.addFragment(new MyAccountFragment(MyAccountFragment.WITHDRAW_RECORD));

        mPageAdapter.addFragment(new MyAccountFragment(MyAccountFragment.RECHARGE_RECORD));

        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setAdapter(mPageAdapter);
        mTableLayout.setTabMode(TabLayout.MODE_FIXED);
        mTableLayout.addTab(mTableLayout.newTab().setText(typeList.get(0)));
        mTableLayout.addTab(mTableLayout.newTab().setText(typeList.get(1)));
        mTableLayout.addTab(mTableLayout.newTab().setText(typeList.get(2)));
        mTableLayout.addTab(mTableLayout.newTab().setText(typeList.get(3)));
        /**绑定viewpager**/
        mTableLayout.setupWithViewPager(mViewPager);
        mTableLayout.setTabsFromPagerAdapter(mPageAdapter);
        mViewPager.setOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(mTableLayout) {
                    @Override
                    public void onPageSelected(int position) {
                        mPageAdapter.refresh();
                    }
                }
        );
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    public class ListAdapter extends FragmentPagerAdapter {

        private ArrayList<MyAccountFragment> fragments = new ArrayList<>();

        public ListAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(MyAccountFragment fragment) {
            fragments.add(fragment);
        }

        public void refresh() {
            fragments.get(mViewPager.getCurrentItem()).refresh();
        }

        @Override
        public Fragment getItem(int position) {
            MyAccountFragment fragment = fragments.get(position);
            return fragment;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return typeList.get(position);
        }
    }


}
