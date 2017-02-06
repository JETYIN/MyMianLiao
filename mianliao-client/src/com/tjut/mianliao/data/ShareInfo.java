package com.tjut.mianliao.data;

import android.content.Context;

import com.tjut.mianliao.R;

import java.util.ArrayList;

/**
 * Created by Silva on 2016/7/19.
 */
public class ShareInfo {

    public static final int SHARE_TYPE_CIRCLE_OF_FRIENDS = 332;
    public static final int SHARE_TYPE_WEI_CHAT = 333;
    public static final int SHARE_TYPE_WEI_BO = 334;
    public static final int SHARE_TYPE_QQ = 335;
    public static final int SHARE_TYPE_QQ_ZONE = 336;
    public static final int SHARE_TYPE_TRIBE = 337;
    public static final int SHARE_TYPE_SCHOOL_HOME = 338;

    public int type;
    public int iconSrc;
    public String name;

    private Context mContext;

    public ShareInfo(Context context) {
        mContext = context;
    }

    private ShareInfo () {
    }

    public ArrayList<ShareInfo> getShareList () {
        ArrayList<ShareInfo> mShareList =  new ArrayList<>();
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.type = ShareInfo.SHARE_TYPE_CIRCLE_OF_FRIENDS;
        shareInfo.name = mContext.getString(R.string.share_friend_circle);
        mShareList.add(shareInfo);

        shareInfo = new ShareInfo();
        shareInfo.type = ShareInfo.SHARE_TYPE_WEI_CHAT;
        shareInfo.name = mContext.getString(R.string.share_wei_chat);
        mShareList.add(shareInfo);

        shareInfo = new ShareInfo();
        shareInfo.type = ShareInfo.SHARE_TYPE_WEI_BO;
        shareInfo.name = mContext.getString(R.string.share_wei_bo);
        mShareList.add(shareInfo);

        shareInfo = new ShareInfo();
        shareInfo.type = ShareInfo.SHARE_TYPE_QQ;
        shareInfo.name = mContext.getString(R.string.share_qq);
        mShareList.add(shareInfo);

        shareInfo = new ShareInfo();
        shareInfo.type = ShareInfo.SHARE_TYPE_QQ_ZONE;
        shareInfo.name = mContext.getString(R.string.share_qq_zone);
        mShareList.add(shareInfo);

        shareInfo = new ShareInfo();
        shareInfo.type = ShareInfo.SHARE_TYPE_TRIBE;
        shareInfo.name = mContext.getString(R.string.share_tribe);
        mShareList.add(shareInfo);

        shareInfo = new ShareInfo();
        shareInfo.type = ShareInfo.SHARE_TYPE_SCHOOL_HOME;
        shareInfo.name = mContext.getString(R.string.share_school_home);
        mShareList.add(shareInfo);

        return  mShareList;
    }

}
