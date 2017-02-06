package com.tjut.mianliao.data;

import com.tjut.mianliao.R;

import java.util.ArrayList;

/**
 * Created by Silva on 2016/7/15.
 */
public class LivingMenuInfo {

    public static final int MENU_TYPE_BLACK = 1;
    public static final int MENU_TYPE_CANCEL_BLACK = 2;
    public static final int MENU_TYPE_REPORT = 3;
    public static final int MENU_TYPE_FOLLOW = 4;
    public static final int MENU_TYPE_CANCEL_FOLLOW = 5;
    public static final int MENU_TYPE_CHECK_PROFILE = 6;
    public static final int MENU_TYPE_SHOW_MANAGER = 7;
    public static final int MENU_TYPE_SET_MANAGER = 8;
    public static final int MENU_TYPE_CANCEL_SET_MANAGER = 9;
    public static final int MENU_TYPE_Gag = 10;
    public static final int MENU_TYPE_CANCEL_Gag = 11;
    public static final int MENU_TYPE_CANCEL = 12;


    public  LivingMenuInfo() {}

    public int menuContent;
    public int type;

    public static ArrayList<LivingMenuInfo> getLivingNormalMenu (boolean isMeManager, LiveAdminStatusInfo statusInfo) {
        ArrayList<LivingMenuInfo> menuInfos = new ArrayList<>();
        LivingMenuInfo mBlackInfo = new LivingMenuInfo();
        LivingMenuInfo mReportInfo = new LivingMenuInfo();
        LivingMenuInfo mCancelInfo = new LivingMenuInfo();
        LivingMenuInfo mGagInfo = new LivingMenuInfo();
        mReportInfo.type = MENU_TYPE_REPORT;
        mReportInfo.menuContent = R.string.cht_report_group;
        mCancelInfo.type = MENU_TYPE_CANCEL;
        mCancelInfo.menuContent = R.string.cancel_dialog;
        if (statusInfo.isInblickList) {
            mBlackInfo.type = MENU_TYPE_CANCEL_BLACK;
            mBlackInfo.menuContent = R.string.live_make_cancel_black;
        } else {
            mBlackInfo.type = MENU_TYPE_BLACK;
            mBlackInfo.menuContent = R.string.live_make_black;
        }
        if (statusInfo.isShutUp) {
            mGagInfo.type = MENU_TYPE_CANCEL_Gag;
            mGagInfo.menuContent = R.string.cancel_no_speak;
        } else {
            mGagInfo.type = MENU_TYPE_Gag;
            mGagInfo.menuContent = R.string.no_speak;
        }
        menuInfos.add(mBlackInfo);
        menuInfos.add(mReportInfo);
        if (isMeManager) {
            menuInfos.add(mGagInfo);
        }
        menuInfos.add(mCancelInfo);
        return menuInfos;
    }

    public static ArrayList<LivingMenuInfo> getLivingManagerMenu (LiveAdminStatusInfo statusInfo) {
        ArrayList<LivingMenuInfo> menuInfos = new ArrayList<>();
        LivingMenuInfo mBlackInfo = new LivingMenuInfo();
        LivingMenuInfo mReportInfo = new LivingMenuInfo();
        LivingMenuInfo mCancelInfo = new LivingMenuInfo();
        mReportInfo.type = MENU_TYPE_REPORT;
        mReportInfo.menuContent = R.string.cht_report_group;
        mCancelInfo.type = MENU_TYPE_CANCEL;
        mCancelInfo.menuContent = R.string.cancel_dialog;
        if (statusInfo.isInblickList) {
            mBlackInfo.type = MENU_TYPE_CANCEL_BLACK;
            mBlackInfo.menuContent = R.string.live_make_cancel_black;
        } else {
            mBlackInfo.type = MENU_TYPE_BLACK;
            mBlackInfo.menuContent = R.string.live_make_black;
        }
        menuInfos.add(mBlackInfo);
        menuInfos.add(mReportInfo);
        menuInfos.add(mCancelInfo);
        return menuInfos;
    }

    public static ArrayList<LivingMenuInfo> getLivingRecordMenu (LiveAdminStatusInfo statusInfo) {
        ArrayList<LivingMenuInfo> menuInfos = new ArrayList<>();
        LivingMenuInfo mGagInfo = new LivingMenuInfo();
        LivingMenuInfo mSetManagerInfo = new LivingMenuInfo();
        LivingMenuInfo mCancelInfo = new LivingMenuInfo();
        mCancelInfo.type = MENU_TYPE_CANCEL;
        mCancelInfo.menuContent = R.string.cancel_dialog;
        if (statusInfo.isShutUp) {
            mGagInfo.type = MENU_TYPE_CANCEL_Gag;
            mGagInfo.menuContent = R.string.cancel_no_speak;
        } else {
            mGagInfo.type = MENU_TYPE_Gag;
            mGagInfo.menuContent = R.string.no_speak;
        }
        menuInfos.add(mGagInfo);
        if (statusInfo.isAdmin) {
            mSetManagerInfo.type = MENU_TYPE_CANCEL_SET_MANAGER;
            mSetManagerInfo.menuContent = R.string.live_cancel_set_manager;
        } else {
            mSetManagerInfo.type = MENU_TYPE_SET_MANAGER;
            mSetManagerInfo.menuContent = R.string.live_set_manager;
        }
        menuInfos.add(mSetManagerInfo);
        menuInfos.add(mCancelInfo);
        return menuInfos;
    }



}
