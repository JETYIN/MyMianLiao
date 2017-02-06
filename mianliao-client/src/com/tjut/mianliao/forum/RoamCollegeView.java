package com.tjut.mianliao.forum;

import java.util.ArrayList;

import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.SearchView;
import com.tjut.mianliao.component.SearchView.OnSearchConditionListener;
import com.tjut.mianliao.component.SearchView.OnSearchTextListener;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.SchoolInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.components.PlanetCollegeView;
import com.tjut.mianliao.forum.components.PlanetCollegeView.Area;
import com.tjut.mianliao.forum.components.PlanetFillTool;
import com.tjut.mianliao.forum.components.SchoolListDialog;
import com.tjut.mianliao.forum.components.SchoolListDialog.OnRefreshListener;
import com.tjut.mianliao.forum.nova.FormOtherSchoolActivity;
import com.tjut.mianliao.task.TaskActivity;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class RoamCollegeView extends LinearLayout implements OnSearchTextListener,
        PlanetCollegeView.StarClickListener, OnClickListener, OnSearchConditionListener,
        SearchView.OnClearIconClickListener, OnRefreshListener {

    public static final String SP_IS_FIRST = "sp_roam_view";

    public RoamCollegeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        View view = inflate(context, R.layout.post_roam_college, this);
        mUserInfoManager = UserInfoManager.getInstance(context);
        mSearchResults = new ArrayList<SchoolInfo>();
        mCollectResults = new ArrayList<SchoolInfo>();
        this.initComponents(view);
        this.initAdapters();
        mUserInfo = AccountInfo.getInstance(context).getUserInfo();
        new FetchUserTask().executeLong();
    }

    private UserInfo mUserInfo;
    private SearchView mSearchView;
    private ListView mLvSearch;
    private SchoolListDialog mSchoolListDialog, mCollectionsDialog;
    private SearchListAdapter mSearchAdapter;
    private CollectListAdapter mCollectAdapter;
    private RegionSchoolListAdapter mRegionSchoolAdapter;
    private ImageView mBtnCollect;
    private Context mContext;
    private UserInfoManager mUserInfoManager;

    private int mPlanetId;
    private boolean mRefresh;
    private ImageView mIvUnblock;
    private RelativeLayout mLlUnblock;

    ArrayList<SchoolInfo> mSearchResults;
    ArrayList<SchoolInfo> mCollectResults;
    private static final int[] sDialogImageRes = { R.drawable.pic_star_blue_title, R.drawable.pic_star_yellow_title,
        R.drawable.pic_star_purple_title, R.drawable.pic_star_pink_title, R.drawable.pic_star_grown_title };
    private static final int[] sPlanetNameRes = { R.string.plc_planet1_name, R.string.plc_planet2_name,
            R.string.plc_planet3_name, R.string.plc_planet4_name, R.string.plc_planet5_name };
    private static final int[] sProListRes = { R.string.plc_west_area, R.string.plc_south_area,
            R.string.plc_north_area, R.string.plc_east_area, R.string.plc_center_area };
    
    private PlanetCollegeView[] planetCollegeViews;

    private void initComponents(View parentView) {
        mLvSearch = (ListView) parentView.findViewById(R.id.lv_search);
        mSearchView = (SearchView) parentView.findViewById(R.id.planet_search);
        mSchoolListDialog = (SchoolListDialog) parentView.findViewById(R.id.school_list_dialog);
        mCollectionsDialog = (SchoolListDialog) parentView.findViewById(R.id.school_collection_list_dialog);
        mSchoolListDialog.setOnRefreshListener(this);
        mBtnCollect = (ImageView) parentView.findViewById(R.id.bt_collect_school);
        mBtnCollect.setOnClickListener(this);

        mSearchView.setOnSearchTextListener(this);
        mSearchView.setOnSearchConditionListener(this);

        PlanetCollegeView[] views = { (PlanetCollegeView) parentView.findViewById(R.id.planet_star3),
                (PlanetCollegeView) parentView.findViewById(R.id.planet_star4),
                (PlanetCollegeView) parentView.findViewById(R.id.planet_star2),
                (PlanetCollegeView) parentView.findViewById(R.id.planet_star5),
                (PlanetCollegeView) parentView.findViewById(R.id.planet_star1) };
        planetCollegeViews = views;

        mIvUnblock = (ImageView) parentView.findViewById(R.id.iv_lock_prompt);
        mLlUnblock = (RelativeLayout) parentView.findViewById(R.id.ll_lock_prompt);
        mIvUnblock.setOnClickListener(this);
        mLlUnblock.setOnClickListener(this);

        int firstPlanetId = Area.West;
        for (PlanetCollegeView view : views) {
            view.setStarClickListener(this);
            view.setPlateId(firstPlanetId++);
        }
        mPlanetFillTool = new PlanetFillTool(mContext, views);
    }
    
    public void fetchData() {
        mPlanetFillTool.fetchData();
    }

    private void initAdapters() {
        mSearchAdapter = new SearchListAdapter();
        mCollectAdapter = new CollectListAdapter();
        mRegionSchoolAdapter = new RegionSchoolListAdapter();

        mLvSearch.setAdapter(mSearchAdapter);
        mSchoolListDialog.setListAdatper(mRegionSchoolAdapter);
        mCollectionsDialog.setListAdatper(mCollectAdapter);

        mSchoolListDialog.setOnItemClickListener(itemClickListener);
        mCollectionsDialog.setOnItemClickListener(itemClickListener);

        mLvSearch.setOnItemClickListener(itemClickListener);

    }

    class SearchListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSearchResults.size();
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
        public View getView(int index, View arg1, ViewGroup arg2) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_roam_college_search, null);
            TextView tvName = (TextView) view.findViewById(R.id.tv_school_name);
            tvName.setText(mSearchResults.get(index).getName());
            if (mSearchResults.get(index).isUnlock()) {
            	tvName.setTextColor(0xFF32BBBC);
            } else {
            	tvName.setTextColor(Color.GRAY);
            }
            return view;
        }

    }

    class CollectListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mCollectResults.size();
        }

        @Override
        public String getItem(int arg0) {
            return mCollectResults.get(arg0).getName();
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int index, View arg1, ViewGroup arg2) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_schoollist, null);
            TextView mTextView;
            mTextView = (TextView) view.findViewById(R.id.tv_school_name);
            mTextView.setText(mCollectResults.get(index).getName());
            mTextView.setTextColor(0xFF32BBBC);
            return view;
        }

    }

    class RegionSchoolListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSearchResults.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }


        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int index, View arg1, ViewGroup arg2) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_schoollist, null);
            TextView mTextView;
            SchoolInfo schoolInfo = mSearchResults.get(index);
            mTextView = (TextView) view.findViewById(R.id.tv_school_name);
            ImageView mIvIcon = (ImageView) view.findViewById(R.id.tv_icon);
            mIvIcon.setVisibility(View.VISIBLE);
            mTextView.setText(schoolInfo.getName());
         // VIP
//            if (schoolInfo.isVip()) {
//                mTextView.setTextColor(0xFF32BBBC);
//                mIvIcon.setImageResource(R.drawable.roam_icon_vip);
//            } else 
            if (schoolInfo.isUnlock()) {
                mTextView.setTextColor(0xFF32BBBC);
                mIvIcon.setVisibility(View.GONE);
            } else {
            	mTextView.setTextColor(Color.GRAY);
            	mIvIcon.setImageResource(R.drawable.roam_icon_lock);
            }

            return view;
        }
    }

    OnItemClickListener itemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
            int schoolId = -1;
            String schoolName = null;
            boolean isCollection = false;
            if (arg0 == mLvSearch) {
                schoolId = mSearchResults.get(arg2).getSchoolId();
                schoolName = mSearchResults.get(arg2).getName();
                isCollection = mSearchResults.get(arg2).isCollection();
                if (!mSearchResults.get(arg2).isUnlock()) {
                	Toast.makeText(mContext, R.string.cf_school_is_lock, Toast.LENGTH_SHORT).show();
                	return;
                }
            } else {
                if (arg0 == mCollectionsDialog.getListView()) {
                    schoolId = mCollectResults.get(arg2 - 1).getSchoolId();
                    schoolName = mCollectResults.get(arg2 - 1).getName();
                    isCollection = mCollectResults.get(arg2 - 1).isCollection();
//                    if (mCollectResults.get(arg2 - 1).isVip()) {
//                        if (!mUserInfo.vip) {
//                            Toast.makeText(mContext, "VIP用户才能进哦!", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                    } else {
                        if (!mCollectResults.get(arg2 - 1).isUnlock()) {
                            Toast.makeText(mContext, R.string.cf_school_is_lock, Toast.LENGTH_SHORT).show();
                            return;
                        }
//                    } 
                } else {
                    schoolId = mSearchResults.get(arg2 - 1).getSchoolId();
                    schoolName = mSearchResults.get(arg2 - 1).getName();
                    isCollection = mSearchResults.get(arg2 - 1).isCollection();
//                    if (mSearchResults.get(arg2 - 1).isVip()) {
//                        if (!mUserInfo.vip) {
//                            Toast.makeText(mContext, "VIP用户才能进哦!", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                    } else {
                        if (!mSearchResults.get(arg2 - 1).isUnlock()) {
                            Toast.makeText(mContext, R.string.cf_school_is_lock, Toast.LENGTH_SHORT).show();
                            return;
                        }
//                    }
                }
            }
            Intent intent = new Intent();
            intent.setClass(mContext, FormOtherSchoolActivity.class);
            intent.putExtra(Forum.INTENT_EXTRA_SCHOOLID, schoolId);
            intent.putExtra(Forum.INTENT_EXTRA_SCHOOLNAME, schoolName);
            intent.putExtra(Forum.INTENT_EXTRA_ISCLOOECTION, isCollection);

            mContext.startActivity(intent);
            mSchoolListDialog.setVisibility(View.GONE);
            mCollectionsDialog.setVisibility(View.GONE);
        }

    };
    private PlanetFillTool mPlanetFillTool;

    public class SearchSchoolTask extends MsTask {
        private String mName;
        private int mOffset;
        private int mLimit;
        private int mAreaId;
        private boolean mIsSearchMode;

        public SearchSchoolTask(String name, int offset, int limit, int areaId,
                boolean suggest, boolean isSearchMode) {
            super(mContext, isSearchMode ? MsRequest.SCHOOL_SEARCH : MsRequest.SCHOOL_SEARCH_AREA);
            mName = name;
            mOffset = offset;
            mLimit = limit;
            mAreaId = areaId;
            mIsSearchMode = isSearchMode;
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder("offset=").append(mOffset)
                    .append("&limit=").append(mLimit);
            if(mIsSearchMode) {
                sb.append("&name=").append(mName);
            } else {
                sb.append("&area_id=").append(mAreaId);
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (mSchoolListDialog.getPullList() != null) {
                mSchoolListDialog.getPullList().onRefreshComplete();
            }
            if (response.isSuccessful()) {
                JSONArray array = response.getJsonArray();
                if (array != null) {
                    if (!mRefresh || mIsSearchMode) {
                        mSearchResults.clear();
                    }
                    for (int i = 0; i < array.length(); i++) {
                        SchoolInfo fs = SchoolInfo.fromJson(array.optJSONObject(i));
                        mSearchResults.add(fs);
                    }
                    if (mIsSearchMode) {
                        mLvSearch.setVisibility(View.VISIBLE);
                        mSearchAdapter.notifyDataSetChanged();
                    } else {
                        DataHelper.deleteRoamschoolInfo(mContext, mAreaId);
                        DataHelper.insertCacheRoamSchoolInfo(mContext, mSearchResults);
                        mRegionSchoolAdapter.notifyDataSetChanged();
                    }

                }
            }
        }
    }

    private class ListCollectSchoolTask extends MsTask {
        private int mOffset;
        private int mLimit;

        public ListCollectSchoolTask(int offset, int limit) {
            super(mContext, MsRequest.SCHOOL_COLLECT_LIST);
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

                if (array != null) {
                    mCollectResults.clear();
                    for (int i = 0; i < array.length(); i++) {
                        SchoolInfo fs = SchoolInfo.fromJson(array.optJSONObject(i));
                        mCollectResults.add(fs);
                    }
                    DataHelper.deleteRoamschoolInfo(mContext);
                    DataHelper.insertCacheRoamSchoolInfo(mContext, mCollectResults);
                    mCollectAdapter.notifyDataSetChanged();

                }
            }
        }
    }

    @Override
    public void onSearchTextChanged(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            mSearchResults.clear();
            mLvSearch.setVisibility(View.GONE);
            return;

        }
        new SearchSchoolTask(Utils.urlEncode(text.toString()), 0, 20, -1, true, true).executeLong();
    }

    @Override
    public void onStartClick(int planetId) {
        if (!planetCollegeViews[planetId - 10].isUnlocked()) {
            mLlUnblock.setVisibility(View.VISIBLE);
            return; 
        }
        setClearBack();
        planetCollegeViews[planetId - 10].setTitleBackground(R.drawable.pic_name_choose);
        mSchoolListDialog.setVisibility(View.VISIBLE);
        mSchoolListDialog.setDialogImage(sDialogImageRes[planetId - 10]);
        mSchoolListDialog.setTitle(sPlanetNameRes[planetId - 10]);
        mSchoolListDialog.setProvince(sProListRes[planetId - 10]);
        mLvSearch.setVisibility(View.GONE);
        mPlanetId = planetId;
        mSearchResults = DataHelper.loadRoamschoolInfos(mContext, mPlanetId);
        if (mSearchResults != null && mSearchResults.size() != 0) {
            mRegionSchoolAdapter.notifyDataSetChanged();
            searchSchollInfo(false);
        } else {
            searchSchollInfo(false);
        }

    }
    
    public class FetchUserTask extends MsTask {

        public FetchUserTask() {
            super(mContext, MsRequest.USER_FULL_INFO);
        }

        @Override
        protected String buildParams() {
            return "user_id=" + mUserInfo.userId;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mUserInfo = UserInfo.fromJson(response.getJsonObject());
                if (mUserInfo == null) {
                    System.out.println("null");
                } else {
                    mUserInfoManager.updateUserInfo(mUserInfo);
                }
            }
        }
    }
    

    private void searchSchollInfo(boolean refresh) {
        mRefresh = refresh;
        int offset = refresh ? mRegionSchoolAdapter.getCount() - 1 : 0;
        new SearchSchoolTask("", offset, 20, mPlanetId, true, false).executeLong();
    }

    @Override
    public void onClick(View view) {
        if (view == mBtnCollect) {
            mCollectionsDialog.setTitle(R.string.plc_my_collection);
            mCollectionsDialog.setVisibility(View.VISIBLE);
            mLvSearch.setVisibility(View.GONE);
            mCollectResults = DataHelper.loadRoamschoolInfos(mContext);
            if (mCollectResults != null && mCollectResults.size() != 0) {
                mCollectAdapter.notifyDataSetChanged(); 
                new ListCollectSchoolTask(0, 20).executeLong();
            } else {
                new ListCollectSchoolTask(0, 20).executeLong();
            }
        }
        switch (view.getId()) {
            case R.id.ll_lock_prompt:
                mLlUnblock.setVisibility(View.INVISIBLE);
                break;
            case R.id.iv_lock_prompt:
                if (mLlUnblock.getVisibility() == View.VISIBLE) {
                    mLlUnblock.setVisibility(View.INVISIBLE);
                    mContext.startActivity(new Intent(mContext, TaskActivity.class));
                }
                break;

            default: 
                break;
        }
    }

    @Override
    public void onSearchConditionClicked() {
        mSchoolListDialog.setVisibility(View.GONE);
    }

    @Override
    public void onClickClearIcon() {
        mLvSearch.setVisibility(View.GONE);
    }

    @Override
    public void onRefreshStart() {
        searchSchollInfo(false);
    }

    @Override
    public void onRefreshEnd() {
        searchSchollInfo(true);
    }

    private void setClearBack() {
    	for (int i = 0; i < 5; i++) {
    		planetCollegeViews[i].setTitleBackground(R.drawable.bg_bt_roam_title);
    	}
    }
}
