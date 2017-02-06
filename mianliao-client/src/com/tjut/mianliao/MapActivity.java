package com.tjut.mianliao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.tjut.mianliao.data.LatLngWrapper;
import com.tjut.mianliao.util.LocationHelper;
import com.tjut.mianliao.util.Utils;

public class MapActivity extends BaseActivity implements LocationHelper.LocationObserver, View.OnClickListener,
        BaiduMap.OnMapStatusChangeListener {

    public static final String EXTRA_PICK_LOCATION = "pick_location";
    public static final String EXTRA_LOCATION = "location";
    public static final String LOCATION_ADDRESS = "address";

    private static final int MAP_ZOOM = 16;
    private static final int MARKER_Z_INDEX = 9;

    private MapView mMapView;
    private LocationHelper mLocationHelper;
    private LatLng mLatLng;

    private boolean mPickLocation;
    private String mLocalAddr;
    private String mLocalAddrOther;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.map, null);

        mMapView = (MapView) findViewById(R.id.mv_map);
        BaiduMap map = mMapView.getMap();
        map.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        map.setMapStatus(MapStatusUpdateFactory.zoomTo(MAP_ZOOM));

        LatLngWrapper wrapper = getIntent().getParcelableExtra(EXTRA_LOCATION);
        if (wrapper != null) {
            mLatLng = wrapper.latLng;
            getTitleBar().showTitleText(R.string.show_location, null);
        }
        updateMapCenter();

        mPickLocation = getIntent().getBooleanExtra(EXTRA_PICK_LOCATION, true);
        if (mPickLocation) {
            findViewById(R.id.iv_marker).setVisibility(View.VISIBLE);
            map.setOnMapStatusChangeListener(this);
        } else if (mLatLng != null) {
            map.addOverlay(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_ic_big_place))
                    .position(mLatLng)
                    .zIndex(MARKER_Z_INDEX));
        }
        enableMyLocation();
        updateMyLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        mLocationHelper.requestUpdates(true);
        if (!mPickLocation && mLatLng != null) {
            reverseGeoCode(mLatLng);
        }
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
        mMapView.onDestroy();
        mLocationHelper.removeObserver(this);
    }

    @Override
    public void onReceiveLocation() {
        updateMyLocation();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_right) {
            Intent data = new Intent();
            data.putExtra(EXTRA_LOCATION, new LatLngWrapper(mLatLng));
            data.putExtra(LOCATION_ADDRESS, mLocalAddr);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) { }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) { }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {
        mLatLng = mapStatus.target;
        reverseGeoCode(mLatLng);
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
        mLocalAddr = mLocationHelper.getCurrentLocAddress();
        if (location != null) {
            if (mPickLocation) {
                getTitleBar().showRightText(R.string.you_sure, this);
            }
            mMapView.getMap().setMyLocationData(new MyLocationData.Builder()
                        .accuracy(location.getRadius())
                        .latitude(location.getLatitude())
                        .longitude(location.getLongitude())
                        .build());
            if (mLatLng == null) {
                mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                updateMapCenter();
            }
        }
    }

    private void updateMapCenter() {
        if (mLatLng != null) {
            mMapView.getMap().setMapStatus(MapStatusUpdateFactory.newLatLng(mLatLng));
        }
    }

    /**
     * show local address
     */
    private void showAddrTextOptions(LatLng latLng) {
        mMapView.getMap().clear();
        View view = getLayoutInflater().inflate(R.layout.marker_location, null);
        TextView textView = (TextView) view.findViewById(R.id.tv_marker_txt);
        textView.setText(mLocalAddrOther);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromView(view);
        InfoWindow infoWindow = new InfoWindow(bitmapDescriptor, latLng, -47, null);
        mMapView.getMap().showInfoWindow(infoWindow);
    }

    private void reverseGeoCode(LatLng latLng) {
        // 创建地理编码检索实例
        GeoCoder geoCoder = GeoCoder.newInstance();
        OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
            // 反地理编码查询结果回调函数
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                if (result != null) {
                    mLocalAddrOther = result.getAddress();
                    mLocalAddr = result.getAddress();
                    if(!mPickLocation){
                        showAddrTextOptions(mLatLng);
                    }
                }
            }

            @Override
            public void onGetGeoCodeResult(GeoCodeResult result) {
            }

        };
        // 设置地理编码检索监听者
        geoCoder.setOnGetGeoCodeResultListener(listener);
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
    }

}
