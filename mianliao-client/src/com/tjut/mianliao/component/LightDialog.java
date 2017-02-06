package com.tjut.mianliao.component;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.tjut.mianliao.R;

/**
 * Custom dialog in order to replace AlertDialog. Because it's hard to customize AlertDialog.
 */
public class LightDialog extends Dialog {

    private Context mContext;

    private RelativeLayout mRlContent;

    private int mButtonHeight;

    private boolean mAutoDismiss = true;

    public LightDialog(Context context) {
        super(context);
        mContext = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.light_dialog);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        mButtonHeight = context.getResources().getDimensionPixelSize(R.dimen.dialog_button_height);
    }

    @Override
    public void setTitle(CharSequence title) {
        ((TextView) findViewById(R.id.tv_title)).setText(title);
    }

    public LightDialog setTitleLd(CharSequence title) {
        ((TextView) findViewById(R.id.tv_title)).setText(title);
        return this;
    }

    public LightDialog setTitleLd(int title) {
        return setTitleLd(mContext.getString(title));
    }

    public LightDialog setMessage(CharSequence message) {
        ((TextView) findViewById(R.id.tv_message)).setText(message);
        return this;
    }

    public LightDialog setMessage(int resId) {
        return setMessage(mContext.getString(resId));
    }

    public LightDialog setPositiveButton(String txt, final OnClickListener onPositiveClicked) {
        getRlContent().setPadding(0, 0, 0, mButtonHeight);
        findViewById(R.id.ll_buttons).setVisibility(View.VISIBLE);
        Button positive = (Button) findViewById(R.id.btn_positive);
        positive.setVisibility(View.VISIBLE);
        positive.setText(txt);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onPositiveClicked != null) {
                    onPositiveClicked.onClick(LightDialog.this, BUTTON_POSITIVE);
                }
                if (mAutoDismiss) {
                    dismiss();
                }
            }
        });
        return this;
    }

    public LightDialog setButtonBackground(int buttonId, int resId) {
        switch (buttonId) {
            case BUTTON_NEGATIVE:
                findViewById(R.id.btn_negative).setBackgroundResource(resId);
                break;
            case BUTTON_POSITIVE:
                findViewById(R.id.btn_positive).setBackgroundResource(resId);
                break;
            default:
                break;
        }
        return this;
    }

    public LightDialog setPositiveButton(int resId, OnClickListener onPositiveClicked) {
        return setPositiveButton(mContext.getString(resId), onPositiveClicked);
    }

    public LightDialog setNegativeButton(String txt, final OnClickListener onNegativeClicked) {
        getRlContent().setPadding(0, 0, 0, mButtonHeight);
        findViewById(R.id.ll_buttons).setVisibility(View.VISIBLE);
        Button negative = (Button) findViewById(R.id.btn_negative);
        negative.setVisibility(View.VISIBLE);
        negative.setText(txt);
        negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onNegativeClicked != null) {
                    onNegativeClicked.onClick(LightDialog.this, BUTTON_NEGATIVE);
                }
                if (mAutoDismiss) {
                    dismiss();
                }
            }
        });
        return this;
    }

    public LightDialog setNegativeButton(int resId, OnClickListener onNegativeClicked) {
        return setNegativeButton(mContext.getString(resId), onNegativeClicked);
    }

    public <T> LightDialog setItems(List<T> array, OnClickListener listener) {
        ArrayAdapter<T> adapter = new ArrayAdapter<T>(mContext, R.layout.list_item_dialog, array);
        return setAdapter(adapter, listener);
    }

    public <T> LightDialog setItems(T[] array, OnClickListener listener) {
        ArrayAdapter<T> adapter = new ArrayAdapter<T>(mContext, R.layout.list_item_dialog, array);
        return setAdapter(adapter, listener);
    }

    public LightDialog setItems(int resId, OnClickListener listener) {
        return setItems(mContext.getResources().getStringArray(resId), listener);
    }

    public LightDialog setAdapter(ListAdapter adapter, final OnClickListener listener) {
        ListView lvItems = (ListView) LayoutInflater
                .from(mContext).inflate(R.layout.dialog_list_view, null);
        lvItems.setAdapter(adapter);
        lvItems.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) {
                    listener.onClick(LightDialog.this, position);
                }
                if (mAutoDismiss) {
                    dismiss();
                }
            }
        });
        return setView(lvItems);
    }

    /**
     * @param autoDismiss If true, the dialog will be dismissed once user clicked
     *                    positive/negative button, or clicked on adapter item. Default
     *                    is true;
     */
    public LightDialog setAutoDismiss(boolean autoDismiss) {
        mAutoDismiss = autoDismiss;
        return this;
    }

    public LightDialog setView(View v) {
        RelativeLayout rlContent = getRlContent();
        rlContent.removeAllViews();
        rlContent.addView(v, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        return this;
    }

    private RelativeLayout getRlContent() {
        if (mRlContent == null) {
            mRlContent = (RelativeLayout) findViewById(R.id.rl_content);
        }
        return mRlContent;
    }
}
