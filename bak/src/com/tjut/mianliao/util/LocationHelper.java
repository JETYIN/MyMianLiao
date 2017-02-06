package com.tjut.mianliao.util;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.LocationManager;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

public class LocationHelper {

    private static final String TAG = "LocationHelper";

    private static final String BAIDU_COOR_TYPE = "bd09ll";

    private static final int HIGHT_ACCURACY_SPAN = 1000 * 60; // 1 min.
    private static final int BATTERY_SAVING_SPAN = 1000 * 60 * 5; // 5 min.

    private static final int OUTDATE_THESHOLD = 1000 * 60 * 2; // 2 min.
    private static final int ACCURACY_THESHOLD = 200; // 200 m

    private static WeakReference<LocationHelper> sInstanceRef;

    private SimpleDateFormat mDateFormatter;
    private List<LocationObserver> mLocObservers;
    private LocationListener mLocListener;
    private LocationClient mLocClientHA;
    private LocationClient mLocClientBS;
    private LocationManager mLocManager;
    private BDLocation mCurrentLoc;

    public static synchronized LocationHelper getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        LocationHelper instance = new LocationHelper(context);
        sInstanceRef = new WeakReference<LocationHelper>(instance);
        return instance;
    }

    private LocationHelper(Context context) {
        mDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        mLocObservers = new ArrayList<LocationObserver>();
        mLocListener = new LocationListener();

        LocationClientOption optHA = new LocationClientOption();
        optHA.setLocationMode(LocationMode.Hight_Accuracy);
        optHA.setCoorType(BAIDU_COOR_TYPE);
        optHA.setScanSpan(HIGHT_ACCURACY_SPAN);
        optHA.setIsNeedAddress(true);
        mLocClientHA = new LocationClient(context.getApplicationContext(), optHA);
        mLocClientHA.registerLocationListener(mLocListener);

        LocationClientOption optBS = new LocationClientOption();
        optBS.setLocationMode(LocationMode.Battery_Saving);
        optBS.setCoorType(BAIDU_COOR_TYPE);
        optBS.setScanSpan(BATTERY_SAVING_SPAN);
        optBS.setIsNeedAddress(true);
        mLocClientBS = new LocationClient(context.getApplicationContext(), optBS);
        mLocClientBS.registerLocationListener(mLocListener);

        mLocManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void addObserver(LocationObserver observer) {
        if (observer != null && !mLocObservers.contains(observer)) {
            mLocObservers.add(observer);
        }
    }

    public void removeObserver(LocationObserver observer) {
        if (observer != null) {
            mLocObservers.remove(observer);
        }
    }

    public void requestUpdates(boolean isHightAccuracy) {
        LocationClient lc = isHightAccuracy ? mLocClientHA : mLocClientBS;
        lc.start();
        lc.requestLocation();
    }

    public void removeUpdates(boolean isHightAccuracy) {
        if (isHightAccuracy) {
            mLocClientHA.stop();
        } else {
            mLocClientBS.stop();
        }
    }

    public BDLocation getCurrentLoc() {
        return mCurrentLoc;
    }

    public String getCurrentLocAddress() {
        BDLocation loc = getCurrentLoc();
        if (loc == null) {
            return "";
        }
        return loc.getProvince() + loc.getCity() + loc.getDistrict() + loc.getStreet();
    }

    public String getCurrentLocString() {
        return new StringBuilder().append(mCurrentLoc.getLongitude())
                .append(Utils.COMMA_DELIMITER).append(mCurrentLoc.getLatitude())
                .toString();
    }

    public void clear() {
        mLocClientHA.stop();
        mLocClientHA.unRegisterLocationListener(mLocListener);
        mLocClientBS.stop();
        mLocClientBS.unRegisterLocationListener(mLocListener);
        mLocObservers.clear();
        sInstanceRef.clear();
    }

    public boolean isGpsEnabled() {
        try {
            return mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Utils.logD(TAG, e.getMessage());
        }
        return false;
    }

    /**
     * Determines whether the Location reading is better than the current one
     *
     * @param location The Location that you want to evaluate
     */
    private boolean isLocationBetter(BDLocation location) {
        if (location == null || location == mCurrentLoc) {
            // no location or same location is always worse
            return false;
        }

        if (mCurrentLoc == null) {
            // A new location is always better
            return true;
        }

        // Check whether the new location fix is newer or older
        long newTime = 0;
        try {
            newTime = mDateFormatter.parse(location.getTime()).getTime();
        } catch (ParseException e) {
            return false;
        }
        long oldTime = 0;
        try {
            oldTime = mDateFormatter.parse(mCurrentLoc.getTime()).getTime();
        } catch (ParseException e) {
            return true;
        }
        long timeDelta = newTime - oldTime;
        boolean isSignificantlyNewer = timeDelta > OUTDATE_THESHOLD;
        boolean isSignificantlyOlder = timeDelta < -OUTDATE_THESHOLD;
        boolean isNewer = timeDelta > 0;

        // If it's been more than defined outdate time since the old location,
        // use the new location because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new one is older than defined outdate time, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getRadius() - mCurrentLoc.getRadius());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > ACCURACY_THESHOLD;

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate
                && location.getLocType() == mCurrentLoc.getLocType()) {
            return true;
        }
        return false;
    }

    private boolean isLocationError(BDLocation location) {
        if (location == null) {
            return true;
        }

        int type = location.getLocType();
        return type != BDLocation.TypeNetWorkLocation && type != BDLocation.TypeGpsLocation
                && type != BDLocation.TypeOffLineLocation;
    }

    public interface LocationObserver {
        void onReceiveLocation();
    }

    private class LocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (!isLocationError(location) && isLocationBetter(location)) {
                mCurrentLoc = location;
            }
            if (mCurrentLoc != null) {
                for (LocationObserver observer : mLocObservers) {
                    observer.onReceiveLocation();
                }
            }
        }
    }
}
