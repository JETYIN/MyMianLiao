package com.tjut.mianliao.im;

import java.text.DecimalFormat;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption.DrivingPolicy;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.chat.ChatActivity;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.contact.ContactUpdateCenter;
import com.tjut.mianliao.contact.ContactUpdateCenter.UpdateType;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.ChatRecord;
import com.tjut.mianliao.data.LatLngWrapper;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.LocationHelper;
import com.tjut.mianliao.util.LocationHelper.LocationObserver;
import com.tjut.mianliao.util.Utils;
import com.tjut.mianliao.xmpp.ChatHelper;
import com.tjut.mianliao.xmpp.ChatHelper.MessageReceiveListener;

public class ShareLocationActivity extends BaseActivity implements LocationObserver,
        ContactUpdateCenter.ContactObserver, MessageReceiveListener {

    public static final String EXTRAL_LOCATION = "location";

    private LocationHelper mLocationHelper;
    private static final int MAP_ZOOM = 16;

    protected static final String TAG = "ShareLocationActivity";
    private MapView mMapView;
    private LatLng mLatLngMe;
    private LatLng mLatLngOther;
    private WalkingRouteOverlay mWalkingRouteOverlay;
    private DrivingRouteOverlay mDrivingRouteOverlay;

    private RoutePlanSearch mRoutePlanSearch;
    private int mDrivingResultIndex = 0;
    private int mTotalDistance;

    private String mChatTarget;
    private ChatHelper mChatHelper;
    private int mChosedWay = 0;
    private DecimalFormat mDecimalFormat;
    private AvatarView mAvatarMe, mAvatarFriend;
    private TextView mShowDistance;
    private AccountInfo mAccountInfo;
    private UserInfo mUserInfo, mChatTargetInfo;
    private UserInfoManager mUserInfoManager;
    private String mDistanceStr;

    @Override
    protected int getLayoutResID() {
        return 0;
    }

    @Override
    protected int getBaseLayoutResID() {
        return R.layout.activity_share_location;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMapView = (MapView) findViewById(R.id.mv_map);
        BaiduMap map = mMapView.getMap();
        map.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        map.setMapStatus(MapStatusUpdateFactory.zoomTo(MAP_ZOOM));
        mWalkingRouteOverlay = new WalkingRouteOverlay(map);
        mDrivingRouteOverlay = new DrivingRouteOverlay(map);

        enableMyLocation();
        updateMyLocation();

        mUserInfoManager = UserInfoManager.getInstance(this);
        mAccountInfo = AccountInfo.getInstance(this);
        mChatTarget = getIntent().getStringExtra(ChatActivity.EXTRA_CHAT_TARGET);
        mUserInfo = mAccountInfo.getUserInfo();
        mChatTargetInfo = mUserInfoManager.getUserInfo(mChatTarget);

        mChatHelper = ChatHelper.getInstance(this);
        mChatHelper.registerReceiveListener(this);
        mRoutePlanSearch = RoutePlanSearch.newInstance();
        mRoutePlanSearch.setOnGetRoutePlanResultListener(routePlanResultListener);

        mDecimalFormat = new DecimalFormat("#.0");
        mAvatarMe = (AvatarView) findViewById(R.id.iv_avatar_me);
        mAvatarMe.setImage(mUserInfo.getAvatar(), mUserInfo.defaultAvatar());
        mAvatarFriend = (AvatarView) findViewById(R.id.iv_avatar_other);
        mAvatarFriend.setImage(mChatTargetInfo.getAvatar(), mChatTargetInfo.defaultAvatar());
        mShowDistance = (TextView) findViewById(R.id.tv_show_distance);

        findViewById(R.id.iv_sharelocation_over).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showResponsedDialog();
            }
        });

        mChatHelper.sendShareLocStart(mChatTarget);
        updatePersonsNumber();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        mLocationHelper.requestUpdates(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
        mLocationHelper.removeUpdates(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChatHelper.sendShareLocStop(mChatTarget);
        mChatHelper.unregisterReceiveListener(this);
        mLocationHelper.removeObserver(this);
        mRoutePlanSearch.setOnGetRoutePlanResultListener(null);
        mWalkingRouteOverlay.removeFromMap();
        mDrivingRouteOverlay.removeFromMap();
    }

    @Override
    public void onBackPressed() {
        showResponsedDialog();
    }

    @Override
    public void onContactsUpdated(final UpdateType type, final Object data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (type) {
                    case Unsubscribe:
                        if (mChatTarget.equals(data)) {
                            finish();
                        }
                        break;

                    case UserInfo:
                        mChatTargetInfo = mUserInfoManager.getUserInfo(mChatTarget);
                        break;

                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void onReceiveLocation() {
        updateMyLocation();
        mChatHelper.sendShareLocLatLng(mChatTarget, mLatLngMe.latitude, mLatLngMe.longitude);
        if (mChatHelper.isSharingLoc()) {
            mLatLngOther = getLatLngOther();
            choseRouteStyleByDistance(mLatLngMe, mLatLngOther);
        }
    }

    @Override
    public void onMessageReceived(final ChatRecord record) {
        if (record.isChatState) {
            return;
        }

        if (record.isTarget(mChatTarget)) {
            switch (record.type) {
                case ChatRecord.CHAT_TYPE_SHARE_REQUEST:
                case ChatRecord.CHAT_TYPE_SHARE_OVER:
                    updatePersonsNumber();
                    break;

                case ChatRecord.CHAT_TYPE_SHARE_RESPONSE:
                    mLatLngOther = getLatLngOther();
                    choseRouteStyleByDistance(mLatLngMe, mLatLngOther);
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void onMessageReceiveFailed(ChatRecord record) { }

    private void updatePersonsNumber() {
        if (mChatHelper.isSharingLoc()) {
            mAvatarFriend.setVisibility(View.VISIBLE);
            mShowDistance.setText(getDistanceString(2, null));
        } else {
            mAvatarFriend.setVisibility(View.GONE);
            mShowDistance.setText(getDistanceString(1, null));
            mWalkingRouteOverlay.removeFromMap();
            mDrivingRouteOverlay.removeFromMap();
        }
    }

    private CharSequence getDistanceString(int num, String distance) {
        StringBuilder sb = new StringBuilder(
                getString(R.string.cht_share_location_persons, num));
        if (distance != null) {
            sb.append(getString(R.string.cht_share_location_distance, distance));
        }
        return sb;
    }

    private LatLng getLatLngOther() {
        LatLngWrapper wrapper = mChatHelper.getLatLngWrapper();
        return wrapper == null ? null : wrapper.latLng;
    }

    private void choseRouteStyleByDistance(LatLng latLng1, LatLng latLng2) {
        if (latLng1 == null || latLng2 == null) {
            return;
        }
        Double distance = DistanceUtil.getDistance(latLng1, latLng2) / 1000;
        // chose route style by distance
        switch (mChosedWay) {
            case 0:
                if (distance <= 10) { // walk
                    walkSearch();
                    mChosedWay = 1;
                } else if (distance < 100) { // drive
                    drivingSearch(mDrivingResultIndex);
                    mChosedWay = 2;
                }
                break;
            case 1:
                walkSearch();
                break;
            case 2:
                drivingSearch(mDrivingResultIndex);
                break;
            default:
                break;
        }
    }

    private void showResponsedDialog() {
        new LightDialog(this).setTitleLd(R.string.cht_sharelication_dialog_title)
                .setMessage(R.string.cht_sharelication_dialog_content)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
    }

    private void enableMyLocation() {
        BaiduMap map = mMapView.getMap();
        map.setMyLocationEnabled(true);
        map.setMyLocationConfigeration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, false, null));
        mLocationHelper = LocationHelper.getInstance(this);
        mLocationHelper.addObserver(this);
        if (mSettings.allowGpsHint() && !mLocationHelper.isGpsEnabled()) {
            Utils.showGpsHintDialog(this);
        }
    }

    private void updateMyLocation() {
        BDLocation location = mLocationHelper.getCurrentLoc();
        if (location != null) {
            mLatLngMe = new LatLng(location.getLatitude(), location.getLongitude());
            updateMapCenter();
            mMapView.getMap().setMyLocationData(new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build());
        }
    }

    private void updateMapCenter() {
        if (mLatLngMe != null) {
            mMapView.getMap().setMapStatus(MapStatusUpdateFactory.newLatLng(mLatLngMe));
        }
    }

    /**
     * search drivingRoute
     */
    private void drivingSearch(int index) {
        DrivingRoutePlanOption drivingOption = new DrivingRoutePlanOption();
        drivingOption.policy(DrivingPolicy.ECAR_DIS_FIRST);
        drivingOption.from(PlanNode.withLocation(mLatLngMe));
        drivingOption.to(PlanNode.withLocation(mLatLngOther));
        mRoutePlanSearch.drivingSearch(drivingOption);
    }

    /**
     * search walkingRoute
     */
    private void walkSearch() {
        WalkingRoutePlanOption walkOption = new WalkingRoutePlanOption();
        walkOption.from(PlanNode.withLocation(mLatLngMe));
        walkOption.to(PlanNode.withLocation(mLatLngOther));
        mRoutePlanSearch.walkingSearch(walkOption);
    }

    /**
     * The route planning results callback
     */
    OnGetRoutePlanResultListener routePlanResultListener = new OnGetRoutePlanResultListener() {

        /**
         * The trail results callback
         */
        @Override
        public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
            mWalkingRouteOverlay.removeFromMap();
            mDrivingRouteOverlay.removeFromMap();
            if (walkingRouteResult == null || walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                toast(R.string.share_location_no_result);
                return;
            }
            mWalkingRouteOverlay.setData(walkingRouteResult.getRouteLines().get(mDrivingResultIndex));
            mWalkingRouteOverlay.addToMap();
            mWalkingRouteOverlay.zoomToSpan();
            mTotalDistance = walkingRouteResult.getRouteLines().get(mDrivingResultIndex).getDistance();
            if (mTotalDistance >= 1000) {
                mDistanceStr = mDecimalFormat.format(mTotalDistance / 1000) + "Km";
            } else {
                mDistanceStr = mTotalDistance + "m";
            }
            mShowDistance.setText(getDistanceString(2, mDistanceStr));
        }

        /**
         * Driving route results callback the results of the query may include multiple driving route scheme
         */
        @Override
        public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
            mWalkingRouteOverlay.removeFromMap();
            mDrivingRouteOverlay.removeFromMap();
            if (drivingRouteResult == null || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                toast(R.string.share_location_no_result);
                return;
            }
            mDrivingRouteOverlay.setData(
                    drivingRouteResult.getRouteLines().get(mDrivingResultIndex));
            mDrivingRouteOverlay.addToMap();
            mDrivingRouteOverlay.zoomToSpan();
            mTotalDistance = drivingRouteResult.getTaxiInfo().getDistance();
            if (mTotalDistance >= 1000) {
                mDistanceStr = mDecimalFormat.format(mTotalDistance / 1000) + "Km";
            } else {
                mDistanceStr = mTotalDistance + "m";
            }
            mShowDistance.setText(getDistanceString(2, mDistanceStr));
        }

        @Override
        public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
        }
    };

}
