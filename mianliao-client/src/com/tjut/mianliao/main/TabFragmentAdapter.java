package com.tjut.mianliao.main;

import java.util.HashMap;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.tjut.mianliao.R;
import com.tjut.mianliao.chat.RecentChatsTabFragment;
import com.tjut.mianliao.component.NaviButton;
import com.tjut.mianliao.forum.PostStreamTabFragment;
import com.tjut.mianliao.live.LiveTabFragment;
import com.tjut.mianliao.live.LivingTabFragrement;
import com.tjut.mianliao.tribe.TribeTabFragement;

public class TabFragmentAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {

    private static enum Tab {
        TAB_FORUM, TAB_TRIBE,  TAB_LIVE, TAB_CHAT
    }

    public static final String SP_IS_FIRST_COLLEGE = "sp_my_college";
    public static final String SP_IS_FIRST_FORUM = "sp_forum_homepge";

    private HashMap<Tab, NaviButton> mTabButtons;
    private NaviButton mActiveButton;

    public TabFragmentAdapter(FragmentManager fm, View parent) {
        super(fm);
        mTabButtons = new HashMap<Tab, NaviButton>();
        mTabButtons.put(Tab.TAB_CHAT, (NaviButton) parent.findViewById(R.id.nb_chat));
        mTabButtons.put(Tab.TAB_FORUM, (NaviButton) parent.findViewById(R.id.nb_forum));
        mTabButtons.put(Tab.TAB_TRIBE, (NaviButton) parent.findViewById(R.id.nb_tribe));
        mTabButtons.put(Tab.TAB_LIVE, (NaviButton) parent.findViewById(R.id.nb_live));
        setActiveButton(Tab.TAB_FORUM);
    }

    public void setActiveButton(Tab tab) {
        if (mActiveButton != null) {
            mActiveButton.setSelected(false);
        }
        mActiveButton = mTabButtons.get(tab);
        mActiveButton.setSelected(true);
    }

    public Tab getTab(int id) {
        switch (id) {
            case R.id.nb_forum:
                return Tab.TAB_FORUM;
            case R.id.nb_tribe:
                return Tab.TAB_TRIBE;
            case R.id.nb_chat:
            	return Tab.TAB_CHAT;
            case R.id.nb_live:
                return Tab.TAB_LIVE;
            default:
                return Tab.TAB_FORUM;
        }
    }

    public int onTabButtonClicked(Tab tab) {
        if (mActiveButton == mTabButtons.get(tab) ||
                mActiveButton == mTabButtons.get(Tab.TAB_CHAT) ||
                mActiveButton == mTabButtons.get(Tab.TAB_TRIBE)) {
            mActiveButton.getRefTab().onTabButtonClicked();
        }
        return tab.ordinal();
    }

    @Override
    public Fragment getItem(int i) {
        switch (Tab.values()[i]) {
            case TAB_FORUM:
                return new PostStreamTabFragment();
            case TAB_TRIBE:
                return new TribeTabFragement();
            case TAB_CHAT:
                return new RecentChatsTabFragment();
            case TAB_LIVE:
                return new LivingTabFragrement();
            default:
                return new PostStreamTabFragment();
        }
    }

    @Override
    public int getCount() {
        return Tab.values().length;
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }

    @Override
    public void onPageSelected(int i) {
        setActiveButton(Tab.values()[i]);
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }
}
