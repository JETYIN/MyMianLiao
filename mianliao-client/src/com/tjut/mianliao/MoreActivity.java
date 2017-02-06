package com.tjut.mianliao;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tjut.mianliao.component.FocusDialog;

public class MoreActivity extends BaseActivity implements OnClickListener {

    private ArrayList<MoreItem> mItems;

    private ListView mLvMore;

    private MoreAdapter mAdapter;

    @Override
    protected int getLayoutResID() {
        return R.layout.main_tab_more;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(R.string.mianliao_tool);
        mItems = new ArrayList<>();
        TypedArray ta = getResources().obtainTypedArray(R.array.more_tab_items);
        int size = ta.length();
        for (int i = 0; i < size; i += 3) {
            MoreItem item = new MoreItem();
            item.itemName = ta.getString(i);
            item.resId = ta.getResourceId(i + 1, 0);
            item.clsName = ta.getString(i + 2);
            mItems.add(item);
        }
        MoreItem item = new MoreItem();
        item.itemName = getString(R.string.fm_class);
        item.itemName = getString(R.string.task_take_note);
        item.itemName = getString(R.string.more_service);
        item.resId = 0;
        mItems.add(item);
        ta.recycle();
        init();
    }

    private void init() {
        mLvMore = (ListView) findViewById(R.id.lv_more_tab);
        mAdapter = new MoreAdapter();
        mLvMore.setAdapter(mAdapter);
    }

    private class MoreAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public MoreItem getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return MoreType.values().length;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getCount() - 1) {
                return MoreType.TYPE_MORE.ordinal();
            }
            return MoreType.TYPE_ITEM.ordinal();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MoreType type = MoreType.values()[getItemViewType(position)];
            if (convertView == null) {
                convertView = inflateView(type, parent);
            }
            MoreItem item = getItem(position);
            TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
            tvName.setText(item.itemName);
            switch (type) {
                case TYPE_ITEM:
                    ImageView ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
                    Picasso.with(MoreActivity.this).load(item.resId).into(ivIcon);
                    break;
                default:
                    break;
            }
            convertView.setTag(item);
            convertView.setOnClickListener(MoreActivity.this);
            return convertView;
        }

    }

    private class MoreItem {
        public int resId;
        public String itemName;
        public String clsName;
    }

    private enum MoreType {
        TYPE_ITEM, TYPE_MORE
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_more_item:
                MoreItem item = (MoreItem) v.getTag();
                try {
                    Class<?> cls = Class.forName(item.clsName);
                    startActivity(new Intent(MoreActivity.this, cls));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.ll_content:
                showDialog();
                break;
            default:
                break;
        }
    }

    private void showDialog() {
        new FocusDialog(this)
                .setPositiveButton("", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }


    public View inflateView(MoreType type, ViewGroup parent) {
        switch (type) {
            case TYPE_ITEM:
                return mInflater.inflate(R.layout.list_item_more_tab, parent, false);
            default:
                return mInflater.inflate(R.layout.list_item_more_tab_more, parent, false);
        }
    }
}