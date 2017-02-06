package com.tjut.mianliao.job;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import android.content.Context;
import android.util.SparseArray;
import android.view.View;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.Option;

public class LocationPicker implements OnWheelChangedListener {

    private View mView;
    private WheelView mWvDistrict;
    private WheelView mWvCity;

    private SparseArray<Option> mLocations;

    public LocationPicker(Context context) {
        mView = View.inflate(context, R.layout.location_picker, null);
        mWvDistrict = (WheelView) mView.findViewById(R.id.wv_district);
        mWvCity = (WheelView) mView.findViewById(R.id.wv_city);
        mWvDistrict.addChangingListener(this);
    }

    public void setLocations(SparseArray<Option> locations) {
        mLocations = locations;
        if (locations != null && locations.size() > 0) {
            updateDistricts();
            updateCities(locations.valueAt(0));
        }
    }

    public void setLocation(Option location) {
        int size = mLocations == null ? 0 : mLocations.size();
        if (size <= 0) {
            return;
        }
        int districtId = location == null ? 0 : location.parentId;
        mWvDistrict.setCurrentItem(mLocations.indexOfKey(districtId));
    }

    public Option getLocation() {
        Option district = mLocations.valueAt(mWvDistrict.getCurrentItem());
        return district != null
                ? district.getSubOption(mWvCity.getCurrentItem()) : null;
    }

    public View getView() {
        return mView;
    }

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        updateCities(mLocations.valueAt(newValue));
    }

    private void updateDistricts() {
        int size = mLocations.size();
        Option[] districts = new Option[size];
        for (int i = 0; i < size; i++) {
            districts[i] = mLocations.valueAt(i);
        }
        ArrayWheelAdapter<Option> adapter = new ArrayWheelAdapter<Option>(
                mView.getContext(), districts);
        adapter.setItemResource(R.layout.list_item_dialog);
        mWvDistrict.setViewAdapter(adapter);
    }

    private void updateCities(Option district) {
        if (district != null && district.hasSubOptions()) {
            ArrayWheelAdapter<Option> adapter = new ArrayWheelAdapter<Option>(
                    mView.getContext(), district.subOptions);
            adapter.setItemResource(R.layout.list_item_dialog);
            mWvCity.setViewAdapter(adapter);
            int size = district.subOptions.size();
            mWvCity.setCurrentItem(Math.min(size - 1, mWvCity.getCurrentItem()));
        }
    }
}
