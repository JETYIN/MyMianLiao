package com.tjut.mianliao.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.baidu.mapapi.model.LatLng;
import com.tjut.mianliao.R;

public class StaticMapView extends FrameLayout {
    private static final String MAP_URL_FORMAT = "http://api.map.baidu.com/staticimage?" +
            "center=%1$f,%2$f&width=%3$d&height=%4$d&zoom=16";
    private LatLng mLocation;

    public StaticMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.comp_static_map, this, true);
    }

    public void showMap(double longitude, double latitude) {
        showMap(new LatLng(latitude, longitude));
    }

    public void showMap(LatLng location) {
        setVisibility(VISIBLE);
        mLocation = location;
        if (getWidth() > 0 && getHeight() > 0) {
            showImage(getWidth(), getHeight());
        }
    }

    public void setMarkerResource(int resource) {
        ((ImageView) findViewById(R.id.iv_smv_marker)).setImageResource(resource);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed && mLocation != null) {
            showImage(right - left, bottom - top);
        }
    }



    private void showImage(int width, int height) {
        ProImageView piv = (ProImageView) findViewById(R.id.piv_map);
        String url = String.format(MAP_URL_FORMAT,
                mLocation.longitude, mLocation.latitude, width, height);
        piv.setImage(url, 0);
    }
}
