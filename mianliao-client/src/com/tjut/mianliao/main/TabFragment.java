package com.tjut.mianliao.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Toast;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.NaviButton;
import com.tjut.mianliao.component.TitleBar;
import com.tjut.mianliao.util.TrackingUtil;
import com.tjut.mianliao.util.Utils;

public abstract class TabFragment extends Fragment {

    private int mIdentity = -1;
    protected TitleBar mTitleBar;
    protected boolean mIsFocused = false;
    protected NaviButton mNaviButton;
    protected LayoutInflater mInflater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInflater = LayoutInflater.from(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        NaviButton button = (NaviButton) getActivity().findViewById(getNaviButtonId());
        if (button != null) {
            button.setRefTab(this);
            mIsFocused = button.isSelected();
            mNaviButton = button;
        }

        View view = inflater.inflate(this.isTitleShow() ?
                R.layout.main_tab_base : R.layout.main_tab_base_no_title, container, false);
        ViewStub vsContent = (ViewStub) view.findViewById(R.id.vs_content);
        vsContent.setLayoutResource(getLayoutId());
        vsContent.inflate();
        mTitleBar = (TitleBar) view.findViewById(R.id.rl_title_bar);

        return view;
    }

    public boolean isTitleShow()
    {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsFocused) {
            onFocus();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mIsFocused) {
            onDeFocus();
        } else {
            System.gc();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onFocus() {
        TrackingUtil.trackBeginPage(getActivity(), getName());
    }

    public void onDeFocus() {
        TrackingUtil.trackEndPage(getActivity(), getName());
    }

    public void onTabButtonClicked() {}

    public void setFocus(boolean isFocused) {
        if (isFocused) {
            onFocus();
        } else {
            onDeFocus();
        }
        mIsFocused = isFocused;
    }

    public void toast(int resId) {
        Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT).show();
    }

    public void toast(CharSequence text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        getActivity().overridePendingTransition(R.anim.activity_slide_right_in,
                R.anim.activity_slide_left_out);
    }

    protected void startActivity(Class<?> cls) {
        startActivity(new Intent(getActivity(), cls));
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        getActivity().overridePendingTransition(R.anim.activity_slide_right_in,
                R.anim.activity_slide_left_out);
    }

    protected void startActivityForResult(Class<?> cls, int requestCode) {
        startActivityForResult(new Intent(getActivity(), cls), requestCode);
    }

    public abstract int getLayoutId();

    public abstract int getNaviButtonId();

    public abstract String getName();

    public int getIdentity() {
        if (mIdentity == -1) {
            mIdentity = Utils.generateIdentify(getName());
        }
        return mIdentity;
    }
}
