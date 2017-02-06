package com.tjut.mianliao.profile;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ViewPagerFragmentAdapter extends FragmentPagerAdapter {

    private static final String[] mTabNames = new String[]{"主页", "直播", "帖子"};

    private List<Fragment> mFragments = new ArrayList<Fragment>();

    public ViewPagerFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment) {
        if (fragment != null && !mFragments.contains(fragment)) {
            mFragments.add(fragment);
        }
    }

    public List<Fragment> getFragments() {
        return mFragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabNames[position];
    }

}
