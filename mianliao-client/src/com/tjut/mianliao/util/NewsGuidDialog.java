package com.tjut.mianliao.util;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.DataHelper;

public class NewsGuidDialog extends Dialog implements OnClickListener {

    private SharedPreferences mPreferences;
    private ImageView mIvGuid;
    private int[] mImagRes;
    private String mSpKey;
    private int mOffset;

    public NewsGuidDialog(Context context, int theme) {
        super(context, theme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_news_dialog);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        mPreferences = DataHelper.getSpForData(context);
        mIvGuid = (ImageView) findViewById(R.id.iv_guid_pic);
        mIvGuid.setOnClickListener(this);
    }

    public void showGuidImage(int[] imgRes, String spKey) {
        if (imgRes == null || imgRes.length <= 0) {
            return;
        }
        mImagRes = imgRes;
        mSpKey = spKey;
        mOffset = 0;
        mIvGuid.setBackgroundResource(mImagRes[0]);
        show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_guid_pic) {
            if (mOffset < mImagRes.length - 1) {
                mIvGuid.setBackgroundResource(mImagRes[++mOffset]);
            } else {
                saveDataToSp();
                hide();
            }
        }
    }

    private  void saveDataToSp() {
        Editor editor = mPreferences.edit();
        editor.putBoolean(mSpKey, false);
        editor.commit();
    }

}
