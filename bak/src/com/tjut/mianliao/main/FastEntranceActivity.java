package com.tjut.mianliao.main;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.tjut.mianliao.ChooseFastMenuActivity;
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.R;
import com.tjut.mianliao.curriculum.CurriculumActivity;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.FastEntranceInfo;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.explore.DressUpMallActivty;
import com.tjut.mianliao.forum.nova.NormalPostActivity;
import com.tjut.mianliao.im.AvatarMarketActivity;
import com.tjut.mianliao.mycollege.TakeNoticesActivity;
import com.tjut.mianliao.profile.SignInActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.ScreenShotTool;
import com.umeng.analytics.MobclickAgent;

/**
 * This Activity need fastBlur project
 * @author YoopWu
 *
 */
public class FastEntranceActivity extends Activity implements OnItemClickListener {

    public static final String EXT_LOAD_DEFAULT_MENU = "load_default_menu";
    
    private static final String WEB_ACTIVITY = "activity";
    private static final String WEB_FUNNY_GAME = "funnyGame";
    private static final int REQUEST_ADD_CODE = 100;
    
    private ArrayList<FastEntranceInfo> mEntranceInfos;
    private FastEntranceInfo mInfoDefault, mInfoAdd, mInfoPost;
    
    Bitmap mBgMap = null;
    private boolean mIsNightMode;
    private FastEntranceAdapter mAdapter;
    private LayoutInflater mInflater;
    private GridView mGridView;
    private int mMenuCount;
    private boolean mLoadDefaultMenu;
    
    
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        mIsNightMode = Settings.getInstance(this).isNightMode();
        mInflater = LayoutInflater.from(this);
        mLoadDefaultMenu = getIntent().getBooleanExtra(EXT_LOAD_DEFAULT_MENU, true);
        mPreferences = DataHelper.getSpForData(this);

        mEntranceInfos = new ArrayList<>();
        fillData();
        mBgMap = ScreenShotTool.snapShotBitmap;
        if (mBgMap == null) {
            mBgMap = BitmapFactory.decodeResource(getResources(),
                    mIsNightMode ?  R.drawable.more_bg_black : R.drawable.more_bg);
        }
        
//        final LayoutInflater inflater = LayoutInflater.from(this);
        final ViewGroup rootView = (ViewGroup) this.findViewById(android.R.id.content);
        setContentView(R.layout.activity_fast_entrance);
        if (Settings.getInstance(FastEntranceActivity.this).isNightMode()) {
            // judge the version_code, avoid NoSuchMethodError
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                findViewById(R.id.btn_close).setBackgroundColor(0xff1b1425);
            } else {
                findViewById(R.id.btn_close).setBackgroundDrawable(new ColorDrawable(0xff1b1425));
            }
        }
        blur(rootView);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                View view = mInflater.inflate(R.layout.fast_entrance_layout, rootView, false);
                rootView.addView(view);
                Animation translateAnimation = new TranslateAnimation(0, 0, 2000, 0);
                translateAnimation.setFillAfter(true);
                translateAnimation.setDuration(200);
                view.startAnimation(translateAnimation);
                mGridView = (GridView) view.findViewById(R.id.gv_fast_entrance);
                mAdapter = new FastEntranceAdapter();
                mGridView.setAdapter(mAdapter);
                mGridView.setOnItemClickListener(FastEntranceActivity.this);
            }
        }, 50);

    }

    private void fillData() {
        boolean setFastMenu = mPreferences.getBoolean(FastEntranceInfo.SP_SET_FAST_MENU, false);
        ArrayList<FastEntranceInfo> infos = DataHelper.loadFastEntranceInfo(FastEntranceActivity.this);
        if (infos != null && infos.size() > 0) {
            mEntranceInfos = infos;
        } else if (mLoadDefaultMenu && !setFastMenu) {
            loadDefaultMenu();
        }
        mMenuCount = mEntranceInfos.size();
        initData();
        sort(mEntranceInfos);
    }

    private void loadDefaultMenu() {
        if (mEntranceInfos == null) {
            mEntranceInfos = new ArrayList<>();
        }
        FastEntranceInfo infoDessup = new FastEntranceInfo();
        infoDessup.className = DressUpMallActivty.class.getName();
        infoDessup.name = getString(R.string.fm_dress_up);
        infoDessup.imgResBig = R.drawable.icon_mall_big;
        FastEntranceInfo infoNote = new FastEntranceInfo();
        infoNote.className = TakeNoticesActivity.class.getName();
        infoNote.name = getString(R.string.fm_note);
        infoNote.imgResBig = R.drawable.icon_notebook_big;
        FastEntranceInfo infoSignin = new FastEntranceInfo();
        infoSignin.className = SignInActivity.class.getName();
        infoSignin.name = getString(R.string.fm_sign_in);
        infoSignin.imgResBig = R.drawable.icon_sign_big;
        FastEntranceInfo infoClass = new FastEntranceInfo();
        infoClass.className = CurriculumActivity.class.getName();
        infoClass.name = getString(R.string.fm_class);
        infoClass.imgResBig = R.drawable.icon_class_big;
        mEntranceInfos.add(infoDessup);
        mEntranceInfos.add(infoNote);
        mEntranceInfos.add(infoSignin);
        mEntranceInfos.add(infoClass);
    }

    private void initData() {
        mInfoDefault = new FastEntranceInfo();
        mInfoDefault.imgResBig = R.drawable.icon_empty_big;
        mInfoDefault.name = "";
        mInfoDefault.className = "";
        mInfoAdd = new FastEntranceInfo();
        mInfoAdd.imgResBig = R.drawable.icon_change_big;
        mInfoAdd.name = getString(R.string.fm_setting);
        mInfoAdd.className = ChooseFastMenuActivity.class.getName();
        mInfoPost = new FastEntranceInfo();
        mInfoPost.imgResBig = R.drawable.icon_post_big;
        mInfoPost.name = getString(R.string.fm_post);
        mInfoPost.className = NormalPostActivity.class.getName();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void blur(View view) {
        float scaleFactor = 8;
        Bitmap overlay = Bitmap.createBitmap((int) (mBgMap.getWidth() / scaleFactor),
                (int) (mBgMap.getHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft() / scaleFactor, -view.getTop() / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(mBgMap, 0, 0, paint);
////        StackBlurManager manager = new StackBlurManager(overlay);
//
//        Bitmap bitmap = manager.process(20);
//        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
//        // judge the version_code, avoid NoSuchMethodError
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            view.setBackground(bitmapDrawable);
//        } else {
//            view.setBackgroundDrawable(bitmapDrawable);
//        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                this.finish();
                // this.overridePendingTransition(R.anim.activity_slide_stay,
                // R.anim.activity_slide_up_down);
                overridePendingTransition(R.anim.activity_slide_down_up, R.anim.activity_slide_stay);
                break;
        }
    }
    
    private class FastEntranceAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public FastEntranceInfo getItem(int position) {
            return mEntranceInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                textView = (TextView) mInflater.inflate(R.layout.list_item_fast_entrance, parent, false);
            } else {
                textView = (TextView) convertView;
            }
            
            if (textView == null) {
                return null;
            }
            
            FastEntranceInfo info = getItem(position);
            textView.setText(info.name);
            textView.setCompoundDrawablesWithIntrinsicBounds(0, info.imgResBig, 0, 0);
            textView.setTag(info);
            return textView;
        }
        
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                MobclickAgent.onEvent(this, MStaticInterface.SCHOOL);
                Intent intentPost = new Intent();
                intentPost.setClass(this, NormalPostActivity.class);
                intentPost.putExtra(Forum.INTENT_EXTRA_NAME, Forum.DEFAULT_FORUM);
                startActivity(intentPost);
                this.finish();
                break;
            default:
                FastEntranceInfo info = mEntranceInfos.get(position);
                if (info.className.length() > 15) {
                    startActivity(info.className);
                } else {
                    viewMarket(info.className);
                }
                break;
        }
    }
    
    private void startActivity(String className) {
        try {
            Class<?> cls = Class.forName(className);
            if (cls.equals(ChooseFastMenuActivity.class)) {
                Intent intent = new Intent(this, cls);
                intent.putExtra(ChooseFastMenuActivity.EXT_ENTRANCES_INFO, mEntranceInfos);
                startActivityForResult(intent, REQUEST_ADD_CODE);
            } else {
                startActivity(cls);
                finish();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        
    }
    
    private void startActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }
    
    private void viewMarket(String className) {
        switch (className) {
            case WEB_ACTIVITY:
                viewMarket(MsRequest.IMRW_ACTIVITY);
                break;
            case WEB_FUNNY_GAME:
                viewMarket(MsRequest.IMRW_GAME);
                break;
            default:
                break;
        }
    }
    
    private void viewMarket(MsRequest request) {
        Intent iMarket = new Intent(this, AvatarMarketActivity.class);
        String url = HttpUtil.getUrl(this, request, "");
        iMarket.putExtra(AvatarMarketActivity.URL, url);
        startActivity(iMarket);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_CODE && resultCode == RESULT_OK) {
            mEntranceInfos = data.getParcelableArrayListExtra(ChooseFastMenuActivity.EXT_ENTRANCES_INFO);
            mMenuCount = mEntranceInfos.size();
            sort(mEntranceInfos);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void sort(ArrayList<FastEntranceInfo> entranceInfos) {
        int size = entranceInfos == null ? 0 : entranceInfos.size();
        if (size == 0) {
            entranceInfos.add(mInfoPost);
        } else {
            entranceInfos.add(0, mInfoPost);
        }
        entranceInfos.add(mInfoAdd);
        if (size < 4) {
            for (int i = size; i < 4; i++) {
                entranceInfos.add(mInfoDefault);
            }
        }
    }
}
