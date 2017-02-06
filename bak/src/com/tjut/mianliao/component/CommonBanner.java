package com.tjut.mianliao.component;

import java.util.ArrayList;

import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BrowserActivity;
import com.tjut.mianliao.DayNightNoticeDialog;
import com.tjut.mianliao.MianLiaoApp;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.BannerInfo;
import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.SystemInfo;
import com.tjut.mianliao.explore.EmotionsDetailActivity;
import com.tjut.mianliao.forum.nova.ChannelInfoManager;
import com.tjut.mianliao.forum.nova.ForumChannelDetailActivity;
import com.tjut.mianliao.forum.nova.ForumPostDetailActivity;
import com.tjut.mianliao.news.NewsDetailsActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class CommonBanner extends ViewSwitcher implements OnClickListener {

    ArrayList<BannerInfo> mBannerData;
    Context mContext;
    LayoutInflater mInflater;
    SwitcherAdapter mAdapter;
    int mBannerType, mSchoolId;
    TextView TvCd = null;
    private boolean mIsTaskRunning;
    private ChannelInfoManager mChannelInfoManager;
    private BannerTask mBannerTask;
    private boolean mIsNightMode;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (TvCd != null) {
                TvCd.setText((CharSequence) msg.obj);
            }
        }
    };

    public CommonBanner(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CommonBanner);
        mBannerType = ta.getInteger(R.styleable.CommonBanner_banner_plate, -1);
        ta.recycle();

        mContext = context;
        mInflater = LayoutInflater.from(context);
        mAdapter = new SwitcherAdapter();
        this.setAdapter(mAdapter);
        mChannelInfoManager = ChannelInfoManager.getInstance(context);

        if (mBannerType != -1) {
            if (mBannerType == Plate.RoamSchool) {
                loadDataFromCache(mBannerType);
            } else {
                loadData(mBannerType);
            }
        }
        mIsNightMode = Settings.getInstance(context).isNightMode();
    }

    public void setParam(int type, int schoolId) {
        this.mSchoolId = schoolId;
        this.mBannerType = type;
        if (!mIsTaskRunning) {
            loadData(type);
        } else {
            if (mBannerTask != null && mBannerTask.mPlate != type) {
                mAdapter.notifyDataSetChanged();
                mBannerTask.cancel(true);
                loadData(type);
            }
            
        }
    }

    private void loadDataFromCache(int type) {
        mBannerData = DataHelper.loadBannerInfos(mContext, type);
        if (mBannerData != null && mBannerData.size() != 0) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void loadData(int type) {
        loadDataFromCache(type);
        mAdapter.notifyDataSetChanged();
        new BannerTask(type, 0, mSchoolId).executeLong();
    }

    public void refreshData() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private class SwitcherAdapter extends ViewSwitcherAdapter {

        public SwitcherAdapter() {
            super();
        }

        @Override
        public int getCount() {
            if (mBannerData == null) {
                return 0;
            } else {
                return mBannerData.size();
            }

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.switcher_item_forum, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.ivImage = (ProImageView) convertView.findViewById(R.id.iv_image);
                viewHolder.tv = (TextView) convertView.findViewById(R.id.tv_info);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            BannerInfo info = mBannerData.get(position);

            Picasso.with(mContext)
	        	.load(info.getImage())
	        	.placeholder(mIsNightMode ? R.drawable.bg_default_big_night :
                    R.drawable.bg_default_big_day)
	        	.into(viewHolder.ivImage);
            viewHolder.ivImage.setImage(info.getImage(), R.drawable.bg_img_loading);
            viewHolder.ivImage.setOnClickListener(CommonBanner.this);
            viewHolder.ivImage.setTag(info);

            viewHolder.tv.setVisibility(View.INVISIBLE);

            if (info.getData() != null && info.getData().getType() == DateType.BlackWhiteModeText) {
                viewHolder.tv.setVisibility(View.VISIBLE);
                TvCd = viewHolder.tv;
                viewHolder.tv.setTextColor(Color.GRAY);
                CountDowner countDowner = CountDowner.getInstance();
                if (countDowner != null) {
                    countDowner.registerBanner(CommonBanner.this);
                } else {
                    getSystemInfo();
                }
            }

            if (info.getData() != null && info.getData().getType() == DateType.ChannelInfo) {
                String id = info.getData().getData();
                if (id != null && id.length() > 0) {
                    mChannelInfoManager.getChannelInfoById(Integer.parseInt(id));
                }
            }

            return convertView;
        }
    }

    @Override
    public void onClick(View arg0) {
        BannerInfo info = (BannerInfo) arg0.getTag();
        if (info.getData() == null || info.getData().getData() == null) {
            return;
        }

        String data = info.getData().getData();

        int type = info.getData().getType();
        Intent intent = null;
        switch (type) {
            case DateType.IdOfPost:
                int postId = Integer.parseInt(data);
                intent = new Intent(mContext, ForumPostDetailActivity.class);
                intent.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA_ID, postId);
                break;
            case DateType.IdOfAnnounceMent:
                int newsId = Integer.parseInt(data);
                intent = new Intent(mContext, NewsDetailsActivity.class);
                intent.putExtra(NewsDetailsActivity.EXTRA_NEWS_ID, newsId);
                break;
            case DateType.PageUrl:
                data = getFinalVisUrl(data);
                intent = new Intent(mContext, BrowserActivity.class);
                intent.putExtra(BrowserActivity.URL, data);
                break;
            case DateType.BlackWhiteModeText:
                intent = new Intent(mContext, DayNightNoticeDialog.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                break;
            case DateType.IdOfExpression:
                int resId = Integer.parseInt(data);
                intent = new Intent(mContext, EmotionsDetailActivity.class);
                intent.putExtra(EmotionsDetailActivity.EXT_RES_ID, resId);
                break;
            case DateType.ChannelInfo:
                int channelId = Integer.parseInt(data);
                ChannelInfo channelInfo = mChannelInfoManager.getChannelInfoById(channelId);
                if (channelInfo == null) {
                    return;
                }
                intent = new Intent(mContext, ForumChannelDetailActivity.class);
                intent.putExtra(ForumChannelDetailActivity.EXT_DATA, channelInfo);
                break;
            default:
                break;
        }

        if (intent != null) {
            mContext.startActivity(intent);
        }
    }

    private String getFinalVisUrl(String url) {
        StringBuilder sb = new StringBuilder(url);
        if (Utils.URL_PATTERN.matcher(url).matches()) {
            AccountInfo info = AccountInfo.getInstance(MianLiaoApp.getAppContext());
            sb.append("&token=").append(info.getToken()).append("&source=ml")
                .append("&uid=").append(String.valueOf(info.getUserId()));
        }
        return sb.toString();
    }

    public void getSystemInfo() {
        new SystemInfoTask().executeLong();
    }

    // Banner位置
    public interface Plate {
        int SquareMySchool = 0; // 广场本校界面
        int RoamSchool = 1; // 漫游进具体高校界面
        int AnnounceMent = 2; // 公告界面
        int ExploreMain = 3; // 探索主界面
        int VipMain = 4; // 会员中心主界面
        int VipIntroduce = 5; // 会员功能介绍界面
        int DecorateMain = 6; // 装扮商城主界面
        int ChatMain = 7; // 聊天表情界面
        int HighGroupChat = 8; // high群聊界面
        int RecruitMain = 9; // 微招聘主界面
        int RecruitQueryMain = 10; // 招聘查询主界面
        int ChatBubbleMain = 11; // 聊天气泡主界面
        int TaskMain = 12; // 任务主界面
        int TribeHomePage = 13; // 部落首页 
        int GameZone = 14; // 游戏专区
    }

    // Banner数据类型
    public interface DateType {
        int IdOfPost = 0; // 帖子id
        int IdOfAnnounceMent = 1; // 公告id
        int PageUrl = 2; // url
        int BlackWhiteModeText = 3; // 黑白模式弹框文字
        int IdOfExpression = 4; // 聊天表情id
        int ChannelInfo = 5; // 跳转到某个频道
        int KdUrl = 6; // 跳转到特殊的网页
    }

    private class BannerTask extends MsTask {

        int mPlate, mDateType, mOtherSchoolId;

        public BannerTask(int plate, int data_type, int other_school_id) {
            super(mContext, MsRequest.BANNER_LIST);
            this.mPlate = plate;
            this.mDateType = data_type;
            this.mOtherSchoolId = other_school_id;
            mBannerTask = this;

        }

        @Override
        protected String buildParams() {

            StringBuilder sb = new StringBuilder("plate=").append(mPlate);

            if (mOtherSchoolId != 0) {
                sb.append("&other_school_id=").append(mOtherSchoolId);
            }

            return sb.toString();
        }

        @Override
        protected void onPreExecute() {
            mIsTaskRunning = true;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mIsTaskRunning = false;
            if (response.isSuccessful()) {
                setViewPageParent((ViewGroup)getParent());
                JSONArray ja = response.json.optJSONArray("response");
                mBannerData = JsonUtil.getArray(ja, BannerInfo.TRANSFORMER);
                DataHelper.deleteCacheBannerByPlate(mContext, mPlate);
                DataHelper.insertCacheBannerInfos(mContext, mBannerData);
                mAdapter.notifyDataSetChanged();

            }
        }
    }

    private class SystemInfoTask extends MsTask {

        public SystemInfoTask() {
            super(mContext, MsRequest.SYSTEM_INFO);
        }

        @Override
        protected void onPostExecute(MsResponse response) {

            if (response.isSuccessful()) {
                SystemInfo systemInfo = SystemInfo.fromJson(response.getJsonObject());
                if (systemInfo != null) {
                    CountDowner.create(systemInfo.start, !systemInfo.neight);
                    if (systemInfo.neight) {
                        Settings.setNightInfoToSp(true);
                    } else {
                        Settings.setNightInfoToSp(false);
                    }
                }
            }
        }
    }

    private class ViewHolder {
        ProImageView ivImage;
        TextView tv;
    }
}
