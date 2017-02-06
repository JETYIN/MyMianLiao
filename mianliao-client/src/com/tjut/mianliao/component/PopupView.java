package com.tjut.mianliao.component;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.settings.Settings;

public class PopupView extends PopupWindow {

    private Context mContext;
    private View mView;
	private Settings mSettings;
	private LinearLayout mPopView;
	private ListView mListView;

    public PopupView(Context context) {
        super(context);
        mContext = context; 
        mView = View.inflate(context, R.layout.popup_view, null);
        mPopView =  (LinearLayout) mView.findViewById(R.id.ll_item_popup);
        mListView = (ListView) mView.findViewById(R.id.lv_popup);
        mSettings = Settings.getInstance(mContext);
        setContentView(mView);
        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable());
        setWidth(context.getResources()
                .getDimensionPixelSize(R.dimen.news_category_popup_width));
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
    }

	public PopupView setItems(int resId, final OnItemClickListener listener) {
        TypedArray ta = mContext.getResources().obtainTypedArray(resId);
        ArrayList<PopupItem> items = new ArrayList<PopupItem>();
        for (int i = 0; i < ta.length(); i += 2) {
            PopupItem item = new PopupItem();
            item.value = ta.getString(i);
            item.iconRes = ta.getResourceId(i + 1, 0);
            items.add(item);
        }
        ta.recycle();

        return setAdapter(new ArrayAdapter<PopupItem>(mContext,
                R.layout.list_item_news_category, R.id.tv_news_category, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tvCategory = (TextView) view.findViewById(R.id.tv_news_category);
                tvCategory.setCompoundDrawablesWithIntrinsicBounds(
                        getItem(position).iconRes, 0, 0, 0);
                return view;
            }
        }).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) {
                    listener.onItemClick(position,
                            (PopupItem) parent.getItemAtPosition(position));
                }
                dismiss();
            }
        });
    }

    public PopupView setAdapter(ListAdapter adapter) {
        ListView listView = (ListView) getContentView().findViewById(R.id.lv_popup);
        listView.setAdapter(adapter);
        return this;
    }

    public PopupView setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        ListView listView = (ListView) getContentView().findViewById(R.id.lv_popup);
        listView.setOnItemClickListener(listener);
        return this;
    }

    public void showAsDropDown(View anchor, boolean centerHorizontal) {
        if (centerHorizontal) {
            int xoff = (anchor.getWidth() - getWidth()) / 2;
            showAsDropDown(anchor, xoff, 0);
        } else {
            showAsDropDown(anchor);
        }
    }

    public static class PopupItem {
        public String value;
        public int iconRes;
        public int count;

        @Override
        public String toString() {
            return value;
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(int position, PopupItem item);
    }
}
